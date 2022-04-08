package io.github.dediamondpro.xcatch.data;

import java.util.ArrayList;

public class HeadingData {
    public ArrayList<Float> headings = new ArrayList<>();
    public int changes = 0;
    public long lastChange = 0;
    public long lastRareOre = 0;

    public HeadingData(float... headings) {
        for (float heading : headings)
            this.headings.add(heading);
    }

    public double getAverage() {
        double x = 0;
        double y = 0;
        for (float angle : headings) {
            x += Math.cos(Math.toRadians(angle));
            y += Math.sin(Math.toRadians(angle));
        }
        return Math.toDegrees(Math.atan2(y, x));
    }
}
