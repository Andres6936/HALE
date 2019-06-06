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

import java.util.ArrayList;

import net.sf.hale.Game;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Creature;
import net.sf.hale.widgets.BasePortraitViewer;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A popup window for showing the contents of a conversation including a list
 * of possible responses.
 *
 * @author Jared Stephen
 */

public class ConversationPopup extends PopupWindow
{
    private Entity parent, target;
    private Scriptable script;

    // current widget state
    private StringBuilder text;
    private ArrayList< ResponseWidget > responses;

    private Label parentName, targetName;
    private BasePortraitViewer parentPortrait, targetPortrait;

    private Content content;

    private boolean closeCalled = false;

    /**
     * Creates a new ConversationPopup which will show the specified conversation
     *
     * @param parent the parent entity which initiated the conversation
     * @param target the target entity being talked to
     * @param script the conversation script to use
     */

    public ConversationPopup( Entity parent, Entity target, Scriptable script )
    {
        super( Game.mainViewer );
        this.parent = parent;
        this.target = target;

        setCloseOnClickedOutside( false );
        setCloseOnEscape( false );

        this.script = script;

        content = new Content( );
        add( content );

        if ( parent instanceof Creature )
        {
            parentPortrait = new PortraitViewer( ( Creature ) parent );
            parentName = new Label( parent.getTemplate( ).getName( ) );
        }

        if ( target instanceof Creature )
        {
            targetPortrait = new PortraitViewer( ( Creature ) target );
            targetName = new Label( target.getTemplate( ).getName( ) );
        }
    }

    /**
     * Initiates the conversation by running the "startConversation" script, laying out this PopupWindow
     * and adding this PopupWindow to the list of popups to be shown by the mainViewer
     */

    public void startConversation( )
    {
        responses = new ArrayList< ResponseWidget >( );
        text = new StringBuilder( );

        this.script.executeFunction( ScriptFunctionType.startConversation, parent, target, this );

        // add text and responses added from script to content
        content.addWidgets( );

        // if close was called in the start conversation script, don't show the popup
        if ( ! closeCalled )
        { Game.mainViewer.showPopup( this ); }
    }

    /**
     * Used by conversation scripts to add the specified string to the text currently
     * shown for this conversation.
     *
     * @param text the raw String to add
     */

    public void addString( String text )
    {
        this.text.append( text );
    }

    /**
     * Used by conversation scripts to add the specified string to the text currently
     * shown for this conversation.  The string is automatically given the default formatting.
     *
     * @param text the text to add
     */

    public void addText( String text )
    {
        this.text.append( "<div style=\"margin-top: 1em;\">" ).append( text ).append( "</div>" );
    }

    public void addTextWithFont( String text, String font )
    {
        this.text.append( "<div style=\"margin-top: 1em; font-family: " + font + "\">" ).append( text ).append( "</div>" );
    }

    /**
     * Adds a response widget with the specified text to the list of responses
     * currently shown for this conversation
     *
     * @param text     the text to show for the response.  can be HTML formatted
     * @param function the callback function to call if this response is selected
     */

    public void addResponse( String text, String function )
    {
        responses.add( new ResponseWidget( text, function ) );
    }

    public void addResponse( String text, String function, Object arg )
    {
        ResponseWidget widget = new ResponseWidget( text, function );
        widget.arg = arg;

        responses.add( widget );
    }

    /**
     * Exits the current conversation by closing the ConversationPopup
     */

    public void exit( )
    {
        closePopup( );
        closeCalled = true;
    }

    private class ResponseWidget extends Button implements Runnable
    {
        private String function;
        private Object arg;

        private final ResponseArea textArea;
        private final HTMLTextAreaModel textAreaModel;

        private ResponseWidget( String text, String function )
        {
            this.function = function;

            textAreaModel = new HTMLTextAreaModel( );
            textArea = new ResponseArea( textAreaModel );
            add( textArea );

            addCallback( this );

            textAreaModel.setHtml( text );
        }

        @Override
        public int getPreferredHeight( )
        {
            return textArea.getPreferredHeight( ) + getBorderVertical( );
        }

        @Override
        public int getPreferredWidth( )
        {
            return Short.MAX_VALUE;
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            layoutChildFullInnerArea( textArea );
        }

        @Override
        public void run( )
        {
            content.responseContent.removeAllChildren( );
            content.removeAllChildren( );
            responses.clear( );
            text = new StringBuilder( );

            script.executeFunction( function, parent, target, ConversationPopup.this, arg );

            // add text and responses added from script to content
            content.addWidgets( );
        }

        private class ResponseArea extends TextArea
        {
            private ResponseArea( HTMLTextAreaModel model )
            {
                super( model );
            }

