package io.xeros.content.skills.mining.motherlode;
import java.util.function.IntUnaryOperator;
/** Tier order/coefficients reference OpenRune ISC; coal XP and nugget roll use pre-2023 rules. */
public enum MotherlodeOre {
    NUGGET(12012,30,0,7,7),RUNITE(451,85,75,-20,5),ADAMANTITE(449,70,45,-90,50),
    MITHRIL(447,55,30,-19,90),GOLD(444,40,15,-40,126),COAL(453,30,0,0,0);
    public final int item,level,low,high;public final double xp;
    MotherlodeOre(int item,int level,double xp,int low,int high){this.item=item;this.level=level;this.xp=xp;this.low=low;this.high=high;}
    public int threshold(int level){
        int n=Math.max(1,Math.min(99,level));
        if(this==NUGGET)return n>=43&&(n-43)%14==0?8:7;
        return Math.max(0,Math.min(256,1+(int)Math.floor((low*(99d-n)+high*(n-1d))/98d+.5)));
    }
    public static MotherlodeOre roll(int miningLevel,IntUnaryOperator random){
        for(MotherlodeOre ore:values())if(ore==COAL)return COAL;else if(miningLevel>=ore.level&&random.applyAsInt(256)<ore.threshold(miningLevel))return ore;
        return COAL;
    }
}
