
import java.io.BufferedReader;  
import java.io.FileReader;  
import java.io.IOException;  
import java.util.List;    
import java.io.FileWriter;
import com.google.gson.Gson;  
import java.util.ArrayList; 


public class ConfigParser
{
	public static ConfigClass configParser(String fileName)
    {
        Gson gson = new Gson();  
     	  ConfigClass countryObj = null;

          try 
          {             
             System.out.println("Reading configuration from " + fileName);  
             System.out.println("----------------------------");  

             BufferedReader br = null;

             do
             {
                 br = new BufferedReader(new FileReader(fileName)); 
             }while(br==null);
                             
             countryObj = gson.fromJson(br, ConfigClass.class);  
               
          }
          catch (IOException e) 
          {  
             e.printStackTrace();  
          } 
          finally
          {
             return countryObj;
          }
    }


    public static void generateJsonFile(String fileName)
    {
          ConfigClass countryObj = new ConfigClass();  
		  countryObj.setClientHomeDir("../Client/");  

      List<Integer> monitor = new ArrayList<Integer>(); 
      monitor.add(20015);
      monitor.add(20016);
      countryObj.setListenPort(monitor);
 
		  List<String> shardHomeDir = new ArrayList<String>();  
		  shardHomeDir.add("../Shard1/");  
		  shardHomeDir.add("../Shard2/");  
		  shardHomeDir.add("../Shard3/");  	    
		  countryObj.setShardHomeDir(shardHomeDir);  

		  countryObj.setMetaData("metaData.json");


          List<String> shardIP = new ArrayList<String>(); 
		  shardIP.add("127.0.0.1");  
		  shardIP.add("127.0.0.1");  
		  shardIP.add("127.0.0.1");
		  countryObj.setShardIP(shardIP);

		  List<Integer> shardPort = new ArrayList<Integer>();
		  shardPort.add(20017);
		  shardPort.add(20018);
		  shardPort.add(20019);
		  countryObj.setShardPort(shardPort);


		  Gson gson = new Gson();  		      
		  String json = gson.toJson(countryObj);  
		    
		  try 
          {  
			   FileWriter writer = new FileWriter(fileName);  
			   writer.write(json);  
			   writer.close();  	    
		  } 
		  catch (IOException e) 
          {  
		   	   e.printStackTrace();  
		  }  

    }

}