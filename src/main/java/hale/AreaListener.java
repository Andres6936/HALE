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

package hale;

import hale.ability.ScriptFunctionType;
import hale.ability.Targeter;
import hale.area.Area;
import hale.defaultability.DefaultAbility;
import hale.defaultability.MouseActionList;
import hale.entity.Creature;
import hale.entity.Item;
import hale.entity.ItemList;
import hale.entity.Location;
import hale.entity.NPC;
import hale.entity.Entity;
import hale.entity.PC;
import hale.interfacelock.InterfaceCombatLock;
import hale.util.AreaUtil;
import hale.util.Point;
import hale.view.AreaViewer;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ThemeInfo;

public class AreaListener
{
    private int lastMouseX, lastMouseY;

    private Point curGridPoint;
    private MouseActionList.Condition curMouseCondition;

    private ThemeInfo themeInfo;

    private Area area;
    private final AreaViewer areaViewer;

    private final CombatRunner combatRunner;

    private boolean mouseDragStartValid;
    private Point mouseDragStart;

    private boolean mouseClickedWithoutDragging = true;

    private TargeterManager targeterManager;

    public AreaListener(Area area, AreaViewer areaViewer)
    {
        this.areaViewer = areaViewer;

        setArea(area);

        combatRunner = new CombatRunner();

        targeterManager = new TargeterManager();
        mouseDragStart = new Point();
    }

    public void setArea(Area area)
    {
        this.area = area;
    }

    public void setThemeInfo(ThemeInfo themeInfo)
    {
        this.themeInfo = themeInfo;
    }

    public CombatRunner getCombatRunner()
    {
        return combatRunner;
    }

    public AreaViewer getAreaViewer()
    {
        return areaViewer;
    }

    public void checkKillEntity(Entity entity)
    {
        if (!(entity instanceof Creature)) return;

        if (entity.getLocation().getArea() != area) return;

        Creature creature = (Creature)entity;

        // if creature has already been removed, don't attempt to remove it again
        if (!area.getEntities().containsEntity(creature)) return;

        if (creature.isSummoned() && creature.getCurrentHitPoints() <= 0) {
            boolean runNextCombatTurn = false;

            if (creature.isPlayerFaction()) {
                if (combatRunner.lastActiveCreature() == creature) {
                    runNextCombatTurn = true;
                }

                Game.curCampaign.party.removeSummon(creature);
            }

            Game.mainViewer.addMessage("red", creature.getTemplate().getName() + " is unsummoned.");
            area.getEntities().removeEntity(creature);

            if (runNextCombatTurn) {
                Game.interfaceLocker.add(new InterfaceCombatLock(creature, 500));
            }

            creature.getTemplate().getScript().executeFunction(ScriptFunctionType.onCreatureDeath, creature);
            creature.endAllAnimations();
            creature.getEffects().executeOnAllAuraChildren(ScriptFunctionType.onTargetExit);
            creature.getEffects().executeOnAll(ScriptFunctionType.onTargetExit, creature);

        } else
            if (!creature.isPlayerFaction() && creature.getCurrentHitPoints() <= 0) {
                Game.mainViewer.addMessage("red", creature.getTemplate().getName() + " is dead.");
                area.getEntities().removeEntity(creature);

                if (creature instanceof NPC) {
                    NPC npc = (NPC)creature;

                    ItemList loot = npc.getTemplate().generateLoot();

                    for (ItemList.Entry entry : loot) {
                        Item item = entry.createItem();
                        item.setLocation(entity.getLocation());

                        area.addItem(item, entry.getQuantity());
                    }
                }

                if (creature.getTemplate().getScript() != null) {
                    creature.getTemplate().getScript().executeFunction(ScriptFunctionType.onCreatureDeath, creature);
                }
                creature.endAllAnimations();
                creature.getEffects().executeOnAllAuraChildren(ScriptFunctionType.onTargetExit);
                creature.getEffects().executeOnAll(ScriptFunctionType.onTargetExit, creature);

            } else
                if (creature.isPlayerFaction() && creature.getCurrentHitPoints() <= 0) {
                    creature.timer.endTurn();
                }

        if (Game.isInTurnMode()) {
            if (!combatRunner.checkContinueCombat()) {
                combatRunner.exitCombat();
            }
        }
    }

    public void nextTurn()
    {
        area.getUtil().setPartyVisibility();

        areaViewer.mouseHoverValid = true;

        if (Game.isInTurnMode()) {
            combatRunner.nextCombatTurn();
        } else {
            Game.curCampaign.getDate().incrementRound();

            for (Entity entity : Game.curCampaign.curArea.getEntities()) {
                if (!(entity instanceof Creature)) continue;

                Creature creature = (Creature)entity;

                if (creature.isPlayerFaction() || creature.isAIActive()) {
                    creature.elapseTime(1);
                }
            }

            Game.selectedEntity = Game.curCampaign.party.getSelected();
            Game.mainViewer.updateInterface();
        }
    }

    public TargeterManager getTargeterManager()
    {
        return targeterManager;
    }

