
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.Gson; 
import java.util.ArrayList;  
import java.util.List; 
import java.util.concurrent.Semaphore;
import java.util.Arrays;
import java.util.*;

public class Shard 
{
    public static int SOCKET_PORT;  
    public final static int FILE_SIZE = 6022386; // file size temporary hard coded
                                                 // should bigger than the file to be downloaded
    public static List<String> backupShardIP = new ArrayList<String>();
    public static List<Integer> backupShardPort = new ArrayList<Integer>();
    public static Gson gson = new Gson();
    public static BasetoString bs = new BasetoString();
    private static Semaphore mutex = new Semaphore(1);

    private static Metadata meta;
    private static String meta_path;

    /* tells it's Shard2 */
    private final static int INDEX = 3;
    private static int ENTRY = 0;


    public static void main(String[] args )throws InterruptedException, IOException
    {
        checkStatus(args);
        readConfig(args[1]);

        ServerSocket servsock = null;
        Socket sock = null;
        BufferedReader br = null; 
        PrintWriter out = null;
        try 
        {
            servsock = new ServerSocket(SOCKET_PORT);
            while (true) 
            {
              System.out.println("***  Shard"+Integer.toString(INDEX)+" waiting for requests ***");

              sock = servsock.accept();
              br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
              out = new PrintWriter(sock.getOutputStream(), true);
              String line, type;
              Basic basic;

              while((line=br.readLine())!=null)
              {

                  basic = gson.fromJson(line, Basic.class);
                  type = basic.getMessageType();

                  switch(type)
                  {
                  case "BYTESTORED":
                    {
                        System.out.println("received status query from client at IP address "
                              + sock.getRemoteSocketAddress().toString());
                        statusQuery(out);
                    }
                    break;

                  case "DATA":
                    {
                        receiveData(line);
                    }
                    break;

                  case "BACKUPDATA":
                    {
                        receiveBackup(line);
                    }
                    break;

                  case "FILEINFO":
                    {
                        sendMetaData(line, out);
                    }
                    break;

                  case "REQUESTDATA":
                    {
                        sendByteBack(line, out);
                    }
                    break;

                  case "REQUESTBACKUP":
                    {
                        sendBackup(line, out);
                    }
                    break;

                  }

              }  
            
              mutex.acquire();
              writeToMetaPath();  
              mutex.release();
          }  
      }
      finally
      {
        if (servsock != null) 
            servsock.close();
        if (sock!=null) 
            sock.close();
        if (br!=null)
            br.close();
        if (out!=null)
            out.close();

        System.out.println("Connection closed!");
      }

    }


    private static void checkStatus(String[] args)
    {
          if(args.length != 2)
          {
              System.err.println("Usage should be in this format:");
              System.err.println("-config configfile.json");
              System.exit(1);
          }

          else if(!args[0].equals("-config"))
          {
              System.err.println("The first argument should be -config!");
              System.exit(1);
          }

          String[] parts = args[1].split("[.]");
          String after = parts[1];
          if(!after.equals("json"))
          {
              System.err.println("The second argument should be a .json file!");
              System.exit(1);
          }
    }


    private static void readConfig(String config)
    {  
          ConfigParser confPars = null;
          File f = new File(config);
          if(!f.exists())
              confPars.generateJsonFile(config);

          confPars.generateJsonFile(config);
          ConfigClass confi_ = confPars.configParser(config);
          List<Integer> shardPort = confi_.getShardPort();
          List<String> shardIP = confi_.getShardIP();
          meta_path = confi_.getMetaData();

          /* get 2nd element */
          SOCKET_PORT = shardPort.get(INDEX-1);

          if(backupShardIP.isEmpty())
          {
              backupShardIP.add(shardIP.get(INDEX-3));
              backupShardIP.add(shardIP.get(INDEX-2));
          }

          if(backupShardPort.isEmpty())
          {
              /* add respective node number */
              backupShardPort.add(shardPort.get(INDEX-3));
              backupShardPort.add(shardPort.get(INDEX-2));
          }

          f = new File(meta_path);
          if(!f.exists())
          {
              meta = new Metadata();
              writeToMetaPath();
          }
          else
              readMetadata();

          System.out.println("");
          System.out.println("Shard"+Integer.toString(INDEX)+" listening on port: " + SOCKET_PORT);
          System.out.println("");

        
    }


