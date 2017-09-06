#This is a test file for creating client and server

# Once client has a url as argument, like 

  Examples:
www.uh.edu/landing-page-images/page-header-news-events.jpg
www.uh.edu/academics/forms/transcript-request-form-2013.pdf

  server will save the file as md5sum hashing of url


# Only sclient.c and sserver.c works. test.c is for char array testing

# Test case

	./ssever -p 51717
	./sclient -p 51717 -h localhost -u www.uh.edu/

	You can find more test cases here

	www.uh.edu/landing-page-images/page-header-news-events.jpg
	www.uh.edu/academics/forms/transcript-request-form-2013.pdf
	www.cs.uh.edu/
	www.gnu.org/software/gnugo/README.txt

# RCF messages among client, server and web

  client -> server: send url information to server

  server -> web: send GET /path/file HTTP/1.0\r\n\r\n

  web -> server: reply with headerfile + content

  server: compute size of files, and compute hashing of url stored as 
  		  filenames

  server -> client: reply with filenames and size of file as bytes# lieyushi
