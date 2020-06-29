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

import main.java.hale.icon.IconFactory;
import main.java.hale.rules.Race;
import main.java.hale.rules.RacialType;

/**
 * The BuilderPane for selecting a character's Race.
 *
 * @author Jared Stephen
 */

public class BuilderPaneRace extends BuilderPane
{
    private List<RaceSelector> raceSelectors;

    /**
     * Creates a new BuilderPaneRace with widgets for editing
     * the race of the specified character, if it can be edited
     *
     * @param character the Buildable character to edit
     */

    public BuilderPaneRace(CharacterBuilder builder, Buildable character)
    {
        super(builder, "Race", character);

        raceSelectors = new ArrayList<RaceSelector>();

        setTitleText("Select Race");

        for (Race race : getCharacter().getSelectableRaces()) {
            RaceSelector selector = new RaceSelector(race);

            if (race == getCharacter().getSelectedRace()) selector.setSelected(true);

            raceSelectors.add(selector);
            addSelector(selector);
        }

        getBackButton().setVisible(false);
    }

    @Override
    public void updateCharacter()
    {
        Race race = getCharacter().getSelectedRace();

        for (RaceSelector selector : raceSelectors) {
            selector.setSelected(selector.race == race);
        }

        getNextButton().setEnabled(race != null);
    }

    @Override
    public void next()
    {

        for (RaceSelector selector : raceSelectors) {
            if (selector.isSelected()) {
                getCharacter().setSelectedRace(selector.race);
                break;
            }
        }

        super.next();
    }

    private class RaceSelector extends BuildablePropertySelector
    {
        private Race race;

        private RaceSelector(Race race)
        {
            super(race.getName(), IconFactory.createIcon(race.getIcon()), false);

            this.race = race;
            this.setSelectable(true);
        }

        @Override
        protected void onMouseClick()
        {
            for (RaceSelector selector : raceSelectors) {
                selector.setSelected(false);
            }

            setSelected(true);

            getNextButton().setEnabled(true);
        }

        @Override
        protected void onMouseHover()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("<div style=\"font-family: large-red; \">");
            sb.append(race.getName()).append("</div>");

            sb.append("<div style=\"font-family: blue; margin-bottom: 1em;\">");
            List<RacialType> racialTypes = race.getRacialTypes();
            for (RacialType type : racialTypes) {
                sb.append(type.getName()).append(" ");
            }
            sb.append("</div>");

            int baseSpeed = 10000 / race.getMovementCost();
            sb.append("<div style=\"margin-bottom: 1em;\">Base Speed: <span style=\"font-family: green;\">");
            sb.append(baseSpeed).append(" hexes / turn</span></div>");

            sb.append(race.getDescriptionFile());

            sb.append("<div style=\"margin-top: 1em; font-family: medium\">Base Racial Attributes</div>");
            sb.append("<table style=\"width: 22ex;\">");
            sb.append("<tr><td>Strength</td><td style=\"font-family: red; text-align:right;\">");
            sb.append(race.getBaseStr());

            sb.append("</td></tr><tr><td>Dexterity</td><td style=\"font-family: red; text-align:right;\">");
            sb.append(race.getBaseDex());

            sb.append("</td></tr><tr><td>Constitution</td><td style=\"font-family: red; text-align:right;\">");
            sb.append(race.getBaseCon());

            sb.append("</td></tr><tr><td>Intelligence</td><td style=\"font-family: red; text-align:right;\">");
            sb.append(race.getBaseInt());

            sb.append("</td></tr><tr><td>Wisdom</td><td style=\"font-family: red; text-align:right;\">");
            sb.append(race.getBaseWis());

            sb.append("</td></tr><tr><td>Charisma</td><td style=\"font-family: red; text-align:right;\">");
            sb.append(race.getBaseCha());

            sb.append("</td></tr></table>");

            getTextModel().setHtml(sb.toString());
            getTextPane().invalidateLayout();
        }
    }
}
