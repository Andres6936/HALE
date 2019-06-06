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

import java.util.HashSet;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.bonus.Stat;
import net.sf.hale.characterbuilder.Buildable;
import net.sf.hale.characterbuilder.CharacterBuilder;
import net.sf.hale.defaultability.Select;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.PC;
import net.sf.hale.icon.Icon;
import net.sf.hale.rules.Merchant;
import net.sf.hale.rules.XP;
import net.sf.hale.view.DragAndDropHandler;
import net.sf.hale.view.DragTarget;
import net.sf.hale.view.DropTarget;
import net.sf.hale.view.ItemListViewer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A Widget for displaying the portrait of a party member, name, and their most important
 * associated stats - name, Hit Points (HP) and Action Points (AP)
 *
 * @author Jared Stephen
 */

public class PortraitViewer extends BasePortraitViewer implements Runnable, DropTarget
{
    private PortraitArea portraitArea;

    private int nameOverlap, hpBarOverlap, effectIconColumns, effectIconStartY;

    private final Label name;

    private final LevelUpButton levelUp;

    private StatFillBar apBar, hpBar;

    // the currently open level up window if it exists
    private CharacterBuilder builder;

    private final PC pc;

    // using a hash set prevents the same icon from appearing twice
    private final Set< Icon > effectIcons;

    /**
     * Creates a new PortraitViewer showing the portrait of the specified Creature
     *
     * @param creature     the Creature to show the portrait of
     * @param portraitArea
     */