            @Override
            protected boolean handleEvent( Event evt )
            {
                // do not handle any events to allow clicks to go through
                // to the button holding this textarea
                return false;
            }
        }
    }

    private class PortraitViewer extends BasePortraitViewer
    {
        private PortraitViewer( Creature creature )
        {
            super( creature );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            // don't show the background image if there is no portrait sprite
            if ( getPortraitSpriteHeight( ) == 0 )
            {
                setBackground( null );
                setOverlay( null );
            }
        }
    }

    private class Content extends Widget
    {
        private final TextArea textArea;
        private final HTMLTextAreaModel textAreaModel;
        private final ScrollPane textPane;
        private final ScrollPane responsePane;
        private final ResponseContent responseContent;

        private Content( )
        {
            textAreaModel = new HTMLTextAreaModel( );
            textArea = new TextArea( textAreaModel );
            textPane = new ScrollPane( textArea );
            textPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
            textPane.setTheme( "dialogpane" );

            responseContent = new ResponseContent( );
            responseContent.setTheme( "content" );
            responsePane = new ScrollPane( responseContent );
            responsePane.setFixed( ScrollPane.Fixed.HORIZONTAL );
            responsePane.setTheme( "responsepane" );
        }

        private void addWidgets( )
        {
            if ( parentPortrait != null )
            {
                add( parentPortrait );
                add( parentName );
            }

            if ( targetPortrait != null )
            {
                add( targetPortrait );
                add( targetName );
            }

            add( textPane );
            add( responsePane );

            for ( ResponseWidget widget : responses )
            {
                responseContent.add( widget );
            }

            textAreaModel.setHtml( text.toString( ) );
        }

        @Override
        public int getPreferredWidth( )
        {
            return getMinWidth( );
        }

        @Override
        public int getPreferredHeight( )
        {
            return getMinHeight( );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            int portraitBottom = getInnerY( );

            if ( parentPortrait != null )
            {
                parentName.setSize( parentName.getPreferredWidth( ), parentName.getPreferredHeight( ) );
                parentName.setPosition( getInnerX( ) + 3 * getInnerWidth( ) / 4 - parentName.getWidth( ) / 2,
                                        getInnerY( ) );

                parentPortrait.setSize( parentPortrait.getPreferredWidth( ), parentPortrait.getPreferredHeight( ) );
                parentPortrait.setPosition( getInnerX( ) + 3 * getInnerWidth( ) / 4 - parentPortrait.getWidth( ) / 2,
                                            parentName.getBottom( ) );
                portraitBottom = parentPortrait.getBottom( );
            }

            if ( targetPortrait != null )
            {
                targetName.setSize( targetName.getPreferredWidth( ), targetName.getPreferredHeight( ) );
                targetName.setPosition( getInnerX( ) + getInnerWidth( ) / 4 - targetName.getWidth( ) / 2,
                                        getInnerY( ) );

                targetPortrait.setSize( targetPortrait.getPreferredWidth( ), targetPortrait.getPreferredHeight( ) );
                targetPortrait.setPosition( getInnerX( ) + getInnerWidth( ) / 4 - targetPortrait.getWidth( ) / 2,
                                            targetName.getBottom( ) );
                portraitBottom = Math.max( portraitBottom, targetPortrait.getBottom( ) );
            }

            int contentHeight = responseContent.getPreferredHeight( ) + responsePane.getBorderVertical( );
            responsePane.setSize( getInnerWidth( ), Math.min( responsePane.getMaxHeight( ), contentHeight ) );
            responsePane.setPosition( getInnerX( ), getInnerBottom( ) - responsePane.getHeight( ) );

            int y = responsePane.getY( );
            if ( y - portraitBottom < 0 )
            { textPane.setSize( getInnerWidth( ), 0 ); }
            else
            { textPane.setSize( getInnerWidth( ), y - portraitBottom ); }

            textPane.setPosition( getInnerX( ), portraitBottom );
        }

        private class ResponseContent extends Widget
        {
            @Override
            public void invalidateLayout( )
            {
                super.invalidateLayout( );

                Content.this.invalidateLayout( );
            }

            @Override
            protected void layout( )
            {
                super.layout( );

                int curY = getInnerY( );
                for ( ResponseWidget widget : responses )
                {
                    widget.setSize( getInnerWidth( ), widget.getPreferredHeight( ) );
                    widget.setPosition( getInnerX( ), curY );
                    curY = widget.getBottom( );
                }
            }

            @Override
            public int getPreferredHeight( )
            {
                int height = getBorderVertical( );
                for ( ResponseWidget widget : responses )
                {
                    height += widget.getPreferredHeight( );
                }

                return height;
            }
        }
    }
}