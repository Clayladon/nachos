#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(){


	char fname[] = {'h','e','l','l','o','.','c','o','f','f','\0'};
	char *fptr = &fname[0];
	
	int *ar;
	int a = exec(fptr,0,null);

	int *br;
	int b = exec(fptr,0,null);
	
	int *cr;
	int c = exec(fptr,0,null);
	
	join(a, ar);
	join(b, br);
	join(c, cr);
	
	return 0;
}
