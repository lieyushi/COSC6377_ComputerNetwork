import java.io.*;

public class BackupData extends RequestData
{
	private String data; 

	public BackupData() 
	{
		super("BACKUPDATA", 0);
		data = "";
	}

	public BackupData(String type, int stored, String fileName, int bytesFrom, int bytesTo, String data)
	{
		super(type, stored, fileName, bytesFrom, bytesTo);
		this.data = data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public String getData()
	{
		return data;
	}
}