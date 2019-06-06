package net.sf.hale.view;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.area.Transition;
import net.sf.hale.rules.WorldMapLocation;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * A popup window showing a graphical map of the game world (using the Campaign
 * worldMapImage) and icons with buttons enabling the user to travel to known
 * locations.
 *
 * @author Jared Stephen
 */

public class WorldMapPopup extends PopupWindow
{
    private final Content content;

    private final WorldMapViewer viewer;
    private final Button close;

    private boolean showAllLocations;

    /**
     * Creates a new WorldMapPopup with the specified parent Widget.  All input
     * in the parent Widget and all children of that Widget is blocked while the
     * popup is open.
     *
     * @param parent the parent Widget used to block input
     */

    public WorldMapPopup( Widget parent, Transition transition )
    {
        super( parent );

        this.setCloseOnEscape( false );
        this.setCloseOnClickedOutside( false );

        content = new Content( );
        this.add( content );

        viewer = new WorldMapViewer( transition );
        viewer.setWorldMapPopup( this );
        content.add( viewer );

        close = new Button( );
        close.setTheme( "closebutton" );
        close.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                closePopup( );
            }
        } );
        content.add( close );

        createLocationsList( );
    }

    /**
     * Sets whether this popup will show all locations, or only revealed locations
     *
     * @param showAll whether all locations will be shown
     */

    public void setShowAllLocations( boolean showAll )
    {
        this.showAllLocations = showAll;

        createLocationsList( );
    }

    private void createLocationsList( )
    {
        // get the list of revealed locations
        List< WorldMapLocation > mapLocations = new ArrayList< WorldMapLocation >( );
        for ( WorldMapLocation mapLocation : Game.curCampaign.worldMapLocations )
        {
            if ( showAllLocations || mapLocation.isRevealed( ) )
            { mapLocations.add( mapLocation ); }
        }

        viewer.updateLocations( mapLocations );
    }

    private class Content extends Widget
    {
        @Override
        protected void layout( )
        {
            close.setSize( close.getPreferredWidth( ), close.getPreferredHeight( ) );
            close.setPosition( getInnerRight( ) - close.getWidth( ), getInnerY( ) );

            viewer.setSize( getInnerWidth( ), getInnerHeight( ) );
        }

        @Override
        public int getPreferredWidth( )
        {
            return viewer.getPreferredWidth( ) + getBorderHorizontal( );
        }

        @Override
        public int getPreferredHeight( )
        {
            return viewer.getPreferredHeight( ) + getBorderVertical( );
        }
    }
}
