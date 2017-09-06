import java.io.*;

public class DATA extends RequestData
{
	private String data; 

	public DATA() 
	{
		super("DATA", 0);
		data = "";
	}

	public DATA(String type, int stored, String fileName, int bytesFrom, int bytesTo, String data)
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