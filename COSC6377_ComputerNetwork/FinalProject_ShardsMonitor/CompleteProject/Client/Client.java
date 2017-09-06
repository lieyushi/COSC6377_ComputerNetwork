import java.io.*;
import java.net.Socket;   
import com.google.gson.Gson; 
import java.util.ArrayList;  
import java.util.List; 
import java.util.Arrays;

public class Client {

    public static int SOCKET_PORT;  
    public static String SERVER; 
    public static int operation; 
    public static String FILE_TO_RECEIVED;
    public final static int FILE_SIZE = 6022386; 
    public static BasetoString bts = new BasetoString();
    public static Gson gson = new Gson();

    public static List<String> shardIP = new ArrayList<String>();
    public static List<Integer> shardPort = new ArrayList<Integer>();

    private static Socket[] sock;
    private static PrintWriter[] out; 
    private static BufferedReader[] br; 

    private static int SHUTDOWN = -1;
    private static Boolean NEEDBACKUP = false;

    public static void main (String[] args) throws IOException 
    {
        
          checkStatus(args);

          String conFileName = args[1];
          readConfig(conFileName);
          FILE_TO_RECEIVED = args[3];

          switch(operation)
          {
          case 1:
                download(FILE_TO_RECEIVED);
                break;

          case 2:
                upload(FILE_TO_RECEIVED);
                break;
          case 3:
                testStorage();
                break;

          default:
                System.err.println("The operation should be either -upload or -download!");
                System.exit(1);
          }

    }


    private static void checkStatus(String[] args)
    {
          if(args.length != 4)
          {
              System.err.println("Usage should be in this format:");
              System.err.println("-config configfile.json operation(-upload/download) filename!");
              System.exit(1);
          }

          else if(!args[0].equals("-config"))
          {
              System.err.println("The first argument should be -config!");
              System.exit(1);
          }

          else if(args[2].equals("-upload"))
          {
              operation = 2;
          }
          else if(args[2].equals("-download"))
          {
              operation = 1;
          }
          else if(args[2].equals("-test"))
          {
              operation = 3;
          }
          else
          {
              System.err.println("The third argument should be either -upload or -download!");
              System.exit(1);
          }
    }

    private static void readConfig(String config)
    {
          ConfigParser confPars = null;
          File f = new File(config);
          if(!(f.exists()))
              confPars.generateJsonFile(config);

          ConfigClass confi_ = confPars.configParser(config);
          shardPort = confi_.getShardPort();
          shardIP = confi_.getShardIP();

          List<Integer> temp_list = confi_.getListenPort();

          SOCKET_PORT = temp_list.get(0);
          SERVER = shardIP.get(0);

          System.out.println("reading configuration information from : " + config);
          for (int i = 0; i < shardPort.size(); i++) 
          {
              System.out.println("Shard" + Integer.toString(i+1) + " " + shardIP.get(i) + " "
              + Integer.toString(shardPort.get(i)));
          }

          System.out.println("");
    }


