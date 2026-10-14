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
package fr.paris.lutece.plugins.managelogs.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * Utilities on the log files list.
 */
public final class ManageLogsUtil
{
    private static final String PROPERTY_REMOVE_DUPLICATE_LOG_FILES = "managelogs.remove_duplicate_log_files";
    private static final String PROPERTY_MAX_FILESIZE_FOR_COMPARAISON = "managelogs.max_filesize_for_comparaison";
    private static final int DEFAULT_MAX_FILESIZE_FOR_COMPARAISON = 10485760;

    /**
     * Private constructor.
     */
    private ManageLogsUtil( )
    {
    }

    /**
     * Checks for a null or blank string.
     *
     * @param strInput
     *            input String
     * @return true if the string is null or blank
     */
    public static boolean isNullOrEmptyWithTrim( String strInput )
    {
        return strInput == null || strInput.trim( ).isEmpty( );
    }

    /**
     * Removes the files that have the same name and the same content as a later file of the list, when
     * managelogs.remove_duplicate_log_files is true.
     *
     * @param fileList
     *            the files
     * @return the files without duplicates
     */
    public static List<ManageLogFile> removeDuplicateFiles( List<ManageLogFile> fileList )
    {
        boolean bRemoveDuplicates = AppPropertiesService.getPropertyBoolean( PROPERTY_REMOVE_DUPLICATE_LOG_FILES, false );

        if ( !bRemoveDuplicates || fileList == null || fileList.isEmpty( ) )
        {
            return fileList;
        }

        int nMaxFileSizeForComparaison = AppPropertiesService.getPropertyInt( PROPERTY_MAX_FILESIZE_FOR_COMPARAISON, DEFAULT_MAX_FILESIZE_FOR_COMPARAISON );
        List<ManageLogFile> returnFileList = new ArrayList<>( );

        for ( int i = 0; i < fileList.size( ); i++ )
        {
            ManageLogFile logFile = fileList.get( i );

            if ( logFile.getSize( ) > nMaxFileSizeForComparaison || !hasDuplicateAfter( fileList, i ) )
            {
                returnFileList.add( logFile );
            }
        }

        return returnFileList;
    }

    /**
     * Tells whether a later file of the list has the same name and the same content.
     *
     * @param fileList
     *            the files
     * @param nIndex
     *            the index of the file to compare
     * @return true if a later file is a duplicate
     */
    private static boolean hasDuplicateAfter( List<ManageLogFile> fileList, int nIndex )
    {
        ManageLogFile logFile = fileList.get( nIndex );

        for ( int j = nIndex + 1; j < fileList.size( ); j++ )
        {
            ManageLogFile fileToCompare = fileList.get( j );

            try
            {
                if ( logFile.getFileName( ).equals( fileToCompare.getFileName( ) )
                        && FileUtils.contentEquals( logFile.getPath( ).toFile( ), fileToCompare.getPath( ).toFile( ) ) )
                {
                    return true;
                }
            }
            catch( IOException e )
            {
                AppLogService.debug( "Error comparing {} and {}", logFile.getPath( ), fileToCompare.getPath( ), e );
            }
        }

        return false;
    }

    /**
     * Sorts the files by name and numbers them from 0.
     *
     * @param logFilesDedup
     *            the files
     */
    public static void reorderFiles( List<ManageLogFile> logFilesDedup )
    {
        Collections.sort( logFilesDedup );

        for ( int i = 0; i < logFilesDedup.size( ); i++ )
        {
            logFilesDedup.get( i ).setItemNumber( i );
        }
    }
}
