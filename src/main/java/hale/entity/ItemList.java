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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import hale.Game;
import hale.loading.JSONOrderedObject;
import hale.loading.Saveable;
import hale.rules.Quality;
import hale.rules.Weight;
import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * A list of entries.  Each entry specified a unique item ID and quality
 * combination, as well as a quantity
 *
 * @author Jared
 */

public class ItemList implements Iterable<ItemList.Entry>, Saveable
{
    private List<Listener> listeners;
    private List<Entry> entries;

    @Override
    public Object save()
    {
        Object[] data = new Object[entries.size()];

        int i = 0;
        for (Entry entry : entries) {
            JSONOrderedObject entryData = new JSONOrderedObject();

            entryData.put("id", entry.id);

            if (entry.quality != null) {
                entryData.put("quality", entry.quality);
            }

            entryData.put("quantity", entry.quantity);

            data[i] = entryData;
            i++;
        }

        return data;
    }

    public void load(SimpleJSONArray data)
    {
        clear();

        for (SimpleJSONArrayEntry entry : data) {
            SimpleJSONObject obj = entry.getObject();

            String id = obj.get("id", null);

            Quality quality = null;
            if (obj.containsKey("quality")) {
                quality = Game.ruleset.getItemQuality(obj.get("quality", null));
            }

            int quantity = obj.get("quantity", 0);

            if (EntityManager.getItemTemplate(id) != null) {
                entries.add(new Entry(id, quality, quantity));
            }
        }
    }

    /**
     * The interface for a class that wants to be notified when this ItemList
     * is modified
     *
     * @author Jared
     */

    public interface Listener
    {

        /**
         * Called whenever an item is added to this list.  This includes
         * adding a new entry or increasing the quantity of an entry
         */

        public void itemListItemAdded(String id, Quality quality, int quantity);

        /**
         * Called whenever the specified entry is removed from this list
         *
         * @param entry  the entry that has been removed
         * @param return true if the listener should be removed from the list of listeners,
         *               false if the listener should remain in the list after this call
         */

        public boolean itemListEntryRemoved(Entry entry);
    }

    /**
     * A single entry in the list, specifying id, quality, and quantity
     *
     * @author Jared
     */

    public static class Entry implements Comparable<Entry>
    {
        private final String id;
        private final String quality;
        private int quantity;

        private Entry(String id, Quality quality, int quantity)
        {
            this.id = id;

            if (quality != null) {
                this.quality = quality.getName();
            } else {
                this.quality = null;
            }

            this.quantity = quantity;
        }

        private Entry(Entry other)
        {
            this.id = other.id;
            this.quality = other.quality;
            this.quantity = other.quantity;
        }

        /**
         * Returns the entity ID of the item specified by this entry
         *
         * @return the entity ID
         */

        public String getID()
        {
            return id;
        }

        /**
         * Returns the quality level of the item specified by this entry, or
         * null if the item has no quality
         *
         * @return the quality level of this item
         */

        public Quality getQuality()
        {
            return Game.ruleset.getItemQuality(quality);
        }

        /**
         * Returns the quantity of this entry in the itemlist
         *
         * @return the quantity
         */

        public int getQuantity()
        {
            return quantity;
        }

        /**
         * Returns true if the item specified by this entry has quality,
         * false otherwise
         *
         * @return whether the item specified by this entry has quality
         */

        public boolean hasQuality()
        {
            return quality != null;
        }

        /**
         * If this Entry's quantity is infinite, returns Integer.MAX_VALUE.
         * Otherwise, returns a quantity between 1 and the quantity of this
         * Entry, randomly selected
         *
         * @return a randomly generated quantity
         */

        public int getRandomQuantity()
        {
            if (quantity == 1) return 1;
            if (quantity == Integer.MAX_VALUE) return Integer.MAX_VALUE;

            return Game.dice.rand(1, quantity);
        }

        /**
         * Creates an item with id and quality matching the attributes of this
         * entry
         *
         * @return a newly created item
         */

        public Item createItem()
        {
            if (hasQuality()) {
                return EntityManager.getItem(id, quality);
            } else {
                return EntityManager.getItem(id);
            }
        }

