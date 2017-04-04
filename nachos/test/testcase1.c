#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"


char buf[1024];

int main(){
	int i;	
	printf("start\n");
	int location = open("hello.c");
	printf("post Open\n");
	int bytesRead = read(location, buf,1024);
	printf("Post read\n");
	for(i = 0; i < bytesRead; ++i){
		printf("a");
	}
	printf("\n");

	close(location);
	return 0;
}
