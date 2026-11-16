package io.xeros.content.skills.runecrafting;
import io.xeros.content.skills.Skill;
import io.xeros.model.entity.player.Player;
/** Recipe data adapted from OpenRune CombinationRuneData (ISC, RS Mod 2025).
 * Upstream: c4cd45ef2d38a0badcc7a9f89773f8f1c29bb16f.
 * License: third-party/OpenRune-LICENSE.txt. */
public enum CombinationRune {
 MIST_AIR(25378,555,4695,1444,6,8),MIST_WATER(25376,556,4695,1438,6,8.5),
 DUST_AIR(25378,557,4696,1440,10,8.3),DUST_EARTH(24972,556,4696,1438,10,9),
 MUD_WATER(25376,557,4698,1440,13,9.3),MUD_EARTH(24972,555,4698,1444,13,9.5),
 SMOKE_AIR(25378,554,4697,1442,15,8.5),SMOKE_FIRE(24971,556,4697,1438,15,9.5),
 STEAM_WATER(25376,554,4694,1442,19,9.5),STEAM_FIRE(24971,555,4694,1444,19,10),
 LAVA_EARTH(24972,554,4699,1442,23,10),LAVA_FIRE(24971,557,4699,1440,23,10.5);
 public final int altar,input,output,talisman,level;public final double experience;
 CombinationRune(int altar,int input,int output,int talisman,int level,double experience){this.altar=altar;this.input=input;this.output=output;this.talisman=talisman;this.level=level;this.experience=experience;}
 public static boolean combine(Player p,int used,int object){return combine(p,used,object,Math::random);}
 static boolean combine(Player p,int used,int object,java.util.function.DoubleSupplier random){
  for(CombinationRune recipe:values())if(recipe.altar==object && recipe.input==used){
   if(!p.getUtilityMagic().canInteract())return true;
   if(p.playerLevel[20]<recipe.level){p.sendMessage("You need Runecrafting level "+recipe.level+".");return true;}
   int count=Math.min(p.getItems().getInventoryCount(7936),p.getItems().getInventoryCount(used));
   if(count<=0){p.sendMessage("You need pure essence and elemental runes.");return true;}
   boolean imbued=System.currentTimeMillis()<p.magicImbueUntil;
   if(!imbued && !p.getItems().playerHasItem(recipe.talisman)){p.sendMessage("You need the talisman of the rune you are combining.");return true;}

   boolean binding=p.getItems().isWearingItem(5521,Player.playerAmulet);int made=0;
   for(int i=0;i<count;i++)if(binding||random.getAsDouble()<.5)made++;
   java.util.List<int[]> inputs=new java.util.ArrayList<>();inputs.add(new int[]{7936,count});inputs.add(new int[]{used,count});if(!imbued)inputs.add(new int[]{recipe.talisman,1});
   int amount=made>0?Runecrafting.withEyeBonus(p,made):0;
   if(!io.xeros.content.skills.ProductionBatch.exchange(p,inputs.toArray(new int[0][]),amount>0?new int[][]{{recipe.output,amount}}:new int[0][]))return true;
   if(binding && --p.bindingNecklaceUses<=0){p.getItems().deleteEquipment(Player.playerAmulet);p.bindingNecklaceUses=16;p.sendMessage("Your binding necklace crumbles to dust.");}
   if(made>0){p.getPA().addSkillXPMultiplied(recipe.experience*made,Skill.RUNECRAFTING.getId(),true);}
   p.startAnimation(791);p.gfx100(186);p.sendMessage("You craft "+made+" combination runes.");return true;
  }
  return false;
 }
}
