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

package hale.rules;

import hale.loading.JSONOrderedObject;
import hale.loading.Saveable;
import hale.resource.ResourceManager;
import hale.util.SimpleJSONObject;

/**
 * A QuestSubEntry is a leaf in the QuestEntry & QuestSubEntry tree.
 * <p>
 * The SubEntry has a title, description, and a completion status.  QuestSubEntries are
 * viewed in {@link main.java.hale.view.LogWindow}
 *
 * @author Jared
 */

public class QuestSubEntry implements Saveable
{
    private boolean completed;

    private boolean showTitle;
    private String title;
    private StringBuilder description;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("title", title);
        data.put("showTitle", showTitle);
        data.put("completed", completed);
        data.put("description", description.toString());

        return data;
    }

    public static QuestSubEntry load(SimpleJSONObject data)
    {
        QuestSubEntry subEntry = new QuestSubEntry(data.get("title", null));

        subEntry.showTitle = data.get("showTitle", false);
        subEntry.completed = data.get("completed", false);

        String description = data.get("description", null);

        subEntry.description.append(description);

        return subEntry;
    }

    /**
     * Create a new QuestSubEntry with the specified title.  The description is initialized to empty
     * and the completion state is initially false
     *
     * @param title The title of the sub entry
     */

    public QuestSubEntry(String title)
    {
        this.title = title;
        this.description = new StringBuilder();
        this.completed = false;
        this.showTitle = true;
    }

    /**
     * Sets whether the title for this QuestSubEntry should be shown in the QuestSetViewer
     *
     * @param showTitle true to show the title, false to not show the title
     */

    public void setShowTitle(boolean showTitle)
    {
        this.showTitle = showTitle;
    }

    /**
     * Returns true if the title for this sub entry should be shown in the quest
     * entry pane, false otherwise
     *
     * @return whether the title for this sub entry should be shown in the QuestSetViewer
     */

    public boolean showTitle()
    {
        return showTitle;
    }

    /**
     * Sets the completion state to true.  This action is irreversible.
     */

    public void setCompleted()
    {
        this.completed = true;
    }

    /**
     * Appends the text contained in the specified resource to the description of this sub entry.
     * The text can be in plain text or the HTML subset supported by TWL.
     *
     * @param resource the resource location of the text to append
     */

    public void addExternalText(String resource)
    {
        description.append(ResourceManager.getResourceAsString(resource));
    }

    /**
     * Appends the specified text to the description of this sub entry.  The text can be in plain
     * text or the HTML subset supported by TWL.
     *
     * @param text the text to append
     */

    public void addText(String text)
    {
        description.append(text);
        description.append("  ");
    }

    /**
     * Sets the description of this sub entry to empty.
     */

    public void clearText()
    {
        description = new StringBuilder();
    }

    /**
     * Returns the completion status of this subEntry.  SubEntries start out with false
     * completion status, and the status can be changed to true with setCompleted()
     *
     * @return the completion status of this subEntry.
     */

    public boolean isCompleted()
    {
        return completed;
    }

    /**
     * Returns the title of this SubEntry
     *
     * @return the title of this SubEntry
     */

    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the current description of this SubEntry.  The SubEntry must be valid HTML
     * if the description is to be used in a TextField.
     *
     * @return the descript of this SubEntry
     */

    public String getDescription()
    {
        return description.toString();
    }

    /**
     * Returns the title of this QuestSubEntry
     */

    @Override
    public String toString()
    {
        return title;
    }
}
