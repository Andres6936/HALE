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

package main.java.hale.util;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * A wrapper for the Rhino JavaScript Engine.  Contains easier to use
 * versions of the functions needed in other classes.
 * <p>
 * JSEngines are managed by the {@link JSEngineManager}.  The
 * JSEngineManager will create new JSEngines as needed if
 * multiple scripts need to be executed concurrently in separate threads.
 *
 * @author Jared Stephen
 */

public class JSEngine
{
    private boolean inUse;
    private final ScriptEngine engine;

    /**
     * Create a new JSEngine by obtaining a JavaScript engine
     * from the specified ScriptEngineManager
     *
     * @param manager the ScriptEngineManager controlling the
     *                JavaScript engine(s)
     */

    public JSEngine(ScriptEngineManager manager)
    {
        this.engine = manager.getEngineByName("JavaScript");
    }

    /**
     * Locks or unlocks this JSEngine; the ScriptEngineManager will
     * not supply this JSEngine to any other thread if it is locked.
     *
     * @param inUse
     */

    protected synchronized void setInUse(boolean inUse)
    {
        this.inUse = inUse;
    }

    /**
     * Returns true if this JSEngine is locked and in use, false otherwise
     *
     * @return true if this JSEngine is locked and in use, false otherwise
     */

    protected synchronized boolean inUse()
    {
        return inUse;
    }

    /**
     * Adds the specified key / value pair to the set of global objects
     * accessible to the scripting environment
     *
     * @param key   the string that will be used to reference the value
     *              in scripts executed by this JSEngine
     * @param value the value to be added as a global variable to the scripting enviroment
     */

    public void put(String key, Object value)
    {
        engine.put(key, value);
    }

    /**
     * Evaluates the specified String as JavaScript code.  All code at the top
     * level of the source script is evaluated, code in methods is parsed.
     * <p>
     * This method needs to be called prior to {@link #invokeFunction(String, Object...)}
     * or {@link #hasFunction(String)}
     *
     * @param script the JavaScript code to evaluate
     * @return the last object referenced by the evaluated code
     * @throws ScriptException if an error or errors occur in the script
     */

    public Object eval(String script) throws ScriptException
    {
        if (!inUse) throw new IllegalStateException("Script engine was already released.");

        return engine.eval(script);
    }

    /**
     * Invokes the specified function with the specified arguments within the script state
     * that has previously been created through the {@link #eval(String)} method.
     *
     * @param function the name of the function to be called
     * @param args     a variable length list of arguments to be passed to the function
     * @return the return value of the function that is executed
     * @throws ScriptException       if an error occurs in executing the function
     * @throws NoSuchMethodException if the function specified cannot be found
     */

    public Object invokeFunction(String function, Object... args) throws ScriptException, NoSuchMethodException
    {
        if (!inUse) throw new IllegalStateException("Script engine was already released.");

        return ((Invocable)engine).invokeFunction(function, args);
    }

    /**
     * Checks the current list of engine bindings for the function with the specified name.
     * <p>
     * If a variable or function with the name exists, returns true, otherwise, returns false.
     * <p>
     * Note that although this method is called "hasFunction", a top level variable (not
     * function) with the specified name will also cause this method to return true
     *
     * @param function the name of the function to search for
     * @return true if a function of the specified name is found, false otherwise
     */

    public boolean hasFunction(String function)
    {
        if (!inUse) throw new IllegalStateException("Script engine was already released.");

        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        return bindings.containsKey(function);
    }

    /**
     * Release use of this ScriptEngine.  After this method is called, no further calls
     * to {@link #eval(String)} or {@link #invokeFunction(String, Object...)} can be performed
     * until the JSEngine is again set to be in use.
     * <p>
     * After released, the JSEngine can now be leased out by the ScriptEngineManager
     */

    public synchronized void release()
    {
        this.inUse = false;

        engine.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
    }

    @Override
    public String toString()
    {
        return engine.toString() + ": " + inUse;
    }
}
