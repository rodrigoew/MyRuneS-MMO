package io.xeros.content.skills.agility;
import io.xeros.model.definitions.*;
import io.xeros.model.entity.player.Player;
import java.util.*;
/** Frozen 2022 weight and energy arithmetic; 100 internal units per displayed percent. */
public final class RunEnergy2022 {
    public static final class Equipment {
        public final int inventory,worn,inventoryGrams,wornGrams,slot,recoveryPercent;
        Equipment(int inventory,int worn,int inventoryGrams,int wornGrams,int slot,int recoveryPercent){this.inventory=inventory;this.worn=worn;this.inventoryGrams=inventoryGrams;this.wornGrams=wornGrams;this.slot=slot;this.recoveryPercent=recoveryPercent;}
    }
    public static final Equipment[] EQUIPMENT={
        new Equipment(11850,11851,600,-3000,0,3),
        new Equipment(11852,11853,800,-4000,1,3),
        new Equipment(11854,11855,900,-5000,4,4),
        new Equipment(11856,11857,900,-6000,7,4),
        new Equipment(11858,11859,100,-3000,9,3),
        new Equipment(11860,11861,400,-4000,10,3),
        new Equipment(27459,27461,400,-4000,10,3),
        new Equipment(21076,21078,400,-4000,10,3),
        new Equipment(13589,13590,400,-4000,10,3),
        new Equipment(24758,24760,400,-4000,10,3),
        new Equipment(13637,13638,400,-4000,10,3),
        new Equipment(13677,13678,400,-4000,10,3),
        new Equipment(13613,13614,400,-4000,10,3),
        new Equipment(13601,13602,400,-4000,10,3),
        new Equipment(13625,13626,400,-4000,10,3),
        new Equipment(25084,25086,400,-4000,10,3),
        new Equipment(27447,27449,800,-4000,1,3),
        new Equipment(21064,21066,800,-4000,1,3),
        new Equipment(13581,13582,800,-4000,1,3),
        new Equipment(24746,24748,800,-4000,1,3),
        new Equipment(13629,13630,800,-4000,1,3),
        new Equipment(13669,13670,800,-4000,1,3),
        new Equipment(13605,13606,800,-4000,1,3),
        new Equipment(13593,13594,800,-4000,1,3),
        new Equipment(13617,13618,800,-4000,1,3),
        new Equipment(25072,25074,800,-4000,1,3),
        new Equipment(27456,27458,100,-3000,9,3),
        new Equipment(21073,21075,100,-3000,9,3),
        new Equipment(13587,13588,100,-3000,9,3),
        new Equipment(24755,24757,100,-3000,9,3),
        new Equipment(13635,13636,100,-3000,9,3),
        new Equipment(13675,13676,100,-3000,9,3),
        new Equipment(13611,13612,100,-3000,9,3),
        new Equipment(13599,13600,100,-3000,9,3),
        new Equipment(13623,13624,100,-3000,9,3),
        new Equipment(25081,25083,100,-3000,9,3),
        new Equipment(27444,27446,600,-3000,0,3),
        new Equipment(21061,21063,600,-3000,0,3),
        new Equipment(13579,13580,600,-3000,0,3),
        new Equipment(24743,24745,600,-3000,0,3),
        new Equipment(13627,13628,600,-3000,0,3),
        new Equipment(13667,13668,600,-3000,0,3),
        new Equipment(13603,13604,600,-3000,0,3),
        new Equipment(13591,13592,600,-3000,0,3),
        new Equipment(13615,13616,600,-3000,0,3),
        new Equipment(25069,25071,600,-3000,0,3),
        new Equipment(27453,27455,900,-6000,7,4),
        new Equipment(21070,21072,900,-6000,7,4),
        new Equipment(13585,13586,900,-6000,7,4),
        new Equipment(24752,24754,900,-6000,7,4),
        new Equipment(13633,13634,900,-6000,7,4),
        new Equipment(13673,13674,900,-6000,7,4),
        new Equipment(13609,13610,900,-6000,7,4),
        new Equipment(13597,13598,900,-6000,7,4),
        new Equipment(13621,13622,900,-6000,7,4),
        new Equipment(25078,25080,900,-6000,7,4),
        new Equipment(27450,27452,900,-5000,4,4),
        new Equipment(21067,21069,900,-5000,4,4),
        new Equipment(13583,13584,900,-5000,4,4),
        new Equipment(24749,24751,900,-5000,4,4),
        new Equipment(13631,13632,900,-5000,4,4),
        new Equipment(13671,13672,900,-5000,4,4),
        new Equipment(13607,13608,900,-5000,4,4),
        new Equipment(13595,13596,900,-5000,4,4),
        new Equipment(13619,13620,900,-5000,4,4),
        new Equipment(25075,25077,900,-5000,4,4),
        new Equipment(10069,10073,453,-2267,1,0),
        new Equipment(10071,10074,453,-4535,1,0),
        new Equipment(88,89,340,-4535,10,0),
        new Equipment(10553,10554,340,-4535,9,0),
        new Equipment(13280,13342,453,-4000,1,3),
        new Equipment(9771,13340,453,-4000,1,3),
        new Equipment(9772,13341,453,-4000,1,3),
    };
    private static final Map<Integer,Equipment> BY_ID=new HashMap<>();
    static{for(Equipment e:EQUIPMENT){if(BY_ID.put(e.inventory,e)!=null||BY_ID.put(e.worn,e)!=null)throw new IllegalStateException("Duplicate energy equipment");}}
    private RunEnergy2022(){}
    public static int inventoryId(int id){Equipment e=BY_ID.get(id);return e==null?id:e.inventory;}
    public static int wornId(int id){Equipment e=BY_ID.get(id);return e==null?id:e.worn;}
    public static int itemGrams(int id,boolean worn){
        Equipment e=BY_ID.get(id);if(e!=null)return worn?e.wornGrams:e.inventoryGrams;
        ItemDef def=ItemDef.forId(id);if(def.isNoted()||def.isStackable())return 0;
        Double weight=ItemStats.forId(id).getWeight();return weight==null?0:(int)Math.round(weight*1000);
    }
    public static int weightGrams(Player p){
        long grams=0;for(int i=0;i<p.playerItems.length;i++)if(p.playerItems[i]>0&&p.playerItemsN[i]>0)grams+=(long)itemGrams(p.playerItems[i]-1,false)*p.playerItemsN[i];
        for(int i=0;i<p.playerEquipment.length;i++)if(p.playerEquipment[i]>0&&p.playerEquipmentN[i]>0)grams+=(long)itemGrams(p.playerEquipment[i],true)*p.playerEquipmentN[i];
        return(int)Math.max(Integer.MIN_VALUE,Math.min(Integer.MAX_VALUE,grams));
    }
    public static int gracefulPieceCount(Player p){
        int pieces=0;for(int slot:new int[]{0,1,4,7,9,10}){Equipment e=BY_ID.get(p.playerEquipment[slot]);if(e!=null&&e.slot==slot&&e.recoveryPercent>0&&p.playerEquipmentN[slot]==1)pieces++;}return pieces;
    }
    public static int restorationBonus(Player p){
        int pieces=0,bonus=0;for(int slot:new int[]{0,1,4,7,9,10}){Equipment e=BY_ID.get(p.playerEquipment[slot]);if(e!=null&&e.slot==slot&&e.recoveryPercent>0&&p.playerEquipmentN[slot]==1){pieces++;bonus+=e.recoveryPercent;}}
        return bonus+(pieces==6?10:0);
    }
    public static int drainUnits(int grams,boolean stamina,boolean endurance){int n=67+(int)(67L*Math.max(0,Math.min(64000,grams))/64000);return stamina?n*3/10:endurance?n*85/100:n;}
    public static int restoreUnits(int agility,int bonusPercent){return(8+Math.max(0,Math.min(99,agility))/6)*(100+bonusPercent)/100;}
}
