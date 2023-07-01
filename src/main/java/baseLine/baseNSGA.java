package baseLine;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.IOException;
import java.util.*;

public class baseNSGA {
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    List<bean.Request> Request;
    //HashMap<Integer, List<EdgeServer>>
    EdgeServerGraph edgeServerGraph=new EdgeServerGraph();
    Map<Integer,double[]> dataVectorMap;
    //用户最近的服务器
    Map<Integer,Integer> useredge;
    //  Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    //缓存决策
    //CachingDecision cachingDecision = new CachingDecision();
    //对于每一个时间戳，都有一个服务器群的存储数据状态
    Map<Integer,List<EdgeServer>> edgeCondition;
    AlgorithmUtils algorithmUtils;
    List<Request> requests ;
    //int x,int minsize,int maxspace,int beginTimestamp,int endTimestamp,int iterations
    int x;
    int minsize;
    int maxspace;
    int itrations;
    public baseNSGA() throws IOException {
    }
    public void initializeData(ExperimentalSetup experimentalSetup) throws IOException {
        this.algorithmUtils = new AlgorithmUtils(experimentalSetup);
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.Request = DBUtils.getAllRequestByTime("request",experimentalSetup.getBeginTimestamp(),experimentalSetup.getEndTimestamp());
        this.useredge= algorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        this.x=experimentalSetup.getX();
        this.minsize=experimentalSetup.getMinsDataSize();
        this.maxspace=experimentalSetup.getMaxStorageSpace();
        this.itrations=experimentalSetup.getItrations();
        this.minsize=experimentalSetup.minsDataSize;
        this.maxspace=experimentalSetup.getMaxStorageSpace();
        this.requests= DBUtils.getAllRequestByTime("request", 1, 100);
        experiment(this.x,this.minsize,this.maxspace,experimentalSetup.getBeginTimestamp(),experimentalSetup.getEndTimestamp(),this.itrations);
        //generateEdgeCondition(beginTimestamp,endTimestamp);
        //initCachingDecision(beginTimestamp,endTimestamp);
    }
    //缓存数据随机放置 输入为一个服务器组，输出为存储数据后的服务器组
    public List<EdgeServer> randomCaching(List<EdgeServer> edgeServers){
        for(EdgeServer server:edgeServers){
            Random random = new Random();
            List<PopularData> pds=new ArrayList<PopularData>();
            //设置一个流行数据表，标记是否被某个边缘服务器放入，不能多次放入
            Map<Integer, Boolean> populardataCondition=new HashMap<Integer, Boolean>();
            for(PopularData popularData:this.experimentalPopularData){
                populardataCondition.put(popularData.getId(),false);
            }
            //每次随机找10次数据填入边缘服务器，若无法填满则停止添加
            int count=0;
            while(server.getRemainingStorageSpace()>=1&&count<10&&experimentalPopularData.size()>0){

                int randomIndex =random.nextInt(experimentalPopularData.size());
                PopularData pd=experimentalPopularData.get(randomIndex);
                count++;
                if(pd.getSize()>server.getRemainingStorageSpace()||populardataCondition.get(pd.getId())){
                    continue;
                }else{
                    pds.add(findById(pd.getId()));
                    server.setRemainingStorageSpace(server.getRemainingStorageSpace()-pd.getSize());
                    populardataCondition.put(pd.getId(),true);
                }
            }
            server.setCachedDataList((ArrayList<PopularData>) pds);
        }
        return edgeServers;
    }
    //查找对应id的数据 输入为数据id，输出为该数据实体
    public PopularData findById(int pid){
        PopularData pd=new PopularData();
        for(PopularData popularData:this.experimentalPopularData){
            if(popularData.getId()==pid){
                pd=popularData;
                break;
            }
        }
        return pd;
    }
    //将服务器组转换为存储决策类，输入为一个服务器组，输出为一个存储决策类
    public  CachingDecision initCachingDecision(List<EdgeServer> servers) {
        //保存第一步的最优解
        CachingDecision cachingDecision = new CachingDecision();
        Map<EdgeServer, HashSet<PopularData>> cachingResult = new HashMap<>();
        for (EdgeServer edgeServer : servers) {
            ArrayList<PopularData> dataList = edgeServer.getCachedDataList();
            if (cachingResult.get(edgeServer) == null) {
                cachingResult.put(edgeServer, new HashSet<>());
            }
            for (PopularData popularData : dataList) {
                cachingResult.get(edgeServer).add(popularData);
            }
        }
        cachingDecision.setCachingState(cachingResult);
        return cachingDecision;
    }
    public List<Request> findRequests(int beginTimestamp,int endTimestamp){
        List<Request> requests=new ArrayList<Request>();
        for(Request rq:this.requests){
            Request temprq=new Request();
            if(rq.getTimestamp()>=beginTimestamp&&rq.getTimestamp()<=endTimestamp){
                temprq=rq;
                requests.add(temprq);
            }
        }
        return requests;
    }
    //计算适应度 输入为时间戳，存储决策类，输出为该决策的适应度
    public double calFitness(int timeStamp,CachingDecision cachingDecision)
    {
        List<Request> requests = findRequests(timeStamp, timeStamp);
        double maxSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
        double finalSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
        double finalFIndex = algorithmUtils.cacheDecisionFIndex(cachingDecision, (ArrayList<bean.Request>) requests);
        double result = algorithmUtils.cacheDecisionFinalValue(cachingDecision, (ArrayList<bean.Request>) requests);
        cachingDecision.setFIndexQoE(finalFIndex);
        cachingDecision.setOptimizationObjective(result);
      //  System.out.println("Timestamp" + timeStamp + " SumQoE: " + finalSumQoE + " FIndex: " + finalFIndex + "FinalValue: " + result);
        return result;
    }
    //打印实验结果，输入为时间戳，存储决策，输出为当前时间戳下该决策的实验结果
    public void printQoe(int timeStamp,CachingDecision cachingDecision)
    {
        List<Request> requests = DBUtils.getAllRequestByTime("request", timeStamp, timeStamp);

        double maxSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
        double finalSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
        double finalFIndex = algorithmUtils.cacheDecisionFIndex(cachingDecision, (ArrayList<bean.Request>) requests);
        double result = algorithmUtils.cacheDecisionFinalValue(cachingDecision, (ArrayList<bean.Request>) requests);
        cachingDecision.setFIndexQoE(finalFIndex);
        cachingDecision.setOptimizationObjective(result);
        System.out.println("Timestamp" + timeStamp + " SumQoE: " + finalSumQoE + " FIndex: " + finalFIndex + "FinalValue: " + result);
    }
    //初始化种群
    //cachingdecision为随机出来的选择策略，x为定义的初始种群个体数，minsize为所有数据中的最小size
    //maxspace为边缘服务器的最大存储容量
    public int[][] initPopulation(CachingDecision cachingDecision,int x,int minsize,int maxspace) {
        Map<EdgeServer, HashSet<PopularData>> cachingstate = cachingDecision.getCachingState();
        int n = maxspace / minsize;
        int length = n * cachingstate.size();
        int[][] population = new int[x][length];
        for(int i=0;i<x;i++){
            int index = 0;
            for (int j = 0; j < cachingstate.size(); j++) {
                Iterator<Map.Entry<EdgeServer, HashSet<PopularData>>> iterator = cachingstate.entrySet().iterator();
                while (iterator.hasNext()) {
                    HashSet<PopularData> pds = iterator.next().getValue();
                    for(PopularData pd:pds){
                        population[i][index++]=pd.getId();
                    }
                    for(int k=0;k<(n-pds.size());k++){
                        population[i][index++]=0;
                    }
                }
            }
        }
        return population;
    }
    //将种群中的一个个体转换为一个存储决策类
    public CachingDecision populationToCachingDecision(int[] individual,int x,int minsize,int maxspace,int edgenum){
        CachingDecision cachingDecision=new CachingDecision();
        Map<EdgeServer, HashSet<PopularData>> cachingstate = new HashMap<EdgeServer, HashSet<PopularData>>();
        List<EdgeServer> edgeServers=new ArrayList<EdgeServer>();
        edgeServers=this.experimentalEdgeServer;
        int index=0;
        int n=maxspace / minsize;
        for(EdgeServer es:edgeServers){
            HashSet<PopularData> pds=new HashSet<PopularData>();
            for(int i=0;i<n;i++){
                if(individual[i]!=0){
                    pds.add(findDataById(individual[i]));
                }
            }
            cachingstate.put(es,pds);
        }
        cachingDecision.setCachingState(cachingstate);
        return cachingDecision;
    }
    //将一个种群的适应度最高的个体选出，并转换为存储决策类
    public CachingDecision findMaxFitnessIndividual(int[][] population,int x,int timestamp,int minsize,int maxspace,int edgenum){
        double maxfitness=0;
        CachingDecision cachingDecision=new CachingDecision();
        for(int i=0;i<x;i++){
            CachingDecision tempcachingDecision= populationToCachingDecision(population[i],x,minsize,maxspace,edgenum);
            double fitness=calFitness(timestamp,tempcachingDecision);
           // calFitness(timestamp,population[i]);
            if(fitness>maxfitness){
                maxfitness=fitness;
                cachingDecision=tempcachingDecision;
            }
        }
        return cachingDecision;
    }
    //根据数据id，返回一个数据实体
    public PopularData findDataById(int pid){
        PopularData pd=new PopularData();
        for(PopularData popularData:this.experimentalPopularData){
            if(popularData.getId()==pid){
                pd=popularData;
                break;
            }
        }
        return pd;
    }
    //计算当前该种群的各个个体的适应度
    public Map<Integer,Double> calAllFitness(int[][] population,int timestamp,int x,int minsize,int maxspace){
        Map<Integer,Double> allFitness=new HashMap<Integer, Double>();
        for(int i=0;i<x;i++){
            CachingDecision cachingDecision=populationToCachingDecision(population[i],x,minsize,maxspace,this.experimentalEdgeServer.size());
            double fitness=calFitness(timestamp,cachingDecision);
            allFitness.put(i,fitness);
        }
        return allFitness;
    }
    //锦标赛算法选择 输入为一个该种群所有个体索引及适应度值的map，和锦标赛大小,size为每次参加锦标赛的个体数，parent为应选择出作为父代的个体数 x为种群个体数
    //输出为一个存父代的索引和适应度的数组
    public Map<Integer,Double> tournamentparents(Map<Integer,Double> allFitness,int size,int parent,int x){
       // PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        boolean[] selected=new boolean[x];
        for(int i=0;i<x;i++)
            selected[i]=false;
        Map<Integer,Double> tourparents=new HashMap<Integer, Double>();
        for(int i=0;i<parent;i++){
            Set<Integer> keySet = allFitness.keySet();
            List<Integer> indexList = new ArrayList<>(keySet);
            Map<Integer, Double> selectedFitness = selectRandomFitness(allFitness, indexList, size,selected);
            int maxIndex = findMaxFitnessIndex(selectedFitness);
            selected[maxIndex]=true;
            double maxfitness=selectedFitness.get(maxIndex);
            tourparents.put(maxIndex,maxfitness);
        }
        return tourparents;
    }
    //从已经选择出来的父代个体中按照锦标赛算法选择两个作为双亲，进行交叉操作，parents为已选择出的父代个体数，size为每次锦标赛选择的个体数,x为种群个数
    public Map<Integer,Double> tournamentparent(Map<Integer,Double> parents,int size,int x){
        // PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        boolean[] selected=new boolean[x];
        for(int i=0;i<x;i++)
            selected[i]=false;
        Map<Integer,Double> parent=new HashMap<Integer, Double>();
        for(int i=0;i<2;i++){
            Set<Integer> keySet = parents.keySet();
            List<Integer> indexList = new ArrayList<>(keySet);
            Map<Integer, Double> selectedFitness = selectRandomFitness(parents, indexList, size,selected);
            int maxIndex = findMaxFitnessIndex(selectedFitness);
            double maxfitness=selectedFitness.get(maxIndex);
            parent.put(maxIndex,maxfitness);
            selected[maxIndex]=true;
        }
        return parent;
    }
    //从给定的锦标赛参赛个体中，选择获胜的个体，即适应度最高的个体，并返回它的index值
    private static int findMaxFitnessIndex(Map<Integer, Double> selectedFitness) {
        // 初始的最大 fitness 值设为负无穷大
        double maxFitness = Double.NEGATIVE_INFINITY;
        // 初始的最大 fitness 值对应的键 index
        int maxIndex = 0;
        // 遍历 selectedFitness 的键值对
        for (Map.Entry<Integer, Double> entry : selectedFitness.entrySet()) {
            int currentIndex = entry.getKey();
            double currentFitness = entry.getValue();
            // 检查当前 fitness 是否大于最大 fitness 值
            if (currentFitness > maxFitness) {
                maxFitness = currentFitness;
                maxIndex = currentIndex;
            }
        }
        return maxIndex;
    }
    //从给定的 allFitness 中随机选择 n 个不重复的键值对，并返回一个新的 Map，作为一次锦标赛的参赛个体
    private static Map<Integer, Double> selectRandomFitness(Map<Integer, Double> allFitness, List<Integer> indexList, int n,boolean[] selected) {
                // 创建一个随机数生成器
                Random random = new Random();

                // 创建一个新的 Map，用于存储选中的键值对
                Map<Integer, Double> selectedFitness = new HashMap<>();

                // 遍历选中的个数 n
                for (int i = 0; i < n; i++) {
                    // 生成一个随机索引
                    int randomIndex = random.nextInt(indexList.size());
                    // 从索引列表中获取选中的索引值
                    int selectedIndex = indexList.get(randomIndex);
                    while(selected[selectedIndex]){
                        randomIndex = random.nextInt(indexList.size());
                        // 从索引列表中获取选中的索引值
                        selectedIndex = indexList.get(randomIndex);
                    }
            // 根据选中的索引值从 allFitness 中获取对应的键值对
            Double selectedValue = allFitness.get(selectedIndex);

            // 将选中的键值对存储到新的 Map 中
            selectedFitness.put(selectedIndex, selectedValue);

            // 从索引列表中移除已选中的索引，避免重复选择
            indexList.remove(randomIndex);
        }

        return selectedFitness;
    }
    //对于长度为length的个体，随机生成2n个交叉点，其中n的范围设置为[1,length/crossPointRange]，其中n的确定:给定每个个体数组的长度，在这个范围内随机生成
    //返回值为一个int数组，按照从小到大存放交叉点
    public int[] randomCrossPoint(int length,int n){
        int[] random=new int[2*n];
        Random rand = new Random();
        HashSet<Integer> generated = new HashSet<>();
        while (generated.size() < 2*n) {
            int num = rand.nextInt(length); // 生成随机数
            generated.add(num); // 添加到集合
        }
        int index = 0;
        for (int num : generated) {
            random[index++] = num; // 存入数组
        }
        Arrays.sort(random); // 对数组进行排序
        return random;
    }
    //对两个父代个体进行交叉操作，输入为fathers两个父代个体的索引和适应度，population：种群数组，randomPoint为存储交叉点的数组
    private int[][] crossOperation(Map<Integer, Double> fathers, int[][] population, int[] randomPoint) {
        int[][] father=new int[2][population[0].length];
        int i=0;
        for(Integer index : fathers.keySet()){
            father[i++]=population[index];
        }
        for(int j=0;j<randomPoint.length;j++){
            for(int k=randomPoint[j++];k<=randomPoint[j];k++){
                int temp=father[0][k];
                father[0][k]=father[1][k];
                father[1][k]=temp;
            }
        }
        return father;
    }
    //进行变异操作，输入为nextgeneration：已经交叉操作后的下一代的种群，generatechildren为产生的孩子数量，对孩子进行变异操作，x为种群数量，length为个体长度
    //mutationRandom为变异概率，maxspace为服务器最大容量，minsize为数据最小size
    private int[][] mutation(int[][] nextgeneration, int generatechildren,int x,int length,double mutationRandom,int maxspace,int minsize) {
        int[][]finalNextGeneration=new int[x][length];
        Random random=new Random();
        for(int i=0;i<generatechildren;i++){
            for(int j=0;j<nextgeneration[i].length;j++){
                if (random.nextDouble() < mutationRandom) {
                    if (random.nextBoolean()) {
                        // 一半的概率为0
                        finalNextGeneration[i][j]=0;
                    }
                    else{
                        // 发生变异，随机选择新值
                        int count=10;
                        int newValueIndex = random.nextInt(this.experimentalPopularData.size());
                        while(!reasonable(nextgeneration[i],j,maxspace,minsize,newValueIndex)&&count<10){
                            newValueIndex=random.nextInt(this.experimentalPopularData.size());
                            count++;
                        }
                        finalNextGeneration[i][j] = this.experimentalPopularData.get(newValueIndex).getId();
                    }
                }
                else{
                    finalNextGeneration[i][j]=nextgeneration[i][j];
                }
            }
        }
        if (x - generatechildren >= 0)
            System.arraycopy(nextgeneration, generatechildren, finalNextGeneration, generatechildren, x - generatechildren);
        return finalNextGeneration;
    }
    //判断改变该位置存储的数据情况后是否合理，合理返回true
    //输入 individual为该突变个体，j为突变位置，maxspace为该服务器最大存储容量，minsize为数据最小size，newValueIndex为随机选择数据在experimentdata中对应的索引值
    private boolean reasonable(int[] individual, int j, int maxspace, int minsize, int newValueIndex) {
        int n=maxspace/minsize;
        int dataSize=this.experimentalPopularData.get(newValueIndex).getSize();
        int remainder=(j+1)%n;
        if(remainder==0){
            remainder=n;
        }
        int currentSize=0;
        for(int i=(j+1-remainder);i<(j+1-remainder+n);i++){
            if(i==j){
                currentSize+=dataSize;
            }
            else{
                int tempSize=findSizeById(individual[i]);
                currentSize+=tempSize;
            }
        }
        return currentSize <= maxspace;
    }

