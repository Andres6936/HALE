/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package main.java.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.java.hale.entity.Creature;

/**
 * A sub editor for editing creatures
 *
 * @author Jared
 */

public class CreatureSubEditor extends JPanel
{
    /**
     * Creates a new SubEditor editing the specified creature
     *
     * @param creature
     */

    protected CreatureSubEditor(JFrame parent, Creature creature)
    {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        JLabel id = new JLabel("ID: " + creature.getTemplate().getID());
        JLabel name = new JLabel("Name: " + creature.getTemplate().getName());

        add(id, c);

        c.gridy++;

        add(name, c);
    }
}
