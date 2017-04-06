#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char *argv[]){

	char argument = *argv[0];

	printf("Argument: %d\n", argument);
	exit(127);
	return 0;
}
