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

package main.java.hale.mainmenu;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import main.java.hale.resource.ResourceType;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONParser;
import main.java.hale.widgets.TextAreaNoInput;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/*
 * A widget used within the CampaignPopup for selecting a campaign group to load
 */

public class CampaignGroupSelector extends ToggleButton implements Runnable
{
    private final CampaignGroup group;

    private CampaignPopup callback;

    private TextArea groupNameArea;
    //private Label groupName;

    private CampaignSelector selected;

    private CampaignGroupSelector(CampaignGroup group)
    {
        this.group = group;

        HTMLTextAreaModel model = new HTMLTextAreaModel();
        model.setHtml("<div style=\"font-family: large-white;\">" + group.name + "</div>");

        groupNameArea = new TextAreaNoInput(model);
        groupNameArea.setTheme("namearea");
        add(groupNameArea);

        //groupName = new Label(group.name);
        //groupName.setTheme("namelabel");
        //add(groupName);

        addCallback(this);
    }

    /**
     * Creates a new group selector with a single entry in the group
     *
     * @param descriptor the single and default entry for this campaign group
     */

    public CampaignGroupSelector(CampaignDescriptor descriptor)
    {
        this(new CampaignGroup(descriptor));
    }

    /**
     * Creates a new group selector widget for the campaign group with the specified ID
     *
     * @param id the ID (based on the filename of the group)
     */

    public CampaignGroupSelector(String id, Map<String, CampaignDescriptor> campaigns)
    {
        this(new CampaignGroup(id, campaigns));
    }

    /**
     * Gets a list of the IDs of all campaigns in this campaign group
     *
     * @return the list of campaign IDs
     */

    public List<String> getAllCampaignIDs()
    {
        List<String> ids = new ArrayList<String>();

        for (CampaignDescriptor descriptor : group.entries) {
            ids.add(descriptor.id);
        }

        return ids;
    }

    /**
     * called when a different group selector has gained focus
     */

    public void deselect()
    {
        this.setActive(false);

        removeAllChildren();
        add(groupNameArea);
        invalidateLayout();
    }

    /**
     * Selects the campaign with the specified ID from within this group, if it is present
     *
     * @param id
     */

    public void selectCampaign(String id)
    {
        if (id == null) return;

        for (CampaignDescriptor descriptor : group.entries) {
            if (descriptor.id.equals(id)) {
                addChildWidgets(descriptor);
                callback.groupSelected(this);
                setActive(true);
            }
        }
    }

    private void addChildWidgets(CampaignDescriptor descriptorToSelect)
    {
        removeAllChildren();
        add(groupNameArea);

        for (CampaignDescriptor descriptor : group.entries) {
            CampaignSelector selector = new CampaignSelector(descriptor);

            // if this is the default entry, select it
            if (descriptor == descriptorToSelect) {
                selected = selector;
                selector.setActive(true);
            }

            // only show selectors if there are two or more entries
            if (group.entries.size() > 1) {
                add(selector);
            }
        }

        invalidateLayout();
    }

    // selector clicked callback

    @Override
    public void run()
    {
        addChildWidgets(group.defaultEntry);

        callback.groupSelected(this);
        setActive(true);
    }

    /**
     * Returns the Campaign ID of the currently selected Campaign
     *
     * @return the selected campaign ID
     */

    public String getSelectedID()
    {
        return selected.descriptor.id;
    }

    /**
     * Returns the description of the currently selected campaign
     *
     * @return the current description
     */

    public String getSelectedDescription()
    {
        return selected.descriptor.description;
    }

    /**
     * Returns the name of the campaign group that this selector is representing
     *
     * @return the name of the campaign group
     */

    public String getCampaignName()
    {
        return group.name;
    }

    /**
     * Sets the callback that is used when this selector is clicked
     *
     * @param popup
     */

    public void setCallback(CampaignPopup popup)
    {
        this.callback = popup;
    }

    @Override
    public int getPreferredHeight()
    {
        int height = 0;

        for (int i = 0; i < getNumChildren(); i++) {
            height += getChild(i).getPreferredHeight();
        }

        return height + getBorderVertical();
    }

    @Override
    protected void layout()
    {
        int numChildren = getNumChildren();

        groupNameArea.setSize(getInnerWidth(), groupNameArea.getPreferredHeight());

        // when not expanded, layout the name centered, when expanded, lay it out at the top
        if (numChildren == 1) {
            groupNameArea.setPosition(getInnerX(), getInnerY() + getInnerHeight() / 2 - groupNameArea.getHeight() / 2);
        } else {
            groupNameArea.setPosition(getInnerX(), getInnerY());
        }

        int curY = getInnerY() + groupNameArea.getPreferredHeight();

        for (int i = 1; i < numChildren; i++) {
            Widget child = getChild(i);

            child.setSize(getInnerWidth(), child.getPreferredHeight());
            child.setPosition(getInnerX(), curY);

            curY = child.getBottom();
        }
    }

    private class CampaignSelector extends ToggleButton implements Runnable
    {
        private CampaignDescriptor descriptor;

        private CampaignSelector(CampaignDescriptor descriptor)
        {
            super(descriptor.name);
            this.descriptor = descriptor;
            addCallback(this);
        }

        @Override
        public void run()
        {
            if (selected != null) {
                selected.setActive(false);
            }

            selected = this;
            setActive(true);

            callback.groupSelected(CampaignGroupSelector.this);
        }
    }

    /**
     * A class containing the campaign description and name
     */

    public static class CampaignDescriptor
    {
        public final String id;
        public final String name;
        public final String description;

        /**
         * Creates a new CampaignDescriptor
         *
         * @param id          the ID of the campaign
         * @param name        the name of the campaign
         * @param description the HTML description of the campaign
         */

        protected CampaignDescriptor(String id, String name, String description)
        {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    private static class CampaignGroup
    {
        private final String name;
        private final CampaignDescriptor defaultEntry;
        private final List<CampaignDescriptor> entries;

        private CampaignGroup(CampaignDescriptor entry)
        {
            this.name = entry.name;
            this.defaultEntry = entry;
            this.entries = Collections.singletonList(entry);
        }

        private CampaignGroup(String id, Map<String, CampaignDescriptor> campaigns)
        {
            SimpleJSONParser parser = new SimpleJSONParser(new File("campaigns/" + id + ResourceType.JSON.getExtension()));

            this.name = parser.get("name", id);

            String defaultEntry = parser.get("defaultCampaign", null);
            this.defaultEntry = campaigns.get(defaultEntry);

            this.entries = new ArrayList<CampaignDescriptor>();

            SimpleJSONArray array = parser.getArray("campaigns");
            for (SimpleJSONArrayEntry entry : array) {
                String entryID = entry.getString();

                this.entries.add(campaigns.get(entryID));
            }
        }
    }
}
