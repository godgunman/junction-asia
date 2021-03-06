#!/usr/bin/env bash
TMPDATA=/tmp/train.data
DATA=data/train.data
MODEL=data/train.data.model
if [ -z "$1" ]; then
  echo 'train.sh <svm data ...>'
  exit
fi
if [ -e $TMPDATA ]; then
  rm -f $TMPDATA
fi
for F in "$@"; do
  cat $F >> $TMPDATA
done
cd "$(dirname "${BASH_SOURCE[0]}")"/..
if [ ! -d data ]; then
  mkdir data
fi
mv $TMPDATA $DATA
./libsvm/svm-train $DATA
mv train.data.model $MODEL
./libsvm/svm-predict $DATA $MODEL /dev/null
