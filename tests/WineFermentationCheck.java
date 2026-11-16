package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.cooking.WineFermentation;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;import io.xeros.model.items.bank.*;import java.util.*;import java.util.concurrent.atomic.AtomicLong;
public class WineFermentationCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();p.setLoginName("wineauditqa");p.setDisplayName("Wineauditqa");return p;}
 static int count(Player p,int item){return SkillsAuditCheck.count(p,item);}
 static void give(Player p,int item,int n){SkillsAuditCheck.item(p,item,n);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static long bankCount(Player p,int id){long total=0;for(BankTab tab:p.getBank().getBankTab())for(BankItem item:tab.getItems())if(item.getId()==id+1)total+=item.getAmount();return total;}
 static void mixAndReset(){
  Player p=actor();AtomicLong time=new AtomicLong(1_000_000);WineFermentation wine=new WineFermentation(p,time::get);give(p,1987,2);give(p,1937,2);int old=p.playerXP[7];p.playerLevel[7]=34;
  ok(!wine.mix(1),"Wine below level 35");tick(2);ok(count(p,1987)==2&&count(p,1937)==2&&count(p,1995)==0,"Rejected recipe consumed materials");
  p.playerLevel[7]=99;ok(wine.mix(1),"Wine mix");tick(2);ok(count(p,1995)==1&&count(p,1987)==1&&count(p,1937)==1&&p.playerXP[7]==old,"Wine initial recipe or premature XP");long first=wine.deadline();
  time.set(first-1);ok(!wine.process()&&count(p,1995)==1,"Fermentation before 12 seconds");wine.mix(1);tick(2);ok(count(p,1995)==2&&wine.deadline()==time.get()+12000,"New jug did not reset whole batch timer");
  time.set(wine.deadline()-1);ok(!wine.process(),"Reset boundary early");time.incrementAndGet();ok(wine.process()&&count(p,1995)==0&&count(p,1993)==2,"Fermentation at boundary");ExpandedSkillsCheck.xp(p,7,old,400);old=p.playerXP[7];ok(!wine.process()&&p.playerXP[7]==old,"Repeated fermentation duplicated XP");clear(p);
  System.out.println("WINE_LEVEL_MATERIALS_NO_PREMATURE_XP_12_SECONDS_BATCH_RESET_BOUNDARY_DUPLICATE_OK");
 }
 static void bankAndFull(){
  Player p=actor();AtomicLong time=new AtomicLong(1000);WineFermentation wine=new WineFermentation(p,time::get);for(int i=0;i<9;i++)p.getBank().getBankTab()[i].add(new BankItem(1996,i+1));p.getBank().getBankTab()[5].add(new BankItem(1994,2));give(p,1995,3);wine.decode("1000,1");int old=p.playerXP[7];ok(wine.process(),"Bank fermentation");ok(count(p,1993)==3&&bankCount(p,1993)==47&&bankCount(p,1995)==0,"All nine bank tabs and inventory");ExpandedSkillsCheck.xp(p,7,old,48*200);
  p=actor();p.playerLevel[7]=35;wine=new WineFermentation(p,time::get);BankTab tab=p.getBank().getBankTab()[0];tab.add(new BankItem(1996,100));int capacity=p.getBank().getBankCapacity();for(int i=0;i<capacity-1;i++)tab.add(new BankItem(10000+i,1));ok(p.getBank().getItemCount()==capacity,"Full bank fixture");wine.decode("1000,1");old=p.playerXP[7];ok(wine.process(),"Full bank fermentation lost or deferred items");long good=bankCount(p,1993),bad=bankCount(p,1991);ok(good>0&&bad>0&&good+bad==100&&bankCount(p,1995)==0&&p.getBank().getItemCount()==capacity+1,"Good/bad results in temporarily overfull bank");ExpandedSkillsCheck.xp(p,7,old,good*200);
  ok(p.getBank().hasRoomFor(new BankItem(1994,1)),"Existing stack rejected in full bank");ok(!p.getBank().hasRoomFor(new BankItem(996,1)),"New item accepted over capacity");
  Player q=actor();BankTab qt=q.getBank().getBankTab()[0];qt.add(new BankItem(996,100));int cap=q.getBank().getBankCapacity();for(int i=0;i<cap-1;i++)qt.add(new BankItem(10000+i,1));give(q,995,10);ok(q.getItems().addToBank(995,10,false,true)&&count(q,995)==0&&bankCount(q,995)==110,"Deposit into existing stack at capacity");
  System.out.println("WINE_NINE_BANK_TABS_INVENTORY_FULL_BANK_GOOD_BAD_CONSERVATION_AND_EXISTING_STACK_DEPOSIT_OK");
 }
 static void overflow(){
  Player p=actor();AtomicLong time=new AtomicLong(1000);WineFermentation wine=new WineFermentation(p,time::get);BankTab tab=p.getBank().getBankTab()[0];tab.add(new BankItem(1994,Integer.MAX_VALUE));tab.add(new BankItem(1996,1));wine.decode("1000,1");int old=p.playerXP[7];ok(!wine.process()&&bankCount(p,1993)==Integer.MAX_VALUE&&bankCount(p,1995)==1&&p.playerXP[7]==old,"Overflow destroyed wine or granted XP");
  tab.remove(new BankItem(1994,1),0,false);ok(wine.process()&&bankCount(p,1993)==Integer.MAX_VALUE&&bankCount(p,1995)==0,"Overflow recovery");
  boolean rejected=false;try{tab.add(new BankItem(1994,1));}catch(ArithmeticException e){rejected=true;}ok(rejected&&bankCount(p,1993)==Integer.MAX_VALUE,"BankTab arithmetic overflow corrupted previous amount");
  String before=wine.encode();ok(!wine.decode("-1,0")&&!wine.decode("abc")&&wine.encode().equals(before),"Invalid fermentation state changed existing state");
  System.out.println("WINE_BANK_INTEGER_OVERFLOW_ATOMIC_REJECTION_RECOVERY_AND_MALFORMED_SAVE_OK");
 }
 static void persistence()throws Exception{
  Player p=actor();give(p,1995,2);p.getBank().getBankTab()[2].add(new BankItem(1996,3));p.getWineFermentation().decode((System.currentTimeMillis()-1)+",1");int old=p.playerXP[7];p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;
  try{ok(PlayerSave.saveGameInstant(p),"Wine save");Player q=actor();ok(PlayerSave.loadGame(q,"wineauditqa",p.playerPass,true).toString().equals("SUCCESS"),"Wine reload");ok(q.getWineFermentation().deadline()>0&&count(q,1995)==2&&bankCount(q,1995)==3,"Pending state not preserved");PlayerSave.login(q);ok(count(q,1993)==2&&bankCount(q,1993)==3&&q.getWineFermentation().deadline()==0,"Offline fermentation on login");ExpandedSkillsCheck.xp(q,7,old,1000);int after=q.playerXP[7];PlayerSave.login(q);ok(q.playerXP[7]==after,"Relogin repeated XP");}
  finally{PlayerHandler.players[1]=null;clear(p);}
  for(int level=68;level<=99;level++)ok(WineFermentation.successful(1000,level,new SplittableRandom(1))==1000,"Wine failed at level "+level);
  System.out.println("WINE_REAL_CHARACTER_SAVE_LOAD_LOGIN_OFFLINE_XP_ONCE_AND_LEVEL_68_PLUS_NO_FAILURES_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;mixAndReset();bankAndFull();overflow();persistence();System.out.println("WINE_FERMENTATION_ALL_CHECKS_OK");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
