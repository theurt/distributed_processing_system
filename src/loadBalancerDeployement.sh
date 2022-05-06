#!/bin/bash

ipAdd=$1
nServ=$2
echo "${ipAdd}" | grep -E "\b([0-9]{1,3}\.){3}[0-9]{1,3}:[0-9]{2,4}\b" |wc -l > tmp1
echo "${ipAdd}" | grep -E "\b(localhost:[0-9]{2,4})\b" |wc -l > tmp2
echo "${nServ}" | grep -E "\b([0-9]*)\b" |wc -l > tmp3


isIp=$(cat tmp1)
isLocal=$(cat tmp2)
isInt=$(cat tmp3)

rm tmp1
rm tmp2
rm tmp3

#Pas d'option => on utilise localhost:8080
if [ -z "$1" ]
then
	adresse="localhost:4040"
	nServ="12"
	flag="1"
else
	#Le paramètre 1 est-il un IP/Locahost
	if [ "${isIp}" == "0" ] && [ "${islocal}" == "0" ]
	then
		echo "format : ./multiServerDeployment.sh [127.0.0.1:8080] [n]"
		exit -1
	else
		#Le paramètre 2 est-il le nombre de serveur
		if [ "${nServ}" == "0" ] || [ -z "$2" ]
		then
			echo "format : ./multiServerDeployment.sh [127.0.0.1:8080] [n]"
                	exit -1
		else

			#Y-a-t-il une liste d'ip pour les serveurs ? 
			if [ -z "$3" ]
			then
				adresse=$1
				nbServ=$2
				flag="1"

			#Oui
			else
				#Vérifier que la liste ne contient que des IP/locahost
				i=1
				for arg in "$@"
				do
					if [ $i -eq 1 ]
					then 
						test+="$arg"
					else 
						test+=" $arg"
					fi

					if [ $i -gt 2 ]
					then       	
						echo "${arg}" | grep -E "\b([0-9]{1,3}\.){3}[0-9]{1,3}:[0-9]{2,4}\b" |wc -l > tmp1
						echo "${arg}" | grep -E "\b(localhost:[0-9]{2,4})\b" |wc -l > tmp2
						isIp=$(cat tmp1)
						isLocal=$(cat tmp2)
					
					 	#Le paramètre 1 est-il un IP/Locahost
        					if [ "${isIp}" == "0" ] && [ "${islocal}" == "0" ]
        					then
                					echo "format : ./multiServerDeployment.sh [127.0.0.1:8080] [n]"
                					exit -1
						fi
					fi
					((i=i+1))

				done
				#La liste d'IP/localhost est correcte (on tronque la fin)
				flag="0"
			fi

		fi
	fi
fi
#Compiler les fichiers
javac linda/*/*.java

if [ "${flag}" == "0" ]
then
	#Lancer le serveur (le service de nommage est inclus dedans)
	echo "$test"
	java linda.multiserver.LoadBalancer "$@"
else
	#Lancer le serveur (le service de nommage est inclus dedans)
	echo "java linda.multiserver.LoadBalancer "${adresse}" "${nServ}""
        java linda.multiserver.LoadBalancer "${adresse}" "${nServ}"

fi	
