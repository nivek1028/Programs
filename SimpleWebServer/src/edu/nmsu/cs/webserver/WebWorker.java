package edu.nmsu.cs.webserver;

import java.io.BufferedInputStream;

/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

	private Socket socket;
	private FileInputStream in;
	private Boolean isRoot = false;

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");
		try
		{
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			String filename = readHTTPRequest(is);
			File file = null;
			String currentdir = System.getProperty("user.dir");
			//System.out.println("dins " + (currentdir.indexOf("Server") + 5));
			currentdir = currentdir.substring(0, (currentdir.indexOf("Server")+6));
			if(filename != null) 
			{
				file = new File(currentdir + filename);
			}
			//readHTTPRequest(is);
			writeHTTPHeader(os, "text/html", file);
			writeContent(os, file);
			os.flush();
			socket.close();
		}
		catch (Exception e)
		{
			System.err.println("Output error: " + e);
		}
		System.err.println("Done handling connection.");
		return;
	}

	/**
	 * Read the HTTP request header.
	 * @return 
	 **/
	private String readHTTPRequest(InputStream is)
	{
		String line;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		String path = null;
		while (true)
		{
			try
			{
				while (!r.ready())
					Thread.sleep(1);
				line = r.readLine();
				System.err.println("Request line: (" + line + ")");
				if(line.startsWith("GET")) 
				{
					path = line;
					path = path.replaceFirst("GET ", "");
					path = path.replaceFirst("HTTP.1.1", "");
					System.err.println("The value of your line is: " + path);					
					/*
					 * try { in = new FileInputStream(path);
					 * 
					 * } catch (Exception fileNotFound) { System.out.println("This is not the way "
					 * + path); }
					 */
				}
				
				if (line.length() == 0)
					break;
			}
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;
			}
		}
		return path;
}

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType, File file) throws Exception
	{
		String checker = file.getPath();
		String current = System.getProperty("user.dir");
		//System.err.println("111111111111111" + current);
		//System.out.println("21222121 " + current + checker);
		//checker = current + checker;
		//System.out.println("111111111111111" + checker);
		int has = checker.indexOf("html");
		int gif = checker.indexOf("gif");
		int jpeg = checker.indexOf("jpg");
		int png = checker.indexOf("png");
		if(file.exists())		//checks if the file exists to then determine the content type
		{
			
			if(png != -1)		//if it exists it checks these image types
			{
				contentType = "image/png";
			}
			else if(gif != -1)
			{
				contentType = "image/gif";
			}
			else if (jpeg != -1)
			{
				contentType = "image/jpg";
			}
			else 				//if not then it sets it as an html
			{
				contentType = "text/html";
			}
		}
		System.err.println(checker);
		Date d = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		os.write("HTTP/1.1 200 OK\n".getBytes());
		os.write("Date: ".getBytes());
		os.write((df.format(d)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: Jon's very own server\n".getBytes());
		// os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
		// os.write("Content-Length: 438\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write(contentType.getBytes());		//gives it the content type
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		return;
	}


	
	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os, File file) throws Exception
	{
		Date date = new Date();
		String servername = "Kevin's Server";
		String line = "";
		String checker = file.getPath();
		int has = checker.indexOf(".html");
		int gif = checker.indexOf(".gif");
		int jpeg = checker.indexOf(".jpg");
		int png = checker.indexOf(".png");
		System.out.println(checker.length());
		String currentdir = System.getProperty("user.dir");
		currentdir = currentdir + "\\";
		//System.out.println("111111" + checker);
		//System.out.println("222222" + currentdir);
		//System.out.println(currentdir.equals(checker));

		
		  if(checker.length() <= (currentdir.length() + 1))		//69 equals to the number of characters in the path until the SimpleWebserver directory 
		  { 									
			  os.write("<html><head></head><body>\n".getBytes());
			  os.write("<h3>Welcome to the Server</h3>\n".getBytes());
			  os.write("</body>My name is SkyNet</html>\n".getBytes());
		  }
		 
		  else if(file.exists())		//if the file is found then
		  {
			  System.out.println("file exist" + file);
				  if(has != -1)				//prints the html
				  {
					  // FileReader reads text files in the default encoding.
					  FileReader fileReader = new FileReader(file.getPath());
					  // Always wrap FileReader in BufferedReader.
					  BufferedReader bufferedReader = new BufferedReader(fileReader);

					  while((line = bufferedReader.readLine()) != null) 
					  {
						  if(line.contains("<cs371date>")) 
						  {
							  line = line.replace("<cs371date>", date.toString());
						  }
						  if(line.contains("<cs371server>"))
						  {
							  line = line.replace("<cs371server>", servername);
						  }
						  os.write(line.getBytes());
						  os.write("\n".getBytes());
					  }  

					  bufferedReader.close();
				  }
				  
				  if(png != -1 || gif != -1 || jpeg != -1)		//prints the image 
				  {
					  Boolean gorge = file.exists();
					  System.out.println(gorge);
					  BufferedInputStream reader;			//the buffer reader does not work for images so we have to use inputstream
					  try 
					  {
						  reader = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));		//tells it where to get the bytes of the image
						  int info = reader.read();
						  while (info != -1)
						  {
							  os.write(info);		//writes the image
							  info = reader.read();
						  }
						  
						  reader.close();
					  }
					  catch(FileNotFoundException e)
					  {
						  e.printStackTrace();
					  }  
				  }
		  }
		  	else			//in case that the file or directory does not exist
			{													
				os.write("<html><head></head><body>\n".getBytes());	
				os.write("<h3>404: Not found.</h3>\n".getBytes());
				os.write("</body>SkyNet</html>\n".getBytes());
			}
		}
	
} // end class