        @Override
        public int compareTo(Entry other)
        {
            ItemTemplate thisTemplate = EntityManager.getItemTemplate(this.id);
            ItemTemplate otherTemplate = EntityManager.getItemTemplate(other.id);

            int nameComparison = thisTemplate.getName().compareTo(otherTemplate.getName());

            if (nameComparison != 0) {
                return nameComparison;
            }

            if (this.quality != null && other.quality != null) {
                return this.quality.compareTo(other.quality);
            } else
                if (this.quality == null && other.quality == null) {
                    return 0;
                } else
                    if (this.quality == null) {
                        return -1;
                    } else {
                        return 1;
                    }
        }
    }

    /**
     * Creates a new ItemList with no entries
     */

    public ItemList()
    {
        entries = new ArrayList<Entry>();
        listeners = new ArrayList<Listener>();
    }

    /**
     * Creates a new ItemList containing exactly the same entries
     * as the specified ItemList
     *
     * @param other the ItemList to copy
     */

    public ItemList(ItemList other)
    {
        this();

        for (Entry entry : other.entries) {
            this.entries.add(new Entry(entry));
        }
    }

    /**
     * Creates an itemlist by parsing the specified JSON data
     *
     * @param data the JSON to parse
     */

    public ItemList(SimpleJSONArray data)
    {
        this();

        load(data);
    }

