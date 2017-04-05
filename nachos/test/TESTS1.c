#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(){

	char filename[] = {'I','m','a','g','i','n','a','r','y'};
	char *fptr = &filename[0];

	//Imaginary is not an existing file
	int fd = open(fptr);
	
	if(fd != -1){
		printf("Failure1\n");
		exit(0);
	}

	//Create Imaginary
	fd = creat(fptr);

	if(fd == -1){
		printf("Failure2\n");
		exit(0);
	}

	//Open Imaginary, should open a second reference to fptr
	int ofd = open(fptr);
	printf("fd = %d\nofd = %d\n", fd, ofd);

	//Close ofd and unlink fd/ Close both instances of the OpenFile
	int cls = close(ofd);
	int unlk = unlink(fptr);

	//Imaginary should no longer be in the FileSystem
	int status = open(fptr);

	//If status = -1, then Imaginary does not exist which is
	//the desired outcome

	if(status != -1)
		printf("Failure3\n");
		exit(0);



	return 0;
}
