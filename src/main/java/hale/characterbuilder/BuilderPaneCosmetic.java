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

package main.java.hale.characterbuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.entity.Creature;
import main.java.hale.entity.PC;
import main.java.hale.icon.IconFactory;
import main.java.hale.resource.SpriteManager;
import main.java.hale.rules.Race;
import main.java.hale.rules.Ruleset;
import main.java.hale.widgets.BasePortraitViewer;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.utils.TintAnimator;

/**
 * The BuilderPane for editing cosmetic aspects of the Buildable character:
 * name, gender, portrait, hair, and hair color
 *
 * @author Jared Stephen
 */

public class BuilderPaneCosmetic extends AbstractBuilderPane implements PortraitSelector.Callback
{
    private static final Color DEFAULT_CLOTHING_COLOR = Color.GREEN;

    private static final Color[] CLOTHING_COLORS = {Color.WHITE, Color.SILVER, Color.GRAY, new Color(0xFF444444),
            Color.MAROON, Color.RED, Color.ORANGE, Color.YELLOW, Color.LIGHTYELLOW,
            Color.GREEN, Color.OLIVE, Color.LIME, Color.TEAL, Color.AQUA, Color.LIGHTBLUE,
            Color.BLUE, Color.NAVY, Color.LIGHTPINK,
            Color.FUCHSIA, Color.PURPLE, new Color(0xFF501002)};

    private Widget appearanceHolder;
    private CharacterViewer characterViewer;
    private PortraitViewer portraitViewer;

    private int gap, smallGap;
    private String noNameMessage, noGenderMessage, noPortraitMessage;

    private PC workingCopy;
    private Race workingRace;
    private boolean[] beardsValid, hairValid;

    private Label nameLabel;
    private EditField nameField;
    private Button randomName;

    private Label genderLabel;
    private List<GenderSelector> genderSelectors;

    private int hairStyleMax;
    private Color currentHairColor;
    private int currentHairStyle;
    private final NumberFormat numberFormat;

    private Label hairLabel;
    private Label hairStyleLabel;
    private Button prevHairStyle, nextHairStyle;
    private ColorPicker hairColorPicker;

    private int beardStyleMax;
    private Color currentBeardColor;
    private int currentBeardStyle;

    private Label beardLabel;
    private Label beardStyleLabel;
    private Button prevBeardStyle, nextBeardStyle;
    private ColorPicker beardColorPicker;

    private Label skinLabel;
    private ColorPicker skinColorPicker;
    private Color currentSkinColor;

    private Label clothingLabel;
    private ColorPicker clothingColorPicker;
    private Color currentClothingColor;

    private Label portraitLabel;
    private Button choosePortrait;

    /**
     * Create a new BuilderPaneCosmetic editing the specified character
     *
     * @param builder   the parent CharacterBuilder
     * @param character the Buildable character being edited
     */

