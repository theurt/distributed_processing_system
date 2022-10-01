#!/bin/bash

ipAdd=$1
nServ=$2
echo "${ipAdd}" | grep -E "\b([0-9]{1,3}\.){3}[0-9]{1,3}:[0-9]{2,4}\b" |wc -l > tmp1
echo "${ipAdd}" | grep -E "\b(localhost:[0-9]{2,4})\b" |wc -l > tmp2
echo "${nServ}" | grep -E "\b([0-9]*)\b" |wc -l > tmp3


isIp=$(cat tmp1)
isLocal=$(cat tmp2)
rm tmp1
rm tmp2

#Pas d'option => on utilise localhost:8080
if [ -z "$1" ]
then
	adresse="localhost:8080"

else
	if [ "${isIp}" == "0" ] && [ "${islocal}" == "0" ]
	then
		echo "format : ./serverSlaveDeployment.sh [127.0.0.1:8080] [n]"
		exit -1
	else
	 	#Le paramètre 2 est-il le nombre de serveur
                if [ "${nServ}" == "0" ] || [ -z "$2" ]
                then
                        echo "format : ./serverSlaveDeployment.sh [127.0.0.1:8080] [n]"
                        exit -1
                else	
			adresse=$1
			nServ=$2
		fi
	fi
fi
#Compiler les fichiers
javac linda/*/*.java

#Lancer le serveur (le service de nommage est inclus dedans)
gnome-terminal --title="slaveServer "${adresse}" " -- bash -c "java linda.multiserveur.LindaMultiserver "${adresse}" "${nServ}"  ; bash"
