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

package net.sf.hale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.hale.ability.AsyncScriptable;
import net.sf.hale.ability.ListTargeter;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.ability.Targeter;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Ammo;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.Encounter;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Weapon;
import net.sf.hale.icon.SimpleIcon;
import net.sf.hale.interfacelock.EntityAttackAnimation;
import net.sf.hale.interfacelock.InterfaceAILock;
import net.sf.hale.interfacelock.InterfaceCombatLock;
import net.sf.hale.interfacelock.InterfaceLock;
import net.sf.hale.interfacelock.InterfaceTargeterLock;
import net.sf.hale.interfacelock.MovementHandler;
import net.sf.hale.mainmenu.InGameMenu;
import net.sf.hale.particle.Animation;
import net.sf.hale.rules.Attack;
import net.sf.hale.rules.Faction;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;

/**
 * Class for handling the order and progression of combat, including calling AI scripts for
 * each NPC and creating callbacks to continue combat after each player character's turn
 *
 * @author Jared
 */

public class CombatRunner
{
    private final List< Creature > creatures = new ArrayList< Creature >( );
    private int activeCreatureIndex = - 1;

    private boolean combatModeInitiating = false;

    private int combatStartRound;

    /**
     * Advances combat to the next round - the next creature in the combat
     * queue will have its turn.  If the game is not already in combat mode,
     * combat is started and the first creature's turn will occur immediately
     * thereafter
     */

    public void nextCombatTurn( )
    {
        if ( combatModeInitiating )
        {
            // don't start combat unless AI has been activated
            if ( ! Game.isInTurnMode( ) )
            {
                Game.setTurnMode( true );

                startCombat( );
            }
        }

        runTurn( );
    }

    /**
     * Returns the list of creatures currently threatening the specified target.  This is
     * assumed to be threatening for a purpose other than movement.  To get the list of
     * threatening creatures for AoOs due to movement, use {@link #getThreateningCreaturesAtNextPosition(Creature)}
     * Returns an empty list if there are no threatening creatures.
     *
     * @param target the creature being threatening
     * @return the list of threatening creatures
     */

    public List< Creature > getThreateningCreatures( Creature target )
    {
        List< Creature > threatens = new ArrayList< Creature >( );

        if ( ! Game.isInTurnMode( ) ) return threatens;

        if ( target.stats.isHidden( ) ) return threatens;

        for ( Creature current : creatures )
        {
            if ( current == target ) continue;

            if ( current.isDead( ) || current.isDying( ) ) continue;

            if ( ! current.getFaction( ).isHostile( target ) ) continue;

            if ( ! current.threatensLocation( target.getLocation( ) ) ) continue;

            threatens.add( current );
        }

        return threatens;
    }

    /**
     * Returns the list of creatures currently threatening the specified target at their next position
     * assumed to be threatening for the purpose of movement
     *
     * @param target the creature being threatening
     * @return the list of threatening creatures
     */

    private List< Creature > getThreateningCreaturesAtNextPosition( Creature target )
    {
        List< Creature > creatures = new ArrayList< Creature >( );

        if ( ! Game.isInTurnMode( ) ) return creatures;

        if ( target.stats.isHidden( ) ) return creatures;

        for ( Creature current : this.creatures )
        {
            if ( current == target ) continue;

            if ( current.isDead( ) || current.isDying( ) ) continue;

            if ( ! current.getFaction( ).isHostile( target ) ) continue;

            if ( ! current.threatensLocation( target.getLocation( ) ) ) continue;

            if ( ! current.canTakeMoveAoOIgnoringLocation( target ) ) continue;

            creatures.add( current );
        }

        return creatures;
    }

    /**
     * Causes the specified target creature to provoke attacks of opportunity from all threatening
     * creatures
     *
     * @param target the target creature for the AoOs
     * @param mover  the mover that will be paused by any attacks of opportunity, if this is an
     *               attack of opportuntiy due to movement.  If it is not due to movement, this should be null
     * @return true if at least one attack of opportuntiy requiring a pause was provoked, false otherwise.
     * Only attacks of opportunity from player characters require a pause
     */

