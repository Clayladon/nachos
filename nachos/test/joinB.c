#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char *argv[]){

	char argument = *argv[0];

	printf("B attempting to join non-child process A\n");

	printf("A's supposed PID: %d\n", (int)(argument));
	join((int)(argument - '0'),null);
	exit(127);
	return 0;
}
