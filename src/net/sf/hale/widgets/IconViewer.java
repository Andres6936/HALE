package net.sf.hale.widgets;

import org.lwjgl.opengl.GL11;

import net.sf.hale.icon.Icon;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A Widget for showing a generic icon from any source.
 *
 * @author Jared Stephen
 */

public class IconViewer extends Button
{

    private short minWidth, minHeight;

    private boolean enableEventHandling;
    private Icon icon;

    /**
     * Creates a new IconViewer displaying blank
     */

    public IconViewer( )
    {
        this( null );
    }

    /**
     * Creates a new IconViewer displaying the specified icon
     *
     * @param icon the Icon to be displayed
     */

    public IconViewer( Icon icon )
    {
        this.icon = icon;

        enableEventHandling = true;
    }

    /**
     * Creates a new IconViewer displaying the specified icon
     * and showing the specified tooltip
     *
     * @param icon    the Icon to be displayed
     * @param tooltip the tooltip that will appear when the user
     *                hovers the mouse over this Widget
     */

    public IconViewer( Icon icon, String tooltip )
    {
        this( icon );
        this.setTooltipContent( tooltip );
    }

    /**
     * Sets whether this widget will handle events such as Button clicks,
     * mouse hover, etc.
     *
     * @param enabled whether this Widget will handle events
     */

    public void setEventHandlingEnabled( boolean enabled )
    {
        this.enableEventHandling = enabled;
    }

    /**
     * Returns the Icon that this IconViewer is currently displaying.
     * If no Icon is being displayed, returns null.
     *
     * @return the Icon currently being displayed
     */

    public Icon getIcon( )
    {
        return this.icon;
    }

    /**
     * Sets the Icon that will be displayed by this Widget
     *
     * @param icon the Icon to be displayed
     */

    public void setIcon( Icon icon )
    {
        this.icon = icon;
    }

    @Override
    protected void paintWidget( GUI gui )
    {
        super.paintWidget( gui );

        if ( icon != null )
        {
            icon.drawCentered( getInnerX( ), getInnerY( ), getInnerWidth( ), getInnerHeight( ) );

            GL11.glColor3f( 1.0f, 1.0f, 1.0f );
        }
    }

    @Override
    protected boolean handleEvent( Event evt )
    {
        if ( ! this.enableEventHandling ) return false;

        switch ( evt.getType( ) )
        {
            case MOUSE_WHEEL:
                // do not eat mouse wheel events so that scrolling
                // from containing scroll panes works
                return false;
            default:
                return super.handleEvent( evt );
        }
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        minWidth = ( short ) themeInfo.getParameter( "minWidth", 0 );
        minHeight = ( short ) themeInfo.getParameter( "minHeight", 0 );
    }

    @Override
    public int getPreferredWidth( )
    {
        int width = getBorderHorizontal( );
        if ( icon != null ) width += icon.getWidth( );

        return Math.max( width, minWidth );
    }

    @Override
    public int getPreferredHeight( )
    {
        int height = getBorderVertical( );
        if ( icon != null ) height += icon.getHeight( );

        return Math.max( height, minHeight );
    }
}
