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

package main.java.hale.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.hale.Game;
import main.java.hale.Party;
import main.java.hale.ScriptState;
import main.java.hale.ability.AbilitySlot;
import main.java.hale.ability.ScriptFunctionType;
import main.java.hale.area.Area;
import main.java.hale.area.Transition;
import main.java.hale.entity.CreatedItem;
import main.java.hale.entity.Creature;
import main.java.hale.entity.Encounter;
import main.java.hale.entity.EncounterTemplate;
import main.java.hale.entity.EntityManager;
import main.java.hale.entity.EquippableItemTemplate;
import main.java.hale.entity.Location;
import main.java.hale.entity.PC;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.ReferenceHandler;
import main.java.hale.resource.ResourceManager;
import main.java.hale.resource.ResourceType;
import main.java.hale.resource.Sprite;
import main.java.hale.resource.SpriteManager;
import main.java.hale.tileset.Tileset;
import main.java.hale.util.FileUtil;
import main.java.hale.util.Logger;
import main.java.hale.util.Point;
import main.java.hale.util.PointImmutable;
import main.java.hale.util.SaveGameUtil;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;
import main.java.hale.view.WorldMapPopup;

public class Campaign
{
    private final String id;
    private String startArea;
    private String name;
    private int minPartySize, maxPartySize;
    private int minStartingLevel, maxStartingLevel;
    private boolean allowLevelUp;
    private int minCurrency;
    private String worldMapImage;
    private String startingCharacter;
    private Date date;

    private final RecipeManager recipeManager;
    private final Map<String, Tileset> tilesets;
    private final Map<String, EncounterTemplate> encounterTemplates;
    private final Map<String, CreatedItem> createdItems;
    private final List<Faction.CustomRelationship> customRelationships;
    private final Map<String, Merchant> merchants;
    private final Map<String, Transition> transitions;
    private final Map<String, Area> areas;

    public Party party;
    public final Currency partyCurrency;
    public final List<WorldMapLocation> worldMapLocations;
    public QuestEntryList questEntries;
    public ScriptState scriptState;
    public Area curArea;

    private String currentDifficulty;

    /**
     * Gets a JSONObject with all of the save game data from this campaign
     *
     * @return the JSONObject
     */

    public JSONOrderedObject getSaveGameData()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("comment", "Automatically generated Save file.  Edit at your own risk!");

        data.put("id", id);
        data.put("currentDifficulty", currentDifficulty);
        data.put("date", date.getTotalRoundsElapsed());
        data.put("partyCurrency", partyCurrency.getValue());

        data.put("currentArea", SaveGameUtil.getRef(curArea));

        data.put("party", party.save());

        Object[] areasData = new Object[areas.size()];
        int i = 0;
        for (String areaID : areas.keySet()) {
            areasData[i] = areas.get(areaID).save();
            i++;
        }
        data.put("loadedAreas", areasData);

        if (createdItems.size() > 0) {
            Object[] createdItemsData = new Object[createdItems.size()];
            i = 0;
            for (CreatedItem createdItem : createdItems.values()) {
                createdItemsData[i] = createdItem.save();
                i++;
            }

            data.put("createdItems", createdItemsData);
        }

        List<Object> transitionData = new ArrayList<Object>();
        for (String transitionID : transitions.keySet()) {
            Object transition = transitions.get(transitionID).save();

            if (transition != null) {
                transitionData.add(transition);
            }
        }
        data.put("transitions", transitionData.toArray());

        Object[] merchantData = new Object[merchants.size()];
        i = 0;
        for (String merchantID : merchants.keySet()) {
            merchantData[i] = merchants.get(merchantID).save();
            i++;
        }
        data.put("merchants", merchantData);

        data.put("questEntries", questEntries.save());

        List<Object> locationData = new ArrayList<Object>();
        for (WorldMapLocation location : worldMapLocations) {
            if (location.isRevealed()) {
                locationData.add(location.save());
            }
        }

        if (locationData.size() > 0) {
            data.put("worldMapLocations", locationData.toArray());
        }

        if (!scriptState.isEmpty()) {
            data.put("scriptState", scriptState.save());
        }

        if (customRelationships.size() > 0) {
            i = 0;
            Object[] crData = new Object[customRelationships.size()];
            for (Faction.CustomRelationship cr : customRelationships) {
                crData[i] = cr.save();
                i++;
            }
            data.put("factionRelationships", crData);
        }

