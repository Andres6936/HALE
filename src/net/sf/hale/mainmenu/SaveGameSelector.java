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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import net.sf.hale.Game;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SaveFileHeader;
import net.sf.hale.util.SaveGameUtil;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A widget for viewing a single specified save game and its
 * header properties
 *
 * @author Jared Stephen
 */

public class SaveGameSelector extends AbstractSaveGamePopup.Selector
{
    private String saveGame;

    private Label name, date, partyName, partyLevel, area;
    private int nameDateGap, partyLevelGap;

    private boolean versionIDMatchesCurrentVersion;

    /**
     * Creates a new SaveGameSelector viewing the specified save game with the
     * specified date format for the last modified time label
     *
     * @param saveGame         the save game to view
     * @param simpleDateFormat the SimpleDateFormat used to format the date label
     *                         displaying the last modified time for the save game
     */

    public SaveGameSelector( String saveGame, DateFormat simpleDateFormat )
    {
        this.saveGame = saveGame;

        File saveFile = SaveGameUtil.getSaveFile( saveGame );
        Date saveDate = new Date( saveFile.lastModified( ) );

        name = new Label( saveGame );
        name.setTheme( "namelabel" );
        add( name );

        date = new Label( simpleDateFormat.format( saveDate ) );
        date.setTheme( "datelabel" );
        add( date );

        partyName = new Label( );
        partyName.setTheme( "partynamelabel" );
        add( partyName );

        partyLevel = new Label( );
        partyLevel.setTheme( "partylevellabel" );
        add( partyLevel );

        area = new Label( );
        area.setTheme( "arealabel" );
        add( area );

        FileInputStream fin = null;
        try
        {
            fin = new FileInputStream( saveFile );
            GZIPInputStream gz = new GZIPInputStream( fin );

            SaveFileHeader header = SaveFileHeader.read( gz );
            fin.close( );

            partyName.setText( header.getPartyNamesString( ) );
            partyLevel.setText( "Level " + header.getPartyLevel( ) );
            area.setText( header.getAreaName( ) );

            String versionID = header.getVersionID( );
            versionIDMatchesCurrentVersion = Game.config.getVersionID( ).equals( versionID );

        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error reading header for save game file " + saveFile.getPath( ), e );
        }
        finally
        {
            if ( fin != null )
            {
                try
                {
                    fin.close( );
                }
                catch ( IOException e )
                {
                    Logger.appendToErrorLog( "Error closing file " + saveFile.getPath( ), e );
                }
            }
        }
    }

    @Override
    public boolean checkVersionID( )
    {
        return versionIDMatchesCurrentVersion;
    }

    /**
     * Returns the save game ID string that was used in the creation of this selector
     *
     * @return the save game ID string
     */

    @Override
    public String getSaveGame( )
    {
        return saveGame;
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        nameDateGap = themeInfo.getParameter( "nameDateGap", 0 );
        partyLevelGap = themeInfo.getParameter( "partyLevelGap", 0 );
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        int centerY = getInnerY( ) + getInnerHeight( ) / 2;

        name.setPosition( getInnerX( ), centerY );

        int columnX = getInnerX( ) + name.getPreferredWidth( ) + nameDateGap;

        area.setPosition( columnX, centerY );
        partyName.setPosition( columnX, centerY - partyName.getPreferredHeight( ) );
        partyLevel.setPosition( columnX + partyName.getPreferredWidth( ) + partyLevelGap, partyName.getY( ) );

        date.setPosition( columnX, centerY + date.getPreferredHeight( ) );
    }

    @Override
    public int getPreferredHeight( )
    {
        int heightLeft = name.getPreferredHeight( );
        int heightRight = date.getPreferredHeight( ) + partyName.getPreferredHeight( ) +
                partyLevel.getPreferredHeight( );

        return Math.max( heightLeft, heightRight );
    }
}