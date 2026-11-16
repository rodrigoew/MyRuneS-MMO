package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.definitions.ItemStats;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import java.util.*;

/** A falcon and its uncollected prey belong to the borrowing character. */
public final class Falconry {
    public static final int EMPTY_GLOVE=10023,FALCON_GLOVE=10024,PROJECTILE=918;
    public static final Random RANDOM=new Random();
    private static final Map<NPC,Falconry> FLIGHTS=new IdentityHashMap<>(),PREY=new IdentityHashMap<>();
    private static final Set<Long> AREA=new HashSet<>();
    public enum Species {
        SPOTTED(5531,43,104,10125,26,310),DARK(5532,57,132,10115,0,253),DASHING(5533,69,156,10127,0,205);
        public final int npc,level,xp,fur,low,high;
        Species(int npc,int level,int xp,int fur,int low,int high){this.npc=npc;this.level=level;this.xp=xp;this.fur=fur;this.low=low;this.high=high;}
        public int threshold(int hunter){hunter=Math.max(1,Math.min(126,hunter));return Math.max(0,Math.min(256,low*(99-hunter)/98+high*(hunter-1)/98+1));}
        public static Species forNpc(int npc){for(Species s:values())if(s.npc==npc)return s;return null;}
    }
    private final Player p;
    private boolean loan,changingEquipment;
    private Flight flight;
    private NPC prey;
    private Species caught;
    public Falconry(Player p){this.p=p;}
    private static long tile(int x,int y){return (long)x<<32|y&0xffffffffL;}
    /** Resolve the enclosure through its actual fence and floor collision, rather than a generous rectangle. */
    public static void initialize(){
        AREA.clear();ArrayDeque<Position> queue=new ArrayDeque<>();queue.add(new Position(2373,3604,0));AREA.add(tile(2373,3604));
        int[][] directions={{-1,0,0x1280108},{1,0,0x1280180},{0,-1,0x1280102},{0,1,0x1280120}};
        while(!queue.isEmpty()){Position from=queue.remove();for(int[] d:directions){int x=from.getX()+d[0],y=from.getY()+d[1];if(x<2357||x>2400||y<3569||y>3640||(RegionProvider.getGlobal().getClipping(x,y,0)&d[2])!=0||AREA.contains(tile(x,y)))continue;AREA.add(tile(x,y));queue.add(new Position(x,y,0));}}
        if(AREA.size()<100||AREA.stream().anyMatch(t->{int x=(int)(t>>32),y=(int)(long)t;return x==2357||x==2400||y==3569||y==3640;}))throw new IllegalStateException("Falconry enclosure is missing its native fence collision");
    }
    public static boolean inside(int x,int y,int height){return height==0&&AREA.contains(tile(x,y));}
    public static boolean glove(int id){return id==EMPTY_GLOVE||id==FALCON_GLOVE;}
    public boolean hasLoan(){return loan;}
    public boolean isFlying(){return flight!=null;}
    public NPC prey(){return prey;}
    public String encode(){return loan?"1":"0";}
    public boolean restore(String text){if(!text.equals("0")&&!text.equals("1"))return false;loan=text.equals("1");return true;}
    private boolean inArea(){return p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal()&&inside(p.absX,p.absY,p.heightLevel);}
    private boolean ready(){return p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!Server.getMultiplayerSessionListener().inAnySession(p)&&inArea();}
    private static boolean registered(NPC n){return n!=null&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&!n.isUnregister()&&!n.isDead()&&!n.needRespawn&&!n.isInvisible()&&n.getInstance()==null;}
    private boolean matthias(){return ready()&&p.distanceToPoint(2374,3604)<=2&&Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).anyMatch(n->(n.getNpcId()==1340||n.getNpcId()==1341)&&n.absX==2374&&n.absY==3604&&n.heightLevel==0&&registered(n));}
    private boolean emptyHands(){return p.playerEquipment[Player.playerHands]<=0&&p.playerEquipment[Player.playerShield]<=0;}
    private boolean holding(int id){return p.playerEquipment[Player.playerWeapon]==id&&p.playerEquipmentN[Player.playerWeapon]==1;}
    private void wear(int item){changingEquipment=true;try{p.getItems().equipItem(item,item<0?0:1,Player.playerWeapon);}finally{changingEquipment=false;}}
    public boolean permitsEquipmentChange(int item,int slot){
        if(changingEquipment)return true;
        if(glove(item)||loan&&(slot==Player.playerWeapon||slot==Player.playerShield||slot==Player.playerHands)){p.sendMessage("Return your falcon and glove to Matthias before changing your hands, weapon or shield.");return false;}return true;
    }
    public boolean permitsWear(int item){if(glove(item))return permitsEquipmentChange(item,Player.playerWeapon);if(!loan)return true;var stats=ItemStats.forId(item).getEquipment();return stats==null||permitsEquipmentChange(item,stats.getSlot());}
    public boolean permitsRemoval(int slot){if(slot==Player.playerWeapon&&glove(p.playerEquipment[slot])){p.sendMessage("Speak to Matthias to return the falcon and glove.");return false;}return true;}
    public boolean borrow(){
        if(!matthias())return false;p.getPA().removeAllWindows();
        if(loan){p.sendMessage("You already have a loan. Matthias can recover your falcon without charging you again.");return false;}
        if(p.playerLevel[21]<43){p.sendMessage("You need Hunter level 43 to borrow a falcon.");return false;}
        if(p.playerEquipment[Player.playerWeapon]>0||!emptyHands()){p.sendMessage("Free your weapon, shield and hands slots first. You may keep your ring on.");return false;}
        if(!ProductionBatch.exchange(p,new int[][]{{995,500}},new int[0][]))return false;
        removeInventoryGloves();loan=true;wear(FALCON_GLOVE);p.sendMessage("Matthias lends you a falcon and glove for 500 coins. Return them before leaving the enclosure.");return true;
    }
    public boolean recover(){
        if(!matthias())return false;p.getPA().removeAllWindows();
        if(!loan){p.sendMessage("You must first borrow a falcon from Matthias.");return false;}
        if(flight!=null||prey!=null){p.sendMessage("Your falcon is still hunting or guarding its prey.");return false;}
        if(!emptyHands()||p.playerEquipment[Player.playerWeapon]>0&&!holding(EMPTY_GLOVE)&&!holding(FALCON_GLOVE)){p.sendMessage("Free your weapon, shield and hands slots first.");return false;}
        wear(FALCON_GLOVE);p.sendMessage("Matthias returns your falcon. You do not need to pay again while staying in the enclosure.");return true;
    }
    public boolean returnToMatthias(){if(!matthias())return false;p.getPA().removeAllWindows();endLoan();p.sendMessage("You return the falcon and glove to Matthias.");return true;}
    private void removeInventoryGloves(){for(int id:new int[]{EMPTY_GLOVE,FALCON_GLOVE}){int n=p.getItems().getInventoryCount(id);if(n>0)p.getItems().deleteItem2(id,n);}}
    private void clearPrey(){if(prey!=null){PREY.remove(prey,this);if(prey.getIndex()>0&&NPCHandler.npcs[prey.getIndex()]==prey)prey.unregisterInstant();prey=null;caught=null;p.getPA().createPlayerHints(0,0);}}
    private void endLoan(){if(flight!=null)flight.close(false);clearPrey();loan=false;removeInventoryGloves();if(glove(p.playerEquipment[Player.playerWeapon]))wear(-1);}
    public void logout(){if(flight!=null)flight.close(false);clearPrey();}
    public void unsafeDeath(){logout();loan=false;}
    public void process(){
        // Login restores its destination before the movement phase applies it to absX/absY.
        if(p.getTeleportToX()!=-1||p.getTeleportToY()!=-1)return;
        if(loan&&!inArea()){endLoan();p.sendMessage("Your borrowed falcon and glove have been returned to Matthias.");}
        else if(!loan){removeInventoryGloves();if(glove(p.playerEquipment[Player.playerWeapon]))wear(-1);}
        if(prey!=null&&!registered(prey)){clearPrey();p.sendMessage("Your falcon has returned to Matthias. You may collect it from him without paying again.");}
    }
    /** This ranged action is routed before ordinary NPC follow/walk handling. */
    public boolean launch(NPC n){
        Species species=n==null?null:Species.forNpc(n.getNpcId());if(species==null)return false;
        if(!ready()||!registered(n)||n.heightLevel!=p.heightLevel||!inside(n.absX,n.absY,n.heightLevel))return true;
        if(!loan||!holding(FALCON_GLOVE)||!emptyHands()||flight!=null||prey!=null){p.sendMessage("You need your borrowed falcon ready on your glove.");return true;}
        if(p.playerLevel[21]<species.level){p.sendMessage("You need Hunter level "+species.level+" to hunt this kebbit.");return true;}
        int distance=Math.max(Math.abs(n.absX-p.absX),Math.abs(n.absY-p.absY));if(distance>15){p.sendMessage("That kebbit is too far away for your falcon.");return true;}
        if(FLIGHTS.containsKey(n)){p.sendMessage("Another falcon is already pursuing that kebbit.");return true;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.getPA().resetFollow();p.stopMovement();p.attacking.reset();
        wear(EMPTY_GLOVE);Flight attempt=new Flight(n,species,Math.max(2,2+distance/2));flight=attempt;FLIGHTS.put(n,this);p.facePosition(n.absX,n.absY);p.startAnimation(5162);p.getPA().sendSound(2633);
        p.getPA().createPlayersProjectile(p.absX,p.absY,n.absY-p.absY,n.absX-p.absX,50,attempt.duration*30,PROJECTILE,26,1,n.getIndex()+1,0);
        Server.getEventHandler().submit(new Event<Flight>("falcon-flight",attempt,attempt.duration){public void update(){if(!attempt.valid())stop();else if(n.absX!=attempt.nx||n.absY!=attempt.ny)attempt.moved=true;}public void execute(){attempt.finish();stop();}public void stop(){super.stop();attempt.close(true);}});return true;
    }
    private final class Flight {
        final NPC target;final Species species;final int x=p.absX,y=p.absY,nx,ny,duration;final long token=p.productionActionToken;boolean moved,closed;
        Flight(NPC n,Species species,int duration){target=n;this.species=species;nx=n.absX;ny=n.absY;this.duration=duration;}
        boolean valid(){return !closed&&flight==this&&FLIGHTS.get(target)==Falconry.this&&ready()&&loan&&holding(EMPTY_GLOVE)&&emptyHands()&&registered(target)&&p.absX==x&&p.absY==y&&p.productionActionToken==token&&p.playerLevel[21]>=species.level;}
        void finish(){
            if(!valid())return;
            if(moved||RANDOM.nextInt(256)>=species.threshold(p.playerLevel[21])){p.sendMessage("The falcon swoops down but misses the kebbit.");return;}
            target.resetWalkingQueue();target.setDead(true);target.applyDead=true;target.needRespawn=true;target.setInvisible(true);target.actionTimer=5;target.setUpdateRequired(true);
            prey=new NPC(1342,new Position(nx,ny,0));prey.randomWalk=false;prey.walkingType=0;prey.getBehaviour().setWalkHome(false);prey.getBehaviour().setAggressive(false);prey.getBehaviour().setRespawn(false);caught=species;PREY.put(prey,Falconry.this);NPC landed=prey;
            p.getPA().createPlayerHints(1,landed.getIndex());p.getPA().sendSound(2634);p.sendMessage("Your falcon caught the kebbit. Retrieve it to collect the catch and Hunter experience.");
            Server.getEventHandler().submit(new Event<Falconry>("falcon-prey",Falconry.this,100){public void update(){if(prey!=landed||!loan||p.isDisconnected()||!inArea()){if(prey==landed)clearPrey();stop();}}public void execute(){if(prey==landed){clearPrey();p.sendMessage("Your falcon has left its prey and returned to Matthias. No experience was gained.");}stop();}});
        }
        void close(boolean returnFalcon){if(closed)return;closed=true;FLIGHTS.remove(target,Falconry.this);if(flight==this)flight=null;if(returnFalcon&&prey==null&&loan&&inArea()&&!p.isDisconnected()&&p.getSession()!=null&&holding(EMPTY_GLOVE))wear(FALCON_GLOVE);if(p.productionActionToken==token)p.startAnimation(65535);}
    }
    public boolean retrieve(NPC n){
        if(n==null||n.getNpcId()<1342||n.getNpcId()>1344)return false;
        if(PREY.get(n)!=this||n!=prey){p.sendMessage("That falcon is not guarding your catch.");return true;}
        if(!ready()||!loan||!holding(EMPTY_GLOVE)||!emptyHands()||!registered(n)||p.heightLevel!=n.heightLevel||Math.max(Math.abs(p.absX-n.absX),Math.abs(p.absY-n.absY))>1)return true;
        Species reward=caught;if(reward==null)return true;p.interruptActions();p.getPA().removeAllWindows();
        if(!ProductionBatch.exchange(p,new int[0][],new int[][]{{526,1},{reward.fur,1}}))return true;
        clearPrey();wear(FALCON_GLOVE);p.getPA().addSkillXPMultiplied(reward.xp,21,true);p.sendMessage("You retrieve your falcon and the "+reward.name().toLowerCase()+" kebbit's fur and bones.");return true;
    }
    public boolean npc(NPC n,int option){
        if(n==null)return false;if(n.getNpcId()>=1342&&n.getNpcId()<=1344){if(option==1)retrieve(n);return true;}
        if(n.getNpcId()!=1340&&n.getNpcId()!=1341)return false;if(option!=1||!matthias()||n.absX!=2374||n.absY!=3604)return true;
        new DialogueBuilder(p).option("Matthias - Falconry",new DialogueOption("Borrow a falcon and glove: 500 coins",q->q.getFalconry().borrow()),new DialogueOption("Recover my borrowed falcon",q->q.getFalconry().recover()),new DialogueOption("Return my falcon and glove",q->q.getFalconry().returnToMatthias()),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return true;
    }
    public static boolean stile(Player p,WorldObject object,int option){
        if(object==null||object.id!=19222||object.x!=2371||object.y!=3620)return false;
        Position size=object.getObjectSize();int width=Math.max(1,size.getX()),length=Math.max(1,size.getY());
        int dx=Math.max(object.x-p.absX,Math.max(0,p.absX-object.x-width+1)),dy=Math.max(object.y-p.absY,Math.max(0,p.absY-object.y-length+1));
        if(option!=1||object.height!=p.heightLevel||p.heightLevel!=0||p.getInstance()!=null||!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||Server.getMultiplayerSessionListener().inAnySession(p)||Math.max(dx,dy)>1)return true;
        if(p.getRegionProvider().get(object.x,object.y).getWorldObject(object.id,object.x,object.y,0).filter(object::equals).isEmpty())return true;
        boolean leaving=inside(p.absX,p.absY,0);int tx=object.x,ty=leaving?object.y+length:object.y-1;if((p.getRegionProvider().getClipping(tx,ty,0)&0x1280100)!=0)return true;
        p.interruptActions();p.stopMovement();p.getPA().removeAllWindows();if(leaving)p.getFalconry().endLoan();long token=p.productionActionToken;int sx=p.absX,sy=p.absY;PlayerMovementState previous=p.getMovementState(),locked=new PlayerMovementState(false,false,true);p.setMovementState(locked);p.facePosition(tx,ty);p.startAnimation(839);
        Server.getEventHandler().submit(new Event<Player>("falconry-stile",p,2){public void execute(){if(token==p.productionActionToken&&!p.isDead&&!p.isDisconnected()&&p.absX==sx&&p.absY==sy&&p.heightLevel==0)p.moveTo(new Position(tx,ty,0));stop();}public void stop(){super.stop();if(p.getMovementState()==locked)p.setMovementState(previous);p.startAnimation(65535);}});return true;
    }
}
