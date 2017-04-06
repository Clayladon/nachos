#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char *argv[]){
	
	int n, i;
	for(i = 0; i < 5000; ++i){

		n = (i+3)/5 * 123;
	}

	exit(23);
	return 0;
}
