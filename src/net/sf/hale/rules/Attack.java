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

package net.sf.hale.rules;

import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.area.AreaEntityList;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Ammo;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Weapon;
import net.sf.hale.entity.WeaponTemplate;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/*
 * An attack should be used as follows.  Call the constructor as normal.
 * Then after that call ON_ATTACK scripts for the attacker and ON_DEFENSE scripts for the defender.
 * These can modify various parts of the attack such as the defender AC.
 * After this, call computeIsHit() which computes whether the attack hits and also finishes the attack.
 * Finally, call the ON_HIT scripts and then apply the damage to the target if you so choose.
 *
 * Note that calling computeIsHit() causes the attacker to be updated.  Before that point, the attacker is not
 * considered to have taken the attack.  The damage from the attack is not applied to the defender
 * anywhere in this class.
 */

public class Attack
{
    private int attackRoll = 0;
    private int damageRoll = 0;
    private int threatRoll = 0;
    private int baseAttackBonus = 0;
    private int attackBonus = 0;
    private float damageBonus = 0;
    private int totalAttack = 0;
    private int totalDamage = 0;
    private int rangePenalty = 0;
    private int defenderAC = 0;

    private int extraDamage = 0;
    private int extraAttack = 0;

    private boolean negateDamage = false;
    private Damage damage = null;

    private int damageMin = 0, damageMax = 0;

    private int appliedDamage = 0;

    private int flankingBonus = 0;
    private boolean flankingAttack = false;

    private boolean hit = false;
    private boolean meleeTouchAttack = false;
    private boolean rangedTouchAttack = false;

    private Creature attacker;
    private Creature defender;
    private Weapon weapon;
    private Inventory.Slot inventorySlot;

    private StringBuilder message;

    public boolean damageNegated( ) { return negateDamage; }

    public void negateDamage( ) { negateDamage = true; }

    public void addDamage( Damage damage )
    {
        this.damage.add( damage );
    }

    public Damage getDamage( ) { return damage; }

    public String getMessage( ) { return message.toString( ); }

    public int getExtraAttack( ) { return extraAttack; }

    public int getExtraDamage( ) { return extraDamage; }

    public int getAppliedDamage( ) { return appliedDamage; }

    public int getFlankingBonus( ) { return flankingBonus; }

    public boolean isFlankingAttack( ) { return flankingAttack; }

    public boolean isMeleeWeaponAttack( )
    {
        if ( meleeTouchAttack || rangedTouchAttack ) return false;

        if ( weapon == null ) return false;

        return weapon.isMelee( );
    }

    public boolean isRangedWeaponAttack( )
    {
        if ( meleeTouchAttack || rangedTouchAttack ) return false;

        if ( weapon == null ) return false;

        return weapon.isRanged( );
    }

    public boolean isRanged( )
    {
        if ( rangedTouchAttack ) return true;

        if ( weapon == null ) return false;

        return weapon.isRanged( );
    }

    public boolean causesDamage( )
    {
        // if touch attack
        if ( damage == null ) return false;

        return damage.causesDamage( );
    }

    public int getAttackRoll( ) { return attackRoll; }

    public int getDamageRoll( ) { return damageRoll; }

    public int getThreatRoll( ) { return threatRoll; }

    public int getBaseAttackBonus( ) { return baseAttackBonus; }

    public int getAttackBonus( ) { return attackBonus; }

    public float getDamageBonus( ) { return damageBonus; }

    public int getTotalAttack( ) { return totalAttack; }

    public int getTotalDamage( ) { return totalDamage; }

    public int getRangePenalty( ) { return rangePenalty; }

    public int getDefenderAC( ) { return defenderAC; }

    public int getMinimumDamage( ) { return damageMin; }

    public int getMaximumDamage( ) { return damageMax; }

    public Creature getAttacker( ) { return attacker; }

    public Creature getDefender( ) { return defender; }

    public Weapon getWeapon( ) { return weapon; }

