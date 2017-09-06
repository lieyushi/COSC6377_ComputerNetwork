import java.io.*;

public class FILEINFO extends DATA
{
	private int firstFrom;
	private int firstTo;
	private int secondFrom;
	private int secondTo;

	public FILEINFO()
	{
		super("FILEINFO", 0, "", -1, -1, "");
		firstFrom = firstTo = secondFrom = secondTo = -1;
	}

	public FILEINFO(String type, int stored, String fileName, int bytesFrom, int bytesTo, 
					String data, int firstFrom, int firstTo, int secondFrom, int secondTo)
	{
		super(type, stored, fileName, bytesFrom, bytesTo, data);
		this.firstFrom = firstFrom;
		this.firstTo = firstTo;
		this.secondFrom = secondFrom;
		this.secondTo = secondTo;
	}

	public void setFirstFrom(int firstFrom)
	{
		this.firstFrom = firstFrom;
	}

	public int getFirstFrom()
	{
		return firstFrom;
	}

	public void setFirstTo(int firstTo)
	{
		this.firstTo = firstTo;
	}

	public int getFirstTo()
	{
		return firstTo;
	}

	public void setSecondFrom(int secondFrom)
	{
		this.secondFrom = secondFrom;
	}

	public int getSecondFrom()
	{
		return secondFrom;
	}

	public void setSecondTo(int secondTo)
	{
		this.secondTo = secondTo;
	}

	public int getSecondTo()
	{
		return secondTo;
	}
}