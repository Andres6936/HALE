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

package main.java.hale.entity;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import main.java.hale.Game;
import main.java.hale.resource.ResourceManager;
import main.java.hale.resource.ResourceType;
import main.java.hale.rules.Quality;
import main.java.hale.util.Logger;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * Class for storing all available EntityTemplates
 *
 * @author Jared
 */

public class EntityManager
{
    private static Map<String, EntityTemplate> templates = new HashMap<String, EntityTemplate>();

    // stored creatures with already added levels and stats
    private static Map<String, Creature> creatures = new HashMap<String, Creature>();

    /**
     * Adds the specified PC to the list of available creatures.  This method is only
     * used when loading a PC from save game data, as the data might not be accessible
     * elsewhere (from a creature file)
     *
     * @param pc
     */

    public static void addLoadedPC(PC pc)
    {
        templates.put(pc.getTemplate().getID(), pc.getTemplate());
        creatures.put(pc.getTemplate().getID(), pc);
    }

    /**
     * Gets an instance of the NPC based on the template with the specified ID.
     * If the NPCTemplate has not been loaded yet, it is loaded from the appropriate
     * resource in the "creatures/" resource directory
     *
     * @param entityID
     * @return a new NPC based on the specified template
     */

    public static NPC getNPC(String entityID)
    {
        if (!creatures.containsKey(entityID)) {
            loadNPC(entityID);
        }

        NPC npc = new NPC((NPC)creatures.get(entityID));
        npc.resetTime();
        return npc;
    }

    /**
     * Gets an instance of the PC based on the template with the specified ID.  If the
     * PCTemplate has not yet been loaded, it is loaded from the appropriate resource:
     * First, the "creatures/" directory is checked.  If no matching ID is found there, then
     * the characters base directory and the "characters/" directories are checked.
     *
     * @param entityID
     * @return a new PC based on the template with the specified ID
     */

    public static PC getPC(String entityID)
    {
        if (!creatures.containsKey(entityID)) {
            loadPC(entityID);
        }

        PC pc = new PC((PC)creatures.get(entityID));
        pc.resetTime();
        return pc;
    }

    private static void loadNPC(String entityID)
    {
        String resource = "creatures/" + entityID;

        SimpleJSONParser parser = new SimpleJSONParser(resource, ResourceType.JSON);

        // first create the template
        NPCTemplate template = new NPCTemplate(entityID, parser.getObject());
        templates.put(entityID, template);

        // now create the NPC from the same JSON
        NPC npc = new NPC(template, parser);
        creatures.put(entityID, npc);
    }

    private static void loadPC(String entityID)
    {
        String resource = "creatures/" + entityID;
        SimpleJSONParser parser;

        if (ResourceManager.hasResource(resource + ResourceType.JSON.getExtension())) {
            parser = new SimpleJSONParser(resource, ResourceType.JSON);
        } else {
            String fileName = entityID + ResourceType.JSON.getExtension();

            // look for a file in the pregen characters directory and then the user characters directory
            if (new File("characters/" + fileName).isFile()) {
                parser = new SimpleJSONParser(new File("characters/" + fileName));
            } else
                if (new File(Game.plataform.getCharactersDirectory() + fileName).isFile()) {
                    parser = new SimpleJSONParser(new File(Game.plataform.getCharactersDirectory() + fileName));
                } else {
                    throw new IllegalArgumentException("Unable to locate PC " + entityID);
                }
        }

        // first create the template
        PCTemplate template = new PCTemplate(entityID, parser.getObject());
        templates.put(entityID, template);

        try {
            PC pc = new PC(template, parser);
            creatures.put(entityID, pc);
        } catch (Exception e) {
            Logger.appendToErrorLog("Error loading PC " + entityID, e);
        }
    }

    /**
     * Gets an instance of the item with the specified ID.  If the template for this
     * item has not yet been loaded, it is created from the appropriate resource in the
     * "items/" resource directory.
     *
     * @param entityID
     * @return a new Item based on the template with the specified ID, or null if no such template exists
     */

    public static Item getItem(String entityID)
    {
        ItemTemplate template = getItemTemplate(entityID);

        if (template == null) return null;

        Item item = template.createInstance();
        Quality quality = template.getDefaultQuality();
        if (quality == null) {
            item.setQuality(null);
        } else {
            item.setQuality(quality.getName());
        }

        return item;
    }

    /**
     * Gets an instance of the item with the specified ID.  If the template for this
     * item has not yet been loaded, it is created from the appropriate resource in the
     * "items/" resource directory.  The quality of the returned item will be set to the
     * specified quality
     *
     * @param entityID
     * @param quality
     * @return a new Item based on the template with the specified ID
     */

    public static Item getItem(String entityID, String quality)
    {
        Item item = getItemTemplate(entityID).createInstance();
        item.setQuality(quality);

        return item;
    }

    /**
     * Returns true if this EntityManager contains an entity template with the specified ID,
     * false otherwise
     *
     * @param entityID
     * @return whether there is an entity with the specified ID in this EntityManager
     */

    public static boolean hasEntityTemplate(String entityID)
    {
        return templates.containsKey(entityID);
    }

