package by.bsuir.aleksandrov.imit_model;

public class AdjustmentStation {
    private final double meanAdjustmentTime;
    private final double varianceAdjustmentTime;
    private double nextFreeTime = 0;

    public AdjustmentStation(double meanAdjustmentTime, double varianceAdjustmentTime) {
        this.meanAdjustmentTime = meanAdjustmentTime;
        this.varianceAdjustmentTime = varianceAdjustmentTime;
    }

    public double getAdjustmentTime(RandomGenerator generator) {
        return generator.generateNormalTime(meanAdjustmentTime, varianceAdjustmentTime);
    }

    public double getNextFreeTime() {
        return nextFreeTime;
    }

    public void setNextFreeTime(double nextFreeTime) {
        this.nextFreeTime = nextFreeTime;
    }
}

