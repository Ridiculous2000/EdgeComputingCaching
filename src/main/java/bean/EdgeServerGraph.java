package bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EdgeServerGraph {
    int connectionRange = 5;
    int edgeNum;
    ArrayList<EdgeServer> allEdgeServer;
    HashMap<Integer, List<EdgeServer>> serverGraph;

    void initGraph(ArrayList<EdgeServer> allEdgeServer){
        this.allEdgeServer = allEdgeServer;
        for(EdgeServer edgeServer:this.allEdgeServer){
            serverGraph.put(edgeServer.getId(),new ArrayList<EdgeServer>());
        }
    }

    void generateEdge(int edgeNum){
        this.edgeNum = edgeNum;
        generateEdgeByRandom();
        ensureConnectivity();
        
    }

    private void generateEdgeByRandom() {

    }

    private void ensureConnectivity() {

    }

    private boolean judgeConnectivity() {
        return true;
    }






}
