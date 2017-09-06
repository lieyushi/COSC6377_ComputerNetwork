/* A simple server in the internet domain using TCP
   The port number is passed as an argument 
   This version runs forever, forking off a separate 
   process for each connection. Could detect ip and download files
*/
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdbool.h>
#include <netdb.h>
#include <arpa/inet.h>


#define MAX_SIZE 0xB0000

int vacant;
unsigned long size;
void get_vacant();
char* download(const char* buffer);
void dostuff(int); /* function prototype */
void get_ip(const char* buffer)
{
    struct hostent *host = (struct hostent *)gethostbyname(buffer);
    int iplen = 15;
    char *ip = (char *)malloc(iplen+1);
    memset(ip, 0, iplen+1);
    if(inet_ntop(AF_INET,(void *)host->h_addr_list[0],ip, iplen) == NULL)
    {
       printf("Can't resolve the ip address of the host %s!\n", buffer);
       return;
    }
    printf("IP address of host %s is: %s\n", buffer, ip);
}

void error(const char *msg)
{
    perror(msg);
    exit(1);
}

int main(int argc, char *argv[])
{
     int sockfd, newsockfd, portno, pid;
     socklen_t clilen;
     struct sockaddr_in serv_addr, cli_addr;

     if (argc < 3) {
         fprintf(stderr,"ERROR, argument format is wrong!\n");
         fprintf(stderr, "The format should be like: \n");
         fprintf(stderr, "./sserver -p portno\n");
         exit(-1);
     }

     sockfd = socket(AF_INET, SOCK_STREAM, 0);
     if (sockfd < 0) 
        error("ERROR opening socket");
     printf("waiting for clients\n");
     bzero((char *) &serv_addr, sizeof(serv_addr));
     if(strcmp("-p", argv[1]))
     {
        fprintf(stderr, "The argument should be -p\n");
        exit(-1);
     }
     portno = atoi(argv[2]);
     serv_addr.sin_family = AF_INET;
     serv_addr.sin_addr.s_addr = INADDR_ANY;
     serv_addr.sin_port = htons(portno);
     if (bind(sockfd, (struct sockaddr *) &serv_addr,
              sizeof(serv_addr)) < 0) 
              error("ERROR on binding");
     listen(sockfd,5);
     clilen = sizeof(cli_addr);
     while (1) {
         newsockfd = accept(sockfd, 
               (struct sockaddr *) &cli_addr, &clilen);
         if (newsockfd < 0) 
             error("ERROR on accept");

         printf("a client has connected\n");
         pid = fork();
         if (pid < 0)
         {
             error("ERROR on fork");
             exit(-1);
           }
         if (pid == 0)  {
             close(sockfd);
             dostuff(newsockfd);
             exit(-1);
         }
         else 
             close(newsockfd);
     } /* end of while */
     close(sockfd);
     return 0; /* we never get here */
}

/******** DOSTUFF() *********************
 There is a separate instance of this function 
 for each connection.  It handles all communication
 once a connnection has been established.
 *****************************************/
void dostuff (int sock)
{
   int n;
   char buffer[256];
      
   bzero(buffer,256);
   n = read(sock,buffer,255);
   if (n < 0) 
      error("ERROR reading from socket");
   printf("received url: %s\n", buffer);

   /* gets the size (# of bytes in downloaded files) */
   char* file_ = download(buffer);

   /* pass message to sclient */
   char reply[200];
   //snprintf(file_, sizeof(file_), "%d", vacant);
   char mess[] = "I got your message\nReceived filename %s\nSize %lu\n";
   sprintf(reply, mess, file_, size);

   n = write(sock,reply,100);
   if (n < 0) 
      error("ERROR writing to socket");
   printf("sent filename to the client: %s\n", file_);
   free(file_);
   return;
}


