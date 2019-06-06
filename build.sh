#!/bin/bash

# create bin directory

if [ ! -d bin ]; then
	mkdir bin
fi

# clean up old hale.jar files if they exist

if [ -e hale.jar ]; then
	rm hale.jar
fi

if [ -e bin/hale.jar ]; then
	rm bin/hale.jar
fi

# run compiler

javac @compilerargs.txt

# create and copy the jar file

cd bin

jar cf hale.jar *

cp hale.jar ../

cd ../
