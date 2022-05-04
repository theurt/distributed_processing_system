#!/bin/bash

ipAdd=$1
echo "${ipAdd}" | grep -E "\b([0-9]{1,3}\.){3}[0-9]{1,3}:[0-9]{2,4}\b" |wc -l > tmp1
echo "${ipAdd}" | grep -E "\b(localhost:[0-9]{2,4})\b" |wc -l > tmp2

isIp=$(cat tmp1)
isLocal=$(cat tmp2)
rm tmp1
rm tmp2

#Pas d'option => on utilise localhost:8080
if [ -z "$1" ]
then
        adresse="localhost:8080"

else
        if [ "${isIp}"  == "0" ] && [ "${islocal}" == "0" ]
        then
                echo "format : ./clientDeployment.sh [127.0.0.1:8080]"
                exit -1
        else
                adresse=$1
        fi
fi
#Compiler les fichiers
javac linda/*/*.java

#Lancer le serveur (le service de nommage est inclus dedans)
gnome-terminal --title="Client ${adresse}" -- bash -c "java linda.autres.Shell ${adresse}; bash"

