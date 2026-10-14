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
package fr.paris.lutece.plugins.managelogs.business;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of ManageLogFile.
 */
public class ManageLogFileTest
{
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
        _folder = Files.createTempDirectory( "managelogs" );
    }

    /**
     * Removes the test folder.
     *
     * @throws IOException
     *             if a file cannot be removed
     */
    @AfterEach
    public void removeFolder( ) throws IOException
    {
        try ( DirectoryStream<Path> stream = Files.newDirectoryStream( _folder ) )
        {
            for ( Path path : stream )
            {
                Files.delete( path );
            }
        }
        Files.delete( _folder );
    }

    /**
     * The name, the size and the position of an existing file are kept.
     *
     * @throws IOException
     *             if the file cannot be written
     */
    @Test
    public void testExistingFile( ) throws IOException
    {
        Path file = Files.write( _folder.resolve( "application.log" ), new byte [ 10 ] );

        ManageLogFile logFile = new ManageLogFile( file, 3 );

        assertEquals( file, logFile.getPath( ), "the path is kept" );
        assertEquals( "application.log", logFile.getFileName( ), "the file name is kept" );
        assertEquals( 10L, logFile.getSize( ), "the size is the size of the file" );
        assertEquals( Integer.valueOf( 3 ), logFile.getItemNumber( ), "the position is kept" );
    }

    /**
     * A file removed since it was listed (log rotation) has a size of 0, without error.
     */
    @Test
    public void testRemovedFile( )
    {
        Path file = _folder.resolve( "error-1.log.gz" );

        ManageLogFile logFile = new ManageLogFile( file, 0 );

        assertEquals( "error-1.log.gz", logFile.getFileName( ), "the file name is kept" );
        assertEquals( 0L, logFile.getSize( ), "the size of a removed file is 0" );
    }

    /**
     * A log file can be serialized, the list being kept in the session.
     *
     * @throws IOException
     *             if the file cannot be written or serialized
     * @throws ClassNotFoundException
     *             if the file cannot be deserialized
     */
    @Test
    public void testSerialization( ) throws IOException, ClassNotFoundException
    {
        Path file = Files.write( _folder.resolve( "application.log" ), new byte [ 5 ] );
        ByteArrayOutputStream bytes = new ByteArrayOutputStream( );

        try ( ObjectOutputStream out = new ObjectOutputStream( bytes ) )
        {
            out.writeObject( new ManageLogFile( file, 1 ) );
        }

        try ( ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( bytes.toByteArray( ) ) ) )
        {
            ManageLogFile logFile = (ManageLogFile) in.readObject( );

            assertEquals( file, logFile.getPath( ), "the path is restored" );
            assertEquals( 5L, logFile.getSize( ), "the size is restored" );
        }
    }
}
