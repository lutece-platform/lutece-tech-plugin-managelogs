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

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageLogsUtil
{
    private static final String PROPERTY_REMOVE_DUPLICATE_LOG_FILES= "managelogs.remove_duplicate_log_files";
    private static final String PROPERTY_MAX_FILESIZE_FOR_COMPARAISON = "managelogs.max_filesize_for_comparaison";

    private ManageLogsUtil( )
    {

    }

    /**
     * Check for null or empty string (after trim)
     * @param strInput input String
     * @return result of check for null and empty
     */
    public static boolean isNullOrEmptyWithTrim( String strInput )
    {
        return strInput == null || strInput.trim().isEmpty();
    }

    /**
     * Check if a file is readable on the system file (uses java nio Files and Paths)
     * @param strFile absolute path
     * @return result of check
     */
    public static boolean isFileReadable( String strFile )
    {
        return !isNullOrEmptyWithTrim( strFile ) && Files.isReadable( Paths.get ( strFile ) );
    }

    /**
     * Remove files that have same name and same content
     * @param fileList
     * @return
     */
    public static List<ManageLogFile> removeDuplicateFiles( List<ManageLogFile> fileList )
    {
        boolean removeDuplicates = AppPropertiesService.getPropertyBoolean( PROPERTY_REMOVE_DUPLICATE_LOG_FILES, false );

        if (removeDuplicates && fileList != null && !fileList.isEmpty())
        {
            int maxFileSizeForComparaison = AppPropertiesService.getPropertyInt( PROPERTY_MAX_FILESIZE_FOR_COMPARAISON, 10485760 );// default 10 MB
            List<ManageLogFile> returnFileList = new ArrayList<>();

            for ( int i=0; i<fileList.size(); i++ ) {
                ManageLogFile logFile = fileList.get( i );

                boolean fileHasDuplicate = false;
                if ( logFile.getSize() <= maxFileSizeForComparaison )
                {
                    for ( int j=i+1; j<fileList.size(); j++)
                    {
                        ManageLogFile fileToCompare = fileList.get( j );
                        if (logFile.getFileName( ).equals( fileToCompare.getFileName( ) ) ) {
                            try
                            {
                                if ( FileUtils.contentEquals( logFile.getPath( ).toFile( ), fileToCompare.getPath( ).toFile( ) ) )
                                {
                                    // content is equal
                                    fileHasDuplicate = true;
                                    break;
                                }
                            }
                            catch ( IOException e )
                            {
                                // do nothing
                            }
                        }
                    }
                }
                if ( !fileHasDuplicate )
                {
                    returnFileList.add( logFile );
                }
            }

            return returnFileList;
        }
        else
        {
            return fileList;
        }
    }

    public static void reorderFiles( List<ManageLogFile> logFilesDedup )
    {
        Collections.sort( logFilesDedup );
        for ( int i=0; i<logFilesDedup.size(); i++)
        {
            logFilesDedup.get( i ).setItemNumber( i );
        }
    }
}
