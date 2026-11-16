package io.xeros.model.entity.npc.pets;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;
import io.xeros.model.items.GameItem;
import java.util.*;
import java.util.stream.Collectors;

/** June-2022 insurance and shared reclaim tokens for the two Hunter pet families.
 * Further skilling/boss families can be registered after their acquisition paths are reviewed.
 */
public final class PetInsurance2022 {
    public static final int HERBI=21509, BABY_CHINCHOMPA=13324;
    private final Player player;
    private final Map<Integer,Integer> insured=new TreeMap<>();
    private int freeReclaims;
    private boolean restoredCurrentFormat;

    public PetInsurance2022(Player player) { this.player=player; }

    public static int family(int item) {
        if(item==HERBI) return HERBI;
        return item>=13323 && item<=13326 ? BABY_CHINCHOMPA : -1;
    }

    private static String name(int family) { return family==HERBI ? "Herbi" : "Baby chinchompa"; }
    public int freeReclaims() { return freeReclaims; }
    public boolean insured(int item) { return insured.containsKey(family(item)); }
    public boolean owns(int item) {
        int family=family(item);
        if(family<0) return false;
        if(family(player.petSummonId)==family) return true;
        int[] forms=family==HERBI ? new int[]{HERBI} : new int[]{13323,13324,13325,13326};
        for(int form:forms) if(player.getItems().getItemCount(form,false)>0) return true;
        return false;
    }

    /** Old Herbi saves load before or after the new record without duplicating free tokens. */
    public void importLegacyHerbi(boolean unlocked, int tokens) {
        if(restoredCurrentFormat) return;
        if(unlocked) insured.put(HERBI,HERBI); else insured.remove(HERBI);
        freeReclaims=tokens;
    }

    public void recoverExistingOwnedPets() {
        if(owns(HERBI)) insured.putIfAbsent(HERBI,HERBI);
        if(owns(BABY_CHINCHOMPA)) {
            int form=13324;
            for(int item:new int[]{13323,13324,13325})
                if(player.petSummonId==item || player.getItems().getItemCount(item,false)>0) { form=item; break; }
            insured.putIfAbsent(BABY_CHINCHOMPA,form);
        }
        // June 2022 also made an existing collection-log unlock or bank placeholder sufficient.
        // A shared custom GIM log does not establish personal pet ownership.
        if(!player.getCollectionLog().isGroupIronman() && player.getCollectionLog().getCollections()!=null)
            for(List<GameItem> collection:player.getCollectionLog().getCollections().values())
                if(collection!=null) for(GameItem item:collection)
                    if(item!=null && item.getAmount()>0) rememberExistingForm(item.getId());
        for(io.xeros.model.items.bank.BankTab tab:player.getBank().getBankTab())
            if(tab!=null) for(io.xeros.model.items.bank.BankItem item:tab.getItems())
                if(item!=null) rememberExistingForm(item.getId()-1);
    }

    private void rememberExistingForm(int item) {
        int key=family(item);
        if(key>=0) insured.putIfAbsent(key,item==13326?BABY_CHINCHOMPA:item);
    }

    public String encode() {
        String pets=insured.isEmpty() ? "-" : insured.entrySet().stream()
                .map(entry->entry.getKey()+"="+entry.getValue()).collect(Collectors.joining(":"));
        return "1,"+freeReclaims+","+pets;
    }

    public boolean restore(String value) {
        if(value==null || !value.matches("1,[0-9]{1,10},(?:-|[0-9=:]{1,80})")) return false;
        try {
            String[] parts=value.split(",",-1);
            int tokens=Integer.parseInt(parts[1]);
            Map<Integer,Integer> replacement=new TreeMap<>();
            if(!parts[2].equals("-")) for(String pet:parts[2].split(":",-1)) {
                String[] fields=pet.split("=",-1);
                if(fields.length!=2) return false;
                int key=Integer.parseInt(fields[0]),form=Integer.parseInt(fields[1]);
                if(key!=family(form) || key!=HERBI && key!=BABY_CHINCHOMPA || form==13326
                        || replacement.putIfAbsent(key,form)!=null) return false;
            }
            if(tokens<0) return false;
            insured.clear(); insured.putAll(replacement); freeReclaims=tokens;
            restoredCurrentFormat=true; return true;
        } catch(NumberFormatException exception) { return false; }
    }

    /** Resources are planned atomically, with a new pet taking the last free slot first.
     * Award is deferred so callers can commit their XP and activity state before follower spawning saves.
     */
    public RewardCommit exchange(int[][] inputs, int[][] resources, int rolledPet, Position position) {
        if(rolledPet!=-1 && family(rolledPet)<0) throw new IllegalArgumentException("Unsupported pet reward");
        if(!ProductionBatch.canExchange(player,inputs,resources)) return null;
        boolean inBackpack=family(rolledPet)>=0 && !owns(rolledPet)
                && (player.hasFollower || player.hasPetSpawned || player.petSummonId>0)
                && ProductionBatch.canExchange(player,inputs,new int[][]{{rolledPet,1}});
        List<int[]> outputs=new ArrayList<>();
        Map<Integer,Integer> ground=new LinkedHashMap<>();
        if(inBackpack) outputs.add(new int[]{rolledPet,1});
        for(int[] item:resources) outputs.add(item.clone());
        while(inBackpack && !ProductionBatch.canExchange(player,inputs,outputs.toArray(int[][]::new))) {
            int[] resource=outputs.stream().filter(item->item[0]!=rolledPet && item[1]>0)
                    .reduce((first,last)->last).orElse(null);
            if(resource==null) return null;
            resource[1]--; ground.merge(resource[0],1,Integer::sum);
            if(resource[1]==0) outputs.remove(resource);
        }
        if(!ProductionBatch.exchange(player,inputs,outputs.toArray(int[][]::new))) return null;
        for(Map.Entry<Integer,Integer> item:ground.entrySet()) Server.itemHandler.createPrivateTimedGroundItem(
                player,new GameItem(item.getKey(),item.getValue()),position,100);
        return new RewardCommit(rolledPet,inBackpack);
    }

