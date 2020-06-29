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
import java.util.List;

import de.matthiasmann.twl.AnimationState;

import hale.Game;
import hale.defaultability.MouseActionList;
import hale.entity.Creature;
import hale.entity.Entity;
import hale.entity.Location;
import hale.util.AreaUtil;
import hale.util.Point;
import hale.util.Logger;

/**
 * For those Abilities which need a target or targets, this class supports a range
 * of target selection behaviors.  This is the base class for all other Targeters.
 * <p>
 * Ability scripts can make use of this class by first creating a Targeter,
 * setting the parameters for what constitutes a valid target, and then requesting
 * the targets from the targeter.  This will cause the user interface to allow the
 * entering of targets or enable a creature's AI to select a target.
 *
 * @author Jared Stephen
 */

public abstract class Targeter
{
    private boolean canceled;

    private int maxRange, minRange;

    private int mouseX, mouseY;
    private Point mouseGridPoint;
    private boolean mouseHoverValid;
    private MouseActionList.Condition mouseActionCondition;

    private Creature parent;
    private Scriptable scriptable;
    private boolean cancelable;
    private String menuTitle;

    private AbilitySlot abilitySlot;

    private List<Point> allowedPoints;

    private List<Object> callbackArguments;

    // see addAllowedPoints or addAllowedCreatures;
    private boolean treatAllowedPointsAsNonEmpty;

    // default value is Targeter.ActivateCallback, can also be customized
    private Runnable activateCallback;
    private Runnable cancelCallback;

    private CheckValidCallback checkValidCallback;

    /**
     * Creates a new Targeter with the specified Creature as parent and the specified
     * scriptable managing the callback script.  Optionally, an AbilitySlot can be
     * specified to be stored with this Targeter and accessible via {@link #getSlot()}.
     * This AbilitySlot will have no impact on the internal functioning of the Targeter.
     * In particular, the supplied parent and scriptable are used, NOT the Targeter's
     * parent and ability.
     * <p>
     * This Targeter will be cancelable by default.
     *
     * @param parent      the parent Activator / Creature that is targeting using this Targeter
     * @param scriptable  the scriptable responsible for managing the script that
     *                    will be used in the callback
     * @param abilitySlot optional AbilitySlot that can be stored along with this Targeter
     */

    public Targeter(Creature parent, Scriptable scriptable, AbilitySlot abilitySlot)
    {
        this.parent = parent;
        this.scriptable = scriptable;
        this.cancelable = true;
        this.abilitySlot = abilitySlot;

        if (abilitySlot != null) {
            this.setMenuTitle(abilitySlot.getAbility().getName());
        }

        this.allowedPoints = new ArrayList<Point>();

        this.callbackArguments = new ArrayList<Object>();
        this.callbackArguments.add(this);

        this.treatAllowedPointsAsNonEmpty = false;

        this.activateCallback = new ActivateCallback();
        this.cancelCallback = new CancelCallback();
    }

    /**
     * Returns the parent for this Targeter
     *
     * @return the parent for this Targeter
     */

    public Creature getParent()
    {
        return parent;
    }

    /**
     * Returns the AbilitySlot determining the parent for this Targeter and
     * the Ability whose script will be used in the callback.  This will
     * return null if no AbilitySlot was specified at Targeter creation.
     *
     * @return the parent AbilitySlot for this Targeter
     */

    public AbilitySlot getSlot()
    {
        return abilitySlot;
    }

    /**
     * Returns whether this Targeter currently has a set of valid selected targets
     * and is ready to end and have its callback run.
     *
     * @return true if this Targeter has a valid set of selected targets, false otherwise
     */

    public abstract boolean hasSelectedTargets();

    /**
     * Returns whether this Targeter can be canceled prior to gaining a valid set of targets
     *
     * @return true if this Targeter can be canceled, false otherwise
     */

    public boolean isCancelable()
    {
        return cancelable;
    }

    /**
     * Sets whether this Targeter can be canceled prior to having a complete, valid set of
     * targets.
     *
     * @param cancelable true if this Targeter should be cancelable, false if not
     */