    private static void download(String dataFile)
    {
          initialize();

          int[][] shardStored = new int[shardPort.size()][2];
          int total = 0;
          FileOutputStream fos = null;
          BufferedOutputStream bos = null;

          try
          {

            for (int i=0; i<shardPort.size(); i++) 
            {

                  if(out[i]==null)
                  {
                      SHUTDOWN = i;
                      NEEDBACKUP = true;
                      System.out.println("shard"+Integer.toString(SHUTDOWN+1)+" is shut down!");
                      continue;
                  }
                  System.out.println("connected to shard " + Integer.toString(i+1) + " at IP address: " +
                                       shardIP.get(i) + " port: " + Integer.toString(shardPort.get(i)));
                  System.out.println("connected");
                  System.out.println("");

                  /* asking if shard has information for a specific datafile */
                  System.out.println("asking if shard"+Integer.toString(i+1)+" has "+dataFile);
                  FILEINFO rdt = new FILEINFO("FILEINFO",0, dataFile,-1,-1,"",-1,-1,-1,-1);
                  String requestStr = gson.toJson(rdt);

                  out[i].println(requestStr);

                  System.out.println("reply was:");

                  requestStr = br[i].readLine();

                  if(requestStr==null)
                  {
                      System.out.println("shard"+Integer.toString(i+1)+" is shut down!");
                      continue;
                  }

                  FILEINFO fileinfo = gson.fromJson(requestStr, FILEINFO.class);

                  if(fileinfo.getBytesStored()==0)
                  {
                       System.out.println("No bytes are found on shard"+Integer.toString(i+1)
                        +" for "+dataFile);
                       shardStored[i][0] = -1;
                       shardStored[i][1] = -1;
                  }
                  else
                  {
                    System.out.println("primary bytes ["+Integer.toString(fileinfo.getBytesFrom())
                           +","+Integer.toString(fileinfo.getBytesTo())+"] bytes of "+dataFile);
                    System.out.println("backup bytes ["+Integer.toString(fileinfo.getFirstFrom())
                           +","+Integer.toString(fileinfo.getFirstTo())+"] bytes of "+dataFile);
                    System.out.println("backup bytes ["+Integer.toString(fileinfo.getSecondFrom())
                           +","+Integer.toString(fileinfo.getSecondTo())+"] bytes of "+dataFile);
                    shardStored[i][0] = fileinfo.getBytesFrom();
                    shardStored[i][1] = fileinfo.getBytesTo();

                    if(total < fileinfo.getBytesTo())
                        total = fileinfo.getBytesTo();
                    if(total < fileinfo.getFirstTo())
                        total = fileinfo.getFirstTo();
                    if(total < fileinfo.getSecondTo())
                        total = fileinfo.getSecondTo();

                  }
            }

            if(total==0)
            {
                 System.out.println("");
                 System.out.println("All shards don't have bytes for "+dataFile);
            }

            else
            {
              byte[] origin = new byte[total+1];

              for (int i=0; i<shardPort.size(); i++) 
              {
                    if(i==SHUTDOWN)
                        continue;

                    System.out.println("downloading primary bytes ["+Integer.toString(shardStored[i][0])
                                       +","+Integer.toString(shardStored[i][1])+"] from shard"
                                       +Integer.toString(i+1));
                    RequestData rdt = new RequestData("REQUESTDATA",0,dataFile,shardStored[i][0],
                                       shardStored[i][1]);
                    String requestStr = gson.toJson(rdt);

                    out[i].println(requestStr);

                    requestStr = br[i].readLine();

                    DATA data = gson.fromJson(requestStr, DATA.class);

                    byte[] subArray = bts.decodeToByte(data.getData());
                    System.arraycopy(subArray, 0, origin, data.getBytesFrom(), data.getBytesStored());


                    if(NEEDBACKUP)
                    {
                        RequestData backup = new RequestData("REQUESTBACKUP",0,dataFile,0,0);
                        
                        requestStr = gson.toJson(backup);

                        out[i].println(requestStr);

                        requestStr = br[i].readLine();

                        BackupData data_ = gson.fromJson(requestStr, BackupData.class);

                        subArray = bts.decodeToByte(data_.getData());
                        System.arraycopy(subArray, 0, origin, data_.getBytesFrom(), data_.getBytesStored());

                        System.out.println("downloading backup bytes ["+Integer.toString(data_.getBytesFrom())
                                       +","+Integer.toString(data_.getBytesTo())+"] from shard"
                                       +Integer.toString(i+1));
                        
                        requestStr = br[i].readLine();

                        data_ = gson.fromJson(requestStr, BackupData.class);

                        subArray = bts.decodeToByte(data_.getData());
                        System.arraycopy(subArray, 0, origin, data_.getBytesFrom(), data_.getBytesStored());

                        System.out.println("downloading backup bytes ["+Integer.toString(data_.getBytesFrom())
                                       +","+Integer.toString(data_.getBytesTo())+"] from shard"
                                       +Integer.toString(i+1));               
                    }


                    System.out.println("closing connection to shard"+Integer.toString(i+1));
                    System.out.println("");
              }

              System.out.println("saving "+dataFile);

              fos = new FileOutputStream(FILE_TO_RECEIVED);
              bos = new BufferedOutputStream(fos);

              bos.write(origin, 0 , origin.length);
              bos.flush();

              if(fos!=null)
                    fos.close();
              if(bos!=null)
                    bos.close();
            }

            finishCommunication();

          }
          catch (IOException e) 
          {  
                e.printStackTrace();  
                System.exit(1); 
          }

    }


