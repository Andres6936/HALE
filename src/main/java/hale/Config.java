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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.theme.ThemeManager;

import main.java.hale.resource.URLResourceStreamHandler;
import main.java.hale.util.FileUtil;
import main.java.hale.util.Logger;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * The class containing global configuration variables, such as screen resolution
 * debug settings, and anything else contained in the config.txt file
 *
 * @author Jared Stephen
 */

public class Config
{
    /**
     * The config file version.  Used to know if a config file is out of date and should be replaced.  Shouldn't be used anywhere else
     **/
    public static final int Version = 1;

    private int resolutionX, resolutionY;
    private final boolean fullscreen;
    private boolean scale2x;
    private final boolean showFPS, capFPS;
    private final boolean combatAutoScroll;
    private final int toolTipDelay;
    private final long randSeed;
    private final boolean randSeedSet;
    private final boolean scriptConsoleEnabled;
    private final boolean debugMode;
    private final boolean warningMode;
    private final int combatDelay;
    private final long checkForUpdatesInterval;

    private final String versionID;

    private final Map<String, Integer> keyBindingActions;

    /**
     * Returns the amount of time the game should wait between checking for updates
     *
     * @return the amount of time the game waits between checking for updates
     */

    public long getCheckForUpdatesInterval()
    {
        return checkForUpdatesInterval;
    }

    /**
     * Returns true if a random seed has been set in the config file, false otherwise
     *
     * @return true if a random seed has been set, false otherwise
     */

    public boolean randSeedSet()
    {
        return randSeedSet;
    }

    /**
     * Returns the random seed set in the config file or 0l if it was not set
     *
     * @return the random seed set in the config file
     */

    public long getRandSeed()
    {
        return randSeed;
    }

    /**
     * Returns the base combat speed in milliseconds.  This is used to determine
     * the amount of time movement and cycling through hostiles takes
     *
     * @return the base combat speed
     */

    public int getCombatDelay()
    {
        return combatDelay;
    }

    /**
     * Returns the horizontal display resolution, independant of any scaling factor
     *
     * @return the horizontal display resolution
     */

    public int getUnscaledResolutionX()
    {
        return resolutionX;
    }

    /**
     * Returns the vertical display resolution, independant of any scaling factor
     *
     * @return the vertical display resolution
     */

    public int getUnscaledResolutionY()
    {
        return resolutionY;
    }

    /**
     * Returns the horizontal display resolution set in the config file, divided by the scaling factor
     *
     * @return the horizontal display resolution set in the config file, taking into account scaling factor
     */

    public int getResolutionX()
    {
        return resolutionX / getScaleFactor();
    }

    /**
     * Returns the vertical display resolution set in the config file, divided by the scaling factor
     *
     * @return the vertical display resolution, taking into account the scaling factor
     */

    public int getResolutionY()
    {
        return resolutionY / getScaleFactor();
    }

    /**
     * Returns true if the view should automatically scroll to show combat actions such as movement
     * and attacks of opportunity, false otherwise
     *
     * @return whether to auto scroll in combat
     */

    public boolean autoScrollDuringCombat()
    {
        return combatAutoScroll;
    }

    /**
     * Returns true if the Frames per second (FPS) should be shown in the mainViewer, false otherwise
     *
     * @return whether the mainViewer should show FPS
     */

    public boolean showFPS()
    {
        return showFPS;
    }

    /**
     * Returns true if the game should be run in fullscreen mode, false otherwise
     *
     * @return whether the game should be run in fullscreen mode
     */

    public boolean getFullscreen()
    {
        return fullscreen;
    }

    /**
     * Returns true if the refresh rate is capped at 60 hz, false if it is not capped
     *
     * @return whether the referesh rate is capped
     */

    public boolean capFPS()
    {
        return capFPS;
    }

    /**
     * Returns true if the user can access the script console with the ~ key, false otherwise
     *
     * @return whether the script console is accessible
     */

    public boolean isScriptConsoleEnabled()
    {
        return scriptConsoleEnabled;
    }

