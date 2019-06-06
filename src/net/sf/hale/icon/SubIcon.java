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

package net.sf.hale.icon;

import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;

public class SubIcon implements Comparable< SubIcon >
{
    public enum Type
    {
        Quiver,
        Cloak,
        BaseBackground,
        Boots,
        Torso,
        BaseForeground,
        Gloves,
        Hair,
        Ears,
        Head,
        Beard,
        Shield,
        MainHandWeapon,
        OffHandWeapon
    }

    ;

    private Color color;
    private Color secondaryColor;

    private boolean coversHair;
    private boolean coversBeard;
    private String icon;
    private String secondaryIcon;
    private Type type;
    private Point offset;

    private SubIcon( ) { }

    public SubIcon multiplyByColor( Color color )
    {
        SubIcon subIcon = new SubIcon( );

        subIcon.color = this.color.multiply( color );
        subIcon.secondaryColor = this.secondaryColor.multiply( color );
        subIcon.coversHair = this.coversHair;
        subIcon.coversBeard = this.coversBeard;
        subIcon.icon = this.icon;
        subIcon.secondaryIcon = this.secondaryIcon;
        subIcon.type = this.type;
        subIcon.offset = new Point( this.offset );

        return subIcon;
    }

    private void initialize( Race race, Ruleset.Gender gender )
    {
        // look for a secondary icon if one has not been set
        if ( secondaryIcon == null )
        {
            String secondaryBase = icon + "Secondary";
            String secondaryRaceGender = icon + race.getSubIconRaceString( ) + gender + "Secondary";
            String secondaryRace = icon + race.getSubIconRaceString( ) + "Secondary";

            if ( SpriteManager.hasSprite( secondaryRaceGender ) )
            { secondaryIcon = secondaryRaceGender; }
            else if ( SpriteManager.hasSprite( secondaryRace ) )
            { secondaryIcon = secondaryRace; }
            else if ( SpriteManager.hasSprite( secondaryBase ) )
            { secondaryIcon = secondaryBase; }
        }

        if ( type == Type.OffHandWeapon )
        {
            // look for an off hand specified icon
            String iconOffhand = icon + Type.OffHandWeapon.toString( );
            if ( SpriteManager.hasSprite( iconOffhand ) )
            {
                this.icon = iconOffhand;
            }

        }
        else
        {
            // look for a racial / gender specific icon
            String iconRaceGender = icon + race.getSubIconRaceString( ) + gender;
            String iconRace = icon + race.getSubIconRaceString( );

            if ( SpriteManager.hasSprite( iconRaceGender ) )
            { this.icon = iconRaceGender; }
            else if ( SpriteManager.hasSprite( iconRace ) )
            { this.icon = iconRace; }
        }

        if ( color == null )
        { this.color = Color.WHITE; }

        if ( secondaryColor == null )
        { secondaryColor = Color.WHITE; }

        this.offset = race.getIconOffset( type );
    }

    public boolean coversHair( )
    {
        return coversHair;
    }

    public boolean coversBeard( )
    {
        return coversBeard;
    }

    public int getWidth( )
    {
        return SpriteManager.getSprite( this.icon ).getWidth( );
    }

    public int getHeight( )
    {
        return SpriteManager.getSprite( this.icon ).getHeight( );
    }

    public final void draw( int x, int y )
    {
        GL11.glColor4ub( color.getR( ), color.getG( ), color.getB( ), color.getA( ) );

        SpriteManager.getSprite( this.icon ).draw( x + offset.x, y + offset.y );

        Sprite secondarySprite = SpriteManager.getSprite( this.secondaryIcon );
        if ( secondarySprite != null )
        {
            GL11.glColor4ub( secondaryColor.getR( ), secondaryColor.getG( ),
                             secondaryColor.getB( ), secondaryColor.getA( ) );

            secondarySprite.draw( x + offset.x, y + offset.y );
        }
    }

    public Color getSecondaryColor( ) { return secondaryColor; }

    public String getSecondaryIcon( ) { return secondaryIcon; }

    public Color getColor( ) { return color; }

    public Point getOffset( ) { return offset; }

    public String getIcon( ) { return icon; }

    public Type getType( ) { return type; }

    @Override
    public int compareTo( SubIcon other )
    {
        return this.type.compareTo( other.type );
    }

    /**
     * A factory class for easily creating subIcons with many different parameter types
     *
     * @author Jared
     */

    public static class Factory
    {
        private String icon;
        private Color color;
        private Type type;
        private Race race;
        private Ruleset.Gender gender;
        private boolean coversBeard;
        private boolean coversHair;

        private String secondaryIcon;
        private Color secondaryColor;

        public Factory( Type type, Race race, Ruleset.Gender gender )
        {
            this.type = type;
            this.race = race;
            this.gender = gender;
        }

        public void setCoversHair( boolean coversHair )
        {
            this.coversHair = coversHair;
        }

        public void setCoversBeard( boolean coversBeard )
        {
            this.coversBeard = coversBeard;
        }

        public void setPrimaryIcon( String icon, Color color )
        {
            this.icon = icon;
            this.color = color;
        }

        public void setSecondaryIcon( String icon, Color color )
        {
            this.secondaryIcon = icon;
            this.secondaryColor = color;
        }

        /**
         * Creates a subIcon using the fields from this factory
         *
         * @return the newly created subicon
         */

        public SubIcon createSubIcon( )
        {
            SubIcon subIcon = new SubIcon( );

            subIcon.icon = icon;
            subIcon.color = color;
            subIcon.type = type;

            subIcon.coversBeard = coversBeard;
            subIcon.coversHair = coversHair;

            subIcon.secondaryIcon = secondaryIcon;
            subIcon.secondaryColor = secondaryColor;

            subIcon.initialize( race, gender );

            return subIcon;
        }
    }
}
