/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package hale.mainmenu;

import java.util.ArrayList;
import java.util.List;

import hale.DifficultyManager;
import hale.Game;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget for selecting a difficulty setting from among the options present
 * in the difficulty manager
 *
 * @author Jared
 */

public class DifficultySelector extends Widget
{
    private DifficultyManager manager;

    private ToggleButton activeDifficultyButton;

    private HTMLTextAreaModel textAreaModel;
    private TextArea textArea;

    private Label difficultyLabel;
    private List<ToggleButton> buttons;

    /**
     * Creates a new DifficultySelector widget
     */

    public DifficultySelector()
    {
        difficultyLabel = new Label();
        difficultyLabel.setTheme("difficultylabel");
        add(difficultyLabel);

        buttons = new ArrayList<ToggleButton>();
        manager = Game.ruleset.getDifficultyManager();
        for (String level : manager.getDifficultyLevels()) {
            ToggleButton button = new SelectorButton(level);
            button.setTheme("difficultybutton");

            if (level.equals(manager.getCurrentDifficulty())) {
                button.setActive(true);
                activeDifficultyButton = button;
            }

            buttons.add(button);
            add(button);
        }

        textAreaModel = new HTMLTextAreaModel();
        textAreaModel.setHtml(manager.getDifficultyDescription(manager.getCurrentDifficulty()));
        textArea = new TextArea(textAreaModel);
        textArea.setTheme("difficultydescription");
        add(textArea);
    }

    @Override
    protected void layout()
    {
        int centerX = getInnerX() + getInnerWidth() / 2;

        difficultyLabel.setSize(difficultyLabel.getPreferredWidth(), difficultyLabel.getPreferredHeight());
        difficultyLabel.setPosition(centerX - difficultyLabel.getWidth() / 2, getInnerY());

        int curX = getInnerX();
        int curY = difficultyLabel.getBottom();
        int maxY = curY;

        for (ToggleButton button : buttons) {
            button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
            button.setPosition(curX, curY);

            curX = button.getRight();
            maxY = button.getBottom();
        }

        textArea.setSize(getInnerWidth(), Math.min(textArea.getPreferredHeight(), textArea.getMaxHeight()));
        textArea.setPosition(getInnerX(), maxY);
    }

    @Override
    public int getPreferredWidth()
    {
        int width = 0;
        for (ToggleButton button : buttons) {
            width += button.getPreferredWidth();
        }

        return width + getBorderHorizontal();
    }

    @Override
    public int getPreferredHeight()
    {
        int height = 0;
        for (ToggleButton button : buttons) {
            height = Math.max(height, button.getPreferredHeight());
        }

        height += Math.min(textArea.getPreferredHeight(), textArea.getMaxHeight());

        return height + difficultyLabel.getPreferredHeight() + getBorderVertical();
    }

    /**
     * Returns the ID String of the currently selected difficulty
     *
     * @return the ID of the current difficulty
     */

    public String getSelectedDifficulty()
    {
        return activeDifficultyButton.getText();
    }

    private void difficultyButtonPressed(SelectorButton button)
    {
        if (activeDifficultyButton != null) {
            activeDifficultyButton.setActive(false);
        }

        button.setActive(true);
        activeDifficultyButton = button;

        textAreaModel.setHtml(manager.getDifficultyDescription(button.difficulty));
    }

    private class SelectorButton extends ToggleButton implements Runnable
    {
        private String difficulty;

        private SelectorButton(String difficulty)
        {
            super(difficulty);
            this.difficulty = difficulty;
            addCallback(this);
        }

        @Override
        public void run()
        {
            difficultyButtonPressed(this);
        }
    }
}
