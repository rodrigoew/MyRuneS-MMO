package io.xeros.content.skills.construction;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.*;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;
import static io.xeros.content.skills.construction.HouseKitchenData.*;

/** Interactive kitchen supplies and tea; permanent furniture remains in the house save. */
public final class HouseKitchen {
    // A ten-second boil is a comparison value from the earlier public implementation.
    // Its exact 2022 server timing still needs an independent reference.
    public static final int BOIL_TICKS=17;
    private static final class KettleState {
        final String recipe;final int stove;boolean hot;
        KettleState(String recipe,int stove){this.recipe=recipe;this.stove=stove;}
    }
    private final PlayerHouse house;
    private final Player p;
    private final Map<PlayerHouse.Room,KettleState> kettles=new IdentityHashMap<>();
    public HouseKitchen(PlayerHouse house,Player p){this.house=house;this.p=p;}
    private boolean active(){return house.kitchenOwnerReady()&&!p.isDisconnected()&&p.getSession()!=null;}
    public void clear(){kettles.clear();Server.getEventHandler().stop(p,"house-kettle");}
    public void removeTemporaryInventory(){
        boolean changed=false;
        for(int i=0;i<p.playerItems.length;i++)if(temporary(p.playerItems[i]-1)){p.playerItems[i]=0;p.playerItemsN[i]=0;changed=true;}
        if(changed)p.getItems().sendInventoryInterface(3214);
    }
    void furnitureRemoved(PlayerHouse.Room room,int slot){if(slot==15398)kettles.remove(room);}
    void removeRoom(PlayerHouse.Room room){kettles.remove(room);}
    int model(PlayerHouse.Binding b,HouseFurnishing f){
        KettleState state=kettles.get(b.room);
        if(HouseFurnishing.slot(b.root.id)==15398&&state!=null&&state.recipe.equals(f.key))
            return state.hot?HOT_STOVES[state.stove]:HEATING_STOVES[state.stove];
        return f.model(b.root.id);
    }
    public boolean click(WorldObject o,int option){
        PlayerHouse.Binding b=house.kitchenFixture(o);if(b==null)return false;
        HouseFurnishing f=house.furnishing(b);
        if(option==1&&(f.group==15399||f.group==15403)){
            supplies(b,f.group==15399?shelves(f.key):larder(f.key),0);return true;
        }
        if(f.group==15398){
            KettleState state=kettles.get(b.room);
            if(option==1&&state!=null){
                if(!state.hot){p.sendMessage("The kettle has not boiled yet.");return true;}
                if(ProductionBatch.exchange(p,new int[0][],new int[][]{{HOT_KETTLE,1}})){
                    kettles.remove(b.room);house.renderKitchen(b.room);p.startAnimation(832);
                }return true;
            }
            if(option==2&&f.key.equals("FANCY_RANGE")){
                for(var item:p.getItems().getInventoryItems())if(Cooking.cookThisFood(p,item.getId(),o.id))return true;
                p.sendMessage("You have no food to cook.");return true;
            }
        }
        return false;
    }
    private void supplies(PlayerHouse.Binding b,int[] items,int page){
        if(!house.kitchenReady(b)||items.length==0)return;
        String recipe=house.furnishing(b).key;
        List<DialogueOption> options=new ArrayList<>();
        for(int i=page*3;i<Math.min(page*3+3,items.length);i++){
            int id=items[i];
            options.add(new DialogueOption(ItemAssistant.getItemName(id),q->{
                q.getPA().removeAllWindows();
                if(house.kitchenReady(b)&&house.furnishing(b).key.equals(recipe)){
                    if(ProductionBatch.exchange(q,new int[0][],new int[][]{{id,1}}))q.sendMessage("You take the "+ItemAssistant.getItemName(id).toLowerCase()+".");
                }
            }));
        }
        if(page*3+3<items.length)options.add(new DialogueOption("More",q->supplies(b,items,page+1)));
        else options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
        if(page>0)options.add(new DialogueOption("Página anterior",q->supplies(b,items,page-1)));
        new DialogueBuilder(p).option("Utensílios da cozinha",options.toArray(new DialogueOption[0])).send();
    }
    public boolean use(int item,WorldObject o){
        if(!house.inside())return false;
        PlayerHouse.Binding b=house.kitchenFixture(o);if(b==null)return true;
        HouseFurnishing f=house.furnishing(b);
        if(f.group==15404){
            int out=waterOutput(item);
            if(out>=0)fill(b,item,out);
            else p.sendMessage("You cannot fill that item here.");
            return true;
        }
        if(f.group==15401){
            int drink=barrel(f.key);
            if(item==GLASS&&drink>=0&&p.getItems().playerHasItem(GLASS)){
                if(ProductionBatch.exchange(p,new int[][]{{GLASS,1}},new int[][]{{drink,1}}))p.startAnimation(3660);
            }else p.sendMessage("Use a beer glass from your kitchen shelves.");
            return true;
        }
        if(f.group==15398){
            if(item==FULL_KETTLE){heat(b,f);return true;}
            if(item==KETTLE){p.sendMessage("Fill the kettle with water first.");return true;}
            if(!Cooking.cookThisFood(p,item,o.id))p.sendMessage("You cannot cook that item.");
            return true;
        }
        return true;
    }
    private static int waterOutput(int item){
        if(item==KETTLE)return FULL_KETTLE;
        if(WaterFilling.wateringCan(item))return 5340;
        switch(item){case 1925:return 1929;case 1935:return 1937;case 1923:return 1921;case 229:return 227;default:return -1;}
    }
    private void fill(PlayerHouse.Binding b,int item,int product){
        if(!house.kitchenReady(b)||!p.getItems().playerHasItem(item))return;
        if(item==5340){p.sendMessage("Your watering can is already full.");return;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;String recipe=house.furnishing(b).key;p.startAnimation(2295);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,3){
            public void execute(){
                if(token!=p.productionActionToken||!active()||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()||!house.kitchenReady(b)||!house.furnishing(b).key.equals(recipe)){stop();return;}
                if(!ProductionBatch.exchange(p,new int[][]{{item,1}},new int[][]{{product,1}})){stop();return;}
                p.startAnimation(2295);p.sendMessage("You fill the container with water.");
                if(!p.getItems().playerHasItem(item))stop();
            }
            public void stop(){super.stop();if(token==p.productionActionToken)p.startAnimation(65535);}
        });
    }
    private void heat(PlayerHouse.Binding b,HouseFurnishing f){
        int stove=index(STOVES,f.model(b.root.id));
        if(stove<0){p.sendMessage("You need a stove with a hook or pot for the kettle.");return;}
        if(kettles.containsKey(b.room)){p.sendMessage("There is already a kettle on this stove.");return;}
        if(!house.kitchenReady(b)||!ProductionBatch.exchange(p,new int[][]{{FULL_KETTLE,1}},new int[0][]))return;
        KettleState state=new KettleState(f.key,stove);kettles.put(b.room,state);house.renderKitchen(b.room);p.startAnimation(832);p.sendMessage("You place the kettle over the fire.");
        Server.getEventHandler().submit(new Event<Player>("house-kettle",p,BOIL_TICKS){
            public void execute(){
                if(!house.inside()||p.isDisconnected()||p.isDead||!house.roomRegistered(b.room)||kettles.get(b.room)!=state||!state.recipe.equals(b.room.furniture.get(15398))){stop();return;}
                state.hot=true;house.renderKitchen(b.room);p.sendMessage("The kettle boils.");stop();
            }
        });
    }
    private boolean exchange(int[][] in,int[][] out){return active()&&ProductionBatch.exchange(p,in,out);}
    private boolean cooking(){
        if(p.playerLevel[7]>=20)return true;p.sendMessage("You need Cooking level 20 to make tea.");return false;
    }
    public boolean mix(int first,int second){
        if(!temporary(first)&&!temporary(second))return false;
        if(!active()){p.sendMessage("Use these kitchen items inside your house.");return true;}
        for(int orientation=0;orientation<2;orientation++){
            int a=orientation==0?first:second,b=orientation==0?second:first;
            int family=index(EMPTY_POTS,b);
            if(a==LEAVES&&family>=0){
                if(cooking())exchange(new int[][]{{LEAVES,1},{b,1}},new int[][]{{LEAF_POTS[family],1}});
                return true;
            }
            family=index(LEAF_POTS,b);
            if(a==HOT_KETTLE&&family>=0){
                if(cooking()&&exchange(new int[][]{{HOT_KETTLE,1},{b,1}},new int[][]{{KETTLE,1},{FULL_POTS[family],1}}))
                    p.getPA().addSkillXPMultiplied(52,7,true);
                return true;
            }
            family=potFamily(a);int cup=index(CUPS,b);
            if(family>=0&&cup>=0){
                if(cooking())exchange(new int[][]{{a,1},{b,1}},new int[][]{{a==FULL_POTS[family]+6?EMPTY_POTS[family]:a+2,1},{TEAS[cup],1}});
                return true;
            }
            cup=index(TEAS,a);
            if(cup>=0&&b==1927){exchange(new int[][]{{a,1},{1927,1}},new int[][]{{MILKY_TEAS[cup],1},{1925,1}});return true;}
        }
        p.sendMessage("These kitchen items cannot be combined.");return true;
    }
    public boolean drinkTea(int item,int slot,int inventory){
        int cup=index(TEAS,item);if(cup<0)cup=index(MILKY_TEAS,item);if(cup<0)return false;
        if(inventory!=3214||slot<0||slot>=p.playerItems.length||p.playerItems[slot]!=item+1||!active()||p.teleTimer>0||p.getPotionTimer().elapsed()<3)return true;
        if(replaceSlot(item,slot,CUPS[cup])){
            p.getPotionTimer().reset();p.startAnimation(829);p.getPA().sendSound(2401);
            p.playerLevel[22]=Math.max(p.playerLevel[22],p.getPA().getLevelForXP(p.playerXP[22])+cup+1);p.getPA().refreshSkill(22);
            p.sendMessage("You drink the tea.");
        }return true;
    }
    public boolean emptyTea(int item,int slot,int inventory){
        int cup=index(TEAS,item);if(cup<0)cup=index(MILKY_TEAS,item);if(cup<0)return false;
        if(inventory==3214&&slot>=0&&slot<p.playerItems.length&&p.playerItems[slot]==item+1&&active())
            replaceSlot(item,slot,CUPS[cup]);
        return true;
    }
    private boolean replaceSlot(int item,int slot,int product){
        if(slot<0||slot>=p.playerItems.length||p.playerItems[slot]!=item+1||p.playerItemsN[slot]!=1)return false;
        p.playerItems[slot]=product+1;p.getItems().sendInventoryInterface(3214);return true;
    }
}
