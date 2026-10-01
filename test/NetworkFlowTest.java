package test;

import algorithms.Dinic;
import algorithms.EdmondsKarp;
import algorithms.FordFulkerson;
import data.SampleNetworks;
import data.TNTPNetworkLoader;
import model.FlowEdge;
import model.FlowNetwork;
import model.FlowResult;

/**
 * Automated Test Suite verifying correctness, flow conservation, capacity constraints,
 * and algorithm equivalence for Network Flow Optimization, including the Sioux Falls dataset.
 */
public class NetworkFlowTest {
    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("Running NetworkFlowTest automated verification suite...\n");

        testBasicDiamondNetwork();
        testFlowConservationAndCapacityLimits();
        testMaxFlowMinCutTheorem();
        test12NodeBenchmarkNetworkParity();
        testDisconnectedSink();
        testMultiPathBottlenecks();
        testSiouxFallsDatasetIntegration();

        System.out.println("\n" + "=".repeat(50));
        System.out.printf("Test Results: %d / %d tests passed successfully.\n", testsPassed, testsRun);
        System.out.println("=".repeat(50));

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            System.out.println("  ✔ PASS: " + testName);
            testsPassed++;
        } else {
            System.err.println("  ❌ FAIL: " + testName);
        }
    }

    private static void testBasicDiamondNetwork() {
        FlowNetwork net = new FlowNetwork(4);
        net.addEdge(0, 1, 10);
        net.addEdge(0, 2, 10);
        net.addEdge(1, 2, 2);
        net.addEdge(1, 3, 10);
        net.addEdge(2, 3, 10);

        FlowResult rFF = new FordFulkerson().computeMaxFlow(net.cloneNetwork(), 0, 3);
        FlowResult rEK = new EdmondsKarp().computeMaxFlow(net.cloneNetwork(), 0, 3);
        FlowResult rDinic = new Dinic().computeMaxFlow(net.cloneNetwork(), 0, 3);

        assertTrue("Diamond Network Max Flow = 20 (Ford-Fulkerson)", Math.abs(rFF.getMaxFlow() - 20.0) < 1e-6);
        assertTrue("Diamond Network Max Flow = 20 (Edmonds-Karp)", Math.abs(rEK.getMaxFlow() - 20.0) < 1e-6);
        assertTrue("Diamond Network Max Flow = 20 (Dinic)", Math.abs(rDinic.getMaxFlow() - 20.0) < 1e-6);
    }

    private static void testFlowConservationAndCapacityLimits() {
        FlowNetwork net = SampleNetworks.createTelecommunicationsNetwork();
        int s = 0, t = 7;

        Dinic dinic = new Dinic();
        FlowResult result = dinic.computeMaxFlow(net, s, t);

        boolean capacityValid = true;
        for (FlowEdge edge : net.getForwardEdges()) {
            if (edge.getFlow() < -1e-9 || edge.getFlow() > edge.getCapacity() + 1e-9) {
                capacityValid = false;
                break;
            }
        }
        assertTrue("Capacity Constraints (0 <= flow <= capacity) on all edges", capacityValid);

        boolean conservationValid = true;
        for (int v = 0; v < net.getNumVertices(); v++) {
            if (v == s || v == t) continue;
            double inFlow = 0;
            double outFlow = 0;

            for (FlowEdge edge : net.getForwardEdges()) {
                if (edge.getTo() == v) inFlow += edge.getFlow();
                if (edge.getFrom() == v) outFlow += edge.getFlow();
            }

            if (Math.abs(inFlow - outFlow) > 1e-6) {
                conservationValid = false;
                break;
            }
        }
        assertTrue("Flow Conservation (Sum of Inflow == Sum of Outflow) at every node", conservationValid);
    }

    private static void testMaxFlowMinCutTheorem() {
        FlowNetwork net = SampleNetworks.createUrbanTrafficNetwork();
        int s = 0, t = 5;

        Dinic dinic = new Dinic();
        FlowResult result = dinic.computeMaxFlow(net, s, t);

        double cutCapacity = result.getMinCutEdges().stream().mapToDouble(FlowEdge::getCapacity).sum();
        assertTrue("Max-Flow Min-Cut Theorem (Max Flow == Min Cut Capacity)",
                Math.abs(result.getMaxFlow() - cutCapacity) < 1e-6);
    }

    private static void test12NodeBenchmarkNetworkParity() {
        FlowNetwork net = SampleNetworks.create12NodeBenchmarkNetwork();
        int s = 0, t = 11;

        FlowResult rFF = new FordFulkerson().computeMaxFlow(net.cloneNetwork(), s, t);
        FlowResult rEK = new EdmondsKarp().computeMaxFlow(net.cloneNetwork(), s, t);
        FlowResult rDinic = new Dinic().computeMaxFlow(net.cloneNetwork(), s, t);

        assertTrue("12-Node Network: FF and EK agreement", Math.abs(rFF.getMaxFlow() - rEK.getMaxFlow()) < 1e-6);
        assertTrue("12-Node Network: EK and Dinic agreement", Math.abs(rEK.getMaxFlow() - rDinic.getMaxFlow()) < 1e-6);
        assertTrue("12-Node Network: Dinic iteration efficiency", rDinic.getIterations() <= rEK.getIterations());
    }

    private static void testDisconnectedSink() {
        FlowNetwork net = new FlowNetwork(4);
        net.addEdge(0, 1, 10);
        net.addEdge(2, 3, 10);

        FlowResult result = new Dinic().computeMaxFlow(net, 0, 3);
        assertTrue("Disconnected Sink Yields Max Flow = 0", Math.abs(result.getMaxFlow() - 0.0) < 1e-6);
    }

    private static void testMultiPathBottlenecks() {
        FlowNetwork net = SampleNetworks.createPipelineDistributionNetwork();
        FlowResult rFF = new FordFulkerson().computeMaxFlow(net.cloneNetwork(), 0, 6);
        FlowResult rDinic = new Dinic().computeMaxFlow(net.cloneNetwork(), 0, 6);

        assertTrue("Pipeline Network: Algorithms agree on optimal throughput",
                Math.abs(rFF.getMaxFlow() - rDinic.getMaxFlow()) < 1e-6);
    }

    private static void testSiouxFallsDatasetIntegration() {
        TNTPNetworkLoader.SiouxFallsData sfData = SampleNetworks.getSiouxFallsData();
        assertTrue("Sioux Falls dataset loaded successfully", sfData != null);
        if (sfData == null) return;

        assertTrue("Sioux Falls contains exactly 24 zones/nodes", sfData.numNodes == 24);
        assertTrue("Sioux Falls contains 76 directed road links", sfData.numLinks == 76);
        assertTrue("Sioux Falls geospatial coordinates loaded for 24 nodes", sfData.coordinates.size() == 24);

        double totalOdDemand = 0;
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                totalOdDemand += sfData.odMatrix[i][j];
            }
        }
        assertTrue("Sioux Falls trips OD total demand equals 360,600", Math.abs(totalOdDemand - 360600.0) < 1e-3);

        // Test flow optimization on Sioux Falls: Zone 1 to Zone 24
        FlowNetwork sfNet = sfData.network.cloneNetwork();
        FlowResult rEK = new EdmondsKarp().computeMaxFlow(sfNet.cloneNetwork(), 0, 23);
        FlowResult rDinic = new Dinic().computeMaxFlow(sfNet.cloneNetwork(), 0, 23);

        assertTrue("Sioux Falls Zone 1 -> Zone 24: EK and Dinic agree on Max Flow",
                Math.abs(rEK.getMaxFlow() - rDinic.getMaxFlow()) < 1e-3);
        assertTrue("Sioux Falls Max Flow is strictly positive (> 10000 vehicles)", rDinic.getMaxFlow() > 10000);
    }
}
