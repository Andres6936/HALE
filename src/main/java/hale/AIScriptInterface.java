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

package hale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import hale.*;
import hale.bonus.Stat;
import hale.defaultability.Move;
import hale.entity.Creature;
import hale.entity.Encounter;
import hale.entity.Location;
import hale.entity.Path;
import hale.interfacelock.MovementHandler;
import hale.rules.Faction;
import hale.util.AreaUtil;
import hale.util.Point;

/**
 * Class containing static methods that are useful for many of the JavaScript scripts,
 * particularly creature AI scripts.
 *
 * @author Jared Stephen
 */

public class AIScriptInterface
{

    /**
     * Returns a path for the parent to the specified position, or null if no path
     * exists
     *
     * @param x the x coordinate in the current area
     * @param y the y coordinate in the current area
     * @return the path to the specified position
     */

    public Path getMovementPath(Creature parent, int x, int y)
    {
        Move move = new Move();

        if (!move.canMove(parent, parent.getLocation().getInSameArea(x, y), 0)) return null;

        return move.getComputedPath();
    }

    /**
     * Returns a path for the parent to the specified position, or null if no path
     * exists
     *
     * @param location
     * @return the path to the specified position
     */

    public Path getMovementPath(Creature parent, Location location)
    {
        Move move = new Move();

        if (!move.canMove(parent, location, 0)) return null;

        return move.getComputedPath();
    }

    /**
     * The parent creature will attempt to find a path and then move along that path towards the
     * target position, until it is the specified distance away.  The function will wait until
     * movement is completed before returning.
     * <p>
     * This function will return true if movement occurred, even if it was interrupted.  It will
     * only return false if no movement occurred.
     * <p>
     * The movement from this method will provoke attacks of opportunity from any threatening
     * creatures.
     *
     * @param parent       the Creature that will attempt to move
     * @param x            the x grid coordinate of the position to move towards
     * @param y            the y grid coordinate of the position to move towards
     * @param distanceAway the desired distance from parent to target upon completion of the move
     * @return true if the parent moved, false otherwise
     */

    public boolean moveTowards(Creature parent, int x, int y, int distanceAway)
    {
        return moveTowards(parent, new Location(parent.getLocation().getArea(), x, y), distanceAway, true);
    }

    /**
     * The parent creature will attempt to find a path and then move along that path towards the
     * target position within the current area, until it is the specified distance away.  The function
     * will wait until movement is completed before returning.
     * <p>
     * This function will return true if movement occurred, even if it was interrupted.  It will
     * only return false if no movement occurred.
     *
     * @param parent       the Creature that will attempt to move
     * @param location     the position that parent will move towards.
     * @param distanceAway the desired distance from parent to target upon completion of the move
     * @param provokeAoOs  whether to provoke Attacks of opportunity from threatening creatures
     * @return true if the parent moved, false otherwise
     */

    public boolean moveTowards(Creature parent, int x, int y, int distanceAway, boolean provokeAoOs)
    {
        return moveTowards(parent, new Location(parent.getLocation().getArea(), x, y), distanceAway, provokeAoOs);
    }

    /**
     * The parent creature will attempt to find a path and then move along that path towards the
     * target position, until it is the specified distance away.  The function will wait until
     * movement is completed before returning.
     * <p>
     * This function will return true if movement occurred, even if it was interrupted.  It will
     * only return false if no movement occurred.
     *
     * @param parent       the Creature that will attempt to move
     * @param location     the position that parent will move towards.
     * @param distanceAway the desired distance from parent to target upon completion of the move
     * @param provokeAoOs  whether to provoke Attacks of opportunity from threatening creatures
     * @return true if the parent moved, false otherwise
     */

    public boolean moveTowards(Creature parent, Location location, int distanceAway, boolean provokeAoOs)
    {
        Move move = new Move();
        move.setTruncatePath(false);

        if (!move.canMove(parent, location, distanceAway)) {
            return false;
        }

        if (!move.moveTowards(parent, location, distanceAway, provokeAoOs)) {
            return false;
        }

        MovementHandler.Mover mover = move.getMover();
        if (mover == null) return false;

        try {
            // wait for the movement to complete
            synchronized (mover) {
                while (!mover.isFinished()) {
                    mover.wait();
                }
            }
        } catch (InterruptedException e) {
            // thread was interrupted, should exit
            return false;
        }

        return true;
    }

