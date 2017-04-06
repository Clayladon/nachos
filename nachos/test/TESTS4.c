#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(){


	char fname[] = {'h','e','l','l','o','.','c','o','f','f','\0'};
	char *fptr = &fname[0];
	
	int *ar;
	int a = exec(fptr,0,null);

	
	return 0;
}