    public PortraitViewer( PC creature, PortraitArea portraitArea )
    {
        super( creature );

        this.pc = creature;
        this.portraitArea = portraitArea;

        this.effectIcons = new HashSet< Icon >( );

        name = new Label( creature.getTemplate( ).getName( ) );
        name.setTheme( "namelabel" );
        add( name );

        hpBar = new StatFillBar( );
        hpBar.setTheme( "hpbar" );
        add( hpBar );

        apBar = new StatFillBar( );
        apBar.setTheme( "apbar" );
        add( apBar );

        levelUp = new LevelUpButton( );
        levelUp.setTheme( "levelupbutton" );
        levelUp.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                builder = new CharacterBuilder( new Buildable( pc ) );
                Game.mainViewer.add( builder );
                builder.addFinishCallback( new CharacterBuilder.FinishCallback( )
                {
                    @Override
                    public void creatureModified( String id )
                    {
                        Game.mainViewer.updateInterface( );
                        builder = null;
                    }
                } );
            }
        } );
        add( levelUp );

        addCallback( this );
        setEnableEventHandling( true );
    }

    /**
     * Gets the PC being viewed by this PortraitViewer
     *
     * @return the PC
     */

    public PC getPC( )
    {
        return pc;
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        nameOverlap = themeInfo.getParameter( "nameOverlap", 0 );
        hpBarOverlap = themeInfo.getParameter( "hpBarOverlap", 0 );

        levelUp.getAnimationState( ).setAnimationState( MainPane.STATE_NOTIFICATION, true );

        effectIconColumns = themeInfo.getParameter( "effectIconColumns", 0 );
        effectIconStartY = themeInfo.getParameter( "effectIconStartY", 0 );
    }

    @Override
    public int getPreferredHeight( )
    {
        int height = super.getPreferredHeight( );

        height += name.getPreferredHeight( ) - nameOverlap;
        height += apBar.getPreferredHeight( );
        height += hpBar.getPreferredHeight( ) - hpBarOverlap;

        return height;
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        int centerX = getInnerX( ) + getInnerWidth( ) / 2;

        //name.setSize(name.getPreferredWidth(), name.getPreferredHeight());
        name.setPosition( centerX - name.getPreferredWidth( ) / 2, getInnerY( ) + name.getPreferredHeight( ) / 2 );

        this.setPortraitY( name.getPreferredHeight( ) - nameOverlap );
        int spriteHeight = this.getPortraitSpriteHeight( );

        apBar.setSize( apBar.getPreferredWidth( ), apBar.getPreferredHeight( ) );
        hpBar.setSize( hpBar.getPreferredWidth( ), hpBar.getPreferredHeight( ) );

        hpBar.setPosition( centerX - hpBar.getWidth( ) / 2,
                           getInnerY( ) + name.getPreferredHeight( ) + spriteHeight - nameOverlap - hpBarOverlap );
        apBar.setPosition( centerX - apBar.getWidth( ) / 2, hpBar.getBottom( ) );

        levelUp.setSize( levelUp.getPreferredWidth( ), levelUp.getPreferredHeight( ) );
        levelUp.setPosition( centerX - levelUp.getWidth( ) / 2, hpBar.getY( ) - levelUp.getHeight( ) - 1 );
    }

    /**
     * Closes any level up (CharacterBuilder) window associated with this
     * PortraitViewer
     */

    public void closeLevelUpWindow( )
    {
        if ( builder != null && builder.getParent( ) != null )
        {
            builder.getParent( ).removeChild( builder );
            builder = null;
        }
    }

    /**
     * Sets the level up button on this Widget to the specified enabled state
     *
     * @param enabled the enabled state for the level up button
     */

    public void setLevelUpEnabled( boolean enabled )
    {
        levelUp.setEnabled( enabled );
    }

    /**
     * Updates all the label text, ap bar, and hp bar of this PortraitViewer with
     * any changes to the creature being viewed
     */

    public void updateContent( )
    {
        setActive( pc == Game.curCampaign.party.getSelected( ) );

        hpBar.setText( "HP: " + pc.getCurrentHitPoints( ) + "/" + pc.stats.get( Stat.MaxHP ) );

        if ( pc.isDead( ) )
        {
            apBar.setText( "Dead" );
            hpBar.setText( "" );
        }
        else if ( pc.isDying( ) )
        {
            apBar.setText( "Dying" );
        }
        else
        {
            apBar.setText( "AP: " + ( pc.timer.getAP( ) / 100 ) );
        }

        int charLevel = pc.stats.get( Stat.CreatureLevel );
        int xpForNext = XP.getPointsForLevel( charLevel + 1 );
        if ( pc.getExperiencePoints( ) >= xpForNext )
        {
            levelUp.setVisible( true );
        }
        else
        {
            levelUp.setVisible( false );
        }

        float healthWidth = Math.min( ( ( float ) pc.getCurrentHitPoints( ) ) / ( ( float ) pc.stats.get( Stat.MaxHP ) ), 1.0f );
        float apWidth = Math.min( ( ( float ) pc.timer.getAP( ) ) / ( Math.max( 10000, pc.timer.getMaxAP( ) ) ), 1.0f );

        hpBar.setValue( healthWidth );
        apBar.setValue( apWidth );

        effectIcons.clear( );
        for ( Effect effect : pc.getEffects( ) )
        {
            effect.getIcons( effectIcons );
        }
    }

    @Override
    protected void paintWidget( GUI gui )
    {
        super.paintWidget( gui );

        int x = getInnerRight( );
        int y = getInnerY( ) + effectIconStartY;

        int colCount = 0;

        for ( Icon icon : effectIcons )
        {
            x -= icon.getWidth( );

            icon.draw( x, y );

            colCount++;

            if ( colCount == effectIconColumns )
            {
                colCount = 0;
                y += icon.getHeight( );
                x = getInnerRight( );
            }
        }
    }

    // button clicked callback
    @Override
    public void run( )
    {
        setActive( true );
        Select.selectCreature( getCreature( ) );
        Game.areaViewer.addDelayedScrollToCreature( getCreature( ) );
    }

    @Override
    protected boolean handleEvent( Event evt )
    {
        // don't handle events during interface lock
        if ( Game.interfaceLocker.locked( ) ) return false;

        switch ( evt.getType( ) )
        {
            case MOUSE_DRAGGED:
                portraitArea.checkMouseDrag( this, evt );
                break;
            case MOUSE_BTNUP:
                portraitArea.checkMouseDragRelease( this );

                if ( evt.getMouseButton( ) == Event.MOUSE_RBUTTON )
                {
                    Select.selectCreature( getCreature( ) );
                    Game.mainViewer.inventoryWindow.setVisible( true );
                }
                break;
            default:
                // do nothing
        }

        return super.handleEvent( evt );
    }

    @Override
    protected boolean isMouseInside( Event evt )
    {
        return super.isMouseInside( evt );
    }

    private class LevelUpButton extends Button
    {
        private String disabledTooltip;

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            disabledTooltip = themeInfo.getParameter( "disabledtooltip", ( String ) null );
        }

        @Override
        public void setEnabled( boolean enabled )
        {
            super.setEnabled( enabled );

            if ( enabled )
            {
                setTooltipContent( "Level up " + getCreature( ).getTemplate( ).getName( ) );
            }
            else
            {
                setTooltipContent( disabledTooltip );
            }
        }
    }

    @Override
    public void dragAndDropStartHover( DragTarget target )
    {
        if ( target.getParentPC( ) == pc ) return;

        if ( target.getItem( ) != null )
        {
            if ( target.getParentPC( ) != null )
            {
                // an attempt at a give drag & drop
                getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, true );
            }
            else if ( target.getItemContainer( ) != null && Game.mainViewer.containerWindow.getOpener( ) == getCreature( ) )
            {
                // an attempt at a pick up drag & drop
                getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, true );
            }
            else if ( target.getItemMerchant( ) != null )
            {
                // an attempt at a buy drag and drop
                getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, true );
            }
        }
    }

    @Override
    public void dragAndDropStopHover( DragTarget target )
    {
        getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, false );
    }

    @Override
    public void dropDragTarget( DragTarget target )
    {
        if ( target.getParentPC( ) == pc ) return;


        if ( target.getItem( ) != null )
        {
            if ( target.getParentPC( ) != null )
            {
                // attempt give drag & drop
                Inventory givingInventory = target.getParentPC( ).inventory;

                if ( target.getItemEquipSlot( ) != null )
                {
                    givingInventory.getGiveEquippedCallback( target.getItemEquipSlot( ), pc ).run( );
                }
                else
                {
                    int quantity = givingInventory.getUnequippedItems( ).getQuantity( target.getItem( ) );
                    givingInventory.getGiveCallback( target.getItem( ), quantity, pc ).run( );
                }

            }
            else if ( target.getItemContainer( ) != null && Game.mainViewer.containerWindow.getOpener( ) == getCreature( ) )
            {
                // attempt pick up drag & drop, only for the container opener
                Container container = target.getItemContainer( );

                int quantity = container.getCurrentItems( ).getQuantity( target.getItem( ) );
                pc.inventory.getTakeCallback( target.getItem( ), quantity, container ).run( );

            }
            else if ( target.getItemMerchant( ) != null )
            {
                // attempt buy drag & drop
                Merchant merchant = target.getItemMerchant( );
                int merchantQuantity = target.getItemMerchant( ).getCurrentItems( ).getQuantity( target.getItem( ) );
                int maxQuantity = ItemListViewer.getMerchantBuyMaxQuantity( merchant, target.getItem( ), merchantQuantity );

                if ( maxQuantity > 0 )
                {
                    // don't allow buy attempts when the item can't be afforded
                    pc.inventory.getBuyCallback( target.getItem( ), maxQuantity, target.getItemMerchant( ) ).run( );
                }
            }
        }

        getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, false );
    }
}
