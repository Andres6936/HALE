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

package main.java.hale.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import main.java.hale.Game;
import main.java.hale.rules.QuestEntry;
import main.java.hale.rules.QuestSubEntry;
import main.java.hale.widgets.ExpandableWidget;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * A widget for viewing the set of quests currently active and completed for the
 * party.
 *
 * @author Jared Stephen
 */

public class QuestSetViewer extends Widget
{
    private QuestEntry newEntry;

    private Map<String, QuestViewer> viewers;

    private DialogLayout.Group mainH;
    private DialogLayout.Group mainV;

    private ToggleButton showCompleted;
    private ScrollPane pane;
    private DialogLayout paneContent;

    /**
     * Creates a new, Empty QuestSetViewer Widget.
     */

    public QuestSetViewer()
    {
        paneContent = new DialogLayout();
        paneContent.setTheme("content");
        pane = new ScrollPane(paneContent);
        pane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(pane);

        showCompleted = new ToggleButton();
        showCompleted.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                updateContent();
            }
        });
        showCompleted.setTheme("showcompletedbutton");
        add(showCompleted);

        viewers = new HashMap<String, QuestViewer>();

        mainH = paneContent.createParallelGroup();
        mainV = paneContent.createSequentialGroup();

        paneContent.setHorizontalGroup(mainH);
        paneContent.setVerticalGroup(mainV);
    }

    /**
     * Sets the new Entry for this QuestSetViewer to the specified entry.  The
     * viewer will scroll to show this new entry.
     *
     * @param newEntry the entry to set as the new entry
     */

    public void notifyNewEntry(QuestEntry newEntry)
    {
        this.newEntry = newEntry;

        Game.mainViewer.updateInterface();
    }

    @Override
    protected void layout()
    {
        super.layout();

        showCompleted.setSize(showCompleted.getPreferredWidth(), showCompleted.getPreferredHeight());
        showCompleted.setPosition(getInnerRight() - showCompleted.getWidth(), getInnerY());

        pane.setSize(getInnerWidth(), getInnerBottom() - showCompleted.getBottom());
        pane.setPosition(getInnerX(), getInnerY() + showCompleted.getHeight());
    }

    /**
     * Updates this QuestSetViewer to show all QuestEntries currently in the campaign.
     * Only shows completed quest entries if the show completed button is checked.
     */

    public void updateContent()
    {
        updateEntries(Game.curCampaign.questEntries.getActiveEntries());

        if (showCompleted.isActive()) {
            updateEntries(Game.curCampaign.questEntries.getCompletedEntries());
        } else {
            removeEntries(Game.curCampaign.questEntries.getCompletedEntries());
        }

        if (newEntry != null) {
            QuestViewer viewer = viewers.get(newEntry.getTitle());

            if (viewer != null) {
                pane.setScrollPositionY(viewer.getY() - paneContent.getY());
            }

            newEntry = null;
        }
    }

    private void updateEntries(Collection<QuestEntry> entries)
    {
        for (QuestEntry entry : entries) {
            String title = entry.getTitle();

            if (viewers.containsKey(title)) {
                viewers.get(title).update();
            } else {
                // add a new viewer if one does not exist for this QuestEntry
                QuestViewer viewer = new QuestViewer(entry);
                viewer.update();

                viewers.put(title, viewer);
                mainH.addWidget(viewer);
                mainV.addWidget(viewer);
            }
        }
    }

    private void removeEntries(Collection<QuestEntry> entries)
    {
        for (QuestEntry entry : entries) {
            String title = entry.getTitle();

            if (viewers.containsKey(title)) {
                Widget viewer = viewers.get(title);

                paneContent.removeChild(viewer);
                viewers.remove(title);
            }
        }
    }

    private class QuestViewer extends ExpandableWidget
    {
        private QuestEntry entry;

        private QuestViewer(QuestEntry entry)
        {
            super(null);

            this.entry = entry;
        }

        @Override
        public void update()
        {
            super.update();

            if (entry.getNumSubEntries() < 2) {
                this.setExpandContractVisible(false);
            } else {
                this.setExpandContractVisible(true);
            }
        }

        @Override
        public int getPreferredHeight()
        {
            return getTextArea().getPreferredHeight() + getButtonHeight() + getBorderVertical();
        }

        @Override
        protected void appendDescriptionMain(StringBuilder sb)
        {
            sb.append("<div style=\"font-family: medium-bold;\">");
            sb.append(entry.getTitle());
            sb.append("</div>");

            if (entry.getNumSubEntries() > 0) {
                QuestSubEntry subEntry = entry.getMostRecentSubEntry();
                sb.append("<div style=\"margin-top: 1em; font-family: medium;\">");
                if (subEntry.showTitle()) sb.append(subEntry.getTitle());
                sb.append("</div>");
                sb.append(subEntry.getDescription());
            }
        }

        @Override
        protected void appendDescriptionDetails(StringBuilder sb)
        {
            if (entry.getNumSubEntries() < 1) return;

            Iterator<QuestSubEntry> iter = entry.iterator();

            // the first sub entry has already been read in appendDescriptionMain
            iter.next();

            while (iter.hasNext()) {
                QuestSubEntry subEntry = iter.next();
                sb.append("<div style=\"margin-top: 1em; font-family: medium;\">");
                if (subEntry.showTitle()) sb.append(subEntry.getTitle());
                sb.append("</div>");
                sb.append(subEntry.getDescription());
            }
        }
    }
}
