package io.xeros.content.bosses.kratos;

import com.google.common.collect.Lists;
import io.xeros.model.CombatType;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.HealthStatus;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCHandler;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.model.entity.player.Position;



public class KratosNpc extends NPC {




    public static final Position SPAWN_POSITION = new Position(3027, 5233, 0);

    public static boolean spawned = false;

    private boolean minionSpawn;
    private int minionWaves;
    private final java.util.List<NPC> ownedMinions=new java.util.ArrayList<>();
    public int attackCounter;
    public Position nextWalk;

    public KratosNpc(int id, Position pos) {
        super(id, pos);
        resetForSpawn();
        spawned = true;
    }

    public void resetForSpawn() {
        walkingType = 1;
        nextWalk = null;
        attackCounter = 0;
        getBehaviour().setWalkHome(false);
        getBehaviour().setAggressive(true);
        if (getNpcId() == 6009) {//Kratos
            getBehaviour().setRespawn(true);
        } else {
            getBehaviour().setRespawn(false);
        }
        if (getNpcId() == 6006) {//Npcs.SPAWN_OF_SARACHNIS
            setNpcAutoAttacks(Lists.newArrayList(
                    new KratosMinionMelee().apply(this)
            ));
        } else if (getNpcId() == 6005) {//Npcs.SPAWN_OF_SARACHNIS_2
            setNpcAutoAttacks(Lists.newArrayList(
                    new KratosMinionMage().apply(this)
            ));

        } else {
            setNpcAutoAttacks(Lists.newArrayList(
                    new KratosMelee().apply(this),
                    new KratosRanged().apply(this)
            ));
        }
    }

    @Override
    public boolean susceptibleTo(HealthStatus status) {
        return false;
    }

    @Override
    public boolean hasBlockAnimation() {
        return false;
    }

    @Override
    public boolean canBeDamaged(Entity entity) {
        return !isDead();
    }

    @Override
    public boolean canBeAttacked(Entity entity) {
        return !isDead();
    }

    @Override
    public boolean isAutoRetaliate() {
        return getBehaviour().isAggressive() && !isDead() && nextWalk == null;
    }

    @Override
    public void process() {
        try {
            if (this.getNpcId() == 6009) {
                if (attackCounter >= 5) {
                    attackCounter = 0;
                    int p = getPlayerAttackingIndex();
                    if (PlayerHandler.players[p] != null) {
                        Player player = PlayerHandler.players[p];
                        this.attack(player, new KratosWeb().apply(this));
                    }
                }
                if(minionWaves<2&&getHealth().getCurrentHealth()<=getHealth().getMaximumHealth()*(minionWaves==0?.66:.33)){minionWaves++;minionSpawn=true;spawnMinions();}

                if (this.getNpcId() == 6008 && this.getHealth().getCurrentHealth() <= 450) {
                   // int p = getPlayerAttackingIndex();
                    this.setAttackType(CombatType.SPECIAL);
                    this.hitDelayTimer = 3;
                }


                if (nextWalk != null) {
                    if (getPosition().equals(nextWalk)) {
                        nextWalk = null;
                        getBehaviour().setRunnable(false);
                        getBehaviour().setAggressive(true);
                    } else {
                        getBehaviour().setRunnable(true);
                        moveTowards(nextWalk.getX(),nextWalk.getY(),false);
                    }
                }
            }
            processCombat();
        } catch (Exception e) {
            e.printStackTrace();
            unregister();
        }
    }

    private void processCombat() {
        super.process();
    }

    @Override
    public NPC provideRespawnInstance() {
        NPC boss = new KratosNpc(6009, SPAWN_POSITION);
        boss.getBehaviour().setAggressive(true);
        return boss;
    }

    @Override
    public void onDeath() {
        super.onDeath();
        if(getNpcId()==6009||getNpcId()==6008){spawned=false;despawnMinions();minionSpawn=false;minionWaves=0;}
    }
    private void spawnMinions(){for(int id:new int[]{6006,6005,6005}){NPC n=new KratosNpc(id,new Position(getX(),getY(),getHeight()));if(getInstance()!=null)getInstance().add(n);n.setPlayerAttackingIndex(getPlayerAttackingIndex());ownedMinions.add(n);}}
    private void despawnMinions(){for(NPC n:ownedMinions)if(!n.isUnregister())n.unregister();ownedMinions.clear();}

    private boolean minionsDead() {
        return NPCHandler.getNpc(6006, getHeight()) == null || NPCHandler.getNpc(6005, getHeight()) == null;
    }


    private void enrangedSpecial(NPC npc) {
        NPCHandler.handler.groundSpell(npc, null, 315, 317, "corp", 4);
    }

}
