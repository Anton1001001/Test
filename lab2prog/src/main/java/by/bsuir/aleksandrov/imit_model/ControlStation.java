package by.bsuir.aleksandrov.imit_model;

public class ControlStation {
    private final double minProcessTime;
    private final double maxProcessTime;
    private final double defectProbability;
    private double nextFreeTime = 0;

    public ControlStation(double minProcessTime, double maxProcessTime, double defectProbability) {
        this.minProcessTime = minProcessTime;
        this.maxProcessTime = maxProcessTime;
        this.defectProbability = defectProbability;
    }

    public double getProcessTime(RandomGenerator generator) {
        return generator.generateUniformTime(minProcessTime, maxProcessTime);
    }

    public boolean shouldSendForAdjustment(RandomGenerator generator) {
        return generator.generateDefect(defectProbability);
    }

    public double getNextFreeTime() {
        return nextFreeTime;
    }

    public void setNextFreeTime(double nextFreeTime) {
        this.nextFreeTime = nextFreeTime;
    }
}