    public BuilderPaneCosmetic(CharacterBuilder builder, Buildable character)
    {
        super(builder, "Cosmetic", character);

        numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumIntegerDigits(3);
        numberFormat.setMaximumIntegerDigits(3);

        // determine the total number of available hairstyles
        int i = 1;
        String currentValue = null;
        do {
            currentValue = "subIcons/hair" + numberFormat.format(i);
            i++;
        } while (SpriteManager.hasSprite(currentValue));

        this.hairStyleMax = i - 2;

        hairValid = new boolean[hairStyleMax + 1];

        // determine the total number of available beard styles
        i = 1;
        currentValue = null;
        do {
            currentValue = "subIcons/beard" + numberFormat.format(i);
            i++;
        } while (SpriteManager.hasSprite(currentValue));

        this.beardStyleMax = i - 2;

        beardsValid = new boolean[beardStyleMax + 1];

        // add widgets
        appearanceHolder = new AppearanceArea();
        add(appearanceHolder);

        characterViewer = new CharacterViewer();
        characterViewer.setTheme("characterviewer");
        appearanceHolder.add(characterViewer);

        portraitViewer = new PortraitViewer(workingCopy);
        appearanceHolder.add(portraitViewer);

        nameLabel = new Label("Name");
        nameLabel.setTheme("namelabel");
        add(nameLabel);

        nameField = new EditField();
        nameField.addCallback(new EditField.Callback()
        {
            @Override
            public void callback(int key)
            {
                setName(nameField.getText());
            }
        });
        nameField.setTheme("nameeditfield");
        add(nameField);

        randomName = new Button("Random");
        randomName.setTheme("randomnamebutton");
        randomName.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                // setting the name field text will fire the nameField callback
                if (workingCopy.getTemplate().getGender() == Ruleset.Gender.Male) {
                    nameField.setText(workingCopy.getTemplate().getRace().getRandomMaleName());
                } else {
                    nameField.setText(workingCopy.getTemplate().getRace().getRandomFemaleName());
                }
            }
        });
        add(randomName);

        genderLabel = new Label("Gender");
        genderLabel.setTheme("genderlabel");
        add(genderLabel);

        genderSelectors = new ArrayList<GenderSelector>();
        for (Ruleset.Gender gender : Ruleset.Gender.values()) {
            String iconRule = gender.toString() + "Icon";

            GenderSelector g = new GenderSelector(gender, Game.ruleset.getString(iconRule));
            g.setTheme("genderselector");
            add(g);
            genderSelectors.add(g);
        }

        hairLabel = new Label("Hair");
        hairLabel.setTheme("hairlabel");
        add(hairLabel);

        hairStyleLabel = new Label("Style");
        hairStyleLabel.setTheme("hairstylelabel");
        add(hairStyleLabel);

        prevHairStyle = new Button("<");
        prevHairStyle.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                previousHairStyle();
            }
        });
        prevHairStyle.setTheme("prevhairstyle");
        add(prevHairStyle);

        nextHairStyle = new Button(">");
        nextHairStyle.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                nextHairStyle();
            }
        });
        nextHairStyle.setTheme("nexthairstyle");
        add(nextHairStyle);

        beardLabel = new Label("Beard");
        beardLabel.setTheme("beardlabel");
        add(beardLabel);

        beardStyleLabel = new Label("Style");
        beardStyleLabel.setTheme("beardstylelabel");
        add(beardStyleLabel);

        prevBeardStyle = new Button("<");
        prevBeardStyle.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                previousBeardStyle();
            }
        });
        prevBeardStyle.setTheme("prevbeardstyle");
        add(prevBeardStyle);

        nextBeardStyle = new Button(">");
        nextBeardStyle.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                nextBeardStyle();
            }
        });
        nextBeardStyle.setTheme("nextbeardstyle");
        add(nextBeardStyle);

        skinLabel = new Label("Skin");
        skinLabel.setTheme("skinlabel");
        add(skinLabel);

        clothingLabel = new Label("Clothing");
        clothingLabel.setTheme("clothinglabel");
        add(clothingLabel);

        clothingColorPicker = new ColorPicker(new ColorCallback()
        {
            @Override
            public void colorSet(Color color)
            {
                setClothingColor(color);
            }

            @Override
            public Color getCurrentColor()
            {
                return getCharacter().getSelectedClothingColor();
            }
        }, CLOTHING_COLORS);
        clothingColorPicker.setTheme("clothingcolorpicker");
        add(clothingColorPicker);

        currentClothingColor = DEFAULT_CLOTHING_COLOR;

        portraitLabel = new Label("Portrait");
        portraitLabel.setTheme("portraitlabel");
        add(portraitLabel);

        choosePortrait = new Button("Choose...");
        choosePortrait.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                PortraitSelector p = new PortraitSelector(BuilderPaneCosmetic.this, getCharacter());
                p.setCallback(BuilderPaneCosmetic.this);
                p.openPopupCentered();
            }
        });
        choosePortrait.setTheme("chooseportraitbutton");
        add(choosePortrait);

        getNextButton().setText("Finish & Save");

        updateWorkingCopy();
    }

    @Override
    public void next()
    {
        getCharacterBuilder().finish();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        smallGap = themeInfo.getParameter("smallgap", 0);
        gap = themeInfo.getParameter("gap", 0);

        noNameMessage = themeInfo.getParameter("nonamemessage", (String)null);
        noGenderMessage = themeInfo.getParameter("nogendermessage", (String)null);
        noPortraitMessage = themeInfo.getParameter("noportraitmessage", (String)null);
    }

    @Override
    protected void layout()
    {
        super.layout();

        // determine label column width

        nameLabel.setSize(nameLabel.getPreferredWidth(), nameLabel.getPreferredHeight());
        genderLabel.setSize(genderLabel.getPreferredWidth(), genderLabel.getPreferredHeight());
        hairLabel.setSize(hairLabel.getPreferredWidth(), hairLabel.getPreferredHeight());
        beardLabel.setSize(beardLabel.getPreferredWidth(), beardLabel.getPreferredHeight());
        skinLabel.setSize(skinLabel.getPreferredWidth(), skinLabel.getPreferredHeight());
        clothingLabel.setSize(clothingLabel.getPreferredWidth(), clothingLabel.getPreferredHeight());
        portraitLabel.setSize(portraitLabel.getPreferredWidth(), portraitLabel.getPreferredHeight());

        int labelColumnWidth = Math.max(nameLabel.getWidth(), genderLabel.getWidth());
        labelColumnWidth = Math.max(labelColumnWidth, hairLabel.getWidth());
        labelColumnWidth = Math.max(labelColumnWidth, beardLabel.getWidth());
        labelColumnWidth = Math.max(labelColumnWidth, skinLabel.getWidth());
        labelColumnWidth = Math.max(labelColumnWidth, portraitLabel.getWidth());
        labelColumnWidth = Math.max(labelColumnWidth, clothingLabel.getWidth());
        labelColumnWidth += gap;

        // layout content column
        int contentColumnX = getInnerX() + labelColumnWidth + gap;

        // layout name field
        nameField.setSize(nameField.getPreferredWidth(), nameField.getPreferredHeight());
        nameField.setPosition(contentColumnX, getInnerY());
        int nameCenterY = nameField.getY() + nameField.getHeight() / 2;

        randomName.setSize(randomName.getPreferredWidth(), randomName.getPreferredHeight());
        randomName.setPosition(nameField.getRight() + smallGap, nameCenterY - randomName.getHeight() / 2);

        int curY = nameField.getBottom() + gap;

        // layout gender selectors
        int curX = contentColumnX;
        for (GenderSelector g : genderSelectors) {
            g.setSize(g.getPreferredWidth(), g.getPreferredHeight());
            g.setPosition(curX, curY);

            curX = g.getRight() + smallGap;
        }
        int genderCenterY = genderSelectors.get(0).getY() + genderSelectors.get(0).getHeight() / 2;

        // layout hair selector widgets
        hairColorPicker.setSize(hairColorPicker.getPreferredWidth(), hairColorPicker.getPreferredHeight());
        int hairCenterY = genderSelectors.get(0).getBottom() + gap + hairColorPicker.getHeight() / 2;

        hairStyleLabel.setSize(hairStyleLabel.getPreferredWidth(), hairStyleLabel.getPreferredHeight());
        prevHairStyle.setSize(prevHairStyle.getPreferredWidth(), hairStyleLabel.getPreferredHeight());
        nextHairStyle.setSize(nextHairStyle.getPreferredWidth(), hairStyleLabel.getPreferredHeight());

        prevHairStyle.setPosition(contentColumnX, hairCenterY - prevHairStyle.getHeight() / 2);
        hairStyleLabel.setPosition(prevHairStyle.getRight() + smallGap, hairCenterY - hairStyleLabel.getHeight() / 2);
        nextHairStyle.setPosition(hairStyleLabel.getRight() + smallGap, hairCenterY - nextHairStyle.getHeight() / 2);
        hairColorPicker.setPosition(nextHairStyle.getRight() + smallGap, hairCenterY - hairColorPicker.getHeight() / 2);

        // layout beard selector widgets
        beardColorPicker.setSize(beardColorPicker.getPreferredWidth(), beardColorPicker.getPreferredHeight());
        int beardCenterY = hairColorPicker.getBottom() + gap + beardColorPicker.getHeight() / 2;

        beardStyleLabel.setSize(beardStyleLabel.getPreferredWidth(), beardStyleLabel.getPreferredHeight());
        prevBeardStyle.setSize(prevBeardStyle.getPreferredWidth(), beardStyleLabel.getPreferredHeight());
        nextBeardStyle.setSize(nextBeardStyle.getPreferredWidth(), beardStyleLabel.getPreferredHeight());

        prevBeardStyle.setPosition(contentColumnX, beardCenterY - prevBeardStyle.getHeight() / 2);
        beardStyleLabel.setPosition(prevBeardStyle.getRight() + smallGap, beardCenterY - beardStyleLabel.getHeight() / 2);
        nextBeardStyle.setPosition(beardStyleLabel.getRight() + smallGap, beardCenterY - nextBeardStyle.getHeight() / 2);
        beardColorPicker.setPosition(nextBeardStyle.getRight() + smallGap, beardCenterY - beardColorPicker.getHeight() / 2);

        // layout skin selector
        skinColorPicker.setSize(skinColorPicker.getPreferredWidth(), skinColorPicker.getPreferredHeight());
        int skinCenterY = beardColorPicker.getBottom() + gap + skinColorPicker.getHeight() / 2;
        skinColorPicker.setPosition(contentColumnX, skinCenterY - skinColorPicker.getHeight() / 2);

        // layout clothing selector
        clothingColorPicker.setSize(clothingColorPicker.getPreferredWidth(), clothingColorPicker.getPreferredHeight());
        int clothingCenterY = skinColorPicker.getBottom() + gap + clothingColorPicker.getHeight() / 2;
        clothingColorPicker.setPosition(contentColumnX, clothingCenterY - clothingColorPicker.getHeight() / 2);

        // layout portrait selector
        choosePortrait.setSize(choosePortrait.getPreferredWidth(), choosePortrait.getPreferredHeight());
        int portraitCenterY = clothingColorPicker.getBottom() + gap + choosePortrait.getHeight() / 2;
        choosePortrait.setPosition(contentColumnX, portraitCenterY - choosePortrait.getHeight() / 2);

        // layout labels column
        nameLabel.setPosition(contentColumnX - gap - nameLabel.getWidth(),
                nameCenterY - nameLabel.getHeight() / 2);
        genderLabel.setPosition(contentColumnX - gap - genderLabel.getWidth(),
                genderCenterY - genderLabel.getHeight() / 2);
        hairLabel.setPosition(contentColumnX - gap - hairLabel.getWidth(),
                hairCenterY - hairLabel.getHeight() / 2);
        beardLabel.setPosition(contentColumnX - gap - beardLabel.getWidth(),
                beardCenterY - beardLabel.getHeight() / 2);
        skinLabel.setPosition(contentColumnX - gap - skinLabel.getWidth(),
                skinCenterY - skinLabel.getHeight() / 2);
        clothingLabel.setPosition(contentColumnX - gap - clothingLabel.getWidth(),
                clothingCenterY - clothingLabel.getHeight() / 2);
        portraitLabel.setPosition(contentColumnX - gap - portraitLabel.getWidth(),
                portraitCenterY - portraitLabel.getHeight() / 2);

        // layout appearance holder
        appearanceHolder.setSize(appearanceHolder.getPreferredWidth(), appearanceHolder.getPreferredHeight());

        // determine farther x we have layed out so far
        int maxX = Math.max(genderSelectors.get(genderSelectors.size() - 1).getRight(), nameField.getRight());
        maxX = Math.max(beardColorPicker.getRight(), maxX);
        maxX = Math.max(hairColorPicker.getRight(), maxX);
        maxX = Math.max(skinColorPicker.getRight(), maxX);
        maxX = Math.max(clothingColorPicker.getRight(), maxX);
        maxX = Math.max(choosePortrait.getRight(), maxX);

        int centerX = (getInnerRight() + maxX) / 2;

        appearanceHolder.setPosition(centerX - appearanceHolder.getWidth() / 2, getInnerY() + gap);
    }

    private void updateWorkingCopy()
    {
        workingCopy = getCharacter().getWorkingCopy();
        characterViewer.character = workingCopy;

        portraitViewer.setCreature(workingCopy);

        // set next button state
        Button next = getNextButton();

        if (nameField.getTextLength() == 0) {
            next.setTooltipContent(noNameMessage);
            next.setEnabled(false);
        } else
            if (getCharacter().getSelectedGender() == null) {
                next.setTooltipContent(noGenderMessage);
                next.setEnabled(false);
            } else
                if (getCharacter().getSelectedPortrait() == null) {
                    next.setTooltipContent(noPortraitMessage);
                    next.setEnabled(false);
                } else {
                    next.setTooltipContent(null);
                    next.setEnabled(true);
                }

        // set editing buttons state
        boolean editEnabled = getCharacter().getSelectedGender() != null;
        boolean hasBeard = getCharacter().getSelectedGender() != Ruleset.Gender.Female;

        if (beardColorPicker != null) {
            beardColorPicker.setEnabled(editEnabled && hasBeard && getCharacter().getSelectedRace().hasBeard());
        }
        if (hairColorPicker != null) {
            hairColorPicker.setEnabled(editEnabled && getCharacter().getSelectedRace().hasHair());
        }

        nextHairStyle.setEnabled(editEnabled && getCharacter().getSelectedRace().hasHair());
        prevHairStyle.setEnabled(editEnabled && getCharacter().getSelectedRace().hasHair());
        nextBeardStyle.setEnabled(editEnabled && hasBeard && getCharacter().getSelectedRace().hasBeard());
        prevBeardStyle.setEnabled(editEnabled && hasBeard && getCharacter().getSelectedRace().hasBeard());
        choosePortrait.setEnabled(editEnabled);

        if (skinColorPicker != null) skinColorPicker.setEnabled(editEnabled);
        clothingColorPicker.setEnabled(editEnabled);

        portraitViewer.setVisible(getCharacter().getSelectedPortrait() != null);

        invalidateLayout();
    }

    private void createHairBeardAndSkinPickers()
    {
        if (hairColorPicker != null) {
            this.removeChild(hairColorPicker);
        }

        if (skinColorPicker != null) {
            this.removeChild(skinColorPicker);
        }

        if (beardColorPicker != null) {
            this.removeChild(beardColorPicker);
        }

        List<String> hairBeardColorsList = workingRace.getHairAndBeardColors();
        Color[] hairBeardColors = new Color[hairBeardColorsList.size()];
        int i = 0;
        for (String color : hairBeardColorsList) {
            hairBeardColors[i] = Color.parserColor(color);
            i++;
        }

        List<String> skinColorsList = workingRace.getSkinColors();
        Color[] skinColors = new Color[skinColorsList.size()];
        i = 0;
        for (String color : skinColorsList) {
            skinColors[i] = Color.parserColor(color);
            i++;
        }

        hairColorPicker = new ColorPicker(new ColorCallback()
        {
            @Override
            public void colorSet(Color color)
            {
                setHairColor(color);
            }

            @Override
            public Color getCurrentColor()
            {
                return getCharacter().getSelectedHairColor();
            }
        }, hairBeardColors);
        hairColorPicker.setTheme("haircolorpicker");
        add(hairColorPicker);

        beardColorPicker = new ColorPicker(new ColorCallback()
        {
            @Override
            public void colorSet(Color color)
            {
                setBeardColor(color);
            }

            @Override
            public Color getCurrentColor()
            {
                return getCharacter().getSelectedBeardColor();
            }
        }, hairBeardColors);
        beardColorPicker.setTheme("beardcolorpicker");
        add(beardColorPicker);

        skinColorPicker = new ColorPicker(new ColorCallback()
        {
            @Override
            public void colorSet(Color color)
            {
                setSkinColor(color);
            }

            @Override
            public Color getCurrentColor()
            {
                return getCharacter().getSelectedSkinColor();
            }
        }, skinColors);
        skinColorPicker.setTheme("skincolorpicker");
        add(skinColorPicker);

        for (i = 0; i < hairValid.length; i++) {
            hairValid[i] = false;
        }
        for (int hairIndex : workingRace.getSelectableHairIndices()) {
            hairValid[hairIndex] = true;
        }

        for (i = 0; i < beardsValid.length; i++) {
            beardsValid[i] = false;
        }
        for (int beardIndex : workingRace.getSelectableBeardIndices()) {
            beardsValid[beardIndex] = true;
        }
    }

    @Override
    protected void updateCharacter()
    {
        // reset the fields as needed
        if (getCharacter().getSelectedRace() != workingRace) {
            workingRace = getCharacter().getSelectedRace();
            createHairBeardAndSkinPickers();
        }

        if (getCharacter().getSelectedName() == null) {
            nameField.setText("");
        }

        if (getCharacter().getSelectedGender() == null) {
            for (GenderSelector g : genderSelectors) {
                g.setSelected(false);
            }
        }

        if (getCharacter().getSelectedHairIcon() == null) {
            currentHairStyle = workingRace.getDefaultHairIndex();
            currentHairColor = Color.parserColor(workingRace.getDefaultHairColor());
        }

        if (getCharacter().getSelectedBeardIcon() == null) {
            currentBeardStyle = workingRace.getDefaultBeardIndex();
            currentBeardColor = Color.parserColor(workingRace.getDefaultBeardColor());
        }

        if (getCharacter().getSelectedSkinColor() == null) {
            currentSkinColor = Color.parserColor(workingRace.getDefaultSkinColor());
        }

        if (getCharacter().getSelectedClothingColor() == null) {
            currentClothingColor = DEFAULT_CLOTHING_COLOR;
        }

        updateWorkingCopy();
    }

    @Override
    public void portraitSelected(String portrait)
    {
        getCharacter().setSelectedPortrait(portrait);

        updateWorkingCopy();
    }

    private void setGender(Ruleset.Gender gender)
    {
        getCharacter().setSelectedGender(gender);

        getCharacter().setSelectedSkinColor(currentSkinColor);
        getCharacter().setSelectedClothingColor(currentClothingColor);

        getCharacter().setSelectedHairIcon("subIcons/hair" + numberFormat.format(currentHairStyle));
        getCharacter().setSelectedHairColor(currentHairColor);

        if (gender == Ruleset.Gender.Female) {
            getCharacter().setSelectedBeardIcon(null);
            getCharacter().setSelectedBeardColor(null);
            currentBeardStyle = getCharacter().getSelectedRace().getDefaultBeardIndex();
        } else {
            getCharacter().setSelectedBeardIcon("subIcons/beard" + numberFormat.format(currentBeardStyle));
            getCharacter().setSelectedBeardColor(currentBeardColor);
        }

        updateWorkingCopy();
    }

    private void setName(String name)
    {
        getCharacter().setSelectedName(name);

        updateWorkingCopy();
    }

    private void setClothingColor(Color color)
    {
        currentClothingColor = color;
        getCharacter().setSelectedClothingColor(color);

        updateWorkingCopy();
    }

    private void setSkinColor(Color color)
    {
        currentSkinColor = color;

        getCharacter().setSelectedSkinColor(color);

        updateWorkingCopy();
    }

    private void setHairColor(Color color)
    {
        currentHairColor = color;

        getCharacter().setSelectedHairColor(color);

        updateWorkingCopy();
    }

    private void setBeardColor(Color color)
    {
        currentBeardColor = color;

        getCharacter().setSelectedBeardColor(color);

        updateWorkingCopy();
    }

    private void previousBeardStyle()
    {
        int startStyle = currentBeardStyle;
        do {
            currentBeardStyle--;
            if (currentBeardStyle == 0) currentBeardStyle = beardStyleMax;
        } while (!beardsValid[currentBeardStyle] && currentBeardStyle != startStyle);

        getCharacter().setSelectedBeardIcon("subIcons/beard" + numberFormat.format(currentBeardStyle));

        updateWorkingCopy();
    }

    private void nextBeardStyle()
    {
        int startStyle = currentBeardStyle;
        do {
            currentBeardStyle++;
            if (currentBeardStyle > beardStyleMax) currentBeardStyle = 1;
        } while (!beardsValid[currentBeardStyle] && currentBeardStyle != startStyle);

        getCharacter().setSelectedBeardIcon("subIcons/beard" + numberFormat.format(currentBeardStyle));

        updateWorkingCopy();
    }

    private void previousHairStyle()
    {
        int startStyle = currentHairStyle;
        do {
            currentHairStyle--;
            if (currentHairStyle == 0) currentHairStyle = hairStyleMax;
        } while (!hairValid[currentHairStyle] && currentHairStyle != startStyle);

        getCharacter().setSelectedHairIcon("subIcons/hair" + numberFormat.format(currentHairStyle));

        updateWorkingCopy();
    }

    private void nextHairStyle()
    {
        int startStyle = currentHairStyle;
        do {
            currentHairStyle++;
            if (currentHairStyle > hairStyleMax) currentHairStyle = 1;
        } while (!hairValid[currentHairStyle] && currentHairStyle != startStyle);

        getCharacter().setSelectedHairIcon("subIcons/hair" + numberFormat.format(currentHairStyle));

        updateWorkingCopy();
    }

    private class AppearanceArea extends Widget
    {
        @Override
        public int getPreferredWidth()
        {
            return getBorderHorizontal() + portraitViewer.getPreferredWidth() +
                    characterViewer.getPreferredWidth();
        }

        @Override
        public int getPreferredHeight()
        {
            return Math.max(portraitViewer.getPreferredHeight(), characterViewer.getPreferredHeight()) +
                    getBorderVertical();
        }

        @Override
        protected void layout()
        {
            portraitViewer.setSize(portraitViewer.getPreferredWidth(), portraitViewer.getPreferredHeight());
            portraitViewer.setPosition(getInnerX(), getInnerY());

            characterViewer.setSize(characterViewer.getPreferredWidth(), characterViewer.getPreferredHeight());
            characterViewer.setPosition(portraitViewer.getRight(),
                    getInnerY() + getInnerHeight() / 2 - characterViewer.getHeight() / 2);
        }
    }

    private class PortraitViewer extends BasePortraitViewer
    {
        private PortraitViewer(Creature creature)
        {
            super(creature);
        }

        @Override
        public int getPreferredWidth()
        {
            return 100 + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            return 100 + getBorderVertical();
        }
    }

    private class GenderSelector extends BuildablePropertySelector
    {
        private Ruleset.Gender gender;

        private GenderSelector(Ruleset.Gender gender, String icon)
        {
            super(gender.toString(), IconFactory.createIcon(icon), false);
            this.gender = gender;
            this.setSelectable(true);
        }

        @Override
        protected void onMouseClick()
        {
            setGender(gender);

            for (GenderSelector g : genderSelectors) {
                g.setSelected(false);
            }

            this.setSelected(true);
        }
    }

    private class ColorPicker extends Widget
    {
        private int numColumns;
        private final ColorCallback callback;

        private List<ColorButton> colorButtons;
        private Button otherColorButton;

        private ColorPicker(ColorCallback callback, Color[] buttonColors)
        {
            this.callback = callback;

            colorButtons = new ArrayList<ColorButton>();
            for (Color color : buttonColors) {
                colorButtons.add(new ColorButton(color, callback));
            }

            otherColorButton = new Button("Other...");
            otherColorButton.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    ColorSelectorPopup popup = new ColorSelectorPopup(BuilderPaneCosmetic.this);
                    popup.addCallback(new ColorSelectorPopup.Callback()
                    {
                        @Override
                        public void colorSelected(Color color)
                        {
                            ColorPicker.this.callback.colorSet(color);
                        }
                    });
                    popup.setColor(ColorPicker.this.callback.getCurrentColor());
                    popup.openPopupCentered();
                }
            });
            otherColorButton.setTheme("othercolorbutton");
            add(otherColorButton);

            for (ColorButton button : colorButtons) {
                add(button);
            }
        }

        @Override
        protected void layout()
        {
            int curX = getInnerX();
            int curY = getInnerY();
            int count = 0;

            for (ColorButton button : colorButtons) {
                button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
                button.setPosition(curX, curY);

                curX = button.getRight();

                count++;

                if (count == numColumns) {
                    count = 0;
                    curX = getInnerX();
                    curY = button.getBottom();
                }
            }

            otherColorButton.setSize(getInnerRight() - curX, colorButtons.get(0).getPreferredHeight());
            otherColorButton.setPosition(curX, curY);
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            this.numColumns = themeInfo.getParameter("numcolumns", 0);
        }

        @Override
        public int getPreferredWidth()
        {
            return colorButtons.get(0).getPreferredWidth() * numColumns + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            return colorButtons.get(0).getPreferredHeight() * ((colorButtons.size() / numColumns) + 1);
        }
    }

    private interface ColorCallback
    {
        public void colorSet(Color color);

        public Color getCurrentColor();
    }

    private class ColorButton extends Button implements Runnable
    {
        private Color color;
        private ColorCallback callback;

        private ColorButton(Color color, ColorCallback callback)
        {
            setTintAnimator(new TintAnimator(new TintAnimator.GUITimeSource(this), color));
            addCallback(this);

            this.color = color;
            this.callback = callback;
        }

        @Override
        public void run()
        {
            callback.colorSet(color);
        }
    }

    private class CharacterViewer extends Widget
    {
        private PC character;

        @Override
        protected void paintWidget(GUI gui)
        {
            if (character != null) {
                character.uiDraw(getInnerX(), getInnerY());
            }
        }

        @Override
        public int getPreferredInnerWidth()
        {
            return Game.TILE_SIZE;
        }

        @Override
        public int getPreferredInnerHeight()
        {
            return Game.TILE_SIZE;
        }
    }
}
