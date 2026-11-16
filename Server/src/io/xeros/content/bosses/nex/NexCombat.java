package io.xeros.content.bosses.nex;
import io.xeros.content.instances.InstancedArea;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
/** A delayed hit belongs to the encounter that created it, not just matching map coordinates. */
public final class NexCombat {
 private NexCombat(){}
 public static boolean active(Player p,InstancedArea arena){return p!=null&&arena instanceof Nex&&!arena.isDisposed()&&p.getInstance()==arena&&arena.getPlayers().contains(p)&&!p.isDead&&p.getHealth().getCurrentHealth()>0&&!p.isDisconnected()&&p.getSession()!=null&&p.heightLevel==arena.getHeight()&&NexNPC.BOUNDARY.in(p);}
 public static boolean active(NPC n,Player p){return n!=null&&!n.isDead()&&n.getHealth().getCurrentHealth()>0&&active(p,n.getInstance());}
 public static void clear(Player p){p.hasNexVirus=false;p.hasHadNexVirus=false;p.nexVirusTimer=0;}
}
