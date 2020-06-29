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

package main.java.hale.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import main.java.hale.Game;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.Saveable;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * A quest entry is a specific set of sub entries and a title.  Whenever the player
 * performs an action such as entering a new area or talking to a creature, the script
 * file associated with that action can create a new quest entry or sub entry.  As new
 * sub entries are created, typically old ones are marked as finished.  Eventually, the
 * player will complete the quest line and mark the entire QuestEntry as finished.
 *
 * @author Jared Stephen
 */

public class QuestEntry implements Iterable<QuestSubEntry>, Saveable
{
    private boolean completed;
    private boolean showLogNotifications;
    private final Map<String, QuestSubEntry> subEntries;

    // use a linked list rather than a linkedhashmap which lets us iterate over
    // the elements in reverse order
    private final LinkedList<QuestSubEntry> subEntriesList;

    private final String title;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("title", title);
        data.put("completed", completed);
        data.put("showLogNotifications", showLogNotifications);

        int i = 0;
        Object[] subEntryData = new Object[subEntriesList.size()];
        for (QuestSubEntry subEntry : subEntriesList) {
            subEntryData[i] = subEntry.save();
            i++;
        }
        data.put("subEntries", subEntryData);

        return data;
    }

    public static QuestEntry load(SimpleJSONObject data)
    {
        QuestEntry entry = new QuestEntry(data.get("title", null));

        entry.completed = data.get("completed", false);
        entry.showLogNotifications = data.get("showLogNotifications", false);

        for (SimpleJSONArrayEntry arrayEntry : data.getArray("subEntries")) {
            SimpleJSONObject entryObject = arrayEntry.getObject();

            QuestSubEntry subEntry = QuestSubEntry.load(entryObject);

            entry.subEntriesList.add(subEntry);
        }

        for (QuestSubEntry subEntry : entry.subEntriesList) {
            entry.subEntries.put(subEntry.getTitle(), subEntry);
        }

        return entry;
    }

    /**
     * Creates a new QuestEntry with the specified title.  This entry will not have
     * any sub entries yet.
     *
     * @param title the title String for this QuestEntry.
     */

    public QuestEntry(String title)
    {
        this.title = title;
        this.completed = false;
        this.showLogNotifications = true;

        this.subEntries = new HashMap<String, QuestSubEntry>();
        this.subEntriesList = new LinkedList<QuestSubEntry>();
    }

    /**
     * Returns true if and only if this Quest should show new log notifications
     * in the UI
     *
     * @return whether this QuestEntry shows new log notifications
     */

    public boolean showsLogNotifications()
    {
        return showLogNotifications;
    }

    /**
     * Sets whether this Quest will trigger the new log notification in the game UI
     * when a new quest sub entry is created
     *
     * @param show whether this Quest will cause new log notifications to occur
     */

    public void setShowLogNotifications(boolean show)
    {
        this.showLogNotifications = show;
    }

    /**
     * Sets the entire quest as completed.  This quest
     * entry is then removed from the list of active quest entries and added
     * to the list of completed quest entries.
     */

    public void setCompleted()
    {
        this.completed = true;
        Game.curCampaign.questEntries.setCompleted(this);
    }

    /**
     * Marks all current subEntries contained in this QuestEntry to completed.
     */

    public synchronized void setCurrentSubEntriesCompleted()
    {
        for (String key : subEntries.keySet()) {
            subEntries.get(key).setCompleted();
        }
    }

    /**
     * Returns true if and only if this QuestEntry contains a SubEntry with
     * the specified title
     *
     * @param title the title of the sub entry
     * @return whether this QuestEntry contains a subEntry with the specified title
     */

    public boolean hasSubEntry(String title)
    {
        return subEntries.containsKey(title);
    }

    /**
     * Returns the QuestSubEntry contained in this QuestEntry with the specified title,
     * or null if no such QuestSubEntry exists
     *
     * @param title the title of the QuestSubEntry
     * @return the QuestSubEntry in this QuestEntry with the specified title
     */

    public QuestSubEntry getSubEntry(String title)
    {
        return subEntries.get(title);
    }

    /**
     * Creates a new QuestSubEntry within this QuestEntry with the specified
     * title.  The description of the QuestSubEntry is not set by this method,
     * just the title.  The QuestSubEntry is added to the list of sub entries
     * within this entry.
     *
     * @param title the title for the new QuestSubEntry
     * @return the created QuestSubEntry
     */

    public synchronized QuestSubEntry createSubEntry(String title)
    {
        QuestSubEntry entry = new QuestSubEntry(title);

        subEntries.put(entry.getTitle(), entry);
        subEntriesList.add(entry);

        Game.mainViewer.setNewQuestEntry(this);
        Game.mainViewer.updateInterface();

        return entry;
    }

    /**
     * Returns the number of subentries contained in this QuestEntry
     *
     * @return the number of sub entries contained in this QuestEntry
     */

    public int getNumSubEntries()
    {
        return subEntriesList.size();
    }

    /**
     * Returns the QuestSubEntry that was most recently added to this QuestEntry
     *
     * @return the most recently created QuestSubEntry
     * @throws NoSuchElementException if this QuestEntry has no sub entries
     */

    public QuestSubEntry getMostRecentSubEntry()
    {
        return subEntriesList.getLast();
    }

    @Override
    public Iterator<QuestSubEntry> iterator()
    {
        return new EntryIterator();
    }

    /**
     * Returns the title string for this QuestEntry
     *
     * @return the title string for this QuestEntry
     */

    public String getTitle()
    {
        return title;
    }

    /**
     * Returns true if and only if this QuestEntry is marked as completed
     *
     * @return whether this QuestEntry is completed
     */

    public boolean isCompleted()
    {
        return completed;
    }

    @Override
    public String toString()
    {
        return title;
    }

    private class EntryIterator implements Iterator<QuestSubEntry>
    {
        private Iterator<QuestSubEntry> iter;

        private EntryIterator()
        {
            iter = subEntriesList.descendingIterator();
        }

        @Override
        public boolean hasNext()
        {
            return iter.hasNext();
        }

        @Override
        public QuestSubEntry next()
        {
            return iter.next();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Cannot remove a sub entry from a quest entry.");
        }

    }
}
