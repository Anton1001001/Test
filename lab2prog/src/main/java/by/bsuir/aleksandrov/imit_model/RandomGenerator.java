package by.bsuir.aleksandrov.imit_model;

import java.util.Random;

public class RandomGenerator {
    private final Random random = new Random();

    public double generateNormalTime(double mean, double variance) {
        return mean + random.nextGaussian() * Math.sqrt(variance);
    }

    public double generateUniformTime(double min, double max) {
        return min + random.nextDouble(max - min + 1);
    }

    public boolean generateDefect(double probability) {
        return random.nextDouble() < probability;
    }
}
