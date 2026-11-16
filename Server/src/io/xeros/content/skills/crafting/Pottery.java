package io.xeros.content.skills.crafting;

import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import java.util.*;
import java.util.function.DoubleSupplier;

/** The five pottery products available in the 2022 cache. */
public final class Pottery {
    public static final class Recipe {
        public final String name;
        public final int unfired, product, level;
        public final double shapingXp, firingXp;
        Recipe(String n,int u,int p,int l,double s,double f){name=n;unfired=u;product=p;level=l;shapingXp=s;firingXp=f;}
    }
    private static final List<Recipe> RECIPES=Collections.unmodifiableList(Arrays.asList(
        new Recipe("Pot",1787,1931,1,6.3,6.3),
        new Recipe("Pie dish",1789,2313,7,15,10),
        new Recipe("Bowl",1791,1923,8,18,15),
        new Recipe("Plant pot",5352,5350,19,20,17.5),
        new Recipe("Pot lid",4438,4440,25,20,20)));
    private Pottery(){}
    public static List<Recipe> recipes(){return RECIPES;}
    public static boolean wheel(int id){return id==4310||id==14887||id==20375||id==39395||id==40733;}
    public static boolean oven(int id){return id==4308||id==11601||id==39396;}
    public static double firingSuccess(int level){
        // Wiki crowdsourced estimate (180,800); not an officially confirmed formula.
        return Math.min(1.0,(Math.floor(180+(800-180)*(Math.max(1,Math.min(99,level))-1)/98.0)+1)/256.0);
    }
    public static boolean make(Player p,Recipe r,boolean fire,int quantity){return make(p,r,fire,quantity,Math::random);}
    public static boolean make(Player p,Recipe r,boolean fire,int quantity,DoubleSupplier roll){
        if(!RECIPES.contains(r))return false;
        if(!fire)return ProductionBatch.start(p,12,r.level,r.shapingXp,quantity,883,3,new int[][]{{1761,1}},new int[][]{{r.unfired,1}},null);
        // Removing an unstackable first reserves exactly the slot required by its fired product.
        return ProductionBatch.start(p,12,1,0,quantity,899,7,new int[][]{{r.unfired,1}},new int[0][],()->{
            if(roll.getAsDouble()<firingSuccess(p.playerLevel[12])){
                p.getItems().addItem(r.product,1);p.getPA().addSkillXPMultiplied(r.firingXp,12,true);
            }else p.sendMessage("The pottery cracks in the oven.");
        });
    }
    public static boolean soften(Player p,int a,int b){
        int water=a==434?b:b==434?a:-1;
        int empty=water==1929?1925:water==1937?1935:water==1921?1923:0;
        if(empty==0)return false;
        ProductionBatch.start(p,12,1,0,1,883,2,new int[][]{{434,1},{water,1}},new int[][]{{1761,1},{empty,1}},null);return true;
    }
    public static void menu(Player p,boolean fire,int x,int y,int item){
        int h=p.heightLevel;
        if(!near(p,x,y,h))return;
        List<DialogueOption> options=new ArrayList<>();
        for(Recipe r:RECIPES)if(item<0||item==(fire?r.unfired:1761))options.add(new DialogueOption(r.name,q->{
            if(!near(q,x,y,h))return;
            new DialogueBuilder(q).option("How many?",new DialogueOption("Make 1",v->{if(near(v,x,y,h))make(v,r,fire,1);}),
                new DialogueOption("Make 5",v->{if(near(v,x,y,h))make(v,r,fire,5);}),new DialogueOption("Make all",v->{if(near(v,x,y,h))make(v,r,fire,28);})).send();
        }));
        if(!options.isEmpty())new DialogueBuilder(p).option(fire?"Fire pottery":"Shape soft clay",options.toArray(new DialogueOption[0])).send();
    }
    private static boolean near(Player p,int x,int y,int h){return p.heightLevel==h&&Math.abs(p.absX-x)<=3&&Math.abs(p.absY-y)<=3&&!p.isDead&&!p.getLock().cannotInteract(p);}
}
