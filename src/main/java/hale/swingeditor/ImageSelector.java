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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A widget for selecting an image and optionally an associated color
 *
 * @author Jared
 */

public class ImageSelector extends JPanel
{
    private List<String> choices;
    private List<BufferedImage> icons;

    private JLabel title;
    private IconButton iconButton;
    private JButton chooseColor;

    private Color currentColor;
    private BufferedImage currentImage;

    private int gridWidth;
    private JFrame parent;

    /**
     * Creates a new ImageSelector choosing between the specified set of choices
     * By default it will not show a color selector
     *
     * @param defaultChoice the initially set choice
     * @param choicesMap    the choices
     */

    public ImageSelector(JFrame parent, String defaultChoice, Map<String, BufferedImage> choicesMap)
    {
        super(new GridBagLayout());

        this.parent = parent;
        currentColor = Color.white;

        GridBagConstraints c = new GridBagConstraints();

        this.choices = new ArrayList<String>();
        this.icons = new ArrayList<BufferedImage>();

        Iterator<String> iter = choicesMap.keySet().iterator();

        int initialIndex = 0;
        Integer[] intArray = new Integer[choicesMap.size()];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = new Integer(i);

            String iconString = iter.next();

            choices.add(iconString);
            icons.add(choicesMap.get(iconString));

            if (iconString.equals(defaultChoice)) {
                initialIndex = i;
            }
        }

        c.gridwidth = 2;
        c.gridy = 0;
        c.gridx = GridBagConstraints.REMAINDER;
        title = new JLabel();
        title.setFont(title.getFont().deriveFont(18.0f));
        add(title, c);
        c.gridy++;

        c.fill = GridBagConstraints.HORIZONTAL;

        iconButton = new IconButton(new ShowIconChooser());
        if (initialIndex != -1) {
            currentImage = icons.get(initialIndex);
            iconButton.setImageToCurrentImage();
        }
        add(iconButton, c);
        c.gridy++;

        chooseColor = new JButton(new ShowColorChooser());
        add(chooseColor, c);
        c.gridy++;

        this.gridWidth = 7;
    }

    /**
     * Sets the number of columns in the item selector popup
     *
     * @param width
     */

    public void setGridColumns(int width)
    {
        this.gridWidth = width;
    }

    /**
     * Sets the color that is initially set for this ImageSelector
     *
     * @param color
     */

    public void setDefaultColor(de.matthiasmann.twl.Color color)
    {
        this.currentColor = new Color(color.toARGB(), true);

        chooseColor.setBackground(currentColor);
        iconButton.setImageToCurrentImage();
    }

    /**
     * Sets the color that is initially set for this ImageSelector
     *
     * @param color
     */

    public void setDefaultColor(Color color)
    {
        this.currentColor = color;
        iconButton.setImageToCurrentImage();
    }

    /**
     * Sets whether this image selector allows the user to also select an
     * associated color
     *
     * @param show
     */

    public void setShowColorChooser(boolean show)
    {
        chooseColor.setVisible(show);
    }

    /**
     * Sets the title of this ImageSelector, which is shown at the top of the widget
     *
     * @param title
     */

    public void setTitle(String title)
    {
        this.title.setText(title);
    }

    private class IconButton extends JButton
    {
        private BufferedImage image;

        private IconButton(AbstractAction action)
        {
            super(action);
        }

        private IconButton(AbstractAction action, BufferedImage image)
        {
            super(action);

            setIcon(new ImageIcon(image));

            this.image = image;
        }

        private void setImageToCurrentImage()
        {
            image = EditorManager.copy(currentImage, currentColor);

            setIcon(new ImageIcon(image));
        }
    }

    private class ShowIconChooser extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent evt)
        {
            ColorSelectorWindow window = new ColorSelectorWindow();
            window.setVisible(true);
        }
    }

    private class ColorSelectorWindow extends JDialog
    {
        private JScrollPane scrollPane;

        private ColorSelectorWindow()
        {
            super(parent, "Choose an Icon", true);

            JPanel scrollPaneContent = new JPanel(new GridLayout(0, gridWidth));
            scrollPane = new JScrollPane(scrollPaneContent);
            scrollPane.getVerticalScrollBar().setUnitIncrement(50);
            add(scrollPane);

            SelectColor select = new SelectColor(this);

            for (BufferedImage image : icons) {
                IconButton b = new IconButton(select, image);
                b.setBackground(Color.WHITE);
                scrollPaneContent.add(b);
            }

            pack();
            setLocationRelativeTo(parent);
        }

        @Override
        public Dimension getPreferredSize()
        {
            Dimension sup = super.getPreferredSize();

            if (sup.height > 600) sup.height = 600;

            return sup;
        }
    }

    private class SelectColor extends AbstractAction
    {
        private JDialog menu;

        private SelectColor(JDialog menu)
        {
            this.menu = menu;
        }

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            IconButton b = (IconButton)evt.getSource();

            currentImage = b.image;

            menu.setVisible(false);

            iconButton.setImageToCurrentImage();
        }
    }

    private class ShowColorChooser extends AbstractAction
    {
        private ShowColorChooser()
        {
            super("Choose Color");
        }

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            Color color = JColorChooser.showDialog(ImageSelector.this, "Choose a Color", currentColor);

            if (color != null) {
                currentColor = color;
            }

            chooseColor.setBackground(currentColor);
            iconButton.setImageToCurrentImage();
        }
    }
}
