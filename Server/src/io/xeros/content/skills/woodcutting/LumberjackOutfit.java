package io.xeros.content.skills.woodcutting;
import java.util.function.IntPredicate;
final class LumberjackOutfit {
    private static final int[] ITEMS = {10933, 10939, 10940, 10941};
    private static final int[] TENTHS_OF_PERCENT = {2, 8, 6, 4};
    static double multiplier(IntPredicate worn) {
        int bonus=0,count=0;
        for(int i=0;i<ITEMS.length;i++) if(worn.test(ITEMS[i])) {bonus+=TENTHS_OF_PERCENT[i];count++;}
        if(count==ITEMS.length) bonus+=5;
        return 1d+bonus/1000d;
    }
}
