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

package main.java.hale.loading;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A single task that is performed in the process of loading.  Each task has a
 * different weight and can contain sub tasks.
 *
 * @author Jared Stephen
 */

public class LoadingTask
{
    private String description;

    private int taskWeight;
    private Runnable task;

    private Iterator<SubTask> subTasksIter;
    private List<SubTask> subTasks;

    /**
     * Creates a new LoadingTask with the default weight of 1 and the specified description
     *
     * @param task        the Runnable that will execute the task when run()
     * @param description a string representing what this task is accomplishing
     */

    public LoadingTask(Runnable task, String description)
    {
        this.task = task;
        this.description = description;
        this.taskWeight = 1;

        this.subTasks = new ArrayList<SubTask>();
    }

    /**
     * Creates a new LoadingTask with the specified weight and description
     *
     * @param task        the Runnable that will execute the task when run()
     * @param description a string representing what this task is accomplishing
     * @param weight      the weight for this LoadingTask (not including any subtasks)
     */

    public LoadingTask(Runnable task, String description, int weight)
    {
        this.task = task;
        this.description = description;
        this.taskWeight = weight;

        this.subTasks = new ArrayList<SubTask>();
    }

    /**
     * Adds a sub task to this LoadingTask with the specified weight
     *
     * @param task   the subTask to execute
     * @param weight the weight of the subTask
     */

    public void addSubTask(Runnable task, int weight)
    {
        SubTask subTask = new SubTask();
        subTask.task = task;
        subTask.weight = weight;

        this.subTasks.add(subTask);
        taskWeight += subTask.weight;
    }

    /**
     * Returns the weight for this LoadingTask.
     *
     * @return the weight for this LoadingTask
     */

    public int getWeight()
    {
        return taskWeight;
    }

    /**
     * Returns the description for this LoadingTask
     *
     * @return the description for this LoadingTask
     */

    public String getDescription()
    {
        return description;
    }

    /**
     * Initializes this LoadingTask for execution.  Must be called prior
     * to {@link #executeNextTask()}
     */

    public void initialize()
    {
        subTasksIter = subTasks.iterator();
    }

    /**
     * Executes the next task for this LoadingTask, if there are
     * any tasks left.  If there are no subtasks, there will only
     * be one task, the main task
     *
     * @return the amount of weight for the just completed task
     */

    public int executeNextTask()
    {
        if (task != null) {
            task.run();
            task = null;
            return taskWeight;
        } else
            if (subTasksIter.hasNext()) {
                SubTask subTask = subTasksIter.next();
                subTask.task.run();
                return subTask.weight;
            } else {
                throw new NoSuchElementException();
            }
    }

    /**
     * Returns true if this LoadingTask has another task to execute,
     * false otherwise
     *
     * @return true if and only if this LoadingTask has another task
     * to execute
     */

    public boolean hasNextTask()
    {
        return task != null || subTasksIter.hasNext();
    }

    private class SubTask
    {
        private Runnable task;
        private int weight;
    }
}
