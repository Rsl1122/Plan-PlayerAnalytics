package com.djrapitops.plan.data.time;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import utilities.RandomData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class WorldTimesTest {

    private final String worldOne = "ONE";
    private final String worldTwo = "TWO";
    private final String[] gms = GMTimes.getGMKeyArray();
    private long time = System.currentTimeMillis();
    private WorldTimes worldTimes = new WorldTimes(worldOne, gms[0], time);

    @Test
    public void stateAffectedByWorldChange() {
        long changeTime = time + 1000L;
        worldTimes.updateState(worldTwo, gms[0], changeTime);

        assertEquals(1000L, worldTimes.getWorldPlaytime(worldOne));
        assertEquals(1000L, worldTimes.getGMTimes(worldOne).getTime(gms[0]));
    }

    @Test
    public void stateAffectedByGamemodeChange() {
        long changeTime = time + 1000L;
        worldTimes.updateState(worldOne, gms[0], changeTime);

        assertEquals(1000L, worldTimes.getWorldPlaytime(worldOne));
        assertEquals(1000L, worldTimes.getGMTimes(worldOne).getTime(gms[0]));
    }

    @Test
    public void stateAffectedByTwoChangesAtOnce() {
        long changeTime = time + 1000L;
        long changeTime2 = changeTime + 1000L;

        worldTimes.updateState(worldTwo, gms[2], changeTime);

        assertEquals(1000L, worldTimes.getWorldPlaytime(worldOne));
        assertEquals(1000L, worldTimes.getGMTimes(worldOne).getTime(gms[0]));

        worldTimes.updateState(worldOne, gms[1], changeTime2);

        assertEquals(1000L, worldTimes.getWorldPlaytime(worldOne));
        assertEquals(1000L, worldTimes.getGMTimes(worldOne).getTime(gms[0]));
        assertEquals(1000L, worldTimes.getGMTimes(worldTwo).getTime(gms[2]));
    }

    @Test
    public void stateAffectedByManyWorldChanges() {
        long amount = 1000L;
        String[] worlds = new String[]{worldOne, worldTwo};

        Map<String, List<String>> testedW = ImmutableMap.of(worldOne, new ArrayList<>(), worldTwo, new ArrayList<>());

        String lastWorld = worldOne;
        String lastGM = gms[0];
        for (int i = 1; i <= 50; i++) {
            int wRndm = RandomData.randomInt(0, worlds.length);
            int gmRndm = RandomData.randomInt(0, gms.length);

            String world = worlds[wRndm];
            String gm = gms[gmRndm];
            testedW.get(lastWorld).add(lastGM);
            lastGM = gm;
            lastWorld = world;

            long time = i * amount + this.time;

            worldTimes.updateState(world, gm, time);
        }

        long worldOneCount = testedW.get(worldOne).size();
        long worldTwoCount = testedW.get(worldTwo).size();
        long worldTimeOne = worldOneCount * amount;
        long worldTimeTwo = worldTwoCount * amount;

        long time1 = worldTimes.getWorldPlaytime(worldOne);
        long time2 = worldTimes.getWorldPlaytime(worldTwo);

        // Tests World time calculation.
        assertEquals(amount * 50, time1 + time2);
        assertEquals(worldTimeOne, time1);
        assertEquals(worldTimeTwo, time2);
    }

    @Test
    public void gamemodeTrackingWorksForASingleWorld() {
        long changeTime = time + 1000L;
        long changeTime2 = changeTime + 1000L;

        GMTimes gmTimes = worldTimes.getGMTimes(worldOne);

        worldTimes.updateState(worldOne, "CREATIVE", changeTime);

        assertEquals(1000L, gmTimes.getTime("SURVIVAL"));
        assertEquals(0L, gmTimes.getTime("CREATIVE"));

        worldTimes.updateState(worldOne, "ADVENTURE", changeTime2);

        assertEquals(1000L, gmTimes.getTime("SURVIVAL"));
        assertEquals(1000L, gmTimes.getTime("CREATIVE"));
        assertEquals(0L, gmTimes.getTime("ADVENTURE"));
    }

    @Test
    public void gamemodeTrackingWorksForTwoWorlds() {
        long changeTime = time + 1000L;
        long changeTime2 = time + 2000L;

        GMTimes worldOneGMTimes = worldTimes.getGMTimes(worldOne);

        worldTimes.updateState(worldOne, "CREATIVE", changeTime);
        worldTimes.updateState(worldOne, "ADVENTURE", changeTime2);

        assertEquals(1000L, worldOneGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldOneGMTimes.getTime("CREATIVE"));
        assertEquals(0L, worldOneGMTimes.getTime("ADVENTURE"));

        worldTimes.updateState(worldTwo, "SURVIVAL", time + 3000L);
        GMTimes worldTwoGMTimes = worldTimes.getGMTimes(worldTwo);

        assertEquals(1000L, worldOneGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldOneGMTimes.getTime("CREATIVE"));
        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));

        assertEquals(0L, worldTwoGMTimes.getTime("SURVIVAL"));
        assertEquals(0L, worldTwoGMTimes.getTime("CREATIVE"));
        assertEquals(0L, worldTwoGMTimes.getTime("ADVENTURE"));

        worldTimes.updateState(worldTwo, "CREATIVE", time + 4000L);

        assertEquals(1000L, worldOneGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldOneGMTimes.getTime("CREATIVE"));
        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));

        assertEquals(1000L, worldTwoGMTimes.getTime("SURVIVAL"));
        assertEquals(0L, worldTwoGMTimes.getTime("CREATIVE"));

        worldTimes.updateState(worldTwo, "CREATIVE", time + 5000L);

        assertEquals(1000L, worldTwoGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldTwoGMTimes.getTime("CREATIVE"));

        // No change should occur.
        worldTimes.updateState(worldOne, "ADVENTURE", time + 5000L);

        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));
        assertEquals(1000L, worldTwoGMTimes.getTime("CREATIVE"));

        worldTimes.updateState(worldTwo, "CREATIVE", time + 5000L);
        worldTimes.updateState(worldOne, "ADVENTURE", time + 6000L);

        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));
        assertEquals(2000L, worldTwoGMTimes.getTime("CREATIVE"));

        worldTimes.updateState(worldTwo, "ADVENTURE", time + 7000L);

        assertEquals(2000L, worldTwoGMTimes.getTime("CREATIVE"));
        assertEquals(2000L, worldOneGMTimes.getTime("ADVENTURE"));
    }
}