package io.xeros.solo;

import io.xeros.Server;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;

/** Scheduled by the game loop so inventory and XP are saved at the same tick. */
public final class SoloAutosave {
    private static final Object OWNER = new Object();
    private SoloAutosave() {}
    public static void start() {
        Server.getEventHandler().stop(OWNER);
        Server.getEventHandler().submit(new Event<Object>("solo-autosave", OWNER, 50) {
            public void execute() { saveOnline(false); }
        });
    }
    public static boolean saveOnline(boolean notify) {
        boolean saved = true; int count = 0;
        for (Player player : PlayerHandler.getPlayers()) {
            if (!player.saveCharacter || player.isBot()) continue;
            boolean ok = PlayerSave.saveGameInstant(player);
            saved &= ok; count++;
            if (!ok) System.err.println("SOLO_AUTOSAVE_FAILED");
            if (notify && ok) player.sendMessage("Your progress has been saved.");
        }
        if (count > 0 && saved) System.out.println("SOLO_AUTOSAVED");
        return saved;
    }
}
