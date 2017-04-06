#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(){


	char fnameA[] = {'j','o','i','n','A','.','c','o','f','f','\0'};
	char fnameB[] = {'j','o','i','n','B','.','c','o','f','f','\0'};
	char *fptrA = &fnameA[0];
	char *fptrB = &fnameB[0];
	
//	int *ar;
//	int a = exec(fptrA,0,null);
	int a = 1;
	char args[1] = {'0'};
	args[0] = (char)a;
	char *argsPtr = &args[0];	
	int b = exec(fptrB,1, argsPtr);
	int *br;

	
//	join(a, ar);
	join(b, br);

//	printf("A's PID: %d\n", a);
	printf("B's PID: %d\n", b);
	printf("B's Return Code: %d\n", *br);
	
	return 0;
}
