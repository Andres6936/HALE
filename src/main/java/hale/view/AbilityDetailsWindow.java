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

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import main.java.hale.ability.Ability;
import main.java.hale.entity.Creature;
import main.java.hale.widgets.IconViewer;

/**
 * A widget for displaying all of the details about an Ability in a single window.
 *
 * @author Jared Stephen
 */

public class AbilityDetailsWindow extends GameSubWindow
{
    /**
     * Create a new AbilityDetailsWindow displaying information about the
     * specified Ability
     *
     * @param ability the Ability to view
     * @param parent  the owner of the specified ability, or null to specify no owner
     * @param upgrade whether to base on the upgraded version (true) of the base version (false)
     */

    public AbilityDetailsWindow(Ability ability, Creature parent, boolean upgrade)
    {
        this.setTitle("Details for " + (upgrade ? ability.getUpgradedName(parent) : ability.getName()));

        DialogLayout layout = new DialogLayout();
        layout.setTheme("content");
        this.add(layout);

        // set up the widgets for the top row
        IconViewer iconViewer = new IconViewer(upgrade ? ability.getUpgradedIcon(parent) : ability.getIcon());
        iconViewer.setEventHandlingEnabled(false);

        Label title = new Label(upgrade ? ability.getUpgradedName(parent) : ability.getName());
        title.setTheme("titlelabel");

        DialogLayout.Group topV = layout.createParallelGroup(iconViewer, title);

        DialogLayout.Group topH = layout.createSequentialGroup(iconViewer);
        topH.addGap(10);
        topH.addWidget(title);
        topH.addGap(10);

        // set up the widgets for the bottom area
        HTMLTextAreaModel textAreaModel = new HTMLTextAreaModel();
        TextArea textArea = new TextArea(textAreaModel);
        ScrollPane textPane = new ScrollPane(textArea);
        textPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

        // set up the main layout
        DialogLayout.Group mainH = layout.createParallelGroup(topH);
        mainH.addWidget(textPane);

        DialogLayout.Group mainV = layout.createSequentialGroup(topV);
        mainV.addGap(5);
        mainV.addWidget(textPane);

        layout.setHorizontalGroup(mainH);
        layout.setVerticalGroup(mainV);

        // create the text area contents
        StringBuilder sb = new StringBuilder();

        ability.appendDetails(sb, parent, upgrade);

        sb.append("<div style=\"margin-top: 1em\">");
        sb.append(ability.getDescription());
        sb.append("</div>");

        ability.appendUpgradesDescription(sb, parent);
        textAreaModel.setHtml(sb.toString());
    }

    /*
     * This overrides the default close behavior of GameSubWindow
     * @see main.java.hale.view.GameSubWindow#run()
     */

    @Override
    public void run()
    {
        getParent().removeChild(this);
    }
}
