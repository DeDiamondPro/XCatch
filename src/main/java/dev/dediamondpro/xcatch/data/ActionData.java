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

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ActionData implements Comparable<ActionData> {
    public ActionType type;
    public long time;
    public String ore;
    public int amount;
    public UUID worldUID;
    public int x;
    public int y;
    public int z;

    public ActionData(ActionType type, long time, String ore, int amount, UUID worldUID, int x, int y, int z) {
        this.type = type;
        this.time = time;
        this.ore = ore;
        this.amount = amount;
        this.worldUID = worldUID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compareTo(@NotNull ActionData o) {
        return (int) (o.time - time);
    }

    public enum ActionType {
        @SerializedName("0")
        BAN,
        @SerializedName("1")
        FLAG
    }
}
