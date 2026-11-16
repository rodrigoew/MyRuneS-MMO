package io.xeros.content.skills.hunter.impling;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.Player;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/** Character-owned Puro-Puro equipment, exchange and daily supplies (2022). */
public final class ElnockServices {
    public static final int NPC_ID=5734,GENERATOR=11258,REPELLENT=11262,SCROLL=11273;
    private final Player p;
    private boolean starter,strengthXp=true,repellent;
    private int net,jars,charges=100,daily;
    private long day=-1,affinityUntil;
    public ElnockServices(Player p){this.p=p;}
    public boolean strengthXp(){return strengthXp;}
    public int charges(){return charges;}
    public int storedJars(){return jars;}
    public int storedNet(){return net;}
    public boolean storedRepellent(){return repellent;}
    public int purchasedToday(){rollDay();return daily;}
    public boolean starterClaimed(){return starter;}
    public boolean affinity(){return System.currentTimeMillis()<affinityUntil;}
    public void grantAffinity(){affinityUntil=System.currentTimeMillis()+30*60*1000L;}
    private boolean ready(){return p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!p.getLock().cannotInteract(p)&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    private boolean near(){return ready()&&p.heightLevel==0&&p.distanceToPoint(2586,4314)<=2&&Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).anyMatch(n->n.getNpcId()==NPC_ID&&n.getX()==2586&&n.getY()==4314&&n.getHeight()==0&&!n.isDead()&&!n.isInvisible());}
    private int count(int id){return p.getItems().getInventoryCount(id);}
    private boolean owns(int id){return count(id)>0||p.getItems().bankContains(id)||p.getItems().isWearingItem(id);}
    private void close(){p.getPA().removeAllWindows();}
    private void rollDay(){long now=LocalDate.now(ZoneOffset.UTC).toEpochDay();if(now>day){day=now;daily=0;}}
    public String encode(){return "2,"+(starter?1:0)+","+net+","+(repellent?1:0)+","+jars+","+charges+","+(strengthXp?1:0)+","+day+","+daily+","+affinityUntil;}
    public boolean restore(String text){
        try{String[] s=text.trim().split(",",-1);if(!(s.length==9&&s[0].equals("1")||s.length==10&&s[0].equals("2")))return false;int a=Integer.parseInt(s[1]),b=Integer.parseInt(s[2]),c=Integer.parseInt(s[3]),d=Integer.parseInt(s[4]),e=Integer.parseInt(s[5]),f=Integer.parseInt(s[6]),h=Integer.parseInt(s[8]);long g=Long.parseLong(s[7]),expiry=s.length==10?Long.parseLong(s[9]):0;
            if((a!=0&&a!=1)||(b!=0&&b!=10010&&b!=11259)||(c!=0&&c!=1)||d<0||d>1000||e<0||e>100||(f!=0&&f!=1)||g< -1||h<0||h>10)return false;
            if(expiry<0)return false;starter=a==1;net=b;repellent=c==1;jars=d;charges=e;strengthXp=f==1;day=g;daily=h;affinityUntil=Math.min(expiry,System.currentTimeMillis()+30*60*1000L);return true;
        }catch(NumberFormatException ex){return false;}
    }
    public boolean starter(){
        if(!near())return false;close();if(starter){p.sendMessage("Elnock has already given you your starting equipment; it cannot be reclaimed.");return false;}
        if(!ProductionBatch.exchange(p,new int[0][],new int[][]{{10010,1},{11260,7},{SCROLL,1}}))return false;
        starter=true;p.sendMessage("Elnock gives you a butterfly net, seven impling jars and an impling scroll.");return true;
    }
    public boolean scroll(){if(!near())return false;close();if(owns(SCROLL)){p.sendMessage("You already have an impling scroll.");return false;}return ProductionBatch.exchange(p,new int[0][],new int[][]{{SCROLL,1}});}
    public boolean exchange(int item){
        if(!near())return false;close();int[][] cost;
        if(item==11259)cost=new int[][]{{11242,3},{11244,2},{11246,1}};
        else if(item==REPELLENT)cost=new int[][]{{11238,3},{11240,2},{11242,1}};
        else if(item==GENERATOR){cost=new int[][]{{11246,3},{11248,2},{11250,1}};if(owns(GENERATOR)){p.sendMessage("You already own a jar generator. Choose Recharge to refill it for the same exchange cost.");return false;}}
        else return false;
        if(!ProductionBatch.exchange(p,cost,new int[][]{{item,1}}))return false;if(item==GENERATOR)charges=100;return true;
    }
    public boolean recharge(){
        if(!near())return false;close();if(count(GENERATOR)!=1){p.sendMessage("Bring your jar generator in your inventory to recharge it.");return false;}
        if(charges==100){p.sendMessage("Your jar generator is already fully charged.");return false;}
        if(!ProductionBatch.exchange(p,new int[][]{{11246,3},{11248,2},{11250,1}},new int[0][]))return false;charges=100;p.sendMessage("Elnock recharges your jar generator to 100%.");return true;
    }
    public boolean emptyJars(int filled){if(!near()||ImplingData.forJar(filled)==null)return false;close();return ProductionBatch.exchange(p,new int[][]{{filled,1}},new int[][]{{11260,3}});}
    public int buyJars(int requested){
        if(!near()||requested<=0)return 0;close();rollDay();int limit=Math.min(requested,10-daily),bought=0;
        while(bought<limit&&ProductionBatch.exchange(p,new int[][]{{995,2000}},new int[][]{{11260,1}})){daily++;bought++;}
        p.sendMessage("You bought "+bought+" impling jar(s). Elnock can sell you "+(10-daily)+" more today.");return bought;
    }
    public boolean toggleStrength(){if(!near())return false;close();strengthXp=!strengthXp;p.sendMessage(strengthXp?"You will receive Strength XP when pushing magical wheat.":"You will no longer receive Strength XP when pushing magical wheat.");return true;}
    public int deposit(){
        if(!near())return 0;close();int added=0;
        if(net==0){int candidate=count(11259)>0?11259:count(10010)>0?10010:0;if(candidate!=0&&ProductionBatch.exchange(p,new int[][]{{candidate,1}},new int[0][])){net=candidate;added++;}}
        if(!repellent&&count(REPELLENT)>0&&ProductionBatch.exchange(p,new int[][]{{REPELLENT,1}},new int[0][])){repellent=true;added++;}
        int amount=Math.min(1000-jars,count(11260));if(amount>0&&ProductionBatch.exchange(p,new int[][]{{11260,amount}},new int[0][])){jars+=amount;added+=amount;}
        p.sendMessage("Elnock stores "+jars+" impling jars, "+(net==0?"no net":"one net")+" and "+(repellent?"one imp repellent":"no imp repellent")+". Unequip a net before storing it.");return added;
    }
    public int withdraw(){
        if(!near())return 0;close();int taken=0;
        if(net!=0&&ProductionBatch.exchange(p,new int[0][],new int[][]{{net,1}})){net=0;taken++;}
        if(repellent&&ProductionBatch.exchange(p,new int[0][],new int[][]{{REPELLENT,1}})){repellent=false;taken++;}
        while(jars>0&&ProductionBatch.exchange(p,new int[0][],new int[][]{{11260,1}})){jars--;taken++;}
        p.sendMessage("You withdraw "+taken+" item(s); "+jars+" jars remain safely stored with Elnock.");return taken;
    }
    public boolean generator(int option){
        if(!ready()||count(GENERATOR)!=1||option<1||option>3)return false;
        if(option==3){p.sendMessage("Your jar generator has "+charges+"% charge remaining.");return true;}
        int cost=option==1?3:1;if(charges<cost){p.sendMessage("Your jar generator needs "+cost+"% charge for that jar.");return true;}
        int[][] input=charges==cost?new int[][]{{GENERATOR,1}}:new int[0][];
        if(ProductionBatch.exchange(p,input,new int[][]{{option==1?11260:10012,1}})){charges-=cost;if(charges==0)p.sendMessage("Your jar generator runs out of charge and disappears.");}return true;
    }
    public boolean item(int id,int option){
        if(id==GENERATOR){generator(option);return true;}
        if(id!=SCROLL||option!=1)return false;if(!ready()||count(SCROLL)==0)return true;
        if(!Impling.puro(p.absX,p.absY,p.heightLevel)){p.sendMessage("The impling scroll can only be used in Puro-Puro.");return true;}
        if(p.getOpenInterface()==ImplingJournal.ROOT){close();return true;}
        close();p.getPA().sendFrame126("Impling scroll",64803);p.getPA().sendFrame126("Carried jars",64806);p.getPA().sendFrame126("",64807);
        for(ImplingData d:ImplingData.values()){p.getPA().sendFrame126(d==ImplingData.CRYSTAL?"-":Integer.toString(count(d.jar)),64811+d.ordinal()*3);p.getPA().sendFrame126("",64812+d.ordinal()*3);}
        p.getPA().sendFrame126("Filled impling jars currently in your inventory.",64850);p.getPA().showInterface(ImplingJournal.ROOT);return true;
    }
    public boolean npc(NPC npc,int option){
        if(npc==null||npc.getNpcId()!=NPC_ID)return false;if(!near()||npc.getX()!=2586||npc.getY()!=4314)return true;
        if(option==4){withdraw();return true;}if(option==2){tradeMenu();return true;}if(option==3){storageMenu();return true;}
        if(option==1){if(!starter)starter();mainMenu();}return true;
    }
    private void mainMenu(){if(!near())return;new DialogueBuilder(p).option("Elnock Inquisitor",new DialogueOption("Trade implings for equipment",q->q.getElnock().tradeMenu()),new DialogueOption("Store or withdraw equipment",q->q.getElnock().storageMenu()),new DialogueOption("Supplies and magical wheat",q->q.getElnock().suppliesMenu()),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();}
    private void tradeMenu(){if(!near())return;new DialogueBuilder(p).option("Elnock's Exchange",new DialogueOption("Magic net: 3 gourmet, 2 earth, 1 essence",q->q.getElnock().exchange(11259)),new DialogueOption("Repellent: 3 baby, 2 young, 1 gourmet",q->q.getElnock().exchange(REPELLENT)),new DialogueOption("Jar generator: 3 essence, 2 eclectic, 1 nature",q->q.getElnock().generatorMenu()),new DialogueOption("One filled impling jar for three empty jars",q->q.getElnock().jarMenu(0)),new DialogueOption("Back",q->q.getElnock().mainMenu())).send();}
    private void generatorMenu(){if(!near())return;new DialogueBuilder(p).option("Jar generator - 3 essence, 2 eclectic, 1 nature",new DialogueOption("Obtain jar generator",q->q.getElnock().exchange(GENERATOR)),new DialogueOption("Recharge current generator",q->q.getElnock().recharge()),new DialogueOption("Back",q->q.getElnock().tradeMenu())).send();}
    private void jarMenu(int page){if(!near())return;List<DialogueOption> options=new ArrayList<>();ImplingData[] all=ImplingData.values();int start=page*3;for(int i=start;i<Math.min(start+3,all.length);i++){ImplingData d=all[i];options.add(new DialogueOption(d.name+" jar",q->q.getElnock().emptyJars(d.jar)));}options.add(new DialogueOption("More",q->q.getElnock().jarMenu((page+1)%4)));options.add(new DialogueOption("Back",q->q.getElnock().tradeMenu()));new DialogueBuilder(p).option("Trade one filled jar for three empty jars",options.toArray(new DialogueOption[0])).send();}
    private void storageMenu(){if(!near())return;new DialogueBuilder(p).option("Elnock's storage - "+jars+"/1000 jars",new DialogueOption("Store equipment and empty jars",q->q.getElnock().deposit()),new DialogueOption("Withdraw stored equipment",q->q.getElnock().withdraw()),new DialogueOption("Back",q->q.getElnock().mainMenu())).send();}
    private void suppliesMenu(){if(!near())return;new DialogueBuilder(p).option("Elnock's supplies",new DialogueOption("Buy jars: 2000 coins each, 10 per day",q->q.getElnock().buyMenu()),new DialogueOption("Replace impling scroll",q->q.getElnock().scroll()),new DialogueOption("Toggle Strength XP from magical wheat",q->q.getElnock().toggleStrength()),new DialogueOption("Starting equipment",q->q.getElnock().starter()),new DialogueOption("Back",q->q.getElnock().mainMenu())).send();}
    private void buyMenu(){if(!near())return;new DialogueBuilder(p).option("Daily impling jars: "+(10-purchasedToday())+" remaining",new DialogueOption("Buy 1: 2000 coins",q->q.getElnock().buyJars(1)),new DialogueOption("Buy 5: 10000 coins",q->q.getElnock().buyJars(5)),new DialogueOption("Buy 10: 20000 coins",q->q.getElnock().buyJars(10)),new DialogueOption("Back",q->q.getElnock().suppliesMenu())).send();}
}
