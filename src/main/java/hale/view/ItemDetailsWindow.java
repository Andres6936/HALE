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

package main.java.hale.view;

import java.util.List;

import main.java.hale.Game;
import main.java.hale.ability.Effect;
import main.java.hale.bonus.Bonus;
import main.java.hale.entity.Ammo;
import main.java.hale.entity.Enchantment;
import main.java.hale.entity.Entity;
import main.java.hale.entity.EntityListener;
import main.java.hale.entity.Item;
import main.java.hale.entity.Armor;
import main.java.hale.entity.EquippableItem;
import main.java.hale.entity.Weapon;
import main.java.hale.icon.IconFactory;
import main.java.hale.rules.BaseWeapon;
import main.java.hale.rules.Currency;
import main.java.hale.rules.Recipe;
import main.java.hale.rules.Skill;
import main.java.hale.rules.Weight;
import main.java.hale.widgets.IconViewer;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget for displaying details about a specific Item
 * in a single window.
 *
 * @author Jared Stephen
 */

public class ItemDetailsWindow extends GameSubWindow implements EntityListener
{
    private Item item;
    private HTMLTextAreaModel textAreaModel;

    private String staticDescriptionTop, staticDescriptionBottom;

    /**
     * Create a new ItemDetailsWindow that shows the details for
     * the specified Item
     *
     * @param item the Item to show details for
     */

    public ItemDetailsWindow(Item item)
    {
        setTitle("Details for " + item.getTemplate().getName());
        item.addViewer(this);
        this.item = item;

        DialogLayout layout = new DialogLayout();
        layout.setTheme("content");
        this.add(layout);

        // set up the widgets for the top row
        String titleString = item.getLongName();

        IconViewer iconViewer = new IconViewer();
        iconViewer.setEventHandlingEnabled(false);
        if (item.getTemplate().getIcon() != null) {
            iconViewer.setIcon(item.getTemplate().getIcon());
        } else {
            iconViewer.setIcon(IconFactory.emptyIcon);
        }
        Label title = new Label(titleString);
        title.setTheme("titlelabel");

        DialogLayout.Group topRowV = layout.createParallelGroup(iconViewer, title);

        DialogLayout.Group topRowH = layout.createSequentialGroup(iconViewer);
        topRowH.addGap(10);
        topRowH.addWidget(title);
        topRowH.addGap(10);

        // create widgets for details text area
        textAreaModel = new HTMLTextAreaModel();
        TextArea textArea = new TextArea(textAreaModel);
        ScrollPane textPane = new ScrollPane(textArea);
        textPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        textPane.setTheme("detailspane");

        // set the main top level layout
        Group mainGroupV = layout.createSequentialGroup();
        mainGroupV.addGroup(topRowV);
        mainGroupV.addGap(5);
        mainGroupV.addWidget(textPane);

        Group mainGroupH = layout.createParallelGroup();
        mainGroupH.addGroup(topRowH);
        mainGroupH.addWidget(textPane);

        layout.setHorizontalGroup(mainGroupH);
        layout.setVerticalGroup(mainGroupV);

        // build the unchanging part of the text area content
        buildStaticDescriptionTop();
        buildStaticDescriptionBottom();

        entityUpdated(item);
    }

    @Override
    public void removeListener()
    {
        getParent().removeChild(this);
    }

    @Override
    public void entityUpdated(Entity entity)
    {
        textAreaModel.setHtml(getTextAreaContent(item));

        invalidateLayout();
    }

    /*
     * This overrides the default close behavior of GameSubWindow
     * @see main.java.hale.view.GameSubWindow#run()
     */

    @Override
    public void run()
    {
        removeListener();
        item.removeViewer(this);
    }

    private void appendEnchantments(EquippableItem item, StringBuilder sb)
    {
        List<Enchantment> enchantments = item.getTemplate().getEnchantments();
        if (enchantments.size() > 0) {
            sb.append("<div style=\"margin-bottom: 1em;\">");
            sb.append("<span style=\"font-family: medium-blue;\">Enchantments</span>");
            for (Enchantment enchantment : enchantments) {
                sb.append(enchantment.getBonuses().getDescription());
            }
            sb.append("</div>");
        }
    }

    private void buildStaticDescriptionTop()
    {
        StringBuilder sb = new StringBuilder();

        if (item instanceof Ammo) {
            // don't show the item type string for ammo
            appendAmmoString((Ammo)item, sb);
            appendEnchantments((EquippableItem)item, sb);
        } else
            if (item instanceof EquippableItem) {
                appendItemTypeString((EquippableItem)item, sb);
                appendEnchantments((EquippableItem)item, sb);

                if (item instanceof Armor) {
                    appendArmorString((Armor)item, sb);
                } else
                    if (item instanceof Weapon) {
                        appendWeaponString((Weapon)item, sb);
                    }
            }

        appendItemString(item, sb);

        staticDescriptionTop = sb.toString();
    }

