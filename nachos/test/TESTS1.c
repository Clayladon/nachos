#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(){

	char filename[] = {'I','m','a','g','i','n','a','r','y'};
	char *fptr = &filename[0];

	//Imaginary is not an existing file
	int fd = open(fptr);
	
	if(fd != -1){
		printf("Failure1\nfd = %d", fd);
		exit(0);
	}

	//Create Imaginary
	fd = creat(fptr);

	if(fd == -1){
		printf("Failure\n");
		exit(0);
	}

	//Open Imaginary, compare File Descriptor to fd

	int ofd = open(fptr);

	printf("fd = %d\nofd = %d\n", fd, ofd);

	return 0;
}
