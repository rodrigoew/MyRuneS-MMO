package io.xeros.content.skills.mining;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.world.ShopHandler;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** Mining Guild access and mineral exchange using the prices before May 2024. */
public final class MiningGuild {
    public static final int SHOP=220,MINERAL=21341;
    private static final Map<Integer,Integer> PRICES=Map.of(21343,60,21345,120,24851,10,24853,20);
    private final Player p;private boolean minerals=true,caveAccess;
    public MiningGuild(Player p){this.p=p;}
    public boolean receivesMinerals(){return minerals;}
    public boolean caveAccess(){return caveAccess;}
    public String encodeCaveAccess(){return caveAccess?"1":"0";}
    public boolean decodeCaveAccess(String text){if(!text.equals("0")&&!text.equals("1"))return false;caveAccess=text.equals("1");return true;}
    /** The western crystals introduced in September 2021, checked against the 2022 loc data. */
    public static boolean diaryCrystals(int x,int y,int h){return h==0&&x>=3001&&x<=3012&&y>=9705&&y<=9727;}
    public boolean canMineAmethyst(int x,int y,int h){return !diaryCrystals(x,y,h)||caveAccess&&p.getDiaryManager().getFaladorDiary().hasCompleted("ELITE");}
    public boolean requestCaveAccess(){
        if(!belona())return false;p.getPA().removeAllWindows();
        if(!p.getDiaryManager().getFaladorDiary().hasCompleted("ELITE")){p.sendMessage("Complete the Elite Falador Diary before asking Belona for access to the western amethyst cave.");return false;}
        caveAccess=true;p.sendMessage("Belona grants you permission to mine the western amethyst crystals.");return true;
    }
    public String encode(){return minerals?"1":"0";}
    public boolean decode(String text){if(!text.equals("0")&&!text.equals("1"))return false;minerals=text.equals("1");return true;}
    private boolean ready(){return !p.isDead&&!p.isDisconnected()&&p.getSession()!=null&&!p.isBanking&&!p.getLock().cannotInteract(p)&&p.getGemBag().configurationPermitted()&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    private boolean near(int npc,int x,int y){return ready()&&p.heightLevel==0&&p.distanceToPoint(x,y)<=2&&Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).anyMatch(n->n.getNpcId()==npc&&!n.isDead()&&n.getHeight()==0&&n.getX()==x&&n.getY()==y);}
    private boolean belona(){return near(7719,3017,9724);}
    public static int price(int item){return PRICES.getOrDefault(item,0);}
    private static int slot(int item){for(int i=0;i<ShopHandler.ShopItems[SHOP].length;i++)if(ShopHandler.ShopItems[SHOP][i]==item+1)return i;return -1;}
    public void openShop(){if(belona()){p.getPA().removeAllWindows();p.getShops().openShop(SHOP);}}
    public boolean buy(int item,int fromSlot,int requested){
        if(!belona()||p.myShopId!=SHOP||!p.isShopping||requested<=0||price(item)==0||fromSlot<0||fromSlot>=ShopHandler.ShopItems[SHOP].length||fromSlot!=slot(item))return false;
        int amount=Math.min(requested,ShopHandler.ShopItemsN[SHOP][fromSlot]),bought=0;
        while(bought<amount&&ProductionBatch.exchange(p,new int[][]{{MINERAL,price(item)}},new int[][]{{item,1}})){ShopHandler.ShopItemsN[SHOP][fromSlot]--;bought++;}
        if(bought==0)p.sendMessage("You need enough unidentified minerals, stock and inventory space.");
        refresh();return bought>0;
    }
    public boolean sell(int item,int fromSlot,int requested){
        int shopSlot=slot(item),cost=price(item)*4/5;
        if(!belona()||p.myShopId!=SHOP||!p.isShopping||cost<=0||shopSlot<0||requested<=0||fromSlot<0||fromSlot>=p.playerItems.length||p.playerItems[fromSlot]!=item+1)return false;
        int amount=Math.min(requested,Math.min(p.getItems().getInventoryCount(item),Math.min(Integer.MAX_VALUE/cost,Integer.MAX_VALUE-ShopHandler.ShopItemsN[SHOP][shopSlot])));
        if(amount<=0||!ProductionBatch.exchange(p,new int[][]{{item,amount}},new int[][]{{MINERAL,cost*amount}}))return false;
        ShopHandler.ShopItemsN[SHOP][shopSlot]+=amount;refresh();return true;
    }
    private void refresh(){p.getItems().sendInventoryInterface(3823);p.getShops().resetShop(SHOP);p.getShops().updatePlayerShop();}
    public boolean combine(){
        if(!belona())return false;
        if(!ProductionBatch.exchange(p,new int[][]{{21343,1},{21345,1},{MINERAL,60}},new int[][]{{21392,1}})){p.sendMessage("Bring both mining gloves and superior mining gloves, plus 60 unidentified minerals.");return false;}
        p.getPA().removeAllWindows();p.sendMessage("Belona combines your gloves into expert mining gloves.");return true;
    }
    public boolean toggle(){if(!belona())return false;minerals=!minerals;p.sendMessage(minerals?"You will now receive unidentified minerals in the members' Mining Guild.":"You will no longer receive unidentified minerals.");return true;}
    public boolean cape(){
        if(!near(7716,3018,3337))return false;
        if(p.getPA().getLevelForXP(p.playerXP[14])<99){p.sendMessage("You need base Mining level 99 to buy the Mining skillcape.");return false;}
        boolean trimmed=false;for(int i=0;i<p.playerXP.length;i++)if(i!=14&&p.getPA().getLevelForXP(p.playerXP[i])>=99){trimmed=true;break;}
        if(!ProductionBatch.exchange(p,new int[][]{{995,99000}},new int[][]{{trimmed?9793:9792,1},{9794,1}}))return false;
        p.getPA().removeAllWindows();return true;
    }
    public boolean npc(NPC npc,int option){
        if(npc==null)return false;
        if(npc.getNpcId()==7719&&npc.getX()==3017&&npc.getY()==9724){
            if(!belona())return true;
            if(option==2){openShop();return true;}if(option==3){toggle();return true;}if(option!=1)return false;
            new DialogueBuilder(p).option("Belona - Mining Guild",new DialogueOption("Trade unidentified minerals",q->q.getMiningGuild().openShop()),new DialogueOption("Combine gloves: 60 minerals",q->q.getMiningGuild().combine()),new DialogueOption(minerals?"Stop receiving minerals":"Receive minerals",q->{q.getPA().removeAllWindows();q.getMiningGuild().toggle();}),new DialogueOption("Access to the western amethyst cave",q->q.getMiningGuild().requestCaveAccess()),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return true;
        }
        if(npc.getNpcId()==7716&&npc.getX()==3018&&npc.getY()==3337){
            if(near(7716,3018,3337))new DialogueBuilder(p).option("Gadrin — Mining Guild",new DialogueOption("Buy Mining cape and hood: 99000 coins",q->q.getMiningGuild().cape()),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return true;
        }return false;
    }
    private boolean near(WorldObject o){if(o==null||!ready()||o.height!=p.heightLevel)return false;var size=o.getObjectSize();return Math.max(Math.max(o.x-p.absX,p.absX-o.x-size.getX()+1),Math.max(o.y-p.absY,p.absY-o.y-size.getY()+1))<=1;}
    public boolean object(int id,int x,int y){
        boolean down=id==30367&&x>=3018&&x<=3020&&y>=3338&&y<=3340,up=id==17385&&x>=3018&&x<=3020&&y>=9738&&y<=9740;
        boolean door=id==30364&&x==3046&&y==9756||id==30365&&x==3019&&y==9732||id==30366&&x==3043&&y==9729;
        if(!down&&!up&&!door)return false;
        WorldObject o=p.getRegionProvider().get(x,y).getWorldObject(id,x,y,p.heightLevel).orElse(null);if(!near(o)||p.heightLevel!=0)return true;
        MiningGuildArea.initialize();
        if((down||id==30364&&p.absY>y)&&p.playerLevel[14]<60){p.sendMessage("You need Mining level 60 to enter the Mining Guild.");return true;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        if(door){GlobalObject opened=new GlobalObject(-1,x,y,0,o.face,o.type,2,id);opened.setInstance(p.getInstance());Server.getGlobalObjects().add(opened);int target=p.absY>y?y:y+1;PathFinder.getPathFinder().findRoute(p,x,target,false,1,1);return true;}
        final int px=p.absX,py=p.absY;long token=p.productionActionToken;p.startAnimation(down?828:827);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,1){public void execute(){if(token==p.productionActionToken&&ready()&&p.absX==px&&p.absY==py&&p.heightLevel==0&&(up||p.playerLevel[14]>=60)){p.moveTo(new Position(px,py+(down?6400:-6400),0));p.startAnimation(65535);}stop();}});return true;
    }
}
