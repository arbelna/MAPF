package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.*;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PrioritisedPlanningSolverTest {

    private final Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final Enum_MapLocationType w = Enum_MapLocationType.WALL;
    private final Enum_MapLocationType[][] map_2D_circle = {
            {w, w, w, w, w, w},
            {w, w, e, e, e, w},
            {w, w, e, w, e, w},
            {w, w, e, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    private final I_Map mapCircle = MapFactory.newSimple4Connected2D_GraphMap(map_2D_circle);

    Enum_MapLocationType[][] map_2D_empty = {
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
    };
    private final I_Map mapEmpty = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty);

    Enum_MapLocationType[][] map_2D_withPocket = {
            {e, w, e, w, e, w},
            {e, w, e, e, e, e},
            {w, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, e, w, e, e, e},
    };
    private final I_Map mapWithPocket = MapFactory.newSimple4Connected2D_GraphMap(map_2D_withPocket);


    private final Enum_MapLocationType[][] map_2D_smallMaze = {
            {e, e, e, w, e, w},
            {e, w, e, e, e, e},
            {e, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, w, w, e, e, e},
    };
    private final I_Map mapSmallMaze = MapFactory.newSimple4Connected2D_GraphMap(map_2D_smallMaze);

    private final I_Coordinate coor12 = new Coordinate_2D(1,2);
    private final I_Coordinate coor13 = new Coordinate_2D(1,3);
    private final I_Coordinate coor14 = new Coordinate_2D(1,4);
    private final I_Coordinate coor22 = new Coordinate_2D(2,2);
    private final I_Coordinate coor24 = new Coordinate_2D(2,4);
    private final I_Coordinate coor32 = new Coordinate_2D(3,2);
    private final I_Coordinate coor33 = new Coordinate_2D(3,3);
    private final I_Coordinate coor34 = new Coordinate_2D(3,4);
    private final I_Coordinate coor35 = new Coordinate_2D(3,5);

    private final I_Coordinate coor11 = new Coordinate_2D(1,1);
    private final I_Coordinate coor43 = new Coordinate_2D(4,3);
    private final I_Coordinate coor53 = new Coordinate_2D(5,3);
    private final I_Coordinate coor54 = new Coordinate_2D(5,4);
    private final I_Coordinate coor55 = new Coordinate_2D(5,5);
    private final I_Coordinate coor05 = new Coordinate_2D(0,5);

    private final I_Coordinate coor04 = new Coordinate_2D(0,4);
    private final I_Coordinate coor00 = new Coordinate_2D(0,0);
    private final I_Coordinate coor01 = new Coordinate_2D(0,1);
    private final I_Coordinate coor10 = new Coordinate_2D(1,0);

    private final I_Location location12 = mapCircle.getMapLocation(coor12);
    private final I_Location location13 = mapCircle.getMapLocation(coor13);
    private final I_Location location14 = mapCircle.getMapLocation(coor14);
    private final I_Location location22 = mapCircle.getMapLocation(coor22);
    private final I_Location location24 = mapCircle.getMapLocation(coor24);
    private final I_Location location32 = mapCircle.getMapLocation(coor32);
    private final I_Location location33 = mapCircle.getMapLocation(coor33);
    private final I_Location location34 = mapCircle.getMapLocation(coor34);

    private final I_Location location11 = mapCircle.getMapLocation(coor11);
    private final I_Location location43 = mapCircle.getMapLocation(coor43);
    private final I_Location location53 = mapCircle.getMapLocation(coor53);
    private final I_Location location54 = mapCircle.getMapLocation(coor54);
    private final I_Location location55 = mapCircle.getMapLocation(coor55);
    private final I_Location location05 = mapCircle.getMapLocation(coor05);

    private final I_Location location04 = mapCircle.getMapLocation(coor04);
    private final I_Location location00 = mapCircle.getMapLocation(coor00);
    private final I_Location location01 = mapCircle.getMapLocation(coor01);
    private final I_Location location10 = mapCircle.getMapLocation(coor10);

    private final Agent agent33to12 = new Agent(0, coor33, coor12);
    private final Agent agent12to33 = new Agent(1, coor12, coor33);
    private final Agent agent53to05 = new Agent(2, coor53, coor05);
    private final Agent agent43to11 = new Agent(3, coor43, coor11);
    private final Agent agent04to00 = new Agent(4, coor04, coor00);
    private final Agent agent00to10 = new Agent(5, coor00, coor10);
    private final Agent agent10to00 = new Agent(6, coor10, coor00);
    private final Agent agent43to53 = new Agent(7, coor43, coor53);
    private final Agent agent55to34 = new Agent(8, coor55, coor34);
    private final Agent agent33to35 = new Agent(9, coor33, coor35);
    private final Agent agent34to32 = new Agent(10, coor34, coor32);

    InstanceBuilder_BGU builder = new InstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
    private final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private final MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});
    private final MAPF_Instance instanceUnsolvableBecauseOrderWithInfiniteWait = new MAPF_Instance("instanceUnsolvableWithInfiniteWait", mapWithPocket, new Agent[]{agent43to53, agent55to34});
    private final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});

    I_Solver ppSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver());


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
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(10, solved.sumIndividualCosts());
        assertEquals(8, solved.makespan());
    }

    @Test
    void failsBeforeTimeoutWhenFacedWithInfiniteConstraints() {
        MAPF_Instance testInstance = instanceUnsolvableBecauseOrderWithInfiniteWait;
        long timeout = 10*1000;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(timeout, null, instanceReport, null));

        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm)
        assertNull(solved);
    }

    @Test
    void unsolvable() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertNull(solved);
    }

    @Test
    void failsBeforeTimeoutWithRandomInitialAndContingency() {
        MAPF_Instance testInstance = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00, agent55to34, agent43to53});
        I_Solver solver = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 2, RestartsStrategy.RestartsKind.randomRestarts), null, null);
        long timeout = 10*1000;
        Solution solved = solver.solve(testInstance, new RunParameters(timeout, null, instanceReport, null));

        System.out.println(instanceReport);
        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm, exhausts the orderings search space)
        assertNull(solved);
        // should perform 3 + 21 attempts
        assertNotNull(instanceReport.getIntegerValue("attempt #2 time"));
        assertEquals(21, instanceReport.getIntegerValue("count contingency attempts"));
    }

    @Test
    void failsBeforeTimeoutWithDeterministicInitialAndContingency() {
        MAPF_Instance testInstance = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent55to34, agent43to53, agent00to10, agent10to00});
        I_Solver solver = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.deterministicRescheduling, 2, RestartsStrategy.RestartsKind.deterministicRescheduling), null, null);
        long timeout = 10*1000;
        Solution solved = solver.solve(testInstance, new RunParameters(timeout, null, instanceReport, null));

        System.out.println(instanceReport);
        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm)
        assertNull(solved);
        // should perform 3 + 1 attempts
        assertNotNull(instanceReport.getIntegerValue("attempt #2 time"));
        assertEquals(1, instanceReport.getIntegerValue("count contingency attempts"));
    }

    @Test
    void solvesWhenBadInitialOrderAndHasContingency() {
        MAPF_Instance testInstance = instanceUnsolvableBecauseOrderWithInfiniteWait;
        long timeout = 10*1000;
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(null, null, RestartsStrategy.RestartsKind.randomRestarts), null, null);
        Solution solved = solver.solve(testInstance, new RunParameters(timeout, null, instanceReport, null));
        // should be able to solve in one of the restarts
        assertNotNull(solved);

        solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(null, null, RestartsStrategy.RestartsKind.deterministicRescheduling), null, null);
        solved = solver.solve(testInstance, new RunParameters(timeout, null, instanceReport, null));
        // should be able to solve in one of the restarts
        assertNotNull(solved);

        // sanity check that it does indeed fail without the contingency
        solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(), null, null);
        solved = solver.solve(testInstance, new RunParameters(timeout, null, instanceReport, null));
        // should fail without the contingency
        assertNull(solved);
    }

    @Test
    void sortAgents() {
        MAPF_Instance testInstance = instanceCircle1;
        I_Solver solver = new PrioritisedPlanning_Solver((Agent a1, Agent a2) -> a2.priority - a1.priority);

        Agent agent0 = new Agent(0, coor33, coor12, 10);
        Agent agent1 = new Agent(1, coor12, coor33, 1);

        MAPF_Instance agent0prioritisedInstance = new MAPF_Instance("agent0prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent0prioritisedSolution = solver.solve(agent0prioritisedInstance, new RunParameters(instanceReport));

        agent0 = new Agent(0, coor33, coor12, 1);
        agent1 = new Agent(1, coor12, coor33, 10);

        MAPF_Instance agent1prioritisedInstance = new MAPF_Instance("agent1prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent1prioritisedSolution = solver.solve(agent1prioritisedInstance, new RunParameters(instanceReport));

        assertTrue(agent0prioritisedSolution.solves(testInstance));
        assertTrue(agent1prioritisedSolution.solves(testInstance));

        assertEquals(agent0prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent0prioritisedSolution.getPlanFor(agent0).size(), 3);

        assertEquals(agent1prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent1prioritisedSolution.getPlanFor(agent1).size(), 3);
    }


    @Test
    void TestingBenchmark(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = ppSolver;
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance = null;
        // load the pre-made benchmark
        try {
            long timeout = 5 /*seconds*/
                    *1000L;
            Map<String, Map<String, String>> benchmarks = readResultsCSV(path + "\\Results.csv");
            int numSolved = 0;
            int numFailed = 0;
            int numValid = 0;
            int numOptimal = 0;
            int numValidSuboptimal = 0;
            int numInvalidOptimal = 0;
            // run all benchmark instances. this code is mostly copied from Environment.Experiment.
            while ((instance = instanceManager.getNextInstance()) != null) {
//                if (!instance.name.equals("Instance-32-20-20-0")){
//                    continue;
//                }

                //build report
                InstanceReport report = S_Metrics.newInstanceReport();
                report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
                report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                report.putStringValue(InstanceReport.StandardFields.solver, solver.name());

                RunParameters runParameters = new RunParameters(timeout, null, report, null);

                //solve
                System.out.println("---------- solving "  + instance.name + " ----------");
                Solution solution = solver.solve(instance, runParameters);

                // validate
                Map<String, String> benchmarkForInstance = benchmarks.get(instance.name);
                if(benchmarkForInstance == null){
                    System.out.println("can't find benchmark for " + instance.name);
                    continue;
                }

                boolean solved = solution != null;
                System.out.println("Solved?: " + (solved ? "yes" : "no"));
//                if (useAsserts) assertNotNull(solution);
                if (solved) numSolved++;
                else numFailed++;

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);

                    report.putIntegerValue("Runtime Delta",
                            report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) - (int)Float.parseFloat(benchmarkForInstance.get("Plan time")));

                    if(valid) numValid++;
                    if(optimal) numOptimal++;
                    if(valid && !optimal) numValidSuboptimal++;
                    if(!valid && optimal) numInvalidOptimal++;
                }
            }

            System.out.println("--- TOTALS: ---");
            System.out.println("timeout for each (seconds): " + (timeout/1000));
            System.out.println("solved: " + numSolved);
            System.out.println("failed: " + numFailed);
            System.out.println("valid: " + numValid);
            System.out.println("optimal: " + numOptimal);
            System.out.println("valid but not optimal: " + numValidSuboptimal);
            System.out.println("not valid but optimal: " + numInvalidOptimal);

            //save results
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Test
    void TestingBenchmarkWInitialRandomRestarts(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 2), null, null);
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance = null;
        // load the pre-made benchmark
        try {
            long timeout = 5 /*seconds*/
                    *1000L;
            Map<String, Map<String, String>> benchmarks = readResultsCSV(path + "\\Results.csv");
            int numSolved = 0;
            int numFailed = 0;
            int numValid = 0;
            int numOptimal = 0;
            int numValidSuboptimal = 0;
            int numInvalidOptimal = 0;
            // run all benchmark instances. this code is mostly copied from Environment.Experiment.
            while ((instance = instanceManager.getNextInstance()) != null) {
//                if (!instance.name.equals("brc202d-10-8")){
//                    continue;
//                }

                //build report
                InstanceReport report = S_Metrics.newInstanceReport();
                report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
                report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                report.putStringValue(InstanceReport.StandardFields.solver, solver.name());

                RunParameters runParameters = new RunParameters(timeout, null, report, null);

                //solve
                System.out.println("---------- solving "  + instance.name + " ----------");
                Solution solution = solver.solve(instance, runParameters);

                // validate
                Map<String, String> benchmarkForInstance = benchmarks.get(instance.name);
                if(benchmarkForInstance == null){
                    System.out.println("can't find benchmark for " + instance.name);
                    continue;
                }

                boolean solved = solution != null;
                System.out.println("Solved?: " + (solved ? "yes" : "no"));
//                if (useAsserts) assertNotNull(solution);
                if (solved) numSolved++;
                else numFailed++;

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);

                    report.putIntegerValue("Runtime Delta",
                            report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) - (int)Float.parseFloat(benchmarkForInstance.get("Plan time")));

                    if(valid) numValid++;
                    if(optimal) numOptimal++;
                    if(valid && !optimal) numValidSuboptimal++;
                    if(!valid && optimal) numInvalidOptimal++;
                }
            }

            System.out.println("--- TOTALS: ---");
            System.out.println("timeout for each (seconds): " + (timeout/1000));
            System.out.println("solved: " + numSolved);
            System.out.println("failed: " + numFailed);
            System.out.println("valid: " + numValid);
            System.out.println("optimal: " + numOptimal);
            System.out.println("valid but not optimal: " + numValidSuboptimal);
            System.out.println("not valid but optimal: " + numInvalidOptimal);

            //save results
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Test
    void TestingBenchmarkWInitialDeterministicRestarts(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.deterministicRescheduling, 2), null, null);
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance = null;
        // load the pre-made benchmark
        try {
            long timeout = 5 /*seconds*/
                    *1000L;
            Map<String, Map<String, String>> benchmarks = readResultsCSV(path + "\\Results.csv");
            int numSolved = 0;
            int numFailed = 0;
            int numValid = 0;
            int numOptimal = 0;
            int numValidSuboptimal = 0;
            int numInvalidOptimal = 0;
            // run all benchmark instances. this code is mostly copied from Environment.Experiment.
            while ((instance = instanceManager.getNextInstance()) != null) {
//                if (!instance.name.equals("brc202d-10-8")){
//                    continue;
//                }

                //build report
                InstanceReport report = S_Metrics.newInstanceReport();
                report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
                report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                report.putStringValue(InstanceReport.StandardFields.solver, solver.name());

                RunParameters runParameters = new RunParameters(timeout, null, report, null);

                //solve
                System.out.println("---------- solving "  + instance.name + " ----------");
                Solution solution = solver.solve(instance, runParameters);

                // validate
                Map<String, String> benchmarkForInstance = benchmarks.get(instance.name);
                if(benchmarkForInstance == null){
                    System.out.println("can't find benchmark for " + instance.name);
                    continue;
                }

                boolean solved = solution != null;
                System.out.println("Solved?: " + (solved ? "yes" : "no"));
//                if (useAsserts) assertNotNull(solution);
                if (solved) numSolved++;
                else numFailed++;

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);

                    report.putIntegerValue("Runtime Delta",
                            report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) - (int)Float.parseFloat(benchmarkForInstance.get("Plan time")));

                    if(valid) numValid++;
                    if(optimal) numOptimal++;
                    if(valid && !optimal) numValidSuboptimal++;
                    if(!valid && optimal) numInvalidOptimal++;
                }
            }

            System.out.println("--- TOTALS: ---");
            System.out.println("timeout for each (seconds): " + (timeout/1000));
            System.out.println("solved: " + numSolved);
            System.out.println("failed: " + numFailed);
            System.out.println("valid: " + numValid);
            System.out.println("optimal: " + numOptimal);
            System.out.println("valid but not optimal: " + numValidSuboptimal);
            System.out.println("not valid but optimal: " + numInvalidOptimal);

            //save results
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This contains diverse instances
     */
    @Test
    void comparativeDiverseTestHasContingencyVsNoContingency(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null);
        String nameBaseline = baselineSolver.name();

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(null, null, RestartsStrategy.RestartsKind.randomRestarts), null, null);
        String nameExperimental = competitorSolver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
                new InstanceProperties(null, -1d, new int[]{100}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
//        long timeout = 60 /*seconds*/   *1000L;
        long timeout = 10 /*seconds*/   *1000L;
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
            reportBaseline.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

            RunParameters runParametersBaseline = new RunParameters(timeout, null, reportBaseline, null);

            //solve
            Solution solutionBaseline = baselineSolver.solve(instance, runParametersBaseline);

            // run experiment (with the improvement)
            //build report
            InstanceReport reportExperimental = S_Metrics.newInstanceReport();
            reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportExperimental.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

            RunParameters runParametersExperimental = new RunParameters(timeout, null, reportExperimental, null);

            //solve
            Solution solutionExperimental = competitorSolver.solve(instance, runParametersExperimental);

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
            else System.out.println();

            if(solutionBaseline != null && solutionExperimental != null){
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
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
                            InstanceReport.StandardFields.solver,
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

    @Test
    void sharedGoals(){
        PrioritisedPlanning_Solver ppSolverSharedGoals = new PrioritisedPlanning_Solver(null, null, null, null, true, null);

        MAPF_Instance instanceEmptyPlusSharedGoal1 = new MAPF_Instance("instanceEmptyPlusSharedGoal1", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, new Agent(20, coor14, coor05)});
        MAPF_Instance instanceEmptyPlusSharedGoal2 = new MAPF_Instance("instanceEmptyPlusSharedGoal2", mapEmpty,
                new Agent[]{new Agent(20, coor14, coor05), agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoal3 = new MAPF_Instance("instanceEmptyPlusSharedGoal3", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor14, coor05), agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoal4 = new MAPF_Instance("instanceEmptyPlusSharedGoal4", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor24, coor12), agent43to11, agent04to00});

        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart1 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart1", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, new Agent(20, coor33, coor05)});
        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart2 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart2", mapEmpty,
                new Agent[]{new Agent(20, coor33, coor05), agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart3 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart3", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor33, coor05), agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart4 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart4", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor43, coor00), agent43to11, agent04to00});

        // like a duplicate agent except for the id
        MAPF_Instance instanceEmptyPlusSharedGoalAndStart1 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndStart1", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor43, coor11), agent43to11, agent04to00});

        MAPF_Instance instanceCircle1SharedGoal = new MAPF_Instance("instanceCircle1SharedGoal", mapCircle, new Agent[]{agent33to12, agent12to33, new Agent(20, coor32, coor12)});
        // like a duplicate agent except for the id
        MAPF_Instance instanceCircle1SharedGoalAndStart = new MAPF_Instance("instanceCircle1SharedGoalAndStart", mapCircle, new Agent[]{agent33to12, agent12to33, new Agent(20, coor33, coor12)});

        MAPF_Instance instanceCircle2SharedGoal = new MAPF_Instance("instanceCircle2SharedGoal", mapCircle, new Agent[]{agent12to33, agent33to12, new Agent(20, coor32, coor12)});
        // like a duplicate agent except for the id
        MAPF_Instance instanceCircle2SharedGoalAndStart = new MAPF_Instance("instanceCircle2SharedGoalAndStart", mapCircle, new Agent[]{agent12to33, agent33to12, new Agent(20, coor33, coor12)});

        System.out.println("should find a solution:");
        for (MAPF_Instance testInstance : new MAPF_Instance[]{instanceEmptyPlusSharedGoal1, instanceEmptyPlusSharedGoal2, instanceEmptyPlusSharedGoal3, instanceEmptyPlusSharedGoal4,
                instanceEmptyPlusSharedGoalAndSomeStart1, instanceEmptyPlusSharedGoalAndSomeStart2, instanceEmptyPlusSharedGoalAndSomeStart3, instanceEmptyPlusSharedGoalAndSomeStart4,
                instanceEmptyPlusSharedGoalAndStart1, instanceCircle1SharedGoal, instanceCircle1SharedGoalAndStart, instanceCircle2SharedGoal, instanceCircle2SharedGoalAndStart}){
            System.out.println("testing " + testInstance.name);
            Solution solution = ppSolverSharedGoals.solve(testInstance, new RunParameters(instanceReport));
            assertNotNull(solution);
            assertTrue(solution.solves(testInstance, true, true));
        }

        MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});

        System.out.println("should not find a solution:");
        for (MAPF_Instance testInstance : new MAPF_Instance[]{instanceUnsolvable, this.instanceUnsolvableBecauseOrderWithInfiniteWait}){
            System.out.println("testing " + testInstance.name);
            Solution solution = ppSolverSharedGoals.solve(testInstance, new RunParameters(instanceReport));
            assertNull(solution);
        }
    }


    private Map<String, Map<String, String>> readResultsCSV(String pathToCsv) throws IOException {
        Map<String, Map<String, String>> result  = new HashMap<>();
        BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));

        String headerRow = csvReader.readLine();
        String[] header = headerRow.split(",");
        int fileNameIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if(header[i].equals("File")) {fileNameIndex = i;}
        }

        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] tupleAsArray = row.split(",");
            if(tupleAsArray.length < 1 ) continue;
            Map<String, String> tupleAsMap = new HashMap<>(tupleAsArray.length);
            for (int i = 0; i < tupleAsArray.length; i++) {
                String value = tupleAsArray[i];
                tupleAsMap.put(header[i], value);
            }

            String key = tupleAsArray[fileNameIndex];
            result.put(key, tupleAsMap);
        }
        csvReader.close();

        return result;
    }


}