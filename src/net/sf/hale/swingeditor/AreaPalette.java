package net.sf.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.tileset.FeatureType;
import net.sf.hale.tileset.Layer;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tile;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.PointImmutable;

/**
 * Class for selecting different types of objects such as terrain which
 * can then be painted onto the current area
 *
 * @author Jared
 */

public class AreaPalette extends JPanel implements AreaRenderer.ViewHandler
{
    private AreaRenderer renderer;
    private Area area;
    private Tileset tileset;

    private TerrainGrid grid;

    private final String[] tabTitles = { "Terrain", "Features", "Elevation", "Tiles", "Passable", "Transparent" };
    private final AreaClickHandler[] defaultHandlers = { new TerrainAction( ), new FeatureAction( ),
            new ElevationAction( ), new TileAction( ), new PassableAction( ), new TransparentAction( ) };
    private int tabIndex;

    private JLabel mouse, view;

    /**
     * Creates a new palette.  It is empty until an area is set
     */

    public AreaPalette( )
    {
        super( new GridBagLayout( ) );
    }

    /**
     * Sets the area this palette is interacting with.  If non-null,
     * adds widgets for the area's tileset.  If null, all children
     * are removed from this palette
     *
     * @param area
     */

    public void setArea( AreaRenderer areaRenderer )
    {
        this.renderer = areaRenderer;
        this.area = areaRenderer.getArea( );

        this.removeAll( );

        if ( area != null )
        {
            this.tileset = Game.curCampaign.getTileset( area.getTileset( ) );

            addWidgets( );
        }

        tabIndex = 0;
        renderer.setClickHandler( defaultHandlers[ tabIndex ] );
        renderer.setViewHandler( this );

        grid = new TerrainGrid( area );
    }

    /**
     * Returns the area associated with this palette
     *
     * @return the area
     */

    public Area getArea( )
    {
        return area;
    }

    private void addWidgets( )
    {
        GridBagConstraints c = new GridBagConstraints( );
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets( 2, 5, 2, 5 );
        c.anchor = GridBagConstraints.WEST;

        c.gridwidth = 3;
        JLabel areaName = new JLabel( "Editing Area: " + area.getID( ) );
        add( areaName, c );

        c.gridy++;
        mouse = new JLabel( " " );
        add( mouse, c );

        c.gridy++;
        view = new JLabel( "View at -1, -1" );
        add( view, c );

        c.gridy++;
        c.gridwidth = 1;
        c.ipadx = 100;
        JLabel title = new JLabel( "Tileset: " + area.getTileset( ) );
        add( title, c );

        c.gridx++;
        c.ipadx = 0;
        add( new JLabel( "Radius" ), c );

        c.gridx++;
        JSpinner radius = new JSpinner( renderer.getMouseRadiusModel( ) );
        add( radius, c );

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        JTabbedPane contentPane = new JTabbedPane( );
        contentPane.addChangeListener( new TabChangedListener( ) );
        add( contentPane, c );

        // add terrain tab
        List< JButton > tileButtons = new ArrayList< JButton >( );

        List< String > terrainTypeIDs = new ArrayList< String >( tileset.getTerrainTypeIDs( ) );
        Collections.sort( terrainTypeIDs );

        for ( String terrainTypeID : terrainTypeIDs )
        {
            tileButtons.add( createTerrainButton( tileset.getTerrainType( terrainTypeID ) ) );
        }

        contentPane.addTab( tabTitles[ 0 ], getTabPanel( tileButtons ) );

        // add features tab
        tileButtons.clear( );

        List< String > featureTypeIDs = new ArrayList< String >( tileset.getFeatureTypeIDs( ) );
        Collections.sort( featureTypeIDs );

        for ( String featureTypeID : featureTypeIDs )
        {
            tileButtons.add( createFeatureButton( tileset.getFeatureType( featureTypeID ) ) );
        }

        contentPane.addTab( tabTitles[ 1 ], getTabPanel( tileButtons ) );

        // add elevation tab
        contentPane.addTab( tabTitles[ 2 ], new JPanel( ) );

        // add tiles tab
        tileButtons.clear( );

        for ( String layerID : tileset.getLayerIDs( ) )
        {
            Layer layer = tileset.getLayer( layerID );

            for ( String tileID : layer.getTiles( ) )
            {
                tileButtons.add( createTileButton( tileID, layerID ) );
            }
        }

        contentPane.addTab( tabTitles[ 3 ], getTabPanel( tileButtons ) );

        contentPane.addTab( tabTitles[ 4 ], new JPanel( ) );

        contentPane.addTab( tabTitles[ 5 ], new JPanel( ) );
    }

