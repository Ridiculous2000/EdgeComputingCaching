package bean;

import util.AlgorithmUtils;
import util.OtherUtils;

import java.util.*;

public class EdgeServerGraph {
    //每个服务器与最近的 N 个节点建立链接，用于控制图的规模
    int connectionRange = 1;
    int edgeNum;
    ArrayList<EdgeServer> allEdgeServer = new ArrayList<>();
    HashMap<Integer,Integer> edgeServerIdToIndex = new HashMap<>();
    HashMap<Integer, List<EdgeServer>> serverGraph = new HashMap<>();
    Map<Integer,Map<Integer,List<Integer>>> edgeServerDistance=new HashMap<>();
    //HashMap<Integer,Integer> indexToEdgeServerId = new HashMap<>();
    private static final int INF = Integer.MAX_VALUE;

    public void initGraph(ArrayList<EdgeServer> allEdgeServer){
        //赋值
        this.allEdgeServer = allEdgeServer;
        for(EdgeServer edgeServer:this.allEdgeServer){
            serverGraph.put(edgeServer.getId(),new ArrayList<EdgeServer>());
        }
        for(int i=0;i<allEdgeServer.size();i++){
            edgeServerIdToIndex.put(allEdgeServer.get(i).getId(),i);
          //  indexToEdgeServerId.put(i,allEdgeServer.get(i).getId());
        }
        //生成初始边
        generateEdgeByDistance();
        //确保联通
        if(judgeConnectivity()){
            System.out.println("联通");
        }else{
            System.out.println("不联通");
        }
        HashMap<Integer, ArrayList<EdgeServer>> allConnectedComponents = getConnectedComponents();
        ensureConnectivity(allConnectedComponents);
        if(judgeConnectivity()){
            System.out.println("联通");
        }else{
            System.out.println("不联通");
        }
    }

