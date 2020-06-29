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

package main.java.hale.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.hale.AreaListener;
import main.java.hale.Game;
import main.java.hale.Keybindings;
import main.java.hale.defaultability.MouseActionList;
import main.java.hale.entity.Entity;
import main.java.hale.loading.LoadingTaskList;
import main.java.hale.loading.LoadingWaitPopup;
import main.java.hale.mainmenu.ConfirmQuitPopup;
import main.java.hale.mainmenu.MainMenuAction;
import main.java.hale.quickbar.QuickbarViewer;
import main.java.hale.resource.SpriteManager;
import main.java.hale.rules.Merchant;
import main.java.hale.rules.QuestEntry;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Logger;
import main.java.hale.util.Point;
import main.java.hale.widgets.EntityMouseover;
import main.java.hale.widgets.FixedFadeAway;
import main.java.hale.widgets.InitiativeTicker;
import main.java.hale.widgets.MainPane;
import main.java.hale.widgets.OverHeadFadeAway;
import main.java.hale.widgets.PortraitArea;
import main.java.hale.widgets.RightClickMenu;
import main.java.hale.widgets.TextAreaNoInput;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * The root widget containing all other widgets and controlling the basic game
 * flow.  This class includes the main loop of the game with rendering, input handling
 * and animating
 *
 * @author Jared
 */

public class MainViewer extends DesktopArea
{
    private final GUI gui;

    private final List<Entity> entityUpdateList = new ArrayList<Entity>();
    private final List<PopupWindow> popupsToShow = new ArrayList<PopupWindow>();
    private final List<PopupWindow> popupsToHide = new ArrayList<PopupWindow>();
    private final FPSCounter fpsCounter;

    public final CharacterWindow characterWindow;
    public final InventoryWindow inventoryWindow;
    public final MiniMapWindow miniMapWindow;
    public final LogWindow logWindow;
    public final MessagesWindow messagesWindow;

    public final ContainerWindow containerWindow;
    public final MerchantWindow merchantWindow;
    public final CraftingWindow craftingWindow;
    public final ScriptConsole scriptConsole;

    private final QuickbarViewer quickbarViewer;
    private final PortraitArea portraitArea;
    private final InitiativeTicker ticker;
    private final MainPane mainPane;

    private final RightClickMenu menu;
    private final EntityMouseover mouseOver;

    private final TextArea targeterDescription;
    private final HTMLTextAreaModel targeterDescriptionModel;

    private final List<OverHeadFadeAway> fadeAways;
    private final List<OverHeadFadeAway> fadeAwaysToAdd;

    private final Keybindings keyBindings;

    private MainMenuAction action;

    private boolean isRunning = false;
    private boolean exitGame = false;
    private boolean updateInterface = false;
    private QuestEntry newQuestEntry;

    public int mouseX, mouseY;
    private long frameTime;

    /**
     * Creates a new MainViewer
     */

    public MainViewer()
    {
        Game.mainViewer = this;
        Game.mouseActions = new MouseActionList();

        fadeAways = new ArrayList<OverHeadFadeAway>();
        fadeAwaysToAdd = new ArrayList<OverHeadFadeAway>();

        this.setTheme("");

        gui = new GUI(this, Game.renderer);
        gui.setSize();
        gui.applyTheme(Game.themeManager);
        gui.setTooltipDelay(Game.config.getTooltipDelay());

        messagesWindow = new MessagesWindow();
        messagesWindow.setVisible(false);

        characterWindow = new CharacterWindow();
        characterWindow.setVisible(false);

        inventoryWindow = new InventoryWindow();
        inventoryWindow.setVisible(false);

        containerWindow = new ContainerWindow();
        containerWindow.setVisible(false);

        craftingWindow = new CraftingWindow();
        craftingWindow.setVisible(false);

        miniMapWindow = new MiniMapWindow();
        miniMapWindow.setVisible(false);

        logWindow = new LogWindow();
        logWindow.setVisible(false);

        merchantWindow = new MerchantWindow();
        merchantWindow.setVisible(false);

        scriptConsole = new ScriptConsole();
        scriptConsole.setVisible(false);

        mouseOver = new EntityMouseover();
        mouseOver.setVisible(false);

        Game.areaViewer = new AreaViewer(Game.curCampaign.curArea);
        Game.areaListener = new AreaListener(Game.curCampaign.curArea, Game.areaViewer);
        Game.areaViewer.setListener(Game.areaListener);

        menu = new RightClickMenu(this);

        mainPane = new MainPane();
        fpsCounter = new FPSCounter();
        quickbarViewer = new QuickbarViewer();
        portraitArea = new PortraitArea();
        ticker = new InitiativeTicker();

        targeterDescriptionModel = new HTMLTextAreaModel();
        targeterDescription = new TextAreaNoInput(targeterDescriptionModel);
        targeterDescription.setTheme("targeterdescription");

        // create the default key bindings
        keyBindings = new Keybindings();
    }

