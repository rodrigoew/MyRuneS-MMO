package io.xeros.solo;

import io.xeros.*;
import io.xeros.content.combat.magic.*;
import io.xeros.content.skills.thieving.Thieving;
import io.xeros.model.entity.HealthStatus;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.packets.ClickingButtons;
import io.xeros.model.entity.player.packets.npcoptions.NpcOptionTwo;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.items.GameItem;
import io.xeros.util.Location3D;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.DoubleSupplier;

public final class ThievingMagicPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void set(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id+"/"+n);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static long xp(Player p,int id){return p.playerXP[id]*10L+p.playerXpTenths[id];}
 static NPC npc(int id){return Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).filter(n->n.getNpcId()==id).findFirst().orElseThrow(()->new AssertionError("Native NPC missing "+id));}
 static void near(Player p,NPC n){p.absX=n.absX;p.absY=n.absY;p.heightLevel=n.heightLevel;}
 static void ready(Player p,double roll)throws Exception{set(p.getThieving(),"lastInteraction",0L);set(p.getThieving(),"stunnedUntil",0L);set(p.getThieving(),"successRoll",(DoubleSupplier)()->roll);}
 static void advance(int n)throws Exception{Field f=Server.class.getDeclaredField("tickCount");f.setAccessible(true);for(int i=0;i<n;i++){f.setLong(null,Server.getTickCount()+1);SkillsAuditCheck.tick(1);}}
 static void runes(Player p){for(int id:new int[]{554,555,556,557,558,559,560,561,562,563,564,565,566,9075,21880})give(p,id,10000);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void thievingTables()throws Exception{
  NPC n=npc(3297);
  for(Thieving.Pickpocket target:Thieving.Pickpocket.values()){
   Player p=actor();near(p,n);ready(p,0);int level=((Number)field(target,"level")).intValue();long before=xp(p,17);p.playerLevel[17]=level-1;
   p.getThieving().steal(target,n);ok(xp(p,17)==before,"Underlevel pickpocket "+target);p.playerLevel[17]=level;ready(p,0);p.getThieving().steal(target,n);
   ok(xp(p,17)-before==Math.round(((Number)field(target,"experience")).doubleValue()*10),"Pickpocket XP "+target);long after=xp(p,17);p.getThieving().steal(target,n);ok(xp(p,17)==after,"Pickpocket cooldown "+target);clear(p);
  }
  for(Thieving.Stall stall:Thieving.Stall.values()){
   Player p=actor();Location3D at=new Location3D(p.absX,p.absY,p.heightLevel);int level=((Number)field(stall,"level")).intValue();long before=xp(p,17);GameItem item=(GameItem)field(stall,"item");p.playerLevel[17]=level-1;
   p.getThieving().steal(stall,at);ok(xp(p,17)==before&&count(p,item.getId())==0,"Underlevel stall "+stall);p.playerLevel[17]=level;p.getThieving().steal(stall,at);
   ok(xp(p,17)-before==Math.round(((Number)field(stall,"experience")).doubleValue()*10)&&count(p,item.getId())==item.getAmount(),"Stall primary loot/XP "+stall);long after=xp(p,17);p.getThieving().steal(stall,at);ok(xp(p,17)==after,"Stall cooldown "+stall);clear(p);
   p=actor();give(p,2347,28);before=xp(p,17);p.getThieving().steal(stall,new Location3D(p.absX,p.absY,p.heightLevel));ok(xp(p,17)==before&&count(p,item.getId())==0,"Full stall bag earned XP "+stall);clear(p);
   p=actor();give(p,2347,27);p.getThieving().steal(stall,new Location3D(p.absX,p.absY,p.heightLevel));ok(count(p,item.getId())==item.getAmount(),"Last slot primary loot lost "+stall);clear(p);
  }
  System.out.println("THIEVING_ALL_"+Thieving.Pickpocket.values().length+"_PICKPOCKET_TABLES_"+Thieving.Stall.values().length+"_STALLS_LEVEL_XP_COOLDOWN_FULL_BAG_AND_LAST_SLOT_PASS");
 }
 static void thievingTransactions()throws Exception{
  NPC n=npc(3297);Player p=actor();near(p,n);ready(p,0);give(p,995,100);give(p,2347,27);long before=xp(p,17);p.npcClickIndex=n.getIndex();NpcOptionTwo.handleOption(p,n.getNpcId());ok(count(p,995)==150&&xp(p,17)-before==843,"Native knight full bag existing coin stack");clear(p);
  p=actor();near(p,n);ready(p,0);give(p,995,Integer.MAX_VALUE-20);give(p,2347,27);before=xp(p,17);p.getThieving().steal(Thieving.Pickpocket.KNIGHT,n);ok(count(p,995)==Integer.MAX_VALUE-20&&xp(p,17)==before,"Overflow lost coins or awarded XP");clear(p);
  p=actor();near(p,n);ready(p,0);give(p,2347,28);before=xp(p,17);p.getThieving().steal(Thieving.Pickpocket.KNIGHT,n);ok(xp(p,17)==before&&count(p,995)==0,"Full bag without coin stack");clear(p);
  p=actor();near(p,n);ready(p,0);for(int i=0;i<5;i++)p.playerEquipment[new int[]{0,4,7,9,10}[i]]=new int[]{5554,5553,5555,5556,5557}[i];p.getThieving().steal(Thieving.Pickpocket.KNIGHT,n);ok(count(p,995)==100,"Full Rogue outfit double loot");clear(p);
  for(int mode=0;mode<8;mode++){
   p=actor();near(p,n);ready(p,0);before=xp(p,17);
   switch(mode){case 0:p.isDead=true;break;case 1:p.setDisconnected(true);break;case 2:p.isBanking=true;break;case 3:p.isShopping=true;break;case 4:p.teleTimer=1;break;case 5:p.setMovementState(new PlayerMovementStateBuilder().setLocked(true).createPlayerMovementState());break;case 6:p.absX+=10;break;case 7:p.heightLevel++;break;}
   p.getThieving().steal(Thieving.Pickpocket.KNIGHT,n);ok(xp(p,17)==before&&count(p,995)==0,"Pickpocket invalid state "+mode);clear(p);
  }
  p=actor();near(p,n);ready(p,1);before=xp(p,17);int hp=p.getHealth().getCurrentHealth();p.getThieving().steal(Thieving.Pickpocket.MAN,n);ok(p.getThieving().isStunned()&&p.getHealth().getCurrentHealth()==hp-1&&xp(p,17)==before,"Failure stun/damage/noXP");p.getThieving().steal(Thieving.Pickpocket.MAN,n);ok(p.getHealth().getCurrentHealth()==hp-1,"Stun allowed another attempt");ready(p,0);p.getThieving().steal(Thieving.Pickpocket.MAN,n);ok(count(p,995)==3,"Recovered stun");clear(p);
  n=npc(3550);p=actor();near(p,n);ready(p,0);before=xp(p,17);ok(!p.getThieving().knockout(n)&&xp(p,17)==before,"Missing blackjack");p.playerEquipment[3]=4599;ok(p.getThieving().knockout(n),"Blackjack knockout");for(int i=0;i<2;i++){set(p.getThieving(),"lastInteraction",0L);p.getThieving().steal(Thieving.Pickpocket.MENAPHITE_THUG,n);}ok(count(p,995)==120&&xp(p,17)-before==3300,"Two knockout thefts");set(p.getThieving(),"lastInteraction",0L);set(p.getThieving(),"successRoll",(DoubleSupplier)()->1d);p.getThieving().steal(Thieving.Pickpocket.MENAPHITE_THUG,n);ok(count(p,995)==120&&p.getThieving().isStunned(),"Third pick retained guaranteed knockout");clear(p);
  System.out.println("THIEVING_NATIVE_KNIGHT_FULL_COIN_STACK_OVERFLOW_ATOMIC_REFUSAL_ROGUE_DOUBLE_EIGHT_GUARDS_STUN_AND_TWO_BLACKJACK_PICKS_PASS");
 }
 static void magicTable(){
  for(int i=0;i<CombatSpellData.MAGIC_SPELLS.length;i++){
   Player p=actor();runes(p);int staff=CombatSpellData.getStaffNeeded(p,i);if(staff>0)p.playerEquipment[3]=staff;
   ok(MagicRequirements.checkMagicReqs(p,i,false),"Spell row requirements "+i+" length="+CombatSpellData.MAGIC_SPELLS[i].length);clear(p);
  }
  System.out.println("MAGIC_ALL_"+CombatSpellData.MAGIC_SPELLS.length+"_SPELL_ROWS_RUNE_REQUIREMENTS_SHORT_ARRAYS_NO_EXCEPTION_PASS");
 }
 static void cureMe()throws Exception{
  Player p=actor();p.playerMagicBook=2;runes(p);p.getHealth().proposeStatus(HealthStatus.VENOM,8,Optional.empty());long before=xp(p,6);int astral=count(p,9075);ok(LunarUtilities.cureMe(p)&&p.getHealth().getStatus()==HealthStatus.POISON&&xp(p,6)-before==690&&count(p,9075)==astral-2,"Cure Me first venom cast");ok(!LunarUtilities.cureMe(p)&&count(p,9075)==astral-2,"Cure Me same tick");advance(3);ok(LunarUtilities.cureMe(p)&&p.getHealth().getStatus()==HealthStatus.NORMAL&&count(p,9075)==astral-4&&xp(p,6)-before==1380,"Cure Me second cure");advance(3);ok(!LunarUtilities.cureMe(p)&&count(p,9075)==astral-4,"Healthy cast charged");p.getHealth().proposeStatus(HealthStatus.POISON,4,Optional.empty());ok(p.getHealth().getStatus()==HealthStatus.POISON,"Cure Me granted immunity");clear(p);
  for(int mode=0;mode<5;mode++){
   p=actor();p.playerMagicBook=2;runes(p);p.getHealth().proposeStatus(HealthStatus.POISON,4,Optional.empty());astral=count(p,9075);before=xp(p,6);
   switch(mode){case 0:p.playerLevel[6]=70;break;case 1:p.playerMagicBook=0;break;case 2:p.getItems().deleteItem2(563,count(p,563));break;case 3:p.isBanking=true;break;case 4:p.setMovementState(new PlayerMovementStateBuilder().setLocked(true).createPlayerMovementState());break;}
   ok(!LunarUtilities.cureMe(p)&&count(p,9075)==astral&&xp(p,6)==before&&p.getHealth().getStatus()==HealthStatus.POISON,"Cure Me refusal "+mode);clear(p);
  }
  p=actor();p.playerMagicBook=2;runes(p);p.getHealth().proposeStatus(HealthStatus.POISON,4,Optional.empty());p.getInStream().buffer=new byte[]{(byte)(30091>>8),(byte)30091};p.getInStream().currentOffset=0;new ClickingButtons().processPacket(p,185,2);ok(p.getHealth().getStatus()==HealthStatus.NORMAL,"Cure Me native button");clear(p);
  System.out.println("MAGIC_CURE_ME_NATIVE_BUTTON_VENOM_TWO_CASTS_69XP_EACH_RUNES_CADENCE_NO_IMMUNITY_AND_FIVE_ATOMIC_REFUSALS_PASS");
 }
 static void vengeance()throws Exception{
  for(int mode=0;mode<8;mode++){
   Player p=actor();runes(p);p.playerMagicBook=2;long before=xp(p,6);int astral=count(p,9075);
   switch(mode){case 0:p.playerMagicBook=0;break;case 1:p.playerLevel[6]=93;break;case 2:p.playerLevel[1]=39;break;case 3:p.isBanking=true;break;case 4:p.lastCast=System.currentTimeMillis();break;case 5:p.vengOn=true;break;case 6:p.isDead=true;break;case 7:p.getItems().deleteItem2(560,count(p,560));break;}
   p.getPA().castVengeance();ok(count(p,9075)==astral&&xp(p,6)==before&&!p.usingMagic,"Vengeance refusal state "+mode);clear(p);
  }
  Player p=actor();runes(p);p.playerMagicBook=2;int astral=count(p,9075),death=count(p,560),earth=count(p,557);long before=xp(p,6);p.getPA().castVengeance();ok(p.vengOn&&count(p,9075)==astral-4&&count(p,560)==death-2&&count(p,557)==earth-10&&xp(p,6)-before==1120,"Vengeance actual cost/XP");p.getPA().castVengeance();ok(count(p,9075)==astral-4,"Vengeance double cast");clear(p);
  System.out.println("MAGIC_VENGEANCE_LUNAR_94_MAGIC_40_DEFENCE_112XP_EXACT_RUNES_EIGHT_REFUSALS_AND_NO_STICKY_USINGMAGIC_PASS");
 }
 static void imbue()throws Exception{
  Player p=actor();p.playerMagicBook=2;runes(p);int astral=count(p,9075);long before=xp(p,6);p.isBanking=true;ok(!MagicImbue.cast(p)&&count(p,9075)==astral&&xp(p,6)==before,"Imbue cast while banking");p.isBanking=false;ok(MagicImbue.cast(p)&&count(p,9075)==astral-2&&xp(p,6)-before==860,"Imbue legitimate cast");ok(!LunarUtilities.humidify(p)&&count(p,9075)==astral-2,"Shared Lunar delay bypass");ok(!MagicImbue.cast(p)&&count(p,9075)==astral-2,"Imbue active duplicate");clear(p);
  System.out.println("MAGIC_IMBUE_BANK_REFUSAL_ATOMIC_COST_86XP_SHARED_DELAY_AND_ACTIVE_EFFECT_NO_DUPLICATE_PASS");
 }
 static void aid()throws Exception{
  for(int spell:new int[]{30048,30282,30290,30298}){
   Player p=actor(),q=actor();p.playerMagicBook=2;runes(p);q.setIndex(2);q.acceptAid=true;PlayerHandler.players[2]=q;p.playerAttackingIndex=2;q.absX=p.absX;q.absY=p.absY;q.heightLevel=p.heightLevel;q.specAmount=10;p.specAmount=10;q.vengOn=true;
   int[] bag=p.playerItemsN.clone();LunarSpells.CastingLunarOnPlayer(p,spell);ok(Arrays.equals(bag,p.playerItemsN),"No-effect aid consumed runes "+spell);clear(p);clear(q);PlayerHandler.players[2]=null;
  }
  Player p=actor(),q=actor();p.playerMagicBook=2;runes(p);q.setIndex(2);q.acceptAid=true;PlayerHandler.players[2]=q;p.playerAttackingIndex=2;q.absX=p.absX;q.absY=p.absY;q.heightLevel=p.heightLevel;q.specAmount=8;p.specAmount=10;LunarSpells.CastingLunarOnPlayer(p,30282);ok(q.specAmount==10&&p.specAmount==5,"Energy transfer overflow");clear(p);clear(q);PlayerHandler.players[2]=null;
  p=actor();q=actor();p.playerMagicBook=2;runes(p);q.setIndex(2);q.acceptAid=true;PlayerHandler.players[2]=q;p.playerAttackingIndex=2;q.absX=p.absX;q.absY=p.absY;q.heightLevel=p.heightLevel;p.getHealth().setCurrentHealth(80);q.getHealth().setCurrentHealth(90);LunarSpells.CastingLunarOnPlayer(p,30290);ok(q.getHealth().getCurrentHealth()==99&&p.getHealth().getCurrentHealth()==71,"Heal Other bounded transfer uses actual health");clear(p);clear(q);PlayerHandler.players[2]=null;
  System.out.println("MAGIC_LUNAR_AID_NO_EFFECT_NO_CHARGE_SPECIAL_CAP_AND_BOUNDED_ACTUAL_HEALTH_TRANSFER_PASS");
 }
 static void save()throws Exception{
  Player p=actor();p.setLoginName("skills53qa");p.setDisplayName("Skills53qa");p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;give(p,995,1234);p.getPA().addSkillXPMultiplied(84.3,17,true);p.getPA().addSkillXPMultiplied(69,6,true);long theft=xp(p,17),magic=xp(p,6);ok(PlayerSave.saveGameInstant(p),"Skills save");Player q=actor();ok(PlayerSave.loadGame(q,"skills53qa","IsolatedAudit",true).toString().equals("SUCCESS")&&xp(q,17)==theft&&xp(q,6)==magic&&count(q,995)==1234,"Skill XP/fractional/loot reload");PlayerHandler.players[1]=null;clear(p);clear(q);System.out.println("THIEVING_MAGIC_FRACTIONAL_XP_AND_LOOT_ACTUAL_SAVE_RELOAD_PASS");
 }
 public static void main(String[]args)throws Exception{
  Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;
  Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());
  thievingTables();thievingTransactions();System.out.println("THIEVING_PLAYABLE_V53_PASS");magicTable();cureMe();vengeance();imbue();aid();save();System.out.println("MAGIC_PLAYABLE_V53_PASS");System.exit(0);
 }
}
