package io.xeros.content.minigames.inferno;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.xeros.Server;
import io.xeros.achievements.AchievementHandler;
import io.xeros.achievements.AchievementList;
import io.xeros.content.minigames.rfd.DisposeTypes;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.definitions.NpcDef;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCHandler;
import io.xeros.model.entity.npc.NPCSpawning;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;

public class Inferno extends Tzkalzuk {

    private static final int DEFAULT_WAVE = 0;

    public static void startInferno(Player player, int wave) {
        if(!player.getUtilityMagic().canInteract()||player.getInstance()!=null)return;
        if(wave<0||wave>=InfernoWaveData.LEVEL.length)return;
        new Inferno(player, Boundary.INFERNO).create(wave==DEFAULT_WAVE?player.infernoSoloWave:wave);
    }

    public static void moveToExit(Player player) {
        player.setTeleportToX(EXIT.getX());
        player.setTeleportToY(EXIT.getY());
        player.heightLevel = 0;
    }

    public static int getDefaultWave() {
        return DEFAULT_WAVE;
    }

    public static void reset(Player player) {
        Inferno inferno = player.getInferno();
        if (inferno != null) {
            inferno.remove(player);
        }
    }

    public static void gamble(Player player) {
        if (!player.getItems().playerHasItem(21295)) {
            player.sendMessage("You do not have an infernal cape.");
            return;
        }

        player.getItems().deleteItem(21295, 1);

        if (Misc.random(25) == 0) {
            PlayerHandler.executeGlobalMessage("[@red@PET@bla@] @cr20@<col=255> " + player.getDisplayName() + "</col> received a pet from <col=255>TzKal-Zuk</col>.");
            player.getItems().addItemUnderAnyCircumstance(21291, 1);
            player.getDH().sendDialogues(74, 7690);
        } else {
            player.getDH().sendDialogues(73, 7690);
            return;
        }
    }

    private int infernoWaveId = 0;
    private int infernoWaveType = 0;
    private int killsRemaining;
    boolean started;

    private final Walls walls = new Walls();

    public Inferno(Player player, Boundary boundary) {
        super(player, boundary);
    }

    public void create(int wave) {
        player.getPA().removeAllWindows();
        player.getPA().movePlayer(2271, 5329, getHeight());
        add(player);
        setInfernoWaveType(wave);
        setInfernoWaveId(wave);
        walls.setDefaultWallAttributes();
        player.infernoLeaveTimer = System.currentTimeMillis();
        walls.createWalls(player, this);
        spawn();
        player.sendMessage("Inferno solo: 69 ondas. Ao sair, use ::solo inferno para reiniciar a última onda salva.", 255);
    }

    public void leaveGame(){end(DisposeTypes.INCOMPLETE);}

    public void stop(){end(DisposeTypes.COMPLETE);}
    public void handleDeath(){handleDeath(player);}
    @Override public boolean handleDeath(Player p){if(p!=player)return false;finished=true;p.infernoSoloWave=0;remove(p);moveToExit(p);p.getHealth().reset();p.sendMessage("Tentativa de Inferno encerrada. Equipamentos preservados.");io.xeros.model.entity.player.save.PlayerSave.saveGame(p);return true;}
    public final void end(DisposeTypes result){
      if(finished||isDisposed()||player.getInferno()!=this)return;
      if(result==DisposeTypes.COMPLETE){if(infernoWaveId!=68||tzkal==null||!tzkal.isDead()||!zukDefeated)return;finished=true;zukDead=true;player.infernoSoloWave=0;player.infernoSoloCompletions++;io.xeros.content.minigames.SoloActivityRewards.give(player,java.util.List.of(new io.xeros.model.items.GameItem(TOKKUL,100000),new io.xeros.model.items.GameItem(21295)),EXIT);player.sendMessage("Inferno concluído! Infernal cape e Tokkul entregues. Progresso salvo.");}
      remove(player);moveToExit(player);io.xeros.model.entity.player.save.PlayerSave.saveGame(player);
    }
    @Override public void onDispose(){CycleEventHandler.getSingleton().stopEvents(this);cutsceneWalkBlock=false;player.getPA().resetCamera();}

