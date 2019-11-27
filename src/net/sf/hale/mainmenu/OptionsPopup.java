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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.DisplayMode;

import net.sf.hale.Config;
import net.sf.hale.Game;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.SaveWriter;
import net.sf.hale.util.Logger;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

/**
 * The menu Popup responsible for setting game options, such
 * as resolution and tooltip delay.
 *
 * @author Jared Stephen
 */

public class OptionsPopup extends PopupWindow
{
    private MainMenu mainMenu;
    private Content content;

    public OptionsPopup( MainMenu mainMenu )
    {
        super( mainMenu );
        this.mainMenu = mainMenu;

        content = new Content( );
        add( content );

        setCloseOnClickedOutside( false );
        setCloseOnEscape( true );
    }

    /**
     * Saves the config specified by the arguments to config.json
     *
     * @param resX         ResolutionX
     * @param resY         ResolutionY
     * @param fullscreen   true for fullscreen mode, false for windowed mode
     * @param tooltipDelay Delay until tooltips appear in milliseconds
     * @param combatDelay  combat delay in milliseconds
     */

    private void writeConfigToFile( int resX, int resY, boolean fullscreen, boolean scale2x,
                                    int tooltipDelay, int combatDelay, boolean showFPS, boolean combatAutoScroll,
                                    Map< String, String > keyBindings ) throws IOException
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        File fout = new File( Game.plataform.getConfigDirectory( ) + "config.json" );
        PrintWriter out = new PrintWriter( fout );

        Integer[] resOut = new Integer[ 2 ];
        resOut[ 0 ] = resX;
        resOut[ 1 ] = resY;
        data.put( "Resolution", resOut );
        data.put( "ConfigVersion", Config.Version );
        data.put( "Fullscreen", fullscreen );
        data.put( "Scale2X", scale2x );
        data.put( "ShowFPS", showFPS );
        data.put( "CapFPS", Game.config.capFPS( ) );
        data.put( "CombatAutoScroll", combatAutoScroll );
        data.put( "TooltipDelay", tooltipDelay );
        data.put( "CombatDelay", combatDelay );
        data.put( "ScriptConsoleEnabled", Game.config.isScriptConsoleEnabled( ) );
        data.put( "DebugMode", Game.config.isDebugModeEnabled( ) );
        data.put( "WarningMode", Game.config.isWarningModeEnabled( ) );
        data.put( "CheckForUpdatesInterval", Game.config.getCheckForUpdatesInterval( ) );
        if ( Game.config.randSeedSet( ) )
        {
            data.put( "RandSeed", Game.config.getRandSeed( ) );
        }

        JSONOrderedObject bindingsOut = new JSONOrderedObject( );
        for ( String actionName : keyBindings.keySet( ) )
        {
            bindingsOut.put( actionName, keyBindings.get( actionName ) );
        }
        data.put( "Keybindings", bindingsOut );