    public Inventory.Slot getSlot( ) { return inventorySlot; }

    public void addExtraAttack( int attack ) { this.extraAttack += attack; }

    public void addExtraDamage( int damage ) { this.extraDamage += damage; }

    public void setAppliedDamage( int appliedDamage ) { this.appliedDamage = appliedDamage; }

    public void setDefenderAC( int defenderAC ) { this.defenderAC = defenderAC; }

    public void setFlankingBonus( int flankingBonus ) { this.flankingBonus = flankingBonus; }

    public void setFlankingAttack( boolean flankingAttack ) { this.flankingAttack = flankingAttack; }

    // Touch attack (melee or ranged)
    public Attack( Creature attacker, Creature defender, boolean ranged )
    {
        this.attacker = attacker;
        this.defender = defender;

        meleeTouchAttack = ! ranged;
        rangedTouchAttack = ranged;

        defenderAC = defender.stats.get( Stat.TouchArmorClass ) + Game.curCampaign.curArea.getConcealment( attacker, defender );
        attackBonus = attacker.stats.get( Stat.LevelAttackBonus ) + attacker.stats.get( Stat.TouchAttackBonus );

        for ( RacialType racialType : attacker.getTemplate( ).getRace( ).getRacialTypes( ) )
        {
            defenderAC += defender.stats.get( racialType.getName( ), Bonus.Type.ArmorClassVsRacialType );
        }

        for ( RacialType racialType : defender.getTemplate( ).getRace( ).getRacialTypes( ) )
        {
            attackBonus += attacker.stats.get( racialType.getName( ), Bonus.Type.AttackVsRacialType );
        }

        attackRoll = Game.dice.d100( );
        totalAttack = attackRoll + attackBonus;
    }

    // Dummy non-weapon attack
    public Attack( Creature attacker, Creature defender )
    {
        this.attacker = attacker;
        this.defender = defender;
    }

