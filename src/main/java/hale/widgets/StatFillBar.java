package hale.widgets;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.TextWidget;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.renderer.Image;

/**
 * A bar that can be filled or empty to display the status of a
 * statistic
 *
 * @author Jared
 */

public class StatFillBar extends TextWidget
{
    private Image fullImage;
    private Image emptyImage;
    private float value;

    /**
     * Sets the text being display by this statbar
     *
     * @param text
     */

    public void setText(String text)
    {
        setCharSequence(text);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        this.fullImage = themeInfo.getImage("fullImage");
        this.emptyImage = themeInfo.getImage("emptyImage");
    }

    /**
     * Sets the fractional value (from 0.0 to 1.0) that the bar
     * is filled
     *
     * @param value
     */

    public void setValue(float value)
    {
        this.value = Math.max(0.0f, Math.min(1.0f, value));
    }

    @Override
    public int getPreferredWidth()
    {
        Image bg = getBackground();

        return bg != null ? bg.getWidth() : 0;
    }

    @Override
    public int getPreferredHeight()
    {
        Image bg = getBackground();

        return bg != null ? bg.getHeight() : 0;
    }

    @Override
    protected void paint(GUI gui)
    {
        if (fullImage != null && emptyImage != null) {
            int cutOff = (int)(fullImage.getWidth() * value);

            fullImage.draw(getAnimationState(), getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
            emptyImage.draw(getAnimationState(), getInnerX() + cutOff, getInnerY(),
                    getInnerWidth() - cutOff, getInnerHeight());
        }

        paintBackground(gui);
        paintWidget(gui);
        paintChildren(gui);
        paintOverlay(gui);
    }
}
