package bean;

public class AlgorithmResult {
    String algorithmName;
    double sumQoE;
    double FIndex;
    double FinalValue;

    @Override
    public String toString() {
        return "algorithmName='" + algorithmName + '\'' +
                ", sumQoE=" + sumQoE +
                ", FIndex=" + FIndex +
                ", FinalValue=" + FinalValue+"\n" ;
    }

    public AlgorithmResult(String algorithmName, double sumQoE, double FIndex, double finalValue) {
        this.algorithmName = algorithmName;
        this.sumQoE = sumQoE;
        this.FIndex = FIndex;
        FinalValue = finalValue;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public double getSumQoE() {
        return sumQoE;
    }

    public void setSumQoE(double sumQoE) {
        this.sumQoE = sumQoE;
    }

    public double getFIndex() {
        return FIndex;
    }

    public void setFIndex(double FIndex) {
        this.FIndex = FIndex;
    }

    public double getFinalValue() {
        return FinalValue;
    }

    public void setFinalValue(double finalValue) {
        FinalValue = finalValue;
    }
}
