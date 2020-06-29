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

package hale.mainmenu;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hale.Config;
import hale.Game;
import hale.loading.CampaignLoadingTaskList;
import hale.loading.LoadingTaskList;
import hale.loading.LoadingWaitPopup;
import hale.resource.Sprite;
import hale.resource.SpriteManager;
import hale.rules.Campaign;
import hale.util.FileUtil;
import hale.util.Logger;
import hale.util.Point;
import hale.util.SaveGameUtil;
import hale.widgets.HTMLPopup;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;

/**
 * The main menu widget.  This is what is displayed when the player first starts the game.
 * <p>
 * It handles choosing a campaign and selecting a party.  From this menu, the player can
 * access options, release notes, credits, or start the game proper.
 *
 * @author Jared Stephen
 */

public class MainMenu extends DesktopArea implements LoadGamePopup.Callback
{
    private Runnable exitCallback;

    private Sprite backgroundSprite;
    private Point backgroundSpriteOffset;

    private final GUI gui;

    private LoadingTaskList loader;
    private LoadingWaitPopup popup;

    private boolean menuRunning = true;
    private boolean exit = false;
    private boolean restart = false;
    private boolean exitOnLoad = false;

    private String loadGame = null;

    private final Label campaignLabel;

    private int buttonGap, titleOffset;

    private final Button campaignButton;
    private final Button newGameButton;
    private final Button loadGameButton;
    private final Button updateButton;
    private final Button optionsButton;
    private final Button creditsButton;
    private final Button exitButton;

    private final Button releaseNotesButton;
    private final Label versionLabel;

    private final List<PopupWindow> popupsToShow = new ArrayList<PopupWindow>();
    private final List<PopupWindow> popupsToHide = new ArrayList<PopupWindow>();

    private String version;

    /**
     * Create a new MainMenu, with buttons for choosing campaign, loading games,
     * launching the editor, etc.
     */

