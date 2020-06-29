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

package main.java.hale.swingeditor;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.java.hale.entity.Creature;
import main.java.hale.entity.EntityManager;


/**
 * A mode specifying the types of assets currently being edited by a subeditor
 *
 * @author Jared
 */

public enum AssetType
{
    Creatures(new CreatureCallback(), "creatures");

    private String containingDirectory;
    private Callback callback;

    AssetType(Callback modeSetCallback, String containingDirectory)
    {
        this.callback = modeSetCallback;
        this.containingDirectory = containingDirectory;
    }

    /**
     * Creates and returns a new sub editor for the specified asset
     *
     * @param parent  the parent frame
     * @param assetID the ID of the asset to edit
     * @return a new sub editor for the asset
     */

    public JPanel createSubEditor(JFrame parent, String assetID)
    {
        return callback.getSubEditor(parent, assetID);
    }

    /**
     * Returns the asset associated with this asset type
     *
     * @param assetID the resource ID of the asset
     * @return the asset associated with this asset type
     */

    public Object getAsset(String assetID)
    {
        return callback.getAsset(assetID);
    }

    /**
     * Returns the directory containing this asset type
     *
     * @return the resource directory ID
     */

    public String getContainingDirectory()
    {
        return containingDirectory;
    }

    /**
     * Runs the callback for the specified editor to set this Mode
     *
     * @param editor the editor to set the mode for
     */

    public void setMode(AssetEditor editor)
    {
        callback.setMode(editor);
    }

    private static class CreatureCallback implements Callback
    {
        @Override
        public void setMode(AssetEditor editor)
        {
            editor.setMode(EditorManager.getCreaturesModel());
        }

        @Override
        public Creature getAsset(String assetID)
        {
            return EntityManager.getNPC(assetID);
        }

        @Override
        public JPanel getSubEditor(JFrame parent, String assetID)
        {
            return new CreatureSubEditor(parent, getAsset(assetID));
        }
    }

    private interface Callback
    {
        public void setMode(AssetEditor editor);

        public Object getAsset(String assetID);

        public JPanel getSubEditor(JFrame parent, String assetID);
    }
}
