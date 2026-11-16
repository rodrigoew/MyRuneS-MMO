package io.xeros.content.skills.prayer;
import io.xeros.Server;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.npc.NPC;
import io.xeros.util.Location3D;
import java.util.*;
/** Demonic remains added in June 2021; kept distinct from altar-compatible bones. */
public enum Ashes {
 FIENDISH(25766,10),VILE(25769,25),MALICIOUS(25772,65),ABYSSAL(25775,85),INFERNAL(25778,110);
 public final int item;public final double xp;
 Ashes(int item,double xp){this.item=item;this.xp=xp;}
 public static Ashes forItem(int item){for(Ashes a:values())if(a.item==item)return a;return null;}
 public static Ashes forNpcName(String name){
  if(name==null)return null;String n=name.toLowerCase(Locale.ROOT);
  if(Arrays.asList("cerberus","k'ril tsutsaroth","skotizo","zalcano").contains(n))return INFERNAL;
  if(Arrays.asList("abyssal demon","greater abyssal demon","abyssal sire").contains(n))return ABYSSAL;
  if(Arrays.asList("black demon","nechryael","greater nechryael","demonic gorilla","jungle demon","balfrug kreeyath","tstanon karlak","zakl'n gritch").contains(n))return MALICIOUS;
  if(Arrays.asList("lesser demon","greater demon","hellhound","bloodveld","mutated bloodveld","agrith naar","othainian","holthion","doomion","chronozon","bouncer").contains(n))return VILE;
  if(Arrays.asList("imp","pyrefiend","icefiend","waterfiend","pyrelord","death spawn","scion","spawn").contains(n))return FIENDISH;
  return null;
 }
 public static void drop(Player p,NPC npc,Location3D position){Ashes ash=forNpcName(npc.getName());if(ash!=null)Server.itemHandler.createGroundItem(p,ash.item,position.getX(),position.getY(),position.getZ(),1,p.getIndex());}
}
