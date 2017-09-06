import java.io.*;
import java.util.Arrays;
import com.google.gson.Gson; 
//import java.util.Base64;
import java.util.UUID;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.DatatypeConverter;

public class BasetoString
{
	//private static Base64 base = new Base64();
	public static String encodeToString(byte[] input)
	{
		//String base64encodedString = Base64.getEncoder().encodeToString(input);
		String base64encodedString = DatatypeConverter.printBase64Binary(input);
		return base64encodedString;
	}


	public static byte[] decodeToByte(String input)
	{
		//byte[] array = DatatypeConverter.parseBase64Binary(input);
		byte[] array = DatatypeConverter.parseBase64Binary(input);
		return array;
	}


	public static void main(String args[])
	{
		try
		{
		 	byte[] hello = ("Hello world").getBytes("utf-8");
		 	System.out.println("Decoded is " + new String(hello, "utf-8"));
		 	String helloWorld = encodeToString(hello);
		 	System.out.println("Encoded is " + helloWorld);
		 	System.out.println("Decoded is " + new String(decodeToByte(helloWorld)));

		 	int[] num = new int[]{30, 20, 15, 32};
		 	int[] index = new int[]{0,1,2,3};
		 	 int j;
		     boolean flag = true;   // set flag to true to begin first pass
		     int temp;   //holding variable

		     while ( flag )
		     {
		            flag= false;    //set flag to false awaiting a possible swap
		            for( j=0;  j < num.length -1;  j++ )
		            {
		                   if ( num[ j ] > num[j+1] )   // change to > for ascending sort
		                   {

		                   		    temp = num[ j ];                //swap elements
                           num[ j ] = num[ j+1 ];
                           num[ j+1 ] = temp;

		                           temp = index[j];
		                           index[j] = index[j+1];
		                           index[j+1] =temp;
		                          flag = true;              //shows a swap occurred  
		                  } 
		            } 
		      } 

		      for (int i=0; i<num.length; i++) 
		      {
		      		System.out.print(num[i]);
		      		System.out.print(" ");	
		      }

		       for (int i=0; i<num.length; i++) 
		      {
		      		System.out.print(index[i]);
		      		System.out.print(" ");	
		      }

		}
		catch(UnsupportedEncodingException e)
		{
         	System.out.println("Error :" + e.getMessage());
        }

	}


}