    /**
     * Returns true if debug mode is enabled and error messages will be printed to the
     * standard output in addition to being logged to log/error.txt
     *
     * @return whether debug mode is enabled
     */

    public boolean isDebugModeEnabled()
    {
        return debugMode;
    }

    /**
     * Returns true if warning mode is enabled and warning message will be printed to the
     * standard output in addition to being logged to log/warning.txt
     *
     * @return whether warning mode is enabled
     */

    public boolean isWarningModeEnabled()
    {
        return warningMode;
    }

    /**
     * Returns the global tooltip delay set in the config file
     *
     * @return the global tooltip delay
     */

    public int getTooltipDelay()
    {
        return toolTipDelay;
    }

    /**
     * Returns the unique version ID for the current binary version of hale being run
     *
     * @return the unique version ID for the current binary version being run
     */

    public String getVersionID()
    {
        return versionID;
    }

    /**
     * Returns whether the entire display should be scaled by a factor of two
     *
     * @return whether 2x scaling is on
     */

    public boolean scale2x()
    {
        return scale2x;
    }

    /**
     * Returns the display scale factor.  1 if not scaling, 2 if scale2x is enabled
     *
     * @return the display scale factor
     */

    public int getScaleFactor()
    {
        return scale2x ? 2 : 1;
    }

    /**
     * Returns the integer keyboard code associated with the given action
     *
     * @param actionName Name of action
     * @return the integer key code, or -1 if no key is associated with the action
     */

    public int getKeyForAction(String actionName)
    {
        Integer key = keyBindingActions.get(actionName);

        return Objects.requireNonNullElse(key, -1);
    }

    /**
     * Returns the list of all keyboard action names in this config.  The list is
     * not sorted
     *
     * @return the list of keyboard action names
     */

    public List<String> getKeyActionNames()
    {
        return new ArrayList<>(keyBindingActions.keySet());
    }

    /**
     * Creates a new Config from the specified file
     *
     * @param fileName the name of the file to read the config from
     */

    public Config(final String fileName)
    {
        versionID = FileUtil.getHalfMD5Sum(new File("hale.jar"));

        final File configFile = new File(fileName);

        // create the config file if it does not already exist or is old
        if (!checkConfigFile(configFile)) {
            createConfigFile(fileName);
        }

        SimpleJSONParser parser = new SimpleJSONParser(configFile);

        SimpleJSONArray resArray = parser.getArray("Resolution");
        Iterator<SimpleJSONArrayEntry> iter = resArray.iterator();

        resolutionX = iter.next().getInt(800);
        resolutionY = iter.next().getInt(600);

        fullscreen = parser.get("Fullscreen", false);
        scale2x = parser.get("Scale2X", false);

        SimpleJSONArray edResArray = parser.getArray("EditorResolution");
        iter = edResArray.iterator();

        showFPS = parser.get("ShowFPS", false);
        combatAutoScroll = parser.get("CombatAutoScroll", true);
        capFPS = parser.get("CapFPS", false);
        toolTipDelay = parser.get("TooltipDelay", 400);
        combatDelay = parser.get("CombatDelay", 150);
        scriptConsoleEnabled = parser.get("ScriptConsoleEnabled", false);
        debugMode = parser.get("DebugMode", false);
        warningMode = parser.get("WarningMode", false);
        checkForUpdatesInterval = parser.get("CheckForUpdatesInterval", 86400000);

        if (parser.containsKey("RandomSeed")) {
            randSeedSet = true;
            randSeed = parser.get("RandomSeed", 0);
        } else {
            randSeedSet = false;
            randSeed = 0L;
        }

        keyBindingActions = new HashMap<>();

        SimpleJSONObject bindingsObject = parser.getObject("Keybindings");
        for (String bindingName : bindingsObject.keySet()) {
            String keyboardKey = bindingsObject.get(bindingName, null);

            if (keyboardKey.length() > 0) {
                keyBindingActions.put(bindingName, Event.getKeyCodeForName(keyboardKey));
            } else {
                keyBindingActions.put(bindingName, -1);
            }

        }

        // prevent an unused key warning
        parser.get("ConfigVersion", 0);

        parser.warnOnUnusedKeys();
    }

