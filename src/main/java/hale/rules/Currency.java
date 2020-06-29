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

package main.java.hale.rules;

import main.java.hale.entity.Item;
import main.java.hale.util.StringParser;
import main.java.hale.util.Logger;

/**
 * This is a mutable class representing the amount of currency currently held by a creature.
 * <p>
 * 1 Platinum Piece (PP) = 10 Gold Pieces (GP) = 100 Silver Pieces (SP) = 1000 Copper Pieces (CP)
 * <p>
 * Value is stored in a unit where 100 = 1 CP
 *
 * @author Jared
 */

public class Currency
{
    private int value;

    /**
     * Creates a new Currency object with a value of zero
     */

    public Currency()
    {
        this.value = 0;
    }

    /**
     * Creates a new Currency object with the specified value
     *
     * @param value the value in copper pieces (CP) over 100
     */

    public Currency(int value)
    {
        this.value = value;
    }

    /**
     * Creates a new Currency object with exactly the same value as the specified Currency
     *
     * @param other
     */

    public Currency(Currency other)
    {
        this.value = other.value;
    }

    /**
     * Sets the value of this currency
     *
     * @param value the value in copper pieces (CP) over 100
     */

    public void setValue(int value)
    {
        this.value = value;
    }

    /**
     * Gets the total value of this Currency in Copper Pieces over 100
     *
     * @return the total value
     */

    public int getValue()
    {
        return value;
    }

    /**
     * Adds the specified amount of value to this currency
     *
     * @param value the value, in Copper Pieces / 100
     */

    public void addValue(int value)
    {
        this.value += value;
    }

    /**
     * Adds the specified amount of value to this currency
     *
     * @param cp the amount of value to add in Copper Pieces
     */

    public void addCP(int cp)
    {
        this.value += cp * 100;
    }

    /**
     * Adds the specified amount of value to this currency
     *
     * @param sp the amount of value to add in Silver Pieces
     */

    public void addSP(int sp)
    {
        this.value += sp * 1000;
    }

    /**
     * Adds the specified amount of value to this currency
     *
     * @param gp the amount of value to add in Gold Pieces
     */

    public void addGP(int gp)
    {
        this.value += gp * 10000;
    }

    /**
     * Adds the specified amount of value to this currency
     *
     * @param pp the amount of value to add in Platinum Pieces
     */

    public void addPP(int pp)
    {
        this.value += pp * 100000;
    }

    /**
     * Adds the amount of value from the other currency to this currency
     *
     * @param other
     */

    public void add(Currency other)
    {
        this.value += other.value;
    }

    @Override
    public String toString()
    {
        return getPP() + " PP " + getGP() + " GP " + getSP() + " SP " + getCP() + " CP";
    }

    private final int getCP()
    {
        return (value / 100) % 10;
    }

    private final int getSP()
    {
        return (value / 1000) % 10;
    }

    private final int getGP()
    {
        return (value / 10000) % 10;
    }

    private final int getPP()
    {
        return (value / 100000);
    }

    /**
     * Returns a string description of this currency object using as high value currency
     * types as possible.  (Any multiple of 1000 will be represented as PP, anything left
     * over from that multiple of 100 will be representing as GP, and so on).
     *
     * @return the short string description
     */

    public String shortString()
    {
        StringBuilder builder = new StringBuilder();

        if (getPP() != 0) builder.append(getPP() + " PP ");
        if (getGP() != 0) builder.append(getGP() + " GP ");
        if (getSP() != 0) builder.append(getSP() + " SP ");
        if (getCP() != 0) builder.append(getCP() + " CP ");

        if (builder.length() == 0) builder.append("0 CP ");

        return builder.toString().trim();
    }

    /**
     * Returns the short string description of the specified value, multiplied by the
     * specified percentage. See {@link #shortString()}.
     *
     * @param percentage the integer percentage
     * @return the short string description.
     */

