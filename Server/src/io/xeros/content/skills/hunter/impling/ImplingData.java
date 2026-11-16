package io.xeros.content.skills.hunter.impling;
import java.util.Arrays;
/** Factual creature IDs, XP and capture coefficients from historical 2022 references. */
public enum ImplingData {
    BABY("Baby impling",11238,17,18,20,79,402,1635,1645),
    YOUNG("Young impling",11240,22,20,22,69,351,1636,1646),
    GOURMET("Gourmet impling",11242,28,22,24,61,325,1637,1647),
    EARTH("Earth impling",11244,36,25,27,51,302,1638,1648),
    ESSENCE("Essence impling",11246,42,27,29,40,275,1639,1649),
    ECLECTIC("Eclectic impling",11248,50,30,32,30,250,1640,1650),
    NATURE("Nature impling",11250,58,34,36,20,200,1641,1651),
    MAGPIE("Magpie impling",11252,65,44,216,15,177,1642,1652),
    NINJA("Ninja impling",11254,74,50,240,10,151,1643,1653),
    CRYSTAL("Crystal impling",23768,80,0,280,8,141,8741,8742,8743,8744,8745,8746,8747,8748,8749,8750,8751,8752,8753,8754,8755,8756,8757),
    DRAGON("Dragon impling",11256,83,65,300,5,125,1644,1654),
    LUCKY("Lucky impling",19732,89,80,380,3,100,7233,7302);
    public final String name;
    public final int jar,requirement,experience,wildExperience,low,high,npcId;
    private final int[] npcIds;
    ImplingData(String name,int jar,int level,int puroXp,int wildXp,int low,int high,int... ids){this.name=name;this.jar=jar;requirement=level;experience=puroXp;wildExperience=wildXp;this.low=low;this.high=high;npcIds=ids.clone();npcId=ids[ids.length-1];}
    public int[] npcIds(){return npcIds.clone();}
    public int xp(boolean puro){return puro?experience:wildExperience;}
    public int threshold(int level,boolean improved){
        level=Math.max(1,Math.min(126,level));int bonus=improved?20:0;
        // The 2022 chart truncates the two interpolation terms separately, then adds one.
        int value=(low+bonus)*(99-level)/98+(high+bonus)*(level-1)/98+1;
        return Math.max(0,Math.min(256,value));
    }
    public static ImplingData forId(int id){for(ImplingData d:values())for(int n:d.npcIds)if(n==id)return d;return null;}
    public static ImplingData forJar(int id){for(ImplingData d:values())if(d.jar==id)return d;return null;}
}
