package io.xeros.content.skills.construction;

/** Native kitchen item and scenery identifiers, with the 2022 shelf contents. */
public final class HouseKitchenData {
    private HouseKitchenData() {}
    public static final int KETTLE=7688,FULL_KETTLE=7690,HOT_KETTLE=7691,LEAVES=7738,GLASS=7742;
    public static final int[] CUPS={7728,7732,7735};
    public static final int[] EMPTY_POTS={7702,7714,7726},LEAF_POTS={7700,7712,7724},FULL_POTS={7692,7704,7716};
    public static final int[] TEAS={7730,7733,7736},MILKY_TEAS={7731,7734,7737};
    public static final int[] STOVES={13529,13531,13533,13536,13539,13542};
    public static final int[] HEATING_STOVES={13530,13532,13534,13537,13541,13544};
    public static final int[] HOT_STOVES={13530,13532,13535,13538,13540,13543};
    public static boolean temporary(int id){return id>=7688&&id<=7755;}
    public static int index(int[] items,int id){for(int i=0;i<items.length;i++)if(items[i]==id)return i;return -1;}
    public static int potFamily(int id){for(int i=0;i<FULL_POTS.length;i++)if(id>=FULL_POTS[i]&&id<=FULL_POTS[i]+6&&(id-FULL_POTS[i])%2==0)return i;return -1;}
    public static int[] larder(String key){
        switch(key){
            case "WOODEN_LARDER":return new int[]{LEAVES,1927};
            case "OAK_LARDER":return new int[]{LEAVES,1927,1944,1933};
            case "TEAK_LARDER":return new int[]{LEAVES,1927,1944,1933,1942,1550,1957,1985};
            default:return new int[0];
        }
    }
    public static int[] shelves(String key){
        switch(key){
            case "WOODEN_SHELVES_1":return new int[]{KETTLE,7702,7728};
            case "WOODEN_SHELVES_2":return new int[]{KETTLE,7702,7728,GLASS};
            case "WOODEN_SHELVES_3":return new int[]{KETTLE,7714,7732,GLASS,1887};
            case "OAK_SHELVES_1":return new int[]{KETTLE,7702,7728,GLASS,1887,1923};
            case "OAK_SHELVES_2":return new int[]{KETTLE,7714,7732,GLASS,1887,1923,2313};
            case "TEAK_SHELVES_1":return new int[]{KETTLE,7714,7732,GLASS,1887,1923,2313,1931};
            case "TEAK_SHELVES_2":return new int[]{KETTLE,7726,7735,GLASS,1887,1923,2313,1931,1949};
            default:return new int[0];
        }
    }
    public static int barrel(String key){
        switch(key){
            case "BEER_BARREL":return 7740;
            case "CIDER_BARREL":return 7752;
            case "ASGARNIAN_ALE_BARREL":return 7744;
            case "GREENMANS_ALE_BARREL":return 7746;
            case "DRAGON_BITTER_BARREL":return 7748;
            case "CHEFS_DELIGHT_BARREL":return 7754;
            default:return -1;
        }
    }
}
