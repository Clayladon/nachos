#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(){
	
	//Invalid filename
	char filename[] = {' '};
	char *fptr = &filename[0];

	//Attempt to create with invalid filename
	int fd = creat(fptr);
	printf("Attempt at creating a file with an invalid file name: \n\tFile Descriptor: %d\n", fd);
	
	if(fd != -1){
		printf("Failure1\n");
		exit(0);
	}


	//Close ofd and unlink fd/ Close both instances of the OpenFile
	int cls = close(3);
	printf("Attempt to close invalid File Descriptor: \n\tClose Return Code: %d\n", cls);

	if(cls != -1){
		printf("Failure2\n");
		exit(0);
	}

	int unlk = unlink(fptr);
	printf("Attempt to unlink non-exsitent file: \n\tUnlink Return Code: %d\n", unlk);

	if(unlk != -1){
		printf("Failure3\n");
		exit(0);
	}

	return 0;
}
