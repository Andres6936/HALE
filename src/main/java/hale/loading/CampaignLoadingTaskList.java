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

package hale.loading;

import java.io.File;

import hale.Game;
import hale.entity.EntityManager;
import hale.resource.ResourceManager;
import hale.resource.ResourceType;
import hale.resource.SpriteManager;
import hale.rules.Ruleset;

/**
 * A loading task list that performs the initial actions neccesary to load the campaign
 *
 * @author Jared Stephen
 */

public class CampaignLoadingTaskList extends LoadingTaskList
{
    /**
     * Creates a LoadingTaskList populated by the tasks needed to load a campaign.
     * This LoadingTaskList must be started in the normal way, by calling {@link #start()}
     */

    public CampaignLoadingTaskList()
    {
        super();

        SpriteManager.clear();

        Runnable registerCampaign = new Runnable()
        {
            @Override
            public void run()
            {
                ResourceManager.registerCampaignPackage();
            }
        };

        // load spritesheets task
        LoadingTask spriteTask = new LoadingTask(null, "Loading Images");
        for (String resource : ResourceManager.getResourcesInDirectory("images")) {
            if (!resource.endsWith(ResourceType.JSON.getExtension())) continue;

            spriteTask.addSubTask(new SpriteSheetLoader(resource), 10);
        }

        Runnable loadAnimations = new Runnable()
        {
            @Override
            public void run()
            {
                Game.particleManager.loadBaseResources();
            }
        };

        Runnable loadRuleset = new Runnable()
        {
            @Override
            public void run()
            {
                Game.ruleset = new Ruleset();
                Game.ruleset.readData();

                EntityManager.clear();
            }
        };

        Runnable loadCampaign = new Runnable()
        {
            @Override
            public void run()
            {
                Game.curCampaign.readCampaignFile();

                // free up memory
                System.gc();
            }
        };

        addTask(registerCampaign, "Registering Campaign Resources", 1);
        addTask(spriteTask);
        addTask(loadAnimations, "Loading Animations", 5);
        addTask(loadRuleset, "Loading Ruleset", 30);
        addTask(loadCampaign, "Loading Campaign resources", 30);
    }

    @Override
    protected void onError()
    {
        Game.curCampaign = null;

        File f = new File(Game.plataform.getConfigDirectory() + "lastOpenCampaign.txt");
        f.delete();
    }

    private class SpriteSheetLoader implements Runnable
    {
        private String resource;

        private SpriteSheetLoader(String resource)
        {
            this.resource = resource;
        }

        @Override
        public void run()
        {
            SpriteManager.readSpriteSheet(resource);
        }
    }
}
