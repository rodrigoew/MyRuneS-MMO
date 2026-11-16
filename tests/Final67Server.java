package io.xeros.solo;
import io.xeros.model.entity.player.*;
public final class Final67Server {
 public static void main(String[]args)throws Exception{
  Thread fixture=new Thread(()->{try{for(int i=0;i<600;i++){Thread.sleep(500);Player p=PlayerHandler.getPlayerByLoginName("final67qa");if(p!=null&&p.isActive){PlayerHandler.addQueuedAction(()->{p.getRights().setPrimary(Right.OWNER);p.getPA().closeAllWindows();p.sendMessage("FINAL_QA_READY");});break;}}}catch(Exception e){e.printStackTrace();}},"Fixture");fixture.setDaemon(true);fixture.start();SoloServer.main(args);
 }
}
