package io.xeros.content.skills;
import io.xeros.model.entity.player.Player;
/** Piece bonuses in tenths of a percent, plus the full-set bonus. */
public enum SkillingOutfit {
 ANGLER(new int[]{13258,13259,13260,13261},new int[]{4,8,6,2}),
 PROSPECTOR(new int[]{12013,12014,12015,12016},new int[]{4,8,6,2}),
 PYROMANCER(new int[]{20704,20706,20708,20710},new int[]{8,6,4,2}),
 FARMER(new int[]{13646,13642,13640,13644},new int[]{4,8,6,2});
 private final int[] items, bonuses;
 SkillingOutfit(int[] items,int[] bonuses){this.items=items;this.bonuses=bonuses;}
 public double multiplier(Player player){int total=0,pieces=0;for(int i=0;i<items.length;i++)if(player.getItems().isWearingItem(items[i]) || (this==PROSPECTOR&&player.getItems().isWearingItem(25549+2*i)) || (this==PROSPECTOR&&items[i]==12014&&player.getItems().isWearingItem(13107,Player.playerChest)) || (this == FARMER && player.getItems().isWearingItem(items[i] == 13642 ? 13643 : items[i] + 1))){total+=bonuses[i];pieces++;}return 1d+(total+(pieces==items.length?5:0))/1000d;}
}