    private JScrollPane getTabPanel( List< JButton > tileButtons )
    {
        JPanel panel = new JPanel( new GridBagLayout( ) );

        GridBagConstraints c = new GridBagConstraints( );
        c.insets = new Insets( 2, 2, 2, 2 );

        int row = 0;
        int col = 0;

        for ( JButton tileButton : tileButtons )
        {
            c.gridx = row;
            c.gridy = col;

            panel.add( tileButton, c );

            row++;
            if ( row == 2 )
            {
                row = 0;
                col++;
            }
        }

        JScrollPane scrollPane = new JScrollPane( panel );
        scrollPane.getVerticalScrollBar( ).setUnitIncrement( 64 );
        scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

        return scrollPane;
    }

    private Icon getIconFromImage( String tileID, String layerID )
    {
        String spriteID = tileset.getLayer( layerID ).getSpriteID( tileID );

        String spriteSheetID = ResourceManager.getResourceDirectory( spriteID ) + ResourceType.PNG.getExtension( );

        BufferedImage sourceImage = SpriteManager.getSourceImage( spriteSheetID );

        Sprite tileSprite = SpriteManager.getImage( spriteID );

        int x = ( int ) ( tileSprite.getTexCoordStartX( ) * sourceImage.getWidth( ) );
        int y = ( int ) ( tileSprite.getTexCoordStartY( ) * sourceImage.getHeight( ) );
        int x2 = ( int ) ( tileSprite.getTexCoordEndX( ) * sourceImage.getWidth( ) );
        int y2 = ( int ) ( tileSprite.getTexCoordEndY( ) * sourceImage.getHeight( ) );

        return new ImageIcon( sourceImage.getSubimage( x, y, x2 - x, y2 - y ) );
    }

    private JButton createTileButton( String tileID, String layerID )
    {
        return new JButton( new TileAction( null, tileID, layerID, getIconFromImage( tileID, layerID ) ) );
    }

    private JButton createTerrainButton( TerrainType terrainType )
    {
        TerrainTile previewTile = terrainType.getPreviewTile( );
        String tileID = previewTile.getID( );
        String layerID = previewTile.getLayerID( );

        JButton button = new JButton( new TerrainAction( terrainType, tileID, layerID, getIconFromImage( tileID, layerID ) ) );
        button.setVerticalTextPosition( SwingConstants.TOP );
        button.setHorizontalTextPosition( SwingConstants.CENTER );

        return button;
    }

    private JButton createFeatureButton( FeatureType featureType )
    {
        TerrainTile previewTile = featureType.getPreviewTile( );
        String tileID = previewTile.getID( );
        String layerID = previewTile.getLayerID( );

        JButton button = new JButton( new FeatureAction( featureType, tileID, layerID, getIconFromImage( tileID, layerID ) ) );
        button.setVerticalTextPosition( SwingConstants.TOP );
        button.setHorizontalTextPosition( SwingConstants.CENTER );

        return button;
    }

    private class TabChangedListener implements ChangeListener
    {
        @Override
        public void stateChanged( ChangeEvent changeEvent )
        {
            JTabbedPane source = ( JTabbedPane ) changeEvent.getSource( );

            int index = source.getSelectedIndex( );

            if ( index != tabIndex )
            {
                tabIndex = index;
                renderer.setClickHandler( defaultHandlers[ tabIndex ] );
                renderer.setActionPreviewTile( null );

                renderer.setDrawPassable( tabIndex == 4 );
                renderer.setDrawTransparent( tabIndex == 5 );
            }
        }
    }

    private class ElevationAction implements AreaClickHandler
    {
        @Override
        public void leftClicked( int x, int y, int r )
        {
            grid.modifyElevation( x, y, r, ( byte ) + 1 );
        }

        @Override
        public void rightClicked( int x, int y, int r )
        {
            grid.modifyElevation( x, y, r, ( byte ) - 1 );
        }
    }

