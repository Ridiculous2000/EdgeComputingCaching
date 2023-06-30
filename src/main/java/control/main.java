package control;

import baseLine.*;
import bean.AlgorithmResult;
import bean.ExperimentalSetup;
import data_generation.GenerateData;
import our_algorithm.OurAlgorithm;
import util.AlgorithmUtils;
import util.FileUtils;
import util.OtherUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class main {



    public static double ZipFS = 1.5;
    static ExperimentalSetup experimentalSetup = new ExperimentalSetup();
    public static void main(String[] args) throws IOException {

        //导入数据集基本数据
//        InitBasicData initBasicData = new InitBasicData();
//        initBasicData.insertBasicUser();
//        initBasicData.insertBasicEdgeServer();

        //生成默认数据
//        initDefaultSetup();
//        initDefaultData();

        //运行python代码

        //运行代码
        //        runningTime();

    }

    public static void initDefaultSetup(){
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
    }


    public static void initDefaultData() throws IOException {
//        生成初始化数据
        GenerateData generateData = new GenerateData();
        //从表中添加实验用户，传入要选择的用户数量
        generateData.addExperimentalUser(experimentalSetup.getExperimentalUser());
        //从表中添加实验服务器，传入要选择的服务器数量
        generateData.addExperimentalServer(experimentalSetup.getExperimentalServer(),experimentalSetup.getMaxStorageSpace());
        //要生成的数据数量，以及大小
        generateData.newPopularData(experimentalSetup.getExperimentalData(),experimentalSetup.getMaxDataSize());
        //为用户随机生成喜好序列，并基于zipf分布给出请求各个数据的概率
        generateData.newUserDataProbability(ZipFS);
        //总共的时间序列
        ArrayList<Integer> timestampList = new ArrayList<Integer>();
        for(int i=1;i<=experimentalSetup.getEndTimestamp();i++){
            timestampList.add(i);
        }
//        为各个时间段生成数据，目前默认一个用户只在一个时间段发送一个请求
        generateData.newRequest(timestampList);
        //根据启止时间内的数据，构建请求矩阵
        OtherUtils.writeRequestMatrix(0,experimentalSetup.getBeginTimestamp());
        //矩阵分解是python实现
        //写入用户历史，便于预测
        FileUtils.writeRequestSequenceToFile("src/AlgorithmicData/user_history.txt");
        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
        ourAlgorithm.getHawkesEvents(experimentalSetup.getBeginTimestamp()-1,"src/AlgorithmicData/history_hawkes_events.txt");
        ourAlgorithm.getHawkesEvents(experimentalSetup.getEndTimestamp(),"src/AlgorithmicData/all_hawkes_events.txt");

    }

    public static void runningTime() throws IOException {
        //本文算法
        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
        ourAlgorithm.addPredictRequest();


        double FIndexWeight = experimentalSetup.getFIndexWeight();
        double SumQoEWeight = experimentalSetup.getSumQoEWeight();
        int beginTime  = experimentalSetup.getBeginTimestamp();
        int endTime = experimentalSetup.getEndTimestamp();

        double[] sumQoEList = new double[200];
        double[] FIndexList = new double[200];
        double[] FinalList = new double[200];
        //1-50是训练数据 , 51以后是实验数据
        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
            for(int j=0;j<3;j++){
                AlgorithmResult algorithmResult = ourAlgorithm.findBestDecision(i);
                sumQoEList[i] = Math.max(sumQoEList[i],algorithmResult.getSumQoE());
                FIndexList[i] = Math.max(FIndexList[i],algorithmResult.getFIndex());
            }
            //读取各个服务器-数据对收到的请求时间
        }
        for(int i=0;i<FinalList.length;i++){
            FinalList[i] =  (FIndexWeight*FIndexList[i] + SumQoEWeight*(sumQoEList[i]/experimentalSetup.getExperimentalUser())) / (FIndexWeight+SumQoEWeight);
        }

        BaseRandom baseRandom=new BaseRandom();
        double[] randomSumQoEList = new double[200];
        double[] randomFIndexList = new double[200];
        double[] randomFinalList = new double[200];
        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
            for(int j=0;j<3;j++){
                experimentalSetup.setBeginTimestamp(i);
                AlgorithmResult algorithmResult = baseRandom.initializeData(experimentalSetup);
                randomSumQoEList[i] = Math.min(randomSumQoEList[i],algorithmResult.getSumQoE());
                randomFIndexList[i] = Math.min(randomFIndexList[i],algorithmResult.getFIndex());
            }
            //读取各个服务器-数据对收到的请求时间
        }
        for(int i=0;i<randomFinalList.length;i++){
            randomFinalList[i] =  (FIndexWeight*randomFIndexList[i] + SumQoEWeight*(randomSumQoEList[i]/experimentalSetup.getExperimentalUser())) / (FIndexWeight+SumQoEWeight);
        }


        BaseGCO baseGCO=new BaseGCO();
        double[] GCOSumQoEList = new double[200];
        double[] GCOFIndexList = new double[200];
        double[] GCOFinalList = new double[200];
        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
            for(int j=0;j<3;j++){
                experimentalSetup.setBeginTimestamp(i);
                AlgorithmResult algorithmResult = baseGCO.initializeData(experimentalSetup);
                GCOSumQoEList[i] = Math.min(GCOSumQoEList[i],algorithmResult.getSumQoE());
                GCOFIndexList[i] = Math.min(GCOFIndexList[i],algorithmResult.getFIndex());
            }
            //读取各个服务器-数据对收到的请求时间
        }
        for(int i=0;i<GCOFinalList.length;i++){
            GCOFinalList[i] =  (FIndexWeight*GCOFIndexList[i] + SumQoEWeight*(GCOSumQoEList[i]/experimentalSetup.getExperimentalUser())) / (FIndexWeight+SumQoEWeight);
        }


        BaseLFU baseLFU=new BaseLFU();
        double[] LFUSumQoEList = new double[200];
        double[] LFUFIndexList = new double[200];
        double[] LFUFinalList = new double[200];
        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
            for(int j=0;j<3;j++){
                experimentalSetup.setBeginTimestamp(i);
                AlgorithmResult algorithmResult = baseLFU.initializeData(experimentalSetup);
                LFUSumQoEList[i] = Math.min(LFUSumQoEList[i],algorithmResult.getSumQoE());
                LFUFIndexList[i] = Math.min(LFUFIndexList[i],algorithmResult.getFIndex());
            }
            //读取各个服务器-数据对收到的请求时间
        }
        for(int i=0;i<LFUFinalList.length;i++){
            LFUFinalList[i] =  (FIndexWeight*LFUFIndexList[i] + SumQoEWeight*(LFUSumQoEList[i]/experimentalSetup.getExperimentalUser())) / (FIndexWeight+SumQoEWeight);
        }

        String filePath = "src/result/time.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder ours = new StringBuilder();
            ours.append("Ours \n");
            ours.append("SumQoE:");
            ours.append(joinArrayElements(sumQoEList, beginTime, endTime)+"\n");
            ours.append("FIndex:");
            ours.append(joinArrayElements(FIndexList, beginTime, endTime)+"\n");
            ours.append("FinalValue:");
            ours.append(joinArrayElements(FinalList, beginTime, endTime)+"\n");
            writer.write(ours.toString());

            StringBuilder random = new StringBuilder();
            ours.append("Random \n");
            ours.append("SumQoE:");
            ours.append(joinArrayElements(randomSumQoEList, beginTime, endTime)+"\n");
            ours.append("FIndex:");
            ours.append(joinArrayElements(randomFIndexList, beginTime, endTime)+"\n");
            ours.append("FinalValue:");
            ours.append(joinArrayElements(randomFinalList, beginTime, endTime)+"\n");
            writer.write(ours.toString());

            StringBuilder GCO = new StringBuilder();
            ours.append("GCO \n");
            ours.append("SumQoE:");
            ours.append(joinArrayElements(GCOSumQoEList, beginTime, endTime)+"\n");
            ours.append("FIndex:");
            ours.append(joinArrayElements(GCOFIndexList, beginTime, endTime)+"\n");
            ours.append("FinalValue:");
            ours.append(joinArrayElements(GCOFinalList, beginTime, endTime)+"\n");
            writer.write(ours.toString());


            StringBuilder LFU = new StringBuilder();
            ours.append("LFU \n");
            ours.append("SumQoE:");
            ours.append(joinArrayElements(LFUSumQoEList, beginTime, endTime)+"\n");
            ours.append("FIndex:");
            ours.append(joinArrayElements(LFUFIndexList, beginTime, endTime)+"\n");
            ours.append("FinalValue:");
            ours.append(joinArrayElements(LFUFinalList, beginTime, endTime)+"\n");
            writer.write(ours.toString());

        }
    }


    public static void runningMaxHop() throws IOException {
        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
        ourAlgorithm.addPredictRequest();
        BaseRandom baseRandom = new BaseRandom();
        BaseGCO baseGCO = new BaseGCO();
        BaseLFU baseLFU = new BaseLFU();
        String filePath = "src/result/max_hop.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder showResult = new StringBuilder();
            for(int i=1;i<5;i++){
                experimentalSetup.setMaxHop(i);

                showResult.append("MaxHop:"+i+"\n");

                AlgorithmResult ourAlgorithmResult = ourAlgorithm.findBestDecision(experimentalSetup.getBeginTimestamp());
                showResult.append(ourAlgorithmResult.toString());

                AlgorithmResult randomAlgorithmResult = baseRandom.initializeData(experimentalSetup);
                showResult.append(randomAlgorithmResult.toString());

                AlgorithmResult GCOAlgorithmResult = baseGCO.initializeData(experimentalSetup);
                showResult.append(GCOAlgorithmResult.toString());

                AlgorithmResult LFUAlgorithmResult = baseLFU.initializeData(experimentalSetup);
                showResult.append(LFUAlgorithmResult.toString());
            }

            writer.write(showResult.toString());

        }
    }

    public static void runningMaxStorage() throws IOException {
        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
        ourAlgorithm.addPredictRequest();
        BaseRandom baseRandom = new BaseRandom();
        BaseGCO baseGCO = new BaseGCO();
        BaseLFU baseLFU = new BaseLFU();
        String filePath = "src/result/max_storage.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder showResult = new StringBuilder();
            for(int i=3;i<=8;i++){
                experimentalSetup.setMaxStorageSpace(i);

                showResult.append("MaxStorage:"+i+"\n");

                AlgorithmResult ourAlgorithmResult = ourAlgorithm.findBestDecision(experimentalSetup.getBeginTimestamp());
                showResult.append(ourAlgorithmResult.toString());

                AlgorithmResult randomAlgorithmResult = baseRandom.initializeData(experimentalSetup);
                showResult.append(randomAlgorithmResult.toString());

                AlgorithmResult GCOAlgorithmResult = baseGCO.initializeData(experimentalSetup);
                showResult.append(GCOAlgorithmResult.toString());

                AlgorithmResult LFUAlgorithmResult = baseLFU.initializeData(experimentalSetup);
                showResult.append(LFUAlgorithmResult.toString());
            }

            writer.write(showResult.toString());

        }
    }


    public static String joinArrayElements(double[] array, int startIndex, int endIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = startIndex; i <= endIndex; i++) {
            stringBuilder.append(array[i]);
            if (i < endIndex) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public static void runningAlgorithm() throws IOException {

        System.out.println("   =================    ");
        BaseRandom baseRandom=new BaseRandom();
        baseRandom.initializeData(experimentalSetup);
        System.out.println("   =================    ");
        BaseGCO baseGCO = new BaseGCO();
        baseGCO.initializeData(experimentalSetup);
        System.out.println("   =================    ");
        BaseLFU baseLFU=new BaseLFU();
        baseLFU.initializeData(experimentalSetup);

        baseNSGA baseNSGA=new baseNSGA();
        baseNSGA.initializeData(experimentalSetup);
        baseNSGA.experiment(100,1,3,experimentalSetup.getBeginTimestamp(),experimentalSetup.getEndTimestamp(),40,30,20,6,0.1,10);

    }



}
