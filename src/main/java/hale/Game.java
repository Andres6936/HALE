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

package main.java.hale;

import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import main.java.hale.defaultability.MouseActionList;
import main.java.hale.entity.Entity;
import main.java.hale.entity.EntityManager;
import main.java.hale.entity.EquippableItemTemplate;
import main.java.hale.interfacelock.InterfaceLocker;
import main.java.hale.loading.AsyncTextureLoader;
import main.java.hale.loading.LoadGameLoadingTaskList;
import main.java.hale.loading.LoadingTaskList;
import main.java.hale.loading.LoadingWaitPopup;
import main.java.hale.mainmenu.ErrorPopup;
import main.java.hale.mainmenu.MainMenu;
import main.java.hale.mainmenu.MainMenuAction;
import main.java.hale.particle.ParticleManager;
import main.java.hale.plataform.LinuxOS;
import main.java.hale.plataform.MacOS;
import main.java.hale.plataform.Plataform;
import main.java.hale.plataform.WindowsOS;
import main.java.hale.resource.ResourceManager;
import main.java.hale.resource.SpriteManager;
import main.java.hale.rules.Campaign;
import main.java.hale.rules.Dice;
import main.java.hale.rules.Ruleset;
import main.java.hale.util.JSEngineManager;
import main.java.hale.util.Logger;
import main.java.hale.util.SaveGameUtil;
import main.java.hale.view.AreaViewer;
import main.java.hale.view.MainViewer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The class containing the main method and an assortment of global variables
 *
 * @author Jared
 */

public class Game
{
    /**
     * The height and width of a Tile in pixels
     */

    public static final int TILE_SIZE = 72;

    /**
     * The width of a Tile for the purposes of creating a Tile grid
     */

    public static final int TILE_WIDTH = 54;

    /**
     * The ratio between the size of an equilateral hexagon and the game's slightly fat hexagons
     */

    public static final double TILE_RATIO = Math.sqrt(3.0) * TILE_WIDTH / TILE_SIZE;

    /**
     * The size of a standard spell or item icon
     */

    public static final int ICON_SIZE = 45;

    private static NumberFormat numberFormat;

    /**
     * The global config, containing video mode etc
     */

    public static Config config;

    /**
     * The current campaign.  This is the object that is serialized when the game is saved.
     * Contains all areas, creatures and items
     */

    public static Campaign curCampaign;

    /**
     * The globally selected entity.  This is the entity that is highlighted as active, and that the area
     * will center on
     */

    public static Entity selectedEntity;

    /**
     * Contains roles, races, abilities, etc
     */

    public static Ruleset ruleset;

    /**
     * keeps track of all particle effects and animations
     */

    public static ParticleManager particleManager;

    /**
     * Contains many of the methods that are generally
     * used by script files.  Many of these are wrappers around methods existing
     * elsewhere in the code
     */

    public static ScriptInterface scriptInterface;

    /**
     * keeps track of all current Script Engines.  Scripts are
     * executed through Java 6's built in Scripting support for JS through RhinoScriptEngine.
     * The JSEngineManager ensures that each script engine is only being used in one thread at
     * a time, and creates new engines as needed.
     */

    public static JSEngineManager scriptEngineManager;

    /**
     * The main view, the root widget.  Also contains the in game main loop.
     */

    public static MainViewer mainViewer;

    /**
     * contains the top level event handling for the area.
     */

    public static AreaListener areaListener;

    /**
     * The viewer containing the rendering code for the current area.
     */

    public static AreaViewer areaViewer;

    /**
     * Keeps track of rounds and elapses them in real time based on
     * the current campaign configuration while not in combat
     */

    public static GameTimer timer;

    /**
     * Handles random number generation.  Contains helper functions for common cases.
     */

    public static Dice dice;

    /**
     * All valid screen resolutions at the current Bits per Pixel.
     */

    public static List<DisplayMode> allDisplayModes;

    public static List<DisplayMode> all2xUsableDisplayModes;

    /**
     * Returns whether there is at least one display mode that is usable with 2x scaling enabled,
     * i.e. at least 1600 width, 1200 height
     *
     * @return whether there is at least one display mode which is usuable with 2x scaling
     */

    public static boolean hasUsable2xDisplayMode()
    {
        return !all2xUsableDisplayModes.isEmpty();
    }

    /**
     * The primary low level renderer for TWL.
     */

    public static LWJGLRenderer renderer;

    /**
     * The theme manager for TWL.
     */

    public static ThemeManager themeManager;

    /**
     * Handles disabling player interface input when appropriate, such as during animations,
     * at the start and end of combat, etc
     */

    public static InterfaceLocker interfaceLocker;

    /**
     * Contains information on what mouse cursor to show given a set of conditions
     */

    public static MouseActionList mouseActions;

    /**
     * Used to load textures in a thread safe manner
     */

    public static AsyncTextureLoader textureLoader;

    /**
     * System Operative when run the application
     */
    public static Plataform plataform;

    private static boolean turnMode = false;

    /**
     * Returns whether the game is currently in turn based or combat mode.  Note that this will
     * return true if the game is in force turn mode, even if there are no hostiles
     * active.
     *
     * @return whether or not the game is currently in turn based mode
     */

