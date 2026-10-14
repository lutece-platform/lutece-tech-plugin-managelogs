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

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A log file offered for download.
 */
public class ManageLogFile implements Comparable<ManageLogFile>, Serializable
{
    private static final long serialVersionUID = 1L;

    private String _strPath;
    private String _fileName;
    private long _size;
    private Integer _itemNumber;

    /**
     * Builds a log file from its path.
     *
     * @param path
     *            the file path
     * @param itemNumber
     *            the position of the file in the list
     */
    public ManageLogFile( Path path, int itemNumber )
    {
        if ( path != null )
        {
            _strPath = path.toString( );
            _fileName = path.getFileName( ).toString( );
            _size = getSize( path );
        }
        _itemNumber = itemNumber;
    }

    /**
     * Returns the size of a file, 0 when the file has been removed since it was listed (log rotation).
     *
     * @param path
     *            the file path
     * @return the size in bytes
     */
    private static long getSize( Path path )
    {
        try
        {
            return Files.size( path );
        }
        catch( IOException e )
        {
            return 0;
        }
    }

    /**
     * Returns the path.
     *
     * @return the path
     */
    public Path getPath( )
    {
        return _strPath == null ? null : Paths.get( _strPath );
    }

    /**
     * Sets the path.
     *
     * @param path
     *            the path
     */
    public void setPath( Path path )
    {
        _strPath = path == null ? null : path.toString( );
    }

    /**
     * Returns the file name.
     *
     * @return the file name
     */
    public String getFileName( )
    {
        return _fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName
     *            the file name
     */
    public void setFileName( String fileName )
    {
        _fileName = fileName;
    }

    /**
     * Returns the size in bytes.
     *
     * @return the size
     */
    public long getSize( )
    {
        return _size;
    }

    /**
     * Sets the size in bytes.
     *
     * @param size
     *            the size
     */
    public void setSize( long size )
    {
        _size = size;
    }

    /**
     * Returns the position of the file in the list.
     *
     * @return the position
     */
    public Integer getItemNumber( )
    {
        return _itemNumber;
    }

    /**
     * Sets the position of the file in the list.
     *
     * @param itemNumber
     *            the position
     */
    public void setItemNumber( Integer itemNumber )
    {
        _itemNumber = itemNumber;
    }

    /**
     * Compares the file names, ignoring case.
     *
     * @param o
     *            the other file
     * @return the comparison result
     */
    @Override
    public int compareTo( ManageLogFile o )
    {
        return getFileName( ).compareToIgnoreCase( o.getFileName( ) );
    }
}
