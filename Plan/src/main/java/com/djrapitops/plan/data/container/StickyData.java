/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plugin.api.TimeAmount;
import com.google.common.base.Objects;

import java.util.List;

public class StickyData {
    private final double activityIndex;
    private Double messagesSent;
    private Double onlineOnJoin;

    public StickyData(PlayerProfile player) {
        activityIndex = player.getActivityIndex(player.getRegistered() + TimeAmount.DAY.ms()).getValue();
        loadActionVariables(player.getActions());
    }

    public StickyData(double activityIndex, Double messagesSent, Double onlineOnJoin) {
        this.activityIndex = activityIndex;
        this.messagesSent = messagesSent;
        this.onlineOnJoin = onlineOnJoin;
    }

    private void loadActionVariables(List<Action> actions) {
        for (Action action : actions) {
            try {
                if (messagesSent == null && action.getDoneAction() == Actions.FIRST_LOGOUT) {
                    messagesSent = (double) loadSentMessages(action);
                }
                if (onlineOnJoin == null && action.getDoneAction() == Actions.FIRST_SESSION) {
                    onlineOnJoin = (double) loadOnlineOnJoin(action);
                }
            } catch (IllegalArgumentException ignore) {
                /* continue */
            }
        }
        setDefaultValuesIfNull();
    }

    private void setDefaultValuesIfNull() {
        if (messagesSent == null) {
            messagesSent = 0.0;
        }
        if (onlineOnJoin == null) {
            onlineOnJoin = 0.0;
        }
    }

    private int loadOnlineOnJoin(Action action) {
        String additionalInfo = action.getAdditionalInfo();
        String[] split = additionalInfo.split(" ");
        if (split.length == 3) {
            return Integer.parseInt(split[1]);
        }
        throw new IllegalArgumentException("Improper Action");
    }

    private int loadSentMessages(Action action) {
        String additionalInfo = action.getAdditionalInfo();
        String[] split = additionalInfo.split(": ");
        if (split.length == 2) {
            return Integer.parseInt(split[1]);
        }
        throw new IllegalArgumentException("Improper Action");
    }

    public double distance(StickyData data) {
        double num = 0;
        num += Math.abs(data.activityIndex - activityIndex) * 2.0;
        num += Math.abs(data.onlineOnJoin - onlineOnJoin) / 10.0;
        num += Math.abs(data.messagesSent - messagesSent) / 10.0;

        return num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StickyData that = (StickyData) o;
        return Double.compare(that.activityIndex, activityIndex) == 0 &&
                Objects.equal(messagesSent, that.messagesSent) &&
                Objects.equal(onlineOnJoin, that.onlineOnJoin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activityIndex, messagesSent, onlineOnJoin);
    }

    public double getOnlineOnJoin() {
        return onlineOnJoin;
    }

    public double getActivityIndex() {
        return activityIndex;
    }

    public Double getMessagesSent() {
        return messagesSent;
    }
}