    public static boolean isInTurnMode()
    {
        return turnMode;
    }

    /**
     * Switches turn based combat mode on or off.  Combat mode off means the game will run in real
     * time, combat mode on means it will run turn based
     *
     * @param mode true to turn on combat mode, false to turn it off
     */

    public static void setTurnMode(boolean mode)
    {
        turnMode = mode;
    }

    /**
     * Returns a number format set up to show the specified number
     * of digits after the decimal point
     *
     * @param digits the number of digits to show after the decimal point
     * @return a NumberFormat that will show the specified number of digits
     */

    public static NumberFormat numberFormat(int digits)
    {
        numberFormat.setMinimumFractionDigits(digits);
        numberFormat.setMaximumFractionDigits(digits);
        return numberFormat;
    }

    /**
     * The OS type of the user currently running this program
     *
     * @author Jared Stephen
     */

    public enum OSType
    {
        Unix,
        Windows,
        Mac,
        Win7;
    }

    /**
     * The current running operating system
     */

    public static OSType osType;

    /**
     * The global main method.  Handles initializing the global variables,
     * determining available display modes, creating the display, and parsing any arguments
     * <p>
     * Then, starts the main menu loop.  Once the player makes a selection, either the game
     * proper or the editor is loaded.
     *
     * @param args any arguments passed to the program are ignored
     */
    public static void main(String[] args)
    {
        // Determine System Operative
        String systemOperative = System.getProperty("os.name").toLowerCase();

        if (systemOperative.contains("win")) {
            plataform = new WindowsOS();
        } else
            if (systemOperative.contains("mac")) {
                plataform = new MacOS();
            } else
                if (systemOperative.contains("linux")) {
                    plataform = new LinuxOS();
                }

        plataform.createDiretoriesIfNotExist();

        // initialize inventory slots - equippable item types
        EquippableItemTemplate.initializeTypesMap();

        // determines the resource IDs of all available core resources, but does not read them in yet
        ResourceManager.registerCorePackage();

        Game.numberFormat = NumberFormat.getInstance();
        Game.config = new Config(Game.plataform.getConfigDirectory() + "config.json");
        Game.dice = new Dice();

        Game.scriptEngineManager = new JSEngineManager();
        Game.scriptInterface = new ScriptInterface();

        Game.particleManager = new ParticleManager();

        Game.timer = new GameTimer();
        Game.interfaceLocker = new InterfaceLocker();
        Game.scriptInterface.ai = new AIScriptInterface();

        Display.setTitle("Hale");
        setDisplayIcon();

        textureLoader = new AsyncTextureLoader();

        try {
            allDisplayModes = Config.getUsableDisplayModes(false);
            all2xUsableDisplayModes = Config.getUsableDisplayModes(true);
        } catch (Exception e) {
            Logger.appendToErrorLog("Error registering display modes.", e);
        }

        Config.createGameDisplay();

        try {
            // run the game in a loop until the specified action is Exit
            MainMenuAction action = new MainMenuAction(MainMenuAction.Action.ShowMainMenu);
            while (action.getAction() != MainMenuAction.Action.Exit) {
                // perform the specified action and get the new action caused
                // by performing it
                action = performAction(action);
            }

        } catch (Exception e) {
            Logger.appendToErrorLog("Uncaught exception in game.", e);
        }

        Display.destroy();
    }

    /**
     * Returns a List of all currently active, non daemon threads in the main thread group, other
     * than the main thread
     *
     * @return the list of all threads
     */

    public static List<Thread> getActiveThreads()
    {
        Thread current = Thread.currentThread();

        // find the current thread group
        ThreadGroup group = current.getThreadGroup();

        Thread[] threads = new Thread[group.activeCount() * 2];

        int numThreads = group.enumerate(threads);

        List<Thread> threadsList = new ArrayList<Thread>();

        for (int i = 0; i < numThreads; i++) {
            Thread thread = threads[i];

            if (thread.isDaemon()) continue;

            if (thread == current) continue;

            threadsList.add(thread);
        }

        return threadsList;
    }

