import java.io.*;
import java.net.Socket;   
import com.google.gson.Gson; 
import java.util.ArrayList;  
import java.util.List; 
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Monitor{

    public static int SOCKET_PORT;  
    public static String SERVER; 
    public final static int FILE_SIZE = 6022386; 

    public static BasetoString bts = new BasetoString();
    public static Gson gson = new Gson();

    public static List<String> shardIP = new ArrayList<String>();
    public static List<Integer> shardPort = new ArrayList<Integer>();

    private static Socket[] sock;
    private static PrintWriter[] out; 
    private static BufferedReader[] br; 

    private static PrintWriter pw;

    public static void main (String[] args) throws IOException 
    {
        
          checkStatus(args);

          String conFileName = args[1];
          readConfig(conFileName);

          long mainTime = System.currentTimeMillis()/1000;

          Basic basic = new Basic("BYTESTORED", -1);
          String bytesRequest = gson.toJson(basic);

          pw = new PrintWriter(new FileOutputStream(new File("count.csv"), true)); 

          Basic reply = null;
          String str = null;

          int stamp = 0;

          while(true)
          {
              initialize();
              askForBytes(bytesRequest, str, reply, stamp);
              stamp++;
          }
    }


    private static void checkStatus(String[] args)
    {
          if(args.length != 2)
          {
              System.err.println("Usage should be in this format:");
              System.err.println("-config configfile.json!");
              System.exit(1);
          }

          else if(!args[0].equals("-config"))
          {
              System.err.println("The first argument should be -config!");
              System.exit(1);
          }
    }

    private static void readConfig(String config)
    {

      try
      {
            ConfigParser confPars = null;

            File f = new File(config);
            if(!f.exists())
                confPars.generateJsonFile(config);

            ConfigClass confi_ = confPars.configParser(config);
            shardPort = confi_.getShardPort();
            shardIP = confi_.getShardIP();

            List<Integer> temp_list = confi_.getListenPort();

            SOCKET_PORT = temp_list.get(1);
            SERVER = shardIP.get(0);

            System.out.println("reading configuration information from : " + config);
            for (int i = 0; i < shardPort.size(); i++) 
            {
                System.out.println("Shard" + Integer.toString(i+1) + " " + shardIP.get(i) + " "
                + Integer.toString(shardPort.get(i)));
            }

            System.out.println("");

            File g = new File("count.csv");
            if(!g.exists())
            {

                pw = new PrintWriter(new FileOutputStream(new File("count.csv"), true));
                StringBuilder sb = new StringBuilder();
                sb.append("Time");
                sb.append(',');
                sb.append("Shard1");
                sb.append(',');
                sb.append("Shard2");
                sb.append(',');
                sb.append("Shard3");
                sb.append('\n');

                pw.write(sb.toString());
                pw.close();
            }
        }
        catch(IOException e)
        {
            System.exit(1);
        }

    }


    private static void askForBytes(String bytesRequest, String str, Basic reply, int stamp)
    {
          try
          {
            
            int[] temp = new int[3];

            for (int i=0; i<shardPort.size(); i++) 
            {

                  if(out[i]==null)
                  {
                      System.out.println("shard"+Integer.toString(i+1)+" is shut down!");
                      System.exit(1);
                  }
                  System.out.println("connected to shard " + Integer.toString(i+1) + " at IP address: " +
                                       shardIP.get(i) + " port: " + Integer.toString(shardPort.get(i)));
                  System.out.println("connected");
                  System.out.println("");

                  out[i].println(bytesRequest);

                  str = br[i].readLine();

                  if(str==null)
                  {
                      System.out.println("shard"+Integer.toString(i+1)+" is shut down!");
                      System.exit(1);
                  }
                  reply = gson.fromJson(str, Basic.class);

                  temp[i]=reply.getBytesStored();
            }
            pw = new PrintWriter(new FileOutputStream(new File("count.csv"), true)); 
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toString(stamp));
            sb.append(',');
            sb.append(Integer.toString(temp[0]));
            sb.append(',');
            sb.append(Integer.toString(temp[1]));
            sb.append(',');
            sb.append(Integer.toString(temp[2]));
            sb.append('\n');

            pw.write(sb.toString());
            pw.close(); 

            for (int i=0; i<shardPort.size(); i++) 
            {
                br[i].close();
                out[i].close();  
            }

            Thread.sleep(500);

          }
          catch (InterruptedException e)
          {
                e.printStackTrace();  
                System.exit(1); 
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
}