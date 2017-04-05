#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(){
	
	//Invalid filename
	char filename[] = {' '};
	char *fptr = &filename[0];

	//Attempt to create with invalid filename
	int fd = creat(fptr);

	if(fd == -1){
		printf("Failure1\n");
		exit(0);
	}


	//Close ofd and unlink fd/ Close both instances of the OpenFile
	int cls = close(3);

	if(cls != -1){
		printf("Failure2\n");
		exit(0);
	}

	int unlk = unlink(4);

	if(unlk != -1){
		printf("Failure3\n");
		exit(0);
	}

	return 0;
}
