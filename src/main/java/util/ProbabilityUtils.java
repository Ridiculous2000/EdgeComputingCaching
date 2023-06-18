package util;

import java.util.*;

public class ProbabilityUtils {

    //���ݴ����������Ҫѡ�����������һ���ж��Ƿ�ѡ�е�flag����
    public static boolean[] randomSelect(int allNum,int selectNum){
        Random random = new Random();
        int remaining = allNum;
        boolean[] flagList = new boolean[allNum];
        for (int i = 0; i < selectNum; i++) {
            int index = random.nextInt(remaining);
            int count = 0;
            // �������飬�ҵ���index��δѡ��������޸�Ϊtrue
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
        // �����һ������
        double norm = 0.0;
        for (int i = 1; i <= n; i++) {
            norm += 1.0 / Math.pow(i, S);
        }
        // ����ÿ������ѡ�еĸ���
        for (int i = 0; i < n; i++) {
            int value = rankList.get(i);
            double probability = (1.0 / Math.pow(i + 1, S)) / norm;
            zipFProbability.put(value, probability);
        }
        return zipFProbability;
    }

}
