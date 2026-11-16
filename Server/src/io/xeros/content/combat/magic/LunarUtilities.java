package io.xeros.content.combat.magic;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.crafting.CraftingData;
import io.xeros.content.skills.cooking.CookingRecipes;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Inventory utilities using the same atomic rune payment and action cancellation as item spells. */
public final class LunarUtilities {
    private LunarUtilities(){}
    private static final int[][] PLANKS={{1511,960,70},{1521,8778,175},{6333,8780,350},{6332,8782,1050}};
    private static boolean ready(Player p,int level){
        if(p.playerMagicBook!=2||!p.getUtilityMagic().canInteract()||!p.getUtilityMagic().ready())return false;
        if(p.playerLevel[6]<level){p.sendMessage("Você precisa de nível "+level+" em Magia e do livro Lunar.");return false;}
        return true;
    }
    public static boolean button(Player p,int widget){
        switch(widget){case 30091:cureMe(p);return true;case 30056:humidify(p);return true;case 30186:start(p,1,-1);return true;
            case 30017:start(p,2,-1);return true;case 30242:plankMenu(p);return true;default:return false;}
    }
    private static void committed(Player p,double magicXp,int ticks){p.getPA().addSkillXPMultiplied(magicXp,6,true);p.getUtilityMagic().committedCast(ticks);p.startAnimation(4413);}
    public static boolean humidify(Player p){
        if(!ready(p,68))return false;
        List<int[]> in=new ArrayList<>(),out=new ArrayList<>();
        int[][] pairs={{1925,1929},{1935,1937},{1923,1921},{229,227},{434,1761},{1831,1823},{1829,1823},{1827,1823},{1825,1823}};
        for(int[] pair:pairs){int n=p.getItems().getInventoryCount(pair[0]);if(n>0){in.add(new int[]{pair[0],n});out.add(new int[]{pair[1],n});}}
        for(int can=5331;can<5340;can++){int n=p.getItems().getInventoryCount(can);if(n>0){in.add(new int[]{can,n});out.add(new int[]{5340,n});}}
        if(in.isEmpty()){p.sendMessage("Você não tem recipientes vazios ou clay para Humidify.");return false;}
        if(!RunePayment.exchangeWithInventoryInputs(p,in.toArray(new int[0][]),new int[][]{{9075,1},{555,3},{554,1}},out.toArray(new int[0][])))return false;
        p.interruptActions();p.getPA().stopSkilling();committed(p,65,3);return true;
    }
    /** No immunity: venom requires two casts, first reducing it to ordinary poison. */
    public static boolean cureMe(Player p){
        if(!ready(p,71))return false;
        if(p.getHealth().getStatus()==io.xeros.model.entity.HealthStatus.NORMAL){p.sendMessage("Você não está envenenado.");return false;}
        if(!RunePayment.consume(p,new int[][]{{9075,2},{564,2},{563,1}})){p.sendMessage("Você não tem as runas necessárias para Cure Me.");return false;}
        p.getHealth().antipoison(0);committed(p,69,3);p.gfx100(736);
        p.sendMessage(p.getHealth().getStatus()==io.xeros.model.entity.HealthStatus.POISON?"Cure Me reduziu venom a veneno. Lance novamente para curar.":"Cure Me curou seu veneno.");return true;
    }
    private static void plankMenu(Player p){
        if(!ready(p,86))return;List<DialogueOption> options=new ArrayList<>();
        for(int[] row:PLANKS)options.add(new DialogueOption(io.xeros.model.definitions.ItemDef.forId(row[0]).getName()+" ("+row[2]+" moedas)",q->{q.getPA().removeAllWindows();start(q,0,row[0]);}));
        options.add(new DialogueOption("Cancelar",q->q.getPA().removeAllWindows()));
        new DialogueBuilder(p).option("Plank Make",options.toArray(new DialogueOption[0])).send();
    }
    public static boolean once(Player p,int kind,int log){
        int level=kind==0?86:kind==1?80:65;if(!ready(p,level))return false;
        int[][] in=null,out=null,cost=null;double xp=0,craftXp=0,cookXp=0;
        if(kind==0){for(int[] row:PLANKS)if(row[0]==log){in=new int[][]{{row[0],1},{995,row[2]}};out=new int[][]{{row[1],1}};cost=new int[][]{{9075,2},{561,1},{557,15}};xp=90;break;}}
        else if(kind==1){for(CraftingData.amuletData recipe:CraftingData.amuletData.values())if(p.getItems().getInventoryCount(recipe.getAmuletId())>0){in=new int[][]{{recipe.getAmuletId(),1}};out=new int[][]{{recipe.getProduct(),1}};cost=new int[][]{{9075,2},{557,10},{555,5}};xp=83;craftXp=4;break;}}
        else if(kind==2){for(CookingRecipes.Bake recipe:CookingRecipes.bakes())if(recipe.burnt==2329&&p.getItems().getInventoryCount(recipe.raw)>0&&p.playerLevel[7]>=recipe.level){in=new int[][]{{recipe.raw,1}};out=new int[][]{{recipe.cooked,1}};cost=new int[][]{{9075,1},{554,5},{555,4}};xp=60;cookXp=recipe.xp;break;}}
        if(in==null||!RunePayment.exchangeWithInventoryInputs(p,in,cost,out))return false;
        committed(p,xp,3);if(craftXp>0)p.getPA().addSkillXPMultiplied(craftXp,12,true);if(cookXp>0)p.getPA().addSkillXPMultiplied(cookXp,7,true);return true;
    }
    public static boolean start(Player p,int kind,int log){
        int level=kind==0?86:kind==1?80:65;if(!ready(p,level))return false;
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.attacking.reset();
        if(!once(p,kind,log)){p.sendMessage("Confira o nível, as runas, os materiais e as moedas para esta magia.");return false;}
        final long token=p.productionActionToken;final int x=p.absX,y=p.absY,h=p.heightLevel;
        Server.getEventHandler().submit(new Event<Player>("lunar-utility",p,kind==0?6:3){
            public void execute(){if(token!=p.productionActionToken||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()||!once(p,kind,log))stop();}
        });return true;
    }
}
