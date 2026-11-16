package io.xeros.content.skills.fletching;

import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;

/** Material-complete recipes; menus and execution share the same table. */
public final class FletchingRecipes {
    public static final class Recipe {
        public final int first,second,tool,product,quantity,level,ticks;
        public final double xp;
        Recipe(int a,int b,int t,int p,int n,int l,double x,int delay){first=a;second=b;tool=t;product=p;quantity=n;level=l;xp=x;ticks=delay;}
    }
    private static final List<Recipe> RECIPES=new ArrayList<>();
    static {
        for(FletchableLog r:FletchableLog.values()){
            int qty=r.getProduct()==52?shaftQuantity(r.getItemId()):1;
            add(r.getItemId(),0,946,r.getProduct(),qty,r.getLevel(),r.getExperience(),3);
        }
        add(1511,0,946,19584,15,3,5,3);
        int[][] stocks={{1511,9440,9,6},{1521,9442,24,16},{1519,9444,39,22},{6333,9446,46,27},
            {1517,9448,54,32},{6332,9450,61,41},{1515,9452,69,50},{1513,21952,78,70}};
        int[] limbs={9420,9422,9423,9425,9427,9429,9431,21918};
        int[] unstrung={9454,9456,9457,9459,9461,9463,9465,21921};
        double[] assembly={12,32,44,54,64,82,100,135};
        for(int i=0;i<stocks.length;i++){
            int[] r=stocks[i];add(r[0],0,946,r[1],1,r[2],r[3],3);
            add(r[1],limbs[i],2347,unstrung[i],1,r[2],assembly[i],2);
        }
        int[] logs={1521,1519,1517,1515,1513,19669},levels={27,42,57,72,87,92};
        double[] xp={50,83,116.5,150,183,216};
        for(int i=0;i<logs.length;i++)add(logs[i],logs[i],946,22251+3*i,1,levels[i],xp[i],7);
        for(FletchableGem r:FletchableGem.values())add(r.getGem(),0,1755,r.getTips(),r.getAmount(),r.getLevel(),r.getExperience(),5);
        add(411,0,1755,46,6,41,3.2,5);add(413,0,1755,46,24,41,3.2,5);
        add(22935,0,946,1391,1,40,80,3);
        add(12922,0,1755,12924,1,78,120,3);
        // Kebbit XP is awarded per spike, which produces six bolts.
        add(10105,0,1755,10158,6,32,5.8,2);
        add(10107,0,1755,10159,6,42,7.9,2);
        add(9416,9142,0,9418,1,59,11,2);
        add(9418,954,0,9419,1,59,0,1);
        // Ballista assembly, released with Monkey Madness II (2016).
        add(19586,19592,0,19595,1,47,15,3);add(19595,19601,0,19604,1,47,15,3);add(19604,19610,0,19478,1,47,300,3);
        add(19589,19592,0,19598,1,72,30,3);add(19598,19601,0,19607,1,72,30,3);add(19607,19610,0,19481,1,72,600,3);
    }
    private FletchingRecipes(){}
    private static void add(int a,int b,int tool,int p,int qty,int level,double xp,int ticks){RECIPES.add(new Recipe(a,b,tool,p,qty,level,xp,ticks));}
    public static List<Recipe> recipes(){return Collections.unmodifiableList(RECIPES);}
    public static int shaftQuantity(int log){switch(log){case 1521:return 30;case 1519:return 45;case 1517:return 60;case 1515:return 75;case 1513:return 90;case 19669:return 105;default:return 15;}}
    public static boolean make(Player p,Recipe r,int quantity){
        if(!RECIPES.contains(r)||!p.getUtilityMagic().canInteract())return false;
        List<int[]> inputs=new ArrayList<>();inputs.add(new int[]{r.first,1});if(r.second>0)inputs.add(new int[]{r.second,1});if(r.tool>0)inputs.add(new int[]{r.tool,0});
        return ProductionBatch.start(p,9,r.level,r.xp,quantity,r.tool==1755?886:1248,r.ticks,inputs.toArray(new int[0][]),new int[][]{{r.product,r.quantity}},()->io.xeros.content.achievement_diary.DiaryProduction.fletch(p,r.product),()->p.getUtilityMagic().canInteract());
    }
    public static boolean itemOnItem(Player p,int a,int b){
        List<Recipe> matches=new ArrayList<>();
        for(Recipe r:RECIPES){
            if(r.second==0){if((a==r.tool&&b==r.first)||(b==r.tool&&a==r.first))matches.add(r);}
            else if((a==r.first&&b==r.second)||(b==r.first&&a==r.second))matches.add(r);
        }
        if(matches.isEmpty())return false;
        page(p,matches,0);return true;
    }
    private static void page(Player p,List<Recipe> list,int offset){
        List<DialogueOption> options=new ArrayList<>();
        for(int i=offset;i<Math.min(offset+4,list.size());i++){Recipe r=list.get(i);options.add(new DialogueOption(ItemAssistant.getItemName(r.product)+" (level "+r.level+")",q->quantity(q,r)));}
        if(offset+4<list.size())options.add(new DialogueOption("More...",q->page(q,list,offset+4)));
        new DialogueBuilder(p).option("Fletching",options.toArray(new DialogueOption[0])).send();
    }
    private static void quantity(Player p,Recipe r){
        new DialogueBuilder(p).option("How many?",new DialogueOption("Make 1",q->make(q,r,1)),new DialogueOption("Make 5",q->make(q,r,5)),new DialogueOption("Make all",q->make(q,r,28))).send();
    }
}
