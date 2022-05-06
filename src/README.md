Pour déployer linda sur un serveur unique :
./serverDeployment [IP:PORT] (argument optionnel, si absent localhsot:8080 est utilisé)
ex ./serverDeployment 127.0.0.1:4000

Pour déployer le multiserveur :
  -Cas 1 : on teste sur la même machine, dans ce cas on peut simplement lancer ./loadBalancerDeployment [IP:PORT] [NBSERVEURS]
  Si le premier parametre est précise le deuxième est obligatoire, sinon par dfaut on lance un loadBalancer sur localhost:4000
  et 10 serveurs esclaves sur localhost:8080 jusqu'à 90
  - CAs 2 : on utilise de vrais serveurs 
   Dans cette situation, il faut déployer linda sur tous les serveurs esclaves avec le script ./serverSlaveDeployment [IP:PORT] [N]
   Il faut préciser l'ip et le port du serveur et le nombre de serveurs esclaves total
   Enseiit il faut utiliser ./loadBalancerDeployment [IP:PORT] [NBSERVEURS] [IP:PORT] [IP:PORT] ...
   avec à partir du troisième argument la liste des adresses des serveurs esclaves 
   
Pour déployer un client :
./clientDeployment [central|reparti] [IP:PORT]
Si deploiement linda central pas besoin de deuxième argument
Si reparti est rentré comme premier argument il faut nécessairement l'IP:PORT
du serveur en deuxième arguement
Si pas d'argument on choisit un mode réparti sur localhost:8080
