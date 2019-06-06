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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import de.matthiasmann.twl.Color;

/**
 * A Composed Creature Icon is an icon consisting of one or more sub-icons.  This icon
 * type is used to draw for Creatures.  It consists of potentially many {@link SubIcon}
 *
 * @author Jared
 */

public class ComposedCreatureIcon implements Icon, Iterable< ComposedCreatureIcon.Entry >
{
    private final Color skinColor;
    private final Color clothingColor;

    private final List< Entry > entries;

    /**
     * A single entry in this composed icon, describing one subIcon
     *
     * @author Jared
     */

    public class Entry
    {
        public final SubIcon.Type type;
        public final String spriteID;
        public final Color color;

        private Entry( SubIcon.Type type, String spriteID, Color color )
        {
            this.type = type;
            this.spriteID = spriteID;
            this.color = color;
        }
    }

    private ComposedCreatureIcon( Color skinColor, Color clothingColor )
    {
        entries = new ArrayList< Entry >( );

        if ( skinColor == null )
        { this.skinColor = Color.WHITE; }
        else
        { this.skinColor = skinColor; }

        if ( clothingColor == null )
        { this.clothingColor = Color.WHITE; }
        else
        { this.clothingColor = clothingColor; }
    }

    /**
     * Creates a new ComposedCreatureIcon with the specified data predefined.  This is used in creating
     * new characters in the editor.
     *
     * @param skinColor
     * @param clothingColor
     * @param icons
     */

    public ComposedCreatureIcon( Color skinColor, Color clothingColor, List< SubIcon > icons )
    {
        this( skinColor, clothingColor );

        for ( SubIcon icon : icons )
        {
            entries.add( new Entry( icon.getType( ), icon.getIcon( ), icon.getColor( ) ) );
        }
    }

    /**
     * Creates a new ComposedCreatureIcon
     *
     * @param data the JSON to parse
     */

    protected ComposedCreatureIcon( SimpleJSONObject data )
    {
        entries = new ArrayList< Entry >( );

        if ( data.containsKey( "skinColor" ) )
        {
            skinColor = Color.parserColor( data.get( "skinColor", null ) );
        }
        else
        {
            skinColor = Color.WHITE;
        }

        if ( data.containsKey( "clothingColor" ) )
        {
            clothingColor = Color.parserColor( data.get( "clothingColor", null ) );
        }
        else
        {
            clothingColor = Color.WHITE;
        }

        for ( SimpleJSONArrayEntry entry : data.getArray( "subIcons" ) )
        {
            SimpleJSONObject iconObject = entry.getObject( );

            SubIcon.Type type = SubIcon.Type.valueOf( iconObject.get( "type", null ) );
            String spriteID = iconObject.get( "sprite", null );
            Color color = Color.parserColor( iconObject.get( "color", null ) );

            entries.add( new Entry( type, spriteID, color ) );
        }
    }

    /**
     * Creates a new ComposedCreatureIcon which is a copy of the
     * specified Icon
     *
     * @param other
     */

    protected ComposedCreatureIcon( ComposedCreatureIcon other )
    {
        entries = new ArrayList< Entry >( other.entries );

        skinColor = other.skinColor;
        clothingColor = other.clothingColor;
    }

    @Override
    public ComposedCreatureIcon multiplyByColor( Color color )
    {
        ComposedCreatureIcon icon = new ComposedCreatureIcon( this.skinColor.multiply( color ),
                                                              this.clothingColor.multiply( color ) );

        for ( Entry entry : this.entries )
        {
            icon.entries.add( new Entry( entry.type, entry.spriteID, entry.color.multiply( color ) ) );
        }

        return icon;
    }

    /**
     * Returns the skin color of this icon
     *
     * @return the skin color
     */

    public Color getSkinColor( )
    {
        return skinColor;
    }

    /**
     * Returns the clothing color of this icon
     *
     * @return the clothing color
     */

    public Color getClothingColor( )
    {
        return clothingColor;
    }

    /**
     * Returns true if this composed icon contains a SubIcon with a type of SubIcon.Type.BaseBackground,
     * false otherwise.  If this is true, then this composed icon is overriding the default racial icons,
     * and the base racial icons will not be added to creatures using this as their icon
     *
     * @return whether this icon contains a baseBackground SubIcon
     */

    public boolean containsBaseBackgroundSubIcon( )
    {
        for ( Entry entry : entries )
        {
            if ( entry.type == SubIcon.Type.BaseBackground )
            { return true; }
        }

        return false;
    }

    /**
     * Drawing is not implemented for ComposedCreatureIcons.  Instead, a SubIconList
     * must be created from this Icon.  That SubIconList can then be drawn
     */

    @Override
    public void draw( int x, int y )
    {
        // do nothing
    }

    /**
     * Drawing is not implemented for ComposedCreatureIcons.  Instead, a SubIconList
     * must be created from this Icon.  That SubIconList can then be drawn
     */

    @Override
    public void drawCentered( int x, int y, int width, int height )
    {
        // do nothing
    }

    @Override
    public int getWidth( )
    {
        return Game.TILE_SIZE;
    }

    @Override
    public int getHeight( )
    {
        return Game.TILE_SIZE;
    }

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject out = new JSONOrderedObject( );

        out.put( "skinColor", "#" + Integer.toHexString( skinColor.toARGB( ) ) );
        out.put( "clothingColor", "#" + Integer.toHexString( clothingColor.toARGB( ) ) );

        List< Object > subIconsOut = new ArrayList< Object >( );

        for ( Entry entry : entries )
        {
            JSONOrderedObject entryOut = new JSONOrderedObject( );
            entryOut.put( "type", entry.type.name( ) );
            entryOut.put( "sprite", entry.spriteID );
            entryOut.put( "color", "#" + Integer.toHexString( entry.color.toARGB( ) ) );

            subIconsOut.add( entryOut );
        }

        out.put( "subIcons", subIconsOut.toArray( ) );

        return out;
    }

    @Override
    public Iterator< Entry > iterator( )
    {
        return new IconIterator( );
    }

    private class IconIterator implements Iterator< Entry >
    {
        private int index = 0;

        @Override
        public boolean hasNext( )
        {
            return index < entries.size( );
        }

        @Override
        public Entry next( )
        {
            index++;

            return entries.get( index - 1 );
        }

        @Override
        public void remove( )
        {
            throw new UnsupportedOperationException( "ComposedCreatureIcons are immutable and elements may not be removed" );
        }
    }
}
