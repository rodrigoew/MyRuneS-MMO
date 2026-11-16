package io.xeros.content.skills.mining;

import io.xeros.content.dialogue.*;
import io.xeros.content.skills.*;
import io.xeros.model.entity.player.Player;
import java.util.function.IntUnaryOperator;

/** Persistent charges for the solo character's infernal pickaxe, celestial ring and signet. */
public final class MiningTools {
    public static final int DUST=25527;
    private final Player p;private int infernal=5000,ring,signet;
    public MiningTools(Player p){this.p=p;}
    public int infernal(){return infernal;}public int charges(int id){return id==25539||id==25541?ring:id==25543||id==25545?signet:0;}
    private void charges(int id,int n){if(id==25539||id==25541)ring=n;else signet=n;}
    public static boolean ring(int id){return id==25539||id==25541||id==25543||id==25545;}
    public static boolean charged(int id){return id==25541||id==25545;}
    public String encode(){return infernal+","+ring+","+signet;}
    public boolean decode(String value){try{String[] fields=value.split(",",-1);if(fields.length!=3)return false;int[] n=new int[3];for(int i=0;i<3;i++){if(!fields[i].matches("[0-9]{1,5}"))return false;n[i]=Integer.parseInt(fields[i]);if(n[i]>(i==0?5000:10000))return false;}infernal=n[0];ring=n[1];signet=n[2];return true;}catch(RuntimeException e){return false;}}
    private boolean ready(){return !p.isBanking&&!p.isShopping&&p.getUtilityMagic().canInteract()&&p.getGemBag().configurationPermitted();}
    public void check(int id){
        if(id==13243||id==13244)p.sendMessage("Your infernal pickaxe has "+(id==13244?0:infernal)+" / 5000 charges.");
        else if(ring(id))p.sendMessage("Your celestial "+(id==25543||id==25545?"signet":"ring")+" has "+charges(id)+" / 10000 charges.");
    }
    private void replace(int oldId,int newId){
        for(int i=0;i<p.playerItems.length;i++)if(p.playerItems[i]==oldId+1){p.playerItems[i]=newId+1;p.getItems().sendInventoryInterface(3214);return;}
        for(int slot=0;slot<p.playerEquipment.length;slot++)if(p.playerEquipment[slot]==oldId){p.getItems().equipItem(newId,p.playerEquipmentN[slot],slot);return;}
    }
    public boolean charge(int id){
        if(!ring(id)||!ready()||!p.getItems().playerHasItem(id))return false;
        int amount=Math.min(10000-charges(id),p.getItems().getInventoryCount(DUST));if(amount<=0){p.sendMessage("You need stardust and room for more charges.");return false;}
        int result=charged(id)?id:id+2;int[][] input=result==id?new int[][]{{id,0},{DUST,amount}}:new int[][]{{id,1},{DUST,amount}};
        int[][] output=result==id?new int[0][]:new int[][]{{result,1}};
        if(!ProductionBatch.exchange(p,input,output))return false;charges(id,charges(id)+amount);p.interruptActions();check(result);return true;
    }
    public boolean uncharge(int id){
        if(!charged(id)||!ready()||!p.getItems().playerHasItem(id))return false;int amount=charges(id);
        int[][] outputs=amount==0?new int[][]{{id-2,1}}:new int[][]{{id-2,1},{DUST,amount}};
        if(!ProductionBatch.exchange(p,new int[][]{{id,1}},outputs))return false;charges(id,0);p.interruptActions();return true;
    }
    public boolean requestUncharge(int id){
        if(!charged(id)||!ready()||!p.getItems().playerHasItem(id))return false;
        new DialogueBuilder(p).option("Return the remaining stardust?",new DialogueOption("Uncharge",q->{q.getPA().removeAllWindows();uncharge(id);}),new DialogueOption("Keep charged",q->q.getPA().removeAllWindows())).send();return true;
    }
    public boolean itemOnItem(int a,int b){
        int id=ring(a)?a:ring(b)?b:-1,other=id==a?b:a;if(id>=0&&other==DUST){charge(id);return true;}
        int pick=(a==13243||a==13244)?a:(b==13243||b==13244)?b:-1;int reagent=pick==a?b:a;
        if(pick<0||(reagent!=13233&&reagent!=11920))return false;
        if(pick==13243){p.sendMessage("The infernal pickaxe must run out of charges before it can be recharged.");return true;}
        recharge(reagent);return true;
    }
    private boolean levels(){return p.getPA().getLevelForXP(p.playerXP[14])>=61&&p.getPA().getLevelForXP(p.playerXP[13])>=85;}
    public boolean create(){
        if(!ready()||!levels()){p.sendMessage("You need base Mining level 61 and Smithing level 85.");return false;}
        if(p.getItems().getItemCount(13243,true)>0||p.getItems().getItemCount(13244,true)>0){p.sendMessage("You already own an infernal pickaxe.");return false;}
        return make(new int[][]{{13233,1},{11920,1}});
    }
    public boolean recharge(int reagent){
        if((reagent!=13233&&reagent!=11920)||!ready()||!levels())return false;
        return make(new int[][]{{13244,1},{reagent,1}});
    }
    private boolean make(int[][] inputs){return ProductionBatch.start(p,13,85,350,1,4514,3,inputs,new int[][]{{13243,1}},()->{infernal=5000;p.getPA().addSkillXPMultiplied(200,14,true);p.sendMessage("Your infernal pickaxe now has 5000 charges.");});}
    public static double infernalXp(CoreRockMining.Rock rock){
        switch(rock){case COPPER:case TIN:return 3.6;case IRON:case SILVER:return 5.5;case COAL:return 6;case GOLD:return 9;case MITHRIL:return 12;case ADAMANT:return 18.75;case RUNITE:return 25;case BLURITE:return 4;default:return 0;}
    }
    public boolean combust(CoreRockMining.Rock rock,IntUnaryOperator random){
        double xp=infernalXp(rock);if(xp==0||infernal<=0||Pickaxe.getBestPickaxe(p)!=Pickaxe.INFERNAL||random.applyAsInt(3)!=0)return false;
        infernal--;p.getPA().addSkillXPMultiplied(xp,13,true);if(infernal==0){replace(13243,13244);p.sendMessage("Your infernal pickaxe has run out of charges.");}return true;
    }
    public boolean ringBonus(CoreRockMining.Rock rock,IntUnaryOperator random){
        int id=p.playerEquipment[Player.playerRing];if(!charged(id)||charges(id)<=0||rock==CoreRockMining.Rock.ESSENCE||rock==CoreRockMining.Rock.RUNITE)return false;
        charges(id,charges(id)-1);boolean bonus=random.applyAsInt(10)==0;if(charges(id)==0)replace(id,id-2);return bonus;
    }
}