    private int findSizeById(int i) {
        int size=0;
        for(PopularData pd:this.experimentalPopularData){
            if(pd.getId()==i){
                size=pd.getSize();
                break;
            }
        }
        return size;
    }
    //进行实验 x为种群个体数
    // minsize为数据的最小size，maxspace为服务器的最大容量，begintime和endtime为预测的时间戳
    // parent为每次遗传选择的父代数量，tournamentsize为锦标赛大小,crossPointRange确定随机生成2n个交叉点的n的取值范围，为[1,length/crossPointRange]，length是个体数组长度
    //其中crossPointRange>=2 mutationRandom为每个基因位的变异概率,iterations为迭代次数，进行多少轮遗传
    public void experiment(int x,int minsize,int maxspace,int beginTimestamp,int endTimestamp,int iterations){
        int parent=x/3;
        int tounamentparentssize=x/5;
        int tounamentparentsize=parent/5;
        int crossPointRange=6;
        double mutationRandom=0.1;
        for(int time=beginTimestamp;time<=endTimestamp;time++){
            int n=maxspace/minsize;
            int length = n * this.experimentalEdgeServer.size();
            int[][] population=new int[x][length];
            List<EdgeServer> tempservers=DBUtils.getAllEdgeServer();
            //1.初始化生成x个个体的种群
            for(int i=0;i<x;i++){
                List<EdgeServer> servers=new ArrayList<EdgeServer>();
                servers=tempservers;
                servers=randomCaching(servers);
                CachingDecision cachingDecision=initCachingDecision(servers);
                //population=initPopulation(cachingDecision,x,minsize,maxspace);
                Map<EdgeServer, HashSet<PopularData>> cachingstate = cachingDecision.getCachingState();
                int index = 0;
                for (Map.Entry<EdgeServer, HashSet<PopularData>> edgeServerHashSetEntry : cachingstate.entrySet()) {
                    HashSet<PopularData> pds = edgeServerHashSetEntry.getValue();
                    for (PopularData pd : pds) {
                        population[i][index++] = pd.getId();
                    }
                    for (int k = 0; k < (n - pds.size()); k++) {
                        population[i][index++] = 0;
                    }
                }
            }
            //开始迭代
            for(int numbers=0;numbers<iterations;numbers++){
                //2.确认适应度 allFitness存储为<索引i，适应度值>
                Map<Integer, Double> allFitness=new HashMap<Integer, Double>();
                allFitness=calAllFitness(population,time,x,minsize,maxspace);
                //3.选择操作 用选择操作选择适应度较高的个体作为父代，用于生成下一代个体，使用锦标赛算法进行选择
                //存放已经选择出来作为父代的索引和适应度
                Map<Integer, Double> selectedIndividual;
                selectedIndividual=tournamentparents(allFitness,tounamentparentssize,parent,x);
                //4.交叉操作 使用交叉操作对父代个体进行交叉，生成下一代个体。可以采用单点交叉或多点交叉来交换基因片段。
                //确定生成交叉点的个数
                int range=length/crossPointRange;
                Random rand = new Random();
                //crossPointNum的取值
                //[1,length/crossPointRange]
                int generatechildren=x-parent;
                int[][] nextgeneration=new int[x][length];
                for(int i=0;i<generatechildren;i++){
                    int crossPointNum=rand.nextInt(range)+1;
                    //存放交叉点位置的数组
                    int[] randomPoint=randomCrossPoint(length,crossPointNum);
                    //从选出的父代个体中再次使用选择操作，如轮盘赌选择或锦标赛选择，选出两个个体作为交叉操作的父代。
                    Map<Integer,Double> fathers=tournamentparent(selectedIndividual,tounamentparentsize,x);
                    //这两个父代个体将进行交叉操作，生成两个子代。
                    int[][] twoChild=new int[2][length];
                    twoChild=crossOperation(fathers,population,randomPoint);
                    nextgeneration[i++]=twoChild[0];
                    nextgeneration[i]=twoChild[1];
                }
                int insertIndex=generatechildren;
                //下一代中把选择出的父代个体也加进去，最后种群的个体形成为：父代和他们的孩子们
                for(Integer index : selectedIndividual.keySet()){
                    nextgeneration[insertIndex++]=population[index];
                }
                //5.变异操作: 使用变异操作对交叉后的个体进行基因的微小改变，引入随机性。
                // 可以通过翻转某些基因位或随机改变某些基因位的值来实现变异。
                //对它们产生的孩子进行变异操作
                //int[][] nextgeneration, int generatechildren,int x,int length,double mutationRandom,int maxspace,int minsize
                nextgeneration=mutation(nextgeneration,generatechildren,x,length,mutationRandom,maxspace,minsize);
                population=nextgeneration;
                //            //6.生成下一代: 根据选择、交叉和变异操作，生成下一代个体，并替换当前种群。
                //            //7.终止条件: 设定终止条件，例如达到最大迭代次数或找到满意的解决方案。
                //            //编写一个java程序，输入为两个长度相等都为length的int数组a[],b[]，随机生成偶数个小于length的非负整数a0,a1,a2,a3,到a2n，将这些整数从小到大排列，并将a[a0]到a[a1]
            }
            //int[][] population,int x,int timestamp,int minsize,int maxspace,int edgenum
            CachingDecision cachingDecision=findMaxFitnessIndividual(population,x,time,minsize,maxspace,this.experimentalEdgeServer.size());
            printQoe(time,cachingDecision);
        }
    }
}
