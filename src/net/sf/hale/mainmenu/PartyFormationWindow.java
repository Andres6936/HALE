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

package net.sf.hale.mainmenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.SavedParty;
import net.sf.hale.characterbuilder.Buildable;
import net.sf.hale.characterbuilder.CharacterBuilder;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.EntityManager;
import net.sf.hale.entity.PC;
import net.sf.hale.resource.ResourceType;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * The widget showing the options available for forming a party.
 *
 * @author Jared Stephen
 */

public class PartyFormationWindow extends Widget
{
    private MainMenu mainMenu;
    private NewGameWindow newGameWindow;

    private Label titleLabel;

    private Label nameLabel;
    private EditField nameField;

    private ToggleButton showPregeneratedCharactersButton;
    private Button cancel, accept;

    private Label partySize;
    private Label partyTitle;
    private ScrollPane partyPane;
    private Content partyPaneContent;

    private Label availableTitle;
    private ScrollPane availablePane;
    private Content availablePaneContent;

    private int smallGap;
    private int sectionGap;
    private int selectableWidth;

    private CharacterSelector highlightedSelector;

    private Set< String > charactersInParties;

    private DifficultySelector difficultySelector;

    /**
     * Creates a new PartyFormationWindow with the specified mainMenu as the parent Widget.
     *
     * @param mainMenu            the parent Widget for this PartyFormationWindow
     * @param window              the NewGameWindow that is updated when a party is accepted
     * @param charactersInParties
     */

