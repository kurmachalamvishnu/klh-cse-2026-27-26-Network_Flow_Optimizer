package data;

import model.FlowNetwork;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Map;

/**
 * Predefined realistic flow networks for testing, education, and benchmarking.
 * Includes the official Sioux Falls transportation benchmark suite (net, node, trips),
 * as well as the 12-node benchmark and municipal systems.
 */
public class SampleNetworks {

    private static TNTPNetworkLoader.SiouxFallsData cachedSiouxFalls = null;

    public static synchronized TNTPNetworkLoader.SiouxFallsData getSiouxFallsData() {
        if (cachedSiouxFalls == null) {
            try {
                cachedSiouxFalls = TNTPNetworkLoader.loadSiouxFalls();
            } catch (IOException e) {
                System.err.println("Warning: Unable to load Sioux Falls TNTP files: " + e.getMessage());
            }
        }
        return cachedSiouxFalls;
    }

    /**
     * Sioux Falls Point-to-Point Flow Network (default Zone 1 to Zone 24).
     * 24 nodes, 76 physical road links with realistic vehicle capacities.
     */
    public static FlowNetwork createSiouxFallsNetwork() {
        TNTPNetworkLoader.SiouxFallsData data = getSiouxFallsData();
        if (data != null) {
            return data.network.cloneNetwork();
        }
        return create12NodeBenchmarkNetwork(); // fallback
    }

    /**
     * Sioux Falls System-Wide Trip Demand Network.
     * Incorporates all 576 Origin-Destination demands from SiouxFalls_trips.tntp (Total demand: 360,600 vehicles)
     * using a Super-Source and Super-Sink connected to the 76 road segments.
     */
    public static FlowNetwork createSiouxFallsSystemWideNetwork() {
        TNTPNetworkLoader.SiouxFallsData data = getSiouxFallsData();
        if (data != null) {
            return data.buildSystemWideFlowNetwork();
        }
        return create12NodeBenchmarkNetwork();
    }

    public static Map<Integer, Point2D.Double> getSiouxFallsCoordinates() {
        TNTPNetworkLoader.SiouxFallsData data = getSiouxFallsData();
        return data != null ? data.coordinates : null;
    }

    /**
     * 12-Node Benchmark Network.
     * Layered topology with multiple paths and cross-connections.
     * Demonstrates performance differences between Ford-Fulkerson, Edmonds-Karp, and Dinic.
     */
    public static FlowNetwork create12NodeBenchmarkNetwork() {
        FlowNetwork g = new FlowNetwork(12);

        g.setVertexName(0, "Source (S)");
        g.setVertexName(1, "N1");
        g.setVertexName(2, "N2");
        g.setVertexName(3, "N3");
        g.setVertexName(4, "N4");
        g.setVertexName(5, "N5");
        g.setVertexName(6, "N6");
        g.setVertexName(7, "N7");
        g.setVertexName(8, "N8");
        g.setVertexName(9, "N9");
        g.setVertexName(10, "N10");
        g.setVertexName(11, "Sink (T)");

        g.addEdge(0, 1, 15);
        g.addEdge(0, 2, 20);
        g.addEdge(0, 3, 10);

        g.addEdge(1, 4, 10);
        g.addEdge(1, 5, 8);
        g.addEdge(2, 5, 12);
        g.addEdge(2, 6, 10);
        g.addEdge(3, 6, 8);
        g.addEdge(3, 7, 7);

        g.addEdge(4, 5, 4);
        g.addEdge(6, 5, 3);
        g.addEdge(6, 7, 5);

        g.addEdge(4, 8, 9);
        g.addEdge(5, 8, 11);
        g.addEdge(5, 9, 13);
        g.addEdge(6, 9, 8);
        g.addEdge(7, 10, 12);

        g.addEdge(8, 10, 6);
        g.addEdge(9, 10, 7);

        g.addEdge(8, 11, 14);
        g.addEdge(9, 11, 18);
        g.addEdge(10, 11, 15);

        return g;
    }

    /**
     * Telecommunication & Data Center Bandwidth Allocation Network.
     */
    public static FlowNetwork createTelecommunicationsNetwork() {
        FlowNetwork g = new FlowNetwork(8);

        g.setVertexName(0, "Central_DataCenter (S)");
        g.setVertexName(1, "Core_Router_East");
        g.setVertexName(2, "Core_Router_West");
        g.setVertexName(3, "Aggregator_Alpha");
        g.setVertexName(4, "Aggregator_Beta");
        g.setVertexName(5, "Edge_Switch_1");
        g.setVertexName(6, "Edge_Switch_2");
        g.setVertexName(7, "Gateway_Clients (T)");

        g.addEdge(0, 1, 40.0);
        g.addEdge(0, 2, 40.0);
        g.addEdge(1, 3, 25.0);
        g.addEdge(1, 4, 20.0);
        g.addEdge(2, 3, 15.0);
        g.addEdge(2, 4, 30.0);
        g.addEdge(3, 4, 10.0);
        g.addEdge(3, 5, 30.0);
        g.addEdge(4, 6, 35.0);
        g.addEdge(5, 6, 10.0);
        g.addEdge(5, 7, 30.0);
        g.addEdge(6, 7, 40.0);

        return g;
    }

    /**
     * Urban Traffic Network.
     */
    public static FlowNetwork createUrbanTrafficNetwork() {
        FlowNetwork g = new FlowNetwork(6);

        g.setVertexName(0, "Highway_Inlet (S)");
        g.setVertexName(1, "Junction_North");
        g.setVertexName(2, "Junction_South");
        g.setVertexName(3, "Square_Central");
        g.setVertexName(4, "East_Boulevard");
        g.setVertexName(5, "Downtown_Exit (T)");

        g.addEdge(0, 1, 1600);
        g.addEdge(0, 2, 1300);
        g.addEdge(1, 2, 400);
        g.addEdge(1, 3, 1200);
        g.addEdge(2, 1, 100);
        g.addEdge(2, 4, 1400);
        g.addEdge(3, 2, 900);
        g.addEdge(3, 5, 2000);
        g.addEdge(4, 3, 700);
        g.addEdge(4, 5, 400);

        return g;
    }

    /**
     * Municipal Water Distribution Pipeline System.
     */
    public static FlowNetwork createPipelineDistributionNetwork() {
        FlowNetwork g = new FlowNetwork(7);

        g.setVertexName(0, "Main_Reservoir (S)");
        g.setVertexName(1, "Pumping_Station_A");
        g.setVertexName(2, "Pumping_Station_B");
        g.setVertexName(3, "Filtration_Plant");
        g.setVertexName(4, "Storage_Tank_North");
        g.setVertexName(5, "Storage_Tank_South");
        g.setVertexName(6, "City_Supply_Zone (T)");

        g.addEdge(0, 1, 50.0);
        g.addEdge(0, 2, 45.0);
        g.addEdge(1, 3, 35.0);
        g.addEdge(1, 4, 25.0);
        g.addEdge(2, 3, 30.0);
        g.addEdge(2, 5, 20.0);
        g.addEdge(3, 4, 15.0);
        g.addEdge(3, 5, 15.0);
        g.addEdge(4, 6, 40.0);
        g.addEdge(5, 6, 35.0);

        return g;
    }
}
