package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.prayer.*;import io.xeros.model.definitions.ItemDef;import io.xeros.model.entity.player.*;import java.util.*;
public final class HistoricalPrayerBonesCheck {
 static final int[] IDS={526,530,528,4812,3123,4830,4834,2859,3183,532,3125,4832,534,22780,536,22783,6729,6812,11943,22786,22124,3179,3180,3181,3182,3185,3186,3187,25422};
 static final double[] XP={4.5,5.3,4.5,22.5,25,84,140,4.5,5,15,15,96,30,50,72,80,125,72,85,110,150,16,18,18,18,5,5,3,4.5};
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;java.lang.reflect.Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());ok(Bone.values().length==29,"29individualhistoricaltypes");for(int n=0;n<IDS.length;n++){Bone b=Prayer.isOperableBone(IDS[n]).orElseThrow();ok(b.getExperience()==XP[n],"Primary2022XP "+IDS[n]);Player p=AttackDefencePlayableCheck.actor();p.getItems().addItem(IDS[n],1);int xp=p.playerXP[5],fraction=p.playerXpTenths[5];p.getPrayer().bury(b);ok(p.getItems().getInventoryCount(IDS[n])==0&&p.playerXP[5]-xp==(int)XP[n]&&p.playerXpTenths[5]==(int)Math.round(XP[n]*10)%10,"Actual burial exact primary tenths "+IDS[n]);}
  ok(Bone.MONKEY.getItemId()==3183&&Prayer.isOperableBone(3179).orElseThrow().getExperience()==16,"Common monkey versus ninja distinction");ok(ItemDef.forId(25422).getName().equals("Bleached bones")&&ItemDef.forId(25422).getNoteId()==-1,"Native cache confirms bleached bones no note counterpart");
  System.out.println("HISTORICAL_PRAYER_BONES_ALL29_INDEPENDENT_PRIMARY_2022_IDS_XP_ACTUAL_BURIAL_TENTHS_MONKEY3183_5_NINJA3179_16_SEVEN_APE_VARIANTS_BLEACHED25422_AND_NATIVE_NO_NOTE_OK");
  System.out.println("HISTORICAL_PRAYER_BONES_ALL_CHECKS_PASSED");System.exit(0);
 }catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
