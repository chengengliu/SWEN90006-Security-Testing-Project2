#!/bin/bash
for i in {0..99}
do
  awk 'length > max_length { max_length = length; longest_line = $0 } END { print max_length }' ./tests/fuzz-$i.txt
done