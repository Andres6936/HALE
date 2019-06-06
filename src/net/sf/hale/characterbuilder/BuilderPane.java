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

package net.sf.hale.characterbuilder;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * The base class for most panes of the CharacterBuilder.  BuilderPanes
 * are the primary content of the CharacterBuilder and give the user the
 * ability to customize aspects of the Creature being built.  All BuilderPanes
 * extend AbstractBuilderPane but some BuilderPanes do not extend this class.
 *
 * @author Jared Stephen
 */

public abstract class BuilderPane extends AbstractBuilderPane
{
    private HTMLTextAreaModel textAreaModel;
    private ScrollPane textPane;

    private Label titleLabel;
    private ScrollPane selectorPane;
    private DialogLayout selectorPaneContent;
    private DialogLayout.Group selectorPaneContentH;
    private DialogLayout.Group selectorPaneContentV;
    private Label pointsLabel;

    private int titleGap, paneGap;

    /**
     * Create a BuilderPane with the specified descriptive name.
     *
     * @param builder   the CharacterBuilder containing this BuilderPane
     * @param name      the identifying name for this BuilderPane
     * @param character the character that this BuilderPane will be editing
     */

    public BuilderPane( CharacterBuilder builder, String name, Buildable character )
    {
        super( builder, name, character );

        textAreaModel = new HTMLTextAreaModel( );
        TextArea textArea = new TextArea( textAreaModel );
        textPane = new ScrollPane( textArea );
        textPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
        textPane.setCanAcceptKeyboardFocus( false );
        textPane.setTheme( "descriptionpane" );

        titleLabel = new Label( );
        titleLabel.setTheme( "titlelabel" );

        selectorPaneContent = new DialogLayout( );
        selectorPaneContent.setTheme( "content" );
        selectorPane = new ScrollPane( selectorPaneContent );
        selectorPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
        selectorPane.setTheme( "selectorpane" );

        selectorPaneContentH = selectorPaneContent.createParallelGroup( );
        selectorPaneContentV = selectorPaneContent.createSequentialGroup( );
        selectorPaneContent.setHorizontalGroup( selectorPaneContentH );
        selectorPaneContent.setVerticalGroup( selectorPaneContentV );

        pointsLabel = new Label( );
        pointsLabel.setTheme( "pointslabel" );

        this.add( pointsLabel );
        this.add( titleLabel );
        this.add( selectorPane );
        this.add( textPane );
    }

    /**
     * Returns the ScrollPane that holds all selectors that are added to this Widget
     * via {@link #addSelector(Widget)}
     *
     * @return the ScrollPane that holds all selectors in this Widget
     */

    protected ScrollPane getSelectorPane( )
    {
        return selectorPane;
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        this.titleGap = themeInfo.getParameter( "titlegap", 0 );
        this.paneGap = themeInfo.getParameter( "panegap", 0 );
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        titleLabel.setSize( titleLabel.getPreferredWidth( ), titleLabel.getPreferredHeight( ) );
        titleLabel.setPosition( getInnerX( ), getInnerY( ) );

        pointsLabel.setSize( pointsLabel.getPreferredWidth( ), pointsLabel.getPreferredHeight( ) );
        pointsLabel.setPosition( getInnerX( ), titleLabel.getBottom( ) );

        int backNextWidth = getBackButton( ).getWidth( ) + getNextButton( ).getWidth( );

        int selectorPaneWidth = selectorPane.getPreferredWidth( );
        int selectorPaneHeight = Math.min( selectorPane.getPreferredHeight( ),
                                           getNextButton( ).getY( ) - pointsLabel.getBottom( ) - titleGap - paneGap - getAdditionalSelectorPaneHeightLimit( ) );

        int textPaneX = getInnerX( ) + Math.max( backNextWidth, selectorPaneWidth );

        selectorPane.setPosition( getInnerX( ), pointsLabel.getBottom( ) + titleGap );
        selectorPane.setSize( textPaneX - getInnerX( ), selectorPaneHeight );

        // if the preferred size is too small leaving a horizontal scrollbar,
        // recompute the size based on the scrollbar being visible
        selectorPane.updateScrollbarSizes( );
        if ( selectorPane.getHorizontalScrollbar( ).isVisible( ) )
        {
            selectorPane.setSize( selectorPane.getPreferredWidth( ), selectorPaneHeight );
            textPaneX = selectorPane.getRight( );
            selectorPane.getHorizontalScrollbar( ).setVisible( false );
        }

        textPane.setPosition( textPaneX, getInnerY( ) );
        textPane.setSize( getInnerRight( ) - textPaneX, getInnerHeight( ) );
    }

    /**
     * Sets the text displayed by the points label to the specified text
     *
     * @param text the text to display in the points label
     */

    public void setPointsText( String text )
    {
        pointsLabel.setText( text );
    }

    /**
     * Sets the text displayed by the title label to the specified text
     *
     * @param text the text to display in the title label
     */

    public void setTitleText( String text )
    {
        titleLabel.setText( text );
    }

    /**
     * Adds the specified Widget to the scroll pane containing the selectors for this
     * BuilderPane
     *
     * @param selector the selector to add
     */

    public void addSelector( Widget selector )
    {
        selectorPaneContentH.addWidget( selector );
        selectorPaneContentV.addWidget( selector );
    }

    /**
     * Removes all widgets from the scroll pane containing the selectors
     */

    public void clearSelectors( )
    {
        selectorPaneContentH.clear( true );
        selectorPaneContentV.clear( true );
    }

    /**
     * Returns the ScrollPane used for displaying descriptions in this
     * BuilderPane
     *
     * @return the ScrollPane used by this BuilderPane
     */

    public ScrollPane getTextPane( )
    {
        return textPane;
    }

    /**
     * Returns the HTML model used by the TextArea used for displaying
     * descriptions
     *
     * @return the text area model
     */

    public HTMLTextAreaModel getTextModel( )
    {
        return textAreaModel;
    }
}
