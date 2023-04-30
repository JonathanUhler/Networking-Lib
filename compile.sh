#!/bin/bash


mkdir -p obj
rm -rf obj/*

javac -Xlint:unchecked -Xlint:deprecation -d obj $(find src -name '*.java')
