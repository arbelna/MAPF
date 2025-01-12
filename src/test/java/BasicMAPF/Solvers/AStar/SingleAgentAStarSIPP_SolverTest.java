package BasicMAPF.Solvers.AStar;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.GoalConditions.SingleTargetCoordinateGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedAGoalAtSomePointInPlanGoalCondition;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.*;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

import static BasicMAPF.Solvers.AStar.SingleAgentAStar_SolverTest.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Agents.*;
import static org.junit.jupiter.api.Assertions.*;

class SingleAgentAStarSIPP_SolverTest {

    private I_Location location12Circle = mapCircle.getMapLocation(coor12);
    private I_Location location13Circle = mapCircle.getMapLocation(coor13);
    private I_Location location14Circle = mapCircle.getMapLocation(coor14);
    private I_Location location22Circle = mapCircle.getMapLocation(coor22);
    private I_Location location24Circle = mapCircle.getMapLocation(coor24);
    private I_Location location32Circle = mapCircle.getMapLocation(coor32);
    private I_Location location33Circle = mapCircle.getMapLocation(coor33);
    private I_Location location34Circle = mapCircle.getMapLocation(coor34);

    private I_Location location11 = mapCircle.getMapLocation(coor11);
    private I_Location location43 = mapCircle.getMapLocation(coor43);
    private I_Location location53 = mapCircle.getMapLocation(coor53);
    private I_Location location05 = mapCircle.getMapLocation(coor05);

