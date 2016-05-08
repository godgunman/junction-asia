#!/usr/bin/env bash
MODEL=data/train.data.model
cd "$(dirname "${BASH_SOURCE[0]}")"/..
if [ ! -f "$MODEL" ]; then
  echo $MODEL not found.
  exit
fi
adb push $MODEL /sdcard/naruto_go
