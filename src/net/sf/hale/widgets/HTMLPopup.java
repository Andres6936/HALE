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

package net.sf.hale.widgets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.textarea.TextAreaModel;
import net.sf.hale.Game;
import net.sf.hale.ability.DelayedScriptCallback;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.util.FileUtil;

/**
 * A popup window containing a text field showing some HTML,
 * and a button to close the popup.
 *
 * @author Jared Stephen
 */

public class HTMLPopup extends PopupWindow
{
    private final TextArea textArea;
    private final TextAreaModel textAreaModel;
    private final ScrollPane textPane;

    private final Content content;

    private final Button closeButton;

    private final List< DelayedScriptCallback > scriptCallbacks;

    /**
     * Creates a new HTML popup displaying the HTML found in the specified file
     *
     * @param file the file containing the HTML to be shown.
     * @throws IOException
     */

    public HTMLPopup( File file, Widget parent ) throws IOException
    {
        this( FileUtil.readFileAsString( file.getPath( ) ), parent );
    }

    /**
     * Creates a new HTML popup displaying the HTML found in the specified resource.
     * The resource must be a valid ResourceID as defined by {@link net.sf.hale.resource.ResourceManager}.
     * <p>
     * This popup will use the mainViewer as the parent widget
     *
     * @param resource The String pointing to the resource containing the HTML to be shown.
     */

    public HTMLPopup( String resource )
    {
        this( ResourceManager.getResourceAsString( resource ), Game.mainViewer );
    }

    private HTMLPopup( String htmlContent, Widget parent )
    {
        super( parent );

        scriptCallbacks = new ArrayList< DelayedScriptCallback >( );

        this.setCloseOnClickedOutside( false );

        content = new Content( );

        this.add( content );

        HTMLTextAreaModel model = new HTMLTextAreaModel( );
        model.setHtml( htmlContent );
        textAreaModel = model;

        textArea = new TextArea( textAreaModel );
        textPane = new ScrollPane( textArea );
        textPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
        textPane.setCanAcceptKeyboardFocus( false );

        content.add( textPane );

        closeButton = new CloseButton( );
        content.add( closeButton );
    }

    /**
     * Sets the size of the popup to the specified width and height.
     *
     * @param width  The width to be set
     * @param height The height to be set
     * @return Whether the size was successfully set.
     */

    @Override
    public boolean setSize( int width, int height )
    {
        content.prefHeight = height;
        content.prefWidth = width;

        return super.setSize( width, height );
    }

    /**
     * Adds this HTMLPopup to the popups list to be shown the next time
     * {@link net.sf.hale.view.MainViewer} does an update
     */

    public void show( )
    {
        Game.mainViewer.showPopup( this );
    }

    /**
     * Creates a script runner callback which will be run when this popup is closed
     *
     * @param scriptLocation the script resource name of the script file to be run,
     *                       relative to the scripts/ directory
     * @param function       the function to be called within the script file
     */

    public void addCallback( String scriptLocation, String function )
    {
        String script = ResourceManager.getScriptResourceAsString( scriptLocation );

        Scriptable scriptable = new Scriptable( script, scriptLocation, false );
        DelayedScriptCallback callback = new DelayedScriptCallback( scriptable, function );

        scriptCallbacks.add( callback );
    }

    /**
     * Creates a script runner callback which will be run when this popup is closed
     *
     * @param scriptLocation the script resource name of the script file to be run,
     *                       relative to the scripts/ directory
     * @param function       the function to be called within the script file
     * @param arg            a generic argument that can optionally be passed to the script function
     */

    public void addCallback( String scriptLocation, String function, Object arg )
    {
        String script = ResourceManager.getScriptResourceAsString( scriptLocation );

        Scriptable scriptable = new Scriptable( script, scriptLocation, false );
        DelayedScriptCallback callback = new DelayedScriptCallback( scriptable, function );
        callback.addArgument( arg );

        scriptCallbacks.add( callback );
    }

    /**
     * Creates a script runner callback which will be run when this popup is closed
     *
     * @param scriptLocation the script resource name of the script file to be run,
     *                       relative to the scripts/ directory
     * @param function       the function to be called within the script file
     * @param args           arguments that can optionally be passed to the script function
     */

    public void addCallback( String scriptLocation, String function, Object[] args )
    {
        String script = ResourceManager.getScriptResourceAsString( scriptLocation );

        Scriptable scriptable = new Scriptable( script, scriptLocation, false );
        DelayedScriptCallback callback = new DelayedScriptCallback( scriptable, function );
        callback.addArguments( args );

        scriptCallbacks.add( callback );
    }

    private class CloseButton extends Button implements Runnable
    {
        private CloseButton( )
        {
            super( "Continue" );
            this.addCallback( this );
        }

        @Override
        public void run( )
        {
            HTMLPopup.this.closePopup( );

            for ( DelayedScriptCallback callback : scriptCallbacks )
            {
                callback.start( );
            }
        }
    }

    private class Content extends Widget
    {
        int prefWidth;
        int prefHeight;

        @Override
        public int getPreferredWidth( )
        {
            return prefWidth;
        }

        @Override
        public int getPreferredHeight( )
        {
            return prefHeight;
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            closeButton.setSize( getInnerWidth( ), 20 );
            closeButton.setPosition( getInnerX( ), getInnerBottom( ) - closeButton.getHeight( ) );

            textPane.setPosition( getInnerX( ), getInnerY( ) );
            textPane.setSize( getInnerWidth( ), closeButton.getY( ) - getInnerY( ) );
        }
    }
}
