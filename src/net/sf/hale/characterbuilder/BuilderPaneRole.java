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

package net.sf.hale.characterbuilder;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.entity.PC;
import net.sf.hale.rules.Role;

/**
 * The BuilderPane for adding levels to a character's set of roles.
 *
 * @author Jared Stephen
 */

public class BuilderPaneRole extends BuilderPane implements PointAllocatorModel.Listener
{
    private List< RoleSelector > roleSelectors;
    private PointAllocatorModel points;

    private PC workingCopy;

    /**
     * Create a new BuilderPaneRole editing the roles for the
     * specified character.
     *
     * @param character the Buildable character to edit
     */
    public BuilderPaneRole( CharacterBuilder builder, Buildable character )
    {
        super( builder, "Role", character );

        if ( ! character.isNewCharacter( ) )
        {
            getBackButton( ).setVisible( false );
        }

        roleSelectors = new ArrayList< RoleSelector >( );

        points = new PointAllocatorModel( 1 );
        points.addListener( this );

        super.setTitleText( "Select a Role" );

        allocatorModelUpdated( );

        workingCopy = this.getCharacter( ).getWorkingCopy( );
    }

    @Override
    protected void next( )
    {
        setCharacterRole( );

        if ( getCharacter( ).isNewCharacter( ) )
        {
            super.next( );
        }
        else
        {
            AbstractBuilderPane next = getCharacterBuilder( ).getNextPane( this );
            if ( next == null ) return;
            AbstractBuilderPane nextNext = getCharacterBuilder( ).getNextPane( next );
            if ( nextNext != null ) getCharacterBuilder( ).setActivePane( nextNext );
        }
    }

    private RoleSelector addSelector( Role role )
    {
        RoleSelector selector = new RoleSelector( role );

        int level = getCharacter( ).getLevel( role );

        selector.setValue( level );
        selector.setMinMaxValue( level, level + 1 );
        selector.setPointAllocatorModel( points );

        if ( role == getCharacter( ).getSelectedRole( ) )
        {
            selector.addModelPoints( 1 );
            selector.addValue( 1 );
        }

        roleSelectors.add( selector );
        super.addSelector( selector );

        return selector;
    }

    @Override
    protected void updateCharacter( )
    {
        for ( RoleSelector selector : roleSelectors )
        {
            points.removeListener( selector );
        }
        roleSelectors.clear( );
        clearSelectors( );
        points.setPointsRemaining( 1 );

        // first add selectable roles
        for ( Role role : getCharacter( ).getSelectableRoles( ) )
        {
            addSelector( role );
        }

        // now add future selectable roles
        for ( Role role : getCharacter( ).getFutureOrPastSelectableRoles( ) )
        {
            RoleSelector selector = addSelector( role );
            selector.setEnabled( false );
        }

        for ( RoleSelector selector : roleSelectors )
        {
            selector.allocatorModelUpdated( );
        }

        allocatorModelUpdated( );
    }

    private void setCharacterRole( )
    {
        for ( RoleSelector selector : roleSelectors )
        {
            if ( selector.getValue( ) > selector.getMinValue( ) )
            { getCharacter( ).setSelectedRole( selector.role ); }
        }
    }

    @Override
    public void allocatorModelUpdated( )
    {
        getNextButton( ).setEnabled( points.getRemainingPoints( ) < 1.0 );
    }

    private void setTextAreaContent( Role role )
    {
        StringBuilder sb = new StringBuilder( );

        role.appendDescription( sb, workingCopy );

        getTextModel( ).setHtml( sb.toString( ) );
        getTextPane( ).invalidateLayout( );
    }

    private class RoleSelector extends BuildablePropertySelector
    {
        private Role role;

        public RoleSelector( Role role )
        {
            super( role.getName( ), role.getIcon( ), true );

            this.role = role;
        }

        @Override
        protected void onMouseHover( )
        {
            setTextAreaContent( role );
        }
    }
}
