package io.xeros.content.skills.cooking;

import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;

public final class DairyChurning {
    public static final class Recipe {
        public final int ingredient,product,container,level,ticks;
        public final double xp;
        Recipe(int i,int p,int c,int l,double x,int t){ingredient=i;product=p;container=c;level=l;xp=x;ticks=t;}
    }
    private static final List<Recipe> RECIPES=Collections.unmodifiableList(Arrays.asList(
        new Recipe(1927,2130,1925,21,18,8),new Recipe(1927,6697,1925,38,40.5,16),
        new Recipe(2130,6697,1931,38,22.5,8),new Recipe(1927,1985,1925,48,64,24),
        new Recipe(2130,1985,1931,48,46,16),new Recipe(6697,1985,0,48,23.5,8)));
    private DairyChurning(){}
    public static List<Recipe> recipes(){return RECIPES;}
    public static boolean churn(int id){return id==11695||id==11696||id==25720||id==35937;}
    public static boolean make(Player p,Recipe r,int quantity){
        if(!RECIPES.contains(r)||!p.getUtilityMagic().canInteract())return false;
        int[][] outputs=r.container==0?new int[][]{{r.product,1}}:new int[][]{{r.product,1},{r.container,1}};
        return ProductionBatch.start(p,7,r.level,r.xp,quantity,894,r.ticks,new int[][]{{r.ingredient,1}},outputs,null,()->p.getUtilityMagic().canInteract());
    }
    public static void menu(Player p,int x,int y,int ingredient){
        int h=p.heightLevel;if(!near(p,x,y,h))return;
        List<DialogueOption> options=new ArrayList<>();
        // Choose the starting ingredient explicitly when several kinds are carried.
        for(Recipe r:RECIPES)if((ingredient<0||ingredient==r.ingredient)&&p.getItems().playerHasItem(r.ingredient,1)){
            options.add(new DialogueOption(ItemAssistant.getItemName(r.product)+" from "+ItemAssistant.getItemName(r.ingredient),q->{
                if(!near(q,x,y,h))return;
                new DialogueBuilder(q).option("How many?",new DialogueOption("Make 1",v->{if(near(v,x,y,h))make(v,r,1);}),
                    new DialogueOption("Make 5",v->{if(near(v,x,y,h))make(v,r,5);}),new DialogueOption("Make all",v->{if(near(v,x,y,h))make(v,r,28);})).send();
            }));
        }
        if(options.isEmpty()){p.sendMessage("Bring milk, cream or butter to churn.");return;}
        if(options.size()>5){
            new DialogueBuilder(p).option("Choose an ingredient",new DialogueOption("Milk",q->{if(near(q,x,y,h))menu(q,x,y,1927);}),
                new DialogueOption("Cream",q->{if(near(q,x,y,h))menu(q,x,y,2130);}),new DialogueOption("Butter",q->{if(near(q,x,y,h))menu(q,x,y,6697);})).send();
        }else new DialogueBuilder(p).option("Dairy churning",options.toArray(new DialogueOption[0])).send();
    }
    private static boolean near(Player p,int x,int y,int h){return p.heightLevel==h&&Math.abs(p.absX-x)<=3&&Math.abs(p.absY-y)<=3&&p.getUtilityMagic().canInteract();}
}