    public void setCancelable(boolean cancelable)
    {
        this.cancelable = cancelable;
    }

    /**
     * Activates this Targeter and readies it for selection.  At this point
     * either the player will need to select targets through the interface
     * or the AI can specify targets.
     */

    public void activate()
    {
        Game.areaListener.getTargeterManager().addTargeter(this);
    }

    /**
     * Sets the minimum range in grid coordinates that any point can be away from the
     * parent AbilityActivator and be selectable
     *
     * @param minRange the minimum range for this Targeter
     */

    public void setMinRange(int minRange)
    {
        this.minRange = minRange;
    }

    /**
     * Sets the maximum range in grid coordinates that any point can be away from the
     * parent AbilityActivator and be selectable
     *
     * @param maxRange the maximum range for this Targeter
     */

    public void setMaxRange(int maxRange)
    {
        this.maxRange = maxRange;
    }

    /**
     * Returns the minimum range for selectable points for this Targeter
     *
     * @return the minimum range for selectable points for this Targeter
     */

    public int getMinRange()
    {
        return minRange;
    }

    /**
     * Returns the maximum range for selectable points for this Targeter
     *
     * @return the maximum range for selectable points for this Targeter
     */

    public int getMaxRange()
    {
        return maxRange;
    }

    /**
     * Adds the specified Point to the list of allowed, selectable Points
     *
     * @param point the Point to add
     */

    public void addAllowedPoint(Point point)
    {
        allowedPoints.add(point);
    }

    /**
     * Adds the specified Point to the list of allowed, selectable Points
     *
     * @param location the Point to add
     */

    public void addAllowedPoint(Location location)
    {
        addAllowedPoint(location.toPoint());
    }

    /**
     * Adds all points in the specified List to the list of allowed, selectable
     * Points.  Note that if the List of Points is empty then this Targeter will
     * still be set to only return true from {@link #hasAllowedPoint(Point)} if
     * the Point is in the list.  In other words, the list of allowed points will
     * always be treated as non-empty after a call to this method.
     *
     * @param points the List of Points to add
     */

    public void addAllowedPoints(List<Point> points)
    {
        for (Point point : points) {
            allowedPoints.add(new Point(point));
        }

        treatAllowedPointsAsNonEmpty = true;
    }

    /**
     * Adds the position of the specified creature to the list of selectable Points
     *
     * @param creature the Creature or AbilityActivator
     */

    public void addAllowedCreature(Creature creature)
    {
        allowedPoints.add(creature.getLocation().toPoint());
    }

    /**
     * Adds all the positions of the specified Entities in the List to the set of
     * selectable Points.  Note that if the List of Points is empty then this Targeter will
     * still be set to only return true from {@link #hasAllowedPoint(Point)} if
     * the Point is in the list.  In other words, the list of allowed points will
     * always be treated as non-empty after a call to this method.
     *
     * @param entities the Entites whose positions are to be added
     */

    public void addAllowedCreatures(List<Entity> entities)
    {
        for (Entity entity : entities) {
            allowedPoints.add(entity.getLocation().toPoint());
        }

        treatAllowedPointsAsNonEmpty = true;
    }

    /**
     * Returns the List of allowed selectable points for purposes of highlighting
     * in the interface.  An empty list indicates that all targetable Points are
     * valid.  (Meaning points that are visible, passable, and explored usually)
     *
     * @return the List of all allowed selectable points
     */

    public List<Point> getAllowedPoints()
    {
        return allowedPoints;
    }

    /**
     * Returns true if the List of allowed Points contains a Point equal to the Point p.
     * If the List of allowed Points is empty, assumes all Points are allowed and returns
     * true.
     *
     * @param p the Point to search for
     * @return true if and only if the List contains a Point equal to p, or if the List of
     * allowed Points is empty
     */

    public boolean hasAllowedPoint(Point p)
    {
        if (allowedPoints.isEmpty() && !treatAllowedPointsAsNonEmpty) return true;

        for (Point point : allowedPoints) {
            if (point.equals(p)) return true;
        }

        return false;
    }

