#!/bin/bash

echo "serveurs " $1
for i in {1..10}
do
    echo $i
    ./client ns ../donnees-4172.txt
    echo " "
done

echo " "