    public MainMenu()
    {
        this.setTheme("mainmenu");

        gui = new GUI(this, Game.renderer);
        gui.setSize();
        gui.applyTheme(Game.themeManager);
        gui.setTooltipDelay(Game.config.getTooltipDelay());

        campaignLabel = new Label();
        campaignLabel.setTheme("campaignlabel");
        this.add(campaignLabel);

        try {
            version = FileUtil.readFileAsString("docs/version.txt");
        } catch (IOException e) {
            Logger.appendToErrorLog("Error reading version information", e);
        }

        campaignButton = new Button();
        campaignButton.setTheme("campaignbutton");
        campaignButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                CampaignPopup popup = new CampaignPopup(MainMenu.this);
                popup.openPopupCentered();
            }
        });
        this.add(campaignButton);

        newGameButton = new Button();
        newGameButton.setTheme("newgamebutton");
        newGameButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                NewGameWindow newGameWindow = new NewGameWindow(MainMenu.this);

                if (Game.curCampaign.getStartingCharacter() == null && Game.curCampaign.getMaxPartySize() == 1) {
                    PartyFormationWindow formationWindow = new PartyFormationWindow(MainMenu.this,
                            null, newGameWindow.getCharactersUsedInParties());
                    add(formationWindow);
                } else {
                    add(newGameWindow);
                }
            }
        });
        this.add(newGameButton);

        loadGameButton = new Button();
        loadGameButton.setTheme("loadgamebutton");
        loadGameButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                // show the load game popup without any warnings about losing progress
                LoadGamePopup popup = new LoadGamePopup(MainMenu.this, true);
                popup.setCallback(MainMenu.this);
                popup.openPopupCentered();
            }
        });
        this.add(loadGameButton);

        creditsButton = new Button();
        creditsButton.setTheme("creditsbutton");
        creditsButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    HTMLPopup popup = new HTMLPopup(new File("docs/Contributors.html"), MainMenu.this);
                    popup.setSize(640, 480);
                    popup.openPopupCentered();
                } catch (IOException e) {
                    Logger.appendToErrorLog("Error retrieving contributors file", e);
                }
            }
        });
        this.add(creditsButton);

        updateButton = new Button();
        updateButton.setTheme("updatebutton");
        updateButton.setVisible(false);
        updateButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                new Updater().runUpdater(MainMenu.this);
            }
        });
        this.add(updateButton);

        optionsButton = new Button();
        optionsButton.setTheme("optionsbutton");
        optionsButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                OptionsPopup popup = new OptionsPopup(MainMenu.this);
                popup.openPopupCentered();
            }
        });
        this.add(optionsButton);

        exitButton = new Button();
        exitButton.setTheme("exitbutton");
        exitButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                showExitPopup();
            }
        });
        this.add(exitButton);

        // load the background image
        backgroundSprite = SpriteManager.getSpriteAnyExtension("mainmenu");
        backgroundSpriteOffset = new Point();
        if (backgroundSprite != null) {
            backgroundSpriteOffset.x = (Game.config.getResolutionX() - backgroundSprite.getWidth()) / 2;
            backgroundSpriteOffset.y = (Game.config.getResolutionY() - backgroundSprite.getHeight()) / 2;
        }

        // load last open campaign from file if it exists
        String campaignID = this.getLastOpenCampaign();
        if (campaignID != null) this.loadCampaign(campaignID);


        if (version.equals("svn")) {
            versionLabel = new Label("Build ID: " + Game.config.getVersionID());
        } else
            if (version.equals("disabled")) {
                versionLabel = new Label("Version Disabled");
            } else {
                versionLabel = new Label("Version: " + version);

                File updateAvailable = new File(Game.plataform.getConfigDirectory() + "updateAvailable.txt");
                if (updateAvailable.isFile()) {
                    updateButton.setVisible(true);
                } else
                    if (Game.osType == Game.OSType.Windows) {
                        // only attempt auto updates under windows
                        // in linux, updates are downloaded via the unified package manager
                        // for each distro

                        long curTime = System.currentTimeMillis();
                        long interval = Game.config.getCheckForUpdatesInterval();
                        long lastTime = Config.getLastCheckForUpdatesTime();

                        if (lastTime + interval < curTime) {
                            CheckForUpdatesTask task = new CheckForUpdatesTask(this);
                            task.start();
                        }

                        Config.writeCheckForUpdatesTime(curTime);
                    }
            }
        versionLabel.setTheme("versionlabel");
        this.add(versionLabel);

        releaseNotesButton = new Button();
        releaseNotesButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    HTMLPopup popup = new HTMLPopup(new File("docs/Release Notes.html"), MainMenu.this);
                    popup.setSize(640, 480);
                    popup.openPopupCentered();
                } catch (IOException e) {
                    Logger.appendToErrorLog("Error retrieving release notes", e);
                }
            }
        });
        releaseNotesButton.setTheme("releasenotesbutton");
        add(releaseNotesButton);
    }

    private void showExitPopup()
    {
        ConfirmationPopup popup = new ConfirmationPopup(MainMenu.this);
        popup.setTitleText("Are you sure you wish to exit?");
        popup.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                exit = true;
                menuRunning = false;
            }
        });
        popup.openPopupCentered();
    }

    /**
     * Enables the update button based on the finding of a new version
     */

    public void enableUpdate()
    {
        updateButton.setVisible(true);
    }

    /**
     * Returns the version string of this current version
     *
     * @return the version string
     */

    public String getVersion()
    {
        return version;
    }

    /**
     * Hides the specified popup the next time the GUI updates
     *
     * @param popup
     */

    public void hidePopup(PopupWindow popup)
    {
        synchronized (popupsToHide) {
            popupsToHide.add(popup);
        }
    }

    /**
     * Shows the specified popup the next time the GUI updates
     *
     * @param popup
     */

    public void showPopup(PopupWindow popup)
    {
        synchronized (popupsToShow) {
            popupsToShow.add(popup);
        }
    }

    /**
     * Sets the visible state of all buttons in this main menu to the
     * specified value.  Note that if a widget hides these buttons,
     * it needs to re-show them prior to closing
     *
     * @param visible
     */

    protected void setButtonsVisible(boolean visible)
    {
        campaignButton.setVisible(visible);
        newGameButton.setVisible(visible);
        loadGameButton.setVisible(visible);
        //updateButton.setVisible(visible);
        optionsButton.setVisible(visible);
        exitButton.setVisible(visible);
        creditsButton.setVisible(visible);

        releaseNotesButton.setEnabled(visible);
    }

    @Override
    protected void paintWidget(GUI gui)
    {
        if (backgroundSprite != null) {
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            //backgroundSprite.draw(backgroundSpriteOffset.x, backgroundSpriteOffset.y);
            backgroundSprite.draw(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        buttonGap = themeInfo.getParameter("buttonGap", 0);
        titleOffset = themeInfo.getParameter("titleOffset", 0);
    }

    @Override
    protected void layout()
    {
        super.layout();

        int resX = Game.config.getResolutionX();

        campaignButton.setSize(campaignButton.getPreferredWidth(), campaignButton.getPreferredHeight());
        newGameButton.setSize(newGameButton.getPreferredWidth(), newGameButton.getPreferredHeight());
        loadGameButton.setSize(loadGameButton.getPreferredWidth(), loadGameButton.getPreferredHeight());
        creditsButton.setSize(creditsButton.getPreferredWidth(), creditsButton.getPreferredHeight());
        optionsButton.setSize(optionsButton.getPreferredWidth(), optionsButton.getPreferredHeight());
        exitButton.setSize(exitButton.getPreferredWidth(), exitButton.getPreferredHeight());

        int buttonHeight = campaignButton.getHeight() + newGameButton.getHeight() +
                loadGameButton.getHeight() + updateButton.getHeight() +
                optionsButton.getHeight() + exitButton.getHeight() + 6 * buttonGap;

        int buttonY = (Game.config.getResolutionY() - buttonHeight) / 2;

        campaignButton.setPosition((resX - campaignButton.getWidth()) / 2, buttonY);
        newGameButton.setPosition((resX - newGameButton.getWidth()) / 2, campaignButton.getBottom() + buttonGap);
        loadGameButton.setPosition((resX - loadGameButton.getWidth()) / 2, newGameButton.getBottom() + buttonGap);
        creditsButton.setPosition((resX - creditsButton.getWidth()) / 2, loadGameButton.getBottom() + buttonGap);
        optionsButton.setPosition((resX - optionsButton.getWidth()) / 2, creditsButton.getBottom() + buttonGap);
        exitButton.setPosition((resX - exitButton.getWidth()) / 2, optionsButton.getBottom() + buttonGap);

        campaignLabel.setSize(campaignLabel.getPreferredWidth(), campaignLabel.getPreferredHeight());
        campaignLabel.setPosition((resX - campaignLabel.getWidth()) / 2, buttonY - campaignLabel.getHeight() - titleOffset);

        releaseNotesButton.setSize(releaseNotesButton.getPreferredWidth(), releaseNotesButton.getPreferredHeight());
        releaseNotesButton.setPosition(getInnerRight() - releaseNotesButton.getWidth(),
                getInnerBottom() - releaseNotesButton.getHeight());

        versionLabel.setSize(versionLabel.getPreferredWidth(), versionLabel.getPreferredHeight());
        versionLabel.setPosition(getInnerRight() - versionLabel.getWidth(),
                releaseNotesButton.getY() - versionLabel.getHeight());

        updateButton.setSize(updateButton.getPreferredWidth(), updateButton.getPreferredHeight());
        updateButton.setPosition(getInnerRight() - updateButton.getWidth(), 0);
    }

    private void handlePopups()
    {
        synchronized (popupsToShow) {
            for (PopupWindow p : popupsToShow) {
                p.openPopupCentered();
            }

            popupsToShow.clear();
        }

        synchronized (popupsToHide) {
            for (PopupWindow p : popupsToHide) {
                p.closePopup();
            }

            popupsToHide.clear();
        }
    }

    /**
     * This function is called after creating the MainMenu.  Runs the main display
     * loop for the menu until the user either starts or loads a game, launches the
     * editor, exits the menu, or selects a new resolution in the config (which causes
     * the MainMenu to be reloaded)
     *
     * @return the MainMenuAction that should be taken by the game
     */

    public MainMenuAction mainLoop()
    {
        update();

        while (menuRunning) {
            if (loader != null && !loader.isAlive()) {
                popup.closePopup();
                MainMenu.this.update();
                loader = null;

                if (exitOnLoad) {
                    menuRunning = false;
                }
            }

            handlePopups();

            Game.textureLoader.update();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            try {
                gui.update();
            } catch (Exception e) {
                Logger.appendToErrorLog("Error in GUI update", e);
            }

            Display.update(false);
            GL11.glGetError();
            Display.sync(60);
            Display.processMessages();

            if (Display.isCloseRequested()) {
                showExitPopup();
            }
        }

        if (exitCallback != null) {
            exitCallback.run();
        }


        gui.destroy();

        if (exit) {
            return new MainMenuAction(MainMenuAction.Action.Exit);
        } else
            if (restart) {
                return new MainMenuAction(MainMenuAction.Action.Restart);
            } else
                if (loadGame != null) {
                    return new MainMenuAction(loadGame);
                } else {
                    return new MainMenuAction(MainMenuAction.Action.NewGame);
                }
    }

    /**
     * Sets the specified callback to be run after this main menu has completed its
     * main loop, just prior to exiting
     *
     * @param callback the callback to run()
     */

    public void setExitCallback(Runnable callback)
    {
        this.exitCallback = callback;
    }

    /**
     * Sets this MainMenu to exit its main loop upon completion of the campaign loading process
     * It will not exit until completion of the load, regardless of other exit commands
     */

    public void setExitOnLoad()
    {
        exitOnLoad = true;
    }

    /**
     * Specify that the menu should be restarted.  The main loop
     * will terminate on its next iteration after calling this
     */

    public void restartMenu()
    {
        this.menuRunning = false;
        this.restart = true;
    }

    /**
     * Save the specified Campaign ID to the campaigns/lastOpenCampaign.txt
     * file.  This file is automatically read at game startup and will
     * automatically choose the specified campaign when the MainMenu is started.
     *
     * @param id the ID of the campaign to write
     */

    public static void writeLastOpenCampaign(String id)
    {
        File f = new File(Game.plataform.getConfigDirectory() + "lastOpenCampaign.txt");

        try {
            FileWriter writer = new FileWriter(f, false);
            writer.write(id);
            writer.close();

        } catch (Exception e) {
            Logger.appendToErrorLog("Error writing last open campaign file.", e);
        }
    }

    /**
     * Called when the state of the menu has changed.  This method controls
     * enabling and disabling buttons as appropriate, setting title text,
     * and exiting the menu when a save game has been loaded
     * <p>
     * Note that this method should be called after one of the sub menu's
     * has either completed or been canceled, in order to enable the main
     * menu buttons again
     */

    public void update()
    {
        if (Game.curCampaign == null) {
            // if no campaign is selected, disable starting or loading a game
            newGameButton.setEnabled(false);
            loadGameButton.setEnabled(false);
        } else {
            newGameButton.setEnabled(true);
            loadGameButton.setEnabled(SaveGameUtil.getSaveGames().size() > 0);

            if (Game.curCampaign != null) {

                if (Game.curCampaign.party.size() > 0 || loadGame != null) {

                    if (exitOnLoad) {
                        setButtonsVisible(false);
                        campaignLabel.setVisible(false);

                    } else {
                        menuRunning = false;

                        // hide the menu while loading takes place
                        setVisible(false);
                    }

                }

                campaignLabel.setText(Game.curCampaign.getName());
            }
        }
    }

    private String getLastOpenCampaign()
    {
        try {
            return FileUtil.readFileAsString(Game.plataform.getConfigDirectory() + "lastOpenCampaign.txt");
        } catch (Exception e) {
            Logger.appendToErrorLog("Error loading last open campaign file.", e);
            return null;
        }
    }

    @Override
    public void loadGameAccepted(String saveGame)
    {
        this.loadGame = saveGame;
        update();
    }

    /**
     * The specified campaign is loaded into memory and set as the current campaign
     * in Game.curCampaign
     *
     * @param campaignID the resource ID of the campaign to load
     */

    public void loadCampaign(String campaignID)
    {
        Game.curCampaign = new Campaign(campaignID);

        loader = new CampaignLoadingTaskList();

        // recreate the bg sprite after clearing the sprite manager
        backgroundSprite = SpriteManager.getSpriteAnyExtension("mainmenu");

        loader.start();

        popup = new LoadingWaitPopup(MainMenu.this, "Loading " + campaignID);
        popup.setBGSprite(backgroundSprite);
        popup.setLoadingTaskList(loader);
        popup.openPopupCentered();
    }
}
