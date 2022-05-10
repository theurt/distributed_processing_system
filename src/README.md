# INSTRUCTIONS POUR DEPLOYER LINDA 


## Pour déployer linda sur un serveur unique 

La commande a utiliser est la suivante : 

```console 
./serverSingleDeployment [IP:PORT]  
```

IP doit etre sous le format "XXX.XXX.XXX.XXX" ou sous le format "localhost" et port un nombre entre 0 et 10000.  
Notez qu'il s'agit d'un argument optionnel et que s'il est absent on lance un localhost:8080

**En cas de déploiement sur un serveur distant** le paramètre rentré devrait etre l'adresse du serveur sur lequel vous déployer, nous 
ne sommes pas responsables en cas de non respect de cette consigne.

## Pour déployer linda sur le multiserveur
  
  * Deploiement local :  
  ```console 
  ./loadBalancerDeployment [IP:PORT] [NBSERVEURS]
  ```
  Si le premier paramètre est précisé le deuxième est obligatoire
  Si aucun paramètre n'est rentré, par défaut on lance un loadBalancer sur localhost:4040 et 10 serveurs "esclaves" de localhost:8080 jusqu'à 
  localhost:8090
  
  * Deploiement sur une machine distante :  
   Dans cette situation, il faut déployer linda sur tous les serveurs esclaves au préalable avec le script ./serverSlaveDeployment [IP:PORT] [N]
   Il faut préciser l'ip et le port du serveur esclave **et le nombre de serveurs esclaves total.**  
   **Seulement ensuite** il faut utiliser ```console ./loadBalancerDeployment [IP:PORT] [NBSERVEURS] [IP:PORT] [IP:PORT] ... ``` (liste des serveurs esclaves)
   
   * Deploiement à l'aide d'un fichier de configuration :  
   Respecter le format suivant (tout sur une même ligne) : [IP:PORT] [NBSERVEURS] [IP:PORT] [IP:PORT] ... (liste des serveurs esclaves)
   Puis rentrer ```console cat exemple.conf | ./loadBalancerDeployement.sh `xargs` ``` avec exemple.conf votre fichier
   
   **ATTENTION, il vous faut au préalable avoir utilisé le script serverSlaveDeployment comme mentionné au préalable**
   
   
## Pour déployer un client/utiliser notre fantastique outil multitâche

```console ./clientDeployment [central|reparti] [IP:PORT] ```

Le premier argument permet de demander un lancement de l'outil sur une instance locale/central de Linda.
Si reparti est rentré comme premier argument il faut nécessairement préciser le deuxième paramètre.
Si pas aucun argument on lance l'outil pour vous avec une version répartie sur un serveur localhost:8080

**MERCI D'AVOIR LU CE MINI-GUIDE.**
