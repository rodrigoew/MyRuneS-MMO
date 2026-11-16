package io.xeros.content.instances;

import io.xeros.model.collisionmap.*;
import io.xeros.model.entity.player.*;
import io.xeros.util.Stream;
import java.util.*;

/** Packet-241 scene assembled from native 8x8 map chunks with independent collision. */
public final class DynamicMapScene extends InstancedArea {
    public static final class Chunk {
        public final int sourceX,sourceY,sourcePlane,rotation;
        public Chunk(int x,int y,int plane,int rotation){
            if(x<0||x>1023||y<0||y>2047||plane<0||plane>3||rotation<0||rotation>3)throw new IllegalArgumentException("Map chunk");
            sourceX=x;sourceY=y;sourcePlane=plane;this.rotation=rotation;
        }
        public int packed(){return sourcePlane<<24|sourceX<<14|sourceY<<3|rotation<<1;}
    }
    private final Player owner;
    private final int baseX,baseY;
    private final Map<Long,Chunk> chunks=new HashMap<>();
    private final List<WorldObject> overrides=new ArrayList<>();
    private boolean dirty=true;
    private java.util.function.BiPredicate<WorldObject,Integer> click;
    private Runnable close;
    public DynamicMapScene(Player owner,int baseX,int baseY){
        super(InstanceConfiguration.CLOSE_ON_EMPTY,0,new Boundary(baseX-16,baseY-16,baseX+88,baseY+88));
        if(owner==null||baseX%64!=0||baseY%64!=0)throw new IllegalArgumentException("Scene anchor");
        this.owner=owner;this.baseX=baseX;this.baseY=baseY;
        for(int h=0;h<4;h++)for(int x=baseX-16;x<baseX+88;x++)for(int y=baseY-16;y<baseY+88;y++)
            get(x,y).setClipFlags(x,y,h,0x200000,0x200000);
    }
    private static long key(int x,int y,int plane){return (long)plane<<40|(long)x<<20|y;}
    public Chunk chunk(int x,int y,int plane){return chunks.get(key(x,y,plane));}
    public int size(){return chunks.size();}
    public static int rotateFlags(int flags,int rotation){
        int result=flags&~(0xff|0x1fe00|RegionProvider.NPC_TILE_FLAG);
        int walking=flags&0xff,projectile=(flags>>>9)&0xff;
        for(int i=0;i<rotation;i++){walking=(walking<<2|walking>>>6)&255;projectile=(projectile<<2|projectile>>>6)&255;}
        return result|walking|projectile<<9;
    }
    public static int[] rotateTile(int x,int y,int rotation){
        switch(rotation){case 0:return new int[]{x,y};case 1:return new int[]{y,7-x};case 2:return new int[]{7-x,7-y};case 3:return new int[]{7-y,x};default:throw new IllegalArgumentException("Rotation");}
    }
    private static int[] rotateObject(int x,int y,int width,int length,int rotation){
        switch(rotation){case 0:return new int[]{x,y};case 1:return new int[]{y,7-x-(width-1)};case 2:return new int[]{7-x-(width-1),7-y-(length-1)};case 3:return new int[]{7-y-(length-1),x};default:throw new IllegalArgumentException("Rotation");}
    }
    /** Destination x/y are room cells relative to the scene anchor. */
    public void place(int x,int y,int plane,Chunk chunk){
        place(x,y,plane,chunk,false);
    }
    /** Project the map before server-added skilling overlays, with matching collision. */
    public void placeBaseMap(int x,int y,int plane,Chunk chunk){
        place(x,y,plane,chunk,true);
    }
    private void place(int x,int y,int plane,Chunk chunk,boolean baseMap){
        if(x<0||x>8||y<0||y>8||plane<0||plane>3||chunk==null)throw new IllegalArgumentException("Destination room");
        int dx=baseX+x*8,dy=baseY+y*8,sx=chunk.sourceX*8,sy=chunk.sourceY*8;
        if(chunks.putIfAbsent(key(dx>>3,dy>>3,plane),chunk)!=null)throw new IllegalStateException("Duplicate room cell");
        RegionProvider source=RegionProvider.getGlobal();
        for(int tx=0;tx<8;tx++)for(int ty=0;ty<8;ty++){
            int[] pos=rotateTile(tx,ty,chunk.rotation);int px=dx+pos[0],py=dy+pos[1];
            Region base=baseMap?Region.getBaseMapRegion(sx+tx,sy+ty):source.get(sx+tx,sy+ty);
            get(px,py).setClipFlags(px,py,plane,rotateFlags(base.getClip(sx+tx,sy+ty,chunk.sourcePlane),chunk.rotation),
                rotateFlags(base.getProjectileClip(sx+tx,sy+ty,chunk.sourcePlane),chunk.rotation));
        }
        for(WorldObject o:(baseMap?Region.getBaseMapRegion(sx,sy):source.get(sx,sy)).worldObjectsSnapshot()){
            if(o.x<sx||o.x>=sx+8||o.y<sy||o.y>=sy+8||o.height!=chunk.sourcePlane||o.type<0||o.type>22)continue;
            var size=o.getObjectSize();int[] pos=rotateObject(o.x-sx,o.y-sy,size.getX(),size.getY(),chunk.rotation);
            WorldObject projected=new WorldObject(o.id,dx+pos[0],dy+pos[1],plane,o.type,(o.face+chunk.rotation)&3);
            get(projected.x,projected.y).addWorldObject(projected);
        }dirty=true;
    }
    public boolean activeFor(Player p){return !isDisposed()&&p==owner&&p.getInstance()==this;}
    public void onClick(java.util.function.BiPredicate<WorldObject,Integer> handler){click=handler;}
    public void onClose(Runnable handler){close=handler;}
    @Override public boolean handleClickObject(Player p,WorldObject object,int option){return activeFor(p)&&click!=null&&click.test(object,option);}
    /** Overlay a native object, updating this scene's interaction and collision maps together. */
    public void replace(WorldObject original,int replacement){
        if(original==null||replacement< -1||get(original.x,original.y).getWorldObject(original.id,original.x,original.y,original.height).isEmpty())throw new IllegalArgumentException("Scene object");
        overlayAt(original,replacement);
    }
    /** Restore or change an owned slot, including a currently hidden multi-object part. */
    public void overlayAt(WorldObject anchor,int replacement){
        if(anchor==null||replacement< -1||chunk(anchor.x>>3,anchor.y>>3,anchor.height)==null)throw new IllegalArgumentException("Scene slot");
        var region=get(anchor.x,anchor.y);
        for(WorldObject current:region.worldObjectsSnapshot())if(current.x==anchor.x&&current.y==anchor.y&&current.height==anchor.height&&current.type==anchor.type){
            region.removeObject(current.id,current.x,current.y,current.height,current.type,current.face);
            region.removeWorldObject(new io.xeros.model.world.objects.GlobalObject(current.id,current.x,current.y,current.height,current.face,current.type));
        }
        WorldObject target=new WorldObject(replacement,anchor.x,anchor.y,anchor.height,anchor.type,anchor.face);
        if(replacement>=0){region.addObject(replacement,target.x,target.y,target.height,target.type,target.face);region.addWorldObject(target);}
        overrides.removeIf(o->o.x==target.x&&o.y==target.y&&o.height==target.height&&o.type==target.type);overrides.add(target);
    }
    public void sendOverrides(Player p){if(!activeFor(p))return;for(WorldObject o:overrides)if(o.height==p.heightLevel&&Math.abs(o.x-p.absX)<104&&Math.abs(o.y-p.absY)<104)p.getPA().object(o.id,o.x,o.y,o.face,o.type,false);}
    public boolean dirty(){return dirty;}
    public void activate(Position position){
        if(owner.getInstance()!=null&&owner.getInstance()!=this)throw new IllegalStateException("Leave the current instance first");
        owner.getRegionProvider().removeNpcClipping(RegionProvider.NPC_TILE_FLAG,owner.absX,owner.absY,owner.heightLevel);
        add(owner);owner.setDynamicMapScene(this);owner.moveTo(position);dirty=true;
    }
    public void sendRegion(Player p,Stream stream){
        if(!activeFor(p))throw new IllegalStateException("Scene owner");
        stream.createFrameVarSizeWord(241);stream.writeWordA(p.mapRegionY+6);stream.initBitAccess();
        for(int h=0;h<4;h++)for(int x=0;x<13;x++)for(int y=0;y<13;y++){
            Chunk chunk=chunk(p.mapRegionX+x,p.mapRegionY+y,h);stream.writeBits(1,chunk==null?0:1);if(chunk!=null)stream.writeBits(26,chunk.packed());
        }
        stream.finishBitAccess();stream.writeWord(p.mapRegionX+6);stream.endFrameVarSizeWord();dirty=false;
    }
    public void markDirty(){dirty=true;}
    @Override public void onDispose(){if(owner.getDynamicMapScene()==this)owner.setDynamicMapScene(null);if(close!=null)close.run();}
}