    /**
     * Adds the specified listener to be notified of changes to this ItemList
     *
     * @param listener
     */

    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }

    /**
     * Removes the specified listener from recieving ItemList change notifications
     *
     * @param listener
     */

    public void removeListener(Listener listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Checks all entries in this item list.  Any with invalid item IDs will be removed
     */

    public void validate()
    {
        // remove invalid entries without triggering any notifications
        Iterator<Entry> iter = entries.iterator();

        while (iter.hasNext()) {
            Entry entry = iter.next();

            try {
                EntityManager.getItem(entry.id, entry.quality);
            } catch (Exception e) {
                iter.remove();
            }
        }
    }

    /**
     * Removes all entries from this itemList, notifying any listeners for each one
     */

    public void clear()
    {
        for (int i = entries.size() - 1; i >= 0; i--) {
            removeEntry(i);
        }
    }

    /**
     * Returns the total number of unique ID - quality combinations in this
     * ItemList.  This is the number of entries.
     *
     * @return the number of entries
     */

    public int size()
    {
        return entries.size();
    }

    /**
     * Adds all items with associated quantities from the specified itemlist to
     * this itemlist
     *
     * @param itemList
     */

    public void addAll(ItemList itemList)
    {
        for (ItemList.Entry entry : itemList) {
            add(entry.id, entry.quality, entry.quantity);
        }
    }

    /**
     * Adds the specified quantity of the specified item to this this
     * ItemList
     *
     * @param id       the entity ID of the item
     * @param quality  the quality of the item, or null for no quality
     * @param quantity the quantity of the item; this should be non-negative
     */

    public void add(String id, Quality quality, int quantity)
    {
        // figure out whether null quality means no or default
        if (EntityManager.getItemTemplate(id).hasQuality()) {
            if (quality == null) {
                quality = EntityManager.getItemTemplate(id).getDefaultQuality();
            }
        } else {
            quality = null;
        }

        int index = findEntry(id, quality);

        if (index == -1) {
            Entry entry = new Entry(id, quality, quantity);
            entries.add(entry);
        } else {
            entries.get(index).quantity += quantity;
        }

        for (Listener listener : listeners) {
            listener.itemListItemAdded(id, quality, quantity);
        }
    }

    /**
     * Adds the specified quantity of the item with the specified ID and quality
     *
     * @param id
     * @param quality  the ID of the quality
     * @param quantity
     */

    public void add(String id, String quality, int quantity)
    {
        add(id, Game.ruleset.getItemQuality(quality), quantity);
    }

    /**
     * Adds a quantity of one of the item with the specified ID and quality
     *
     * @param id
     * @param quality the ID of the quality
     */

    public void add(String id, String quality)
    {
        add(id, Game.ruleset.getItemQuality(quality), 1);
    }

    /**
     * Adds the specified quantity of the item with the specified ID.  The
     * quality is assumed to be no-quality, otherwise it will be set to the
     * default
     *
     * @param id
     * @param quantity
     */

    public void add(String id, int quantity)
    {
        add(id, (Quality)null, quantity);
    }

    /**
     * Adds a quantity of one of the item with the specified ID. The quality
     * is assumed to be no quality or the default.
     *
     * @param id
     */

    public void add(String id)
    {
        add(id, (Quality)null, 1);
    }

    /**
     * Adds the specified quantity of the specified item to this ItemList
     *
     * @param item
     * @param quantity the quantity to add; this should be non-negative
     */

    public void add(Item item, int quantity)
    {
        add(item.getTemplate().getID(), item.getQuality(), quantity);
    }

    /**
     * Adds a quantity of one of the specified item to this ItemList
     *
     * @param item
     */

    public void add(Item item)
    {
        add(item.getTemplate().getID(), item.getQuality(), 1);
    }

    /**
     * Removes up to the specified quantity of the specified item from this itemList
     *
     * @param id       the Entity ID of the item
     * @param quantity the quantity to remove; this should be non-negative
     * @return the quantity of the item that was actually removed from this ItemList.  This
     * may be less than or equal to the specified quantity
     */

    public int remove(String id, int quantity)
    {
        int qtyLeftToRemove = quantity;

        for (int i = 0; i < entries.size(); i++) {
            if (!entries.get(i).id.equals(id)) continue;

            if (entries.get(i).quantity > qtyLeftToRemove) {
                entries.get(i).quantity -= qtyLeftToRemove;
                // the entire quantity was removed
                qtyLeftToRemove = 0;
                break;
            } else
                if (entries.get(i).quantity == qtyLeftToRemove) {
                    removeEntry(i);
                    qtyLeftToRemove = 0;
                    break;
                } else {
                    // part of the quantity was removed, keep looking
                    qtyLeftToRemove -= entries.get(i).quantity;
                    removeEntry(i);
                }
        }

        return quantity - qtyLeftToRemove;
    }

    /**
     * Removes up to the specified quantity of the specified item from this itemList
     *
     * @param id       the Entity ID of the item
     * @param quality  the quality of the item, or null for no quality
     * @param quantity the quantity to remove; this should be non-negative
     * @return the quantity of the item that was actually removed from this ItemList.  This
     * may be less than or equal to the specified quantity
     */

    public int remove(String id, Quality quality, int quantity)
    {
        int index = findEntry(id, quality);

        if (index == -1) {
            return 0;
        } else {
            int curQuantity = entries.get(index).quantity;

            if (curQuantity > quantity) {
                entries.get(index).quantity = curQuantity - quantity;
                return quantity;
            } else
                if (curQuantity == quantity) {
                    removeEntry(index);
                    return quantity;
                } else {
                    removeEntry(index);
                    return curQuantity;
                }
        }
    }

    /**
     * Removes up to the specified quantity of the specified item
     *
     * @param item
     * @param quantity the quantity to remove; this should be non-negative
     * @return the quantity of the item that was actually removed from this ItemList.  This
     * may be less than or equal to the specified quantity
     */

    public int remove(Item item, int quantity)
    {
        return remove(item.getTemplate().getID(), item.getQuality(), quantity);
    }

    /**
     * Removes up to a quantity of one of the specified item
     *
     * @param item
     * @return the quantity of the item that was actually removed from this ItemList.  This
     * may be less than or equal to the specified quantity
     */

    public int remove(Item item)
    {
        return remove(item.getTemplate().getID(), item.getQuality(), 1);
    }

    /**
     * Returns true if and only if this ItemList contains at least the specified quantity
     * of the specified item
     *
     * @param item
     * @param quantity
     * @return whether this ItemList contains the specified quantity of the specified Item
     */

    public boolean contains(Item item, int quantity)
    {
        int index = findEntry(item.getTemplate().getID(), item.getQuality());

        if (index == -1) {
            return false;
        } else {
            return entries.get(index).quantity >= quantity;
        }
    }

    /**
     * Returns true if and only if this ItemList contains one or more of the specified Item
     *
     * @param item
     * @return whether this ItemList contains at least one of the specified item
     */

    public boolean contains(Item item)
    {
        return contains(item, 1);
    }

    /**
     * Returns true if this item list contains one or more items with the specified ID
     *
     * @param id the entity ID
     * @return whether this itemList contains at least one item with the specified ID
     */

    public boolean contains(String id)
    {
        for (Entry entry : entries) {
            if (entry.id.equals(id)) return true;
        }

        return false;
    }

    /**
     * Returns true if and only if this ItemList contains one or more of the specified Item
     *
     * @param id
     * @param quality
     * @return whether this ItemList contains at least one of the specified item
     */

    public boolean contains(String id, String quality)
    {
        int index = findEntry(id, Game.ruleset.getItemQuality(quality));

        return index != -1;
    }

    /**
     * Gets the quantity of the specified item currently held in this ItemList, or
     * zero if no matching item is in this List
     *
     * @param item
     * @return the quantity of the item in this List
     */

    public int getQuantity(Item item)
    {
        int index = findEntry(item.getTemplate().getID(), item.getQuality());

        if (index == -1) {
            return 0;
        } else {
            return entries.get(index).quantity;
        }
    }

    /**
     * Returns the total quantity of all items in this list with an ID matching
     * the specified
     *
     * @param itemID
     * @return the total quantity of items with the specified ID
     */

    public int getQuantity(String itemID)
    {
        int quantity = 0;

        for (Entry entry : entries) {
            if (entry.id.equals(itemID)) {
                quantity += entry.quantity;
            }
        }

        return quantity;
    }

    /**
     * Returns the item list entry with the specified ID and quality, or null if
     * no matching entry exists
     *
     * @param id      the item ID
     * @param quality the quality, or null to indicate no quality
     * @return the matching item list entry
     */

    public ItemList.Entry find(String id, Quality quality)
    {
        int index = findEntry(id, quality);

        if (index == -1) {
            return null;
        } else {
            return entries.get(index);
        }
    }

    /**
     * Returns the item list entry with the specified ID and quality, or null if
     * no matching entry exists
     *
     * @param id        the item ID
     * @param qualityID the id of the quality, or null to indicate no quality
     * @return the matching item list entry
     */

    public ItemList.Entry find(String id, String qualityID)
    {
        if (qualityID == null) {
            return find(id, (Quality)null);
        } else {
            return find(id, Game.ruleset.getItemQuality(qualityID));
        }
    }

    private int findEntry(String id, Quality quality)
    {
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);

            if (entry.id.equals(id) && entry.getQuality() == quality) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Gets an entry at random from all the entries in this ItemList.
     *
     * @return a randomly chosen entry.  Will return null if this ItemList
     * is empty
     */

    public Entry getRandomEntry()
    {
        if (entries.size() == 0) {
            return null;
        }

        if (entries.size() == 1) {
            return entries.get(0);
        }

        int index = Game.dice.rand(0, entries.size() - 1);

        return entries.get(index);
    }

    /**
     * Sorts the entries in this item list, so that items are organized by name and quality
     */

    public void sort()
    {
        Collections.sort(entries);
    }

    /**
     * Gets the total weight of all items in this List.  Note that if any item has
     * infinite quantity, the resulting weight from this method is undefined.
     *
     * @return the total weight of all items in this list
     */

    public Weight getTotalWeight()
    {
        int grams = 0;

        for (Entry entry : entries) {
            ItemTemplate template = EntityManager.getItemTemplate(entry.id);

            grams += template.getWeightInGrams() * entry.quantity;
        }

        return new Weight(grams);
    }

    @Override
    public Iterator<Entry> iterator()
    {
        return entries.iterator();
    }

    /*
     * Sets the quantity to zero, notifies all listeners, and removes this entry
     * from the list
     */

    private void removeEntry(int index)
    {
        Entry entry = entries.get(index);

        entry.quantity = 0;

        Iterator<Listener> iter = listeners.iterator();

        while (iter.hasNext()) {
            Listener listener = iter.next();

            if (listener.itemListEntryRemoved(entry)) {
                iter.remove();
            }
        }

        entries.remove(index);
    }
}
