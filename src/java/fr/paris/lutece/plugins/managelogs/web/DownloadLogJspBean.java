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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.paris.lutece.plugins.managelogs.business.ManageLogFile;
import fr.paris.lutece.plugins.managelogs.service.LogFileService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
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
import jakarta.servlet.http.HttpServletResponse;

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

    private static final String ERROR_INVALID_LOG = "managelogs.manage_logdownload.error.invalid";

    private static final String ERROR_DOWNLOAD_LOG = "managelogs.manage_logdownload.error.download";

    private static final String CONTENT_TYPE = "application/octet-stream";

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    private static final String CACHE_CONTROL_NO_STORE = "no-store";

    @Inject
    private LogFileService _logFileService;

    private List<ManageLogFile> _listLogFiles = new ArrayList<>( );

    // the core keeps the response private : kept here to stream the file
    private transient HttpServletResponse _response;

    /**
     * Keeps the response, to stream the file to download.
     *
     * @param request
     *            The HTTP request
     * @param response
     *            The HTTP response
     * @return The page
     * @throws AccessDeniedException
     *             if the user has not the right
     */
    @Override
    public String processController( HttpServletRequest request, HttpServletResponse response ) throws AccessDeniedException
    {
        _response = response;

        return super.processController( request, response );
    }

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
     * Sends a log file of the list shown last, from DoDownloadLog.jsp which writes nothing else. The file is streamed, not
     * loaded in memory, up to its size when the download starts, the file being possibly still written.
     *
     * @param request
     *            The HTTP request
     * @return null once the file is sent, else the redirection to the list with an error
     */
    @Action( value = ACTION_DOWNLOAD_LOG, securityTokenDisabled = true )
    public String downloadLog( HttpServletRequest request )
    {
        ManageLogFile logFile = getLogFile( request.getParameter( PARAMETER_ID_LOG ) );

        if ( logFile == null )
        {
            addError( ERROR_INVALID_LOG, getLocale( ) );
            return redirectView( request, VIEW_DEFAULT );
        }

        Path path = logFile.getPath( );

        try ( InputStream inputStream = Files.newInputStream( path ) )
        {
            long lSize = Files.size( path );

            _response.setContentType( CONTENT_TYPE );
            _response.setHeader( HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName( ).toString( ).replace( '"', '_' ) + "\"" );
            _response.setHeader( HEADER_CACHE_CONTROL, CACHE_CONTROL_NO_STORE );
            _response.setContentLengthLong( lSize );

            OutputStream outputStream = _response.getOutputStream( );
            IOUtils.copyLarge( inputStream, outputStream, 0, lSize );
            outputStream.flush( );
        }
        catch( IOException e )
        {
            AppLogService.error( "Error downloading file {}", path, e );

            if ( !_response.isCommitted( ) )
            {
                _response.reset( );
                addError( ERROR_DOWNLOAD_LOG, getLocale( ) );
                return redirectView( request, VIEW_DEFAULT );
            }
        }

        return null;
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
