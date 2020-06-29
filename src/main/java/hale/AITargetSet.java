package main.java.hale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import main.java.hale.entity.Creature;
import main.java.hale.entity.Path;
import main.java.hale.util.AreaUtil;

/**
 * A class for holding a set of possible targets for the AI to attack.
 *
 * @author Jared
 */

public class AITargetSet
{

    private final List<TargetInfo> targets;

    /**
     * Creates a new AITargetSet
     *
     * @param parent          the attacking creature
     * @param possibleTargets the list of possible targets for the attacker.  Only some
     *                        (or none) of these could end up in the final set
     */

    public AITargetSet(Creature parent, List<Creature> possibleTargets)
    {
        targets = new ArrayList<TargetInfo>();

        int reach = parent.getMainHandWeapon().getTemplate().getMaxRange();

        for (Creature target : possibleTargets) {
            int distance;
            Path path = null;
            if (parent.getMainHandWeapon().isMelee()) {
                // for melee first check the weapon reach
                distance = AreaUtil.distance(parent.getLocation().getX(), parent.getLocation().getY(),
                        target.getLocation().getX(), target.getLocation().getY());

                // if the creature is not in melee range, compute the distance by finding a path
                if (distance > reach) {
                    path = parent.getLocation().getArea().getUtil().findShortestPath(parent,
                            target.getLocation().toPoint(), reach);

                    // distance is path length + 1
                    if (path != null) {
                        distance = path.length() + 1;
                    } else {
                        distance = Integer.MAX_VALUE;
                    }
                }
            } else {
                // for non melee, compute using shortest path to the target's tile
                path = parent.getLocation().getArea().getUtil().findShortestPathIgnoreCreatures(parent,
                        target.getLocation().toPoint());

                // distance is path length + 1
                if (path != null) {
                    distance = path.length() + 1;
                } else {
                    distance = Integer.MAX_VALUE;
                }
            }

            if (distance < Integer.MAX_VALUE) {
                if (path == null) {
                    path = new Path(target.getLocation().getArea());
                }

                TargetInfo targetInfo = new TargetInfo(target, distance, path);
                targets.add(targetInfo);
            }
        }
    }

    /**
     * @param index
     * @return return the path for the target at the specified index.
     * if the target is already in melee range, the path will be empty
     */

    public Path getPath(int index)
    {
        return targets.get(index).path;
    }

    /**
     * @param index
     * @return the target at the specified index
     */

    public Creature getTarget(int index)
    {
        return targets.get(index).target;
    }

    /**
     * Returns the distance from the parent to the target in a path finding sense
     *
     * @param index
     * @return the distance of the target at the specified index to the attacker
     */

    public int getDistance(int index)
    {
        return targets.get(index).distance;
    }

    /**
     * Returns the number of targets in this set
     *
     * @return the number of targets
     */

    public int size()
    {
        return targets.size();
    }

    /**
     * Sorts the list of targets in this set with the ones with fewest AoOs
     * to move to first
     */

    public void sortFewestAoOsFirst()
    {
        Collections.sort(targets, new AoOSorter());
    }

    /**
     * Sorts the list of targets in this set with the ones closest to the parent
     * first
     */

    public void sortClosestFirst()
    {
        Collections.sort(targets, new DistanceSorter());
    }

    private class DistanceSorter implements Comparator<TargetInfo>
    {
        @Override
        public int compare(TargetInfo a, TargetInfo b)
        {
            return a.distance - b.distance;
        }
    }

    private class AoOSorter implements Comparator<TargetInfo>
    {
        @Override
        public int compare(TargetInfo a, TargetInfo b)
        {
            return a.path.getNumAoOs() - b.path.getNumAoOs();
        }
    }

    private class TargetInfo
    {
        private final Creature target;
        private final int distance;
        private final Path path;

        private TargetInfo(Creature target, int distance, Path path)
        {
            this.target = target;
            this.distance = distance;
            this.path = path;
        }
    }
}