    /**
     * The parent creature will attempt to find a path and then move along that path towards the
     * target position, until it is the specified distance away.  The function will wait until
     * movement is completed before returning.
     * <p>
     * This function will return true if movement occurred, even if it was interrupted.  It will
     * only return false if no movement occurred.
     * <p>
     * The movement from this method will provoke attacks of opportunity from any threatening
     * creatures.
     *
     * @param parent       the Creature that will attempt to move
     * @param location     the position that parent will move towards.
     * @param distanceAway the desired distance from parent to target upon completion of the move
     * @return true if the parent moved, false otherwise
     */

    public boolean moveTowards(Creature parent, Location location, int distanceAway)
    {
        return moveTowards(parent, location, distanceAway, true);
    }

    /**
     * Returns a point with the hex grid coordinates of the tile nearest to parent
     * that is passable by creatures but unoccupied by any creature.
     * <p>
     * Note that this point may not be unique, however the distance between parent
     * and the returned point will be less than or equal to the distance between
     * parent and all other unoccupied points in the area
     *
     * @param center    the point to find the closest point to
     * @param maxRadius the maximum search radius for the point.  The creature's visibility
     *                  radius is often a good choice for this.
     * @return the closest empty tile, or null if no empty tile was found within the
     * search radius
     */

    public Point findClosestEmptyTile(Point center, int maxRadius)
    {
        if (checkEmpty(center)) return center;

        for (int r = 1; r <= maxRadius; r++) {
            for (int i = 0; i < r * 6; i++) {
                Point grid = AreaUtil.convertPolarToGrid(center, r, i);
                if (checkEmpty(grid)) return grid;
            }
        }

        return null;
    }

    private boolean checkEmpty(Point grid)
    {
        if (!Game.curCampaign.curArea.isPassable(grid.x, grid.y)) return false;

        return (Game.curCampaign.curArea.getCreatureAtGridPoint(grid) == null);
    }

    /**
     * All creatures currently threatening parent with available attacks of opportuntity will
     * recieve one attack of opportunity against parent with their main weapon.
     * <p>
     * If one or more of the creatures getting an attack of opportunity is player controlled
     * (isPlayerSelectable() returns true), then this function will wait until the player
     * makes their choice (whether to use the attack or not) before returning.
     *
     * @param parent the creature to provoke attacks against
     */

    public void provokeAttacksOfOpportunity(Creature parent)
    {
        Game.areaListener.getCombatRunner().provokeAttacksOfOpportunity(parent, null);
    }

    /**
     * Returns the creature meeting the specified faction relationship with parent
     * having the largest value of (maximumHP - currentHP) that is visible to parent
     * and not dead.
     *
     * @param parent       the creature looking for the target, whose visibility and
     *                     faction are used
     * @param relationship The faction relationship between parent and the target that is found.
     *                     Must be either "Hostile", "Neutral", or "Friendly"
     * @return the visible creature meeting the relationship criterion with the most damage.  null
     * if no visible creature meeting the relationship criterion is found.
     */

    public Creature findCreatureWithMostDamage(Creature parent, String relationship)
    {
        List<Creature> creatures = getLiveVisibleCreatures(parent, relationship);

        int bestDamage = Integer.MIN_VALUE;
        Creature bestCreature = null;

        for (Creature c : creatures) {
            int cDamage = c.stats.get(Stat.MaxHP) - c.getCurrentHitPoints();

            if (cDamage > bestDamage) {
                bestCreature = c;
                bestDamage = cDamage;
            }
        }

        return bestCreature;
    }

    /**
     * Returns the creature meeting the specified faction relationship with parent
     * having the largest value of maximumHP that is visible to parent
     * and not dead.
     *
     * @param parent       the creature looking for the target, whose visibility and
     *                     faction are used
     * @param relationship The faction relationship between parent and the target that is found.
     *                     Must be either "Hostile", "Neutral", or "Friendly"
     * @return the visible creature meeting the relationship criterion with the most maximum HP.  null
     * if no visible creature meeting the relationship criterion is found.
     */

