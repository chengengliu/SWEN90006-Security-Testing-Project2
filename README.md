# SWEN90006 Assignment 2 2019

Please see the assignment handout which contains all the essential
information.

Structure of this repository:

* src/original/: -  where the code for the original passbook lives
* src/vuln-1 -- src/vuln-5 - where your vulnerable versions will live
* poc/:        -  where your PoCs will live
* fuzzer/:     -  where your fuzzer will live

Pre-Included Scripts:

* Makefile         - makefile for building the C implementation etc.
* get_coverage.sh  - script to generate coverage reports
* run_fuzzer.sh    - script for running your fuzzer to generate inputs 

Vulnerable Versions (you should put your security vulnerabilities in here):

* src/vuln-1/passbook.c -- src/vuln-5/passbook.c

Proofs of Concept (PoCs that you should provide for each vulnerability):

* poc/vuln-1.poc -- poc/vuln-5.poc

