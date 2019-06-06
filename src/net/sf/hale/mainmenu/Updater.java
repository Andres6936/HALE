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

package net.sf.hale.mainmenu;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import net.sf.hale.util.FileUtil;
import net.sf.hale.Game;
import net.sf.hale.util.Logger;

/**
 * The class that calls the updater which in turn will restart hale
 *
 * @author Jared
 */

public class Updater extends Thread
{
    private String argument;
    private String updaterCommand;

    private boolean canceled;

    private UpdatePopup popup;

    /**
     * Creates a new Updater instance
     */

    public Updater( )
    {
        try
        {
            argument = Game.getProgramCommand( );
        }
        catch ( IOException e )
        {
            Logger.appendToErrorLog( "Error getting program restart command", e );
        }

        updaterCommand = System.getProperty( "java.home" ) + "/bin/java -jar updater.jar" + " " + argument;
    }

    /**
     * Cancels the current update task
     */

    public void cancel( )
    {
        canceled = true;
    }

    /**
     * Restarts hale and applies the update
     *
     * @param mainMenu
     */

    public void runUpdater( MainMenu mainMenu )
    {
        this.popup = new UpdatePopup( mainMenu, this );
        popup.openPopupCentered( );

        this.start( );
    }

    private void finish( )
    {
        // remove the updates available file
        new File( Game.getConfigBaseDirectory( ) + "updateAvailable.txt" ).delete( );

        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            @Override
            public void run( )
            {
                try
                {
                    Runtime.getRuntime( ).exec( updaterCommand );
                }
                catch ( IOException e )
                {
                    e.printStackTrace( );
                }
            }
        } );

        System.exit( 0 );
    }

    private void downloadUpdater( String downloadURL )
    {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
            // delete the old updater file
            File file = new File( "updater.jar" );
            file.delete( );

            // download the new file
            in = new BufferedInputStream( new URL( downloadURL ).openStream( ) );
            fout = new FileOutputStream( file );

            byte[] buffer = new byte[ 2048 ];
            int count;
            while ( ( count = in.read( buffer, 0, 2048 ) ) != - 1 )
            {
                if ( canceled ) break;

                // write the bytes to the file
                fout.write( buffer, 0, count );
            }

        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error downloading updater", e );
        }

        try
        {
            if ( in != null )
            { in.close( ); }

            if ( fout != null )
            { fout.close( ); }

        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error closing stream", e );
        }
    }

    @Override
    public void run( )
    {
        popup.updateCurrentTask( "Checking updater version." );

        String localVersion = FileUtil.getMD5Sum( new File( "updater.jar" ) );
        String serverVersion = null;
        String downloadURL = null;

        InputStream in = null;

        try
        {
            in = new URL( "http://www.halegame.com/updater-version.txt" ).openStream( );

            BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
            serverVersion = reader.readLine( ).toUpperCase( );
            downloadURL = reader.readLine( );

        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error checking updater version", e );
            popup.updateCurrentTask( "Error: Unable to update." );
        }

        if ( in != null )
        {
            try
            {
                in.close( );
            }
            catch ( IOException e )
            {
                Logger.appendToErrorLog( "Error closing input stream while checking for updater version", e );
            }
        }

        if ( canceled ) return;

        if ( ! localVersion.equals( serverVersion ) )
        {
            // we need to update the updater
            popup.updateCurrentTask( "Downloading updater." );

            downloadUpdater( downloadURL );

            if ( canceled ) return;

            finish( );

        }
        else
        {
            // we do not need to do any updates
            finish( );
        }
    }
}
