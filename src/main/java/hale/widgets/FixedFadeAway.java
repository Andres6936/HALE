package main.java.hale.widgets;

import de.matthiasmann.twl.Color;
import main.java.hale.util.Point;

public class FixedFadeAway extends OverHeadFadeAway
{
    private Point screenPoint;

    public FixedFadeAway(String text, Point screenPoint, Color color)
    {
        super(text, new Point(0, 0), color);
        this.screenPoint = screenPoint;
        this.setTheme("overheadfadeaway");
    }

    @Override
    public void initialize(long startTime)
    {
        super.initialize(startTime);

        setPosition(screenPoint.x, screenPoint.y);
    }

    @Override
    public void updateTime(long curTime)
    {
        super.updateTime(curTime);

        setPosition(screenPoint.x, screenPoint.y);
    }
}
