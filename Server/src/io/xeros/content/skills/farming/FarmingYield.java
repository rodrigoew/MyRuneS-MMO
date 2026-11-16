package io.xeros.content.skills.farming;

import io.xeros.model.Items;
import io.xeros.model.entity.player.Player;

/** The pre-2023 harvest-life model; secateurs must be equipped in this revision. */
public final class FarmingYield {
    private FarmingYield() {}

    public static int saveThreshold(Plants crop, int level, int bonusPercent) {
        int low;
        switch (crop) {
            case GUAM: low=25; break; case MARENTILL: low=28; break;
            case TARROMIN: low=31; break; case HARRALANDER: low=36; break;
            case RANARR: low=39; break; case TOADFLAX: low=43; break;
            case IRIT: low=46; break; case AVANTOE: low=50; break;
            case KWUARM: low=54; break; case SNAPDRAGON: low=57; break;
            case CADANTINE: low=60; break; case LANTADYME: low=64; break;
            case DWARF_WEED: low=67; break; case TORSTOL: low=71; break;
            case POTATO: low=101; break; case ONION: low=105; break;
            case CABBAGE: low=107; break; case TOMATO: low=112; break;
            case SWEETCORN: low=88; break; case STRAWBERRY: low=103; break;
            case WATERMELON: low=126; break;
            case SNAPE_GRASS: low=148; break;
            default: return 0;
        }
        int high=crop.type==SeedType.HERB?80:crop==Plants.SNAPE_GRASS?195:180;
        int value=low+(high-low)*(Math.max(1,Math.min(99,level))-1)/98;
        return Math.min(255, value*(100+Math.max(0,bonusPercent))/100+1);
    }

    public static int bonus(Player p, Plants crop) {
        if(crop.type!=SeedType.HERB && crop.type!=SeedType.ALLOTMENT)return 0;
        int bonus=p.getItems().isWearingItem(Items.MAGIC_SECATEURS)?10:0;
        if(crop.type==SeedType.HERB && (io.xeros.content.SkillcapePerks.FARMING.isWearing(p)
                ||io.xeros.content.SkillcapePerks.isWearingMaxCape(p)))bonus+=5;
        return bonus;
    }

    public static int flowerAmount(Plants crop, int level, int roll) {
        if(crop==Plants.WOAD)return 3;
        if(crop==Plants.LIMPWURT)return 3+Math.floorMod(roll,Math.max(1,level))/10;
        return 1;
    }
}
