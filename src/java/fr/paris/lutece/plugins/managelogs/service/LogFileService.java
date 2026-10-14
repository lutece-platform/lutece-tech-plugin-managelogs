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

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.plugins.managelogs.util.ManageLogsUtil;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lists the log files that can be downloaded : the files written by the Log4j 2 appenders of the webapp
 * and the files of the folders of managelogs.addlog.folder restricted to the folders of managelogs.limit.folder
 */
public final class LogFileService
{
    private static final String PROPERTY_LIMIT_FOLDER = "managelogs.limit.folder";
    private static final String PROPERTY_ADD_LOG_FOLDER = "managelogs.addlog.folder";
    private static final String SEPARATOR = ";";
    private static final String SLASH = "/";
    private static final String BACKSLASH = "\\";
    private static final String PATTERN_START = "%";
    private static final String CURRENT_FOLDER = ".";

    /**
     * Private constructor
     */
    private LogFileService( )
    {
    }

    /**
     * Get the log files that can be downloaded, sorted by name and numbered
     *
     * @return the log files
     */
    public static List<ManageLogFile> getLogFiles( )
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
     * Get the files written by the file appenders of the Log4j 2 configuration of the webapp, with their rolled over files
     *
     * @return the files
     */
    static Set<Path> getConfiguredLogFiles( )
    {
        Set<Path> setLogs = new TreeSet<>( );
        Object context = LogManager.getContext( false );

        if ( !( context instanceof LoggerContext ) )
        {
            AppLogService.error( "Log4j 2 core is not the logging implementation, only the files of {} are listed", PROPERTY_ADD_LOG_FOLDER );
            return setLogs;
        }

        for ( Appender appender : ( (LoggerContext) context ).getConfiguration( ).getAppenders( ).values( ) )
        {
            if ( appender instanceof FileAppender )
            {
                addLogFamily( setLogs, ( (FileAppender) appender ).getFileName( ), null );
            }
            else if ( appender instanceof RollingFileAppender )
            {
                RollingFileAppender rollingAppender = (RollingFileAppender) appender;
                addLogFamily( setLogs, rollingAppender.getFileName( ), rollingAppender.getFilePattern( ) );
            }
            else if ( appender instanceof RandomAccessFileAppender )
            {
                addLogFamily( setLogs, ( (RandomAccessFileAppender) appender ).getFileName( ), null );
            }
            else if ( appender instanceof RollingRandomAccessFileAppender )
            {
                RollingRandomAccessFileAppender rollingAppender = (RollingRandomAccessFileAppender) appender;
                addLogFamily( setLogs, rollingAppender.getFileName( ), rollingAppender.getFilePattern( ) );
            }
        }

        return setLogs;
    }

    /**
     * Add the file of an appender and the files of its folder whose name starts with the name of the file, then the files
     * matching the beginning of the rollover pattern
     *
     * @param setLogs
     *            the files found
     * @param strFileName
     *            the file of the appender, may be null
     * @param strFilePattern
     *            the rollover pattern of the appender, may be null
     */
    private static void addLogFamily( Set<Path> setLogs, String strFileName, String strFilePattern )
    {
        if ( !ManageLogsUtil.isNullOrEmptyWithTrim( strFileName ) )
        {
            Path file = Paths.get( strFileName ).toAbsolutePath( ).normalize( );
            addFilesStartingWith( setLogs, file.getParent( ), file.getFileName( ).toString( ) );
        }

        if ( ManageLogsUtil.isNullOrEmptyWithTrim( strFilePattern ) )
        {
            return;
        }

        String strPattern = strFilePattern.replace( BACKSLASH, SLASH );
        int nLastSlash = strPattern.lastIndexOf( SLASH );
        String strPatternFolder = nLastSlash < 0 ? CURRENT_FOLDER : strPattern.substring( 0, nLastSlash );
        String strPatternName = strPattern.substring( nLastSlash + 1 );
        int nPatternStart = strPatternName.indexOf( PATTERN_START );
        String strPrefix = nPatternStart < 0 ? strPatternName : strPatternName.substring( 0, nPatternStart );

        if ( !strPatternFolder.contains( PATTERN_START ) && !strPrefix.isEmpty( ) )
        {
            addFilesStartingWith( setLogs, Paths.get( strPatternFolder ).toAbsolutePath( ).normalize( ), strPrefix );
        }
    }

    /**
     * Add the files of a folder whose name starts with a prefix
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
     * Get the files of the folders of managelogs.addlog.folder, restricted to the folders of managelogs.limit.folder
     *
     * @return the files
     */
    static Set<Path> getAdditionalLogFiles( )
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
     * Get the folders of a property, separated by ';', relative to the webapp or absolute
     *
     * @param strProperty
     *            the property key
     * @return the absolute and normalized folders
     */
    private static List<Path> getFolders( String strProperty )
    {
        List<Path> listFolders = new ArrayList<>( );
        String strFolders = AppPropertiesService.getProperty( strProperty, "" );

        for ( String strFolder : strFolders.split( SEPARATOR ) )
        {
            if ( !ManageLogsUtil.isNullOrEmptyWithTrim( strFolder ) )
            {
                listFolders.add( Paths.get( getAbsolutePath( strFolder.trim( ) ) ).toAbsolutePath( ).normalize( ) );
            }
        }

        return listFolders;
    }

    /**
     * Get the absolute path of a path relative to the webapp
     *
     * @param strPath
     *            the path, relative to the webapp or absolute
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
     * Check if a file is in one of the folders or their sub-folders
     *
     * @param path
     *            the file
     * @param listFolders
     *            the absolute and normalized folders
     * @return true if the file is in one of the folders
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
     * List the readable regular files of a folder
     *
     * @param folder
     *            the folder, may be null
     * @return the absolute and normalized files
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
