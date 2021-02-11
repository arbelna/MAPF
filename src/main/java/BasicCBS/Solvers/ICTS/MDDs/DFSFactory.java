package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

public class DFSFactory implements I_MDDSearcherFactory {

    private boolean disappearAtGoal = false;

    @Override
    public void setDefaultDisappearAtGoal(boolean disappearAtGoal) {
        this.disappearAtGoal = disappearAtGoal;
    }

    @Override
    public A_MDDSearcher createSearcher(ICTS_Solver highLevelSolver, I_Location source, I_Location target, Agent agent, DistanceTableAStarHeuristicICTS heuristic) {
        return new DFSMDDBuilder(highLevelSolver, source, target, agent, heuristic, disappearAtGoal);
    }

}
