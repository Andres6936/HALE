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

package hale.ability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.Button;

import hale.Game;
import hale.defaultability.MouseActionList;
import hale.entity.Creature;
import hale.rules.Faction;
import hale.util.Point;
import hale.widgets.RightClickMenu;

/**
 * A class for any Targeter that affects a set of points in the
 * area based on the targeted point.  This Targeter also shows
 * affected creatures within the affected points.
 *
 * @author Jared Stephen
 */

public abstract class AreaTargeter extends Targeter
{
    private List<Point> affectedPoints;
    private List<Creature> affectedCreatures;
    private Faction.Relationship affectedCreatureRelationship;

    private boolean allowAffectedCreaturesEmpty;
    private boolean allowOccupiedTile;

    /**
     * Creates a new AreaTargeter with the specified AbilitySlot as parent
     *
     * @param parent     the parent Creature that is targeting using this Targeter
     * @param scriptable the scriptable responsible for managing the script that
     *                   will be used in the callback
     * @param slot       optional AbilitySlot that can be stored along with this Targeter
     */

    public AreaTargeter(Creature parent, Scriptable scriptable, AbilitySlot slot)
    {
        super(parent, scriptable, slot);

        affectedPoints = new ArrayList<Point>();
        affectedCreatures = new ArrayList<Creature>();

        allowAffectedCreaturesEmpty = true;
        allowOccupiedTile = true;
    }

    /**
     * Sets whether this AreaTargeter is allowed to make a selection on a tile that
     * is occupied by a creature.  If set to false, then the selection must be made
     * over an unoccupied tile.
     *
     * @param allow whether to allow a selection on an occupied tile
     */

    public void setAllowOccupiedTileSelection(boolean allow)
    {
        this.allowOccupiedTile = allow;
    }

    /**
     * Sets whether this AreaTargeter is allowed to make a selection when there are no
     * affected creatures currently being targeted.
     *
     * @param allow whether to allow a selection with no affected creatures
     */

    public void setAllowAffectedCreaturesEmpty(boolean allow)
    {
        this.allowAffectedCreaturesEmpty = allow;
    }

    /**
     * Returns the List of grid Points that are affected by this Targeter;
     * i.e. the list of Points within the specified radius from the
     * selected Point.
     *
     * @return the List of affected Points
     */

    public List<Point> getAffectedPoints()
    {
        return affectedPoints;
    }

    /**
     * Returns the List of Creatures that are affected by this Targeter.
     * This means within the specified radius of the currently targeted
     * Point and meeting any other criterion, such as Faction Relationship
     * criterion.
     *
     * @return the List of Creatures affected by this Targeter
     */

    public List<Creature> getAffectedCreatures()
    {
        return affectedCreatures;
    }

    /**
     * Sets the Faction Relationship that all affected Creatures must have
     * with the parent of this Targeter.  Creatures that do not have the
     * specified faction relationship will not be affected.
     * <p>
     * The relationship must be a String representation of a valid relationship.
     *
     * @param relationship "Hostile", "Neutral", or "Friendly"
     */

    public void setRelationshipCriterion(String relationship)
    {
        this.affectedCreatureRelationship = Faction.Relationship.valueOf(relationship);

        affectedCreatures.clear();
        // recompute affected creatures
        for (Point p : affectedPoints) {
            Creature creature = Game.curCampaign.curArea.getCreatureAtGridPoint(p);
            if (creature != null && meetsRelationshipCriterion(creature)) {
                affectedCreatures.add(creature);
            }
        }
    }

    /**
     * Returns true if and only if the specified Creature meets the relationship criterion
     * if one has been set.  Always returns true if no criterion has been set.
     *
     * @param creature the Creature to check the Faction relationship with
     * @return true if and only if the specified Creature meets any Faction Relationship
     * criterion
     */

    protected boolean meetsRelationshipCriterion(Creature creature)
    {
        if (this.affectedCreatureRelationship == null) {
            Faction playerFaction = Game.ruleset.getFaction(Game.ruleset.getString("PlayerFaction"));


            // if friendly fire is disabled, hostile spells don't affect friendly PC creatures
            boolean friendlyFireOnPCs = Game.ruleset.getDifficultyManager().friendlyFireOnPCs();
            boolean playerTargetingPlayer = getParent().getFaction() == playerFaction &&
                    creature.getFaction() == playerFaction;

            boolean spellIsHostile;
            switch (getSlot().getAbility().getActionType()) {
                case Debuff:
                case Damage:
                    spellIsHostile = true;
                default:
                    spellIsHostile = false;
            }

            if (spellIsHostile && playerTargetingPlayer && !friendlyFireOnPCs) {
                return false;
            } else {
                return true;
            }
        } else {
            return this.getParent().getFaction().getRelationship(creature) == affectedCreatureRelationship;
        }
    }

    @Override
    public boolean hasSelectedTargets()
    {
        return mouseHoverValid() && affectedCreaturesEmptyConditionMet();
    }

