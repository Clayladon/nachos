#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){

	char filename[] = {'t','e','x','t','.','t','x','t'};
	char *fPtr = &filename[0];

	int fd = open(fPtr);
	printf("File descriptor: %i", fd);
	
	return 0;
}
	
