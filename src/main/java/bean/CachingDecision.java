package bean;

import java.util.HashSet;
import java.util.Map;

public class CachingDecision {
    int timestamp = 0;
    Map<EdgeServer, HashSet<Integer>> cachingState;
    double FIndexQoE;
    double SumQoE;
    double OptimizationObjective;

}
