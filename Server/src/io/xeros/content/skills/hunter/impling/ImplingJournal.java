package io.xeros.content.skills.hunter.impling;
import io.xeros.model.entity.player.Player;
/** Character-owned counters, independent of nets and surviving ordinary logout. */
public final class ImplingJournal {
    public static final int ROOT=64800;
    private final Player player;
    private final int[] surface=new int[12],puro=new int[12];
    public ImplingJournal(Player player){this.player=player;}
    public synchronized void record(ImplingData data,boolean inPuro){int[] a=inPuro?puro:surface;int i=data.ordinal();if(a[i]<Integer.MAX_VALUE)a[i]++;}
    public synchronized int count(ImplingData data,boolean inPuro){return (inPuro?puro:surface)[data.ordinal()];}
    public synchronized String encode(boolean inPuro){StringBuilder b=new StringBuilder();for(int n:(inPuro?puro:surface)){if(b.length()>0)b.append(',');b.append(n);}return b.toString();}
    public synchronized void restore(String value,boolean inPuro){int[] a=inPuro?puro:surface;java.util.Arrays.fill(a,0);String[] parts=value.trim().split(",",-1);if(parts.length!=12)return;for(int i=0;i<a.length;i++)try{long n=Long.parseLong(parts[i].trim());a[i]=(int)Math.max(0,Math.min(Integer.MAX_VALUE,n));}catch(NumberFormatException ignored){a[i]=0;}}
    public static boolean net(int id){return id==10010||id==11259;}
    public boolean checkInventory(int id){if(!net(id))return false;if(player.getItems().playerHasItem(id))show();return true;}
    public boolean checkWorn(int id,int action){if(!net(id))return false;if(action==1&&player.playerEquipment[Player.playerWeapon]==id&&player.playerEquipmentN[Player.playerWeapon]>0)show();return true;}
    public void show(){
        if(!player.getUtilityMagic().canInteract()||player.getMovementState().isLocked())return;
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();
        player.getPA().sendFrame126("Impling Tracker",64803);
        player.getPA().sendFrame126("Surface world",64806);
        player.getPA().sendFrame126("Puro-Puro",64807);
        player.getPA().sendFrame126("Catches are saved automatically.",64850);
        for(ImplingData d:ImplingData.values()){int i=d.ordinal();player.getPA().sendFrame126(Integer.toString(count(d,false)),64811+i*3);player.getPA().sendFrame126(d==ImplingData.CRYSTAL?"-":Integer.toString(count(d,true)),64812+i*3);}
        player.getPA().showInterface(ROOT);
    }
}
