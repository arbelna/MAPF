package BasicCBS.Solvers.ICTS.MergedMDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.ICTS.MDDs.MDD;
import BasicCBS.Solvers.Solution;

import java.util.Map;

public interface I_MergedMDDSolver {
    /**
     * Looks for a joint solution of all agents, restricted to each agent moving only on its MDD.
     * @param agentMDDs MDD for each agent
     * @param highLevelSolver The high level solver (for timeout check)
     * @return a joint solution of all agents, restricted to each agent moving only on its MDD.
     */
    Solution findJointSolution(Map<Agent, MDD> agentMDDs, ICTS_Solver highLevelSolver);
}