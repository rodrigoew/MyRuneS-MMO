package io.xeros.content.combat.magic;

import io.xeros.Server;
import io.xeros.content.combat.Hitmark;
import io.xeros.content.dialogue.*;
import io.xeros.content.instances.DynamicMapScene;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.bank.*;
import java.util.*;

/** Owner-only projections of the native arena. Currency belongs to the character, not the scene. */
public final class MageTrainingArena {
 public static final int BASE=8832;
 public static final Position LOBBY=new Position(3363,3317,0);
 private static final int[] LEVELS={33,21,7,15},OVERLAYS={15962,15892,15917,15931};
 private static final int[] SHAPES={6899,6898,6900,6901},SPELLS={1155,1165,1176,1180,1187,6003,23649},ENCHANT_LEVELS={7,27,49,57,68,87,93};
 private static final double[] ENCHANT_XP={14,28,45,50.5,58.5,72,87.5};
 private static final int[][][] ENCHANT_RUNES={{{564,1},{555,1}},{{564,1},{556,3}},{{564,1},{554,5}},{{564,1},{557,10}},{{564,1},{555,15},{557,15}},{{564,1},{554,20},{557,20}},{{564,1},{565,20},{566,20}}};
 private static final int[] TEMP={6893,6894,6895,6896,6897,6898,6899,6900,6901,6902,6903,6904,6905,6906,6907};
 private static final int[] GRAVE_RUNES={555,557,561,560,565};
 private final Player p;
 private DynamicMapScene scene;
 private int room=-1,rotation,bonusShape,freeAlch=-1,graveFruit,clock,nextDamage=6,spellDelay,collectDelay;
 private int[] values={1,5,8,15,30};
 private final Map<String,Integer> piles=new HashMap<>();
 private static final int[][] STONES={{26,45},{25,35},{31,32},{47,33},{46,43},{45,51}};
 private final int[] stoneRespawn=new int[STONES.length];
 private long owedCoins;
 private MtaMaze maze;
 private NPC rewardsGuardian;
 private boolean changing;
 public MageTrainingArena(Player p){this.p=p;}
 public boolean inside(){return scene!=null&&scene.activeFor(p);}
 public int room(){return inside()?room:-1;}
 public DynamicMapScene scene(){return scene;}
 public MtaMaze maze(){return maze;}
 public NPC rewardsGuardian(){return rewardsGuardian;}
 public void rewardsGuardian(NPC guardian){rewardsGuardian=guardian;}
 private boolean ready(){return !p.isDead&&p.getHealth().getCurrentHealth()>0&&!p.isDisconnected()&&p.getSession()!=null&&!p.getLock().cannotInteract(p)&&!p.isBanking&&!p.isShopping&&p.teleTimer==0&&!Server.getMultiplayerSessionListener().inAnySession(p);}
 private boolean hat(){for(int id:new int[]{6885,6886,6887})if(p.getItems().playerHasItem(id)||p.playerEquipment[0]==id)return true;return false;}
 public boolean enter(int type){
  if(type<0||type>3||!ready()||p.getInstance()!=null)return false;
  if(!hat()){p.sendMessage("Ask the Entrance Guardian for a progress hat first.");return false;}
  if(p.playerLevel[6]<LEVELS[type]){p.sendMessage("You need Magic level "+LEVELS[type]+" to enter this room.");return false;}
  if(type==1&&hasCoins()){p.sendMessage("Bank your coins and coin containers before entering the Alchemist's Playground.");return false;}
  if(type==3&&(p.getItems().playerHasItem(1963)||p.getItems().playerHasItem(6883))){p.sendMessage("Leave your bananas and peaches outside the Creature Graveyard.");return false;}
  room=type;clock=0;nextDamage=6;graveFruit=0;spellDelay=0;collectDelay=0;piles.clear();
  if(type==0){maze=new MtaMaze(this,p);maze.start();}else openOrdinary();
  final DynamicMapScene visit=scene;
  Server.getEventHandler().submit(new Event<Player>("mage-training-visit",p,1){
   public void execute(){if(!inside()||scene!=visit){stop();return;}pulse();}
  });
  return inside();
 }
 private boolean hasCoins(){
  if(p.getItems().playerHasItem(995))return true;
  // Pouches, caskets and money sacks must not introduce outside currency into this room.
  for(int i=0;i<p.playerItems.length;i++)if(p.playerItems[i]>0){String name=io.xeros.model.definitions.ItemDef.forId(p.playerItems[i]-1).getName().toLowerCase(java.util.Locale.ROOT);if(name.contains("coin pouch")||name.contains("coin bag")||name.contains("money bag")||name.contains("casket"))return true;}
  return false;
 }
 private void openOrdinary(){
  int plane=room==1?2:room==3?1:0;
  DynamicMapScene next=new DynamicMapScene(p,BASE,BASE);
  for(int x=0;x<8;x++)for(int y=0;y<8;y++)next.placeBaseMap(x,y,0,new DynamicMapScene.Chunk(416+x,1200+y,plane,0));
  setScene(next);
  int sx=room==1?38:35,sy=room==1?23:room==2?49:39;
  next.activate(new Position(BASE+sx,BASE+sy,0));
  NPCSpawning.spawnNpc(p,room==1?5980:room==2?5981:5982,BASE+36,BASE+(room==1?27:room==2?47:36),0,0,-1,false,false);
  if(room==1)rotateAlch();if(room==2){rotateBonus();Arrays.fill(stoneRespawn,0);for(int i=0;i<STONES.length;i++)spawnStone(i);}refresh();
 }
 void setScene(DynamicMapScene next){
  scene=next;next.onClick((o,option)->click(o,option));
  next.onClose(()->{if(scene==next&&!changing){settleAndClean();scene=null;maze=null;room=-1;p.getPA().resetCamera();p.getPA().walkableInterface(-1);}});
 }
 void replaceMaze(DynamicMapScene next){
  changing=true;try{if(scene!=null)scene.remove(p);}finally{changing=false;}
  setScene(next);
 }
 public boolean allowsTeleport(){return room()!=3;}
 public boolean rescueLethal(int damage){
  if(room()!=3||damage<p.getHealth().getCurrentHealth()||damage<=0)return false;
  rescue();return true;
 }
 private void rescue(){p.getMageTrainingProgress().penalize(3,10);p.getHealth().setCurrentHealth(p.getPA().getLevelForXP(p.playerXP[3]));leave();p.sendMessage("The guardian rescues you. You lose 10 Graveyard points, but keep your equipment.");}
 private void pulse(){
  clock++;if(spellDelay>0)spellDelay--;if(collectDelay>0)collectDelay--;
  if(room==1&&clock%70==0)rotateAlch();if(room==2&&clock%40==0)rotateBonus();
  if(room==2)for(int i=0;i<STONES.length;i++){
   int[] pos=STONES[i];boolean present=Server.itemHandler.getGroundItem(p,6903,BASE+pos[0],BASE+pos[1],0)!=null;
   if(!present){if(stoneRespawn[i]==0)stoneRespawn[i]=clock+700+io.xeros.util.Misc.random(100);else if(clock>=stoneRespawn[i]){spawnStone(i);stoneRespawn[i]=0;}}
  }
  // Historical damage is two; the legacy variable cadence remains separately uncertified.
  if(room==3&&clock>=nextDamage){nextDamage=clock+6+io.xeros.util.Misc.random(6);p.appendDamage(2,Hitmark.HIT);}
 }
 private void spawnStone(int i){int[] pos=STONES[i];Server.itemHandler.createPrivateTimedGroundItem(p,new io.xeros.model.items.GameItem(6903,1),new Position(BASE+pos[0],BASE+pos[1],0),Integer.MAX_VALUE-1);}
 private void rotateAlch(){
  rotation=io.xeros.util.Misc.random(7);List<Integer> prices=new ArrayList<>(List.of(1,5,8,15,30));Collections.shuffle(prices);for(int i=0;i<5;i++)values[i]=prices.get(i);
  freeAlch=io.xeros.util.Misc.random(2)==0?io.xeros.util.Misc.random(4):-1;refresh();
 }
 private void rotateBonus(){int previous=bonusShape;do{bonusShape=io.xeros.util.Misc.random(3);}while(bonusShape==previous);refresh();}
 public void refresh(){
  if(!inside())return;var pa=p.getPA();pa.walkableInterface(OVERLAYS[room]);
  pa.sendString(""+p.getMageTrainingProgress().points(room),room==0?15966:room==1?15896:room==2?15921:15935);
  if(room==0)pa.sendString(""+p.getMageTrainingProgress().streak(),15968);
  if(room==1)for(int i=0;i<5;i++){pa.sendString(""+values[i],15902+i);pa.sendInterfaceHidden(15907+i,i!=freeAlch);}
  if(room==2){pa.sendString("Bonus:",15930);int display=new int[]{0,1,3,2}[bonusShape];for(int i=0;i<4;i++)pa.sendInterfaceHidden(15922+i,i!=display);}
 }
 public boolean click(WorldObject o,int option){
  if(!inside()||o.height!=p.heightLevel||Math.max(Math.abs(p.absX-o.x),Math.abs(p.absY-o.y))>3)return false;
  if(o.id==23677){leave();return true;}
  if(option!=1)return false;
  if(room==1){
   if(o.id==10734){depositAlch();return true;}
   int[] cupboards={23678,23680,23682,23684,23686,23688,23690,23692};
   for(int i=0;i<8;i++)if(o.id==cupboards[i]){if(collectDelay>0)return true;int item=(i-rotation+8)%8;if(item<5){if(ProductionBatch.exchange(p,new int[0][],new int[][]{{6893+item,1}}))collectDelay=1;}else p.sendMessage("The cupboard is empty.");return true;}
  }
  if(room==2){
   if(o.id==23698){depositOrbs();return true;}
   for(int i=0;i<4;i++)if(o.id==23694+i){if(collectDelay==0&&ProductionBatch.exchange(p,new int[0][],new int[][]{{SHAPES[i],1}}))collectDelay=1;return true;}
  }
  if(room==3){
   if(o.id==10735){depositFruit();return true;}
   if(o.id>=10725&&o.id<=10728){if(collectDelay>0)return true;String key=o.x+":"+o.y;int n=piles.getOrDefault(key,0);if(ProductionBatch.exchange(p,new int[0][],new int[][]{{6904+n/4,1}})){piles.put(key,(n+1)%16);collectDelay=1;}return true;}
  }
  return false;
 }
 /** Intercepts every item spell inside the activity, so temporary goods never reach ordinary alchemy. */
 public boolean itemSpell(int id,int slot,int widget){
  if(!inside())return false;
  if(!ready()||spellDelay>0||slot<0||slot>=28||p.playerItems[slot]!=id+1||p.playerMagicBook!=0)return true;
  if(room==1&&(widget==1162||widget==1178)&&id>=6893&&id<=6897){
   boolean high=widget==1178;if(p.playerLevel[6]<(high?55:21))return true;
   List<int[]> cost=new ArrayList<>();cost.add(new int[]{id,1});if(id-6893!=freeAlch){cost.add(new int[]{561,1});cost.add(new int[]{554,high?5:3});}
   if(RunePayment.exchange(p,cost.toArray(new int[0][]),new int[][]{{995,values[id-6893]}})){castAnimation(high?713:712,high?113:112,high?65:31,5);if(p.getItems().getInventoryCount(995)>=10000)p.sendMessage("Warning: depositing more than 12,000 coins wastes the entire deposit.");}return true;
  }
  if(room==2){int tier=-1;for(int i=0;i<SPELLS.length;i++)if(widget==SPELLS[i])tier=i;
   if(tier>=0&&(id>=6898&&id<=6901||id==6903)&&p.playerLevel[6]>=ENCHANT_LEVELS[tier]){
    if(enchantOne(id,tier,true)&&p.getItems().getInventoryCount(id)>0)autoEnchant(id,tier);return true;
   }
  }
  p.sendMessage("That spell cannot be used on this item in this training room.");return true;
 }
 private boolean enchantOne(int id,int tier,boolean manual){
  List<int[]> cost=new ArrayList<>();cost.add(new int[]{id,1});Collections.addAll(cost,ENCHANT_RUNES[tier]);
  if(!RunePayment.exchange(p,cost.toArray(new int[0][]),new int[][]{{6902,1}}))return false;
  p.getMageTrainingProgress().enchant(tier+1,id==6903,id==SHAPES[bonusShape]);
  if(manual){p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();}
  p.startAnimation(tier<2?719:tier<4?720:721);p.gfx100(tier<2?114:tier<4?115:tier==4?116:452);p.getPA().addSkillXPMultiplied(ENCHANT_XP[tier],6,true);spellDelay=3;refresh();return true;
 }
 /** Same-type automatic enchanting was added in 2019; the 2022 spell interval is seven ticks. */
 private void autoEnchant(int id,int tier){
  final DynamicMapScene visit=scene;final long token=p.productionActionToken;final int x=p.absX,y=p.absY,h=p.heightLevel;
  Server.getEventHandler().submit(new Event<Player>("mta-auto-enchant",p,7){
   public void execute(){
    if(token!=p.productionActionToken||scene!=visit||room()!=2||!ready()||p.playerMagicBook!=0||p.playerLevel[6]<ENCHANT_LEVELS[tier]||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()||spellDelay>0||p.getItems().getInventoryCount(id)==0){stop();return;}
    if(!enchantOne(id,tier,false)||p.getItems().getInventoryCount(id)==0)stop();
   }
  });
 }
 public boolean boneSpell(int action){
  if(room()!=3)return false;
  if(action==8014||action==8015){p.sendMessage("Use the bone conversion spells in this room; tablets do not count.");return true;}
  if(action!=4135&&action!=62005)return false;
  boolean peaches=action==62005;if(!ready()||spellDelay>0)return true;
  if(p.playerMagicBook!=0||p.playerLevel[6]<(peaches?60:15)){p.sendMessage("Use the normal spellbook with the required Magic level.");return true;}
  if(peaches&&!p.getMageTrainingProgress().peachesUnlocked()){p.sendMessage("Unlock Bones to Peaches at the Rewards Guardian first.");return true;}
  List<int[]> cost=new ArrayList<>();int count=0;for(int i=0;i<4;i++){int n=p.getItems().getInventoryCount(6904+i);if(n>0){cost.add(new int[]{6904+i,n});count+=n*(i+1);}}
  if(count==0){p.sendMessage("Collect some animals' bones from a bone pile.");return true;}
  cost.add(new int[]{561,peaches?2:1});cost.add(new int[]{555,peaches?4:2});cost.add(new int[]{557,peaches?4:2});
  // The historical room discards fruit that cannot fit after the bones are consumed.
  int[][] inputs=cost.toArray(new int[0][]);count=Math.min(count,28);
  if(!RunePayment.canExchange(p,inputs,new int[0][])){p.sendMessage("You need the required runes to convert these bones.");return true;}
  // Exhausted inventory rune stacks free slots too; pouch/staff runes do not.
  while(count>0&&!RunePayment.canExchange(p,inputs,new int[][]{{peaches?6883:1963,count}}))count--;
  if(count>0&&RunePayment.exchange(p,inputs,new int[][]{{peaches?6883:1963,count}}))castAnimation(722,141,peaches?35.5:25,1);
  return true;
 }
 private void castAnimation(int animation,int gfx,double xp,int ticks){p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.startAnimation(animation);p.gfx100(gfx);p.getPA().addSkillXPMultiplied(xp,6,true);spellDelay=ticks;}
 private void depositOrbs(){int n=p.getItems().getInventoryCount(6902);if(n==0)return;int sets=n/20;int[][] output=sets==0?new int[0][]:new int[][]{{new int[]{560,565,564}[io.xeros.util.Misc.random(2)],sets*3}};if(ProductionBatch.exchange(p,new int[][]{{6902,n}},output))p.sendMessage("You deposit "+n+" orbs.");}
 private void depositFruit(){
  int bananas=p.getItems().getInventoryCount(1963),peaches=p.getItems().getInventoryCount(6883),n=bananas+peaches;if(n==0)return;
  int sets=(graveFruit+n)/16;List<int[]> cost=new ArrayList<>();if(bananas>0)cost.add(new int[]{1963,bananas});if(peaches>0)cost.add(new int[]{6883,peaches});
  if(ProductionBatch.exchange(p,cost.toArray(new int[0][]),sets==0?new int[0][]:new int[][]{{GRAVE_RUNES[io.xeros.util.Misc.random(4)],sets}})){graveFruit=(graveFruit+n)%16;p.getMageTrainingProgress().award(3,sets);refresh();}
 }
 private void depositAlch(){
  int coins=p.getItems().getInventoryCount(995);if(coins<=0||owedCoins>Long.MAX_VALUE-(coins/100)*10L)return;if(!ProductionBatch.exchange(p,new int[][]{{995,coins}},new int[0][]))return;
  if(coins>12000){p.sendMessage("The collector rejects your excessive deposit; all those coins are lost.");return;}
  p.getMageTrainingProgress().award(1,coins/100);owedCoins+=(coins/100)*10L;p.getPA().addSkillXPMultiplied(coins*2.0,6,true);refresh();
 }
 public long owedCoins(){return owedCoins;}
 public void claimCoins(){
  if(owedCoins<=0)return;int amount=(int)Math.min(owedCoins,Integer.MAX_VALUE);
  if(!p.getMode().isBankingPermitted()){if(ProductionBatch.exchange(p,new int[0][],new int[][]{{995,amount}}))owedCoins-=amount;return;}
  BankItem coin=new BankItem(996,amount);BankTab target=p.getBank().getBankTab()[0];for(BankTab tab:p.getBank().getBankTab())if(tab.contains(coin)){target=tab;break;}
  if(!p.getBank().hasRoomFor(coin)||!target.spaceAvailable(coin))return;target.add(coin);owedCoins-=amount;
 }
 private void settleAndClean(){if(room==1)depositAlch();for(int id:TEMP)p.getItems().deleteItem2(id,p.getItems().getInventoryCount(id));if(room==3)for(int id:new int[]{1963,6883})p.getItems().deleteItem2(id,p.getItems().getInventoryCount(id));claimCoins();}
 public void leave(){if(!inside())return;p.interruptActions();p.getPA().stopSkilling();p.getPA().resetCamera();DynamicMapScene old=scene;old.remove(p);p.moveTo(LOBBY);p.getPA().walkableInterface(-1);}
 public void logout(){if(inside())leave();}
 public String encodeVisit(){return "1|"+(inside()?room:-1)+"|"+owedCoins;}
 public boolean decodeVisit(String text){try{if(text==null||text.length()>60)return false;String[] f=text.split("\\|",-1);if(f.length!=3||!f[0].equals("1"))return false;int r=Integer.parseInt(f[1]);long owed=Long.parseLong(f[2]);if(r< -1||r>3||owed<0)return false;room=r;owedCoins=owed;return true;}catch(RuntimeException e){return false;}}
 public void safeLogin(){
  if(!inside()&&(room>=0||canvas(p.absX,p.absY)||canvas(p.getTeleportToX(),p.getTeleportToY()))){settleAndClean();room=-1;p.moveTo(LOBBY);}claimCoins();p.getPA().sendConfig(1987,p.getMageTrainingProgress().peachesUnlocked()?1:0);
 }
 private static boolean canvas(int x,int y){return x>=BASE-16&&x<BASE+88&&y>=BASE-16&&y<BASE+88;}
 private boolean npcReady(NPC n){return n!=null&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&!n.isUnregister()&&n.heightLevel==p.heightLevel&&n.getInstance()==p.getInstance()&&n.distance(p.getPosition())<=3&&ready();}
 public boolean npc(NPC n,int option){
  if(!npcReady(n))return false;
  if(n.getNpcId()>=5978&&n.getNpcId()<=5984&&n.getNpcId()!=5983)claimCoins();
  if(maze!=null&&maze.npc(n,option))return true;
  if(n.getNpcId()==5978&&option==1){new DialogueBuilder(p).option("Mage Training Arena",new DialogueOption("Get a progress hat",q->{q.getPA().removeAllWindows();if(npcReady(n)&&!hat())ProductionBatch.exchange(q,new int[0][],new int[][]{{6885,1}});}),new DialogueOption("View my points",q->{if(npcReady(n))pointsDialogue();}),new DialogueOption("Buy an arena book (200 coins)",q->{q.getPA().removeAllWindows();if(npcReady(n))ProductionBatch.exchange(q,new int[][]{{995,200}},new int[][]{{6891,1}});}),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return true;}
  if(n.getNpcId()==5984&&(option==1||option==4)){MtaRewards.open(p,n);return true;}
  if(inside()&&n.getNpcId()>=5979&&n.getNpcId()<=5982&&option==1){new DialogueBuilder(p).option("Training room",new DialogueOption("How to play",q->{if(npcReady(n))tutorial();}),new DialogueOption("My pizazz points",q->{if(npcReady(n))pointsDialogue();}),new DialogueOption("Leave this room",q->{q.getPA().removeAllWindows();if(npcReady(n))leave();})).send();return true;}return false;
 }
 private void tutorial(){
  String[][] text={
   {"Cast Telekinetic Grab on the statue from a maze edge.","The statue moves toward your side until a wall stops it.","Guide it onto the goal, then speak to the guardian.","Five solved mazes grant extra points, law runes and XP."},
   {"Search the cupboards for items to turn into coins.","The displayed prices change every 42 seconds.","A green arrow marks an item that costs no runes to alch.","Deposit coins before you exceed 12,000 in one deposit."},
   {"Take shapes and use your jewellery enchantment spells.","The shape shown by Bonus gives extra points.","You can cast each spell yourself or let it repeat slowly.","You may deposit 20 orbs for three bonus runes."},
   {"Collect animals' bones; the four types yield 1 to 4 fruit.","Convert them with Bones to Bananas or Bones to Peaches.","Deposit 16 fruit for each point, and eat some to heal.","Use the centre exit to leave; teleports cannot work here."}
  };if(inside())new DialogueBuilder(p).statement(text[room]).send();
 }
 private void pointsDialogue(){new DialogueBuilder(p).statement("Telekinetic: "+p.getMageTrainingProgress().points(0)+"    Alchemist: "+p.getMageTrainingProgress().points(1),"Enchantment: "+p.getMageTrainingProgress().points(2)+"    Graveyard: "+p.getMageTrainingProgress().points(3),p.getMageTrainingProgress().peachesUnlocked()?"Bones to Peaches: unlocked":"Bones to Peaches: not yet unlocked").send();}
 public boolean npcSpell(NPC n,int widget){return inside()&&room==0&&maze!=null&&maze.cast(n,widget);}
 public static boolean lobbyObject(Player p,int id,int x,int y){
  if(p.heightLevel>1||x<3350||x>3378||y<3290||y>3335)return false;
  if(id==10721&&p.heightLevel==0){p.moveTo(new Position(3363,p.absY<3300?3301:3298,0));return true;}
  if(id>=23673&&id<=23676){if(p.heightLevel!=0)return true;int[] types={0,2,1,3};p.getMageTrainingArena().enter(types[id-23673]);return true;}
  if(id==10771||id==10775){p.moveTo(new Position(x,y+1,1));return true;}
  if(id==10773||id==10776){p.moveTo(new Position(x+1,y-1,0));return true;}return false;
 }
}
