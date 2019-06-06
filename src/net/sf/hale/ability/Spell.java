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

package net.sf.hale.ability;

import java.util.List;

import de.matthiasmann.twl.Color;

import net.sf.hale.Game;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Creature;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A Spell is a special type of Ability with some additional information such
 * as spell components and spell level.  Note that spells do not support
 * inline scripts; the script must be specified as an external file
 *
 * @author Jared Stephen
 */

public class Spell extends Ability
{
    private final boolean verbalComponent;
    private final boolean somaticComponent;
    private final boolean spellResistanceApplies;

    /**
     * Create a new Spell with the specified parameters
     *
     * @param id             the ID String of the Spell
     * @param script         the script contents of the Spell
     * @param scriptLocation the resource location of the script for the Spell
     * @param data           the JSON containing keys with values for the data making up
     *                       this Spell
     */

    protected Spell( String id, String script, String scriptLocation, SimpleJSONObject data )
    {
        super( id, script, scriptLocation, data, false );

        this.verbalComponent = data.get( "hasVerbalComponent", true );
        this.somaticComponent = data.get( "hasSomaticComponent", true );
        this.spellResistanceApplies = data.get( "spellResistanceApplies", true );
    }

    /**
     * Returns true if and only if this Spell has a verbal component.  Spells with
     * verbal components are affected by Silence effects.
     *
     * @return true if and only if this Spell has a verbal component
     */

    public boolean hasVerbalComponent( )
    {
        return verbalComponent;
    }

    /**
     * Returns true if and only if this Spell has a somatic component.  Spells with
     * somatic components are affected by spell failure due to armor.
     *
     * @return true if and only if this Spell has a somatic component
     */

    public boolean hasSomaticComponent( )
    {
        return somaticComponent;
    }

    /**
     * Returns true if and only if this Spell is affected by spell resistance.  Spell
     * resistance can reduce the damage and duration of spells, but some spells are not
     * affected by it.
     *
     * @return true if and only if this Spell is affected by spell resistance
     */

    public boolean spellResistanceApplies( )
    {
        return spellResistanceApplies;
    }

    /**
     * In addition to the actions performed by the parent Object,
     * also performs search checks for the parent Entity.
     * <p>
     * See {@link #activate(Creature)}
     */

    @Override
    public void activate( Creature parent )
    {
        super.activate( parent );

        Game.scriptInterface.performSearchChecksForCreature( parent, Game.ruleset.getValue( "HideCastSpellPenalty" ) );
    }

    @Override
    public int getCooldown( Creature parent )
    {
        int baseCooldown = super.getCooldown( parent );

        return baseCooldown - parent.stats.get( Bonus.Type.SpellCooldown );
    }

    /**
     * Determines whether the parent casting this Spell should suffer random Spell
     * failure.  It is assumed that the parent is suffering no additional failure due
     * to target concealment.  This is typically the situation when the caster is casting
     * a point in the Area (not a hostile creature)
     *
     * @param parent the parent Creature that is casting this Spell
     * @return true if the spell should be cast successfully, false if the spell should fail
     */

    public boolean checkSpellFailure( Creature parent )
    {
        return checkSpellFailure( parent, 0, null );
    }

    /**
     * Determines whether the parent casting this Spell should suffer random Spell failure.
     * For hostile or neutral targets, the parent takes a concealment penalty based on the concealment between themselves
     * and the specified target.  This concealment penalty will most commonly be 0.  For friendly
     * targets (or when the caster is the target), concealment penalty is always zero
     *
     * @param parent the Creature casting this Spell
     * @param target the target Creature for this Spell
     * @return true if the spell should be cast successfully, false if the spell should fail
     */

    public boolean checkSpellFailure( Creature parent, Creature target )
    {
        if ( parent.getLocation( ).equals( target.getLocation( ) ) || parent.getFaction( ).isFriendly( target ) )
        { return checkSpellFailure( parent, 0, target ); }

        int concealment = parent.getLocation( ).getArea( ).getConcealment( parent, target );

        return checkSpellFailure( parent, concealment, target );
    }

    /**
     * Determines whether the parent casting this Spell should suffer random Spell failure.
     * The parent takes a concealment penalty based on the largest concealment between
     * themselves and any individual target.  This case should be used when casting a spell
     * targeting multiple individual hostile creatures.
     *
     * @param parent  the Creature casting this Spell
     * @param targets the List of targets for this Spell
     * @return true if the spell should be cast successfully, false if the spell should fail
     */