    // Attack with a weapon
    public Attack( Creature attacker, Creature defender, Inventory.Slot slot )
    {
        this.attacker = attacker;
        this.defender = defender;
        this.message = new StringBuilder( );
        this.inventorySlot = slot;
        this.baseAttackBonus = attacker.stats.get( Stat.LevelAttackBonus );

        switch ( inventorySlot )
        {
            case MainHand:
                damageBonus = 1.0f + ( float ) attacker.stats.get( Stat.MainHandDamageBonus ) / 100.0f;
                attackBonus = attacker.stats.get( Stat.MainHandAttackBonus );
                break;
            case OffHand:
                damageBonus = 1.0f + ( float ) attacker.stats.get( Stat.OffHandDamageBonus ) / 100.0f;
                attackBonus = attacker.stats.get( Stat.OffHandAttackBonus );
                break;
            default:
                throw new IllegalArgumentException( "Attacks can only be made with main or off hand weapons" );
        }

        this.weapon = ( Weapon ) attacker.inventory.getEquippedItem( slot );
        if ( weapon == null ) weapon = attacker.getDefaultWeapon( );

        int concealment = Game.curCampaign.curArea.getConcealment( attacker, defender );
        if ( weapon.isRanged( ) )
        {
            concealment = Math.max( 0, concealment - attacker.stats.get( Bonus.Type.ConcealmentIgnoringRanged ) );
        }

        defenderAC = defender.stats.get( Stat.ArmorClass ) + concealment;

        for ( RacialType racialType : attacker.getTemplate( ).getRace( ).getRacialTypes( ) )
        {
            defenderAC += defender.stats.get( racialType.getName( ), Bonus.Type.ArmorClassVsRacialType );
        }

        for ( RacialType racialType : defender.getTemplate( ).getRace( ).getRacialTypes( ) )
        {
            attackBonus += attacker.stats.get( racialType.getName( ), Bonus.Type.AttackVsRacialType );
            damageBonus += ( float ) attacker.stats.get( racialType.getName( ), Bonus.Type.DamageVsRacialType ) / 100.0f;
        }

        Ammo quiver = ( Ammo ) attacker.inventory.getEquippedItem( Inventory.Slot.Quiver );
        int quiverAttackBonus = 0;
        int quiverDamageBonus = 0;

        if ( ! weapon.isMelee( ) && quiver != null )
        {
            int weaponRangePenalty = weapon.getTemplate( ).getRangePenalty( ) * ( 100 - attacker.stats.get( Bonus.Type.RangePenalty ) ) / 100;

            int distance = attacker.getLocation( ).getDistance( defender.getLocation( ) );
            rangePenalty += ( distance * weaponRangePenalty ) / 20;

            if ( weapon.getTemplate( ).getWeaponType( ) != WeaponTemplate.Type.Thrown )
            {
                quiverAttackBonus = quiver.getQualityAttackBonus( ) + quiver.bonuses.get( Bonus.Type.WeaponAttack );
                quiverDamageBonus = quiver.getQualityDamageBonus( ) + quiver.bonuses.get( Bonus.Type.WeaponDamage );
            }
        }

        attackRoll = Game.dice.d100( );
        damageRoll = Game.dice.rand( weapon.getTemplate( ).getMinDamage( ), weapon.getTemplate( ).getMaxDamage( ) );

        damageBonus += ( ( float ) attacker.stats.get( Stat.LevelDamageBonus ) ) / 100.0f;
        damageBonus += ( float ) ( weapon.getQualityDamageBonus( ) + weapon.bonuses.get( Bonus.Type.WeaponDamage ) ) / 100.0f;
        damageBonus += ( float ) ( quiverDamageBonus ) / 100.0f;

        attackBonus += baseAttackBonus - rangePenalty;
        attackBonus += weapon.bonuses.get( Bonus.Type.WeaponAttack ) + weapon.getQualityAttackBonus( ) + quiverAttackBonus;

        damageBonus += ( float ) attacker.stats.get( weapon.getTemplate( ).getDamageType( ).getName( ), Bonus.Type.DamageForWeaponType ) / 100.0f;
        attackBonus += attacker.stats.get( weapon.getTemplate( ).getDamageType( ).getName( ), Bonus.Type.AttackForWeaponType );

        totalAttack = attackRoll + attackBonus;
        totalDamage = ( int ) Math.round( ( ( float ) damageRoll * damageBonus ) );

        damageMin = ( int ) Math.round( ( ( float ) weapon.getTemplate( ).getMinDamage( ) * damageBonus ) );
        damageMax = ( int ) Math.round( ( ( float ) weapon.getTemplate( ).getMaxDamage( ) * damageBonus ) );

        damage = new Damage( defender, weapon.getTemplate( ).getDamageType( ), totalDamage );

        // add any standalone damage bonuses from the weapon or ammo
        // note that bonuses from a quiver will be applied here to melee attacks
        // if we were to create ammo with bonuses
        damage.add( attacker.stats.rollStandaloneDamage( defender ) );
    }

