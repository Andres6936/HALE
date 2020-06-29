package hale;

import java.io.IOException;
import java.net.URL;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.input.Input;
import de.matthiasmann.twl.renderer.Texture;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;

/**
 * Class overriding TWL LWJGL Renderer, in order to support entire-display scaling
 *
 * @author Jared
 */

public class HaleLWJGLRenderer extends LWJGLRenderer
{

    public HaleLWJGLRenderer() throws LWJGLException
    {
        super();
    }

    @Override
    protected void setupGLState()
    {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_TRANSFORM_BIT | GL11.GL_HINT_BIT |
                GL11.GL_COLOR_BUFFER_BIT | GL11.GL_SCISSOR_BIT | GL11.GL_LINE_BIT | GL11.GL_TEXTURE_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, Game.config.getResolutionX(), Game.config.getResolutionY(), 0, -1.0, 1.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);
        GL11.glEnable(GL14.GL_COLOR_SUM);
    }

    @Override
    public Texture loadTexture(URL url, String formatStr, String filterStr) throws IOException
    {
        return super.loadTexture(url, formatStr, "NEAREST");
    }

    @Override
    public int getHeight()
    {
        return super.getHeight() / Game.config.getScaleFactor();
    }

    @Override
    public int getWidth()
    {
        return super.getWidth() / Game.config.getScaleFactor();
    }

    @Override
    public Input getInput()
    {
        return new HaleLWJGLInput();
    }

    @Override
    public void clipEnter(int x, int y, int w, int h)
    {
        super.clipEnter(x * Game.config.getScaleFactor(), y * Game.config.getScaleFactor(),
                w * Game.config.getScaleFactor(), h * Game.config.getScaleFactor());
    }

    @Override
    public void clipEnter(Rect rect)
    {
        super.clipEnter(rect);
    }
}
