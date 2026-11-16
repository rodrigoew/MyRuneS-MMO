package io.xeros.content.combat.range;
import io.xeros.model.*;import io.xeros.model.entity.*;import io.xeros.model.entity.player.*;import io.xeros.content.combat.*;import io.xeros.content.combat.specials.Special;import io.xeros.content.combat.specials.impl.*;import java.util.*;
/** 2022 bolt effects: select once at launch, apply side effects once on a valid impact. */
public final class EnchantedBolts {
 private EnchantedBolts(){}
 public static final int[][] IDS={{Items.OPAL_BOLTS_E,Items.OPAL_DRAGON_BOLTS_E},{Items.JADE_BOLTS_E,Items.JADE_DRAGON_BOLTS_E},{Items.PEARL_BOLTS_E,Items.PEARL_DRAGON_BOLTS_E},{Items.TOPAZ_BOLTS_E,Items.TOPAZ_DRAGON_BOLTS_E},{Items.SAPPHIRE_BOLTS_E,Items.SAPPHIRE_DRAGON_BOLTS_E},{Items.EMERALD_BOLTS_E,Items.EMERALD_DRAGON_BOLTS_E},{Items.RUBY_BOLTS_E,Items.RUBY_DRAGON_BOLTS_E},{Items.DIAMOND_BOLTS_E,Items.DIAMOND_DRAGON_BOLTS_E},{Items.DRAGONSTONE_BOLTS_E,Items.DRAGONSTONE_DRAGON_BOLTS_E},{Items.ONYX_BOLTS_E,Items.ONYX_DRAGON_BOLTS_E}};
 private static final double[] CHANCE={.05,.06,.06,.04,.05,.55,.06,.10,.06,.10};
 public static int type(int ammo){for(int i=0;i<IDS.length;i++)for(int id:IDS[i])if(ammo==id)return i;return -1;}
 public static double chance(int type,boolean npc,boolean diary,boolean armadyl){double c=CHANCE[type]+(type==9&&npc?.01:0);return Math.min(1,c*(diary?1.1:1)*(armadyl?2:1));}
 public static final class Result{public int amount,kind=-1;public boolean accurate,boosted;public Result(int amount,boolean accurate){this.amount=amount;this.accurate=accurate;}}
 public static Result roll(Player p,Entity target,int max,double accuracy,Special special,Random random){
  boolean hit=accuracy>=random.nextDouble();Result out=new Result(random.nextInt(Math.max(0,max)+1),hit);int type=type(p.playerEquipment[13]);if(type<0)return out;boolean z=p.playerEquipment[3]==26374,forced=special instanceof ZaryteCrossbow;
  if(forced&&!hit)return out;boolean diary=p.getDiaryManager().getKandarinDiary().hasDone(io.xeros.content.achievement_diary.DifficultyAchievementDiary.EntryDifficulty.HARD);
  if(!forced&&random.nextDouble()>=chance(type,target.isNPC(),diary,special instanceof ArmadylCrossbow))return out;
  if(type==3&&target.isNPC()||type==6&&p.getHealth().getCurrentHealth()<10||type==9&&target.isNPC()&&target.asNPC().isUndead())return out;
  if(type==8&&(target.isNPC()&&target.asNPC().isDragon()||target.isPlayer()&&(target.asPlayer().getItems().isWearingAnyItem(1540,11283,11284)||System.currentTimeMillis()-target.asPlayer().lastAntifirePotion<target.asPlayer().antifireDelay)))return out;
  boolean bypass=type==0||type==1||type==2||type==3||type==4||type==6||type==7||type==8;
  if(!hit&&!bypass)return out;out.accurate=forced?hit:(hit||bypass);out.kind=type;out.boosted=z;int level=p.playerLevel[4];
  if(type==0)out.amount+=level/(z?9:10);
  if(type==2){String name=target.isNPC()?target.asNPC().getName().toLowerCase():"";boolean fiery=name.contains("fire")||name.contains("fiery")||name.contains("pyrefiend")||name.contains("infernal");out.amount+=(int)Math.floor(level/(fiery?15d:20d)*(z?1.1:1));}
  if(type==6)out.amount=Math.min(z?110:100,(int)Math.floor(target.getHealth().getCurrentHealth()*(z?.22:.20)));
  if(type==7)out.amount=random.nextInt((int)Math.floor(max*(z?1.25:1.15))+1);
  if(type==8)out.amount+=(int)Math.floor(level*(z?.22:.20));
  if(type==9)out.amount=random.nextInt((int)Math.floor(max*(z?1.32:1.20))+1);
  return out;
 }
 public static void impact(Player p,Entity target,Damage hit,int dealt){if(hit.boltKind<0||!hit.isSuccess()||dealt<0)return;int k=hit.boltKind;hit.boltKind=-1;int[] gfx={749,755,750,757,751,752,754,758,756,753};target.startGraphic(new Graphic(gfx[k]));
  if(k==1&&target.isFreezable())target.freezeTimer=Math.max(target.freezeTimer,hit.boostedBolt?4:3);
  if(k==3&&target.isPlayer()){Player q=target.asPlayer();q.playerLevel[6]=Math.max(0,q.playerLevel[6]-2);q.getPA().refreshSkill(6);}
  if(k==4&&target.isPlayer()){Player q=target.asPlayer();int drain=Math.min(q.playerLevel[5],hit.boostedBolt?3:2);q.playerLevel[5]-=drain;p.playerLevel[5]=Math.min(p.getPA().getLevelForXP(p.playerXP[5]),p.playerLevel[5]+drain);q.getPA().refreshSkill(5);p.getPA().refreshSkill(5);}
  if(k==5)target.getHealth().proposeStatus(HealthStatus.POISON,hit.boostedBolt?6:5,Optional.of(p));
  if(k==6){int loss=Math.min(Math.max(0,p.getHealth().getCurrentHealth()-1),p.getHealth().getCurrentHealth()/10);if(loss>0)p.appendDamage(p,loss,Hitmark.HIT);}
  if(k==9)p.getHealth().increase((int)Math.floor(dealt*(hit.boostedBolt?.275:.25)));
 }
}
