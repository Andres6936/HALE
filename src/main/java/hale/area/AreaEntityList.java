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

package hale.area;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import hale.entity.Container;
import hale.entity.Creature;
import hale.entity.Door;
import hale.entity.Entity;
import hale.entity.EntityManager;
import hale.entity.Location;
import hale.entity.NPC;
import hale.entity.PC;
import hale.entity.PCTemplate;
import hale.entity.Trap;
import hale.loading.JSONOrderedObject;
import hale.loading.LoadGameException;
import hale.loading.ReferenceHandler;
import hale.loading.Saveable;
import hale.util.AreaUtil;
import hale.util.Point;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

public class AreaEntityList implements Saveable, Iterable<Entity>
{
    private EntityList[][] entities;
    private Set<Entity> entitiesSet;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        JSONOrderedObject[] entitiesData = new JSONOrderedObject[entitiesSet.size()];
        int i = 0;
        for (Entity entity : entitiesSet) {
            entitiesData[i] = entity.save();
            i++;
        }
        data.put("entities", entitiesData);

        return data;
    }

    private enum ClassType
    {
        PC, NPC, Container, Door, Item, Trap, EquippableItem, Ammo, Armor, Weapon;
    }

    private Entity createEntity(SimpleJSONObject entryData)
    {
        String id = entryData.get("id", null);
        ClassType classType = ClassType.valueOf(entryData.get("class", null));

        switch (classType) {
            case PC:
                // for PCs, we must construct the template from the JSON, as we can't rely
                // on the character file still existing
                PCTemplate template = new PCTemplate(id, entryData);
                return new PC(template);
            case NPC:
                NPC npc = EntityManager.getNPC(id);
                npc.abilities.clear();
                return npc;
            case Container:
                return EntityManager.getContainer(id);
            case Door:
                return EntityManager.getDoor(id);
            default:
                return EntityManager.getItem(id);
        }
    }

    /**
     * Loads this AreaEntityList from the specified JSON data
     *
     * @param data
     * @param area
     * @param refHandler
     * @throws LoadGameException
     */

    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        for (SimpleJSONArrayEntry entry : data.getArray("entities")) {
            SimpleJSONObject entryData = entry.getObject();

            Entity entity = createEntity(entryData);
            entity.load(entryData, area, refHandler);

            if (entity instanceof Container) {
                addContainer((Container)entity);
            } else
                if (entity instanceof Trap) {
                    addTrap((Trap)entity);
                }

            addEntity(entity);

            // for PCs, we need to manually add them to the EntityManager, as their
            // template was loaded directly from the save game data
            if (entity instanceof PC) {
                EntityManager.addLoadedPC(new PC(((PC)entity)));
            }
        }
    }

    public AreaEntityList(int width, int height)
    {
        entities = new EntityList[width][height];
        entitiesSet = new LinkedHashSet<Entity>();
    }

    /**
     * Returns true if this List contains the specified entity within the standard
     * EntityList.  Does not return true for entities that have been removed and
     * are still being tracked within the dead creatures list
     *
     * @param entity the entity to check for
     * @return true if and only if this List contains the specified entity
     */

    public final boolean containsEntity(Entity entity)
    {
        return entitiesSet.contains(entity);
    }

    /**
     * Adds the specified trap to this entity list at its location
     *
     * @param trap the trap to add
     */

    public synchronized void addTrap(Trap trap)
    {
        if (entitiesSet.contains(trap)) return;

        addTrapAt(trap.getLocation().getX(), trap.getLocation().getY(), trap);

        entitiesSet.add(trap);
    }

    /**
     * Adds the specified container to this entity list at its location.
     *
     * @param container the container to add
     */

    public synchronized void addContainer(Container container)
    {
        if (entitiesSet.contains(container)) return;

        addContainerAt(container.getLocation().getX(), container.getLocation().getY(), container);

        entitiesSet.add(container);
    }

    /**
     * Adds the specified entity to this entity list at its location.  Containers
     * should be added with {@link #addContainer(Entity)}, while traps should be added
     * with  {@link #addTrap(Trap)}
     *
     * @param entity the entity to add
     */

    public synchronized void addEntity(Entity entity)
    {
        if (entitiesSet.contains(entity)) return;

        addAt(entity.getLocation().getX(), entity.getLocation().getY(), entity);

        entitiesSet.add(entity);
    }

    private void setMatrix(boolean[][] matrix, boolean value, int x, int y)
    {
        if (x >= 0 && y >= 0 && x < matrix.length && y < matrix[0].length) {
            matrix[x][y] = value;
        }
    }

    /**
     * Gets the matrix of passabilities for each tile in the map based on the
     * specified creature, ignored creatures
     *
     * @param mover the creature who is moving
     * @return the matrix of passabilities
     */

    public synchronized boolean[][] getDoorPassabilities(Creature mover)
    {
        boolean[][] pass = new boolean[entities.length][entities[0].length];
        AreaUtil.setMatrix(pass, true);

        for (Entity entity : entitiesSet) {
            if (entity instanceof Door) {
                if (!((Door)entity).isOpen()) {
                    setMatrix(pass, false, entity.getLocation().getX(), entity.getLocation().getY());
                }
            }
        }

        return pass;
    }

    /**
     * Gets the matrix of passabilities for each tile in the map based on the
     * specified creature
     *
     * @param mover the creature who is moving
     * @return the matrix of passabilities
     */

    public synchronized boolean[][] getEntityPassabilities(Creature mover)
    {
        boolean[][] pass = new boolean[entities.length][entities[0].length];
        AreaUtil.setMatrix(pass, true);

        for (Entity entity : entitiesSet) {
            if (entity instanceof Door) {
                if (!((Door)entity).isOpen()) {
                    setMatrix(pass, false, entity.getLocation().getX(), entity.getLocation().getY());
                }
            } else
                if (entity instanceof Creature) {
                    // mover can pass through creature unless:
                    // creature is not friendly and is not helpless

                    // if movement is interrupted while two creatures overlap, the mover will be pushed
                    // back to their last position which will prevent the two remaining in the same tile.

                    if (!entity.getFaction().isFriendly(mover) && !((Creature)entity).stats.isHelpless()) {
                        setMatrix(pass, false, entity.getLocation().getX(), entity.getLocation().getY());
                    }
                }
        }

        return pass;
    }

    /**
     * Starts any animations on effects in all contained creatures
     */

    public void startEffectAnimations()
    {
        for (Entity entity : entitiesSet) {
            entity.getEffects().startAnimations();
        }
    }

    /**
     * Removes the specified Entity from the set of entities within this Area.
     *
     * @param entity the entity to remove
     */

    public synchronized void removeEntity(Entity entity)
    {
        if (!entitiesSet.contains(entity)) return;

        removeAt(entity.getLocation().getX(), entity.getLocation().getY(), entity);

        entitiesSet.remove(entity);

        // remove the creature from the encounter that is tracking it, if applicable
        if (entity instanceof Creature) {
            Creature creature = (Creature)entity;

            if (creature.getEncounter() != null) {
                creature.getEncounter().removeCreatureFromArea(creature);
            }
        }
    }

    /*
     * traps go on the very bottom of the list
     */

    private void addTrapAt(int x, int y, Trap trap)
    {
        if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return;

        if (entities[x][y] == null) {
            entities[x][y] = new EntityList();
        }

        entities[x][y].add(0, trap);
    }

    /*
     * containers go above traps but below everything else
     */

    private void addContainerAt(int x, int y, Container container)
    {
        if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return;

        if (entities[x][y] == null) {
            entities[x][y] = new EntityList();
        }

        // find the index to add at
        int index;
        for (index = 0; index < entities[x][y].size(); index++) {
            if (!(entities[x][y].get(index) instanceof Trap)) {
                break;
            }
        }

        entities[x][y].add(index, container);
    }

    private void addAt(int x, int y, Entity entity)
    {
        if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return;

        if (entities[x][y] == null) {
            entities[x][y] = new EntityList();
        }

        entities[x][y].add(entity);
    }

    private void removeAt(int x, int y, Entity entity)
    {
        if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return;

        if (entities[x][y] == null) return;

        entities[x][y].remove(entity);

        if (entities[x][y].size() == 0) entities[x][y] = null;
    }

    public void moveEntity(Entity entity, Location lastLocation)
    {
        if (!entitiesSet.contains(entity)) return;

        removeAt(lastLocation.getX(), lastLocation.getY(), entity);

        addAt(entity.getLocation().getX(), entity.getLocation().getY(), entity);
    }

    public List<Entity> getEntitiesWithID(String id)
    {
        List<Entity> entities = new ArrayList<Entity>();

        for (Entity e : entitiesSet) {
            if (e.getTemplate().getID().equals(id)) entities.add(e);
        }

        return entities;
    }

    public Entity getEntityWithID(String id)
    {
        for (Entity e : entitiesSet) {
            if (e.getTemplate().getID().equals(id)) return e;
        }

        return null;
    }

    public final Door getDoor(int x, int y)
    {
        if (entities[x][y] == null) return null;

        for (Entity entity : entities[x][y]) {
            if (entity instanceof Door) return (Door)entity;
        }

        return null;
    }

    public final Trap getTrap(int x, int y)
    {
        if (entities[x][y] == null) return null;

        for (Entity entity : entities[x][y]) {
            if (entity instanceof Trap) return (Trap)entity;
        }

        return null;
    }

    public final Container getContainer(int x, int y)
    {
        if (entities[x][y] == null) return null;

        for (Entity entity : entities[x][y]) {
            if (entity instanceof Container) return (Container)entity;
        }

        return null;
    }

    public final Creature getCreature(int x, int y)
    {
        if (entities[x][y] == null) return null;

        for (Entity entity : entities[x][y]) {
            if (entity instanceof Creature) return (Creature)entity;
        }

        return null;
    }

    public final List<Creature> getCreatures(int x, int y)
    {
        if (entities[x][y] == null) return new ArrayList<Creature>(0);

        List<Creature> creatures = new ArrayList<Creature>();

        for (Entity entity : entities[x][y]) {
            if (entity instanceof Creature) {
                creatures.add((Creature)entity);
            }
        }

        return creatures;
    }

    public final List<Entity> getEntities(int x, int y)
    {
        if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return new ArrayList<Entity>(0);

        if (entities[x][y] == null) return new ArrayList<Entity>(0);

        List<Entity> foundEntities = new ArrayList<Entity>(entities[x][y].size());

        for (Entity entity : entities[x][y]) {
            foundEntities.add(entity);
        }

        return foundEntities;
    }

    /**
     * Returns the set of entities at the specified grid position.  This set must not
     * be modified.
     *
     * @param x the x grid coordinate
     * @param y the y grid coordinate
     * @return the set of entities at the specified grid position.
     */

    public final Collection<Entity> getEntitiesSet(int x, int y)
    {
        return entities[x][y];
    }

    /**
     * Returns all creatures in this list at any grid position
     *
     * @return all creatures in this list
     */

    public final List<Creature> getAllCreatures()
    {
        List<Creature> creatures = new ArrayList<Creature>();

        for (Entity entity : entitiesSet) {
            if (entity instanceof Creature) creatures.add((Creature)entity);
        }

        return creatures;
    }

    /**
     * Returns all containers in this list at any grid position
     *
     * @return all containers in this list
     */

    public final List<Container> getAllContainers()
    {
        List<Container> containers = new ArrayList<Container>();

        for (Entity entity : entitiesSet) {
            if (entity instanceof Container) containers.add((Container)entity);
        }

        return containers;
    }

    /**
     * Returns a list of all doors at all locations in this list
     *
     * @return a list of all doors
     */

    public List<Door> getAllDoors()
    {
        List<Door> doors = new ArrayList<Door>();

        for (Entity entity : entitiesSet) {
            if (entity instanceof Door) doors.add((Door)entity);
        }

        return doors;
    }

    public List<Trap> getVisibleTraps(boolean[][] visibility)
    {
        List<Trap> traps = new LinkedList<Trap>();

        for (int i = 0; i < entities.length; i++) {
            for (int j = 0; j < entities[0].length; j++) {
                if (visibility[i][j]) {
                    Trap t = getTrap(i, j);
                    if (t != null) traps.add(t);
                }
            }
        }

        return traps;
    }

    public List<Creature> getVisibleCreatures(boolean[][] visibility)
    {
        List<Creature> creatures = new LinkedList<Creature>();

        for (int i = 0; i < entities.length; i++) {
            for (int j = 0; j < entities[0].length; j++) {
                if (visibility[i][j]) {
                    Creature c = getCreature(i, j);
                    if (c != null) creatures.add(c);
                }
            }
        }

        return creatures;
    }

    /**
     * Gets the list of all creatures that are currently visible to the specified
     * creature
     *
     * @param parent
     * @return the list of visible creatures
     */

    public List<Creature> getVisibleCreatures(Creature parent)
    {
        List<Creature> creatures = new ArrayList<Creature>();

        for (int i = 0; i < entities.length; i++) {
            for (int j = 0; j < entities[0].length; j++) {
                if (!parent.hasVisibilityInCurrentArea(i, j)) continue;

                Creature creature = getCreature(i, j);
                if (creature != null) {
                    creatures.add(creature);
                }
            }
        }

        return creatures;
    }

    public List<Creature> getCreaturesWithinRadius(int x, int y, int radius)
    {
        List<Creature> creatures = new LinkedList<Creature>();

        Creature current = getCreature(x, y);
        if (current != null) creatures.add(current);

        for (int r = 1; r <= radius; r++) {
            for (int i = 0; i < 6 * r; i++) {
                Point p = AreaUtil.convertPolarToGrid(x, y, r, i);

                if (p.x < 0 || p.x >= entities.length || p.y < 0 || p.y >= entities[0].length) {
                    continue;
                }

                current = getCreature(p.x, p.y);

                if (current != null) {
                    creatures.add(current);
                }
            }
        }

        return creatures;
    }

    public int getNumberOfCreatures(int x, int y)
    {
        if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return 0;

        if (entities[x][y] == null) return 0;

        int count = 0;

        for (Entity e : entities[x][y]) {
            if (e instanceof Creature) count++;
        }

        return count;
    }

    public void resize(int newWidth, int newHeight)
    {
        EntityList[][] newEntities = new EntityList[newWidth][newHeight];
        HashSet<Entity> newHash = new HashSet<Entity>();

        int width = Math.min(this.entities.length, newWidth);
        int height = Math.min(this.entities[0].length, newHeight);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newEntities[i][j] = this.entities[i][j];

                if (entities[i][j] != null) newHash.addAll(entities[i][j]);
            }
        }

        this.entities = newEntities;
        this.entitiesSet = newHash;
    }

    @Override
    public Iterator<Entity> iterator()
    {
        return new EntityIterator();
    }

    private class EntityIterator implements Iterator<Entity>
    {
        private Iterator<Entity> hashSetIterator;
        private Entity last;

        private EntityIterator()
        {
            this.hashSetIterator = entitiesSet.iterator();
            last = null;
        }

        @Override
        public boolean hasNext()
        {
            return hashSetIterator.hasNext();
        }

        @Override
        public Entity next()
        {
            last = hashSetIterator.next();
            return last;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    private class EntityList extends ArrayList<Entity>
    {
        private static final long serialVersionUID = 7587119408526288199L;

        private EntityList()
        {
            super(2);
        }
    }
}