    public boolean handleEvent(Event evt)
    {
        if (evt.getType() == Event.Type.MOUSE_MOVED) {
            this.lastMouseX = evt.getMouseX();
            this.lastMouseY = evt.getMouseY();

            computeMouseState();
        }

        if (curGridPoint == null) return true;

        Location curLocation = new Location(area, curGridPoint.x, curGridPoint.y);

        switch (evt.getType()) {
            case MOUSE_BTNDOWN:
                mouseClickedWithoutDragging = true;

                if (!Game.curCampaign.party.isCurrentlyMoving() && evt.getMouseButton() == Event.MOUSE_RBUTTON) {
                    if (targeterManager.isInTargetMode()) {
                        targeterManager.getCurrentTargeter().showMenu(evt.getMouseX() - 2, evt.getMouseY() - 25);
                    } else
                        if (Game.interfaceLocker.locked()) {
                            // Do nothing
                        } else {
                            Game.mouseActions.showDefaultAbilitiesMenu(Game.curCampaign.party.getSelected(),
                                    curLocation, evt.getMouseX() - 2, evt.getMouseY() - 25);
                        }
                }
                break;
            case MOUSE_BTNUP:
                if (evt.getMouseButton() != Event.MOUSE_LBUTTON) break;


                if (mouseDragStartValid) {
                    mouseDragStartValid = false;
                } else
                    if (Game.curCampaign.party.isCurrentlyMoving() && mouseClickedWithoutDragging) {
                        Game.mainViewer.getMainPane().cancelAllOrders();
                    } else
                        if (targeterManager.isInTargetMode() && mouseClickedWithoutDragging) {
                            targeterManager.getCurrentTargeter().performLeftClickAction();
                        } else
                            if (curMouseCondition != null && mouseClickedWithoutDragging) {
                                DefaultAbility ability = curMouseCondition.getAbility();
                                if (ability != null) {
                                    PC parent = Game.curCampaign.party.getSelected();
                                    if (ability.canActivate(parent, curLocation)) ability.activate(parent, curLocation);
                                }

                            }
                break;
            case MOUSE_DRAGGED:
                mouseClickedWithoutDragging = false;

                if (mouseDragStartValid) {
                    mouseDragStartValid = false;
                    areaViewer.scroll(-2 * (evt.getMouseX() - mouseDragStart.x), -2 * (evt.getMouseY() - mouseDragStart.y));
                } else {
                    mouseDragStart.x = evt.getMouseX();
                    mouseDragStart.y = evt.getMouseY();
                    mouseDragStartValid = true;
                }
                break;
            default:
        }

        return true;
    }

    public void computeMouseState()
    {
        int xOffset = lastMouseX + areaViewer.getScrollX() - areaViewer.getX();
        int yOffset = lastMouseY + areaViewer.getScrollY() - areaViewer.getY();

        // compute grid point of mouse and limit it to area coordinates
        curGridPoint = AreaUtil.convertScreenToGrid(xOffset, yOffset);

        if (curGridPoint.x < 0) {
            curGridPoint.x = 0;
        } else
            if (curGridPoint.x >= area.getWidth()) curGridPoint.x = area.getWidth() - 1;

        if (curGridPoint.y < 0) {
            curGridPoint.y = 0;
        } else
            if (curGridPoint.y >= area.getHeight()) curGridPoint.y = area.getHeight() - 1;

        Game.mainViewer.getMouseOver().setPoint(curGridPoint);

        areaViewer.mouseHoverTile.x = curGridPoint.x;
        areaViewer.mouseHoverTile.y = curGridPoint.y;

        if (targeterManager.isInTargetMode()) {
            Targeter targeter = targeterManager.getCurrentTargeter();

            if (targeter.getParent() instanceof PC) {
                targeter.setMousePosition(xOffset, yOffset, curGridPoint);

                areaViewer.mouseHoverValid = targeter.mouseHoverValid();
                curMouseCondition = targeter.getMouseActionCondition();

                targeter.setTitleText();
            }

        } else
            if (Game.interfaceLocker.locked()) {
                curMouseCondition = MouseActionList.Condition.Cancel;
                areaViewer.mouseHoverValid = false;

                Game.mainViewer.clearTargetTitleText();
            } else {
                Location curLocation = new Location(area, curGridPoint.x, curGridPoint.y);

                curMouseCondition = Game.mouseActions.getDefaultMouseCondition(Game.curCampaign.party.getSelected(),
                        curLocation);

                areaViewer.mouseHoverValid = curMouseCondition != MouseActionList.Condition.Cancel;

                Game.mainViewer.clearTargetTitleText();
            }

        String cursor = Game.mouseActions.getMouseCursor(curMouseCondition);
        areaViewer.setMouseCursor(themeInfo.getMouseCursor(cursor));
    }

    public int getMouseGUIX()
    {
        return lastMouseX;
    }

    public int getMouseGUIY()
    {
        return lastMouseY;
    }

    public int getLastMouseX()
    {
        return lastMouseX + areaViewer.getScrollX() - areaViewer.getX();
    }

    public int getLastMouseY()
    {
        return lastMouseY + areaViewer.getScrollY() - areaViewer.getY();
    }
}