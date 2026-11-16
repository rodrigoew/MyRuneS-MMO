package io.xeros.content.skills.fletching;
import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import java.util.function.BooleanSupplier;
/** Each action consumes only the available matched pairs, including the final partial set. */
public final class FletchingAmmo {
 private FletchingAmmo(){}
 private static int available(Player p,int a,int b,int size){return Math.min(size,Math.min(p.getItems().getInventoryCount(a),p.getItems().getInventoryCount(b)));}
 public static boolean once(Player p,int level,double xp,int a,int b,int product,int size){
  if(!p.getUtilityMagic().canInteract()||p.playerLevel[9]<level)return false;
  p.interruptActions();p.getPA().stopSkilling();
  return make(p,xp,a,b,product,available(p,a,b,size));
 }
 private static boolean make(Player p,double xp,int a,int b,int product,int n){
  if(n<=0||!ProductionBatch.exchange(p,new int[][]{{a,n},{b,n}},new int[][]{{product,n}}))return false;
  p.getPA().addSkillXPMultiplied(xp*n,9,true);return true;
 }
 public static boolean start(Player p,int level,double xp,int a,int b,int product,int size,int sets,BooleanSupplier valid){
  if(size<=0||sets<=0||!p.getUtilityMagic().canInteract()||p.playerLevel[9]<level||!valid.getAsBoolean()||available(p,a,b,size)<=0)return false;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
  long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;p.playerSkilling[9]=true;
  Server.getEventHandler().submit(new Event<Player>("skilling",p,2){
   int left=sets;
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[9]||!p.getUtilityMagic().canInteract()||p.absX!=x||p.absY!=y||p.heightLevel!=h||p.playerLevel[9]<level||!valid.getAsBoolean()
      ||!make(p,xp,a,b,product,available(p,a,b,size))){stop();return;}
    if(--left<=0||available(p,a,b,size)<=0)stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[9]=false;p.startAnimation(65535);}}
  });return true;
 }
}
