package by.bsuir.aleksandrov.imit_model;

class Product {
    private static int nextId = 1;
    private final int id;
    private int defectCount;
    private int currentStation;
    private Double startTimeInSystem = null;
    private boolean inAdjQueue = false;

    public Product() {
        this.id = nextId++;
        this.defectCount = 0;
        this.currentStation = 0;
    }

    public boolean isInAdjQueue() {
        return inAdjQueue;
    }

    public void setInAdjQueue(boolean inAdjQueue) {
        this.inAdjQueue = inAdjQueue;
    }

    public Double getStartTimeInSystem() {
        return startTimeInSystem;
    }

    public void setStartTimeInSystem(Double startTimeInSystem) {
        this.startTimeInSystem = startTimeInSystem;
    }

    public int getId() {
        return id;
    }

    public int getDefectCount() {
        return defectCount;
    }

    public void incrementDefect() {
        this.defectCount++;
    }

    public int getCurrentStation() {
        return currentStation;
    }

    public void moveToFirstStation() {
        this.currentStation = 0;
    }

    public void moveToNextStation() {
        this.currentStation++;
    }

    public boolean isFullyProcessed() {
        return currentStation >= 3;
    }
}