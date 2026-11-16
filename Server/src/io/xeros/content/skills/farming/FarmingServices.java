package io.xeros.content.skills.farming;

import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Personal tool storage shared by the world's Tool leprechauns. */
public final class FarmingServices {
    private final Player player;
    private final Map<Integer,Integer> tools=new TreeMap<>();
    private static final Set<Integer> BASIC=Set.of(5341,952,5343,5325,5329,7409);
    public FarmingServices(Player player){this.player=player;}
    private boolean near(NPC npc){return npc!=null&&!npc.isDead()&&player.getUtilityMagic().canInteract()&&!player.isForceMovementActive()&&npc.heightLevel==player.heightLevel
            &&Math.max(Math.abs(player.absX-npc.absX),Math.abs(player.absY-npc.absY))<=3
            &&!player.getLock().cannotInteract(player)&&!player.getBankPin().requiresUnlock();}
    private static boolean leprechaun(NPC npc){return npc!=null&&(npc.getNpcId()==0||npc.getNpcId()==7757);}
    private static int capacity(int id){if(BASIC.contains(id))return 100;if(id>=5331&&id<=5340)return 1;
        return id==1925||id==6032||id==6034||id==21483||id==6036?1000:0;}
    public int stored(int id){return tools.getOrDefault(id,0);}
    public boolean deposit(NPC npc,int item){
        if(!leprechaun(npc)||!near(npc))return false;
        ItemDef def=ItemDef.forId(item);int id=def.getUnNotedIdIfNoted(),cap=capacity(id);
        if(cap==0)return false;
        if(id>=5331&&id<=5340&&tools.keySet().stream().anyMatch(x->x>=5331&&x<=5340))return false;
        int amount=Math.min(cap-stored(id),player.getItems().getInventoryCount(item));
        if(amount<=0||!ProductionBatch.exchange(player,new int[][]{{item,amount}},new int[0][]))return false;
        tools.put(id,stored(id)+amount);player.sendMessage("Ferramentas guardadas com Tool leprechaun.");return true;
    }
    public boolean withdraw(NPC npc,int id){
        if(!leprechaun(npc)||!near(npc)||stored(id)<=0)return false;
        if(!ProductionBatch.exchange(player,new int[0][],new int[][]{{id,1}}))return false;
        int left=stored(id)-1;if(left==0)tools.remove(id);else tools.put(id,left);return true;
    }
    public boolean note(NPC npc,int id){
        if(!leprechaun(npc)||!near(npc))return false;
        ItemDef def=ItemDef.forId(id);if(def.isNoted()||def.getNotedItemIfAvailable()==id)return false;
        boolean produce=id==6055;
        for(Plants crop:Plants.values())if(crop.harvest==id&&crop.type!=SeedType.TREE)produce=true;
        if(!produce)return false;
        if(id==1967&&npc.absX>3030&&npc.absX<3075&&npc.absY>3280&&npc.absY<3340)return false;
        int count=player.getItems().getInventoryCount(id);
        return count>0&&ProductionBatch.exchange(player,new int[][]{{id,count}},new int[][]{{def.getNotedItemIfAvailable(),count}});
    }
    public boolean use(NPC npc,int id){return leprechaun(npc)&&(deposit(npc,id)||note(npc,id));}
    public boolean npc(NPC npc,int option){
        if(leprechaun(npc)){if(near(npc))menu(npc,0);return true;}
        if(npc!=null&&npc.getNpcId()>=2669&&npc.getNpcId()<=2689){if(near(npc))gardener(npc);return true;}return false;
    }
    private static int[] payment(Plants plant){switch(plant){
        case OAK_TREE:return new int[]{5968,1};case WILLOW_TREE:return new int[]{5386,1};case MAPLE_TREE:return new int[]{5396,1};case YEW_TREE:return new int[]{6016,10};case MAGIC_TREE:return new int[]{5974,25};
        case APPLE_TREE:return new int[]{5986,9};case BANANA_TREE:return new int[]{5386,4};case ORANGE_TREE:return new int[]{5406,3};case CURRY_TREE:return new int[]{5416,5};case PINEAPPLE_TREE:return new int[]{5982,10};case PAPAYA_TREE:return new int[]{2114,10};case PALM_TREE:return new int[]{5972,15};default:return null;}}
    private boolean assigned(NPC npc,Plant crop){return near(npc)&&crop!=null&&crop.isTree()&&player.getFarming().contains(crop)&&Math.max(Math.abs(npc.absX-crop.getPatch().bottomLeft.getX()),Math.abs(npc.absY-crop.getPatch().bottomLeft.getY()))<=12;}
    public boolean protect(NPC npc,Plant crop){
        if(npc==null||npc.getNpcId()<2669||npc.getNpcId()>2689||!assigned(npc,crop)||crop.dead||crop.isDiseased()||crop.paidProtection||crop.stage>=Plants.values()[crop.plant].stages)return false;
        int[] cost=payment(Plants.values()[crop.plant]);if(cost==null)return false;
        int noted=ItemDef.forId(cost[0]).getNotedItemIfAvailable(),plain=Math.min(cost[1],player.getItems().getInventoryCount(cost[0])),remaining=cost[1]-plain;
        if(remaining>0&&(noted==cost[0]||player.getItems().getInventoryCount(noted)<remaining)){player.sendMessage("O pagamento é "+cost[1]+" x "+ItemDef.forId(cost[0]).getName()+" (notas aceitas).");return false;}
        List<int[]> in=new ArrayList<>();if(plain>0)in.add(new int[]{cost[0],plain});if(remaining>0)in.add(new int[]{noted,remaining});
        if(!ProductionBatch.exchange(player,in.toArray(new int[0][]),new int[0][]))return false;
        crop.paidProtection=true;player.sendMessage("Pagamento aceito. Esta árvore ficará protegida durante o crescimento.");return true;
    }
    private void gardener(NPC npc){
        List<DialogueOption> options=new ArrayList<>();for(FarmingPatches patch:FarmingPatches.values()){
            Plant crop=player.getFarming().getPlantedPatch(patch.bottomLeft.getX(),patch.bottomLeft.getY());if(!assigned(npc,crop))continue;
            int[] cost=payment(Plants.values()[crop.plant]);if(cost==null)continue;
            options.add(new DialogueOption(Plants.values()[crop.plant]+" — "+cost[1]+" x "+ItemDef.forId(cost[0]).getName(),p->{p.getPA().removeAllWindows();protect(npc,crop);}));
            if(options.size()==4)break;
        }
        if(options.isEmpty()){player.sendMessage("Plante uma árvore neste local antes de contratar proteção.");return;}
        options.add(new DialogueOption("Cancelar",p->p.getPA().removeAllWindows()));new DialogueBuilder(player).option("Proteger plantação",options.toArray(new DialogueOption[0])).send();
    }
    private void menu(NPC npc,int page){
        if(!near(npc))return;List<Integer> ids=new ArrayList<>(tools.keySet());List<DialogueOption> options=new ArrayList<>();
        page=Math.max(0,Math.min(page,Math.max(0,(ids.size()-1)/2)));final int current=page;
        for(int i=page*2;i<Math.min(ids.size(),page*2+2);i++){int id=ids.get(i);options.add(new DialogueOption(ItemDef.forId(id).getName()+" ("+stored(id)+")",p->{withdraw(npc,id);menu(npc,current);}));}
        if(page>0)options.add(new DialogueOption("Página anterior",p->menu(npc,current-1)));
        if((page+1)*2<ids.size())options.add(new DialogueOption("Próxima página",p->menu(npc,current+1)));
        options.add(new DialogueOption("Fechar",p->p.getPA().removeAllWindows()));
        player.sendMessage("Use uma ferramenta ou colheita em Tool leprechaun para guardar ou transformar em notas.");
        new DialogueBuilder(player).option("Ferramentas guardadas",options.toArray(new DialogueOption[0])).send();
    }
    public String encode(){StringBuilder out=new StringBuilder();tools.forEach((id,n)->out.append(id).append(':').append(n).append(','));return out.toString();}
    public void decode(String raw){Map<Integer,Integer> next=new TreeMap<>();try{for(String part:raw.split(",")){if(part.isEmpty())continue;String[] pair=part.split(":");if(pair.length!=2)return;int id=Integer.parseInt(pair[0]),n=Integer.parseInt(pair[1]);if(n<1||n>capacity(id)||next.put(id,n)!=null)return;}
        if(next.keySet().stream().filter(id->id>=5331&&id<=5340).count()>1)return;tools.clear();tools.putAll(next);
    }catch(NumberFormatException ignored){}}
}
