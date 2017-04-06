#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(){

	char filename[] = {'I','m','a','g','i','n','a','r','y'};
	char *fptr = &filename[0];

	//Imaginary is not an existing file
	int fd = open(fptr);

	printf("Attempt at opening non-existent file: \n\tFile Descriptor: %d\n\n", fd);
	
	if(fd != -1){
		printf("Failure1\n");
		exit(0);
	}

	//Create Imaginary
	fd = creat(fptr);
	printf("Attempt at creating non-existent file: \n\tFile Descriptor: %d\n\n", fd);

	if(fd == -1){
		printf("Failure2\n");
		exit(0);
	}

	//Open Imaginary, should open a second reference to fptr
	int ofd = open(fptr);
	printf("Attempt at opening existing file: \n\tFile Descriptor: %d\n\n", ofd);


	// Create Imaginary, should open a third reference to fptr
	int cfd = creat(fptr);
	printf("Attempt at creating existing file: \n\tFile Descriptor: %d\n\n", cfd);

	//Fill up local file array and try to overfill
	int i; 
	printf("\nOpen max amount of files\n\n");	
	for(i = 0; i < 17; ++i){
		printf("File Descriptor: %d\n", open(fptr));
	}	

	

	// Test unlink and close
	for(i = 2; i < 15; ++i){
		close(i);
		printf("Closing File Descriptor: %d\n", i);

	
	}
	unlink(fptr);


	//Imaginary should no longer be in the FileSystem
	int status = open(fptr);

	//If status = -1, then Imaginary does not exist which is
	//the desired outcome

	if(status != -1){
		printf("Failure3\n");
		exit(0);
	}
	else{
		printf("\nTest 1 - PASS \n");
	}


	return 0;
}
