package net.sf.hale.view;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.EntityListener;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.Role;

/**
 * A widget for displaying basic information about a creature in
 * a single window.  This is used by the "Examine" default ability.
 *
 * @author Jared Stephen
 */

public class CreatureDetailsWindow extends GameSubWindow implements EntityListener
{
    private Creature creature;
    private HTMLTextAreaModel textAreaModel;

    /**
     * Create a new CreatureDetailsWindow displaying details for the specified Creature.
     *
     * @param creature the Creature to show details for
     */

    public CreatureDetailsWindow( Creature creature )
    {
        this.creature = creature;
        creature.addViewer( this );

        this.setSize( 280, 300 );
        this.setTitle( "Details for " + creature.getTemplate( ).getName( ) );

        DialogLayout layout = new DialogLayout( );
        layout.setTheme( "content" );
        this.add( layout );

        // set up the widgets for the top row
        Widget viewer = new Viewer( );
        viewer.setTheme( "iconviewer" );
        Label title = new Label( creature.getTemplate( ).getName( ) );
        title.setTheme( "titlelabel" );

        DialogLayout.Group topRowV = layout.createParallelGroup( viewer, title );

        DialogLayout.Group topRowH = layout.createSequentialGroup( viewer );
        topRowH.addGap( 10 );
        topRowH.addWidget( title );
        topRowH.addGap( 10 );

        // create widgets for details text area
        textAreaModel = new HTMLTextAreaModel( );
        TextArea textArea = new TextArea( textAreaModel );
        ScrollPane textPane = new ScrollPane( textArea );
        textPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
        textPane.setTheme( "detailspane" );

        // set the main top level layout
        Group mainGroupV = layout.createSequentialGroup( );
        mainGroupV.addGroup( topRowV );
        mainGroupV.addGap( 5 );
        mainGroupV.addWidget( textPane );

        Group mainGroupH = layout.createParallelGroup( );
        mainGroupH.addGroup( topRowH );
        mainGroupH.addWidget( textPane );

        layout.setHorizontalGroup( mainGroupH );
        layout.setVerticalGroup( mainGroupV );

        entityUpdated( creature );
    }

    @Override
    public void removeListener( )
    {
        getParent( ).removeChild( this );
    }

    @Override
    public void entityUpdated( Entity entity )
    {
        textAreaModel.setHtml( getTextAreaContent( creature ) );

        invalidateLayout( );
    }

    /*
     * This overrides the default close behavior of GameSubWindow
     * @see net.sf.hale.view.GameSubWindow#run()
     */

    @Override
    public void run( )
    {
        removeListener( );
        creature.removeViewer( this );
    }

    private static void appendDetails( Creature creature, StringBuilder sb )
    {
        sb.append( "<div style=\"font-family: medium;\">" );
        sb.append( creature.getTemplate( ).getGender( ) ).append( ' ' );
        sb.append( "<span style=\"font-family: medium-blue;\">" ).append( creature.getTemplate( ).getRace( ).getName( ) ).append( "</span>" );
        sb.append( "</div>" );

        sb.append( "<div style=\"font-family: medium; margin-bottom: 1em\">" );
        for ( String roleID : creature.roles.getRoleIDs( ) )
        {
            Role role = Game.ruleset.getRole( roleID );
            int level = creature.roles.getLevel( role );

            sb.append( "<p>" );
            sb.append( "Level <span style=\"font-family: medium-italic;\">" ).append( level ).append( "</span> " );
            sb.append( "<span style=\"font-family: medium-red;\">" ).append( role.getName( ) ).append( "</span>" );
            sb.append( "</p>" );
        }
        sb.append( "</div>" );

        sb.append( "<div style=\"font-family: medium; margin-bottom: 1em\">" );
        sb.append( "Hit Points " );
        sb.append( "<span style=\"font-family: medium-italic-green\">" );
        sb.append( creature.getCurrentHitPoints( ) ).append( "</span> / <span style=\"font-family: medium-italic-green\">" );
        sb.append( creature.stats.get( Stat.MaxHP ) ).append( "</span>" );
        sb.append( "</div>" );

        Item mainHand = creature.getMainHandWeapon( );
        Item offHand = creature.inventory.getEquippedItem( Inventory.Slot.OffHand );

        sb.append( "<div style=\"margin-bottom: 1em; font-family: medium;\"><p>Main hand</p>" );
        sb.append( "<div style=\"font-family: medium-italic-blue\">" );
        sb.append( mainHand.getTemplate( ).getName( ) ).append( "</div></div>" );

        if ( offHand != null )
        {
            sb.append( "<div style=\"margin-bottom: 1em; font-family: medium;\"><p>Off hand</p>" );
            sb.append( "<div style=\"font-family: medium-italic-blue\">" );
            sb.append( offHand.getTemplate( ).getName( ) ).append( "</div></div>" );
        }

    }

    private static String getTextAreaContent( Creature creature )
    {
        StringBuilder sb = new StringBuilder( );

        if ( creature.getTemplate( ).getRace( ).showDetailedDescription( ) )
        {
            appendDetails( creature, sb );
        }
        else
        {
            sb.append( "<div style=\"font-family: medium-blue;\">" );
            sb.append( creature.getTemplate( ).getRace( ).getName( ) );
            sb.append( "</div>" );
        }


        synchronized ( creature.getEffects( ) )
        {
            for ( Effect effect : creature.getEffects( ) )
            {
                effect.appendDescription( sb );
            }
        }

        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            Item item = creature.inventory.getEquippedItem( slot );
            if ( item == null ) continue;

            if ( item != null )
            {
                synchronized ( item.getEffects( ) )
                {
                    for ( Effect effect : item.getEffects( ) )
                    {
                        effect.appendDescription( sb );
                    }
                }
            }
        }

        return sb.toString( );
    }

    private class Viewer extends Widget
    {
        @Override
        public int getMinHeight( ) { return Game.TILE_SIZE + getBorderHorizontal( ); }

        @Override
        public int getMinWidth( ) { return Game.TILE_SIZE + getBorderVertical( ); }

        @Override
        protected void paintWidget( GUI gui )
        {
            super.paintWidget( gui );

            creature.uiDraw( getInnerX( ), getInnerY( ) );
        }
    }
}
