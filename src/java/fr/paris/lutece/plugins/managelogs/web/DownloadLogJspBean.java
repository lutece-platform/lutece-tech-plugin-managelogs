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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.plugins.managelogs.service.LogFileService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to download logs
 */
@SessionScoped
@Named
@Controller( controllerJsp = "DownloadLog.jsp", controllerPath = "jsp/admin/plugins/managelogs/", right = DownloadLogJspBean.RIGHT_MANAGELOGSPROPERTIES, securityTokenEnabled = true )
public class DownloadLogJspBean extends MVCAdminJspBean
{
    public static final String RIGHT_MANAGELOGSPROPERTIES = "MANAGELOGS_MANAGEMENT";

    private static final long serialVersionUID = -1L;

    private static final String TEMPLATE_MANAGE_LOGPROPERTIESS = "/admin/plugins/managelogs/download_logs.html";

    private static final String PARAMETER_ID_LOG = "idLog";

    private static final String PROPERTY_PAGE_TITLE_MANAGE_LOGPROPERTIESS = "managelogs.manage_logdownload.pageTitle";

    private static final String MARK_LIST_LOGS = "logs_list";

    private static final String VIEW_DEFAULT = "listLogs";

    private static final String ACTION_DOWNLOAD_LOG = "download";

    private static final String CONTENT_TYPE = "application/octet-stream";

    @Inject
    private LogFileService _logFileService;

    private List<ManageLogFile> _listLogFiles = new ArrayList<>( );

    /**
     * Builds the list of the log files.
     *
     * @param model
     *            the model
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_DEFAULT, defaultView = true )
    public String getLogs( Models model, HttpServletRequest request )
    {
        _listLogFiles = new ArrayList<>( _logFileService.getLogFiles( ) );

        if ( _listLogFiles.isEmpty( ) )
        {
            AppLogService.debug( "No log file found" );
        }

        model.put( MARK_LIST_LOGS, _listLogFiles );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_LOGPROPERTIESS, TEMPLATE_MANAGE_LOGPROPERTIESS, model );
    }

    /**
     * Sends a log file of the list shown last, from DoDownloadLog.jsp which writes nothing else.
     *
     * @param request
     *            The HTTP request
     * @return null once the file is sent, else the redirection to the list
     */
    @Action( value = ACTION_DOWNLOAD_LOG, securityTokenDisabled = true )
    public String downloadLog( HttpServletRequest request )
    {
        ManageLogFile logFile = getLogFile( request.getParameter( PARAMETER_ID_LOG ) );

        if ( logFile != null )
        {
            Path path = logFile.getPath( );

            try
            {
                download( Files.readAllBytes( path ), path.getFileName( ).toString( ), CONTENT_TYPE );
                return null;
            }
            catch( IOException e )
            {
                AppLogService.error( "Error downloading file {}", path, e );
            }
        }

        return redirectView( request, VIEW_DEFAULT );
    }

    /**
     * Returns the log file of the list shown last whose number is given.
     *
     * @param strId
     *            the number of the file
     * @return the log file, or null when the number is not one of the list
     */
    private ManageLogFile getLogFile( String strId )
    {
        int nIdFile;

        try
        {
            nIdFile = Integer.parseInt( strId );
        }
        catch( NumberFormatException e )
        {
            AppLogService.error( "Error, log number invalid ({})", strId );
            return null;
        }

        if ( nIdFile < 0 || nIdFile >= _listLogFiles.size( ) )
        {
            AppLogService.error( "Error, log number invalid ({})", nIdFile );
            return null;
        }

        return _listLogFiles.get( nIdFile );
    }
}
