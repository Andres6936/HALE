/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import net.sf.hale.Game;
import net.sf.hale.util.Logger;

/**
 * The class that checks to see if an update is available
 *
 * @author Jared
 */

public class CheckForUpdatesTask extends Thread
{
    private MainMenu mainMenu;

    private InputStream in;

    /**
     * Creates a new task which will check for updates
     *
     * @param mainMenu the mainmenu to notify if an update is found
     */

    public CheckForUpdatesTask( MainMenu mainMenu )
    {
        this.mainMenu = mainMenu;
    }

    @Override
    public void run( )
    {
        try
        {
            in = new URL( "http://www.halegame.com/version.txt" ).openStream( );

            BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
            String serverVersion = reader.readLine( );

            if ( ! serverVersion.equals( mainMenu.getVersion( ) ) )
            {
                mainMenu.enableUpdate( );

                new File( Game.plataform.getConfigDirectory( ) + "updateAvailable.txt" ).createNewFile( );
            }

        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error checking for updates", e );
        }

        if ( in != null )
        {
            try
            {
                in.close( );
            }
            catch ( IOException e )
            {
                Logger.appendToErrorLog( "Error closing input stream while checking for updates", e );
            }
        }
    }
}
