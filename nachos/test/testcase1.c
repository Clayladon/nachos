#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){
	
	int location = open("hello.c");
	write(1,0,1024);
	
	return 0;
}
