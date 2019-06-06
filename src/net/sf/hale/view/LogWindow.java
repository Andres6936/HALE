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

package net.sf.hale.view;

import net.sf.hale.Game;
import net.sf.hale.rules.QuestEntry;

import de.matthiasmann.twl.TabbedPane;

/**
 * The GameSubWindow showing party specific information including the quest log
 * and available recipes list
 *
 * @author Jared Stephen
 */

public class LogWindow extends GameSubWindow
{
    private final TabbedPane content;

    private final TabbedPane.Tab questTab;

    private final RecipeSetViewer recipeViewer;
    private final QuestSetViewer questViewer;

    /**
     * Creates a new empty LogWindow.  You must first call {@link #updateContent()}
     * prior to showing the LogWindow
     */

    public LogWindow( )
    {
        this.setTitle( "Log" );

        content = new TabbedPane( );
        content.setTheme( "content" );
        add( content );

        questViewer = new QuestSetViewer( );
        questTab = content.addTab( "Quests", questViewer );

        recipeViewer = new RecipeSetViewer( false );
        content.addTab( "Recipes", recipeViewer );
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        content.setPosition( getInnerX( ), getInnerY( ) );
        content.setSize( getInnerWidth( ), getInnerBottom( ) - content.getY( ) );
    }

    @Override
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        if ( visible && content.getActiveTab( ) == questTab )
        {
            Game.mainViewer.getMainPane( ).setLogNotification( false );
        }
    }

    /**
     * Updates the current content being viewed by this LogWindow with any
     * new quest entries, sub entries, or recipes.  This method will also clear
     * the new log entry notification in the main pane if this widget is visible
     * and the quest tab is active
     */

    public void updateContent( )
    {
        recipeViewer.updateContent( );
        questViewer.updateContent( );

        if ( isVisible( ) && content.getActiveTab( ) == questTab )
        {
            Game.mainViewer.getMainPane( ).setLogNotification( false );
        }
    }

    /**
     * Sets the new quest entry for the QuestSetViewer within this LogWindow
     * to the specified QuestEntry.  Also activates the notification within the
     * Log button on the main pane.  The quests tab is automatically shown.  Note
     * that if the specified entry does not show log notifications, no action is taken
     *
     * @param entry the new QuestEntry
     */

    public void notifyNewEntry( QuestEntry entry )
    {
        if ( ! entry.showsLogNotifications( ) ) return;

        questViewer.notifyNewEntry( entry );

        if ( ! isVisible( ) )
        {
            Game.mainViewer.getMainPane( ).setLogNotification( true );
        }

        content.setActiveTab( questTab );
    }
}