    private static ByteBuffer loadIcon(InputStream is)
    {
        BufferedImage image;
        try {
            image = ImageIO.read(is);
        } catch (IOException e) {
            Logger.appendToErrorLog("Error loading display icon", e);
            return null;
        }

        byte[] imageBytes = new byte[image.getWidth() * image.getHeight() * 4];
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int pixel = image.getRGB(j, i);
                for (int k = 0; k < 3; k++) // red, green, blue
                {
                    imageBytes[(i * image.getWidth() + j) * 4 + k] = (byte)(((pixel >> (2 - k) * 8)) & 255);
                }
                imageBytes[(i * image.getWidth() + j) * 4 + 3] = (byte)(((pixel >> (3) * 8)) & 255); // alpha
            }
        }

        return ByteBuffer.wrap(imageBytes);
    }

    private static void setDisplayIcon()
    {
        InputStream is128 = ResourceManager.getStream("gui/hale128.png");
        InputStream is64 = ResourceManager.getStream("gui/hale64.png");
        InputStream is32 = ResourceManager.getStream("gui/hale32.png");
        InputStream is16 = ResourceManager.getStream("gui/hale16.png");

        ByteBuffer[] list = new ByteBuffer[4];

        list[0] = loadIcon(is128);
        list[1] = loadIcon(is64);
        list[2] = loadIcon(is32);
        list[3] = loadIcon(is16);

        Display.setIcon(list);
    }


    private static void destroyDisplay()
    {
        SpriteManager.clear();
        Game.themeManager.destroy();
        Display.destroy();
    }

    /*
     * Returns the specified action if it is MainMenuAction.Action.Exit, otherwise
     * performs the action and returns the action result
     */

    private static MainMenuAction performAction(MainMenuAction action)
    {
        action.runPreActionCallback();

        switch (action.getAction()) {
            case Exit:
                return action;
            case Restart:
                destroyDisplay();
                Config.createGameDisplay();
                break;
            case ShowMainMenu:
                MainMenu mainMenu = new MainMenu();

                // show an error popup if one should be shown
                if (action.getErrorPopupMessages().size() > 0) {
                    ErrorPopup popup = new ErrorPopup(mainMenu, action.getErrorPopupMessages());
                    popup.openPopupCentered();
                }

                action = mainMenu.mainLoop();

                // the players loaded open campaign is stored in campaigns/lastOpenCampaign.txt
                // this is automatically opened when you start the game
                if (Game.curCampaign != null) {
                    MainMenu.writeLastOpenCampaign(Game.curCampaign.getID());
                }

                return action;
            case NewGame:
                EntityManager.clear();
                Game.curCampaign.curArea.addPlayerCharacters();
                Game.mainViewer = new MainViewer();
                Game.curCampaign.curArea.setEntityVisibility();

                // exit if the mainviewer says to
                return Game.mainViewer.runCampaign(true);
            case LoadGame:
                LoadingTaskList loader = null;

                try {
                    Game.mainViewer = new MainViewer();
                    loader = new LoadGameLoadingTaskList(SaveGameUtil.getSaveFile(action.getLoadGameFile()));
                    loader.start();

                    LoadingWaitPopup popup = new LoadingWaitPopup(Game.mainViewer, "Loading Saved Game");
                    popup.setLoadingTaskList(loader);
                    popup.setBGSprite(SpriteManager.getSpriteAnyExtension("loadingscreen"));
                    popup.openPopupCentered();
                    Game.mainViewer.runLoadingLoop(loader, popup);

                } catch (Exception e) {
                    Logger.appendToErrorLog("Error loading saved game: " + action.getLoadGameFile(), e);
                }

                if (loader != null && loader.isCompletedSuccessfully()) {
                    Game.mainViewer.addMessage("link", "Save file loaded successfully.");
                    return Game.mainViewer.runCampaign(false);
                } else {
                    // there was an error in loading
                    MainMenuAction mma = new MainMenuAction(MainMenuAction.Action.ShowMainMenu);
                    mma.addErrorPopupMessage("Unable to load the save file \"" + action.getLoadGameFile() + "\".");
                    mma.addErrorPopupMessage("");
                    mma.addErrorPopupMessage("Try exiting and restarting the game, then loading");
                    mma.addErrorPopupMessage("the file again.  If the problem persists, then the");
                    mma.addErrorPopupMessage("save file is corrupt or from an incompatible");
                    mma.addErrorPopupMessage("version and cannot be loaded.");

                    return mma;
                }
        }

        // reshow the main menu if no other action was specified above
        return new MainMenuAction(MainMenuAction.Action.ShowMainMenu);
    }

    /**
     * Sun property pointing the main class and its arguments.
     * Might not be defined on non Hotspot VM implementations.
     */
    public static final String MAIN_COMMAND = "main.java.hale.Game";

    /**
     * Returns the String command which can be used to invoke this program in exactly the same way as it has
     * currently been invoked
     *
     * @return the String command to invoke this program
     */

    public static String getProgramCommand() throws IOException
    {
        // java binary
        String java = System.getProperty("java.home") + "/bin/java";
        // vm arguments
        List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        StringBuffer vmArgsOneLine = new StringBuffer();
        for (String arg : vmArguments) {
            // if it's the agent argument : we ignore it otherwise the
            // address of the old application and the new one will be in conflict
            if (!arg.contains("-agentlib")) {
                vmArgsOneLine.append(arg);
                vmArgsOneLine.append(" ");
            }
        }
        // init the command to execute, add the vm args
        final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);

        // program main and program arguments
        String[] mainCommand = MAIN_COMMAND.split(" ");

        // program main is a jar
        if (mainCommand[0].endsWith(".jar")) {
            // if it's a jar, add -jar mainJar
            cmd.append("-jar " + new File(mainCommand[0]).getPath());
        } else {
            // else it's a .class, add the classpath and mainClass
            cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
        }

        // finally add program arguments
        for (int i = 1; i < mainCommand.length; i++) {
            cmd.append(" ");
            cmd.append(mainCommand[i]);
        }

        return cmd.toString();
    }
}
