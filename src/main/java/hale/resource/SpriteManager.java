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

package hale.resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import hale.Game;
import hale.util.Logger;
import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;
import hale.util.SimpleJSONParser;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * A class for managing sprites and texture memory used by the Game.  Note that this
 * class does not manage GUI images, as those are held internally by TWL.
 *
 * @author Jared Stephen
 */

public class SpriteManager
{
    private final static Map<String, Sprite> sprites = new HashMap<String, Sprite>();
    private final static Map<String, Sprite> spriteSheets = new HashMap<String, Sprite>();

    private final static Map<String, BufferedImage> sourceImages = new HashMap<String, BufferedImage>();

    private static boolean saveSourceImages = false;

    /**
     * Sets whether the buffered images that are read in to create spritesheets are saved or discarded.  if false,
     * these images are discarded once the spritesheets have been loaded into an openGL context.  if true, the
     * BufferedImages are saved for later use
     *
     * @param saveSourceImages
     */

    public static void setSaveSourceImages(boolean saveSourceImages)
    {
        SpriteManager.saveSourceImages = saveSourceImages;
    }

    /**
     * Performs preloading of all Sprites specified as part of a spritesheet in the "images"
     * resource directory.  The Sprites in those spritesheets will be accessible via their
     * resource ID after this method returns.
     */

    public static void loadSpriteSheets()
    {
        Set<String> resources = ResourceManager.getResourcesInDirectory("images");
        for (String resource : resources) {
            if (resource.endsWith(ResourceType.JSON.getExtension())) {
                SpriteManager.readSpriteSheet(resource);
            }
        }
    }

    /**
     * Preloads all portrait images located in the "portraits" resource directory.  This method
     * is used by the game editor to allow fast selection of portraits.  In general, all
     * portraits will not be preloaded as they can be loaded on demand via {@link #getPortrait(String)}
     */

    public static void loadAllPortraits()
    {
        for (String resource : ResourceManager.getResourcesInDirectory("portraits")) {
            if (resource.endsWith(ResourceType.PNG.getExtension())) {
                SpriteManager.getImage(resource);
            }
        }
    }

    /**
     * Reads the SpriteSheet at the specified location.  All sprites specified in the SpriteSheet
     * are loaded into texture memory and become available via {@link #getImage(String)} and
     * similar methods.
     *
     * @param resource the resource ID of the SpriteSheet to load
     * @return a List of all Sprite resource IDs contained within the SpriteSheet which are loaded
     * into texture memory
     */

    public static List<String> readSpriteSheet(String resource)
    {
        List<Sprite> spritesToLoad = new ArrayList<Sprite>();
        ByteBufferSized pixelData = null;
        String parent = new File(resource).getParent().replace('\\', '/');
        List<String> images = new ArrayList<String>();

        SimpleJSONParser parser = new SimpleJSONParser(resource);

        int gridSize = parser.get("gridSize", 0);

        String source = parser.get("source", null);
        // category is a mapping between source, and reference.
        String category = parser.get("category", null);
        String mapping = category != null ? category : source;
        pixelData = SpriteManager.loadPixels(parent + "/" + source + ".png");
        Sprite spriteSheet = new Sprite(0, pixelData.width, pixelData.height);
        spritesToLoad.add(spriteSheet);

        SimpleJSONObject imagesIn = parser.getObject("images");

        for (String imageID : imagesIn.keySet()) {
            SimpleJSONArray imageIn = imagesIn.getArray(imageID);

            Iterator<SimpleJSONArrayEntry> iter = imageIn.iterator();

            int x = iter.next().getInt(0);
            int y = iter.next().getInt(0);

            int w;
            int h;
            // either classic definition x,y,w,h, or grid position row, column
            if (iter.hasNext()) {
                w = iter.next().getInt(0);
                h = iter.next().getInt(0);
            } else {
                w = gridSize;
                h = gridSize;
                x *= gridSize;
                y *= gridSize;
            }

            String reference = parent + "/" + mapping + "/" + imageID + ".png";

            // compute the texture coordinates
            double texCoordStartX = (double)x / (double)spriteSheet.getWidth();
            double texCoordStartY = (double)y / (double)spriteSheet.getHeight();
            double texCoordEndX = (double)(x + w) / (double)spriteSheet.getWidth();
            double texCoordEndY = (double)(y + h) / (double)spriteSheet.getHeight();

            // add the sprite
            Sprite sprite = new Sprite(spriteSheet.getTextureReference(), w, h,
                    texCoordStartX, texCoordStartY, texCoordEndX, texCoordEndY);
            images.add(mapping + "/" + imageID);

            if (sprites.containsKey(reference)) {
                Logger.appendToWarningLog("Warning, overwriting sprite " + reference);
            }
            sprites.put(reference, sprite);

            spritesToLoad.add(sprite);
        }

        // now save the spritesheet as its own sprite
        String reference = parent + "/" + source;

        // load the sprite texture for this spritesheet asynchronously
        Game.textureLoader.loadTexture(pixelData.pixels, pixelData.width,
                pixelData.height, spritesToLoad);

        if (spriteSheets.containsKey(reference)) {
            Logger.appendToWarningLog("Warning, SpriteSheet " + reference +
                    " is being overwritten.  This is a texture memory leak.");
        }
        spriteSheets.put(reference, spriteSheet);

        return images;
    }