    //返回各个联通分量
    public HashMap<Integer, ArrayList<EdgeServer>> getConnectedComponents() {
        UnionFind uf = new UnionFind(allEdgeServer.size());
        for (Map.Entry<Integer, List<EdgeServer>> entry:serverGraph.entrySet()){
            for(EdgeServer neighbor:entry.getValue()){
                uf.union(edgeServerIdToIndex.get(entry.getKey()),edgeServerIdToIndex.get(neighbor.getId()));
            }
        }
        //压缩一下路径，便于debug
        for(int i=0;i<uf.parent.length;i++){
            uf.parent[i] = uf.find(uf.parent[i]);
        }
        // 使用哈希表记录每个集合的成员列表
        HashMap<Integer, ArrayList<EdgeServer>> allComponents = new HashMap<>();
        for(int i=0;i<allEdgeServer.size();i++){
            int rootId = allEdgeServer.get(uf.find(edgeServerIdToIndex.get(allEdgeServer.get(i).getId()))).getId();
            if(!allComponents.containsKey(rootId)){
                allComponents.put(rootId,new ArrayList<EdgeServer>());
            }
            allComponents.get(rootId).add(allEdgeServer.get(i));
        }
        return allComponents;
    }
    //使用弗洛伊德算法计算各个边缘服务器为x以内的距离
    //返回为<边缘服务器id，<距离，距这个边缘服务器距离的服务器id列表>>
    public void floyd(int x){
        int n=this.allEdgeServer.size();
        int[][] distances = new int[n][n];
        // 初始化距离矩阵
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distances[i][j] = (i == j) ? 0 : INF;
            }
        }
        // 更新邻接表中的边权值到距离矩阵
        for (int i = 0; i < this.allEdgeServer.size(); i++) {
            for (EdgeServer server : this.serverGraph.get(i)) {
                distances[i][edgeServerIdToIndex.get(server.getId())] = 1;
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distances[i][k] != INF && distances[k][j] != INF &&
                            distances[i][j] > distances[i][k] + distances[k][j]) {
                        distances[i][j] = distances[i][k] + distances[k][j];
                    }
                }
            }
        }

        // 构建结果的数据结构
        //Map<Integer, Map<Integer, List<Integer>>> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Map<Integer, List<Integer>> distanceMap = new HashMap<>();
            for (int j = 1; j <= x; j++) {
                List<Integer> nodeList = new ArrayList<>();
                for (int k = 0; k < n; k++) {
                    if (distances[i][k] == j) {
                        nodeList.add(this.allEdgeServer.get(k).getId());
                    }
                }
                distanceMap.put(j, nodeList);
            }
            this.edgeServerDistance.put(this.allEdgeServer.get(i).getId(), distanceMap);
        }
       // return result;
    }
    //就近生成服务器的边
    private void generateEdgeByDistance() {
        for (int i = 0; i < allEdgeServer.size(); i++) {
            // 根据距离建立优先队列
            EdgeServer currentServer = allEdgeServer.get(i);
            PriorityQueue<EdgeServer> closestServers = new PriorityQueue<>(Comparator.comparingDouble(server -> AlgorithmUtils.calculateDistance(currentServer, server)));
            for (int j = 0; j < allEdgeServer.size(); j++) {
                EdgeServer targetServer = allEdgeServer.get(j);
                if (currentServer.getId() == targetServer.getId()) {
                    continue;
                }
                closestServers.offer(targetServer);
            }
            // 添加与最近的三个节点的连接(三个里面，不包含已经连接的节点)
            int closestCount = 0;
            while (!closestServers.isEmpty() && closestCount < this.connectionRange) {
                EdgeServer closestServer = closestServers.poll();
                // 避免重复连接
                if (!serverGraph.get(currentServer.getId()).contains(closestServer)) {
                    serverGraph.get(currentServer.getId()).add(closestServer);
                    serverGraph.get(closestServer.getId()).add(currentServer);
                    closestCount++;
                }
            }
        }
    }

    private void ensureConnectivity(HashMap<Integer, ArrayList<EdgeServer>> allConnectedComponents) {
        Map.Entry<Integer,ArrayList<EdgeServer>> firstComponent = null;
        for(Map.Entry<Integer,ArrayList<EdgeServer>> component:allConnectedComponents.entrySet()){
            firstComponent = component;
            break;
        }
        for(Map.Entry<Integer,ArrayList<EdgeServer>> component:allConnectedComponents.entrySet()){
            int firstComponentId = firstComponent.getKey();
            int componentId = component.getKey();
            if(firstComponentId!=componentId){
                serverGraph.get(firstComponentId).add(allEdgeServer.get(edgeServerIdToIndex.get(componentId)));
                serverGraph.get(componentId).add(allEdgeServer.get(edgeServerIdToIndex.get(firstComponentId)));
            }
        }
    }

    private boolean judgeConnectivity() {
        UnionFind uf = new UnionFind(allEdgeServer.size());
        for (Map.Entry<Integer, List<EdgeServer>> entry:serverGraph.entrySet()){
            for(EdgeServer neighbor:entry.getValue()){
                uf.union(edgeServerIdToIndex.get(entry.getKey()),edgeServerIdToIndex.get(neighbor.getId()));
            }
        }
        return uf.size[uf.find(0)] == allEdgeServer.size();
    }


    public int getConnectionRange() {
        return connectionRange;
    }

    public void setConnectionRange(int connectionRange) {
        this.connectionRange = connectionRange;
    }

    public int getEdgeNum() {
        return edgeNum;
    }

    public void setEdgeNum(int edgeNum) {
        this.edgeNum = edgeNum;
    }

    public ArrayList<EdgeServer> getAllEdgeServer() {
        return allEdgeServer;
    }

    public void setAllEdgeServer(ArrayList<EdgeServer> allEdgeServer) {
        this.allEdgeServer = allEdgeServer;
    }

    public HashMap<Integer, List<EdgeServer>> getServerGraph() {
        return serverGraph;
    }

    public void setServerGraph(HashMap<Integer, List<EdgeServer>> serverGraph) {
        this.serverGraph = serverGraph;
    }
}
