package io.xeros.content.combat.magic;
import io.xeros.Server;
import io.xeros.content.achievement_diary.impl.LumbridgeDraynorDiaryEntry;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import java.util.*;
/** Whole-inventory conversion; tablets are items, not unlocked spellbook casts. */
public final class BoneConversion {
 private static final int[] ELIGIBLE={526,528,530,532,2859,3125,3183,3185,3186,3187,25422};
 private final Player p;private boolean busy;
 public BoneConversion(Player p){this.p=p;}
 public boolean cast(int action){
  if(p.getMageTrainingArena().boneSpell(action))return true;
  boolean tablet=action==8014||action==8015,peaches=action==8015||action==62005;
  if(!tablet&&action!=4135&&action!=62005)return false;
  if(busy||p.isDead||p.getHealth().getCurrentHealth()<=0||p.isDisconnected()||p.getSession()==null||p.getLock().cannotInteract(p)||p.isBanking||p.isShopping||p.teleTimer>0)return false;
  if(!tablet&&(p.playerMagicBook!=0||p.playerLevel[6]<(peaches?60:15))){p.sendMessage("You need the normal spellbook and the required Magic level.");return false;}
  if(!tablet&&peaches&&!p.getMageTrainingProgress().peachesUnlocked()){p.sendMessage("Unlock Bones to Peaches at the Mage Training Arena first.");return false;}
  List<int[]> inputs=new ArrayList<>();int count=0;
  for(int id:ELIGIBLE){int amount=p.getItems().getInventoryCount(id);if(amount>0){inputs.add(new int[]{id,amount});count+=amount;}}
  if(count==0){p.sendMessage("You aren't holding any suitable bones.");return false;}
  if(tablet)inputs.add(new int[]{action,1});else{inputs.add(new int[]{561,peaches?2:1});inputs.add(new int[]{555,peaches?4:2});inputs.add(new int[]{557,peaches?4:2});}
  if(!RunePayment.exchange(p,inputs.toArray(new int[0][]),new int[][]{{peaches?6883:1963,count}}))return false;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.attacking.reset();p.startAnimation(722);p.gfx100(141);busy=true;
  if(!tablet){p.getPA().addSkillXPMultiplied(peaches?35.5:25,6,true);
   if(peaches&&p.heightLevel==0&&p.absX>=3292&&p.absX<=3309&&p.absY>=3159&&p.absY<=3176)p.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.BONES_TO_PEACHES);
  }
  Server.getEventHandler().submit(new Event<Player>("bone-conversion",p,1){public void execute(){stop();}public void stop(){super.stop();busy=false;}});
  return true;
 }
}
