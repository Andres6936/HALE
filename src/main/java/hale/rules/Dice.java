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

import java.util.Random;

import hale.Game;

/**
 * Class for handling all random number generation
 *
 * @author Jared
 */

public class Dice
{
    private final Random generator;

    /**
     * initializes a new dice object using the seed from the config, if applicable
     */

    public Dice()
    {
        generator = new Random();

        if (Game.config.randSeedSet()) {
            generator.setSeed(Game.config.getRandSeed());
        }
    }

    /**
     * Initializes a new dice object using the specified seed, ignoring any seed from the
     * config
     *
     * @param seed Seed of configuration
     */

    public Dice(long seed)
    {
        generator = new Random();
        generator.setSeed(seed);
    }

    public float gaussian(float mean, float stddev)
    {
        return ((float)generator.nextGaussian()) * stddev + mean;
    }

    /**
     * Generates a random long suitable for use as a seed in another random
     * generator
     *
     * @return a random long
     */

    public long randSeed()
    {
        return generator.nextLong();
    }

    public double rand(double min, double max)
    {
        double range = max - min;

        return generator.nextDouble() * range + min;
    }

    public float rand(float min, float max)
    {
        float range = max - min;

        return generator.nextFloat() * range + min;
    }

    public int randInt(int min, int max)
    {
        return rand(min, max);
    }

    public int rand(int min, int max)
    {
        int range = max - min + 1;

        return generator.nextInt(range) + min;
    }

    public int d(int base, int multiple)
    {
        int total = 0;
        for (int i = 0; i < multiple; i++) {
            total += (generator.nextInt(base) + 1);
        }

        return total;
    }

    public int d2(int multiple)
    {
        return d(2, multiple);
    }

    public int d3(int multiple)
    {
        return d(3, multiple);
    }

    public int d4(int multiple)
    {
        return d(4, multiple);
    }

    public int d5(int multiple)
    {
        return d(5, multiple);
    }

    public int d6(int multiple)
    {
        return d(6, multiple);
    }

    public int d8(int multiple)
    {
        return d(8, multiple);
    }

    public int d10(int multiple)
    {
        return d(10, multiple);
    }

    public int d12(int multiple)
    {
        return d(12, multiple);
    }

    public int d20(int multiple)
    {
        return d(20, multiple);
    }

    public int d100(int multiple)
    {
        return d(100, multiple);
    }

    public int d2()
    {
        return d(2, 1);
    }

    public int d3()
    {
        return d(3, 1);
    }

    public int d4()
    {
        return d(4, 1);
    }

    public int d5()
    {
        return d(5, 1);
    }

    public int d6()
    {
        return d(6, 1);
    }

    public int d8()
    {
        return d(8, 1);
    }

    public int d10()
    {
        return d(10, 1);
    }

    public int d12()
    {
        return d(12, 1);
    }

    public int d20()
    {
        return d(20, 1);
    }

    public int d100()
    {
        return d(100, 1);
    }
}