    public PartyFormationWindow( MainMenu mainMenu, NewGameWindow window, Set< String > charactersInParties )
    {
        this.charactersInParties = charactersInParties;
        this.mainMenu = mainMenu;
        this.newGameWindow = window;
        mainMenu.setButtonsVisible( false );

        titleLabel = new Label( );
        titleLabel.setTheme( "titlelabel" );
        add( titleLabel );

        nameLabel = new Label( );
        nameLabel.setTheme( "namelabel" );

        nameField = new EditField( );
        nameField.addCallback( new EditField.Callback( )
        {
            @Override
            public void callback( int key )
            {
                setAcceptEnabled( );
            }
        } );
        nameField.setTheme( "namefield" );

        partyTitle = new Label( );
        partyTitle.setTheme( "partytitlelabel" );
        add( partyTitle );

        if ( window != null )
        { // if we are forming a party
            add( nameLabel );
            add( nameField );

            titleLabel.setText( "Party Formation" );
            partyTitle.setText( "Your Party" );
        }
        else
        { // if we are choosing a single character
            titleLabel.setText( "Choose a Character" );
            partyTitle.setText( "Your Character" );

            difficultySelector = new DifficultySelector( );
            add( difficultySelector );
        }

        int minSize = Game.curCampaign.getMinPartySize( );
        int maxSize = Game.curCampaign.getMaxPartySize( );
        String postFix;
        if ( minSize == maxSize )
        {
            if ( minSize == 1 ) { postFix = "1 Character"; }
            else { postFix = minSize + " Characters"; }
        }
        else
        {
            postFix = minSize + " to " + maxSize + " Characters";
        }

        int minLevel;
        if ( Game.curCampaign.allowLevelUp( ) )
        {
            minLevel = 1;
        }
        else
        {
            minLevel = Game.curCampaign.getMinStartingLevel( );
        }
        int maxLevel = Game.curCampaign.getMaxStartingLevel( );

        if ( minLevel == maxLevel )
        {
            postFix += " of Level " + minLevel + ")";
        }
        else
        {
            postFix += " of Levels " + minLevel + " to " + maxLevel + ")";
        }

        partySize = new Label( "(Select " + postFix );
        partySize.setTheme( "partysizelabel" );
        add( partySize );

        availableTitle = new Label( );
        availableTitle.setTheme( "availabletitlelabel" );
        add( availableTitle );

        showPregeneratedCharactersButton = new ToggleButton( );
        showPregeneratedCharactersButton.setTheme( "showpregeneratedcharactersbutton" );
        showPregeneratedCharactersButton.setActive( true );
        showPregeneratedCharactersButton.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                partyPaneContent.clear( );
                populateSelectableList( );
            }
        } );
        add( showPregeneratedCharactersButton );

        cancel = new Button( );
        cancel.setTheme( "cancelbutton" );
        cancel.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                PartyFormationWindow.this.mainMenu.removeChild( PartyFormationWindow.this );
                PartyFormationWindow.this.mainMenu.setButtonsVisible( true );
            }
        } );
        add( cancel );

        accept = new Button( );
        accept.setTheme( "acceptbutton" );
        accept.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                acceptParty( );
                PartyFormationWindow.this.mainMenu.removeChild( PartyFormationWindow.this );
                PartyFormationWindow.this.mainMenu.setButtonsVisible( true );
            }
        } );
        add( accept );

        partyPaneContent = new Content( );
        partyPane = new ScrollPane( partyPaneContent );
        partyPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
        partyPane.setTheme( "partypane" );
        add( partyPane );

        availablePaneContent = new Content( );
        availablePane = new ScrollPane( availablePaneContent );
        availablePane.setFixed( ScrollPane.Fixed.HORIZONTAL );
        availablePane.setTheme( "selectorpane" );
        add( availablePane );

        populateSelectableList( );

        setAcceptEnabled( );
    }

    /**
     * Removes the specified selector from the party pane if it is in the
     * party pane.  Removes the specified selector from the available pane if it is
     * in that pane.
     *
     * @param selector the selector to remove
     */

    public void removeSelector( CharacterSelector selector )
    {
        partyPaneContent.removeSelector( selector );
        availablePaneContent.removeSelector( selector );

        setAcceptEnabled( );
    }

    private List< PC > getPCsInDirectory( String directory )
    {
        List< PC > pcs = new ArrayList< PC >( );

        File directoryFile = new File( directory );

        for ( String idPath : directoryFile.list( ) )
        {
            if ( ! idPath.endsWith( ResourceType.JSON.getExtension( ) ) ) continue;

            File creatureFile = new File( directory + idPath );
            if ( ! creatureFile.isFile( ) ) continue;

            String creatureID = idPath.substring( 0, idPath.length( ) - 5 );

            PC pc = EntityManager.getPC( creatureID );
            // TODO verify PC is valid for campaign

            if ( pc.getTemplate( ).isPregenerated( ) && ! showPregeneratedCharactersButton.isActive( ) ) continue;

            pc.resetTime( );

            pcs.add( pc );
        }

        return pcs;
    }

    private class CreateCharacterSelector extends Widget implements Runnable
    {
        private Button newChar;

        private CreateCharacterSelector( )
        {
            newChar = new Button( );
            newChar.setTheme( "newcharacterbutton" );
            newChar.addCallback( this );
            add( newChar );
        }

        @Override
        protected void layout( )
        {
            newChar.setPosition( getInnerX( ), getInnerY( ) );
            newChar.setSize( newChar.getPreferredWidth( ), newChar.getPreferredHeight( ) );
        }

        @Override
        public void run( )
        {
            showCharacterEditor( );
        }
    }

    /**
     * Adds character selectors for each available unique character
     */

    private void populateSelectableList( )
    {
        availablePaneContent.clear( );

        availablePaneContent.add( new CreateCharacterSelector( ) );

        List< PC > pcs = new ArrayList< PC >( );
        pcs.addAll( getPCsInDirectory( "characters/" ) );
        pcs.addAll( getPCsInDirectory( Game.plataform.getCharactersDirectory( ) ) );

        List< UniqueCharacter > characters = new ArrayList< UniqueCharacter >( );

        for ( PC pc : pcs )
        {
            // check adding the creature to the existing unique characters
            boolean creatureAdded = false;

            for ( UniqueCharacter character : characters )
            {
                if ( character.addIfMatches( pc ) )
                {
                    creatureAdded = true;
                    break;
                }
            }

            // if the creature didn't match any of the existing ones, create a new one
            if ( ! creatureAdded )
            {
                UniqueCharacter uc = new UniqueCharacter( pc );

                characters.add( uc );
            }
        }

        // add one character selector for each unique character
        for ( UniqueCharacter character : characters )
        {
            CharacterSelector selector = new CharacterSelector( character, mainMenu, charactersInParties );
            selector.showDeleteButtons( );
            selector.setNewGameWindow( this );
            availablePaneContent.addSelector( selector, true, false );
        }
    }

    private void acceptParty( )
    {
        if ( newGameWindow != null )
        {
            int maxLevel = 0;
            int minLevel = Integer.MAX_VALUE;
            List< String > characterIDs = new ArrayList< String >( );
            for ( CharacterSelector selector : partyPaneContent.selectors )
            {
                characterIDs.add( selector.getCreatureID( ) );

                Creature creature = selector.getCreature( );

                maxLevel = Math.max( maxLevel, creature.roles.getTotalLevel( ) );
                minLevel = Math.min( minLevel, creature.roles.getTotalLevel( ) );
            }

            SavedParty party = new SavedParty( characterIDs, nameField.getText( ), minLevel, maxLevel, 0 );
            party.writeToFile( );

            newGameWindow.populatePartySelectors( party.getID( ) );
        }
        else
        {
            List< String > party = new ArrayList< String >( );
            for ( CharacterSelector selector : partyPaneContent.selectors )
            {
                party.add( selector.getCreatureID( ) );
            }

            Game.curCampaign.addParty( party, nameField.getText( ) );
            Game.curCampaign.party.setFirstMemberSelected( );
            Game.curCampaign.partyCurrency.setValue( 0 );
            Game.curCampaign.levelUpToMinIfAllowed( );
            Game.ruleset.getDifficultyManager( ).setCurrentDifficulty( difficultySelector.getSelectedDifficulty( ) );
            mainMenu.update( );
        }
    }

    /**
     * used to temporarily highlight a newly created character
     *
     * @param selector the selector to highlight
     */

    private void setHighlightedSelector( CharacterSelector selector )
    {
        clearHighlightedSelector( );

        selector.getAnimationState( ).setAnimationState( Button.STATE_SELECTED, true );
        this.highlightedSelector = selector;
    }

    private void clearHighlightedSelector( )
    {
        if ( highlightedSelector != null )
        {
            highlightedSelector.getAnimationState( ).setAnimationState( Button.STATE_SELECTED, false );
            highlightedSelector = null;
        }
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        smallGap = themeInfo.getParameter( "smallGap", 0 );
        sectionGap = themeInfo.getParameter( "sectionGap", 0 );
        selectableWidth = themeInfo.getParameter( "selectableWidth", 0 );

        nameField.setText( themeInfo.getParameter( "defaultpartyname", "" ) );
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        setSize( selectableWidth, getMaxHeight( ) );

        setPosition( ( Game.config.getResolutionX( ) - getWidth( ) ) / 2,
                     ( Game.config.getResolutionY( ) - getHeight( ) ) / 2 );

        int centerX = getInnerX( ) + getInnerWidth( ) / 2;

        cancel.setSize( cancel.getPreferredWidth( ), cancel.getPreferredHeight( ) );
        accept.setSize( accept.getPreferredWidth( ), accept.getPreferredHeight( ) );

        accept.setPosition( centerX - accept.getWidth( ) - smallGap,
                            getInnerBottom( ) - accept.getHeight( ) );

        cancel.setPosition( centerX + smallGap, getInnerBottom( ) - cancel.getHeight( ) );

        titleLabel.setSize( titleLabel.getPreferredWidth( ), titleLabel.getPreferredHeight( ) );
        titleLabel.setPosition( centerX - titleLabel.getWidth( ) / 2, getInnerY( ) );

        int paneBottom, nameBottom;
        if ( difficultySelector == null )
        {
            paneBottom = Math.min( cancel.getY( ), accept.getY( ) );

            nameLabel.setSize( nameLabel.getPreferredWidth( ), nameLabel.getPreferredHeight( ) );
            nameField.setSize( nameField.getPreferredWidth( ), nameField.getPreferredHeight( ) );

            int nameWidth = nameField.getWidth( ) + nameLabel.getWidth( ) + smallGap;
            int nameHeight = Math.max( nameField.getHeight( ), nameLabel.getHeight( ) );

            nameLabel.setPosition( centerX - nameWidth / 2,
                                   titleLabel.getBottom( ) + sectionGap + nameHeight / 2 - nameLabel.getHeight( ) / 2 );

            nameField.setPosition( nameLabel.getRight( ) + smallGap,
                                   titleLabel.getBottom( ) + sectionGap + nameHeight / 2 - nameField.getHeight( ) / 2 );

            nameBottom = Math.max( Math.max( nameLabel.getBottom( ), nameField.getBottom( ) ), titleLabel.getBottom( ) );
        }
        else
        {
            difficultySelector.setSize( difficultySelector.getPreferredWidth( ), difficultySelector.getPreferredHeight( ) );
            difficultySelector.setPosition( centerX - difficultySelector.getWidth( ) / 2,
                                            accept.getY( ) - sectionGap - difficultySelector.getHeight( ) );

            paneBottom = difficultySelector.getY( ) - sectionGap;

            nameBottom = titleLabel.getBottom( );
        }

        partyTitle.setSize( partyTitle.getPreferredWidth( ), partyTitle.getPreferredHeight( ) );
        availableTitle.setSize( availableTitle.getPreferredWidth( ), availableTitle.getPreferredHeight( ) );
        partyTitle.setPosition( getInnerX( ), nameBottom + sectionGap );
        availableTitle.setPosition( centerX, nameBottom + sectionGap );

        partySize.setSize( partySize.getPreferredWidth( ), partySize.getPreferredHeight( ) );
        partySize.setPosition( getInnerX( ), partyTitle.getBottom( ) );

        partyPane.setPosition( getInnerX( ), partySize.getBottom( ) );
        partyPane.setSize( getInnerWidth( ) / 2, paneBottom - partyPane.getY( ) );

        showPregeneratedCharactersButton.setSize( showPregeneratedCharactersButton.getPreferredWidth( ),
                                                  showPregeneratedCharactersButton.getPreferredHeight( ) );
        showPregeneratedCharactersButton.setPosition( getInnerRight( ) - showPregeneratedCharactersButton.getWidth( ),
                                                      paneBottom - showPregeneratedCharactersButton.getHeight( ) );

        availablePane.setPosition( partyPane.getRight( ), availableTitle.getBottom( ) );
        availablePane.setSize( getInnerWidth( ) - partyPane.getWidth( ),
                               showPregeneratedCharactersButton.getY( ) - availablePane.getY( ) );
    }

    /**
     * This will allow the user to create a new character from scratch
     * using the currently selected Campaign rules, and save the character.
     */

    private void showCharacterEditor( )
    {
        CharacterBuilder builder = new CharacterBuilder( new Buildable( ) );
        mainMenu.add( builder );
        builder.addFinishCallback( new CharacterBuilder.FinishCallback( )
        {
            @Override
            public void creatureModified( String id )
            {
                PC pc = EntityManager.getPC( id );

                CharacterSelector selector = new CharacterSelector( pc, mainMenu );
                availablePaneContent.addSelectorToTop( selector, true, false );

                // highlight and scroll to the newly created character
                setHighlightedSelector( selector );
                availablePane.setScrollPositionY( 0 );

                invalidateLayout( );
            }
        } );
    }

    private void setAcceptEnabled( )
    {
        int partySize = partyPaneContent.selectors.size( );
        int maxSize = Game.curCampaign.getMaxPartySize( );
        int minSize = Game.curCampaign.getMinPartySize( );

        // disable all add buttons if the selection has hit the max party size
        if ( partySize == maxSize )
        {
            for ( CharacterSelector selector : availablePaneContent.selectors )
            {
                selector.setAddRemoveEnabled( false );
            }
        }
        else
        {
            for ( CharacterSelector selector : availablePaneContent.selectors )
            {
                selector.setAddRemoveEnabled( true );
            }
        }

        boolean nameNotEntered = nameField.getTextLength( ) == 0;
        boolean partyTooLarge = partySize > maxSize;
        boolean partyTooSmall = partySize < minSize;

        if ( nameNotEntered )
        {
            accept.setTooltipContent( "You must enter a name for the party" );
            accept.setEnabled( false );
        }
        else if ( partyTooLarge )
        {
            accept.setTooltipContent( "There are too many members in the party" );
            accept.setEnabled( false );
        }
        else if ( partyTooSmall )
        {
            accept.setTooltipContent( "There are too few members in the party" );
            accept.setEnabled( false );
        }
        else
        {
            accept.setTooltipContent( null );
            accept.setEnabled( true );
        }
    }

    private class Content extends Widget
    {
        private List< CharacterSelector > selectors;
        private int selectorGap;

        private Content( )
        {
            selectors = new ArrayList< CharacterSelector >( );
        }

        private void addSelectorToTop( CharacterSelector selector, boolean addButton, boolean removeButton )
        {
            add( selector, true, addButton, removeButton );
        }

        private void addSelector( CharacterSelector selector, boolean addButton, boolean removeButton )
        {
            add( selector, false, addButton, removeButton );
        }

        private void add( CharacterSelector selector, boolean top, boolean addButton, boolean removeButton )
        {
            if ( addButton )
            {
                selector.setAddRemoveButton( "(+) Select", new AddCallback( selector ) );
            }
            else if ( removeButton )
            {
                selector.setAddRemoveButton( "(-) Remove", new RemoveCallback( selector ) );
            }

            if ( top ) { selectors.add( 0, selector ); }
            else { selectors.add( selector ); }

            add( selector );

            if ( top )
            {
                // move the just added widget to the top of the list
                // below the create character button
                this.moveChild( getNumChildren( ) - 1, 1 );
            }

            invalidateLayout( );
        }

        private void clear( )
        {
            selectors.clear( );
            removeAllChildren( );
        }

        private void removeSelector( CharacterSelector selector )
        {
            if ( ! selectors.contains( selector ) ) return;

            selectors.remove( selector );
            selector.setAddRemoveButton( null, null );
            removeChild( selector );
            invalidateLayout( );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            selectorGap = themeInfo.getParameter( "selectorGap", 0 );
        }

        @Override
        public int getPreferredHeight( )
        {
            int height = 0;
            for ( int i = 0; i < getNumChildren( ); i++ )
            {
                height += getChild( i ).getPreferredHeight( );
            }
            height += Math.max( ( getNumChildren( ) - 1 ), 0 ) * selectorGap;

            return height + getBorderVertical( );
        }

        @Override
        public int getPreferredWidth( )
        {
            int width = 0;
            for ( int i = 0; i < getNumChildren( ); i++ )
            {
                width = Math.max( width, getChild( i ).getPreferredWidth( ) );
            }

            return width + getBorderHorizontal( );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            int lastY = getInnerY( );
            for ( int i = 0; i < getNumChildren( ); i++ )
            {
                Widget child = getChild( i );
                child.setSize( child.getPreferredWidth( ), child.getPreferredHeight( ) );
                child.setPosition( getInnerX( ), lastY );

                lastY = child.getBottom( ) + selectorGap;
            }
        }
    }

    private class AddCallback implements Runnable
    {
        private CharacterSelector selector;

        private AddCallback( CharacterSelector selector )
        {
            this.selector = selector;
        }

        @Override
        public void run( )
        {
            availablePaneContent.removeSelector( selector );
            partyPaneContent.addSelector( selector, false, true );

            setAcceptEnabled( );

            clearHighlightedSelector( );
        }
    }

    private class RemoveCallback implements Runnable
    {
        private CharacterSelector selector;

        private RemoveCallback( CharacterSelector selector )
        {
            this.selector = selector;
        }

        @Override
        public void run( )
        {
            partyPaneContent.removeSelector( selector );
            availablePaneContent.addSelector( selector, true, false );

            setAcceptEnabled( );

            clearHighlightedSelector( );
        }
    }
}
