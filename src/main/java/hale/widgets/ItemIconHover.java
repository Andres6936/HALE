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

package hale.widgets;

import java.util.List;

import hale.Game;
import hale.bonus.Bonus;
import hale.bonus.BonusList;
import hale.entity.Armor;
import hale.entity.Enchantment;
import hale.entity.EquippableItem;
import hale.entity.Item;
import hale.entity.Weapon;
import hale.rules.Currency;
import hale.rules.Weight;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A tooltip for an item icon, showing basic information about the item
 *
 * @author Jared Stephen
 */

public class ItemIconHover extends TextArea
{
    private HTMLTextAreaModel textAreaModel;

    private ItemIconViewer parent;
    private Item item;

    private String emptyHoverText;
    private String valueDescription;
    private int valuePercentage;

    private String requiresText;

    /**
     * Creates a new ItemIconTooltip
     *
     * @param item        the item that this hover will display information for
     * @param hoverSource the parent widget that created this hover
     */

    public ItemIconHover(Item item, ItemIconViewer hoverSource)
    {
        this.textAreaModel = new HTMLTextAreaModel();
        this.setModel(textAreaModel);

        valueDescription = "Value";
        valuePercentage = 100;

        this.parent = hoverSource;
        this.item = item;
    }

    @Override
    public int getPreferredWidth()
    {
        // the + 1 here prevents wrap around that sometimes occurs; most likely a bug with
        // de.matthiasmann.twl.TextArea
        return super.getPreferredInnerWidth() + getBorderHorizontal() + 1;
    }

    /**
     * Sets the text specifying the missing requirement for this item
     *
     * @param text the text
     */

    public void setRequiresText(String text)
    {
        this.requiresText = text;
    }

    /**
     * Set the text that is displayed when this hover is not displaying
     * information on an item
     *
     * @param text the text to display
     */

    public void setEmptyHoverText(String text)
    {
        this.emptyHoverText = text;
    }

    /**
     * Returns the widget responsible for the creation of this hover
     *
     * @return the hover source widget
     */

    public Widget getHoverSource()
    {
        return parent;
    }

    /**
     * Sets the value being displayed by this ItemIconTooltip
     *
     * @param description the label for the value
     * @param percentage  the percentage multiplier for the value being displayed
     */

    public void setValue(String description, int percentage)
    {
        this.valueDescription = description;
        this.valuePercentage = percentage;
    }

    @Override
    protected boolean handleEvent(Event evt)
    {
        // don't swallow any events
        return false;
    }

    /**
     * Sets the text being displayed by this tooltip
     */

    public void updateText()
    {
        StringBuilder sb = new StringBuilder();

        if (item == null) {
            appendEmptyText(sb);
        } else {
            appendItemText(sb);
        }

        textAreaModel.setHtml(sb.toString());
    }

    private void appendEmptyText(StringBuilder sb)
    {
        sb.append("<div style=\"font-family: medium-bold;\">");
        sb.append(emptyHoverText);
        sb.append("</div>");
    }

    private void appendItemText(StringBuilder sb)
    {
        sb.append("<div style=\"font-family: medium-bold;\">");

        sb.append(item.getLongName());
        sb.append("</div>");

        if (requiresText != null) {
            sb.append("<div style=\"font-family: red\">");
            sb.append("Requires ");
            sb.append(requiresText);
            sb.append("</div>");
        }

        sb.append(valueDescription);
        sb.append(": <span style=\"font-family: blue;\">");

        Currency qualityValue = new Currency(item.getQualityValue());

        sb.append(qualityValue.shortString(valuePercentage));
        sb.append("</span>");

        // append weight information
        sb.append("<div>Weight: <span style=\"font-family: blue;\">");
        sb.append(Weight.toStringKilograms(item.getTemplate().getWeightInGrams()));
        if (parent.getQuantity() == 1) {
            sb.append("</span> kg</div>");
        } else
            if (parent.getQuantity() == Integer.MAX_VALUE) {
                sb.append("</span> kg each</div>");
            } else {
                sb.append("</span> kg each / <span style=\"font-family: blue;\">");
                sb.append(Weight.toStringKilograms(item.getTemplate().getWeightInGrams() * parent.getQuantity()));
                sb.append("</span> kg total</div>");
            }


        // get all of the enchantment bonuses on this item
        BonusList allBonuses = new BonusList();
        if (item instanceof EquippableItem) {
            List<Enchantment> enchantments = ((EquippableItem)item).getTemplate().getEnchantments();
            for (Enchantment enchantment : enchantments) {
                allBonuses.addAll(enchantment.getBonuses());
            }
        }

        if (item instanceof Weapon) {
            Weapon weapon = (Weapon)item;

            sb.append("<div>Base Damage: ");

            float damageMult = 1.0f + (weapon.getQualityDamageBonus() + weapon.bonuses.get(Bonus.Type.WeaponDamage)) / 100.0f;
            float damageMin = ((float)weapon.getTemplate().getMinDamage() * damageMult);
            float damageMax = ((float)weapon.getTemplate().getMaxDamage() * damageMult);

            sb.append("<span style=\"font-family: red;\">");
            sb.append(Game.numberFormat(1).format(damageMin));
            sb.append("</span>");

            sb.append(" to <span style=\"font-family: red;\">");
            sb.append(Game.numberFormat(1).format(damageMax));
            sb.append("</span> ");
            sb.append(weapon.getTemplate().getDamageType().getName());
            sb.append("</div>");

            // show attack with enchantment bonuses
            sb.append("<div>Attack: <span style=\"font-family: blue;\">");
            int attackBonus = weapon.getQualityAttackBonus();
            for (Bonus bonus : allBonuses.getBonusesOfType(Bonus.Type.WeaponAttack)) {
                attackBonus += bonus.getValue();
            }
            if (attackBonus > 0) sb.append("+");
            sb.append(attackBonus);
            sb.append("</span>, Cost: <span style=\"font-family: green;\">");
            sb.append(weapon.getTemplate().getAttackCost() / 100);
            sb.append("</span> AP</div>");

        } else
            if (item instanceof Armor) {
                Armor armor = (Armor)item;

                if (armor.getQualityModifiedArmorClass() > 0.0) {
                    sb.append("<div>Defense <span style=\"font-family: green;\">");
                    sb.append(Game.numberFormat(1).format(armor.getQualityModifiedArmorClass()));
                    sb.append("</span></div>");
                }

                if (armor.getQualityModifiedArmorPenalty() > 0.0) {
                    sb.append("<div>Armor Penalty <span style=\"font-family: red;\">");
                    sb.append(Game.numberFormat(1).format(armor.getQualityModifiedArmorPenalty()));
                    sb.append("</span></div>");
                }

                if (armor.getQualityModifiedMovementPenalty() > 0.0) {
                    sb.append("<div>Movement Penalty <span style=\"font-family: red;\">");
                    sb.append(Game.numberFormat(1).format(armor.getQualityModifiedMovementPenalty()));
                    sb.append("</span></div>");
                }
            }

        if (item.getTemplate().isQuest()) {
            sb.append("<div style=\"font-family: green\">Quest Item</div>");
        }

        // append enchantments
        for (Bonus bonus : allBonuses) {
            switch (bonus.getType()) {
                case WeaponAttack:
                    // we have already shown these types of bonuses
                    break;
                default:
                    sb.append("<div>");
                    bonus.appendDescription(sb);
                    sb.append("</div>");
            }
        }
    }
}