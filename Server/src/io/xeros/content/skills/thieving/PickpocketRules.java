package io.xeros.content.skills.thieving;
/** Adapted from Alter's PickpocketPlugin/JSON at 9ddbbbe3 (BSD-2-Clause).
 * Copyright (c) 2023 Alter. License retained in third-party/Alter-LICENSE.txt.
 * These are the emulator's balance curves, not a claim of exact OSRS rates.
 */
final class PickpocketRules {
 final double base, perLevel; final int ticks, lowDamage, highDamage, level;
 PickpocketRules(int level,double base,double perLevel,int ticks,int lowDamage,int highDamage){this.level=level;this.base=base;this.perLevel=perLevel;this.ticks=ticks;this.lowDamage=lowDamage;this.highDamage=highDamage;}
 double chance(int current){return Math.max(.05,Math.min(.95,base+Math.max(0,current-level)*perLevel));}
 static PickpocketRules forTarget(Thieving.Pickpocket target){
  switch(target){
   case WARRIOR:return new PickpocketRules(25,.55,.008,8,2,2);
   case GUARD:return new PickpocketRules(40,.55,.008,8,2,2);
   case KNIGHT:return new PickpocketRules(55,.60,.0077,8,3,3);
   case MAN:return new PickpocketRules(1,.55,.015,7,1,1);
   case FARMER:return new PickpocketRules(38,.35,.008,8,1,3);
   case MENAPHITE_THUG:return new PickpocketRules(65,.32,.007,8,2,5);
   case HERO:return new PickpocketRules(80,.26,.0055,10,3,4);
   // Local targets not present in the donor dataset retain explicit solo balance.
   default:return new PickpocketRules(75,.40,.008,8,1,3);
  }
 }
}
