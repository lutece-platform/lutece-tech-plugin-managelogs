# Plugin Manage Logs

## Introduction
This plugin allow you to modify log4j configuration on the fly and download log files from within your Lutece back-office.

## Configuration
Edit WEB-INF/conf/override/plugins/managelogs.properties file
Change values for
<ul>
<li>managelogs.limit.folder : folders from wich your are allowed to download log files</li>
<li>managelogs.addlog.folder : additionnal log folder (like tomcat log folder) to grep log files from beyond folders declared in your Lutece log4j</li>
</ul>
    

## Usage
First step: Add the right "MANAGELOGS_MANAGEMENT" to your admin user
Access log managment via System>Manage log

First tab allows you to configure a new log4j properties file or revert to default
Second tabl allows you to download log files