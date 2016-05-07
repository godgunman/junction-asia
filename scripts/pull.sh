#!/usr/bin/env bash
cd "$(dirname "${BASH_SOURCE[0]}")"/..
if [ ! -d data ]; then
  mkdir data
fi
cd data
i=0
while [ -e $i ]; do
  i=$((i+1))
done
mkdir $i && cd $i
adb pull /sdcard/naruto_go