char* download(const char* buffer)
{
   const char* port = "80";
   int sockfd, portno, n;   
   struct sockaddr_in serv_addr;
   struct hostent * server;

   portno = atoi(port);
   sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
   if (sockfd < 0) 
      error("ERROR opening socket");

   /* extract host name from passed url, like getting www.uh.edu from www.uh.edu/computerscience.txt */
   const char *pch = strstr(buffer,(const char*)("//"));
   char buffer_[100];
   if(pch-buffer>0)
      strncpy(buffer_,buffer+(pch-buffer)+2,strlen(buffer)-2);
   else
      strncpy(buffer_,buffer, strlen(buffer));

   pch = strchr(buffer_, '/');
   if(!pch)
   {
      fprintf(stderr,"ERROR, can't find '/'!");
      exit(-1);
   }
   char *host = (char*)malloc(pch-buffer_+1);
   strncpy(host, buffer_, pch-buffer_);
   //printf("%s\n", host);


   server = (struct hostent *)gethostbyname(host);

    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }


/****   Here www.gnu.org can't work for ip extraction and inet_pton function, I don't know why. If we use bcopy then everything downloading is working well   ******/    
    get_ip(host);

    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;

    bcopy((char *)server->h_addr, (char *)&serv_addr.sin_addr.s_addr, server->h_length);
    /*n = inet_pton(AF_INET, ip, (void*)(&(serv_addr.sin_addr.s_addr)));
    if(n<0)
    {
        perror("Can't set serv_addr.s_addr!\n");
        exit(-1);
    }
    else if(n == 0)
    {
        fprintf(stderr,"%s is not a valid IP address\n", ip);
        exit(-1);
    }*/
    //free(ip);
    serv_addr.sin_port = htons(portno);
    if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) 
        error("ERROR connecting");

    char get_[100], buf[100];
    strncpy(buf, pch, strlen(buffer_) - (pch-buffer_) + 1);
    const char* temp = "GET %s HTTP/1.0\r\nHost: %s\r\n\r\n";
    sprintf(get_,temp,buf,host);

    //printf("%s\n", get_);

    free(host);

    //get_vacant();

    /* use popen to create pile and use md5sum to convert the url into filenname */
    char *file_=(char*)malloc(3000);
    char readbuf[100];
    FILE *pipein_fp;
    const char* pipe_file = "echo -n %s | md5sum | cut -d ' ' -f 1";
    sprintf(readbuf, pipe_file, buffer);

    if((pipein_fp=popen(readbuf,"r"))==NULL)
    {
        perror("Error popen pipe!\n");
        exit(-1);
    }
    memset(readbuf,0,100);
    memset(file_, 0, 3000);
    char *tem = (char*)malloc(4000);
    memset(tem,0,4000);
    while(fgets(readbuf,100, pipein_fp)>0)
    {
       strcat(file_,readbuf);
    }
    char *find_endline;
    if((find_endline=strstr(file_,"\n")) != NULL)
        strncpy(tem, file_, find_endline-file_);
    else
        strncpy(tem, file_, strlen(file_));
    free(file_);
    printf("File name after md5sum is: %s\n", tem);
    //get_vacant();
    //snprintf(file_, sizeof(file_), "%d", vacant);
    FILE *pFile = fopen(tem,"a");
    if(pFile == NULL)
    {
        error("ERROR creating a file!");
        exit(-1);
    }

    size = 0;
    n = send(sockfd,get_,strlen(get_), 0);
    if(n < 0)
        error("ERROR writing to socket!");

    int bytes;
    char *reply = (char*)malloc(MAX_SIZE);
    bool find = false;
    char *header;
    while((bytes = recv(sockfd, reply, MAX_SIZE, 0))>0)
    {    
        if(!find)
        {
          header = strstr(reply,"\r\n\r\n");
          if(header-reply>=0)
          {
              bytes-=header-reply+4;
              header +=4;
              find = true;
              
          }
        }
        else
              header = reply;
        if(find)
        {
            fwrite(header, bytes,1,pFile);
            size += bytes;
        }
        memset(reply, 0, MAX_SIZE);
    }
    close(sockfd);
    fclose(pFile);
    printf("%s\n", "downloaded the page");
    printf("storing to filename: %s\n", tem);
    return tem;
}


/* gets the number from 1 which is not used in local directory */
void get_vacant()
{
   bool find = false;
   FILE *pFile;
   vacant = 1;
   while(!find)
   {
      char file_[100];
      snprintf(file_, sizeof(file_), "%d", vacant);
      pFile = fopen(file_,"r");
      if(pFile == NULL)
      {
          find = true;
      }
      else
      {
          vacant++;
      }
   }
 }
