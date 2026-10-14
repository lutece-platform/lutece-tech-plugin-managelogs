# Plugin Manage Logs

## English version

### Introduction
This plugin allows you to download the log files of your Lutece webapp (Log4j 2 configuration) and of additional folders from within your Lutece back-office.

### Configuration
Edit WEB-INF/conf/override/plugins/managelogs.properties file
Change values for
<ul>
<li>managelogs.limit.folder : folders from wich your are allowed to download the log files of managelogs.addlog.folder</li>
<li>managelogs.addlog.folder : additionnal log folder (like tomcat log folder) to grep log files from beyond the files written by your Lutece Log4j 2 configuration</li>
</ul>
    

### Usage
First step: Add the right "MANAGELOGS_MANAGEMENT" to your admin user

Access log managment via System>Manage log

The page lists the log files and allows you to download them

### Additional information
This version requires Lutece 7 (lutece-core 7.0.0 to 7.x, Log4j 2) and Java 11. Use the 1.0.x versions (branch develop_core6) for Lutece 6 and the 2.x versions for Lutece 8.

## French version

### Introduction
Le plugin permet de télécharger l'ensemble des logs de l'application (Lutece + tomcat) via le BO Lutece.


### Installation
Ajouter la dépendance suivante au pom.xml du site Lutece :

&lt;dependency&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;fr.paris.lutece.plugins&lt;/groupId&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;plugin-managelogs&lt;/artifactId&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;1.1.0&lt;/version&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;type&gt;lutece-plugin&lt;/type&gt;<br>
&lt;/dependency&gt;

### Configuration
Surcharger dans le répertoire "WEB-INF/conf/override" le fichier managelogs.properties

Les deux variables à minima à surcharger sont "managelogs.limit.folder" et "managelogs.addlog.folder"

Définition des variables (tous les chemins peuvent être absolus ou relatifs) :
<ul>
<li>managelogs.limit.folder : permet de restreindre l'accès aux répertoires définis ici pour le téléchargement des fichiers. Défini pour des raisons de sécurité : il s'applique aux fichiers de managelogs.addlog.folder, les fichiers de la configuration Log4j 2 étant toujours listés. Liste de répertoires séparés par ';'</li>
<li>managelogs.addlog.folder : permet d'ajouter des répertoires complémentaires (par exemple répertoire de log tomcat) pour télécharger les fichiers. Liste de répertoires séparés par ';'</li>
<li>managelogs.remove_duplicate_log_files : true/false, permet de supprimer les doublons sur la base du nom de fichier et de son contenu</li>
<li>managelogs.max_filesize_for_comparaison : taille maximale d'un fichier pour lancer la comparaison de suppression des doublons</li>
</ul>

### Utilisation
1) Activer le plugin dans la gestion des plugins du BO Lutece

2) Ajouter le droit "Gestion des logs" à l'utilisateur d'administration qui devra être habilité

3) Accéder à la page Gestion des logs (menu Système)


La page remonte les fichiers de log écrits par la configuration Log4j 2 de la webapp (WEB-INF/conf/log.properties) ainsi que les fichiers des répertoires définis par la variable managelogs.addlog.folder du plugin (dans la limite des répertoires de managelogs.limit.folder).

La modification à chaud de la configuration des logs n'existe plus depuis la version 1.1.0.


### Informations complémentaires
Le code du plugin est disponible sur github: https://github.com/lutece-platform/lutece-tech-plugin-managelogs

Cette version nécessite Lutece 7 (lutece-core 7.0.0 à 7.x, Log4j 2) et Java 11. Les versions 1.0.x (branche develop_core6) sont destinées à Lutece 6 et les versions 2.x à Lutece 8.