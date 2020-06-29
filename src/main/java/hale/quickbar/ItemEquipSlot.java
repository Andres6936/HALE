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

import de.matthiasmann.twl.Button;
import hale.Game;
import hale.bonus.Bonus;
import hale.entity.Armor;
import hale.entity.EntityManager;
import hale.entity.EquippableItem;
import hale.entity.Inventory;
import hale.entity.PC;
import hale.entity.Weapon;
import hale.entity.WeaponTemplate;
import hale.icon.Icon;
import hale.loading.JSONOrderedObject;
import hale.widgets.RightClickMenu;

/**
 * A quickbar slot for holding an equippable item.  When activated, the
 * specified Item is equipped if not currently equipped and unequipped
 * if currently equipped.
 *
 * @author Jared Stephen
 */

public class ItemEquipSlot extends QuickbarSlot
{
    private EquippableItem item;
    private EquippableItem secondaryItem;
    private PC parent;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("type", "equip");
        data.put("itemID", item.getTemplate().getID());

        if (item.getTemplate().hasQuality()) {
            data.put("itemQuality", item.getQuality().getName());
        }

        if (secondaryItem != null) {
            data.put("secondaryItemID", secondaryItem.getTemplate().getID());

            if (secondaryItem.getTemplate().hasQuality()) {
                data.put("secondaryItemQuality", secondaryItem.getQuality().getName());
            }
        }

