package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCHandler;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import java.util.*;

/** Net hunting uses the 2022 four species; jars are exchanged only on a successful catch. */
public final class ButterflyHunting {
    public enum Species {
        RUBY(5556,10020,15,24,913), SAPPHIRE(5555,10018,25,34,912),
        SNOWY(5554,10016,35,44,911), WARLOCK(5553,10014,45,54,910);
        public final int npc,jar,level,xp,releaseGfx;
        Species(int npc,int jar,int level,int xp,int gfx){this.npc=npc;this.jar=jar;this.level=level;this.xp=xp;releaseGfx=gfx;}
    }
    private static final Map<NPC,Capture> CLAIMS=new IdentityHashMap<>();
    private static final Map<Player,Capture> HUNTERS=new IdentityHashMap<>();
    private static final Random RANDOM=new Random();
    private ButterflyHunting(){}
    public static Species species(int id){for(Species s:Species.values())if(s.npc==id)return s;return null;}
    public static Species jar(int id){for(Species s:Species.values())if(s.jar==id)return s;return null;}
    private static boolean canRelease(Player p){return p!=null&&p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    private static boolean usable(Player p){return canRelease(p)&&p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal();}
    private static boolean live(NPC n){int i=n.getIndex();return i>0&&i<NPCHandler.npcs.length&&NPCHandler.npcs[i]==n&&!n.isDead()&&!n.needRespawn&&!n.isInvisible()&&n.getInstance()==null;}
    private static boolean net(Player p){int id=p.playerEquipment[Player.playerWeapon];return p.playerEquipmentN[Player.playerWeapon]>0&&(id==10010||(id==11259&&p.getLevelForXP(p.playerXP[21])>=17));}
    private static boolean near(Player p,NPC n){return p.heightLevel==n.getHeight()&&Math.max(Math.abs(p.absX-n.getX()),Math.abs(p.absY-n.getY()))<=1;}
    /** Local chance interpolation: exact 2022 catch coefficients remain an explicit fidelity gap. */
    static int chance(int level,Species species,boolean magic){return Math.min(255,Math.max(1,Math.min(235,96+Math.max(0,level-species.level)*2)+(magic?20:0)));}
    public static boolean click(Player p,NPC n,int option){
        Species s=n==null?null:species(n.getNpcId());if(s==null)return false;
        if(option!=1||!usable(p)||!live(n)||!near(p,n))return true;
        if(p.playerLevel[21]<s.level){p.sendMessage("You need Hunter level "+s.level+" to catch this butterfly.");return true;}
        if(!net(p)){p.sendMessage("You need to wield a butterfly net to catch butterflies.");return true;}
        if(!p.getItems().playerHasItem(10012)){p.sendMessage("You need an empty butterfly jar.");return true;}
        Capture claimed=CLAIMS.get(n);
        if(claimed!=null&&!claimed.valid())claimed.close();
        if(CLAIMS.containsKey(n)){p.sendMessage("Someone is already trying to catch that butterfly.");return true;}
        Capture old=HUNTERS.get(p);if(old!=null)old.close();
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.getPA().resetFollow();
        Capture c=new Capture(p,n,s);CLAIMS.put(n,c);HUNTERS.put(p,c);c.begin();return true;
    }
    public static boolean release(Player p,int item,int slot,int widget){
        Species s=jar(item);if(s==null)return false;
        if(widget!=3214||!canRelease(p)||!p.getItems().isItemInInventorySlot(item,slot))return true;
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        // Slot validation precedes the atomic one-for-one exchange; no stat effect on the releaser.
        if(ProductionBatch.exchange(p,new int[][]{{item,1}},new int[][]{{10012,1}})){
            p.startAnimation(5214);p.gfx0(s.releaseGfx);p.sendMessage("You release the butterfly from the jar.");
        }
        return true;
    }
    public static boolean aid(Player p,Player target,int item,int slot,int widget){
        Species s=jar(item);if(s==null)return false;
        if(widget!=3214||!canRelease(p)||target==null||target==p||!canRelease(target)||!p.getItems().isItemInInventorySlot(item,slot))return true;
        int i=target.getIndex();if(i<=0||i>=PlayerHandler.players.length||PlayerHandler.players[i]!=target)return true;
        if(p.getInstance()!=target.getInstance()||p.heightLevel!=target.heightLevel||Math.max(Math.abs(p.absX-target.absX),Math.abs(p.absY-target.absY))>1){p.sendMessage("You need to move closer to that player.");return true;}
        if(p.getMode().isIronmanType()||target.getMode().isIronmanType()){p.sendMessage("Ironmen cannot give or receive butterfly effects.");return true;}
        if(!target.acceptAid){p.sendMessage("That player is not accepting aid.");return true;}
        if(s!=Species.SNOWY&&(!p.getPosition().inMulti()||!target.getPosition().inMulti()||!p.getFriendsList().getRepository().isFriend(target))){p.sendMessage("Use this on a friend in a multi-combat area.");return true;}
        if(!ProductionBatch.exchange(p,new int[][]{{item,1}},new int[][]{{10012,1}}))return true;
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.facePosition(target.absX,target.absY);p.startAnimation(5214);target.gfx0(s.releaseGfx);
        if(s==Species.SNOWY){target.getHealth().increase(15);target.getPA().refreshSkill(3);}
        else{int skill=s==Species.RUBY?0:s==Species.SAPPHIRE?1:2;int base=target.getLevelForXP(target.playerXP[skill]);int boosted=base+base*15/100+4;target.playerLevel[skill]=Math.max(target.playerLevel[skill],boosted);target.getPA().refreshSkill(skill);}
        p.sendMessage("You release the butterfly to help "+target.getDisplayName()+".");return true;
    }
    private static final class Capture {
        final Player p;final NPC n;final Species s;final int x,y,h,weapon;final long token;final boolean wandering;
        int age;boolean closed;
        Capture(Player p,NPC n,Species s){this.p=p;this.n=n;this.s=s;x=p.absX;y=p.absY;h=p.heightLevel;weapon=p.playerEquipment[Player.playerWeapon];token=p.productionActionToken;wandering=n.randomWalk;}
        boolean valid(){return !closed&&CLAIMS.get(n)==this&&HUNTERS.get(p)==this&&usable(p)&&live(n)&&near(p,n)&&p.absX==x&&p.absY==y&&p.heightLevel==h&&p.productionActionToken==token&&p.playerLevel[21]>=s.level&&net(p)&&p.playerEquipment[Player.playerWeapon]==weapon&&p.getItems().playerHasItem(10012);}
        void begin(){n.randomWalk=false;n.resetWalkingQueue();p.facePosition(n.getX(),n.getY());p.startAnimation(5209);p.getPA().sendSound(2623);
            Server.getEventHandler().submit(new Event<Capture>("butterfly-net",this,1){public void execute(){if(!step())stop();}public void stop(){super.stop();close();}});
        }
        boolean step(){
            if(!valid())return false;if(++age<2)return true;
            if(RANDOM.nextInt(256)>=chance(p.playerLevel[21],s,weapon==11259)){p.sendMessage("You fail to catch the butterfly.");return false;}
            if(!ProductionBatch.exchange(p,new int[][]{{10012,1}},new int[][]{{s.jar,1}}))return false;
            // Use the existing native respawn path while bypassing combat death, drops and kill credit.
            n.resetWalkingQueue();n.setDead(true);n.applyDead=true;n.needRespawn=true;n.actionTimer=5;n.setInvisible(true);n.setUpdateRequired(true);
            p.getPA().addSkillXPMultiplied(s.xp,21,true);p.sendMessage("You catch the butterfly.");return false;
        }
        void close(){if(closed)return;closed=true;CLAIMS.remove(n,this);HUNTERS.remove(p,this);n.randomWalk=wandering;if(p.productionActionToken==token)p.startAnimation(65535);}
    }
}
