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

package hale.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hale.Game;
import hale.ability.Ability;
import hale.ability.AbilitySelectionList;
import hale.entity.Creature;
import hale.entity.EntityManager;
import hale.entity.Inventory;
import hale.entity.WeaponTemplate;
import hale.icon.SubIcon;
import hale.resource.ResourceManager;
import hale.resource.ResourceType;
import hale.util.Logger;
import hale.util.Point;
import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;
import hale.util.SimpleJSONParser;

public class Race
{
    private final String id;
    private final String name;

    private final String icon;

    private final int movementCost;
    private final WeaponTemplate defaultWeapon;
    private final boolean playerSelectable;
    private final List<RacialType> racialTypes;
    private final String descriptionFile;

    private final String maleForegroundIcon;
    private final String maleBackgroundIcon;
    private final String maleEarsIcon;

    private final String femaleForegroundIcon;
    private final String femaleBackgroundIcon;
    private final String femaleEarsIcon;

    private final String maleClothesIcon;
    private final String femaleClothesIcon;

    private final Map<Integer, List<String>> abilitySelectionLists;

    private final String subIconRaceString;
    private final Map<SubIcon.Type, Point> iconOffsets;

    private final List<String> abilities;

    private final List<String> randomMaleNames, randomFemaleNames, hairAndBeardColors, skinColors;
    private final boolean hasBeard, hasHair;
    private final String defaultBeardColor, defaultHairColor, defaultSkinColor;
    private final int defaultBeardIndex, defaultHairIndex;
    private final List<Integer> selectableBeardIndices, selectableHairIndices;

    private final int baseStr, baseDex, baseCon, baseInt, baseWis, baseCha;

    private final boolean showDetailedDescription;

    private final List<Inventory.Slot> restrictedSlots;

