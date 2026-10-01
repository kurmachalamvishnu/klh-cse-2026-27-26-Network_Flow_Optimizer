package model;

/**
 * Represents a directed edge in a flow network with residual capacities.
 * Encapsulates the forward edge and links to its residual (reverse) edge.
 */
public class FlowEdge {
    private final int from;
    private final int to;
    private final double capacity;
    private double flow;
    private FlowEdge residualEdge;

    /**
     * Constructs a FlowEdge with given source, destination, and capacity.
     */
    public FlowEdge(int from, int to, double capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Edge capacity must be non-negative");
        }
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = 0.0;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getFlow() {
        return flow;
    }

    public void setFlow(double flow) {
        this.flow = flow;
    }

    public FlowEdge getResidualEdge() {
        return residualEdge;
    }

    public void setResidualEdge(FlowEdge residualEdge) {
        this.residualEdge = residualEdge;
    }

    /**
     * Returns the other endpoint of the edge given one endpoint.
     */
    public int other(int vertex) {
        if (vertex == from) return to;
        if (vertex == to) return from;
        throw new IllegalArgumentException("Invalid vertex: " + vertex);
    }

    /**
     * Returns residual capacity of edge in direction to given vertex.
     */
    public double residualCapacityTo(int vertex) {
        if (vertex == from) {
            // Backward edge residual capacity is the flow already pushed
            return flow;
        } else if (vertex == to) {
            // Forward edge residual capacity is (capacity - flow)
            return capacity - flow;
        } else {
            throw new IllegalArgumentException("Invalid vertex endpoint: " + vertex);
        }
    }

    /**
     * Augments flow along the edge toward the specified vertex.
     */
    public void addResidualFlowTo(int vertex, double delta) {
        if (vertex == from) {
            // Decreasing flow in forward direction is equivalent to pushing backward
            flow -= delta;
        } else if (vertex == to) {
            // Increasing flow in forward direction
            flow += delta;
        } else {
            throw new IllegalArgumentException("Invalid vertex endpoint: " + vertex);
        }

        // Keep residual edge synced
        if (residualEdge != null) {
            residualEdge.flow = -this.flow;
        }
    }

    /**
     * Resets the flow along this edge to zero.
     */
    public void reset() {
        this.flow = 0.0;
        if (residualEdge != null) {
            residualEdge.flow = 0.0;
        }
    }

    /**
     * Checks if this edge is saturated (flow reaches full capacity).
     */
    public boolean isSaturated() {
        return Math.abs(capacity - flow) < 1e-9 && capacity > 0;
    }

    @Override
    public String toString() {
        return String.format("%d -> %d (flow: %.2f / cap: %.2f)", from, to, flow, capacity);
    }
}
