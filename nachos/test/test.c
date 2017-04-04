#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"



int main(){

	char filename[] = {'h', 'e', 'l','l','o','.','c','\0'};

	char *fptr = &filename[0];

	printf("About to Open\n");

	int location = open(fptr);
	
	printf("Opened\n");

	char *buf[1024];

	int bytesRead = read(location, buf, 1024);

	int i = 0;
	for(i = 0; i < bytesRead; ++i){

		printf(buf[i]);
	}

	printf("File Read");

	return 0;
}