    public void computeFlankingBonus( AreaEntityList entities )
    {
        Point screenAtt = AreaUtil.convertGridToScreenAndCenter( attacker.getLocation( ).getX( ), attacker.getLocation( ).getY( ) );
        Point screenDef = AreaUtil.convertGridToScreenAndCenter( defender.getLocation( ).getX( ), defender.getLocation( ).getY( ) );

        List< Creature > creatures = entities.getCreaturesWithinRadius( defender.getLocation( ).getX( ),
                                                                        defender.getLocation( ).getY( ), Game.curCampaign.curArea.getVisibilityRadius( ) );

        for ( Creature flanker : creatures )
        {
            if ( flanker == attacker || flanker == defender ) continue;

            if ( ! flanker.threatensLocation( defender.getLocation( ) ) ) continue;

            if ( flanker.getFaction( ).getRelationship( defender ) != Faction.Relationship.Hostile ) continue;

            Point screenOth = AreaUtil.convertGridToScreenAndCenter( flanker.getLocation( ).getX( ), flanker.getLocation( ).getY( ) );

            double a2 = AreaUtil.euclideanDistance2( screenDef.x, screenDef.y, screenOth.x, screenOth.y );
            double b2 = AreaUtil.euclideanDistance2( screenDef.x, screenDef.y, screenAtt.x, screenAtt.y );
            double c2 = AreaUtil.euclideanDistance2( screenAtt.x, screenAtt.y, screenOth.x, screenOth.y );

            double a = Math.sqrt( a2 );
            double b = Math.sqrt( b2 );

            double theta = Math.acos( ( a2 + b2 - c2 ) / ( 2 * a * b ) ) * 360.0 / ( 2.0 * Math.PI );

            if ( theta > 140.0 - attacker.stats.get( Bonus.Type.FlankingAngle ) )
            {
                flankingBonus = 20;
                flankingAttack = true;
                Game.mainViewer.addMessage( "green", attacker.getTemplate( ).getName( ) + " and " +
                        flanker.getTemplate( ).getName( ) + " are flanking " + defender.getTemplate( ).getName( ) );
                return;
            }
        }
    }

    public boolean isHit( ) { return hit; }

    public boolean computeIsHit( )
    {
        attackBonus += flankingBonus;
        totalAttack += flankingBonus;

        if ( meleeTouchAttack ) { return isHitMeleeTouch( ); }
        else if ( rangedTouchAttack ) { return isHitRangedTouch( ); }
        else { return isHitNormal( ); }
    }

    private boolean isHitRangedTouch( )
    {
        message = new StringBuilder( );
        message.append( attacker.getTemplate( ).getName( ) + " attempts ranged touch attack on " +
                                defender.getTemplate( ).getName( ) + ": " );
        message.append( attackRoll + " + " + attackBonus + " = " + totalAttack + " vs " + defenderAC + ".  " );

        if ( attackRoll > 95 || ( attackRoll > 5 && totalAttack >= defenderAC ) )
        {
            message.append( "Succeeds." );
            hit = true;
        }
        else
        {
            message.append( "Miss." );
            hit = false;
        }

        return hit;
    }

    private boolean isHitMeleeTouch( )
    {
        message = new StringBuilder( );
        message.append( attacker.getTemplate( ).getName( ) + " attempts melee touch attack on " + defender.getTemplate( ).getName( ) + ": " );
        message.append( attackRoll + " + " + attackBonus + " = " + totalAttack + " vs " + defenderAC + ".  " );

        if ( attackRoll > 95 || ( attackRoll > 5 && totalAttack >= defenderAC ) )
        {
            message.append( "Succeeds." );
            hit = true;
        }
        else
        {
            message.append( "Miss." );
            hit = false;
        }

        return hit;
    }

