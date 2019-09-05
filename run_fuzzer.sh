#!/bin/sh

if [ ! -d fuzzer/ ]; then
   echo "fuzzer/ directory not found here!"
   exit 1
fi
   
if [ ! -d tests/ ]; then
    mkdir -p tests/
fi

cd fuzzer/
javac Fuzzer.java

NUMFUZZ=100
FUZZER_OUTPUT=fuzz.txt
i=0;
while [ $i -lt $NUMFUZZ ]; do
      rm -f ${FUZZER_OUTPUT}
      java Fuzzer
      if [ ! -f ${FUZZER_OUTPUT} ]; then
          echo "Fuzzer did not produce output file ${FUZZER_OUTPUT}"
          exit 1
      fi
      mv ${FUZZER_OUTPUT} ../tests/fuzz-$i.txt
      let i=$i+1
done
