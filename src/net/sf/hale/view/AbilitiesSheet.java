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
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.ability.AbilitySelectionList;
import net.sf.hale.ability.CreatureAbilitySet;
import net.sf.hale.characterbuilder.AbilitySelectionListPane;
import net.sf.hale.characterbuilder.AbilitySelectorButton;
import net.sf.hale.entity.PC;
import net.sf.hale.rules.Role;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The view Widget for the set of known Abilities and AbilitySlots for a given
 * Creature.  In the Game, the AbilitiesSheet is contained within the
 * CharacterPane as one of its Tabs.
 * <p>
 * Each time updateContent is called, the entire contents of this view are
 * recreated, allowing a single instance to show Abilities for different Creatures
 * and to update as stats change.
 *
 * @author Jared Stephen
 */

public class AbilitiesSheet extends ScrollPane implements CreatureAbilitySet.Listener,
        AbilitySelectorButton.HoverHolder
{

    private boolean abilitySetModified;
    private PC parent;

    private int viewerGap;

    private Content content;

    private List< AbilitySelectionListPane > listPanes;

    private Widget hoverTop, hoverBottom;

    /**
     * Creates a new, empty AbilitiesSheet
     */

    public AbilitiesSheet( )
    {
        content = new Content( );
        setContent( content );

        listPanes = new ArrayList< AbilitySelectionListPane >( );
    }

    @Override
    public void abilitySetModified( )
    {
        abilitySetModified = true;
    }

    /**
     * Rebuilds the content of this Widget to show the Abilities and
     * AbilitySlots for the specified Creature.
     *
     * @param parent the Creature whose Abilities are to be shown in this
     *               Widget
     */

    public void updateContent( PC parent )
    {
        if ( ! abilitySetModified && parent == this.parent ) return;

        if ( parent != this.parent )
        {
            if ( this.parent != null )
            { parent.abilities.removeListener( this ); }

            this.parent = parent;
            parent.abilities.addListener( this );
        }

        if ( abilitySetModified )
        {
            abilitySetModified = false;
        }

        rebuildAbilityLists( );
    }

    private void rebuildAbilityLists( )
    {
        for ( AbilitySelectionListPane pane : listPanes )
        {
            content.removeChild( pane );
        }
        listPanes.clear( );

        List< AbilitySelectionList > lists = new ArrayList< AbilitySelectionList >( );

        // get all the list referenced by all roles for the parent Creature
        for ( String id : parent.roles.getRoleIDs( ) )
        {
            Role role = Game.ruleset.getRole( id );

            lists.addAll( role.getAllReferencedAbilitySelectionLists( ) );
        }

        // get all the lists references by the race for the parent creature
        lists.addAll( parent.getTemplate( ).getRace( ).getAllReferencedAbilitySelectionLists( ) );

        List< String > listsAlreadyAdded = new ArrayList< String >( );

        // add the list buttons
        for ( AbilitySelectionList list : lists )
        {
            if ( listsAlreadyAdded.contains( list.getID( ) ) ) continue;

            AbilitySelectionListPane pane = new AbilitySelectionListPane( list, parent, this, false, listsAlreadyAdded );
            listPanes.add( pane );
            content.add( pane );

            addListsRecursive( listsAlreadyAdded, list );
        }
    }

    private void addListsRecursive( List< String > listIDs, AbilitySelectionList list )
    {
        listIDs.add( list.getID( ) );

        for ( String id : list.getSubListIDs( ) )
        {
            AbilitySelectionList subList = Game.ruleset.getAbilitySelectionList( id );
            addListsRecursive( listIDs, subList );
        }
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        viewerGap = themeInfo.getParameter( "viewerGap", 5 );
    }

    private class Content extends Widget
    {
        @Override
        public void layout( )
        {
            super.layout( );

            int curY = getInnerY( );

            for ( AbilitySelectionListPane pane : listPanes )
            {
                pane.setSize( pane.getPreferredWidth( ), pane.getPreferredHeight( ) );
                pane.setPosition( getInnerX( ) + pane.getGridSize( ) / 2, curY );

                curY = pane.getBottom( ) + viewerGap;
            }
        }

        @Override
        public int getPreferredInnerWidth( )
        {
            int width = 0;
            for ( AbilitySelectionListPane pane : listPanes )
            {
                width = Math.max( width, pane.getPreferredWidth( ) );
            }
            return width;
        }

        @Override
        public int getPreferredInnerHeight( )
        {
            int height = 0;
            for ( AbilitySelectionListPane pane : listPanes )
            {
                height += pane.getPreferredHeight( ) + viewerGap;
            }
            return height;
        }
    }

    @Override
    public void removeHoverWidgets( Widget hoverTop, Widget hoverBottom )
    {
        if ( this.hoverTop != null && this.hoverTop == hoverTop )
        {
            content.removeChild( this.hoverTop );
            this.hoverTop = null;
        }

        if ( this.hoverBottom != null && this.hoverBottom == hoverBottom )
        {
            content.removeChild( this.hoverBottom );
            this.hoverBottom = null;
        }
    }

    @Override
    public void setHoverWidgets( Widget hoverTop, Widget hoverBottom )
    {
        if ( this.hoverTop != null ) content.removeChild( this.hoverTop );
        if ( this.hoverBottom != null ) content.removeChild( this.hoverBottom );

        this.hoverTop = hoverTop;
        this.hoverBottom = hoverBottom;

        if ( hoverTop != null )
        {
            content.add( hoverTop );
        }

        if ( hoverBottom != null )
        {
            content.add( hoverBottom );
        }
    }
}