    private boolean finished,spawnPending,zukDefeated;
    private final java.util.Set<NPC> credited=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    public boolean active(){return !finished&&!isDisposed()&&player.getInferno()==this&&!player.isDead&&!player.isDisconnected();}
    public void onNpcDeath(NPC npc){if(!active()||npc.getInstance()!=this||!npc.isDead()||!credited.add(npc))return;
      if(npc.getNpcId()==InfernoWaveData.TZKAL_ZUK){if(infernoWaveId==68&&npc==tzkal&&npc.getHealth().getCurrentHealth()==0)zukDefeated=true;return;}if(infernoWaveId>=68)return;
      if(--killsRemaining==0){infernoWaveId++;player.infernoSoloWave=infernoWaveId;io.xeros.model.entity.player.save.PlayerSave.saveGame(player);spawn();}}
    public void spawn() {
        if(!active()||spawnPending)return;spawnPending=true;started=false;
        player.infernoSoloWave=Math.max(0,Math.min(68,infernoWaveId));io.xeros.model.entity.player.save.PlayerSave.saveGame(player);
        final int[][] type = InfernoWaveData.LEVEL;
        if (getInfernoWaveId() >= type.length && getInfernoWaveType() > 0 && Boundary.isIn(player, Boundary.INFERNO)) {
            if (player.getInferno().zukDead)
                stop();
            return;
        }

        final Inferno instance = this;
        CycleEventHandler.getSingleton().addEvent(this, new CycleEvent() {
            @Override
            public void execute(CycleEventContainer event) {
                if (!active()) {
                    event.stop();
                    return;
                }
                if (!Boundary.isIn(player, Boundary.INFERNO)) {
                    reset(player);
                    event.stop();
                    return;
                }
                if (getInfernoWaveId() >= type.length && getInfernoWaveType() > 0) {
                    onStopped();
                    event.stop();
                    return;
                }
                if (getInfernoWaveId() != 66 && getInfernoWaveId() != 67 && getInfernoWaveId() != 68) {
                    walls.createWalls(player, instance);
                }
                spawnPending=false;started = true;

                if (getInfernoWaveId() != 0 && getInfernoWaveId() < type.length)
                    player.sendMessage("@red@Onda " + (getInfernoWaveId() + 1) + "/" + type.length + ".", 255);
                if (getInfernoWaveId() == 68) {
                    initiateTzkalzuk();
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2270, 5333, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30354, 2270, 5333, getHeight(), instance);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2259, 5349, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30355, 2259, 5349, getHeight(), instance);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2278, 5349, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30355, 2278, 5349, getHeight(), instance);
                    walls.killWalls();
                }
                if (getInfernoWaveId() == 66) {
                    setKillsRemaining(1);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2270, 5333, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30354, 2270, 5333, getHeight(), instance);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2259, 5349, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30355, 2259, 5349, getHeight(), instance);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2278, 5349, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30355, 2278, 5349, getHeight(), instance);
                    walls.killWalls();
                    NPC jad = NPCSpawning.spawnNpcOld(player, InfernoWaveData.JALTOK_JAD, 2271 + Misc.random(-4, 4), 5342 + Misc.random(-4, 4), getHeight(), 1, InfernoWaveData.getHp(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getMax(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getAtk(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getDef(InfernoWaveData.JALTOK_JAD), false, false);
                    jad.setInstance(Inferno.this);
                    JadCombat.start(jad, player);
                }
                if (getInfernoWaveId() == 67) {
                    setKillsRemaining(3);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2270, 5333, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30354, 2270, 5333, getHeight(), instance);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2259, 5349, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30355, 2259, 5349, getHeight(), instance);
                    Server.getGlobalObjects().add(new GlobalObject(-1, 2278, 5349, getHeight()).setInstance(instance));
                    Server.getGlobalObjects().remove(30355, 2278, 5349, getHeight(), instance);
                    walls.killWalls();
                    CycleEventHandler.getSingleton().addEvent(instance, new CycleEvent() {
                        NPC jad1;
                        NPC jad2;
                        NPC jad3;
                        int ticks;

                        @Override
                        public void execute(CycleEventContainer container) {
                            if(!active()){container.stop();return;}
                            if (ticks == 0) {
                                jad1 = NPCSpawning.spawnNpcOld(player, InfernoWaveData.JALTOK_JAD, 2263, 5341, getHeight(), 1, InfernoWaveData.getHp(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getMax(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getAtk(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getDef(InfernoWaveData.JALTOK_JAD), false, false);
                                jad1.setInstance(player.getInstance());
                                JadCombat.start(jad1, player);
                            }
                            if (ticks == 2) {
                                jad2 = NPCSpawning.spawnNpcOld(player, InfernoWaveData.JALTOK_JAD, 2269, 5347, getHeight(), 1, InfernoWaveData.getHp(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getMax(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getAtk(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getDef(InfernoWaveData.JALTOK_JAD), false, false);
                                jad2.setInstance(player.getInstance());
                                JadCombat.start(jad2, player);
                            }
                            if (ticks == 4) {
                                jad3 = NPCSpawning.spawnNpcOld(player, InfernoWaveData.JALTOK_JAD, 2276, 5341, getHeight(), 1, InfernoWaveData.getHp(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getMax(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getAtk(InfernoWaveData.JALTOK_JAD), InfernoWaveData.getDef(InfernoWaveData.JALTOK_JAD), false, false);
                                jad3.setInstance(player.getInstance());
                                JadCombat.start(jad3, player);
                                container.stop();
                            }
                            ticks++;
                        }
                    }, 1);
                }

                if (getInfernoWaveId() < 66) {
                    setKillsRemaining(type[getInfernoWaveId()].length + 3);
                }

                if (getInfernoWaveId() < 66) {
                    for (int i = 0; i < getKillsRemaining() - 3; i++) {
                        final int npcType = type[getInfernoWaveId()][i];
                        int npcSize = NpcDef.forId(npcType).getSize();
                        int startX = 2271;
                        int startY = 5342;
                        int attempts=0;do {
                            startX += Misc.random(-5, 5);
                            startY += Misc.random(-5, 5);
                        } while(++attempts<50&&player.getRegionProvider().isOccupiedByNpc(npcSize, startX, startY, player.getHeight()));
                        final int hp = InfernoWaveData.getHp(npcType);
                        final int maxhit = InfernoWaveData.getMax(npcType);
                        final int atk = InfernoWaveData.getAtk(npcType);
                        final int def = InfernoWaveData.getDef(npcType);
                        NPC spawn = NPCSpawning.spawnNpcOld(player, npcType, startX, startY, getHeight(), 1, hp, maxhit, atk, def, true, false);
                        spawn.setInstance(player.getInstance());
                    }
                }
                event.stop();
            }

            @Override
            public void onStopped() {
            }
        }, 16);

        if (getInfernoWaveId() < 66) {
            walls.walls(player, this, type);
        }
    }

    public void killAllSpawns() {
        for (int i = 0; i < NPCHandler.npcs.length; i++) {
            NPC npc = NPCHandler.npcs[i];
            if (npc != null) {
                if (NPCHandler.isInfernoNpc(npc)) {
                    if (NPCHandler.isSpawnedBy(player, npc)) {
                        npc.unregister();
                    }
                }
            }
        }
        Server.getGlobalObjects().add(new GlobalObject(-1, 2270, 5333, getHeight()).setInstance(this));
        Server.getGlobalObjects().remove(30354, 2270, 5333, getHeight(), this);
        Server.getGlobalObjects().add(new GlobalObject(-1, 2259, 5349, getHeight()).setInstance(this));
        Server.getGlobalObjects().remove(30355, 2259, 5349, getHeight(), this);
        Server.getGlobalObjects().add(new GlobalObject(-1, 2278, 5349, getHeight()).setInstance(this));
        Server.getGlobalObjects().remove(30355, 2278, 5349, getHeight(), this);
        walls.killWalls();
        walls.killNibs();
    }



    public int getKillsRemaining() {
        return killsRemaining;
    }

    public void setKillsRemaining(int remaining) {
        this.killsRemaining = remaining;
    }

    public int getInfernoWaveId() {
        return infernoWaveId;
    }

    public void setInfernoWaveId(int infernoWaveId) {
        this.infernoWaveId = infernoWaveId;
    }

    public int getInfernoWaveType() {
        return infernoWaveType;
    }

    public void setInfernoWaveType(int infernoWaveType) {
        this.infernoWaveType = infernoWaveType;
    }
}