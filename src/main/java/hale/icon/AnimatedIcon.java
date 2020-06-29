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

package hale.icon;

import hale.Game;
import hale.loading.JSONOrderedObject;
import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;
import de.matthiasmann.twl.Color;

/**
 * An icon that consists of one or more frames, each played with a set duration,
 * looped endlessly
 *
 * @author Jared
 */

public class AnimatedIcon implements Icon
{

    private final int frameDuration;
    private final SimpleIcon[] frames;

    private final int totalDuration;

    /**
     * Constructs an AnimatedIcon by parsing the specified JSON
     *
     * @param data
     */

    public AnimatedIcon(SimpleJSONObject data)
    {
        this.frameDuration = data.get("frameDuration", 0);

        SimpleJSONArray framesIn = data.getArray("frames");

        int index = 0;
        this.frames = new SimpleIcon[framesIn.size()];
        for (SimpleJSONArrayEntry entry : framesIn) {
            SimpleIcon frame = new SimpleIcon(entry.getObject());
            this.frames[index] = frame;
            index++;
        }

        this.totalDuration = this.frames.length * frameDuration;
    }

    private AnimatedIcon(int frameDuration, SimpleIcon[] frames)
    {
        this.frameDuration = frameDuration;
        this.frames = frames;
        this.totalDuration = frameDuration * frames.length;
    }

    @Override
    public void draw(int x, int y)
    {
        int index = (int)(Game.mainViewer.getFrameTime() % totalDuration) / frameDuration;

        this.frames[index].draw(x, y);
    }

    @Override
    public void drawCentered(int x, int y, int width, int height)
    {
        int index = (int)(Game.mainViewer.getFrameTime() % totalDuration) / frameDuration;

        this.frames[index].drawCentered(x, y, width, height);
    }

    @Override
    public int getWidth()
    {
        return frames[0].getWidth();
    }

    @Override
    public int getHeight()
    {
        return frames[0].getHeight();
    }

    @Override
    public AnimatedIcon multiplyByColor(Color color)
    {
        SimpleIcon[] frames = new SimpleIcon[this.frames.length];

        for (int i = 0; i < frames.length; i++) {
            frames[i] = this.frames[i].multiplyByColor(color);
        }

        return new AnimatedIcon(this.frameDuration, frames);
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject out = new JSONOrderedObject();

        out.put("frameDuration", this.frameDuration);

        JSONOrderedObject[] framesOut = new JSONOrderedObject[this.frames.length];
        for (int i = 0; i < this.frames.length; i++) {
            framesOut[i] = this.frames[i].save();
        }
        out.put("frames", framesOut);

        return out;
    }

}
