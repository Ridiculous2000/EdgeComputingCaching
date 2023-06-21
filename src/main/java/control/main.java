package control;

import our_algorithm.OurAlgorithm;

import java.io.IOException;

public class main {

    public static int minTimestamp = 0;
    public static int maxTimestamp = 50;
    public static void main(String[] args) throws IOException {
        //�����������
//        InitBasicData initBasicData = new InitBasicData();
//        initBasicData.insertBasicUser();
//        initBasicData.insertBasicEdgeServer();
        //������Ҫ������
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
        //����ֽ�
//        OtherUtils.buildRequestMatrix(0,50);
        OurAlgorithm ourAlgorithm = new OurAlgorithm();
        ourAlgorithm.InitializeData();
        System.out.println("adw");

    }
}
