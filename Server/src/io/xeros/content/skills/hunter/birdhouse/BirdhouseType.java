package io.xeros.content.skills.hunter.birdhouse;

/** Pre-August-2026 XP; the local game's content target is OSRS 2022. */
public enum BirdhouseType {
    REGULAR(1511,21512,5,5,15,280,.100),
    OAK(1521,21515,15,14,20,420,.125),
    WILLOW(1519,21518,25,24,25,560,.128),
    TEAK(6333,21521,35,34,30,700,.130),
    MAPLE(1517,22192,45,44,35,820,.140),
    MAHOGANY(6332,22195,50,49,40,960,.150),
    YEW(1515,22198,60,59,45,1020,.160),
    MAGIC(1513,22201,75,74,50,1140,.170),
    REDWOOD(19669,22204,90,89,55,1200,.175);
    public final int log,item,craftingLevel,hunterLevel;
    public final double craftingXp,hunterXp,nestChanceAt99;
    BirdhouseType(int log,int item,int craftingLevel,int hunterLevel,double craftingXp,double hunterXp,double nestChanceAt99){
        this.log=log;this.item=item;this.craftingLevel=craftingLevel;this.hunterLevel=hunterLevel;this.craftingXp=craftingXp;this.hunterXp=hunterXp;this.nestChanceAt99=nestChanceAt99;
    }
    public static BirdhouseType forLog(int item){for(BirdhouseType type:values())if(type.log==item)return type;return null;}
    public static BirdhouseType forItem(int item){for(BirdhouseType type:values())if(type.item==item)return type;return null;}
}
