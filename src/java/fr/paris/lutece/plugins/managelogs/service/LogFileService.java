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
package fr.paris.lutece.plugins.managelogs.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.eclipse.microprofile.config.Config;

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.plugins.managelogs.util.ManageLogsUtil;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Finds the log files offered for download: the files written by the file appenders of the Log4j 2 configuration, with
 * their rolled files, or, when Log4j 2 sends its events to the application server (log4j-to-jul), the files of the log
 * folder of the server; and the files of the folders of managelogs.addlog.folder that lie in managelogs.limit.folder.
 */
@ApplicationScoped
public class LogFileService
{
    private static final String PROPERTY_LIMIT_FOLDER = "managelogs.limit.folder";
    private static final String PROPERTY_ADD_LOG_FOLDER = "managelogs.addlog.folder";
    private static final String SEPARATOR = ";";
    private static final String SLASH = "/";
    private static final String PATTERN_START = "%";
    private static final String CLASS_LOG4J_CORE_CONTEXT = "org.apache.logging.log4j.core.LoggerContext";
    private static final String PROPERTY_SERVER_LOGS_DIR = "server.logs.dir";

    @Inject
    private Config _config;

    /**
     * Returns the log files, sorted by name, duplicates removed when configured, numbered from 0.
     *
     * @return the log files
     */
    public List<ManageLogFile> getLogFiles( )
    {
        Set<Path> setLogs = new TreeSet<>( );
        setLogs.addAll( getConfiguredLogFiles( ) );
        setLogs.addAll( getAdditionalLogFiles( ) );

        List<ManageLogFile> listFiles = new ArrayList<>( );
        int nItem = 0;
        for ( Path path : setLogs )
        {
            listFiles.add( new ManageLogFile( path, nItem++ ) );
        }

        List<ManageLogFile> listDedup = ManageLogsUtil.removeDuplicateFiles( listFiles );
        ManageLogsUtil.reorderFiles( listDedup );

        return listDedup;
    }

    /**
     * Returns the files written by the file appenders of the Log4j 2 configuration of this webapp, with their rolled files,
     * or the files of the log folder of the application server when Log4j 2 core is not the logging implementation.
     *
     * @return the files
     */
    Set<Path> getConfiguredLogFiles( )
    {
        Object context = LogManager.getContext( false );

        if ( !isLog4jCore( context ) )
        {
            return getServerLogFiles( );
        }

        Set<Path> setLogs = new TreeSet<>( );

        for ( Log4jCoreAppenders.AppenderFile file : Log4jCoreAppenders.getAppenderFiles( context ) )
        {
            addLogFamily( setLogs, file.fileName( ), file.filePattern( ) );
        }

        return setLogs;
    }

    /**
     * Returns the files of the log folder of the application server, named by the system property server.logs.dir (Open
     * Liberty), none when it is not set.
     *
     * @return the files
     */
    Set<Path> getServerLogFiles( )
    {
        Set<Path> setLogs = new TreeSet<>( );
        String strServerLogsDir = System.getProperty( PROPERTY_SERVER_LOGS_DIR );

        if ( ManageLogsUtil.isNullOrEmptyWithTrim( strServerLogsDir ) )
        {
            AppLogService.debug( "Log4j 2 core is not the logging implementation and {} is not set", PROPERTY_SERVER_LOGS_DIR );
            return setLogs;
        }

        setLogs.addAll( listFiles( Paths.get( strServerLogsDir ).toAbsolutePath( ).normalize( ) ) );

        return setLogs;
    }

    /**
     * Tells whether a logger context is a Log4j 2 core one, without loading Log4j 2 core when it is absent.
     *
     * @param context
     *            the logger context
     * @return true if Log4j 2 core is the logging implementation
     */
    private static boolean isLog4jCore( Object context )
    {
        try
        {
            return Class.forName( CLASS_LOG4J_CORE_CONTEXT, false, LogFileService.class.getClassLoader( ) ).isInstance( context );
        }
        catch( ClassNotFoundException e )
        {
            return false;
        }
    }

