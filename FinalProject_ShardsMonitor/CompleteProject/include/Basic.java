import java.io.*;

public class Basic
{
	private String MessageType;
	private int bytesStored;

	public Basic()
	{
		MessageType = "BYTESTORED";
		bytesStored = 0;
	}

	public Basic(String type, int bytesStored)
	{
		MessageType = type;
		this.bytesStored = bytesStored;
	}

	public void setMessageType(String type)
	{
		MessageType = type;
	}

	public String getMessageType()
	{
		return MessageType;
	}

	public void setBytesStored(int bytesStored)
	{
		this.bytesStored = bytesStored;
	}

	public int getBytesStored()
	{
		return bytesStored;
	}
}