        SaveWriter.writeJSON( data, out );
        out.close( );
    }

    private class Content extends DialogLayout
    {
        private Label title, keybindingsTitle;
        private Button accept, cancel, reset;

        private final ToggleButton fullscreen;
        private final ToggleButton scale2x;
        private final ComboBox< String > modesBox;
        private final SimpleChangableListModel< String > modesModel;
        private final Scrollbar tooltipDelay, combatSpeed;
        private final ToggleButton fpsCounter;
        private final ToggleButton autoscrollToggle;
        private final Label modesTitle, tooltipTitle;
        private final Label combatSpeedTitle;
        private final ScrollPane keyBindingsPane;
        private final KeyBindingsContent keyBindingsContent;

        private Group mainH, mainV;

        private Content( )
        {
            mainH = createParallelGroup( );
            mainV = createSequentialGroup( );

            title = new Label( );
            title.setTheme( "titlelabel" );
            addHorizontalWidgets( title );

            modesTitle = new Label( );
            modesTitle.setTheme( "modeslabel" );

            modesModel = new SimpleChangableListModel< String >( );
            modesBox = new ComboBox< String >( modesModel );
            modesBox.setTheme( "modesbox" );
            modesBox.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    setAcceptEnabled( );
                }
            } );

            fullscreen = new ToggleButton( );
            fullscreen.setTheme( "fullscreentoggle" );

            addHorizontalWidgets( modesTitle, modesBox, fullscreen );

            scale2x = new ToggleButton( );
            scale2x.addCallback( new Runnable( )
            {
                public void run( )
                {
                    repopulateDisplayModes( Game.config, scale2x.isActive( ) );
                }
            } );
            scale2x.setTheme( "scale2xtoggle" );
            if ( ! Game.hasUsable2xDisplayMode( ) )
            {
                scale2x.setEnabled( false );
            }
            addHorizontalWidgets( scale2x, new Label( ) ); // add label as spacer so scale2x doesn't fill entire width

            tooltipTitle = new Label( );
            tooltipTitle.setTheme( "tooltiplabel" );

            tooltipDelay = new Scrollbar( Scrollbar.Orientation.HORIZONTAL );
            tooltipDelay.setModel( new SimpleIntegerModel( 1, 20, 4 ) );
            tooltipDelay.setPageSize( 1 );
            tooltipDelay.setTheme( "tooltipbar" );
            addHorizontalWidgets( tooltipTitle, tooltipDelay );

            combatSpeedTitle = new Label( );
            combatSpeedTitle.setTheme( "combatspeedlabel" );

            combatSpeed = new Scrollbar( Scrollbar.Orientation.HORIZONTAL );
            combatSpeed.setModel( new SimpleIntegerModel( 1, 5, 3 ) );
            combatSpeed.setPageSize( 1 );
            combatSpeed.setTheme( "combatspeedbar" );
            addHorizontalWidgets( combatSpeedTitle, combatSpeed );

            fpsCounter = new ToggleButton( );
            fpsCounter.setTheme( "fpstoggle" );
            addHorizontalWidgets( fpsCounter, new Label( ) );

            autoscrollToggle = new ToggleButton( );
            autoscrollToggle.setTheme( "autoscrolltoggle" );
            addHorizontalWidgets( autoscrollToggle, new Label( ) );

            mainV.addGap( DialogLayout.LARGE_GAP );

            keybindingsTitle = new Label( );
            keybindingsTitle.setTheme( "keybindingslabel" );
            addHorizontalWidgets( keybindingsTitle );

            keyBindingsContent = new KeyBindingsContent( );
            keyBindingsPane = new ScrollPane( keyBindingsContent );
            keyBindingsPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
            keyBindingsPane.setTheme( "keybindingspane" );
            addHorizontalWidgets( keyBindingsPane );

            mainV.addGap( DialogLayout.LARGE_GAP );

            accept = new Button( );
            accept.setTheme( "acceptbutton" );
            accept.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    applySettings( );
                    OptionsPopup.this.closePopup( );
                }
            } );

            cancel = new Button( );
            cancel.setTheme( "cancelbutton" );
            cancel.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    OptionsPopup.this.closePopup( );
                }
            } );

            reset = new Button( );
            reset.setTheme( "resetbutton" );
            reset.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    ConfirmationPopup popup = new ConfirmationPopup( mainMenu );
                    popup.setTitleText( "Reset all options to defaults?" );
                    popup.addCallback( new Runnable( )
                    {
                        @Override
                        public void run( )
                        {
                            resetOptions( );
                        }
                    } );
                    popup.openPopupCentered( );
                }
            } );

            Group buttonsH = createSequentialGroup( );
            Group buttonsV = createParallelGroup( );

            buttonsH.addWidgets( accept, cancel );
            buttonsH.addGap( "button" );
            buttonsH.addWidget( reset );

            buttonsV.addWidgets( accept, cancel, reset );
            mainH.addGroup( buttonsH );
            mainV.addGroup( buttonsV );

            setHorizontalGroup( mainH );
            setVerticalGroup( mainV );

            // initialize all content
            initializeWidgetsToConfig( Game.config );
        }

        private void setAcceptEnabled( )
        {
            accept.setEnabled( modesBox.getSelected( ) != - 1 );
        }

        private void addHorizontalWidgets( Widget... widgets )
        {
            switch ( widgets.length )
            {
                case 0:
                    break;
                case 1:
                    mainH.addWidget( widgets[ 0 ] );
                    mainV.addWidget( widgets[ 0 ] );
                    break;
                default:
                    Group gH = createSequentialGroup( widgets );
                    Group gV = createParallelGroup( widgets );
                    mainH.addGroup( gH );
                    mainV.addGroup( gV );
            }
        }

        private void resetOptions( )
        {
            Config defaultConfig = new Config( "docs/defaultConfig.json" );

            initializeWidgetsToConfig( defaultConfig );
        }

        private void applySettings( )
        {
            // get the set of key bindings that is currently being shown in the UI
            Map< String, String > keyBindings = new LinkedHashMap< String, String >( );

            for ( KeyBindWidget widget : keyBindingsContent.widgets )
            {
                if ( widget.keyBinding.getText( ) != null )
                {
                    keyBindings.put( widget.actionName, widget.keyBinding.getText( ) );
                }
                else
                {
                    keyBindings.put( widget.actionName, "" );
                }
            }

            boolean showFPS = this.fpsCounter.isActive( );

            boolean combatAutoScroll = this.autoscrollToggle.isActive( );

            boolean fullscreen = this.fullscreen.isActive( );

            boolean scale2x = this.scale2x.isActive( );

            DisplayMode mode;
            if ( scale2x )
            {
                mode = Game.all2xUsableDisplayModes.get( this.modesBox.getSelected( ) );
            }
            else
            {
                mode = Game.allDisplayModes.get( this.modesBox.getSelected( ) );
            }

            // get tooltip delay in milliseconds
            int tooltipDelay = this.tooltipDelay.getValue( ) * 100;

            // get combat speed in milliseconds
            int combatSpeed = ( 6 - this.combatSpeed.getValue( ) ) * 50;

            try
            {
                writeConfigToFile( mode.getWidth( ), mode.getHeight( ), fullscreen, scale2x, tooltipDelay,
                                   combatSpeed, showFPS, combatAutoScroll, keyBindings );
            }
            catch ( Exception e )
            {
                Logger.appendToErrorLog( "Error writing configuration file", e );
            }

            if ( mode.getWidth( ) == Game.config.getResolutionX( ) && mode.getHeight( ) == Game.config.getResolutionY( ) &&
                    fullscreen == Game.config.getFullscreen( ) && scale2x == Game.config.scale2x( ) )
            {

                Game.config = new Config( Game.plataform.getConfigDirectory( ) + "config.json" );
            }
            else
            {
                Game.config = new Config( Game.plataform.getConfigDirectory( ) + "config.json" );
                mainMenu.restartMenu( );
            }
        }

        private void repopulateDisplayModes( Config config, boolean scale2x )
        {
            modesModel.clear( );
            for ( DisplayMode mode : ( scale2x ? Game.all2xUsableDisplayModes : Game.allDisplayModes ) )
            {
                modesModel.addElement( mode.getWidth( ) + " x " + mode.getHeight( ) );
            }

            int index = Config.getMatchingDisplayMode( scale2x, config.getUnscaledResolutionX( ), config.getUnscaledResolutionY( ) );
            modesBox.setSelected( index );
        }

        private void initializeWidgetsToConfig( Config config )
        {
            fullscreen.setActive( config.getFullscreen( ) );
            scale2x.setActive( config.scale2x( ) );
            tooltipDelay.setValue( config.getTooltipDelay( ) / 100 );
            combatSpeed.setValue( 6 - ( config.getCombatDelay( ) / 50 ) );
            fpsCounter.setActive( config.showFPS( ) );
            autoscrollToggle.setActive( config.autoScrollDuringCombat( ) );

            repopulateDisplayModes( config, config.scale2x( ) );

            keyBindingsContent.initializeWidgetsToConfig( config );

            setAcceptEnabled( );
        }
    }

    private class KeyBindingsContent extends DialogLayout
    {
        private List< KeyBindWidget > widgets;

        private KeyBindingsContent( )
        {
            widgets = new ArrayList< KeyBindWidget >( );
        }

        private void initializeWidgetsToConfig( Config config )
        {
            removeAllChildren( );
            widgets.clear( );

            Group mainH = createParallelGroup( );
            Group mainV = createSequentialGroup( );

            List< String > actions = config.getKeyActionNames( );
            Collections.sort( actions );

            for ( String actionName : actions )
            {
                KeyBindWidget widget = new KeyBindWidget( actionName, config );
                mainH.addWidget( widget );
                mainV.addWidget( widget );

                widgets.add( widget );
            }

            setHorizontalGroup( mainH );
            setVerticalGroup( mainV );
        }
    }

    private class KeyBindWidget extends Widget implements KeyBindPopup.Callback
    {
        private Label actionLabel;
        private Button keyBinding;

        private String actionName;

        private KeyBindWidget( String actionName, Config config )
        {
            this.actionName = actionName;

            actionLabel = new Label( actionName );
            actionLabel.setTheme( "actionlabel" );
            add( actionLabel );

            int keyCode = config.getKeyForAction( actionName );
            String keyChar = Event.getKeyNameForCode( keyCode );

            keyBinding = new Button( keyChar );
            keyBinding.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    showKeyBindPopup( );
                }
            } );
            keyBinding.setTheme( "keybindingbutton" );
            add( keyBinding );
        }

        private void showKeyBindPopup( )
        {
            KeyBindPopup popup = new KeyBindPopup( KeyBindWidget.this, KeyBindWidget.this );
            popup.openPopupCentered( );
        }

        @Override
        public int getPreferredInnerHeight( )
        {
            return Math.max( actionLabel.getPreferredHeight( ), keyBinding.getPreferredHeight( ) );
        }

        @Override
        protected void layout( )
        {
            actionLabel.setSize( actionLabel.getPreferredWidth( ), actionLabel.getPreferredHeight( ) );
            keyBinding.setSize( keyBinding.getPreferredWidth( ), keyBinding.getPreferredHeight( ) );

            actionLabel.setPosition( getInnerX( ), getInnerY( ) + getInnerHeight( ) / 2 - actionLabel.getHeight( ) / 2 );
            keyBinding.setPosition( actionLabel.getRight( ), getInnerY( ) + getInnerHeight( ) / 2 - keyBinding.getHeight( ) / 2 );
        }

        @Override
        public String getActionName( )
        {
            return actionName;
        }

        @Override
        public void keyBound( int keyCode )
        {
            String keyChar = Event.getKeyNameForCode( keyCode );

            // bind the key for this widget
            keyBinding.setText( keyChar );

            // check for conflicts with the key
            for ( KeyBindWidget widget : content.keyBindingsContent.widgets )
            {
                if ( widget == this ) continue;

                if ( keyChar.equals( widget.keyBinding.getText( ) ) )
                {
                    widget.keyBinding.setText( "" );
                }
            }
        }
    }
}
