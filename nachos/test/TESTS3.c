#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){

	char buffer[24];
	char *buf = &buffer[0];

	int bytesRead = read(4, buf, 24);

	printf("Bytes read: %d\n", bytesRead);

	char buffer2[] = {'H','E','L','L','O',' ','W','O','R','L','D','!','\n'};
	char *buf2 = &buffer2[0];

	int bytesWritten = write(4, buf2, 13);

	printf("Bytes written: %d\n", bytesWritten);

	return 0;
}
	
