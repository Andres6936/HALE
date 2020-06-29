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

package main.java.hale.mainmenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import main.java.hale.Game;
import main.java.hale.entity.PC;
import main.java.hale.rules.Role;
import main.java.hale.view.CharacterWindow;
import main.java.hale.widgets.BasePortraitViewer;
import main.java.hale.widgets.TextAreaNoInput;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget for displaying basic information about a character, with a button
 * to show detailed information via the {@link main.java.hale.view.CharacterWindow}
 * and optionally a button with a supplied callback to add / remove the character
 * from the party
 *
 * @author Jared Stephen
 */

public class CharacterSelector extends Widget
{
    private int numRoleLines;

    private int expandBoxY, expandBoxBorder, expandExtraPadding;

    private Button details;
    private HTMLTextAreaModel textAreaModel;
    private TextArea textArea;

    private Button addRemove;
    private ExpandBox expand;

    private boolean characterMeetsLevelRequirements;
    private UniqueCharacter character;
    private PC pc;
    private Widget parent;

    private BasePortraitViewer portrait;

    private DeleteButton deleteButton;

    private PartyFormationWindow newGameWindow;

    private boolean showDeleteButtons;

    private Set<String> charactersInParties;

    /**
     * Creates a new CharacterSelector for the set of creatures contained in the specified UniqueCharacter.
     * Any CharacterWindows that are created are added to the specified parent widget
     *
     * @param character the set of characters to view
     * @param parent    the parent widget to add any details windows to
     */