    public boolean checkSpellFailure( Creature parent, List< Creature > targets )
    {
        int bestConcealment = 0;
        for ( Creature defender : targets )
        {
            bestConcealment = Math.max( bestConcealment, parent.getLocation( ).getArea( ).getConcealment( parent, defender ) );
        }

        return checkSpellFailure( parent, bestConcealment, null );
    }

    /**
     * Returns the integer percentage chance for the specified parent casting this spell to fail, assuming
     * no concealment penalty
     *
     * @param parent the parent casting the spell
     * @return the spell failure chance
     */

    public int getSpellFailurePercentage( Creature parent )
    {
        int failure = parent.stats.getBaseSpellFailure( getSpellLevel( parent ) ) +
                parent.roles.getThreatenedSpellFailure( );

        if ( verbalComponent )
        {
            failure += parent.roles.getVerbalSpellFailure( );
        }

        if ( somaticComponent )
        {
            failure += parent.roles.getSomaticSpellFailure( );
        }

        return Math.min( 100, failure );
    }

    /*
     * The base method used to determine spell failure in all of the above cases
     */

    private boolean checkSpellFailure( Creature parent, int concealmentPenalty, Creature target )
    {
        int failure = parent.stats.getBaseSpellFailure( getSpellLevel( parent ) );
        int check = Game.dice.d100( );

        boolean verbal = false, threatening = false, concealment = false;

        StringBuilder message = new StringBuilder( );
        message.append( parent.getTemplate( ).getName( ) );
        message.append( " casts " ).append( getName( ) );

        if ( target != null )
        {
            message.append( " on " ).append( target.getName( ) );
        }

        message.append( " with " );

        if ( verbalComponent )
        {
            int verbalFailure = parent.roles.getVerbalSpellFailure( );

            if ( verbalFailure >= Integer.MAX_VALUE / 10 )
            {
                // this implies the parent or area is silenced
                message.append( "100% failure due to silence : Failure." );
                Game.mainViewer.addMessage( message.toString( ) );
                Game.mainViewer.addFadeAway( "Spell Failed!", parent.getLocation( ).getX( ),
                                             parent.getLocation( ).getY( ), new Color( 0xFFAbA9A9 ) );
                return false;
            }

            if ( verbalFailure > 0 )
            {
                verbal = true;
                failure += verbalFailure;
            }
        }


        if ( somaticComponent )
        {
            failure += parent.roles.getSomaticSpellFailure( );
        }

        int threatenedFailure = parent.roles.getThreatenedSpellFailure( );
        if ( threatenedFailure > 0 )
        {
            threatening = true;
            failure += threatenedFailure;
        }

        if ( concealmentPenalty > 0 )
        {
            concealment = true;
            failure += concealmentPenalty;
        }

        int failureCount = ( verbal ? 1 : 0 ) + ( threatening ? 1 : 0 ) + ( concealment ? 1 : 0 );

        message.append( Math.max( 0, failure ) );
        message.append( "% failure" );

        // now create the failure component message if needed
        if ( failureCount > 0 )
        {
            message.append( " due to " );

            if ( verbal )
            {
                message.append( "deafness" );

                if ( failureCount == 3 )
                { message.append( ", " ); }
                else if ( failureCount == 2 )
                { message.append( " and " ); }

                failureCount--;
            }

            if ( concealment )
            {
                message.append( "concealment" );

                if ( failureCount == 2 )
                { message.append( " and " ); }
            }

            if ( threatening )
            {
                message.append( "threatening creatures" );
            }
        }

        if ( check <= failure )
        {
            message.append( " : Failure." );

            Game.mainViewer.addFadeAway( "Spell Failed!", parent.getLocation( ).getX( ),
                                         parent.getLocation( ).getY( ), new Color( 0xFFAbA9A9 ) );
        }
        else
        {
            message.append( " : Success." );
        }

        Game.mainViewer.addMessage( message.toString( ) );

        return check > failure;
    }

    /**
     * The specified target takes the specified type and amount of damage.  The damage can be
     * modified by spell bonuses on the parent Creature.  The damage will be modified by
     * spell resistance if this Spell has {@link #spellResistanceApplies()} equal to true.
     *
     * @param parent     the parent or caster of this Spell
     * @param target     the target that will take the damage
     * @param damage     the number of hit points of damage to apply before modification based
     *                   on resistances and bonuses
     * @param damageType the {@link net.sf.hale.rules.DamageType} of the damage to apply
     */

