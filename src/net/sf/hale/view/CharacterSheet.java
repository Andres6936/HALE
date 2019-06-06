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

import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Ammo;
import net.sf.hale.entity.Armor;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.EquippableItem;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.Weapon;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.Role;
import net.sf.hale.rules.XP;
import net.sf.hale.widgets.BasePortraitViewer;
import net.sf.hale.widgets.StatFillBar;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget for displaying an overview of a character along with their portrait.
 * The overview includes stats like name, race, role, attributes, equipped items
 * in hands, most stats contained in {@link net.sf.hale.bonus.StatManager} and
 * any current Effects applied to the parent.
 *
 * @author Jared Stephen
 */

public class CharacterSheet extends ScrollPane
{
    private Creature creature;

    private final StatFillBar xpBar;
    private final TextArea textArea;
    private final HTMLTextAreaModel textAreaModel;

    private BasePortraitViewer viewer;

    private final Widget content;

    /**
     * Create a new CharacterSheet.  You must call updateContent
     * before this CharacterSheet will show anything.
     */

    public CharacterSheet( )
    {
        content = new Content( );

        textAreaModel = new HTMLTextAreaModel( );
        textArea = new TextArea( textAreaModel );

        this.setContent( content );
        this.setFixed( ScrollPane.Fixed.HORIZONTAL );

        content.add( textArea );

        xpBar = new StatFillBar( );
        xpBar.setTheme( "xpbar" );
        content.add( xpBar );
    }

    /**
     * Sets this CharacterSheet to show stats and the portrait of the specified Creature
     *
     * @param parent the Creature to show stats for
     */

