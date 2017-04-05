#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(){


	char fname[] = {'h','e','l','l','o','.','c','o','f','f','\0'};
	char *fptr = &fname[0];
	int num = exec(fptr,0,null);

	int status = 0;
	int *sts = &status;

	printf("\nSuccess\n");
	
	return 0;
}
