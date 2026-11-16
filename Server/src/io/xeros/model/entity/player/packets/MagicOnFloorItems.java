package io.xeros.model.entity.player.packets;
import io.xeros.Server;
import io.xeros.model.entity.player.*;
import io.xeros.model.multiplayersession.*;
import io.xeros.model.multiplayersession.duel.DuelSession;
/** Native spell-on-ground-item packet; validation and costs belong to the cast. */
public final class MagicOnFloorItems implements PacketType {
 public void processPacket(Player p,int packetType,int packetSize){
  if(packetSize!=8||p.getMovementState().isLocked()||p.getLock().cannotInteract(p))return;
  int y=p.getInStream().readSignedWordBigEndian(),id=p.getInStream().readUnsignedWord(),x=p.getInStream().readSignedWordBigEndian(),spell=p.getInStream().readUnsignedWordA();
  DuelSession duel=(DuelSession)Server.getMultiplayerSessionListener().getMultiplayerSession(p,MultiplayerSessionType.DUEL);
  if(duel!=null&&duel.getStage().getStage()>MultiplayerSessionStage.REQUEST&&duel.getStage().getStage()<MultiplayerSessionStage.FURTHER_INTERATION){p.sendMessage("Your actions have declined the duel.");duel.getOther(p).sendMessage("The challenger has declined the duel.");duel.finish(MultiplayerSessionFinalizeType.WITHDRAW_ITEMS);return;}
  p.getTelekineticGrab().cast(spell,id,x,y);
 }
}
