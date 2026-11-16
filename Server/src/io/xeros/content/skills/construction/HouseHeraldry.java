package io.xeros.content.skills.construction;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import static io.xeros.content.skills.construction.HouseHeraldryData.*;

/** A permanent personal crest and owner-scoped six-tick heraldic painting. */
public final class HouseHeraldry {
 private final PlayerHouse house;private final Player p;private Crest crest;
 public HouseHeraldry(PlayerHouse house,Player p){this.house=house;this.p=p;}
 public Crest crest(){return crest;}
 public String encode(){return crest==null?"NONE":crest.name();}
 public boolean decode(String value){
  if("NONE".equals(value)){crest=null;return true;}
  try{Crest parsed=Crest.valueOf(value);crest=parsed;return true;}catch(IllegalArgumentException|NullPointerException e){return false;}
 }
 private boolean quest(String name){return p.getQuesting().getQuestList().stream().anyMatch(q->q.getName().equalsIgnoreCase(name)&&q.isQuestCompleted());}
 public boolean eligible(Crest c){
  if(c==null)return false;
  switch(c){
   case ARRAV:return quest("Shield of Arrav");case DORGESHUUN:return quest("The Lost Tribe");
   case DRAGON:return quest("Dragon Slayer I");case FAIRY:return quest("Lost City");
   case GUTHIX:case SARADOMIN:case ZAMORAK:return p.getLevelForXP(p.playerXP[5])>=70;
   case HORSE:for(int id:new int[]{2520,2522,2524,2526})if(p.getItems().playerHasItem(id))return true;return false;
   case SKULL:return p.skullTimer>0;
   default:return true;
  }
 }
 private boolean near(NPC npc){return npc!=null&&npc.getNpcId()==3100&&ConstructionWorld.near(p,npc)&&p.getHealth().getCurrentHealth()>0&&p.teleTimer==0;}
 private boolean service(NPC npc){
  if(!near(npc))return false;
  if(p.playerLevel[22]<16){p.sendMessage("You need Construction level 16 to discuss your family crest.");return false;}return true;
 }
 public void talk(NPC npc){
  if(!service(npc))return;
  if(crest==null){
   new DialogueBuilder(p).option("Discover your family crest?",new DialogueOption("Discover my crest (free)",q->assign(npc)),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();
  }else new DialogueBuilder(p).option("Your crest: "+crest.label,new DialogueOption("Change my crest",q->menu(npc,0)),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();
 }
 private void assign(NPC npc){
  p.getPA().removeAllWindows();if(!service(npc)||crest!=null)return;
  // The historical reference specifies a random first assignment, but not its distribution.
  List<Crest> choices=new ArrayList<>();for(Crest c:Crest.values())if(c!=Crest.MONEY&&eligible(c))choices.add(c);
  crest=choices.get(ThreadLocalRandom.current().nextInt(choices.size()));p.sendMessage("Your family crest is "+crest.label+".");
 }
 private void menu(NPC npc,int page){
  if(!service(npc)||crest==null||page<0||page>5)return;
  List<DialogueOption> rows=new ArrayList<>();Crest[] crests=Crest.values();
  for(int i=page*3;i<Math.min(crests.length,page*3+3);i++){Crest c=crests[i];rows.add(new DialogueOption(c.label+" ("+(c==Crest.MONEY?"500,000":"5,000")+" coins)",q->confirm(npc,c)));}
  if(page*3+3<crests.length)rows.add(new DialogueOption("More",q->menu(npc,page+1)));
  else rows.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
  new DialogueBuilder(p).option("Choose your family crest",rows.toArray(new DialogueOption[0])).send();
 }
 private void confirm(NPC npc,Crest c){
  if(!service(npc))return;
  if(!eligible(c)){p.getPA().removeAllWindows();p.sendMessage("You do not meet the requirements for the "+c.label+" crest.");return;}
  new DialogueBuilder(p).option("Change to "+c.label+"?",new DialogueOption("Pay "+(c==Crest.MONEY?"500,000":"5,000")+" coins",q->change(npc,c)),new DialogueOption("Cancel",q->q.getPA().removeAllWindows())).send();
 }
 private boolean change(NPC npc,Crest c){
  p.getPA().removeAllWindows();if(!service(npc)||crest==null||!eligible(c))return false;
  int cost=c==Crest.MONEY?500000:5000;
  if(!ProductionBatch.exchange(p,new int[][]{{995,cost}},new int[0][])){p.sendMessage("You do not have enough coins.");return false;}
  crest=c;p.sendMessage("Your family crest is now "+crest.label+".");return true;
 }
 private int tier(PlayerHouse.Binding b){
  HouseFurnishing f=house.furnishing(b);if(f==null||f.group!=15450)return -1;
  switch(f.key){case "PLUMING_STAND":return 0;case "SHIELD_EASEL":return 1;case "BANNER_EASEL":return 2;default:return -1;}
 }
 public boolean click(PlayerHouse.Binding b,int option){if(option!=1||tier(b)<0)return false;menu(b,0,-1);return true;}
 public boolean use(int item,WorldObject object){
  if(!house.inside())return false;PlayerHouse.Binding b=house.furnitureFixture(object);if(b==null||tier(b)<0)return false;
  if(HouseHeraldryData.get(item)!=null){p.sendMessage("A painted helm or shield cannot be repainted.");return true;}
  menu(b,0,item);return true;
 }
 private void menu(PlayerHouse.Binding b,int page,int input){
  if(!house.furnitureReady(b))return;
  if(crest==null){p.sendMessage("Speak to Sir Renitee in Falador to discover your family crest.");return;}
  List<Product> products=new ArrayList<>();
  for(Product r:all())if(r.crest==crest&&r.kind<=tier(b)&&(input==-1||r.base==input||(r.kind==2&&(input==8790||input==960))))products.add(r);
  if(products.isEmpty()){p.sendMessage("Bring an unpainted full helm, kiteshield, or banner materials.");return;}
  if(page<0||page*3>=products.size())return;
  List<DialogueOption> rows=new ArrayList<>();for(int i=page*3;i<Math.min(products.size(),page*3+3);i++){Product r=products.get(i);rows.add(new DialogueOption(ItemAssistant.getItemName(r.item),q->start(b,r)));}
  if(page*3+3<products.size())rows.add(new DialogueOption("More",q->menu(b,page+1,input)));else rows.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
  new DialogueBuilder(p).option("Paint your "+crest.label+" crest",rows.toArray(new DialogueOption[0])).send();
 }
 private boolean requirements(Product r){return p.playerLevel[12]>=r.level&&p.getLevelForXP(p.playerXP[22])>=r.constructionLevel;}
 private boolean start(PlayerHouse.Binding b,Product r){
  p.getPA().removeAllWindows();
  if(!all().contains(r)||crest!=r.crest||!house.furnitureReady(b)||r.kind>tier(b)||p.getHealth().getCurrentHealth()<=0||p.teleTimer>0)return false;
  if(!requirements(r)){p.sendMessage("You need Crafting level "+r.level+" and base Construction level "+r.constructionLevel+".");return false;}
  p.interruptActions();p.getPA().stopSkilling();long token=p.productionActionToken;String key=house.furnishing(b).key;int x=p.absX,y=p.absY,h=p.heightLevel;p.playerSkilling[12]=true;
  // Painting duration is specified by the historical recipes; animation 898 is a visual approximation.
  p.startAnimation(898);
  Server.getEventHandler().submit(new Event<Player>("skilling",p,r.ticks){
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[12]||crest!=r.crest||!house.furnitureReady(b)||!key.equals(house.furnishing(b).key)||r.kind>tier(b)||!requirements(r)||p.isDead||p.getHealth().getCurrentHealth()<=0||p.isDisconnected()||p.getSession()==null||p.teleTimer>0||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()){stop();return;}
    if(ProductionBatch.exchange(p,r.materials,new int[][]{{r.item,1}})){p.getPA().addSkillXPMultiplied(r.xp,12,true);p.sendMessage("You paint your "+r.crest.label+" crest.");}stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[12]=false;p.startAnimation(65535);}}
  });return true;
 }
}