    private I_Location location04 = mapCircle.getMapLocation(coor04);
    private I_Location location00 = mapCircle.getMapLocation(coor00);

    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent53to05});
    private MAPF_Instance instanceEmpty2 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent43to11});
    private MAPF_Instance instance1stepSolution = im.getNextInstance();
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent04to00});
    private MAPF_Instance instanceMaze1 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent04to40});
    private MAPF_Instance instanceMaze2 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent00to55});
    private MAPF_Instance instanceMaze3 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent43to53});
    private MAPF_Instance instanceMaze4 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent53to15});

    I_Solver sipp = new SingleAgentAStarSIPP_Solver();

    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = S_Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        S_Metrics.removeReport(instanceReport);
    }


    @Test
    void oneMoveSolution() {
        MAPF_Instance testInstance = instance1stepSolution;
        Solution s = sipp.solve(testInstance, new RunParameters());

        Map<Agent, SingleAgentPlan> plans = new HashMap<>();
        SingleAgentPlan plan = new SingleAgentPlan(testInstance.agents.get(0));
        I_Location location = testInstance.map.getMapLocation(new Coordinate_2D(4,5));
        plan.addMove(new Move(testInstance.agents.get(0), 1, location, location));
        plans.put(testInstance.agents.get(0), plan);
        Solution expected = new Solution(plans);

        assertEquals(s, expected);
    }

    @Test
    void circleOptimality1(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        Solution solved = sipp.solve(testInstance, new RunParameters());

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 2, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 3, location22Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(3, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);

    }

    @Test
    void circleOptimalityWaitingBecauseOfConstraint1(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint vertexConstraint = new Constraint(null, 1, null, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(vertexConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = sipp.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location33Circle));
        plan.addMove(new Move(agent, 2, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 4, location22Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(4, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);

    }

    @Test
    void circleOptimalityWaitingBecauseOfConstraint2(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint vertexConstraint = new Constraint(agent, 1, null, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(vertexConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = sipp.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location33Circle));
        plan.addMove(new Move(agent, 2, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 4, location22Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(4, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void circleOptimalityWaitingBecauseOfConstraint3(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint swappingConstraint = new Constraint(agent, 1, location33Circle, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(swappingConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = sipp.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location33Circle));
        plan.addMove(new Move(agent, 2, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 4, location22Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(4, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void circleOptimalityOtherDirectionBecauseOfConstraints(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint swappingConstraint1 = new Constraint(null, 1, location33Circle, location32Circle);
        Constraint swappingConstraint2 = new Constraint(null, 2, location33Circle, location32Circle);
        Constraint swappingConstraint3 = new Constraint(null, 3, location33Circle, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(swappingConstraint1);
        constraints.add(swappingConstraint2);
        constraints.add(swappingConstraint3);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = sipp.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        plan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        plan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        plan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        plan.addMove(new Move(agent, 5, location13Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(5, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);

    }

    @Test
    void largeNumberOfConstraints(){
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        List<I_Location> locations = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            for (int j = 0; j <= 5; j++) {
                I_Coordinate newCoor = new Coordinate_2D(i, j);
                I_Location newLocation = instanceEmpty1.map.getMapLocation(newCoor);
                locations.add(newLocation);
            }
        }
        Random rand = new Random();
        rand.setSeed(10);
        ConstraintSet constraints = new ConstraintSet();
        Set<I_Location> checkDuplicates = new HashSet<>();
        for (int t = 1; t <= 30; t++) {
            for (int j = 0; j < 20; j++) {
                I_Location randomLocation = locations.get(rand.nextInt(locations.size()));
                if (checkDuplicates.contains(randomLocation)){
                    j--;
                    continue;
                }
                checkDuplicates.add(randomLocation);
                Constraint constraint = new Constraint(agent, t, null, randomLocation);
                constraints.add(constraint);
            }
            checkDuplicates = new HashSet<>();
        }
        SingleAgentAStarSIPP_Solver sipp = new SingleAgentAStarSIPP_Solver();
        RunParameters parameters = new RunParameters(constraints);
        long startTime = System.currentTimeMillis();
        Solution sippSolution = sipp.solve(testInstance, parameters);
        long endTime = System.currentTimeMillis();

        int sippExpandedNodes = sipp.getExpandedNodes();
        int sippGeneratedNodes = sipp.getGeneratedNodes();

        List<Integer> sippPlanCosts = null;
        if (sippSolution != null){
            List<I_Location> sippPlanLocations = planLocations(sippSolution.getPlanFor(agent));
            sippPlanCosts = getCosts(agent, unitCostAndNoHeuristic, sippPlanLocations);
            System.out.println("SIPP:");
            System.out.println("Running Time:");
            System.out.println(endTime - startTime);
            System.out.println("Expanded nodes:");
            System.out.println(sippExpandedNodes);
            System.out.println("Generated nodes:");
            System.out.println(sippGeneratedNodes);
        }
        else{
            System.out.println("SIPP Didn't Solve!!!");
        }

        SingleAgentAStar_Solver astar = new SingleAgentAStar_Solver();
        startTime = System.currentTimeMillis();
        Solution aStarSolution = astar.solve(testInstance, parameters);
        endTime = System.currentTimeMillis();

        int astarExpandedNodes = astar.getExpandedNodes();
        int astarGeneratedNodes = astar.getGeneratedNodes();

        List<Integer> aStarPlanCosts = null;
        if (aStarSolution != null){
            List<I_Location> aStarPlanLocations = planLocations(aStarSolution.getPlanFor(agent));
            aStarPlanCosts = getCosts(agent, unitCostAndNoHeuristic, aStarPlanLocations);
            System.out.println("aStar:");
            System.out.println("Running Time:");
            System.out.println(endTime - startTime);
            System.out.println("Expanded nodes:");
            System.out.println(astarExpandedNodes);
            System.out.println("Generated nodes:");
            System.out.println(astarGeneratedNodes);
        }
        else{
            System.out.println("aStar Didn't Solve!!!");
        }


        System.out.println("Costs were:");
        System.out.println(unitCostAndNoHeuristic);

        assertNotNull(aStarSolution);
        assertNotNull(sippSolution);

        int costAStar = 0;
        int costSipp = 0;
        for (int i = 0; i < Math.max(aStarPlanCosts.size(), sippPlanCosts.size()); i++) {
            if (i < aStarPlanCosts.size()){
                costAStar += aStarPlanCosts.get(i);
            }
            if (i < sippPlanCosts.size()){
                costSipp += sippPlanCosts.get(i);
            }
        }
        assertEquals(costAStar, costSipp, "aStar cost " + costAStar + " should be the same as Sipp cost " + costSipp + "");
        assertTrue(astarExpandedNodes > sippExpandedNodes, "aStar number of expanded nodes: " + astarExpandedNodes + " should be greater than Sipp number of expanded nodes: " + sippExpandedNodes + "");
        assertTrue(astarGeneratedNodes > sippGeneratedNodes, "aStar number of generated nodes: " + astarGeneratedNodes + " should be greater than Sipp number of generated nodes: " + sippGeneratedNodes + "");
    }

    @Test
    void circleOptimalityNorthwestToSoutheast(){
        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Solution solved = sipp.solve(testInstance, new RunParameters());

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location12Circle, location22Circle));
        plan.addMove(new Move(agent, 2, location22Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location33Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(3, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void emptyOptimality(){
        MAPF_Instance testInstance1 = instanceEmpty1;
        Agent agent1 = testInstance1.agents.get(0);

        MAPF_Instance testInstance2 = instanceEmpty2;
        Agent agent2 = testInstance2.agents.get(0);

        Solution solved1 = sipp.solve(testInstance1, new RunParameters());
        Solution solved2 = sipp.solve(testInstance2, new RunParameters());

        assertEquals(7, solved1.getPlanFor(agent1).size());
        assertEquals(5, solved2.getPlanFor(agent2).size());
    }

    @Test
    void unsolvableShouldTimeout(){
        MAPF_Instance testInstance = instanceUnsolvable;

        // three second timeout
        RunParameters runParameters = new RunParameters(1000*3);
        Solution solved = sipp.solve(testInstance, runParameters);

        assertNull(solved);
    }

    @Test
    void accountsForConstraintAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved1 = sipp.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(10, solved1.getPlanFor(agent).size());
    }

    @Test
    void accountsForConstraintAfterReachingGoal2() {
        // now with an expected plan

        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,5, null, location33Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved = sipp.solve(testInstance, runParameters);

        SingleAgentPlan plan3 = new SingleAgentPlan(agent);
        plan3.addMove(new Move(agent, 1, location12Circle, location22Circle));
        plan3.addMove(new Move(agent, 2, location22Circle, location32Circle));
        plan3.addMove(new Move(agent, 3, location32Circle, location32Circle));
        plan3.addMove(new Move(agent, 4, location32Circle, location32Circle));
        plan3.addMove(new Move(agent, 5, location32Circle, location32Circle));
        plan3.addMove(new Move(agent, 6, location32Circle, location33Circle));
        Solution expected = new Solution();
        expected.putPlan(plan3);

        assertEquals(6, solved.getPlanFor(agent).size());
        assertTrue(expected.equals(solved));
    }

    @Test
    void accountsForConstraintInFarFutureAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,90, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,200, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);
        for (int t = 0; t < 200 /*agents*/ * 200 /*timesteps* * 2 /*constraints*/; t++) {
            constraints.add(new Constraint(agent,t, null, instanceEmpty1.map.getMapLocation(coor15)));
        }
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved1 = sipp.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(201, solved1.getPlanFor(agent).size());
    }

    @Test
    void accountsForMultipleConstraintsAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved1 = sipp.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(15, solved1.getPlanFor(agent).size());
    }

    @Test
    void continuingFromExistingPlan() {
        // modified from circleOptimality1()

        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        SingleAgentPlan existingPlan = new SingleAgentPlan(agent);
        existingPlan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        existingPlan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        Solution existingSolution = new Solution();
        existingSolution.putPlan(existingPlan);

        // give the solver a plan to continue from
        Solution solved = sipp.solve(testInstance, new RunParameters(existingSolution));

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        plan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        plan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        plan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        plan.addMove(new Move(agent, 5, location13Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(5, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }
    @Disabled
    @Test
    void findsPIBTStylePlanUnderConstraintsUsingPIBTStyleGoalCondition() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParameters(constraints, new InstanceReport()));
        runParameters.goalCondition = new VisitedAGoalAtSomePointInPlanGoalCondition(new SingleTargetCoordinateGoalCondition(agent.target));

        Solution solved1 = sipp.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
    }
    @Disabled
    @Test
    void findsPIBTStylePlanUnderConstraintsAlsoAroundGoalUsingPIBTStyleGoalCondition() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoalAroundGoal1 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor15));
        Constraint constraintAtTimeAfterReachingGoalAroundGoal2 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor04));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);
        constraints.add(constraintAtTimeAfterReachingGoalAroundGoal1);
        constraints.add(constraintAtTimeAfterReachingGoalAroundGoal2);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParameters(constraints, new InstanceReport()));
        runParameters.goalCondition = new VisitedAGoalAtSomePointInPlanGoalCondition(new SingleTargetCoordinateGoalCondition(agent.target));

        Solution solved1 = sipp.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else,
        // but the surrounding locations also have constraints in the future, so has to take 2 steps
        assertEquals(9, solved1.getPlanFor(agent).size());
    }
    private final AStarGAndH unitCostAndNoHeuristic = new SingleAgentAStar_SolverTest.UnitCostAndNoHeuristic();

    @Test
    void optimalVsUCS1(){
        MAPF_Instance testInstance = instanceMaze1;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }

    @Test
    void optimalVsUCS2(){
        MAPF_Instance testInstance = instanceMaze2;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCS3(){
        MAPF_Instance testInstance = instanceMaze3;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCS4(){
        MAPF_Instance testInstance = instanceMaze4;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCSDynamic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentMapsWithNames;
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        compareAStarAndUCS(sipp, new InstanceReport(),
                                agent, testInstance, unitCostAndNoHeuristic);
                    }
                }
            }
        }
    }
    @Test
    void optimalVsUCSDynamicWithDistanceTableHeuristic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentMapsWithNames;
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        DistanceTableAStarHeuristic distanceTableAStarHeuristic = new DistanceTableAStarHeuristic(testInstance.agents, testInstance.map);
                        compareAStarAndUCS(sipp, new InstanceReport(),
                                agent, testInstance, distanceTableAStarHeuristic);
                    }
                }
            }
        }
    }
    @Test
    void optimalVsUCSDDynamicWithManhattanDistanceHeuristic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentGridMapsWithNames; // grid maps only!
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        compareAStarAndUCS(sipp, new InstanceReport(), agent, testInstance, new UnitCostsAndManhattanDistance(agent.target));
                    }
                }
            }
        }
    }
    @Test
    void optimalVsUCSWeightedEdges1(){
        MAPF_Instance testInstance = instanceMaze1;
        Agent agent = testInstance.agents.get(0);
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges2(){
        MAPF_Instance testInstance = instanceMaze2;
        Agent agent = testInstance.agents.get(0);
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges3(){
        MAPF_Instance testInstance = instanceMaze3;
        Agent agent = testInstance.agents.get(0);
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges4(){
        MAPF_Instance testInstance = instanceMaze4;
        Agent agent = testInstance.agents.get(0);
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdgesDynamic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentMapsWithNames;
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));
                        compareAStarAndUCS(sipp, new InstanceReport(), agent, testInstance, randomStableCosts);
                    }
                }
            }
        }
    }

    private void compareAStarAndUCS(I_Solver aStar, InstanceReport instanceReport, Agent agent, MAPF_Instance testInstance, AStarGAndH costFunction) {
        RunParameters aStarRunParameters = new RunParameters_SAAStar(instanceReport, costFunction);

        String identifier = testInstance.name + " " + agent.source + " to " + agent.target;
        System.out.println("\n" + identifier);

        Solution aStarSolution = aStar.solve(testInstance, aStarRunParameters);
        List<Integer> aSTsarPlanCosts = null;
        if (aStarSolution != null){
            List<I_Location> aStarPlanLocations = planLocations(aStarSolution.getPlanFor(agent));
            aSTsarPlanCosts = getCosts(agent, costFunction, aStarPlanLocations);
            System.out.println("AStar:");
            System.out.println(aStarPlanLocations);
            System.out.println(aSTsarPlanCosts);
        }
        else{
            System.out.println("AStar Didn't Solve!!!");
        }

        List<I_Location> UCSPlanLocations = NoStateTimeSearches.uniformCostSearch(testInstance.map.getMapLocation(agent.target),
                testInstance.map.getMapLocation(agent.source), costFunction, agent);
        List<Integer> UCSPlanCosts = null;
        if (UCSPlanLocations != null){
            UCSPlanCosts = getCosts(agent, costFunction, UCSPlanLocations);
            System.out.println("UCS:");
            System.out.println(UCSPlanLocations);
            System.out.println(UCSPlanCosts);
        }
        else{
            System.out.println("UCS Didn't Solve!!!");
        }


        System.out.println("Costs were:");
        System.out.println(costFunction);

        assertNotNull(aStarSolution);
        assertNotNull(UCSPlanLocations);

        int costAStar = 0;
        int costUCS = 0;
        for (int i = 0; i < Math.max(aSTsarPlanCosts.size(), UCSPlanCosts.size()); i++) {
            if (i < aSTsarPlanCosts.size()){
                costAStar += aSTsarPlanCosts.get(i);
            }
            if (i < UCSPlanCosts.size()){
                costUCS += UCSPlanCosts.get(i);
            }
        }
        assertEquals(costAStar, costUCS);
    }

    @Test
    void comparativeDiverseTest(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver regularCBS = new CBS_Solver(null, null, null,
                null, null, false, null, null);
        String nameBaseline = "regularCBS";
        I_Solver singleAgentSippCBS = new CBS_Solver(new SingleAgentAStarSIPP_Solver(), null, null,
                null, null, false, null, null);
        String nameExperimental = "singleAgentSippCBS";
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
//                new InstanceProperties(null, -1d, new int[]{5, 10, 15, 20, 25}));
                new InstanceProperties(null, -1d, new int[]{5, 10, 15}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
//        long timeout = 60 /*seconds*/   *1000L;
        long timeout = 1000000000 /*seconds*/   *1000L;
        int solvedByBaseline = 0;
        int solvedByExperimental = 0;
        int runtimeBaseline = 0;
        int runtimeExperimental = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // run baseline (without the improvement)
            //build report
            InstanceReport reportBaseline = S_Metrics.newInstanceReport();
            reportBaseline.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportBaseline.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportBaseline.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportBaseline.putStringValue(InstanceReport.StandardFields.solver, "regularCBS");

            RunParameters runParametersBaseline = new RunParameters(timeout, null, reportBaseline, null);

            //solve
            Solution solutionBaseline = regularCBS.solve(instance, runParametersBaseline);

            // run experimentl (with the improvement)
            //build report
            InstanceReport reportExperimental = S_Metrics.newInstanceReport();
            reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportExperimental.putStringValue(InstanceReport.StandardFields.solver, "singleAgentSippCBS");

            RunParameters runParametersExperimental = new RunParameters(timeout, null, reportExperimental, null);

            //solve
            Solution solutionExperimental = singleAgentSippCBS.solve(instance, runParametersExperimental);

            // compare

            boolean baselineSolved = solutionBaseline != null;
            solvedByBaseline += baselineSolved ? 1 : 0;
            boolean experimentalSolved = solutionExperimental != null;
            solvedByExperimental += experimentalSolved ? 1 : 0;
            System.out.println(nameBaseline + " Solved?: " + (baselineSolved ? "yes" : "no") +
                    " ; " + nameExperimental + " solved?: " + (experimentalSolved ? "yes" : "no"));

            if(solutionBaseline != null){
                boolean valid = solutionBaseline.solves(instance);
                System.out.print(nameBaseline + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionExperimental != null){
                boolean valid = solutionExperimental.solves(instance);
                System.out.println(" " + nameExperimental + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionBaseline != null && solutionExperimental != null){
                int optimalCost = solutionBaseline.sumIndividualCosts();
                int costWeGot = solutionExperimental.sumIndividualCosts();
                boolean optimal = optimalCost==costWeGot;
                System.out.println(nameExperimental + " cost is " + (optimal ? "optimal (" + costWeGot +")" :
                        ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                reportBaseline.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                reportExperimental.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                if (useAsserts) assertEquals(optimalCost, costWeGot);

                // runtimes
                runtimeBaseline += reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                runtimeExperimental += reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                reportBaseline.putIntegerValue("Runtime Delta",
                        reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                - reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
            }
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(nameBaseline + " solved: " + solvedByBaseline);
        System.out.println(nameExperimental + " solved: " + solvedByExperimental);
        System.out.println("runtime totals (instances where both solved) :");
        System.out.println(nameBaseline + " time: " + runtimeBaseline);
        System.out.println(nameExperimental + " time: " + runtimeExperimental);

        //save results
        DateFormat dateFormat = S_Metrics.defaultDateFormat;
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "CBS_Tests"});
        File directory = new File(resultsOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String updatedPath = resultsOutputDir + "\\results " + dateFormat.format(System.currentTimeMillis()) + ".csv";
        try {
            S_Metrics.exportCSV(new FileOutputStream(updatedPath),
                    new String[]{
                            InstanceReport.StandardFields.instanceName,
                            InstanceReport.StandardFields.numAgents,
                            InstanceReport.StandardFields.timeoutThresholdMS,
                            InstanceReport.StandardFields.solved,
                            InstanceReport.StandardFields.elapsedTimeMS,
                            "Runtime Delta",
                            InstanceReport.StandardFields.solutionCost,
                            "Cost Delta",
                            InstanceReport.StandardFields.totalLowLevelTimeMS,
                            InstanceReport.StandardFields.generatedNodes,
                            InstanceReport.StandardFields.expandedNodes,
                            InstanceReport.StandardFields.generatedNodesLowLevel,
                            InstanceReport.StandardFields.expandedNodesLowLevel});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}