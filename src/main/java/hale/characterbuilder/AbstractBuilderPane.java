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

package hale.characterbuilder;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

/**
 * The Abstract base class for the BuilderPane class
 *
 * @author Jared Stephen
 */

public abstract class AbstractBuilderPane extends Widget
{
    private CharacterBuilder builder;
    private String name;
    private Buildable character;

    private Button next, back;

    /**
     * Create an AbstractBuilderPane with the specified descriptive name.
     *
     * @param builder   the CharacterBuilder containing this BuilderPane
     * @param name      the identifying name for this BuilderPane
     * @param character the character that this BuilderPane will be editing
     */

    public AbstractBuilderPane(CharacterBuilder builder, String name, Buildable character)
    {
        this.builder = builder;
        this.name = name;
        this.character = character;

        next = new Button("Next >>");
        next.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                next();
            }
        });
        next.setTheme("nextbutton");

        back = new Button("<< Back");
        back.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                back();
            }
        });
        back.setTheme("backbutton");

        this.add(back);
        this.add(next);
    }

    @Override
    protected void layout()
    {
        back.setSize(back.getPreferredWidth(), back.getPreferredHeight());
        next.setSize(next.getPreferredWidth(), next.getPreferredHeight());

        back.setPosition(getInnerX(), getInnerBottom() - back.getHeight());
        next.setPosition(back.getRight(), getInnerBottom() - next.getHeight());
    }

    /**
     * returns an additional number of pixels that should be removed from the
     * overall height of the selector pane.  called during layout
     *
     * @return an amount to reomve from the selector pane height
     */

    protected int getAdditionalSelectorPaneHeightLimit()
    {
        return 0;
    }

    /**
     * Returns the back button used to go to the previous active pane
     *
     * @return the back button
     */

    protected Button getBackButton()
    {
        return back;
    }

    /**
     * Returns the next button that is used to go to the next active pane.
     *
     * @return the next button
     */

    protected Button getNextButton()
    {
        return next;
    }

    /**
     * Returns the CharacterBuilder containing this BuilderPane
     *
     * @return the CharacterBuilder containing this BuilderPane
     */

    protected CharacterBuilder getCharacterBuilder()
    {
        return builder;
    }

    /**
     * Returns the character that this BuilderPane is editing
     *
     * @return the character that this BuilderPane is editing
     */

    public Buildable getCharacter()
    {
        return character;
    }

    /**
     * Returns the identifying name of this BuilderPane
     *
     * @return the identifying name of this BuilderPane
     */

    public String getName()
    {
        return name;
    }

    /**
     * Tells this BuilderPane to update based on changes to the Buildable character.
     * The default implementation does nothing.
     */

    protected abstract void updateCharacter();

    /**
     * Called whenever the next button is clicked.  The default implementation
     * activates the next BuilderPane in the list for the CharacterBuilder.  Is
     * overridden by subclasses.
     */

    protected void next()
    {
        AbstractBuilderPane next = getCharacterBuilder().getNextPane(this);
        if (next != null) getCharacterBuilder().setActivePane(next);
    }

    /**
     * Called whenever the back button is clicked.  The default implementation
     * activates the previous BuilderPane in the list for the CharacterBuilder.
     */

    protected void back()
    {
        AbstractBuilderPane prev = getCharacterBuilder().getPreviousPane(this);
        if (prev != null) getCharacterBuilder().setActivePane(prev);
    }

    // make this widget expand to take up all available space in DialogLayouts
    @Override
    public int getPreferredInnerWidth()
    {
        return Short.MAX_VALUE;
    }

    @Override
    public int getPreferredInnerHeight()
    {
        return Short.MAX_VALUE;
    }

    @Override
    public int getMinHeight()
    {
        return 300;
    }
}
