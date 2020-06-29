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

package main.java.hale.particle;

import main.java.hale.Game;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.util.SimpleJSONObject;

public class LineParticleGenerator extends ParticleGenerator
{
    private enum LineStepMode
    {
        X, Y;
    }

    private LineStepMode lineStepMode;
    private float lineStepRatio;
    private float lineStartX, lineStartY, lineEndX, lineEndY;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = super.save();

        data.put("lineStartX", lineStartX - getX());
        data.put("lineStartY", lineStartY - getY());
        data.put("lineEndX", lineEndX - getX());
        data.put("lineEndY", lineEndY - getY());

        return data;
    }

    public static ParticleGenerator load(SimpleJSONObject data) throws LoadGameException
    {
        Mode mode = Mode.valueOf(data.get("generatorMode", null));
        String particleSprite = data.get("sprite", null);
        float numParticles = data.get("numParticles", 0.0f);

        LineParticleGenerator generator = new LineParticleGenerator(mode, particleSprite, numParticles);

        generator.loadBase(data);

        generator.lineStartX = data.get("lineStartX", 0.0f);
        generator.lineStartY = data.get("lineStartY", 0.0f);
        generator.lineEndX = data.get("lineEndX", 0.0f);
        generator.lineEndY = data.get("lineEndY", 0.0f);

        return generator;
    }

    public LineParticleGenerator(Mode mode, String sprite, float numParticles)
    {
        super(mode, sprite, numParticles);
    }

    public LineParticleGenerator(LineParticleGenerator other)
    {
        super(other);
        this.lineStepMode = other.lineStepMode;
        this.lineStepRatio = other.lineStepRatio;
        this.lineStartX = other.lineStartX;
        this.lineStartY = other.lineStartY;
        this.lineEndX = other.lineEndX;
        this.lineEndY = other.lineEndY;
    }

    public void setLineStart(float lineStartX, float lineStartY)
    {
        this.lineStartX = lineStartX;
        this.lineStartY = lineStartY;

    }

    public void setLineEnd(float lineEndX, float lineEndY)
    {
        this.lineEndX = lineEndX;
        this.lineEndY = lineEndY;
    }

    @Override
    public void offsetPosition(float x, float y)
    {
        super.offsetPosition(x, y);

        lineStartX += x;
        lineEndX += x;

        lineStartY += y;
        lineEndY += y;
    }

    @Override
    public boolean initialize()
    {
        if (!super.initialize()) return false;

        this.lineStartX += getX();
        this.lineStartY += getY();
        this.lineEndX += getX();
        this.lineEndY += getY();

        float diffX = lineEndX - lineStartX;
        float diffY = lineEndY - lineStartY;

        if (Math.abs(diffX) > Math.abs(diffY)) {
            lineStepMode = LineStepMode.X;
            lineStepRatio = diffY / diffX;
        } else {
            lineStepMode = LineStepMode.Y;
            lineStepRatio = diffX / diffY;
        }

        return true;
    }

    @Override
    protected void setParticlePosition(Particle particle)
    {
        float posX;
        float posY;

        switch (lineStepMode) {
            case X:
                posX = Game.dice.rand(lineStartX, lineEndX);
                posY = (posX - lineStartX) * lineStepRatio + lineStartY;
                break;
            default:
                posY = Game.dice.rand(lineStartY, lineEndY);
                posX = (posY - lineStartY) * lineStepRatio + lineStartX;
                break;
        }

        particle.setPosition(posX, posY);
    }

    @Override
    public Animated getCopy()
    {
        return new LineParticleGenerator(this);
    }
}
