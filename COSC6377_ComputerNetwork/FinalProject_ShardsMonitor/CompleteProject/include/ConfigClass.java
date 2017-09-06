
import java.util.ArrayList;  
import java.util.List; 

public class ConfigClass
{
	private String clientHomeDir;
	private List<Integer> ListenPort;
	private List<String>  shardHomeDir;
	private String metaData;
	private List<String>  shardIP;
	private List<Integer>  shardPort;


	public String getClientHomeDir()
	{
		return clientHomeDir;
	}

	public void setClientHomeDir(String clientHomeDir)
	{
		this.clientHomeDir = clientHomeDir;
	}

	public void setListenPort(List<Integer> ListenPort)
	{
		this.ListenPort = ListenPort;
	}

	public List<Integer> getListenPort()
	{
		return ListenPort;
	}

	public List<String>  getShardHomeDir()
	{
		return shardHomeDir;
	}

	public void setShardHomeDir(List<String> shardHomeDir)
	{
		this.shardHomeDir = shardHomeDir;
	}

	public String getMetaData()
	{
		return metaData;
	}

	public void setMetaData(String metaData)
	{
		this.metaData = metaData;
	}

	public List<String>  getShardIP()
	{
		return shardIP;
	}

	public void setShardIP(List<String> shardIP)
	{
		this.shardIP = shardIP;
	}

	public List<Integer>  getShardPort()
	{
		return shardPort;
	}
 
	public void setShardPort(List<Integer> shardPort)
	{
		this.shardPort = shardPort;
	}

}