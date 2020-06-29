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

package main.java.hale.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import main.java.hale.Game;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.SaveWriter;
import main.java.hale.resource.ResourceType;

/**
 * A static class providing methods for working with save game files
 *
 * @author Jared Stephen
 */

public class SaveGameUtil
{

    /**
     * Returns a save-able reference string for the specified object
     *
     * @param object the object to get the reference for
     * @return the reference string
     */

    public static String getRef(Object object)
    {
        return object.getClass().getName() + '@' + Integer.toHexString(object.hashCode());
    }

    /**
     * Loads the specified Object using the JSONObject data.  The data must contain a "class"
     * entry with the class name of an Object with a static "load" method
     *
     * @param data the JSON data to load
     * @return the loaded object
     * @throws LoadGameException
     */

    public static Object loadObject(SimpleJSONObject data) throws LoadGameException
    {
        try {
            Class<?> clazz = Class.forName(data.get("class", null));

            Method method = clazz.getMethod("load", SimpleJSONObject.class);

            // invoke the static load method on the specified class to create the animated object
            // all instances of animated must have a load method for tis to work
            return method.invoke(null, data);

        } catch (ClassNotFoundException e) {
            throw new LoadGameException("Error loading object, class " + data.get("class", null) + " not found.");
        } catch (SecurityException e) {
            throw new LoadGameException("Error loading object, method load is not accessible.");
        } catch (NoSuchMethodException e) {
            throw new LoadGameException("Error loading object for class " +
                    data.get("class", null) + ", method load not found.");
        } catch (IllegalArgumentException e) {
            throw new LoadGameException("Error loading object, invalid load arguments");
        } catch (IllegalAccessException e) {
            throw new LoadGameException("Error loading object, method load is not accessible.");
        } catch (InvocationTargetException e) {
            Logger.appendToErrorLog("Error loading object.", e.getCause());

            throw new LoadGameException("Error loading object, method load constructor error");
        }
    }

    private static void sortFilesByTimeModified(File[] files)
    {
        Arrays.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File f1, File f2)
            {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });
    }

    /**
     * Returns a list of Strings representing each saved game in the "saves"
     * folder.  The file corresponding to each string can be obtained with
     * {@link #getSaveFile(String)}.  If the saves folder does not exist
     * or is empty, an empty list is returned
     *
     * @return the list of Strings representing each saved game in the "saves" folder
     */

    public static List<String> getSaveGames()
    {
        List<String> saves = new ArrayList<String>();

        File dir = new File(Game.plataform.getSaveDirectory());

        File[] files = dir.listFiles();
        if (files == null) return saves;

        sortFilesByTimeModified(files);

        for (File f : files) {
            String name = f.getName();

            if (name.startsWith(Game.curCampaign.getID() + "-") && name.endsWith(ResourceType.SaveGame.getExtension())) {
                String subName = name.substring(Game.curCampaign.getID().length() + 1,
                        name.length() - ResourceType.SaveGame.getLength());

                saves.add(subName);
            }
        }

        return saves;
    }

    /**
     * Returns true if and only if the specified name represents a quicksave file
     *
     * @param saveGame the save game string to check
     * @return whether the string represents a quicksave file
     */

    public static boolean isQuickSave(String saveGame)
    {
        return saveGame.startsWith("quicksave");
    }

    /**
     * Returns the File for the next quicksave that should be written.  This is determined
     * by finding the most recently written quicksave and incrementing the quicksave
     * index by 1, wrapping around to 1 if the index becomes greater than 9.
     *
     * @return the File for the next quicksave that should be written
     */

    public static File getNextQuickSaveFile()
    {
        File[] files = new File(Game.plataform.getSaveDirectory()).listFiles();

        if (files == null) return getSaveFile("quicksave1");

        sortFilesByTimeModified(files);

        for (File f : files) {
            String name = f.getName();

            if (!name.startsWith(Game.curCampaign.getID())) continue;

            String subName = name.substring(Game.curCampaign.getID().length() + 1,
                    name.length() - ResourceType.SaveGame.getLength());

            if (!subName.startsWith("quicksave")) continue;

            int index = 0;
            try {
                index = Integer.parseInt(Character.toString(subName.charAt(subName.length() - 1)));
            } catch (Exception e) {
                Logger.appendToErrorLog("Error finding most recently created quicksave.  Reverting to index 1.", e);
            }

            index++;
            if (index > 9) index = 1;

            return getSaveFile("quicksave" + index);
        }

        return getSaveFile("quicksave1");
    }

    /**
     * Returns the save game file corresponding to the save with the specified name.
     * The file can then be saved or loaded with {@link #saveGame(File)} or by using the
     * LoadGameLoadingTaskList
     *
     * @param name the name of the save game to get
     * @return the File for the specified save game
     */

    public static File getSaveFile(String name)
    {
        File dir = new File(Game.plataform.getSaveDirectory());
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }

        return new File(Game.plataform.getSaveDirectory() + Game.curCampaign.getID() + "-" +
                name + ResourceType.SaveGame.getExtension());
    }

    /**
     * The current state of the game (as contained in the Game.curCampaign object)
     * is saved to the specified file in a compressed format.  A header containing
     * some basic information is also saved to the file.
     *
     * @param file the file to save to
     * @throws IOException any exception thrown by the OutputStream used to write the file
     */

    public static void saveGame(File file) throws IOException
    {
        JSONOrderedObject data = Game.curCampaign.getSaveGameData();

        FileOutputStream fos = new FileOutputStream(file);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        SaveFileHeader header = new SaveFileHeader(Game.curCampaign);
        SaveFileHeader.write(header, gz);

        PrintWriter writer = new PrintWriter(gz);
        SaveWriter.writeJSON(data, writer);

        writer.close();
    }
}
