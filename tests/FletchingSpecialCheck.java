package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.*;
import io.xeros.content.skills.fletching.*;
import io.xeros.content.dialogue.*;
import io.xeros.content.items.UseItem;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.definitions.ItemDef;
import java.util.*;
public final class FletchingSpecialCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){Player p=AttackDefencePlayableCheck.actor();p.setTeleportToX(-1);p.setTeleportToY(-1);return p;}
 static void give(Player p,int id,int n){SkillsAuditCheck.item(p,id,n);}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static long xp(Player p){return 10L*p.playerXP[9]+p.playerXpTenths[9];}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void select(Player p){ok(p.getDialogueBuilder()!=null,"Recipe menu absent");p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_1);}
 static FletchingRecipes.Recipe recipe(int product){return FletchingRecipes.recipes().stream().filter(r->r.product==product).findFirst().orElseThrow(()->new AssertionError("Special recipe absent "+product));}
 static final int[][] FACTS={{10105,0,1755,10158,6,32,58,2},{10107,0,1755,10159,6,42,79,2},{9416,9142,0,9418,1,59,110,2},{9418,954,0,9419,1,59,0,1}};
 static void fixtures(Player p,int[] f,int amount){give(p,f[0],amount);if(f[1]>0)give(p,f[1],amount);if(f[2]>0)give(p,f[2],1);}
 static void recipes(){for(int[] f:FACTS){FletchingRecipes.Recipe r=recipe(f[3]);ok(r.first==f[0]&&r.second==f[1]&&r.tool==f[2]&&r.quantity==f[4]&&r.level==f[5]&&Math.round(r.xp*10)==f[6]&&r.ticks==f[7],"Independent2022 recipe "+f[3]);
  for(int id:new int[]{f[0],f[1],f[2],f[3]})if(id>0)ok(ItemDef.getDefinitions().containsKey(id)&&!ItemDef.forId(id).isNoted(),"Real unnoted item "+id);
  Player p=actor();fixtures(p,f,2);long before=xp(p);p.playerLevel[9]=f[5]-1;ok(!FletchingRecipes.make(p,r,1),"Below level accepted");tick(r.ticks);ok(xp(p)==before&&count(p,f[0])==2,"Level failure consumed");p.playerLevel[9]=f[5];
  ok(FletchingRecipes.make(p,r,2),"Exact level start");tick(r.ticks*2);ok(count(p,f[3])==2*f[4]&&count(p,f[0])==0&&(f[1]==0||count(p,f[1])==0),"Two complete products "+f[3]);ok(xp(p)-before==f[6]*10L,"Per action XP at5x "+f[3]+" actual="+(xp(p)-before)+" expected="+(f[6]*10L));if(f[2]>0)ok(count(p,f[2])==1,"Tool consumed");clear(p);
  p=actor();fixtures(p,f,2);before=xp(p);ok(FletchingRecipes.make(p,r,2),"Interrupt fixture");p.interruptActions();tick(r.ticks*3);ok(count(p,f[0])==2&&xp(p)==before,"Interrupted output");clear(p);
  p=actor();fixtures(p,f,1);before=xp(p);ok(FletchingRecipes.make(p,r,2),"Exhaustion fixture");tick(r.ticks*3);ok(count(p,f[3])==f[4]&&xp(p)-before==f[6]*5L,"No extra output/XP on exhaustion");clear(p);
  if(f[2]>0){p=actor();give(p,f[0],1);before=xp(p);FletchingRecipes.make(p,r,1);tick(r.ticks);ok(count(p,f[0])==1&&count(p,f[3])==0&&xp(p)==before,"Absent chisel");clear(p);}
 }
 System.out.println("FLETCHING_SPECIAL_FOUR_2022_RECIPES_LEVELS_TOOLS_PER_COMPONENT_VS_PER_SPIKE_XP_QUANTITIES_INTERRUPTION_AND_NO_EXTRA_OUTPUT_OK");}
 static void actualRoutes(){for(int[] f:FACTS){Player p=actor();fixtures(p,f,1);int a=f[1]>0?f[0]:f[2],b=f[1]>0?f[1]:f[0];long before=xp(p);UseItem.ItemonItem(p,b,a,p.getItems().getInventoryItemSlot(b),p.getItems().getInventoryItemSlot(a));select(p);select(p);tick(f[7]);ok(count(p,f[3])==f[4]&&xp(p)-before==f[6]*5L,"Actual reverse item-on-item and quantity route "+f[3]);clear(p);}
 Player p=actor();give(p,9416,1);give(p,9142,1);give(p,954,1);long before=xp(p);FletchingRecipes.make(p,recipe(9418),1);tick(2);ok(count(p,9418)==1&&count(p,954)==1,"Grapple first stage");FletchingRecipes.make(p,recipe(9419),1);tick(1);ok(count(p,9419)==1&&count(p,9418)==0&&count(p,954)==0&&xp(p)-before==550,"Grapple full chain no second-stage XP");clear(p);
 System.out.println("FLETCHING_SPECIAL_REAL_REVERSE_ITEM_ON_ITEM_DIALOG_QUANTITY_FOUR_ROUTES_AND_MITH_GRAPPLE_FULL_CHAIN_NO_SECOND_XP_OK");}
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;java.lang.reflect.Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,java.util.Collections.emptyList());recipes();actualRoutes();System.out.println("FLETCHING_SPECIAL_ALL_CHECKS_PASSED");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