    public Race(String id, SimpleJSONParser parser)
    {
        this.id = id;
        this.name = parser.get("name", id);

        String defaultWeaponID = parser.get("defaultWeapon", null);
        this.defaultWeapon = (WeaponTemplate)EntityManager.getItemTemplate(defaultWeaponID);

        parser.setWarnOnMissingKeys(false);

        this.movementCost = parser.get("baseMovementCost", 1000);
        this.descriptionFile = parser.get("descriptionFile", "descriptions/races/" + name + ResourceType.HTML.getExtension());
        this.icon = parser.get("icon", null);
        this.playerSelectable = parser.get("playerSelectable", false);
        this.showDetailedDescription = parser.get("showDetailedDescription", true);

        if (parser.containsKey("baseAttributes")) {
            SimpleJSONArray attrArray = parser.getArray("baseAttributes");

            Iterator<SimpleJSONArrayEntry> iter = attrArray.iterator();

            this.baseStr = iter.next().getInt(10);
            this.baseDex = iter.next().getInt(10);
            this.baseCon = iter.next().getInt(10);
            this.baseInt = iter.next().getInt(10);
            this.baseWis = iter.next().getInt(10);
            this.baseCha = iter.next().getInt(10);
        } else {
            this.baseStr = 10;
            this.baseDex = 10;
            this.baseCon = 10;
            this.baseInt = 10;
            this.baseWis = 10;
            this.baseCha = 10;
        }

        List<Inventory.Slot> restrictedSlots = new ArrayList<Inventory.Slot>();
        if (parser.containsKey("restrictedSlots")) {
            SimpleJSONArray array = parser.getArray("restrictedSlots");
            for (SimpleJSONArrayEntry entry : array) {
                String slotID = entry.getString();
                restrictedSlots.add(Inventory.Slot.valueOf(slotID));
            }
        }
        this.restrictedSlots = Collections.unmodifiableList(restrictedSlots);

        abilities = new ArrayList<String>();
        if (parser.containsKey("abilities")) {
            SimpleJSONArray array = parser.getArray("abilities");
            for (SimpleJSONArrayEntry entry : array) {
                abilities.add(entry.getString());
            }
        }
        ((ArrayList<String>)abilities).trimToSize();

        racialTypes = new ArrayList<RacialType>();
        if (parser.containsKey("racialTypes")) {
            SimpleJSONArray array = parser.getArray("racialTypes");
            for (SimpleJSONArrayEntry entry : array) {
                racialTypes.add(Game.ruleset.getRacialType(entry.getString()));
            }
        }
        ((ArrayList<RacialType>)racialTypes).trimToSize();

        this.hasBeard = parser.get("hasBeard", true);
        this.hasHair = parser.get("hasHair", true);
        this.defaultBeardIndex = parser.get("defaultBeardIndex", 1);
        this.defaultHairIndex = parser.get("defaultHairIndex", 1);
        this.defaultBeardColor = parser.get("defaultBeardColor", null);
        this.defaultHairColor = parser.get("defaultHairColor", null);
        this.defaultSkinColor = parser.get("defaultSkinColor", null);

        List<Integer> selectableHairIndices = new ArrayList<Integer>();
        if (parser.containsKey("selectableHairIndices")) {
            SimpleJSONArray array = parser.getArray("selectableHairIndices");
            for (SimpleJSONArrayEntry entry : array) {
                selectableHairIndices.add(entry.getInt(1));
            }
        }
        this.selectableHairIndices = Collections.unmodifiableList(selectableHairIndices);

        List<Integer> selectableBeardIndices = new ArrayList<Integer>();
        if (parser.containsKey("selectableBeardIndices")) {
            SimpleJSONArray array = parser.getArray("selectableBeardIndices");
            for (SimpleJSONArrayEntry entry : array) {
                selectableBeardIndices.add(entry.getInt(1));
            }
        }
        this.selectableBeardIndices = Collections.unmodifiableList(selectableBeardIndices);


        List<String> hairAndBeardColors = new ArrayList<String>();
        if (parser.containsKey("hairAndBeardColors")) {
            SimpleJSONArray array = parser.getArray("hairAndBeardColors");
            for (SimpleJSONArrayEntry entry : array) {
                hairAndBeardColors.add(entry.getString());
            }
        }
        this.hairAndBeardColors = Collections.unmodifiableList(hairAndBeardColors);

        List<String> skinColors = new ArrayList<String>();
        if (parser.containsKey("skinColors")) {
            SimpleJSONArray array = parser.getArray("skinColors");
            for (SimpleJSONArrayEntry entry : array) {
                skinColors.add(entry.getString());
            }
        }
        this.skinColors = Collections.unmodifiableList(skinColors);

        this.subIconRaceString = parser.get("subIconRaceString", null);
        if (parser.containsKey("icons")) {
            SimpleJSONObject obj = parser.getObject("icons");

            this.maleBackgroundIcon = obj.get("maleBackground", null);
            this.maleForegroundIcon = obj.get("maleForeground", null);
            this.maleEarsIcon = obj.get("maleEars", null);
            this.femaleBackgroundIcon = obj.get("femaleBackground", null);
            this.femaleForegroundIcon = obj.get("femaleForeground", null);
            this.femaleEarsIcon = obj.get("femaleEars", null);
            this.maleClothesIcon = obj.get("maleClothes", null);
            this.femaleClothesIcon = obj.get("femaleClothes", null);
        } else {
            this.maleBackgroundIcon = null;
            this.maleForegroundIcon = null;
            this.maleEarsIcon = null;
            this.femaleBackgroundIcon = null;
            this.femaleForegroundIcon = null;
            this.femaleEarsIcon = null;
            this.maleClothesIcon = null;
            this.femaleClothesIcon = null;
        }

        iconOffsets = new HashMap<SubIcon.Type, Point>();
        if (parser.containsKey("iconOffsets")) {
            SimpleJSONObject obj = parser.getObject("iconOffsets");

            for (String key : obj.keySet()) {
                SubIcon.Type type = SubIcon.Type.valueOf(key);

                SimpleJSONArray array = obj.getArray(key);
                Iterator<SimpleJSONArrayEntry> iter = array.iterator();

                int x = iter.next().getInt(0);
                int y = iter.next().getInt(0);

                iconOffsets.put(type, new Point(x, y));
            }
        }

        abilitySelectionLists = new HashMap<Integer, List<String>>();
        if (parser.containsKey("abilitySelectionsFromList")) {
            SimpleJSONObject obj = parser.getObject("abilitySelectionsFromList");

            for (String listID : obj.keySet()) {
                SimpleJSONArray array = obj.getArray(listID);

                for (SimpleJSONArrayEntry entry : array) {
                    int level = entry.getInt(0);

                    addAbilitySelectionListAtLevel(listID, level);
                }
            }
        }

        randomMaleNames = new ArrayList<String>();
        if (parser.containsKey("randomMaleNames")) {
            SimpleJSONArray array = parser.getArray("randomMaleNames");
            for (SimpleJSONArrayEntry entry : array) {
                randomMaleNames.add(entry.getString());
            }
        }
        ((ArrayList<String>)randomMaleNames).trimToSize();

        randomFemaleNames = new ArrayList<String>();
        if (parser.containsKey("randomFemaleNames")) {
            SimpleJSONArray array = parser.getArray("randomFemaleNames");
            for (SimpleJSONArrayEntry entry : array) {
                randomFemaleNames.add(entry.getString());
            }
        }
        ((ArrayList<String>)randomFemaleNames).trimToSize();

        parser.warnOnUnusedKeys();
    }

