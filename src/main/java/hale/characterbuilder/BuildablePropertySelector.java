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

import main.java.hale.icon.Icon;
import main.java.hale.widgets.IconViewer;
import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;

/**
 * The base class for the selectors used to customize various attributes
 * of a character, such as race, role, and skills.  (Note that Attributes does
 * NOT refer to the primary Stats of Str, Dex, Con, Int, Wis, and Cha)
 *
 * @author Jared Stephen
 */

public class BuildablePropertySelector extends DialogLayout implements PointAllocatorModel.Listener
{
    private PointAllocatorModel pointsModel;
    private int minValue, maxValue;
    private int value;
    private boolean hasValue;

    private boolean selected;
    private boolean selectable;

    private IconViewer iconViewer;
    private Label valueLabel, nameLabel;
    private Button decrement, increment;

    /**
     * Creates a new PropertySelector displaying the specified icon and specified
     * name as a label.
     *
     * @param name     the name to display for this PropertySelector
     * @param icon     the icon to display for this PropertySelector
     * @param hasValue true if this selector should display a value and increment/decrement
     *                 buttons, false otherwise
     */

    public BuildablePropertySelector(String name, Icon icon, boolean hasValue)
    {
        this.hasValue = hasValue;
        this.selected = false;
        this.selectable = false;

        iconViewer = new IconViewer();
        if (icon != null) iconViewer.setIcon(icon);

        nameLabel = new Label(name);
        nameLabel.setTheme("namelabel");

        Group mainH = createSequentialGroup(iconViewer, nameLabel);
        Group mainV = createParallelGroup(iconViewer, nameLabel);
        mainH.addGap("namevalue");

        if (hasValue) {

            decrement = new Button();
            decrement.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    addModelPoints(-1);
                    addValue(-1);
                }
            });
            decrement.setTheme("decrementbutton");

            increment = new Button();
            increment.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    addModelPoints(1);
                    addValue(1);
                }
            });
            increment.setTheme("incrementbutton");

            valueLabel = new Label();
            valueLabel.setTheme("valuelabel");
            mainH.addWidget(valueLabel);
            mainV.addWidget(valueLabel);

            Group decIncH = createParallelGroup(increment, decrement);
            Group decIncV = createSequentialGroup(increment);
            decIncV.addGap("decrementincrement");
            decIncV.addWidget(decrement);

            mainH.addGroup(decIncH);
            mainV.addGroup(decIncV);

            setIncrementDecrementEnabled();
        }

        setHorizontalGroup(mainH);
        setVerticalGroup(mainV);
    }

    /**
     * Sets the name displayed by this selector
     *
     * @param name the name to display
     */

    public void setName(String name)
    {
        nameLabel.setText(name);
    }

    /**
     * Sets the icon displayed by this Selector to the specified icon
     *
     * @param icon the icon to display
     */

    public void setIcon(Icon icon)
    {
        iconViewer.setIcon(icon);
    }

    /**
     * The specified number of points will be allocated to the PointAllocatorModel
     * assigned to this Selector.  If there is no PointAllocatorModel assigned,
     * nothing is done.  This method is called by the increment and decrement buttons
     * when left clicked.
     * <p>
     * Note that this method can be overridden if more complex behavior is desired, such
     * as varying the number of points allocated in the model from either +1 or -1 from
     * the increment and decrement buttons.
     *
     * @param points the number of points that will be allocated
     */

    protected void addModelPoints(int points)
    {
        if (pointsModel != null) pointsModel.allocatePoints(points);
    }

    @Override
    public void allocatorModelUpdated()
    {
        setIncrementDecrementEnabled();
    }

    /**
     * Sets the PointAllocatorModel for this Selector.  This model is used
     * to manage the total number of points available to a set of Selectors.
     *
     * @param model the PointAllocatorModel to use
     */

    public void setPointAllocatorModel(PointAllocatorModel model)
    {
        this.pointsModel = model;
        model.addListener(this);
    }

    /**
     * Returns the current value displayed by this BuildablePropertySelector.
     * Returns 0 if this selector does not have a value.
     *
     * @return the current value displayed by this Widget
     */

    public int getValue()
    {
        return value;
    }

    /**
     * Adds the specified value to the value currently displayed by this PropertySelector.
     * This method is called by the increment and decrement buttons when modifying the value.
     *
     * @param valueToAdd the value to add
     */

    protected void addValue(int valueToAdd)
    {
        setValue(value + valueToAdd);
    }

    /**
     * Sets the value currently displayed by this PropertySelector.  This function does
     * not respect the values set using {@link #setMinMaxValue(int, int)}
     *
     * @param value the value to display
     * @throws UnsupportedOperationException if this Selector does not have value
     */

    protected void setValue(int value)
    {
        if (!hasValue) throw new UnsupportedOperationException("This selector does not have value.");

        this.value = value;
        valueLabel.setText(Integer.toString(value));

        setIncrementDecrementEnabled();
    }

    /**
     * Returns the minimum allowable value for this Selector
     *
     * @return the minimum allowable value
     */

    public int getMinValue()
    {
        return minValue;
    }

    /**
     * Returns the maximum allowable value for this Selector
     *
     * @return the maximum allowable value
     */

    public int getMaxValue()
    {
        return maxValue;
    }

    /**
     * Sets the minimum and maximum values that this Selector can have.  The increment and
     * decrement buttons will be greyed out and unclickable if the current value equals
     * maxValue or the current value equals minValue, respectively.
     *
     * @param minValue the minimum value this selector can attain
     * @param maxValue the maximum value this selector can attain
     * @throws UnsupportedOperationException if this Selector does not have value
     */

    protected void setMinMaxValue(int minValue, int maxValue)
    {
        if (!hasValue) throw new UnsupportedOperationException("This selector does not have value.");

        this.minValue = minValue;
        this.maxValue = maxValue;

        setIncrementDecrementEnabled();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);

        nameLabel.setEnabled(enabled);

        if (hasValue) {
            valueLabel.setEnabled(enabled);
            setIncrementDecrementEnabled();
        }
    }

    /**
     * Returns the increment button used to increment the value in this Selector
     *
     * @return the increment button
     */

    protected de.matthiasmann.twl.Button getIncrementButton()
    {
        return increment;
    }

    /**
     * Returns the decrement Button used to decrement the value in this Selector
     *
     * @return the decrement button
     */

    protected de.matthiasmann.twl.Button getDecrementButton()
    {
        return decrement;
    }

    /**
     * Sets the enabled state of the increment and decrement buttons based on whether there
     * are sufficient remaining points in the PointAllocatorModel and whether the min or
     * max value has been reached.
     */

    protected void setIncrementDecrementEnabled()
    {
        boolean modelOK = pointsModel != null ? pointsModel.getRemainingPoints() > 0.0 : true;

        increment.setEnabled(isEnabled() && value < maxValue && modelOK);
        decrement.setEnabled(isEnabled() && value > minValue);
    }

    /**
     * Returns whether this Widget is capable of being selected
     *
     * @return whether this Widget is capable of being selected
     */

    public boolean isSelectable()
    {
        return selectable;
    }

    /**
     * Sets whether this Widget is capable of being selected.
     *
     * @param selectable whether this Widget is capable of being
     *                   selected on a mouse click
     */

    public void setSelectable(boolean selectable)
    {
        this.selectable = selectable;
    }

    /**
     * Returns true if this Widget is currently in the highlighted, selected state
     *
     * @return true if and only if this Widget is selected
     */

    public boolean isSelected()
    {
        return selected;
    }

    /**
     * Sets whether this Selector is currently selected
     *
     * @param selected whether this selector is currently selected
     */

    public void setSelected(boolean selected)
    {
        this.selected = selected;
        getAnimationState().setAnimationState(Button.STATE_SELECTED, selected);
    }

    @Override
    protected boolean handleEvent(Event evt)
    {
        AnimationState animationState = getAnimationState();

        if (evt.isMouseEvent()) {
            boolean hover = (evt.getType() != Event.Type.MOUSE_EXITED) && isMouseInside(evt);

            if (animationState.getAnimationState(Button.STATE_HOVER) != hover) {
                if (hover) onMouseHover();

                animationState.setAnimationState(Button.STATE_HOVER, hover);
            }
        }
        switch (evt.getType()) {
            case MOUSE_BTNDOWN:
                if (isEnabled() && isSelectable()) {
                    animationState.setAnimationState(Button.STATE_PRESSED, true);
                }
                break;
            case MOUSE_BTNUP:
                animationState.setAnimationState(Button.STATE_PRESSED, false);
                if (isEnabled() && isSelectable() && isMouseInside(evt)) {
                    setSelected(!selected);
                    onMouseClick();
                }
                break;
            case MOUSE_WHEEL:
                return false;
            default:
        }

        return evt.isMouseEvent();
    }

    /**
     * Called whenever the mouse starts hovering over this Widget.  Default
     * implementation does nothing.
     */

    protected void onMouseHover()
    {
    }

    /**
     * Called whenever the user clicks on this Widget, selecting it.  Default
     * implementation does nothing.
     */

    protected void onMouseClick()
    {
    }

    // a button sending hover events to the main class
    private class Button extends de.matthiasmann.twl.Button
    {
        @Override
        protected boolean handleEvent(Event evt)
        {
            BuildablePropertySelector.this.handleEvent(evt);

            return super.handleEvent(evt);
        }
    }

    // a label sending hover events to the main class
    private class Label extends de.matthiasmann.twl.Label
    {
        private Label()
        {
        }

        private Label(String text)
        {
            super(text);
        }

        @Override
        protected boolean handleEvent(Event evt)
        {
            BuildablePropertySelector.this.handleEvent(evt);

            return super.handleEvent(evt);
        }
    }
}