    public Creature findCreatureWithMostMaxHP(Creature parent, String relationship)
    {
        List<Creature> creatures = getLiveVisibleCreatures(parent, relationship);

        int bestMaxHP = 0;
        Creature bestCreature = null;

        for (Creature c : creatures) {
            if (c.stats.get(Stat.MaxHP) > bestMaxHP) {
                bestCreature = c;
                bestMaxHP = c.stats.get(Stat.MaxHP);
            }
        }

        return bestCreature;
    }

    /**
     * Finds the creature meeting the specified relationship with the parent creature and being the
     * nearest in terms of hex tile distance.  Ties are decided randomly
     *
     * @param parent       the parent creature
     * @param relationship The faction relationship between parent and the target that is found.
     *                     Must be either "Hostile", "Neutral", or "Friendly"
     * @return the nearest creature to the parent
     */

    public Creature findNearestCreature(Creature parent, String relationship)
    {
        // list of creatures tied for closest
        List<Creature> closest = new ArrayList<Creature>();

        List<Creature> creatures = getLiveVisibleCreatures(parent, relationship);

        int smallestDistance = Integer.MAX_VALUE;

        //compute the simple distance
        for (Creature target : creatures) {
            int distance = AreaUtil.distance(parent.getLocation().getX(), parent.getLocation().getY(),
                    target.getLocation().getX(), target.getLocation().getY());

            if (distance < smallestDistance) {
                closest.clear();
                smallestDistance = distance;
                closest.add(target);
            } else
                if (distance == smallestDistance) {
                    closest.add(target);
                }
        }

        if (closest.size() == 0) {
            return null;
        } else {
            // return randomly chosen target from list of closest
            return closest.get(Game.dice.rand(0, closest.size() - 1));
        }
    }

    /**
     * Returns the set of creatures known to the specified parent that can potentially
     * be attacked currently.  this set can then be sorted, etc by the caller
     *
     * @param parent
     * @return the set of potentially attackable creatures
     */

    public AITargetSet getPotentialAttackTargets(Creature parent)
    {
        List<Creature> creatures = getLiveVisibleCreatures(parent, Faction.Relationship.Hostile.toString());

        return new AITargetSet(parent, creatures);
    }

    /**
     * Returns a list of all creatures that parent can attack from its current
     * position without moving, using its currently equipped main weapon.
     *
     * @param parent the creature to be attacking
     * @return all creatures within reach of parent's main weapon without
     * parent needing to move.
     */

    public List<Creature> getAttackableCreatures(Creature parent)
    {
        ArrayList<Creature> attackable = new ArrayList<Creature>();

        List<Creature> creatures;
        Encounter encounter = parent.getEncounter();

        // if parent has an encounter, use the encounter hostile list to save
        // needing to compute the list of visible creatures.  All visible creatures
        // will be on the knownHostiles list
        if (encounter != null) {
            creatures = encounter.getHostiles();
        } else {
            creatures = getLiveVisibleCreatures(parent, "Hostile");
        }

        for (Creature target : creatures) {
            if (target.isDying() || target.isDead()) continue;

            if (!parent.canAttack(target.getLocation())) continue;

            if (target.stats.isHidden()) continue;

            attackable.add(target);
        }

        return attackable;
    }

    /**
     * Returns a list of all creatures occupying tiles directly adjacent or equal to
     * parent's position matching the specified faction relationship
     *
     * @param parent       the creature whose position is used
     * @param relationship The faction relationship between parent and the target that is found.
     *                     Must be either "Hostile", "Neutral", or "Friendly"
     * @return the list of all creatures that can be touched by a creature at parent's position.
     * This list will generally include parent, unless parent does not actually occupy the area
     * (as is the case for scroll casters).
     */

    public List<Creature> getTouchableCreatures(Creature parent, String relationship)
    {
        ArrayList<Creature> creatures = new ArrayList<Creature>();

        Faction activeFaction = parent.getFaction();

        Faction.Relationship rel = null;

        if (relationship != null) rel = Faction.Relationship.valueOf(relationship);

        Point[] positions = AreaUtil.getAdjacentTiles(parent.getLocation().getX(), parent.getLocation().getY());

        for (Point p : positions) {
            Creature c = Game.curCampaign.curArea.getCreatureAtGridPoint(p);
            if (c != null) {
                Faction.Relationship curRel = activeFaction.getRelationship(c.getFaction());
                if (rel == null || curRel == rel) {
                    creatures.add(c);
                }
            }
        }

        // add the creature standing at the same position as caster.  This will be different
        // than just adding the caster for spells cast from scrolls
        Creature c = Game.curCampaign.curArea.getCreatureAtGridPoint(parent.getLocation().getX(), parent.getLocation().getY());
        if (c != null && (rel == null || activeFaction.getRelationship(activeFaction) == rel)) {
            creatures.add(c);
        }

        return creatures;
    }

