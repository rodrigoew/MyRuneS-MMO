package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.*;import io.xeros.content.skills.woodcutting.*;import io.xeros.content.skills.firemake.*;import io.xeros.model.collisionmap.*;import io.xeros.model.definitions.ItemDef;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;import java.util.*;
public final class WoodcuttingToolsCheck{
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(){Player p=AttackDefencePlayableCheck.actor();p.setLoginName("axetoolqa");p.setDisplayName("Axetoolqa");return p;}
 static void tick(int n){SkillsAuditCheck.tick(n);}static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id);}static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void xp(Player p,int skill,int before,double amount){Player q=actor();int old=q.playerXP[skill];q.getPA().addSkillXPMultiplied(amount,skill,true);ok(p.playerXP[skill]-before==q.playerXP[skill]-old,"XP "+skill+" got="+(p.playerXP[skill]-before)+" expected="+(q.playerXP[skill]-old));}
 static void axes(){
  int[][] rows={{1351,1,0},{1349,1,1},{1353,6,2},{1361,11,3},{1355,21,4},{1357,31,5},{1359,41,6},{6739,61,7},{13241,61,7},{13242,61,7},{20011,61,7},{23279,41,6},{23673,71,8},{23675,61,7}};
  for(int[] row:rows){Player p=actor();give(p,row[0],1);p.playerLevel[8]=row[1]-1;ok(Hatchet.getBest(p)==null,"Axe lower level "+row[0]);p.playerLevel[8]=row[1];ok(Hatchet.getBest(p)!=null&&HistoricalWoodcutting.axeTier(p)==row[2],"Axe level/tier "+row[0]);ok(ItemDef.getDefinitions().containsKey(row[0]),"Missing axe definition "+row[0]);}
  Player p=actor();give(p,6739,1);give(p,13242,1);give(p,13241,1);ok(Hatchet.getBest(p)==Hatchet.INFERNAL_AXE,"Charged infernal preferred over equal speed dragon/empty");give(p,23673,1);ok(Hatchet.getBest(p)==Hatchet.CRYSTAL,"Crystal preferred");
  ok(ItemDef.forId(23673).getRequirements().stream().anyMatch(x->x.getSkill()==Skill.ATTACK&&x.getLevel()==70)&&ItemDef.forId(23673).getRequirements().stream().anyMatch(x->x.getSkill()==Skill.AGILITY&&x.getLevel()==50),"Crystal wield requirements");
  for(var kind:HistoricalTreeData.Kind.values())ok(kind.rates.length==9,"Nine axe rate profiles "+kind);
  System.out.println("WOODCUTTING_TOOLS_FOURTEEN_AXE_TYPES_LEVEL_BOUNDARIES_TIERS_BEST_SELECTION_NATIVE_DEFINITIONS_AND_CRYSTAL_WIELD_REQUIREMENTS_OK");
 }
 static void fire(){
  int[] ids={1511,2862,1521,1519,6333,1517,10810,6332,1515,1513,19669},levels={1,1,15,30,35,45,42,50,60,75,90};
  double[] values={40,40,60,90,105,135,125,157.5,202.5,303.8,350};
  for(int i=0;i<ids.length;i++){LogData data=LogData.getLogData(actor(),ids[i]);ok(data!=null&&data.getlevelRequirement()==levels[i]&&data.getExperience()==values[i],"Historical Firemaking table "+ids[i]);}
  for(var kind:HistoricalTreeData.Kind.values())for(int roll=0;roll<3;roll++){
   Player p=actor();p.playerLevel[11]=1;give(p,13241,1);ok(p.getWoodcuttingTools().decode("1,10000"),"Counter fixture");int before=p.playerXP[11];final int value=roll;
   boolean burned=p.getWoodcuttingTools().combust(Hatchet.INFERNAL_AXE,kind,b->value);LogData data=LogData.getLogData(p,kind.item);boolean expected=roll==0&&data!=null;
   ok(burned==expected,"Infernal one in three/all log types "+kind+"/"+roll);
   if(expected){xp(p,11,before,data.getExperience()/2);ok(count(p,13242)==1&&count(p,13241)==0&&p.getWoodcuttingTools().infernal()==0,"Last infernal charge becomes empty");}
   else ok(p.playerXP[11]==before&&p.getWoodcuttingTools().infernal()==1&&count(p,13241)==1,"No infernal effect on failed roll/nonburnable bark");
  }
  Player pyro=actor();give(pyro,13241,1);for(int i=0;i<4;i++)pyro.playerEquipment[new int[]{Player.playerChest,Player.playerLegs,Player.playerHat,Player.playerFeet}[i]]=new int[]{20704,20706,20708,20710}[i];
  int before=pyro.playerXP[11];ok(pyro.getWoodcuttingTools().combust(Hatchet.INFERNAL_AXE,HistoricalTreeData.Kind.REDWOOD,b->0),"Low Firemaking can burn redwood");xp(pyro,11,before,175*1.025);
  System.out.println("WOODCUTTING_TOOLS_ELEVEN_FROZEN_FIREMAKING_LOG_LEVELS_XP_THIRTY_SIX_COMBUSTION_CASES_HALF_XP_5000_BURNS_LAST_CHARGE_PYROMANCER_AND_NO_FIREMAKING_USE_LEVEL_GATE_OK");
 }
 static void infernal(){
  Player p=actor();give(p,6739,1);give(p,13233,1);int wc=p.playerXP[8],fm=p.playerXP[11];
  ok(p.getWoodcuttingTools().createInfernal(),"Infernal create");tick(2);ok(count(p,13241)==0,"Three tick infernal");tick(1);clear(p);
  ok(count(p,13241)==1&&count(p,6739)==0&&count(p,13233)==0&&p.getWoodcuttingTools().infernal()==5000,"Infernal transaction");xp(p,8,wc,200);xp(p,11,fm,350);
  for(int skill:new int[]{8,11}){Player q=actor();q.playerXP[skill]=q.getPA().getXPForLevel(skill==8?60:84);q.playerLevel[skill]=99;give(q,6739,1);give(q,13233,1);ok(!q.getWoodcuttingTools().createInfernal(),"Nonboostable base level "+skill);}
  for(int reagent:new int[]{6739,13233}){Player q=actor();give(q,13242,1);give(q,reagent,1);ok(q.getWoodcuttingTools().rechargeInfernal(reagent),"Infernal recharge reagent");tick(3);clear(q);ok(count(q,13242)==0&&count(q,reagent)==0&&count(q,13241)==1&&q.getWoodcuttingTools().infernal()==5000,"Recharge transaction");}
  Player moved=actor();give(moved,6739,1);give(moved,13233,1);moved.getWoodcuttingTools().createInfernal();moved.absX++;tick(3);clear(moved);ok(count(moved,13241)==0&&count(moved,6739)==1,"Moved creation cancels");
  System.out.println("WOODCUTTING_TOOLS_INFERNAL_BASE61_85_THREE_TICKS_ATOMIC_CREATION_200WC_350FM_BOTH_RECHARGE_REAGENTS_AND_MOVEMENT_CANCEL_OK");
 }
 static void crystals()throws Exception{
  for(int id:new int[]{23673,23675}){Player p=actor();give(p,id,1);give(p,23962,250);ok(p.getWoodcuttingTools().decode("5000,19999"),"Near full crystal");ok(p.getWoodcuttingTools().rechargeCrystal(id),"Recharge crystal");ok(p.getWoodcuttingTools().crystal()==20000&&count(p,23962)==(id==23675?50:249)&&count(p,23673)==1,"Crystal100/shard max20000");}
  for(boolean worn:new boolean[]{false,true})for(int ring:new int[]{-1,23943,25543,25545}){
   Player p=actor();if(worn){p.playerEquipment[Player.playerWeapon]=23673;p.playerEquipmentN[Player.playerWeapon]=1;}else give(p,23673,1);p.playerEquipment[Player.playerRing]=ring;p.getWoodcuttingTools().decode("5000,1");
   if(ring!=-1){p.getWoodcuttingTools().consumeCrystal(Hatchet.CRYSTAL,b->0);ok(p.getWoodcuttingTools().crystal()==1,"Signet save");}
   p.getWoodcuttingTools().consumeCrystal(Hatchet.CRYSTAL,b->1);
   ok(p.getWoodcuttingTools().crystal()==0&&(worn?p.playerEquipment[Player.playerWeapon]==23675:count(p,23675)==1),"Last crystal charge empty/worn");
  }
  for(WorldObject bowl:MiningAccessoriesCheck.bowls()){
   Player p=MiningGuildCheck.actor(bowl.x-1,bowl.y);p.heightLevel=bowl.height;give(p,6739,1);give(p,23953,1);give(p,23962,120);int smith=p.playerXP[13],craft=p.playerXP[12];
   ok(p.getWoodcuttingTools().createCrystal(bowl),"Actual singing bowl");tick(2);ok(count(p,23673)==0,"Three tick crystal");tick(1);clear(p);
   ok(count(p,23673)==1&&count(p,6739)==0&&count(p,23953)==0&&count(p,23962)==0&&p.getWoodcuttingTools().crystal()==10000,"Crystal creation transaction");xp(p,13,smith,6000);xp(p,12,craft,6000);
   ok(p.getWoodcuttingTools().revert(23673)&&count(p,23673)==0&&count(p,6739)==1&&count(p,23953)==1&&p.getWoodcuttingTools().crystal()==0,"Revert returns axe/seed loses charges");
  }
  WorldObject bowl=MiningAccessoriesCheck.bowls().get(0);Player drain=MiningGuildCheck.actor(bowl.x-1,bowl.y);drain.heightLevel=bowl.height;give(drain,6739,1);give(drain,23953,1);give(drain,23962,120);drain.getWoodcuttingTools().createCrystal(bowl);drain.playerLevel[12]=75;tick(3);clear(drain);ok(count(drain,23673)==0&&count(drain,23962)==120,"Craft boost expiry cancels crystal");
  System.out.println("WOODCUTTING_TOOLS_CRYSTAL_SHARDS100_CAP20000_SIGNETS_WORN_AND_INVENTORY_DEPLETION_TWO_NATIVE_BOWLS_THREE_TICKS_BOTH6000_XP_REVERT_AND_LEVEL_DRAIN_CANCEL_OK");
 }
 static void saved()throws Exception{
  Player p=actor();ok(p.getWoodcuttingTools().decode("1234,5678"),"Saved state");
  for(String bad:new String[]{"-1,1","5001,1","1,20001","1","1,2,3","NaN,2","1,-1","999999,1"}){ok(!p.getWoodcuttingTools().decode(bad)&&p.getWoodcuttingTools().encode().equals("1234,5678"),"Malformed save rejected atomically "+bad);}
  p.setIndex(1);p.saveCharacter=true;p.isActive=true;PlayerHandler.players[1]=p;ok(PlayerSave.saveGameInstant(p),"Save axes");Player q=actor();ok(PlayerSave.loadGame(q,"axetoolqa","IsolatedAudit",true).toString().equals("SUCCESS")&&q.getWoodcuttingTools().encode().equals("1234,5678"),"Axe charges reload");PlayerHandler.players[1]=null;
  System.out.println("WOODCUTTING_TOOLS_COUNTER_SAVE_RELOAD_EIGHT_MALFORMED_VALUES_ATOMIC_REJECTION_OK");
 }
 static void actions()throws Exception{
  var rows=HistoricalTreeCheck.nodes();WorldObject normal=rows.stream().filter(o->o.id==1276&&o.height==0).findFirst().orElseThrow();
  Player burn=MiningGuildCheck.actor(normal.x-1,normal.y);burn.heightLevel=normal.height;give(burn,13241,1);burn.getWoodcuttingTools().decode("1,10000");burn.playerLevel[11]=1;int wc=burn.playerXP[8],fm=burn.playerXP[11];
  ok(HistoricalWoodcutting.start(burn,normal.id,normal.x,normal.y,b->b==256||b==3?0:b-1),"Actual infernal tree action");tick(4);clear(burn);
  ok(count(burn,1511)==0&&count(burn,13242)==1&&burn.getWoodcuttingTools().infernal()==0,"Actual log combusted and final charge empty");xp(burn,8,wc,25);xp(burn,11,fm,20);HistoricalTreeCheck.restore(normal,98);
  Player crystal=MiningGuildCheck.actor(normal.x-1,normal.y);crystal.heightLevel=normal.height;give(crystal,23673,1);crystal.getWoodcuttingTools().decode("5000,1");
  ok(HistoricalWoodcutting.start(crystal,normal.id,normal.x,normal.y,b->b==256?0:b-1),"Actual crystal action");tick(4);clear(crystal);
  ok(count(crystal,1511)==1&&count(crystal,23675)==1&&crystal.getWoodcuttingTools().crystal()==0,"Actual successful log consumes crystal charge");HistoricalTreeCheck.restore(normal,98);
  WorldObject magic=rows.stream().filter(o->o.id==10834&&o.height==0).findFirst().orElseThrow();
  Player miss=MiningGuildCheck.actor(magic.x-1,magic.y);miss.heightLevel=magic.height;give(miss,23673,1);miss.getWoodcuttingTools().decode("5000,1");
  ok(HistoricalWoodcutting.start(miss,magic.id,magic.x,magic.y,b->b-1),"Failed crystal action");tick(4);clear(miss);
  ok(count(miss,1513)==0&&count(miss,23673)==1&&miss.getWoodcuttingTools().crystal()==1,"Failed roll consumes no charge");
  io.xeros.model.entity.npc.NPC singer=null;for(var npc:io.xeros.model.entity.npc.NPCHandler.npcs)if(npc!=null&&npc.getNpcId()==9136){singer=npc;break;}ok(singer!=null,"Live Conwenna");
  Player service=MiningGuildCheck.actor(singer.getX()-1,singer.getY());service.heightLevel=singer.getHeight();service.playerLevel[12]=1;service.playerLevel[13]=1;service.playerLevel[14]=1;service.playerLevel[8]=71;give(service,6739,1);give(service,23953,1);give(service,23962,180);int smith=service.playerXP[13],craft=service.playerXP[12];
  ok(service.getWoodcuttingTools().singerService(singer)&&count(service,23673)==1&&count(service,23962)==0&&service.getWoodcuttingTools().crystal()==10000,"Actual NPC axe service without production levels");
  ok(service.playerXP[13]==smith&&service.playerXP[12]==craft,"Paid NPC service gives no production XP");
  System.out.println("WOODCUTTING_TOOLS_REAL_NATIVE_TREE_INFERNAL_BURN_BOTH_XP_LAST_CHARGE_CRYSTAL_SUCCESS_ONLY_CHARGE_AND_LIVE_CONWENNA180_SERVICE_NO_PRODUCTION_LEVELS_OR_XP_OK");
 }
 static void specials()throws Exception{
  var method=io.xeros.model.entity.player.packets.SpecialAttackHandler.class.getDeclaredMethod("processSpecial",Player.class,int.class);method.setAccessible(true);
  for(int id:new int[]{6739,13241,13242,20011,23673,23675}){
   Player p=actor();p.playerEquipment[Player.playerWeapon]=id;p.playerEquipmentN[Player.playerWeapon]=1;p.specAmount=10;p.getWoodcuttingTools().decode("1234,5678");
   ok(io.xeros.content.combat.specials.Specials.forWeaponId(id)!=null,"Special registered "+id);method.invoke(null,p,29074);
   ok(p.specAmount==0&&p.playerLevel[8]==102&&p.getWoodcuttingTools().encode().equals("1234,5678"),"Actual axe special handler boost/cost/no charges "+id);
   p.playerLevel[8]=99;p.specAmount=9;method.invoke(null,p,29074);ok(p.specAmount==9&&p.playerLevel[8]==99,"Insufficient special "+id);
  }
  System.out.println("WOODCUTTING_TOOLS_SIX_ACTUAL_SPECIAL_HANDLER_VARIANTS_WOODCUTTING_PLUS3_FULL_ENERGY_COST_NO_TOOL_CHARGES_AND_LOW_ENERGY_REJECTION_OK");
 }
 public static void main(String[] args){try{
  Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;io.xeros.model.cycleevent.CycleEventHandler.getSingleton().stopEvents(Boolean.FALSE);
  axes();fire();infernal();crystals();actions();specials();saved();System.out.println("WOODCUTTING_TOOLS_ALL_CHECKS_PASSED");System.exit(0);
 }catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
