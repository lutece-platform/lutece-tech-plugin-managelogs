# Plugin Manage Logs

## English version

### Introduction
This plugin allows you to modify log4j configuration on the fly and download log files from within your Lutece back-office.

### Configuration
Edit WEB-INF/conf/override/plugins/managelogs.properties file
Change values for
<ul>
<li>managelogs.limit.folder : folders from wich your are allowed to download log files</li>
<li>managelogs.addlog.folder : additionnal log folder (like tomcat log folder) to grep log files from beyond folders declared in your Lutece log4j</li>
</ul>
    

### Usage
First step: Add the right "MANAGELOGS_MANAGEMENT" to your admin user
Access log managment via System>Manage log

First tab allows you to configure a new log4j properties file or revert to default

Second tab allows you to download log files

### Additional information
Minimum Lutece version is 5.0.1 with java 7

## French version

### Introduction
Le plugin permet de modifier la configuration log4j à chaud et de télécharger l'ensemble des logs de l'application (Lutece + tomcat) via le BO Lutece.


### Installation
Ajouter la dépendance suivante au pom.xml du site Lutece :

&lt;dependency&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;fr.paris.lutece.plugins&lt;/groupId&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;plugin-managelogs&lt;/artifactId&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;1.0.0&lt;/version&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;type&gt;lutece-plugin&lt;/type&gt;<br>
&lt;/dependency&gt;

### Configuration
Surcharger dans le répertoire "WEB-INF/conf/override" le fichier managelogs.properties

Les deux variables à minima à surcharger sont "managelogs.limit.folder" et "managelogs.addlog.folder"

Définition des variables (tous les chemins peuvent être absolus ou relatifs) :
<ul>
<li>managelogs.tmp.log.path : défini le chemin dans lequel le fichier de log surchargeant la configuration log4j est déposé (par défaut /WEB-INF/conf/override/). Attention le chemin ne doit pas être /WEB-INF/conf/ et il doit exister au lancement de l'application</li>
<li>managelogs.tmp.log.filename : défini le nom du fichier surchargeant la configuration log4j</li>
<li>managelogs.lutece.log.path : chemin contenant la configuration par défaut log4j de Lutece (chemin vers le fichier config.properties)</li>
<li>managelogs.lutecec.log.file : fichier par défaut de la configuration log4j de Lutece (par défaut config.properties)</li>
<li>managelogs.limit.folder : permet de restreindre l'accès aux répertoires définis ici pour le téléchargement des fichiers. Défini pour des raisons de sécurité et éviter que l'utlisateur ne puisse via la modification à chaud de la configuration des logs de télécharger n'importe quel fichier du serveur. Liste de répertoires séparés par ';'</li>
<li>managelogs.addlog.folder : permet d'ajouter des répertoires complémentaires (par exemple répertoire de log tomcat) pour télécharger les fichiers. Liste de répertoires séparés par ';'</li>
</ul>

### Utilisation
1) Activer le plugin dans la gestion des plugins du BO Lutece

2) Ajouter le droit "Manage Logs Properties (Niveau 0) - Manage log4j properties" à l'utilisateur d'administration qui devra être habilité

3) Accéder à la page Manage Logs Properties


Le premier onglet permet de modifier le fichier de surcharge log4j et de savoir quel fichier de configuration des logs est en cours d'utilisation

Attention la surchage log4j doit contenir l'ensemble de la configuration log4j voulue car le fichier vient se substituer entièrement à la configuration log4j du fichier par défaut.

Le deuxième onglet remonte les fichiers de log définis dans la configuration log4j ainsi que les fichiers définis par la variable managelogs.addlog.folder du plugin.


### Informations complémentaires
Le code du plugin est disponible sur github: https://github.com/lutece-platform/lutece-tech-plugin-managelogs

La version minimum de Lutece supportée est la 5.0.1 (java 7)