package io.xeros.content.combat.magic;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;

/** 2022 reward costs, ordered Telekinetic / Alchemist / Enchantment / Graveyard. */
public final class MtaRewards {
 private static final class Reward {
  final int id,previous;final int[] cost;
  Reward(int id,int previous,int t,int a,int e,int g){this.id=id;this.previous=previous;cost=new int[]{t,a,e,g};}
 }
 private static final Reward[] REWARDS={
  new Reward(6916,0,350,400,3000,350),new Reward(6918,0,400,450,4000,400),new Reward(6920,0,450,500,5000,450),new Reward(6922,0,175,225,1500,175),new Reward(6924,0,120,120,1200,120),
  new Reward(6908,0,30,30,300,30),new Reward(6910,6908,60,60,600,60),new Reward(6912,6910,150,200,1500,150),new Reward(6914,6912,240,240,2400,240),new Reward(6889,0,500,550,6000,500),new Reward(-1,0,200,300,2000,200),
  new Reward(4695,0,1,1,15,1),new Reward(4696,0,1,1,15,1),new Reward(4698,0,1,1,15,1),new Reward(4697,0,1,1,15,1),new Reward(4694,0,1,1,15,1),new Reward(4699,0,1,1,15,1),
  new Reward(564,0,0,0,5,0),new Reward(562,0,0,1,5,1),new Reward(561,0,0,1,0,1),new Reward(560,0,2,1,20,1),new Reward(563,0,2,0,0,0),new Reward(566,0,2,2,25,2),new Reward(565,0,2,2,25,2)
 };
 private static final int[] SLOTS={5,6,7,8,0,1,2,3,4,9,10,11,12,14,13,15,16,17,18,19,20,21,22,23};
 private static boolean live(Player p,NPC n){return n!=null&&!n.isUnregister()&&n.getIndex()>0&&io.xeros.model.entity.npc.NPCHandler.npcs[n.getIndex()]==n&&n.getNpcId()==5984&&n.heightLevel==p.heightLevel&&n.getInstance()==p.getInstance()&&n.distance(p.getPosition())<=3&&!p.isDead&&!p.isDisconnected()&&p.getSession()!=null&&!p.getLock().cannotInteract(p)&&!p.isBanking&&!p.isShopping&&p.teleTimer==0;}
 private static String name(Reward r){return r.id<0?"Bones to Peaches unlock":ItemAssistant.getItemName(r.id);}
 public static void open(Player p,NPC guardian){
  if(!live(p,guardian))return;p.getPA().removeAllWindows();p.getMageTrainingArena().rewardsGuardian(guardian);
  List<io.xeros.model.items.GameItem> stock=new ArrayList<>();for(int index:SLOTS){Reward r=REWARDS[index];stock.add(new io.xeros.model.items.GameItem(r.id<0?6926:r.id,1));}
  io.xeros.model.entity.player.PlayerAssistant.sendItems(p,15948,stock,24);
  int[] rooms={0,2,1,3};for(int i=0;i<4;i++)p.getPA().sendString(""+p.getMageTrainingProgress().points(rooms[i]),15955+i);
  p.getPA().showInterface(15944);
 }
 public static boolean container(Player p,int component,int slot,int item,int option){
  if(component!=15948)return false;
  NPC guardian=p.getMageTrainingArena().rewardsGuardian();if(p.getOpenInterface()!=15944||slot<0||slot>=SLOTS.length||!live(p,guardian)||p.getBankPin().requiresUnlock())return true;
  int index=SLOTS[slot];Reward r=REWARDS[index];if(item!=(r.id<0?6926:r.id))return true;
  if(option==1){p.sendMessage(name(r)+": Telekinetic "+r.cost[0]+", Alchemist "+r.cost[1]+", Enchantment "+r.cost[2]+", Graveyard "+r.cost[3]+" points."+(r.previous>0?" Requires "+ItemAssistant.getItemName(r.previous)+".":""));return true;}
  if(option==2){buy(p,guardian,index);open(p,guardian);}return true;
 }
 public static void menu(Player p,NPC n,int page){
  if(!live(p,n)||page<0||page*3>=REWARDS.length)return;List<DialogueOption> options=new ArrayList<>();
  for(int i=page*3;i<Math.min(page*3+3,REWARDS.length);i++){final int index=i;options.add(new DialogueOption(name(REWARDS[i]),q->confirm(q,n,index,page)));}
  if((page+1)*3<REWARDS.length)options.add(new DialogueOption("More rewards",q->menu(q,n,page+1)));else options.add(new DialogueOption("First page",q->menu(q,n,0)));
  options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));new DialogueBuilder(p).option("Mage Training Arena rewards",options.toArray(new DialogueOption[0])).send();
 }
 private static void confirm(Player p,NPC n,int index,int page){
  if(!live(p,n))return;Reward r=REWARDS[index];String cost="T "+r.cost[0]+" / A "+r.cost[1]+" / E "+r.cost[2]+" / G "+r.cost[3];
  new DialogueBuilder(p).option(name(r)+": "+cost,new DialogueOption("Buy"+(r.previous>0?" (exchange "+ItemAssistant.getItemName(r.previous)+")":""),q->{q.getPA().removeAllWindows();buy(q,n,index);}),new DialogueOption("Back",q->menu(q,n,page))).send();
 }
 public static boolean buy(Player p,NPC n,int index){
  if(index<0||index>=REWARDS.length||!live(p,n))return false;Reward r=REWARDS[index];MageTrainingProgress progress=p.getMageTrainingProgress();
  if(r.id<0){if(progress.unlockPeaches()){p.sendMessage("You unlock Bones to Peaches.");return true;}p.sendMessage("You need Magic level 60, the four point costs and an unlearned spell.");return false;}
  if(!progress.canAfford(r.cost)){p.sendMessage("You do not have enough pizazz points.");return false;}
  if(!ProductionBatch.exchange(p,r.previous>0?new int[][]{{r.previous,1}}:new int[0][],new int[][]{{r.id,1}})){p.sendMessage("You need inventory space and the previous wand for an upgrade.");return false;}
  progress.spend(r.cost);p.sendMessage("You receive "+name(r)+".");return true;
 }
}