    /**
     * Returns the list of all creatures within line of sight of parent meeting the specified
     * faction relationship and within the specified range.
     *
     * @param parent       the creature whose visibility is used and to compare faction and distance to
     * @param relationship The faction relationship between parent and the target that is found.
     *                     Must be either "Hostile", "Neutral", or "Friendly"
     * @param range        the maximum distance between parent and target allowed for returned creatures.
     *                     This distance is in feet (equal to 5 times the hex grid distance).
     * @return the list of all creatures visible to parent meeting the specified constraints.
     */

    public List<Creature> getVisibleCreaturesWithinRange(Creature parent, String relationship, int range)
    {
        Faction.Relationship rel = null;
        if (relationship != null) rel = Faction.Relationship.valueOf(relationship);

        List<Creature> creatures = AreaUtil.getVisibleCreatures(parent, rel);

        creatures.removeIf(c -> AreaUtil.distance(c.getLocation().getX(), c.getLocation().getY(),
                parent.getLocation().getX(), parent.getLocation().getY()) * 5 > range);

        return creatures;
    }

    /**
     * Returns the list of all creatures within the specified range of parent meeting the specified
     * faction relationship.
     *
     * @param parent       the creature to compare faction and distance to
     * @param relationship The faction relationship between parent and the target that is found.
     *                     Must be either "Hostile", "Neutral", or "Friendly"
     * @param range        the maximum distance between parent and target allowed for returned creatures.
     *                     This distance is in feet (equal to 5 times the hex grid distance).
     * @return the list of all creatures within range of the parent meeting the faction relationship
     * constraint.
     */

    public List<Creature> getAllCreaturesWithinRange(Creature parent, String relationship, int range)
    {
        Faction.Relationship rel = null;
        if (relationship != null) rel = Faction.Relationship.valueOf(relationship);

        List<Creature> creatures =
                Game.curCampaign.curArea.getEntities().getCreaturesWithinRadius(parent.getLocation().getX(),
                        parent.getLocation().getY(), range / 5);

        if (rel != null) {
            Iterator<Creature> iter = creatures.iterator();
            while (iter.hasNext()) {
                if (parent.getFaction().getRelationship(iter.next().getFaction()) != rel) {
                    iter.remove();
                }
            }
        }

        return creatures;
    }

    /**
     * Returns the list of all creatures within line of sight of parent meeting the specified
     * faction relationship and who are not dead.
     *
     * @param parent       the creature whose visibility is used and to compare faction with
     * @param relationship The faction relationship between parent and the target that is found.
     *                     Must be either "Hostile", "Neutral", or "Friendly"
     * @return the list of all creatures visible to parent meeting the specified constraints.
     */

    public List<Creature> getLiveVisibleCreatures(Creature parent, String relationship)
    {
        Faction.Relationship rel = null;
        if (relationship != null) rel = Faction.Relationship.valueOf(relationship);

        Encounter encounter = parent.getEncounter();
        List<Creature> creatures;

        if (rel == Faction.Relationship.Hostile && encounter != null) {
            creatures = encounter.getHostiles();
        } else {
            creatures = AreaUtil.getVisibleCreatures(parent, rel);
        }

        creatures.removeIf(c -> c.isDead() || c.isDying() || c.stats.isHidden());

        return creatures;
    }

    public void sortCreatureListClosestFirst(Creature parent, List<Creature> creatures)
    {
        creatures.sort(new CreatureSorter(parent));
    }

    private static class CreatureSorter implements Comparator<Creature>
    {
        private final Creature parent;

        private CreatureSorter(Creature parent)
        {
            this.parent = parent;
        }

        @Override
        public int compare(Creature a, Creature b)
        {
            return AreaUtil.distance(a.getLocation().getX(), a.getLocation().getY(),
                    parent.getLocation().getX(), parent.getLocation().getY()) -
                    AreaUtil.distance(b.getLocation().getX(), b.getLocation().getY(),
                            parent.getLocation().getX(), parent.getLocation().getY());
        }
    }
}