    private boolean isHitNormal( )
    {
        message = new StringBuilder( );

        // remove ammo if needed
        if ( weapon.getTemplate( ).getWeaponType( ) == WeaponTemplate.Type.Thrown )
        {
            // remove one of the specified weapon.  This will remove from unequipped items first,
            // keeping the weapon equipped unless it is the last one
            attacker.inventory.remove( weapon );

        }
        else if ( ! weapon.isMelee( ) )
        {
            Ammo quiver = ( Ammo ) attacker.inventory.getEquippedItem( Inventory.Slot.Quiver );

            // remove one of the specified ammo.  This will remove from unequipped items first,
            // keeping the ammo equipped unless it is the last one
            if ( quiver != null )
            {
                attacker.inventory.remove( quiver );
            }
        }

        if ( attacker.getOffHandWeapon( ) != null )
        {
            switch ( inventorySlot )
            {
                case MainHand:
                    message.append( "<span style=\"font-family:green;\">[Main hand attack]</span> " );
                    break;
                case OffHand:
                    message.append( "<span style=\"font-family:green;\">[Off hand attack]</span> " );
                    break;
                default:
                    // do nothing
            }
        }

        boolean criticalHitImmunity = defender.stats.has( Bonus.Type.CriticalHitImmunity );

        // critical hit immunity also grants immunity to extraAttack / extraDamage
        if ( ! criticalHitImmunity )
        {
            totalAttack += extraAttack;
            attackBonus += extraAttack;
        }

        message.append( attacker.getTemplate( ).getName( ) + " attacks " + defender.getTemplate( ).getName( ) + ": " +
                                attackToString( ) + " vs AC " + defenderAC );

        if ( attackRoll > 95 || ( attackRoll > 5 && totalAttack >= defenderAC ) )
        {
            hit = true;

            int threatRange = weapon.getTemplate( ).getCriticalThreat( ) -
                    attacker.stats.get( weapon.getTemplate( ).getBaseWeapon( ).getName( ), Bonus.Type.BaseWeaponCriticalChance ) -
                    weapon.bonuses.get( Bonus.Type.WeaponCriticalChance ) - attacker.stats.get( Bonus.Type.CriticalChance );

            if ( attackRoll >= threatRange && ! criticalHitImmunity )
            {
                threatRoll = Game.dice.d100( );

                message.append( ". Critical threat: " + threatToString( ) );

                int threatCheck = threatRoll + attackBonus;

                boolean isCriticalHit = threatCheck > 95 || threatCheck >= defenderAC;

                // no critical hits on PCs if that has been disabled by the difficulty manager
                if ( defender.isPlayerFaction( ) && ! Game.ruleset.getDifficultyManager( ).criticalHitsOnPCs( ) )
                { isCriticalHit = false; }

                if ( isCriticalHit )
                {
                    message.append( ". Critical Hit" );

                    int multiplier = weapon.getTemplate( ).getCriticalMultiplier( ) +
                            attacker.stats.get( weapon.getTemplate( ).getBaseWeapon( ).getName( ), Bonus.Type.BaseWeaponCriticalMultiplier ) +
                            weapon.bonuses.get( Bonus.Type.WeaponCriticalMultiplier ) + attacker.stats.get( Bonus.Type.CriticalMultiplier );

                    damage.add( weapon.getTemplate( ).getDamageType( ), totalDamage * ( multiplier - 1 ) );
                    damageRoll *= multiplier;
                    totalDamage *= multiplier;

                    // shake the screen on a critical hit
                    Game.areaViewer.addScreenShake( );

                }
                else
                {
                    message.append( ". Normal Hit" );
                }
            }
            else
            {
                message.append( ". Hit" );
            }

            // critical hit immunity also grants immunity to extraAttack / extraDamage
            if ( ! criticalHitImmunity )
            {
                // extra damage doesn't get multiplied by critical hits
                damage.add( weapon.getTemplate( ).getDamageType( ), extraDamage );
                totalDamage += extraDamage;
            }

            if ( criticalHitImmunity && ( attackRoll >= threatRange || extraDamage > 0 || extraAttack > 0 ) )
            {
                message.append( ". " ).append( defender.getTemplate( ).getName( ) ).append( " is immune to critical hits" );
            }

            message.append( ". " );

        }
        else
        {
            hit = false;
            message.append( ". Miss." );
        }

        return hit;
    }

    public int computeAppliedDamage( )
    {
        this.appliedDamage = damage.computeAppliedDamage( );

        return appliedDamage;
    }

    public String threatToString( )
    {
        return ( threatRoll + " + " + attackBonus + " = " + ( threatRoll + attackBonus ) );
    }

    public String attackToString( )
    {
        return ( attackRoll + " + " + attackBonus + " = " + totalAttack );
    }

    public String damageToString( )
    {
        String damageString = ( damageRoll + " * " + Game.numberFormat( 3 ).format( damageBonus ) );
        if ( extraDamage != 0 ) damageString = damageString + " + " + extraDamage;
        return damageString + " = " + totalDamage;
    }

    @Override
    public String toString( )
    {
        return ( "Rolled " + attackToString( ) + " for " + damageToString( ) + " Damage" );
    }
}