    /**
     * Sets whether the current mouse hover state will show a valid or
     * invalid selection.  This state will be used when drawing the interface
     * to provide feedback to the user.
     *
     * @param mouseHoverValid whether the mouse hover state is valid
     */

    protected void setMouseHoverValid(boolean mouseHoverValid)
    {
        this.mouseHoverValid = mouseHoverValid;
    }

    /**
     * Returns true if the current mouse hover state is valid, false otherwise
     *
     * @return true if the current mouse hover state is valid, false otherwise
     */

    public boolean mouseHoverValid()
    {
        return mouseHoverValid;
    }

    /**
     * Sets the current mouse action condition.  This determines what the mouse
     * cursor will look like and provides feedback to the user.
     *
     * @param condition the Condition for the mouse cursor
     */

    protected void setMouseActionCondition(MouseActionList.Condition condition)
    {
        this.mouseActionCondition = condition;
    }

    /**
     * Returns the current mouse action condition of the mouse cursor for this Targeter.
     *
     * @return the current condition for the mouse cursor
     */

    public MouseActionList.Condition getMouseActionCondition()
    {
        return mouseActionCondition;
    }

    /**
     * Returns the mouse x coordinate when this targeter last had its mouse position set
     *
     * @return the mouse x screen coordinate
     */

    public int getMouseX()
    {
        return mouseX;
    }

    /**
     * Returns the mouse y coordinate when this targeter last had its mouse position set
     *
     * @return the mouse y screen coordinate
     */

    public int getMouseY()
    {
        return mouseY;
    }

    /**
     * Returns the grid point corresponding to the last position that
     * the mouse was set to
     *
     * @return the mouse grid point
     */

    public Point getMouseGridPosition()
    {
        return mouseGridPoint;
    }

    /**
     * Returns the screen point corresponding to the center of the grid
     * coordinates of the current mouse location.  Note that this will not
     * usually equal the exact mouse coordinates
     *
     * @return the mouse screen point
     */

    public Point getMouseScreenPosition()
    {
        return AreaUtil.convertGridToScreenAndCenter(mouseGridPoint);
    }

    /**
     * Sets the current mouse position to the specified location with
     * the exact screen coordinates as the center of the specified location.
     * See {@link #setMousePosition(int, int, Point)}
     *
     * @param location the location to set the mouse
     * @return whether the gridPoint has changed since the last time the
     * mouse position was set
     */

    public boolean setMousePosition(Location location)
    {
        Point screenPoint = location.getCenteredScreenPoint();
        return setMousePosition(screenPoint.x, screenPoint.y, location.toPoint());
    }

    /**
     * Sets the current mouse position to the specified gridPoint with
     * the exact screen coordinates as the center of the specified gridPoint.
     * See {@link #setMousePosition(int, int, Point)}
     *
     * @param gridPoint the position to set the mouse in grid coordinates
     * @return whether the gridPoint has changed since the last time the
     * mouse position was set
     */

    public boolean setMousePosition(Point gridPoint)
    {
        Point screenPoint = AreaUtil.convertGridToScreenAndCenter(gridPoint);
        return setMousePosition(screenPoint.x, screenPoint.y, gridPoint);
    }

    /**
     * Sets the current mouse position for this targeter.  Updates any
     * drawing, currently effected targets, and mouse hover state as needed.
     * <p>
     * Returns true if the gridPoint has changed since the last time
     * this function was called, false otherwise
     *
     * @param x         the current x position of the mouse in screen coordinates
     * @param y         the current y position of the mouse in screen coordinates
     * @param gridPoint the current position of the mouse in grid coordinates
     * @return whether the gridPoint has changed since the last time the mouse
     * position was set
     */

