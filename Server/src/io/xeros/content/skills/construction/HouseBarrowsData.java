package io.xeros.content.skills.construction;
import java.util.*;
/** Historical Barrows item identifiers and full NPC repair costs. */
public final class HouseBarrowsData {
 public static final class Piece {
  public final String name;public final int npcCost;public final int[] states;
  Piece(String name,int cost,int[] states){this.name=name;this.npcCost=cost;this.states=states;}
  public int state(int item){for(int i=0;i<states.length;i++)if(states[i]==item)return i;return -1;}
 }
 private static final List<Piece> DATA=new ArrayList<>();
 private static void row(String name,int cost,int[] states){DATA.add(new Piece(name,cost,states));}
 static {
  row("Ahrim's hood",60000,new int[]{4708,4856,4857,4858,4859,4860});
  row("Ahrim's staff",100000,new int[]{4710,4862,4863,4864,4865,4866});
  row("Ahrim's robetop",90000,new int[]{4712,4868,4869,4870,4871,4872});
  row("Ahrim's robeskirt",80000,new int[]{4714,4874,4875,4876,4877,4878});
  row("Dharok's helm",60000,new int[]{4716,4880,4881,4882,4883,4884});
  row("Dharok's greataxe",100000,new int[]{4718,4886,4887,4888,4889,4890});
  row("Dharok's platebody",90000,new int[]{4720,4892,4893,4894,4895,4896});
  row("Dharok's platelegs",80000,new int[]{4722,4898,4899,4900,4901,4902});
  row("Guthan's helm",60000,new int[]{4724,4904,4905,4906,4907,4908});
  row("Guthan's warspear",100000,new int[]{4726,4910,4911,4912,4913,4914});
  row("Guthan's platebody",90000,new int[]{4728,4916,4917,4918,4919,4920});
  row("Guthan's chainskirt",80000,new int[]{4730,4922,4923,4924,4925,4926});
  row("Karil's coif",60000,new int[]{4732,4928,4929,4930,4931,4932});
  row("Karil's crossbow",100000,new int[]{4734,4934,4935,4936,4937,4938});
  row("Karil's leathertop",90000,new int[]{4736,4940,4941,4942,4943,4944});
  row("Karil's leatherskirt",80000,new int[]{4738,4946,4947,4948,4949,4950});
  row("Torag's helm",60000,new int[]{4745,4952,4953,4954,4955,4956});
  row("Torag's hammers",100000,new int[]{4747,4958,4959,4960,4961,4962});
  row("Torag's platebody",90000,new int[]{4749,4964,4965,4966,4967,4968});
  row("Torag's platelegs",80000,new int[]{4751,4970,4971,4972,4973,4974});
  row("Verac's helm",60000,new int[]{4753,4976,4977,4978,4979,4980});
  row("Verac's flail",100000,new int[]{4755,4982,4983,4984,4985,4986});
  row("Verac's brassard",90000,new int[]{4757,4988,4989,4990,4991,4992});
  row("Verac's plateskirt",80000,new int[]{4759,4994,4995,4996,4997,4998});
 }
 public static List<Piece> all(){return Collections.unmodifiableList(DATA);}
 public static Piece get(int item){for(Piece p:DATA)if(p.state(item)>=0)return p;return null;}
 /** Default charge boundary for a spawned visible state; combat residuals need their own ledger. */
 public static int defaultCharges(Piece p,int item){int state=p.state(item);if(state<0)throw new IllegalArgumentException("Barrows item");return state<=1?1000:Math.max(0,1250-state*250);}
 public static int price(Piece piece,int remainingCharges,int smithingLevel){if(remainingCharges<0||remainingCharges>1000)throw new IllegalArgumentException("Barrows charges");int level=Math.max(0,Math.min(200,smithingLevel));long numerator=(long)piece.npcCost*(1000-remainingCharges)*(200-level);return (int)((numerator+199999)/200000);}
 private HouseBarrowsData(){}
}
