package by.bsuir.aleksandrov.imit_model;

class Event implements Comparable<Event> {
    public static final int GENERATE_PRODUCT = 1;
    public static final int START_PROCESSING = 2;
    public static final int END_PROCESSING = 3;
    public static final int START_ADJUSTMENT = 4;
    public static final int END_ADJUSTMENT = 5;

    private final double eventTime;
    private final int eventType;
    private final Product product;

    public Event(double eventTime, int eventType, Product product) {
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.product = product;
    }

    public double getEventTime() {
        return eventTime;
    }

    public int getEventType() {
        return eventType;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public int compareTo(Event other) {
        return Double.compare(this.eventTime, other.eventTime);
    }

}
