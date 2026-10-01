package ui;

import algorithms.Dinic;
import algorithms.EdmondsKarp;
import algorithms.FordFulkerson;
import algorithms.MaxFlowAlgorithm;
import data.SampleNetworks;
import model.FlowEdge;
import model.FlowNetwork;
import model.FlowResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swing Graphical Visualizer for Network Flow Optimization.
 * Renders graph nodes, directed edges with capacities/flows, and highlights Min-Cut bottlenecks.
 * Features full geospatial layout rendering for the Sioux Falls benchmark dataset!
 */
public class NetworkFlowVisualizerGUI extends JFrame {
    private FlowNetwork network;
    private int source;
    private int sink;
    private FlowResult currentResult;
    private Map<Integer, Point2D.Double> geoCoords;

    private final GraphCanvas canvas;
    private final JTextArea logArea;
    private final JLabel statusLabel;
    private final JComboBox<String> scenarioCombo;
    private final JComboBox<String> algorithmCombo;
    private final JSpinner sourceSpinner;
    private final JSpinner sinkSpinner;

    public NetworkFlowVisualizerGUI(FlowNetwork initialNetwork, int initialSource, int initialSink) {
        super("Network Flow Optimization System - Sioux Falls & Benchmarks (DSA-3)");
        this.network = initialNetwork != null ? initialNetwork : SampleNetworks.createSiouxFallsNetwork();
        this.source = initialSource;
        this.sink = initialSink;
        this.geoCoords = SampleNetworks.getSiouxFallsCoordinates();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        toolbar.setBackground(new Color(245, 247, 250));

        toolbar.add(new JLabel("Scenario:"));
        scenarioCombo = new JComboBox<>(new String[]{
                "Sioux Falls (24 Nodes, 76 Links) [TNTP Dataset]",
                "Sioux Falls System-Wide Demand (360k Trips OD Matrix)",
                "12-Node Benchmark Network (Slide 8)",
                "Telecommunications & Data Center",
                "Urban Traffic Network",
                "Water Pipeline System"
        });
        scenarioCombo.addActionListener(e -> onScenarioChanged());
        toolbar.add(scenarioCombo);

        toolbar.add(new JLabel("Source S:"));
        sourceSpinner = new JSpinner(new SpinnerNumberModel(source + 1, 1, 50, 1));
        sourceSpinner.setPreferredSize(new Dimension(50, 24));
        sourceSpinner.addChangeListener(e -> onSourceSinkChanged());
        toolbar.add(sourceSpinner);

        toolbar.add(new JLabel("Sink T:"));
        sinkSpinner = new JSpinner(new SpinnerNumberModel(sink + 1, 1, 50, 1));
        sinkSpinner.setPreferredSize(new Dimension(50, 24));
        sinkSpinner.addChangeListener(e -> onSourceSinkChanged());
        toolbar.add(sinkSpinner);

        toolbar.add(new JLabel("Algorithm:"));
        algorithmCombo = new JComboBox<>(new String[]{
                "Dinic's Algorithm (O(V²·E))",
                "Edmonds-Karp BFS (O(V·E²))",
                "Ford-Fulkerson DFS (O(E·max_flow))"
        });
        toolbar.add(algorithmCombo);

        JButton computeBtn = new JButton("Compute Max Flow");
        computeBtn.setBackground(new Color(33, 150, 243));
        computeBtn.setForeground(Color.WHITE);
        computeBtn.setFocusPainted(false);
        computeBtn.addActionListener(e -> runSelectedAlgorithm());
        toolbar.add(computeBtn);

        JButton resetBtn = new JButton("Reset Flow");
        resetBtn.addActionListener(e -> resetNetworkFlows());
        toolbar.add(resetBtn);

        root.add(toolbar, BorderLayout.NORTH);

        // Center: Graph Canvas
        canvas = new GraphCanvas();
        root.add(canvas, BorderLayout.CENTER);

        // Bottom: Status and Output Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        statusLabel = new JLabel(" Ready. Select scenario and click 'Compute Max Flow'.");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 249, 250));
        JScrollPane scrollPane = new JScrollPane(logArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        root.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(root);

        // Initialize with default
        onScenarioChanged();
    }

    private void onSourceSinkChanged() {
        int s = (Integer) sourceSpinner.getValue() - 1;
        int t = (Integer) sinkSpinner.getValue() - 1;
        if (s >= 0 && s < network.getNumVertices() && t >= 0 && t < network.getNumVertices() && s != t) {
            this.source = s;
            this.sink = t;
            runSelectedAlgorithm();
        }
    }

    private void onScenarioChanged() {
        int idx = scenarioCombo.getSelectedIndex();
        switch (idx) {
            case 0 -> {
                network = SampleNetworks.createSiouxFallsNetwork();
                source = 0;   // Zone 1
                sink = 23;    // Zone 24
                geoCoords = SampleNetworks.getSiouxFallsCoordinates();
            }
            case 1 -> {
                network = SampleNetworks.createSiouxFallsSystemWideNetwork();
                source = 24;  // Super-Source
                sink = 25;    // Super-Sink
                geoCoords = SampleNetworks.getSiouxFallsCoordinates();
            }
            case 2 -> {
                network = SampleNetworks.create12NodeBenchmarkNetwork();
                source = 0;
                sink = 11;
                geoCoords = null;
            }
            case 3 -> {
                network = SampleNetworks.createTelecommunicationsNetwork();
                source = 0;
                sink = 7;
                geoCoords = null;
            }
            case 4 -> {
                network = SampleNetworks.createUrbanTrafficNetwork();
                source = 0;
                sink = 5;
                geoCoords = null;
            }
            case 5 -> {
                network = SampleNetworks.createPipelineDistributionNetwork();
                source = 0;
                sink = 6;
                geoCoords = null;
            }
        }

        sourceSpinner.setValue(source + 1);
        sinkSpinner.setValue(sink + 1);
        ((SpinnerNumberModel) sourceSpinner.getModel()).setMaximum(network.getNumVertices());
        ((SpinnerNumberModel) sinkSpinner.getModel()).setMaximum(network.getNumVertices());

        runSelectedAlgorithm();
    }

    private void runSelectedAlgorithm() {
        int idx = algorithmCombo.getSelectedIndex();
        MaxFlowAlgorithm algo = switch (idx) {
            case 0 -> new Dinic();
            case 1 -> new EdmondsKarp();
            case 2 -> new FordFulkerson();
            default -> new Dinic();
        };

        currentResult = algo.computeMaxFlow(network, source, sink);

        statusLabel.setText(String.format(" ✔ [%s] Max Flow: %.2f | Iterations/Phases: %d | Time: %.3f ms | Bottlenecks: %d edges",
                network.getVertexName(source) + " -> " + network.getVertexName(sink),
                currentResult.getMaxFlow(),
                currentResult.getIterations(),
                currentResult.getExecutionTimeMs(),
                currentResult.getMinCutEdges().size()));

        StringBuilder log = new StringBuilder();
        log.append(currentResult.getFormattedSummary(network));
        log.append("\nRecent Augmentations:\n");
        List<String> paths = currentResult.getAugmentingPaths();
        int limit = Math.min(10, paths.size());
        for (int i = 0; i < limit; i++) {
            log.append("  ").append(paths.get(i)).append("\n");
        }
        if (paths.size() > limit) {
            log.append(String.format("  ... (%d more augmentation steps)\n", paths.size() - limit));
        }
        logArea.setText(log.toString());
        logArea.setCaretPosition(0);

        canvas.recomputeLayout();
        canvas.repaint();
    }

    private void resetNetworkFlows() {
        network.resetFlows();
        currentResult = null;
        statusLabel.setText(" Flows reset to 0.");
        logArea.setText("Flows reset. Edges are ready for new computation.\n");
        canvas.repaint();
    }

    private class GraphCanvas extends JPanel {
        private final Map<Integer, Point> nodePositions = new HashMap<>();

        public GraphCanvas() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1));
        }

        public void recomputeLayout() {
            nodePositions.clear();
            int n = network.getNumVertices();
            int width = Math.max(getWidth(), 800);
            int height = Math.max(getHeight(), 450);
            int padding = 60;

            if (geoCoords != null && !geoCoords.isEmpty()) {
                // Use actual geospatial coordinates from SiouxFalls_node.tntp
                double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
                double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

                for (Point2D.Double pt : geoCoords.values()) {
                    minX = Math.min(minX, pt.x);
                    maxX = Math.max(maxX, pt.x);
                    minY = Math.min(minY, pt.y);
                    maxY = Math.max(maxY, pt.y);
                }

                for (int i = 0; i < n; i++) {
                    Point2D.Double pt = geoCoords.get(i);
                    if (pt != null) {
                        int x = padding + (int) ((pt.x - minX) / (maxX - minX) * (width - 2 * padding));
                        // Invert Y axis because latitude goes north
                        int y = height - padding - (int) ((pt.y - minY) / (maxY - minY) * (height - 2 * padding));
                        nodePositions.put(i, new Point(x, y));
                    }
                }

                // If super-source / super-sink exist
                if (n > 24) {
                    nodePositions.put(24, new Point(padding / 2, height / 2));          // Super Source
                    nodePositions.put(25, new Point(width - padding / 2, height / 2));  // Super Sink
                }
            } else {
                // Layered layout approximation for other benchmark graphs
                nodePositions.put(source, new Point(padding, height / 2));
                nodePositions.put(sink, new Point(width - padding, height / 2));

                int middleCount = n - 2;
                int cols = Math.max(1, (int) Math.ceil(Math.sqrt(middleCount)));
                int colWidth = (width - 2 * padding - 100) / Math.max(1, cols);

                int currentMid = 0;
                for (int i = 0; i < n; i++) {
                    if (i == source || i == sink) continue;
                    int col = currentMid % cols;
                    int row = currentMid / cols;
                    int rowsInCol = (middleCount + cols - 1) / cols;

                    int x = padding + 60 + col * colWidth;
                    int y = 60 + (row + 1) * (height - 120) / (rowsInCol + 1);
                    nodePositions.put(i, new Point(x, y));
                    currentMid++;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (nodePositions.size() != network.getNumVertices()) {
                recomputeLayout();
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw Edges
            for (FlowEdge edge : network.getForwardEdges()) {
                Point p1 = nodePositions.get(edge.getFrom());
                Point p2 = nodePositions.get(edge.getTo());
                if (p1 == null || p2 == null) continue;

                boolean isBottleneck = currentResult != null &&
                        currentResult.getMinCutEdges().stream().anyMatch(e -> e.getFrom() == edge.getFrom() && e.getTo() == edge.getTo());

                if (isBottleneck) {
                    g2.setColor(new Color(220, 38, 38)); // Red for Min-Cut bottleneck
                    g2.setStroke(new BasicStroke(3.0f));
                } else if (edge.getFlow() > 1e-9) {
                    g2.setColor(new Color(37, 99, 235)); // Blue for active flow
                    g2.setStroke(new BasicStroke(2.0f));
                } else {
                    g2.setColor(new Color(210, 218, 228)); // Gray for idle link
                    g2.setStroke(new BasicStroke(1.2f));
                }

                drawArrow(g2, p1.x, p1.y, p2.x, p2.y);

                // Label edge flow / capacity
                if (edge.getFlow() > 1e-9 || isBottleneck || network.getNumVertices() <= 12) {
                    int midX = (p1.x * 2 + p2.x * 3) / 5;
                    int midY = (p1.y * 2 + p2.y * 3) / 5;

                    String text = String.format("%.0f/%.0f", edge.getFlow(), edge.getCapacity());
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    int textW = fm.stringWidth(text);

                    g2.setColor(new Color(255, 255, 255, 220));
                    g2.fillRoundRect(midX - textW / 2 - 2, midY - 11, textW + 4, 14, 4, 4);

                    if (isBottleneck) {
                        g2.setColor(new Color(185, 28, 28));
                    } else if (edge.getFlow() > 1e-9) {
                        g2.setColor(new Color(30, 64, 175));
                    } else {
                        g2.setColor(new Color(100, 116, 139));
                    }
                    g2.drawString(text, midX - textW / 2, midY);
                }
            }

            // Draw Nodes
            int radius = 18;
            for (int i = 0; i < network.getNumVertices(); i++) {
                Point p = nodePositions.get(i);
                if (p == null) continue;

                if (i == source) {
                    g2.setColor(new Color(34, 197, 94)); // Green: Source
                } else if (i == sink) {
                    g2.setColor(new Color(239, 68, 68)); // Red: Sink
                } else if (currentResult != null && currentResult.getSourceCutSet() != null && currentResult.getSourceCutSet().contains(i)) {
                    g2.setColor(new Color(191, 219, 254)); // Light Blue
                } else {
                    g2.setColor(new Color(241, 245, 249));
                }

                g2.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                g2.setColor(new Color(51, 65, 85));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

                // Label node number (1-based for Sioux Falls)
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                String idStr = (geoCoords != null && i < 24) ? String.valueOf(i + 1) : String.valueOf(i);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(idStr, p.x - fm.stringWidth(idStr) / 2, p.y + 4);
            }

            drawLegend(g2);
        }

        private void drawLegend(Graphics2D g2) {
            int lx = 15;
            int ly = 15;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(lx, ly, 250, 85, 8, 8);
            g2.setColor(new Color(203, 213, 225));
            g2.drawRoundRect(lx, ly, 250, 85, 8, 8);

            g2.setColor(new Color(34, 197, 94));
            g2.fillOval(lx + 8, ly + 8, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString("Source S (" + network.getVertexName(source) + ")", lx + 26, ly + 18);

            g2.setColor(new Color(239, 68, 68));
            g2.fillOval(lx + 8, ly + 26, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString("Sink T (" + network.getVertexName(sink) + ")", lx + 26, ly + 36);

            g2.setColor(new Color(220, 38, 38));
            g2.fillRect(lx + 8, ly + 46, 14, 4);
            g2.setColor(Color.BLACK);
            g2.drawString("Min-Cut Bottleneck Link (Saturated)", lx + 26, ly + 52);

            g2.setColor(new Color(37, 99, 235));
            g2.fillRect(lx + 8, ly + 62, 14, 3);
            g2.setColor(Color.BLACK);
            g2.drawString("Active Traffic Flow", lx + 26, ly + 68);
        }

        private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            double dx = x2 - x1, dy = y2 - y1;
            double angle = Math.atan2(dy, dx);
            int nodeRadius = 18;

            int startX = (int) (x1 + nodeRadius * Math.cos(angle));
            int startY = (int) (y1 + nodeRadius * Math.sin(angle));
            int endX = (int) (x2 - nodeRadius * Math.cos(angle));
            int endY = (int) (y2 - nodeRadius * Math.sin(angle));

            g2.drawLine(startX, startY, endX, endY);

            int arrowSize = 8;
            AffineTransform tx = new AffineTransform();
            tx.translate(endX, endY);
            tx.rotate(angle);

            Polygon arrow = new Polygon();
            arrow.addPoint(0, 0);
            arrow.addPoint(-arrowSize, -arrowSize / 2);
            arrow.addPoint(-arrowSize, arrowSize / 2);

            Shape oldShape = g2.getClip();
            AffineTransform oldTx = g2.getTransform();
            g2.transform(tx);
            g2.fill(arrow);
            g2.setTransform(oldTx);
        }
    }
}