    public final class RewardCommit {
        private final int item;
        private final boolean inBackpack;
        private boolean awarded;
        private RewardCommit(int item, boolean inBackpack) { this.item=item; this.inBackpack=inBackpack; }
        public void award() {
            if(awarded) return;
            awarded=true;
            if(family(item)>=0) PetInsurance2022.this.award(item,inBackpack);
        }
    }

    public void award(int item, boolean alreadyInBackpack) {
        int family=family(item);
        if(family<0) throw new IllegalArgumentException("Unsupported pet insurance family");
        insured.putIfAbsent(family,item==13326 ? BABY_CHINCHOMPA : item);
        player.getCollectionLog().handleDrop(player,5,item,1);
        if(alreadyInBackpack) {
            player.sendMessage("You feel something weird sneaking into your backpack."); return;
        }
        if(owns(item)) {
            if(freeReclaims<Integer.MAX_VALUE) freeReclaims++;
            player.sendMessage("You have been given one free pet reclaim with Probita."); return;
        }
        if(!player.hasFollower && !player.hasPetSpawned && player.petSummonId<=0) {
            PetHandler.spawn(player,PetHandler.forItem(item),true,false);
            if(player.petSummonId==item && player.hasPetSpawned) {
                player.sendMessage("You have a funny feeling like you're being followed."); return;
            }
        }
        if(ProductionBatch.exchange(player,new int[0][],new int[][]{{item,1}}))
            player.sendMessage("You feel something weird sneaking into your backpack.");
        else player.sendMessage(name(family)+" has been automatically insured and recorded for recovery.");
    }

    public static boolean nearProbita(Player player, NPC npc) {
        return npc!=null && npc.getNpcId()==5906 && npc.getIndex()>0 && NPCHandler.npcs[npc.getIndex()]==npc
                && !npc.isUnregister() && !npc.isDead() && !npc.isInvisible()
                && npc.getX()==2622 && npc.getY()==3293 && npc.getHeight()==0
                && player.heightLevel==0 && player.getInstance()==null
                && player.getRegionProvider()==RegionProvider.getGlobal()
                && player.distanceToPoint(npc.getX(),npc.getY())<=2
                && player.getTeleportToX()==-1 && player.getTeleportToY()==-1
                && player.getUtilityMagic().canInteract() && !player.getMovementState().isLocked()
                && !player.getLock().cannotInteract(player) && !player.getBankPin().requiresUnlock()
                && !Server.getMultiplayerSessionListener().inAnySession(player);
    }

    public boolean reclaim(int item, NPC npc) {
        if(!nearProbita(player,npc)) return false;
        int family=family(item);
        Integer form=insured.get(family);
        if(form==null) { player.sendMessage("You have not unlocked that pet."); return false; }
        if(owns(item)) { player.sendMessage("You already have that pet with you or in storage."); return false; }
        boolean free=freeReclaims>0;
        if(!ProductionBatch.exchange(player,free?new int[0][]:new int[][]{{995,1000000}},
                new int[][]{{form,1}})) return false;
        if(free) freeReclaims--;
        player.sendMessage("Probita returns "+name(family)+" to you"
                +(free?" using one free reclaim.":" for 1,000,000 coins."));
        return true;
    }

    public static boolean npc(Player player, NPC npc, int option) {
        if(npc==null || npc.getNpcId()!=5906) return false;
        if(!nearProbita(player,npc) || option!=1 && option!=2) return true;
        player.getPA().removeAllWindows();
        PetInsurance2022 service=player.getPetInsurance();
        player.sendMessage("Insured Hunter pets: "+(service.insured.isEmpty()?"none":service.insured.keySet()
                .stream().map(PetInsurance2022::name).collect(Collectors.joining(", ")))
                +". Free reclaims available: "+service.freeReclaims+".");
        List<Integer> available=service.insured.keySet().stream().filter(pet->!service.owns(pet)).collect(Collectors.toList());
        if(available.isEmpty()) return true;
        List<DialogueOption> options=new ArrayList<>();
        for(int pet:available) options.add(new DialogueOption(
                available.size()==1 ? service.freeReclaims>0?"Use one free reclaim":"Pay 1,000,000 coins"
                        : name(pet)+(service.freeReclaims>0?" (free reclaim)":" (1,000,000 coins)"),
                p->{p.getPA().removeAllWindows();p.getPetInsurance().reclaim(pet,npc);}));
        options.add(new DialogueOption("Cancel",p->p.getPA().removeAllWindows()));
        new DialogueBuilder(player).option(available.size()==1 ? "Reclaim "+name(available.get(0))+"?" : "Choose a pet to reclaim",
                options.toArray(new DialogueOption[0])).send();
        return true;
    }
}
