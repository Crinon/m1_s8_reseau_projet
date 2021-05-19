Avant d'utiliser ce programme, vous devez modifier le fichier hosts de votre machine, il doit comporter les lignes suivantes :
127.0.0.1     miniweb.miage
127.0.0.1     dopetrope.miage
127.0.0.1     projethtmldut.miage
127.0.0.1     verti.miage

Java doit être installé

Pour exécuter le programme, utilisez le main ou exécutez le script correspondant à votre système d'exploitation.

Accès aux sites par defaut :
http://projethtmldut.miage:4000/
http://miniweb.miage:4000/
http://verti.miage:4000/
http://dopetrope.miage:4000/

Bonus du projet choisit : listing de repertoire
Pour accéder au listing de repertoire d'un site, ajouter /help.html à la fin de l'url (exemple : http://miniweb.miage:4000/help.html)

Par défaut, le programme écoute le port 4000, il peut être modifié dans le fichier "properties" dans le dossier resources

Pour ajouter un nouveau site, il faut l'ajouter dans le fichier host de votre machine, et placer le dossier du site dans src\main\resources

Il est possible de protéger un site avec le basic HTTP authentification en placant un fichier nommé ".htpasswd" à la racine du site
Ce fichier doit contenir les informations de connexion au format username:password (un par ligne)
Le site miniweb.miage:port est protégé, vous trouverez les informations de connexion dans son fichier .htpasswd

Le programme n'écoute que sur un seul port, nous voulions avoir un port par site, mais le fichier hosts ne permet pas de diriger sur un port.
Une solution aurait été d'avoir un serveur qui écoute sur un port, et en fonction du DNS utilisé il aurait rediriger la requête vers le port du site correspondant.
A la place, notre serveur distribue directement les fichiers du site correspondant au DNS utilisé.

Toutes les fonctionnalités demandées sont implémentées