        return data;
    }

    public void load(SimpleJSONParser data) throws LoadGameException
    {
        ReferenceHandler refHandler = new ReferenceHandler();

        if (!this.id.equals(data.get("id", null))) {
            throw new LoadGameException("Campaign Save file ID does not match");
        }

        Game.ruleset.getDifficultyManager().setCurrentDifficulty(data.get("currentDifficulty", null));

        date.reset();
        date.incrementRounds(data.get("date", 0));

        partyCurrency.setValue(data.get("partyCurrency", 0));

        this.createdItems.clear();
        if (data.containsKey("createdItems")) {
            for (SimpleJSONArrayEntry entry : data.getArray("createdItems")) {
                CreatedItem createdItem = CreatedItem.load(entry.getObject());

                this.createdItems.put(createdItem.getCreatedItemID(), createdItem);
            }
        }

        areas.clear();
        SimpleJSONArray loadedAreas = data.getArray("loadedAreas");
        for (SimpleJSONArrayEntry entry : loadedAreas) {
            SimpleJSONObject areaData = entry.getObject();

            Area area = Area.load(areaData, refHandler);
            areas.put(area.getID(), area);
        }

        this.party = Party.load(data.getObject("party"), refHandler);

        this.curArea = refHandler.getArea(data.get("currentArea", null));
        // start animations on the current area effects
        curArea.startEffectAnimations();

        // clear existing transitions by reloading them
        this.loadAreaTransitions();
        for (SimpleJSONArrayEntry entry : data.getArray("transitions")) {
            SimpleJSONObject entryData = entry.getObject();

            String name = entryData.get("name", null);

            this.getAreaTransition(name).load(entryData);
        }

        this.merchants.clear();
        for (SimpleJSONArrayEntry entry : data.getArray("merchants")) {
            SimpleJSONObject entryData = entry.getObject();

            String id = entryData.get("id", null);

            this.getMerchant(id).load(entryData);
        }

        this.questEntries = QuestEntryList.load(data.getObject("questEntries"));

        if (data.containsKey("worldMapLocations")) {
            for (SimpleJSONArrayEntry entry : data.getArray("worldMapLocations")) {
                SimpleJSONObject entryData = entry.getObject();

                String name = entryData.get("name", null);
                boolean revealed = entryData.get("revealed", false);

                // find the location with the specified name and set the revealed status
                for (WorldMapLocation location : worldMapLocations) {
                    if (location.getName().equals(name)) {
                        location.setRevealed(revealed);
                    }
                }
            }
        }

        this.scriptState = new ScriptState();
        if (data.containsKey("scriptState")) {
            this.scriptState.load(data.getObject("scriptState"));
        }

        if (data.containsKey("factionRelationships")) {
            for (SimpleJSONArrayEntry entry : data.getArray("factionRelationships")) {
                Faction.CustomRelationship cr = Faction.CustomRelationship.load(entry.getObject());
                cr.setFactionRelationships();
                addCustomRelationship(cr);
            }
        }

        refHandler.resolveAllReferences();
    }

    public void readCampaignFile()
    {
        SimpleJSONParser parser = new SimpleJSONParser("campaign" + ResourceType.JSON.getExtension());

        name = parser.get("name", null);
        startArea = parser.get("startArea", null);

        if (parser.containsKey("worldMapBackground")) {
            worldMapImage = parser.get("worldMapBackground", null);
        } else {
            worldMapImage = null;
        }

        if (parser.containsKey("startingCharacter")) {
            startingCharacter = parser.get("startingCharacter", null);
        } else {
            startingCharacter = null;
        }

        SimpleJSONObject partySizeIn = parser.getObject("partySize");
        minPartySize = partySizeIn.get("min", 0);
        maxPartySize = partySizeIn.get("max", 0);

        SimpleJSONObject startingLevelIn = parser.getObject("startingLevel");
        minStartingLevel = startingLevelIn.get("min", 0);
        maxStartingLevel = startingLevelIn.get("max", 0);

        if (parser.containsKey("allowLevelUp")) {
            allowLevelUp = parser.get("allowLevelUp", false);
        } else {
            allowLevelUp = false;
        }

        if (parser.containsKey("minCurrency")) {
            minCurrency = parser.get("minCurrency", 0);
        } else {
            minCurrency = 0;
        }

        SimpleJSONObject timeIn = parser.getObject("timeConversions");
        int roundsPerMinute = timeIn.get("roundsPerMinute", 10);
        int minutesPerHour = timeIn.get("minutesPerHour", 60);
        int hoursPerDay = timeIn.get("hoursPerDay", 24);
        int daysPerMonth = timeIn.get("daysPerMonth", 30);
        date = new Date(roundsPerMinute, minutesPerHour, hoursPerDay, daysPerMonth);

        SimpleJSONArray locationsIn = parser.getArray("locations");
        for (SimpleJSONArrayEntry entry : locationsIn) {
            worldMapLocations.add(new WorldMapLocation(entry.getObject()));
        }

        parser.warnOnUnusedKeys();

        loadAreaTransitions();
        loadRecipes();
        loadTilesets();

        if (startArea != null) {
            curArea = getArea(startArea);
        } else {
            Logger.appendToErrorLog("No start area defined for campaign: " + getID());
        }
    }


    public Campaign(String id)
    {
        this.id = id;
        name = "";
        areas = new HashMap<String, Area>();
        transitions = new HashMap<String, Transition>();
        party = new Party();
        encounterTemplates = new HashMap<String, EncounterTemplate>();
        merchants = new HashMap<String, Merchant>();
        recipeManager = new RecipeManager();
        partyCurrency = new Currency();

        questEntries = new QuestEntryList();
        worldMapLocations = new ArrayList<WorldMapLocation>();

        createdItems = new HashMap<String, CreatedItem>();
        tilesets = new HashMap<String, Tileset>();

        scriptState = new ScriptState();

        customRelationships = new ArrayList<Faction.CustomRelationship>();
    }

    public void addCustomRelationship(Faction.CustomRelationship cr)
    {
        customRelationships.add(cr);
    }

    public Collection<CreatedItem> getCreatedItems()
    {
        return createdItems.values();
    }

    public CreatedItem getCreatedItem(String createdItemID)
    {
        return createdItems.get(createdItemID);
    }

    public void addCreatedItem(CreatedItem createdItem)
    {
        this.createdItems.put(createdItem.getCreatedItemID(), createdItem);
    }

    public int getBestPartySkillCheck(String skillID)
    {
        int modifier = getBestPartySkillModifier(skillID);

        return modifier + Game.dice.d100();
    }

    public int getBestPartySkillRanks(String skillID)
    {
        int bestRanks = -1;

        for (Creature c : party) {
            int currentRanks = c.skills.getRanks(skillID);

            if (currentRanks > bestRanks) {
                bestRanks = currentRanks;
            }
        }

        return bestRanks;
    }

    public int getBestPartySkillModifier(String skillID)
    {
        int bestModifier = 0;

        for (Creature creature : party) {
            int currentModifier = creature.skills.getTotalModifier(skillID);
            if (currentModifier > bestModifier) bestModifier = currentModifier;
        }

        return bestModifier;
    }

    /**
     * Initializes the Player Character "party" (group of playable characters) for
     * this Campaign.  If this Campaign has a specified starting character
     * (if @link {@link #getStartingCharacter()} returns non-null) then the starting
     * character returned by getStartingCharacter is added to the party. Otherwise,
     * all of the characters specified by the given list of IDs is added.
     *
     * @param characterIDs the list of character IDs to be added to the party for this
     * @param name         the name of the party
     *                     Campaign if the starting character is null.
     */

    public void addParty(List<String> characterIDs, String name)
    {
        int minLevelXP = XP.getPointsForLevel(minStartingLevel);

        if (startingCharacter != null) {
            PC pc = EntityManager.getPC(startingCharacter);

            // auto level up the PC to min level if allowed by this campaign
            if (this.allowLevelUp) {
                if (pc.getExperiencePoints() < minLevelXP) {
                    pc.addExperiencePoints(minLevelXP - pc.getExperiencePoints());
                }
            }

            party.add(pc);

        } else {
            for (String id : characterIDs) {
                PC pc = EntityManager.getPC(id);
                party.add(pc);
            }
        }

        party.setName(name);
    }

    /**
     * Adds the specified characters directly to the party, bypassing any campaign
     * default starting character
     *
     * @param characters the characters to add
     * @param name       the name of the party
     */

    public void addPartyCreatures(List<PC> characters, String name)
    {
        for (PC creature : characters) {
            party.add(creature);
        }

        party.setName(name);
    }

    public void checkEncounterRespawns()
    {
        for (Area area : areas.values()) {
            area.checkEncounterRespawns();
        }
    }

    public void transition(String transitionID)
    {
        Transition transition = this.getAreaTransition(transitionID);

        transition(transition, false);
    }

    public void transition(Transition transition, boolean isFromWorldMap)
    {
        // reveal world map location if it is not already revealed
        WorldMapLocation location = Game.curCampaign.getWorldMapLocation(transition.getWorldMapLocation());
        if (location != null) {
            location.setRevealed(true);
        }

        // run current area onExit script
        curArea.runOnAreaExit(transition);

        // get the appropriate end point
        Transition.EndPoint endPoint;
        if (isFromWorldMap) {
            endPoint = transition.getEndPointForWorldMap();
        } else {
            endPoint = transition.getEndPointForCreaturesInCurrentArea();
        }

        if (endPoint == null) {
            throw new IllegalArgumentException("Error transitioning with " +
                    transition.getID() + ". No match for area found.");
        }

        if (endPoint.isWorldMap()) {
            new WorldMapPopup(Game.mainViewer, transition).openPopupCentered();
            return;
        }

        Creature mainMover = Game.curCampaign.party.getSelected();

        List<AbilitySlot> canceledAuraSlots = new ArrayList<AbilitySlot>();

        Iterator<Creature> partyIter = Game.curCampaign.party.allCreaturesIterator();
        while (partyIter.hasNext()) {
            Creature current = partyIter.next();

            canceledAuraSlots.addAll(current.abilities.cancelAllAuras());

            curArea.getEntities().removeEntity(current);
        }

        getTileset(curArea.getTileset()).freeTiles();

        // now we switch over to the new area
        curArea = getArea(endPoint.getAreaID());
        curArea.runOnAreaLoad(transition);

        Iterator<PointImmutable> destinationPositionsIter = endPoint.getPartyPositionsIterator();

        mainMover.setLocation(new Location(curArea, destinationPositionsIter.next()));
        curArea.getEntities().addEntity(mainMover);

        partyIter = Game.curCampaign.party.allCreaturesIterator();
        while (partyIter.hasNext()) {
            Creature current = partyIter.next();

            // we have already added this creature
            if (current == mainMover) continue;

            if (destinationPositionsIter.hasNext()) {
                current.setLocation(new Location(curArea, destinationPositionsIter.next()));
                curArea.getEntities().addEntity(current);
            } else {
                // if there are no more explicit transition locations, just add the creature
                // wherever there is a nearby space
                Point newPosition = Game.scriptInterface.ai.findClosestEmptyTile(mainMover.getLocation().toPoint(), 3);

                if (newPosition == null) {
                    Logger.appendToErrorLog("Unable to find transition space for " + current.getName() +
                            "in " + transition.getID());
                } else {
                    current.setLocation(new Location(curArea, newPosition));
                    curArea.getEntities().addEntity(current);
                }
            }
        }

        Game.mainViewer.addMessage("red", "Entered area " + Game.curCampaign.curArea.getName());

        if (Game.isInTurnMode()) {
            Game.areaListener.getCombatRunner().exitCombat();
        }

        Game.areaViewer.setArea(curArea);
        Game.areaListener.setArea(curArea);

        curArea.setEntityVisibility();

        Game.areaListener.nextTurn();

        Game.areaListener.getCombatRunner().checkAIActivation();

        Game.areaViewer.scrollToCreature(Game.curCampaign.party.getSelected());

        for (AbilitySlot slot : canceledAuraSlots) {
            // reactivate cancelable mode ability slots
            // that were canceled due to auras being removed
            slot.getAbility().executeFunction(ScriptFunctionType.onReactivate, slot);
        }
    }

    public WorldMapLocation getWorldMapLocation(String ref)
    {
        for (WorldMapLocation location : worldMapLocations) {
            if (location.getName().equals(ref)) return location;
        }

        return null;
    }

    public Encounter getEncounter(String encounterID, Location location)
    {
        EncounterTemplate template = encounterTemplates.get(encounterID);

        if (template == null) {
            String resource = "encounters/" + encounterID;

            SimpleJSONParser parser = new SimpleJSONParser(resource, ResourceType.JSON);

            template = new EncounterTemplate(encounterID, parser);
            encounterTemplates.put(encounterID, template);
        }

        return new Encounter(template, location);
    }

    public void removeArea(String id)
    {
        areas.remove(id);
    }

    public Area getArea(String ref)
    {
        Area area = areas.get(ref);

        if (area == null) {
            try {
                area = new Area(ref);
                areas.put(ref, area);
            } catch (Exception e) {
                Logger.appendToErrorLog("Error loading area " + ref, e);
            }
        }

        return area;
    }

    public void loadAreaTransitions()
    {
        transitions.clear();

        Set<String> resources = ResourceManager.getResourcesInDirectory("transitions");
        for (String resource : resources) {

            String id = ResourceManager.getResourceID(resource, "transitions", ResourceType.JSON);
            if (id == null) continue;

            getAreaTransition(id);
        }
    }

    private void loadTilesets()
    {
        for (String resource : ResourceManager.getResourcesInDirectory("tilesets")) {
            String relativePath = FileUtil.getRelativePath("tilesets", resource);
            if (relativePath.contains("/") || relativePath.contains(File.separator)) {
                // only parse resources at the top level, lower level resources are spritesheets
                continue;
            }

            String id = ResourceManager.getResourceID(resource, "tilesets", ResourceType.JSON);
            if (id == null) continue;

            Tileset tileset = new Tileset(id, resource);
            tilesets.put(id, tileset);
        }
    }

    public void loadRecipes()
    {
        recipeManager.loadRecipes();
    }

    public Transition getAreaTransition(String ref)
    {
        Transition transition = transitions.get(ref);

        if (transition == null) {
            transition = new Transition(ref);
            transitions.put(ref, transition);
        }

        return transition;
    }

    public Merchant getMerchant(String name)
    {
        Merchant merchant = merchants.get(name);

        if (merchant == null) {
            merchant = new Merchant(name);
            if (merchant != null) {
                merchants.put(name, merchant);
            }
        }

        return merchant;
    }

    public Recipe getRecipe(String id)
    {
        return recipeManager.getRecipe(id);
    }

    public void setCurrentDifficulty(String difficulty)
    {
        this.currentDifficulty = difficulty;
    }

    public Sprite getWorldMapSprite()
    {
        if (worldMapImage == null) return null;

        return SpriteManager.getSpriteAnyExtension(worldMapImage);
    }

    public String getWorldMapImage()
    {
        return worldMapImage;
    }

    public String getStartArea()
    {
        return startArea;
    }

    public Tileset getTileset(String id)
    {
        return tilesets.get(id);
    }

    public List<String> getRecipeIDsForSkill(Skill skill)
    {
        return recipeManager.getRecipeIDsForSkill(skill);
    }

    public List<String> getEnchantmentsForItemType(EquippableItemTemplate.Type type)
    {
        return recipeManager.getEnchantmentsForItemType(type);
    }

    public Collection<Transition> getAreaTransitions()
    {
        return transitions.values();
    }

    public String getStartingCharacter()
    {
        return startingCharacter;
    }

    public Currency getPartyCurrency()
    {
        return partyCurrency;
    }

    public int getMinPartySize()
    {
        return minPartySize;
    }

    public int getMaxPartySize()
    {
        return maxPartySize;
    }

    public int getMinStartingLevel()
    {
        return minStartingLevel;
    }

    public int getMaxStartingLevel()
    {
        return maxStartingLevel;
    }

    public Date getDate()
    {
        return date;
    }

    public String getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Returns true if characters can be created below min level and then given
     * xp to reach that level
     *
     * @return true if characters can be auto leveled from below the min level
     */

    public boolean allowLevelUp()
    {
        return allowLevelUp;
    }

    /**
     * Returns the minimum amount of currency a party can start with in units of CP over 100.  if a party
     * has less than this; it is given this amount
     *
     * @return
     */

    public int getMinCurrency()
    {
        return minCurrency;
    }

    /**
     * If the campaign options allow party members to level up to the min level automatically,
     * do so
     */

    public void levelUpToMinIfAllowed()
    {
        if (allowLevelUp()) {
            int xp = XP.getPointsForLevel(getMinStartingLevel());

            for (PC pc : party) {
                int pcXP = pc.getExperiencePoints();

                pc.addExperiencePoints(Math.max((xp - pcXP), 0));
            }
        }
    }
}
