package io.xeros.content.questing;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.SkillLevel;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** Eagles' Peak, with mechanisms saved separately from the eight journal stages. */
public final class EaglesPeak extends Quest {
    private static final int[][] COST={{10167,10},{1939,1},{1765,1},{995,50}};
    private static final int[][] COSTUMES={{10171,2},{10172,2}};
    private static final Map<NPC,EaglesPeak> OWNERS=new IdentityHashMap<>();
    final EaglesPeakPuzzle puzzle=new EaglesPeakPuzzle();
    private final EaglesPeakScene scene=new EaglesPeakScene(this);
    private final EagleTransport2022 transport=new EagleTransport2022(player);
    private NPC nick,kebbit;
    private NPC lessonFerret;
    private GlobalObject lessonTrap;
    private boolean lessonPending;
    private boolean rewarded,configured;
    private int room;
    private int sentStage=-1;
    private String sentPuzzle="";
    public EaglesPeak(Player p){super(p);}
    public String getName(){return "Eagles' Peak";}
    public List<SkillLevel> getStartRequirements(){return Collections.emptyList();}
    public int getCompletionStage(){return 8;}
    public int getQuestPoints(){return 2;}
    public List<String> getCompletedRewardsList(){return Arrays.asList("2 Quest points","2,500 Hunter XP","Box trap hunting","Eagle transport");}
    public List<String> getJournalText(int stage){
        switch(stage){
            case 0:return Arrays.asList("Charlie at Ardougne Zoo needs help finding a huntsman.","I need level 27 Hunter to follow the trail.");
            case 1:return Arrays.asList("Find Nickolaus near the mountain west of the Gnome Stronghold.","His abandoned camp may contain a clue.");
            case 2:return Arrays.asList("The bird book contained a metal feather.","Perhaps it fits the rock entrance at the mountain summit.");
            case 3:return Arrays.asList("Nickolaus is trapped in the eagle's nest.",puzzle.dialogue<3?"Shout across the chasm to find out how to help.":puzzle.dialogue<5?"Asyff in Varrock can make two eagle disguises.":"Find bronze, silver and golden feathers to open the stone door.");
            case 4:return Arrays.asList("The stone door is unlocked.","Wear an eagle cape and fake beak to pass the eagle.","Bring Nickolaus his own disguise.");
            case 5:case 6:return Arrays.asList("Nickolaus escaped from the nest.","Meet him at his camp for a lesson in catching ferrets.");
            case 7:return Arrays.asList("I can now use box traps.","Bring a captured ferret to Charlie at Ardougne Zoo.");
            default:return Arrays.asList("Charlie has a ferret for the zoo.","I rescued Nickolaus and learned to use box traps.");
        }
    }
    @Override public void setStage(int stage){if(stage<0||stage>8)return;super.setStage(stage);rewarded=stage==8;configured=false;}
    @Override public void incrementStage(){super.incrementStage();sendConfigs();}
    public void giveQuestCompletionRewards(){if(isQuestCompleted()&&!rewarded){rewarded=true;player.getPA().addSkillXPExact(2500,21);player.getPA().removeAllWindows();player.getQuesting().openQuestCompletedJournal(this,false);}}
    public String encode(){return puzzle.encode();}
    public void restore(String record){puzzle.restore(record);configured=false;}
    public EagleTransport2022 getTransport(){return transport;}
    private boolean ready(){return !player.isDead&&player.getHealth().getCurrentHealth()>0&&!player.isDisconnected()&&!player.getLock().cannotInteract(player)&&player.getInstance()==null&&!player.getBankPin().requiresUnlock()&&!Server.getMultiplayerSessionListener().inAnySession(player);}
    private boolean near(int x,int y,int h,int distance){return ready()&&player.heightLevel==h&&Math.max(Math.abs(player.absX-x),Math.abs(player.absY-y))<=distance;}
    private boolean near(NPC n,int distance){return n!=null&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&!n.isDead()&&!n.applyDead&&!n.needRespawn&&!n.isInvisible()&&near(n.absX,n.absY,n.heightLevel,distance);}
    private boolean has(int item){return player.getItems().playerHasItem(item,1);}
    private void say(int id,String...lines){new DialogueBuilder(player).npc(id,lines).send();}
    private void message(String text){player.sendMessage(text);}
    private void offer(NPC n,String label,String text,Runnable action){new DialogueBuilder(player).npc(n.getNpcId(),text).option(new DialogueOption(label,p->{if(near(n,3))action.run();}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();}
    private boolean give(int item,int amount){return ProductionBatch.exchange(player,new int[0][],new int[][]{{item,amount}});}
    private void sendConfigs(){player.getPA().sendFrame87Masked(934,0xffffc07f,puzzle.config934(getStage()));player.getPA().sendFrame87Masked(935,0x38,puzzle.dialogue<<3);sentStage=getStage();sentPuzzle=encode();configured=true;}
    public void process(){
        transport.process();
        if(!configured||sentStage!=getStage()||!sentPuzzle.equals(encode()))sendConfigs();
        boolean cave=player.heightLevel==3&&player.absX>=1978&&player.absX<=2035&&player.absY>=4935&&player.absY<=4992;
        boolean camp=player.heightLevel==0&&Math.abs(player.absX-2317)<25&&Math.abs(player.absY-3504)<25;
        boolean wantCave=cave&&getStage()>=3&&getStage()<=4,wantCamp=camp&&getStage()>=5;
        int desired=wantCave?(getStage()==3?1484:1485):wantCamp?1485:-1;
        if(nick!=null&&(desired<0||nick.heightLevel!=(wantCave?3:0))){remove(nick);nick=null;}
        if(desired>=0&&nick==null)nick=spawn(desired,wantCave?2006:2317,wantCave?4960:3504,wantCave?3:0);
        else if(nick!=null&&nick.getNpcId()!=desired)nick.requestTransform(desired);
        if(kebbit!=null&&(player.heightLevel!=2||player.absX<1930||player.absX>1985||player.absY<4860||player.absY>4890)){remove(kebbit);kebbit=null;}
        boolean bronze=player.heightLevel==2&&player.absX>=1967&&player.absX<=1982&&player.absY>=4907&&player.absY<=4923;
        int nextRoom=bronze?1:player.heightLevel==2&&player.absX>=1920&&player.absX<=1983&&player.absY>=4889&&player.absY<=4923?2:0;
        if(nextRoom!=room){scene.release();room=nextRoom;}
        if(room==1&&getStage()>=3)scene.bronze(puzzle);else if(room==2&&getStage()>=3)scene.gold(puzzle);else scene.release();
    }
    public void logout(){transport.logout();clearLesson();remove(nick);nick=null;remove(kebbit);kebbit=null;scene.release();room=0;configured=false;}
    private NPC spawn(int id,int x,int y,int h){NPC n=new NPC(id,new Position(x,y,h)){
        @Override public boolean canBeAttacked(io.xeros.model.entity.Entity entity){return getNpcId()==1494&&entity==player;}
        @Override public boolean canBeDamaged(io.xeros.model.entity.Entity entity){return canBeAttacked(entity);}
    };n.randomWalk=false;n.walkingType=0;n.getBehaviour().setWalkHome(false);n.getBehaviour().setAggressive(false);n.getBehaviour().setRespawn(false);OWNERS.put(n,this);return n;}
    private static void remove(NPC n){if(n!=null){OWNERS.remove(n);if(n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n)n.unregisterInstant();}}
    public static boolean visible(Player p,NPC n){EaglesPeak owner=OWNERS.get(n);return (owner==null||owner.player==p)&&EagleTransport2022.visible(p,n);}
    public static boolean temporary(NPC n){return OWNERS.containsKey(n);}
    public boolean rangedNpc(NPC n){
        if(n!=null&&n.getNpcId()==1490){
            if(!near(n,16))return true;
            player.stopMovement();player.getPA().resetFollow();player.clickNpcType=0;
            if(near(n,4)){handleNpcClick(n,1);return true;}
            // Walking past the guardian is a scripted crossing, not ordinary
            // following to the anchor under its three-by-three body.
            List<Position> approaches=new ArrayList<>();
            for(int dx=-3;dx<=3;dx++)for(int dy=-3;dy<=3;dy++){
                int x=n.absX+dx,y=n.absY+dy;if(n.insideOf(x,y)||(player.getRegionProvider().getClipping(x,y,n.heightLevel)&0x1280100)!=0)continue;
                approaches.add(new Position(x,y,n.heightLevel));
            }
            approaches.sort(Comparator.comparingInt(t->Math.abs(t.getX()-player.absX)+Math.abs(t.getY()-player.absY)));
            for(Position tile:approaches)if(PathFinder.getPathFinder().accessable(player,tile.getX(),tile.getY())){
                PathFinder.getPathFinder().findRoute(player,tile.getX(),tile.getY(),true,1,1);
                player.setTickable(new io.xeros.model.tickable.impl.WalkToTickable(player,new Position(n.absX-3,n.absY-3,n.heightLevel),7,7,p->{if(near(n,4))handleNpcClick(n,1);}));return true;
            }
            message("You cannot reach the eagle from here.");return true;
        }
        if(n==null||n.getNpcId()!=1484)return false;
        if(n!=nick||OWNERS.get(n)!=this||getStage()!=3||!near(n,12))return true;
        player.stopMovement();player.getPA().resetFollow();player.clickNpcType=0;
        offerShout(n);return true;
    }
    private void offerShout(NPC n){new DialogueBuilder(player).npc(1484,"The eagle has decided that I belong in its nest.","Could you get two disguises so we can walk out together?").option(new DialogueOption("I will find the disguises.",p->{if(getStage()==3&&n==nick&&near(n,12)){puzzle.dialogue=Math.max(3,puzzle.dialogue);sendConfigs();say(1484,"Asyff runs the fancy dress shop in Varrock.","These large feathers should be useful to him.");}}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();}
    /** The wall door is a scripted crossing; approaching its solid anchor can
     * cancel the object click on the east side. Keep the approach on that side. */
    public boolean approachDoor(WorldObject object,int option){
        if(object.height!=3||object.x!=2003||(object.y!=4947&&object.y!=4948)
                ||!(object.id==19843||object.id==19991||object.id>=19914&&object.id<=19918))return false;
        if(option!=1||!near(object.x,object.y,3,16))return true;
        player.stopMovement();player.newWalkCmdSteps=0;player.getPA().resetFollow();
        if(near(object.x,object.y,3,2)){handleObjectClick(object,option);return true;}
        int x=player.absX<=2003?2002:2004,y=Math.max(4947,Math.min(4948,player.absY));
        if(!PathFinder.getPathFinder().accessable(player,x,y)){message("You cannot reach the stone door from here.");return true;}
        PathFinder.getPathFinder().findRoute(player,x,y,false,0,0);
        long token=player.productionActionToken;
        player.setTickable((container,p)->{
            if(token!=p.productionActionToken||!ready()||p.heightLevel!=3){container.stop();return;}
            if(p.newWalkCmdSteps>0||!p.isWalkingQueueEmpty())return;
            container.stop();if(p.absX==x&&p.absY==y)handleObjectClick(object,option);
        });return true;
    }
    @Override public boolean handleNpcClick(NPC n,int option){
        int id=n.getNpcId();if(id==2887&&option!=1)return false;if(id!=1495&&id!=2887&&id!=1484&&id!=1485&&id!=1486&&id!=1490&&id!=1494)return false;
        if(id==1484)return rangedNpc(n);
        if(!near(n,id==1490?4:3))return true;
        if(id==1494){if(option==2&&n==kebbit&&OWNERS.get(n)==this&&puzzle.silver==4){silverReward(n);remove(kebbit);kebbit=null;}return true;}
        if(option!=1)return true;
        if(id==1495){
            if(getStage()==0){if(player.playerLevel[21]<27){say(id,"I need a hunter experienced enough to follow a ferret.","Come back with Hunter level 27.");return true;}offer(n,"Look for Nickolaus.","Our ferret escaped. Nickolaus went to the mountain west of the stronghold, but has not returned.",()->{if(getStage()==0&&player.playerLevel[21]>=27){incrementStage();say(id,"Look for his camp near Eagles' Peak.");}});}
            else if(getStage()==7){if(!has(10092))say(id,"Please bring a live ferret for the zoo.");else offer(n,"Hand over the ferret.","You found Nickolaus! May I have that ferret for the zoo?",()->{if(getStage()==7&&ProductionBatch.exchange(player,new int[][]{{10092,1}},new int[0][])){incrementStage();giveQuestCompletionRewards();}});}
            else say(id,isQuestCompleted()?"The ferret has settled in well. Thank you for helping.":"Please look for Nickolaus near Eagles' Peak.");return true;
        }
        if(id==2887){
            if(getStage()<3||puzzle.dialogue<3){say(id,"I make fancy dress costumes. Let me know if you need one.");return true;}
            if(puzzle.dialogue==3){offer(n,"Ask for two eagle costumes.","For two eagle costumes I need ten eagle feathers, swamp tar, yellow dye and fifty coins.",()->{puzzle.dialogue=4;sendConfigs();say(id,"Bring the materials and I will make both outfits.");});return true;}
            offer(n,"Make two eagle costumes.","Do you have ten eagle feathers, swamp tar, yellow dye and fifty coins?",()->{if(puzzle.dialogue>=4&&ProductionBatch.exchange(player,COST,COSTUMES)){puzzle.dialogue=5;sendConfigs();say(id,"Two capes and two beaks. One set for each of you.");}else message("Bring all the materials and leave space for both disguises.");});return true;
        }
        if(id==1490){
            if(getStage()<4){message("The nest is beyond the locked stone door.");return true;}
            if(player.playerEquipment[Player.playerHat]!=10172||player.playerEquipment[Player.playerCape]!=10171){player.appendDamage(n,1,io.xeros.content.combat.Hitmark.HIT);message("The eagle pecks at you. Perhaps an eagle disguise would help.");return true;}
            player.getPA().movePlayer(2008,player.absY<=4954?4959:4953,3);return true;
        }
        if(n!=nick||OWNERS.get(n)!=this)return true;
        if(n.heightLevel==3){
            if(getStage()!=4||player.absY<4956){say(id,"Come around to the nest, wearing your disguise.");return true;}
            if(player.playerEquipment[Player.playerHat]!=10172||player.playerEquipment[Player.playerCape]!=10171){say(id,"Put on your own eagle cape and beak first.");return true;}
            offer(n,"Give Nickolaus his disguise.","You made it past the eagle. Did you bring my disguise?",()->{if(getStage()==4&&player.playerEquipment[Player.playerHat]==10172&&player.playerEquipment[Player.playerCape]==10171&&ProductionBatch.exchange(player,new int[][]{{10171,1},{10172,1}},new int[0][])){incrementStage();say(id,"I will meet you back at the camp.");remove(nick);nick=null;}});
        }else if(getStage()==5||getStage()==6){
            offer(n,"Learn to catch a ferret.","Let me show you how to use a box trap. Keep the ferret for Charlie.",()->beginLesson(n));
        }else say(id,"You can use box traps to catch ferrets and chinchompas.");return true;
    }
    @Override public boolean handleItemClick(int item){if(item!=10173)return false;if(!ready())return true;if(getStage()==1&&give(10174,1)){incrementStage();message("A metal feather falls out of the bird book.");}else message("The book describes the eagles living in the mountain.");return true;}
    private boolean featherAlready(int item){if(has(item))return true;for(io.xeros.model.items.bank.BankTab tab:player.getBank().getBankTab())if(tab!=null)for(io.xeros.model.items.bank.BankItem banked:tab.getItems())if(banked!=null&&banked.getId()==item+1&&banked.getAmount()>0)return true;return false;}
    private void silverDrop(Position at){Server.itemHandler.createPrivateTimedGroundItem(player,new io.xeros.model.items.GameItem(10176,1),at,200);}
    private void silverReward(NPC prey){if(puzzle.silver!=4)return;puzzle.silver=5;silverDrop(prey.getPosition());sendConfigs();message(prey.isDead()?"The kebbit drops a silver feather.":"The kebbit drops a silver feather as it flees.");}
    @Override public void handleNpcKilled(NPC n){if(n==kebbit&&OWNERS.get(n)==this&&puzzle.silver==4)silverReward(n);}
    /** Owned quest prey have fixed quest drops; they do not use global random drop tables. */
    public static boolean death(NPC n){EaglesPeak owner=OWNERS.get(n);if(owner==null||n!=owner.kebbit)return false;if(n.isDead()&&n.applyDead&&!n.needRespawn&&owner.puzzle.silver==4){owner.silverReward(n);Server.itemHandler.createPrivateTimedGroundItem(owner.player,new io.xeros.model.items.GameItem(526,1),n.getPosition(),200);}return true;}
    private void clearLesson(){lessonPending=false;remove(lessonFerret);lessonFerret=null;if(lessonTrap!=null){Server.getGlobalObjects().removeIfCurrent(lessonTrap);lessonTrap=null;}}
    private void lessonModel(int id,Position tile){if(lessonTrap!=null)Server.getGlobalObjects().removeIfCurrent(lessonTrap);lessonTrap=new GlobalObject(id,tile.getX(),tile.getY(),0,0,10,-1);Server.getGlobalObjects().add(lessonTrap);}
    private void beginLesson(NPC n){
        if(lessonPending||getStage()!=5&&getStage()!=6||puzzle.lessonGiven||!near(n,3))return;
        if(!ProductionBatch.canExchange(player,new int[0][],new int[][]{{10008,1},{10092,1}})){message("Leave two free inventory spaces for the trap and ferret.");return;}
        Position tile=null,approach=null;
        for(int dx=-3;dx<=3&&tile==null;dx++)for(int dy=-3;dy<=3&&tile==null;dy++){
            int x=n.absX+dx,y=n.absY+dy;if(Math.max(Math.abs(dx),Math.abs(dy))<2||player.absX==x&&player.absY==y)continue;
            if((player.getRegionProvider().getClipping(x,y,0)&0x1280100)!=0||(player.getRegionProvider().getClipping(x+1,y,0)&0x1280100)!=0)continue;
            if(player.absX==x+1&&player.absY==y)continue;
            tile=new Position(x,y,0);approach=new Position(x+1,y,0);
        }
        if(tile==null){message("Make some room around the camp for the demonstration.");return;}
        final Position trapTile=tile,preyTile=approach;
        if(getStage()==5)incrementStage();
        boolean started=ProductionBatch.start(player,21,27,0,1,827,6,new int[0][],new int[][]{{10008,1},{10092,1}},()->{
            puzzle.lessonGiven=true;incrementStage();clearLesson();say(1485,"You can now use box traps. Take the ferret to Charlie.");
        },()->getStage()==6&&!puzzle.lessonGiven&&near(n,3));
        if(!started)return;
        lessonPending=true;lessonModel(9380,trapTile);lessonFerret=spawn(1505,preyTile.getX(),preyTile.getY(),0);
        final long token=player.productionActionToken;final int x=player.absX,y=player.absY;
        Server.getEventHandler().submit(new io.xeros.model.cycleevent.Event<Player>("eagles-peak-lesson",player,2){int phase;
            public void execute(){
                if(!lessonPending||token!=player.productionActionToken||!near(n,3)||player.absX!=x||player.absY!=y){stop();return;}
                if(++phase==1){remove(lessonFerret);lessonFerret=null;lessonModel(9394,trapTile);}
                else if(phase==2)lessonModel(9384,trapTile);else stop();
            }
            public void stop(){super.stop();clearLesson();}
        });
    }
    @Override public boolean handleItemOnObject(int item,int object,int x,int y){
        if(transport.itemOnObject(item,object,x,y))return true;
        if(x==2328&&y==3494&&(object==19790||object==19925||object==19926)){if(item!=10174)return true;if(near(x,y,0,3)&&getStage()==2&&has(item)){incrementStage();message("The metal feather turns in the lock. The rock entrance opens.");}return true;}
        if(x==2003&&(y==4947||y==4948)&&(object==19843||object==19991||object>=19914&&object<=19918)){
            if(!near(x,y,3,2)||getStage()!=3||puzzle.dialogue<5)return true;
            boolean acceptable=item==10177&&!puzzle.bronzeInserted||item==10176&&puzzle.silver!=6||item==10175&&!puzzle.goldInserted;
            if(!acceptable)return true;
            if(ProductionBatch.exchange(player,new int[][]{{item,1}},new int[0][])){if(item==10177)puzzle.bronzeInserted=true;else if(item==10176)puzzle.silver=6;else puzzle.goldInserted=true;if(puzzle.allFeathers())incrementStage();sendConfigs();message("The feather fits into its recess in the stone door.");}return true;
        }
        if(object>=19936&&object<=19944){
            int[][] coords={{1945,4915},{1947,4898},{1962,4894},{1966,4890},{1931,4916},{1935,4897},{1947,4907},{1962,4901},{1972,4892}};
            int[] at=coords[object-19936];
            if(item!=10178||x!=at[0]||y!=at[1]||!near(x,y,2,2)||getStage()!=3||puzzle.dialogue<5)return true;
            Optional<WorldObject> actual=player.getRegionProvider().get(x,y).getWorldObject(object,x,y,2);if(!actual.isPresent())return true;
            // Native access mask 13, rotated by the map object's face: only the
            // front of the feeder accepts seeds. Never put a player under a moving bird.
            int face=actual.get().face,dx=face==0?1:face==2?-1:0,dy=face==1?-1:face==3?1:0;
            if(player.absX!=x+dx||player.absY!=y+dy){message("Stand in front of the bird feeder to fill it.");return true;}
            if(ProductionBatch.exchange(player,new int[][]{{item,1}},new int[0][])){message(puzzle.feed(object)?"The metal bird moves towards the feeder.":"The closed wings keep the birds away from that feeder.");sendConfigs();}
            return true;
        }
        return false;
    }
    @Override public boolean handleObjectClick(WorldObject o,int option){
        if(transport.object(o,option))return true;
        if(o.id==19787||o.id==19885||o.id==19886){if(o.x!=2319||o.y!=3506)return false;if(option==1&&near(o.x,o.y,0,3)&&getStage()>0){int item=getStage()==1?10173:10174;if(!featherAlready(item))give(item,1);else message("I already have the clue from these books.");}return true;}
        if(o.x==2328&&o.y==3494&&(o.id==19790||o.id==19925||o.id==19926)){if(option==1&&near(o.x,o.y,0,3)){if(getStage()>=3)player.getPA().movePlayer(1994,4983,3);else message("There is a feather-shaped recess in the rock.");}return true;}
        int[][] tunnels={{19891,1993,4983,3,2329,3493,0},{19897,2023,4982,3,1957,4908,2},{19894,1957,4909,2,2024,4982,3},{19903,1986,4972,3,1947,4868,2},{19900,1947,4867,2,1987,4972,3},{19909,1986,4949,3,1974,4908,2},{19906,1974,4907,2,1987,4949,3}};
        for(int[] t:tunnels)if(o.id==t[0]&&o.x==t[1]&&o.y==t[2]){if(option==1&&near(o.x,o.y,t[3],2)&&getStage()>=3)player.getPA().movePlayer(t[4],t[5],t[6]);return true;}
        if(o.id==19922&&o.height==3){if(option==1&&near(o.x,o.y,3,2)&&getStage()==3&&puzzle.dialogue>=3)give(10167,1);return true;}
        if(o.x==2003&&(o.y==4947||o.y==4948)&&(o.id==19843||o.id==19991||o.id>=19914&&o.id<=19918)){if(option==1&&near(o.x,o.y,3,2)){if(puzzle.allFeathers()&&getStage()>=4)player.getPA().movePlayer(player.absX<=2003?2004:2002,Math.max(4947,Math.min(4948,player.absY)),3);else message("Three feather-shaped recesses hold the stone door shut.");}return true;}
        if(getStage()!=3||puzzle.dialogue<5)return false;
        if((o.id==19980||o.id==19984)&&scene.owns(o.id,o.x,o.y,o.height)){
            if(option==1&&near(o.x,o.y,2,o.id==19980?4:2)){
                if(!puzzle.bronzeTriggered){puzzle.triggerBronze();scene.bronze(puzzle);sendConfigs();message("The net hoists the pedestal out of reach. Four winches control it.");}
                else if(puzzle.bronzeSolved()&&!puzzle.bronzeInserted&&!featherAlready(10177))give(10177,1);
            }return true;
        }
        if(o.id>=19976&&o.id<=19979){int[][] at={{1970,4919},{1978,4919},{1970,4910},{1978,4910}};int i=o.id-19976;if(o.x!=at[i][0]||o.y!=at[i][1])return false;if(option==1&&near(o.x,o.y,2,2)){if(puzzle.winch(i)){scene.bronze(puzzle);sendConfigs();message(puzzle.bronzeRaised?"The winch strains against the net.":"The last rope gives way and the pedestal falls.");}else message("The winches do nothing while the net is slack.");}return true;}
        if(o.id==19974&&o.x==1947&&o.y==4873){if(option==1&&near(o.x,o.y,2,2)){if(player.playerLevel[21]<27)message("You need Hunter level 27 to follow this trail.");else if(puzzle.silver==0){puzzle.silver=1;sendConfigs();message("A trail leads away from the empty pedestal.");}}return true;}
        if((o.id==19969&&o.x==1961&&o.y==4875)||(o.id==19970&&o.x==1967&&o.y==4879)){int stage=o.id==19969?1:2;if(option==1&&near(o.x,o.y,2,2)){if(puzzle.silver==stage){puzzle.silver++;sendConfigs();message("The tracks continue beyond the rocks.");}else message("Follow the visible trail to its end first.");}return true;}
        if(o.id==19967&&o.x==1971&&o.y==4886){if(option==1&&near(o.x,o.y,2,2)){if(puzzle.silver==3){puzzle.silver=4;sendConfigs();}if(puzzle.silver==4&&kebbit==null){kebbit=spawn(1494,1971,4880,2);kebbit.attackEntity(player);}else if(puzzle.silver==5&&!featherAlready(10176)&&Server.itemHandler.getOwnedGroundItem(player,10176,2)==null)silverDrop(new Position(1971,4880,2));}return true;}
        if(o.id==19919&&o.x==1958&&o.y==4906){if(option==1&&near(o.x,o.y,2,2))give(10178,6);return true;}
        int[][] levers={{19946,1935,4902},{19947,1925,4915},{19948,1943,4911},{19949,1978,4891}};
        for(int[] l:levers)if(o.id==l[0]&&o.x==l[1]&&o.y==l[2]){if(option==1&&near(o.x,o.y,2,2)){puzzle.lever(o.id);sendConfigs();message("The stone wings shift as you operate the lever.");}return true;}
        if(o.id==19945&&o.x==2022&&o.y==4981){if(option==1&&near(o.x,o.y,3,2)){puzzle.resetGold();sendConfigs();message("The metal birds and wings return to their starting positions.");}return true;}
        if(o.id==19950&&o.x==1928&&o.y==4907){if(option==1&&near(o.x,o.y,2,2)){if(puzzle.goldReachable()&&!puzzle.goldInserted&&!featherAlready(10175))give(10175,1);else message("The moving birds and wings still block the golden feather.");}return true;}
        return false;
    }
}