    @Override
    public void showMenu(int x, int y)
    {
        RightClickMenu menu = Game.mainViewer.getMenu();

        menu.clear();

        menu.setPosition(x, y);
        menu.addMenuLevel(this.getMenuTitle());

        if (this.getMouseActionCondition() == MouseActionList.Condition.TargetSelect) {
            Button button = new Button("Activate");
            button.addCallback(getActivateCallback());

            menu.addButton(button);
        }

        if (this.isCancelable()) {
            Button button = new Button("Cancel");
            button.addCallback(getCancelCallback());

            menu.addButton(button);
        }

        menu.show();
    }

    @Override
    public void performLeftClickAction()
    {
        if (this.getMouseActionCondition() == MouseActionList.Condition.TargetSelect) {
            getActivateCallback().run();
        }
    }

    @Override
    public boolean setMousePosition(int x, int y, Point gridPoint)
    {
        boolean returnValue = super.setMousePosition(x, y, gridPoint);

        if (!returnValue && updateMouseStateOnlyWhenGridPointChanges()) return false;

        affectedPoints.clear();
        affectedCreatures.clear();

        computeAffectedPoints(x, y, gridPoint);

        // remove out of bounds points
        Iterator<Point> iter = affectedPoints.iterator();
        while (iter.hasNext()) {
            Point p = iter.next();

            if (p.x < 0 || p.x >= Game.curCampaign.curArea.getWidth() || p.y < 0 || p.y >= Game.curCampaign.curArea.getHeight()) {
                iter.remove();
            }
        }

        // compute affected creatures based on affected points
        for (Point p : affectedPoints) {
            Creature creature = Game.curCampaign.curArea.getCreatureAtGridPoint(p);
            if (creature != null && meetsRelationshipCriterion(creature)) {
                affectedCreatures.add(creature);
            }
        }

        // determine mouse action based on hover state
        if (mouseHoverValid() && affectedCreaturesEmptyConditionMet() && occupiedTileConditionMet(gridPoint)) {
            setMouseActionCondition(MouseActionList.Condition.TargetSelect);
        } else {
            setMouseHoverValid(false);
            setMouseActionCondition(MouseActionList.Condition.Cancel);
        }

        return returnValue;
    }

    private boolean occupiedTileConditionMet(Point gridPoint)
    {
        if (this.allowOccupiedTile) return true;

        Creature c = Game.curCampaign.curArea.getCreatureAtGridPoint(gridPoint);
        return (c == null);
    }

    private boolean affectedCreaturesEmptyConditionMet()
    {
        if (allowAffectedCreaturesEmpty) {
            return true;
        } else {
            return affectedCreatures.size() > 0;
        }
    }

    /**
     * Computes the set of Points that are affected by this AreaTargeter.  This
     * will be specific to each implementation.
     *
     * @param x         the x screen coordinate of the mouse
     * @param y         the y screen coordinate of the mouse
     * @param gridPoint the current mouse position that is selected.  This function
     *                  will only be called with a valid, selectable gridPoint.
     */

    protected abstract void computeAffectedPoints(int x, int y, Point gridPoint);

    /**
     * Indicates whether the mouse state for this Targeter should be updated
     * every time there is a mouse event, or only when the grid point being
     * hovered over changes.
     *
     * @return true if the mouse state should be updated only when grid point
     * changes, false if the mouse state should be updated every mouse event
     */

    protected abstract boolean updateMouseStateOnlyWhenGridPointChanges();

    /*
     * Draws the set of affected points, affected creatures, and allowed points
     * (non-Javadoc)
     * @see main.java.hale.effect.Targeter#draw()
     */

    @Override
    public boolean draw(AnimationState as)
    {
        if (!super.draw(as)) {
            return false;
        }

        for (Point p : affectedPoints) {
            Game.areaViewer.drawGreyHex(p, as);
        }

        for (Creature creature : affectedCreatures) {
            Game.areaViewer.drawRedHex(creature.getLocation().toPoint(), as);
        }

        for (Point p : getAllowedPoints()) {
            Game.areaViewer.drawGreenHex(p, as);
        }

        return true;
    }

    /**
     * Gets the number of targets currently affected by the targeter with the appropriate
     * status
     *
     * @param desirable true to get desirable targets (friendly for positive, hostile
     *                  for negative abilities), or false to get undesirable
     * @return the number of targets of the specified status
     */

    private int getTargetCount(boolean desirable)
    {
        if (getSlot() == null) return 0;

        Faction.Relationship targetRelationship;

        switch (getSlot().getAbility().getActionType()) {
            case Buff:
            case Heal:
                targetRelationship = Faction.Relationship.Friendly;
                break;
            case Debuff:
            case Damage:
                targetRelationship = Faction.Relationship.Hostile;
                break;
            default:
                return 0;
        }

        int count = 0;
        for (Creature creature : affectedCreatures) {
            Faction.Relationship creatureRelationship = getParent().getFaction().getRelationship(creature);

            if (desirable && creatureRelationship == targetRelationship) {
                count++;
            } else
                if (!desirable && creatureRelationship != targetRelationship) {
                    count++;
                }
        }

        return count;
    }

    @Override
    public int getDesirableTargetCount()
    {
        return getTargetCount(true);

    }

    @Override
    public int getUndesirableTargetCount()
    {
        return getTargetCount(false);
    }
}
