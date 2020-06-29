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

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import main.java.hale.Game;
import main.java.hale.entity.EntityManager;
import main.java.hale.resource.ResourceManager;
import main.java.hale.resource.SpriteManager;
import main.java.hale.rules.Campaign;

/**
 * A class for loading a campaign asynchronously
 *
 * @author Jared
 */

public class CampaignLoader extends SwingWorker<Void, Void>
{
    private ProgressMonitor monitor;

    private String campaignID;

    private EditorMenuBar parent;

    /**
     * Creates a new CampaignLoader
     *
     * @param parent     the menu bar that created this loader
     * @param campaignID the ID of the campaign to load
     */

    public CampaignLoader(EditorMenuBar parent, String campaignID)
    {
        this.campaignID = campaignID;
        this.parent = parent;

        monitor = new ProgressMonitor(parent, "Loading Campaign " + campaignID,
                "Loading Sprite Sheets", 0, 9);
        monitor.setMillisToDecideToPopup(0);
        monitor.setMillisToPopup(0);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        monitor.setProgress(1);

        // parse spritesheets to determine sprite names and dimensions,
        // but don't actually load them into texture memory
        SpriteManager.loadSpriteSheets();
        Game.textureLoader.clear();

        if (checkCanceled()) return null;
        monitor.setNote("Loading Portraits");
        monitor.setProgress(3);

        SpriteManager.loadAllPortraits();

        EntityManager.clear();
        Game.curCampaign = new Campaign(campaignID);

        if (checkCanceled()) return null;
        monitor.setNote("Registering resources");
        monitor.setProgress(5);

        ResourceManager.registerCampaignPackage();

        if (checkCanceled()) return null;
        monitor.setNote("Reading Ruleset");
        monitor.setProgress(6);

        Game.ruleset.readData();

        if (checkCanceled()) return null;
        monitor.setNote("Reading Campaign Data");
        monitor.setProgress(7);

        Game.curCampaign.readCampaignFile();

        if (checkCanceled()) return null;
        monitor.setProgress(8);

        EditorManager.loadAllAssets();

        if (checkCanceled()) return null;
        monitor.setProgress(9);

        return null;
    }

    @Override
    protected void done()
    {
        parent.updateCampaign();
    }

    private boolean checkCanceled()
    {
        if (monitor.isCanceled()) {
            EntityManager.clear();
            Game.curCampaign = null;

            return true;
        }

        return false;
    }
}
