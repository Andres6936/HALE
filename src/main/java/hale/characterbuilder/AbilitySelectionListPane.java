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

package main.java.hale.characterbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import main.java.hale.Game;
import main.java.hale.ability.Ability;
import main.java.hale.ability.AbilitySelectionList;
import main.java.hale.ability.AbilitySelectionList.Connector;
import main.java.hale.entity.Creature;
import main.java.hale.entity.PC;
import main.java.hale.util.Pointf;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A widget for viewing a set of Abilities shown in a List and any
 * subLists.
 *
 * @author Jared Stephen
 */

public class AbilitySelectionListPane extends Widget
{
    private final boolean showSelectable;
    private AbilitySelectionList list;
    private int gridSize;

    private List<AbilitySelectorButton> buttons;
    private List<AbilitySelectionListPane> subListPanes;
    private List<Label> subListLabels;

    private List<ConnectorViewer> connectorViewers;

    private Creature parentPC;

    /**
     * Create a new AbilitySelectionListPane viewing the specified list for the specified Creature.
     * SubListPanes will be created recursively.
     *
     * @param list            the AbilitySelectionList to view
     * @param parentPC        the Creature viewing the list
     * @param parent          the parent widget that mouse hoverovers will be added to
     * @param showSelectable  true if abilities where prereqs are met should be highlighted, false if not
     * @param listIDsNotToAdd IDs of sublists which should be ignored
     */

    public AbilitySelectionListPane(AbilitySelectionList list, PC parentPC,
                                    AbilitySelectorButton.HoverHolder parent, boolean showSelectable, List<String> listIDsNotToAdd)
    {
        this.parentPC = parentPC;
        this.list = list;
        this.showSelectable = showSelectable;

        Set<Ability> abilities = list.getAbilities();
        Set<String> subLists = list.getSubListIDs();
        List<AbilitySelectionList.Connector> connectors = list.getConnectors();

        buttons = new ArrayList<AbilitySelectorButton>(abilities.size());
        subListPanes = new ArrayList<AbilitySelectionListPane>(subLists.size());
        subListLabels = new ArrayList<Label>(subLists.size());
        connectorViewers = new ArrayList<ConnectorViewer>(connectors.size());

        // create the connectors
        for (AbilitySelectionList.Connector connector : list.getConnectors()) {
            ConnectorViewer viewer = new ConnectorViewer(connector);

            connectorViewers.add(viewer);
            add(viewer);
        }

        // create the top level ability selector buttons
        for (Ability ability : list.getAbilities()) {
            AbilitySelectorButton button = new AbilitySelectorButton(ability, parentPC, parent, showSelectable);
            button.setWidgetToAddWindowsTo(parent.getGUI().getRootPane());
            button.setState(parentPC);

            add(button);
            buttons.add(button);
        }

        // create sub lists
        for (String listID : subLists) {
            AbilitySelectionList subList = Game.ruleset.getAbilitySelectionList(listID);

            if (listIDsNotToAdd.contains(listID)) continue;

            AbilitySelectionListPane pane = new AbilitySelectionListPane(subList, parentPC, parent, showSelectable, listIDsNotToAdd);
            add(pane);
            subListPanes.add(pane);

            Label subListTitle = new Label(subList.getName());
            subListTitle.setTheme("title");
            add(subListTitle);
            subListLabels.add(subListTitle);
        }
    }

    /**
     * returns the grid size for this pane
     *
     * @return the grid size
     */

    public int getGridSize()
    {
        return gridSize;
    }

    /**
     * Adds the specified callback as a callback for all AbilitySelectoButtons in this
     * pane and all sub panes.
     *
     * @param callback the callback to add
     */

