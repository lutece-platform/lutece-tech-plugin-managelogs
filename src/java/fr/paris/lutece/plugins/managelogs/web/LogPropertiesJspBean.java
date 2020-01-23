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

import fr.paris.lutece.plugins.managelogs.business.LogProperties;
import fr.paris.lutece.plugins.managelogs.util.ManageLogsUtil;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.util.url.UrlItem;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static fr.paris.lutece.plugins.managelogs.web.AbstractManageLogsPropertiesJspBean.RIGHT_MANAGELOGSPROPERTIES;

/**
 * This class provides the user interface to manage LogProperties features ( manage, modify )
 */
@Controller( controllerJsp = "ManageLogProperties.jsp", controllerPath = "jsp/admin/plugins/managelogs/", right = RIGHT_MANAGELOGSPROPERTIES)
public class LogPropertiesJspBean extends AbstractManageLogsPropertiesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_LOGPROPERTIES = "/admin/plugins/managelogs/manage_logproperties.html";
    private static final String TEMPLATE_MODIFY_LOGPROPERTIES = "/admin/plugins/managelogs/modify_logproperties.html";

    // Parameters
    private static final String PARAMETER_ID_LOGPROPERTIES = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_LOGPROPERTIES = "managelogs.manage_logproperties.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_LOGPROPERTIES = "managelogs.modify_logproperties.pageTitle";

    // Markers
    private static final String MARK_LOGPROPERTIES = "logproperties";
    private static final String MARK_TMP_LOG_EXISTS = "tmplogexists";
    private static final String MARK_TMP_LOG_USED = "tmplogused";
    private static final String MARK_LOG_CONF_IN_USE = "conffile";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_LOGPROPERTIES = "managelogs.message.confirmRemoveLogProperties";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "managelogs.model.entity.logproperties.attribute.";

    // Views
    private static final String VIEW_MANAGE_LOGPROPERTIES = "manageLogProperties";
    private static final String VIEW_MODIFY_LOGPROPERTIES = "modifyLogProperties";

    // Actions
    private static final String ACTION_MODIFY_LOGPROPERTIES = "modifyLogProperties";
    private static final String ACTION_REMOVE_LOGPROPERTIES = "removeLogProperties";
    private static final String ACTION_CONFIRM_REMOVE_LOGPROPERTIES = "confirmRemoveLogProperties";

    // Infos
    private static final String INFO_LOGPROPERTIES_UPDATED = "managelogs.info.logproperties.updated";
    private static final String INFO_LOGPROPERTIES_REMOVED = "managelogs.info.logproperties.removed";

    // Errors
    private static final String ERROR_LOGPROPERTIES_WRITE = "managelogs.error.logproperties.write";
    private static final String ERROR_LOGPROPERTIES_DELETE = "managelogs.error.logproperties.delete";
    private static final String ERROR_LOGPROPERTIES_CONF_ERROR = "managelogs.error.logproperties.configuration";

    // Session variable to store working values
    private LogProperties _logproperties;

    //Constants
    private static final String LOG_CONF_CHANGE =    "Log configuration changed to use ";
    
    /**
     * Build the Manage View
     * @param request The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_LOGPROPERTIES, defaultView = true )
    public String getManageLogProperties( HttpServletRequest request )
    {
        _logproperties = new LogProperties();
        Map<String, Object> model = new HashMap<>(  );

        // check if tmp log file exists
        if ( ManageLogsUtil.isFileReadable( TMP_LOG_ABSOLUTE ) )
        {
            _logproperties.setCurrentProperties( getTmpLogFileContent() );
            model.put( MARK_TMP_LOG_EXISTS, "true" );
        }
        else
        {
            _logproperties.setCurrentProperties( StringUtils.EMPTY );
            model.put( MARK_TMP_LOG_EXISTS, "false" );
        }
        _logproperties.setDefaultProperties( "# insert log4j property values here #" );

        // get currently used log configuration file
        String log4jConfigFile = null;
        if (!APP_SERVER_MULTI_WEBAPP)
        {
            log4jConfigFile = System.getProperty( "log4j.configuration" );
        }

        if ( ManageLogsUtil.isFileReadable( log4jConfigFile ) )
        {
            model.put( MARK_LOG_CONF_IN_USE, log4jConfigFile );

            // check whether tmp conf file is used
            if (TMP_LOG_ABSOLUTE.equalsIgnoreCase( log4jConfigFile ))
            {
                model.put( MARK_TMP_LOG_USED, "true" );
            }
            else
            {
                model.put( MARK_TMP_LOG_USED, "false" );
            }
        }
        else
        {
            model.put( MARK_LOG_CONF_IN_USE, "Undefined" );
            model.put( MARK_TMP_LOG_USED, "Undefined" );
        }


        model.put( MARK_LOGPROPERTIES, _logproperties );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_LOGPROPERTIES, TEMPLATE_MANAGE_LOGPROPERTIES, model );
    }


    /**
     * Returns the form to update info about a logproperties
     *
     * @param request The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_LOGPROPERTIES )
    public String getModifyLogProperties( HttpServletRequest request )
    {
        if ( _logproperties == null )
        {
            _logproperties = new LogProperties();
            _logproperties.setDefaultProperties( "# insert log4j property values here #" );
            if ( !Files.isReadable( Paths.get( TMP_LOG_ABSOLUTE ) ) )
            {
                _logproperties.setCurrentProperties( StringUtils.EMPTY );
            }
            else
            {
                _logproperties.setCurrentProperties( getTmpLogFileContent() );
            }
        }

        Map<String, Object> model = getModel(  );
        model.put( MARK_LOGPROPERTIES, _logproperties );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_LOGPROPERTIES, TEMPLATE_MODIFY_LOGPROPERTIES, model );
    }

    /**
     * Process the change form of a logproperties
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_LOGPROPERTIES )
    public String doModifyLogProperties( HttpServletRequest request )
    {
        AppLogService.debug( "Begin modify log configuration" );

        populate( _logproperties, request );

        // Check constraints
        if ( !validateBean( _logproperties, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_LOGPROPERTIES, PARAMETER_ID_LOGPROPERTIES, _logproperties.getId( ) );
        }

        if (_logproperties.getCurrentProperties()!=null && !_logproperties.getCurrentProperties().trim().isEmpty())
        {
            // Check if path in the new configuration is in one of the right folder
            try
            {
                getFilesFromConfiguration( Arrays.asList( _logproperties.getCurrentProperties( ).split( "\n" ) ), true);
            }
            catch ( AccessDeniedException e )
            {
                // one or more file appender is writing to a non allowed folder
                addError( ERROR_LOGPROPERTIES_CONF_ERROR, request.getLocale() );
                return redirect( request, VIEW_MODIFY_LOGPROPERTIES, PARAMETER_ID_LOGPROPERTIES, _logproperties.getId( ) );
            }

            // Create temporary log4j file
            try
            {
                createTempLogConfFile( request );
            } catch ( IOException e )
            {
                return redirect( request, VIEW_MODIFY_LOGPROPERTIES, PARAMETER_ID_LOGPROPERTIES, _logproperties.getId( ) );
            }

            // reload log4j file
            AppLogService.init( TMP_LOG_PATH, TMP_LOG_FILE_NAME );
            AppLogService.info( LOG_CONF_CHANGE + TMP_LOG_PATH + ( TMP_LOG_PATH.endsWith( SLASH ) ? EMPTY : SLASH ) + TMP_LOG_FILE_NAME );
        }
        else
        {
            // Use standard Lutece log file
            deleteTempLogConfFile( request );
            // reload log4j file
            AppLogService.init( LUTECE_LOG_PATH, LUTECE_LOG_FILE );
            AppLogService.info( LOG_CONF_CHANGE + LUTECE_LOG_PATH + ( LUTECE_LOG_PATH.endsWith( SLASH ) ? EMPTY : SLASH ) + LUTECE_LOG_FILE );
        }

        addInfo( INFO_LOGPROPERTIES_UPDATED, getLocale(  ) );
        return redirectView( request, VIEW_MANAGE_LOGPROPERTIES );
    }

    /**
     * Manages the removal form of a logproperties whose identifier is in the http
     * request
     *
     * @param request The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_LOGPROPERTIES )
    public String getConfirmRemoveLogProperties( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_LOGPROPERTIES ) );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_LOGPROPERTIES, url.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a logproperties
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage logproperties
     */
    @Action( ACTION_REMOVE_LOGPROPERTIES )
    public String doRemoveLogProperties( HttpServletRequest request )
    {
        // Use standard Lutece log file
        deleteTempLogConfFile( request );
        // reload log4j file
        AppLogService.init( LUTECE_LOG_PATH, LUTECE_LOG_FILE );
        AppLogService.info( LOG_CONF_CHANGE + LUTECE_LOG_PATH + ( LUTECE_LOG_PATH.endsWith( SLASH ) ? EMPTY : SLASH ) + LUTECE_LOG_FILE );

        addInfo( INFO_LOGPROPERTIES_REMOVED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_LOGPROPERTIES );
    }

    /**
     * Create the temporary log configuration
     * @param request The Http request
     * @throws IOException error writing the file
     */
    private void createTempLogConfFile( HttpServletRequest request ) throws IOException
    {
        try
        {
            // no option to write method means CREATE, TRUNCATE_EXISTING, and WRITE
            Files.write( Paths.get( TMP_LOG_ABSOLUTE ), _logproperties.getCurrentProperties( ).getBytes( Charset.defaultCharset() ) );
        }
        catch ( IOException e )
        {
            // Error when file not writable. Eg.: path does not exists
            AppLogService.error( "Error writing file: " + TMP_LOG_PATH + ( TMP_LOG_PATH.endsWith( SLASH ) ? EMPTY : SLASH ) + TMP_LOG_FILE_NAME, e );
            addError( ERROR_LOGPROPERTIES_WRITE , request.getLocale() );

            throw e;
        }
    }

    /**
     * Delete the temporary log configuration
     * @param request The Http request
     */
    private void deleteTempLogConfFile( HttpServletRequest request )
    {
        try
        {
            boolean isDeleted = Files.deleteIfExists(  Paths.get(TMP_LOG_PATH_ABSOLUTE + (TMP_LOG_PATH_ABSOLUTE.endsWith(SLASH) ? EMPTY : SLASH) + TMP_LOG_FILE_NAME) );
            if (!isDeleted)
            {
                AppLogService.error( "Error deleting file: " + TMP_LOG_PATH_ABSOLUTE + ( TMP_LOG_PATH_ABSOLUTE.endsWith( SLASH ) ? EMPTY : SLASH ) + TMP_LOG_FILE_NAME );
                addError( ERROR_LOGPROPERTIES_DELETE, request.getLocale() );
            }
        }
        catch ( IOException e )
        {
            AppLogService.error( "Error deleting file: " + TMP_LOG_PATH_ABSOLUTE + ( TMP_LOG_PATH_ABSOLUTE.endsWith( SLASH ) ? EMPTY : SLASH ) + TMP_LOG_FILE_NAME, e );
            addError( ERROR_LOGPROPERTIES_DELETE, request.getLocale() );
        }
    }

    private String getTmpLogFileContent()
    {
        String retour = StringUtils.EMPTY;

        List<String> lines;
        try
        {
            lines = Files.readAllLines( Paths.get( TMP_LOG_PATH_ABSOLUTE + ( TMP_LOG_PATH_ABSOLUTE.endsWith(SLASH) ? EMPTY : SLASH) + TMP_LOG_FILE_NAME), Charset.defaultCharset( ) );
            StringBuilder properties = new StringBuilder( StringUtils.EMPTY );
            for ( String line: lines ) {
                properties.append( "\n" ).append( line );
            }
            retour = properties.toString();
        }
        catch ( IOException e )
        {
            AppLogService.error( "Error reading " + TMP_LOG_FILE_NAME, e );
        }

        return retour;
    }
}
