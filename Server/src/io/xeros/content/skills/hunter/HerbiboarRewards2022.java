package io.xeros.content.skills.hunter;

import io.xeros.model.items.GameItem;
import java.util.*;

/** Frozen 2022 herb order, published ranges and base XP; no later Hunter Guild rewards. */
public final class HerbiboarRewards2022 {
    public enum Herb {
        RANARR(207,-10,20,32,99), TORSTOL(219,-70,20,77,99),
        SNAPDRAGON(3051,-60,20,75,99), DWARF_WEED(217,-50,30,62,99),
        LANTADYME(2485,-30,40,41,99), CADANTINE(215,-10,50,31,99),
        KWUARM(213,10,60,31,99), AVANTOE(211,20,60,31,99),
        IRIT(209,30,70,31,99), TARROMIN(203,70,-20,31,78),
        HARRALANDER(205,100,-30,31,78), MARRENTILL(201,170,-40,31,80);
        public final int item,low,high,min,max;
        Herb(int item,int low,int high,int min,int max){this.item=item;this.low=low;this.high=high;this.min=min;this.max=max;}
        /** Local integer interpolation; the published eligibility ranges take precedence at edges. */
        public int threshold(int level){
            level=Math.max(1,Math.min(99,level));if(level<min||level>max)return 0;
            return Math.max(1,Math.min(256,low*(99-level)/98+high*(level-1)/98+1));
        }
    }
    public static final int GUAM=199,HERBI=21509,NUMULITE=21555;
    private HerbiboarRewards2022(){}
    public static int hunterXp(int baseLevel){
        baseLevel=Math.max(1,Math.min(99,baseLevel));
        return baseLevel<=94?Math.max(0,1950+(baseLevel-80)*30):2385+(baseLevel-95)*19;
    }
    /** Independent cascading rolls, not a normalized weighted herb table. Guam is the fallback. */
    public static int herb(Random random,int herblore){
        for(Herb herb:Herb.values()){int threshold=herb.threshold(herblore);if(threshold>0&&random.nextInt(256)<threshold)return herb.item;}
        return GUAM;
    }
    public static final class Harvest {
        public final int baseAmount,herbloreXp;public final int[][] items;
        Harvest(int baseAmount,int[][] items){this.baseAmount=baseAmount;this.herbloreXp=baseAmount*25;this.items=items;}
    }
    /** The extra equipped-secateurs herb does not grant additional Herblore experience. */
    public static Harvest harvest(Random random,int herblore,boolean equippedMagicSecateurs){
        int amount=1+random.nextInt(3);Map<Integer,Integer> items=new LinkedHashMap<>();
        for(int n=0;n<amount+(equippedMagicSecateurs?1:0);n++)items.merge(herb(random,herblore),1,Integer::sum);
        return new Harvest(amount,items.entrySet().stream().map(e->new int[]{e.getKey(),e.getValue()}).toArray(int[][]::new));
    }
    /** The four fossil frequencies are published; numulite's 1/8 chance is a local choice. */
    public static GameItem inspection(Random random){
        int roll=random.nextInt(2500);
        if(roll<50)return new GameItem(21562,1);
        if(roll<75)return new GameItem(21564,1);
        if(roll<95)return new GameItem(21566,1);
        if(roll<100)return new GameItem(21568,1);
        return random.nextInt(8)==0?new GameItem(NUMULITE,5+random.nextInt(21)):null;
    }
    public static boolean pet(Random random){return random.nextInt(6500)==0;}
}
