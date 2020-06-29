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
import de.matthiasmann.twl.Button;

import hale.Game;
import hale.defaultability.MouseActionList;
import hale.entity.Creature;
import hale.util.Point;
import hale.widgets.RightClickMenu;

/**
 * A Targeter that allows the selection of one or more points from
 * a pre-specified list of allowed points.  Selected points may or
 * may not be required to be unique.
 *
 * @author Jared Stephen
 */

public class ListTargeter extends Targeter
{
    private int numberOfSelections;
    private boolean uniqueSelectionsRequired;

    private List<Point> selectedPoints;

    /**
     * Create a new ListTargeter with an empty list of allowed points
     * and no selections yet made.  By default, only one selection
     * will be needed.
     *
     * @param parent     the parent Creature that is targeting using this Targeter
     * @param scriptable the scriptable responsible for managing the script that
     *                   will be used in the callback
     * @param slot       optional AbilitySlot that can be stored along with this Targeter
     */

    public ListTargeter(Creature parent, Scriptable scriptable, AbilitySlot slot)
    {
        super(parent, scriptable, slot);
        this.selectedPoints = new ArrayList<Point>();
        this.numberOfSelections = 1;
        this.uniqueSelectionsRequired = false;
    }

    /*
     * (non-Javadoc)
     * @see main.java.hale.effect.Targeter#hasSelectedTargets()
     */

    @Override
    public boolean hasSelectedTargets()
    {
        return numberOfSelections == selectedPoints.size();
    }

    /**
     * Sets the number of selections that must be made for this
     * Targeter to have a valid set of targets.  By default, this
     * is equal to 1.  Note that passing numberOfSelections equal to any
     * value less than 1 will have the effect of setting the value to 1.
     *
     * @param numberOfSelections the number of selections that must be made
     *                           for this Targeter
     */

    public void setNumberOfSelections(int numberOfSelections)
    {
        if (numberOfSelections < 1) numberOfSelections = 1;

        this.numberOfSelections = numberOfSelections;
    }

    /**
     * Returns the total number of selections that must be made for this
     * Targeter to have a valid set of targets.
     *
     * @return the total number of selections for this Targeter
     */

    public int getNumberOfSelections()
    {
        return numberOfSelections;
    }

    /**
     * Returns the first selected point for this Targeter.  For Targeters
     * with more than 1 numberOfSelections, use {@link #getSelected(int)}
     *
     * @return the first selected Point for this Targeter
     */

    public Point getSelected()
    {
        if (selectedPoints.size() < 1) return null;

        return this.selectedPoints.get(0);
    }

    /**
     * Returns the Creature at the first selected Point for this Targeter.
     * If there is no Creature at the Point, returns null.
     *
     * @return the Creature at the first selected Point for this Targeter
     */

    public Creature getSelectedCreature()
    {
        if (selectedPoints.size() < 1) return null;

        return Game.curCampaign.curArea.getCreatureAtGridPoint(selectedPoints.get(0));
    }

    /**
     * Returns the selected point at the specified index.  For Targeters
     * with only 1 numberOfSelections, you can also use {@link #getSelected()}
     *
     * @param index the index of the point to retrieve
     * @return the selected Point at the specified index
     */

    public Point getSelected(int index)
    {
        if (selectedPoints.size() < index + 1) return null;

        return this.selectedPoints.get(index);
    }

    /**
     * Returns a List of all the Creatures at the selected points for this Targeter.
     * If there are no Creatures at any selected points, returns an empty List.
     *
     * @return the List of all Creatures at the selected points
     */

    public List<Creature> getSelectedCreatures()
    {
        List<Creature> creatures = new ArrayList<Creature>();

        for (Point p : selectedPoints) {
            Creature creature = Game.curCampaign.curArea.getCreatureAtGridPoint(p);
            if (creature != null) creatures.add(creature);
        }

        return creatures;
    }

    /**
     * Sets whether multiple target selections must be unique.  If false,
     * then the same Point can be selected multiple times.  If true, then
     * each selected Point must be unique.
     *
     * @param uniqueSelectionsRequired whether multiple target selections
     *                                 must be unique
     */

