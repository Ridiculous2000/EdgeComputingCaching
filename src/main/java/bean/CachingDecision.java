package bean;

import java.util.HashSet;
import java.util.Map;

public class CachingDecision {
    int timestamp = 0;
    Map<EdgeServer, HashSet<PopularData>> cachingState;
    double FIndexQoE;
    double SumQoE;
    double OptimizationObjective;

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public Map<EdgeServer, HashSet<PopularData>> getCachingState() {
        return cachingState;
    }

    public void setCachingState(Map<EdgeServer, HashSet<PopularData>> cachingState) {
        this.cachingState = cachingState;
    }

    public double getFIndexQoE() {
        return FIndexQoE;
    }

    public void setFIndexQoE(double FIndexQoE) {
        this.FIndexQoE = FIndexQoE;
    }

    public double getSumQoE() {
        return SumQoE;
    }

    public void setSumQoE(double sumQoE) {
        SumQoE = sumQoE;
    }

    public double getOptimizationObjective() {
        return OptimizationObjective;
    }

    public void setOptimizationObjective(double optimizationObjective) {
        OptimizationObjective = optimizationObjective;
    }
}
