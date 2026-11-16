package io.xeros.model.entity.npc.pets;

import io.xeros.Server;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueOption;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCHandler;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSave;
import java.util.Random;

/** The golden form is a 1/10,000 metamorphosis, never a separate Hunter drop. */
public final class BabyChinchompaMorph2022 {
    public static final Random RANDOM=new Random();
    private BabyChinchompaMorph2022() { }

    private static boolean variant(int npc) { return npc>=6718 && npc<=6721; }
    private static int item(int npc) { return 13323+npc-6718; }

    private static boolean valid(Player p,NPC n) {
        return n!=null && variant(n.getNpcId()) && n.getIndex()>0 && NPCHandler.npcs[n.getIndex()]==n
                && !n.isUnregister() && !n.isDead() && !n.isInvisible() && !n.needRespawn
                && p.getIndex()>0 && n.spawnedBy==p.getIndex() && p.hasPetSpawned && p.hasFollower
                && p.petSummonId==item(n.getNpcId()) && p.getUtilityMagic().canInteract()
                && !p.getMovementState().isLocked() && !p.getInterfaceEvent().isActive()
                && !p.getBankPin().requiresUnlock() && p.getTeleportToX()==-1 && p.getTeleportToY()==-1
                && !Server.getMultiplayerSessionListener().inAnySession(p)
                && n.getInstance()==p.getInstance() && n.heightLevel==p.heightLevel
                && Math.max(Math.abs(p.absX-n.absX),Math.abs(p.absY-n.absY))<=2;
    }

    public static boolean click(Player p,NPC n) {
        if(n==null || !variant(n.getNpcId())) return false;
        if(!valid(p,n)) return true;
        p.getPA().resetFollow();p.stopMovement();
        if(n.getNpcId()==6721) {
            new DialogueBuilder(p).statement("Changing this golden chinchompa will remove its golden colour.")
                    .option("Change your golden chinchompa?",
                            new DialogueOption("Yes, change its colour.",q->{
                                q.getPA().removeAllWindows();
                                if(n.getNpcId()==6721 && valid(q,n)) transform(q,n,false);
                            }),
                            new DialogueOption("No, keep it golden.",q->q.getPA().removeAllWindows())).send();
        } else transform(p,n,true);
        return true;
    }

    private static void transform(Player p,NPC n,boolean goldRoll) {
        if(!valid(p,n)) return;
        p.getPetInsurance().recoverExistingOwnedPets();
        int next;
        if(goldRoll && RANDOM.nextInt(10000)==0) next=6721;
        else {
            int current=n.getNpcId();
            next=6718+RANDOM.nextInt(current==6721?3:2);
            if(current!=6721 && next>=current) next++;
        }
        n.requestTransform(next);
        p.petSummonId=item(next);
        p.sendMessage(next==6721?"Your baby chinchompa turns golden!":"Your baby chinchompa changes colour.");
        PlayerSave.saveGame(p);
    }
}
