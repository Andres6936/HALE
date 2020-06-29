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

package hale.loading;

import org.lwjgl.opengl.GL11;

import hale.Game;
import hale.resource.Sprite;
import hale.util.Point;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ProgressBar;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The popup that appears when the game is currently loading some assets
 *
 * @author Jared Stephen
 */

public class LoadingWaitPopup extends PopupWindow
{
    private Sprite bgSprite;
    private Point bgSpriteOffset;

    private Label title;
    private Label description;
    private ProgressBar progressBar;
    private int labelBarGap, titleGap;

    private Content content;

    private LoadingTaskList taskList;
    private String curDescription;

    /**
     * Creates a new LoadingWaitPopup
     *
     * @param parent    the parent Widget used to determine the Widget tree to block input from
     * @param titleText the title display while this Popup is open
     */

    public LoadingWaitPopup(Widget parent, String titleText)
    {
        super(parent);

        setCloseOnEscape(false);
        setCloseOnClickedOutside(false);

        content = new Content();
        add(content);

        title = new Label(titleText);
        title.setTheme("titlelabel");
        content.add(title);

        description = new Label(" ");
        description.setTheme("descriptionlabel");
        content.add(description);

        progressBar = new ProgressBar();
        content.add(progressBar);
    }

    /**
     * Sets the Sprite that will be drawn as the background of this popup
     *
     * @param sprite the Sprite to use as the background
     */

    public void setBGSprite(Sprite sprite)
    {
        this.bgSprite = sprite;
        bgSpriteOffset = new Point();
        if (bgSprite != null) {
            bgSpriteOffset.x = (Game.config.getResolutionX() - bgSprite.getWidth()) / 2;
            bgSpriteOffset.y = (Game.config.getResolutionY() - bgSprite.getHeight()) / 2;
        }
    }

    /**
     * Sets the LoadingTaskList that is used by this Popup to determine the
     * progress bar completion amount and description text
     *
     * @param list the LoadingTaskList
     */

    public void setLoadingTaskList(LoadingTaskList list)
    {
        this.taskList = list;
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        labelBarGap = themeInfo.getParameter("labelBarGap", 0);
        titleGap = themeInfo.getParameter("titleGap", 0);
    }

    private class Content extends Widget
    {
        @Override
        protected void paintWidget(GUI gui)
        {

            if (bgSprite != null) {
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                //bgSprite.draw(bgSpriteOffset.x, bgSpriteOffset.y);
                bgSprite.draw(0, 0, getWidth(), getHeight());
            }
        }

        @Override
        protected void layout()
        {
            int centerX = getInnerX() + getInnerWidth() / 2;

            progressBar.setSize(getInnerWidth(), progressBar.getPreferredHeight());
            progressBar.setPosition(getInnerX(), getInnerBottom() - progressBar.getHeight());

            description.setSize(description.getPreferredWidth(), description.getPreferredHeight());
            description.setPosition(centerX - description.getWidth() / 2,
                    progressBar.getY() - labelBarGap - description.getHeight());

            title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
            title.setPosition(centerX - title.getWidth() / 2,
                    description.getY() - titleGap - title.getHeight());
        }

        @Override
        public int getPreferredWidth()
        {
            return Game.config.getResolutionX();
        }

        @Override
        public int getPreferredHeight()
        {
            return Game.config.getResolutionY();
        }
    }

    @Override
    protected void paint(GUI gui)
    {
        super.paint(gui);

        String newDescription = taskList.getCurrentDescription();
        if (newDescription != curDescription) {
            description.setText(newDescription);
            curDescription = newDescription;
        }

        progressBar.setValue(taskList.getCompletedFraction());
    }
}
