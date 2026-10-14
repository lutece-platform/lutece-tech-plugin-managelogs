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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;

/**
 * Reads the file appenders of a Log4j 2 core configuration. Loaded only when Log4j 2 core is the logging implementation.
 */
final class Log4jCoreAppenders
{
    /**
     * Private constructor.
     */
    private Log4jCoreAppenders( )
    {
    }

    /**
     * Returns the file and the roll pattern of each file appender of the configuration of a context.
     *
     * @param context
     *            the Log4j 2 core logger context
     * @return the file of each file appender, with its roll pattern or null
     */
    static List<AppenderFile> getAppenderFiles( Object context )
    {
        List<AppenderFile> listFiles = new ArrayList<>( );

        for ( Appender appender : ( (LoggerContext) context ).getConfiguration( ).getAppenders( ).values( ) )
        {
            if ( appender instanceof FileAppender )
            {
                FileAppender fileAppender = (FileAppender) appender;
                listFiles.add( new AppenderFile( fileAppender.getFileName( ), null ) );
            }
            else if ( appender instanceof RollingFileAppender )
            {
                RollingFileAppender rollingAppender = (RollingFileAppender) appender;
                listFiles.add( new AppenderFile( rollingAppender.getFileName( ), rollingAppender.getFilePattern( ) ) );
            }
            else if ( appender instanceof RandomAccessFileAppender )
            {
                RandomAccessFileAppender randomAppender = (RandomAccessFileAppender) appender;
                listFiles.add( new AppenderFile( randomAppender.getFileName( ), null ) );
            }
            else if ( appender instanceof RollingRandomAccessFileAppender )
            {
                RollingRandomAccessFileAppender rollingRandomAppender = (RollingRandomAccessFileAppender) appender;
                listFiles.add( new AppenderFile( rollingRandomAppender.getFileName( ), rollingRandomAppender.getFilePattern( ) ) );
            }
        }

        return listFiles;
    }

    /**
     * The file of a file appender and its roll pattern (a record in the Lutece 8 version, Java 11 here).
     */
    static final class AppenderFile
    {
        private final String _strFileName;
        private final String _strFilePattern;

        /**
         * Builds the file of a file appender.
         *
         * @param fileName
         *            the file name
         * @param filePattern
         *            the roll pattern, or null
         */
        AppenderFile( String fileName, String filePattern )
        {
            _strFileName = fileName;
            _strFilePattern = filePattern;
        }

        /**
         * Returns the file name.
         *
         * @return the file name
         */
        String fileName( )
        {
            return _strFileName;
        }

        /**
         * Returns the roll pattern.
         *
         * @return the roll pattern, or null
         */
        String filePattern( )
        {
            return _strFilePattern;
        }
    }
}
