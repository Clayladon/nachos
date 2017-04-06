#include "stdlib.h"
#include "syscall.h"
#include "stdio.h"

int main(){

	char fileName[] = "Imaginary";
	char *fptr = &fileName[0];
	
	char *argv[3] = {};
	printf("Trying to Exec non existent file: %d\n", exec(fptr,0,null));
	printf("Trying to Exec with improper argc: %d\n", exec(fptr,-1,null));
	printf("Trying to Exec with mismatched argc, argv: %d\n", exec(fptr,5,argv));

	return 0;
}

	
