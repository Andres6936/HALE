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

import hale.Game;
import hale.entity.Creature;
import hale.entity.EntityManager;
import hale.entity.Item;
import hale.entity.ItemList;
import hale.entity.LootList;
import hale.loading.JSONOrderedObject;
import hale.loading.Saveable;
import hale.resource.ResourceType;
import hale.util.SimpleJSONObject;
import hale.util.SimpleJSONParser;

public class Merchant implements Saveable
{
    private final LootList baseItems;
    private String name;
    private int buyValuePercentage;
    private int sellValuePercentage;
    private boolean usesSpeechSkill;
    private boolean confirmOnExit;
    private int currentBuyPercentage;
    private int currentSellPercentage;
    private int respawnHours;

    private final String id;
    private int lastRespawnRounds;
    private ItemList currentItems;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("id", id);
        data.put("lastRespawnRound", lastRespawnRounds);

        data.put("currentItems", currentItems.save());

        return data;
    }

    public void load(SimpleJSONObject data)
    {
        this.lastRespawnRounds = data.get("lastRespawnRound", 0);

        this.currentItems = new ItemList();
        this.currentItems.load(data.getArray("currentItems"));
    }

    public Merchant(String id)
    {
        this.id = id;

        SimpleJSONParser parser = new SimpleJSONParser("merchants/" + id, ResourceType.JSON);

        this.name = parser.get("name", null);
        this.sellValuePercentage = parser.get("sellValuePercentage", 0);
        this.buyValuePercentage = parser.get("buyValuePercentage", 0);
        this.usesSpeechSkill = parser.get("usesSpeechSkill", false);
        this.confirmOnExit = parser.get("confirmOnExit", false);
        this.respawnHours = parser.get("respawnHours", 0);

        this.baseItems = new LootList(parser.getArray("items"));

        parser.warnOnUnusedKeys();

        this.currentBuyPercentage = buyValuePercentage;
        this.currentSellPercentage = sellValuePercentage;
    }

    public ItemList getCurrentItems()
    {
        return currentItems;
    }

    public int getRespawnHours()
    {
        return respawnHours;
    }

    public LootList getBaseItems()
    {
        return baseItems;
    }

    public boolean usesSpeechSkill()
    {
        return usesSpeechSkill;
    }

    public boolean confirmOnExit()
    {
        return confirmOnExit;
    }

    private boolean checkRespawn()
    {
        if (currentItems == null) return true;

        if (respawnHours == 0) return false;

        int currentRound = Game.curCampaign.getDate().getTotalRoundsElapsed();
        int elapsedRounds = currentRound - this.lastRespawnRounds;

        return elapsedRounds >= respawnHours * Game.curCampaign.getDate().ROUNDS_PER_HOUR;
    }

    /**
     * Gets the current list of items for this merchant, respawning if the respawn time has passed
     *
     * @return the current list of items for this merchant
     */

    public ItemList updateCurrentItems()
    {
        if (checkRespawn()) {
            this.lastRespawnRounds = Game.curCampaign.getDate().getTotalRoundsElapsed();
            this.currentItems = baseItems.generate();
        }

        return currentItems;
    }

    public String getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setPartySpeech(int partySpeech)
    {
        if (usesSpeechSkill) {
            double gapExponent = -1.0 * ((double)partySpeech) / ((double)Game.ruleset.getValue("BuySellGapSpeechExpFactor"));
            double gapPercentage = Math.exp(gapExponent);

            double base = (double)(sellValuePercentage - buyValuePercentage) / 2.0;

            int modifier = (int)Math.round(base * (1.0 - gapPercentage));


            this.currentBuyPercentage = buyValuePercentage + modifier;
            this.currentSellPercentage = sellValuePercentage - modifier;
        }
    }

    public void setBuyValuePercentage(int buyValuePercentage)
    {
        this.buyValuePercentage = buyValuePercentage;
    }

    public void setSellValuePercentage(int sellValuePercentage)
    {
        this.sellValuePercentage = sellValuePercentage;
    }

    public int getBuyValuePercentage()
    {
        return buyValuePercentage;
    }

    public int getSellValuePercentage()
    {
        return sellValuePercentage;
    }

    public int getCurrentBuyPercentage()
    {
        return currentBuyPercentage;
    }

    public int getCurrentSellPercentage()
    {
        return currentSellPercentage;
    }

    public void sellItem(Item item, Creature creature)
    {
        sellItem(item, creature, 1);
    }

    public void buyItem(Item item, Creature creature)
    {
        buyItem(item, creature, 1);
    }

    public void sellItem(Item item, Creature creature, int quantity)
    {
        int cost = Currency.getPlayerBuyCost(item, quantity, currentSellPercentage).getValue();

        if (Game.curCampaign.getPartyCurrency().getValue() < cost) return;

        Item soldItem = EntityManager.getItem(item.getTemplate().getID(), item.getQuality());

        Game.curCampaign.getPartyCurrency().addValue(-cost);
        creature.inventory.getUnequippedItems().add(soldItem, quantity);

        if (currentItems.getQuantity(item) != Integer.MAX_VALUE) {
            currentItems.remove(item, quantity);
        }

        Game.mainViewer.updateInterface();
    }

    public void buyItem(Item item, Creature creature, int quantity)
    {
        if (item.getTemplate().isQuest()) return;

        int cost = Currency.getPlayerSellCost(item, quantity, currentBuyPercentage).getValue();

        Game.curCampaign.getPartyCurrency().addValue(cost);
        creature.inventory.getUnequippedItems().remove(item, quantity);

        if (currentItems.getQuantity(item) != Integer.MAX_VALUE) {
            currentItems.add(item, quantity);
        }

        Game.mainViewer.updateInterface();
    }
}
