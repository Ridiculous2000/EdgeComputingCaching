package control;

import data_generation.GenerateData;
import data_generation.InitBasicData;

import java.util.ArrayList;

public class main {
    public static void main(String[] args) {
//        InitBasicData initBasicData = new InitBasicData();
//        initBasicData.insertBasicUser();
//        initBasicData.insertBasicEdgeServer();
        GenerateData generateData = new GenerateData();
//        generateData.addExperimentalUser(400);
//        generateData.addExperimentalServer(40);
//        generateData.newPopularData(100,3);
//        generateData.newUserDataProbability(1.5);
        ArrayList<Integer> timestampList = new ArrayList<Integer>();
        for(int i=1;i<=80;i++){
            timestampList.add(i);
        }
        generateData.newRequest(timestampList);
    }
}