    /**
     * Checks whether the current config file is ok to be used.  If it is not ok, then
     * the default config should be copied over
     *
     * @param configFile File of configuration
     * @return true if the file is ok, false if the default file should be copied over (either the
     * config doesn't exist or it is out of date)
     */

    private boolean checkConfigFile(final File configFile)
    {
        if (!configFile.isFile()) return false;

        SimpleJSONParser parser = new SimpleJSONParser(configFile);

        int fileVersion = parser.get("ConfigVersion", 0);

        if (fileVersion != Config.Version) {
            Logger.appendToWarningLog("Removing existing config file as version " + fileVersion + " does not match " + Config.Version);
            configFile.delete();
            return false;
        }

        return true;
    }

    private void createConfigFile(String fileName)
    {
        try {
            FileUtil.copyFile(new File("docs/defaultConfig.json"), new File(fileName));
        } catch (IOException e) {
            Logger.appendToErrorLog("Error creating configuration file.", e);
        }
    }

    /**
     * Writes the specified time to disk as the last check for updates time
     *
     * @param time the time in milliseconds since midnight, January 1, 1970 UTC
     */

    public static void writeCheckForUpdatesTime(long time)
    {
        try {
            FileUtil.writeStringToFile(new File(Game.plataform.getConfigDirectory() + "lastUpdateTime.txt"), Long.toString(time));
        } catch (IOException e) {
            Logger.appendToErrorLog("Error writing last update time to file", e);
        }
    }

    /**
     * Gets the last time that updates were checked for, or 0 if
     * updates have never been checked for
     *
     * @return the last update check time (in milliseconds since midnight, January 1, 1970 UTC)
     */

    public static long getLastCheckForUpdatesTime()
    {
        File file = new File(Game.plataform.getConfigDirectory() + "lastUpdateTime.txt");
        if (file.canRead()) {
            String time = null;
            try {
                time = FileUtil.readFileAsString(Game.plataform.getConfigDirectory() + "lastUpdateTime.txt");
            } catch (IOException e) {
                Logger.appendToErrorLog("Error reading last update time", e);
            }

            if (time != null) {
                try {
                    return Long.parseLong(time);
                } catch (Exception e) {
                    Logger.appendToErrorLog("Error parsing last update time", e);
                }
            }

        }

        return 0L;
    }

    /**
     * Returns a List of all DisplayModes that have Bits Per Pixel matching
     * the user's Desktop BPP, are fullscreen capable, and have a high enough
     * resolution (800x600 or greater)
     * <p>
     * The returned List will be sorted by Display Resolution.
     *
     * @param scale2x whether we are scaling by 2x.  this limits resolutions based on the 2x scale factor
     * @return a List of all usable DisplayModes
     * @throws LWJGLException
     */

