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

package hale.ability;

import java.util.ArrayList;
import java.util.List;

import hale.util.Logger;

/**
 * A script callback that starts a new thread with a timer; running a
 * script function callback with a specified set of arguments after
 * a specified delay time.
 * <p>
 * Although this is called DelayedScriptCallback, a delay of 0 is supported.
 *
 * @author Jared Stephen
 */

public class DelayedScriptCallback extends Thread
{
    private final Scriptable scriptable;
    private long delayInMillis;
    private String callbackFunction;

    private final List<Object> arguments;

    /**
     * Create a new DelayedScriptCallback with the specified
     * AbstractScriptable which controls the script and script execution.
     *
     * @param scriptable the AbstractScriptable controlling the script
     * @param function   the callback function to execute
     */

    public DelayedScriptCallback(Scriptable scriptable, String function)
    {
        this.scriptable = scriptable;
        this.delayInMillis = 0l;
        this.callbackFunction = function;
        this.arguments = new ArrayList<Object>(5);
    }

    /**
     * Sets the delay after which the callback for this DelayedScriptCallback
     * will be executed.
     *
     * @param delayInSeconds the amount of delay in seconds
     */

    public void setDelay(float delayInSeconds)
    {
        this.delayInMillis = (long)(1000.0f * delayInSeconds);
    }

    /**
     * Adds the following argument which will be passed to the callback function
     * upon execution.  The ScriptInterface argument is always passed to callback
     * functions first, then all supplied arguments are passed, in order.
     *
     * @param arg the argument to pass to the callback
     */

    public void addArgument(Object arg)
    {
        arguments.add(arg);
    }

    /**
     * Adds the following arguments which will be passed to the callback function
     * upon execution.  The ScriptInterface argument is always passed to callback
     * functions first, then all supplied arguments are passed, in order.
     *
     * @param args the arguments to pass to the callback
     */

    public void addArguments(Object[] args)
    {
        for (Object arg : args) {
            arguments.add(arg);
        }
    }

    /**
     * Executes the specified callback function with the specified arguments
     * after the specified delay.  Should be called via the {@link #start()} method.
     */

    @Override
    public void run()
    {
        if (delayInMillis != 0l) {
            try {
                Thread.sleep(delayInMillis);
            } catch (InterruptedException e) {
                //thread was interrupted, can exit
                return;
            }
        }

        try {
            scriptable.executeFunction(callbackFunction, arguments.toArray());
        } catch (Exception e) {
            Logger.appendToErrorLog("Error while executing script " + scriptable.getScriptLocation() +
                    " function " + callbackFunction, e);
        }
    }
}
