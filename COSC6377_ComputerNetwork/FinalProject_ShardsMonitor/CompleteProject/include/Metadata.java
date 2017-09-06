import java.util.ArrayList;  
import java.util.List; 

public class Metadata
{
	public int totalByte;
	public List<Integer> storedList;
	public List<String>  fileList;
	public List<Integer> primaryFromList;
	public List<Integer> primaryToList;
	public List<Integer> firstFromList;
	public List<Integer> firstToList;
	public List<Integer> secondFromList;
	public List<Integer> secondToList;

	public Metadata()
	{
		totalByte = 0;
		storedList = new ArrayList<Integer>();
		fileList = new ArrayList<String>();
		primaryFromList = new ArrayList<Integer>();
		primaryToList = new ArrayList<Integer>();
		firstFromList = new ArrayList<Integer>();
		firstToList = new ArrayList<Integer>();
		secondFromList = new ArrayList<Integer>();
		secondToList = new ArrayList<Integer>();
	}

	public void computeTotalByte()
	{
		totalByte = 0;
		for (int i=0; i<storedList.size(); i++)
		{
			totalByte+=storedList.get(i);	
		}
	}
}
	