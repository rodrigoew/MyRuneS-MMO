package io.xeros.content.skills.farming;

/** Published 2017 disease rolls. Unpublished crops retain the base's approximate risk. */
public final class FarmingDisease {
    private FarmingDisease() {}

    public static int threshold(Plants crop,int compost,boolean watered) {
        int raw=crop.type==SeedType.HERB?26:crop.type==SeedType.FRUIT_TREE?17:
                crop==Plants.MAPLE_TREE?12:crop==Plants.MAGIC_TREE?8:12;
        // Watering lowers risk; its exact reduction for unpublished crops is approximated here.
        if(watered)raw/=2;
        if(compost==1)raw/=2;else if(compost==2)raw/=5;else if(compost>=3)raw/=10;
        return raw+1;
    }

    public static boolean susceptible(Plants crop,int stage) {
        return stage>0&&stage<crop.stages&&!(crop.type==SeedType.FRUIT_TREE&&stage==crop.stages-1);
    }
}