    /**
     * Returns the source image created for the spritesheet of the specified ID.  Note that
     * {@link #setSaveSourceImages(boolean)} needs to be set to true when the spritesheet is
     * loaded for this method to find the image
     *
     * @param id
     * @return the saved image or null if no such image exists
     */

    public static BufferedImage getSourceImage(String id)
    {
        return sourceImages.get(id);
    }

    /**
     * Gets the full length resource ID for the specified image ID
     *
     * @param imageID
     * @return the resource ID
     */

    public static String getResourceID(String imageID)
    {
        if (getSprite(imageID, ResourceType.PNG) != null) {
            return "images/" + imageID + ResourceType.PNG.getExtension();
        } else
            if (getSprite(imageID, ResourceType.JPEG) != null) {
                return "images/" + imageID + ResourceType.JPEG.getExtension();
            } else
                if (getPortrait(imageID) != null) {
                    return "portraits/" + imageID + ResourceType.PNG.getExtension();
                } else {
                    return null;
                }
    }

    /**
     * Returns the set of all the resource IDs for the Sprites contained in this SpriteManager.
     * Each Sprite can then be accessed via {@link #getImage(String)}.  Note that the returned
     * set is unmodifiable.
     *
     * @return the set of all Sprite resource IDs in this SpriteManager
     */

    public static Set<String> getSpriteIDs()
    {
        return Collections.unmodifiableSet(sprites.keySet());
    }

    /**
     * Returns true if and only if the sprite with the specified resource ID has already been
     * loaded into texture memory and is available via this SpriteManager.  If the specified
     * resource ID ends with the PNG extension, then the resource ID is assumed to be a complete
     * ID suitable for use with {@link #getImage(String)}.  If it does not end with the PNG
     * extension, it is assumed to be a Sprite ID suitable for use with {@link #getSprite(String)}
     *
     * @param ref the resource ID of the Sprite to check for
     * @return true if and only if the specified Sprite is found
     */

    public static final boolean hasSprite(String ref)
    {
        if (ref.endsWith(ResourceType.PNG.getExtension())) {
            return sprites.containsKey(ref);
        } else {
            return sprites.containsKey("images/" + ref + ResourceType.PNG.getExtension());
        }
    }

    /**
     * Returns the Sprite found at the specified Sprite ID.  This is the Sprite found at
     * the resource ID constructed as the "images/" directory plus the specified ID plus the
     * extension.  Extensions are checked in the following order: PNG, JPEG
     *
     * @param ref the Sprite ID
     * @return the Sprite found at the specified ID or null if no sprite is found
     */

    public static final Sprite getSpriteAnyExtension(String ref)
    {
        Sprite sprite = SpriteManager.getImage("images/" + ref + ResourceType.PNG.getExtension());
        if (sprite != null) return sprite;

        sprite = SpriteManager.getImage("images/" + ref + ResourceType.JPEG.getExtension());
        return sprite;
    }

    /**
     * Returns the Sprite found at the specified Sprite ID.  This is the Sprite found at
     * the resource ID constructed as the "images/" directory plus the specified ID plus the
     * extension for the specified type
     *
     * @param ref the Sprite ID
     * @return the Sprite found at the specified ID or null if no sprite is found
     */

    public static final Sprite getSprite(String ref, ResourceType type)
    {
        return SpriteManager.getImage("images/" + ref + type.getExtension());
    }

    /**
     * Returns the Sprite found at the specified Sprite ID.  This is the Sprite found at
     * the resource ID constructed as the "images/" directory plus the specified ID plus the
     * PNG extension
     *
     * @param ref the Sprite ID
     * @return the Sprite found at the specified ID or null if no sprite is found
     */

    public static final Sprite getSprite(String ref)
    {
        return SpriteManager.getImage("images/" + ref + ResourceType.PNG.getExtension());
    }

    /**
     * Returns the Sprite found at the specified Portrait ID.  This is the Sprite found
     * at the resource ID constructed as the "portraits/" directory plus the specified ID
     * plus the PNG extension
     *
     * @param ref the Portrait ID
     * @return the Sprite found at the specified ID or null if no sprite is found
     */

    public static final Sprite getPortrait(String ref)
    {
        return SpriteManager.getImage("portraits/" + ref + ResourceType.PNG.getExtension());
    }

    /**
     * Returns the Sprite found at the specified resource ID.
     *
     * @param ref the resource ID reference for the Sprite
     * @return the Sprite at the specified ID
     */

    public static final Sprite getImage(String ref)
    {
        if (!sprites.containsKey(ref)) {
            if (ResourceManager.hasResource(ref)) {
                sprites.put(ref, SpriteManager.loadImage(ref));
            }
        }

        return sprites.get(ref);
    }

