package io.xeros.solo;
import java.nio.file.*;import java.nio.charset.*;import java.util.*;import java.lang.reflect.*;import com.google.gson.Gson;
public class Translation71Check {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static String server(String s){return io.xeros.content.locale.PtBr.message(s);}
 static String client(String s){return com.client.locale.PtBr.message(s);}
 @SuppressWarnings("unchecked")public static void main(String[]args)throws Exception{
  Thread.currentThread().setName("GameThread");
  Map<String,String> catalog=new Gson().fromJson(Files.readString(Path.of(args[0])),Map.class);
  int count=0;for(Map.Entry<String,String> e:catalog.entrySet()){ok(server(e.getKey()).equals(e.getValue()),"Server catalog "+e.getKey());ok(client(e.getKey()).equals(e.getValue()),"Client catalog "+e.getKey());ok(!e.getValue().contains("XQZ"),"Opaque token "+e.getKey());ok(client(server(e.getKey())).equals(e.getValue()),"Double translation catalog "+e.getKey()+" => "+client(server(e.getKey())));count++;}
  for(String name:new String[]{"General Graardor","Kree'arra","K'ril Tsutsaroth","Commander Zilyana","Nex","Vorkath","Zulrah","TzTok-Jad","Dagannoth Rex","Cerberus","Tumeken's shadow","Rune platebody","Air rune","Torva full helm","Notes","Research notes","Iron ore","Dragon scimitar"}){ok(server(name).equals(name)&&client(name).equals(name),"Canonical name "+name);ok(com.client.locale.PtBr.action("Attack @yel@"+name).equals("Atacar @yel@"+name),"NPC action "+name);ok(com.client.locale.PtBr.action("Take @lre@"+name).equals("Pegar @lre@"+name),"Item action "+name);}
  for(String s:new String[]{"Congratulations, you just advanced a cooking level!","Please finish what you're doing.","You need more inventory space.","General information","Quest Points","Settings"}){String t=server(s);ok(!t.equals(s),"Common text "+s);ok(client(t).equals(t),"Double translation "+s);}
  com.google.gson.JsonArray rows=new com.google.gson.JsonParser().parse(Files.readString(Path.of(args[1]))).getAsJsonObject().getAsJsonArray("templates");
  int failures=0;int dynamic=0;for(com.google.gson.JsonElement element:rows){com.google.gson.JsonObject row=element.getAsJsonObject();String input=row.get("key").getAsString();com.google.gson.JsonArray flags=row.getAsJsonArray("skill_groups");List<String> values=new ArrayList<>();for(int n=0;n<flags.size();n++){boolean sk=flags.get(n).getAsBoolean();String v=sk?"Magic":"Q71V"+n+"AZ";values.add(sk?"Magia":v);input=input.replace("{"+n+"}",v);}String output=server(input);for(String v:values)if(!output.contains(v)){System.out.println("Lost dynamic field "+row.get("key")+" => "+output);failures++;}ok(!output.contains("{0}"),"Unresolved dynamic field "+output);dynamic++;}
  ok(dynamic>800,"Dynamic template audit count");ok(failures==0,"Dynamic field failures: "+failures);
  for(String numeric:new String[]{"0","11","0/11","50/99","125000","1,234","3.2","22:59"})ok(server(numeric).equals(numeric)&&client(numeric).equals(numeric),"Bare counter rewritten "+numeric);
  String heading=server("<col=CC8400>Regioes concluidas: </col>@gre@0/11");ok(heading.contains("0/11")&&!heading.contains("que é"),"Diary counter translation "+heading);
  ok(com.client.locale.PtBr.action("Inspect @lre@Rune platebody").equals("Inspecionar @lre@Rune platebody"),"General menu action");
  String qty=server("You need 55 Crafting to do this.");ok(qty.contains("55")&&!qty.startsWith("You"),"Dynamic level "+qty);
  String original="Você avançou em Oração! Ação, construção, missão: çãéíóú.";
  io.xeros.util.Stream ss=new io.xeros.util.Stream(new byte[1024]);ss.writeString(original);ok(ss.currentOffset==original.getBytes(Charset.forName("windows-1252")).length+1,"Server wire size");ss.currentOffset=0;ok(ss.readString().equals(original),"Server accent roundtrip");
  com.client.Stream cs=new com.client.Stream(new byte[1024]);cs.writeString(original);ok(cs.currentOffset==original.getBytes(Charset.forName("windows-1252")).length+1,"Client wire size");cs.currentOffset=0;ok(cs.readString().equals(original),"Client accent roundtrip");
  String longText=("Você recebeu Rune platebody e 50 Coins. ").repeat(30);List<String> lines=io.xeros.content.locale.PtBr.chatLines(longText);ok(lines.size()>1,"Long chat split");for(String line:lines)ok(line.length()<=200&&!line.contains("\n"),"Unsafe chat payload");ok(String.join("",lines).replace(" ","").equals(longText.replace(" ","")),"Chat content lost");
  List<String> wrapped=io.xeros.content.locale.PtBr.dialogueLines(new String[]{longText},54);for(String line:wrapped)ok(line.length()<=54,"Dialogue line width");ok(String.join("",wrapped).replace(" ","").equals(longText.replace(" ","")),"Dialogue content lost");
  ok(com.client.locale.PtBr.interfaceMessage(4884,"Smith").equals("Smith"),"NPC header translation");ok(com.client.locale.PtBr.interfaceMessage(970,"Hunter").equals("Hunter"),"Player header translation");
  ok(com.client.locale.PtBr.action("Withdraw-All-but-1 @lre@Air rune").equals("Retirar tudo menos 1 @lre@Air rune"),"Bank quantity action");
  long started=System.nanoTime();for(int i=0;i<10000;i++)client("You need "+(1+i%99)+" Magic level.");ok((System.nanoTime()-started)/1e9<10,"Translation hot loop too slow");
  System.out.println("TRANSLATION71_"+count+"_CATALOG_ENTRIES_BOTH_SIDES_NAMES_ACTIONS_ACCENTS_SAFE_PACKETS_DIALOGUE_WIDTH_AND_IDEMPOTENCE_PASS");
 }
}