    private void addAbilitySelectionListAtLevel(String listID, int level)
    {
        List<String> listsAtLevel = abilitySelectionLists.get(level);
        if (listsAtLevel == null) {
            listsAtLevel = new ArrayList<String>(1);
            abilitySelectionLists.put(level, listsAtLevel);
        }

        listsAtLevel.add(listID);
    }

    /**
     * returns true if the slot is restricted - this race cannot equip items in the specified slot,
     * false otherwise
     *
     * @param slot
     * @return whether the slot is restricted
     */

    public boolean isSlotRestricted(Inventory.Slot slot)
    {
        return restrictedSlots.contains(slot);
    }

    /**
     * returns true if the slot is restricted - this race cannot equip items in the specified slot,
     * false otherwise
     *
     * @param slot
     * @return whether the slot is restricted
     */

    public boolean isSlotRestricted(String slot)
    {
        return isSlotRestricted(Inventory.Slot.valueOf(slot));
    }

    /**
     * Returns the string that should be used for this race when selecting sub icons from the
     * set of available sub icons.
     *
     * @return the string used for this race when selecting sub icons
     */

    public String getSubIconRaceString()
    {
        return subIconRaceString;
    }

    /**
     * Returns true if members of this race should draw this sub icon type, false
     * otherwise.  Races will only draw sub icon types that have an explicitly
     * definied icon offset.  Note that this method is only applicable for sub icons
     * associated with inventory items, base racial sub icons can always be drawn.
     *
     * @param type
     * @return whether members of this race should draw sub icons of this type
     */

    public boolean drawsSubIconType(SubIcon.Type type)
    {
        return iconOffsets.containsKey(type);
    }

    public Point getIconOffset(SubIcon.Type type)
    {
        if (!iconOffsets.containsKey(type)) {
            iconOffsets.put(type, new Point(0, 0));
        }

        return new Point(iconOffsets.get(type));
    }

    private String getRandomFromList(List<String> names)
    {
        switch (names.size()) {
            case 0:
                return "";
            case 1:
                return names.get(0);
            default:
                return names.get(Game.dice.rand(0, names.size() - 1));
        }
    }

    public int getDefaultBeardIndex()
    {
        return defaultBeardIndex;
    }

    public int getDefaultHairIndex()
    {
        return defaultHairIndex;
    }

    public String getDefaultHairColor()
    {
        return defaultHairColor;
    }

    public String getDefaultBeardColor()
    {
        return defaultBeardColor;
    }

    public String getDefaultSkinColor()
    {
        return defaultSkinColor;
    }

    /**
     * returns the list of indices (sub-icon sprite indices) of hair icons that are valid for this
     * race as a player selection
     *
     * @return the list of valid hair indices
     */

    public List<Integer> getSelectableHairIndices()
    {
        return selectableHairIndices;
    }

    /**
     * returns the list of indices (sub-icon sprite indices) of beard icons that are valid for this
     * race as a player selection
     *
     * @return the list of valid beard indices
     */

    public List<Integer> getSelectableBeardIndices()
    {
        return selectableBeardIndices;
    }

    /**
     * returns the list of standard hair and beard colors for this race
     *
     * @return the list of standard hair and beard colors for this race
     */

    public List<String> getHairAndBeardColors()
    {
        return hairAndBeardColors;
    }

    /**
     * returns the list of standard skin colors for this race
     *
     * @return the list of standard skin colors for this race
     */

    public List<String> getSkinColors()
    {
        return skinColors;
    }

    /**
     * returns whether player characters of this race can have a beard
     *
     * @return whether player characters of this race can have a beard
     */

