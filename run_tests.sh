#!/bin/sh

PROGRAM=$1
USE_POC=$2

if [ "$USE_POC" = "--use-poc" ]; then
	# test program with its proof of concept test
	./bin/${PROGRAM}/passbook-san ./poc/${PROGRAM}.poc
	exit $?
else
	# fuzz program with all tests in the tests/ folder
	./bin/${PROGRAM}/passbook-san ./tests/*
	exit $?
fi
