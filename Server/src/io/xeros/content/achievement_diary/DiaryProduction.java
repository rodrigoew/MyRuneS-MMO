package io.xeros.content.achievement_diary;
import io.xeros.content.achievement_diary.impl.*;import io.xeros.model.entity.player.*;
/** Called only after a successful material transaction, once per recipe, never on menus or decanting. */
public final class DiaryProduction {
 private DiaryProduction(){}
 public static void potion(Player p,int id){
  if(id==12695||id==12697){p.getDiaryManager().getVarrockDiary().progress(VarrockDiaryEntry.SUPER_COMBAT);if(Boundary.isIn(p,Boundary.ARDOUGNE_ZOO_BRIDGE_BOUNDARY))p.getDiaryManager().getArdougneDiary().progress(ArdougneDiaryEntry.SUPER_COMBAT_ARD);}
  if(id==9741&&Boundary.isIn(p,Boundary.DESERT_BOUNDARY))p.getDiaryManager().getDesertDiary().progress(DesertDiaryEntry.COMBAT_POTION);
  if(id==163&&Boundary.isIn(p,Boundary.RELLEKKA_BOUNDARY))p.getDiaryManager().getFremennikDiary().progress(FremennikDiaryEntry.MIX_SUPER_DEFENCE);
  if(id==5940&&Boundary.isIn(p,Boundary.CATHERBY_BOUNDARY))p.getDiaryManager().getKandarinDiary().progress(KandarinDiaryEntry.WEAPON_POISON_PLUS_PLUS);
  if((id==12905||id==12907||id==12909||id==12911)&&Boundary.isIn(p,Boundary.BRIMHAVEN_BOUNDARY))p.getDiaryManager().getKaramjaDiary().progress(KaramjaDiaryEntry.ANTI_VENOM);
 }
 public static void smith(Player p,int id){
  if(id==865)p.getDiaryManager().getVarrockDiary().progress(VarrockDiaryEntry.SMITH_STEEL_KNIFES);
  if(id==868)p.getDiaryManager().getVarrockDiary().progress(VarrockDiaryEntry.SMITH_RUNE_KNIFES);
  if(id==1127)p.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.RUNE_PLATE_LUM);
  if(Boundary.isIn(p,Boundary.RESOURCE_AREA_BOUNDARY)){if(id==1355)p.getDiaryManager().getWildernessDiary().progress(WildernessDiaryEntry.SMITH_MITHRIL_AXE);if(id==1331)p.getDiaryManager().getWildernessDiary().progress(WildernessDiaryEntry.SMITH_ADAMANT_SCIMITAR);if(id==1333)p.getDiaryManager().getWildernessDiary().progress(WildernessDiaryEntry.SMITH_RUNE_SCIM_WILD);}
 }
 public static void fletch(Player p,int id){
  if((id==70||id==859)&&Boundary.isIn(p,Boundary.CATHERBY_BOUNDARY))p.getDiaryManager().getKandarinDiary().progress(KandarinDiaryEntry.FLETCH_MAGIC_BOW);
  if((id==54||id==843)&&Boundary.isIn(p,Boundary.GNOME_STRONGHOLD_BOUNDARY))p.getDiaryManager().getWesternDiary().progress(WesternDiaryEntry.FLETCH_OAK_SHORT_WEST);
 }
}
