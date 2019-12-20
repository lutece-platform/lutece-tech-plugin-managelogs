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
Le plugin permet de modifier la configuration log4j � chaud et de t�l�charger l'ensemble des logs de l'application (Lutece + tomcat) via le BO Lutece.


### Installation
Ajouter la d�pendance suivante au pom.xml du site Lutece :

&lt;dependency&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;fr.paris.lutece.plugins&lt;/groupId&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;plugin-managelogs&lt;/artifactId&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;1.0.0&lt;/version&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;type&gt;lutece-plugin&lt;/type&gt;<br>
&lt;/dependency&gt;

### Configuration
Surcharger dans le r�pertoire "WEB-INF/conf/override" le fichier managelogs.properties

Les deux variables � minima � surcharger sont "managelogs.limit.folder" et "managelogs.addlog.folder"

D�finition des variables (tous les chemins peuvent �tre absolus ou relatifs) :
<ul>
<li>managelogs.tmp.log.path : d�fini le chemin dans lequel le fichier de log surchargeant la configuration log4j est d�pos� (par d�faut /WEB-INF/conf/override/). Attention le chemin ne doit pas �tre /WEB-INF/conf/ et il doit exister au lancement de l'application</li>
<li>managelogs.tmp.log.filename : d�fini le nom du fichier surchargeant la configuration log4j</li>
<li>managelogs.lutece.log.path : chemin contenant la configuration par d�faut log4j de Lutece (chemin vers le fichier config.properties)</li>
<li>managelogs.lutecec.log.file : fichier par d�faut de la configuration log4j de Lutece (par d�faut config.properties)</li>
<li>managelogs.limit.folder : permet de restreindre l'acc�s aux r�pertoires d�finis ici pour le t�l�chargement des fichiers. D�fini pour des raisons de s�curit� et �viter que l'utlisateur ne puisse via la modification � chaud de la configuration des logs de t�l�charger n'importe quel fichier du serveur. Liste de r�pertoires s�par�s par ';'</li>
<li>managelogs.addlog.folder : permet d'ajouter des r�pertoires compl�mentaires (par exemple r�pertoire de log tomcat) pour t�l�charger les fichiers. Liste de r�pertoires s�par�s par ';'</li>
</ul>

### Utilisation
1) Activer le plugin dans la gestion des plugins du BO Lutece

2) Ajouter le droit "Manage Logs Properties (Niveau 0) - Manage log4j properties" � l'utilisateur d'administration qui devra �tre habilit�

3) Acc�der � la page Manage Logs Properties


Le premier onglet permet de modifier le fichier de surcharge log4j et de savoir quel fichier de configuration des logs est en cours d'utilisation

Attention la surchage log4j doit contenir l'ensemble de la configuration log4j voulue car le fichier vient se substituer enti�rement � la configuration log4j du fichier par d�faut.

Le deuxi�me onglet remonte les fichiers de log d�finis dans la configuration log4j ainsi que les fichiers d�finis par la variable managelogs.addlog.folder du plugin.


### Informations compl�mentaires
Le code du plugin est disponible sur github: https://github.com/lutece-platform/lutece-tech-plugin-managelogs

La version minimum de Lutece support�e est la 5.0.1 (java 7)