/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

import net.sf.hale.Game;

/**
 * Class containing methods for sending output to the log files
 * and, when debug modes are enabled, to the standard output
 *
 * @author Jared Stephen
 */

public class Logger
{
    private static long lastWarningTimeMillis = 0l;

    /**
     * Appends the specified warning message to the
     * warning log at "log/warning.log".  If {@link net.sf.hale.Config#isWarningModeEnabled()}
     * then the warning will also be displayed on the standard output.
     *
     * @param context the warning message to display
     */

    public static void appendToWarningLog( String context )
    {
        if ( Game.config == null || Game.config.isWarningModeEnabled( ) )
        {
            System.out.println( context );
        }

        PrintWriter out = getWarningPrintWriter( );

        out.println( context );

        out.close( );
    }

    /**
     * Appends the specified error message with a stack trace to the error log at "log/error.log".
     * If {@link net.sf.hale.Config#isDebugModeEnabled()} then the error (but not the stack trace)
     * will also be displayed on the standard error output
     *
     * @param context the error message to display
     */

    public static void appendToErrorLog( String context )
    {
        if ( Game.config == null || Game.config.isDebugModeEnabled( ) )
        {
            System.err.println( context );
        }

        PrintWriter out = getErrorPrintWriter( );

        out.print( context );
        out.println( );

        out.print( "Stack Trace:" );
        out.println( );
        new Exception( ).printStackTrace( out );
        out.println( );

        out.close( );
    }

    /**
     * Appends the specified error message with the specified stack trace to the error log at
     * "log/error.log".  If {@link net.sf.hale.Config#isDebugModeEnabled()} then the error and
     * stack trace will also be displayed on the standard error output.
     *
     * @param context   the error message to display
     * @param exception the exception generating the stack trace to display
     */

    public static void appendToErrorLog( String context, Throwable exception )
    {
        if ( Game.config == null || Game.config.isDebugModeEnabled( ) )
        {
            System.err.println( context );
            exception.printStackTrace( );
        }

        PrintWriter out = getErrorPrintWriter( );

        out.print( context );
        out.println( );

        exception.printStackTrace( out );
        out.println( );

        // print the causes recursively
        Throwable currentException = exception;
        while ( ( currentException = currentException.getCause( ) ) != null )
        {
            out.println( "Caused by" );
            currentException.printStackTrace( out );
            out.println( );
        }

        out.close( );
    }

    private static PrintWriter getErrorPrintWriter( )
    {
        PrintWriter writer = getPrintWriter( "error" );
        writer.print( "Log entry created " + Calendar.getInstance( ).getTime( ).toString( ) );
        writer.println( );

        return writer;
    }

    private static PrintWriter getWarningPrintWriter( )
    {
        PrintWriter writer = getPrintWriter( "warning" );

        long curTime = System.currentTimeMillis( );
        if ( curTime > lastWarningTimeMillis + 1000l )
        {
            lastWarningTimeMillis = curTime;
            writer.print( "Log entry created " + Calendar.getInstance( ).getTime( ).toString( ) );
            writer.println( );
        }

        return writer;
    }

    private static PrintWriter getPrintWriter( String name )
    {
        try
        {
            File dir = new File( Game.plataform.getLogDirectory() );
            if ( ! dir.exists( ) || ! dir.isDirectory( ) )
            {
                dir.mkdir( );
            }

            File fout = new File( Game.plataform.getLogDirectory() + name + ".log" );

            BufferedWriter out = new BufferedWriter( new FileWriter( fout, true ) );
            PrintWriter pout = new PrintWriter( out );

            return pout;

        }
        catch ( Exception e )
        {
            System.err.println( "Error attempting to get print writer for log: " + name );
            e.printStackTrace( );
        }

        return null;
    }
}
