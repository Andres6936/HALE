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

import net.sf.hale.Game;
import net.sf.hale.area.Transition;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Trap;
import net.sf.hale.util.Point;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

/**
 * Widget for displaying basic information about an Entity or AreaTransition
 * on mouse over
 *
 * @author Jared Stephen
 */

public class EntityMouseover extends Widget
{
    private final Label name;
    private final Label status;

    private Entity selectedEntity;
    private Point gridPoint;

    /**
     * Creates a new EntityMouseover.  You must first set the point
     * with {@link #setPoint(Point)} before anything is shown
     */

    public EntityMouseover( )
    {
        name = new Label( );
        name.setTheme( "namelabel" );
        this.add( name );

        status = new Label( );
        status.setTheme( "statuslabel" );
        this.add( status );
    }

    /**
     * Returns the entity that is currently being hovered by this Mouseover
     * or null if there is no such entity
     *
     * @return the mouse over entity
     */

    public Entity getSelectedEntity( ) { return selectedEntity; }

    /**
     * Returns the grid point that is currently being hovered or null if
     * this mouseover is inactive
     *
     * @return the grid point that is being hovered
     */

    public Point getPoint( ) { return gridPoint; }

    /**
     * Sets the point that is currently hovered.  Looks for entities and transitions in the
     * grid point and hovers them if they exist.
     *
     * @param gridPoint the grid point to hover
     */

    public void setPoint( Point gridPoint )
    {
        boolean newPoint = ! gridPoint.equals( this.gridPoint );
        this.gridPoint = gridPoint;

        if ( ! Game.curCampaign.curArea.isVisible( gridPoint ) )
        {
            setVisible( false );
            return;
        }

        Creature creature = Game.curCampaign.curArea.getCreatureAtGridPoint( gridPoint );
        Door door = Game.curCampaign.curArea.getDoorAtGridPoint( gridPoint );
        Container container = Game.curCampaign.curArea.getContainerAtGridPoint( gridPoint );
        Trap trap = Game.curCampaign.curArea.getTrapAtGridPoint( gridPoint );
        Transition transition = Game.curCampaign.curArea.getTransitionAtGridPoint( gridPoint );

        if ( transition != null && transition.isActivated( ) )
        {
            selectedEntity = null;
            name.setText( "Travel to" );
            Transition.EndPoint endPoint = transition.getEndPointForCreaturesInCurrentArea( );
            status.setText( endPoint.getLabel( ) );

        }
        else if ( creature != null )
        {
            selectedEntity = creature;
            name.setText( creature.getTemplate( ).getName( ) );
            if ( creature.isDead( ) )
            { status.setText( "Dead" ); }
            else if ( creature.isPlayerFaction( ) || creature.isSummoned( ) ||
                    creature.getFaction( ).isHostile( Game.curCampaign.party.getSelected( ) ) )
            // only show HP for hostile creatures or player characters
            { status.setText( creature.getCurrentHitPoints( ) + " / " + creature.stats.get( Stat.MaxHP ) ); }
            else
            { status.setText( "" ); }

        }
        else if ( door != null )
        {
            selectedEntity = door;
            name.setText( door.getTemplate( ).getName( ) );
            if ( door.isLocked( ) ) { status.setText( "Locked" ); }
            else { status.setText( "" ); }

        }
        else if ( container != null )
        {
            selectedEntity = container;
            name.setText( container.getTemplate( ).getName( ) );
            if ( container.isLocked( ) ) { status.setText( "Locked" ); }
            else { status.setText( "" ); }

        }
        else if ( trap != null && trap.isSpotted( ) )
        {
            selectedEntity = trap;
            name.setText( trap.getTemplate( ).getName( ) );
            status.setText( "" );

        }
        else
        {
            this.gridPoint = null;
            this.selectedEntity = null;
        }

        setVisible( this.gridPoint != null );

        if ( isVisible( ) && newPoint )
        {
            Game.mainViewer.invalidateLayout( );
        }
    }

    @Override
    public int getPreferredWidth( )
    {
        return Math.max( name.getPreferredWidth( ), status.getPreferredWidth( ) ) + getBorderHorizontal( );
    }

    @Override
    public int getPreferredHeight( )
    {
        return name.getPreferredHeight( ) + status.getPreferredHeight( ) + getBorderHorizontal( );
    }

    @Override
    protected void layout( )
    {
        int centerX = getInnerX( ) + getInnerWidth( ) / 2;

        name.setPosition( centerX - name.getPreferredWidth( ) / 2, getInnerY( ) + name.getPreferredHeight( ) / 2 );
        status.setPosition( centerX - status.getPreferredWidth( ) / 2,
                            getInnerY( ) + name.getPreferredHeight( ) + status.getPreferredHeight( ) / 2 );
    }

    @Override
    public boolean handleEvent( Event evt )
    {
        Game.areaListener.handleEvent( evt );

        return false;
    }
}
