package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.construction.*;import io.xeros.content.skills.prayer.Bone;import io.xeros.model.collisionmap.*;import io.xeros.model.entity.player.*;import java.lang.reflect.*;import java.util.*;import java.util.function.IntUnaryOperator;
public final class HouseChapelCheck {
 static final Set<Integer> GROUPS=Set.of(15269,15270,15271,13733,15272,15275,15276);
 static final String[] ALTARS={"OAK_ALTAR","TEAK_ALTAR","CLOTH_ALTAR","MAHOGANY_ALTAR","LIMESTONE_ALTAR","MARBLE_ALTAR","GILDED_ALTAR"};
 static final int[] MULTIPLIERS={100,110,125,150,175,200,250};
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}static void tick(int n){SkillsAuditCheck.tick(n);}static void bag(Player p){HouseWorkshopCheck.bag(p);}static void give(Player p,int id,int n){HouseWorkshopCheck.give(p,id,n);}static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static Player house(){Player p=HouseConstructionCheck.house();give(p,995,50000);ok(p.getHouse().addRoom(PlayerHouse.Kind.CHAPEL,3,2,0),"Chapel level45 price50000");p.getNextPlayerMovement();bag(p);p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static List<WorldObject> objects(Player p){List<WorldObject> list=new ArrayList<>();for(WorldObject o:p.getRegionProvider().get(8000,8000).worldObjectsSnapshot())if(o.height==1&&o.x>=8024&&o.x<8032&&o.y>=8016&&o.y<8024)list.add(o);return list;}
 static WorldObject model(Player p,int id){return objects(p).stream().filter(o->o.id==id).findFirst().orElseThrow(()->new AssertionError("Chapel model "+id));}
 static WorldObject root(Player p,int group){return objects(p).stream().filter(o->GROUPS.contains(HouseFurnishing.group(o.id))&&HouseFurnishing.group(o.id)==group).findFirst().orElseThrow(()->new AssertionError("Chapel root "+group));}
 static void at(Player p,WorldObject o){HouseConstructionCheck.at(p,o.x-1,o.y,1);}
 static void build(Player p,HouseFurnishing f){bag(p);give(p,2347,1);give(p,8794,1);for(int[] in:f.materials)give(p,in[0],in[1]);HouseConstructionCheck.select(p,root(p,f.group),f);tick(5);}
 static int countModel(Player p,int id){return (int)objects(p).stream().filter(o->o.id==id).count();}
 static long xp(Player p,int skill){return p.playerXP[skill]*10L+p.playerXpTenths[skill];}
 static void recipes(){
  Player p=house();int tested=0;
  for(HouseFurnishing f:HouseFurnishing.all())if(GROUPS.contains(f.group)){
   bag(p);give(p,2347,1);give(p,8794,1);for(int[] in:f.materials)give(p,in[0],in[1]);WorldObject o=root(p,f.group);int before=p.playerXP[22];HouseConstructionCheck.select(p,o,f);tick(4);ok(p.playerXP[22]==before,"Premature furniture XP "+f.key);for(int[] in:f.materials)ok(count(p,in[0])==in[1],"Premature materials");tick(1);ok(p.playerXP[22]==before+(int)f.xp,"Five-tick XP "+f.key);for(int[] in:f.materials)ok(count(p,in[0])==0,"Consumed exact recipe "+f.key);
   int expected=f.group==13733?f.key.equals("SHUTTERED_WINDOW")?13253:f.key.equals("DECORATIVE_WINDOW")?13258:13259:f.group==15272?f.models[o.id-15272]:f.models[0];WorldObject built=model(p,expected);int parts=f.group==13733?6:f.group==15271||f.group==15275?2:1;ok(countModel(p,expected)>=parts,"All native parts rendered "+f.key);
   at(p,built);ok(p.getDynamicMapScene().handleClickObject(p,built,4),"Remove "+f.key);HouseConstructionCheck.choice(p,0);ok(!p.getHouse().encode().contains(f.group+"="),"Removed canonical slot "+f.key);tested++;
  }
  ok(tested==34&&HouseFurnishing.all().size()==207,"34 additional recipes");ok(HouseFurnishing.get("STEEL_TORCHES").xp==40&&Arrays.deepEquals(HouseFurnishing.get("GILDED_ALTAR").materials,new int[][]{{8786,2},{8790,2},{8784,4}}),"Literal primary corrections");ok(HouseFurnishing.get("GNOME_CHILD_ICON").level==45&&HouseFurnishing.get("GNOME_CHILD_ICON").xp==100&&Arrays.deepEquals(HouseFurnishing.get("GNOME_CHILD_ICON").materials,new int[][]{{8778,3},{25338,1}}),"2021 Gnome recipe");p.getHouse().exit();
  System.out.println("HOUSE_CHAPEL_ALL34_ACTUAL_NATIVE_FURNITURE_RECIPES_FIVE_TICKS_EXACT_MATERIALS_XP_SIX_WINDOWS_TWO_LAMPS_TWO_STATUES_RUG_PARTS_REMOVE_GILDED_FOUR_GOLD_LEAF_STEEL_TORCH40_GNOME2021_OK");
 }
 static Player fixture(String altar,String lamp,String icon,String extra,int rotation){
  Player p=MiningGuildCheck.actor(2953,3224);p.getHealth().setMaximumHealth(99);p.getHealth().reset();String layout="1|1|2,2,GARDEN,0,15361=EXIT_PORTAL|2,3,PARLOUR,0|3,2,CHAPEL,"+rotation+(altar==null?"":",15270="+altar)+(lamp==null?"":",15271="+lamp)+(icon==null?"":",15269="+icon)+(extra==null?"":extra);ok(p.getHouse().decode(layout)&&p.getHouse().enter(false),"Fixture decode/enter");p.getNextPlayerMovement();return p;
 }
 static void styles(){
  String[] icons={null,"SARADOMIN_SYMBOL","GUTHIX_SYMBOL","ZAMORAK_SYMBOL","BOB_ICON","GNOME_CHILD_ICON"};
  int[] windows={13258,13258,13256,13260,13254,40899},statues={13273,13273,13276,13279,13282,40917};
  int[][] altarIds={{13179,13179,13181,13180,13179,40872},{13182,13182,13184,13183,13182,40873},{13185,13185,13186,13187,13185,40874},{13188,13188,13190,13189,13188,40875},{13191,13191,13193,13192,13191,40876},{13194,13194,13196,13195,13194,40877},{13197,13197,13199,13198,13197,40878}};
  int tests=0;for(int a=0;a<7;a++)for(int style=0;style<icons.length;style++){
   Player p=fixture(ALTARS[a],null,icons[style],",13733=DECORATIVE_WINDOW,15275=LARGE_STATUE",0);ok(countModel(p,altarIds[a][style])==1&&countModel(p,windows[style])==6&&countModel(p,statues[style])==2,"Explicit historical themed variants "+a+"/"+style);String saved=p.getHouse().encode();Player q=MiningGuildCheck.actor(2953,3224);ok(q.getHouse().decode(saved)&&q.getHouse().encode().equals(saved),"Themed layout save");p.getHouse().exit();tests++;
  }
  ok(tests==42,"Seven altars six alignments incl default");System.out.println("HOUSE_CHAPEL_42_NATIVE_ALTAR_ICON_ALIGNMENTS_SARADOMIN_DEFAULT_BOB_SHARED_ALTAR_GUTHIX_ZAMORAK_GNOME2021_SIX_WINDOWS_TWO_STATUES_AND_LAYOUT_SAVES_OK");
 }
 static void light(Player p,int x){WorldObject o=objects(p).stream().filter(v->v.id>=13208&&v.id<=13213&&v.x==x).findFirst().orElseThrow();at(p,o);ok(p.getDynamicMapScene().handleClickObject(p,o,1),"Light route");tick(1);}
 static void multipliers(){
  int tests=0;for(int a=0;a<7;a++)for(int lamps=0;lamps<=2;lamps++){
   Player p=fixture(ALTARS[a],"INCENSE_BURNER",null,null,0);give(p,590,1);if(lamps>0)give(p,251,lamps);for(int l=0;l<lamps;l++)light(p,8024+(l==0?1:6));WorldObject altar=model(p,HouseFurnishing.get(ALTARS[a]).models[0]);at(p,altar);
   for(Bone bone:Bone.values()){
    long before=xp(p,5),con=xp(p,22);give(p,bone.getItemId(),1);ok(p.getHouse().chapel().use(bone.getItemId(),altar),"Bone use route");ok(count(p,bone.getItemId())==1&&xp(p,5)==before,"No premature offering");tick(1);long amount=(long)Math.floor((bone.getExperience()*(MULTIPLIERS[a]+50*lamps)/100d)*10+1e-8);ok(count(p,bone.getItemId())==0&&xp(p,5)==before+amount&&xp(p,22)==con,"Actual bone XP fraction "+a+"/"+lamps+"/"+bone);tests++;
   }p.getHouse().exit();
  }
  ok(tests==7*3*Bone.values().length,"All table entries");System.out.println("HOUSE_CHAPEL_"+tests+"_ACTUAL_BONE_OFFERINGS_SEVEN_ALTARS_ZERO_ONE_TWO_LIT_BURNERS_EXACT_PERCENTAGES_FRACTIONAL_PRAYER_XP_NO_CONSTRUCTION_XP_OK");
 }
 static Object call(Object o,String name,Class<?>[] types,Object... args)throws Exception{Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(o,args);}
 static void lampsAndGuards()throws Exception{
  int combinations=0;for(int level:new int[]{30,50,85,99})for(int roll=0;roll<level;roll++){ok(HouseChapel.duration(level,roll)==200+level+roll,"Published 2018 formula");combinations++;}ok(combinations==264,"Duration rolls");
  Player p=fixture("GILDED_ALTAR","INCENSE_BURNER",null,null,0);give(p,590,1);give(p,251,4);p.playerLevel[11]=30;long fm=xp(p,11);light(p,8025);ok(countModel(p,13209)==1&&count(p,251)==3&&xp(p,11)==fm,"First independent burner no FM XP");tick(10);light(p,8030);ok(countModel(p,13209)==2&&count(p,251)==2,"Second independent burner");tick(219);ok(countModel(p,13208)==1&&countModel(p,13209)==1,"First burner expires before second");tick(11);ok(countModel(p,13208)==2,"Second burner expires at own duration");
  light(p,8025);tick(100);light(p,8025);tick(130);ok(countModel(p,13209)==1&&count(p,251)==0,"Relight resets expiry and consumes another herb");tick(100);ok(countModel(p,13209)==0,"Relight expires once");
  give(p,251,1);p.playerLevel[11]=29;WorldObject lamp=model(p,13208);at(p,lamp);p.getDynamicMapScene().handleClickObject(p,lamp,1);tick(1);ok(count(p,251)==1&&countModel(p,13209)==0,"FM29 denied");p.playerLevel[11]=30;light(p,8025);ok(countModel(p,13209)==1,"FM30 permitted");p.getHouse().command("build");p.getNextPlayerMovement();ok(countModel(p,13209)==1,"Building-mode rebuild preserves burner");for(int r=1;r<4;r++){ok(p.getHouse().rotateRoom(3,2,r),"Rotate chapel");p.getNextPlayerMovement();ok(countModel(p,13209)==1&&countModel(p,13208)==1,"Independent identity through rotation "+r);}String saved=p.getHouse().encode();p.getHouse().exit();p.getNextPlayerMovement();ok(p.getHouse().decode(saved)&&p.getHouse().enter(false),"Reentry");p.getNextPlayerMovement();ok(countModel(p,13209)==0&&countModel(p,13208)==2,"Exit extinguishes all burners");p.getHouse().exit();
  p=fixture("GILDED_ALTAR",null,null,null,0);WorldObject altar=model(p,13197);at(p,altar);p.playerLevel[5]=0;long before=xp(p,5);p.getDynamicMapScene().handleClickObject(p,altar,1);ok(p.playerLevel[5]==99&&xp(p,5)==before,"Prayer restores no XP");p.playerLevel[5]=104;p.getDynamicMapScene().handleClickObject(p,altar,1);ok(p.playerLevel[5]==104,"No drain overheal");
  give(p,537,1);p.getHouse().chapel().use(537,altar);tick(1);ok(count(p,537)==1&&xp(p,5)==before,"Noted bones denied");give(p,25769,1);p.getHouse().chapel().use(25769,altar);tick(1);ok(count(p,25769)==1,"Ashes denied");give(p,536,3);p.getHouse().chapel().use(536,altar);tick(1);ok(count(p,536)==2&&xp(p,5)==before+1800,"Gilded unlit dragon180");tick(2);ok(count(p,536)==2,"Automatic cadence no early extra");tick(1);ok(count(p,536)==1,"Automatic three ticks");p.absX--;tick(3);ok(count(p,536)==1,"Movement cancels remaining bone");p.getHouse().exit();
  System.out.println("HOUSE_CHAPEL_264_PUBLISHED_DURATION_ROLLS_INDEPENDENT_EXPIRY_RELIGHT_FM30_HERB_CONSUMPTION_NO_FM_XP_ROTATIONS_MODE_PRESERVE_EXIT_CLEANUP_PRAYER_RESTORE_OVERHEAL_NOTES_ASHES_AND_THREE_TICK_CANCEL_GUARDS_OK");
 }
 public static void main(String[] args){Field rng=null;Object old=null;try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());rng=HouseChapel.class.getDeclaredField("random");rng.setAccessible(true);old=rng.get(null);rng.set(null,(IntUnaryOperator)(bound->0));recipes();styles();multipliers();lampsAndGuards();System.out.println("HOUSE_CHAPEL_ALL_CHECKS_PASSED");rng.set(null,old);System.exit(0);}catch(Throwable t){try{if(rng!=null&&old!=null)rng.set(null,old);}catch(Exception ignored){}t.printStackTrace();System.exit(1);}}
}
