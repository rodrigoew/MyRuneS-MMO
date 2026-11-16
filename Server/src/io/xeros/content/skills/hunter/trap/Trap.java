package io.xeros.content.skills.hunter.trap;

import com.google.common.collect.ImmutableSet;
import io.xeros.content.skills.hunter.GroundTrapHunting;
import io.xeros.content.skills.hunter.GroundTrapSpecies2022;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.RandomGen;
import java.util.*;
import java.util.stream.IntStream;

/** Trap handle retained from Stand Up's original Hunter API; lifecycle belongs to GroundTrapHunting. */
public abstract class Trap {
    public final RandomGen random=new RandomGen();
    protected final Player player;
    private final TrapType type;
    private TrapState state=TrapState.PENDING;
    private GlobalObject object;
    private boolean abandoned;
    private GroundTrapSpecies2022 captured;
    private GameItem[] loot;

    protected Trap(Player player,TrapType type) {
        this.player=player;this.type=type;
        object=new GlobalObject(type.objectId,player.absX,player.absY,player.heightLevel);
    }
    public final void synchronize(GlobalObject object,TrapState state) { this.object=object;this.state=state; }
    public final void caught(GroundTrapSpecies2022 species,GameItem[] items) {
        captured=species;
        loot=items==null?null:Arrays.stream(items).map(i->new GameItem(i.getId(),i.getAmount())).toArray(GameItem[]::new);
    }
    public void trap(NPC npc) { GroundTrapHunting.lure(this,npc); }
    public boolean canCatch(NPC npc) {
        GroundTrapSpecies2022 species=npc==null?null:GroundTrapSpecies2022.npc(npc.getNpcId());
        return species!=null && species.trapItem()==type.itemId && player.playerLevel[21]>=species.level;
    }
    public int successFormula(NPC npc) {
        GroundTrapSpecies2022 species=npc==null?null:GroundTrapSpecies2022.npc(npc.getNpcId());
        return species==null?0:species.threshold(player.playerLevel[21],false,0)/100;
    }
    /** Camouflage is cosmetic; this compatibility query never modifies capture chance or XP. */
    public boolean hasLarupiaSetEquipped() {
        return IntStream.of(10041,10043,10045).allMatch(id->player.getItems().isWearingItem(id));
    }
    public void submit() { }
    public void onSetup() { }
    public void onPickUp() { }
    public void onCatch(NPC npc) { trap(npc); }
    public void onSequence(CycleEventContainer container) { }
    public GameItem[] reward() {
        if(loot==null) throw new IllegalStateException("No captured animal.");
        return Arrays.stream(loot).map(i->new GameItem(i.getId(),i.getAmount())).toArray(GameItem[]::new);
    }
    public double experience() {
        if(captured==null) throw new IllegalStateException("No captured animal.");
        return captured.xp;
    }
    public boolean canClaim(GlobalObject candidate) {
        return state==TrapState.CAUGHT && captured!=null && candidate.getObjectId()==object.getObjectId()
                && candidate.getX()==object.getX() && candidate.getY()==object.getY() && candidate.getHeight()==object.getHeight();
    }
    public Player getPlayer() { return player; }
    public TrapType getType() { return type; }
    public TrapState getState() { return state; }
    public void setState(TrapState state) { this.state=state; }
    public GlobalObject getObject() { return object; }
    public boolean isAbandoned() { return abandoned; }
    public void setAbandoned(boolean value) { abandoned=value; }

    public enum TrapType {
        BOX_TRAP(9380,10008),DISMANTLED_BOX_TRAP(9385,10008),DISMANTLED_BIRD_SNARE(9344,10006),BIRD_SNARE(9345,10006);
        private final int objectId,itemId;
        TrapType(int objectId,int itemId) { this.objectId=objectId;this.itemId=itemId; }
        public int getObjectId() { return objectId; }
        public int getItemId() { return itemId; }
        public static Optional<TrapType> getTrapByObjectId(int id) { return Arrays.stream(values()).filter(t->t.objectId==id).findFirst(); }
        public static Optional<TrapType> getTrapByItemId(int id) { return Arrays.stream(values()).filter(t->t.itemId==id).findFirst(); }
    }
    public enum TrapState { PENDING,TRIGGERED,CAUGHT,FALLEN }
}
