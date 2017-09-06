#include<stdio.h>
#include<string.h>
int main()
{
/*	char *buffer = "http://www.uh.edu/landing-page-images/page-header-news-events.jpg";
    printf("buffer size is %d\n", strlen(buffer));
	char *pch = strchr(buffer, '/');
    char *host = (char*)malloc(pch-buffer+1);
    strncpy(host, buffer, pch-buffer);
	printf("The array host becomes %s\n", host);

    char *s = strstr(buffer,"I love you");
    printf("%d\n",s-buffer);
    char tmp[10];
    strncpy(tmp,buffer, 7);
    printf("%d\n",strlen(tmp));
    if(strcmp(tmp,"http://") == 0)
        printf("%s\n","they're the same!");*/

    char a[100];
    memset(a, 0, 100);
    char b[] = "I love you very much";
    char c[] = ", and do you love me?";
    strcat(a,b); strcat(a,c);
    printf("%s\n", a);
	
	return 0;
}