    /**
     * Gets an instance of the item with the specified ID.  If the template for this
     * item has not yet been loaded, it is created from the appropriate resource in the
     * "items/" resource directory.  The quality of the returned item will be set to the
     * specified quality
     *
     * @param entityID
     * @param quality  the items quality, or null to not set a quality
     * @return a new Item based on the template with the specified ID
     */

    public static Item getItem(String entityID, Quality quality)
    {
        Item item = getItemTemplate(entityID).createInstance();
        if (quality != null) {
            item.setQuality(quality.getName());
        } else {
            item.setQuality(null);
        }

        return item;
    }

    /**
     * Gets an instance of the container with the specified ID.  If the template for
     * this container has not yet been loaded, it is created from the appropriate
     * resource in the "items/" directory.
     *
     * @param entityID
     * @return a new Container based on the template with the specified ID
     */

    public static Container getContainer(String entityID)
    {
        return getContainerTemplate(entityID).createInstance();
    }

    /**
     * Gets an instance of the door with the specified ID.  If the template for this
     * door has not yet been loaded, it is created from the appropriate resource
     * in the "items/" directory
     *
     * @param entityID
     * @return a new Door based on the template with the specified ID
     */

    public static Door getDoor(String entityID)
    {
        return getDoorTemplate(entityID).createInstance();
    }

    /**
     * Gets an instance of temporary container, which is the entity with the rule ID defined
     * by the string "TemporaryContainerID"
     *
     * @return a temporary container
     */

    public static Container getTemporaryContainer()
    {
        return getContainer(Game.ruleset.getString("TemporaryContainerID"));
    }

    /**
     * Gets the template used to create items of the specified ID, or null if no such template exists
     *
     * @param entityID
     * @return the template used to create the item
     */

    public static ItemTemplate getItemTemplate(String entityID)
    {
        if (templates.containsKey(entityID)) {
            return (ItemTemplate)templates.get(entityID);
        }

        if (ResourceManager.hasResource("items/" + entityID + ResourceType.JSON.getExtension())) {
            // if the resource exists, parse it

            String resource = "items/" + entityID;
            SimpleJSONParser parser = new SimpleJSONParser(resource, ResourceType.JSON);

            String className = parser.get("class", "Item");
            String templateName = className + "Template";

            // use reflection to create an appropriate object based on the specified class
            try {
                Class<?> templateClass = Class.forName(EntityManager.class.getPackage().getName() + "." + templateName);

                Constructor<?> templateConstructor = templateClass.getConstructor(String.class, SimpleJSONObject.class);

                ItemTemplate template = (ItemTemplate)templateConstructor.newInstance(entityID, parser.getObject());

                templates.put(entityID, template);

            } catch (Exception e) {
                Logger.appendToErrorLog("Error constructing item template " + entityID, e);
            }

        } else
            if (Game.curCampaign.getCreatedItem(entityID) != null) {
                // look for a created item
                CreatedItem createdItem = Game.curCampaign.getCreatedItem(entityID);
                templates.put(entityID, createdItem.getTemplate());
            } else {
                return null;
            }

        return (ItemTemplate)templates.get(entityID);
    }

    /**
     * Gets the template used to create containers of the specified ID
     *
     * @param entityID
     * @return the template used to create the container
     */

    public static ContainerTemplate getContainerTemplate(String entityID)
    {
        if (templates.containsKey(entityID)) {
            return (ContainerTemplate)templates.get(entityID);
        }

        String resource = "items/" + entityID;
        SimpleJSONParser parser = new SimpleJSONParser(resource, ResourceType.JSON);

        ContainerTemplate template = new ContainerTemplate(entityID, parser.getObject());
        templates.put(entityID, template);

        return template;
    }

    /**
     * Gets the template used to create doors of the specified ID
     *
     * @param entityID
     * @return the template used to create the door
     */

    public static DoorTemplate getDoorTemplate(String entityID)
    {
        if (templates.containsKey(entityID)) {
            return (DoorTemplate)templates.get(entityID);
        }

        String resource = "items/" + entityID;
        SimpleJSONParser parser = new SimpleJSONParser(resource, ResourceType.JSON);

        DoorTemplate template = new DoorTemplate(entityID, parser.getObject());
        templates.put(entityID, template);

        return template;
    }

    /**
     * Gets the template used to create npcs of the specified ID
     *
     * @param entityID
     * @return the template used to create the NPC
     */

    public static NPCTemplate getNPCTemplate(String entityID)
    {
        if (!templates.containsKey(entityID)) {
            loadNPC(entityID);
        }

        return (NPCTemplate)templates.get(entityID);
    }

    /**
     * Gets the template used to create PCs of the specified ID
     *
     * @param entityID
     * @return the template used to create the PC
     */

    public static PCTemplate getPCTemplate(String entityID)
    {
        if (!templates.containsKey(entityID)) {
            loadPC(entityID);
        }

        return (PCTemplate)templates.get(entityID);
    }

    /**
     * Removes all entities that have been stored, forcing future calls
     * to read entities from disk
     */

    public static void clear()
    {
        templates.clear();
        creatures.clear();
    }
}
