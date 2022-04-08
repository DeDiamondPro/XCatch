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
