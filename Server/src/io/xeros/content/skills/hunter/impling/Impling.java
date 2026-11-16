package io.xeros.content.skills.hunter.impling;
import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCHandler;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** A capture reserves its real NPC; reward, XP and journal commit once at completion. */
public final class Impling {
    private static final Map<NPC,Capture> CLAIMS=new IdentityHashMap<>();
    private static final Map<Player,Capture> HUNTERS=new IdentityHashMap<>();
    private static final Random RANDOM=new Random();
    private Impling(){}
    public static boolean isImp(int id){return ImplingData.forId(id)!=null;}
    public static boolean reserved(NPC npc){return CLAIMS.containsKey(npc);}
    public static boolean puro(int x,int y,int h){return h==0&&x>=2556&&x<=2626&&y>=4288&&y<=4354;}
    private static boolean usable(Player p){return p!=null&&p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!Server.getMultiplayerSessionListener().inAnySession(p)&&p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal();}
    private static boolean live(NPC n){int i=n.getIndex();return i>0&&i<NPCHandler.npcs.length&&NPCHandler.npcs[i]==n&&!n.isUnregister()&&!n.isDead()&&!n.needRespawn&&!n.isInvisible()&&n.getInstance()==null;}
    private static boolean near(Player p,NPC n){return p.heightLevel==n.heightLevel&&Math.max(Math.abs(p.absX-n.absX),Math.abs(p.absY-n.absY))<=1;}
    /** 0=bare-handed,1=ordinary net,2=magic net. The equipment requirement is a base level. */
    private static int mode(Player p){
        int id=p.playerEquipment[Player.playerWeapon];if(p.playerEquipmentN[Player.playerWeapon]<=0)return 0;
        if(id==10010)return 1;
        return id==11259&&p.getLevelForXP(p.playerXP[21])>=17?2:0;
    }
    public static void catchImpling(Player p,NPC n){
        ImplingData d=n==null?null:ImplingData.forId(n.getNpcId());if(d==null||!usable(p)||!live(n)||!near(p,n))return;
        boolean inPuro=puro(p.absX,p.absY,p.heightLevel);if(inPuro&&d==ImplingData.CRYSTAL)return;
        int mode=mode(p),required=d.requirement+(mode==0?10:0);
        if(p.playerLevel[21]<required){p.sendMessage("You need Hunter level "+required+(mode==0?" to catch this impling bare-handed.":" to catch this impling."));return;}
        if((inPuro||mode!=0)&&!p.getItems().playerHasItem(11260)){p.sendMessage("You need an empty impling jar.");return;}
        Capture previous=CLAIMS.get(n);if(previous!=null&&!previous.valid())previous.close();
        if(CLAIMS.containsKey(n)){p.sendMessage("Someone is already trying to catch that impling.");return;}
        previous=HUNTERS.get(p);if(previous!=null)previous.close();
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.getPA().resetFollow();
        Capture c=new Capture(p,n,d,mode,inPuro);CLAIMS.put(n,c);HUNTERS.put(p,c);c.begin();
    }
    public static void getReward(Player p,int item){ImplingLoot.open(p,item);}
    /** Compatibility for callers; a hunter catch does not enter the combat death/drop pipeline. */
    public static void kill(NPC n){
        ImplingData d=n==null?null:ImplingData.forId(n.getNpcId());if(d==null)return;
        boolean inPuro=puro(n.absX,n.absY,n.heightLevel);
        n.resetWalkingQueue();n.setDead(true);n.applyDead=true;n.needRespawn=true;n.setInvisible(true);n.setUpdateRequired(true);
        if(ImplingWorld.captured(n))return;
        n.actionTimer=d==ImplingData.CRYSTAL?3000:!inPuro?200:d==ImplingData.EARTH||d==ImplingData.ESSENCE?50:d.ordinal()<=ImplingData.ECLECTIC.ordinal()?7:200;
    }
    private static final class Capture {
        final Player p;final NPC n;final ImplingData d;final int net,x,y,h,weapon,weaponCount;final boolean inPuro,walking;final long token;
        int age;boolean closed;
        Capture(Player p,NPC n,ImplingData d,int net,boolean puro){this.p=p;this.n=n;this.d=d;this.net=net;inPuro=puro;x=p.absX;y=p.absY;h=p.heightLevel;weapon=p.playerEquipment[Player.playerWeapon];weaponCount=p.playerEquipmentN[Player.playerWeapon];token=p.productionActionToken;walking=n.randomWalk;}
        boolean valid(){return !closed&&CLAIMS.get(n)==this&&HUNTERS.get(p)==this&&usable(p)&&live(n)&&near(p,n)&&p.absX==x&&p.absY==y&&p.heightLevel==h&&p.productionActionToken==token&&mode(p)==net&&p.playerEquipment[Player.playerWeapon]==weapon&&p.playerEquipmentN[Player.playerWeapon]==weaponCount&&p.playerLevel[21]>=d.requirement+(net==0?10:0)&&(!(inPuro||net!=0)||p.getItems().playerHasItem(11260));}
        void begin(){n.randomWalk=false;n.resetWalkingQueue();p.facePosition(n.absX,n.absY);p.startAnimation(net==0?7171:net==1?6606:6605);if(net!=0)p.getPA().sendSound(2623);
            Server.getEventHandler().submit(new Event<Capture>("impling-net",this,1){public void execute(){if(!step())stop();}public void stop(){super.stop();close();}});
        }
        boolean step(){
            if(!valid())return false;if(++age<2)return true;
            if(RANDOM.nextInt(256)>=d.threshold(p.playerLevel[21],net!=1)){n.startAnimation(6616);p.sendMessage("You fail to catch the impling.");return false;}
            boolean jarred=inPuro||net!=0;
            int[][] input=jarred?new int[][]{{11260,1}}:new int[0][];
            int[][] reward=jarred?new int[][]{{d.jar,1}}:ImplingLoot.roll(p,d,false).items;
            if(!ProductionBatch.exchange(p,input,reward))return false;
            kill(n);p.getImplingJournal().record(d,inPuro);p.getPA().addSkillXPMultiplied(d.xp(inPuro),21,true);
            p.sendMessage(jarred?"You catch the "+d.name+" in a jar.":"You catch and loot the "+d.name+" bare-handed.");return false;
        }
        void close(){if(closed)return;closed=true;CLAIMS.remove(n,this);HUNTERS.remove(p,this);n.randomWalk=walking;if(p.productionActionToken==token)p.startAnimation(65535);}
    }
}
