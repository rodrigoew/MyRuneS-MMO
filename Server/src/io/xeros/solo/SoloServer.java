package io.xeros.solo;
import io.xeros.Server;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import java.io.*;
public final class SoloServer {
    public static void main(String[] args) throws Exception {
        System.setProperty("solo.mode","true");
        Server.main(args);
        PlayerHandler.addQueuedAction(SoloAutosave::start);
        BufferedReader input=new BufferedReader(new InputStreamReader(System.in));
        String line;
        while((line=input.readLine())!=null) {
            if(line.equals("stop")) {
                PlayerHandler.addQueuedAction(()->{
                    for(Player p:PlayerHandler.getPlayers()) {
                        if(!PlayerSave.saveGameInstant(p)) {
                            System.err.println("SOLO_SAVE_FAILED "+p.getLoginName()); return;
                        }
                    }
                    System.out.println("SOLO_SAVED");
                    System.exit(0);
                });
            } else if(line.equals("qa nex") && Boolean.getBoolean("solo.qa")) {
                PlayerHandler.addQueuedAction(()->{
                    Player p=PlayerHandler.getPlayerByLoginName("soloteste");
                    if(p==null) return;
                    p.getPA().closeAllWindows();
                    new io.xeros.content.commands.owner.Setlevel().execute(p,"setlevel","0 22 99");
                    p.getHealth().setMaximumHealth(99);
                    p.getHealth().reset();
                    p.getItems().equipItem(26382,1,Player.playerHat);
                    p.getItems().equipItem(26384,1,Player.playerChest);
                    p.getItems().equipItem(26386,1,Player.playerLegs);
                    p.getItems().equipItem(4151,1,Player.playerWeapon);
                    io.xeros.content.commands.CommandManager.execute(p,"solo nex");
                    System.out.println("SOLO_QA_NEX_ENTERED "+p.getPosition());
                });
            }
        }
        // A closed launcher also closes stdin: save and terminate its world.
        PlayerHandler.addQueuedAction(() -> {
            if (SoloAutosave.saveOnline(false)) {
                System.out.println("SOLO_SAVED_ON_LAUNCHER_EXIT");
                System.exit(0);
            }
        });
    }
}
