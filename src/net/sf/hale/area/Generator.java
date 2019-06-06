package net.sf.hale.area;

import net.sf.hale.rules.Dice;

public interface Generator
{

    /**
     * Generate a map based on the rules of this generator
     */

    public void generate( );

    /**
     * Sets the random generator used by this generator
     *
     * @param random
     */

    public void setDice( Dice random );
}
