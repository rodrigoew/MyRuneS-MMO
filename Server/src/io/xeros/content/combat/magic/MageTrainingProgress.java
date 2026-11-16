package io.xeros.content.combat.magic;
import io.xeros.model.entity.player.Player;
import java.util.*;
/** Persistent activity currency and spell unlock, independent of a live room instance. */
public final class MageTrainingProgress {
 public static final int TELEKINETIC=0,ALCHEMIST=1,ENCHANTMENT=2,GRAVEYARD=3;
 private static final int[] CAPS={4000,8000,16000,4000},PEACH_COST={200,300,2000,200};
 private final Player p;private int[] points=new int[4];private int streak,enchanted;private boolean peaches;
 public MageTrainingProgress(Player p){this.p=p;}
 public int points(int room){if(room<0||room>3)throw new IllegalArgumentException("Room");return points[room];}
 public int streak(){return streak;}public int enchanted(){return enchanted;}public boolean peachesUnlocked(){return peaches;}
 public int award(int room,int amount){
  if(room<0||room>3||amount<0)return 0;int before=points[room];points[room]=(int)Math.min(CAPS[room],(long)before+amount);return points[room]-before;
 }
 public void penalize(int room,int amount){if(room>=0&&room<4&&amount>0)points[room]=Math.max(0,points[room]-amount);}
 public boolean canAfford(int[] cost){if(cost==null||cost.length!=4)return false;for(int i=0;i<4;i++)if(cost[i]<0||points[i]<cost[i])return false;return true;}
 public boolean spend(int[] cost){if(!canAfford(cost))return false;for(int i=0;i<4;i++)points[i]-=cost[i];return true;}
 public boolean unlockPeaches(){
  if(peaches||p.playerLevel[6]<60)return false;for(int i=0;i<4;i++)if(points[i]<PEACH_COST[i])return false;
  for(int i=0;i<4;i++)points[i]-=PEACH_COST[i];peaches=true;p.getPA().sendConfig(1987,1);return true;
 }
 /** True means the room should also give the fifth-maze rune/XP reward. */
 public boolean mazeSolved(){award(TELEKINETIC,2);if(++streak<5)return false;streak=0;award(TELEKINETIC,8);return true;}
 public int enchant(int tier,boolean dragonstone,boolean bonus){
  if(tier<1||tier>7)return 0;int amount;
  if(dragonstone)amount=tier*2;
  else{enchanted=(enchanted+1)%10;amount=tier*(enchanted==0?2:1)+(bonus?2:0);}
  return award(ENCHANTMENT,amount);
 }
 public String encode(){return "1|"+points[0]+"|"+points[1]+"|"+points[2]+"|"+points[3]+"|"+streak+"|"+enchanted+"|"+(peaches?1:0);}
 public boolean decode(String text){
  try{if(text==null||text.length()>120)return false;String[] fields=text.split("\\|",-1);if(fields.length!=8||!fields[0].equals("1"))return false;
   int[] next=new int[4];for(int i=0;i<4;i++){next[i]=Integer.parseInt(fields[i+1]);if(next[i]<0||next[i]>CAPS[i])return false;}
   int nextStreak=Integer.parseInt(fields[5]),nextEnchanted=Integer.parseInt(fields[6]);if(nextStreak<0||nextStreak>4||nextEnchanted<0||nextEnchanted>9||!fields[7].equals("0")&&!fields[7].equals("1"))return false;
   points=next;streak=nextStreak;enchanted=nextEnchanted;peaches=fields[7].equals("1");return true;
  }catch(RuntimeException e){return false;}
 }
}
