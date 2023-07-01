package control;

import baseLine.BaseGCO;
import baseLine.BaseLFU;
import baseLine.BaseRandom;
import baseLine.baseNSGA;
import bean.ExperimentalSetup;
import our_algorithm.OurAlgorithm;
import util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class main {

    public static int BeginTimestamp = 51;
    public static int EndTimestamp = 80;
    public static int ExperimentalServer = 40;
    public static int ExperimentalUser = 400;
    public static int ExperimentalData = 200;
    public static int RequestNumForUser = 1;
    public static int maxHop = 2;
    public static double latencyWeight = -1.5;
    public static double SimWeight = -4;
    

    public static double ZipFS = 1.5;
    public static void main(String[] args) throws IOException {

        //导入数据集基本数据
//        InitBasicData initBasicData = new InitBasicData();
//        initBasicData.insertBasicUser();
//        initBasicData.insertBasicEdgeServer();

        //实验参数设置
        ExperimentalSetup experimentalSetup = new ExperimentalSetup();
        //时间
        experimentalSetup.setBeginTimestamp(51);
        experimentalSetup.setEndTimestamp(100);
        //基本数据
        experimentalSetup.setExperimentalData(100);
        experimentalSetup.setExperimentalServer(40);
        experimentalSetup.setExperimentalUser(400);
        //实验设置
        experimentalSetup.setMaxHop(4);
        experimentalSetup.setMaxStorageSpace(3);
        //参数设置
        experimentalSetup.setLatencyWeight(-1);
        experimentalSetup.setSimWeight(-6);
        experimentalSetup.setSumQoEWeight(2);
        experimentalSetup.setFIndexWeight(1);
        experimentalSetup.setZ(3);
        //遗传算法参数设置

////
////        生成初始化数据
//
//        GenerateData generateData = new GenerateData();
//        //从表中添加实验用户，传入要选择的用户数量
//        generateData.addExperimentalUser(experimentalSetup.getExperimentalUser());
//        //从表中添加实验服务器，传入要选择的服务器数量
//        generateData.addExperimentalServer(experimentalSetup.getExperimentalServer(),experimentalSetup.getMaxStorageSpace());
//        //要生成的数据数量，以及大小
//        generateData.newPopularData(experimentalSetup.getExperimentalData(),experimentalSetup.getMaxDataSize());
//        //为用户随机生成喜好序列，并基于zipf分布给出请求各个数据的概率
//        generateData.newUserDataProbability(ZipFS);
//        //总共的时间序列
//        ArrayList<Integer> timestampList = new ArrayList<Integer>();
//        for(int i=1;i<=80;i++){
//            timestampList.add(i);
//        }
////        为各个时间段生成数据，目前默认一个用户只在一个时间段发送一个请求
//        generateData.newRequest(timestampList);
//        //根据启止时间内的数据，构建请求矩阵
//        OtherUtils.writeRequestMatrix(0,experimentalSetup.getBeginTimestamp());
//        //矩阵分解是python实现
//        //写入用户历史，便于预测
//        FileUtils.writeRequestSequenceToFile("src/AlgorithmicData/user_history.txt");
////        //读出预测结果，写入预测数据
//        HashMap<Integer, ArrayList<Integer>> requestByTime = FileUtils.readUserBehaviorFromFile("src/AlgorithmicData/user_history.txt");
//
////        //本文算法
////        初始化算法所需内容
        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//        ourAlgorithm.getHawkesEvents(experimentalSetup.getBeginTimestamp()-1,"src/AlgorithmicData/history_hawkes_events.txt");
//        ourAlgorithm.getHawkesEvents(experimentalSetup.getEndTimestamp(),"src/AlgorithmicData/all_hawkes_events.txt");
////
//
//        ourAlgorithm.addPredictRequest();

//
//        //1-50是训练数据 , 51以后是实验数据
//        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
//            //读取各个服务器-数据对收到的请求时间
//            ourAlgorithm.findBestDecision(i);
//        }
////
//        System.out.println("   =================    ");
//        BaseRandom baseRandom=new BaseRandom();
//        baseRandom.initializeData(experimentalSetup);
//        System.out.println("   =================    ");
//        BaseGCO baseGCO = new BaseGCO();
//        baseGCO.initializeData(experimentalSetup);
//        System.out.println("   =================    ");
//        BaseLFU baseLFU=new BaseLFU();
//        baseLFU.initializeData(experimentalSetup);
//        System.out.println("   =================    ");
        baseNSGA baseNSGA=new baseNSGA();
        baseNSGA.initializeData(experimentalSetup);
    }
}
