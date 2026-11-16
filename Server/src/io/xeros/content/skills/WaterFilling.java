package io.xeros.content.skills;

import io.xeros.Server;
import io.xeros.model.collisionmap.ObjectDef;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import java.util.Locale;

/** Ordinary clean-water sources; wells and special fluids need their own interactions. */
public final class WaterFilling {
    private WaterFilling() {}
    public static boolean wateringCan(int item){return item==5331||item>=5333&&item<=5340;}
    public static boolean source(int object){
        if(object==15936)return true;
        ObjectDef definition=ObjectDef.getObjectDef(object);if(definition==null||definition.name==null)return false;
        String name=definition.name.toLowerCase(Locale.ROOT);
        return name.equals("sink")||name.equals("waterpump")||name.equals("water pump")||name.equals("fountain");
    }
    private static int output(int item){return wateringCan(item)?5340:item==1925?1929:item==1935?1937:item==1923?1921:item==229?227:-1;}
    public static boolean use(Player p,int item,int object,int x,int y){
        int product=output(item);if(product<0||!source(object))return false;
        if(object!=15936&&!RegionProvider.getGlobal().get(x,y).getWorldObject(object,x,y,p.heightLevel).isPresent())return true;
        if(p.isDead||p.getLock().cannotInteract(p)||Math.max(Math.abs(p.absX-x),Math.abs(p.absY-y))>3)return true;
        if(item==5340){p.sendMessage("Your watering can is already full.");return true;}
        if(!p.getItems().playerHasItem(item))return true;
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        final long token=p.productionActionToken;final int originX=p.absX,originY=p.absY,height=p.heightLevel;
        p.startAnimation(2295);p.facePosition(x,y);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,3){
            public void execute(){
                if(token!=p.productionActionToken||p.isDisconnected()||p.getSession()==null||p.isDead
                        ||p.getLock().cannotInteract(p)||p.absX!=originX||p.absY!=originY||p.heightLevel!=height){stop();return;}
                int next=item;
                if(wateringCan(item)){
                    next=-1;
                    for(int id:new int[]{5331,5333,5334,5335,5336,5337,5338,5339})if(p.getItems().playerHasItem(id)){next=id;break;}
                    if(next<0){stop();return;}
                }
                if(!ProductionBatch.exchange(p,new int[][]{{next,1}},new int[][]{{product,1}})){stop();return;}
                p.startAnimation(2295);
                p.sendMessage(wateringCan(item)?"You fill the watering can.":"You fill the container with water.");
            }
            public void stop(){super.stop();if(token==p.productionActionToken)p.startAnimation(65535);}
        });
        return true;
    }
}
