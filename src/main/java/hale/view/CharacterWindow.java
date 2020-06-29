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

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.Widget;
import main.java.hale.entity.Entity;
import main.java.hale.entity.EntityListener;
import main.java.hale.entity.PC;

/**
 * The window showing the detailed statistics about a Character.  Includes tabs for an
 * overview, skills, and abilities
 *
 * @author Jared Stephen
 */

public class CharacterWindow extends GameSubWindow implements EntityListener
{
    private final Content content;

    private final Button exportButton;

    private final TabbedPane tabbedPane;

    private final CharacterSheet characterSheet;
    private final SkillSetViewer skillSetViewer;
    private final AbilitiesSheet abilitiesSheet;

    /**
     * Creates a new CharacterWindow.  You must call {@link #updateContent(PC)}
     * to specify the Creature being viewed.
     */

    public CharacterWindow()
    {
        content = new Content();
        add(content);

        exportButton = new Button();
        exportButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                new ExportPopup(getParent()).openPopupCentered();
            }
        });
        exportButton.setTheme("exportbutton");
        content.add(exportButton);

        tabbedPane = new TabbedPane();
        tabbedPane.setTheme("content");
        content.add(tabbedPane);

        characterSheet = new CharacterSheet();
        skillSetViewer = new SkillSetViewer();
        abilitiesSheet = new AbilitiesSheet();

        tabbedPane.addTab("Overview", characterSheet);
        tabbedPane.addTab("Skills", skillSetViewer);
        tabbedPane.addTab("Abilities", abilitiesSheet);
    }

    /**
     * Updates the content of this CharacterWindow for the specified Creature.
     *
     * @param creature the Player Character Creature to view
     */

    public void updateContent(PC creature)
    {
        this.setTitle("Character Record for " + creature.getTemplate().getName());

        characterSheet.updateContent(creature);
        skillSetViewer.updateContent(creature);
        abilitiesSheet.updateContent(creature);
    }

    /**
     * Sets the "export" button to be invisible, disabling the export option
     */

    protected void hideExportButton()
    {
        exportButton.setVisible(false);
    }

    private class Content extends Widget
    {
        private Content()
        {
            setTheme("");
        }

        @Override
        protected void layout()
        {
            tabbedPane.setSize(getInnerWidth(), getInnerHeight());
            tabbedPane.setPosition(getInnerX(), getInnerY());

            exportButton.setSize(exportButton.getPreferredWidth(), exportButton.getPreferredHeight());
            exportButton.setPosition(getInnerRight() - exportButton.getWidth(), getInnerY());
        }
    }

    @Override
    public void entityUpdated(Entity entity)
    {
        // TODO implement
    }

    @Override
    public void removeListener()
    {
        getParent().removeChild(this);
    }
}
