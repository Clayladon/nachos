#include "stdlib.h"
#include "syscall.h"
#include "stdio.h"

int main(){

	char fileName[] = "Imaginary";
	char *fptr = &fileName[0];
	char *argv[3] = {};

	int a = exec(fptr,0,null);

	char fileName2[] = "hello.c";
	char *fptr2 = &fileName2[0];

	int b = exec(fptr2,-1, null);
	int c = exec(fptr2,5, argv);

	join(a,null);
	join(b,null);
	join(c,null);

	printf("Trying to Exec non existent file: %d\n", a);
	printf("Trying to Exec with improper argc: %d\n", b);
	printf("Trying to Exec with mismatched argc, argv: %d\n", c);

	return 0;
}

	