    public void applyDamage( Creature parent, Creature target, int damage, String damageType )
    {
        // determine multiplier from the parent caster
        int spellDamageMult = parent.stats.get( Bonus.Type.SpellDamage ) +
                parent.stats.get( damageType, Bonus.Type.DamageForSpellType );

        damage = ( damage * ( 100 + spellDamageMult ) ) / 100;

        // determine spell resistance factor
        int spellResistance = Math.max( 0, spellResistanceApplies ? target.stats.get( Bonus.Type.SpellResistance ) : 0 );

        if ( spellResistance != 0 )
        {
            Game.mainViewer.addMessage( "blue", target.getTemplate( ).getName( ) + "'s Spell Resistance absorbs " +
                    spellResistance + "% of the spell." );
        }

        int damageMult = 100 - spellResistance;
        if ( damageMult < 0 ) damageMult = 0;

        damage = ( damage * damageMult ) / 100;
        target.takeDamage( damage, damageType );

        // check to add the parent as a hostile to the target
        if ( parent.getFaction( ).isHostile( target ) && target.getEncounter( ) != null )
        {
            target.getEncounter( ).checkAddHostile( parent );
        }
    }

    /**
     * The specified target is healed by the specified number of hit points of damage.  This damage
     * can be modified based on bonuses on the parent Creature and spell resistance on the target
     * Creature, if spell resistance applies to this Spell.
     *
     * @param parent the parent caster of this Spell
     * @param target the target that will have damage healed
     * @param damage the number of hit points to heal
     */

    public void applyHealing( Creature parent, Creature target, int damage )
    {
        int bonusHealingFactor = parent.stats.get( Bonus.Type.SpellHealing );

        damage = ( damage * ( 100 + bonusHealingFactor ) ) / 100;

        int spellResistance = Math.max( 0, spellResistanceApplies ? target.stats.get( Bonus.Type.SpellResistance ) : 0 );

        if ( spellResistance == 0 )
        {
            target.healDamage( damage );
        }
        else
        {
            int damageMult = Math.min( 0, 100 - spellResistance );
            target.healDamage( damage * damageMult / 100 );
        }
    }

    /*
     * (non-Javadoc)
     * @see net.sf.hale.ability.Ability#setSpellDuration(net.sf.hale.ability.Effect, net.sf.hale.ability.AbilityActivator)
     */

    @Override
    public void setSpellDuration( Effect effect, Creature parent )
    {
        int duration = effect.getRoundsRemaining( );
        // compute SpellDuration bonus
        int durationBonus = parent.stats.get( Bonus.Type.SpellDuration );
        duration = duration * ( 100 + durationBonus ) / 100;

        // compute spell resistance modification
        if ( spellResistanceApplies )
        {
            int spellResistanceMultiplier = Math.min( 100, Math.max( 0, 100 - effect.getTarget( ).getSpellResistance( ) ) );
            duration = duration * spellResistanceMultiplier / 100;
        }

        effect.setDuration( duration );
    }

    public int getCheckDifficulty( Creature parent )
    {
        return 50 + ( parent.stats.getSpellCastingAttribute( ) - 10 ) * 2 + 3 * parent.stats.getCasterLevel( );
    }

    @Override
    public void appendDetails( StringBuilder sb, Creature parent, boolean upgrade )
    {
        sb.append( "<p><span style=\"font-family: medium-blue\">Spell</span></p>" );

        super.appendDetails( sb, parent, upgrade );

        sb.append( "<table style=\"font-family: medium; vertical-align: middle; margin-top: 1em;\">" );

        sb.append( "<tr><td style=\"width: 10ex;\">" );
        sb.append( "Spell Level</td><td style=\"font-family: medium-blue\">" );
        sb.append( getSpellLevel( parent ) ).append( "</td></tr>" );

        sb.append( "</table>" );

        sb.append( "<table style=\"font-family: medium; vertical-align: middle;\">" );
        if ( spellResistanceApplies )
        {
            sb.append( "<tr><td>Affected by " );
        }
        else
        {
            sb.append( "<tr><td>Ignores " );
        }
        sb.append( "<span style=\"font-family: medium-red\">Spell Resistance</span></td></tr>" );
        sb.append( "</table>" );
    }
}
