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

package io.github.dediamondpro.xcatch.data;

import com.google.gson.annotations.SerializedName;

public class ActionData implements Comparable {
    public ActionType type;
    public long time;

    public ActionData(ActionType type, long time) {
        this.type = type;
        this.time = time;
    }

    @Override
    public int compareTo(Object o) {
        ActionData actionData = (ActionData) o;
        return (int) (actionData.time - time);
    }

    public enum ActionType {
        @SerializedName("0")
        BAN,
        @SerializedName("1")
        FLAG
    }
}