    public boolean setMousePosition(int x, int y, Point gridPoint)
    {
        mouseX = x;
        mouseY = y;

        if (gridPoint.equals(mouseGridPoint)) return false;

        // set the grid point if it has changed
        mouseGridPoint = new Point(gridPoint);

        // can only target passable tiles
        boolean targetOK = Game.curCampaign.curArea.isPassable(gridPoint.x, gridPoint.y);

        // can only target visible tiles
        targetOK = targetOK && parent.hasVisibilityInCurrentArea(gridPoint.x, gridPoint.y);

        // player characters can only target explored tiles
        if (parent.getFaction() == Game.ruleset.getFaction(Game.ruleset.getString("PlayerFaction"))) {
            targetOK = targetOK && Game.curCampaign.curArea.getExplored()[gridPoint.x][gridPoint.y];
        }

        // check range conditions
        int range = AreaUtil.distance(mouseGridPoint, this.parent.getLocation().toPoint());
        if (maxRange != 0) {
            targetOK = targetOK && range <= maxRange;
        }

        if (minRange != 0) {
            targetOK = targetOK && range >= minRange;
        }

        targetOK = targetOK && hasAllowedPoint(gridPoint);

        setMouseHoverValid(targetOK);

        return true;
    }

    /**
     * Called during the screen drawing process.  Performs any special
     * drawing for this Targeter.
     *
     * @param as the current GUI animation state
     * @return false if this targeter is not to be drawn at all
     */

    public boolean draw(AnimationState as)
    {
        if (!parent.isPlayerFaction()) return false;

        Game.areaViewer.drawAnimHex(parent.getLocation().getX(), parent.getLocation().getY(), as);

        return true;
    }

    /**
     * Sets the title that the menu for this Targeter will have
     *
     * @param menuTitle the title String
     */

    public void setMenuTitle(String menuTitle)
    {
        this.menuTitle = menuTitle;
    }

    /**
     * Returns the title String for the menu for this Targeter
     *
     * @return this Targeter's menu title String
     */

    public String getMenuTitle()
    {
        return menuTitle;
    }

    /**
     * Shows the right click menu for this targeter.  This will typically have
     * buttons for adding targets, activating the mode, or canceling the targeter.
     *
     * @param x the x screen position for the menu
     * @param y the y screen position for the menu
     */

    public abstract void showMenu(int x, int y);

    /**
     * Performs the appropriate left click action for this Targeter at the
     * last specified mouse position.  This can include activating the targeter and
     * calling the callback if the current gridPoint is a valid selection, adding
     * a target, or other actions for more specialized targeters.
     */

    public abstract void performLeftClickAction();

    /**
     * Returns the callback that when run will cancel and exit this
     * Targeter.
     *
     * @return the cancel callback for this Targeter.
     */

    public Runnable getCancelCallback()
    {
        return cancelCallback;
    }

    /**
     * Returns the callback that when run will activate this callback,
     * finishing it and calling the onTargetSelect callback
     *
     * @return the activate callback for this Targeter
     */

    protected Runnable getActivateCallback()
    {
        return activateCallback;
    }

    /**
     * Sets the callback that is activated (run() is called) when this Targeter is
     * canceled
     *
     * @param callback the callback
     */

    public void setCancelCallback(Runnable callback)
    {
        this.cancelCallback = callback;
    }

    /**
     * Sets the callback that is activated (run() method is called) when
     * this Targeter is activated.
     *
     * @param callback the callback to set
     */

    public void setActivateCallback(Runnable callback)
    {
        this.activateCallback = callback;
    }

    /**
     * Adds the specified argument as an additional argument that will be
     * passed to the activate callback (onTargetSelect) beyond the standard
     * "game" ScriptInterface and this Targeter.
     *
     * @param argument the argument to add
     */

    public void addCallbackArgument(Object argument)
    {
        callbackArguments.add(argument);
    }

    /**
     * Adds the specified arguments as additional arguments that will be
     * passed to the activate callback (onTargetSelect) beyond the standard
     * "game" ScriptInterface and this Targeter.
     *
     * @param arguments the arguments to add
     */

    public void addCallbackArguments(Object[] arguments)
    {
        for (Object arg : arguments) {
            callbackArguments.add(arg);
        }
    }

