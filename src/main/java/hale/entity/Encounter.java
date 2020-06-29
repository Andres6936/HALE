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

package hale.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import hale.Game;
import hale.area.Area;
import hale.loading.JSONOrderedObject;
import hale.loading.ReferenceHandler;
import hale.rules.Currency;
import hale.rules.Faction;
import hale.rules.XP;
import hale.util.Logger;
import hale.util.PointImmutable;
import hale.util.SaveGameUtil;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * A specific instance of an EncounterTemplate
 *
 * @author Jared
 */

public class Encounter implements Iterable<Creature>
{
    private final EncounterTemplate template;

    private Faction faction;

    private final Location location;

    private int lastSpawnRound;

    private List<Creature> creaturesInArea;

    private boolean aiActive;
    private List<Creature> knownHostiles;

    private int currencyToReward;
    private int xpToReward;

    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("name", template.getID());
        data.put("x", location.getX());
        data.put("y", location.getY());
        data.put("lastSpawnTime", lastSpawnRound);
        data.put("faction", faction.getName());

        if (currencyToReward != 0) {
            data.put("currencyToReward", currencyToReward);
        }

        if (xpToReward != 0) {
            data.put("xpToReward", xpToReward);
        }

        // store the list of creatures for this encounter
        List<Object> creaturesData = new ArrayList<Object>();
        for (Creature creature : creaturesInArea) {

            creaturesData.add(SaveGameUtil.getRef(creature));
        }

        if (!creaturesData.isEmpty()) {
            data.put("creatures", creaturesData.toArray());
        }