    public void addAbilitySelectorCallback(AbilitySelectorButton.Callback callback)
    {
        for (AbilitySelectorButton button : buttons) {
            button.addCallback(callback);
        }

        for (AbilitySelectionListPane pane : subListPanes) {
            pane.addAbilitySelectorCallback(callback);
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        this.gridSize = themeInfo.getParameter("gridSize", 0);

        String alreadyOwnedText = themeInfo.getParameter("alreadyOwnedText", "");
        String prereqsNotMetText = themeInfo.getParameter("prereqsNotMetText", "");
        String selectableText = themeInfo.getParameter("selectableText", "");
        String notOwnedText = themeInfo.getParameter("notOwnedText", "");

        for (AbilitySelectorButton button : buttons) {
            if (parentPC.abilities.has(button.getAbility())) {
                button.setHoverText(alreadyOwnedText, Color.PURPLE);
            } else
                if (showSelectable) {
                    if (!button.getAbility().meetsPrereqs(parentPC)) {
                        button.setHoverText(prereqsNotMetText, Color.RED);
                    } else {
                        button.setHoverText(selectableText, Color.GREEN);
                    }
                } else {
                    button.setHoverText(notOwnedText, Color.RED);
                }
        }
    }

    @Override
    protected void layout()
    {
        super.layout();

        // keep track of the size of this pane
        int maxRight = getX();
        int maxBottom = getY();

        // layout the top level buttons
        for (AbilitySelectorButton button : buttons) {
            Pointf position = this.list.getGridPosition(button.getAbility());

            button.setPosition(getInnerX() + (int)(gridSize * position.x),
                    getInnerY() + (int)(gridSize * position.y));

            if (button.getRight() > maxRight) maxRight = button.getRight();
            if (button.getBottom() > maxBottom) maxBottom = button.getBottom();
        }

        // layout any sub panes
        for (int i = 0; i < subListPanes.size(); i++) {
            AbilitySelectionListPane pane = subListPanes.get(i);
            Label paneLabel = subListLabels.get(i);

            Pointf position = this.list.getGridPosition(pane.list);

            pane.setPosition(getInnerX() + (int)(gridSize * position.x),
                    getInnerY() + (int)(gridSize * position.y));

            paneLabel.setSize(paneLabel.getPreferredWidth(), paneLabel.getPreferredHeight());
            paneLabel.setPosition(pane.getX(), pane.getY() - paneLabel.getHeight());

            int labelPaneMaxRight = Math.max(paneLabel.getRight(), pane.getRight());

            if (labelPaneMaxRight > maxRight) maxRight = labelPaneMaxRight;
            if (pane.getBottom() > maxBottom) maxBottom = pane.getBottom();
        }

        setSize(maxRight + getBorderRight() - getX(), maxBottom + getBorderBottom() - getY());

        //figure out button size for connectors
        int buttonWidth, buttonHeight;
        if (buttons.size() > 0) {
            buttonWidth = buttons.get(0).getWidth();
            buttonHeight = buttons.get(0).getHeight();
        } else {
            buttonWidth = 0;
            buttonHeight = 0;
        }

        //layout connectors
        for (ConnectorViewer viewer : connectorViewers) {
            viewer.setSize(viewer.getBackground().getWidth(), viewer.getBackground().getHeight());

            Pointf point = viewer.connector.getPoint();

            float posX = gridSize * point.x + (buttonWidth - viewer.getWidth()) / 2.0f;
            ;
            float posY = gridSize * point.y;

            switch (viewer.connector.getType()) {
                case OneUp:
                case TwoUp:
                case ThreeUp:
                case FourUp:
                    posY -= viewer.getHeight();
                    break;
                case OneDown:
                case TwoDown:
                case ThreeDown:
                case FourDown:
                    posY += buttonHeight;
                    break;
                default:
                    // do nothing
            }

            viewer.setPosition(getInnerX() + (int)posX, getInnerY() + (int)posY);
        }
    }

    private class ConnectorViewer extends Widget
    {
        private Connector connector;

        private ConnectorViewer(Connector connector)
        {
            this.connector = connector;
            this.setTheme("connector");
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            switch (connector.getType()) {
                case OneUp:
                    setBackground(themeInfo.getImage("one-up"));
                    break;
                case OneDown:
                    setBackground(themeInfo.getImage("one-down"));
                    break;
                case TwoUp:
                    setBackground(themeInfo.getImage("two-up"));
                    break;
                case TwoDown:
                    setBackground(themeInfo.getImage("two-down"));
                    break;
                case ThreeUp:
                    setBackground(themeInfo.getImage("three-up"));
                    break;
                case ThreeDown:
                    setBackground(themeInfo.getImage("three-down"));
                    break;
                case FourUp:
                    setBackground(themeInfo.getImage("four-up"));
                    break;
                case FourDown:
                    setBackground(themeInfo.getImage("four-down"));
                    break;
                case Through:
                    setBackground(themeInfo.getImage("through"));
                    break;
            }
        }
    }
}
