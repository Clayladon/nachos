#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char *argv[]){

	char a = argv[0];

	printf("B join A attempt: join(%c, r)\n", a);
	int *r = join(a, r);

	exit(127);
	return 0;
}
