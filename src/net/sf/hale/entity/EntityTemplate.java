/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.entity;

import net.sf.hale.ability.Scriptable;
import net.sf.hale.icon.Icon;
import net.sf.hale.icon.IconFactory;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.util.SimpleJSONObject;

/**
 * The class containing immutable parts of an entity.  This is used to create
 * unique instances of entities
 *
 * @author Jared
 */

public abstract class EntityTemplate
{
    // the unique, global entity ID
    private final String id;

    private final Scriptable conversation;

    // the Icon that is used to draw this entity
    private final Icon icon;

    private final String name;
    private final String description;

    private final Scriptable script;

    /**
     * Creates an EntityTemplate with the specified ID and name.  All other fields
     * are set to default (null) values
     *
     * @param id
     * @param name
     * @param icon the icon to use for both UI and Area Icons
     */

    public EntityTemplate( String id, String name, Icon icon )
    {
        this.id = id;
        this.name = name;

        this.conversation = null;
        this.script = null;
        this.description = null;
        this.icon = icon;
    }

    /**
     * Creates an EntityTemplate with the specified ID using the data in the
     * specified JSON parser
     *
     * @param id   the entity ID
     * @param data the JSONObject containing the data to be parsed
     */

    public EntityTemplate( String id, SimpleJSONObject data )
    {
        this.id = id;

        this.name = data.get( "name", null );

        if ( data.containsKey( "description" ) )
        { this.description = data.get( "description", null ); }
        else
        { this.description = null; }

        // read in the script if it exists
        String scriptID = data.get( "script", null );

        if ( scriptID == null )
        {
            this.script = null;
        }
        else
        {
            String scriptContents = ResourceManager.getScriptResourceAsString( scriptID );

            this.script = new Scriptable( scriptContents, scriptID, false );
        }

        // read in the conversation script if it exists
        String convoID = null;
        if ( data.containsKey( "conversation" ) )
        { convoID = data.get( "conversation", null ); }

        if ( convoID == null )
        {
            conversation = null;
        }
        else
        {
            String scriptContents = ResourceManager.getScriptResourceAsString( convoID );

            conversation = new Scriptable( scriptContents, convoID, false );
        }

        // read in the global icon if it exists
        if ( data.containsKey( "icon" ) )
        {
            icon = IconFactory.createIcon( data.getObject( "icon" ) );
        }
        else
        {
            icon = IconFactory.emptyIcon;
        }
    }

    /**
     * Creates an EntityTemplate which is a copy of the specified template, except for
     * the specified ID and createdItem properties
     *
     * @param id          the ID of the new template
     * @param other       the template to copy
     * @param createdItem
     */

    protected EntityTemplate( String id, EntityTemplate other, CreatedItem createdItem )
    {
        this.id = id;
        this.conversation = other.conversation;
        this.icon = createdItem.getModifiedIcon( other.icon );
        this.name = createdItem.getModifiedName( other.name );
        this.description = other.description;
        this.script = other.script;
    }

    /**
     * Creates an Entity based on this template.  The Entity will be of the correct class
     * based on the class of this template
     *
     * @return an Entity based on this template.
     */

    public abstract Entity createInstance( );

    /**
     * Returns the unique, global ID for this entity
     *
     * @return the unique ID
     */

    public String getID( )
    {
        return id;
    }

    /**
     * Returns the name of this Entity
     *
     * @return the name
     */

    public String getName( )
    {
        return name;
    }

    /**
     * Returns the description string for this Entity
     *
     * @return the description
     */

    public String getDescription( )
    {
        return description;
    }

    /**
     * Returns the Icon for this entity template.
     *
     * @return the UI icon
     */

    public Icon getIcon( )
    {
        return icon;
    }

    /**
     * Returns the conversation script for this entity, or null if none is defined
     *
     * @return the conversation script
     */

    public Scriptable getConversation( )
    {
        return conversation;
    }

    /**
     * Returns true if this EntityTemplate has an associated script, false otherwise
     *
     * @return whether this Entity has a script
     */

    public boolean hasScript( )
    {
        return script != null;
    }

    /**
     * Returns the general script for this entity.  This functions as the AI script
     * for creatures and general scripting for items
     *
     * @return the script
     */

    public Scriptable getScript( )
    {
        return script;
    }
}
