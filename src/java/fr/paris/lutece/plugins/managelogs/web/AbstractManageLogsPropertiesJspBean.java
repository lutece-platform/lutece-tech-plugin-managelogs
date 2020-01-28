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

import fr.paris.lutece.plugins.managelogs.util.ManageLogsUtil;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;

import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ManageLogsProperties JSP Bean abstract class for JSP Bean
 */
public abstract class AbstractManageLogsPropertiesJspBean extends MVCAdminJspBean
{
    //Constants
    protected static final String SLASH = "/";
    protected static final String EMPTY = "";

    // Rights
    public static final String RIGHT_MANAGELOGSPROPERTIES = "MANAGELOGS_MANAGEMENT";
    
    // Properties

    protected static final String TMP_LOG_PATH = AppPropertiesService.getProperty( "managelogs.tmp.log.path", "WEB-INF/conf/override/" );
    protected static final String TMP_LOG_FILE_NAME = AppPropertiesService.getProperty( "managelogs.tmp.log.filename", "tmp_log.properties" );
    protected static final String TMP_LOG_PATH_ABSOLUTE = getAbsolutePath(TMP_LOG_PATH );
    protected static final String TMP_LOG_ABSOLUTE = TMP_LOG_PATH_ABSOLUTE + ( TMP_LOG_PATH_ABSOLUTE.endsWith( SLASH ) ? EMPTY : SLASH ) + TMP_LOG_FILE_NAME;

    protected static final String LUTECE_LOG_PATH = AppPropertiesService.getProperty( "managelogs.lutece.log.path", "WEB-INF/conf/" );
    protected static final String LUTECE_LOG_FILE = AppPropertiesService.getProperty( "managelogs.lutecec.log.file", "config.properties" );

    protected static final String ALTERNATE_LOG_CONF_FILE_ABSOLUTE = getAbsolutePath( LUTECE_LOG_PATH + ( LUTECE_LOG_PATH.endsWith( SLASH ) ? EMPTY : SLASH ) + "override/" + "log.properties" );
    protected static final String LUTECE_CONF_FILE_ABSOLUTE = getAbsolutePath( LUTECE_LOG_PATH + ( LUTECE_LOG_PATH.endsWith( SLASH ) ? EMPTY : SLASH ) + LUTECE_LOG_FILE );

    protected static final boolean APP_SERVER_MULTI_WEBAPP = AppPropertiesService.getPropertyBoolean( "managelogs.is.multi.webapp", false );

    private static List<String> _listLogFoldersAllowed = new ArrayList<>(  );

    static
    {
        // add path
        String strListFoldersRelative = AppPropertiesService.getProperty( "managelogs.limit.folder" );
        if ( strListFoldersRelative != null )
        {
            String[] arrFoldersRelative = strListFoldersRelative.split( ";" );
            for (String relativeFolder : arrFoldersRelative)
            {
                if ( !ManageLogsUtil.isNullOrEmptyWithTrim( relativeFolder ) )
                {
                    _listLogFoldersAllowed.add( getAbsolutePath( relativeFolder ) );
                }
            }
        }
    }


    /**
     * return absolute path from a path
     * @param strPath the path
     * @return the absolute path
     */
    protected static String getAbsolutePath(String strPath)
    {
        Path path = Paths.get( strPath );
        if ( path.isAbsolute( ) )
        {
            return strPath;
        }
        else
        {
            // relative
            return AppPathService.getAbsolutePathFromRelativePath(( strPath.startsWith(SLASH) ? EMPTY : SLASH ) + strPath );
        }
    }

    /**
     * get log files from configuration and check if parent folder is accessible  and in allowed folder
     * @param lines lines from configuration file
     * @return list of files in the log properties
     */
    protected static List<String> getFilesFromConfiguration( List<String> lines, boolean throwExceptionOnInaccessibleFile ) throws AccessDeniedException
    {
        if ( lines == null )
        {
            return new ArrayList<>(  );
        }

        List<String> listConfiguredFiles = new ArrayList<>(  );
        for ( String line : lines )
        {
            // check if line is valid (key=value) and not comment
            if ( !ManageLogsUtil.isNullOrEmptyWithTrim( line ) && !line.trim( ).startsWith( "#" ) && line.split( "=" ).length == 2 && line.contains( ".File=" ) )
            {
                String fileName = line.split( "=" )[ 1 ];
                String strParentAbsoluteDirectory = Paths.get( getAbsolutePath( fileName.replaceAll( "\r",EMPTY ) ) ).getParent( ).toString( );
                if ( isLogFileAccessible( strParentAbsoluteDirectory ) )
                {
                    listConfiguredFiles.add( fileName );
                }
                else
                {
                    AppLogService.debug( "File " + fileName + " inaccessible" );
                    if ( throwExceptionOnInaccessibleFile )
                    {
                        throw new AccessDeniedException( "Folder " + strParentAbsoluteDirectory + " is denied (check 'limit.folder' configuration" );
                    }
                }
            }
        }

        return listConfiguredFiles;
    }

    /**
     * Return true if file is accessible and within allowed log folder
     * @param file the file to check
     * @return the check result
     */
    protected static boolean isLogFileAccessible(String file)
    {
        if ( ManageLogsUtil.isNullOrEmptyWithTrim( file ) )
        {
            return false;
        }

        boolean bIsFileOK = false;

        // check if the file is in an allowed folder (or sub-folder)
        for ( String strValidFolder : _listLogFoldersAllowed )
        {
            if ( !ManageLogsUtil.isNullOrEmptyWithTrim( strValidFolder ) &&  Paths.get( file.trim() ).startsWith( Paths.get( strValidFolder.trim() ) ) )
            {
                bIsFileOK = true;
                break;
            }
        }

        return  bIsFileOK && ManageLogsUtil.isFileReadable( file );
    }

}