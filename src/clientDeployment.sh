#!/bin/bash

ipAdd=$2
central=$1

echo "${ipAdd}" | grep -E "\b([0-9]{1,3}\.){3}[0-9]{1,3}:[0-9]{2,4}\b" |wc -l > tmp1
echo "${ipAdd}" | grep -E "\b(localhost:[0-9]{2,4})\b" |wc -l > tmp2

isIp=$(cat tmp1)
isLocal=$(cat tmp2)
echo "${central}" | grep -E "\b(central|reparti)\b" |wc -l > tmp3

rm tmp1
rm tmp2
rm tmp3


#Pas d'option => on utilise localhost:8080 avec le mode réparti par défaut
if [ -z "$1" ]
then
        adresse="localhost:8080"
	centralBool="false"
else

	#Mode central ou réparti ?
        if [ "${central}" == "0" ]
	then
		echo "format : ./clientDeployment.sh [central|reparti] [127.0.0.1:8080]"
                exit -1
	else

		#Mode central
		if [ "${central}" == "central" ]
		then
			centralBool="true"
			if [ -z "$1" ]
			then
				echo "format : ./clientDeployment.sh central ou ./clientDeployment.sh repartie [129.0.0.1:4000]"
			fi

		#Mode réparti	
		else
			centralBool="false"
			#Parsing adresse serveur
			if [ "${isIp}"  == "0" ] && [ "${islocal}" == "0" ]
        		then
                		echo "format : ./clientDeployment.sh [central|reparti] [127.0.0.1:8080]"
                		exit -1
        		else
                		adresse=$2
			fi
        	fi
	fi
fi
#Compiler les fichiers
javac linda/*/*.java
javac linda/*/*/*.java

#Lancer le serveur (le service de nommage est inclus dedans)
gnome-terminal --title="Client ${adresse}" -- bash -c "java linda.autre.ToolSwissKnife ${centralBool} ${adresse}; bash"

