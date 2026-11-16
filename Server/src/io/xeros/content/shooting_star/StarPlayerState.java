package io.xeros.content.shooting_star;
/** Statistics persist with the character; the live sky resets when the world restarts. */
public final class StarPlayerState {
 private int discoveries;private long dust;
 public int discoveries(){return discoveries;}public long dust(){return dust;}
 public void discover(){if(discoveries<Integer.MAX_VALUE)discoveries++;}
 public void mined(int amount){if(amount>0)dust=dust>Long.MAX_VALUE-amount?Long.MAX_VALUE:dust+amount;}
 public String encode(){return discoveries+","+dust;}
 public boolean decode(String value){try{String[] fields=value.split(",",-1);if(fields.length!=2||!fields[0].matches("[0-9]{1,10}")||!fields[1].matches("[0-9]{1,19}"))return false;int next=Integer.parseInt(fields[0]);long total=Long.parseLong(fields[1]);discoveries=next;dust=total;return true;}catch(RuntimeException e){return false;}}
}
