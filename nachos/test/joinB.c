#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char *argv[]){

	int a = (int)argv[0];

	printf("B join A attempt: join(%d, r)\n", a);
	int *r = join(a, r);

	exit(127);
	return 0;
}
