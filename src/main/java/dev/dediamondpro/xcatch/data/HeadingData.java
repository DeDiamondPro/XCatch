/*
 * This file is part of XCatch.
 *
 * XCatch is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * XCatch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see
 * <https://www.gnu.org/licenses/>.
 */

/*
 * This file is part of XCatch.
 *
 * XCatch is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * XCatch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package dev.dediamondpro.xcatch.data;

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
