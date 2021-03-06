/*
 * Copyright (c) 2002-2019, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
 	
package fr.paris.lutece.plugins.managelogs.web;

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.plugins.managelogs.util.ManageLogsUtil;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.paris.lutece.plugins.managelogs.web.AbstractManageLogsPropertiesJspBean.RIGHT_MANAGELOGSPROPERTIES;

/**
 * This class provides the user interface to download logs
 */
@Controller( controllerJsp = "DownloadLog.jsp", controllerPath = "jsp/admin/plugins/managelogs/", right = RIGHT_MANAGELOGSPROPERTIES )
public class DownloadLogJspBean extends AbstractManageLogsPropertiesJspBean
{
    static final long serialVersionUID = -1;
    
    // Templates
    private static final String TEMPLATE_MANAGE_LOGPROPERTIESS = "/admin/plugins/managelogs/download_logs.html";

    // Parameters
    private static final String PARAMETER_ID_LOG = "idLog";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_LOGPROPERTIESS = "managelogs.manage_logdownload.pageTitle";

    // Markers
    private static final String MARK_LIST_LOGS = "logs_list";
    // Properties

    // Validations

    // Views
    private static final String VIEW_DEFAULT = "listLogs";

    // Actions
    private static final String ACTION_DOWNLOAD_LOG = "download";

    // Infos

    // Errors


    // Session variable to store working values
    List<ManageLogFile> listLogFiles;

    private static final String CONTENT_TYPE = "application/octet-stream";

