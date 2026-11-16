package io.xeros.content.questing;

import io.xeros.Server;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** Dynamic quest scenery is leased, never removed by ID from somebody else's scene. */
final class EaglesPeakScene {
    private static EaglesPeakScene occupant;
    private final Map<String,GlobalObject> objects=new LinkedHashMap<>();
    private final EaglesPeak quest;
    EaglesPeakScene(EaglesPeak quest){this.quest=quest;}
    boolean claim(){if(occupant!=null&&occupant!=this)return false;occupant=this;return true;}
    void bronze(EaglesPeakPuzzle state){
        if(!claim())return;
        if(!state.bronzeTriggered)put("bronze",19980,1973,4914,2,0,10);
        else put("bronze",state.bronzeRaised?19981:19984,1974,4915,2,0,10);
    }
    void gold(EaglesPeakPuzzle state){
        if(!claim())return;
        // Object anchors fit the two-tile rails in the supplied cache. Positions are
        // reconstructed for this server; the historical helper supplies the lever/feeder states.
        bird("bird1",state.moved(0)?1967:1971,4890,0);
        bird("bird2",1963,state.moved(1)?4894:4900,1);
        bird("bird3",1945,state.moved(2)?4903:4906,1);
        bird("bird4",state.moved(3)?1943:1931,4914,0);
        bird("bird5",state.goldWestVisited&&!state.moved(4)?1935:1945,4898,0);
        wings("east",1959,4900,0,!state.down(4));
        wings("northwest",1931,4917,1,state.down(4)^state.down(8)^state.down(1));
        wings("south",1945,4902,1,!state.down(1));
        wings("gold",1939,4906,0,!state.down(2));
    }
    private void bird(String key,int x,int y,int face){put(key,19935,x,y,2,face,10);}
    private void wings(String key,int x,int y,int face,boolean closed){
        put(key+"a",closed?19870:-1,x,y,2,face,0);
        put(key+"b",closed?19871:-1,x+(face==1?1:0),y+(face==0?1:0),2,face,0);
    }
    void put(String key,int id,int x,int y,int h,int face,int type){
        GlobalObject previous=objects.get(key);
        if(previous!=null&&previous.getObjectId()==id&&previous.getX()==x&&previous.getY()==y&&previous.getHeight()==h&&previous.getFace()==face&&previous.getType()==type)return;
        if(previous!=null)Server.getGlobalObjects().removeIfCurrent(previous);
        if(id<0){objects.remove(key);return;}
        GlobalObject object=new GlobalObject(id,x,y,h,face,type,-1);Server.getGlobalObjects().add(object);objects.put(key,object);
    }
    void release(){for(GlobalObject object:objects.values())Server.getGlobalObjects().removeIfCurrent(object);objects.clear();if(occupant==this)occupant=null;}
    boolean owns(int id,int x,int y,int height){for(GlobalObject o:objects.values())if(o.getObjectId()==id&&o.getX()==x&&o.getY()==y&&o.getHeight()==height)return true;return false;}
}
