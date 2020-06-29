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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.Saveable;
import main.java.hale.util.Logger;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

public class QuestEntryList implements Saveable
{
    private final Map<String, QuestEntry> activeEntries;
    private final Map<String, QuestEntry> completedEntries;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        Object[] activeData = new Object[activeEntries.size()];
        int i = 0;
        for (String key : activeEntries.keySet()) {
            activeData[i] = activeEntries.get(key).save();
            i++;
        }
        data.put("activeEntries", activeData);

        Object[] completedData = new Object[completedEntries.size()];
        i = 0;
        for (String key : completedEntries.keySet()) {
            completedData[i] = completedEntries.get(key).save();
            i++;
        }
        data.put("completedEntries", completedData);

        return data;
    }

    public static QuestEntryList load(SimpleJSONObject data)
    {
        QuestEntryList list = new QuestEntryList();

        for (SimpleJSONArrayEntry entry : data.getArray("activeEntries")) {
            SimpleJSONObject entryData = entry.getObject();

            QuestEntry questEntry = QuestEntry.load(entryData);

            list.activeEntries.put(questEntry.getTitle(), questEntry);
        }

        for (SimpleJSONArrayEntry entry : data.getArray("completedEntries")) {
            SimpleJSONObject entryData = entry.getObject();

            QuestEntry questEntry = QuestEntry.load(entryData);

            list.completedEntries.put(questEntry.getTitle(), questEntry);
        }

        return list;
    }

    public QuestEntryList()
    {
        activeEntries = new LinkedHashMap<String, QuestEntry>();
        completedEntries = new LinkedHashMap<String, QuestEntry>();
    }

    public void addEntry(QuestEntry entry)
    {
        if (hasEntry(entry)) {
            Logger.appendToErrorLog("Attempted to add entry with duplicate title " + entry.getTitle());
            return;
        }

        if (entry.isCompleted()) {
            addCompleted(entry);
        } else {
            addActive(entry);
        }
    }

    public QuestEntry getEntry(String title)
    {
        if (activeEntries.containsKey(title)) {
            return activeEntries.get(title);
        } else {
            return completedEntries.get(title);
        }
    }

    public boolean hasEntry(String title)
    {
        if (activeEntries.containsKey(title)) return true;

        return completedEntries.containsKey(title);
    }

    public boolean hasEntry(QuestEntry entry)
    {
        return hasEntry(entry.getTitle());
    }

    public void setCompleted(QuestEntry entry)
    {
        if (activeEntries.containsKey(entry.getTitle())) {
            removeActive(entry);
            addCompleted(entry);
        }
    }

    private void addCompleted(QuestEntry entry)
    {
        completedEntries.put(entry.getTitle(), entry);
    }

    private void addActive(QuestEntry entry)
    {
        activeEntries.put(entry.getTitle(), entry);
    }

    private void removeActive(QuestEntry entry)
    {
        activeEntries.remove(entry.getTitle());
    }

    public Collection<QuestEntry> getCompletedEntries()
    {
        return Collections.unmodifiableCollection(completedEntries.values());

    }

    public Collection<QuestEntry> getActiveEntries()
    {
        return Collections.unmodifiableCollection(activeEntries.values());
    }
}
