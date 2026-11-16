package io.xeros.content.skills.mining;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCHandler;
import java.util.function.IntUnaryOperator;

/** Historical crystal tool and bracelet charges, separate from the V13 save format. */
public final class MiningAccessories {
    public static final int CRYSTAL=23680,INACTIVE=23682,SHARD=23962,SEED=23953,BRACELET=11074;
    private final Player p;
    private int crystal=10000,clay=28;
    public MiningAccessories(Player p){this.p=p;}
    public int crystal(){return crystal;}
    public int clay(){return clay;}
    public String encode(){return crystal+","+clay;}
    public boolean decode(String text){
        try{String[] fields=text.split(",",-1);if(fields.length!=2||!fields[0].matches("[0-9]{1,5}")||!fields[1].matches("[0-9]{1,2}"))return false;
            int a=Integer.parseInt(fields[0]),b=Integer.parseInt(fields[1]);if(a>20000||b<1||b>28)return false;crystal=a;clay=b;return true;
        }catch(RuntimeException e){return false;}
    }
    private boolean ready(){return p.getUtilityMagic().canInteract()&&p.getGemBag().configurationPermitted();}
    public boolean check(int id){
        if(id==CRYSTAL||id==INACTIVE){p.sendMessage("Your crystal pickaxe has "+(id==INACTIVE?0:crystal)+" / 20000 charges.");return true;}
        if(id==BRACELET){p.sendMessage("Your bracelet of clay has "+clay+" / 28 charges.");return true;}return false;
    }
    private void replace(int from,int to){
        for(int slot=0;slot<p.playerEquipment.length;slot++)if(p.playerEquipment[slot]==from){p.getItems().equipItem(to,1,slot);return;}
        for(int slot=0;slot<p.playerItems.length;slot++)if(p.playerItems[slot]==from+1){p.playerItems[slot]=to+1;p.getItems().sendInventoryInterface(3214);return;}
    }
    public void consumeCrystal(Pickaxe used,IntUnaryOperator random){
        if(used!=Pickaxe.CRYSTAL||crystal<=0)return;
        int ring=p.playerEquipment[Player.playerRing];if((ring==23943||ring==25543||ring==25545)&&random.applyAsInt(10)==0)return;
        if(--crystal==0){replace(CRYSTAL,INACTIVE);p.sendMessage("Your crystal pickaxe has become inactive.");}
    }
    public boolean bracelet(CoreRockMining.Rock rock){return (rock==CoreRockMining.Rock.CLAY||rock==CoreRockMining.Rock.SOFT_CLAY)&&p.getItems().isWearingItem(BRACELET,Player.playerHands);}
    public void consumeClay(){
        if(--clay>0)return;
        p.getItems().equipItem(-1,0,Player.playerHands);clay=28;p.sendMessage("Your bracelet of clay crumbles to dust.");
    }
    public boolean recharge(int id){
        if((id!=CRYSTAL&&id!=INACTIVE)||!ready()||!p.getItems().playerHasItem(id))return false;
        int current=id==INACTIVE?0:crystal;
        int amount=Math.min((20000-current+99)/100,p.getItems().getInventoryCount(SHARD));
        if(amount<=0){p.sendMessage("You need crystal shards and room for more charges.");return false;}
        int[][] in=id==INACTIVE?new int[][]{{INACTIVE,1},{SHARD,amount}}:new int[][]{{CRYSTAL,0},{SHARD,amount}};
        if(!ProductionBatch.exchange(p,in,id==INACTIVE?new int[][]{{CRYSTAL,1}}:new int[0][]))return false;
        crystal=Math.min(20000,current+100*amount);p.interruptActions();check(CRYSTAL);return true;
    }
    public boolean itemOnItem(int a,int b){
        if(a==SHARD&&(b==CRYSTAL||b==INACTIVE)){recharge(b);return true;}
        if(b==SHARD&&(a==CRYSTAL||a==INACTIVE)){recharge(a);return true;}return false;
    }
    public boolean revert(int id){
        if((id!=CRYSTAL&&id!=INACTIVE)||!ready()||!p.getItems().playerHasItem(id))return false;
        if(!ProductionBatch.exchange(p,new int[][]{{id,1}},new int[][]{{11920,1},{SEED,1}}))return false;
        crystal=0;p.interruptActions();return true;
    }
    public boolean requestRevert(int id){
        if((id!=CRYSTAL&&id!=INACTIVE)||!ready()||!p.getItems().playerHasItem(id))return false;
        new DialogueBuilder(p).option("Revert? All crystal charges will be lost.",new DialogueOption("Revert to seed and dragon pickaxe",q->{q.getPA().removeAllWindows();revert(id);}),new DialogueOption("Keep the crystal pickaxe",q->q.getPA().removeAllWindows())).send();return true;
    }
    private boolean nearSinger(NPC npc){
        if(npc==null||!ready()||p.isDisconnected()||p.getSession()==null||npc.isDead()||p.heightLevel!=npc.getHeight()||p.distanceToPoint(npc.getX(),npc.getY())>2)return false;
        boolean reese=(npc.getNpcId()==9133||npc.getNpcId()==9134)&&npc.getX()==3295&&npc.getY()==6043&&npc.getHeight()==0;
        boolean conwenna=(npc.getNpcId()==9135||npc.getNpcId()==9136)&&npc.getX()==3238&&npc.getY()==6066&&npc.getHeight()==0;
        return (reese||conwenna)&&npc.getIndex()>=0&&npc.getIndex()<NPCHandler.npcs.length&&NPCHandler.npcs[npc.getIndex()]==npc;
    }
    public boolean singerService(NPC npc){
        if(!nearSinger(npc))return false;
        if(p.playerLevel[14]<71){p.sendMessage("You need Mining level 71 for the crystal pickaxe.");return false;}
        if(p.getItems().getItemCount(CRYSTAL,true)>0||p.getItems().getItemCount(INACTIVE,true)>0){p.sendMessage("You already own a crystal pickaxe.");return false;}
        if(!ProductionBatch.exchange(p,new int[][]{{11920,1},{SEED,1},{SHARD,180}},new int[][]{{CRYSTAL,1}})){p.sendMessage("Bring a dragon pickaxe, a crystal tool seed and 180 crystal shards.");return false;}
        crystal=10000;p.interruptActions();p.getPA().removeAllWindows();p.sendMessage("The crystal singer has made your pickaxe. The fee included 60 extra crystal shards.");check(CRYSTAL);return true;
    }
    public boolean canUseSinger(NPC npc){return nearSinger(npc);}
    public boolean npc(NPC npc,int option){
        if(npc==null||npc.getNpcId()<9133||npc.getNpcId()>9136||(option!=1&&option!=2))return false;
        if(!nearSinger(npc))return true;
        new DialogueBuilder(p).option("Pay the crystal singer?",new DialogueOption("Crystal pickaxe: dragon pickaxe, seed, 180 shards",q->{q.getPA().removeAllWindows();q.getMiningAccessories().singerService(npc);}),new DialogueOption("Other crystal tools",q->new DialogueBuilder(q).option("Crystal axe",new DialogueOption("Dragon axe + tool seed + 180 shards",v->{v.getPA().removeAllWindows();v.getWoodcuttingTools().singerService(npc);}),new DialogueOption("Leave",v->v.getPA().removeAllWindows())).send())).send();return true;
    }
    private boolean near(WorldObject bowl){
        if(bowl==null||bowl.id!=36552||p.heightLevel!=bowl.height)return false;
        if(p.getRegionProvider().get(bowl.x,bowl.y).getWorldObject(bowl.id,bowl.x,bowl.y,bowl.height).isEmpty())return false;
        var size=bowl.getObjectSize();return Math.max(Math.max(bowl.x-p.absX,p.absX-(bowl.x+size.getX()-1)),Math.max(bowl.y-p.absY,p.absY-(bowl.y+size.getY()-1)))<=1;
    }
    public boolean create(WorldObject bowl){
        if(!ready()||!near(bowl))return false;
        if(p.playerLevel[14]<71||p.playerLevel[13]<76||p.playerLevel[12]<76){p.sendMessage("You need Mining 71, Smithing 76 and Crafting 76.");return false;}
        if(p.getItems().getItemCount(CRYSTAL,true)>0||p.getItems().getItemCount(INACTIVE,true)>0){p.sendMessage("You already own a crystal pickaxe.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;
        p.playerSkilling[13]=true;p.startAnimation(899);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,1){int left=3;
            public void execute(){
                if(token!=p.productionActionToken||!p.playerSkilling[13]||!ready()||p.isDisconnected()||p.getSession()==null||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()||!near(bowl)||p.playerLevel[14]<71||p.playerLevel[13]<76||p.playerLevel[12]<76){stop();return;}
                if(--left>0)return;
                if(ProductionBatch.exchange(p,new int[][]{{11920,1},{SEED,1},{SHARD,120}},new int[][]{{CRYSTAL,1}})){crystal=10000;p.getPA().addSkillXPMultiplied(6000,13,true);p.getPA().addSkillXPMultiplied(6000,12,true);check(CRYSTAL);}stop();
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[13]=false;p.startAnimation(65535);}}
        });return true;
    }
    public static boolean click(Player p,int id,int x,int y){
        if(id!=36552)return false;
        WorldObject bowl=p.getRegionProvider().get(x,y).getWorldObject(id,x,y,p.heightLevel).orElse(null);
        new DialogueBuilder(p).option("Sing a crystal tool?",new DialogueOption("Dragon pickaxe + tool seed + 120 shards",q->q.getMiningAccessories().create(bowl)),new DialogueOption("Other crystal tools",q->new DialogueBuilder(q).option("Sing a crystal axe?",new DialogueOption("Dragon axe + tool seed + 120 shards",v->v.getWoodcuttingTools().createCrystal(bowl)),new DialogueOption("Cancel",v->v.getPA().removeAllWindows())).send())).send();return true;
    }
}