    private static final ByteBufferSized loadPixels(String ref)
    {
        BufferedImage sourceImage = null;
        // read the image
        try {
            sourceImage = ImageIO.read(ResourceManager.getStream(ref));

        } catch (IOException e) {
            Logger.appendToErrorLog("Failed to load image: " + ref, e);
            throw new IllegalArgumentException("Image does not exist at " + ref);
        }

        if (saveSourceImages) {
            sourceImages.put(ref, sourceImage);
        }

        // get the rgb color data from the read image
        int[] rgb = new int[sourceImage.getWidth() * sourceImage.getHeight()];
        sourceImage.getRGB(0, 0, sourceImage.getWidth(), sourceImage.getHeight(), rgb, 0, sourceImage.getWidth());

        // convert the rgb data to the appropriate format for OpenGL use
        byte[] src = new byte[rgb.length * 4];
        for (int i = 0; i < rgb.length; i++) {
            src[4 * i + 3] = (byte)(rgb[i] >>> 24);
            src[4 * i + 0] = (byte)(rgb[i] >>> 16);
            src[4 * i + 1] = (byte)(rgb[i] >>> 8);
            src[4 * i + 2] = (byte)rgb[i];
        }

        ByteBuffer pixels = (ByteBuffer)BufferUtils.createByteBuffer(src.length).put(src).flip();

        ByteBufferSized buffer = new ByteBufferSized(pixels, sourceImage.getWidth(), sourceImage.getHeight());
        return buffer;
    }

    private static final Sprite loadImage(String ref)
    {
        ByteBufferSized buffer = SpriteManager.loadPixels(ref);

        Sprite sprite = new Sprite(0, buffer.width, buffer.height);
        List<Sprite> sprites = Collections.singletonList(sprite);

        // load the texture asynchronously
        Game.textureLoader.loadTexture(buffer.pixels, buffer.width, buffer.height, sprites);
        return sprite;
    }

    /**
     * Frees all texture memory associated with this SpriteManager and removes all
     * held Sprites.  After this operation is complete, this SpriteManager will
     * be empty.
     */

    public static void clear()
    {
        Set<Integer> deletedTextures = new HashSet<Integer>();

        // free up all texture memory
        for (String id : spriteSheets.keySet()) {
            Sprite spriteSheet = spriteSheets.get(id);

            Integer texture = spriteSheet.getTextureReference();
            if (!deletedTextures.contains(texture)) {
                GL11.glDeleteTextures(texture);
                deletedTextures.add(texture);
            }
        }

        for (String id : sprites.keySet()) {
            Sprite sprite = sprites.get(id);

            Integer texture = sprite.getTextureReference();
            if (!deletedTextures.contains(texture)) {
                GL11.glDeleteTextures(texture);
                deletedTextures.add(texture);
            }
        }

        sprites.clear();
        spriteSheets.clear();
    }

    /**
     * Frees the texture used by this Sprite and removes all Sprites and Spritesheets in the
     * SpriteManager that reference that texture
     *
     * @param spriteToDelete the Sprite who's texture is to be deleted
     */

    public static void freeTexture(Sprite spriteToDelete)
    {
        int texture = spriteToDelete.getTextureReference();
        GL11.glDeleteTextures(texture);

        Iterator<String> iter = spriteSheets.keySet().iterator();
        while (iter.hasNext()) {
            Sprite spriteSheet = spriteSheets.get(iter.next());

            if (spriteSheet.getTextureReference() == texture) {
                iter.remove();
            }
        }

        iter = sprites.keySet().iterator();
        while (iter.hasNext()) {
            Sprite sprite = sprites.get(iter.next());

            if (sprite.getTextureReference() == texture) {
                iter.remove();
            }
        }
    }

    /**
     * Computes the total size in bytes of all currently loaded sprites and spritesheets.
     * Does not include memory used by the GUI toolkit TWL.
     *
     * @return the total amount of texture memory used by the SpriteManager in bytes
     */

    public static long getTextureMemoryUsage()
    {
        long total = 0;

        Set<Integer> texturesAlreadyCounted = new HashSet<Integer>();

        for (String id : spriteSheets.keySet()) {
            Sprite spriteSheet = spriteSheets.get(id);

            Integer textureID = spriteSheet.getTextureReference();

            if (texturesAlreadyCounted.contains(textureID)) continue;

            texturesAlreadyCounted.add(textureID);

            long memory = spriteSheet.getWidth() * spriteSheet.getHeight() * 4;
            total += memory;
        }

        for (String id : sprites.keySet()) {
            Sprite sprite = sprites.get(id);

            Integer textureID = sprite.getTextureReference();

            if (texturesAlreadyCounted.contains(textureID)) continue;

            texturesAlreadyCounted.add(textureID);

            long memory = sprite.getWidth() * sprite.getHeight() * 4;
            total += memory;
        }

        return total;
    }

    private static class ByteBufferSized
    {
        private ByteBuffer pixels;
        private int width, height;

        private ByteBufferSized(ByteBuffer pixels, int width, int height)
        {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
        }
    }
}
