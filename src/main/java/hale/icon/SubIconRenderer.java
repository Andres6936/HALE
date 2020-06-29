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

package main.java.hale.icon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.rules.Race;
import main.java.hale.rules.Ruleset;
import main.java.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;

/**
 * A SubIconRenderer is based on a creature ComposedCreatureIcon, and is modifiable; it
 * changes as the creature it is based on changes.
 *
 * @author Jared
 */

public class SubIconRenderer implements IconRenderer
{
    private final List<SubIcon> subIcons;

    private Color skinColor;
    private Color clothingColor;

    private SubIcon beard;
    private SubIcon hair;
    private SubIcon ears;

    /**
     * Creates a new SubIconRenderer for a parent with the specified icon, race, and gender
     *
     * @param icon
     * @param race
     * @param gender
     */

    public SubIconRenderer(ComposedCreatureIcon icon, Race race, Ruleset.Gender gender)
    {
        subIcons = new ArrayList<SubIcon>();

        this.skinColor = icon.getSkinColor();
        this.clothingColor = icon.getClothingColor();

        if (!icon.containsBaseBackgroundSubIcon()) {
            addBaseRacialSubIcons(race, gender);
        }

        for (ComposedCreatureIcon.Entry entry : icon) {
            SubIcon.Factory factory = new SubIcon.Factory(entry.type, race, gender);
            factory.setPrimaryIcon(entry.spriteID, entry.color);

            add(factory.createSubIcon());
        }

        Collections.sort(subIcons);
    }

    /**
     * Creates a new SubIconList which is a copy of the
     * specified Icon
     *
     * @param other
     */

    protected SubIconRenderer(SubIconRenderer other)
    {
        subIcons = new ArrayList<SubIcon>(other.subIcons);

        beard = other.beard;
        hair = other.hair;
        ears = other.ears;

        skinColor = other.skinColor;
        clothingColor = other.clothingColor;
    }

    public void setSkinColor(Color color)
    {
        this.skinColor = color;
    }

    public void setClothingColor(Color color)
    {
        this.clothingColor = color;
    }

    public Color getSkinColor()
    {
        return skinColor;
    }

    public Color getClothingColor()
    {
        return clothingColor;
    }

    /**
     * Adds the base background and foreground icons for the specified
     * race and gender to this Icon
     *
     * @param race
     * @param gender
     */

    private void addBaseRacialSubIcons(Race race, Ruleset.Gender gender)
    {
        // remove any old base icons
        remove(SubIcon.Type.BaseBackground);
        remove(SubIcon.Type.BaseForeground);
        remove(SubIcon.Type.Ears);

        switch (gender) {
            case Male:
                if (race.getMaleBackgroundIcon() != null) {
                    SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseBackground, race, gender);
                    factory.setPrimaryIcon(race.getMaleBackgroundIcon(), getSkinColor());
                    factory.setSecondaryIcon(race.getMaleClothesIcon(), getClothingColor());
                    add(factory.createSubIcon());
                }

                if (race.getMaleForegroundIcon() != null) {
                    SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseForeground, race, gender);
                    factory.setPrimaryIcon(race.getMaleForegroundIcon(), getSkinColor());
                    add(factory.createSubIcon());
                }

