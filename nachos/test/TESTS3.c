#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){

	//char filename[] = {'F','i','c','t','i','o','n','a','l'};
	//char *fPtr = &filename[0];

//	int fd = open(fPtr);
//	printf("File descriptor: %d\n", fd);
	char buffer[24];
	char *buf = &buffer[0];

	int bytesRead = read(4, buf, 24);

	printf("Bytes read: %d\n", bytesRead);
	printf("Contents of text.txt:\n%s\n", buffer);

	return 0;
}
	