    public void updateContent( PC parent )
    {
        StringBuilder sb = new StringBuilder( );

        if ( parent == null )
        {
            textAreaModel.setHtml( sb.toString( ) );
            creature = null;
            if ( viewer != null ) content.removeChild( viewer );
            invalidateLayout( );
            return;
        }

        if ( creature != parent )
        {
            this.creature = parent;

            if ( viewer != null ) content.removeChild( viewer );

            viewer = new BasePortraitViewer( creature );
            content.add( viewer );
        }

        int nextLevel = XP.getPointsForLevel( parent.stats.get( Stat.CreatureLevel ) + 1 );
        int curLevel = XP.getPointsForLevel( parent.stats.get( Stat.CreatureLevel ) );

        xpBar.setText( parent.getExperiencePoints( ) + " / " + nextLevel + " XP" );
        float frac = ( parent.getExperiencePoints( ) - curLevel ) / ( ( float ) ( nextLevel - curLevel ) );
        xpBar.setValue( frac );

        sb.append( "<table style=\"vertical-align: top\">" );
        sb.append( "<tr><td style=\"width: 32ex\">" );

        sb.append( "<div style=\"font-family: large;\">" ).append( parent.getTemplate( ).getName( ) ).append( "</div>" );

        sb.append( "<div style=\"margin-top: 1.2em\"></div>" );

        sb.append( "<div style=\"font-family: medium;\">" );
        sb.append( parent.getTemplate( ).getGender( ) ).append( ' ' );
        sb.append( "<span style=\"font-family: medium-blue;\">" ).append( parent.getTemplate( ).getRace( ).getName( ) ).append( "</span>" );
        sb.append( "</div>" );

        sb.append( "<div style=\"font-family: medium;\">" );
        for ( String roleID : parent.roles.getRoleIDs( ) )
        {
            Role role = Game.ruleset.getRole( roleID );
            int level = parent.roles.getLevel( role );

            sb.append( "<p>" );
            sb.append( "Level <span style=\"font-family: medium-italic;\">" ).append( level ).append( "</span> " );
            sb.append( "<span style=\"font-family: medium-red;\">" ).append( role.getName( ) ).append( "</span>" );
            sb.append( "</p>" );
        }

        sb.append( "</div>" );

        sb.append( "</td><td style=\"width: 35ex\">" );

        Weapon mainHand = parent.getMainHandWeapon( );
        EquippableItem offHand = parent.inventory.getEquippedItem( Inventory.Slot.OffHand );

        // show main hand stats
        {
            sb.append( "<div style=\"font-family: medium;\"><p style=\"font-family: medium-bold\">Main hand</p>" );
            sb.append( "<div style=\"font-family: medium-italic-blue\">" ).append( mainHand.getLongName( ) ).append( "</div>" );

            int quiverAttackBonus = 0;
            int quiverDamageBonus = 0;
            switch ( mainHand.getTemplate( ).getWeaponType( ) )
            {
                case Ranged:
                    Ammo quiver = ( Ammo ) parent.inventory.getEquippedItem( Inventory.Slot.Quiver );

                    if ( mainHand.getTemplate( ).isAmmoForThisWeapon( quiver ) )
                    {
                        quiverAttackBonus = quiver.getQualityAttackBonus( ) + quiver.bonuses.get( Bonus.Type.WeaponAttack );
                        quiverDamageBonus = quiver.getQualityAttackBonus( ) + quiver.bonuses.get( Bonus.Type.WeaponDamage );
                    }
                    break;
                default:
                    // do nothing
            }

            int attackBonus = parent.stats.get( Stat.MainHandAttackBonus ) + parent.stats.get( Stat.LevelAttackBonus ) +
                    mainHand.bonuses.get( Bonus.Type.WeaponAttack ) + mainHand.getQualityAttackBonus( ) +
                    quiverAttackBonus + parent.stats.get( mainHand.getTemplate( ).getDamageType( ).getName( ), Bonus.Type.AttackForWeaponType );

            sb.append( "<p>Attack Bonus <span style=\"font-family: medium-green;\">" ).append( attackBonus ).append( "</span></p>" );

            float damageMult = 1.0f + ( float ) ( parent.stats.get( Stat.LevelDamageBonus ) +
                    parent.stats.get( Stat.MainHandDamageBonus ) + mainHand.getQualityDamageBonus( ) +
                    parent.stats.get( mainHand.getTemplate( ).getDamageType( ).getName( ), Bonus.Type.DamageForWeaponType ) +
                    mainHand.bonuses.get( Bonus.Type.WeaponDamage ) + quiverDamageBonus ) / 100.0f;
            float damageMin = ( ( float ) mainHand.getTemplate( ).getMinDamage( ) * damageMult );
            float damageMax = ( ( float ) mainHand.getTemplate( ).getMaxDamage( ) * damageMult );

            int threatRange = mainHand.getTemplate( ).getCriticalThreat( ) -
                    parent.stats.get( mainHand.getTemplate( ).getBaseWeapon( ).getName( ), Bonus.Type.BaseWeaponCriticalChance ) -
                    mainHand.bonuses.get( Bonus.Type.WeaponCriticalChance ) - parent.stats.get( Bonus.Type.CriticalChance );
            int multiplier = mainHand.getTemplate( ).getCriticalMultiplier( ) +
                    parent.stats.get( mainHand.getTemplate( ).getBaseWeapon( ).getName( ), Bonus.Type.BaseWeaponCriticalMultiplier ) +
                    mainHand.bonuses.get( Bonus.Type.WeaponCriticalMultiplier ) + parent.stats.get( Bonus.Type.CriticalMultiplier );

            sb.append( "<p>Damage <span style=\"font-family: medium-red;\">" ).append( Game.numberFormat( 1 ).format( damageMin ) );
            sb.append( "</span> to <span style=\"font-family: medium-red;\">" );
            sb.append( Game.numberFormat( 1 ).format( damageMax ) ).append( "</span><span style=\"font-family: medium-green\"> " );
            sb.append( mainHand.getTemplate( ).getDamageType( ).getName( ) ).append( "</span></p><p>Critical " );
            sb.append( "<span style=\"font-family: medium-green;\">" ).append( Integer.toString( threatRange ) ).append( " - 100</span>" );
            sb.append( " / x" ).append( "<span style=\"font-family: medium-blue;\">" ).append( Integer.toString( multiplier ) );
            sb.append( "</span></p></div>" );
        }

        sb.append( "</td></tr><tr style=\"margin-top: 1em;\"><td>" );

        sb.append( "<div style=\"font-family: medium;\"><table style=\"width: 22ex\">" );
        sb.append( "<tr><td style=\"width: 14 ex;\">Hit Points</td><td style=\"text-align: right\">" );
        sb.append( "<span style=\"font-family: medium-italic-green\">" );
        sb.append( parent.getCurrentHitPoints( ) ).append( "</span> / <span style=\"font-family: medium-italic-green\">" );
        sb.append( parent.stats.get( Stat.MaxHP ) ).append( "</span></td></tr>" );

        sb.append( "<tr><td style=\"width: 14 ex;\">Attack Cost</td><td style=\"text-align: right\">" );
        sb.append( "<span style=\"font-family: medium-italic-red\">" );

        float attackCost = parent.stats.get( Stat.AttackCost ) / 100.0f;

        sb.append( Game.numberFormat( 0 ).format( attackCost ) ).append( "</span> AP</td></tr>" );

        sb.append( "<tr><td style=\"width: 14 ex;\">Movement Cost</td><td style=\"text-align: right\">" );
        sb.append( "<span style=\"font-family: medium-italic-blue\">" );

        float movementCost = parent.stats.get( Stat.MovementCost ) / 100.0f;

        sb.append( Game.numberFormat( 0 ).format( movementCost ) ).append( "</span> AP</td></tr>" );

        sb.append( "<tr><td style=\"width: 14 ex;\">Defense</td><td style=\"text-align: right\">" );
        sb.append( "<span style=\"font-family: medium-italic-blue\">" );
        sb.append( parent.stats.get( Stat.ArmorClass ) ).append( "</span></td></tr>" );

        sb.append( "</table></div>" );

        sb.append( "</td><td>" );

        if ( offHand == null )
        {
            sb.append( "<p style=\"font-family: medium-bold\">Off hand</p>" );
            sb.append( "<div style=\"font-family: medium-italic-blue\">" ).append( "Empty" ).append( "</div>" );
        }
        else
        {
            sb.append( "<div style=\"font-family: medium;\"><p style=\"font-family: medium-bold\">Off hand</p>" );
            sb.append( "<div style=\"font-family: medium-italic-blue\">" ).append( offHand.getLongName( ) ).append( "</div>" );

            switch ( offHand.getTemplate( ).getType( ) )
            {
                case Shield:
                    Armor offHandArmor = ( Armor ) offHand;

                    String armorClass = Game.numberFormat( 1 ).format( offHandArmor.getQualityModifiedArmorClass( ) );

                    sb.append( "<p>Defense <span style=\"font-family: medium-green;\">" ).append( armorClass );
                    sb.append( "</span></p>" );
                    break;
                case Weapon:
                    Weapon offHandWeapon = ( Weapon ) offHand;

                    int attackBonus = parent.stats.get( Stat.OffHandAttackBonus ) + parent.stats.get( Stat.LevelAttackBonus ) +
                            offHand.bonuses.get( Bonus.Type.WeaponAttack ) + offHandWeapon.getQualityAttackBonus( ) +
                            parent.stats.get( offHandWeapon.getTemplate( ).getDamageType( ).getName( ), Bonus.Type.AttackForWeaponType );

                    sb.append( "<p>Attack Bonus <span style=\"font-family: medium-green;\">" ).append( attackBonus ).append( "</span></p>" );

                    float damageMult = 1.0f + ( float ) ( parent.stats.get( Stat.LevelDamageBonus ) +
                            parent.stats.get( Stat.OffHandDamageBonus ) + offHandWeapon.getQualityDamageBonus( ) +
                            parent.stats.get( offHandWeapon.getTemplate( ).getDamageType( ).getName( ), Bonus.Type.DamageForWeaponType ) +
                            offHand.bonuses.get( Bonus.Type.WeaponDamage ) ) / 100.0f;
                    float damageMin = ( ( float ) offHandWeapon.getTemplate( ).getMinDamage( ) * damageMult );
                    float damageMax = ( ( float ) offHandWeapon.getTemplate( ).getMaxDamage( ) * damageMult );

                    int threatRange = offHandWeapon.getTemplate( ).getCriticalThreat( ) -
                            parent.stats.get( offHandWeapon.getTemplate( ).getBaseWeapon( ).getName( ), Bonus.Type.BaseWeaponCriticalChance ) -
                            offHandWeapon.bonuses.get( Bonus.Type.WeaponCriticalChance ) - parent.stats.get( Bonus.Type.CriticalChance );
                    int multiplier = offHandWeapon.getTemplate( ).getCriticalMultiplier( ) +
                            parent.stats.get( offHandWeapon.getTemplate( ).getBaseWeapon( ).getName( ), Bonus.Type.BaseWeaponCriticalMultiplier ) +
                            offHandWeapon.bonuses.get( Bonus.Type.WeaponCriticalMultiplier ) + parent.stats.get( Bonus.Type.CriticalMultiplier );

                    sb.append( "<p>Damage <span style=\"font-family: medium-red;\">" ).append( Game.numberFormat( 1 ).format( damageMin ) );
                    sb.append( "</span> to <span style=\"font-family: medium-red;\">" );
                    sb.append( Game.numberFormat( 1 ).format( damageMax ) ).append( "</span><span style=\"font-family: medium-green\"> " );
                    sb.append( offHandWeapon.getTemplate( ).getDamageType( ).getName( ) ).append( "</span></p><p>Critical " );
                    sb.append( "<span style=\"font-family: medium-green;\">" ).append( Integer.toString( threatRange ) ).append( " - 100</span>" );
                    sb.append( " / x" ).append( "<span style=\"font-family: medium-blue;\">" ).append( Integer.toString( multiplier ) );
                    sb.append( "</span></p>" );
                    break;
                default:
                    // do nothing
            }

            sb.append( "</div>" );
        }

        sb.append( "</td></tr><tr style=\"margin-top: 1em;\"><td>" );

        sb.append( "<div style=\"font-family: medium-bold\">Primary Stats</div>" );
        sb.append( "<div style=\"font-family: medium\"><table style=\"width: 24ex;\">" ); {
        sb.append( "<tr><td style=\"width: 10ex;\">" ).append( "Strength</td><td style=\"width: 3ex; font-family: medium-blue;\">" );
        sb.append( parent.stats.getBaseStr( ) ).append( "</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">" );
        sb.append( parent.stats.get( Bonus.Type.Str ) ).append( "</td><td style=\"width: 2ex;\">=</td>" );
        sb.append( "<td style=\"width: 3ex; font-family: medium-bold\">" ).append( parent.stats.getStr( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 12ex;\">" ).append( "Dexterity</td><td style=\"width: 3ex; font-family: medium-blue;\">" );
        sb.append( parent.stats.getBaseDex( ) ).append( "</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">" );
        sb.append( parent.stats.get( Bonus.Type.Dex ) ).append( "</td><td style=\"width: 2ex;\">=</td>" );
        sb.append( "<td style=\"width: 3ex; font-family: medium-bold\">" ).append( parent.stats.getDex( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 12ex;\">" ).append( "Constitution</td><td style=\"width: 3ex; font-family: medium-blue;\">" );
        sb.append( parent.stats.getBaseCon( ) ).append( "</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">" );
        sb.append( parent.stats.get( Bonus.Type.Con ) ).append( "</td><td style=\"width: 2ex;\">=</td>" );
        sb.append( "<td style=\"width: 3ex; font-family: medium-bold\">" ).append( parent.stats.getCon( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 12ex;\">" ).append( "Intelligence</td><td style=\"width: 3ex; font-family: medium-blue;\">" );
        sb.append( parent.stats.getBaseInt( ) ).append( "</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">" );
        sb.append( parent.stats.get( Bonus.Type.Int ) ).append( "</td><td style=\"width: 2ex;\">=</td>" );
        sb.append( "<td style=\"width: 3ex; font-family: medium-bold\">" ).append( parent.stats.getInt( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 12ex;\">" ).append( "Wisdom</td><td style=\"width: 3ex; font-family: medium-blue;\">" );
        sb.append( parent.stats.getBaseWis( ) ).append( "</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">" );
        sb.append( parent.stats.get( Bonus.Type.Wis ) ).append( "</td><td style=\"width: 2ex;\">=</td>" );
        sb.append( "<td style=\"width: 3ex; font-family: medium-bold\">" ).append( parent.stats.getWis( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 12ex;\">" ).append( "Charisma</td><td style=\"width: 3ex; font-family: medium-blue;\">" );
        sb.append( parent.stats.getBaseCha( ) ).append( "</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">" );
        sb.append( parent.stats.get( Bonus.Type.Cha ) ).append( "</td><td style=\"width: 2ex;\">=</td>" );
        sb.append( "<td style=\"width: 3ex; font-family: medium-bold\">" ).append( parent.stats.getCha( ) ).append( "</td></tr>" );

    }
        sb.append( "</table></div>" );

        sb.append( "</td><td style=\"vertical-align: top\">" );

        if ( parent.roles.getBaseRole( ).getMaximumSpellLevel( ) > 0 )
        {
            sb.append( "<div style=\"font-family: medium-bold;\">Failure Chance by Spell Level</div>" );
            sb.append( "<table style=\"width: 25ex;\">" );
            sb.append( "<tr style=\"font-family: medium;\"><td style=\"width: 7 ex; font-family: medium-bold\">Level</td>" );
            sb.append( "<td colspan = \"5\" style=\"text-align: center; font-family: medium-bold\">Percent Failure</td></tr>" );

            for ( int spellLevel = 1; spellLevel <= parent.roles.getBaseRole( ).getMaximumSpellLevel( ); spellLevel++ )
            {
                int baseChance = parent.stats.getBaseSpellFailure( spellLevel ) + parent.roles.getSomaticSpellFailure( );
                int tempChance = parent.roles.getVerbalSpellFailure( );

                sb.append( "<tr><td style=\"text-indent: 2ex; font-family: blue\">" );
                sb.append( spellLevel );
                sb.append( "</td><td style=\"text-align: right\">" );
                sb.append( baseChance );
                sb.append( "</td><td style=\"text-align: center\">+</td><td style=\"text-align: right\">" );
                sb.append( tempChance );
                sb.append( "</td><td style=\"text-align: center\">=</td><td style=\"font-family: red; text-align: right\">" );
                sb.append( baseChance + tempChance );
                sb.append( "</td></tr>" );
            }

            sb.append( "</table>" );
        }

        sb.append( "</td></tr><tr style=\"margin-top: 1em;\"><td>" );

        sb.append( "<div style=\"font-family: medium-bold\">Secondary Stats</div>" );
        sb.append( "<div style=\"font-family: black;\"><table style=\"width: 30ex; \">" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: blue\">Level Attack Bonus</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Stat.LevelAttackBonus ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: blue\">Level Damage Bonus</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Stat.LevelDamageBonus ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: green\">Mental Resistance</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.getMentalResistance( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: green\">Physical Resistance</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.getPhysicalResistance( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: green\">Reflex Resistance</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.getReflexResistance( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: blue\">Attacks of Opportunity</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.getAttacksOfOpportunity( ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: red\">Touch Defense</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Stat.TouchArmorClass ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: red\">Armor Penalty</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Stat.ArmorPenalty ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: red\">Shield Attack Penalty</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Stat.ShieldAttackPenalty ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: red\">Touch Attack Bonus</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Stat.TouchAttackBonus ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: blue\">Initiative Bonus</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Stat.InitiativeBonus ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 25ex; font-family: blue\">Concealment</td><td style=\"font-family: black\">" );
        sb.append( parent.stats.get( Bonus.Type.Concealment ) ).append( "</td></tr>" );

        sb.append( "<tr><td style=\"width: 10ex; font-family: blue\">" ).append( "Spell Resistance</td><td style=\"font-family: black\">" );
        int spellResistance = Math.min( 100, Math.max( 0, parent.stats.get( Bonus.Type.SpellResistance ) ) );
        sb.append( spellResistance );
        sb.append( "</td></tr>" );

        sb.append( "</table></div>" );

        sb.append( "</td><td>" );

        sb.append( "<div style=\"font-family: medium-bold\">Resistances</div>" );
        sb.append( "<table>" );
        int numResistances = 0;
        for ( DamageType damageType : Game.ruleset.getAllDamageTypes( ) )
        {
            if ( damageType.getName( ).equals( Game.ruleset.getString( "PhysicalDamageType" ) ) ) continue;

            int reduction = parent.stats.getDamageReduction( damageType );
            int immunity = parent.stats.getDamageImmunity( damageType );

            if ( reduction != 0 )
            {
                sb.append( "<tr><td style=\"width: 13ex;\"><span style=\"font-family: blue;\">" );
                sb.append( damageType.getName( ) ).append( "</span>: " );
                sb.append( "</td><td><span style=\"font-family: red;\">" ).append( reduction ).append( "</span>" );
                sb.append( " Damage Reduction</td></tr>" );

                numResistances++;
            }

            if ( immunity != 0 )
            {
                sb.append( "<tr><td style=\"width: 13ex;\"><span style=\"font-family: blue;\">" );
                sb.append( damageType.getName( ) ).append( "</span>: " );
                sb.append( "</td><td><span style=\"font-family: red;\">" ).append( immunity ).append( "</span>" );
                if ( immunity > 0 ) { sb.append( "% Damage Immunity</td></tr>" ); }
                else { sb.append( "% Damage Vulnerability</td></tr>" ); }

                numResistances++;
            }
        }
        sb.append( "</table>" );
        if ( numResistances == 0 )
        {
            sb.append( "<div style=\"font-family: medium-italic\">None</div>" );
        }

        sb.append( "</td></tr></table>" );

        synchronized ( parent.getEffects( ) )
        {
            for ( Effect effect : parent.getEffects( ) )
            {
                effect.appendDescription( sb );
            }
        }

        textAreaModel.setHtml( sb.toString( ) );
        invalidateLayout( );
    }

    private class Content extends Widget
    {
        private int xpBarX, xpBarY, portraitViewerOffset;

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            xpBarX = themeInfo.getParameter( "xpBarX", 0 );
            xpBarY = themeInfo.getParameter( "xpBarY", 0 );
            portraitViewerOffset = themeInfo.getParameter( "portraitViewerOffset", 0 );
        }

        @Override
        public int getPreferredWidth( )
        {
            return textArea.getPreferredWidth( );
        }

        @Override
        public int getPreferredHeight( )
        {
            return textArea.getPreferredHeight( );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            this.layoutChildFullInnerArea( textArea );

            if ( viewer != null )
            {
                viewer.setSize( viewer.getPreferredWidth( ), viewer.getPreferredHeight( ) );
                viewer.setPosition( textArea.getInnerX( ) + portraitViewerOffset, textArea.getInnerY( ) );
            }

            xpBar.setSize( xpBar.getPreferredWidth( ), xpBar.getPreferredHeight( ) );
            xpBar.setPosition( textArea.getInnerX( ) + xpBarX, textArea.getInnerY( ) + xpBarY );
        }
    }
}