                if (race.getMaleEarsIcon() != null) {
                    SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Ears, race, gender);
                    factory.setPrimaryIcon(race.getMaleEarsIcon(), getSkinColor());
                    add(factory.createSubIcon());
                }
                break;
            case Female:
                if (race.getFemaleBackgroundIcon() != null) {
                    SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseBackground, race, gender);
                    factory.setPrimaryIcon(race.getFemaleBackgroundIcon(), getSkinColor());
                    factory.setSecondaryIcon(race.getFemaleClothesIcon(), getClothingColor());
                    add(factory.createSubIcon());
                }

                if (race.getFemaleForegroundIcon() != null) {
                    SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseForeground, race, gender);
                    factory.setPrimaryIcon(race.getFemaleForegroundIcon(), getSkinColor());
                    add(factory.createSubIcon());
                }

                if (race.getFemaleEarsIcon() != null) {
                    SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Ears, race, gender);
                    factory.setPrimaryIcon(race.getFemaleEarsIcon(), getSkinColor());
                    add(factory.createSubIcon());
                }
                break;
        }
    }

    /**
     * Adds the specified SubIcon to the list of SubIcons displayed by this Icon
     *
     * @param subIcon
     */

    public synchronized void add(SubIcon subIcon)
    {
        if (subIcon == null) {
            return;
        }

        if (subIcon.getIcon() == null) {
            return;
        }

        remove(subIcon.getType());

        subIcons.add(subIcon);

        Collections.sort(subIcons);

        switch (subIcon.getType()) {
            case Ears:
                ears = subIcon;
                break;
            case Beard:
                beard = subIcon;
                break;
            case Hair:
                hair = subIcon;
                break;
            case Head:
                remove(SubIcon.Type.Ears);
                break;
            default:
                // do nothing special
        }

        if (subIcon.coversHair()) {
            remove(SubIcon.Type.Hair);
        }

        if (subIcon.coversBeard()) {
            remove(SubIcon.Type.Beard);
        }
    }

    /**
     * Removes all SubIcons from this Icon
     */

    public synchronized void clear()
    {
        subIcons.clear();
        hair = null;
        ears = null;
        beard = null;
    }

    /**
     * Removes all SubIcons of the specified type from this Icon
     *
     * @param type
     */

    public synchronized void remove(SubIcon.Type type)
    {
        Iterator<SubIcon> iter = subIcons.iterator();
        while (iter.hasNext()) {
            if (iter.next().getType() == type) {
                iter.remove();
            }
        }

        if (type == SubIcon.Type.Head && this.ears != null) {
            add(this.ears);
        }

        if (type != SubIcon.Type.Beard) {
            boolean addBeard = true;
            for (SubIcon subIcon : subIcons) {
                if (subIcon.coversBeard()) {
                    addBeard = false;
                    break;
                }

                if (subIcon.getType() == SubIcon.Type.Beard) {
                    addBeard = false;
                    break;
                }
            }

            if (addBeard && this.beard != null) {
                add(this.beard);
            }
        }

        if (type != SubIcon.Type.Hair) {
            boolean addHair = true;
            for (SubIcon subIcon : subIcons) {
                if (subIcon.coversHair()) {
                    addHair = false;
                    break;
                }

                if (subIcon.getType() == SubIcon.Type.Hair) {
                    addHair = false;
                    break;
                }
            }

            if (addHair && this.hair != null) {
                add(this.hair);
            }
        }
    }

    /**
     * Returns the beard icon, even if the beard icon is not currently being
     * drawn
     *
     * @return the beard icon
     */

    public String getBeardIcon()
    {
        if (beard == null) return null;

        return beard.getIcon();
    }

    /**
     * Returns the beard color, even if the beard icon is not currently being drawn
     *
     * @return the beard color
     */

    public Color getBeardColor()
    {
        if (beard == null) return null;

        return beard.getColor();
    }

    /**
     * Returns the hair icon, even if a helmet is equipped and the hair icon is not currently
     * being drawn
     *
     * @return the hair icon
     */

    public String getHairIcon()
    {
        if (hair == null) return null;

        return hair.getIcon();
    }

    /**
     * Returns the hair color, even if a helmet is equipped and the hair icon is not currently
     * being drawn
     *
     * @return the hair color
     */

    public Color getHairColor()
    {
        if (hair == null) return null;

        return hair.getColor();
    }

    public Point getOffset(String type)
    {
        return getOffset(SubIcon.Type.valueOf(type));
    }

    public Color getColor(String type)
    {
        return getColor(SubIcon.Type.valueOf(type));
    }

    public String getIcon(String type)
    {
        return getIcon(SubIcon.Type.valueOf(type));
    }

    public SubIcon getSubIcon(String type)
    {
        return getSubIcon(SubIcon.Type.valueOf(type));
    }

    public SubIcon getSubIcon(SubIcon.Type type)
    {
        for (SubIcon subIcon : subIcons) {
            if (subIcon.getType() == type) return subIcon;
        }

        return null;
    }

    public Point getOffset(SubIcon.Type type)
    {
        for (SubIcon subIcon : subIcons) {
            if (subIcon.getType() == type) return subIcon.getOffset();
        }

        return new Point(0, 0);
    }

    public Color getColor(SubIcon.Type type)
    {
        for (SubIcon subIcon : subIcons) {
            if (subIcon.getType() == type) return subIcon.getColor();
        }

        return null;
    }

    public String getIcon(SubIcon.Type type)
    {
        for (SubIcon subIcon : subIcons) {
            if (subIcon.getType() == type) return subIcon.getIcon();
        }

        return null;
    }


    @Override
    public synchronized void draw(int x, int y)
    {
        for (SubIcon subIcon : subIcons) {
            subIcon.draw(x, y);
        }

        GL11.glColor3f(1.0f, 1.0f, 1.0f);
    }

    @Override
    public void drawCentered(int x, int y, int width, int height)
    {
        int offsetX = x + (width - Game.TILE_SIZE) / 2;
        int offsetY = y + (height - Game.TILE_SIZE) / 2;

        draw(offsetX, offsetY);
    }
}