    private class PassableAction implements AreaClickHandler
    {
        @Override
        public void leftClicked( int x, int y, int r )
        {
            for ( PointImmutable p : area.getPoints( x, y, r ) )
            {
                area.getPassability( )[ p.x ][ p.y ] = true;
            }
        }

        @Override
        public void rightClicked( int x, int y, int r )
        {
            for ( PointImmutable p : area.getPoints( x, y, r ) )
            {
                area.getPassability( )[ p.x ][ p.y ] = false;
            }
        }
    }

    private class TransparentAction implements AreaClickHandler
    {
        @Override
        public void leftClicked( int x, int y, int r )
        {
            for ( PointImmutable p : area.getPoints( x, y, r ) )
            {
                area.getTransparency( )[ p.x ][ p.y ] = true;
            }
        }

        @Override
        public void rightClicked( int x, int y, int r )
        {
            for ( PointImmutable p : area.getPoints( x, y, r ) )
            {
                area.getTransparency( )[ p.x ][ p.y ] = false;
            }
        }
    }

    private class TileAction extends AbstractAction implements AreaClickHandler
    {
        private final String tileID;
        private final String layerID;
        private final String spriteID;

        private TileAction( )
        {
            this.tileID = null;
            this.layerID = null;
            this.spriteID = null;
        }

        private TileAction( String label, String tileID, String layerID, Icon icon )
        {
            super( label, icon );

            this.tileID = tileID;
            this.layerID = layerID;
            this.spriteID = tileset.getLayer( layerID ).getSpriteID( tileID );
        }

        // called when the button is clicked
        @Override
        public void actionPerformed( ActionEvent evt )
        {
            renderer.setActionPreviewTile( new Tile( tileID, spriteID ) );
            renderer.setClickHandler( this );
        }

        @Override
        public void leftClicked( int x, int y, int radius )
        {
            if ( tileID == null ) return;

            for ( PointImmutable p : area.getPoints( x, y, radius ) )
            {
                area.getTileGrid( ).addTile( tileID, layerID, p.x, p.y );
            }

            area.getTileGrid( ).cacheSprites( );
        }

        @Override
        public void rightClicked( int x, int y, int r )
        {
            grid.removeAllTiles( x, y, r );
        }
    }

    private class TerrainAction extends TileAction
    {
        private final TerrainType terrainType;

        private TerrainAction( )
        {
            this.terrainType = null;
        }

        private TerrainAction( TerrainType terrainType, String tileID, String layerID, Icon icon )
        {
            super( terrainType.getID( ), tileID, layerID, icon );

            this.terrainType = terrainType;
        }

        @Override
        public void leftClicked( int x, int y, int r )
        {
            if ( terrainType == null ) return;

            grid.setTerrain( x, y, r, terrainType );
        }
    }

    private class FeatureAction extends TileAction
    {
        private final FeatureType featureType;

        private FeatureAction( )
        {
            this.featureType = null;
        }

        private FeatureAction( FeatureType featureType, String tileID, String layerID, Icon icon )
        {
            super( featureType.getID( ), tileID, layerID, icon );

            this.featureType = featureType;
        }

        @Override
        public void rightClicked( int x, int y, int r )
        {
            grid.removeFeatureTiles( x, y, r );
        }

        @Override
        public void leftClicked( int x, int y, int r )
        {
            if ( featureType != null )
            {
                grid.setFeature( x, y, r, featureType );
            }
        }
    }

    /**
     * Used by AreaRenderer to pass information about a given click
     * back to the appropriate action
     *
     * @author jared
     */

    public interface AreaClickHandler
    {
        /**
         * Called when the user left clicks on the area
         *
         * @param x the grid x coordinate
         * @param y the grid y coordinate
         * @param r the grid radius
         */

        public void leftClicked( int x, int y, int r );

        /**
         * Called when the user right clicks on the area
         *
         * @param x the grid x coordinate
         * @param y the grid y coordinate
         * @param r the grid radius
         */

        public void rightClicked( int x, int y, int r );
    }

    @Override
    public void mouseMoved( int gridx, int gridy )
    {
        mouse.setText( "Mouse at " + gridx + ", " + gridy );
    }

    @Override
    public void viewMoved( int gridx, int gridy )
    {
        view.setText( "View at " + gridx + ", " + gridy );
    }
}
