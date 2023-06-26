package control;

import baseLine.BaseLRU;
import baseLine.BaseRandom;
import baseLine.BaseUCO;
import our_algorithm.OurAlgorithm;
import util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class main {

    public static int minTimestamp = 0;
    public static int maxTimestamp = 50;
    public static void main(String[] args) throws IOException {
        //导入数据集基本数据
//        InitBasicData initBasicData = new InitBasicData();
//        initBasicData.insertBasicUser();
//        initBasicData.insertBasicEdgeServer();
        //生成初始化数据
//        GenerateData generateData = new GenerateData();
//        generateData.addExperimentalUser(400);
//        generateData.addExperimentalServer(40);
//        generateData.newPopularData(100,3);
//        generateData.newUserDataProbability(1.5);
//        ArrayList<Integer> timestampList = new ArrayList<Integer>();
//        for(int i=1;i<=80;i++){
//            timestampList.add(i);
//        }
//        generateData.newRequest(timestampList);
        //构建请求矩阵
//        OtherUtils.buildRequestMatrix(0,50);
        //矩阵分解是python实现
        //本文算法
        OurAlgorithm ourAlgorithm = new OurAlgorithm();
        //初始化算法所需内容
        ourAlgorithm.initializeData();
        ourAlgorithm.findBestDecision(51);
        FileUtils.writeRequestSequenceToFile("src/AlgorithmicData/user_history.txt");
        HashMap<Integer, ArrayList<Integer>> requestByTime = FileUtils.readUserBehaviorFromFile("src/AlgorithmicData/user_history.txt");
        System.out.println("   =================    ");
        BaseRandom baseRandom=new BaseRandom();
        baseRandom.initializeData(51,53);
        baseRandom.experiment(51,53);
        System.out.println("   =================    ");
        BaseUCO baseUCO=new BaseUCO();
        baseUCO.initializeData(51,53);
        baseUCO.experiment(51,53);
//
        System.out.println("   =================    ");
        BaseLRU baseLRU=new BaseLRU();
        baseLRU.initializeData(51,53);
        baseLRU.experiment(51,53);

    }
}
