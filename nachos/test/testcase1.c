#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){
	
	int location = open("hello.c");
	
	prinf("\n" + location);
	return 0;
}
