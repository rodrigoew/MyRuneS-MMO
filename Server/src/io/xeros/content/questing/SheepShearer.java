package io.xeros.content.questing;
import io.xeros.model.entity.player.Player;import io.xeros.model.entity.npc.NPC;import java.util.*;
public final class SheepShearer extends ClassicQuest {
 public SheepShearer(Player p){super(p);}public String getName(){return "Sheep Shearer";}public String getDisplayName(){return "Tosquiador de ovelhas";}public int getCompletionStage(){return 21;}
 public List<String> getCompletedRewardsList(){return Arrays.asList("1 ponto de missao","150 Crafting XP","60 coins");}
 public List<String> getJournalText(int s){return s==0?Arrays.asList("Fale com Fred the Farmer na fazenda ao norte de Lumbridge.","Ele precisa de ajuda com suas ovelhas."):s==21?Arrays.asList("Entreguei as 20 Ball of wool a Fred the Farmer."):Arrays.asList("Fred precisa de 20 Ball of wool, sem notas.","Ja entreguei "+(s-1)+" de 20.","Use Shears nas ovelhas com la do cercado.","Fie a Wool na Spinning wheel do castelo de Lumbridge.","Voce pode entregar as bolas de la em varias viagens.");}
 public void giveQuestCompletionRewards(){exactReward(12,150);}
 public boolean handleNpcClick(NPC n,int option){if(n.getNpcId()!=732||!local(n,3190,3273,0))return false;if(option!=1||!ready()||!nearbyNpc(n))return true;int s=getStage();
 if(s==0)offer(n,0,"Preciso de vinte Ball of wool. Tosquie minhas ovelhas e fie a la em uma roda.","Vou reunir as vinte bolas de la.",()->{step(0,1);if(!owns(1735))replace(1735);say(n.getNpcId(),"Pode trazer a la aos poucos. A roda de fiar fica no primeiro andar do castelo.","Se perder sua Shears, fale comigo novamente.");});
 else if(isQuestCompleted())say(n.getNpcId(),"Obrigado pela ajuda com a la!");
 else {int count=Math.min(21-s,player.getItems().getInventoryCount(1759));if(count==0){if(!owns(1735))offer(n,s,"Voce precisa de Shears para tosquiar as ovelhas.","Pedir uma Shears.",()->replace(1735));else say(n.getNpcId(),"Ainda faltam "+(21-s)+" Ball of wool. As notas nao servem.");}
 else offer(n,s,"Trouxe mais Ball of wool para minha encomenda?","Entregar a la.",()->{int amount=Math.min(21-s,player.getItems().getInventoryCount(1759));if(amount<=0)return;boolean done=s+amount==21;if(!exchange(new int[][]{{1759,amount}},done?new int[][]{{995,60}}:new int[0][]))return;if(done)finish(s,12,150);else{step(s,s+amount);say(n.getNpcId(),"Recebi "+amount+". Ainda faltam "+(21-getStage())+".");}});}return true;}
}
