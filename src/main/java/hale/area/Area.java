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

package main.java.hale.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.java.hale.Game;
import main.java.hale.ability.AreaEffectList;
import main.java.hale.ability.Aura;
import main.java.hale.ability.Effect;
import main.java.hale.ability.EffectTarget;
import main.java.hale.bonus.Bonus;
import main.java.hale.entity.Container;
import main.java.hale.entity.Creature;
import main.java.hale.entity.Door;
import main.java.hale.entity.Encounter;
import main.java.hale.entity.Entity;
import main.java.hale.entity.EntityManager;
import main.java.hale.entity.Item;
import main.java.hale.entity.Location;
import main.java.hale.entity.Openable;
import main.java.hale.entity.PC;
import main.java.hale.entity.Trap;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.ReferenceHandler;
import main.java.hale.loading.Saveable;
import main.java.hale.resource.ResourceType;
import main.java.hale.tileset.AreaElevationGrid;
import main.java.hale.tileset.AreaTileGrid;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Logger;
import main.java.hale.util.Point;
import main.java.hale.util.PointImmutable;
import main.java.hale.util.SaveGameUtil;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

public class Area implements EffectTarget, Saveable
{
    private final List<String> transitions;
    private final int width, height;
    private final int visibilityRadius;
    private final String tileset;
    private final boolean[][] passable;
    private final boolean[][] transparency;
    private final boolean[][] visibility;
    private final AreaElevationGrid elevation;
    private final AreaTileGrid tileGrid;
    private final List<PointImmutable> startLocations;

    private final AreaEntityList entityList;
    private final AreaEffectList effects;
    private final List<Encounter> encounters;
    private final Map<String, Trigger> triggers;
    private final String id, name;
    private final boolean isExplored;
    private final boolean[][] explored;

    private AreaUtil areaUtil;
    private Procedural procedural;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("ref", SaveGameUtil.getRef(this));
        data.put("name", id);

        if (procedural != null) {
            long seed = procedural.getSeed();
            String seedStr = Long.toHexString(seed);
            data.put("generatorSeed", seedStr);
        }

        // write out the explored matrix
        List<Object> exp = new ArrayList<Object>();
        for (int x = 0; x < explored.length; x++) {
            for (int y = 0; y < explored[0].length; y++) {
                if (explored[x][y]) {
                    // write this as a JSON formated object, but with multiple
                    // entries per line
                    exp.add(Integer.toString(x) + ',' + Integer.toString(y));
                }
            }
        }
        data.put("explored", exp.toArray());

        Object[] encounterData = new Object[encounters.size()];
        for (int i = 0; i < encounterData.length; i++) {
            encounterData[i] = encounters.get(i).save();
        }
        data.put("encounters", encounterData);

        List<Object> triggerData = new ArrayList<Object>();
        for (String triggerID : triggers.keySet()) {
            Object trigger = triggers.get(triggerID).save();

            if (trigger != null) {
                triggerData.add(trigger);
            }
        }
        data.put("triggers", triggerData.toArray());

        if (effects.size() > 0) {
            data.put("effects", effects.save());
        }

        data.put("entities", entityList.save());

