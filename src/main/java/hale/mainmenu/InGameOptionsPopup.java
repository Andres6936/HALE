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

package main.java.hale.mainmenu;

import main.java.hale.Game;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The popup window used for setting in game options such as difficulty
 *
 * @author Jared Stephen
 */

public class InGameOptionsPopup extends PopupWindow
{
    private Content content;

    public InGameOptionsPopup(Widget parent)
    {
        super(parent);

        content = new Content();
        add(content);

        setCloseOnClickedOutside(false);
        setCloseOnEscape(true);
    }

    private void applySettings()
    {
        Game.ruleset.getDifficultyManager().setCurrentDifficulty(content.difficultySelector.getSelectedDifficulty());
    }

    private class Content extends Widget
    {
        private int sectionGap, largeGap;

        private DifficultySelector difficultySelector;
        private Label title;
        private Button accept, cancel;

        private Content()
        {
            title = new Label();
            title.setTheme("titlelabel");
            add(title);

            difficultySelector = new DifficultySelector();
            add(difficultySelector);

            accept = new Button();
            accept.setTheme("acceptbutton");
            accept.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    applySettings();
                    InGameOptionsPopup.this.closePopup();
                }
            });
            add(accept);

            cancel = new Button();
            cancel.setTheme("cancelbutton");
            cancel.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    InGameOptionsPopup.this.closePopup();
                }
            });
            add(cancel);
        }

        @Override
        protected void layout()
        {
            int centerX = getInnerX() + getInnerWidth() / 2;

            title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
            title.setPosition(centerX - title.getWidth() / 2, getInnerY());

            difficultySelector.setSize(difficultySelector.getPreferredWidth(), difficultySelector.getPreferredHeight());
            difficultySelector.setPosition(centerX - difficultySelector.getWidth() / 2, title.getBottom() + largeGap);

            accept.setSize(accept.getPreferredWidth(), accept.getPreferredHeight());
            cancel.setSize(cancel.getPreferredWidth(), cancel.getPreferredHeight());

            accept.setPosition(centerX - sectionGap / 2 - accept.getWidth(), difficultySelector.getBottom() + largeGap);
            cancel.setPosition(centerX + sectionGap / 2, difficultySelector.getBottom() + largeGap);
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            sectionGap = themeInfo.getParameter("sectiongap", 0);
            largeGap = themeInfo.getParameter("largegap", 0);
        }

        @Override
        public int getPreferredWidth()
        {
            return difficultySelector.getPreferredWidth() + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            return title.getPreferredHeight() + difficultySelector.getPreferredHeight() +
                    accept.getPreferredHeight() + largeGap * 2 + getBorderVertical();
        }
    }
}
