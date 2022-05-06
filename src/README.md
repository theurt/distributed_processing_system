ligne de commande ./serverDeployment [IP:PORT]
ex ./serverDeployment 127.0.0.1:4000
Si pas d'argument on déploie sur localhost:8080


./clientDeployment [central|reparti] [IP:PORT]
Si deploiement linda central pas besoin de deuxième argument
Si reparti est rentré comme premier argument il faut nécessairement l'IP:PORT
du serveur en deuxième arguement
Si pas d'argument on choisit un mode réparti sur localhost:8080