        return data;
    }

    /**
     * Creates a json object representing the data used for loading this area in a campaign.  this is the
     * base data of the area, not the save game area created from {@link #save()}
     *
     * @return the json data
     */

    public JSONOrderedObject writeToJSON()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("name", name);
        data.put("width", width);
        data.put("height", height);
        data.put("visibilityRadius", visibilityRadius);
        data.put("explored", isExplored);
        data.put("tileset", tileset);

        // write start locations
        List<int[]> startLoc = new ArrayList<int[]>();
        for (PointImmutable p : startLocations) {
            int[] coords = new int[2];
            coords[0] = p.x;
            coords[1] = p.y;
            startLoc.add(coords);
        }
        data.put("startLocations", startLoc.toArray());

        // write creatures
        List<JSONOrderedObject> creaturesData = new ArrayList<JSONOrderedObject>();
        for (Creature creature : entityList.getAllCreatures()) {
            JSONOrderedObject creatureData = new JSONOrderedObject();
            creatureData.put("id", creature.getTemplate().getID());
            creatureData.put("x", creature.getLocation().getX());
            creatureData.put("y", creature.getLocation().getY());

            if (creature instanceof PC) {
                creatureData.put("pc", true);
            }

            creaturesData.add(creatureData);
        }
        data.put("creatures", creaturesData.toArray());

        // write encounter coords
        Map<String, ArrayList<int[]>> encountersData = new LinkedHashMap<String, ArrayList<int[]>>();

        for (Encounter encounter : encounters) {
            int[] coords = new int[2];
            coords[0] = encounter.getLocation().getX();
            coords[1] = encounter.getLocation().getY();

            if (!encountersData.containsKey(encounter.getTemplate().getID())) {
                encountersData.put(encounter.getTemplate().getID(), new ArrayList<int[]>());
            }

            encountersData.get(encounter.getTemplate().getID()).add(coords);
        }

        JSONOrderedObject encountersOut = new JSONOrderedObject();
        for (String id : encountersData.keySet()) {
            encountersOut.put(id, encountersData.get(id).toArray());
        }

        data.put("encounters", encountersOut);

        // write containers
        List<JSONOrderedObject> containersData = new ArrayList<JSONOrderedObject>();
        for (Container container : entityList.getAllContainers()) {
            JSONOrderedObject containerData = new JSONOrderedObject();
            containerData.put("id", container.getTemplate().getID());
            containerData.put("x", container.getLocation().getX());
            containerData.put("y", container.getLocation().getY());

            containersData.add(containerData);
        }
        data.put("containers", containersData.toArray());

        // write doors
        List<JSONOrderedObject> doorsData = new ArrayList<JSONOrderedObject>();
        for (Door door : entityList.getAllDoors()) {
            JSONOrderedObject doorData = new JSONOrderedObject();
            doorData.put("id", door.getTemplate().getID());
            doorData.put("x", door.getLocation().getX());
            doorData.put("y", door.getLocation().getY());

            doorsData.add(doorData);
        }
        data.put("doors", doorsData.toArray());

        // write triggers
        JSONOrderedObject triggersData = new JSONOrderedObject();
        for (String id : triggers.keySet()) {
            JSONOrderedObject triggerData = new JSONOrderedObject();

            Trigger trigger = triggers.get(id);

            triggerData.put("script", trigger.getScript().getScriptLocation());

            List<PointImmutable> points = trigger.getPoints();
            if (points.size() > 0) {
                List<int[]> pointsData = new ArrayList<int[]>();

                for (PointImmutable p : points) {
                    int[] coords = new int[2];
                    coords[0] = p.x;
                    coords[1] = p.y;

                    pointsData.add(coords);
                }

                triggerData.put("points", pointsData.toArray());
            }

            triggersData.put(id, triggerData);
        }

        data.put("triggers", triggersData);

        // write layers
        data.put("layers", tileGrid.writeToJSON());

        // write transparency
        int[][] transparencyData = new int[transparency[0].length][transparency.length];
        for (int x = 0; x < transparency.length; x++) {
            for (int y = 0; y < transparency[x].length; y++) {
                transparencyData[y][x] = transparency[x][y] ? 0 : 1;
            }
        }
        data.put("transparencyGrid", transparencyData);

        // write passability
        int[][] passabilityData = new int[passable[0].length][passable.length];
        for (int x = 0; x < passable.length; x++) {
            for (int y = 0; y < passable[x].length; y++) {
                passabilityData[y][x] = passable[x][y] ? 1 : 0;
            }
        }
        data.put("passabilityGrid", passabilityData);

        // write elevation
        data.put("elevationGrid", elevation.writeToJSON());

        return data;
    }

    /**
     * Loads an area from the specified saved JSON data
     *
     * @param data
     * @param refHandler
     * @return the newly loaded area
     * @throws LoadGameException
     */

    public static Area load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException
    {
        return new Area(data.get("name", null), data, refHandler);
    }

    /**
     * Creates a new area with the specified ID.  The area is parsed from the appropriate resource file
     * ("areas/" + id + ".json")
     *
     * @param id the Area id and name
     * @throws LoadGameException
     */

    public Area(String id) throws LoadGameException
    {
        this(id, null, null);
    }

    /*
     * Creates a new Area.  If the loadedData is non-null, then the area will be partially parsed
     * from the base resource, and then the remainder will be loaded from the save game data
     */

    private Area(String id, SimpleJSONObject loadedData, ReferenceHandler refHandler) throws LoadGameException
    {
        this.id = id;

        SimpleJSONParser parser = new SimpleJSONParser("areas/" + id + ResourceType.JSON.getExtension());

        if (parser.containsKey("name")) {
            this.name = parser.get("name", null);
        } else {
            this.name = id;
        }

        this.width = parser.get("width", 0);
        this.height = parser.get("height", 0);
        this.tileset = parser.get("tileset", null);
        this.visibilityRadius = parser.get("visibilityRadius", 0);
        this.isExplored = parser.get("explored", false);

        // initialize matrices
        explored = new boolean[width][height];
        transparency = new boolean[width][height];
        elevation = new AreaElevationGrid(width, height);
        entityList = new AreaEntityList(width, height);
        effects = new AreaEffectList(this);
        tileGrid = new AreaTileGrid(Game.curCampaign.getTileset(tileset), width, height);
        passable = new boolean[width][height];
        visibility = new boolean[width][height];

        if (parser.get("explored", false)) {
            AreaUtil.setMatrix(explored, true);
        }

        // parse start locations
        if (parser.containsKey("startLocations")) {
            startLocations = new ArrayList<PointImmutable>();

            SimpleJSONArray startLocationsIn = parser.getArray("startLocations");

            for (SimpleJSONArrayEntry entry : startLocationsIn) {
                SimpleJSONArray locationIn = entry.getArray();
                Iterator<SimpleJSONArrayEntry> iter = locationIn.iterator();

                int x = iter.next().getInt(0);
                int y = iter.next().getInt(0);

                startLocations.add(new PointImmutable(x, y));
            }
        } else {
            startLocations = Collections.emptyList();
        }

        // parse triggers
        if (parser.containsKey("triggers")) {
            triggers = new HashMap<String, Trigger>();

            SimpleJSONObject triggersIn = parser.getObject("triggers");
            for (String triggerID : triggersIn.keySet()) {
                triggers.put(triggerID, new Trigger(triggerID, triggersIn.getObject(triggerID)));
            }
        } else {
            triggers = Collections.emptyMap();
        }

        if (parser.containsKey("procedural")) {
            procedural = new Procedural(this, parser.getObject("procedural"));
        }

        int x, y;

        // parse transparency
        SimpleJSONArray transparencyIn = parser.getArray("transparencyGrid");
        y = 0;
        for (SimpleJSONArrayEntry entry : transparencyIn) {
            SimpleJSONArray rowIn = entry.getArray();

            x = 0;
            for (SimpleJSONArrayEntry rowEntry : rowIn) {
                int value = rowEntry.getInt(0);
                this.transparency[x][y] = (value == 0 ? true : false);
                x++;
            }

            y++;
        }

        // parse passability
        SimpleJSONArray passabilityIn = parser.getArray("passabilityGrid");
        y = 0;
        for (SimpleJSONArrayEntry entry : passabilityIn) {
            SimpleJSONArray rowIn = entry.getArray();

            x = 0;
            for (SimpleJSONArrayEntry rowEntry : rowIn) {
                int value = rowEntry.getInt(0);
                this.passable[x][y] = (value == 1 ? true : false);
                x++;
            }

            y++;
        }

        // parse elevation
        SimpleJSONArray elevationIn = parser.getArray("elevationGrid");
        y = 0;
        for (SimpleJSONArrayEntry entry : elevationIn) {
            SimpleJSONArray rowIn = entry.getArray();

            x = 0;
            for (SimpleJSONArrayEntry rowEntry : rowIn) {
                int value = rowEntry.getInt(0);
                this.elevation.setElevation(x, y, (byte)value);
                x++;
            }

            y++;
        }

        // parse tiles
        SimpleJSONObject layersIn = parser.getObject("layers");
        for (String layerID : layersIn.keySet()) {
            SimpleJSONObject layerIn = layersIn.getObject(layerID);

            for (String tileID : layerIn.keySet()) {
                SimpleJSONArray tilePositionsIn = layerIn.getArray(tileID);
                for (SimpleJSONArrayEntry entry : tilePositionsIn) {
                    SimpleJSONArray positionIn = entry.getArray();
                    Iterator<SimpleJSONArrayEntry> iter = positionIn.iterator();

                    int xPosition = iter.next().getInt(0);
                    int yPosition = iter.next().getInt(0);

                    this.tileGrid.addTile(tileID, layerID, xPosition, yPosition);
                }
            }
        }

        encounters = new ArrayList<Encounter>();

        if (loadedData != null) {
            // we are loading from a saved game file
            loadFromSavedFile(loadedData, refHandler);
            // don't warn on unused keys as some are not read
        } else {
            loadFromBaseFile(parser);
            parser.warnOnUnusedKeys();
        }

        // update door transparency
        for (Entity entity : entityList) {
            if (entity instanceof Door) {
                Door door = (Door)entity;

                if (!door.isTransparent()) {
                    transparency[door.getLocation().getX()][door.getLocation().getY()] = false;
                } else {
                    transparency[door.getLocation().getX()][door.getLocation().getY()] = true;
                }
            }
        }

        // get the list of transitions associated with this area
        transitions = new ArrayList<String>();
        for (Transition transition : Game.curCampaign.getAreaTransitions()) {
            if (transition.isFromArea(this) ||
                    (transition.isTwoWay() && transition.isToArea(this))) {
                this.transitions.add(transition.getID());
            }
        }

        if (loadedData == null) {
            // if this is not loaded from a saved game, spawn encounters
            for (Encounter encounter : encounters) {
                encounter.checkSpawnCreatures();
            }
        }

        if (procedural != null) {
            // perform procedural generation if specified
            procedural.generateLayers();
        }
    }

    // loads the data in the JSON as saved file data

    private void loadFromSavedFile(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException
    {
        refHandler.add(data.get("ref", null), this);

        // parse explored data
        SimpleJSONArray exploredArray = data.getArray("explored");
        for (SimpleJSONArrayEntry entry : exploredArray) {
            String exploredString = entry.getString();

            String[] coords = exploredString.split(",");

            if (coords.length != 2) {
                Logger.appendToErrorLog("Error parsing explored entry " + exploredString);
            } else {
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);

                explored[x][y] = true;
            }
        }

        if (data.containsKey("generatorSeed")) {
            String seedHex = data.get("generatorSeed", null);
            long seed = Long.parseLong(seedHex, 16);
            procedural.setSeed(seed);
        }

        // parse entities
        entityList.load(data.getObject("entities"), this, refHandler);

        if (data.containsKey("effects")) {
            effects.load(data.getArray("effects"), refHandler);
        }

        for (SimpleJSONArrayEntry entry : data.getArray("encounters")) {
            SimpleJSONObject entryObject = entry.getObject();

            Encounter encounter = Encounter.load(entryObject, refHandler, this);

            encounters.add(encounter);
        }

        for (SimpleJSONArrayEntry entry : data.getArray("triggers")) {
            SimpleJSONObject entryData = entry.getObject();

            String id = entryData.get("id", null);

            triggers.get(id).load(entryData);
        }
    }

    // loads the parts of the area that are instead loaded from the save game file
    // when saving / loading

    private void loadFromBaseFile(SimpleJSONParser parser)
    {
        // parse creatures
        if (parser.containsKey("creatures")) {
            SimpleJSONArray creaturesIn = parser.getArray("creatures");

            for (SimpleJSONArrayEntry entry : creaturesIn) {
                SimpleJSONObject obj = entry.getObject();

                String id = obj.get("id", null);
                int x = obj.get("x", 0);
                int y = obj.get("y", 0);

                boolean isPC = false;
                if (obj.containsKey("pc")) {
                    isPC = obj.get("pc", false);
                }

                Creature creature;
                if (isPC) {
                    creature = EntityManager.getPC(id);
                } else {
                    creature = EntityManager.getNPC(id);
                }
                creature.setLocation(this, x, y);
                entityList.addEntity(creature);
            }
        }

        // parse encounters
        if (parser.containsKey("encounters")) {
            SimpleJSONObject encountersIn = parser.getObject("encounters");
            for (String encounterID : encountersIn.keySet()) {
                SimpleJSONArray encounterIn = encountersIn.getArray(encounterID);

                for (SimpleJSONArrayEntry entry : encounterIn) {
                    SimpleJSONArray coordsIn = entry.getArray();
                    Iterator<SimpleJSONArrayEntry> iter = coordsIn.iterator();

                    int positionX = iter.next().getInt(0);
                    int positionY = iter.next().getInt(0);

                    // add one encounter for each set of coordinates
                    encounters.add(Game.curCampaign.getEncounter(encounterID, new Location(this, positionX, positionY)));
                }
            }
        }

        // parse containers
        if (parser.containsKey("containers")) {
            SimpleJSONArray containersIn = parser.getArray("containers");

            for (SimpleJSONArrayEntry entry : containersIn) {
                SimpleJSONObject containerIn = entry.getObject();

                Container container = EntityManager.getContainer(containerIn.get("id", null));
                container.setLocation(this, containerIn.get("x", 0), containerIn.get("y", 0));
                this.entityList.addContainer(container);
            }
        }

        // parse doors
        if (parser.containsKey("doors")) {
            SimpleJSONArray doorsIn = parser.getArray("doors");

            for (SimpleJSONArrayEntry entry : doorsIn) {
                SimpleJSONObject doorIn = entry.getObject();

                Door door = EntityManager.getDoor(doorIn.get("id", null));
                door.setLocation(this, doorIn.get("x", 0), doorIn.get("y", 0));
                this.entityList.addEntity(door);
            }
        }

        // parse items - note that this must be done after containers to add the items
        // correctly
        if (parser.containsKey("items")) {
            SimpleJSONArray itemsIn = parser.getArray("items");

            for (SimpleJSONArrayEntry entry : itemsIn) {
                SimpleJSONObject itemIn = entry.getObject();

                int quantity = itemIn.get("quantity", 1);

                Item item;
                if (itemIn.containsKey("quality")) {
                    item = EntityManager.getItem(itemIn.get("id", null), itemIn.get("quality", null));
                } else {
                    item = EntityManager.getItem(itemIn.get("id", null));
                }

                item.setLocation(this, itemIn.get("x", 0), itemIn.get("y", 0));

                if (item instanceof Trap) {
                    // arm traps added directly to the area
                    ((Trap)item).setFaction(Game.ruleset.getFaction("Hostile"));
                    this.placeTrap((Trap)item);
                } else {
                    this.addItem(item, quantity);
                }
            }
        }
    }

    /**
     * Returns true if this point is inside the bounds of this area, false otherwise
     *
     * @param p the point to test
     * @return whether the point is valid for this area
     */

    public boolean isValidPoint(Point p)
    {
        if (p.x < 0 || p.y < 0) return false;

        if (p.x >= this.width || p.y >= this.height) return false;

        return true;
    }

    /**
     * Gets the AreaUtil for this area.  If the AreaUtil does not exist, it is created
     *
     * @return the AreaUtil for this area
     */

    public AreaUtil getUtil()
    {
        if (areaUtil == null) {
            areaUtil = new AreaUtil(this);
        }

        return areaUtil;
    }

    public void runOnAreaLoad(Transition transition)
    {
        for (Trigger trigger : triggers.values()) {
            trigger.checkOnAreaLoad(transition);
        }
    }

    public void runOnAreaExit(Transition transition)
    {
        for (Trigger trigger : triggers.values()) {
            trigger.checkOnAreaExit(transition);
        }
    }

    public void checkPlayerMoved(Entity entity)
    {
        for (Trigger trigger : triggers.values()) {
            trigger.checkPlayerMoved(entity);
        }
    }

    public void checkEncounterRespawns()
    {
        for (Encounter encounter : encounters) {
            encounter.checkSpawnCreatures();
        }
    }

    public String getTileset()
    {
        return tileset;
    }

    public String getName()
    {
        return name;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public List<String> getTransitions()
    {
        return transitions;
    }

    public AreaEntityList getEntities()
    {
        return entityList;
    }

    public AreaTileGrid getTileGrid()
    {
        return tileGrid;
    }

    public AreaElevationGrid getElevationGrid()
    {
        return elevation;
    }

    public List<Encounter> getEncounters()
    {
        return encounters;
    }

    public int getVisibilityRadius()
    {
        return visibilityRadius;
    }

    public boolean[][] getExplored()
    {
        return explored;
    }

    public boolean[][] getTransparency()
    {
        return transparency;
    }

    public boolean[][] getPassability()
    {
        return passable;
    }

    public boolean[][] getVisibility()
    {
        return visibility;
    }

    public void applyEffect(Effect effect, List<Point> points)
    {
        if (!(effect instanceof Aura)) {
            effect.setTarget(this);
        }

        effects.add(effect, points);
    }

    @Override
    public int getSpellResistance()
    {
        return 0;
    }

    @Override
    public void removeEffect(Effect effect)
    {
        effects.remove(effect);
    }

    @Override
    public boolean isValidEffectTarget()
    {
        return true;
    }

    public List<Creature> getAffectedCreatures(Effect effect)
    {
        return effects.getAffectedCreatures(effect, this.entityList);
    }

    /**
     * Starts any animations on all effects in this area's effect list,
     * and also animations on all contained creatures' effects lists
     */

    public void startEffectAnimations()
    {
        effects.startAnimations();

        entityList.startEffectAnimations();
    }

    public List<Effect> getEffectsAt(int x, int y)
    {
        return effects.getEffectsAt(x, y);
    }

    public void moveAura(Aura aura, List<Point> points)
    {
        effects.move(aura, points);
    }

    public int getMovementBonus(Point p)
    {
        return getMovementBonus(p.x, p.y);
    }

    public int getMovementBonus(int x, int y)
    {
        return effects.getBonusAt(Bonus.Type.Movement, x, y);
    }

    public boolean isSilenced(int x, int y)
    {
        return effects.hasBonusAt(Bonus.Type.Silence, x, y);
    }

    public boolean isSilenced(Point p)
    {
        return isSilenced(p.x, p.y);
    }

    private int getConcealment(Creature attacker, Creature defender, int x, int y)
    {
        int concealment = 0;
        int obstructionsInPathConcealment = 0;

        Point from = attacker.getLocation().getCenteredScreenPoint();
        Point to = AreaUtil.convertGridToScreenAndCenter(x, y);

        if (from.x == to.x && from.y == to.y) return 0;

        // note that this list will include the defender's position but will not include the attacker's position.
        // So, concealment on the attacker's tile doesn't affect this calculation
        List<Point> minPath = AreaUtil.findIntersectingHexes(from.x, from.y, to.x, to.y);

        // we compute the average concealment of all the tiles in the path.  However, the straight line
        // path might cross more tiles than are neccessary, adding too much concealment.
        // To smooth over this sort of difference, we take the average and multiply it by the distance
        // between the points rather than the path length.

        int areaPathConcealment = 0;
        for (Point p : minPath) {
            areaPathConcealment += effects.getBonusAt(Bonus.Type.Concealment, p.x, p.y);
            areaPathConcealment -= effects.getBonusAt(Bonus.Type.ConcealmentNegation, p.x, p.y);

            if (!this.transparency[p.x][p.y]) {
                obstructionsInPathConcealment += 15;
            } else {
                Creature c = this.getCreatureAtGridPoint(p);
                if (c != null && c != defender) obstructionsInPathConcealment += 15;
            }
        }

        obstructionsInPathConcealment = Math.min(obstructionsInPathConcealment, 30);

        float areaPathConcealmentAverage = ((float)areaPathConcealment) / ((float)minPath.size());

        concealment += (areaPathConcealmentAverage * attacker.getLocation().getDistance(x, y));

        // now compute the amount of concealment based on defender and attacker stats

        int defenderConcealment = 0;

        if (defender != null) {
            defenderConcealment += defender.stats.get(Bonus.Type.Concealment) -
                    defender.stats.get(Bonus.Type.ConcealmentNegation);
        }

        if (attacker.stats.has(Bonus.Type.Blind)) defenderConcealment += 100;
        defenderConcealment = Math.min(100, defenderConcealment);

        int attackerIgnoring = attacker.stats.get(Bonus.Type.ConcealmentIgnoring);

        int defenderBonus = Math.min(100, Math.max(0, defenderConcealment - attackerIgnoring));

        concealment = Math.min(100, concealment);

        return concealment + defenderBonus + obstructionsInPathConcealment;
    }

    public int getConcealment(Creature attacker, Point position)
    {
        Creature target = this.entityList.getCreature(position.x, position.y);

        return getConcealment(attacker, target, position.x, position.y);
    }

    public int getConcealment(Creature attacker, Creature defender)
    {
        return getConcealment(attacker, defender, defender.getLocation().getX(), defender.getLocation().getY());
    }

    public boolean[][] getMatrixOfSize()
    {
        boolean[][] matrix = new boolean[width][height];

        return matrix;
    }

    public void removeEntity(Entity entity)
    {
        entityList.removeEntity(entity);
    }

    public void placeTrap(Trap trap)
    {
        entityList.addTrap(trap);
    }

    public void addItem(Item item)
    {
        addItem(item, 1);
    }

    public void addItem(Item item, int quantity)
    {
        // add the item to a container
        Container container = item.getLocation().getContainer();
        if (container == null) {
            // create a new temporary container
            container = EntityManager.getTemporaryContainer();
            container.setLocation(item.getLocation());
            entityList.addContainer(container);
        }

        container.getCurrentItems().add(item, quantity);
    }

    public Transition getTransitionAtGridPoint(Point p)
    {
        return getTransitionAtGridPoint(p.x, p.y);
    }

    public Transition getTransitionAtGridPoint(int x, int y)
    {
        for (String s : transitions) {
            Transition transition = Game.curCampaign.getAreaTransition(s);

            Transition.EndPoint endPoint = transition.getEndPointInArea(this);

            if (endPoint.getX() == x && endPoint.getY() == y) return transition;
        }

        return null;
    }

    public Openable getOpenableAtGridPoint(Point p)
    {
        return getOpenableAtGridPoint(p.x, p.y);
    }

    public Openable getOpenableAtGridPoint(int x, int y)
    {
        Openable openable = getDoorAtGridPoint(x, y);
        if (openable == null) {
            return getContainerAtGridPoint(x, y);
        } else {
            return openable;
        }
    }

    public Door getDoorAtGridPoint(int x, int y)
    {
        return entityList.getDoor(x, y);
    }

    public Door getDoorAtGridPoint(Point p)
    {
        return entityList.getDoor(p.x, p.y);
    }

    public Container getContainerAtGridPoint(int x, int y)
    {
        return entityList.getContainer(x, y);
    }

    public Container getContainerAtGridPoint(Point p)
    {
        return entityList.getContainer(p.x, p.y);
    }

    public Trap getTrapAtGridPoint(int x, int y)
    {
        return entityList.getTrap(x, y);
    }

    public Trap getTrapAtGridPoint(Point p)
    {
        return entityList.getTrap(p.x, p.y);
    }

    public List<Entity> getEntitiesWithID(String id)
    {
        return entityList.getEntitiesWithID(id);
    }

    public Entity getEntityWithID(String id)
    {
        return entityList.getEntityWithID(id);
    }

    public Creature getCreatureAtGridPoint(Point p)
    {
        return entityList.getCreature(p.x, p.y);
    }

    public Creature getCreatureAtGridPoint(int x, int y)
    {
        return entityList.getCreature(x, y);
    }

    public boolean[][] getEntityPassabilities(Creature mover)
    {
        return entityList.getEntityPassabilities(mover);
    }

    public List<Entity> getEntitiesAtGridPoint(Point p)
    {
        return getEntitiesAtGridPoint(p.x, p.y);
    }

    public List<Entity> getEntitiesAtGridPoint(int x, int y)
    {
        return entityList.getEntities(x, y);
    }

    public final boolean isVisible(Point p)
    {
        return isVisible(p.x, p.y);
    }

    /**
     * Debugging method to set all tiles explored
     */

    public void setAllTilesExplored()
    {
        for (int i = 0; i < explored.length; i++) {
            for (int j = 0; j < explored[i].length; j++) {
                explored[i][j] = true;
            }
        }
    }

    public final boolean isVisible(int x, int y)
    {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;

        return this.visibility[x][y];
    }

    public final boolean isTransparent(Point p)
    {
        return isTransparent(p.x, p.y);
    }

    public final boolean isTransparent(int x, int y)
    {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;

        return this.transparency[x][y];
    }

    public boolean[][] getCurrentPassable()
    {
        boolean[][] pass = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (entityList.getCreature(x, y) != null) {
                    pass[x][y] = false;
                } else {
                    Door d = entityList.getDoor(x, y);
                    if (d != null && !d.isOpen()) {
                        pass[x][y] = false;
                    } else {
                        pass[x][y] = this.passable[x][y];
                    }
                }
            }
        }

        return pass;
    }

    public final boolean isCurrentPassable(int x, int y)
    {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;

        if (entityList.getCreature(x, y) != null) return false;

        Door door = entityList.getDoor(x, y);
        if (door != null && !door.isOpen()) return false;

        return passable[x][y];
    }

    public final boolean isFreeForCreature(int x, int y)
    {
        if (!isPassable(x, y)) return false;

        if (getCreatureAtGridPoint(x, y) != null) return false;

        if (getDoorAtGridPoint(x, y) != null) return false;

        return true;
    }

    public final boolean isPassable(int x, int y)
    {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;

        return passable[x][y];
    }

    public void setEntityVisibility()
    {
        for (Entity entity : entityList) {
            if (entity instanceof Creature) {
                ((Creature)entity).computeVisibility();
            }
        }
    }

    public void addPlayerCharacters()
    {
        Creature lastCreature = null;

        Iterator<PC> iter = Game.curCampaign.party.iterator();
        Iterator<PointImmutable> posIter = startLocations.iterator();
        while (iter.hasNext()) {
            Creature creature = iter.next();

            if (posIter.hasNext()) {
                PointImmutable point = posIter.next();

                creature.setLocation(new Location(this, point.x, point.y));
            } else {
                // if there are no more explicit transition locations, just add the creature
                // wherever there is a nearby space
                Point point = Game.scriptInterface.ai.findClosestEmptyTile(lastCreature.getLocation().toPoint(), 3);

                if (point != null) {
                    creature.setLocation(new Location(this, point.x, point.y));
                } else {
                    Logger.appendToErrorLog("Warning: Unable to find enough starting positions for area " + id);
                }
            }

            entityList.addEntity(creature);

            lastCreature = creature;
        }
    }

    /**
     * Returns the unique ID of this area
     *
     * @return the unique ID
     */

    public String getID()
    {
        return id;
    }

    /**
     * Gets a list of all valid grid points based on the specified center coordinates with the specified
     * radius
     *
     * @param x
     * @param y
     * @param radius
     * @return a list of all points (in grid coordinates) based on the x, y, r
     */

    public List<PointImmutable> getPoints(int x, int y, int radius)
    {
        List<PointImmutable> points = new ArrayList<PointImmutable>();

        PointImmutable pCenter = new PointImmutable(x, y);

        if (pCenter.isWithinBounds(this)) {
            points.add(pCenter);
        }

        for (int r = 1; r <= radius; r++) {
            for (int i = 0; i < 6 * r; i++) {
                PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));

                if (!p.isWithinBounds(this)) continue;

                points.add(p);
            }
        }

        return points;
    }
}