    public boolean hasBeard()
    {
        return hasBeard;
    }

    /**
     * Returns whether player characters can select a hair icon on character
     * creation
     *
     * @return whether player characters can select a hairstyle
     */

    public boolean hasHair()
    {
        return hasHair;
    }

    public String getRandomMaleName()
    {
        return getRandomFromList(randomMaleNames);
    }

    public String getRandomFemaleName()
    {
        return getRandomFromList(randomFemaleNames);
    }

    public boolean showDetailedDescription()
    {
        return showDetailedDescription;
    }

    public String getMaleClothesIcon()
    {
        return maleClothesIcon;
    }

    public String getFemaleClothesIcon()
    {
        return femaleClothesIcon;
    }

    public String getFemaleEarsIcon()
    {
        return femaleEarsIcon;
    }

    public String getFemaleForegroundIcon()
    {
        return femaleForegroundIcon;
    }

    public String getFemaleBackgroundIcon()
    {
        return femaleBackgroundIcon;
    }

    public String getMaleEarsIcon()
    {
        return maleEarsIcon;
    }

    public String getMaleForegroundIcon()
    {
        return maleForegroundIcon;
    }

    public String getMaleBackgroundIcon()
    {
        return maleBackgroundIcon;
    }

    public String getDescriptionFile()
    {
        return ResourceManager.getResourceAsString(descriptionFile);
    }

    public String getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getIcon()
    {
        return icon;
    }

    public int getMovementCost()
    {
        return movementCost;
    }

    public boolean isPlayerSelectable()
    {
        return playerSelectable;
    }

    public int getBaseStr()
    {
        return baseStr;
    }

    public int getBaseDex()
    {
        return baseDex;
    }

    public int getBaseCon()
    {
        return baseCon;
    }

    public int getBaseInt()
    {
        return baseInt;
    }

    public int getBaseWis()
    {
        return baseWis;
    }

    public int getBaseCha()
    {
        return baseCha;
    }

    public boolean hasRacialType(String racialTypeID)
    {
        for (RacialType type : racialTypes) {
            if (type.getName().equals(racialTypeID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * For each AbilitySelectionList in the returned List, the Creature gaining
     * the creature level specified pick one Ability from that
     * AbilitySelectionList.  If their are no selections to be made, the List
     * will be empty.
     *
     * @param level the creature level that has been gained
     * @return the List of AbilitySelectionLists to choose abilities from
     */

    public List<AbilitySelectionList> getAbilitySelectionsAddedAtLevel(int level)
    {
        List<String> idList = abilitySelectionLists.get(level);

        if (idList == null) return Collections.emptyList();

        List<AbilitySelectionList> lists = new ArrayList<AbilitySelectionList>(idList.size());
        for (String id : idList) {
            lists.add(Game.ruleset.getAbilitySelectionList(id));
        }

        return lists;
    }

    public List<RacialType> getRacialTypes()
    {
        return new ArrayList<RacialType>(racialTypes);
    }

    /**
     * Adds all Racial abilities for this Race to the specified Creature.
     *
     * @param creature the Creature to add abilities to
     */

    public void addAbilitiesToCreature(Creature creature)
    {
        for (String abilityID : abilities) {
            Ability ability = Game.ruleset.getAbility(abilityID);
            if (ability == null) {
                Logger.appendToWarningLog("Racial ability " + abilityID + " for race " + this.id + " not found.");
                continue;
            }

            creature.abilities.addRacialAbility(ability);
        }
    }

    /**
     * Returns a set containing all AbilitySelectionLists that are referenced at any
     * level within this Race
     *
     * @return the set of AbilitySelectionLists
     */

    public Set<AbilitySelectionList> getAllReferencedAbilitySelectionLists()
    {
        Set<AbilitySelectionList> lists = new LinkedHashSet<AbilitySelectionList>();

        for (int level : abilitySelectionLists.keySet()) {
            for (String listID : abilitySelectionLists.get(level)) {
                AbilitySelectionList list = Game.ruleset.getAbilitySelectionList(listID);
                lists.add(list);
            }
        }

        return lists;
    }

    /**
     * Returns the weapon template of this race's default (unarmed) weapon
     *
     * @return the default weapon template
     */

    public WeaponTemplate getDefaultWeaponTemplate()
    {
        return defaultWeapon;
    }
}
