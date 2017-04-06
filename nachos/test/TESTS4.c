#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(){


	char fname[] = {'h','e','l','l','o','.','c','o','f','f','\0'};
	char *fptr = &fname[0];
	
	int *ar;
	int a = exec(fptr,0,null);
	join(a, ar);

	int *br;
	int b = exec(fptr,0,null);
	join(b, br);
	
	int *cr;
	int c = exec(fptr,0,null);
	join(c, cr);
	
	return 0;
}