    /**
     * Adds a log file and the files of its folder whose name starts with its name or with the fixed start of its roll
     * pattern.
     *
     * @param setLogs
     *            the files found
     * @param strFileName
     *            the file name of the appender
     * @param strFilePattern
     *            the roll pattern of the appender, or null
     */
    private static void addLogFamily( Set<Path> setLogs, String strFileName, String strFilePattern )
    {
        if ( ManageLogsUtil.isNullOrEmptyWithTrim( strFileName ) )
        {
            return;
        }

        Path file = Paths.get( strFileName ).toAbsolutePath( ).normalize( );
        addFilesStartingWith( setLogs, file.getParent( ), file.getFileName( ).toString( ) );

        if ( ManageLogsUtil.isNullOrEmptyWithTrim( strFilePattern ) )
        {
            return;
        }

        String strPattern = strFilePattern.replace( '\\', '/' );
        int nLastSlash = strPattern.lastIndexOf( SLASH );
        String strPatternFolder = nLastSlash < 0 ? "." : strPattern.substring( 0, nLastSlash );
        String strPatternName = strPattern.substring( nLastSlash + 1 );
        int nPatternStart = strPatternName.indexOf( PATTERN_START );
        String strPrefix = nPatternStart < 0 ? strPatternName : strPatternName.substring( 0, nPatternStart );

        if ( !strPatternFolder.contains( PATTERN_START ) && !strPrefix.isEmpty( ) )
        {
            addFilesStartingWith( setLogs, Paths.get( strPatternFolder ).toAbsolutePath( ).normalize( ), strPrefix );
        }
    }

    /**
     * Adds the regular readable files of a folder whose name starts with a prefix.
     *
     * @param setLogs
     *            the files found
     * @param folder
     *            the folder
     * @param strPrefix
     *            the prefix
     */
    private static void addFilesStartingWith( Set<Path> setLogs, Path folder, String strPrefix )
    {
        for ( Path path : listFiles( folder ) )
        {
            if ( path.getFileName( ).toString( ).startsWith( strPrefix ) )
            {
                setLogs.add( path );
            }
        }
    }

    /**
     * Returns the files of the folders of managelogs.addlog.folder that lie in a folder of managelogs.limit.folder.
     *
     * @return the files
     */
    Set<Path> getAdditionalLogFiles( )
    {
        Set<Path> setLogs = new TreeSet<>( );
        List<Path> listAllowedFolders = getFolders( PROPERTY_LIMIT_FOLDER );

        for ( Path folder : getFolders( PROPERTY_ADD_LOG_FOLDER ) )
        {
            for ( Path path : listFiles( folder ) )
            {
                if ( isInFolders( path, listAllowedFolders ) )
                {
                    setLogs.add( path );
                }
            }
        }

        return setLogs;
    }

    /**
     * Returns the folders of a property, separated by ';', a relative folder resolved against the webapp.
     *
     * @param strProperty
     *            the property key
     * @return the absolute folders
     */
    private List<Path> getFolders( String strProperty )
    {
        List<Path> listFolders = new ArrayList<>( );
        String strFolders = _config.getOptionalValue( strProperty, String.class ).orElse( "" );

        for ( String strFolder : strFolders.split( SEPARATOR ) )
        {
            if ( !ManageLogsUtil.isNullOrEmptyWithTrim( strFolder ) )
            {
                listFolders.add( Paths.get( getAbsolutePath( strFolder.trim( ) ) ).normalize( ) );
            }
        }

        return listFolders;
    }

    /**
     * Returns the absolute path of a path, a relative path resolved against the webapp.
     *
     * @param strPath
     *            the path
     * @return the absolute path
     */
    private static String getAbsolutePath( String strPath )
    {
        if ( Paths.get( strPath ).isAbsolute( ) )
        {
            return strPath;
        }

        return AppPathService.getAbsolutePathFromRelativePath( ( strPath.startsWith( SLASH ) ? "" : SLASH ) + strPath );
    }

    /**
     * Tells whether a file lies in one of the folders or their sub-folders.
     *
     * @param path
     *            the file
     * @param listFolders
     *            the folders
     * @return true if the file lies in one of the folders
     */
    private static boolean isInFolders( Path path, List<Path> listFolders )
    {
        Path file = path.toAbsolutePath( ).normalize( );

        for ( Path folder : listFolders )
        {
            if ( file.startsWith( folder ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the regular readable files of a folder, none when the folder cannot be read.
     *
     * @param folder
     *            the folder
     * @return the files
     */
    private static List<Path> listFiles( Path folder )
    {
        List<Path> listFiles = new ArrayList<>( );

        if ( folder == null || !Files.isDirectory( folder ) || !Files.isReadable( folder ) )
        {
            return listFiles;
        }

        try ( DirectoryStream<Path> stream = Files.newDirectoryStream( folder ) )
        {
            for ( Path path : stream )
            {
                if ( Files.isRegularFile( path ) && Files.isReadable( path ) )
                {
                    listFiles.add( path.toAbsolutePath( ).normalize( ) );
                }
            }
        }
        catch( IOException e )
        {
            AppLogService.error( "Error listing the log folder {}", folder, e );
        }

        return listFiles;
    }
}
