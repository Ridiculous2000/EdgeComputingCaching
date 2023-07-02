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

    public static double ZipFS = 1;
    static ExperimentalSetup experimentalSetup = new ExperimentalSetup();
    public static void main(String[] args) throws IOException {

        //导入数据集基本数据
//        InitBasicData initBasicData = new InitBasicData();
//        initBasicData.insertBasicUser();
//        initBasicData.insertBasicEdgeServer();

//        initDefaultSetup();
//        initDefaultData();

//        initDefaultSetup();
//        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//        ourAlgorithm.addPredictRequest();

//        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
//            experimentalSetup.setBeginTimestamp(i);
//            ourAlgorithm.findBestDecision(i);
//        }
//        BaseLSTM baseLSTM = new BaseLSTM();
//        baseLSTM.initializeData(experimentalSetup);
//        baseLSTM.getCacheDesition(experimentalSetup);
//        BaseNSGA baseNSGA = new BaseNSGA();
//        baseNSGA.initializeData(experimentalSetup);


//        BaseRandom baseRandom = new BaseRandom();
//        baseRandom.initializeData(experimentalSetup);

//        BaseLFU baseLFU = new BaseLFU();
//        baseLFU.initializeData(experimentalSetup);

//        BaseGCO baseGCO = new BaseGCO();
//        baseGCO.initializeData(experimentalSetup);

//         遗传算法部分
//        baseNSGA baseNSGA=new baseNSGA();
//        baseNSGA.initializeData(experimentalSetup);


//        修改最大存储，然后运算
//        initDefaultSetup();
//        BaseRandom baseRandom = new BaseRandom();
//        BaseGCO baseGCO = new BaseGCO();
//        BaseLFU baseLFU = new BaseLFU();
//        BaseLSTM baseLSTM = new BaseLSTM();
//        BaseNSGA baseNSGA = new BaseNSGA();
//
//        for(int i=3;i<=6;i++){
//            experimentalSetup.setMaxStorageSpace(i);
////            OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//            System.out.println("MaxStorageSpace:"+i);
////            for(int j=experimentalSetup.getBeginTimestamp();j< experimentalSetup.getEndTimestamp();j++){
////                ourAlgorithm.findBestDecision(j);
////            }
////            baseRandom.initializeData(experimentalSetup);
////            baseGCO.initializeData(experimentalSetup);
////            baseLFU.initializeData(experimentalSetup);
//            baseNSGA.initializeData(experimentalSetup);
//        }


//        initDefaultSetup();

//        BaseRandom baseRandom = new BaseRandom();
//        BaseGCO baseGCO = new BaseGCO();
//        BaseLFU baseLFU = new BaseLFU();
//        BaseLSTM baseLSTM = new BaseLSTM();
//        BaseNSGA baseNSGA = new BaseNSGA();
//
//        for(int i=1;i<=5;i++){
//            experimentalSetup.setMaxHop(i);
//            OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//            System.out.println("MaxHop:"+i);
//            for(int j=experimentalSetup.getBeginTimestamp();j< experimentalSetup.getEndTimestamp();j++){
//                ourAlgorithm.findBestDecision(j);
//            }
//            baseRandom.initializeData(experimentalSetup);
//            baseGCO.initializeData(experimentalSetup);
//            baseLFU.initializeData(experimentalSetup);
//            baseNSGA.initializeData(experimentalSetup);
//        }


//        initDefaultSetup();
//        BaseRandom baseRandom = new BaseRandom();
//        BaseGCO baseGCO = new BaseGCO();
//        BaseLFU baseLFU = new BaseLFU();
//        BaseLSTM baseLSTM = new BaseLSTM();
//        BaseNSGA baseNSGA = new BaseNSGA();
//        for(int i=30;i<=60;i=i+10){
//            experimentalSetup.setExperimentalServer(i);
//            GenerateData generateData = new GenerateData();
//            generateData.addExperimentalServer(experimentalSetup.getExperimentalServer(),experimentalSetup.getMaxStorageSpace());
//            OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//            System.out.println("Server Num:"+i);
//            for(int j=experimentalSetup.getBeginTimestamp();j< experimentalSetup.getEndTimestamp();j++){
//                ourAlgorithm.findBestDecision(j);
//            }
//            baseRandom.initializeData(experimentalSetup);
//            baseGCO.initializeData(experimentalSetup);
//            baseLFU.initializeData(experimentalSetup);
//            baseNSGA.initializeData(experimentalSetup);
//        }

//        initDefaultSetup();
//        experimentalSetup.setExperimentalUser(500);
//        initDefaultData();

//        BaseRandom baseRandom = new BaseRandom();
//        BaseGCO baseGCO = new BaseGCO();
//        BaseLFU baseLFU = new BaseLFU();
//        BaseLSTM baseLSTM = new BaseLSTM();
//        BaseNSGA baseNSGA = new BaseNSGA();
//        System.out.println("User Num:"+500);
//        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//        ourAlgorithm.addPredictRequest();

//        for(int j=experimentalSetup.getBeginTimestamp();j< experimentalSetup.getEndTimestamp();j++){
//            ourAlgorithm.findBestDecision(j);
//        }
//        System.out.println("      =====================      ");
//        baseRandom.initializeData(experimentalSetup);
//        System.out.println("      =====================      ");
//        baseGCO.initializeData(experimentalSetup);
//        System.out.println("      =====================      ");
//        baseLFU.initializeData(experimentalSetup);
//        System.out.println("      =====================      ");
//        baseNSGA.initializeData(experimentalSetup);
//        System.out.println("      =====================      ");


        ZipFS = 2;
        initDefaultSetup();
//        initDefaultData();

        BaseRandom baseRandom = new BaseRandom();
        BaseGCO baseGCO = new BaseGCO();
        BaseLFU baseLFU = new BaseLFU();
        BaseLSTM baseLSTM = new BaseLSTM();
        BaseNSGA baseNSGA = new BaseNSGA();
        System.out.println("ZipFS:"+2);
        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//        ourAlgorithm.addPredictRequest();


        for(int j=experimentalSetup.getBeginTimestamp();j< experimentalSetup.getEndTimestamp();j++){
            ourAlgorithm.findBestDecision(j);
        }
        System.out.println("      =====================      ");
        baseRandom.initializeData(experimentalSetup);
        System.out.println("      =====================      ");
        baseGCO.initializeData(experimentalSetup);
        System.out.println("      =====================      ");
        baseLFU.initializeData(experimentalSetup);
        System.out.println("      =====================      ");
        baseNSGA.initializeData(experimentalSetup);
        System.out.println("      =====================      ");

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
        experimentalSetup.setMaxHop(3);
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

//    public static void runningTime() throws IOException {
//        initDefaultSetup();
//        //本文算法
//        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//
//        double FIndexWeight = experimentalSetup.getFIndexWeight();
//        double SumQoEWeight = experimentalSetup.getSumQoEWeight();
//        int beginTime  = experimentalSetup.getBeginTimestamp();
//        int endTime = experimentalSetup.getEndTimestamp();

//        double[] sumQoEList = new double[200];
//        double[] FIndexList = new double[200];
//        double[] FinalList = new double[200];
//        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
//            AlgorithmResult algorithmResult = ourAlgorithm.findBestDecision(i);
//            sumQoEList[i] = algorithmResult.getSumQoE();
//            FIndexList[i] = algorithmResult.getFIndex();
//            FinalList[i] = algorithmResult.getFinalValue();
//        }

//        BaseRandom baseRandom=new BaseRandom();
//        double[] randomSumQoEList = new double[200];
//        double[] randomFIndexList = new double[200];
//        double[] randomFinalList = new double[200];
//        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
//            experimentalSetup.setBeginTimestamp(i);
//            AlgorithmResult algorithmResult = baseRandom.initializeData(experimentalSetup);
//            randomSumQoEList[i] = algorithmResult.getSumQoE();
//            randomFIndexList[i] = algorithmResult.getFIndex();
//            randomFinalList[i] = algorithmResult.getFinalValue();
//        }


//        BaseGCO baseGCO=new BaseGCO();
//        double[] GCOSumQoEList = new double[200];
//        double[] GCOFIndexList = new double[200];
//        double[] GCOFinalList = new double[200];
//        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
//            experimentalSetup.setBeginTimestamp(i);
//            AlgorithmResult algorithmResult = baseGCO.initializeData(experimentalSetup);
//            GCOSumQoEList[i] = algorithmResult.getSumQoE();
//            GCOFIndexList[i] = algorithmResult.getFIndex();
//            GCOFinalList[i] = algorithmResult.getFinalValue();
//        }


//        BaseLFU baseLFU=new BaseLFU();
//        double[] LFUSumQoEList = new double[200];
//        double[] LFUFIndexList = new double[200];
//        double[] LFUFinalList = new double[200];
//        for(int i=experimentalSetup.getBeginTimestamp();i<=experimentalSetup.getEndTimestamp();i++){
//            experimentalSetup.setBeginTimestamp(i);
//            AlgorithmResult algorithmResult = baseLFU.initializeData(experimentalSetup);
//            LFUSumQoEList[i] = algorithmResult.getSumQoE();
//            LFUFIndexList[i] = algorithmResult.getFIndex();
//            LFUFinalList[i] = algorithmResult.getFinalValue();
//        }
//
//
//        String filePath = "src/result/time.txt";
////
////        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
////            StringBuilder ours = new StringBuilder();
////            ours.append("Ours \n");
////            ours.append("SumQoE:");
////            ours.append(joinArrayElements(sumQoEList, beginTime, endTime)+"\n");
////            ours.append("FIndex:");
////            ours.append(joinArrayElements(FIndexList, beginTime, endTime)+"\n");
////            ours.append("FinalValue:");
////            ours.append(joinArrayElements(FinalList, beginTime, endTime)+"\n");
////            writer.write(ours.toString());
////
////            StringBuilder random = new StringBuilder();
////            ours.append("Random \n");
////            ours.append("SumQoE:");
////            ours.append(joinArrayElements(randomSumQoEList, beginTime, endTime)+"\n");
////            ours.append("FIndex:");
////            ours.append(joinArrayElements(randomFIndexList, beginTime, endTime)+"\n");
////            ours.append("FinalValue:");
////            ours.append(joinArrayElements(randomFinalList, beginTime, endTime)+"\n");
////            writer.write(ours.toString());
////
////            StringBuilder GCO = new StringBuilder();
////            ours.append("GCO \n");
////            ours.append("SumQoE:");
////            ours.append(joinArrayElements(GCOSumQoEList, beginTime, endTime)+"\n");
////            ours.append("FIndex:");
////            ours.append(joinArrayElements(GCOFIndexList, beginTime, endTime)+"\n");
////            ours.append("FinalValue:");
////            ours.append(joinArrayElements(GCOFinalList, beginTime, endTime)+"\n");
////            writer.write(ours.toString());
////
////
////            StringBuilder LFU = new StringBuilder();
////            ours.append("LFU \n");
////            ours.append("SumQoE:");
////            ours.append(joinArrayElements(LFUSumQoEList, beginTime, endTime)+"\n");
////            ours.append("FIndex:");
////            ours.append(joinArrayElements(LFUFIndexList, beginTime, endTime)+"\n");
////            ours.append("FinalValue:");
////            ours.append(joinArrayElements(LFUFinalList, beginTime, endTime)+"\n");
////            writer.write(ours.toString());
////
////        }
//    }
//
//
//    public static void runningMaxHop() throws IOException {
//        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
//        ourAlgorithm.addPredictRequest();
//        BaseRandom baseRandom = new BaseRandom();
//        BaseGCO baseGCO = new BaseGCO();
//        BaseLFU baseLFU = new BaseLFU();
//        String filePath = "src/result/max_hop.txt";
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
//            StringBuilder showResult = new StringBuilder();
//            for(int i=1;i<5;i++){
//                experimentalSetup.setMaxHop(i);
//
//                showResult.append("MaxHop:"+i+"\n");
//
//                AlgorithmResult ourAlgorithmResult = ourAlgorithm.findBestDecision(experimentalSetup.getBeginTimestamp());
//                showResult.append(ourAlgorithmResult.toString());
//
////                AlgorithmResult randomAlgorithmResult = baseRandom.initializeData(experimentalSetup);
////                showResult.append(randomAlgorithmResult.toString());
//
//                AlgorithmResult GCOAlgorithmResult = baseGCO.initializeData(experimentalSetup);
//                showResult.append(GCOAlgorithmResult.toString());
//
//                AlgorithmResult LFUAlgorithmResult = baseLFU.initializeData(experimentalSetup);
//                showResult.append(LFUAlgorithmResult.toString());
//            }
//
//            writer.write(showResult.toString());
//
//        }
//    }
//
    public static void runningMaxStorage() throws IOException {
        OurAlgorithm ourAlgorithm = new OurAlgorithm(experimentalSetup);
        ourAlgorithm.addPredictRequest();
        BaseRandom baseRandom = new BaseRandom();
        BaseGCO baseGCO = new BaseGCO();
        BaseLFU baseLFU = new BaseLFU();
        String filePath = "src/result/max_storage.txt";
        StringBuilder showResult = new StringBuilder();
        for(int i=3;i<=8;i++){
            experimentalSetup.setMaxStorageSpace(i);
            showResult.append("MaxStorage:"+i+"\n");

            AlgorithmResult ourAlgorithmResult = ourAlgorithm.findBestDecision(experimentalSetup.getBeginTimestamp());
            showResult.append(ourAlgorithmResult.toString());
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


    }



}
