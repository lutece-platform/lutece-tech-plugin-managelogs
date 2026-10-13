# Plugin Manage Logs

## English version

### Introduction
This plugin lets you download the log files of the webapp from the Lutece back-office.

### Configuration
Edit WEB-INF/conf/override/plugins/managelogs.properties file
Change values for
<ul>
<li>managelogs.addlog.folder : additional log folders (like the application server log folder) to list beyond the files written by the Log4j 2 configuration of the webapp</li>
<li>managelogs.limit.folder : folders (and sub-folders) the files of managelogs.addlog.folder must lie in</li>
</ul>

### Usage
Add the right "MANAGELOGS_MANAGEMENT" to your admin user, then open the feature: it lists the files written by the
file appenders of the Log4j 2 configuration (with their rolled files) and the files of managelogs.addlog.folder.
When Log4j 2 sends its events to the application server (site built with the container-runtime profile: log4j-to-jul,
no log4j-core), the files of the server log folder (system property server.logs.dir, Open Liberty) are listed instead
of the appender files.

### Additional information
Minimum Lutece version is 8.0.0 with Java 17

## French version

### Introduction
Le plugin permet de télécharger les fichiers de log de l'application via le BO Lutece.

### Installation
Ajouter la dépendance suivante au pom.xml du site Lutece :

```xml
<dependency>
    <groupId>fr.paris.lutece.plugins</groupId>
    <artifactId>plugin-managelogs</artifactId>
    <version>2.0.0</version>
    <type>lutece-plugin</type>
</dependency>
```

### Configuration
Surcharger dans le répertoire "WEB-INF/conf/override/plugins" le fichier managelogs.properties

Définition des variables (tous les chemins peuvent être absolus ou relatifs à la webapp) :
<ul>
<li>managelogs.addlog.folder : répertoires complémentaires (par exemple le répertoire de log du serveur d'application) dont les fichiers sont proposés au téléchargement. Liste de répertoires séparés par ';'</li>
<li>managelogs.limit.folder : répertoires (et sous-répertoires) dans lesquels doivent se trouver les fichiers de managelogs.addlog.folder. Liste de répertoires séparés par ';'</li>
<li>managelogs.remove_duplicate_log_files : true/false, supprime les doublons sur la base du nom de fichier et de son contenu</li>
<li>managelogs.max_filesize_for_comparaison : taille maximale d'un fichier pour lancer la comparaison de suppression des doublons</li>
</ul>

### Utilisation
1) Ajouter le droit "MANAGELOGS_MANAGEMENT" à l'utilisateur d'administration qui devra être habilité

2) Accéder à la fonctionnalité : elle liste les fichiers écrits par les appenders fichier de la configuration Log4j 2 de
la webapp (avec leurs fichiers archivés) et les fichiers des répertoires de managelogs.addlog.folder.
Quand Log4j 2 envoie ses événements au serveur d'application (site construit avec le profil container-runtime :
log4j-to-jul, sans log4j-core), ce sont les fichiers du répertoire de logs du serveur (propriété système
server.logs.dir, Open Liberty) qui sont listés à la place de ceux des appenders.

### Informations complémentaires
Le code du plugin est disponible sur github: https://github.com/lutece-platform/lutece-tech-plugin-managelogs

La version minimum de Lutece supportée est la 8.0.0 (Java 17)
