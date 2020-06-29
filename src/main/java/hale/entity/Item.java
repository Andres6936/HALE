/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package hale.entity;

import hale.Game;
import hale.ability.ScriptFunctionType;
import hale.area.Area;
import hale.loading.JSONOrderedObject;
import hale.loading.LoadGameException;
import hale.loading.ReferenceHandler;
import hale.rules.Quality;
import hale.util.Logger;
import hale.util.SimpleJSONObject;
import hale.view.ItemDetailsWindow;

/**
 * A specific, modifiable instance of an item template
 *
 * @author Jared
 */

public class Item extends Entity
{
    private final ItemTemplate template;

    private String quality;

    private int qualityValue;

    @Override
    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        super.load(data, area, refHandler);

        if (data.containsKey("quality")) {
            setQuality(data.get("quality", null));
        }
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject out = super.save();

        if (quality != null) {
            out.put("quality", quality);
        }

        return out;
    }

    /**
     * Creates a new Item.  The {@link #setQuality(String)} method must
     * be called immediately after this constructor, before any other methods
     *
     * @param template the ItemTemplate
     */

    protected Item(ItemTemplate template)
    {
        super(template);

        this.template = template;
    }

    @Override
    public ItemTemplate getTemplate()
    {
        return template;
    }

    /**
     * Sets the quality of this item to the quality with the given ID
     *
     * @param quality the ID of the quality to set
     */

    protected void setQuality(String quality)
    {
        if (template.hasQuality()) {
            Quality qual = Game.ruleset.getItemQuality(quality);
            this.quality = quality;

            this.qualityValue = template.getValueInCPOver100() * qual.getValueAdjustment() / 100;
        } else {
            if (quality != null) {
                Logger.appendToWarningLog("Quality cannot be set for " + getTemplate().getID());
            }

            this.quality = null;
            this.qualityValue = template.getValueInCPOver100();
        }
    }

    /**
     * Returns the specified item quality of this item
     *
     * @return the item quality of this item, or null if the item has no quality
     */

    public Quality getQuality()
    {
        return Game.ruleset.getItemQuality(quality);
    }

    /**
     * Returns the value of this item in a unit such that 1 Copper Piece
     * equals 100.  The value is modified by the quality of the Item
     *
     * @return the value of this item modified by Quality
     */

    public int getQualityValue()
    {
        return qualityValue;
    }

    /**
     * Returns the long name of this item, which consists of the item name
     * (See {@link ItemTemplate#getName()}, as well as modifiers such as
     * quality
     *
     * @return the long name of this item
     */

    public String getLongName()
    {
        if (getTemplate().hasQuality()) {
            return quality + " " + getTemplate().getName();
        } else {
            return getTemplate().getName();
        }
    }

    /**
     * Returns true if and only if the specified parent Creature can use this
     * item with its current state, including AP.  If this Item has a canUse()
     * script function, that function must return true in order for this method
     * to return true.
     *
     * @param parent the Creature to use this item
     * @return true if and only if this item can be used by the specified parent
     */

    public boolean canUse(Creature parent)
    {
        if (!template.isUsable()) return false;

        if (!parent.timer.canPerformAction(template.getUseAP())) return false;

        if (template.getScript().hasFunction(ScriptFunctionType.canUse)) {
            Object returnValue = template.getScript().executeFunction(ScriptFunctionType.canUse, this, parent);
            return Boolean.TRUE.equals(returnValue);
        } else {
            return true;
        }
    }

    /**
     * The specified parent Creature uses this Item.  This item's onUse
     * script Function is called.
     *
     * @param parent the Creature using this Item.
     * @return true if the onUse script function was called, false if it was not because of lack of
     * AP or any other reason.  Note that even if the script function was called, the item may still
     * have failed to been used
     */

    public boolean use(Creature parent)
    {
        if (!template.isUsable()) return false;

        if (!parent.timer.performAction(template.getUseAP())) return false;

        template.getScript().executeFunction(ScriptFunctionType.onUse, this, parent);

        return true;
    }

    /**
     * Returns true if the other object is an Item with matching ID and quality to this
     * Item, false otherwise
     */

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Item)) {
            return false;
        }

        Item otherItem = (Item)other;

        if (!template.getID().equals(otherItem.template.getID())) {
            return false;
        }

        if (quality != null) {
            return quality.equals(otherItem.quality);
        } else {
            return true;
        }
    }

    /**
     * Gets a runnable that will display an item details window at the
     * specified window coordinates when run
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the runnable callback
     */

    public Runnable getExamineDetailsCallback(int x, int y)
    {
        return new ExamineDetailsCallback(x, y);
    }

    /**
     * Returns a callback that will use this item when run.  See {@link #use(Creature)}
     *
     * @param parent the creature to use this item
     * @return a callback that uses this item when run
     */

    public Runnable getUseCallback(Creature parent)
    {
        return new UseCallback(parent);
    }

    private class UseCallback implements Runnable
    {
        private Creature parent;

        private UseCallback(Creature parent)
        {
            this.parent = parent;
        }

        @Override
        public void run()
        {
            use(parent);

            Game.mainViewer.getMenu().hide();
        }
    }

    private class ExamineDetailsCallback implements Runnable
    {
        private final int x, y;

        private ExamineDetailsCallback(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public void run()
        {
            ItemDetailsWindow window = new ItemDetailsWindow(Item.this);
            window.setPosition(x - window.getWidth() / 2, y - window.getHeight() / 2);
            Game.mainViewer.add(window);
            Game.mainViewer.getMenu().hide();
        }
    }
}
