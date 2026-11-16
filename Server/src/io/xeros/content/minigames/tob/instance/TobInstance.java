package io.xeros.content.minigames.tob.instance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import io.xeros.Server;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueOption;
import io.xeros.content.instances.InstanceConfiguration;
import io.xeros.content.instances.InstancedArea;
import io.xeros.content.minigames.tob.TobConstants;
import io.xeros.content.minigames.tob.TobRoom;
import io.xeros.content.minigames.tob.rooms.RoomSevenTreasure;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.player.Player;
import io.xeros.content.party.PlayerParty;
import io.xeros.model.entity.player.Position;
import io.xeros.model.items.GameItem;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;
import io.xeros.util.logging.player.DiedAtTobLog;

public class TobInstance extends InstancedArea {

    public static final String TOB_DEAD_ATTR_KEY = "dead_tob";
    private static final int TREASURE_ROOM_INDEX = 6;

    private final HashSet<String> chestRewards = new HashSet<>();
    private final FoodRewards foodRewards = new FoodRewards();
    private final MvpPoints mvpPoints = new MvpPoints();
    private final HashMap<String, List<GameItem>> chestRewardItems = new HashMap<>();
    private int roomIndex = -1;
    private boolean fightStarted = false;
    private boolean lastRoom;
    private final int size;

    private boolean finalBossComplete;
    private boolean started,transitionPending;
    private final java.util.Set<String> deliveredRewards=new java.util.HashSet<>();
    public int getRoomIndex(){return roomIndex;}
    public boolean consumeReward(Player p){return p.getInstance()==this&&finalBossComplete&&roomIndex==6&&chestRewards.contains(p.getLoginName())&&chestRewardItems.get(p.getLoginName())!=null&&deliveredRewards.add(p.getLoginName());}
    public static boolean enterSolo(Player p){
        if(!Boolean.getBoolean("solo.mode")||!p.getUtilityMagic().canInteract()||p.getInstance()!=null)return false;
        if(p.getPA().calculateTotalLevel()<p.getMode().getTotalLevelForTob()){p.sendMessage("Theatre of Blood exige nível total "+p.getMode().getTotalLevelForTob()+".");return false;}
        TobInstance raid=new TobInstance(1);raid.start(java.util.List.of(p));return p.getInstance()==raid;
    }
    private boolean solo(){return Boolean.getBoolean("solo.mode")&&size==1;}
    private boolean roomComplete(){return getNpcs().stream().noneMatch(n->!n.isDead()&&!n.isUnregister()&&n.getHealth().getCurrentHealth()>0);}
    private void watchSolo(){
        io.xeros.model.cycleevent.CycleEventHandler.getSingleton().addEvent(this,new io.xeros.model.cycleevent.CycleEvent(){public void execute(io.xeros.model.cycleevent.CycleEventContainer e){
            if(isDisposed()||getPlayers().isEmpty()){e.stop();return;}
            Player p=getPlayers().get(0);if(p.isDead||p.isDisconnected())return;
            if(roomIndex<6&&fightStarted&&roomComplete()&&!transitionPending&&(roomIndex!=5||finalBossComplete)){
                transitionPending=true;final int old=roomIndex;
                io.xeros.model.cycleevent.CycleEventHandler.getSingleton().addEvent(TobInstance.this,new io.xeros.model.cycleevent.CycleEvent(){public void execute(io.xeros.model.cycleevent.CycleEventContainer next){if(!isDisposed()&&p.getInstance()==TobInstance.this&&roomIndex==old)moveToNextRoom(p);transitionPending=false;next.stop();}},5);
            }
        }},1);
    }

    public TobInstance(int size) {
        super(InstanceConfiguration.CLOSE_ON_EMPTY, TobConstants.ALL_BOUNDARIES);
        this.size = size;
    }

    @Override
    public void remove(Player player) {
        if (roomIndex == TREASURE_ROOM_INDEX) {
            RoomSevenTreasure.openChest(player, (TobInstance) player.getInstance());
        }

        super.remove(player);
        player.getBossTimers().remove(TobConstants.THEATRE_OF_BLOOD);
    }

    public void removeButLeaveInParty(Player player) {
        super.remove(player);
    }

