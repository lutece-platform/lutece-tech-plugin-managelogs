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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.inject.Inject;

/**
 * Tests of LogFileService.
 */
public class LogFileServiceTest extends LuteceTestCase
{
    private static final String APPENDER_FILE = "managelogsTestFile";
    private static final String APPENDER_ROLLING = "managelogsTestRolling";
    private static final String PROPERTY_ADD_LOG_FOLDER = "managelogs.addlog.folder";
    private static final String PROPERTY_LIMIT_FOLDER = "managelogs.limit.folder";
    private static final String PROPERTY_SERVER_LOGS_DIR = "server.logs.dir";

    @Inject
    private LogFileService _logFileService;

    private Path _folder;

    /**
     * Creates the test folder.
     *
     * @throws IOException
     *             if the folder cannot be created
     */
    @BeforeEach
    public void createFolder( ) throws IOException
    {
        _folder = Files.createTempDirectory( "managelogs" ).toRealPath( );
    }

    /**
     * Removes the test appenders and properties.
     */
    @AfterEach
    public void cleanUp( )
    {
        Configuration configuration = getConfiguration( );

        for ( String strName : new String [ ] { APPENDER_FILE, APPENDER_ROLLING } )
        {
            Appender appender = configuration.getAppender( strName );

            if ( appender != null )
            {
                appender.stop( );
                configuration.getAppenders( ).remove( strName );
            }
        }

        System.clearProperty( PROPERTY_ADD_LOG_FOLDER );
        System.clearProperty( PROPERTY_LIMIT_FOLDER );
        System.clearProperty( PROPERTY_SERVER_LOGS_DIR );
    }

    /**
     * The files of the log folder of the application server are listed, its sub-folders are not.
     *
     * @throws IOException
     *             if a file cannot be written
     */
    @Test
    public void testServerLogFiles( ) throws IOException
    {
        Path fileMessages = Files.createFile( _folder.resolve( "messages.log" ) );
        Path folderFfdc = Files.createDirectory( _folder.resolve( "ffdc" ) );

        System.setProperty( PROPERTY_SERVER_LOGS_DIR, _folder.toString( ) );

        Set<Path> setFiles = _logFileService.getServerLogFiles( );

        assertTrue( setFiles.contains( fileMessages ), "a file of the server log folder is listed" );
        assertFalse( setFiles.contains( folderFfdc ), "a sub-folder of the server log folder is not listed" );
    }

    /**
     * The files of a file appender and of a rolling appender are found with their rolled files, and nothing else of their
     * folder.
     *
     * @throws IOException
     *             if a file cannot be written
     */
    @Test
    public void testConfiguredLogFiles( ) throws IOException
    {
        Configuration configuration = getConfiguration( );
        Path fileLog = _folder.resolve( "plain.log" );
        Path fileRolledByName = Files.createFile( _folder.resolve( "plain.log.1" ) );
        Path fileRolling = _folder.resolve( "rolling.log" );
        Path fileRolledByPattern = Files.createFile( _folder.resolve( "rolling-1.log.gz" ) );
        Path fileOther = Files.createFile( _folder.resolve( "other.txt" ) );

        FileAppender appenderFile = FileAppender.newBuilder( ).setName( APPENDER_FILE ).setConfiguration( configuration )
                .withFileName( fileLog.toString( ) ).build( );
        appenderFile.start( );
        configuration.addAppender( appenderFile );

        RollingFileAppender appenderRolling = RollingFileAppender.newBuilder( ).setName( APPENDER_ROLLING ).setConfiguration( configuration )
                .withFileName( fileRolling.toString( ) ).withFilePattern( _folder.resolve( "rolling-%i.log.gz" ).toString( ) )
                .withPolicy( SizeBasedTriggeringPolicy.createPolicy( "10MB" ) ).build( );
        appenderRolling.start( );
        configuration.addAppender( appenderRolling );

        Set<Path> setFiles = _logFileService.getConfiguredLogFiles( );

        assertTrue( setFiles.contains( fileLog ), "the file of the file appender is listed" );
        assertTrue( setFiles.contains( fileRolledByName ), "a file named after the appender file is listed" );
        assertTrue( setFiles.contains( fileRolling ), "the file of the rolling appender is listed" );
        assertTrue( setFiles.contains( fileRolledByPattern ), "a file of the roll pattern is listed" );
        assertFalse( setFiles.contains( fileOther ), "an unrelated file of the folder is not listed" );
    }

    /**
     * The files of an additional folder are listed only when the folder lies in an allowed folder.
     *
     * @throws IOException
     *             if a file cannot be written
     */
    @Test
    public void testAdditionalLogFiles( ) throws IOException
    {
        Path folderAllowed = Files.createDirectory( _folder.resolve( "allowed" ) );
        Path folderDenied = Files.createDirectory( _folder.resolve( "denied" ) );
        Path fileAllowed = Files.createFile( folderAllowed.resolve( "server.log" ) );
        Path fileDenied = Files.createFile( folderDenied.resolve( "secret.log" ) );

        System.setProperty( PROPERTY_ADD_LOG_FOLDER, folderAllowed + ";" + folderDenied );
        System.setProperty( PROPERTY_LIMIT_FOLDER, folderAllowed.toString( ) );

        Set<Path> setFiles = _logFileService.getAdditionalLogFiles( );

        assertTrue( setFiles.contains( fileAllowed ), "a file of an allowed folder is listed" );
        assertFalse( setFiles.contains( fileDenied ), "a file outside the allowed folders is not listed" );
    }

    /**
     * The list is sorted by file name and numbered from 0.
     *
     * @throws IOException
     *             if a file cannot be written
     */
    @Test
    public void testLogFilesSortedAndNumbered( ) throws IOException
    {
        Path folderAllowed = Files.createDirectory( _folder.resolve( "sorted" ) );
        Files.createFile( folderAllowed.resolve( "b.log" ) );
        Files.createFile( folderAllowed.resolve( "A.log" ) );

        System.setProperty( PROPERTY_ADD_LOG_FOLDER, folderAllowed.toString( ) );
        System.setProperty( PROPERTY_LIMIT_FOLDER, folderAllowed.toString( ) );

        List<ManageLogFile> listFiles = _logFileService.getLogFiles( );
        int nIndexA = indexOf( listFiles, "A.log" );
        int nIndexB = indexOf( listFiles, "b.log" );

        assertTrue( nIndexA >= 0 && nIndexA < nIndexB, "files are sorted by name, ignoring case" );

        for ( int i = 0; i < listFiles.size( ); i++ )
        {
            assertEquals( i, listFiles.get( i ).getItemNumber( ).intValue( ), "files are numbered from 0" );
        }
    }

    /**
     * Returns the position of a file name in the list.
     *
     * @param listFiles
     *            the files
     * @param strName
     *            the file name
     * @return the position, -1 when absent
     */
    private static int indexOf( List<ManageLogFile> listFiles, String strName )
    {
        for ( int i = 0; i < listFiles.size( ); i++ )
        {
            if ( strName.equals( listFiles.get( i ).getFileName( ) ) )
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the Log4j 2 configuration of the test.
     *
     * @return the configuration
     */
    private static Configuration getConfiguration( )
    {
        return ( (LoggerContext) LogManager.getContext( false ) ).getConfiguration( );
    }
}