    private static void statusQuery(PrintWriter out)
    {
          Basic basic = new Basic("BYTESTORED", meta.totalByte);
          String basic_str = gson.toJson(basic);

          out.println(basic_str);         
    }

 
    private static void receiveData(String line)
    {
          DATA data = gson.fromJson(line, DATA.class);
          System.out.println("received upload request of "+Integer.toString(data.getBytesStored())
               +" bytes for "+data.getFileName());
          System.out.println("received primary bytes ["+Integer.toString(data.getBytesFrom())
            +","+Integer.toString(data.getBytesTo())+"] for " + data.getFileName());
          String[] inputParts = data.getFileName().split("[.]");
          String primaryFile = inputParts[0]+".primary";
          try 
          {  
             File f = new File(primaryFile);
             if(f.exists())
             {

                System.out.println("Primary bytes already there!");
                meta.totalByte += data.getBytesStored();
             }

             else
             {
                 mutex.acquire();
                 int occurence = meta.fileList.indexOf(data.getFileName());
                 if(occurence==-1)
                 {
                    meta.fileList.add(data.getFileName());
                    meta.storedList.add(data.getBytesStored());
                    meta.primaryFromList.add(data.getBytesFrom());
                    meta.primaryToList.add(data.getBytesTo());
                    meta.totalByte += data.getBytesStored();
                 }
                 else
                 {
                    meta.primaryFromList.set(occurence, data.getBytesFrom());
                    meta.primaryToList.set(occurence, data.getBytesTo());
                 }

                 mutex.release();

                 FileWriter writer = new FileWriter(primaryFile);  
                 writer.write(line);  
                 writer.close(); 

                 System.out.println("saving "+ data.getFileName());  

                byte[] array = bs.decodeToByte(data.getData());

                int half = data.getBytesStored()/2;

                int[] segment = new int[]{0, half, data.getBytesStored()};
                int[] pos_ = new int[]{data.getBytesFrom(), data.getBytesFrom()+half-1, 
                                       data.getBytesFrom()+half, data.getBytesTo()};

                Socket[] sock_ = new Socket[2];
                PrintWriter[] pw_ = new PrintWriter[2];
                System.out.println("Finishing sending backup!");    

                for (int i = 0; i < backupShardPort.size(); i++) 
                {
                    sock_[i] = new Socket(backupShardIP.get(i), backupShardPort.get(i));
                    pw_[i] = new PrintWriter(sock_[i].getOutputStream(), true);
                    byte[] subArray = Arrays.copyOfRange(array,segment[i],segment[i+1]);
                    BackupData bdt = new BackupData("BACKUPDATA",subArray.length, data.getFileName(),
                      pos_[2*i], pos_[2*i+1], bs.encodeToString(subArray));
                    String bdtStr = gson.toJson(bdt);
                    pw_[i].println(bdtStr); 
                }              

                
                for (int i = 0; i<backupShardPort.size(); i++) 
                {
                    if(sock_[i]!=null)
                        sock_[i].close(); 
                    if(pw_[i]!=null)
                        pw_[i].close();
                }
                
              }
          } 
          catch (InterruptedException e)
          {
              System.out.println("mutex error!");
              System.exit(1); 
          }
          catch (IOException e) 
          {  
               e.printStackTrace();  
               System.exit(1); 
          }  
    }


    private static void  receiveBackup(String line)
    {
          BackupData bdt = gson.fromJson(line, BackupData.class);
          System.out.println("received backup bytes request for "+bdt.getFileName());
          System.out.println("received backup bytes ["+Integer.toString(bdt.getBytesFrom())
                          +","+Integer.toString(bdt.getBytesTo())+"] for "+bdt.getFileName());

          String[] parts = bdt.getFileName().split("[.]");
          String fileName_ = parts[0]+".backup"+Integer.toString(ENTRY);
          try 
          {  
             File f = new File(fileName_);
             if(f.exists())
             {
                System.out.println("Backup bytes already there!");
                meta.totalByte += bdt.getBytesStored();
             }

             else
             {
                 PrintWriter writer = new PrintWriter(fileName_); 
                 writer.print(line);  
                 writer.close();   

                 System.out.println("saving backup bytes ["+Integer.toString(bdt.getBytesFrom())
                              +","+Integer.toString(bdt.getBytesTo())+"] for "+bdt.getFileName());

                 mutex.acquire();

                 int occurence = meta.fileList.indexOf(bdt.getFileName());

                 meta.totalByte += bdt.getBytesStored();
                 if(occurence==-1)
                 {
                      meta.fileList.add(bdt.getFileName());
                      meta.storedList.add(bdt.getBytesStored());
                      if(ENTRY==0)
                      {
                          meta.firstFromList.add(bdt.getBytesFrom());
                          meta.firstToList.add(bdt.getBytesTo());
                      }
                      else if(ENTRY==1)
                      {
                          meta.secondFromList.add(bdt.getBytesFrom());
                          meta.secondToList.add(bdt.getBytesTo());
                      }
                  }
               /* file already record in the shard */
                  else
                  {
    
                       if(ENTRY==0)
                       {
                          meta.firstFromList.add(occurence, bdt.getBytesFrom());
                          meta.firstToList.add(occurence, bdt.getBytesTo());
                       }
                       else if(ENTRY==1)
                       {
                          meta.secondFromList.add(occurence, bdt.getBytesFrom());
                          meta.secondToList.add(occurence, bdt.getBytesTo());
                       }
                      
                   }

                   ENTRY++;
                   if(ENTRY>1)
                      ENTRY = 0;

                   System.out.println("updating metadata");

                   mutex.release(); 
                
              }
                

            }
            catch (InterruptedException e)
            {
                System.out.println("mutex error!");
                System.exit(1); 
            }
            catch (IOException e) 
            {  
               e.printStackTrace();  
               System.exit(1); 
          }  
    }


