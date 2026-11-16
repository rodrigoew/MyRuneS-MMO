package io.xeros.content.skills.farming;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.bank.*;
import java.util.*;
import java.util.function.LongSupplier;

/** Seed pots mature on five-minute boundaries, including watered seedlings in the bank. */
public final class SaplingGrowth {
    public static final class Recipe {
        public final int seed,dry,watered;
        public final Plants crop;
        private Recipe(int seed,int dry,int watered,Plants crop){this.seed=seed;this.dry=dry;this.watered=watered;this.crop=crop;}
    }
    private static final List<Recipe> RECIPES=List.of(
        new Recipe(5312,5358,5364,Plants.OAK_TREE),new Recipe(5313,5359,5365,Plants.WILLOW_TREE),
        new Recipe(5314,5360,5366,Plants.MAPLE_TREE),new Recipe(5315,5361,5367,Plants.YEW_TREE),
        new Recipe(5316,5362,5368,Plants.MAGIC_TREE),new Recipe(5283,5480,5488,Plants.APPLE_TREE),
        new Recipe(5284,5481,5489,Plants.BANANA_TREE),new Recipe(5285,5482,5490,Plants.ORANGE_TREE),
        new Recipe(5286,5483,5491,Plants.CURRY_TREE),new Recipe(5287,5484,5492,Plants.PINEAPPLE_TREE),
        new Recipe(5288,5485,5493,Plants.PAPAYA_TREE),new Recipe(5289,5486,5494,Plants.PALM_TREE));
    private final Player player;
    private final LongSupplier clock;
    private final long[] deadlines=new long[RECIPES.size()];
    private boolean discover=true,blockedMessage;
    public SaplingGrowth(Player player){this(player,System::currentTimeMillis);}
    public SaplingGrowth(Player player,LongSupplier clock){this.player=player;this.clock=clock;}
    public static List<Recipe> recipes(){return RECIPES;}
    public long deadline(Recipe recipe){return deadlines[RECIPES.indexOf(recipe)];}
    public static long nextBoundary(long now){
        if(now<0)throw new IllegalArgumentException("Negative seedling clock");
        long cycle=now/300_000L;
        return cycle>=Long.MAX_VALUE/300_000L?Long.MAX_VALUE:(cycle+1)*300_000L;
    }
    private static boolean can(int item){return item>=5333&&item<=5340||item==25025;}
    public boolean itemOnItem(int first,int second){
        for(Recipe recipe:RECIPES){
            if(first==recipe.seed&&second==5354||second==recipe.seed&&first==5354){
                new DialogueBuilder(player).option("Plant seeds in pots",
                    new DialogueOption("Make 1",p->make(recipe,1)),new DialogueOption("Make 5",p->make(recipe,5)),
                    new DialogueOption("Make all",p->make(recipe,28))).send();return true;
            }
            if(first==recipe.dry&&(can(second)||second==5331)||second==recipe.dry&&(can(first)||first==5331)){
                int wateringCan=first==recipe.dry?second:first;
                if(wateringCan==5331){player.sendMessage("Your watering can is empty.");return true;}
                water(recipe,wateringCan,28);return true;
            }
        }
        return false;
    }
    public boolean make(Recipe recipe,int quantity){
        if(!player.getUtilityMagic().canInteract())return false;
        if(!player.getItems().playerHasItem(5325)){player.sendMessage("You need a gardening trowel to plant seeds in pots.");return false;}
        return ProductionBatch.start(player,19,recipe.crop.level,0,quantity,2291,2,
                new int[][]{{recipe.seed,1},{5354,1},{5325,0}},new int[][]{{recipe.dry,1}},null,()->player.getUtilityMagic().canInteract());
    }
    private int availableCan(int preferred){
        if(can(preferred)&&player.getItems().playerHasItem(preferred))return preferred;
        for(int id=5340;id>=5333;id--)if(player.getItems().playerHasItem(id))return id;
        return player.getItems().playerHasItem(25025)?25025:-1;
    }
    public boolean water(Recipe recipe,int wateringCan,int quantity){
        if(!can(wateringCan)||quantity<=0||!player.getUtilityMagic().canInteract()
                ||!player.getItems().playerHasItem(wateringCan)||!player.getItems().playerHasItem(recipe.dry))return false;
        process();int index=RECIPES.indexOf(recipe);
        if(deadlines[index]>0&&clock.getAsLong()>=deadlines[index]){
            player.sendMessage("Make room in the bank before watering more of this seedling.");return false;
        }
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();
        final long token=player.productionActionToken;final int x=player.absX,y=player.absY,h=player.heightLevel;
        final int[] remaining={Math.min(quantity,10000)};player.playerSkilling[19]=true;player.startAnimation(2293);
        Server.getEventHandler().submit(new Event<Player>("skilling",player,3){
            public void execute(){
                if(token!=player.productionActionToken||!player.playerSkilling[19]||player.isDisconnected()
                        ||player.getSession()==null||!player.getUtilityMagic().canInteract()
                        ||player.absX!=x||player.absY!=y||player.heightLevel!=h){stop();return;}
                process();if(deadlines[index]>0&&clock.getAsLong()>=deadlines[index]){stop();return;}
                int source=availableCan(wateringCan);if(source<0){stop();return;}
                int[][] inputs=source==25025?new int[][]{{recipe.dry,1},{source,0}}:new int[][]{{recipe.dry,1},{source,1}};
                int[][] outputs=source==25025?new int[][]{{recipe.watered,1}}:
                        new int[][]{{recipe.watered,1},{source==5333?5331:source-1,1}};
                if(!ProductionBatch.exchange(player,inputs,outputs)){stop();return;}
                deadlines[index]=nextBoundary(clock.getAsLong());blockedMessage=false;
                player.startAnimation(2293);if(--remaining[0]==0)stop();
            }
            public void stop(){super.stop();if(token==player.productionActionToken){player.playerSkilling[19]=false;player.startAnimation(65535);}}
        });
        return true;
    }
    private boolean hasWatered(Recipe recipe){
        if(player.getItems().getInventoryCount(recipe.watered)>0)return true;
        for(BankTab tab:player.getBank().getBankTab())for(BankItem item:tab.getItems())
            if(item.getId()==recipe.watered+1&&item.getAmount()>0)return true;
        return false;
    }
    public boolean process(){
        long now=clock.getAsLong();
        if(discover){for(int i=0;i<RECIPES.size();i++)if(deadlines[i]==0&&hasWatered(RECIPES.get(i)))deadlines[i]=nextBoundary(now);discover=false;}
        boolean changed=false,blocked=false;
        for(int i=0;i<RECIPES.size();i++){
            if(deadlines[i]==0||now<deadlines[i])continue;
            Recipe recipe=RECIPES.get(i);List<Integer> inventory=new ArrayList<>();boolean malformed=false;
            for(int slot=0;slot<player.playerItems.length;slot++)if(player.playerItems[slot]==recipe.watered+1){
                if(player.playerItemsN[slot]!=1){malformed=true;break;}inventory.add(slot);
            }
            if(malformed){blocked=true;continue;}
            long count=0;for(BankTab tab:player.getBank().getBankTab())for(BankItem item:tab.getItems())
                if(item.getId()==recipe.watered+1)count+=Math.max(0,item.getAmount());
            if(inventory.isEmpty()&&count==0){deadlines[i]=0;continue;}
            Map<Integer,Long> inputs=count==0?Collections.emptyMap():Map.of(recipe.watered,count);
            Map<Integer,Long> outputs=count==0?Collections.emptyMap():Map.of(recipe.crop.seed,count);
            BankTransformation plan=BankTransformation.prepare(player.getBank(),inputs,outputs,false);
            if(plan==null){blocked=true;continue;}
            for(int slot:inventory)player.playerItems[slot]=recipe.crop.seed+1;
            plan.commit();deadlines[i]=0;changed=true;
        }
        if(changed){player.getItems().resetItems(3214);if(player.isBanking){player.getItems().resetTempItems();player.getItems().queueBankContainerUpdate();}player.sendMessage("Your watered seedlings have grown into saplings.");}
        if(blocked&&!blockedMessage)player.sendMessage("A bank stack cannot hold the saplings. Make room to finish growing them.");
        blockedMessage=blocked;return changed;
    }
    public String encode(){
        StringJoiner state=new StringJoiner(",");for(int i=0;i<deadlines.length;i++)if(deadlines[i]>0)state.add(RECIPES.get(i).crop.seed+"="+deadlines[i]);return state.toString();
    }
    public boolean decode(String state){
        try{
            long[] next=new long[deadlines.length];
            if(!state.isEmpty())for(String part:state.split(",",-1)){
                String[] fields=part.split("=",-1);if(fields.length!=2)return false;
                int product=Integer.parseInt(fields[0]);long deadline=Long.parseLong(fields[1]);if(deadline<=0)return false;
                int index=-1;for(int i=0;i<RECIPES.size();i++)if(RECIPES.get(i).crop.seed==product)index=i;
                if(index<0||next[index]!=0)return false;next[index]=deadline;
            }
            System.arraycopy(next,0,deadlines,0,next.length);discover=true;blockedMessage=false;return true;
        }catch(RuntimeException malformed){return false;}
    }
}