    public void start(List<Player> playerList){
        if(started||isDisposed())return;
        if(playerList.isEmpty()||playerList.stream().anyMatch(p->p.getInstance()!=null||!p.getUtilityMagic().canInteract()||p.getPA().calculateTotalLevel()<p.getMode().getTotalLevelForTob())){dispose();return;}
        started=true;playerList.forEach(this::add);initialiseNextRoom(playerList.get(0));TobRoom room=getCurrentRoom();
        playerList.forEach(p->{p.moveTo(resolve(room.getPlayerSpawnPosition()));p.getAttributes().removeBoolean(TOB_DEAD_ATTR_KEY);p.getPA().closeAllWindows();p.getBossTimers().track(TobConstants.THEATRE_OF_BLOOD);p.sendMessage("Theatre of Blood: raid iniciada.");});
        if(solo()){fightStarted=true;Position fight=room.getFightStartPosition();if(fight!=null)playerList.get(0).moveTo(resolve(fight));watchSolo();}
    }

    private void initialiseNextRoom(Player player) {
        if(roomIndex>=6)return;
        roomIndex++;
        TobRoom tobRoom = TobConstants.ROOM_LIST.get(roomIndex);
        var boss = tobRoom.spawn(this);
        if (boss != null) {
            var modifier = TobConstants.getHealthModifier(size);
            var maxHealth = (int) (boss.getHealth().getMaximumHealth() * modifier);
            boss.getHealth().setMaximumHealth(Math.max(1,maxHealth));
            boss.getHealth().reset();
        }
        GlobalObject foodChest = tobRoom.getFoodChestPosition();
        if (foodChest != null) {
            Server.getGlobalObjects().add(foodChest.withHeight(resolveHeight(foodChest.getHeight())).setInstance(this));
        }
        fightStarted = false;
    }

    public void moveToNextRoom(Player player) {
        if(player.getInstance()!=this||player.isDead||isDisposed()||roomIndex<0||roomIndex>=6)return;
        if(solo()){
            if(!roomComplete()||roomIndex==5&&!finalBossComplete){player.sendMessage("Derrote os inimigos desta sala antes de avançar.");return;}
            for(var npc:new java.util.ArrayList<>(getNpcs()))npc.unregister();
            initialiseNextRoom(player);player.healEverything();TobRoom next=getCurrentRoom();Position pos=next.getFightStartPosition();player.moveTo(resolve(pos==null?next.getPlayerSpawnPosition():pos));fightStarted=true;lastRoom=false;
            if(roomIndex==6){RoomSevenTreasure.openChest(player,this);io.xeros.model.entity.player.save.PlayerSave.saveGame(player);}else player.sendMessage("Theatre of Blood: sala "+(roomIndex+1)+"/6.");return;
        }
        if (getCurrentRoom().isRoomComplete(this) || getPlayerRoomIndex(player) < roomIndex) {
            int nextRoomIndex = getPlayerRoomIndex(player) + 1;
            if (roomIndex < nextRoomIndex) {
                initialiseNextRoom(player);
            }

            player.healEverything();
            Position playerSpawnPosition = resolve(TobConstants.ROOM_LIST.get(nextRoomIndex).getPlayerSpawnPosition());
            player.moveTo(playerSpawnPosition);
            player.getAttributes().removeBoolean(TOB_DEAD_ATTR_KEY);
        } else {
            player.sendMessage("You haven't completed this room yet!");
        }
    }

    private int getPlayerRoomIndex(Player player) {
        for (int index = 0; index < TobConstants.ALL_BOUNDARIES.length; index++) {
            if (TobConstants.ALL_BOUNDARIES[index].in(player)) {
                return index;
            }
        }

        return -1;
    }

