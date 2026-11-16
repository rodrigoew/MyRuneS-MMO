package io.xeros.content.skills.mining.motherlode;
import io.xeros.Server;import io.xeros.model.collisionmap.*;import io.xeros.model.world.objects.GlobalObject;import io.xeros.model.cycleevent.Event;import io.xeros.util.Misc;
import java.util.*;import java.util.function.IntUnaryOperator;

/** World-wide veins/wheels; independent of how many players are logged in. OpenRune ISC reference. */
public final class MotherlodeWorld {
    public static final MotherlodeWorld INSTANCE=new MotherlodeWorld();
    public static final class Vein {
        public final WorldObject original;public final int activeId;public final boolean upper;
        public boolean active=true;public long generation,expires=-1,respawn=-1;private GlobalObject shown;
        Vein(WorldObject object){original=object;activeId=object.id-4;upper=upper(object.x,object.y);}
    }
    private boolean initialized;private long cycle;private final Map<Long,Vein> veins=new LinkedHashMap<>();
    private static final class Fall {final WorldObject rock;long restore=-1;GlobalObject shown;Fall(WorldObject rock){this.rock=rock;}}
    private final Map<Long,Fall> falls=new LinkedHashMap<>();
    private final boolean[] broken={false,false};private final long[] breakAt=new long[2];private final GlobalObject[] struts=new GlobalObject[2],wheels=new GlobalObject[2];
    private MotherlodeWorld(){}
    private static long key(int x,int y){return ((long)x<<32)|(y&0xffffffffL);}
    public static boolean area(int x,int y,int h){return h==0&&x>=3712&&x<3776&&y>=5632&&y<5696;}
    public void initialize(){
        if(initialized)return;initialized=true;
        for(WorldObject object:RegionProvider.getGlobal().get(3712,5632).worldObjectsSnapshot()){
            if(area(object.x,object.y,object.height)&&(object.id==26679||object.id==26680))falls.putIfAbsent(key(object.x,object.y),new Fall(object));
            if(area(object.x,object.y,object.height)&&object.type==0&&object.id>=26665&&object.id<=26668)
                veins.putIfAbsent(key(object.x,object.y),new Vein(object));
        }
        for(Vein vein:veins.values())publish(vein,vein.activeId);
        for(int index=0;index<2;index++)repair(index,Misc::trueRand);
        Server.getEventHandler().submit(new Event<MotherlodeWorld>("motherlode-world",this,1){public void execute(){MotherlodeWorld.this.tick();}});
    }
    public Collection<Vein> veins(){return Collections.unmodifiableCollection(veins.values());}
    public Vein vein(int x,int y){return veins.get(key(x,y));}
    public long cycle(){return cycle;}
    public boolean flowing(){return !broken[0]||!broken[1];}
    public boolean broken(int index){return broken[index];}
    public void tick(){
        cycle++;
        for(Vein vein:veins.values()){
            if(vein.active&&vein.expires>=0&&cycle>=vein.expires)deplete(vein,Misc::trueRand);
            else if(!vein.active&&cycle>=vein.respawn){vein.active=true;vein.expires=-1;vein.respawn=-1;vein.generation++;publish(vein,vein.activeId);}
        }
        for(int i=0;i<2;i++)if(!broken[i]&&cycle>=breakAt[i])breakStrut(i);
        for(Fall fall:falls.values())if(fall.restore>=0&&cycle>=fall.restore){
            io.xeros.model.entity.player.PlayerHandler.nonNullStream().filter(p->p.isActive&&!p.isDead&&p.heightLevel==0&&p.absX==fall.rock.x&&p.absY==fall.rock.y).forEach(p->{
                p.appendDamage(null,p.getHealth().getCurrentHealth()*4/100+1,io.xeros.content.combat.Hitmark.HIT);
                for(int[] d:new int[][]{{0,1},{1,0},{0,-1},{-1,0}}){int x=p.absX+d[0],y=p.absY+d[1];if((p.getRegionProvider().getClipping(x,y,0)&0x1280100)==0){p.interruptActions();p.moveTo(new io.xeros.model.entity.player.Position(x,y,0));break;}}
            });
            fall.shown=replace(fall.shown,fall.rock,fall.rock.id,fall.rock.face);fall.restore=-1;
        }
    }
    public boolean harvested(Vein vein,IntUnaryOperator random){
        if(!vein.active)return false;
        if(vein.upper){if(vein.expires<0)vein.expires=cycle+25+random.applyAsInt(21);}
        else if(random.applyAsInt(3)==0)deplete(vein,random);
        return vein.active;
    }
    private void deplete(Vein vein,IntUnaryOperator random){
        vein.active=false;vein.expires=-1;vein.generation++;
        vein.respawn=cycle+(vein.upper?100:150+random.applyAsInt(51));publish(vein,vein.original.id);
    }
    private void publish(Vein vein,int id){vein.shown=replace(vein.shown,vein.original,id,vein.original.face);}
    private GlobalObject replace(GlobalObject previous,WorldObject origin,int id,int face){
        if(previous!=null)Server.getGlobalObjects().remove(previous);
        else RegionProvider.getGlobal().get(origin.x,origin.y).removeObject(origin.id,origin.x,origin.y,origin.height,origin.type,origin.face);
        GlobalObject next=new GlobalObject(id,origin.x,origin.y,origin.height,face,origin.type,-1,-1);
        RegionProvider.getGlobal().get(origin.x,origin.y).removeWorldObject(next);Server.getGlobalObjects().add(next);return next;
    }
    public void breakStrut(int index){
        if(broken[index])return;broken[index]=true;int y=index==0?5663:5669;
        struts[index]=replace(struts[index],new WorldObject(26670,3742,y,0,10,2),26670,2);
        wheels[index]=replace(wheels[index],new WorldObject(26672,3743,y-1,0,10,2),26672,2);
    }
    public boolean repair(int index,IntUnaryOperator random){
        if(struts[index]!=null&&!broken[index])return false;
        broken[index]=false;breakAt[index]=cycle+97*(2+random.applyAsInt(3));int y=index==0?5663:5669;
        struts[index]=replace(struts[index],new WorldObject(26670,3742,y,0,10,2),26669,3);
        wheels[index]=replace(wheels[index],new WorldObject(26672,3743,y-1,0,10,2),26671,3);return true;
    }
    public Collection<WorldObject> rockfalls(){List<WorldObject> out=new ArrayList<>();for(Fall fall:falls.values())out.add(fall.rock);return out;}
    public boolean clearRockfall(WorldObject rock,IntUnaryOperator random){
        Fall fall=falls.get(key(rock.x,rock.y));if(fall==null||fall.rock.id!=rock.id||fall.restore>=0)return false;
        fall.shown=replace(fall.shown,fall.rock,-1,fall.rock.face);fall.restore=cycle+80+random.applyAsInt(21);return true;
    }
    /** Upper-floor bridge tiles, adapted from the ISC reference and checked against this map. */
    public static boolean upper(int x,int y){
        switch(y){
            case 5654:return x>=3763&&x<=3765;case 5655:case 5656:case 5657:return x>=3760&&x<=3766;
            case 5658:return x>=3761&&x<=3765;case 5659:return x>=3761&&x<=3764;case 5661:case 5662:case 5663:case 5664:return x>=3761&&x<=3765;
            case 5660:return x>=3761&&x<=3763;case 5665:return x>=3762&&x<=3765;case 5666:return x>=3763&&x<=3765;
            case 5667:return x>=3762&&x<=3764;case 5668:case 5669:case 5670:return x>=3761&&x<=3764;
            case 5671:return x>=3760&&x<=3763;case 5672:return x>=3759&&x<=3763;case 5673:return x>=3757&&x<=3764;
            case 5674:return x>=3755&&x<=3765;case 5675:return x>=3754&&x<=3766;case 5676:case 5677:return x>=3752&&x<=3766;
            case 5678:return x>=3751&&x<=3766;case 5679:return x>=3750&&x<=3765;case 5680:return x>=3747&&x<=3764;
            case 5681:return x>=3735&&x<=3739||x>=3745&&x<=3764;case 5682:return x>=3734&&x<=3764;case 5683:return x>=3733&&x<=3764;
            case 5684:return x>=3733&&x<=3763;case 5685:return x>=3733&&x<=3748||x>=3750&&x<=3761;
            case 5686:return x>=3733&&x<=3736||x>=3740&&x<=3747||x>=3757&&x<=3758;case 5687:return x>=3743&&x<=3744;
            default:return false;
        }
    }
}
