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

package hale.util;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineManager;

/**
 * A class for managing a pool of available JavaScript interpreters
 *
 * @author Jared
 */

public class JSEngineManager
{
    private final ScriptEngineManager manager;

    private final List<JSEngine> engines;

    /**
     * Creates a new empty manager
     */

    public JSEngineManager()
    {
        this.manager = new ScriptEngineManager();
        this.engines = new ArrayList<>();
    }

    /**
     * Gets an available javascript engine.  If one is not available, a new one is
     * created
     *
     * @return the available JavaScript engine
     */

    public synchronized JSEngine getEngine()
    {
        for (JSEngine engine : engines) {
            if (!engine.inUse()) {
                engine.setInUse(true);
                return engine;
            }
        }

        JSEngine engine = new JSEngine(manager);
        engine.setInUse(true);
        engines.add(engine);
        return engine;
    }

    /**
     * Gets a javascript engine but does not add it to the list of available engines
     * The returned engine will thus never be released and added back to the engine pool
     *
     * @return a newly created javascript engine
     */

    public JSEngine getPermanentEngine()
    {
        JSEngine engine = new JSEngine(manager);
        engine.setInUse(true);
        return engine;
    }
}