    public static List<DisplayMode> getUsableDisplayModes(boolean scale2x) throws LWJGLException
    {
        int scaleFactor = scale2x ? 2 : 1;

        DisplayMode desktop = Display.getDesktopDisplayMode();

        DisplayMode[] allModes = Display.getAvailableDisplayModes();

        List<DisplayMode> goodModes = new ArrayList<DisplayMode>();

        // check each mode in the list of all modes to see if we can use it
        for (DisplayMode mode : allModes) {
            if (mode.getBitsPerPixel() != desktop.getBitsPerPixel()) continue;

            if (!mode.isFullscreenCapable()) continue;

            if (mode.getWidth() < 800 * scaleFactor || mode.getHeight() < 600 * scaleFactor) continue;

            // we need to verify that a mode with the same width and height has not
            // already been added to the list, so we don't add what will look like the
            // same mode to the user twice
            boolean modeDoesNotMatchExisting = true;

            for (DisplayMode existingMode : goodModes) {
                if (existingMode.getWidth() == mode.getWidth() && existingMode.getHeight() == mode.getHeight()) {
                    modeDoesNotMatchExisting = false;
                    break;
                }
            }

            if (modeDoesNotMatchExisting) {
                goodModes.add(mode);
            }
        }

        // sort the list of usable modes by Display Resolution
        goodModes.sort((m1, m2) -> {
            if (m1.getWidth() > m2.getWidth()) {
                return 1;
            } else
                if (m1.getWidth() < m2.getWidth()) {
                    return -1;
                } else {
                    if (m1.getHeight() > m2.getHeight()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
        });

        return goodModes;
    }

    /**
     * Searches the global list of DisplayModes at {@link Game#allDisplayModes} for a
     * DisplayMode with the specified horizontal and vertical resolutions.
     *
     * @param resX the horizontal resolution of the DisplayMode to be found
     * @param resY the vertical resolution of the DisplayMode to be found
     * @return the index of the matching display mode in Game.allDisplayModes, or -1 if no
     * such DisplayMode was found.
     */

    public static int getMatchingDisplayMode(boolean scale2x, int resX, int resY)
    {
        int i = 0;
        for (DisplayMode mode : (scale2x ? Game.all2xUsableDisplayModes : Game.allDisplayModes)) {
            if (mode.getWidth() == resX && mode.getHeight() == resY) return i;

            i++;
        }

        return -1;
    }

    /**
     * Creates a new LWJGL display with the configured x and y game resolution.  If the
     * specified x and y resolution is invalid, falls back to an 800x600 display mode.
     * <p>
     * This method then sets up the OpenGL context for 2D drawing, and sets up TWL using
     * the theme file at gui/simple.xml.
     */

    public static void createGameDisplay()
    {
        createDisplay(Game.config.scale2x(), Game.config.getResolutionX() * Game.config.getScaleFactor(),
                Game.config.getResolutionY() * Game.config.getScaleFactor());
    }

    /**
     * Creates a new LWJGL display with the specified x and y resolutions.  If no such
     * display is found in the list of usable DisplayModes at {@link Game#allDisplayModes},
     * attempts to fallback to an 800x600 display mode.
     * <p>
     * Once the DisplayMode has been set, the OpenGL context is set up for 2D drawing
     * and the TWL Theme file at gui/simple.xml is loaded.
     *
     * @param scale2x whether the display is scaled by 2x
     * @param resX    the horizontal resolution of the Display to create
     * @param resY    the vertical resolution of the Display to create
     */

    private static void createDisplay(boolean scale2x, int resX, int resY)
    {
        DisplayMode mode;

        try {
            int index = getMatchingDisplayMode(scale2x, resX, resY);
            if (index == -1) {
                Logger.appendToErrorLog("No display mode available with configuration: " + resX + "x" + resY + ".  Falling back to 800x600 with no scaling.");

                scale2x = false;
                index = getMatchingDisplayMode(scale2x, 800, 600);
                Game.config.resolutionX = 800;
                Game.config.resolutionY = 600;
                Game.config.scale2x = false;

                if (index == -1) {
                    Logger.appendToErrorLog("Unable to find display mode for fallback 800x600 display.  Exiting.");
                    System.exit(1);
                }
            }

            if (scale2x) {
                mode = Game.all2xUsableDisplayModes.get(index);
            } else {
                mode = Game.allDisplayModes.get(index);
            }

            Display.setDisplayMode(mode);
            Display.setFullscreen(Game.config.getFullscreen());
            Display.create();

            Game.renderer = new HaleLWJGLRenderer();

        } catch (LWJGLException e) {
            Logger.appendToErrorLog("Error creating display.", e);
            System.exit(0);
        }

        URL theme = null;
        try {
            theme = new URL("resource", "localhost", -1, "gui/theme.xml", new URLResourceStreamHandler());
        } catch (MalformedURLException e) {
            Logger.appendToErrorLog("Error creating URL for theme file", e);
        }

        try {
            assert theme != null;
            Game.themeManager = ThemeManager.createThemeManager(theme, Game.renderer);
        } catch (IOException e) {
            Logger.appendToErrorLog("Error creating theme manager", e);
        }

    }
}