        return data;
    }

    /**
     * Creates a new ItemEquipSlot for the specified Item with the parent Creature
     *
     * @param item   the item to equip / unequip
     * @param parent the parent that will be equipping or unequipping the item
     */

    public ItemEquipSlot(EquippableItem item, PC parent)
    {
        this.item = item;
        this.parent = parent;
    }

    /**
     * Returns the Item that this ItemEquipSlot is holding.
     *
     * @return the Item that this ItemEquipSlot is holding.
     */

    public EquippableItem getItem()
    {
        return item;
    }

    /**
     * Attempts to set the secondary item for this ItemEquipSlot to the specified Item.
     * This is only possible in very specific scenarios: if the combination of the primary
     * and secondary item can be wielded in the parent Creature's two hands.
     *
     * @param secondaryItem the item to attempt to add
     * @return true if the secondary item is added successfully, false otherwise
     */

    public boolean setSecondaryItem(EquippableItem secondaryItem)
    {
        if (secondaryItem == null) return false;

        // if a secondary item is already set, this causes trying to add another
        // item to reset the slot
        if (this.secondaryItem != null) return false;

        if (!item.getTemplate().hasPrereqsToEquip(parent)) return false;
        if (!secondaryItem.getTemplate().hasPrereqsToEquip(parent)) return false;

        // make sure the player didn't drop the same item on the slot twice
        if (!parent.inventory.hasBoth(item, secondaryItem)) return false;

        switch (item.getTemplate().getType()) {
            case Weapon:
                switch (secondaryItem.getTemplate().getType()) {
                    case Weapon:
                        return checkWeaponWeapon((Weapon)item, (Weapon)secondaryItem);
                    case Shield:
                        return checkWeaponShield((Weapon)item, (Armor)secondaryItem);
                    default:
                        break;
                }
            case Shield:
                switch (secondaryItem.getTemplate().getType()) {
                    case Weapon:
                        return checkWeaponShield((Weapon)secondaryItem, (Armor)item);
                    default:
                        break;
                }
            default:
                break;
        }

        return false;
    }

    private boolean checkWeaponWeapon(Weapon main, Weapon offHand)
    {
        if (!main.isMelee() || !offHand.isMelee()) return false;

        if (!parent.stats.has(Bonus.Type.DualWieldTraining)) return false;

        if (main.getTemplate().getHanded() == WeaponTemplate.Handed.TwoHanded) {
            return false;
        }

        if (offHand.getTemplate().getHanded() == WeaponTemplate.Handed.TwoHanded) {
            return false;
        }

        // checks are ok, so set the main and secondary items
        item = main;
        secondaryItem = offHand;

        return true;
    }

    private boolean checkWeaponShield(Weapon weapon, Armor shield)
    {
        if (weapon.getTemplate().getHanded() == WeaponTemplate.Handed.TwoHanded) {
            return false;
        }

        // checks are ok, so set the main and secondary items
        item = weapon;
        secondaryItem = shield;

        return true;
    }

    @Override
    public Icon getIcon()
    {
        return item.getTemplate().getIcon();
    }

    @Override
    public String getLabelText()
    {
        switch (item.getTemplate().getType()) {
            case Ammo:
                return Integer.toString(parent.inventory.getTotalQuantity(item));
            default:
                return "";
        }
    }

    @Override
    public boolean isChildActivateable()
    {
        if (parent.inventory.isEquipped(item) && !item.getTemplate().isUnequippable()) {
            return false;
        }

        if (secondaryItem != null && parent.inventory.isEquipped(secondaryItem) &&
                !secondaryItem.getTemplate().isUnequippable()) {
            return false;
        }

        return parent.inventory.canEquip(item, null);
    }

    @Override
    public void childActivate(QuickbarSlotButton button)
    {
        if (!parent.inventory.canEquip(item, null)) return;

        if (secondaryItem != null) {
            if (!parent.inventory.hasBoth(item, secondaryItem)) return;

            if (parent.inventory.isEquipped(item)) {
                // unequip both items
                parent.inventory.getUnequipCallback(parent.inventory.getSlot(item)).run();

                if (parent.inventory.isEquipped(secondaryItem)) {
                    // don't charge AP for the second equip action
                    parent.timer.setFreeMode(true);
                    parent.inventory.getUnequipCallback(parent.inventory.getSlot(secondaryItem)).run();
                    parent.timer.setFreeMode(false);
                }

            } else {
                // equip both items
                parent.inventory.equipItem(item, null);

                if (!parent.inventory.isEquipped(secondaryItem)) {
                    // don't charge AP for the second equip action
                    parent.timer.setFreeMode(true);
                    parent.inventory.equipItem(secondaryItem, null);
                    parent.timer.setFreeMode(false);
                }
            }
        } else {

            Inventory.Slot slot = parent.inventory.getSlot(item);
            if (slot != null) {
                parent.inventory.getUnequipCallback(slot).run();
            } else
                if (parent.inventory.getUnequippedItems().contains(item)) {
                    parent.inventory.getEquipCallback(item, null).run();
                }
        }
    }

    @Override
    public void showExamineWindow(QuickbarSlotButton button)
    {
        item.getExamineDetailsCallback(button.getX(), button.getY()).run();

        if (secondaryItem != null) {
            secondaryItem.getExamineDetailsCallback(button.getX(), button.getY()).run();
        }
    }

    @Override
    public void createRightClickMenu(QuickbarSlotButton button)
    {
        RightClickMenu menu = Game.mainViewer.getMenu();

        String menuTitle = item.getTemplate().getName();
        if (secondaryItem != null) menuTitle = menuTitle + " and " + secondaryItem.getTemplate().getName();

        menu.addMenuLevel(menuTitle);

        String disabledTooltip = null;
        String activateText = null;
        if (parent.inventory.isEquipped(item)) {
            activateText = "Unequip";
            if (!item.getTemplate().isUnequippable()) {
                disabledTooltip = "Item is conjured and cannot be removed.";
            } else {
                disabledTooltip = "Not enough AP to unequip";
            }

        } else {
            activateText = "Equip";
            disabledTooltip = "Not enough AP to equip";
        }

        Button activate = new Button(activateText);
        activate.setEnabled(isActivateable());
        if (!activate.isEnabled()) activate.setTooltipContent(disabledTooltip);
        activate.addCallback(button.getActivateSlotCallback(this));
        menu.addButton(activate);

        Button examine = new Button(item.getTemplate().getName() + " Details");
        examine.addCallback(item.getExamineDetailsCallback(menu.getX(), menu.getY()));
        menu.addButton(examine);

        if (secondaryItem != null) {
            Button examineS = new Button(secondaryItem.getTemplate().getName() + " Details");
            examineS.addCallback(secondaryItem.getExamineDetailsCallback(menu.getX(), menu.getY()));
            menu.addButton(examineS);
        }

        Button clearSlot = new Button("Clear Slot");
        clearSlot.addCallback(button.getClearSlotCallback());
        menu.addButton(clearSlot);

        menu.show();
        // show popup immediately
        if (menu.shouldPopupToggle()) {
            menu.togglePopup();
        }
    }

    @Override
    public String getTooltipText()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Equip ");
        sb.append(item.getLongName());
        if (secondaryItem != null) {
            sb.append(" and ");
            sb.append(secondaryItem.getLongName());
        }

        return sb.toString();
    }

    @Override
    public Icon getSecondaryIcon()
    {
        if (secondaryItem != null) {
            return secondaryItem.getTemplate().getIcon();
        } else {
            return null;
        }
    }

    @Override
    public String getSaveDescription()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Equip ");

        sb.append("\"");
        sb.append(item.getTemplate().getID());
        sb.append("\" \"");
        sb.append(item.getQuality().getName());
        sb.append("\"");

        if (secondaryItem != null) {
            sb.append(" \"");
            sb.append(secondaryItem.getTemplate().getID());
            sb.append("\" \"");
            sb.append(secondaryItem.getQuality().getName());
            sb.append("\"");
        }

        return sb.toString();
    }

    @Override
    public QuickbarSlot getCopy(PC parent)
    {
        ItemEquipSlot slot = new ItemEquipSlot((EquippableItem)EntityManager.getItem(item.getTemplate().getID(),
                item.getQuality()), parent);

        if (this.secondaryItem != null) {
            slot.setSecondaryItem((EquippableItem)EntityManager.getItem(secondaryItem.getTemplate().getID(),
                    secondaryItem.getQuality()));
        }

        return slot;
    }

}
