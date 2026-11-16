package io.xeros.content.skills.hunter;

import io.xeros.model.items.GameItem;
import java.util.Random;

/** Individual creature pages frozen at 2022; older equipment overview tables round some XP. */
public enum GroundTrapSpecies2022 {
    CRIMSON_SWIFT(5549, 1, 34, 10088, 9373, new int[]{9349}, 100, 420, 0, -1),
    GOLDEN_WARBLER(5551, 5, 47, 10090, 9377, new int[]{9376}, 92, 400, 0, -1),
    COPPER_LONGTAIL(5552, 9, 61.2, 10091, 9379, new int[]{9378}, 85, 390, 0, -1),
    CERULEAN_TWITCH(5550, 11, 64.5, 10089, 9375, new int[]{9374}, 82, 380, 0, -1),
    TROPICAL_WAGTAIL(5548, 19, 95.2, 10087, 9348, new int[]{9347}, 75, 370, 0, -1),
    FERRET(1505, 27, 115.2, 10092, 9384, new int[]{9394, 9395, 9396, 9397}, 0, 0, 0, 2132),
    CHINCHOMPA(2910, 53, 198.4, 10033, 9382, new int[]{9386, 9387, 9388, 9389}, 6, 268, 131395, 9994),
    CARNIVOROUS_CHINCHOMPA(2911, 63, 265, 10034, 9383, new int[]{9390, 9391, 9392, 9393}, -78, 228, 98373, 9996),
    BLACK_CHINCHOMPA(2912, 73, 315, 11959, 721, new int[]{2025, 2026, 2028, 2029}, -78, 228, 82758, 9996);

    public final int npc, level, product, fullObject, low, high, petBaseChance, preferredBait;
    public final double xp;
    private final int[] enteringObjects;

    GroundTrapSpecies2022(int npc, int level, double xp, int product, int fullObject,
                          int[] enteringObjects, int low, int high, int petBaseChance, int preferredBait) {
        this.npc=npc; this.level=level; this.xp=xp; this.product=product; this.fullObject=fullObject;
        this.enteringObjects=enteringObjects; this.low=low; this.high=high;
        this.petBaseChance=petBaseChance; this.preferredBait=preferredBait;
    }

    public boolean bird() { return ordinal() < 5; }
    public boolean chinchompa() { return petBaseChance > 0; }
    public int trapItem() { return bird() ? 10006 : 10008; }
    public int openObject() { return bird() ? 9345 : 9380; }
    public int failedObject() { return bird() ? 9344 : 9385; }
    /** Direction: north, east, south, west. Bird scenery rotates as a whole instead. */
    public int enteringObject(int direction) {
        return enteringObjects.length==1 ? enteringObjects[0] : enteringObjects[Math.floorMod(direction,4)];
    }

    public GameItem[] reward(Random random) {
        return bird() ? new GameItem[]{new GameItem(526,1), new GameItem(9978,1),
                new GameItem(product,5+random.nextInt(6))} : new GameItem[]{new GameItem(product,1)};
    }

    public boolean acceptsBait(int item) {
        // The ferret raw-meat family follows older Hunter references; exact 2022 acceptance remains under review.
        if(this==FERRET) return item==2132 || item==2134 || item==2136 || item==2138;
        return preferredBait>0 && item==preferredBait;
    }

    /** Hundredths of one byte: additive smoke/bait bonuses retain their fractional percentage points.
     * Published level endpoints are interpolated with a local integer rounding convention.
     * Ferret has no published endpoint curve: its 35%-95% interpolation is explicitly local.
     */
    public int threshold(int hunter, boolean smoked, int bait) {
        hunter=Math.max(1,Math.min(126,hunter));
        if(hunter<level) return 0;
        int base=this==FERRET ? (int)Math.floor((35+60d*(hunter-27)/72)*256)
                : (Math.floorDiv(low*(99-hunter)+high*(hunter-1),98)+1)*100;
        return Math.max(0,Math.min(25600,base+(smoked?512:0)+(acceptsBait(bait)?768:0)));
    }

    /** Pet chance uses base Hunter, with the 200m-XP multiplier; gold is a metamorphosis, not a separate drop. */
    public int petDenominator(int baseHunter) {
        if(!chinchompa()) return 0;
        return petBaseChance-Math.max(1,Math.min(99,baseHunter))*25;
    }

    public boolean pet(Random random, int baseHunter, boolean maximumXp) {
        int denominator=petDenominator(baseHunter);
        return denominator>0 && random.nextInt(denominator)<(maximumXp?15:1);
    }

    public static GroundTrapSpecies2022 npc(int id) {
        for(GroundTrapSpecies2022 species:values()) if(species.npc==id) return species;
        return null;
    }
}
