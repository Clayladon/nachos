#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(){

	char programName[256];
	char *fptr = &programName[0];
	read(0,fptr,256);


	int x = exec(fptr, 0, null);
	int xr*;
	join(x, xr);
	return 0; 
}
