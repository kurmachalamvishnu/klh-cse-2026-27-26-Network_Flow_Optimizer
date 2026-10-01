import benchmark.BenchmarkResult;
import benchmark.BenchmarkRunner;
import data.SampleNetworks;
import model.FlowNetwork;
import ui.ConsoleApp;
import ui.NetworkFlowVisualizerGUI;
import web.WebServer;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Main application launcher for Network Flow Optimization System.
 * Defaults to starting the embedded Web Server and launching the browser dashboard.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            switch (arg) {
                case "--cli" -> {
                    ConsoleApp app = new ConsoleApp();
                    app.start();
                    return;
                }
                case "--gui" -> {
                    SwingUtilities.invokeLater(() -> {
                        FlowNetwork net = SampleNetworks.createSiouxFallsNetwork();
                        NetworkFlowVisualizerGUI gui = new NetworkFlowVisualizerGUI(net, 0, 23);
                        gui.setVisible(true);
                    });
                    return;
                }
                case "--benchmark" -> {
                    runAllBenchmarks();
                    return;
                }
                case "--help" -> {
                    printUsage();
                    return;
                }
            }
        }

        // Default: Start Web Server and auto-open browser website!
        try {
            WebServer server = new WebServer(8080);
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start Web Server: " + e.getMessage());
            System.out.println("Falling back to interactive console application...");
            ConsoleApp app = new ConsoleApp();
            app.start();
        }
    }

    private static void printUsage() {
        System.out.println("Network Flow Optimization System - Launch Options:");
        System.out.println("  java -cp bin Main              Launch Interactive Website in Browser (Default)");
        System.out.println("  java -cp bin Main --cli        Launch Interactive Terminal Console Menu");
        System.out.println("  java -cp bin Main --gui        Launch Desktop Swing GUI Visualizer");
        System.out.println("  java -cp bin Main --benchmark  Run Automated Benchmark Comparisons");
    }

    private static void runAllBenchmarks() {
        System.out.println("================================================================================");
        System.out.println("       RUNNING BENCHMARK SUITE: SIOUX FALLS DATASET & BASELINES");
        System.out.println("================================================================================");

        FlowNetwork sfNet = SampleNetworks.createSiouxFallsNetwork();
        List<BenchmarkResult> resSF = BenchmarkRunner.runComparison(sfNet, 0, 23);
        BenchmarkRunner.printComparisonTable("Sioux Falls Road Network (Zone 1 -> Zone 24: 76 Links)", resSF);

        FlowNetwork sfSysNet = SampleNetworks.createSiouxFallsSystemWideNetwork();
        List<BenchmarkResult> resSFSys = BenchmarkRunner.runComparison(sfSysNet, 24, 25);
        BenchmarkRunner.printComparisonTable("Sioux Falls System-Wide Demand (360k OD Trips)", resSFSys);

        FlowNetwork net1 = SampleNetworks.create12NodeBenchmarkNetwork();
        List<BenchmarkResult> res1 = BenchmarkRunner.runComparison(net1, 0, 11);
        BenchmarkRunner.printComparisonTable("12-Node Benchmark Network (Slide 8)", res1);

        FlowNetwork net2 = SampleNetworks.createTelecommunicationsNetwork();
        List<BenchmarkResult> res2 = BenchmarkRunner.runComparison(net2, 0, 7);
        BenchmarkRunner.printComparisonTable("Telecommunication & Data Center Bandwidth", res2);
    }
}
