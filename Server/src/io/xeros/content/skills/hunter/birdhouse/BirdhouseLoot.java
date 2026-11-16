package io.xeros.content.skills.hunter.birdhouse;
import io.xeros.Server;
import io.xeros.model.entity.player.Player;
import io.xeros.model.definitions.ItemDef;
import java.util.function.DoubleSupplier;

/** 2018/2021 nest rolls; tier rates documented by Mod Ash, linked in the release guide. */
public final class BirdhouseLoot {
    private BirdhouseLoot(){}
    private static int roll(DoubleSupplier random,int bound){return Math.min(bound-1,(int)(random.getAsDouble()*bound));}
    public static void give(Player p,int item,int quantity){
        if(quantity<=0)return;
        if(!io.xeros.content.skills.ProductionBatch.exchange(p,new int[0][],new int[][]{{item,quantity}}))
            Server.itemHandler.createGroundItem(p,item,p.absX,p.absY,p.heightLevel,quantity,p.getIndex());
    }
    static void reward(Player p,BirdhouseType type){reward(p,type,Math::random);}
    static void reward(Player p,BirdhouseType type,DoubleSupplier random){
        int level=p.playerLevel[21];
        Server.itemHandler.createGroundItem(p,9978,p.absX,p.absY,p.heightLevel,10,p.getIndex());
        give(p,314,30+10*roll(random,4));
        double seedChance=Math.min(1,Math.max(0,Math.round(1+200d*(level-1)/98)/256d));
        if(random.getAsDouble()<seedChance)give(p,5073,1);
        double chance=type.nestChanceAt99*Math.max(49,level-1)/98d;
        for(int i=0;i<10;i++)if(random.getAsDouble()<chance){
            // The base currently supports easy, medium and hard Treasure Trails.
            int clue=roll(random,1500),nest=0,scroll=0;
            if(clue<2){nest=19716;scroll=2722;}else if(clue<5){nest=19714;scroll=2801;}else if(clue<9){nest=19712;scroll=2677;}
            if(nest!=0&&!p.getItems().playerHasItem(nest)&&!p.getItems().bankContains(nest)&&!p.getItems().playerHasItem(scroll)&&!p.getItems().bankContains(scroll)){give(p,nest,1);continue;}
            int value=roll(random,p.getItems().isWearingItem(10132)?95:100);
            give(p,value<3?5070+value:value<35?5074:5075,1);
        }
    }
    // Sample-derived seed weights, not a certified official distribution.
    private static final int[][] SEEDS={{5312,219},{5283,176},{5313,117},{5284,104},{5286,81},{5285,80},{5314,66},{5287,36},{5315,30},{5289,24},{5288,22},{5317,14},{5290,13},{5316,9},{21486,8},{21488,3}};
    private static final int[][] RINGS={{1635,40},{1637,35},{1639,15},{1641,8},{1643,2}};
    private static int pick(int[][] table,DoubleSupplier random){int total=0;for(int[] row:table)total+=row[1];int value=roll(random,total);for(int[] row:table){value-=row[1];if(value<0)return row[0];}throw new AssertionError();}
    public static boolean openNest(Player p,int item){return openNest(p,item,Math::random);}
    static boolean openNest(Player p,int item,DoubleSupplier random){
        int reward;
        if(item==5073)reward=pick(SEEDS,random);
        else if(item==5074)reward=pick(RINGS,random);
        else if(item>=5070&&item<=5072)reward=5076+item-5070;
        else if(item==19712)reward=2677;else if(item==19714)reward=2801;else if(item==19716)reward=2722;else return false;
        if(!p.getItems().playerHasItem(item))return true;
        if(p.getItems().freeSlots()<1){p.sendMessage("You need one free inventory space to search the nest.");return true;}
        if(!p.getUtilityMagic().canInteract())return true;
        p.getItems().deleteItem2(item,1);p.getItems().addItem(5075,1);give(p,reward,1);return true;
    }
}
