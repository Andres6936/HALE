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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.hale.Game;
import net.sf.hale.mainmenu.CampaignGroupSelector.CampaignDescriptor;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONParser;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * The popup window for selecting the current campaign
 *
 * @author Jared Stephen
 */

public class CampaignPopup extends PopupWindow
{
    private CampaignGroupSelector selectedGroup;

    private Content content;
    private MainMenu mainMenu;

    /**
     * Create a new CampaignPopup with the specified MainMenu as the parent Widget
     *
     * @param mainMenu the parent Widget
     */

    public CampaignPopup( MainMenu mainMenu )
    {
        super( mainMenu );
        this.mainMenu = mainMenu;

        content = new Content( );
        add( content );
    }

    private class Content extends Widget
    {
        private Label title;
        private ScrollPane selectorPane;
        private DialogLayout paneContent;
        private Button accept, cancel;

        private HTMLTextAreaModel textAreaModel;
        private ScrollPane textPane;

        private int selectorWidth;
        private int acceptCancelGap;

        private Content( )
        {
            CampaignPopup.this.content = this;

            title = new Label( );
            title.setTheme( "titlelabel" );
            add( title );

            textAreaModel = new HTMLTextAreaModel( );
            TextArea textArea = new TextArea( textAreaModel );
            textPane = new ScrollPane( textArea );
            textPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
            textPane.setCanAcceptKeyboardFocus( false );
            textPane.setTheme( "descriptionpane" );
            add( textPane );

            paneContent = new DialogLayout( );
            paneContent.setTheme( "content" );
            selectorPane = new ScrollPane( paneContent );
            selectorPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
            selectorPane.setTheme( "selectorpane" );
            add( selectorPane );

            accept = new Button( );
            accept.setTheme( "acceptbutton" );
            accept.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    mainMenu.loadCampaign( selectedGroup.getSelectedID( ) );
                    mainMenu.update( );
                    CampaignPopup.this.closePopup( );
                }
            } );
            accept.setEnabled( false );
            add( accept );

            cancel = new Button( );
            cancel.setTheme( "cancelbutton" );
            cancel.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    CampaignPopup.this.closePopup( );
                }
            } );
            add( cancel );

            DialogLayout.Group mainH = paneContent.createParallelGroup( );
            DialogLayout.Group mainV = paneContent.createSequentialGroup( );

            Map< String, CampaignDescriptor > campaigns = CampaignPopup.getAvailableCampaigns( );

            // add available groups to pane content
            for ( CampaignGroupSelector selector : CampaignPopup.getAvailableGroups( campaigns ) )
            {
                selector.setCallback( CampaignPopup.this );

                if ( Game.curCampaign != null )
                { selector.selectCampaign( Game.curCampaign.getID( ) ); }

                mainH.addWidget( selector );
                mainV.addWidget( selector );
            }

            paneContent.setHorizontalGroup( mainH );
            paneContent.setVerticalGroup( mainV );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            acceptCancelGap = themeInfo.getParameter( "acceptCancelGap", 0 );
            selectorWidth = themeInfo.getParameter( "selectorWidth", 0 );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            int centerX = getInnerX( ) + getWidth( ) / 2;

            title.setSize( title.getPreferredWidth( ), title.getPreferredHeight( ) );
            accept.setSize( accept.getPreferredWidth( ), accept.getPreferredHeight( ) );
            cancel.setSize( cancel.getPreferredWidth( ), cancel.getPreferredHeight( ) );

            title.setPosition( centerX - title.getWidth( ) / 2, getInnerY( ) );

            accept.setPosition( centerX - acceptCancelGap - accept.getWidth( ),
                                getInnerBottom( ) - accept.getHeight( ) );

            cancel.setPosition( centerX + acceptCancelGap, getInnerBottom( ) - cancel.getHeight( ) );

            int paneBottom = Math.min( accept.getY( ), cancel.getY( ) );

            selectorPane.setPosition( getInnerX( ), title.getBottom( ) );
            selectorPane.setSize( selectorWidth, paneBottom - title.getBottom( ) );

            textPane.setPosition( selectorPane.getRight( ), title.getBottom( ) );
            textPane.setSize( getInnerRight( ) - textPane.getX( ), paneBottom - title.getBottom( ) );
        }
    }

    /**
     * Called when a campaign group selector is selected
     *
     * @param selector
     */

    protected void groupSelected( CampaignGroupSelector selector )
    {
        if ( selectedGroup != null && selectedGroup != selector )
        {
            selectedGroup.deselect( );
        }

        this.selectedGroup = selector;

        CampaignPopup.this.content.textAreaModel.setHtml( selector.getSelectedDescription( ) );
        CampaignPopup.this.content.textPane.invalidateLayout( );

        CampaignPopup.this.content.accept.setEnabled( true );
    }

    /**
     * Returns a sorted list of all available campaign groups (sorted by group name)
     *
     * @return the sorted list of groups
     */

    private static List< CampaignGroupSelector > getAvailableGroups( Map< String, CampaignDescriptor > campaigns )
    {
        List< CampaignGroupSelector > groups = new ArrayList< CampaignGroupSelector >( );

        // find all of the file defined groups in the campaigns/ directory
        try
        {
            for ( String fileName : new File( "campaigns" ).list( ) )
            {
                File f = new File( "campaigns/" + fileName );

                if ( ! f.isFile( ) || ! f.getName( ).endsWith( ResourceType.JSON.getExtension( ) ) ) continue;

                String id = ResourceManager.getResourceID( f.getName( ), ResourceType.JSON );

                CampaignGroupSelector selector = new CampaignGroupSelector( id, campaigns );

                groups.add( selector );
            }
        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error generating list of campaign groups.", e );
        }

        // now find the set of all grouped campaigns
        Set< String > groupedCampaigns = new HashSet< String >( );

        for ( CampaignGroupSelector selector : groups )
        {
            groupedCampaigns.addAll( selector.getAllCampaignIDs( ) );
        }

        // look through all campaigns.  make up a new group for any ungrouped ones
        for ( String campaignID : campaigns.keySet( ) )
        {
            if ( groupedCampaigns.contains( campaignID ) ) continue;

            CampaignGroupSelector selector = new CampaignGroupSelector( campaigns.get( campaignID ) );

            groups.add( selector );
        }

        // sort all groups by name
        Collections.sort( groups, new Comparator< CampaignGroupSelector >( )
        {
            @Override
            public int compare( CampaignGroupSelector arg0, CampaignGroupSelector arg1 )
            {
                return arg0.getCampaignName( ).compareTo( arg1.getCampaignName( ) );
            }
        } );

        return groups;
    }

    /**
     * Returns a sorted set of all available campaigns (sorted by campaign ID)
     *
     * @return the set of available campaigns
     */

    private static Map< String, CampaignDescriptor > getAvailableCampaigns( )
    {
        List< CampaignDescriptor > campaigns = new ArrayList< CampaignDescriptor >( );

        File campaignDir = new File( "campaigns" );
        String[] fileList = campaignDir.list( );

        try
        {
            // first, add all extracted directories
            for ( String fileName : fileList )
            {
                File f = new File( "campaigns/" + fileName );

                if ( ! f.isDirectory( ) || f.getName( ).startsWith( "." ) ) continue;

                // get the id, name, and description for the campaign
                SimpleJSONParser parser = new SimpleJSONParser( new File( f.getPath( ) + "/campaign" +
                                                                                  ResourceType.JSON.getExtension( ) ) );

                String id = fileName;
                String description = FileUtil.readFileAsString( f.getPath( ) + "/description" +
                                                                        ResourceType.HTML.getExtension( ) );
                String name = parser.get( "name", fileName );

                campaigns.add( new CampaignDescriptor( id, name, description ) );
            }

            // now, look for zip archives.
            for ( String fileName : fileList )
            {
                File f = new File( "campaigns/" + fileName );

                if ( ! f.isFile( ) || ! f.getName( ).endsWith( ResourceType.Zip.getExtension( ) ) ) continue;

                String id = ResourceManager.getResourceID( f.getName( ), ResourceType.Zip );

                // if we already added the directory version of this campaign, don't also add the
                // zip version
                if ( containsCampaign( campaigns, id ) ) continue;

                // get the name and description of the campaign
                ZipFile file = new ZipFile( f );
                ZipEntry entry = file.getEntry( "description" + ResourceType.HTML.getExtension( ) );
                String description = ResourceManager.getResourceAsString( file.getInputStream( entry ) );

                ZipEntry campaignEntry = file.getEntry( "campaign" + ResourceType.JSON.getExtension( ) );
                SimpleJSONParser parser = new SimpleJSONParser( file.getName( ) + "/campaign",
                                                                file.getInputStream( campaignEntry ) );

                String name = parser.get( "name", id );

                campaigns.add( new CampaignDescriptor( id, name, description ) );

                file.close( );
            }
        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error generating list of campaigns.", e );
        }

        // now sort the list by ID
        Collections.sort( campaigns, new Comparator< CampaignDescriptor >( )
        {
            @Override
            public int compare( CampaignDescriptor c1, CampaignDescriptor c2 )
            {
                return c1.id.compareTo( c2.id );
            }
        } );

        // convert the sorted list to a map
        Map< String, CampaignDescriptor > campaignsMap = new LinkedHashMap< String, CampaignDescriptor >( );
        for ( CampaignDescriptor d : campaigns )
        {
            campaignsMap.put( d.id, d );
        }

        return campaignsMap;
    }

    /*
     * Returns true if the specified list already contains a campaign with the specified ID
     */

    private static boolean containsCampaign( List< CampaignDescriptor > campaigns, String id )
    {
        for ( CampaignDescriptor descriptor : campaigns )
        {
            if ( descriptor.id.equals( id ) ) return true;
        }

        return false;
    }
}
