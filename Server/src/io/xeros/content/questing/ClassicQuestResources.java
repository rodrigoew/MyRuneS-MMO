package io.xeros.content.questing;
import io.xeros.Server;import io.xeros.content.dialogue.*;import io.xeros.content.skills.ProductionBatch;import io.xeros.content.skills.crafting.SpinMaterial;import io.xeros.model.collisionmap.WorldObject;import io.xeros.model.entity.player.*;import io.xeros.model.entity.npc.*;import io.xeros.model.cycleevent.Event;import io.xeros.model.entity.player.save.PlayerSave;import io.xeros.model.items.GameItem;import java.util.*;
/** Normal world resources used by the Lumbridge campaigns, not quest item grants. */
public final class ClassicQuestResources {
 private final Player p;private int hopper,flour;private boolean minas;private int ticks,flourSent=-1,altarSent=-1;private final Map<Integer,Integer> respawns=new HashMap<>();
 public ClassicQuestResources(Player p){this.p=p;}
 private boolean ready(){return !p.isDead&&!p.isDisconnected()&&p.getHealth().getCurrentHealth()>0&&p.getInstance()==null&&!p.getLock().cannotInteract(p)&&!p.getBankPin().requiresUnlock();}
 private boolean near(int x,int y,int h){return ready()&&p.heightLevel==h&&Math.max(Math.abs(p.absX-x),Math.abs(p.absY-y))<=3;}
 private boolean npcNear(NPC n){return n!=null&&!n.isUnregister()&&!n.isDead()&&!n.needRespawn&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&near(n.absX,n.absY,n.heightLevel);}
 private boolean actual(int id,int x,int y){for(WorldObject o:p.getRegionProvider().get(x,y).worldObjectsSnapshot())if(o.id==id&&o.x==x&&o.y==y&&o.height==p.heightLevel)return true;return false;}
 private Quest quest(String name){for(Quest q:p.getQuesting().getQuestList())if(q.getName().equals(name))return q;throw new IllegalStateException(name);}
 private void save(){PlayerSave.saveGame(p);}
 public String encode(){return hopper+","+flour+","+(minas?1:0);}
 public void restore(String s){try{String[] a=s.split(",");if(a.length!=3)return;int h=Integer.parseInt(a[0]),f=Integer.parseInt(a[1]);if(h<0||h>1||f<0||f>30)return;hopper=h;flour=f;minas=a[2].equals("1");flourSent=altarSent=-1;}catch(NumberFormatException ignored){}}
 public void process(){ticks++;int f=flour>0?1:0,a=quest("The Restless Ghost").getStage()>=4?4:0;if(flourSent!=f){p.getPA().sendFrame87Masked(695,1,f);flourSent=f;}if(altarSent!=a){p.getPA().sendFrame87Masked(728,4,a);altarSent=a;}
 if(!ready()||p.heightLevel!=0)return;int[][] supplies={{1944,3177,3296},{1931,3209,3214},{1925,3211,3211}};for(int[] v:supplies)if(Math.max(Math.abs(p.absX-v[1]),Math.abs(p.absY-v[2]))<=16&&ticks>=respawns.getOrDefault(v[0],0)&&Server.itemHandler.getGroundItem(p,v[0],v[1],v[2],0)==null){Server.itemHandler.createPrivateTimedGroundItem(p,new GameItem(v[0],1),new Position(v[1],v[2],0),1000);respawns.put(v[0],ticks+50);}}
 private void milk(int x,int y){if(!p.getItems().playerHasItem(1925)){p.sendMessage("Voce precisa de um Bucket vazio para ordenhar.");return;}ProductionBatch.start(p,7,1,0,1,2305,3,new int[][]{{1925,1}},new int[][]{{1927,1}},()->p.sendMessage("Voce enche o Bucket com leite."),()->near(x,y,0));}
 private void shear(NPC n){if(!npcNear(n)||n.getNpcId()!=2693)return;if(!p.getItems().playerHasItem(1735)){p.sendMessage("Voce precisa de Shears. Fred the Farmer pode fornecer uma.");return;}
 ProductionBatch.start(p,12,1,0,1,893,2,new int[][]{{1735,0}},new int[][]{{1737,1}},()->{n.requestTransform(2691);p.sendMessage("Voce tosquia a ovelha e recebe Wool.");Server.getEventHandler().submit(new Event<NPC>("wool_regrowth",n,100){public void execute(){if(!attachment.isUnregister()&&!attachment.isDead()&&!attachment.needRespawn)attachment.requestTransform(2693);stop();}});},()->npcNear(n)&&n.getNpcId()==2693);}
 public boolean npc(NPC n,int option){if(n.getNpcId()==2693){if(option==1)shear(n);return true;}if(n.getNpcId()==2691)return true;
 if(n.getNpcId()==4627&&Math.abs(n.absX-3165)<8&&Math.abs(n.absY-3305)<8){if(option==1&&npcNear(n))ClassicQuest.narration(p,4627,"Colha Grain no campo. Suba as duas escadas do moinho.","Use Grain no Hopper e acione Hopper controls.","Desca e use um Pot no Flour bin para recolher a farinha.").send();return true;}
 if(n.getNpcId()==1902&&Math.abs(n.absX-3257)<8&&Math.abs(n.absY-3447)<8){if(option!=1||!npcNear(n))return true;if(!quest("Rune Mysteries").isQuestCompleted())ClassicQuest.narration(p,1902,"Volte quando tiver descobertas historicas para compartilhar.").send();else if(minas)ClassicQuest.narration(p,1902,"Ja registrei sua descoberta das runas: 5 Kudos.").send();else ClassicQuest.narration(p,1902,"Como voce ajudou os magos a redescobrir a essencia das runas?").option(new DialogueOption("Contar a descoberta de Rune Mysteries.",q->{if(npcNear(n)&&quest("Rune Mysteries").isQuestCompleted()&&!minas){minas=true;save();p.sendMessage("Historian Minas registra sua descoberta: +5 Kudos (total desta descoberta: 5).");}}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();return true;}return false;}
 public boolean itemOnNpc(NPC n,int item,int slot){if(item!=1735||(n.getNpcId()!=2693&&n.getNpcId()!=2691))return false;if(slot>=0&&slot<p.playerItems.length&&p.playerItems[slot]==item+1)shear(n);return true;}
 private void climb(WorldObject o,boolean up,int tx,int ty){if(!near(o.x,o.y,o.height))return;ProductionBatch.start(p,16,1,0,1,up?828:827,2,new int[0][],new int[0][],()->p.getPA().movePlayer(tx,ty,up?o.height+1:o.height-1));}
 private void middle(WorldObject o,int option,int tx,int ty){if(option==2)climb(o,true,tx,ty);else if(option==3)climb(o,false,tx,ty);else if(option==1&&near(o.x,o.y,o.height))new DialogueBuilder(p).option(new DialogueOption("Subir.",q->climb(o,true,tx,ty)),new DialogueOption("Descer.",q->climb(o,false,tx,ty)),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();}
 public boolean object(WorldObject o,int option){
 if(o.id==8689){if(option==1&&near(o.x,o.y,o.height)&&o.height==0)milk(o.x,o.y);return option==1;}
 if(o.id==15506||o.id==15507||o.id==15508){if(option==1&&near(o.x,o.y,o.height))ProductionBatch.start(p,19,1,0,1,827,2,new int[0][],new int[][]{{1947,1}},()->p.sendMessage("Voce colhe Grain."),()->near(o.x,o.y,o.height));return true;}
 if(o.id==12964&&o.x==3164&&o.y==3307&&o.height==0){if(option==1)climb(o,true,3165,3307);return true;}
 if(o.id==12965&&o.x==3164&&o.y==3307&&o.height==1){middle(o,option,3165,3307);return true;}
 if(o.id==12966&&o.x==3164&&o.y==3307&&o.height==2){if(option==1)climb(o,false,3165,3307);return true;}
 if(o.id==16671&&o.x==3204&&(o.y==3207||o.y==3229)&&o.height==0){if(option==1)climb(o,true,3205,o.y+1);return true;}
 if(o.id==16672&&o.x==3204&&(o.y==3207||o.y==3229)&&o.height==1){middle(o,option,3205,o.y+1);return true;}
 if(o.id==16673&&o.x==3205&&(o.y==3208||o.y==3229)&&o.height==2){if(option==1)climb(o,false,3205,o.y);return true;}
 if(o.id==2147&&o.x==3103&&o.y==3176&&o.height==0){if(option==1&&near(o.x,o.y,0))p.climbLadderTo(new Position(3104,9576,0));return true;}
 if(o.id==2148&&o.x==3103&&o.y==9576&&o.height==0){if(option==1&&near(o.x,o.y,0))p.climbLadderTo(new Position(3104,3176,0));return true;}
 if(o.id==24964&&o.x==3166&&o.y==3305&&o.height==2){if(option==1&&near(o.x,o.y,2)){if(hopper==0)p.sendMessage("Primeiro coloque Grain no Hopper.");else if(flour>=30)p.sendMessage("Esvazie o Flour bin antes de moer mais.");else{hopper=0;flour++;process();save();p.startAnimation(832);p.sendMessage("O moinho transforma o Grain em farinha. Recolha-a no terreo.");}}return true;}
 if(o.id==24961&&o.x==3166&&o.y==3307&&o.height==2){if(option==1)use(1947,o.id,o.x,o.y);return true;}
 if((o.id==1781||o.id==1782||o.id==5792)&&o.x==3166&&o.y==3306&&o.height==0){if(option==1)takeFlour(o.x,o.y);return true;}
 if(o.id==14889&&near(o.x,o.y,o.height)&&(option==1||option==2)){if(p.getItems().playerHasItem(1737))SpinMaterial.getInstance().spin(p,1737);else p.sendMessage("Use Wool nesta Spinning wheel.");return true;}
 return false;}
 private void takeFlour(int x,int y){if(!near(x,y,0))return;if(flour<=0){p.sendMessage("O Flour bin esta vazio. Coloque Grain no Hopper e acione os controles.");return;}if(!ProductionBatch.exchange(p,new int[][]{{1931,1}},new int[][]{{1933,1}}))return;flour--;process();save();p.sendMessage("Voce enche um Pot com farinha.");}
 public boolean use(int item,int object,int x,int y){if(object==8689&&item==1925){if(near(x,y,0)&&actual(object,x,y))milk(x,y);return true;}
 if(object==24961&&x==3166&&y==3307){if(item!=1947)return false;if(!near(x,y,2))return true;if(hopper!=0){p.sendMessage("Acione os controles antes de colocar outro Grain.");return true;}if(ProductionBatch.exchange(p,new int[][]{{1947,1}},new int[0][])){hopper=1;save();p.sendMessage("Voce coloca Grain no Hopper. Agora acione Hopper controls.");}return true;}
 if((object==1781||object==1782||object==5792)&&x==3166&&y==3306&&item==1931){takeFlour(x,y);return true;}
 if(object==14889&&item==1737){if(near(x,y,p.heightLevel)&&actual(object,x,y))SpinMaterial.getInstance().spin(p,item);return true;}return false;}
}
