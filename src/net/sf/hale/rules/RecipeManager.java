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

package net.sf.hale.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.entity.EquippableItemTemplate;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONParser;

/**
 * Class for managing the set of recipes available in a given campaign
 *
 * @author Jared Stephen
 */

public class RecipeManager
{
    private Map< String, Recipe > recipes;

    private Map< String, List< String > > recipesBySkill;

    private Map< EquippableItemTemplate.Type, List< String > > enchantmentsByItemType;

    /**
     * Creates a new RecipeManager containing no recipes
     */

    public RecipeManager( )
    {
        recipes = new HashMap< String, Recipe >( );
        recipesBySkill = new HashMap< String, List< String > >( );
        enchantmentsByItemType = new HashMap< EquippableItemTemplate.Type, List< String > >( );
    }

    /**
     * Returns the recipe contained in this RecipeManager with the specified ID.
     *
     * @param id the ID of the recipe to return
     * @return the recipe found with the specified ID, or null if no recipe can be located
     * with the ID
     */

    public Recipe getRecipe( String id )
    {
        return recipes.get( id );
    }

    /**
     * Loads all recipes contained in the standard resource location "recipes" into this
     * RecipeManager.
     */

    public void loadRecipes( )
    {
        recipes.clear( );
        recipesBySkill.clear( );

        Set< String > resources = ResourceManager.getResourcesInDirectory( "recipes" );
        for ( String resource : resources )
        {
            String id = ResourceManager.getResourceIDNoPath( resource, ResourceType.JSON );
            if ( id == null ) continue;

            try
            {
                Recipe recipe = new Recipe( id, new SimpleJSONParser( resource ) );
                recipes.put( id, recipe );

                List< String > recipesOfSkill = recipesBySkill.get( recipe.getSkill( ).getID( ) );
                // if the skill list does not exist, create it
                if ( recipesOfSkill == null )
                {
                    recipesOfSkill = new ArrayList< String >( );
                    recipesBySkill.put( recipe.getSkill( ).getID( ), recipesOfSkill );
                }

                recipesOfSkill.add( id );

                if ( recipe.isResultIngredient( ) )
                {
                    // add it to the list of enchantments by item type
                    for ( EquippableItemTemplate.Type type : recipe.getIngredientItemTypes( ) )
                    {
                        List< String > recipesOfItemType = enchantmentsByItemType.get( type );
                        if ( recipesOfItemType == null )
                        {
                            recipesOfItemType = new ArrayList< String >( );
                            enchantmentsByItemType.put( type, recipesOfItemType );
                        }

                        recipesOfItemType.add( id );
                    }
                }

            }
            catch ( Exception e )
            {
                Logger.appendToErrorLog( "Error loading recipe " + id, e );
            }
        }

        for ( String skillID : recipesBySkill.keySet( ) )
        {
            ( ( ArrayList< String > ) recipesBySkill.get( skillID ) ).trimToSize( );

            // sort recipes by skill requirement
            Collections.sort( recipesBySkill.get( skillID ), new Comparator< String >( )
            {
                @Override
                public int compare( String id1, String id2 )
                {
                    Recipe r1 = recipes.get( id1 );
                    Recipe r2 = recipes.get( id2 );
                    return r1.getSkillRankRequirement( ) - r2.getSkillRankRequirement( );
                }
            } );
        }

        for ( EquippableItemTemplate.Type type : enchantmentsByItemType.keySet( ) )
        {
            ( ( ArrayList< String > ) enchantmentsByItemType.get( type ) ).trimToSize( );
        }
    }

    /**
     * Returns a set containing all the recipe IDs currently registered with this
     * RecipeManager.  Note that the returned Set is unmodifiable
     *
     * @return a set containing all recipe IDs stored in this RecipeManager
     */

    public Set< String > getAllRecipeIDs( )
    {
        return Collections.unmodifiableSet( recipes.keySet( ) );
    }

    /**
     * Returns a List of all Recipe IDs in this Recipe Manager used via the specified skill.
     * Note that the returned list is unmodifiable.  If there are no such recipes, returns an
     * empty List
     *
     * @param skill the Skill that all returned recipe IDs use
     * @return the List of recipe IDs
     */

    public List< String > getRecipeIDsForSkill( Skill skill )
    {
        List< String > recipesOfSkill = recipesBySkill.get( skill.getID( ) );

        if ( recipesOfSkill == null ) { return Collections.emptyList( ); }
        else { return Collections.unmodifiableList( recipesOfSkill ); }
    }

    /**
     * Returns the list of all recipes that modify an existing item and can be applied to items of the
     * specified type
     *
     * @param type
     * @return the list of enchantment recipe IDs
     */

    public List< String > getEnchantmentsForItemType( EquippableItemTemplate.Type type )
    {
        List< String > enchantmentsOfType = enchantmentsByItemType.get( type );

        if ( enchantmentsOfType == null )
        {
            return Collections.emptyList( );
        }
        else
        {
            return Collections.unmodifiableList( enchantmentsOfType );
        }
    }
}
