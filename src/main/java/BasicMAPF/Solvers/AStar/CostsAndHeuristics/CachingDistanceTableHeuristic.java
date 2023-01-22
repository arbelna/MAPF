package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;

/**
 * A dynamic distance table, that will compute ad-hoc the distance table for any target on any map, and cache
 * results for subsequent uses. Must be set to the correct map using {@link #setCurrentMap(I_Map)} before querying
 * and when switching to another map!
 * IMPORTANT: Using this make run times unstable. The first time a new target on a new map is requested
 * is significantly more expensive than subsequent calls. Use with caution!
 */
public class CachingDistanceTableHeuristic implements AStarGAndH {
    private final int mapCacheSize;
    /**
     * Dictionary from {@link I_Map map} to a {@link DistanceTableAStarHeuristic distance table heuristic} for it.
     */
    private final Map<I_Map, DistanceTableAStarHeuristic> distanceTables;
    private I_Map currentMap;

    public CachingDistanceTableHeuristic() {
        this(1);
    }

    public CachingDistanceTableHeuristic(int mapCacheSize) {
        this.mapCacheSize = mapCacheSize;
        this.distanceTables = new LRUMap<>(mapCacheSize);
    }

    public void setCurrentMap(I_Map map){
        this.currentMap = map;
        if (!this.distanceTables.containsKey(map)) {
            this.distanceTables.put(map, new DistanceTableAStarHeuristic(new ArrayList<>(0), map));
        }
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        DistanceTableAStarHeuristic dt = this.distanceTables.get(this.currentMap);
        I_Location target = this.currentMap.getMapLocation(state.getMove().agent.target);
        if (dt.getDistanceDictionaries().containsKey(target)){
            return dt.getH(state);
        }
        else { // should only happen when encountering a new agent (target)
            dt.addTargetToHeuristic(target);
            return this.distanceTables.get(this.currentMap).getH(state);
        }
    }

    @Override
    public boolean isConsistent() {
        return true;
    }
}