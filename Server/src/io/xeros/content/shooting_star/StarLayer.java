package io.xeros.content.shooting_star;
/** Public historical 2022 XP, quantities, pet denominators and rounded success endpoints. */
public enum StarLayer {
 ONE(1,41229,12,1200,911340,30,100),TWO(2,41228,22,700,609390,30,57),
 THREE(3,41227,26,430,554490,30,49),FOUR(4,41226,31,250,521550,30,44),
 FIVE(5,41225,48,175,373320,23,30),SIX(6,41224,74,80,244305,15,20),
 SEVEN(7,41223,123,40,148230,9,13),EIGHT(8,41021,162,40,118035,7,10),
 NINE(9,41020,244,15,87840,6,7);
 public final int size,objectId,xp,health,petBase,lowPercent,highPercent;
 StarLayer(int size,int objectId,int xp,int health,int petBase,int low,int high){this.size=size;this.objectId=objectId;this.xp=xp;this.health=health;this.petBase=petBase;lowPercent=low;highPercent=high;}
 public int level(){return size*10;}public int extraDustPercent(){return size*(size+1);}
 public int threshold(int visible,int invisible){int level=Math.min(99,Math.max(1,visible))+Math.max(0,invisible);return Math.max(0,Math.min(256,(int)Math.floor(256d*(lowPercent*(99d-level)+highPercent*(level-1d))/(98d*100d)+.5)));}
 public int petDenominator(int baseLevel){return Math.max(1,petBase-25*Math.min(99,Math.max(1,baseLevel)));}
 public static StarLayer size(int size){return size>=1&&size<=9?values()[size-1]:null;}
 public static StarLayer object(int id){for(StarLayer layer:values())if(layer.objectId==id)return layer;return null;}
 public static int naturalSize(int roll){if(roll<0||roll>=100)throw new IllegalArgumentException("Star roll");return roll<16?2:roll<34?3:roll<54?4:roll<71?5:roll<83?6:roll<92?7:roll<97?8:9;}
}
