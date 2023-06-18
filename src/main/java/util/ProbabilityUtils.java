package util;

import java.util.*;

public class ProbabilityUtils {

    //根据传入的总数和要选择的数，返回一个判断是否选中的flag数组
    public static boolean[] randomSelect(int allNum,int selectNum){
        Random random = new Random();
        int remaining = allNum;
        boolean[] flagList = new boolean[allNum];
        for (int i = 0; i < selectNum; i++) {
            int index = random.nextInt(remaining);
            int count = 0;
            // 遍历数组，找到第index个未选择的数，修改为true
            for (int j = 0; j < flagList.length; j++) {
                if (!flagList[j]) {
                    if (count == index) {
                        flagList[j] = true;
                        remaining--;
                        break;
                    }
                    count++;
                }
            }
        }
        return flagList;
    }

    public static Map<Integer, Double> getZipFProbability(List<Integer> rankList,double S){
        Map<Integer, Double> zipFProbability = new HashMap<>();
        int n = rankList.size();
        // 计算归一化因子
        double norm = 0.0;
        for (int i = 1; i <= n; i++) {
            norm += 1.0 / Math.pow(i, S);
        }
        // 计算每个数被选中的概率
        for (int i = 0; i < n; i++) {
            int value = rankList.get(i);
            double probability = (1.0 / Math.pow(i + 1, S)) / norm;
            zipFProbability.put(value, probability);
        }
        return zipFProbability;
    }

}
