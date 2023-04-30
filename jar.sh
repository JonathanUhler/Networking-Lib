#!/bin/bash


mkdir -p bin
rm -rf bin/*

jar cf bin/jnet.jar -C obj/ .