    /**
     * Build the Manage View
     * @param request The HTTP request
     * @return The page
     */
    @View( value = VIEW_DEFAULT, defaultView = true )
    public String getLogs( HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<>(  );

        List<ManageLogFile> fileList = getLogFilesMap();

        if ( fileList.isEmpty() )
        {
            // no conf found or no log file
            AppLogService.error( "No log file found" );
        }

        listLogFiles = ManageLogsUtil.removeDuplicateFiles( fileList );
        ManageLogsUtil.reorderFiles(listLogFiles);

        model.put( MARK_LIST_LOGS, listLogFiles );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_LOGPROPERTIESS, TEMPLATE_MANAGE_LOGPROPERTIESS, model );
    }

    private static List<ManageLogFile> getLogFilesMap()
    {
        List<ManageLogFile> fileList = new ArrayList<>();

        Set<Path> setLogFiles = getLogFiles();

        if ( setLogFiles.isEmpty() )
        {
            // no conf found or no log file
            AppLogService.error( "No log file found" );
        }
        else
        {
            // sort files
            List<Path> listFilesSorted = new ArrayList<>( setLogFiles );
            Collections.sort( listFilesSorted );//TODO

            int i=0;
            for ( Path file : listFilesSorted )
            {
                fileList.add( new ManageLogFile( file, i ) );
                i++;
            }
        }

        return fileList;
    }

    /**
     * find log configurations and get log files from them
     * @return list of log file
     */
    private static Set<Path> getLogFiles()
    {
        // Using set to avoid duplicates
        Set<Path> setLogs = new HashSet<>( );

        // STEP 1: get all configuration and log files
        String log4jConfigFile = null;

        // get log file from log4j configuration
        if ( !APP_SERVER_MULTI_WEBAPP )
        {
            log4jConfigFile = System.getProperty( "log4j.configuration" );
            if ( ManageLogsUtil.isFileReadable( log4jConfigFile ) )
            {
                setLogs.addAll( readLogConf( log4jConfigFile ) );
            }
        }

        // try to find tmp log conf
        setLogs.addAll( getLogsFromFile( TMP_LOG_ABSOLUTE, log4jConfigFile ) );

        // try to find log.properties
        setLogs.addAll( getLogsFromFile( ALTERNATE_LOG_CONF_FILE_ABSOLUTE, log4jConfigFile ) );

        // try to find config.properties
        setLogs.addAll( getLogsFromFile( LUTECE_CONF_FILE_ABSOLUTE, log4jConfigFile ) );

        // additionnal logs defined in managelogs.properties
        String strListAdditionalLogDir = AppPropertiesService.getProperty( "managelogs.addlog.folder" );
        if ( strListAdditionalLogDir != null )
        {
            for ( String logDir : strListAdditionalLogDir.split( ";" ) )
            {
                if ( !ManageLogsUtil.isNullOrEmptyWithTrim( logDir ) )
                {
                    try
                    {
                        setLogs.addAll ( listFilesInDirectory( logDir.trim() ) );
                    }
                    catch ( IOException e )
                    {
                        AppLogService.error( "Error getting additionnal files from " + logDir, e );
                    }
                }
            }
        }

        return setLogs;
    }

    private static Set<Path> getLogsFromFile( String file, String log4jConfigFile )
    {
        Set<Path> setLogs = new HashSet<>( );
        if ( !file.equalsIgnoreCase( log4jConfigFile ) && ManageLogsUtil.isFileReadable( file ) )
        {
            setLogs.addAll( readLogConf( file ) );
        }
        return setLogs;
    }

    /**
     * read config file and log files from them
     * @param fileName log configuration file
     * @return list of log file names (with path)
     */
    private static Set<Path> readLogConf( String fileName )
    {
        Set<Path> logFile = new HashSet<>(  );
        try
        {
            List<String> lines = Files.readAllLines( Paths.get( fileName ), Charset.defaultCharset( ) );

            List<String> listFilesFromConfiguration = getFilesFromConfiguration( lines, false );

            for ( String fileFromConfiguration : listFilesFromConfiguration )
            {
                String absoluteDirectory = Paths.get( fileFromConfiguration ).getParent().toString();

                // listing all files
                Set<Path> filesInDirectory = listFilesInDirectory( absoluteDirectory );
                for ( Path fileInDirectory : filesInDirectory )
                {
                    // check if the listed file is a derivative of the configured log file (log rotation)
                    if ( fileInDirectory.getFileName().toString().startsWith( Paths.get( fileFromConfiguration ).getFileName().toString() ) )
                    {
                        logFile.add( fileInDirectory );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            AppLogService.error( "Error reading file " + fileName, e );
        }

        return logFile;
    }



    /**
     * Return files of a directory, with check of allowed directories
     * @param dir directory to list
     * @return list of files in directory
     * @throws IOException error in reading the directory
     */
    private static Set<Path> listFilesInDirectory( String dir ) throws IOException
    {
        Set<Path> fileList = new HashSet<>();
        if ( ManageLogsUtil.isFileReadable( dir ) )
        {
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( Paths.get( dir ) ) )
            {
                for ( Path path : stream )
                {
                    if ( !Files.isDirectory( path ) && isLogFileAccessible( path.toString( ) ) )
                    {
                        fileList.add( path );
                    }
                }
            }
        }
        return fileList;
    }

    /**
     * Download a file
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @Action( ACTION_DOWNLOAD_LOG )
    public String downloadLog( HttpServletRequest request )
    {
        String strId =request.getParameter( PARAMETER_ID_LOG );

        // get logs list
        //List<ManageLogFile> listRF = DownloadLogJspBean.getLogFilesMap();

        if (strId == null) {
            AppLogService.error( "Error, log number null" );
        }
        else
        {
            int idFile;
            try
            {
                idFile = Integer.parseInt( strId );
            }
            catch ( NumberFormatException nfe )
            {
                return getLogs( request );
            }

            if (  idFile < 0 || idFile >= listLogFiles.size() )
            {
                AppLogService.error( "Error, log number invalid ("+idFile+")" );
            }
            else
            {

                Path path = listLogFiles.get( idFile ).getPath();

                try
                {
                    download( Files.readAllBytes( path ), path.getFileName().toString(), CONTENT_TYPE );
                }
                catch ( IOException e )
                {
                    AppLogService.error( "Error downloading file", e );
                }
            }
        }

        return getLogs( request );
    }

}
