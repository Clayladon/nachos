#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(){


	char fnameA[] = {'j','o','i','n','A','.','c','o','f','f','\0'};
	char fnameB[] = {'j','o','i','n','B','.','c','o','f','f','\0'};
	char *fptrA = &fnameA[0];
	char *fptrB = &fnameB[0];
	
	int *ar;
	int aPID = exec(fptrA,0,null);
	join(aPID, ar);
	
//	char a = (char)aPID;
//	char *args[1];
//	args[0] = &a;

//	int bPID = exec(fptrB,1, args);
//	int *br;

	
//	join(bPID, br);

	printf("A's PID: %d\n", aPID);
//	printf("B's PID: %d\n", bPID);
	printf("A's Return Code: %d\n", *ar);
	
	return 0;
}