    private void buildStaticDescriptionBottom()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"margin-top: 1em;\">");
        sb.append(item.getTemplate().getDescription());
        sb.append("</div>");

        if (item.getTemplate().isIngredient()) {
            for (Skill skill : Game.ruleset.getAllSkills()) {
                boolean skillSectionStarted = false;

                for (String recipeID : Game.curCampaign.getRecipeIDsForSkill(skill)) {

                    Recipe recipe = Game.curCampaign.getRecipe(recipeID);

                    if (!recipe.isIngredient(item.getTemplate().getID())) continue;

                    if (!skillSectionStarted) {
                        sb.append("<div style=\"font-family: medium-bold; margin-top: 1em;\">");
                        sb.append(skill.getNoun());
                        sb.append(" Recipes");
                        skillSectionStarted = true;
                    }

                    sb.append("<div style=\"font-family: green\">");
                    sb.append(recipe.getName());
                    sb.append("</div>");

                }

                if (skillSectionStarted) {
                    sb.append("</div>");
                }
            }
        }

        staticDescriptionBottom = sb.toString();
    }

    private String getTextAreaContent(Item item)
    {
        StringBuilder sb = new StringBuilder(staticDescriptionTop);

        synchronized (item.getEffects()) {
            for (Effect effect : item.getEffects()) {
                effect.appendDescription(sb);
            }
        }

        sb.append(staticDescriptionBottom);

        return sb.toString();
    }

    private void appendItemString(Item item, StringBuilder sb)
    {
        sb.append("<table style=\"font-family: medium; vertical-align: middle; margin-bottom: 1em;\">");

        if (item.getTemplate().hasQuality()) {
            sb.append("<tr><td style=\"width: 10ex;\">");
            sb.append("Quality");
            sb.append("</td><td style=\"font-family: medium-red\">");
            sb.append(item.getQuality().getName());
            sb.append("</td></tr>");
        }

        sb.append("<tr><td style=\"width: 10ex;\">");
        sb.append("Value");
        sb.append("</td><td style=\"width: 15ex;\">");
        sb.append("<span style=\"font-family: medium-green\">");
        sb.append(new Currency(item.getQualityValue()).shortString());
        sb.append("</span>");
        sb.append("</td></tr>");

        sb.append("<tr><td style=\"width: 10ex;\">");
        sb.append("Weight");
        sb.append("</td><td>");
        sb.append("<span style=\"font-family: medium-blue\">");
        sb.append(Weight.toStringKilograms(item.getTemplate().getWeightInGrams()));
        sb.append("</span>");
        sb.append(" kg");
        sb.append("</td></tr>");

        if (item.getTemplate().isUsable() && item.getTemplate().getUseAP() != 0) {
            sb.append("<tr><td>Use Cost");
            sb.append("</td><td><span style=\"font-family: medium-blue\">");
            sb.append(item.getTemplate().getUseAP() / 100);
            sb.append("</span> AP");
            sb.append("</td></tr>");
        }

        sb.append("</table>");
    }

    private void appendAmmoString(Ammo item, StringBuilder sb)
    {
        sb.append("<div style=\"font-family: medium; margin-bottom: 1em;\">Ammo for ");

        List<BaseWeapon> baseWeapons = item.getTemplate().getUsableBaseWeapons();
        for (int index = 0; index < baseWeapons.size(); index++) {
            sb.append("<span style=\"font-family: medium-blue\">");
            sb.append(baseWeapons.get(index).getName());
            sb.append("</span>");

            if (index != baseWeapons.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("</div>");
    }

    private void appendArmorString(Armor item, StringBuilder sb)
    {
        String armorClass = Game.numberFormat(1).format(item.getQualityModifiedArmorClass());
        String armorPenalty = Game.numberFormat(1).format(item.getQualityModifiedArmorPenalty());
        String movementPenalty = Game.numberFormat(1).format(item.getQualityModifiedMovementPenalty());

        sb.append("<table style=\"font-family: medium; vertical-align: middle; margin-bottom: 1em;\">");

        sb.append("<tr><td style=\"width: 17ex;\">");
        sb.append("Defense");
        sb.append("</td><td style=\"font-family: medium-blue\">");
        sb.append(armorClass);
        sb.append("</td></tr>");

        sb.append("<tr><td>");
        sb.append("Armor Penalty");
        sb.append("</td><td style=\"font-family: medium-red\">");
        sb.append(armorPenalty);
        sb.append("</td></tr>");

        if (item.getTemplate().getMovementPenalty() != 0) {
            sb.append("<tr><td>");
            sb.append("Movement Penalty");
            sb.append("</td><td style=\"font-family: medium-red\">");
            sb.append(movementPenalty);
            sb.append("</td></tr>");
        }

        if (item.getTemplate().getShieldAttackPenalty() != 0) {
            sb.append("<tr><td>");
            sb.append("Attack Penalty");
            sb.append("</td><td style=\"font-family: medium-red\">");
            sb.append(item.getTemplate().getShieldAttackPenalty());
            sb.append("</td></tr>");
        }

        sb.append("</table>");
    }

    private void appendWeaponString(Weapon item, StringBuilder sb)
    {
        float damageMult = 1.0f + (item.getQualityDamageBonus() + item.bonuses.get(Bonus.Type.WeaponDamage)) / 100.0f;
        String damageMin = Game.numberFormat(1).format(((float)item.getTemplate().getMinDamage() * damageMult));
        String damageMax = Game.numberFormat(1).format(((float)item.getTemplate().getMaxDamage() * damageMult));

        sb.append("<table style=\"font-family: medium; vertical-align: middle; margin-bottom: 1em;\">");

        sb.append("<tr><td style=\"width: 13ex;\">");
        sb.append("Damage");
        sb.append("</td><td>");
        sb.append("<span style=\"font-family: medium-red\">");
        sb.append(damageMin);
        sb.append("</span>");
        sb.append(" to ");
        sb.append("<span style=\"font-family: medium-red\">");
        sb.append(damageMax);
        sb.append("</span>");
        sb.append("</td></tr>");

        sb.append("<tr><td>");
        sb.append("Damage Type");
        sb.append("</td><td style=\"font-family: medium-blue\">");
        sb.append(item.getTemplate().getDamageType().getName());
        sb.append("</td></tr>");

        sb.append("<tr><td>");
        sb.append("Critical");
        sb.append("</td><td>");
        sb.append("<span style=\"font-family: medium-green\">");
        if (item.getTemplate().getCriticalThreat() == 100) {
            sb.append("100");
        } else {
            sb.append(item.getTemplate().getCriticalThreat()).append(" - 100");
        }
        sb.append("</span>");
        sb.append(" / x");
        sb.append("<span style=\"font-family: medium-blue\">");
        sb.append(item.getTemplate().getCriticalMultiplier());
        sb.append("</span>");
        sb.append("</td></tr>");

        sb.append("<tr><td>Attack Bonus</td><td><span style=\"font-family: medium-blue\">");
        sb.append(item.getQualityAttackBonus());
        sb.append("</span></td></tr>");

        sb.append("<tr><td>");
        sb.append("Attack Cost");
        sb.append("</td><td>");
        sb.append("<span style=\"font-family: medium-blue\">");
        sb.append(item.getTemplate().getAttackCost() / 100);
        sb.append("</span>");
        sb.append(" AP");
        sb.append("</td></tr>");

        if (item.getTemplate().getMaxStrengthBonus() != 0) {
            sb.append("<tr><td>");
            sb.append("Max Str Bonus");
            sb.append("</td><td style=\"font-family: medium-blue\">");
            sb.append(item.getTemplate().getMaxStrengthBonus());
            sb.append("</td></tr>");
        }

        int minRange = item.getTemplate().getMinRange();
        int maxRange = item.getTemplate().getMaxRange();
        sb.append("<tr><td>");
        sb.append("Range");
        sb.append("</td><td>");
        sb.append("<span style=\"font-family: medium-red\">");
        sb.append(minRange);
        sb.append("</span>");
        if (minRange == maxRange && minRange == 1) {
            sb.append(" hex</td></tr>");
        } else {
            sb.append(" to ");
            sb.append("<span style=\"font-family: medium-red\">");
            sb.append(maxRange);
            sb.append("</span>");
            sb.append(" hexes</td></tr>");
        }

        if (item.getTemplate().getRangePenalty() != 0) {
            String rangePenalty = Game.numberFormat(1).format(((float)item.getTemplate().getRangePenalty() / 20.0f));

            sb.append("<tr><td>");
            sb.append("Range Penalty");
            sb.append("</td><td>");
            sb.append("<span style=\"font-family: medium-blue\">");
            sb.append(rangePenalty);
            sb.append("</span>");
            sb.append(" per hex");
            sb.append("</td></tr>");
        }

        sb.append("</table>");
    }

    private void appendItemTypeString(EquippableItem item, StringBuilder sb)
    {
        sb.append("<div style=\"font-family: medium; margin-bottom: 1em;\">");

        switch (item.getTemplate().getType()) {
            case Weapon:
                Weapon weapon = (Weapon)item;

                sb.append("<span style=\"font-family: medium-blue\">");
                sb.append(weapon.getTemplate().getHanded().name);
                sb.append("</span> <span style=\"font-family: medium-red\">");
                sb.append(weapon.getTemplate().getWeaponType());
                sb.append("</span> ");
                break;
            case Armor:
            case Gloves:
            case Boots:
            case Helmet:
                Armor armor = (Armor)item;

                if (!armor.getTemplate().getArmorType().getName().equals(Game.ruleset.getString("DefaultArmorType"))) {
                    sb.append("<span style=\"font-family: medium-blue\">");
                    sb.append(armor.getTemplate().getArmorType().getName());
                    sb.append("</span> ");
                    break;
                }
            default:
                // do nothing
        }

        sb.append(item.getTemplate().getType());
        sb.append("</div>");
    }
}
