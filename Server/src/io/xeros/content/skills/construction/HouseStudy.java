package io.xeros.content.skills.construction;
import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.shooting_star.ShootingStarsWorld;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;

/** Owner-bound Study interactions; all recipes retain their facility until completion. */
public final class HouseStudy {
 public static final class Recipe {
  public final int output,level,ticks,rank,branch;public final double xp;public final String unlock;public final int[][] materials;
  Recipe(int output,int level,double xp,int ticks,int rank,int branch,String unlock,int[][] materials){this.output=output;this.level=level;this.xp=xp;this.ticks=ticks;this.rank=rank;this.branch=branch;this.unlock=unlock;this.materials=materials;}
 }
 private static final List<Recipe> RECIPES=Collections.unmodifiableList(Arrays.asList(
  new Recipe(8007,25,35.0,4,0,0,"",new int[][]{{563,1},{556,3},{554,1},{1761,1}}),
  new Recipe(8008,31,41.0,4,1,1,"",new int[][]{{563,1},{556,3},{557,1},{1761,1}}),
  new Recipe(8009,37,48.0,4,1,1,"",new int[][]{{563,1},{556,3},{555,1},{1761,1}}),
  new Recipe(8010,45,55.5,4,2,1,"",new int[][]{{563,1},{556,5},{1761,1}}),
  new Recipe(8011,51,61.0,4,2,1,"Plague City",new int[][]{{563,2},{555,2},{1761,1}}),
  new Recipe(8012,58,68.0,4,3,1,"Watchtower",new int[][]{{563,2},{557,2},{1761,1}}),
  new Recipe(8013,40,30.0,4,3,1,"",new int[][]{{563,1},{556,1},{557,1},{1761,1}}),
  new Recipe(8014,15,25.0,2,1,2,"",new int[][]{{561,1},{557,2},{555,2},{1761,1}}),
  new Recipe(8015,60,35.5,2,3,2,"Mage Training Arena: Bones to Peaches",new int[][]{{561,2},{557,4},{555,4},{1761,1}}),
  new Recipe(8016,7,17.5,2,0,0,"",new int[][]{{564,1},{555,1},{1761,1}}),
  new Recipe(8017,27,27.8,2,1,2,"",new int[][]{{564,1},{556,3},{1761,1}}),
  new Recipe(8018,49,44.5,2,2,2,"",new int[][]{{564,1},{554,5},{1761,1}}),
  new Recipe(8019,57,67.0,3,2,2,"",new int[][]{{564,1},{557,10},{1761,1}}),
  new Recipe(8020,68,78.0,2,3,2,"",new int[][]{{564,1},{557,15},{555,15},{1761,1}}),
  new Recipe(8021,87,73.0,4,3,2,"",new int[][]{{564,1},{557,20},{554,20},{1761,1}})
 ));
 private static final int[][] STAFFS={{1381,1383,1385,1387},{1397,1395,1399,1393},{1405,1403,1407,1401}};
 private static final int[] RUNES={556,555,557,554};
 private static final String[] ELEMENTS={"Air","Water","Earth","Fire"};
 private final PlayerHouse house;private final Player p;
 public HouseStudy(PlayerHouse house,Player p){this.house=house;this.p=p;}
 public static List<Recipe> recipes(){return RECIPES;}
 private boolean live(PlayerHouse.Binding b){return b!=null&&b.room.kind==PlayerHouse.Kind.STUDY&&house.furnitureReady(b)&&p.getHealth().getCurrentHealth()>0&&!p.isDisconnected()&&p.getSession()!=null;}
 private boolean supports(PlayerHouse.Binding b,Recipe r){
  if(!live(b)||house.furnishing(b).group!=15420)return false;String key=house.furnishing(b).key;if(key.equals("MARBLE_LECTERN"))return true;
  int rank=key.equals("OAK_LECTERN")?0:key.startsWith("MAHOGANY")?3:key.startsWith("TEAK")?2:1;
  return r.rank<=rank&&(r.branch==0||r.branch==1&&key.contains("EAGLE")||r.branch==2&&key.contains("DEMON"));
 }
 private boolean unlocked(Recipe r){
  if(r.unlock.isEmpty())return true;
  if(r.output==8015)return p.getMageTrainingProgress().peachesUnlocked();
  return p.getQuesting().getQuestList().stream().anyMatch(q->q.getName().equals(r.unlock)&&q.isQuestCompleted());
 }
 public boolean click(WorldObject o,int option){
  PlayerHouse.Binding b=house.furnitureFixture(o);if(b==null||b.room.kind!=PlayerHouse.Kind.STUDY)return false;
  if(option!=1)return false;if(!live(b))return true;
  int group=house.furnishing(b).group;
  if(group==15420){p.interruptActions();p.getPA().stopSkilling();menu(b,0);return true;}
  if(group==15424){p.interruptActions();p.getPA().stopSkilling();p.startAnimation(3650);String key=house.furnishing(b).key;int minutes=key.equals("WOODEN_TELESCOPE")?24:key.equals("TEAK_TELESCOPE")?9:2;String prediction=ShootingStarsWorld.INSTANCE.forecast(minutes);new DialogueBuilder(p).statement("A shooting star is expected in:",ShootingStarsWorld.INSTANCE.plannedSite().region,prediction.substring(prediction.indexOf("Estimated arrival:"))).send();return true;}
  return false;
 }
 public boolean use(int item,WorldObject o){
  PlayerHouse.Binding b=house.furnitureFixture(o);if(b==null||b.room.kind!=PlayerHouse.Kind.STUDY)return false;
  int group=house.furnishing(b).group;
  if(group==15420&&item==1761){if(live(b)){p.interruptActions();p.getPA().stopSkilling();menu(b,0);}return true;}
  if(group!=15422)return false;
  for(int tier=0;tier<STAFFS.length;tier++)for(int id:STAFFS[tier])if(item==id){if(live(b)&&p.getItems().playerHasItem(item))staffMenu(b,item,tier);return true;}
  p.sendMessage("Use a basic elemental, battle or mystic staff on this crystal.");return true;
 }
 private void staffMenu(PlayerHouse.Binding b,int item,int tier){
  int max=house.furnishing(b).key.equals("CRYSTAL_BALL")?0:house.furnishing(b).key.equals("ELEMENTAL_SPHERE")?1:2;
  if(tier>max){p.sendMessage("You need a higher-tier crystal to change this staff.");return;}
  p.interruptActions();p.getPA().stopSkilling();List<DialogueOption> options=new ArrayList<>();
  for(int i=0;i<4;i++){final int element=i;if(STAFFS[tier][i]==item)continue;options.add(new DialogueOption(ELEMENTS[i]+(tier==0?" (free)":" ("+(tier==1?100:1000)+" runes)"),q->{
   q.getPA().removeAllWindows();if(!live(b))return;int cost=tier==0?0:tier==1?100:1000;
   List<int[]> inputs=new ArrayList<>();inputs.add(new int[]{item,1});if(cost>0)inputs.add(new int[]{RUNES[element],cost});
   if(ProductionBatch.exchange(p,inputs.toArray(new int[0][]),new int[][]{{STAFFS[tier][element],1}})){p.startAnimation(898);p.sendMessage("You change the elemental alignment of your staff.");}
  }));}
  options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));new DialogueBuilder(p).option("Change staff element",options.toArray(new DialogueOption[0])).send();
 }
 private void menu(PlayerHouse.Binding b,int page){
  if(!live(b))return;List<Recipe> rows=new ArrayList<>();for(Recipe r:RECIPES)if(supports(b,r))rows.add(r);List<DialogueOption> options=new ArrayList<>();
  for(int i=page*3;i<Math.min(rows.size(),page*3+3);i++){Recipe r=rows.get(i);options.add(new DialogueOption(ItemAssistant.getItemName(r.output)+" (Magic "+r.level+")",q->quantity(b,r)));}
  if(page*3+3<rows.size())options.add(new DialogueOption("More",q->menu(b,page+1)));else options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
  new DialogueBuilder(p).option("Create magic tablets",options.toArray(new DialogueOption[0])).send();
 }
 private void quantity(PlayerHouse.Binding b,Recipe r){if(!supports(b,r))return;new DialogueBuilder(p).option("How many?",new DialogueOption("Make 1",q->start(b,r,1)),new DialogueOption("Make 5",q->start(b,r,5)),new DialogueOption("Make All",q->start(b,r,10000))).send();}
 private boolean start(PlayerHouse.Binding b,Recipe r,int quantity){
  p.getPA().removeAllWindows();if(quantity<1||!RECIPES.contains(r)||!supports(b,r))return false;
  if(p.playerLevel[6]<r.level){p.sendMessage("You need Magic level "+r.level+".");return false;}
  if(!unlocked(r)){p.sendMessage("This tablet requires "+r.unlock+".");return false;}
  p.interruptActions();p.getPA().stopSkilling();long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;int delay=quantity==1?r.ticks:Math.max(4,r.ticks);p.playerSkilling[6]=true;p.startAnimation(p.playerEquipment[3]>0?4068:4067);
  Server.getEventHandler().submit(new Event<Player>("skilling",p,delay){int remaining=Math.min(quantity,10000);
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[6]||!supports(b,r)||!unlocked(r)||p.playerLevel[6]<r.level||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()){stop();return;}
    if(!StudyRuneExchange.exchange(p,r.materials,r.output)){p.sendMessage("You need soft clay, the required runes and inventory space.");stop();return;}
    p.getPA().addSkillXPMultiplied(r.xp,6,true);p.startAnimation(p.playerEquipment[3]>0?4068:4067);if(--remaining==0)stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[6]=false;p.startAnimation(65535);}}
  });return true;
 }
 public boolean legacyTablet(int index,int quantity){
  if(index<0||index>1||quantity<1||quantity>10000)return false;
  Recipe recipe=RECIPES.get(index==0?7:8);
  for(WorldObject object:house.nearbyFurniture()){
   PlayerHouse.Binding b=house.furnitureFixture(object);if(b!=null&&supports(b,recipe))return start(b,recipe,quantity);
  }
  p.sendMessage("Use an appropriate lectern in your house to create tablets.");return false;
 }
}
