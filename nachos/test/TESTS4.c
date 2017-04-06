#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(){


	char fname[] = {'h','e','l','l','o','.','c','o','f','f','\0'};
	char *fptr = &fname[0];
	
	int *ar;
	printf("Exec hello.coff\n");
	int a = exec(fptr,0,null);
	join(a, ar);
	
	return 0;
}