    public static String shortString(int value, int percentage)
    {
        value = value * percentage / 100;

        StringBuilder builder = new StringBuilder();

        int pp = (value / 100000);
        int gp = (value / 10000) % 10;
        int sp = (value / 1000) % 10;
        int cp = (value / 100) % 10;

        if (pp != 0) builder.append(pp + " PP ");
        if (gp != 0) builder.append(gp + " GP ");
        if (sp != 0) builder.append(sp + " SP ");
        if (cp != 0) builder.append(cp + " CP ");

        if (builder.length() == 0) builder.append("0 CP ");

        return builder.toString().trim();
    }

    /**
     * Returns the short string description of this currency object, multiplied by the
     * specified percentage. See {@link #shortString()}.
     *
     * @param percentage the integer percentage
     * @return the short string description.
     */

    public String shortString(int percentage)
    {
        int oldValue = this.value;

        this.value = this.value * percentage / 100;

        StringBuilder builder = new StringBuilder();

        if (getPP() != 0) builder.append(getPP() + " PP ");
        if (getGP() != 0) builder.append(getGP() + " GP ");
        if (getSP() != 0) builder.append(getSP() + " SP ");
        if (getCP() != 0) builder.append(getCP() + " CP ");

        if (builder.length() == 0) builder.append("0 CP ");

        this.value = oldValue;

        return builder.toString().trim();
    }

    /**
     * Parses the specified string and adds the represented amount of currency
     * to this currency object.  Any errors in parsing will generate warning or
     * error log entries, but will not throw exceptions.  The format of the string
     * should be a series of tokens separated by spaces.  Each token should be in a pair.
     * The first is an integer value, the second is one of "CP", "SP", "GP", or "PP" to
     * indicate the scaling of the first value
     *
     * @param s the string to parse
     */

    public void addFromString(String s)
    {
        StringParser parser = new StringParser(s);

        try {
            while (parser.hasNext()) {
                int value = parser.nextInt();
                if (parser.hasNext()) {
                    String type = parser.next().toLowerCase();

                    if (type.equals("cp")) {
                        addCP(value);
                    } else
                        if (type.equals("sp")) {
                            addSP(value);
                        } else
                            if (type.equals("gp")) {
                                addGP(value);
                            } else
                                if (type.equals("pp")) {
                                    addPP(value);
                                }
                } else {
                    Logger.appendToWarningLog("Warning.  Extra token in currency string " + s);
                }
            }
        } catch (Exception e) {
            Logger.appendToErrorLog("Error parsing currency string.", e);
        }
    }

    /**
     * Returns the value of this currency divided by the value of the specified item marked up
     * by the specified percentage, rounded down
     *
     * @param item
     * @param markup the markup percentage
     * @return the quantity of the specified item that can be afforded by this currency
     */

    public int getMaxNumberAffordable(Item item, int markup)
    {
        int cost = item.getQualityValue() * markup / 100;
        if (cost == 0) return Integer.MAX_VALUE;

        return (this.value / cost);
    }

    /**
     * Creates a new Currency object representing the amount of currency it will cost
     * to buy the specified quantity of the specified item at the specified markdown.
     * <p>
     * Note that "buying" here is from the player's perspective
     *
     * @param item
     * @param quantity
     * @param markdown the markdown percentage of the item's value
     * @return the new Currency object
     */

    public static Currency getPlayerBuyCost(Item item, int quantity, int markdown)
    {
        int cost = item.getQualityValue();

        int value = cost * quantity * markdown / 100;

        return new Currency(value);
    }

    /**
     * Returns a new Currency object representing the amount of currency it will
     * cost to sell the specified quantity of the specified item at the specified
     * markdown.  Selling here is from the player's perspective
     *
     * @param item
     * @param quantity
     * @param markdown the percentage markdown of the item's value
     * @return the new Currency object
     */

    public static Currency getPlayerSellCost(Item item, int quantity, int markdown)
    {
        int cost = item.getQualityValue();

        int value = cost * quantity * markdown / 100;

        return new Currency(value);
    }
}
