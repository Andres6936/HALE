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

package hale.view;

import java.util.ArrayList;
import java.util.List;

import hale.Game;
import hale.entity.Creature;
import hale.rules.Skill;
import hale.widgets.ExpandableWidget;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A widget for viewing the set of skills possessed by a given Creature
 *
 * @author Jared Stephen
 */

public class SkillSetViewer extends ScrollPane
{
    private Creature parent;

    private final List<SkillViewer> viewers;

    private int gap;
    private final Content content;

    /**
     * Creates a new SkillSetViewer
     */

    public SkillSetViewer()
    {
        setFixed(ScrollPane.Fixed.HORIZONTAL);

        viewers = new ArrayList<SkillViewer>();

        content = new Content();
        content.setTheme("content");
        setContent(content);
    }

    /**
     * Sets the character being viewed by this SkillSetViewer
     *
     * @param parent
     */

    public void updateContent(Creature parent)
    {
        if (parent != this.parent) {
            this.parent = parent;
            this.viewers.clear();

            content.removeAllChildren();

            for (Skill skill : Game.ruleset.getAllSkills()) {
                if (!skill.canUse(parent)) continue;

                SkillViewer viewer = new SkillViewer(skill);
                viewers.add(viewer);
                content.add(viewer);
            }
        }

        for (SkillViewer viewer : viewers) {
            viewer.update();
        }
    }

    private class Content extends Widget
    {
        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            gap = themeInfo.getParameter("gap", 0);
        }

        @Override
        protected void layout()
        {
            int viewerWidth = Math.max(0, (getInnerWidth() - gap) / 2);

            int curX = getInnerX();
            int curY = getInnerY();

            // layout first column
            int i = 0;

            for (; i < (viewers.size() + 1) / 2; i++) {
                viewers.get(i).setSize(viewerWidth, viewers.get(i).getPreferredHeight());
                viewers.get(i).setPosition(curX, curY);

                curY += viewers.get(i).getHeight() + gap;
            }

            curX = getInnerX() + (getInnerWidth() - gap) / 2 + gap;
            curY = getInnerY();

            // layout second column
            for (; i < viewers.size(); i++) {
                viewers.get(i).setSize(viewerWidth, viewers.get(i).getPreferredHeight());
                viewers.get(i).setPosition(curX, curY);

                curY += viewers.get(i).getHeight() + gap;
            }
        }
    }

    private class SkillViewer extends ExpandableWidget
    {
        private Skill skill;

        private SkillViewer(Skill skill)
        {
            super(skill.getIcon());
            this.skill = skill;
        }

        @Override
        protected void appendDescriptionMain(StringBuilder sb)
        {
            sb.append("<div style=\"font-family: medium-bold;\">");
            sb.append(skill.getNoun()).append("</div>");

            int total = parent.skills.getTotalModifier(skill);
            int ranks = parent.skills.getRanks(skill);
            int modifier = total - ranks;

            sb.append("<div style=\"font-family: medium\"><span style=\"font-family: medium-blue;\">").append(ranks);
            sb.append("</span> + <span style=\"font-family: medium-green;\">").append(modifier);
            sb.append("</span> = <span style=\"font-family: medium-bold;\">").append(total).append("</span>");
            sb.append("</div>");
        }

        @Override
        protected void appendDescriptionDetails(StringBuilder sb)
        {
            if (skill.isRestrictedToARole()) {
                sb.append("<p>Restricted to <span style=\"font-family: red;\">");
                sb.append(skill.getRestrictToRole()).append("</span></p>");
            }

            sb.append("<p>Key Attribute: <span style=\"font-family: blue;\">");
            sb.append(skill.getKeyAttribute().name);
            sb.append("</span></p>");

            if (!skill.isUsableUntrained()) {
                sb.append("<div style=\"font-family: green;\">Requires training</div>");
            }

            if (skill.suffersArmorPenalty()) {
                sb.append("<div style=\"font-family: green;\">Armor Penalty applies</div>");
            }

            sb.append("<div style=\"margin-top: 1em;\">");
            sb.append(skill.getDescription()).append("</div>");
        }
    }
}
