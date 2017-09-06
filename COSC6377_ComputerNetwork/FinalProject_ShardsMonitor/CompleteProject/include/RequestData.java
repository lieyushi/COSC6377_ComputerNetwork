import java.io.*;

public class RequestData extends Basic
{
	private String fileName;
	private int bytesFrom;
	private int bytesTo;

	public RequestData()
	{
		super("REQUESTDATA",0);

		fileName = "";
		bytesFrom = bytesTo = -1;
	}

	public RequestData(String MessageType, int stored)
	{
		super(MessageType, stored);
		fileName = "";
		bytesFrom = bytesTo = -1;
	}

	public RequestData(String MessageType, int stored, String fileName, int bytesFrom, int bytesTo)
	{
		super(MessageType, stored);
		this.fileName = fileName;
		this.bytesFrom = bytesFrom;
		this.bytesTo = bytesTo;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setBytesFrom(int bytesFrom)
	{
		this.bytesFrom = bytesFrom;
	}

	public int getBytesFrom()
	{
		return bytesFrom;
	}

	public void setBytesTo(int bytesTo)
	{
		this.bytesTo = bytesTo;
	}

	public int getBytesTo()
	{
		return bytesTo;
	}
}