    private static void upload(String fileName)
    {
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        String reply;
        int[] byteStored = new int[shardPort.size()];
        int[] byteSend = new int[shardPort.size()];

        Basic basic = new Basic("BYTESTORED", -1);
        String bytesRequest = gson.toJson(basic);

        initialize();

        for (int i = 0; i < shardPort.size(); i++) 
        {
            try
            {
                System.out.println("connected to shard " + Integer.toString(i+1) + " at IP address: " +
                                   shardIP.get(i) + " port: " + Integer.toString(shardPort.get(i)));
                System.out.println("connected");

                out[i].println(bytesRequest);
                System.out.println("asking currently used storage");

                reply = br[i].readLine();

                checkNullString(reply);

                //System.out.println(reply);

                Basic dt = gson.fromJson(reply, Basic.class);

                byteStored[i] = dt.getBytesStored();
                
                //System.out.println(byteStored[i]);

                System.out.println("reply was " + Integer.toString(byteStored[i]) + " bytes");
                System.out.println("");
            }  
            catch (IOException e) 
            {  
                System.out.println("Error creating socket!");   
                System.exit(1);
            } 
        }

        try
        {
  
            File myFile = new File (fileName);
            byte[] mybytearray  = new byte [(int)myFile.length()];
            fis = new FileInputStream(myFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray,0,mybytearray.length);
            System.out.println("size of upload file " + Integer.toString(mybytearray.length) + " bytes");
            System.out.println("upload sizes are");

            /* split the byte array into parts that shard storage are equal */

            System.out.println(Integer.toString(byteStored[0])+" "+Integer.toString(byteStored[1])
                              +" "+Integer.toString(byteStored[2]));

            int total = byteStored[0]+byteStored[1]+byteStored[2]+mybytearray.length;
            int average = (int) Math.ceil((double)total/3);

            int[] bytesTemp = new int[shardPort.size()];
            System.arraycopy(byteStored,0,bytesTemp,0,byteStored.length);
            int[] index = new int[]{0,1,2};
            int j;
            Boolean flag = true;   // set flag to true to begin first pass
            int temp;   //holding variable

           while ( flag )
           {
                flag= false;    //set flag to false awaiting a possible swap
                for( j=0;  j < bytesTemp.length -1;  j++ )
                {
                     if ( bytesTemp[ j ] > bytesTemp[j+1] )   // change to > for ascending sort
                     {
                         temp = bytesTemp[ j ];                //swap elements
                         bytesTemp[ j ] = bytesTemp[ j+1 ];
                         bytesTemp[ j+1 ] = temp; 
                         temp = index[j];
                         index[j] = index[j+1];
                         index[j+1] =temp;
                         flag = true;              //shows a swap occurred  
                    } 
                } 
            } 

            System.out.println(Integer.toString(bytesTemp[0])+" "+Integer.toString(bytesTemp[1])
                              +" "+Integer.toString(bytesTemp[2]));

            int minToMiddle = bytesTemp[1]-bytesTemp[0];
            int minToMax = bytesTemp[2]-bytesTemp[0];
            int middleToMax = bytesTemp[2]-bytesTemp[1];
            if(minToMiddle>=mybytearray.length)
            {
                byteSend[index[2]]=4;
                byteSend[index[1]]=4;
                byteSend[index[0]]=mybytearray.length-8;
            }
            else if(mybytearray.length <= minToMax+middleToMax && mybytearray.length>minToMiddle)
            {
                byteSend[index[2]]=4;
                int half = (total-byteStored[index[2]]-2)/2;
                byteSend[index[1]]=half-byteStored[index[1]];
                byteSend[index[0]]=mybytearray.length-4-(half-byteStored[index[1]]);
            }
            else if(mybytearray.length >minToMax+middleToMax)
            {
                byteSend[0] = average-byteStored[0];
                byteSend[1] = average-byteStored[1];
                byteSend[2] = mybytearray.length-byteSend[0]-byteSend[1];
            }


            for (int i=0; i<shardPort.size(); i++) 
            {
                System.out.println("shard"+Integer.toString(i+1)+" "+Integer.toString(byteSend[i])+" bytes");          
            }
            System.out.println("");

            System.out.println(Integer.toString(byteSend[0])+" "+Integer.toString(byteSend[1])
              +" "+Integer.toString(byteSend[2])+" "+Integer.toString(mybytearray.length));


            int[] segment = new int[]{0, byteSend[0], byteSend[0]+byteSend[1], mybytearray.length};
            for (int i=0; i<shardPort.size(); i++) 
            {
                System.out.println("uploading "+Integer.toString(byteSend[i])+" bytes of "+fileName+" to"
                                  + " shard"+Integer.toString(i+1));
                byte[] subArray = Arrays.copyOfRange(mybytearray,segment[i],segment[i+1]); 
                String ar_str = bts.encodeToString(subArray);

                DATA dt = new DATA("DATA", subArray.length, fileName, 
                                        segment[i], segment[i+1]-1, ar_str);
                String send_ = gson.toJson(dt);

                out[i].println(send_);

                System.out.println("done");
                System.out.println("closing connection");

                System.out.println("closed connection");
                System.out.println("");
            }
            if(bis != null) 
                bis.close();
            if(fis != null)
                fis.close(); 

            finishCommunication();
        }
        catch (IOException e) 
        {  
            e.printStackTrace();  
            System.exit(1); 
        } 

    }

    private static void initialize()
    {
        sock = new Socket[shardPort.size()];
        out = new PrintWriter[shardPort.size()];
        br = new BufferedReader[shardPort.size()];
        for (int i = 0; i < shardPort.size(); i++) 
        {
            try
            {
                sock[i] = new Socket(shardIP.get(i), shardPort.get(i));
                out[i] = new PrintWriter(sock[i].getOutputStream(), true);
                br[i] = new BufferedReader(new InputStreamReader(sock[i].getInputStream())); 
            }  
            catch (IOException e) 
            {  
                System.out.println("Error creating socket!");  
            } 
        }

    }

    private static void checkNullString(String reply)
    {
        if(reply == null)
        {
           System.out.println("Can't receive string object!");
           System.exit(1);
        }
    }

    private static void testStorage()
    {
        while(true)
        {
            File f = new File(FILE_TO_RECEIVED);
            if(f.exists())
               upload(FILE_TO_RECEIVED);  
        }
    }

    private static void finishCommunication()
    {
      try
      {
        for (int i=0; i<3; i++) 
        {
           if(out[i]!=null)
              out[i].close();
          if(br[i]!=null)
              br[i].close();
          if(sock[i]!=null)
              sock[i].close();  
        }
      }
      catch(IOException e)
      {
         System.out.println("Error closing socket!");  
         System.exit(1);
      }
    }

}