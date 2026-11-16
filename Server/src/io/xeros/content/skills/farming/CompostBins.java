package io.xeros.content.skills.farming;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.SkillingOutfit;
import io.xeros.content.skills.herblore.Herb;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.player.Player;
import java.util.*;
import java.util.function.LongSupplier;

/** Personal compost batches at the seven native pre-2023 facilities. */
public final class CompostBins {
    public enum Bin {
        FALADOR(7836,3056,3312,15),CATHERBY(7837,2804,3464,15),
        PHASMATYS(7838,3610,3522,15),ARDOUGNE(7839,2661,3375,15),
        HOSIDIUS(27112,1730,3558,15),GUILD(34631,1272,3729,30),PRIFDDINAS(34920,3287,6100,15);
        public final int object,x,y,capacity;
        Bin(int object,int x,int y,int capacity){this.object=object;this.x=x;this.y=y;this.capacity=capacity;}
    }
    private static final Set<Integer> NORMAL=Set.of(6055,1947,1971,1779,1951,753,2126,1942,1957,1965,1967,1982,
            5986,5504,2102,2120,1955,1963,2108,5970,6883,5984,6006,6008,5994,5996,5931,6113,5998,6000,6002,
            6010,6014,6012,1793,225,5933,1984,5992,401,403,21504,3138);
    private static final Set<Integer> SUPER=Set.of(5982,231,2114,5972,5974,5976,5978,22929,6004,247,239,6018,
            6043,6045,6047,6049,6051,22935,5980,6469,22932);
    private static final Set<Herb> SUPER_HERBS=EnumSet.of(Herb.TOADFLAX,Herb.AVANTOE,Herb.KWUARM,Herb.SNAPDRAGON,
            Herb.CADANTINE,Herb.LANTADYME,Herb.DWARF_WEED,Herb.TORSTOL);
    private static final class Batch {
        int count,superCount,tomatoes,tier; // 0 filling, 1 compost, 2 super, 3 ultra, 4 tomatoes
        long deadline;boolean opened;
    }
    private final Player player;
    private final LongSupplier clock;
    private final EnumMap<Bin,Batch> batches=new EnumMap<>(Bin.class);
    public CompostBins(Player player){this(player,System::currentTimeMillis);}
    public CompostBins(Player player,LongSupplier clock){this.player=player;this.clock=clock;}
    public static int quality(int item){
        if(SUPER.contains(item))return 2;if(NORMAL.contains(item))return 1;
        for(Herb h:Herb.values())if(item==h.getClean()||item==h.getGrimy())return SUPER_HERBS.contains(h)?2:1;
        return 0;
    }
    private Bin find(int object,int x,int y){
        for(Bin bin:Bin.values())if(bin.object==object&&bin.x==x&&bin.y==y)return bin;return null;
    }
    private WorldObject object(Bin bin){return player.getRegionProvider().get(bin.x,bin.y).getWorldObject(bin.object,bin.x,bin.y,0).orElse(null);}
    private boolean reachable(Bin bin){
        if(player.heightLevel!=0||!player.getUtilityMagic().canInteract()||player.isForceMovementActive()||player.isDisconnected()
                ||player.getSession()==null)return false;
        if(bin==Bin.GUILD&&player.playerLevel[19]<45){player.sendMessage("You need Farming level 45 to use the guild's compost bin.");return false;}
        WorldObject o=object(bin);if(o==null)return false;
        int dx=Math.max(bin.x-player.absX,player.absX-bin.x-o.getObjectSize().getX()+1);
        int dy=Math.max(bin.y-player.absY,player.absY-bin.y-o.getObjectSize().getY()+1);
        return Math.max(dx,dy)<=1;
    }
    private Batch batch(Bin bin){return batches.computeIfAbsent(bin,b->new Batch());}
    public int remaining(Bin bin){return batch(bin).count;}
    public long deadline(Bin bin){return batch(bin).deadline;}
    public int tier(Bin bin){return batch(bin).tier;}
    public boolean opened(Bin bin){return batch(bin).opened;}
    public boolean use(int item,int object,int x,int y){
        Bin bin=find(object,x,y);if(bin==null)return false;
        if(!reachable(bin))return true;
        Batch b=batch(bin);
        if(item==1925){collect(bin);return true;}
        if(item==21622||potion(item)){
            if(!b.opened||b.tier==0||b.count==0){player.sendMessage("Open a finished bin before improving its compost.");return true;}
            if(item==21622){
                if(b.tier!=2){player.sendMessage("Volcanic ash can only improve supercompost.");return true;}
                if(!ProductionBatch.exchange(player,new int[][]{{21622,bin.capacity==30?50:25}},new int[0][])){player.sendMessage("You need "+(bin.capacity==30?50:25)+" volcanic ashes.");return true;}
                player.interruptActions();b.tier=3;player.startAnimation(2283);sendConfigs();return true;
            }
            if(b.tier!=1){player.sendMessage("This compost does not need a compost potion.");return true;}
            if(!ProductionBatch.exchange(player,new int[][]{{item,1}},new int[][]{{nextDose(item),1}}))return true;
            player.interruptActions();b.tier=2;player.startAnimation(2283);sendConfigs();return true;
        }
        int quality=quality(item);
        if(quality==0){player.sendMessage("That item cannot be composted.");return true;}
        if(b.tier!=0||b.count>=bin.capacity){player.sendMessage("This bin cannot hold more produce.");return true;}
        boolean mixing=b.count>0&&(b.superCount==b.count&&quality==1||b.superCount==0&&quality==2);
        if(mixing){
            player.interruptActions();
            new DialogueBuilder(player).statement("Mixing these items will produce regular compost.")
                .option("Add the produce?",new DialogueOption("Yes",p->{if(batch(bin)==b&&reachable(bin))fill(bin,item);}),
                        new DialogueOption("No",p->p.getPA().removeAllWindows())).send();
        }else fill(bin,item);
        return true;
    }
    private void fill(Bin bin,int item){
        Batch b=batch(bin);if(!reachable(bin)||b.tier!=0||b.count>=bin.capacity||quality(item)==0)return;
        start(bin,2,()->{
            if(batches.get(bin)!=b||b.tier!=0||b.count>=bin.capacity)return false;
            if(!ProductionBatch.exchange(player,new int[][]{{item,1}},new int[0][]))return false;
            b.count++;if(quality(item)==2)b.superCount++;if(item==1982)b.tomatoes++;
            sendConfigs();return b.count<bin.capacity&&player.getItems().playerHasItem(item);
        });
    }
    public boolean click(int object,int x,int y,int option){
        Bin bin=find(object,x,y);if(bin==null)return false;
        if(!reachable(bin))return true;
        Batch b=batch(bin);
        if(option==2||option==5){
            if(b.count==0){player.sendMessage("The compost bin is empty.");return true;}
            player.interruptActions();
            new DialogueBuilder(player).option("Discard all contents?",
                    new DialogueOption("Discard",p->{if(reachable(bin)&&batches.get(bin)==b){p.interruptActions();batches.remove(bin);sendConfigs();p.getPA().removeAllWindows();}}),
                    new DialogueOption("Keep",p->p.getPA().removeAllWindows())).send();return true;
        }
        if(option!=1)return true;
        if(b.tier==0){
            if(b.count!=bin.capacity){player.sendMessage("Add "+(bin.capacity-b.count)+" more items before closing this bin.");return true;}
            b.tier=b.tomatoes==bin.capacity?4:b.superCount==bin.capacity?2:1;
            long duration=(b.tier==2||bin.capacity==30?90:60)*60_000L;
            b.deadline=clock.getAsLong()>Long.MAX_VALUE-duration?Long.MAX_VALUE:clock.getAsLong()+duration;
            player.interruptActions();sendConfigs();player.sendMessage("You close the compost bin and leave its contents to rot.");return true;
        }
        if(!b.opened){
            if(clock.getAsLong()<b.deadline){player.sendMessage("The contents are still rotting.");return true;}
            player.interruptActions();b.opened=true;sendConfigs();player.sendMessage("You open the finished compost bin.");return true;
        }
        collect(bin);return true;
    }
    private void collect(Bin bin){
        Batch b=batch(bin);
        if(!b.opened||b.count==0){player.sendMessage("Open a finished compost bin first.");return;}
        start(bin,4,()->{
            if(batches.get(bin)!=b||!b.opened||b.count==0)return false;
            int product=b.tier==4?2518:b.tier==3?21483:b.tier==2?6034:6032;
            if(!ProductionBatch.exchange(player,b.tier==4?new int[0][]:new int[][]{{1925,1}},new int[][]{{product,1}}))return false;
            b.count--;double xp=b.tier==4||b.tier==1?4.5:b.tier==2?8.5:10;
            player.getPA().addSkillXPMultiplied(xp*SkillingOutfit.FARMER.multiplier(player),19,true);
            if(bin==Bin.FALADOR&&b.tier==1)player.getDiaryManager().getFaladorDiary().progress(io.xeros.content.achievement_diary.impl.FaladorDiaryEntry.COMPOST_BUCKET,true,1);
            if(b.count==0)batches.remove(bin);sendConfigs();return b.count>0;
        });
    }
    private interface Step {boolean run();}
    private void start(Bin bin,int ticks,Step step){
        if(!reachable(bin))return;
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();
        long token=player.productionActionToken;int x=player.absX,y=player.absY,h=player.heightLevel;
        player.playerSkilling[19]=true;player.startAnimation(2283);
        Server.getEventHandler().submit(new Event<Player>("skilling",player,ticks){
            public void execute(){
                if(token!=player.productionActionToken||!player.playerSkilling[19]||player.absX!=x||player.absY!=y||player.heightLevel!=h||!reachable(bin)){stop();return;}
                if(!step.run()){stop();return;}player.startAnimation(2283);
            }
            public void stop(){super.stop();if(token==player.productionActionToken){player.playerSkilling[19]=false;player.startAnimation(65535);}}
        });
    }
    public static boolean potion(int id){return id==6470||id==6472||id==6474||id==6476;}
    public static int nextDose(int id){return id==6476?229:id+2;}
    public boolean bucketUpgrade(int first,int second){
        int pot=first==6032?second:second==6032?first:-1;
        if(!potion(pot))return false;
        ProductionBatch.start(player,19,1,0,1,2283,2,new int[][]{{6032,1},{pot,1}},new int[][]{{6034,1},{nextDose(pot),1}},null);return true;
    }
    public int morph(Bin bin){
        Batch b=batch(bin);if(b.count==0)return 0;
        if(b.tier==0){
            int kind=b.tomatoes==b.count?4:b.superCount==b.count?2:1;
            if(bin.capacity==15)return (kind==4?128:kind==2?32:0)+b.count;
            if(b.count<=15)return(kind==4?128:kind==2?32:0)+b.count;
            return(kind==4?207:kind==2?145:47)+b.count;
        }
        if(!b.opened)return b.tier==4?159:b.tier==2?95:31;
        if(bin.capacity==15)return (b.tier==4?143:b.tier==3?175:b.tier==2?47:15)+b.count;
        if(b.tier==3)return 175+b.count;
        if(b.count<=15)return(b.tier==4?143:b.tier==2?47:15)+b.count;
        return(b.tier==4?191:b.tier==2?84:62)+b.count;
    }
    public void sendConfigs(){
        Bin nearest=null;long distance=Long.MAX_VALUE;
        for(Bin bin:Bin.values())if(bin!=Bin.GUILD){long dx=player.absX-bin.x,dy=player.absY-bin.y,d=dx*dx+dy*dy;if(d<distance){distance=d;nearest=bin;}}
        player.getPA().sendConfig(1057,nearest==null?0:morph(nearest));
        player.getPA().sendConfig(2078,morph(Bin.GUILD)<<8);
    }
    public String encode(){
        StringJoiner out=new StringJoiner(";");for(Map.Entry<Bin,Batch> entry:batches.entrySet()){
            Batch b=entry.getValue();if(b.count==0)continue;
            out.add(entry.getKey().name()+","+b.count+","+b.superCount+","+b.tomatoes+","+b.tier+","+b.deadline+","+(b.opened?1:0));
        }return out.toString();
    }
    public boolean decode(String value){
        try{
            EnumMap<Bin,Batch> next=new EnumMap<>(Bin.class);
            if(!value.isEmpty())for(String record:value.split(";",-1)){
                String[] f=record.split(",",-1);if(f.length!=7)return false;
                Bin bin=Bin.valueOf(f[0]);Batch b=new Batch();b.count=Integer.parseInt(f[1]);b.superCount=Integer.parseInt(f[2]);
                b.tomatoes=Integer.parseInt(f[3]);b.tier=Integer.parseInt(f[4]);b.deadline=Long.parseLong(f[5]);int open=Integer.parseInt(f[6]);b.opened=open==1;
                if(next.containsKey(bin)||b.count<1||b.count>bin.capacity||b.superCount<0||b.superCount>bin.capacity
                    ||b.tomatoes<0||b.tomatoes>bin.capacity||b.superCount+b.tomatoes>bin.capacity||b.tier<0||b.tier>4||open<0||open>1
                    ||b.deadline<0||b.tier==0&&(b.deadline!=0||b.opened||b.superCount+b.tomatoes>b.count)
                    ||b.tier!=0&&(b.deadline==0||!b.opened&&b.count!=bin.capacity))return false;
                next.put(bin,b);
            }
            batches.clear();batches.putAll(next);return true;
        }catch(RuntimeException malformed){return false;}
    }
}
