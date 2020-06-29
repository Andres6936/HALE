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

package hale;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import hale.*;
import hale.entity.Creature;
import hale.entity.NPC;
import hale.entity.PC;
import hale.loading.JSONOrderedObject;
import hale.loading.ReferenceHandler;
import hale.loading.Saveable;
import hale.util.Logger;
import hale.util.SaveGameUtil;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

public class Party implements Iterable<PC>, Saveable
{
    public static final int MaxQuickBarSlots = 100;

    private boolean recomputePortraits;

    private String name;
    private final List<PC> characters;
    private final List<NPC> summons;

    private int selectedCharacterIndex;

    private boolean defeated;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("name", name);
        data.put("selectedCharacter", selectedCharacterIndex);

        if (defeated) data.put("defeated", defeated);

        Object[] charactersData = new Object[characters.size()];
        int i = 0;
        for (Creature c : characters) {
            charactersData[i] = SaveGameUtil.getRef(c);
            i++;
        }
        data.put("characters", charactersData);

        if (summons.size() > 0) {
            Object[] summonsData = new Object[summons.size()];
            for (i = 0; i < summons.size(); i++) {
                summonsData[i] = SaveGameUtil.getRef(summons.get(i));
            }
            data.put("summons", summonsData);
        }

        return data;
    }

    public static Party load(SimpleJSONObject data, ReferenceHandler refHandler)
    {
        Party party = new Party();

        party.name = data.get("name", null);
        party.selectedCharacterIndex = data.get("selectedCharacter", 0);

        if (data.containsKey("defeated")) {
            party.defeated = data.get("defeated", false);
        }

        for (SimpleJSONArrayEntry entry : data.getArray("characters")) {
            String charID = entry.getString();

            PC creature = (PC)refHandler.getEntity(charID);
            creature.setFaction(Game.ruleset.getFaction(Game.ruleset.getString("PlayerFaction")));
            party.characters.add(creature);
        }

        if (data.containsKey("summons")) {
            for (SimpleJSONArrayEntry entry : data.getArray("summons")) {
                String creatureID = entry.getString();

                NPC creature = (NPC)refHandler.getEntity(creatureID);

                if (creature == null) {
                    // the summon was not found
                    Logger.appendToWarningLog("When loading, summoned creature " + creatureID + " not found.");
                } else {
                    creature.setFaction(Game.ruleset.getFaction(Game.ruleset.getString("PlayerFaction")));
                    party.summons.add(creature);
                }
            }
        }

        return party;
    }

    /**
     * Creates a new, empty Party
     */

    public Party()
    {
        characters = new ArrayList<PC>();
        summons = new ArrayList<NPC>();

        selectedCharacterIndex = -1;

        recomputePortraits = true;
    }

    /**
     * Moves the order of the specified creature within the party
     *
     * @param creature      the player character to move
     * @param placesForward
     */

    public void movePartyMember(PC creature, int placesForward)
    {
        if (placesForward == 0) throw new IllegalArgumentException("Cannot move a creature by 0.");

        int indexOld = characters.indexOf(creature);
        int indexNew = indexOld + placesForward;

        if (indexNew < indexOld) {
            characters.add(indexNew, creature);
            characters.remove(indexOld + 1);
        } else
            if (indexNew > indexOld) {
                characters.add(indexNew + 1, creature);
                characters.remove(indexOld);
            }

        recomputePortraits = true;
    }

    /**
     * Returns true if and only if the specified creature is a non-summoned member of this
     * current player character party
     *
     * @param creature the creature to check
     * @return whether the creature is a player character
     */

    public boolean isPCPartyMember(Creature creature)
    {
        if (!creature.isPlayerFaction()) return false;
        if (creature.isSummoned()) return false;

        for (Creature character : characters) {
            if (creature == character) return true;
        }

        return false;
    }

    /**
     * Returns the name of this party
     *
     * @return the name of the party
     */

    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of this Party
     *
     * @param name
     */

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Sets the party's defeated status
     *
     * @param defeated true for the party to be defeated, false if not
     */

    public void setDefeated(boolean defeated)
    {
        this.defeated = defeated;

        if (defeated) {
            for (Creature c : characters) {
                c.takeDamage(20 - c.getCurrentHitPoints(), "Effect");
            }

            for (Creature c : summons) {
                c.takeDamage(-c.getCurrentHitPoints(), "Effect");
            }
        }
    }

    /**
     * Returns true if this party has been defeated
     *
     * @return true if and only if this party has been defeated
     */

    public boolean isDefeated()
    {
        return defeated;
    }

    /**
     * Sets the specified creature as the currently selected party
     * member.  If the creature is not a PC in this party, no action is performed
     *
     * @param creature
     */

    public void setSelected(Creature creature)
    {
        int index = characters.indexOf(creature);

        if (index != -1) {
            this.selectedCharacterIndex = index;
        }
    }

    /**
     * Sets the first member of this party as the selected character
     */

    public void setFirstMemberSelected()
    {
        this.selectedCharacterIndex = 0;
    }

    /**
     * Returns the party member at the specified list index
     *
     * @param index
     * @return the party member at the specified list index
     */

    public PC get(int index)
    {
        return characters.get(index);
    }

    /**
     * Returns the party member that is currently selected
     *
     * @return the party member that is selected, or null if no party member is selected
     */

    public PC getSelected()
    {
        return characters.get(selectedCharacterIndex);
    }

    /**
     * Returns the index within the party of the character that is currently selected
     *
     * @return the index of the selected character, or -1 if no character is selected
     */

    public int getSelectedIndex()
    {
        return selectedCharacterIndex;
    }

    /**
     * Returns the number of characters in this party
     *
     * @return the number of characters in this party
     */

    public int size()
    {
        return characters.size();
    }

    /**
     * Adds the specified summoned creature to this party
     *
     * @param creature
     */

    public void addSummon(NPC creature)
    {
        summons.add(creature);
    }

    /**
     * Adds the specified creature to the party
     *
     * @param creature
     */

    public void add(PC creature)
    {
        for (PC pc : this.characters) {
            if (pc == creature) return;
        }

        creature.setFaction(Game.ruleset.getFaction(Game.ruleset.getString("PlayerFaction")));
        characters.add(creature);

        recomputePortraits = true;
    }

    /**
     * Removes the summoned creature from the party
     *
     * @param creature
     */

    public void removeSummon(Creature creature)
    {
        int index = summons.indexOf(creature);

        if (index != -1) {

            summons.remove(index);
        }
    }

    /**
     * Removes the specified creature from the party
     *
     * @param creature
     */

    public void remove(Creature creature)
    {
        int index = characters.indexOf(creature);

        if (index != -1) {
            characters.remove(index);
        }

        recomputePortraits = true;

        creature.setFaction(Game.ruleset.getFaction(Game.ruleset.getString("DefaultFaction")));
    }

    /**
     * Returns the total quantity of the item with the specified ID across all party
     * member inventories
     *
     * @param itemID the ID of the item to check for
     * @return the total item quantity
     */

    public int getQuantity(String itemID)
    {
        int quantityHeld = 0;

        for (Creature creature : characters) {
            quantityHeld += creature.inventory.getTotalQuantity(itemID);
        }

        return quantityHeld;
    }

    /**
     * Returns true if and only if the entire party's combined inventory holds
     * the specified quantity of the specified item
     *
     * @param itemID   the item to check for
     * @param quantity the quantity
     * @return whether the party has the specified quantity of the item
     */

    public boolean hasItem(String itemID, int quantity)
    {
        int quantityHeld = 0;

        for (Creature creature : characters) {
            quantityHeld += creature.inventory.getTotalQuantity(itemID);
        }

        return quantityHeld >= quantity;
    }

    /**
     * Searches for an instance of an item with the specified itemID in all party member
     * inventories, and removes the first instance found, then returns.  If no item is
     * found, no action is performed
     *
     * @param itemID the entity ID of the item to remove
     */

    public void removeItem(String itemID)
    {
        for (Creature creature : characters) {
            if (creature.inventory.remove(itemID, 1) > 0) {
                return;
            }
        }
    }

    /**
     * Searches through all party inventories and removes up to the specified quantity of the
     * item
     *
     * @param itemID   the item to remove
     * @param quantity the quantity to remove
     */

    public void removeItem(String itemID, int quantity)
    {
        int qtyRemoved = 0;
        int qtyLeft = quantity;

        for (Creature creature : characters) {
            qtyRemoved += creature.inventory.remove(itemID, qtyLeft);
            qtyLeft = quantity - qtyRemoved;

            if (qtyLeft == 0) return;
        }
    }

    /**
     * Returns true if any party member is currently moving, false otherwise
     *
     * @return whether a party member is moving
     */

    public boolean isCurrentlyMoving()
    {
        for (Creature creature : characters) {
            if (creature.isCurrentlyMoving()) return true;
        }

        return false;
    }

    public boolean recomputePortraits()
    {
        boolean returnValue = recomputePortraits;

        recomputePortraits = false;

        return returnValue;
    }

    /**
     * Checks all items currently held by this party, removing items with IDs that
     * do not have a valid reference
     */

    public void validateItems()
    {
        for (PC pc : characters) {
            pc.inventory.validateItems();
        }
    }

    /**
     * Returns an iterator over just the player characters in this party
     *
     * @param return an iterator over just the player characters in this party
     */

    @Override
    public Iterator<PC> iterator()
    {
        return new PartyIterator();
    }

    /**
     * Returns an iterator over all creatures, PCs and summons,
     * in this party
     *
     * @return an iterator over all creatures in this party
     */

    public ListIterator<Creature> allCreaturesIterator()
    {
        return new AllCreaturesIterator();
    }

    private class AllCreaturesIterator implements ListIterator<Creature>
    {
        private int iteratorIndex;
        private int charactersSize;

        private AllCreaturesIterator()
        {
            this.iteratorIndex = 0;
            this.charactersSize = characters.size();
        }

        @Override
        public boolean hasNext()
        {
            return iteratorIndex < charactersSize + summons.size();
        }

        @Override
        public Creature next()
        {
            Creature next;
            if (iteratorIndex < characters.size()) {
                next = characters.get(iteratorIndex);
            } else {
                next = summons.get(iteratorIndex - charactersSize);
            }

            iteratorIndex++;

            return next;
        }

        @Override
        public int nextIndex()
        {
            return iteratorIndex + 1;
        }

        @Override
        public boolean hasPrevious()
        {
            return iteratorIndex > 0;
        }

        @Override
        public Creature previous()
        {
            iteratorIndex--;

            if (iteratorIndex < characters.size()) {
                return characters.get(iteratorIndex);
            } else {
                return summons.get(iteratorIndex - charactersSize);
            }
        }

        @Override
        public int previousIndex()
        {
            return iteratorIndex - 1;
        }

        @Override
        public void set(Creature creature)
        {
            throw new UnsupportedOperationException("The party may not be modified through its iterator");
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("The party may not be modified through its iterator");
        }

        @Override
        public void add(Creature pc)
        {
            throw new UnsupportedOperationException("The party may not be modified through its iterator");
        }
    }

    private class PartyIterator implements Iterator<PC>
    {
        private int iteratorIndex;

        private PartyIterator()
        {
            this.iteratorIndex = 0;
        }

        @Override
        public boolean hasNext()
        {
            return iteratorIndex < characters.size();
        }

        @Override
        public PC next()
        {
            PC next = characters.get(iteratorIndex);

            iteratorIndex++;

            return next;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("The party may not be modified through its iterator");
        }
    }
}