    private void addWidgets()
    {
        this.add(Game.areaViewer);

        this.add(mainPane);
        if (Game.config.showFPS()) {
            this.add(fpsCounter);
        }

        this.add(quickbarViewer);
        this.add(portraitArea);
        this.add(ticker);
        this.add(mouseOver);
        this.add(targeterDescription);

        this.add(characterWindow);
        this.add(inventoryWindow);
        this.add(messagesWindow);
        this.add(containerWindow);
        this.add(craftingWindow);
        this.add(miniMapWindow);
        this.add(logWindow);
        this.add(merchantWindow);
        this.add(scriptConsole);

        mainPane.setMovementModeIcon();
    }

    /**
     * Removes the text at the center top of the screen describing a targeter
     */

    public void clearTargetTitleText()
    {
        targeterDescriptionModel.setHtml("");
    }

    /**
     * Adds text to the center top of the screen for displaying the status of a targeter
     *
     * @param line1 the main line, in large font
     * @param line2 a secondary line
     * @param line3 a tertiary line
     */

    public void setTargetTitleText(String line1, String line2, String line3)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family: large-red; text-align: center\">");
        sb.append(line1);
        sb.append("</div>");

        if (line2 != null) {
            sb.append("<div style=\"font-family: yellow; text-align: center;\">");
            sb.append(line2);
            sb.append("</div>");
        }

        if (line3 != null) {
            sb.append("<div style=\"font-family: orange; text-align: center;\">");
            sb.append(line3);
            sb.append("</div>");
        }

        targeterDescriptionModel.setHtml(sb.toString());
    }

    /**
     * Gets the main pane that holds most of UI buttons as well as the message box and quickbar
     *
     * @return the main pane
     */

    public MainPane getMainPane()
    {
        return mainPane;
    }

    /**
     * Gets the portrait area which holds all party member portraits
     *
     * @return the portrait area
     */

    public PortraitArea getPortraitArea()
    {
        return portraitArea;
    }

    /**
     * Gets the viewer for the currently active player's quickbar
     *
     * @return the quickbar viewer
     */

    public QuickbarViewer getQuickbarViewer()
    {
        return quickbarViewer;
    }

    /**
     * Gets the current area coordinate that the mouse is hovering over
     *
     * @return the mouse grid point
     */

    public Point getMouseGridPoint()
    {
        int x = Game.areaListener.getLastMouseX();
        int y = Game.areaListener.getLastMouseY();

        return AreaUtil.convertScreenToGrid(x, y);
    }

    /**
     * Sets the action for this widget to load a saved game file.  The action
     * will be initiated on main loop exit
     *
     * @param loadGame
     */

    public void setLoadGame(String loadGame)
    {
        this.action = new MainMenuAction(loadGame);
    }

    /**
     * Sets the action to the specified mainMenu action.  The action will
     * be initiated on main loop exit
     *
     * @param action
     */

    public void setMainMenuAction(MainMenuAction action)
    {
        this.action = action;
    }

    /**
     * Runs a loop for the purposes of loading a saved game file.  This loop
     * only shows the progress bar and waits on the loading task
     *
     * @param loader
     * @param popup
     */

    public void runLoadingLoop(LoadingTaskList loader, LoadingWaitPopup popup)
    {
        boolean running = true;

        while (running) {
            if (loader != null && !loader.isAlive()) {
                running = false;
                this.hidePopup(popup);
            }

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
                running = false;
            }
        }
    }

    /**
     * The entrance point for the main loop of the game.  Initializes the game
     * state and then runs the main loop, performing rendering and handling input
     *
     * @param newGame true if this is a new game, false if this is a loaded game
     * @return the main menu action that should be performed by the caller
     */

    public MainMenuAction runCampaign(boolean newGame)
    {
        addWidgets();

        // clear any old particles
        Game.particleManager.clear();

        isRunning = true;

        Game.curCampaign.party.validateItems();

        if (newGame) {
            // load tileset
            Game.curCampaign.getTileset(Game.curCampaign.curArea.getTileset()).loadTiles();
            Game.curCampaign.curArea.getTileGrid().cacheSprites();

            Game.mainViewer.addMessage("red", "Entered area " + Game.curCampaign.curArea.getName());
            Game.areaListener.nextTurn();

            Game.curCampaign.curArea.runOnAreaLoad(null);
        }

        // scroll to the selected party member
        updateContent(System.currentTimeMillis());
        gui.update();
        Game.areaViewer.scrollToCreature(Game.curCampaign.party.getSelected());

        // run the main loop
        while (isRunning) {
            // load any async textures
            Game.textureLoader.update();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GL11.glColor3f(1.0f, 1.0f, 1.0f);

            frameTime = System.currentTimeMillis();

            Game.areaViewer.update(frameTime);
            Game.particleManager.update(frameTime);
            Game.interfaceLocker.checkTime(frameTime);
            Game.timer.updateTime(frameTime);

            if (menu.shouldPopupToggle()) {
                menu.togglePopup();
            }

            updateContent(frameTime);

            try {
                gui.update();
            } catch (Exception e) {
                Logger.appendToErrorLog("Error in GUI update", e);
            }

            Display.update(false);
            GL11.glGetError();
            if (Game.config.capFPS()) Display.sync(60);
            Display.processMessages();

            if (Display.isCloseRequested()) {
                new ConfirmQuitPopup(this, ConfirmQuitPopup.QuitMode.ExitGame).openPopupCentered();
            }
        }

        gui.destroy();

        // interrupt any currently executing threads
        for (Thread thread : Game.getActiveThreads()) {
            thread.interrupt();
        }

        Game.interfaceLocker.clear();

        if (action != null) {
            return action;
        } else
            if (exitGame) {
                return new MainMenuAction(MainMenuAction.Action.Exit);
            } else {
                return new MainMenuAction(MainMenuAction.Action.ShowMainMenu);
            }
    }

    /**
     * Adds an OverHeadFadeAway with the specified properties to this viewer
     *
     * @param text  the fade away text
     * @param x     the x grid coordinate
     * @param y     the y grid coordinate
     * @param color the text color
     */

    public void addFadeAway(String text, int x, int y, Color color)
    {
        Point gridPoint = new Point(x, y);

        int offsetY = 0;

        synchronized (fadeAwaysToAdd) {
            for (OverHeadFadeAway fadeAway : fadeAwaysToAdd) {
                if (fadeAway.getGridPoint().equals(gridPoint)) {
                    offsetY += 22;
                }
            }

            OverHeadFadeAway fadeAway = new OverHeadFadeAway(text, gridPoint, color);
            fadeAway.setOffset(0, offsetY);
            fadeAwaysToAdd.add(fadeAway);
        }
    }

    /**
     * Adds a fixed position fade away to the specified screen coordinates
     *
     * @param text  the text to display
     * @param x     the x screen coordinate
     * @param y     the y screen coordinate
     * @param color the text color
     */

    public void addFixedFadeAway(String text, int x, int y, Color color)
    {
        Point screenPoint = new Point(x, y);

        synchronized (fadeAwaysToAdd) {
            FixedFadeAway fadeAway = new FixedFadeAway(text, screenPoint, color);
            fadeAway.setOffset(0, 0);
            fadeAwaysToAdd.add(fadeAway);
        }
    }

    /**
     * Gets the time in milliseconds as of the last frame draw
     *
     * @return the frame draw time in milliseconds
     */

    public long getFrameTime()
    {
        return frameTime;
    }

    /**
     * Returns the current list of overHeadFadeAways.  This list should
     * not be directly modified
     *
     * @return the current overheadfadeAways
     */

    public List<OverHeadFadeAway> getFadeAways()
    {
        return fadeAways;
    }

    /**
     * Causes the mainViewer to exit the main loop on the next loop update,
     * exiting the program entirely
     */

    public void exitGame()
    {
        this.isRunning = false;
        this.exitGame = true;
    }

    /**
     * Causes the mainViewer to exit the main loop on the next loop update,
     * exiting to the main menu
     */

    public void exitToMainMenu()
    {
        this.isRunning = false;
    }

    /**
     * Closes all open sub windows, sending the UI back to the base state
     * except for mainPane widgets
     */

    public void closeAllWindows()
    {
        craftingWindow.setVisible(false);
        containerWindow.setVisible(false);
        characterWindow.setVisible(false);
        inventoryWindow.setVisible(false);
        messagesWindow.setVisible(false);
        merchantWindow.setVisible(false);
        miniMapWindow.setVisible(false);
        logWindow.setVisible(false);

        for (Entity entity : Game.curCampaign.curArea.getEntities()) {
            entity.removeAllListeners();
        }

        portraitArea.closeLevelUpWindows();
    }

    /**
     * Sets the currently global active merchant
     *
     * @param merchant
     */

    public void setMerchant(Merchant merchant)
    {
        merchantWindow.setMerchant(merchant);
        inventoryWindow.setMerchant(merchant);
        updateInterface();
    }

    /**
     * Returns the current mouse over, which is displaying some basic information
     * about the entity or transition on the hex that the mouse is currently over
     *
     * @return the entity or transition mouse over
     */

    public EntityMouseover getMouseOver()
    {
        return mouseOver;
    }

    /**
     * Returns the right click menu that is used to show multiple default actions
     * or inventory actions to the player
     *
     * @return the right click menu
     */

    public RightClickMenu getMenu()
    {
        return menu;
    }

    /**
     * Causes the specified entity to be updated on the next viewer refresh
     *
     * @param entity
     */

    public void updateEntity(Entity entity)
    {
        if (entity == null) return;

        synchronized (entityUpdateList) {
            if (!entityUpdateList.contains(entity)) {
                entityUpdateList.add(entity);
            }
        }

        if (entity.isPlayerFaction() || entity == mouseOver.getSelectedEntity()) {
            updateInterface();
        }
    }

    /**
     * Causes the specified popupwindow to be hidden on the next viewer
     * refresh.  This allows showing popups in a thread safe way.
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
     * Causes the specified popup to be shown on the next viewer refresh.
     * This allows showing popups in a thread safe way.
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
     * Returns true if the player character should not be allowed to move due to open windows (such
     * as the merchant window), false otherwise
     *
     * @return whether movement is blocked due to open windows
     */

    public boolean isMoveDisabledDueToOpenWindows()
    {
        return (merchantWindow.isVisible() || containerWindow.isVisible() || craftingWindow.isVisible());
    }

    /**
     * Causes the entire interface to be refreshed on the next mainviewer update
     */

    public void updateInterface()
    {
        this.updateInterface = true;
    }

    private void updateContent(long curTime)
    {
        Iterator<OverHeadFadeAway> iter = fadeAways.iterator();
        while (iter.hasNext()) {
            OverHeadFadeAway fadeAway = iter.next();
            fadeAway.updateTime(curTime);
            if (fadeAway.isFinished()) {
                iter.remove();
                this.removeChild(fadeAway);
            }
        }

        synchronized (popupsToShow) {
            for (PopupWindow p : popupsToShow) {
                p.openPopupCentered();

                // cancel any current movement
                Game.interfaceLocker.interruptMovement();
            }
            popupsToShow.clear();
        }

        synchronized (popupsToHide) {
            for (PopupWindow p : popupsToHide) {
                p.closePopup();
            }

            popupsToHide.clear();
        }

        synchronized (fadeAwaysToAdd) {
            for (OverHeadFadeAway fadeAway : fadeAwaysToAdd) {
                fadeAways.add(fadeAway);
                this.insertChild(fadeAway, 1);
                fadeAway.initialize(curTime);
            }

            fadeAwaysToAdd.clear();
        }

        synchronized (entityUpdateList) {
            for (int i = 0; i < entityUpdateList.size(); i++) {
                Entity e = entityUpdateList.get(i);

                Game.areaListener.checkKillEntity(e);
                e.updateListeners();
            }
            entityUpdateList.clear();
        }

        if (updateInterface) {
            //long startTime = System.nanoTime();

            this.updateInterface = false;

            messagesWindow.updateContent();
            characterWindow.updateContent(Game.curCampaign.party.getSelected());
            inventoryWindow.updateContent(Game.curCampaign.party.getSelected());
            merchantWindow.updateContent(Game.curCampaign.party.getSelected());
            miniMapWindow.updateContent(Game.curCampaign.curArea);
            logWindow.updateContent();
            containerWindow.updateContent();
            craftingWindow.updateContent();
            quickbarViewer.updateContent(Game.curCampaign.party.getSelected());
            portraitArea.updateContent();

            if (mouseOver.getPoint() != null) mouseOver.setPoint(mouseOver.getPoint());

            if (newQuestEntry != null) {
                logWindow.notifyNewEntry(newQuestEntry);
                newQuestEntry = null;
            }

            mainPane.update();
            ticker.updateContent();

            Game.areaListener.getTargeterManager().checkCurrentTargeter();

            //System.out.println("Interface update in " + (System.nanoTime() - startTime) / 1e9);
        }

        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * Sets the new quest entry.  This provides a flashing notification to the player
     * on the mainPane
     *
     * @param entry
     */

    public void setNewQuestEntry(QuestEntry entry)
    {
        this.newQuestEntry = entry;
    }

    /**
     * Adds the specified message to the MessageBox
     *
     * @param text
     */

    public void addMessage(String text)
    {
        messagesWindow.addMessage("black", text);

        updateInterface();
    }

    /**
     * Adds the specified message, formatted in the specified font to
     * the MessageBox
     *
     * @param font
     * @param text
     */

    public void addMessage(String font, String text)
    {
        messagesWindow.addMessage(font, text);

        updateInterface();
    }

    /**
     * Gets the complete contents of the message box as a string.  Occasionally useful
     * for debugging purposes
     *
     * @return the message box contents
     */

    public String getMessageBoxContents()
    {
        return messagesWindow.getContents();
    }

    /**
     * Returns the active set of key bindings, which provide shortcuts to some functions
     *
     * @return the active set of key bindings
     */

    public Keybindings getKeyBindings()
    {
        return keyBindings;
    }

    @Override
    protected boolean handleEvent(Event evt)
    {
        mouseX = evt.getMouseX();
        mouseY = evt.getMouseY();

        if (evt.getType() == Event.Type.KEY_PRESSED) {
            if (!scriptConsole.hasKeyboardFocus()) {
                keyBindings.fireKeyEvent(evt.getKeyCode());
                return true;
            }
        }

        return super.handleEvent(evt);
    }

    @Override
    protected void layout()
    {
        super.layout();

        mainPane.setSize(getInnerWidth(), mainPane.getPreferredHeight());
        mainPane.setPosition(getInnerX(), getInnerBottom() - mainPane.getHeight());

        int centerX = getInnerX() + getInnerWidth() / 2;

        quickbarViewer.setSize(quickbarViewer.getPreferredWidth(), quickbarViewer.getPreferredHeight());

        int maxQuickbarX = mainPane.getLeftForLayout() - quickbarViewer.getWidth() - mainPane.getBorderLeft();
        quickbarViewer.setPosition(Math.min(centerX - quickbarViewer.getWidth() / 2, maxQuickbarX),
                getInnerBottom() - quickbarViewer.getHeight());

        ticker.setPosition(getInnerX(), getInnerY());

        ticker.setSize(ticker.getPreferredWidth(), mainPane.getY() - getInnerY());

        portraitArea.setSize(portraitArea.getPreferredWidth(), getInnerHeight() - mainPane.getHeight());
        portraitArea.setPosition(getInnerRight() - portraitArea.getWidth(), getInnerY());

        fpsCounter.setPosition(ticker.getRight(), getInnerY() + fpsCounter.getPreferredHeight() / 2);

        int areaViewerOffset;

        if (ticker.isVisible()) {
            areaViewerOffset = Game.areaViewer.getX() - ticker.getRight();
            Game.areaViewer.setPosition(ticker.getRight(), getInnerY());
        } else {
            areaViewerOffset = Game.areaViewer.getX() - getInnerX();
            Game.areaViewer.setPosition(getInnerX(), getInnerY());
        }

        // scroll so the player's viewport doesn't change if the areaviewer moved
        if (areaViewerOffset != 0) {
            for (OverHeadFadeAway fade : fadeAways) {
                fade.scroll(areaViewerOffset, 0);
            }

            Game.areaViewer.scroll(-areaViewerOffset, 0);
        }

        Game.areaViewer.setSize(portraitArea.getX() - Game.areaViewer.getX(), mainPane.getY() - getInnerY());

        if (mouseOver.isVisible()) {
            mouseOver.setSize(mouseOver.getPreferredWidth(), mouseOver.getPreferredHeight());
            Point screen = AreaUtil.convertGridToScreen(mouseOver.getPoint());
            screen.x -= Game.areaViewer.getScrollX() - Game.areaViewer.getInnerX();
            screen.y -= Game.areaViewer.getScrollY() - Game.areaViewer.getInnerY();

            int x = screen.x + Game.TILE_SIZE / 2 - mouseOver.getWidth() / 2;
            int y = screen.y - mouseOver.getHeight();

            if (x < 0) {
                x = 0;
            }

            if (y < 0) {
                y = 0;
            }

            if (x + mouseOver.getWidth() > Game.config.getResolutionX()) {
                x = Game.config.getResolutionX() - mouseOver.getWidth();
            }

            if (y + mouseOver.getHeight() > Game.config.getResolutionY()) {
                y = Game.config.getResolutionY() - mouseOver.getHeight();
            }

            mouseOver.setPosition(x, y);
        }

        targeterDescription.setSize(getInnerWidth(), targeterDescription.getPreferredHeight());
        targeterDescription.setPosition(getInnerX(), getInnerY());
    }

    @Override
    protected void keyboardFocusChildChanged(Widget child)
    {
        // only change the order for GameSubWindows
        if (child != null && child instanceof GameSubWindow) {
            int fromIdx = getChildIndex(child);
            assert fromIdx >= 0;
            int numChildren = getNumChildren();
            if (fromIdx < numChildren - 1) {
                moveChild(fromIdx, numChildren - 1);
            }
        }
    }

    /**
     * Gets the total amount of video texture memory, in bytes, that has been
     * allocated by the SpriteManager
     *
     * @return the total amount of texture memory
     */

    public long getTextureMemoryUsage()
    {
        return SpriteManager.getTextureMemoryUsage();
    }
}