    public void setUniqueSelectionsRequired(boolean uniqueSelectionsRequired)
    {
        this.uniqueSelectionsRequired = uniqueSelectionsRequired;
    }

    /*
     * Returns true if and only if the specified point can be selected.
     * Enforced unique selection if that is specified
     */

    private boolean canSelectPoint(Point gridPoint)
    {
        if (!hasAllowedPoint(gridPoint)) return false;

        if (uniqueSelectionsRequired) {
            for (Point p : selectedPoints) {
                if (p.equals(gridPoint)) return false;
            }
        }

        return true;
    }

    /**
     * @see main.java.hale.ability.Targeter#setMousePosition(int, int, main.java.hale.util.Point)
     * <p>
     * This implementation sets the mouse state based on the set of
     * allowed points.  Hovering over an allowed point sets the mouse state
     * to valid, otherwise the mouse state is invalid.
     */

    @Override
    public boolean setMousePosition(int x, int y, Point gridPoint)
    {
        if (!super.setMousePosition(x, y, gridPoint)) return false;

        // set mouse action condition
        MouseActionList.Condition condition = MouseActionList.Condition.Cancel;

        if (canSelectPoint(gridPoint)) {
            int selectionsLeft = numberOfSelections - this.selectedPoints.size();

            if (selectionsLeft == 1) {
                condition = MouseActionList.Condition.TargetSelect;
            } else
                if (selectionsLeft > 1) {
                    condition = MouseActionList.Condition.TargetSelectAdd;
                }
        }

        setMouseActionCondition(condition);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see main.java.hale.effect.Targeter#showMenu()
     */

    public void showMenu(int x, int y)
    {
        RightClickMenu menu = Game.mainViewer.getMenu();

        menu.clear();

        menu.setPosition(x, y);
        menu.addMenuLevel(this.getMenuTitle());

        if (canSelectPoint(this.getMouseGridPosition())) {
            int selectionsLeft = numberOfSelections - this.selectedPoints.size();

            if (selectionsLeft == 1) {
                Button button = new Button("Select & Activate");
                button.addCallback(new AddPointCallback());
                button.addCallback(getActivateCallback());

                menu.addButton(button);

            } else
                if (selectionsLeft > 1) {
                    Button button = new Button("Select");
                    button.addCallback(new AddPointCallback());

                    menu.addButton(button);

                    // add use all remaining selections and activate button
                    if (!this.uniqueSelectionsRequired) {
                        button = new Button("Select All & Activate");
                        AddAllCallback callback = new AddAllCallback(selectionsLeft);
                        button.addCallback(callback);
                        button.addCallback(getActivateCallback());

                        menu.addButton(button);
                    }
                }
        }

        if (this.isCancelable()) {
            Button button = new Button("Cancel");
            button.addCallback(getCancelCallback());

            menu.addButton(button);
        }

        menu.show();
    }

    /*
     * (non-Javadoc)
     * @see main.java.hale.effect.Targeter#performLeftClickAction()
     */

    public void performLeftClickAction()
    {
        if (!canSelectPoint(this.getMouseGridPosition())) return;

        int selectionsLeft = numberOfSelections - this.selectedPoints.size();

        if (selectionsLeft == 1) {
            selectedPoints.add(new Point(this.getMouseGridPosition()));

            //activate the Targeter callback
            this.getActivateCallback().run();

        } else
            if (selectionsLeft > 1) {
                selectedPoints.add(new Point(this.getMouseGridPosition()));
            }
    }

    @Override
    public boolean draw(AnimationState as)
    {
        if (!super.draw(as)) {
            return false;
        }

        for (Point p : getAllowedPoints()) {
            Game.areaViewer.drawGreenHex(p, as);
        }

        return true;
    }

    private class AddPointCallback implements Runnable
    {
        @Override
        public void run()
        {
            selectedPoints.add(new Point(getMouseGridPosition()));
        }
    }

    private class AddAllCallback implements Runnable
    {
        private int selections;

        private AddAllCallback(int selections)
        {
            this.selections = selections;
        }

        @Override
        public void run()
        {
            for (int i = 0; i < selections; i++) {
                selectedPoints.add(new Point(getMouseGridPosition()));
            }
        }
    }
}
