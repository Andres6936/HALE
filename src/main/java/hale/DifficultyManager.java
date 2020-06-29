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

package main.java.hale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import main.java.hale.resource.ResourceType;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * A class for reading in and managing the set of available difficulty levels
 *
 * @author Jared Stephen
 */

public class DifficultyManager
{
    private final Map<String, DifficultyLevel> levels;

    private String currentDifficulty;

    /**
     * Creates a new Difficulty Manager and reads in the resource at "difficultyLevels.json"
     */

    public DifficultyManager()
    {
        SimpleJSONParser parser = new SimpleJSONParser("difficultyLevels", ResourceType.JSON);

        levels = new LinkedHashMap<String, DifficultyLevel>();

        for (SimpleJSONArrayEntry entry : parser.getArray("difficultyLevels")) {
            SimpleJSONObject levelData = entry.getObject();

            String name = levelData.get("name", null);
            boolean criticalHitsOnPCs = levelData.get("criticalHitsOnPCs", false);
            boolean friendlyFireOnPCs = levelData.get("friendlyFireOnPCs", false);
            int damageFactorOnPCs = levelData.get("damageFactorOnPCs", 100);

            DifficultyLevel level = new DifficultyLevel(criticalHitsOnPCs, friendlyFireOnPCs, damageFactorOnPCs);
            levels.put(name, level);
        }

        currentDifficulty = parser.get("defaultDifficulty", null);

        parser.warnOnUnusedKeys();
    }

    /**
     * Gets an HTML description of the effects of a difficulty level
     *
     * @param difficulty the difficulty to get the description of
     * @return an HTML description
     */

    public String getDifficultyDescription(String difficulty)
    {
        if (!levels.containsKey(difficulty)) {
            throw new IllegalArgumentException("Difficulty " + difficulty + " not found");
        }

        DifficultyLevel level = levels.get(difficulty);

        StringBuilder sb = new StringBuilder();

        sb.append("<div>");
        if (level.criticalHitsOnPCs) {
            sb.append("Normal Critical Hits on Players");
        } else {
            sb.append("No Critical Hits On Players");
        }
        sb.append("</div>");

        sb.append("<div style=\"margin-top: 0.2em\">");
        if (level.friendlyFireOnPCs) {
            sb.append("Normal Friendly Fire on Players from Area Spells");
        } else {
            sb.append("No Friendly Fire on Players from Area Spells");
        }
        sb.append("</div>");

        sb.append("<div style=\"margin-top: 0.2em\">");
        sb.append("Players take ");
        sb.append(Integer.toString(level.damageFactorOnPCs));
        sb.append("% damage");
        sb.append("</div>");

        return sb.toString();
    }

    /**
     * Sets the current difficulty level
     *
     * @param difficulty the name of the difficulty level
     */

    public void setCurrentDifficulty(String difficulty)
    {
        if (!levels.containsKey(difficulty)) {
            throw new IllegalArgumentException("Difficulty level " + difficulty + " not found.");
        }

        currentDifficulty = difficulty;

        if (Game.curCampaign != null) {
            Game.curCampaign.setCurrentDifficulty(currentDifficulty);
        }
    }

    /**
     * Returns the current difficulty that is active for the campaign
     *
     * @return the current difficulty
     */

    public String getCurrentDifficulty()
    {
        return currentDifficulty;
    }

    /**
     * Returns the set of all available difficulty levels
     *
     * @return the set of all difficulty levels
     */

    public Set<String> getDifficultyLevels()
    {
        return Collections.unmodifiableSet(levels.keySet());
    }

    /**
     * Returns the PC damage factor for the current difficulty level
     *
     * @return the PC damage factor, with 100 being no change in damage
     */

    public int getDamageFactorOnPCs()
    {
        return levels.get(currentDifficulty).damageFactorOnPCs;
    }

    /**
     * Returns true if the current difficulty specifies that PCs can take critical hits,
     * false otherwse
     *
     * @return whether PCs can take critical hits
     */

    public boolean criticalHitsOnPCs()
    {
        return levels.get(currentDifficulty).criticalHitsOnPCs;
    }

    /**
     * Returns true if PCs can recieve friendly fire, false otherwise
     *
     * @return whether PCs can recieve friendly fire
     */

    public boolean friendlyFireOnPCs()
    {
        return levels.get(currentDifficulty).friendlyFireOnPCs;
    }

    private class DifficultyLevel
    {
        private final boolean criticalHitsOnPCs;
        private final boolean friendlyFireOnPCs;
        private final int damageFactorOnPCs;

        private DifficultyLevel(boolean criticalHitsOnPCs, boolean friendlyFireOnPCs, int damageFactorOnPCs)
        {
            this.criticalHitsOnPCs = criticalHitsOnPCs;
            this.friendlyFireOnPCs = friendlyFireOnPCs;
            this.damageFactorOnPCs = damageFactorOnPCs;
        }
    }
}