    public boolean provokeAttacksOfOpportunity( Creature target, MovementHandler.Mover mover )
    {
        boolean lockInterface = false;
        boolean alreadyScrolled = false;

        List< Creature > threateningCreatures;
        if ( mover == null ) { threateningCreatures = getThreateningCreatures( target ); }
        else { threateningCreatures = getThreateningCreaturesAtNextPosition( target ); }

        for ( Creature current : threateningCreatures )
        {
            if ( mover != null ) current.takeMoveAoO( target );

            Game.mainViewer.addMessage( "green", current.getTemplate( ).getName( ) + " gets an Attack of Opportunity against " +
                    target.getTemplate( ).getName( ) );

            TakeAoOCallback takeCallback = new TakeAoOCallback( current, target );

            if ( current instanceof PC )
            {
                lockInterface = true;

                // create a list targeter with no scriptable callback and no ability slot for current
                ListTargeter targeter = new ListTargeter( current, null, null );
                targeter.addAllowedPoint( target.getLocation( ) );
                targeter.setMenuTitle( "Attack of Opportunity" );
                targeter.setActivateCallback( takeCallback );

                // the targeter can only proceed if the target is alive when it is set
                targeter.setCheckValidCallback( new CheckAoOCallback( target ) );

                Game.areaListener.getTargeterManager( ).addTargeter( targeter );

                if ( mover != null )
                {
                    mover.incrementPauseCount( );
                    takeCallback.moverToUnPause = mover;

                    CancelAoOCallback cancelCallback = new CancelAoOCallback( );
                    cancelCallback.moverToUnPause = mover;
                    targeter.setCancelCallback( cancelCallback );
                }

                // scroll to the first creature with an AoO
                if ( Game.config.autoScrollDuringCombat( ) && ! alreadyScrolled )
                {
                    Game.areaViewer.addDelayedScrollToCreature( current );
                    alreadyScrolled = true;
                }

            }
            else if ( current.getTemplate( ).hasScript( ) )
            {
                if ( current.getTemplate( ).getScript( ).hasFunction( ScriptFunctionType.takeAttackOfOpportunity ) )
                {
                    Object executeAttack = current.getTemplate( ).getScript( ).executeFunction(
                            ScriptFunctionType.takeAttackOfOpportunity, current, target );

                    if ( Boolean.TRUE.equals( executeAttack ) )
                    {
                        takeCallback.takeAoO( );
                    }
                }
                else
                {
                    takeCallback.takeAoO( );
                }
            }
        }

        if ( lockInterface )
        {
            Game.interfaceLocker.add( new InterfaceTargeterLock( target ) );
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Initializes a combat.  AI active creatures will roll for initiative,
     * and then all creatures will be put into the combat queue.
     */

    private void startCombat( )
    {
        creatures.clear( );
        Game.timer.resetTime( );

        List< CreatureWithInitiative > creatureInitiatives = new ArrayList< CreatureWithInitiative >( );

        for ( Entity e : Game.curCampaign.curArea.getEntities( ) )
        {
            if ( ! ( e instanceof Creature ) ) continue;

            Creature c = ( Creature ) e;

            c.timer.endTurn( ); // disable all actions until c's turn comes up

            // roll initiative
            creatureInitiatives.add( new CreatureWithInitiative( c ) );
        }

        // add the creatures sorted in initiative order
        Collections.sort( creatureInitiatives );
        for ( CreatureWithInitiative cwi : creatureInitiatives )
        {
            creatures.add( cwi.creature );
        }

        Game.mainViewer.getPortraitArea( ).disableAllLevelUp( );
        Game.curCampaign.getDate( ).incrementRound( );

        activeCreatureIndex = - 1;

        combatModeInitiating = false;
    }

    /**
     * Checks the line of sight of all player characters.  If any character
     * has spotted a hostile creature, then combat mode will be initiated after
     * a short delay notifying the player
     *
     * @return true if a hostile was spotted and combat will be initiated, false otherwise
     */

    public boolean checkAIActivation( )
    {
        boolean aiActivated = false;

        for ( Creature c : Game.curCampaign.party )
        {
            aiActivated = aiActivated || checkAIActivation( c );
        }

        if ( aiActivated && ! combatModeInitiating )
        {
            combatModeInitiating = true;
            Game.mainViewer.addMessage( "link", "Hostile creature spotted.  Combat initiated." );
            Game.interfaceLocker.add( new InterfaceCombatLock( Game.curCampaign.party.getSelected( ),
                                                               Game.config.getCombatDelay( ) * 6 ) );
            combatStartRound = Game.curCampaign.getDate( ).getTotalRoundsElapsed( );
        }

        return aiActivated;
    }

    /**
     * Checks whether the given creature can see any creatures hostile to it.  Any hostiles
     * are added to the AI encounter of known enemies
     *
     * @param creature the creature to check
     * @return true if the creature can see one or more hostiles, false otherwise
     */

    private boolean checkAIActivation( Creature creature )
    {
        Encounter friendlyEncounter = creature.getEncounter( );
        List< Creature > friendlies = new ArrayList< Creature >( );
        friendlies.add( creature );
        if ( friendlyEncounter != null )
        {
            friendlyEncounter.setAIActive( true );

            for ( Creature c : friendlyEncounter )
            {
                friendlies.add( c );
            }
        }

        List< Creature > visibleHostiles = AreaUtil.getVisibleCreatures( creature, Faction.Relationship.Hostile );
        for ( Creature hostile : visibleHostiles )
        {
            if ( friendlyEncounter != null )
            {
                friendlyEncounter.addHostile( hostile );
            }

            if ( hostile.getEncounter( ) != null )
            {
                hostile.getEncounter( ).setAIActive( true );

                // don't add the friendly encounter if the party hasn't been spotted yet
                if ( ! creature.stats.isHidden( ) )
                {
                    hostile.getEncounter( ).addHostiles( friendlies );
                }

                if ( friendlyEncounter != null )
                {
                    friendlyEncounter.addHostiles( hostile.getEncounter( ).getCreaturesInArea( ) );
                }
            }
        }

        if ( ! Game.isInTurnMode( ) && visibleHostiles.size( ) > 0 )
        {
            return true;
        }

        return false;
    }


    /**
     * Causes the specified attack to complete.  If animate is false, then the attack is completed
     * immediately, otherwise it will be animated and completed mid animation
     *
     * @param attack  the main attack
     * @param offHand the off hand attack or null if there is no off hand attack
     * @param animate whether this attack should be animated
     * @return the callback which the caller can wait on for the attack to complete
     */

    private DelayedAttackCallback creatureAttack( Attack attack, Attack offHand, boolean animate )
    {
        if ( attack == null )
        {
            Logger.appendToErrorLog( "Error attempting to attack." );
            return null;
        }

        Creature attacker = attack.getAttacker( );
        Creature defender = attack.getDefender( );

        attack.computeFlankingBonus( Game.curCampaign.curArea.getEntities( ) );

        if ( animate )
        {
            DelayedAttackCallback cb;

            long delay = 0l;

            if ( attack.isRanged( ) )
            {
                if ( ! ( attacker instanceof PC ) && Game.config.autoScrollDuringCombat( ) )
                {
                    // for NPCs, scroll to half way between the attacker and defender for
                    // ranged attacks
                    Point aScreen = attacker.getLocation( ).getScreenPoint( );
                    Point dScreen = defender.getLocation( ).getScreenPoint( );
                    int avgX = ( aScreen.x + dScreen.x ) / 2;
                    int avgY = ( aScreen.y + dScreen.y ) / 2;

                    Game.areaViewer.addDelayedScrollToScreenPoint( new Point( avgX, avgY ) );
                }

                SimpleIcon icon = null;

                Weapon weapon = attack.getWeapon( );
                switch ( weapon.getTemplate( ).getWeaponType( ) )
                {
                    case Thrown:
                        icon = attack.getWeapon( ).getTemplate( ).getProjectileIcon( );
                        break;
                    default:
                        Ammo ammo = attacker.inventory.getEquippedQuiver( );

                        if ( ammo != null )
                        {
                            icon = ammo.getTemplate( ).getProjectileIcon( );
                        }
                        break;
                }

                if ( icon != null )
                {
                    // create the animation for ranged attacks
                    Animation animation = new Animation( icon );
                    animation.setAlpha( 1.0f );

                    Point start = attacker.getLocation( ).toPoint( );
                    Point end = defender.getLocation( ).toPoint( );

                    float distance = ( float ) start.screenDistance( end );
                    float angle = ( float ) start.angleTo( end );
                    float speed = 576.0f;

                    animation.setPosition( attacker.getLocation( ).getCenteredScreenPoint( ) );
                    animation.setVelocityMagnitudeAngle( speed, angle );
                    animation.setDuration( distance / speed );
                    animation.setRotation( angle * 180.0f / ( float ) Math.PI );

                    Game.particleManager.add( animation );

                    delay = ( long ) ( 1000.0 * distance / speed );
                }

            }
            else
            {
                //create the attacking animation for melee attacks
                EntityAttackAnimation animation = new EntityAttackAnimation( attacker, defender );
                attacker.setOffsetAnimation( animation );
                Game.particleManager.addEntityOffsetAnimation( animation );

                delay = Game.config.getCombatDelay( );
            }

            // create the callback to compute isHit and damage
            cb = new DelayedAttackCallback( delay, attack, offHand );
            cb.start( );

            InterfaceLock lock = new InterfaceLock( attacker, 2l * Game.config.getCombatDelay( ) );
            Game.interfaceLocker.add( lock );

            return cb;

        }
        else
        {
            // run the attack immediately in this thread
            DelayedAttackCallback cb = new DelayedAttackCallback( 0, attack, offHand );
            cb.run( );

            return cb;
        }
    }

    /**
     * Causes the specified attacker to perform a standard attack against the specified defender.
     * AP will be deducted from the attacker based on their stats.  If the attacker is wielding an
     * off hand weapon, then this attack will also include an off hand attack.  The attack will
     * be animated and completed asynchronously mid animation
     *
     * @param attacker
     * @param defender
     * @return the attack callback, which the caller can wait on for the attack to complete
     */

    public DelayedAttackCallback creatureStandardAttack( Creature attacker, Creature defender )
    {
        if ( defender == null || attacker == null ) return null;

        if ( ! attacker.canAttack( defender.getLocation( ) ) ) return null;

        if ( ! attacker.timer.canAttack( ) ) return null;

        Attack attack = attacker.performMainHandAttack( defender );
        Attack offHand = null;

        // create the off hand attack if the attacker is dual-wielding
        if ( attacker.getOffHandWeapon( ) != null )
        {
            offHand = attacker.performOffHandAttack( defender );
        }

        return creatureAttack( attack, offHand, true );
    }

    /**
     * Causes the specified attacker to perform an attack of opportunity against the specified defender.
     * No AP will be used, and only the main hand weapon will attack.  The attack will
     * be animated and completed asynchronously mid animation
     *
     * @param attacker
     * @param defender
     * @return the attack callback, which the caller can wait on for the attack to complete
     */

    private DelayedAttackCallback creatureAoOAttack( Creature attacker, Creature defender )
    {
        if ( defender == null ) return null;

        Attack attack = attacker.performSingleAttack( defender, Inventory.Slot.MainHand );

        return creatureAttack( attack, null, true );
    }

    /**
     * Causes the attacker to perform an attack against the specified defender, using the weapon
     * in the specified itemSlot, or the main weapon if the itemSlot specified is not a weapon or
     * is invalid.  No AP is subtracted from the attacker, and the attack is not animated and completes
     * immediately.
     *
     * @param attacker
     * @param defender
     * @param slot
     */

    public void creatureSingleAttack( Creature attacker, Creature defender, Inventory.Slot slot )
    {
        creatureAttack( attacker.performSingleAttack( defender, slot ), null, false );
    }

    /**
     * Causes the attacker to perform an attack against the specified defender, using the weapon
     * in the specified itemSlot, or the main weapon if the itemSlot specified is not a weapon or
     * is invalid.  No AP is subtracted from the attacker.  The attack will be animated, and
     * completed asynchronously mid-animation.
     *
     * @param attacker
     * @param defender
     * @param slot
     * @return the callback which the caller may wait on for the attack to complete
     */

    public DelayedAttackCallback creatureSingleAttackAnimate( Creature attacker, Creature defender, Inventory.Slot slot )
    {
        if ( defender == null ) return null;

        if ( ! attacker.threatensLocation( defender.getLocation( ) ) ) return null;

        return creatureAttack( attacker.performSingleAttack( defender, slot ), null, true );
    }

    /**
     * Causes the specified attacker to perform a touch attack against the specified defender.  The touch
     * attack may be either ranged or melee.  Touch attacks do not cost AP.  Melee touch attacks will be
     * animated, while ranged touch attacks are not.  Animated attacks will be done asynchronously.
     *
     * @param attacker
     * @param defender
     * @param ranged   true if the touch attack is ranged, false if it is melee
     * @return a delayed attack callback which can be waited on for the attack to complete
     */

    public DelayedAttackCallback creatureTouchAttack( Creature attacker, Creature defender, boolean ranged )
    {
        Attack attack = new Attack( attacker, defender, ranged );

        if ( ranged )
        { return creatureAttack( attack, null, false ); }
        else
        { return creatureAttack( attack, null, true ); }
    }

    /**
     * Checks whether combat should continue.  If any creature is not dead or dying and is hostile
     * to any other not dead or dying creature in the current combat, then combat should continue.
     *
     * @return true if combat should not continue, false otherwise
     */

    public boolean checkContinueCombat( )
    {
        List< Creature > combatants = new ArrayList< Creature >( );

        for ( Creature creature : creatures )
        {
            if ( creatureIsCombatant( creature ) )
            { combatants.add( creature ); }
        }

        for ( int i = 0; i < combatants.size( ); i++ )
        {
            for ( int j = 0; j < combatants.size( ); j++ )
            {
                if ( combatants.get( i ).getFaction( ).isHostile( combatants.get( j ) ) )
                { return true; }
            }
        }

        return false;
    }

    private boolean creatureIsCombatant( Creature creature )
    {
        if ( creature.isDead( ) || creature.isDying( ) ) return false;

        if ( creature.isPlayerFaction( ) || creature.isAIActive( ) )
        { return true; }
        else
        { return false; }
    }

    /**
     * Checks if combat should continue and runs the turn for the next creature in the combat queue
     * if it should.  If there are no hostiles left or the party is defeated, ends combat.  This method
     * will continue running in a loop for AI creatures.  When encountering a PC creature, the loop will
     * break to allow for player input.  The function will then be re-called after the PC turn.
     */

    private void runTurn( )
    {
        boolean continueCombat = checkContinueCombat( );

        while ( continueCombat && ! isPartyDefeated( ) )
        {
            Creature last = lastActiveCreature( );
            if ( last != null ) last.timer.endTurn( );

            Creature current = nextCreatureInQueue( );

            // if the current waited just reset, no cooldown decrease
            if ( ! current.timer.isWaitedOnce( ) )
            { current.elapseTime( 1 ); }
            else
            { current.timer.reset( ); }

            // dead, dying, or helpless creatures don't get a turn
            if ( current.isDead( ) || current.isDying( ) || current.stats.isHelpless( ) ) continue;

            Game.selectedEntity = current;
            if ( Game.config.autoScrollDuringCombat( ) )
            {
                Game.areaViewer.addDelayedScrollToCreature( current );
            }
            current.searchForHidingCreatures( );

            if ( current.isAIActive( ) )
            {
                checkAIActivation( current );
            }

            if ( current instanceof PC )
            {
                Game.curCampaign.party.setSelected( current );
                Game.curCampaign.curArea.getUtil( ).setPartyVisibility( );

                // allow the player to take their turn
                break;

            }
            else if ( current.getTemplate( ).hasScript( ) && current.isAIActive( ) )
            {
                // if current is an NPC with an AI
                try
                {
                    Scriptable ai = current.getTemplate( ).getScript( );

                    AsyncScriptable runner = new AsyncScriptable( ai );
                    // set delay prior to starting runner's turn
                    runner.setDelayMillis( Game.config.getCombatDelay( ) * 3 );
                    runner.executeAsync( ScriptFunctionType.runTurn, current );

                    Game.interfaceLocker.add( new InterfaceAILock( current, runner ) );

                }
                catch ( Exception e )
                {
                    Logger.appendToErrorLog( "Error running AI script " + current.getTemplate( ).getScript( ), e );
                }

                if ( continueCombat )
                {
                    // add a lock to run the next round unless combat is about to end
                    InterfaceCombatLock lock = new InterfaceCombatLock( current, Game.config.getCombatDelay( ) * 6 );
                    Game.interfaceLocker.add( lock );
                }
                break;
            }
        }

        if ( isPartyDefeated( ) )
        { setGameOver( ); }
        else if ( ! continueCombat )
        { exitCombat( ); }

        Game.mainViewer.updateInterface( );
    }

    /**
     * Changes the order of the combat queue.  The currently active creature is moved
     * the specified number of places forward, so that its turn will come up after that
     * number of creatures.  Only creatures who have not yet taken any actions, and who
     * are not dead, dying, or helpless may wait.
     *
     * @param activePlacesForward the number of queue slots forward to move the creature
     */

    public void activeCreatureWait( int activePlacesForward )
    {

        Creature activeCreature = creatures.get( activeCreatureIndex );

        // dead, dying, helpless creatures can't change their initiative
        if ( activeCreature.stats.isHelpless( ) || activeCreature.isDying( ) || activeCreature.isDead( ) )
        { return; }

        // First, figure out how many places forward we need to move
        // Inactive creatures don't count.
        int curIndex = activeCreatureIndex + 1;
        int placesForward = 0;
        int activePlacesLeft = activePlacesForward;
        while ( activePlacesLeft > 0 )
        {
            if ( curIndex >= creatures.size( ) ) curIndex = 0;

            if ( curIndex == activeCreatureIndex )
            {
                //you can't wait more than 1 turn
                return;
            }

            Creature c = creatures.get( curIndex );
            if ( ( ! c.isDead( ) && c.isAIActive( ) ) || c.isPlayerFaction( ) ) activePlacesLeft--;

            placesForward++;
            curIndex++;
        }

        // Now compute the new index
        int newIndex = activeCreatureIndex + placesForward;
        int newActiveCreatureIndex = activeCreatureIndex - 1;

        if ( newIndex >= creatures.size( ) )
        {
            newIndex -= ( creatures.size( ) - 1 );
            newActiveCreatureIndex = activeCreatureIndex;
        }

        // move the active creature to the new index
        creatures.remove( activeCreatureIndex );
        creatures.add( newIndex, activeCreature );
        activeCreature.timer.waitTurn( );

        activeCreatureIndex = newActiveCreatureIndex;
        nextCombatTurn( );

        Game.mainViewer.updateInterface( );
    }

    /**
     * Checks whether all PC party members are dead or dying
     *
     * @return true if all PC party members are dead or dying, false otherwise
     */

    private boolean isPartyDefeated( )
    {
        for ( Creature c : Game.curCampaign.party )
        {
            if ( ! c.isDead( ) && ! c.isDying( ) ) return false;
        }

        return true;
    }

    /**
     * Called whenever all player characters are either dead or dying during combat.  The player
     * is defeated and must load a game to continue.
     */

    private void setGameOver( )
    {
        Game.mainViewer.addMessage( "link", "You have been defeated." );
        Game.setTurnMode( false );

        Game.curCampaign.party.setDefeated( true );

        InGameMenu menu = new InGameMenu( Game.mainViewer );
        menu.openPopupCentered( );
    }

    /**
     * Exits the current combat.  Dying player characters are healed to 0 HP.  Level up
     * buttons are re-enabled.  Combat XP and currency is awarded, and the interface is
     * updated.
     */

    public void exitCombat( )
    {
        for ( Creature creature : creatures )
        {
            if ( creature.isDead( ) )
            {
                creature.abilities.cancelAllEffects( );
            }
        }

        if ( Game.isInTurnMode( ) )
        {
            Game.mainViewer.addMessage( "link", "Combat has ended." );
        }

        Game.setTurnMode( false );

        Game.mainViewer.getPortraitArea( ).enableAllLevelUp( );

        for ( Creature creature : Game.curCampaign.party )
        {
            if ( creature.isDying( ) && ! creature.isDead( ) )
            {
                int hp = - creature.getCurrentHitPoints( );
                creature.healDamage( hp );
            }

            creature.timer.reset( );
        }

        int combatLength = Game.curCampaign.getDate( ).getTotalRoundsElapsed( ) - combatStartRound;

        for ( Encounter encounter : Game.curCampaign.curArea.getEncounters( ) )
        {
            encounter.checkAwardXP( combatLength );
        }

        Game.selectedEntity = Game.curCampaign.party.getSelected( );

        Game.mainViewer.updateInterface( );
        Game.timer.resetTime( );
    }

    /**
     * Gets the next n creatures in the combat queue.  If n is
     * larger than the size of the queue, it will loop around, potentially
     * holding the same creature multiple times
     *
     * @param n the number of creatures to get
     * @return a list containing the next n creatures in the combat queue
     */

    public List< Creature > getNextCreatures( int n )
    {
        List< Creature > next = new ArrayList< Creature >( n );

        int curIndex = activeCreatureIndex;

        while ( next.size( ) < n )
        {
            Creature c = creatures.get( curIndex );

            if ( ! c.isDead( ) && ( c.isAIActive( ) || c.isPlayerFaction( ) ) )
            {
                next.add( creatures.get( curIndex ) );
            }

            curIndex++;
            if ( curIndex == creatures.size( ) ) curIndex = 0;
        }

        return next;
    }

    /**
     * inserts the specified creature into the combat queue.  This creature will
     * have its turn after the current creature's turn is completed.
     *
     * @param creature the creature to insert
     */

    public void insertCreature( Creature creature )
    {
        creatures.add( activeCreatureIndex + 1, creature );
    }

    /**
     * Returns the creature that had its combat turn previous to the current
     * creature
     *
     * @return the creature that had its combat turn previous to the current
     * creature
     */

    public Creature lastActiveCreature( )
    {
        if ( activeCreatureIndex == - 1 ) return null;

        return creatures.get( activeCreatureIndex );
    }

    /**
     * Returns the current active creature in the combat queue
     *
     * @return the current active creature
     */

    public Creature getActiveCreature( )
    {
        if ( activeCreatureIndex == - 1 ) { return null; }
        else { return creatures.get( activeCreatureIndex ); }
    }

    /**
     * Advances the combat queue to the next active creature.
     *
     * @return the creature that is next in the queue
     */

    private Creature nextCreatureInQueue( )
    {
        activeCreatureIndex++;

        if ( activeCreatureIndex == creatures.size( ) )
        {
            activeCreatureIndex = 0;

            Game.curCampaign.getDate( ).incrementRound( );
        }

        return creatures.get( activeCreatureIndex );
    }

    /**
     * A class for storing a creature and its associated initiative for this combat round.
     * Used in ordering the combat when it starts
     *
     * @author Jared
     */

    private class CreatureWithInitiative implements Comparable< CreatureWithInitiative >
    {
        private Creature creature;
        private int initiative;

        private CreatureWithInitiative( Creature creature )
        {
            this.creature = creature;
            this.initiative = creature.stats.get( Bonus.Type.Initiative ) + Game.dice.d100( );
        }

        @Override
        public int compareTo( CreatureWithInitiative other )
        {
            int c1Init = initiative;
            int c2Init = other.initiative;

            if ( c1Init > c2Init ) { return - 1; }
            else if ( c1Init < c2Init ) { return 1; }
            else
            {
                // initiatives are equal, choose based on bonus
                int c1Mod = creature.stats.get( Bonus.Type.Initiative );
                int c2Mod = other.creature.stats.get( Bonus.Type.Initiative );

                if ( c1Mod > c2Mod ) { return - 1; }
                else if ( c1Mod < c2Mod ) { return 1; }
                else
                {
                    // bonuses are equal, choose randomly
                    return Game.dice.d3( ) - 2;
                }
            }
        }
    }

    /**
     * Checks if an AoO Targeter is valid when it is set; the target must still
     * be alive
     */

    private class CheckAoOCallback implements Targeter.CheckValidCallback
    {
        private Creature target;

        private CheckAoOCallback( Creature target )
        {
            this.target = target;
        }

        @Override
        public boolean isValid( )
        {
            return ! target.isDead( );
        }
    }

    /**
     * A callback to cancel the current AoO and allow combat to continue
     */

    private class CancelAoOCallback implements Runnable
    {
        private MovementHandler.Mover moverToUnPause;

        @Override
        public void run( )
        {
            Game.areaListener.getTargeterManager( ).cancelCurrentTargeter( );
            Game.mainViewer.getMenu( ).hide( );

            if ( moverToUnPause != null )
            { moverToUnPause.decrementPauseCount( ); }
        }
    }

    /**
     * A callback to take the current AoO and then continue combat
     *
     * @author Jared
     */

    private class TakeAoOCallback implements Runnable
    {
        private Creature parent, target;
        private MovementHandler.Mover moverToUnPause;

        private TakeAoOCallback( Creature parent, Creature target )
        {
            this.parent = parent;
            this.target = target;
        }

        @Override
        public void run( )
        {
            Game.areaListener.getTargeterManager( ).endCurrentTargeter( );
            Game.mainViewer.getMenu( ).hide( );

            takeAoO( );

            if ( moverToUnPause != null )
            { moverToUnPause.decrementPauseCount( ); }
        }

        private void takeAoO( )
        {
            parent.takeAttackOfOpportunity( );
            DelayedAttackCallback cb = CombatRunner.this.creatureAoOAttack( parent, target );

            cb.addCallback( new CheckDefeatedAfterAoOCallback( ) );
        }
    }

    private class CheckDefeatedAfterAoOCallback implements Runnable
    {
        @Override
        public void run( )
        {
            if ( isPartyDefeated( ) )
            {
                setGameOver( );
            }
        }
    }
}
