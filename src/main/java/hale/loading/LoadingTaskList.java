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

package hale.loading;

import java.util.ArrayList;
import java.util.List;

import hale.util.Logger;

/**
 * A list of LoadingTasks.  Will execute each task in order, updating the
 * GUI as appropriate.  Should be run as a normal thread, with {@link #start()}
 *
 * @author Jared Stephen
 */

public class LoadingTaskList extends Thread
{
    private List<LoadingTask> tasks;
    private int totalWeight;

    private float completedFraction;
    private String currentDescription;

    private boolean success;

    /**
     * Creates a new, empty LoadingTaskList containing no LoadingTasks
     */

    public LoadingTaskList()
    {
        tasks = new ArrayList<LoadingTask>();
    }

    /**
     * Adds the specified task to the list of tasks to execute.
     *
     * @param task
     */

    public void addTask(LoadingTask task)
    {
        tasks.add(task);
        totalWeight += task.getWeight();

        task.initialize();
    }

    /**
     * Adds a LoadingTask with the specified parameters.  Convenience method.
     *
     * @param task        the Runnable to execute for the task
     * @param description the String description
     * @param weight      the weight for the task
     */

    public void addTask(Runnable task, String description, int weight)
    {
        addTask(new LoadingTask(task, description, weight));
    }

    /**
     * Adds a LoadingTask with the specified parameters and a default weight of 1.
     * Convenience method.
     *
     * @param task        the Runnable to execute for the task
     * @param description the String description
     */

    public void addTask(Runnable task, String description)
    {
        addTask(new LoadingTask(task, description));
    }

    /**
     * Returns the fraction of the tasks in this list that have been completed
     *
     * @return the fraction between 0.0f and 1.0f of the tasks in this list that have
     * been completed
     */

    public float getCompletedFraction()
    {
        return completedFraction;
    }

    /**
     * Returns the String description of the task currently being executed
     *
     * @return the description of the LoadingTask currently being executed
     */

    public String getCurrentDescription()
    {
        return currentDescription;
    }

    /**
     * Returns true if and only if this LoadingTaskList has finished all tasks and
     * completed successfully, without errors
     */

    public boolean isCompletedSuccessfully()
    {
        return success;
    }

    /**
     * Called when an error occurs in the process of executing the task list
     */

    protected void onError()
    {
    }

    @Override
    public void run()
    {
        try {
            int completedWeight = 0;

            for (LoadingTask task : tasks) {
                currentDescription = task.getDescription();

                while (task.hasNextTask()) {
                    completedWeight += task.executeNextTask();
                    completedFraction = ((float)completedWeight) / ((float)totalWeight);
                }
            }

            // sleep so the user sees the completed progress bar for a moment
            Thread.sleep(300);

            success = true;

        } catch (Exception e) {
            onError();
            Logger.appendToErrorLog("Exception while loading data", e);
        }
    }
}