    /**
     * A thread safe way of canceling this targeter, if it is cancelable.  The targeter will be canceled on the
     * next main thread update.  This method blocks until that time
     */

    public void cancel()
    {
        this.canceled = true;

        Game.mainViewer.updateInterface();

        try {
            synchronized (Game.areaListener.getTargeterManager()) {
                while (Game.areaListener.getTargeterManager().getCurrentTargeter() == this) {
                    Game.areaListener.getTargeterManager().wait();
                }
            }

        } catch (InterruptedException e) {
            // thread was interrupted, can exit
            throw new RuntimeException("Thread was interrupted", e);
        }
    }

    /**
     * Checks if this Targeter is valid to be set as the interface targeter
     *
     * @return true if the Targeter is valid and can be set, false otherwise
     */

    public boolean checkValid()
    {
        if (canceled) return false;

        if (checkValidCallback != null) {
            return checkValidCallback.isValid();
        } else {
            return true;
        }
    }

    /**
     * Sets the callback that is called to check whether this targeter is valid
     * when it is set as the Targeter by the targeterManager
     *
     * @param cb the check valid callback
     */

    public void setCheckValidCallback(CheckValidCallback cb)
    {
        this.checkValidCallback = cb;
    }

    /**
     * Sets the title text for this targeter in the mainviewer
     */

    public void setTitleText()
    {
        String line1 = "Select Target for " + menuTitle;

        String line2 = null;
        if (abilitySlot != null && (abilitySlot.getAbility() instanceof Spell)) {
            Spell spell = (Spell)abilitySlot.getAbility();

            int failure = spell.getSpellFailurePercentage(parent);

            line2 = "Base Spell Failure: " + failure + '%';
        }

        String line3 = null;

        if (mouseHoverValid) {
            int concealment = Game.curCampaign.curArea.getConcealment(parent, mouseGridPoint);

            line3 = "Target Concealment " + concealment;
        }

        Game.mainViewer.setTargetTitleText(line1, line2, line3);
    }

    /**
     * Returns the number of desirable targets currently affected by this targeter.
     * For abilities with a negative effect, this will be the number of hostile targets,
     * while for abilities with a positive effect, this will be the number of friendly
     * targets.  For targeters that don't have enough information to determine whether
     * targets are desirable, returns 0
     *
     * @return the number of desirable targets
     */

    public int getDesirableTargetCount()
    {
        if (abilitySlot == null) return 0;

        if (mouseHoverValid) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Returns the number of undesirable targets currently affected by this targeter.
     * For abilities with a negative effect, this will be the number of friendly targets,
     * while for the abilities with a positive effect, this will be the number of hostile
     * targets.  For targeters that don't have enough information to determine whether
     * targets are desirable, returns 0
     *
     * @return the number of undesirable targets
     */

    public int getUndesirableTargetCount()
    {
        return 0;
    }

    /**
     * An interface used when a targeter is set as the current targeter
     *
     * @author Jared Stephen
     */

    public interface CheckValidCallback
    {
        /**
         * This function is called whenever a targeter is set as the current targeter in the
         * TargeterManager.  If this function returns false, the targeter will not be set
         * and will instead be ignored and removed from the queue if applicable
         *
         * @return true if the targeter is valid and should proceed, false otherwise
         */

        public boolean isValid();
    }

    private class CancelCallback implements Runnable
    {
        @Override
        public void run()
        {
            Game.areaListener.getTargeterManager().cancelCurrentTargeter();
            Game.mainViewer.getMenu().hide();
        }
    }

    private class ActivateCallback implements Runnable
    {
        @Override
        public void run()
        {
            Game.areaListener.getTargeterManager().endCurrentTargeter();
            Game.mainViewer.getMenu().hide();

            if (!scriptable.hasFunction(ScriptFunctionType.onTargetSelect)) {
                Logger.appendToWarningLog("Scriptable " + scriptable.getScriptLocation() +
                        " runs a Targeter but does not have an onTargetSelect function.");
            }

            scriptable.executeFunction(ScriptFunctionType.onTargetSelect, callbackArguments.toArray());
        }
    }
}
