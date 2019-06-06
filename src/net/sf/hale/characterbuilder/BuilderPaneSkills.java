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
import java.util.Comparator;
import java.util.List;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ScrollPane;

import net.sf.hale.Game;
import net.sf.hale.entity.PC;
import net.sf.hale.rules.Skill;
import net.sf.hale.rules.SkillSet;
import net.sf.hale.util.Logger;

/**
 * The BuilderPane for editing a character's skills
 *
 * @author Jared Stephen
 */

public class BuilderPaneSkills extends BuilderPane implements PointAllocatorModel.Listener
{
    private PointAllocatorModel points;

    private List< SkillSelector > selectors;

    private final Button resetButton, roleButton;

    /**
     * Creates a new BuilderPane editing the skills of the specified character
     *
     * @param builder   the CharacterBuilder containing this BuilderPane
     * @param character the Buildable character whose skills will be edited
     */

    public BuilderPaneSkills( CharacterBuilder builder, Buildable character )
    {
        super( builder, "Skills", character );

        setTitleText( "Select Skills" );

        points = new PointAllocatorModel( 1 );
        points.addListener( this );

        selectors = new ArrayList< SkillSelector >( );

        resetButton = new Button( "Reset Selections" );
        resetButton.setTheme( "resetbutton" );
        resetButton.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                resetValues( );
            }
        } );
        add( resetButton );

        roleButton = new Button( "Suggested Values" );
        roleButton.setTheme( "rolebutton" );
        roleButton.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                setRoleValues( );
            }
        } );
        add( roleButton );
    }

    private void resetValues( )
    {
        for ( SkillSelector selector : selectors )
        {
            int valueDiff = ( selector.baseValue - selector.getValue( ) ) / 5;

            selector.addModelPoints( valueDiff );
            selector.addValue( valueDiff );
        }
    }

    private void setRoleValues( )
    {
        resetValues( );

        List< SkillSelector > selectorsSorted = new ArrayList< SkillSelector >( selectors );
        selectorsSorted.sort( new Comparator< SkillSelector >( )
        {
            @Override
            public int compare( SkillSelector s1, SkillSelector s2 )
            {
                int s1Ranks = getCharacter( ).getSkillSet( ).getRanks( s1.skill );
                int s2Ranks = getCharacter( ).getSkillSet( ).getRanks( s2.skill );

                if ( s2Ranks > s1Ranks ) { return + 1; }
                else if ( s2Ranks < s1Ranks ) { return - 1; }
                else
                {
                    for ( String skillID : getCharacter( ).getBaseRole( ).getDefaultPlayerSkillSelections( ) )
                    {
                        if ( skillID.equals( s1.skill.getID( ) ) ) return - 1;

                        if ( skillID.equals( s2.skill.getID( ) ) ) return + 1;
                    }
                }

                Logger.appendToErrorLog( "Error in sorting skill selectors between " + s1.skill.getID( ) + " and " + s2.skill.getID( ) );

                return 0;
            }
        } );

        for ( SkillSelector selector : selectorsSorted )
        {
            // break if we are out of points
            if ( points.getRemainingPoints( ) < 5 ) break;

            selector.addModelPoints( 1 );
            selector.addValue( 1 );
        }
    }

    @Override
    protected int getAdditionalSelectorPaneHeightLimit( )
    {
        return Math.max( resetButton.getPreferredHeight( ), roleButton.getPreferredHeight( ) );
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        ScrollPane pane = getSelectorPane( );

        resetButton.setSize( resetButton.getPreferredWidth( ), resetButton.getPreferredHeight( ) );
        roleButton.setSize( roleButton.getPreferredWidth( ), roleButton.getPreferredHeight( ) );

        resetButton.setPosition( getInnerX( ), pane.getBottom( ) );
        roleButton.setPosition( resetButton.getRight( ), pane.getBottom( ) );
    }

    @Override
    public void updateCharacter( )
    {
        for ( SkillSelector selector : selectors )
        {
            points.removeListener( selector );
        }
        selectors.clear( );
        clearSelectors( );

        SkillSet baseSkills = getCharacter( ).getSkillSet( );
        SkillSet alreadySelected = getCharacter( ).getSelectedSkills( );

        int skillLevelMax = Game.ruleset.getValue( "SkillsMaxPerLevel" ) * getCharacter( ).getCreatureLevel( );

        int intelligence = getCharacter( ).getCurrentIntelligence( );

        int totalPoints = getCharacter( ).getSelectedRole( ).getSkillPointsPerLevel( );
        totalPoints += ( intelligence - 10 );
        totalPoints += getCharacter( ).getUnspentSkillPoints( );

        points.setPointsRemaining( totalPoints );

        for ( Skill skill : getCharacter( ).getSelectableSkills( ) )
        {
            SkillSelector selector = new SkillSelector( skill, baseSkills.getRanks( skill ) );
            selector.setMinMaxValue( selector.getValue( ), skillLevelMax );
            selector.setPointAllocatorModel( points );

            if ( alreadySelected != null )
            {
                int added = alreadySelected.getRanks( skill ) / 5;
                selector.addValue( added );
                selector.addModelPoints( added );
            }

            selectors.add( selector );
            addSelector( selector );
        }

        allocatorModelUpdated( );

        if ( alreadySelected == null )
        {
            setRoleValues( );
        }
    }

    @Override
    public void allocatorModelUpdated( )
    {
        double points = this.points.getRemainingPoints( );

        long pointsInt = Math.round( points );
        if ( pointsInt == 1 )
        {
            setPointsText( "1 point remaining" );
        }
        else
        {
            setPointsText( pointsInt + " points remaining" );
        }

        boolean nextEnabled = true;
        for ( SkillSelector selector : selectors )
        {
            if ( selector.canIncrement( ) )
            {
                nextEnabled = false;
                break;
            }
        }

        getNextButton( ).setEnabled( nextEnabled );
    }

    @Override
    protected void next( )
    {
        PC workingCopy = getCharacter( ).getWorkingCopy( );

        SkillSet oldSkills = getCharacter( ).getSkillSet( );
        SkillSet newSkills = new SkillSet( workingCopy );
        for ( SkillSelector selector : selectors )
        {
            int newRanks = selector.getValue( ) - oldSkills.getRanks( selector.skill );
            if ( newRanks > 0 ) newSkills.addRanks( selector.skill, newRanks );
        }

        getCharacter( ).setSelectedSkills( newSkills, ( int ) Math.round( points.getRemainingPoints( ) ) );

        super.next( );
    }

    @Override
    protected void back( )
    {
        if ( getCharacter( ).isNewCharacter( ) )
        {
            super.back( );
        }
        else
        {
            AbstractBuilderPane prev = getCharacterBuilder( ).getPreviousPane( this );
            AbstractBuilderPane prevPrev = getCharacterBuilder( ).getPreviousPane( prev );
            getCharacterBuilder( ).setActivePane( prevPrev );
        }
    }

    private class SkillSelector extends BuildablePropertySelector
    {
        private Skill skill;
        private final int baseValue;

        private SkillSelector( Skill skill, int baseValue )
        {
            super( skill.getNoun( ), skill.getIcon( ), true );
            this.skill = skill;
            this.baseValue = baseValue;

            setValue( baseValue );
        }

        @Override
        protected void addValue( int value )
        {
            setValue( getValue( ) + 5 * value );
        }

        @Override
        protected void addModelPoints( int points )
        {
            BuilderPaneSkills.this.points.allocatePoints( 5.0 * points );
        }

        @Override
        protected void setIncrementDecrementEnabled( )
        {
            getIncrementButton( ).setEnabled( isEnabled( ) && getValue( ) < getMaxValue( ) && canIncrement( ) );
            getDecrementButton( ).setEnabled( isEnabled( ) && getValue( ) > getMinValue( ) );
        }

        private boolean canIncrement( )
        {
            if ( points != null ) { return points.getRemainingPoints( ) >= 5; }
            else { return true; }
        }

        @Override
        protected void onMouseHover( )
        {
            StringBuilder content = new StringBuilder( );
            content.append( "<div style=\"font-family: red; margin-bottom: 1em;\">" );
            content.append( skill.getNoun( ) ).append( "</div>" );

            if ( skill.isRestrictedToARole( ) )
            {
                content.append( "<div style=\"margin-bottom: 1em;\">Restricted to " );
                content.append( "<span style=\"font-family: red;\">" );
                content.append( skill.getRestrictToRole( ) ).append( "</span></div>" );
            }

            content.append( "<div style=\"margin-bottom: 1em;\">Key Attribute: <span style=\"font-family: blue;\">" );
            content.append( skill.getKeyAttribute( ).name ).append( "</span></div>" );


            if ( ! skill.isUsableUntrained( ) )
            {
                content.append( "<div style=\"font-family: green; margin-bottom: 1em;\">Requires training</div>" );
            }

            if ( skill.suffersArmorPenalty( ) )
            {
                content.append( "<div style=\"font-family: green; margin-bottom: 1em;\">Armor Penalty applies</div>" );
            }

            content.append( skill.getDescription( ) );
            getTextModel( ).setHtml( content.toString( ) );
            getTextPane( ).invalidateLayout( );
        }
    }
}