        return data;
    }

    public static Encounter load(SimpleJSONObject data, ReferenceHandler refHandler, Area parent)
    {
        String name = data.get("name", null);
        int x = data.get("x", 0);
        int y = data.get("y", 0);

        Encounter encounter = Game.curCampaign.getEncounter(name, new Location(parent, x, y));

        encounter.lastSpawnRound = data.get("lastSpawnTime", 0);
        encounter.faction = Game.ruleset.getFaction(data.get("faction", null));

        if (data.containsKey("creatures")) {
            for (SimpleJSONArrayEntry entry : data.getArray("creatures")) {
                String creatureRef = entry.getString();

                Creature creature = (Creature)refHandler.getEntity(creatureRef);

                if (creature == null) {
                    Logger.appendToWarningLog("Error resolving reference for creature " +
                            creatureRef + " when loading encounter");
                    continue;
                }


                creature.setEncounter(encounter);
                creature.setFaction(encounter.faction);

                encounter.creaturesInArea.add(creature);
            }
        }

        if (data.containsKey("currencyToReward")) {
            encounter.currencyToReward = data.get("currencyToReward", 0);
        } else {
            encounter.currencyToReward = 0;
        }

        if (data.containsKey("xpToReward")) {
            encounter.xpToReward = data.get("xpToReward", 0);
        } else {
            encounter.xpToReward = 0;
        }

        return encounter;
    }

    /**
     * Creates a new Encounter
     *
     * @param template the template to base this encounter off of
     * @param location the area and coordinates of this encounter
     */

    public Encounter(EncounterTemplate template, Location location)
    {
        this.template = template;

        this.location = location;

        this.lastSpawnRound = -1;

        this.creaturesInArea = new ArrayList<Creature>();

        this.faction = template.getDefaultFaction();

        this.aiActive = false;
        this.knownHostiles = new ArrayList<Creature>();
    }

    /**
     * Sets the ai active status of all non player characters in this Encounter
     *
     * @param aiActive if true, causes all creatures in this Encounter with AI to become active.  In combat mode, they
     *                 will be given a combat turn and have their AI scripts run.
     */

    public void setAIActive(boolean aiActive)
    {
        this.aiActive = aiActive;
    }

    /**
     * Returns true if creatures in this encounter are AI active, false otherwise
     *
     * @return whether the AI of creatures in this encounter has been activated
     */

    public boolean isAIActive()
    {
        return aiActive;
    }

    /**
     * Checks if the specified hostile is already in the list of hostiles, and adds
     * it if it is not
     *
     * @param hostile
     */

    public void checkAddHostile(Creature hostile)
    {
        for (Creature c : knownHostiles) {
            if (c == hostile) {
                return;
            }
        }

        knownHostiles.add(hostile);
    }

    /**
     * Adds the specified creature to the list of hostiles "known" by this encounter.
     * Creatures in this encounter will have knowledge of this hostile in their AI
     * scripts
     *
     * @param hostile
     */

    public void addHostile(Creature hostile)
    {
        checkAddHostile(hostile);
    }

    /**
     * Adds all the specified creatures as hostiles.  See {@link #addHostile(Creature)}
     *
     * @param hostiles
     */

    public void addHostiles(List<Creature> hostiles)
    {
        for (Creature creature : hostiles) {
            checkAddHostile(creature);
        }
    }

    /**
     * Returns the list of hostile creatures that creatures in this encounter
     * know about
     *
     * @return the list of hostile creatures
     */

    public List<Creature> getHostiles()
    {
        return new ArrayList<Creature>(this.knownHostiles);
    }

    /**
     * Clears the list of known hostiles for this encounter.  See {@link #addHostile(Creature)}
     */

    public void removeAllHostiles()
    {
        this.knownHostiles.clear();
    }


    /**
     * Sets the faction of this encounter.  All creatures in this encounter
     * will also have their faction set
     *
     * @param faction the ID of the faction to set
     */

    public void setFaction(String faction)
    {
        setFaction(Game.ruleset.getFaction(faction));
    }

    /**
     * Sets the faction of this encounter.  All creatures in this encounter
     * will also have their faction set
     *
     * @param faction
     */

    public void setFaction(Faction faction)
    {
        this.faction = faction;

        for (Creature creature : creaturesInArea) {
            creature.setFaction(faction);
        }

        Game.areaListener.getCombatRunner().checkAIActivation();
    }

    /**
     * Returns the template with the immutable parts of this Encounter
     *
     * @return the EncounterTemplate
     */

    public EncounterTemplate getTemplate()
    {
        return template;
    }

    /**
     * Returns the current faction of this encounter
     *
     * @return the faction of this encounter
     */

    public Faction getFaction()
    {
        return faction;
    }

    /**
     * Returns the location that is encounter is centered around
     *
     * @return the location of this encounter
     */

    public Location getLocation()
    {
        return location;
    }

    /**
     * Returns the list of creatures associated with this encounter that have been
     * added to the area.  It is safe to add or remove creatures from this list,
     * although this will change the gold reward associated with the encounter
     * when it is completed
     *
     * @return the list of creatures associated with this encounter
     */

    public List<Creature> getCreaturesInArea()
    {
        return creaturesInArea;
    }

    /**
     * Removes the specified creature from the list of creatures this encounter
     * is tracking in the area
     *
     * @param creature
     */

    public void removeCreatureFromArea(Creature creature)
    {
        creaturesInArea.remove(creature);
    }

    /**
     * Checks to see if this Encounter is in a valid state to spawn creatures.
     * If it is, creatures are added around the location of this Encounter.  If
     * not, no action is taken
     * <p>
     * Encounters that respawn are able to spawn creatures every certain number of
     * hours, while non-respawning encounters can only spawn once
     *
     * @return true if creatures were spawned, false otherwise
     */

    public boolean checkSpawnCreatures()
    {
        if (lastSpawnRound == -1) {
            spawnCreatures();
            return true;
        }

        int roundsSinceSpawn = Game.curCampaign.getDate().getTotalRoundsElapsed() - lastSpawnRound;

        if (template.respawns() && roundsSinceSpawn > template.getRespawnTimeInHours() *
                Game.curCampaign.getDate().ROUNDS_PER_HOUR) {
            spawnCreatures();
            return true;
        }

        return false;
    }

    /**
     * This encounter checks if all creatures have been defeated.  If so, the party of player characters is
     * awarded xp.  Encounters only award XP at most once per spawn.
     *
     * @param combatLength the length of the combat, which modifies the amount of XP awarded
     */

    public void checkAwardXP(int combatLength)
    {
        // all creatures must be defeated
        for (Creature creature : creaturesInArea) {
            if (!creature.isDead()) return;
        }

        // reward currency
        if (currencyToReward != 0) {
            Game.curCampaign.partyCurrency.addValue(currencyToReward);
            Game.mainViewer.addMessage("green", "The party gained " + Currency.shortString(currencyToReward, 100) + ".");
            currencyToReward = 0;
        }

        if (xpToReward != 0) {
            // modify xp based on encounter length
            int lengthModifier = 10000 + Math.min(Game.ruleset.getValue("CombatLengthXPFactor") * combatLength,
                    Game.ruleset.getValue("CombatLengthXPMax"));
            xpToReward = xpToReward * lengthModifier / 10000;

            // reward XP
            XP.addPartyXP(xpToReward);
            xpToReward = 0;
        }

        knownHostiles.clear();
        aiActive = false;
    }

    private void spawnCreatures()
    {
        currencyToReward = 0;

        Area area = location.getArea();
        lastSpawnRound = Game.curCampaign.getDate().getTotalRoundsElapsed();

        // remove any old creatures
        for (int i = creaturesInArea.size() - 1; i >= 0; i--) {
            area.removeEntity(creaturesInArea.get(i));
        }
        creaturesInArea.clear();

        aiActive = false;
        knownHostiles.clear();

        if ((template.randomizesCreatures() && Game.scriptInterface.SpawnRandomEncounters) ||
                !template.randomizesCreatures()) {

            Map<PointImmutable, NPC> spawnedCreatures = template.spawnCreatures(location);

            for (PointImmutable p : spawnedCreatures.keySet()) {
                NPC creature = spawnedCreatures.get(p);
                creature.setEncounter(this);
                creature.setLocation(new Location(area, p.x, p.y));
                creature.setFaction(faction);

                area.getEntities().addEntity(creature);
                creaturesInArea.add(creature);

                currencyToReward += creature.getTemplate().generateReward();
            }
        }

        // now compute XP reward
        if (template.getChallengeRating() != -1) {
            // if template has defined challenge, use that to compute XP
            double EC = (double)template.getChallengeRating() / Game.ruleset.getValue("EncounterChallengeFactor");
            xpToReward = (int)(EC * Game.ruleset.getValue("EncounterXPFactor"));
        } else {
            // if template does not have challenge rating, compute one based on the area
            // creatures and use it to get XP

            double averageLevel = 0.0;
            for (Creature creature : creaturesInArea) {
                averageLevel += (creature.roles.getTotalLevel() / ((float)creaturesInArea.size()));
            }

            double groupScaleFactor = Game.ruleset.getValue("EncounterGroupChallengeScaleFactor") / 1000.0;
            double EC = Math.log(creaturesInArea.size() + 1) * groupScaleFactor * averageLevel;

            xpToReward = (int)(EC * Game.ruleset.getValue("EncounterXPFactor"));
        }
    }

    @Override
    public Iterator<Creature> iterator()
    {
        return creaturesInArea.iterator();
    }
}
