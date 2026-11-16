package io.xeros.content.combat.magic;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.instances.DynamicMapScene;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.lock.PlayerLock;

/** Ten native maze layouts; coordinates independently checked against native-map collision. */
public final class MtaMaze {
 // plane, statue x/y, goal x/y, north/south/west/east, player x/y, tutor x/y (region offsets).
 private static final int[][] LAYOUTS={
  {0,15,41,19,50,51,40,9,20,8,54,8,39},{1,22,53,13,44,54,43,12,23,34,49,26,48},
  {1,48,22,57,22,23,12,47,58,54,34,45,15},{1,46,49,55,49,59,48,45,56,50,42,57,41},
  {0,45,14,47,18,19,8,40,51,46,32,45,23},{0,15,16,14,20,21,10,9,20,26,26,23,24},
  {0,40,48,39,56,57,46,37,48,51,52,55,50},{2,18,54,17,54,55,44,14,25,31,37,28,41},
  {2,20,10,11,19,20,9,10,21,40,16,34,15},{1,23,20,25,16,26,15,17,28,27,29,31,25}};
 private final MageTrainingArena arena;private final Player p;
 private NPC statue;private int layout=-1;private boolean busy,solved,observing;
 public MtaMaze(MageTrainingArena a,Player p){arena=a;this.p=p;}
 public NPC statue(){return statue;}public int layout(){return layout;}public boolean busy(){return busy;}public boolean solved(){return solved;}
 public void start(){int previous=layout;do{layout=io.xeros.util.Misc.random(9);}while(layout==previous);open();}
 private void open(){
  busy=false;solved=false;observing=false;p.getPA().resetCamera();int[] f=LAYOUTS[layout];
  DynamicMapScene next=new DynamicMapScene(p,MageTrainingArena.BASE,MageTrainingArena.BASE);
  for(int x=0;x<8;x++)for(int y=0;y<8;y++)next.placeBaseMap(x,y,0,new DynamicMapScene.Chunk(416+x,1208+y,f[0],0));
  arena.replaceMaze(next);int b=MageTrainingArena.BASE;next.activate(new Position(b+f[9],b+f[10],0));
  statue=NPCSpawning.spawnNpc(p,6777,b+f[1],b+f[2],0,0,-1,false,false);
  NPCSpawning.spawnNpc(p,5979,b+f[11],b+f[12],0,0,-1,false,false);arena.refresh();
 }
 private boolean live(NPC n){return arena.room()==0&&n==statue&&n!=null&&!n.isUnregister()&&n.getInstance()==p.getInstance()&&p.heightLevel==0;}
 /** Observe/reset and the completed guardian work across the maze wall, without following into it. */
 public boolean npc(NPC n,int option){
  if(!live(n))return false;
  if(busy||p.getLock().cannotInteract(p))return true;
  p.stopMovement();
  if(option==3&&!solved){int[] f=LAYOUTS[layout];statue.teleport(MageTrainingArena.BASE+f[1],MageTrainingArena.BASE+f[2],0);return true;}
  if(option==1&&solved){final NPC expected=n;new DialogueBuilder(p).option("Continue your training?",new DialogueOption("Yes, give me another maze",q->{q.getPA().removeAllWindows();if(live(expected)&&solved)start();}),new DialogueOption("Leave the theatre",q->{q.getPA().removeAllWindows();if(live(expected))arena.leave();})).send();return true;}
  if(option==1){observing=!observing;if(!observing)p.getPA().resetCamera();else observe();return true;}return false;
 }
 private void observe(){
  int[] f=LAYOUTS[layout];int cx=MageTrainingArena.BASE+(f[7]+f[8])/2,cy=MageTrainingArena.BASE+(f[5]+f[6])/2;
  var s=p.getOutStream();s.createFrame(166);s.writeByte(cx-p.mapRegionX*8);s.writeByte(cy-7-p.mapRegionY*8);s.writeWord(1200);s.writeByte(1);s.writeByte(100);
  s.createFrame(177);s.writeByte(cx-p.mapRegionX*8);s.writeByte(cy-p.mapRegionY*8);s.writeWord(0);s.writeByte(1);s.writeByte(100);
 }
 public boolean cast(NPC n,int widget){
  if(!live(n))return false;
  if(widget!=1168||busy||solved||p.playerMagicBook!=0||p.playerLevel[6]<33||p.isDead||p.isDisconnected()||p.getSession()==null||p.getLock().cannotInteract(p)||p.getMovementState().isLocked()||p.isBanking||p.isShopping||p.teleTimer>0)return true;
  if(p.getBankPin().requiresUnlock()){p.getBankPin().open(2);return true;}
  int b=MageTrainingArena.BASE;int[] f=LAYOUTS[layout];int x=p.absX-b,y=p.absY-b,dx=0,dy=0;
  // A cardinal side determines the pull. Corner casts intentionally spend runes without moving.
  if(x>f[7]&&x<f[8]){if(y>=f[5])dy=1;else if(y<=f[6])dy=-1;}
  else if(y>f[6]&&y<f[5]){if(x>=f[8])dx=1;else if(x<=f[7])dx=-1;}
  if(Math.max(Math.abs(p.absX-n.absX),Math.abs(p.absY-n.absY))>20)return true;
  if(!RunePayment.consume(p,new int[][]{{556,1},{563,1}}))return true;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.attacking.reset();p.stopMovement();p.facePosition(n.absX,n.absY);p.startAnimation(723);p.gfx100(142);p.getPA().addSkillXPMultiplied(43,6,true);
  int delay=(int)Math.floor(2+Math.hypot(p.absX-n.absX,p.absY-n.absY)*.5);
  p.getPA().createPlayersProjectile(p.absX,p.absY,n.absY-p.absY,n.absX-p.absX,50,Math.max(40,delay*30),143,40,0,0,0);
  final int moveX=dx,moveY=dy;final DynamicMapScene visit=arena.scene();PlayerLock previous=p.getLock();PlayerLock flight=new PlayerLock(){public boolean cannotLogout(Player q){return previous.cannotLogout(q);}public boolean cannotInteract(Player q){return true;}public boolean cannotClickItem(Player q,int id){return true;}public boolean cannotTeleport(Player q){return true;}};
  busy=true;p.lock(flight);
  Server.getEventHandler().submit(new Event<Player>("mta-statue-projectile",p,Math.max(1,delay)){
   public void execute(){p.restoreLock(flight,previous);if(!live(n)||arena.scene()!=visit){stop();return;}p.getPA().createPlayersStillGfx(144,n.absX,n.absY,0,0);slide(moveX,moveY,visit);super.stop();}
   public void stop(){super.stop();p.restoreLock(flight,previous);busy=false;}
  });return true;
 }
 private void slide(int dx,int dy,DynamicMapScene visit){
  if(dx==0&&dy==0){busy=false;return;}
  int[] f=LAYOUTS[layout];int b=MageTrainingArena.BASE,dir=dx==1?4:dx==-1?3:dy==1?1:6;
  Server.getEventHandler().submit(new Event<Player>("mta-statue-slide",p,1){
   int elapsed;
   public void execute(){
    if(!live(statue)||arena.scene()!=visit||++elapsed>24){stop();return;}
    int x=statue.absX-b,y=statue.absY-b;
    if(x+dx<=f[7]||x+dx>=f[8]||y+dy<=f[6]||y+dy>=f[5]||!RegionProvider.getGlobal().canMove(3328+x,9664+y,f[0],dir,true)){
     if(x==f[3]&&y==f[4])complete();stop();return;
    }
    statue.moveTowards(statue.absX+dx,statue.absY+dy,false,false);
   }
   public void stop(){super.stop();busy=false;}
  });
 }
 private void complete(){
  if(solved)return;solved=true;statue.requestTransform(6779);
  if(p.getMageTrainingProgress().mazeSolved()){
   // Bank fallback preserves the complete fifth-maze reward when the inventory is full.
   if(!io.xeros.content.skills.ProductionBatch.exchange(p,new int[0][],new int[][]{{563,10}}))p.getItems().addItemToBankOrDrop(563,10);
   p.getPA().addSkillXPMultiplied(1000,6,true);p.sendMessage("Five mazes solved: 8 bonus points, 10 law runes and 1,000 Magic experience.");
  }
  arena.refresh();p.sendMessage("The maze is solved. Speak to the Maze Guardian to continue.");
 }
}