    private static void sendByteBack(String line, PrintWriter out)
    {
         RequestData fileinfo = gson.fromJson(line, RequestData.class);
         String[] temp = fileinfo.getFileName().split("[.]");
         String fileName_ = temp[0]+".primary";
         BufferedReader br = null;
         try
         {
             System.out.println("received download request for "+fileinfo.getFileName());
             br = new BufferedReader(new FileReader(fileName_));
             if(br!=null)
             {
               String contents = br.readLine();
               DATA dt = gson.fromJson(contents, DATA.class);
               System.out.println("we have primary bytes ["+Integer.toString(dt.getBytesFrom())
                          +","+Integer.toString(dt.getBytesTo())+"] for "+dt.getFileName());
               out.println(contents);
               System.out.println("sending primary bytes ["+Integer.toString(dt.getBytesFrom())
                          +","+Integer.toString(dt.getBytesTo())+"] to the client");
               System.out.println("done");
             }
             else
             {
                System.out.println("No primary bytes for "+fileinfo.getFileName());
             }
          }
          catch(IOException e)
          {
              System.out.println(e);
              System.exit(1); 
          }
         
    }


    private static void sendBackup(String line, PrintWriter out)
    {

         RequestData fileinfo = gson.fromJson(line, RequestData.class);
         String[] temp = fileinfo.getFileName().split("[.]");
         String fileName_ = temp[0]+".backup";
         BufferedReader[] br = new BufferedReader[2];
         try
         {
             for (int i = 0; i<2; i++) 
             {
                 System.out.println("received backup download request for "+fileinfo.getFileName());
                 br[i] = new BufferedReader(new FileReader(fileName_+Integer.toString(i)));
                 if(br[i]!=null)
                 {
                   String contents = br[i].readLine();
                   BackupData dt = gson.fromJson(contents, BackupData.class);
                   System.out.println("we have backup bytes ["+Integer.toString(dt.getBytesFrom())
                              +","+Integer.toString(dt.getBytesTo())+"] for "+dt.getFileName());
                   out.println(contents);
                   System.out.println("sending backup bytes ["+Integer.toString(dt.getBytesFrom())
                              +","+Integer.toString(dt.getBytesTo())+"] to the client");
                   System.out.println("done");
                   System.out.println("");
                 }
                 else
                 {
                    System.out.println("No backup bytes for "+fileinfo.getFileName());
                 }
             }

             if(br[0]!=null)
                  br[0].close();
              if(br[1]!=null)
                  br[1].close();
          }
          catch(IOException e)
          {
              System.out.println(e);
              System.exit(1); 
          }
    }


    private static void sendMetaData(String line, PrintWriter out)
    {
          FILEINFO fileinfo = gson.fromJson(line, FILEINFO.class);

          String fileName_ = fileinfo.getFileName();

          int position = meta.fileList.indexOf(fileName_);
          FILEINFO filo;
          if(position==-1)
          {
              filo = new FILEINFO("FILEINFO",0,fileName_,-1,-1,"",-1,-1,-1,-1);
          }
          else
          {
              filo = new FILEINFO("FILEINFO",meta.storedList.get(position), fileName_,
                     meta.primaryFromList.get(position), meta.primaryToList.get(position),
                     "",meta.firstFromList.get(position), meta.firstToList.get(position),
                     meta.secondFromList.get(position), meta.secondToList.get(position));
          }
          String filoStr = gson.toJson(filo);
          out.println(filoStr);
    }
 

    private static void readMetadata()
    {
        try 
        {             
           BufferedReader br = null;
           do
           {
               br = new BufferedReader(new FileReader(meta_path)); 
           }while(br==null);
                           
           meta = gson.fromJson(br, Metadata.class);  

           if(br!=null)
              br.close();
             
        }
        catch (IOException e) 
        {  
           e.printStackTrace(); 
           System.exit(1); 
        } 
    }


    private static void writeToMetaPath()
    {
        try
        {
          String json = gson.toJson(meta);             
           FileWriter writer = new FileWriter(meta_path);  
           writer.write(json);  
           writer.close();    
        }
        catch (IOException e) 
        {  
           e.printStackTrace(); 
           System.exit(1); 
        }    
    }
}
