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

package main.java.hale.ability;

import java.util.HashSet;
import java.util.Set;

import javax.script.ScriptException;

import main.java.hale.Game;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.Saveable;
import main.java.hale.util.JSEngine;
import main.java.hale.util.Logger;

/**
 * The base class for any class wanting to have an associated script and
 * list of ScriptFunctionTypes that can be called
 *
 * @author Jared Stephen
 */

public class Scriptable implements Saveable
{
    private final String scriptLocation;
    private final String script;
    private final boolean inline;
    private final Set<ScriptFunctionType> scriptFunctions;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        if (inline) {
            data.put("scriptContents", script);
        }

        if (scriptLocation != null) {
            data.put("scriptLocation", scriptLocation);
        }

        return data;
    }

    /**
     * Creates a new AbstractScriptable with the specified script at the specified location.
     * <p>
     * This object is immutable
     * <p>
     * The script contents are evaluated to determine the list of Script
     * functions available
     *
     * @param script         the Script contents to be evaluated
     * @param scriptLocation the Resource Location of the script
     * @param scriptInline   if true then the specified script is treated as inline,
     *                       meaning that it is not assumed to be directly retrievable from the scriptLocation.
     *                       If this is false, then the scriptable assumes that the script can be retrieved
     *                       as a Resource from the specified location.
     */

    public Scriptable(String script, String scriptLocation, boolean scriptInline)
    {
        scriptFunctions = new HashSet<ScriptFunctionType>();

        this.script = script;
        this.scriptLocation = scriptLocation;
        this.inline = scriptInline;

        if (script != null) {
            JSEngine engine = Game.scriptEngineManager.getEngine();

            try {
                engine.eval(script);
            } catch (ScriptException e) {
                Logger.appendToErrorLog("Error preparsing script at " + scriptLocation, e);
            }

            for (ScriptFunctionType type : ScriptFunctionType.values()) {
                if (engine.hasFunction(type.toString())) {
                    scriptFunctions.add(type);
                }
            }

            engine.release();
        }
    }

    /**
     * Creates a new AbstractScriptable as a copy of the specified AbstractScriptable
     *
     * @param other the AbstractScriptable to copy
     */

    public Scriptable(Scriptable other)
    {
        this.script = other.script;
        this.scriptLocation = other.scriptLocation;
        this.inline = other.inline;

        this.scriptFunctions = new HashSet<ScriptFunctionType>(other.scriptFunctions);
    }

    /**
     * Returns the number of different ScriptFunctionTypes that this Effect's script
     * can execute.  If this Effect has no script, returns 0.
     *
     * @return the number of different ScriptFunctionTypes that this Effect's script
     * can execute.
     */

    public int getNumberOfScriptFunctionTypes()
    {
        return scriptFunctions.size();
    }

    /**
     * Returns true if the script associated with this Scriptable contains the
     * function type, false otherwise.  This function can only check for types
     * that are an instance of the ScriptFunctionType enum
     *
     * @param type the type of script to check for
     * @return whether this object contains the specified script type
     */

    public boolean hasFunction(String type)
    {
        ScriptFunctionType sft = ScriptFunctionType.valueOf(type);

        return scriptFunctions.contains(sft);
    }

    /**
     * Returns true if the Script associated with this AbstractScriptable contains the
     * function type, false otherwise
     *
     * @param type the function type to search for
     * @return whether this object contains the specified function type
     */

    public boolean hasFunction(ScriptFunctionType type)
    {
        return scriptFunctions.contains(type);
    }

    /**
     * Execute the function from this AbstractScriptable's script with the specified function type.
     * The first argument passed to the function will be Game.scriptInterface (see
     * {@link main.java.hale.ScriptInterface}.  After that, each argument specified will
     * be passed in turn
     *
     * @param type      the function type to execute
     * @param arguments optional arguments to pass to the function
     * @return the return value of the executed JavaScript function, or null if no
     * function was executed
     */

    public Object executeFunction(ScriptFunctionType type, Object... arguments)
    {
        if (hasFunction(type)) {
            return executeFunction(type.toString(), arguments);
        } else {
            return null;
        }
    }

    /**
     * Execute the function from this AbstractScriptable's script with the specified function name.
     * The first argument passed to the function will be Game.scriptInterface (see
     * {@link main.java.hale.ScriptInterface}.  After that, the second argument will be the supplied argument
     *
     * @param function the name of the function to be called
     * @param argument optional argument to pass to the function
     * @return the return value of the executed JavaScript function, or null if no
     * function was executed
     */

    public Object executeFunction(String function, Object argument)
    {
        Object[] args = new Object[1];
        args[0] = argument;

        return executeFunction(function, args);
    }

    /**
     * Execute the function from this AbstractScriptable's script with the specified function name.
     * The first argument passed to the function will be Game.scriptInterface (see
     * {@link main.java.hale.ScriptInterface}.  After that, the supplied arguments are passed
     *
     * @param function the name of the function to be called
     * @param arg1     optional first argument to pass to the function
     * @param arg2     optional second argument to pass to the function
     * @return the return value of the executed JavaScript function, or null if no
     * function was executed
     */

    public Object executeFunction(String function, Object arg1, Object arg2)
    {
        Object[] args = new Object[2];
        args[0] = arg1;
        args[1] = arg2;

        return executeFunction(function, args);
    }

    /**
     * Execute the function from this AbstractScriptable's script with the specified function name.
     * The first argument passed to the function will be Game.scriptInterface (see
     * {@link main.java.hale.ScriptInterface}.  After that, each argument specified will
     * be passed in turn
     *
     * @param function  the name of the function to be called
     * @param arguments optional arguments to pass to the function
     * @return the return value of the executed JavaScript function, or null if no
     * function was executed
     */

    public Object executeFunction(String function, Object... arguments)
    {
        Object returnValue = null;

        JSEngine engine = Game.scriptEngineManager.getEngine();

        try {
            // script has already been pre-parsed; eval should not return any errors
            engine.eval(script);
            returnValue = engine.invokeFunction(function, Scriptable.createArgumentList(arguments));
        } catch (ScriptException e) {
            Logger.appendToErrorLog("Error invoking function " + function +
                    " for script " + scriptLocation, e);
        } catch (NoSuchMethodException e) {
            Logger.appendToErrorLog("Error invoking function " + function +
                    " for script " + scriptLocation, e);
        }

        engine.release();

        return returnValue;
    }

    /*
     * Creates an argument list that will work with invokeFunction above with
     * Game.scriptInterface added to the front
     *
     * Just passing Game.scriptInterface then arguments to invokeFunction does
     * not work as the second argument to the javascript function then ends up
     * being an array
     */

    private static Object[] createArgumentList(Object... arguments)
    {
        Object[] args = new Object[arguments.length + 1];
        args[0] = Game.scriptInterface;
        int i = 1;
        for (Object obj : arguments) {
            args[i] = obj;
            i++;
        }
        return args;
    }

    /**
     * Returns the contents of the Script this Object executes
     *
     * @return the contents of the Script this Object executes
     */

    public String getScript()
    {
        return script;
    }

    /**
     * Returns the Script Resource location of the Script this Scriptable executes
     *
     * @return the Script Resource location of the Script this Scriptable executes
     */

    public String getScriptLocation()
    {
        return scriptLocation;
    }

    /**
     * Returns a new DelayedScriptCallback that will execute the specified
     * function from this AbstractScriptable's script.  The delay and any
     * callback arguments will need to be set for the DelayedScriptCallback
     * prior to starting it with the {@link DelayedScriptCallback#start()}
     * method.
     *
     * @param function the callback function for the DelayedScriptCallback
     * @return a new DelayedScriptCallback
     */

    public DelayedScriptCallback createDelayedCallback(String function)
    {
        return new DelayedScriptCallback(this, function);
    }
}
