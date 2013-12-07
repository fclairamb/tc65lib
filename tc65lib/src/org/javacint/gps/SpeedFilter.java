package org.javacint.gps;

/**
 *
 */
class SpeedFilter {

    final double maxSpeed;
    final int period;

    public SpeedFilter(int period, double minSpeed) {
        this.maxSpeed = minSpeed;
        this.period = period;
    }

    public double getMinSpeed() {
        return this.maxSpeed;
    }

    public int getPeriod() {
        return this.period;
    }
}
