/*     This sclient.c is partly derived from Sockets Tutorial C/C++ at
http://www.linuxhowtos.org/C_C++/socket.htm                */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 

void error(const char *msg)
{
    perror(msg);
    exit(0);
}

int main(int argc, char *argv[])
{
    int sockfd, portno, n;
    struct sockaddr_in serv_addr;
    struct hostent *server;

    char buffer[256];

/* This asserts the argc inappropriate case */
    if (argc < 7) {
       fprintf(stderr,"Program %s must have lacked something\n", argv[0]);
       fprintf(stderr,"The standard command will be: \n");
       fprintf(stderr,"./sclient -p portno -h hostname -u url\n");
       fprintf(stderr, "\n");
       exit(-1);
    }

    if(strcmp("-p", argv[1]))
    {
        fprintf(stderr, "argv[1] should be %s\n", "-p");
        exit(-1);
    }   
    portno = atoi(argv[2]);

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) 
        error("ERROR opening socket");

    if(strcmp("-h", argv[3]))
    {
        fprintf(stderr, "argv[3] should be %s\n", "-h");
        exit(-1);
    }   

    server = gethostbyname(argv[4]);

    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }

    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, (char *)&serv_addr.sin_addr.s_addr, server->h_length);
    serv_addr.sin_port = htons(portno);
    if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) 
        error("ERROR connecting");

    printf("connected to the server at port %d host %s\n", portno, argv[4]);
    /*bzero(buffer,256);
    fgets(buffer,255,stdin);*/
    n = write(sockfd,argv[6],strlen(argv[6]));
    if (n < 0) 
         error("ERROR writing to socket");

    printf("sent url: %s\n", argv[6]);

    bzero(buffer,256);

    n = read(sockfd,buffer,255);
    if (n < 0) 
         error("ERROR reading from socket");
    printf("%s\n", buffer);
    close(sockfd);
    return 0;
}