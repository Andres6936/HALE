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

package main.java.hale.view;

import java.util.ArrayList;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.SavedParty;
import main.java.hale.characterbuilder.CharacterBuilder;
import main.java.hale.entity.Creature;
import main.java.hale.entity.PC;
import main.java.hale.widgets.BasePortraitViewer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The widget used for exporting party members as well as the party itself to a file
 *
 * @author Jared
 */

public class ExportPopup extends PopupWindow
{
    private Content content;

    /**
     * Creates a new Popup with the specified parent widget
     *
     * @param parent
     */

    public ExportPopup(Widget parent)
    {
        super(parent);

        content = new Content();

        add(content);

        setCloseOnClickedOutside(false);
        setCloseOnEscape(false);
    }

    private class ExportCallback implements Runnable
    {
        private int index;

        private ExportCallback(int index)
        {
            this.index = index;
        }

        @Override
        public void run()
        {
            content.export(index);
        }
    }

    private class Content extends Widget
    {
        private Label title;
        private Button close;
        private Button exportAll;

        private int minLevel, maxLevel;
        private List<String> exportedIDs;

        private List<BasePortraitViewer> portraits;
        private List<Label> portraitNames;
        private List<Button> portraitExports;

        private int sectionGap;
        private int minWidth;

        private void export(int index)
        {
            Creature creature = portraits.get(index).getCreature();

            String id = CharacterBuilder.savePC((PC)creature);

            Button button = portraitExports.get(index);

            button.setEnabled(false);
            button.setText("Exported!");

            exportedIDs.set(index, id);
        }

        private void exportAll()
        {
            for (int index = 0; index < portraits.size(); index++) {
                if (exportedIDs.get(index) == null) {
                    // this creature has not been exported yet
                    export(index);
                }
            }

            SavedParty party = new SavedParty(exportedIDs, Game.curCampaign.party.getName(),
                    minLevel, maxLevel, Game.curCampaign.getPartyCurrency().getValue());
            party.writeToFile();

            exportAll.setEnabled(false);
            exportAll.setText("Party Exported!");
        }

        private Content()
        {
            title = new Label();
            title.setTheme("titlelabel");
            add(title);

            close = new Button();
            close.setTheme("closebutton");
            close.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    ExportPopup.this.closePopup();
                }
            });
            add(close);

            portraitNames = new ArrayList<Label>();
            portraitExports = new ArrayList<Button>();
            portraits = new ArrayList<BasePortraitViewer>();
            exportedIDs = new ArrayList<String>();

            minLevel = Integer.MAX_VALUE;
            maxLevel = 0;

            int index = 0;
            for (Creature creature : Game.curCampaign.party) {
                minLevel = Math.min(minLevel, creature.roles.getTotalLevel());
                maxLevel = Math.max(maxLevel, creature.roles.getTotalLevel());

                BasePortraitViewer viewer = new BasePortraitViewer(creature);
                add(viewer);
                portraits.add(viewer);

                Label nameLabel = new Label(creature.getTemplate().getName());
                nameLabel.setTheme("portraitnamelabel");
                add(nameLabel);
                portraitNames.add(nameLabel);

                Button button = new Button("Export");
                button.setTheme("exportbutton");
                button.addCallback(new ExportCallback(index));
                add(button);
                portraitExports.add(button);

                exportedIDs.add(null);

                index++;
            }

            exportAll = new Button("Export Party");
            exportAll.setTheme("exportallbutton");
            exportAll.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    exportAll();
                }
            });
            add(exportAll);
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            sectionGap = themeInfo.getParameter("sectiongap", 0);
            minWidth = themeInfo.getParameter("minwidth", 0);
        }

        @Override
        public int getPreferredWidth()
        {
            int portraitWidth = 0;
            for (BasePortraitViewer viewer : portraits) {
                portraitWidth += viewer.getPreferredWidth() + sectionGap;
            }
            portraitWidth -= sectionGap;

            int width = Math.max(title.getPreferredWidth(), portraitWidth);
            width = Math.max(exportAll.getPreferredWidth() + close.getPreferredWidth() + sectionGap, width);

            return Math.max(minWidth, width) + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            int height = title.getPreferredHeight() + close.getPreferredHeight();
            height += 3 * sectionGap;

            int portraitHeight = 0;
            for (int viewerIndex = 0; viewerIndex < portraits.size(); viewerIndex++) {
                BasePortraitViewer viewer = portraits.get(viewerIndex);
                Label label = portraitNames.get(viewerIndex);
                Button button = portraitExports.get(viewerIndex);

                portraitHeight = Math.max(portraitHeight, viewer.getPreferredHeight() +
                        label.getPreferredHeight() + button.getPreferredHeight());
            }
            height += portraitHeight;

            return height + getBorderVertical();
        }

        @Override
        protected void layout()
        {
            int centerX = getInnerX() + getInnerWidth() / 2;

            title.setSize(title.getPreferredWidth(), title.getPreferredHeight());

            close.setSize(close.getPreferredWidth(), close.getPreferredHeight());

            title.setPosition(centerX - title.getWidth() / 2, getInnerY());

            int curX = getInnerX();
            for (int viewerIndex = 0; viewerIndex < portraits.size(); viewerIndex++) {
                BasePortraitViewer viewer = portraits.get(viewerIndex);
                Label label = portraitNames.get(viewerIndex);
                Button button = portraitExports.get(viewerIndex);

                viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
                label.setSize(label.getPreferredWidth(), label.getPreferredHeight());
                button.setSize(viewer.getWidth(), button.getPreferredHeight());

                label.setPosition(curX + viewer.getWidth() / 2 - label.getWidth() / 2,
                        title.getBottom() + sectionGap);
                viewer.setPosition(curX, label.getBottom());
                button.setPosition(curX, viewer.getBottom());

                curX = viewer.getRight() + sectionGap;
            }

            close.setPosition(getInnerRight() - close.getWidth(), getInnerBottom() - close.getHeight());

            exportAll.setSize(exportAll.getPreferredWidth(), exportAll.getPreferredHeight());
            exportAll.setPosition(getInnerX(), getInnerBottom() - exportAll.getHeight());
        }
    }
}
