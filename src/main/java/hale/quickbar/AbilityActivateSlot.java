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

package hale.quickbar;

import java.util.Iterator;
import java.util.List;

import de.matthiasmann.twl.Button;
import hale.Game;
import hale.ability.Ability;
import hale.ability.AbilityActivateCallback;
import hale.ability.AbilityExamineCallback;
import hale.ability.AbilitySlot;
import hale.ability.ScriptFunctionType;
import hale.entity.PC;
import hale.icon.Icon;
import hale.loading.JSONOrderedObject;
import hale.widgets.RightClickMenu;


/**
 * A QuickbarSlot for holding an Ability.  When activated, it
 * runs the onActivate for an available AbilitySlot holding the Ability if possible.
 * Label text is the standard cooldown text for the AbilitySlot.  If multiple AbilitySlots
 * have this Ability readied, then this return values based either the first AbilitySlot
 * found that has cooldownRounds = 0, or if none of those are available, the AbilitySlot
 * with the smallest cooldownRounds remaining.
 *
 * @author Jared Stephen
 */

public class AbilityActivateSlot extends QuickbarSlot
{
    private final String abilityID;
    private PC parent;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("type", "ability");
        data.put("abilityID", abilityID);

        return data;
    }

    /**
     * Creates a new AbilityActivateSlot for the specified Ability
     *
     * @param ability the Ability
     * @param parent  the activator for this Ability
     */

    public AbilityActivateSlot(Ability ability, PC parent)
    {
        this.abilityID = ability.getID();
        this.parent = parent;
    }

    @Override
    public Icon getIcon()
    {
        return parent.abilities.getUpgradedIcon(abilityID);
    }

    public String getAbilityName()
    {
        return parent.abilities.getUpgradedName(abilityID);
    }

    @Override
    public String getLabelText()
    {
        AbilitySlot slot = getBestSlot();
        if (slot != null) {
            return slot.getLabelText();
        } else {
            return "";
        }
    }

    @Override
    public boolean isChildActivateable()
    {
        Ability ability = Game.ruleset.getAbility(abilityID);

        if (!Game.isInTurnMode() && !ability.canActivateOutsideCombat()) return false;
        if (!parent.timer.canPerformAction(ability.getAPCost())) return false;

        for (AbilitySlot slot : parent.abilities.getSlotsWithReadiedAbility(ability)) {
            if (slot.getCooldownRoundsLeft() == 0) return true;
        }

        return false;
    }

    @Override
    public void childActivate(QuickbarSlotButton button)
    {
        AbilitySlot slot = getBestSlot();
        if (slot == null) return;

        if (slot.canActivate()) {
            new AbilityActivateCallback(slot, ScriptFunctionType.onActivate).run();
        } else
            if (slot.canDeactivate()) {
                new AbilityActivateCallback(slot, ScriptFunctionType.onDeactivate).run();
            }
    }

    @Override
    public void showExamineWindow(QuickbarSlotButton button)
    {
        AbilityExamineCallback cb = new AbilityExamineCallback(Game.ruleset.getAbility(abilityID), button, parent);
        cb.setWindowCenter(button.getX(), button.getY());
        cb.run();
    }

    @Override
    public void createRightClickMenu(QuickbarSlotButton button)
    {
        RightClickMenu menu = Game.mainViewer.getMenu();
        menu.addMenuLevel(getAbilityName());

        Button activate = new Button("Activate");
        activate.setEnabled(isActivateable());
        activate.addCallback(button.getActivateSlotCallback(this));
        menu.addButton(activate);

        Button examine = new Button("View Details");
        AbilityExamineCallback cb = new AbilityExamineCallback(Game.ruleset.getAbility(abilityID), button, parent);
        cb.setWindowCenter(menu.getX(), menu.getY());
        examine.addCallback(cb);
        menu.addButton(examine);

        Button clearSlot = new Button("Clear Slot");
        clearSlot.addCallback(button.getClearSlotCallback());
        menu.addButton(clearSlot);

        menu.show();
        // show popup immediately
        if (menu.shouldPopupToggle()) {
            menu.togglePopup();
        }
    }

    private AbilitySlot getBestSlot()
    {
        Ability ability = Game.ruleset.getAbility(abilityID);

        List<AbilitySlot> slots = parent.abilities.getSlotsWithReadiedAbility(ability);

        if (slots.isEmpty()) return null;

        Iterator<AbilitySlot> iter = slots.iterator();
        AbilitySlot bestSlot = iter.next();

        while (iter.hasNext()) {
            AbilitySlot nextSlot = iter.next();

            if (nextSlot.getCooldownRoundsLeft() < bestSlot.getCooldownRoundsLeft()) {
                bestSlot = nextSlot;
            }
        }

        return bestSlot;
    }

    @Override
    public String getTooltipText()
    {
        return "Activate " + getAbilityName();
    }

    @Override
    public Icon getSecondaryIcon()
    {
        return null;
    }

    @Override
    public String getSaveDescription()
    {
        return "Ability \"" + abilityID + "\"";
    }

    @Override
    public QuickbarSlot getCopy(PC parent)
    {
        return new AbilityActivateSlot(Game.ruleset.getAbility(this.abilityID), parent);
    }
}