    @Override
    public boolean handleClickObject(Player player, WorldObject object, int option) {
        if (object.getId() == TobConstants.BOSS_GATE_OBJECT_ID || object.getId() == TobConstants.ENTER_FINAL_ROOM_OBJECT_ID) {
            if (getPlayerRoomIndex(player) == roomIndex && !getCurrentRoom().isRoomComplete(this)
                || lastRoom && object.getId() == TobConstants.ENTER_FINAL_ROOM_OBJECT_ID) {// In last unlocked room and fight not completed
                if (!fightStarted || lastRoom) {
                    if (player.equals(player.getInstance().getPlayers().get(0))) {
                        player.start(new DialogueBuilder(player).option(new DialogueOption("Start fight", this::startFight),
                                new DialogueOption("Cancel", plr -> plr.getPA().closeAllWindows())));
                    } else {
                        player.sendMessage("Only the party leader can start a fight.");
                    }
                } else {
                    if (player.getAttributes().getBoolean(TOB_DEAD_ATTR_KEY)) {
                        player.sendMessage("You've been disqualified from the fight for dying, you must wait.");
                    } else {
                        player.sendMessage("The fight has started, there's no turning back now.");
                    }
                }
            } else {                                                                                                          // In room before last unlocked or room complete
                Optional<TobRoom> gateRoomOptional = TobConstants.ROOM_LIST.stream().filter(gateRoom -> gateRoom.getBoundary().in(player)).findFirst();
                gateRoomOptional.ifPresent(tobRoom -> gateRoomOptional.get().handleClickBossGate(player, object));
            }

            return true;
        }

        for (TobRoom room : TobConstants.ROOM_LIST) {
            if (room.handleClickObject(player, object, option)) {
                return true;
            }
        }

        switch (object.getId()) {
            case TobConstants.TREASURE_ROOM_ENTRANCE_OBJECT_ID:
            case TobConstants.ENTER_NEXT_ROOM_OBJECT_ID:
            case TobConstants.ENTER_FINAL_ROOM_OBJECT_ID:
                moveToNextRoom(player);
                return true;
            case TobConstants.FOOD_CHEST_OBJECT_ID:
                foodRewards.openFoodRewards(player);
                return true;
        }

        return false;
    }

    @Override
    public boolean handleDeath(Player player) {
        if(solo()){removeButLeaveInParty(player);player.moveTo(TobConstants.FINISHED_TOB_POSITION);player.healEverything();player.getAttributes().removeBoolean(TOB_DEAD_ATTR_KEY);player.sendMessage("A tentativa no Theatre of Blood terminou. Seus equipamentos foram preservados.");return true;}
        int roomIndex = getPlayerRoomIndex(player);
        if (roomIndex == -1) {
            player.moveTo(TobConstants.FINISHED_TOB_POSITION);
            player.sendMessage("Could not handle death!");
            return true;
        }

        TobRoom room = TobConstants.ROOM_LIST.get(getPlayerRoomIndex(player));
        player.moveTo(resolve(room.getDeathPosition()));
        player.sendMessage("Oh dear, you have died!");
        player.getAttributes().setBoolean(TOB_DEAD_ATTR_KEY, true);
        Server.getLogging().write(new DiedAtTobLog(player, this));

        if (allDead()) {
            Lists.newArrayList(getPlayers()).forEach(plr -> {
                plr.moveTo(TobConstants.FINISHED_TOB_POSITION);
                removeButLeaveInParty(plr);
                plr.sendMessage("Your performance in the theatre left much to be desired; your team has been defeated.");
            });
        }
        return true;
    }

    private void startFight(Player player) {
        if (getPlayers().stream().allMatch(plr -> getPlayerRoomIndex(plr) == roomIndex)) {
            fightStarted = true;
            TobRoom currentRoom = getCurrentRoom();
            getPlayers().forEach(plr -> {
                if (lastRoom)
                    moveToNextRoom(plr);
                else
                    plr.moveTo(resolve(currentRoom.getFightStartPosition()));
                plr.getPA().closeAllWindows();
            });
            lastRoom = false;
        } else {
            player.sendStatement("All players must be in the room to start the fight.");
        }
    }

    private boolean allDead() {
        return getPlayers().stream().allMatch(plr -> plr.getAttributes().getBoolean(TOB_DEAD_ATTR_KEY));
    }

    @Override
    public void onDispose() { io.xeros.model.cycleevent.CycleEventHandler.getSingleton().stopEvents(this); }

    private TobRoom getCurrentRoom() {
        return TobConstants.ROOM_LIST.get(roomIndex);
    }

    public HashSet<String> getChestRewards() {
        return chestRewards;
    }

    public FoodRewards getFoodRewards() {
        return foodRewards;
    }

    public MvpPoints getMvpPoints() {
        return mvpPoints;
    }

    public MvpPoints getChooseRareWinner() {
        return mvpPoints;
    }

    public HashMap<String, List<GameItem>> getChestRewardItems() {
        return chestRewardItems;
    }

    public int getPartySize() {
        return size;
    }

    public void setLastRoom(boolean lastRoom) {
        this.lastRoom = lastRoom;
    }

    public boolean isFinalBossComplete() {
        return finalBossComplete;
    }

    public void setFinalBossComplete(boolean finalBossComplete) {
        if(finalBossComplete&&!this.finalBossComplete){this.finalBossComplete=true;getPlayers().forEach(p->{p.tobCompletions++;p.sendMessage("Theatre of Blood concluído. Total: "+p.tobCompletions+".");});}
    }

}
