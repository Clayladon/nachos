#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"



int main(){

	char filename[] = {'h', 'e', 'l','l','o','.','c','\0'};

	char *fptr = &filename[0];

	printf("About to Open\n");

	int location = open(fptr);
	
	printf("Opened\n");

	return 0;
}
