package web;

import algorithms.Dinic;
import algorithms.EdmondsKarp;
import algorithms.FordFulkerson;
import algorithms.MaxFlowAlgorithm;
import benchmark.BenchmarkResult;
import benchmark.BenchmarkRunner;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import data.SampleNetworks;
import data.TNTPNetworkLoader;
import model.FlowEdge;
import model.FlowNetwork;
import model.FlowResult;

import java.awt.Desktop;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Embedded HTTP REST Server for the Network Flow Optimization Web Application.
 * Zero external dependencies — built entirely on Java SE HttpServer.
 */
public class WebServer {
    private final int port;
    private HttpServer server;
    private final File webDir;

    public WebServer(int port) {
        this.port = port;
        this.webDir = resolveWebDir();
    }

    private File resolveWebDir() {
        File dir = new File("web");
        if (dir.exists() && dir.isDirectory()) return dir;
        dir = new File("C:/KL/2-1/DSA-3/NetworkFlowOptimization/web");
        if (dir.exists() && dir.isDirectory()) return dir;
        return new File(".");
    }

    public void start() throws IOException {
        int activePort = port;
        while (true) {
            try {
                server = HttpServer.create(new InetSocketAddress(activePort), 0);
                break;
            } catch (IOException e) {
                if (activePort > port + 10) throw e;
                activePort++;
            }
        }

        // Context Handlers
        server.createContext("/api/scenarios", new ScenariosHandler());
        server.createContext("/api/graph", new GraphHandler());
        server.createContext("/api/solve", new SolveHandler());
        server.createContext("/api/benchmark", new BenchmarkHandler());
        server.createContext("/", new StaticFileHandler(webDir));

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        String url = "http://localhost:" + activePort;
        System.out.println("\n" + "=".repeat(75));
        System.out.println("  🌐 NETWORK FLOW OPTIMIZATION WEB DASHBOARD IS RUNNING!");
        System.out.println("  👉 Open in your browser: " + url);
        System.out.println("=".repeat(75) + "\n");

        openBrowser(url);

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server shutting down...");
        }
    }

    public static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            }
        } catch (Exception e) {
            try {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } catch (Exception ignored) {
            }
        }
    }

    private static FlowNetwork getNetworkByScenario(String scenarioId) {
        if (scenarioId == null) scenarioId = "sioux-falls";
        return switch (scenarioId.toLowerCase()) {
            case "sioux-falls-system" -> SampleNetworks.createSiouxFallsSystemWideNetwork();
            case "benchmark-12" -> SampleNetworks.create12NodeBenchmarkNetwork();
            case "telecom" -> SampleNetworks.createTelecommunicationsNetwork();
            case "traffic" -> SampleNetworks.createUrbanTrafficNetwork();
            case "pipeline" -> SampleNetworks.createPipelineDistributionNetwork();
            default -> SampleNetworks.createSiouxFallsNetwork();
        };
    }

    /**
     * Handler for /api/scenarios
     */
    static class ScenariosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreflight(exchange)) return;
            String json = """
            [
              {"id": "sioux-falls", "name": "Sioux Falls Road Network (24 Nodes, 76 Links) [TNTP Dataset]", "defaultSource": 0, "defaultSink": 23, "isMap": true},
              {"id": "sioux-falls-system", "name": "Sioux Falls System-Wide Demand (360k Trips OD Matrix)", "defaultSource": 24, "defaultSink": 25, "isMap": true},
              {"id": "benchmark-12", "name": "12-Node Benchmark Network (Slide 8)", "defaultSource": 0, "defaultSink": 11, "isMap": false},
              {"id": "telecom", "name": "Telecommunications & Data Center Bandwidth", "defaultSource": 0, "defaultSink": 7, "isMap": false},
              {"id": "traffic", "name": "Urban Traffic Capacity Network (6 Nodes)", "defaultSource": 0, "defaultSink": 5, "isMap": false},
              {"id": "pipeline", "name": "Municipal Water Distribution Pipeline", "defaultSource": 0, "defaultSink": 6, "isMap": false}
            ]
            """;
            sendJsonResponse(exchange, json);
        }
    }

    /**
     * Handler for /api/graph?scenario=<id>
     */
    static class GraphHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreflight(exchange)) return;
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String scenario = params.getOrDefault("scenario", "sioux-falls");
            FlowNetwork net = getNetworkByScenario(scenario);

            Map<Integer, Point2D.Double> coords = scenario.startsWith("sioux-falls")
                    ? SampleNetworks.getSiouxFallsCoordinates()
                    : null;

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"scenario\": \"").append(scenario).append("\",\n");
            sb.append("  \"numVertices\": ").append(net.getNumVertices()).append(",\n");
            sb.append("  \"numEdges\": ").append(net.getNumEdges()).append(",\n");

            // Nodes
            sb.append("  \"nodes\": [\n");
            for (int i = 0; i < net.getNumVertices(); i++) {
                sb.append("    {");
                sb.append("\"id\": ").append(i).append(", ");
                sb.append("\"name\": \"").append(net.getVertexName(i)).append("\"");
                if (coords != null && coords.containsKey(i)) {
                    Point2D.Double pt = coords.get(i);
                    sb.append(", \"x\": ").append(pt.x).append(", \"y\": ").append(pt.y);
                }
                sb.append("}").append(i < net.getNumVertices() - 1 ? "," : "").append("\n");
            }
            sb.append("  ],\n");

            // Edges
            sb.append("  \"edges\": [\n");
            List<FlowEdge> edges = net.getForwardEdges();
            for (int i = 0; i < edges.size(); i++) {
                FlowEdge e = edges.get(i);
                sb.append("    {");
                sb.append("\"from\": ").append(e.getFrom()).append(", ");
                sb.append("\"to\": ").append(e.getTo()).append(", ");
                sb.append("\"capacity\": ").append(e.getCapacity()).append(", ");
                sb.append("\"flow\": ").append(e.getFlow());
                sb.append("}").append(i < edges.size() - 1 ? "," : "").append("\n");
            }
            sb.append("  ]\n");
            sb.append("}\n");

            sendJsonResponse(exchange, sb.toString());
        }
    }

    /**
     * Handler for /api/solve?scenario=<id>&algorithm=<algo>&source=<s>&sink=<t>
     */
    static class SolveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreflight(exchange)) return;
            Map<String, String> params;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                params = parseFormOrJsonBody(body);
            } else {
                params = parseQueryParams(exchange.getRequestURI().getQuery());
            }

            String scenario = params.getOrDefault("scenario", "sioux-falls");
            String algoName = params.getOrDefault("algorithm", "dinic").toLowerCase();
            int s = Integer.parseInt(params.getOrDefault("source", "0"));
            int t = Integer.parseInt(params.getOrDefault("sink", "23"));

            FlowNetwork net = getNetworkByScenario(scenario);
            if (s < 0 || s >= net.getNumVertices() || t < 0 || t >= net.getNumVertices() || s == t) {
                sendErrorResponse(exchange, 400, "Invalid source or sink index.");
                return;
            }

            MaxFlowAlgorithm algo = switch (algoName) {
                case "ford-fulkerson", "ff" -> new FordFulkerson();
                case "edmonds-karp", "ek" -> new EdmondsKarp();
                default -> new Dinic();
            };

            FlowResult result = algo.computeMaxFlow(net, s, t);

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"algorithm\": \"").append(result.getAlgorithmName()).append("\",\n");
            sb.append("  \"complexity\": \"").append(result.getTimeComplexity()).append("\",\n");
            sb.append(String.format(Locale.US, "  \"maxFlow\": %.4f,\n", result.getMaxFlow()));
            sb.append("  \"iterations\": ").append(result.getIterations()).append(",\n");
            sb.append(String.format(Locale.US, "  \"executionTimeMs\": %.4f,\n", result.getExecutionTimeMs()));
            sb.append("  \"executionTimeMicros\": ").append(result.getExecutionTimeNanos() / 1000).append(",\n");

            // Augmenting paths (limit to 30)
            sb.append("  \"augmentingPaths\": [\n");
            List<String> paths = result.getAugmentingPaths();
            int limit = Math.min(30, paths.size());
            for (int i = 0; i < limit; i++) {
                sb.append("    \"").append(paths.get(i).replace("\"", "\\\"")).append("\"")
                        .append(i < limit - 1 ? "," : "").append("\n");
            }
            sb.append("  ],\n");

            // Min-Cut Edges
            sb.append("  \"minCutEdges\": [\n");
            List<FlowEdge> cutEdges = result.getMinCutEdges();
            for (int i = 0; i < cutEdges.size(); i++) {
                FlowEdge ce = cutEdges.get(i);
                sb.append("    {\"from\": ").append(ce.getFrom())
                        .append(", \"to\": ").append(ce.getTo())
                        .append(String.format(Locale.US, ", \"capacity\": %.2f, \"flow\": %.2f}", ce.getCapacity(), ce.getFlow()))
                        .append(i < cutEdges.size() - 1 ? "," : "").append("\n");
            }
            sb.append("  ],\n");

            // Edges with final flow
            sb.append("  \"edges\": [\n");
            List<FlowEdge> allEdges = net.getForwardEdges();
            for (int i = 0; i < allEdges.size(); i++) {
                FlowEdge e = allEdges.get(i);
                sb.append("    {\"from\": ").append(e.getFrom())
                        .append(", \"to\": ").append(e.getTo())
                        .append(String.format(Locale.US, ", \"capacity\": %.2f, \"flow\": %.2f}", e.getCapacity(), e.getFlow()))
                        .append(i < allEdges.size() - 1 ? "," : "").append("\n");
            }
            sb.append("  ]\n");
            sb.append("}\n");

            sendJsonResponse(exchange, sb.toString());
        }
    }

    /**
     * Handler for /api/benchmark?scenario=<id>&source=<s>&sink=<t>
     */
    static class BenchmarkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreflight(exchange)) return;
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String scenario = params.getOrDefault("scenario", "sioux-falls");
            FlowNetwork net = getNetworkByScenario(scenario);

            int s = Integer.parseInt(params.getOrDefault("source", scenario.equals("sioux-falls-system") ? "24" : "0"));
            int t = Integer.parseInt(params.getOrDefault("sink", scenario.equals("sioux-falls-system") ? "25" : String.valueOf(net.getNumVertices() - 1)));

            List<BenchmarkResult> results = BenchmarkRunner.runComparison(net, s, t);

            StringBuilder sb = new StringBuilder();
            sb.append("[\n");
            for (int i = 0; i < results.size(); i++) {
                BenchmarkResult br = results.get(i);
                sb.append("  {");
                sb.append("\"algorithm\": \"").append(br.getAlgorithmName()).append("\", ");
                sb.append("\"complexity\": \"").append(br.getTimeComplexity()).append("\", ");
                sb.append(String.format(Locale.US, "\"maxFlow\": %.4f, ", br.getMaxFlow()));
                sb.append("\"iterations\": ").append(br.getIterations()).append(", ");
                sb.append(String.format(Locale.US, "\"executionTimeMs\": %.4f, ", br.getExecutionTimeMs()));
                sb.append("\"executionTimeMicros\": ").append(br.getExecutionTimeMicros()).append(", ");
                sb.append("\"bottlenecks\": ").append(br.getBottleneckEdgesCount()).append(", ");
                sb.append(String.format(Locale.US, "\"minCutCapacity\": %.4f", br.getMinCutCapacity()));
                sb.append("}").append(i < results.size() - 1 ? "," : "").append("\n");
            }
            sb.append("]\n");

            sendJsonResponse(exchange, sb.toString());
        }
    }

    /**
     * Serves static HTML, CSS, and JS files from web directory.
     */
    static class StaticFileHandler implements HttpHandler {
        private final File baseDir;

        public StaticFileHandler(File baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File(baseDir, path.substring(1));
            if (!file.exists() || file.isDirectory()) {
                sendErrorResponse(exchange, 404, "File not found: " + path);
                return;
            }

            String mime = getMimeType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", mime + "; charset=UTF-8");
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getMimeType(String fileName) {
            if (fileName.endsWith(".html")) return "text/html";
            if (fileName.endsWith(".css")) return "text/css";
            if (fileName.endsWith(".js")) return "application/javascript";
            if (fileName.endsWith(".json")) return "application/json";
            if (fileName.endsWith(".svg")) return "image/svg+xml";
            if (fileName.endsWith(".png")) return "image/png";
            return "text/plain";
        }
    }

    private static boolean handleCorsPreflight(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, *");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, *");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendErrorResponse(HttpExchange exchange, int code, String msg) throws IOException {
        String json = "{\"error\": \"" + msg + "\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, *");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                map.put(pair[0].toLowerCase(), java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static Map<String, String> parseFormOrJsonBody(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        body = body.trim();
        if (body.startsWith("{") && body.endsWith("}")) {
            // Simple key-value regex extractor for JSON
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"(\\w+)\"\\s*:\\s*\"?([^\",}]+)\"?");
            java.util.regex.Matcher m = p.matcher(body);
            while (m.find()) {
                map.put(m.group(1).toLowerCase(), m.group(2).trim());
            }
        } else {
            return parseQueryParams(body);
        }
        return map;
    }
}
