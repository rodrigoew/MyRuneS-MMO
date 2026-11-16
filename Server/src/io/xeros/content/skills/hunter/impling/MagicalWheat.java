package io.xeros.content.skills.hunter.impling;

import io.xeros.Server;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.*;
import java.util.Random;

/** The two independent September 2022 efficiency rolls, followed by a timed crossing. */
public final class MagicalWheat {
    public static final Random RANDOM=new Random();
    private MagicalWheat(){}
    public static boolean wheat(int id){return id>=25016&&id<=25021||id>=25024&&id<=25029;}
    public static int threshold(int low,int high,int level){level=Math.max(1,Math.min(126,level));return Math.max(0,Math.min(256,low*(99-level)/98+high*(level-1)/98+1));}
    /** 2=efficient, 1=normal, 0=hard work. The second roll occurs only if the first fails. */
    public static int efficiency(int strength,Random random){if(random.nextInt(256)<threshold(0,100,strength))return 2;return random.nextInt(256)<threshold(10,256,strength)?1:0;}
    public static int ticks(int efficiency,boolean affinity){return 10-2*efficiency-(affinity?3:0);}
    public static boolean click(Player p,WorldObject object,int option){
        if(object==null||!wheat(object.id))return false;
        if(option!=1||!Impling.puro(p.absX,p.absY,p.heightLevel)||!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||Server.getMultiplayerSessionListener().inAnySession(p))return true;
        if(p.playerLevel[21]<17){p.sendMessage("You need Hunter level 17 to enter the Puro-Puro maze.");return true;}
        if(object.height!=0||p.getRegionProvider().get(object.x,object.y).getWorldObject(object.id,object.x,object.y,0).filter(object::equals).isEmpty())return true;
        int dx=object.x-p.absX,dy=object.y-p.absY;if(Math.abs(dx)+Math.abs(dy)!=1)return true;
        int tx=p.absX+dx*2,ty=p.absY+dy*2;
        if(!Impling.puro(tx,ty,0)||(p.getRegionProvider().getClipping(tx,ty,0)&0x1280100)!=0
            ||!ImplingWorld.canFly(p.getRegionProvider(),p.absX,p.absY,0,dx,dy)
            ||!ImplingWorld.canFly(p.getRegionProvider(),object.x,object.y,0,dx,dy))return true;
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.stopMovement();p.attacking.reset();
        int quality=efficiency(p.playerLevel[2],RANDOM),duration=ticks(quality,p.getElnock().affinity());
        long token=p.productionActionToken;int sx=p.absX,sy=p.absY;PlayerMovementState previous=p.getMovementState(),locked=new PlayerMovementState(false,false,true);p.setMovementState(locked);
        p.facePosition(tx,ty);p.startAnimation(6594);p.showForceMovement(dx*2,dy*2,duration);
        Server.getEventHandler().submit(new Event<Player>("puro_wheat",p,duration){
            private boolean valid(){return token==p.productionActionToken&&!p.isDisconnected()&&!p.isDead&&p.getHealth().getCurrentHealth()>0&&p.getSession()!=null&&p.absX==sx&&p.absY==sy&&p.heightLevel==0&&p.getMovementState()==locked;}
            public void update(){if(!valid())stop();}
            public void execute(){if(valid()){p.moveTo(new Position(tx,ty,0));if(quality>0&&p.getElnock().strengthXp())p.getPA().addSkillXPMultiplied(quality*2,2,true);p.sendMessage(quality==2?"You push through the wheat efficiently.":quality==1?"You push through the wheat.":"You push through the wheat with difficulty.");}stop();}
            public void stop(){super.stop();if(p.getMovementState()==locked)p.setMovementState(previous);p.startAnimation(65535);}
        });return true;
    }
}
