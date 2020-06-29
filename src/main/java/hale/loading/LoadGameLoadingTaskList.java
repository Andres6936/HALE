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

package main.java.hale.loading;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import main.java.hale.Game;
import main.java.hale.entity.EntityManager;
import main.java.hale.util.Logger;
import main.java.hale.util.SaveFileHeader;
import main.java.hale.util.SimpleJSONParser;

/**
 * A LoadingTaskList that performs the actions neccesary to load a saved game
 * in an already loaded Campaign
 *
 * @author Jared Stephen
 */

public class LoadGameLoadingTaskList extends LoadingTaskList
{
    private SimpleJSONParser parser;

    private File fileToLoad;
    private GZIPInputStream gz;

    /**
     * Creates a NewGameLoadingTaskList populated by the tasks needed to load the game
     * Must be started in the usual way, using {@link #start()}
     *
     * @param file the file to load
     */

    public LoadGameLoadingTaskList(File file)
    {
        super();
        this.fileToLoad = file;

        try {
            // free tileset if one is already loaded
            if (Game.curCampaign != null && Game.curCampaign.curArea != null) {
                Game.curCampaign.getTileset(Game.curCampaign.curArea.getTileset()).freeTiles();
            }

            FileInputStream fin = new FileInputStream(fileToLoad);
            gz = new GZIPInputStream(fin);

        } catch (Exception e) {
            Logger.appendToErrorLog("Error loading saved game: " + fileToLoad.getPath(), e);
        }

        Runnable loadHeader = new Runnable()
        {
            @Override
            public void run()
            {
                // read in and discard the header data
                try {
                    SaveFileHeader.read(gz);
                } catch (Exception e) {
                    Logger.appendToErrorLog("Error loading saved game: " + fileToLoad.getPath(), e);
                }
            }
        };
        addTask(loadHeader, "Loading Header Data", 1);

        Runnable parseData = new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    Reader reader = new InputStreamReader(gz);
                    parser = new SimpleJSONParser(reader, fileToLoad.getName());
                    parser.setWarnOnMissingKeys(false);
                } catch (Exception e) {
                    Logger.appendToErrorLog("Error parsing saved game: " + fileToLoad.getPath(), e);
                }
            }
        };
        addTask(parseData, "Parsing Save Game File", 1);

        Runnable loadGame = new Runnable()
        {
            @Override
            public void run()
            {
                // read the game file
                try {
                    EntityManager.clear();
                    Game.curCampaign.load(parser);

                } catch (Exception e) {
                    Logger.appendToErrorLog("Error loading saved game: " + fileToLoad.getPath(), e);
                }
            }
        };
        addTask(loadGame, "Loading Game Content", 1);

        Runnable setArea = new Runnable()
        {
            @Override
            public void run()
            {
                if (Game.isInTurnMode()) Game.areaListener.getCombatRunner().exitCombat();
                Game.areaViewer.setArea(Game.curCampaign.curArea);
                Game.areaListener.setArea(Game.curCampaign.curArea);
            }
        };
        addTask(setArea, "Loading Tileset", 5);

        Runnable finishing = new Runnable()
        {
            @Override
            public void run()
            {
                Game.curCampaign.curArea.setEntityVisibility();
                Game.curCampaign.curArea.getUtil().updateVisibility();

                Game.mainViewer.getPortraitArea().updateParty();
                Game.selectedEntity = Game.curCampaign.party.getSelected();
                Game.interfaceLocker.clear();

                // free up memory
                System.gc();
            }
        };
        addTask(finishing, "Setting up interface", 2);
    }
}
