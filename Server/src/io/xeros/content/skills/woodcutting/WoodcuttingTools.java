package io.xeros.content.skills.woodcutting;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.*;
import io.xeros.content.skills.firemake.LogData;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import java.util.function.IntUnaryOperator;

/** Persistent infernal and crystal axe charges for the solo character. */
public final class WoodcuttingTools {
    public static final int INFERNAL=13241,INFERNAL_EMPTY=13242,CRYSTAL=23673,CRYSTAL_EMPTY=23675,SHARD=23962,SEED=23953;
    private final Player p;private int infernal=5000,crystal=10000;
    public WoodcuttingTools(Player p){this.p=p;}
    public int infernal(){return infernal;}public int crystal(){return crystal;}
    public String encode(){return infernal+","+crystal;}
    public boolean decode(String text){
        try{String[] fields=text.split(",",-1);if(fields.length!=2||!fields[0].matches("[0-9]{1,4}")||!fields[1].matches("[0-9]{1,5}"))return false;
            int a=Integer.parseInt(fields[0]),b=Integer.parseInt(fields[1]);if(a>5000||b>20000)return false;infernal=a;crystal=b;return true;
        }catch(RuntimeException e){return false;}
    }
    private boolean ready(){return p.getUtilityMagic().canInteract()&&p.getGemBag().configurationPermitted();}
    private void replace(int from,int to){
        for(int slot=0;slot<p.playerEquipment.length;slot++)if(p.playerEquipment[slot]==from){p.getItems().equipItem(to,1,slot);return;}
        for(int slot=0;slot<p.playerItems.length;slot++)if(p.playerItems[slot]==from+1){p.playerItems[slot]=to+1;p.getItems().sendInventoryInterface(3214);return;}
    }
    public boolean check(int id){
        if(id==INFERNAL||id==INFERNAL_EMPTY){p.sendMessage("Your infernal axe has "+(id==INFERNAL_EMPTY?0:infernal)+" / 5000 charges.");return true;}
        if(id==CRYSTAL||id==CRYSTAL_EMPTY){p.sendMessage("Your crystal axe has "+(id==CRYSTAL_EMPTY?0:crystal)+" / 20000 charges.");return true;}return false;
    }
    public boolean combust(Hatchet used,HistoricalTreeData.Kind kind,IntUnaryOperator random){
        LogData log=LogData.getLogData(p,kind.item);
        if(used!=Hatchet.INFERNAL_AXE||infernal<=0||log==null||random.applyAsInt(3)!=0)return false;
        p.getPA().addSkillXPMultiplied(log.getExperience()*.5*SkillingOutfit.PYROMANCER.multiplier(p),11,true);
        if(--infernal==0){replace(INFERNAL,INFERNAL_EMPTY);p.sendMessage("Your infernal axe has run out of charges.");}
        return true;
    }
    public void consumeCrystal(Hatchet used,IntUnaryOperator random){
        if(used!=Hatchet.CRYSTAL||crystal<=0)return;
        int ring=p.playerEquipment[Player.playerRing];if((ring==23943||ring==25543||ring==25545)&&random.applyAsInt(10)==0)return;
        if(--crystal==0){replace(CRYSTAL,CRYSTAL_EMPTY);p.sendMessage("Your crystal axe has become inactive.");}
    }
    private boolean infernalLevels(){return p.getPA().getLevelForXP(p.playerXP[8])>=61&&p.getPA().getLevelForXP(p.playerXP[11])>=85;}
    public boolean createInfernal(){
        if(!ready()||!infernalLevels()){p.sendMessage("You need base Woodcutting 61 and Firemaking 85.");return false;}
        if(p.getItems().getItemCount(INFERNAL,true)>0||p.getItems().getItemCount(INFERNAL_EMPTY,true)>0){p.sendMessage("You already own an infernal axe.");return false;}
        return infernalBatch(new int[][]{{6739,1},{13233,1}});
    }
    public boolean rechargeInfernal(int reagent){
        if(!ready()||!infernalLevels()||(reagent!=6739&&reagent!=13233))return false;
        if(p.getItems().getItemCount(INFERNAL,true)>0){p.sendMessage("Use your existing charged infernal axe first.");return false;}
        return infernalBatch(new int[][]{{INFERNAL_EMPTY,1},{reagent,1}});
    }
    private boolean infernalBatch(int[][] input){
        return ProductionBatch.start(p,11,1,350,1,4514,3,input,new int[][]{{INFERNAL,1}},()->{
            infernal=5000;p.getPA().addSkillXPMultiplied(200,8,true);check(INFERNAL);
        },this::ready);
    }
    public boolean rechargeCrystal(int id){
        if((id!=CRYSTAL&&id!=CRYSTAL_EMPTY)||!ready()||!p.getItems().playerHasItem(id))return false;
        int current=id==CRYSTAL_EMPTY?0:crystal,amount=Math.min((20000-current+99)/100,p.getItems().getInventoryCount(SHARD));
        if(amount<=0){p.sendMessage("You need crystal shards and room for more charges.");return false;}
        if(!ProductionBatch.exchange(p,id==CRYSTAL_EMPTY?new int[][]{{id,1},{SHARD,amount}}:new int[][]{{id,0},{SHARD,amount}},
            id==CRYSTAL_EMPTY?new int[][]{{CRYSTAL,1}}:new int[0][]))return false;
        crystal=Math.min(20000,current+amount*100);p.interruptActions();check(CRYSTAL);return true;
    }
    public boolean revert(int id){
        if((id!=CRYSTAL&&id!=CRYSTAL_EMPTY)||!ready())return false;
        if(!ProductionBatch.exchange(p,new int[][]{{id,1}},new int[][]{{6739,1},{SEED,1}}))return false;
        crystal=0;p.interruptActions();return true;
    }
    public boolean requestRevert(int id){
        if((id!=CRYSTAL&&id!=CRYSTAL_EMPTY)||!ready()||!p.getItems().playerHasItem(id))return false;
        new DialogueBuilder(p).option("Revert? All crystal charges will be lost.",
            new DialogueOption("Revert to seed and dragon axe",q->{q.getPA().removeAllWindows();revert(id);}),
            new DialogueOption("Keep the crystal axe",q->q.getPA().removeAllWindows())).send();return true;
    }
    public boolean itemOnItem(int a,int b){
        if((a==6739&&b==13233)||(b==6739&&a==13233)){createInfernal();return true;}
        if(a==INFERNAL_EMPTY&&(b==6739||b==13233)){rechargeInfernal(b);return true;}
        if(b==INFERNAL_EMPTY&&(a==6739||a==13233)){rechargeInfernal(a);return true;}
        if(a==SHARD&&(b==CRYSTAL||b==CRYSTAL_EMPTY)){rechargeCrystal(b);return true;}
        if(b==SHARD&&(a==CRYSTAL||a==CRYSTAL_EMPTY)){rechargeCrystal(a);return true;}
        return false;
    }
    private boolean near(WorldObject bowl){
        if(bowl==null||bowl.id!=36552||p.heightLevel!=bowl.height||p.getRegionProvider().get(bowl.x,bowl.y).getWorldObject(bowl.id,bowl.x,bowl.y,bowl.height).isEmpty())return false;
        var size=bowl.getObjectSize();return Math.max(Math.max(bowl.x-p.absX,p.absX-bowl.x-size.getX()+1),Math.max(bowl.y-p.absY,p.absY-bowl.y-size.getY()+1))<=1;
    }
    private boolean ownsCrystal(){return p.getItems().getItemCount(CRYSTAL,true)>0||p.getItems().getItemCount(CRYSTAL_EMPTY,true)>0;}
    public boolean createCrystal(WorldObject bowl){
        if(!ready()||!near(bowl))return false;
        if(p.playerLevel[8]<71||p.playerLevel[13]<76||p.playerLevel[12]<76){p.sendMessage("You need Woodcutting 71, Smithing 76 and Crafting 76.");return false;}
        if(ownsCrystal()){p.sendMessage("You already own a crystal axe.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;p.playerSkilling[13]=true;p.startAnimation(899);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,3){
            public void execute(){
                if(token==p.productionActionToken&&p.playerSkilling[13]&&ready()&&!p.isDisconnected()&&p.getSession()!=null&&p.absX==x&&p.absY==y&&p.heightLevel==h
                    &&p.isWalkingQueueEmpty()&&near(bowl)&&p.playerLevel[8]>=71&&p.playerLevel[13]>=76&&p.playerLevel[12]>=76
                    &&ProductionBatch.exchange(p,new int[][]{{6739,1},{SEED,1},{SHARD,120}},new int[][]{{CRYSTAL,1}})){
                    crystal=10000;p.getPA().addSkillXPMultiplied(6000,13,true);p.getPA().addSkillXPMultiplied(6000,12,true);check(CRYSTAL);
                }stop();
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[13]=false;p.startAnimation(65535);}}
        });return true;
    }
    public boolean singerService(NPC npc){
        if(!ready()||!p.getMiningAccessories().canUseSinger(npc))return false;
        if(p.playerLevel[8]<71){p.sendMessage("You need Woodcutting level 71 for the crystal axe.");return false;}
        if(ownsCrystal()){p.sendMessage("You already own a crystal axe.");return false;}
        if(!ProductionBatch.exchange(p,new int[][]{{6739,1},{SEED,1},{SHARD,180}},new int[][]{{CRYSTAL,1}})){p.sendMessage("Bring a dragon axe, a tool seed and 180 crystal shards.");return false;}
        crystal=10000;p.interruptActions();p.getPA().removeAllWindows();check(CRYSTAL);return true;
    }
}
