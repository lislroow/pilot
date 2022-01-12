#!/bin/bash

## env
. ./env.sh

if [ -e ./env.sh ]; then
  echo "exist"
else
  echo "not exist"
fi