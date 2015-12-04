#!/bin/bash

echo "serveurs " $1
echo "q : " $1 "  " $((2*$1)) "  " $((4*$1))
TEMPS_TOTAL=0
for i in {1..5}
do
    echo $i
    ./client s ../donnees-4172.txt ../listeServeurs.txt
    #./client s ../donnees-small.txt ../listeServeurs.txt
    TEMPS=$?
    TEMPS_TOTAL=$(($TEMPS_TOTAL + $TEMPS ))
    echo "Temps dexecution: " $TEMPS
    echo "Temps total: " $TEMPS_TOTAL

    echo " "
done

./calculer_stats.sh $1
NB_ESSAIS=5
TEMPS_MOY=$(($TEMPS_TOTAL / $NB_ESSAIS ))
echo "Temps moyen: " $TEMPS_MOY

echo " "
echo " "
echo "======================================="
echo " "
echo " "
echo " "
