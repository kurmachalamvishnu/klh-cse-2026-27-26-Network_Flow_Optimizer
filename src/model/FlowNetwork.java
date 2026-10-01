package model;

import java.util.*;

/**
 * Represents a flow network graph using adjacency lists.
 * Supports vertex name labels, forward and residual edge management, and cloning.
 */
public class FlowNetwork {
    private final int numVertices;
    private int numEdges;
    private final List<List<FlowEdge>> adj;
    private final List<FlowEdge> forwardEdges;
    private final Map<Integer, String> vertexNames;
    private final Map<String, Integer> nameToVertex;

    public FlowNetwork(int numVertices) {
        if (numVertices <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive");
        }
        this.numVertices = numVertices;
        this.numEdges = 0;
        this.adj = new ArrayList<>(numVertices);
        this.forwardEdges = new ArrayList<>();
        this.vertexNames = new HashMap<>();
        this.nameToVertex = new HashMap<>();

        for (int i = 0; i < numVertices; i++) {
            adj.add(new ArrayList<>());
            String defaultName = "V" + i;
            vertexNames.put(i, defaultName);
            nameToVertex.put(defaultName, i);
        }
    }

    /**
     * Sets a human-readable name for a vertex.
     */
    public void setVertexName(int vertex, String name) {
        validateVertex(vertex);
        String oldName = vertexNames.get(vertex);
        if (oldName != null) {
            nameToVertex.remove(oldName);
        }
        vertexNames.put(vertex, name);
        nameToVertex.put(name, vertex);
    }

    public String getVertexName(int vertex) {
        validateVertex(vertex);
        return vertexNames.getOrDefault(vertex, "V" + vertex);
    }

    public int getVertexByName(String name) {
        Integer v = nameToVertex.get(name);
        if (v == null) {
            throw new NoSuchElementException("Vertex name not found: " + name);
        }
        return v;
    }

    public boolean hasVertexName(String name) {
        return nameToVertex.containsKey(name);
    }

    /**
     * Adds a directed edge with given capacity.
     * Automatically creates and links the reverse residual edge with 0 capacity.
     */
    public FlowEdge addEdge(int from, int to, double capacity) {
        validateVertex(from);
        validateVertex(to);

        FlowEdge forward = new FlowEdge(from, to, capacity);
        FlowEdge backward = new FlowEdge(to, from, 0.0);

        forward.setResidualEdge(backward);
        backward.setResidualEdge(forward);

        adj.get(from).add(forward);
        adj.get(to).add(backward);

        forwardEdges.add(forward);
        numEdges++;

        return forward;
    }

    public FlowEdge addEdge(String fromName, String toName, double capacity) {
        int u = getVertexByName(fromName);
        int v = getVertexByName(toName);
        return addEdge(u, v, capacity);
    }

    public int getNumVertices() {
        return numVertices;
    }

    public int getNumEdges() {
        return numEdges;
    }

    /**
     * Returns all incident edges (both forward and backward residual edges) for vertex v.
     */
    public List<FlowEdge> adj(int vertex) {
        validateVertex(vertex);
        return adj.get(vertex);
    }

    /**
     * Returns all original forward edges in the network.
     */
    public List<FlowEdge> getForwardEdges() {
        return Collections.unmodifiableList(forwardEdges);
    }

    /**
     * Resets all flows in the network to zero.
     */
    public void resetFlows() {
        for (FlowEdge e : forwardEdges) {
            e.reset();
        }
    }

    /**
     * Creates an independent deep copy of the flow network.
     */
    public FlowNetwork cloneNetwork() {
        FlowNetwork copy = new FlowNetwork(this.numVertices);
        for (int i = 0; i < this.numVertices; i++) {
            copy.setVertexName(i, this.getVertexName(i));
        }
        for (FlowEdge e : this.forwardEdges) {
            copy.addEdge(e.getFrom(), e.getTo(), e.getCapacity());
        }
        return copy;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= numVertices) {
            throw new IndexOutOfBoundsException("Vertex " + v + " is out of bounds [0, " + (numVertices - 1) + "]");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("FlowNetwork (Vertices: %d, Edges: %d)\n", numVertices, numEdges));
        for (FlowEdge edge : forwardEdges) {
            sb.append(String.format("  [%s -> %s] Cap: %.2f, Flow: %.2f\n",
                    getVertexName(edge.getFrom()),
                    getVertexName(edge.getTo()),
                    edge.getCapacity(),
                    edge.getFlow()));
        }
        return sb.toString();
    }
}
