package io.xeros.content.skills.hunter.birdhouse;

import io.xeros.content.dialogue.*;
import io.xeros.model.entity.player.Player;
import java.util.*;
import java.util.function.LongSupplier;

/** Private four-spot state, kept in the character save rather than global objects. */
public final class Birdhouses {
    public static final int CLOCKWORK=8792,HAMMER=2347,CHISEL=1755;
    public static final long BIRD_MILLIS=300_000L;
    private final Player player;
    private final House[] houses=new House[4];
    private final int[] sent={-1,-1,-1,-1};
    private final LongSupplier clock;
    private long lastCraft;
    static final class House {
        final BirdhouseType type;int bait;long started;
        House(BirdhouseType type){this.type=type;}
    }
    public Birdhouses(Player player){this(player,System::currentTimeMillis);}
    Birdhouses(Player player,LongSupplier clock){this.player=player;this.clock=clock;}
    public static int seedStrength(int id){
        if(id>=5305&&id<=5310 || id>=5291&&id<=5294)return 1;
        if(id==5311 || id>=5295&&id<=5304)return 2;
        return 0;
    }
    public boolean craft(int first,int second){
        BirdhouseType type=BirdhouseType.forLog(first==CLOCKWORK?second:first);
        if(type==null || first!=CLOCKWORK&&second!=CLOCKWORK)return false;
        if(!player.getUtilityMagic().canInteract())return true;
        if(player.playerLevel[12]<type.craftingLevel){player.sendMessage("You need Crafting level "+type.craftingLevel+".");return true;}
        if(!player.getItems().playerHasItem(HAMMER)||!player.getItems().playerHasItem(CHISEL)){player.sendMessage("You need a hammer and chisel.");return true;}
        if(!player.getItems().playerHasItem(type.log)||!player.getItems().playerHasItem(CLOCKWORK))return true;
        if(clock.getAsLong()-lastCraft<1200)return true;
        if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[][]{{HAMMER,0},{CHISEL,0},{type.log,1},{CLOCKWORK,1}},new int[][]{{type.item,1}}))return true;player.getPA().addSkillXPMultiplied(type.craftingXp,12,true);player.startAnimation(1248);lastCraft=clock.getAsLong();return true;
    }
    private boolean reachable(BirdhouseSpot spot){
        return player.heightLevel==0 && Math.abs(player.absX-spot.x)<=2 && Math.abs(player.absY-spot.y)<=2 && player.getUtilityMagic().canInteract();
    }
    public boolean useItem(int item,int object,int x,int y){
        BirdhouseSpot spot=BirdhouseSpot.find(object,x,y);if(spot==null)return false;
        if(!reachable(spot))return true;
        BirdhouseType type=BirdhouseType.forItem(item);
        if(type!=null)place(spot,type);else if(seedStrength(item)>0)addSeeds(spot,item);else player.sendMessage("Use a bird house, or hop/herb seeds for bait.");
        return true;
    }
    public boolean click(int object,int x,int y,int option){
        BirdhouseSpot spot=BirdhouseSpot.find(object,x,y);if(spot==null)return false;
        if(!reachable(spot))return true;
        House house=houses[spot.ordinal()];
        if(house==null){
            if(option==1){
                for(int i=BirdhouseType.values().length-1;i>=0;i--){BirdhouseType type=BirdhouseType.values()[i];if(player.playerLevel[21]>=type.hunterLevel&&player.getItems().playerHasItem(type.item)){place(spot,type);return true;}}
                player.sendMessage("Build a bird house using logs on clockwork, with a hammer and chisel, then use it here.");
            }
            return true;
        }
        if(option==2){
            if(house.bait==10){player.sendMessage("Birds caught: "+birds(spot)+"/10. Ready in about "+remainingMinutes(spot)+" minutes.");return true;}
            for(int item=5305;item<=5311&&house.bait<10;item++)addSeeds(spot,item);
            if(house.bait<10)player.sendMessage("Use hop or herb seeds on the bird house. Bait: "+house.bait+"/10.");
        }else if(option==3 && birds(spot)<10){
            player.start(new DialogueBuilder(player).option("Dismantle early? Seeds and rewards will be lost.",new DialogueOption("Return only the clockwork.",p->{p.getPA().closeAllWindows();if(houses[spot.ordinal()]==house&&reachable(spot))dismantle(spot,false);}),DialogueOption.nevermind()));
        }else if(birds(spot)>=10)dismantle(spot,true);
        else player.sendMessage(house.bait<10?"The bird house needs more seeds ("+house.bait+"/10).":"Birds caught: "+birds(spot)+"/10. Ready in about "+remainingMinutes(spot)+" minutes.");
        return true;
    }
    public boolean place(BirdhouseSpot spot,BirdhouseType type){
        if(!reachable(spot)||houses[spot.ordinal()]!=null)return false;
        if(player.playerLevel[21]<type.hunterLevel){player.sendMessage("You need Hunter level "+type.hunterLevel+".");return false;}
        if(!player.getItems().playerHasItem(type.item))return false;
        player.getItems().deleteItem2(type.item,1);houses[spot.ordinal()]=new House(type);player.startAnimation(827);refresh(false);return true;
    }
    public int addSeeds(BirdhouseSpot spot,int item){
        House house=houses[spot.ordinal()];int strength=seedStrength(item);
        if(!reachable(spot)||house==null||strength==0||house.bait>=10)return 0;
        int quantity=Math.min((10-house.bait+strength-1)/strength,player.getItems().getInventoryCount(item));
        if(quantity<=0)return 0;
        player.getItems().deleteItem2(item,quantity);house.bait=Math.min(10,house.bait+quantity*strength);
        if(house.bait==10){house.started=clock.getAsLong();player.sendMessage("The bird house is baited. It will fill in about 50 minutes, including while offline.");}
        refresh(false);return quantity;
    }
    public int birds(BirdhouseSpot spot){House house=houses[spot.ordinal()];return house==null||house.bait<10?0:(int)Math.min(10,Math.max(0,(clock.getAsLong()-house.started)/BIRD_MILLIS));}
    public int remainingMinutes(BirdhouseSpot spot){House house=houses[spot.ordinal()];return house==null||house.bait<10?50:(int)Math.max(0,(BIRD_MILLIS*10-Math.max(0,clock.getAsLong()-house.started)+59_999)/60_000);}
    public boolean dismantle(BirdhouseSpot spot,boolean reward){
        House house=houses[spot.ordinal()];if(house==null||!reachable(spot)||reward&&birds(spot)<10)return false;
        if(player.getItems().freeSlots()<1){player.sendMessage("You need one free inventory space for the clockwork.");return false;}
        houses[spot.ordinal()]=null;player.getItems().addItem(CLOCKWORK,1);
        if(reward){BirdhouseLoot.reward(player,house.type);player.getPA().addSkillXPMultiplied(house.type.hunterXp,21,true);}
        player.startAnimation(827);refresh(false);return true;
    }
    public int configuration(BirdhouseSpot spot){House house=houses[spot.ordinal()];return house==null?0:1+house.type.ordinal()*3+(house.bait==10?(birds(spot)>=10?2:1):0);}
    public void refresh(boolean force){for(BirdhouseSpot spot:BirdhouseSpot.values()){int state=configuration(spot);if(force||sent[spot.ordinal()]!=state){player.getPA().sendConfig(spot.config,state);sent[spot.ordinal()]=state;}}}
    public String encode(){StringJoiner data=new StringJoiner("|");for(House house:houses)data.add(house==null?"-":house.type.ordinal()+","+house.bait+","+house.started);return data.toString();}
    public boolean decode(String data){
        try{String[] parts=data.split("\\|",-1);if(parts.length!=4)return false;House[] loaded=new House[4];
            for(int i=0;i<4;i++){if(parts[i].equals("-"))continue;String[] fields=parts[i].split(",",-1);if(fields.length!=3)return false;int tier=Integer.parseInt(fields[0]),bait=Integer.parseInt(fields[1]);long started=Long.parseLong(fields[2]);if(tier<0||tier>=9||bait<0||bait>10||started<0||bait==10&&started==0||bait<10&&started!=0)return false;House house=new House(BirdhouseType.values()[tier]);house.bait=bait;house.started=started;loaded[i]=house;}
            System.arraycopy(loaded,0,houses,0,4);Arrays.fill(sent,-1);return true;
        }catch(RuntimeException e){return false;}
    }
}
