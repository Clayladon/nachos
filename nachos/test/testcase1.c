#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"


char buf[1024];

int main(){
	
	int location = open("hello.c");
	
	int bytesRead = read(location, buf,1024);

	for(int i = 0; i < bytesRead; ++i){
		printf(buf[i]);
	}
	printf("\n");

	close(location);
	return 0;
}