    public CharacterSelector(UniqueCharacter character, Widget parent, Set<String> charactersInParties)
    {
        this.charactersInParties = charactersInParties;
        if (charactersInParties == null) {
            this.charactersInParties = Collections.emptySet();
        }

        this.character = character;
        this.pc = character.getBestCreature();

        characterMeetsLevelRequirements = pc != null;
        // use the first creature if none meet the requirements
        if (pc == null) {
            pc = character.iterator().next();
        }

        this.parent = parent;

        textAreaModel = new HTMLTextAreaModel();
        textArea = new TextAreaNoInput(textAreaModel);
        textArea.setTheme("description");
        textAreaModel.setHtml(getDescription());
        add(textArea);

        details = new Button();
        details.setTheme("detailsbutton");
        details.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                CharWindow window = new CharWindow();
                CharacterSelector.this.parent.add(window);
                window.updateContent(CharacterSelector.this.pc);
                window.setPosition(details.getRight(), details.getY() - 150);
            }
        });
        add(details);

        portrait = new BasePortraitViewer(pc);
        portrait.setEnableEventHandling(false);
        add(portrait);

        expand = new ExpandBox();

        setDeleteExpandState();
    }

    /**
     * Create a new CharacterSelector for the specified creature.  Any CharacterWindows
     * that are created by this Widget are added to the supplied widget
     *
     * @param pc     the creature to view
     * @param parent the parent widget to add details windows to
     */

    public CharacterSelector(PC pc, Widget parent)
    {
        this(new UniqueCharacter(pc), parent, null);
    }

    /**
     * Causes this CharacterSelector to show a delete button, even if it is only
     * viewing a single creature
     */

    public void showDeleteButtons()
    {
        showDeleteButtons = true;

        setDeleteExpandState();
    }

    private void setDeleteExpandState()
    {
        if (expand != null) {
            removeChild(expand);

            if (character.size() > 1) {
                add(expand);
            }
        }

        if (showDeleteButtons) {
            if (deleteButton != null) removeChild(deleteButton);

            if (character.size() <= 1) {
                deleteButton = new DeleteButton(character.getFirstCreature(), null);
                add(deleteButton);
            }
        }
    }

    /**
     * Returns the Creature that this CharacterSelector was created with or is currently selected
     *
     * @return the PC
     */

    public PC getCreature()
    {
        return pc;
    }

    /**
     * Sets the new game window, which will be refreshed if all creatures in the
     * character being viewed are deleted
     *
     * @param window the window to refresh
     */

    public void setNewGameWindow(PartyFormationWindow window)
    {
        this.newGameWindow = window;
    }

    private void setSelectedCreature(PC pc)
    {
        this.pc = pc;
        textAreaModel.setHtml(getDescription());

        setDeleteExpandState();
    }

    /**
     * Returns the ID string of the Creature that this CharacterSelector was
     * created with
     *
     * @return the ID String of this CharacterSelector's creature
     */

    public String getCreatureID()
    {
        return pc.getTemplate().getID();
    }

    /**
     * Sets the text and callback for the add remove button for this
     * character selector.  By default, the add remove button is not shown.
     *
     * @param text     the text to display on the add / remove button.  If null is passed,
     *                 the add remove button is cleared and not shown on this Widget
     * @param callback the Callback that will be run() whenever the button is clicked
     */

    public void setAddRemoveButton(String text, Runnable callback)
    {
        if (text == null) {
            if (addRemove != null) {
                removeChild(addRemove);
                addRemove = null;
            }
        } else {
            addRemove = new Button(text);
            addRemove.setTheme("addremovebutton");
            addRemove.addCallback(callback);
            addRemove.setEnabled(characterMeetsLevelRequirements);

            if (!addRemove.isEnabled()) {
                addRemove.setTooltipContent("No version of this character meets the level requirements");
            }

            add(addRemove);
        }

        invalidateLayout();
    }

    /**
     * Sets the enabled state of the add remove button to the specified value.  If no
     * add remove button is currently present in this Widget, no action is performed.
     * <p>
     * Note that if no character meets the level requirements, this widget will remain
     * disabled even if this method is called with "true"
     *
     * @param enabled whether the add remove button should be set to enabled or disabled
     */

    public void setAddRemoveEnabled(boolean enabled)
    {
        if (addRemove != null) {
            addRemove.setEnabled(enabled && characterMeetsLevelRequirements);
        }
    }

    private String getDescription()
    {
        numRoleLines = 0;

        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"font-family: medium-bold-white;\">").append(pc.getTemplate().getName()).append("</div>");

        sb.append("<div style=\"font-family: medium-white;\">");
        sb.append(pc.getTemplate().getGender()).append(' ');
        sb.append("<span style=\"font-family: medium-green;\">").append(pc.getTemplate().getRace().getName()).append("</span>");
        sb.append("</div>");

        sb.append("<div style=\"font-family: white; margin-bottom: 1em\">");
        for (String roleID : pc.roles.getRoleIDs()) {
            Role role = Game.ruleset.getRole(roleID);
            int level = pc.roles.getLevel(role);

            sb.append("<p>");
            sb.append("Level <span style=\"font-family: white;\">").append(level).append("</span> ");
            sb.append("<span style=\"font-family: red;\">").append(role.getName()).append("</span>");
            sb.append("</p>");

            numRoleLines++;
        }
        sb.append("</div>");

        return sb.toString();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        expandBoxY = themeInfo.getParameter("expandboxy", 0);
        expandBoxBorder = themeInfo.getParameter("expandboxborder", 0);
        expandExtraPadding = themeInfo.getParameter("expandExtraPadding", 0);
    }

    @Override
    public int getPreferredInnerWidth()
    {
        return portrait.getPreferredWidth() + textArea.getPreferredWidth();
    }

    @Override
    public int getPreferredInnerHeight()
    {
        int height = textArea.getPreferredHeight();
        height += details.getPreferredHeight();
        if (addRemove != null) {
            height += addRemove.getPreferredHeight();
        }

        if (getChildIndex(expand) >= 0) {
            height += expandExtraPadding;
        }

        return Math.max(height, portrait.getPreferredHeight());
    }

    @Override
    protected void layout()
    {
        portrait.setSize(portrait.getPreferredWidth(), portrait.getPreferredHeight());
        portrait.setPosition(getInnerX(), getInnerY() + (getInnerHeight() - portrait.getHeight()) / 2);

        textArea.setPosition(portrait.getRight(), getInnerY());
        textArea.setSize(textArea.getPreferredWidth(), textArea.getPreferredHeight());

        int availWidth = getInnerRight() - portrait.getRight();

        details.setSize(details.getPreferredWidth(), details.getPreferredHeight());
        details.setPosition(portrait.getRight() + availWidth / 2 - details.getWidth() / 2,
                getInnerBottom() - details.getHeight());

        expand.setSize(textArea.getWidth() - expandBoxBorder * 2, expand.getPreferredHeight());
        expand.setPosition(textArea.getX() + expandBoxBorder, getInnerY() + expandBoxY);

        if (addRemove != null) {
            addRemove.setSize(addRemove.getPreferredWidth(), addRemove.getPreferredHeight());
            addRemove.setPosition(portrait.getRight() + availWidth / 2 - addRemove.getWidth() / 2,
                    details.getY() - addRemove.getHeight());
        }

        if (deleteButton != null) {
            deleteButton.setSize(deleteButton.getPreferredWidth(), deleteButton.getPreferredHeight());
            deleteButton.setPosition(getInnerRight() - deleteButton.getWidth() - expandBoxBorder,
                    getInnerY() + expandBoxY + expandBoxBorder);
        }
    }

    private class CharWindow extends CharacterWindow
    {
        public CharWindow()
        {
            setTheme("characterwindow");

            hideExportButton();
        }

        // override the close callback
        @Override
        public void run()
        {
            parent.removeChild(this);
        }
    }

    private class ExpandBox extends Label implements CallbackWithReason<Label.CallbackReason>
    {
        private boolean boxHover;
        private int rowHeight;

        private Button expand;
        private TextArea box;
        private HTMLTextAreaModel textAreaModel;

        private ExpandBox()
        {
            textAreaModel = new HTMLTextAreaModel();
            box = new TextArea(textAreaModel);
            box.setTheme("box");
            if (!characterMeetsLevelRequirements) {
                box.setEnabled(false);
                box.getAnimationState().setAnimationState(STATE_DISABLED, true);
            }
            add(box);

            expand = new Button(getAnimationState());
            expand.setTheme("expandbutton");
            expand.getModel().addStateCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    updateHover();
                }
            });
            expand.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    openPopup();
                }
            });
            add(expand);

            addCallback(this);

            updateBoxText();
        }

        public void updateBoxText()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<div style=\"font-family: white\">");
            for (String roleID : pc.roles.getRoleIDs()) {
                Role role = Game.ruleset.getRole(roleID);
                int level = pc.roles.getLevel(role);

                sb.append("<p>");
                sb.append("Level <span style=\"font-family: white;\">").append(level).append("</span> ");
                sb.append("<span style=\"font-family: red;\">").append(role.getName()).append("</span>");
                sb.append("</p>");
            }
            sb.append("</div>");

            textAreaModel.setHtml(sb.toString());
        }

        private void openPopup()
        {
            Popup popup = new Popup(parent);
            popup.openPopup();

            popup.setPosition(getX(), getInnerBottom());
            popup.setSize(getWidth(), popup.getPreferredHeight());
        }

        // label clicked callback
        @Override
        public void callback(Label.CallbackReason reason)
        {
            openPopup();
        }

        private void updateHover()
        {
            getAnimationState().setAnimationState(Label.STATE_HOVER, boxHover || expand.getModel().isHover());
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            rowHeight = themeInfo.getParameter("rowHeight", 0);
        }

        @Override
        public int getPreferredInnerHeight()
        {
            return Math.max(numRoleLines * rowHeight, expand.getPreferredHeight());
        }

        @Override
        protected void layout()
        {
            expand.setSize(expand.getPreferredWidth(), getPreferredInnerHeight());
            expand.setPosition(getInnerRight() - expand.getWidth(),
                    getInnerY() + (getInnerHeight() - expand.getHeight()) / 2);

            box.setSize(getInnerWidth(), getInnerHeight());
            box.setPosition(getInnerX(), getInnerY());
        }

        @Override
        protected void handleMouseHover(Event evt)
        {
            if (evt.isMouseEvent()) {
                boolean newHover = evt.getType() != Event.Type.MOUSE_EXITED;
                if (newHover != boxHover) {
                    boxHover = newHover;
                    updateHover();
                }
            }
        }
    }

    private class Popup extends PopupWindow
    {
        private Popup(Widget parent)
        {
            super(parent);

            setTheme("characterselectorpopup");

            add(new PopupContent(this));
        }
    }

    private class PopupContent extends Widget
    {
        private List<CharacterButton> selectors;
        private List<DeleteButton> deleteButtons;

        private PopupContent(PopupWindow popup)
        {
            setTheme("content");

            selectors = new ArrayList<CharacterButton>();
            deleteButtons = new ArrayList<DeleteButton>();

            for (PC pc : character) {
                CharacterButton button = new CharacterButton(pc, popup);
                add(button);
                selectors.add(button);

                DeleteButton deleteButton = new DeleteButton(pc, popup);
                if (showDeleteButtons) {
                    add(deleteButton);
                }
                deleteButtons.add(deleteButton);
                button.addDeleteButton(deleteButton);
            }
        }

        @Override
        public int getPreferredHeight()
        {
            int height = getBorderVertical();

            for (CharacterButton child : selectors) {
                height += child.getPreferredHeight();
            }

            return height;
        }

        @Override
        protected void layout()
        {
            int curY = getInnerY();

            for (int i = 0; i < selectors.size(); i++) {
                CharacterButton selectButton = selectors.get(i);
                DeleteButton deleteButton = deleteButtons.get(i);

                selectButton.setSize(getInnerWidth(), selectButton.getPreferredHeight());
                selectButton.setPosition(getInnerX(), curY);

                deleteButton.setSize(deleteButton.getPreferredWidth(), deleteButton.getPreferredHeight());
                deleteButton.setPosition(selectButton.getInnerRight() - deleteButton.getWidth(),
                        selectButton.getInnerY());

                curY = selectButton.getBottom();
            }
        }
    }

    private class DeleteButton extends Button implements Runnable
    {
        private PC pc;
        private PopupWindow parent;

        private DeleteButton(PC pc, PopupWindow parent)
        {
            this.pc = pc;
            this.parent = parent;
            addCallback(this);

            if (pc.getTemplate().isPregenerated()) {
                this.setEnabled(false);
                setTooltipContent("This character is pregenerated and may not be deleted.");
            } else
                if (charactersInParties.contains(pc.getTemplate().getID())) {
                    this.setEnabled(false);
                    setTooltipContent("This character is in one or more parties and may not be deleted.");
                }
        }

        @Override
        public void run()
        {
            ConfirmationPopup popup = new ConfirmationPopup(CharacterSelector.this);

            StringBuilder sb = new StringBuilder();
            sb.append("Delete ");
            sb.append(pc.getTemplate().getName());

            sb.append(", Level ");
            sb.append(pc.roles.getTotalLevel());
            sb.append(" ");
            sb.append(pc.roles.getBaseRole().getName());

            sb.append("?");

            popup.setTitleText(sb.toString());

            popup.setWarningText("This action is permanent and cannot be undone.");

            popup.addCallback(new DeleteCallback(pc, parent));

            popup.openPopupCentered();
        }
    }

    private class DeleteCallback implements Runnable
    {
        private PC pc;
        private PopupWindow parent;

        private DeleteCallback(PC pc, PopupWindow parent)
        {
            this.pc = pc;
            this.parent = parent;
        }

        @Override
        public void run()
        {
            character.deleteCreature(pc);

            if (character.size() == 0) {
                if (newGameWindow != null) {
                    newGameWindow.removeSelector(CharacterSelector.this);
                }
            } else
                if (pc == CharacterSelector.this.pc) {
                    PC best = character.getBestCreature();

                    if (best == null) {
                        newGameWindow.removeSelector(CharacterSelector.this);
                    } else {
                        setSelectedCreature(character.getBestCreature());
                    }
                }

            if (parent != null) {
                parent.closePopup();
            }
        }
    }

    private class CharacterButton extends ToggleButton implements Runnable
    {
        private int rowHeight, numRows;
        private DeleteButton deleteButton;

        private PopupWindow popup;
        private PC pc;

        private TextArea textArea;
        private HTMLTextAreaModel textAreaModel;

        private CharacterButton(PC pc, PopupWindow popup)
        {

            if (!character.meetsLevelConstraints(pc)) {
                setEnabled(false);
            }

            if (pc == CharacterSelector.this.pc) {
                setActive(true);
            }

            this.popup = popup;
            this.pc = pc;

            textAreaModel = new HTMLTextAreaModel();

            numRows = 0;

            StringBuilder sb = new StringBuilder();
            sb.append("<div style=\"font-family: white\">");
            for (String roleID : pc.roles.getRoleIDs()) {
                Role role = Game.ruleset.getRole(roleID);
                int level = pc.roles.getLevel(role);

                sb.append("<p>");
                sb.append("Level <span style=\"font-family: white;\">").append(level).append("</span> ");
                sb.append("<span style=\"font-family: red;\">").append(role.getName()).append("</span>");
                sb.append("</p>");

                numRows++;
            }
            sb.append("</div>");

            textAreaModel.setHtml(sb.toString());

            textArea = new TextAreaNoInput(textAreaModel);
            textArea.setTheme("textarea");
            add(textArea);

            addCallback(this);
        }

        public void addDeleteButton(DeleteButton deleteButton)
        {
            this.deleteButton = deleteButton;
        }

        // button click callback

        @Override
        public void run()
        {
            popup.closePopup();

            setSelectedCreature(pc);

            expand.updateBoxText();
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            rowHeight = themeInfo.getParameter("rowHeight", 0);

            if (!isEnabled()) {
                int minLevel = Game.curCampaign.getMinStartingLevel();
                int maxLevel = Game.curCampaign.getMaxStartingLevel();

                if (minLevel == maxLevel) {
                    setTooltipContent("All characters must be level " + minLevel);
                } else {
                    setTooltipContent("All characters must be from level " + minLevel + " to " + maxLevel);
                }
            }
        }

        @Override
        public int getPreferredInnerHeight()
        {
            return Math.max(rowHeight * numRows, this.deleteButton.getPreferredHeight());
        }

        @Override
        protected void layout()
        {
            textArea.setPosition(getInnerX(), getInnerY());
            textArea.setSize(getInnerWidth(), getInnerHeight());
        }
    }
}
