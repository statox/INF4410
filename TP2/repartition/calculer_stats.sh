#!/bin/bash

echo "serveur " $1
for i in {1..3}
do
    FILE=server$1$i

    P=$(cat $FILE | grep REFUS | wc -l)
    F=$(cat $FILE | grep Taux | wc -l)
    TOTAL=$(($P + $F))

    echo $FILE "  " $P "  " $F " : " $TOTAL
done

echo " "
