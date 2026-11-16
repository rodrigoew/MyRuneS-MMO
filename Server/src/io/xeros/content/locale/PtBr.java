package io.xeros.content.locale;
import java.util.*;
import java.util.regex.*;
/** Presentation translations. Canonical names and action values stay in the original language. */
public final class PtBr {
 private static final Map<String,String> M=new HashMap<>();
 static {
  M.put("Store","Depositar");
  M.put("Select","Selecionar");
  M.put("Your daily reward is ready to be claimed.","Sua recompensa diária está disponível.");
  M.put("Daily Task: Daily tasks are not enabled, speak to the task manager to receive them.","Tarefa diária: converse com o responsável pelas tarefas para habilitá-las.");
  M.put("Rearrange mode:","Organizar itens:");
  M.put("Withdraw as:","Retirar como:");
  M.put("Click here to continue.","Clique aqui para continuar.");
  M.put("You are already logged in.","Este personagem já está conectado.");

  M.put("Walk here","Caminhar aqui");
  M.put("Bank","Banco");
  M.put("Bank all","Depositar tudo");
  M.put("Enter","Entrar");
  M.put("Exit","Sair");
  M.put("Climb","Subir");
  M.put("Read","Ler");
  M.put("Check-health","Verificar saúde");
  M.put("Harvest","Colher");
  M.put("Pick","Colher");
  M.put("Chop","Cortar");
  M.put("Mine","Minerar");
  M.put("Fish","Pescar");
  M.put("Light","Acender");
  M.put("Smelt","Fundir");
  M.put("Smith","Forjar");
  M.put("Craft","Fabricar");
  M.put("Follow","Seguir");
  M.put("Trade with","Negociar com");
  M.put("All","Tudo");
  M.put("Game","Jogo");
  M.put("Public","Público");
  M.put("Private","Privado");
  M.put("Clan","Clã");
  M.put("Trade","Trocas");
  M.put("On","Ligado");
  M.put("Off","Desligado");
  M.put("Friends","Amigos");
  M.put("Hide","Ocultar");
  M.put("Search","Buscar");
  M.put("Search bank","Buscar no banco");
  M.put("Deposit inventory","Depositar mochila");
  M.put("Deposit equipment","Depositar equipamento");
  M.put("Withdraw as","Retirar como");
  M.put("Item","Item");
  M.put("Note","Nota");
  M.put("Rearrange mode","Organizar itens");
  M.put("Swap","Trocar");
  M.put("Insert","Inserir");
  M.put("Quantity","Quantidade");
  M.put("Enter amount:","Digite a quantidade:");
  M.put("Enter name:","Digite o nome:");
  M.put("Total level","Nível total");
  M.put("Total XP","XP total");
  M.put("Current XP:","XP atual:");
  M.put("Next level at:","Próximo nível:");
  M.put("Remaining XP:","XP restante:");
  M.put("Your progress has been saved.","Seu progresso foi salvo.");
  M.put("Invalid item or quantity.","Item ou quantidade inválidos.");
  M.put("Usage: ::item ID quantity","Use ::item ID QUANTIDADE");
  M.put("Use ::addxp SKILL XP or ::addxp list. Example: ::addxp 1 50000","Use ::addxp HABILIDADE XP ou ::addxp list. Exemplo: ::addxp 1 50000");
  M.put("Invalid skill or XP. Use ::addxp list; XP must be positive.","Habilidade ou XP inválidos. Use ::addxp list; o XP deve ser positivo.");
  M.put("No XP added: check XP locks or the 200M limit.","Nenhum XP adicionado: confira os bloqueios de XP e o limite de 200 milhões.");
  M.put("Use whole numbers: ::addxp 1 50000","Use números inteiros: ::addxp 1 50000");
  M.put("Use ::solo home first.","Use ::solo home primeiro.");
  M.put("Use ::solo home before entering another instance.","Use ::solo home antes de entrar em outra instância.");
  M.put("You cannot do this while in combat.","Você não pode fazer isso durante o combate.");
  M.put("You are already in combat.","Você já está em combate.");
  M.put("You cannot teleport while in combat.","Você não pode teleportar durante o combate.");
  M.put("You do not have enough coins.","Você não tem coins suficientes.");
  M.put("You don't have enough coins.","Você não tem coins suficientes.");
  M.put("Your inventory is full.","Sua mochila está cheia.");
  M.put("You have no space in your inventory.","Não há espaço na sua mochila.");
  M.put("You do not have enough space in your inventory.","Não há espaço suficiente na sua mochila.");
  M.put("You have finished your task!","Você concluiu sua tarefa!");
  M.put("You manage to catch a fish.","Você pescou um peixe.");
  M.put("You manage to mine some ore.","Você minerou minério.");
  M.put("You get some logs.","Você conseguiu logs.");
  M.put("You need to wait before doing this again.","Aguarde antes de repetir esta ação.");
  M.put("You have been poisoned!","Você foi envenenado!");
  M.put("You have been poisoned.","Você foi envenenado.");
  M.put("You are now on wave ","Você está na onda ");
  M.put("You need to defeat the current room before moving on!","Derrote os inimigos desta sala antes de avançar.");
  M.put("@pur@The room has been cleared and you are free to pass.","@pur@Sala concluída. O caminho está livre.");
  M.put("@pur@Please go up the stairs to get your rewards.","@pur@Suba a escada para receber as recompensas.");
  M.put("@red@Please destroy both hands before attacking The Great Olm.","@red@Destrua as duas mãos antes de atacar Great Olm.");
  M.put("You do not have an infernal cape.","Você não tem uma Infernal cape.");
  M.put("You must select an option first before confirming.","Selecione uma opção antes de confirmar.");
  M.put("You cannot leave yet, wait a couple of seconds and try again.","Aguarde alguns segundos antes de sair.");
  M.put("You need to complete the raid!","Conclua a raid primeiro!");
  M.put("Choose a skill","Escolha uma habilidade");
  M.put("Choose an option:","Escolha uma opção:");
  M.put("Click to continue","Clique para continuar");
  M.put("Loading - please wait.","Carregando, aguarde.");
  M.put("Loading - please wait...","Carregando, aguarde...");
  M.put("Connection lost","Conexão perdida");
  M.put("Please wait - attempting to reestablish","Aguarde: tentando reconectar");
  M.put("Report abuse","Denunciar abuso");
  M.put("Accept","Aceitar");
  M.put("Decline","Recusar");
  M.put("Confirm","Confirmar");
  M.put("Deposit-All","Depositar tudo");
  M.put("Withdraw-All","Retirar tudo");

  M.put("Attack","Ataque");
  M.put("Defence","Defesa");
  M.put("Strength","Força");
  M.put("Hitpoints","Vitalidade");
  M.put("Ranged","Combate à distância");
  M.put("Prayer","Oração");
  M.put("Magic","Magia");
  M.put("Cooking","Culinária");
  M.put("Woodcutting","Corte de lenha");
  M.put("Fletching","Arco e flecha");
  M.put("Fishing","Pesca");
  M.put("Firemaking","Arte do fogo");
  M.put("Crafting","Artesanato");
  M.put("Smithing","Metalurgia");
  M.put("Mining","Mineração");
  M.put("Herblore","Herbologia");
  M.put("Agility","Agilidade");
  M.put("Thieving","Roubo");
  M.put("Slayer","Extermínio");
  M.put("Farming","Agricultura");
  M.put("Runecrafting","Criação de runas");
  M.put("Hunter","Caça");
  M.put("Construction","Construção");
  M.put("Choose Option","Escolha uma opção");
  M.put("Choose an option","Escolha uma opção");
  M.put("Close","Fechar");
  M.put("Cancel","Cancelar");
  M.put("Continue","Continuar");
  M.put("Click here to continue","Clique aqui para continuar");
  M.put("Click here to continue.","Clique aqui para continuar.");
  M.put("Please wait...","Aguarde...");
  M.put("Yes","Sim");
  M.put("No","Não");
  M.put("Back","Voltar");
  M.put("More","Mais");
  M.put("Next","Próximo");
  M.put("Previous","Anterior");
  M.put("Leave","Sair");
  M.put("Remove","Remover");
  M.put("Keep","Manter");
  M.put("Buy","Comprar");
  M.put("Sell","Vender");
  M.put("Settings","Configurações");
  M.put("Advanced Settings","Configurações avançadas");
  M.put("Display","Tela");
  M.put("Audio","Áudio");
  M.put("Controls","Controles");
  M.put("Chat","Conversa");
  M.put("Logout","Sair do jogo");
  M.put("Log out","Sair do jogo");
  M.put("Click here to logout","Clique aqui para sair");
  M.put("Inventory","Mochila");
  M.put("Equipment","Equipamento");
  M.put("Skills","Habilidades");
  M.put("Quests","Missões");
  M.put("Quest List","Lista de missões");
  M.put("Combat Options","Opções de combate");
  M.put("Friends List","Lista de amigos");
  M.put("Ignore List","Lista de ignorados");
  M.put("Music Player","Músicas");
  M.put("Emotes","Gestos");
  M.put("Total level:","Nível total:");
  M.put("Combat level:","Nível de combate:");
  M.put("Your house","Sua casa");
  M.put("Enter normally","Entrar normalmente");
  M.put("Building mode","Modo de construção");
  M.put("Leave house","Sair da casa");
  M.put("Buy house","Comprar casa");
  M.put("Buy a house in Rimmington for 1,000 coins?","Comprar uma casa em Rimmington por 1.000 moedas?");
  M.put("Your house is ready: a garden, exit portal and parlour.","Sua casa está pronta: jardim, portal de saída e sala.");
  M.put("You need 1,000 coins to buy a house.","Você precisa de 1.000 moedas para comprar uma casa.");
  M.put("Keep at least one exit portal.","Mantenha pelo menos um portal de saída.");
  M.put("Removing this room would disconnect your house.","Remover este cômodo deixaria partes da casa sem acesso.");
  M.put("You need a hammer and saw.","Você precisa de hammer e saw.");
  M.put("Add room","Adicionar cômodo");
  M.put("Build furniture","Construir móvel");
  M.put("Construction stopped. Check your level, tools and materials.","Construção interrompida. Confira nível, ferramentas e materiais.");
  M.put("You received some XP for completing the track!","Você recebeu XP por completar o percurso!");
  M.put("You must complete the full course to gain experience.","Complete todo o percurso para receber o XP da volta.");
  M.put("You need at least one free slot to steal from this.","Você precisa de um espaço livre na mochila para furtar aqui.");
  M.put("You need at least one free slot to steal from this npc.","Você precisa de um espaço livre na mochila para furtar este NPC.");
  M.put("You fail to pick the pocket and are stunned.","O furto falhou e você ficou atordoado.");
  M.put("You were unable to find anything useful.","Você não encontrou nada de útil.");
  M.put("You must have a task to cancel first.","Você precisa ter uma tarefa para cancelá-la.");
  M.put("You must have a task to block first.","Você precisa ter uma tarefa para bloqueá-la.");
  M.put("You don't have any open slots left to block tasks.","Não há espaços livres para bloquear tarefas.");
  M.put("Your bracelet shines.","Seu bracelete brilhou.");
  M.put("Your bracelet has ran out of charges.","Seu bracelete ficou sem cargas.");
  M.put("Your plants are diseased!","Suas plantas estão doentes!");
  M.put("Oh dear, your plants have died!","Suas plantas morreram!");
  M.put("Your plants are healthy and ready to harvest.","Suas plantas estão saudáveis e prontas para a colheita.");
  M.put("Your plants have already been watered.","Suas plantas já foram regadas.");
  M.put("This patch doesn't need watering.","Este canteiro não precisa ser regado.");
  M.put("You check the health of your tree. It is healthy.","Você conferiu a saúde da árvore. Ela está saudável.");
  M.put("You need a spade to harvest this patch.","Você precisa de spade para colher neste canteiro.");
  M.put("A crop you planted has finished growing.","Uma das suas plantações terminou de crescer.");
  M.put("This patch already has this compost or better.","Este canteiro já tem este adubo ou um melhor.");
  M.put("You treat the patch with compost.","Você adubou o canteiro.");
  M.put("Use the pot on an empty, weed-free patch.","Use o vaso em um canteiro vazio e sem ervas daninhas.");
  M.put("You need a gardening trowel to fill plant pots.","Você precisa de gardening trowel para encher os vasos.");
  M.put("You don't have the required runes to cast this spell.","Você não tem as runas necessárias para lançar esta magia.");
  M.put("You don't have enough required runes to cast this spell!","Você não tem as runas necessárias para lançar esta magia!");
  M.put("You can only cast Superheat Item on unnoted ore.","Use Superheat Item em minério que não esteja em notas.");
  M.put("This player is currently not accepting aid.","Este jogador não está aceitando ajuda.");
  M.put("You can only cast vengeance every 30 seconds.","Aguarde 30 segundos entre lançamentos de Vengeance.");
  M.put("This player already have vengeance activated.","Este jogador já tem Vengeance ativo.");
  M.put("You cast vengeance.","Você lançou Vengeance.");
  M.put("This player is currently not poisoned.","Este jogador não está envenenado.");
  M.put("Your hitpoints are too low to do this!","Sua vitalidade está baixa demais para fazer isso!");
  M.put("This player already have full hitpoints.","Este jogador já está com a vitalidade completa.");
  M.put("This player already have full special energy.","Este jogador já está com a energia especial completa.");
  M.put("You do not have enough special energy to transfer.","Você não tem energia especial suficiente para transferir.");
  M.put("Your special energy has been restored by 50%!","Sua energia especial foi restaurada em 50%!");
  M.put("You already have full hitpoints","Sua vitalidade já está completa.");
  M.put("You wake up from your dream..","Você acordou do sonho.");
  M.put("The sleeping has an effect on your health","O sono está recuperando sua vitalidade.");
  M.put("Not enough space in your inventory.","Não há espaço suficiente na sua mochila.");
  M.put("You need more inventory space.","Você precisa de mais espaço na mochila.");
  M.put("Nothing interesting happens.","Nada de interessante acontece.");
  M.put("You cannot do this right now.","Você não pode fazer isso agora.");
  M.put("Please finish what you're doing.","Conclua a ação atual primeiro.");
  M.put("You are already doing this.","Você já está fazendo isso.");
  M.put("You can't reach that.","Você não consegue alcançar esse local.");
  M.put("You are too far away.","Você está longe demais.");
  M.put("You have already used your skillcape's stamina boost today.","Você já usou a recuperação de energia da capa hoje.");
  M.put("Your cape boosts your Agility.","Sua capa aumenta sua Agilidade.");
  M.put("Your cape restores your run energy and gives you a stamina boost.","Sua capa restaura sua energia de corrida e concede vigor.");
  M.put("You drink the lumpy potion.","Você bebe a poção espessa.");
  M.put("You do not have enough coins.","Você não tem moedas suficientes.");
  M.put("You do not have enough runes to cast this spell.","Você não tem runas suficientes para lançar esta magia.");
  M.put("Your inventory is full.","Sua mochila está cheia.");
  M.put("You need to be at a bank to do this.","Você precisa estar em um banco para fazer isso.");
  M.put("Walk here","Caminhar aqui");
  M.put("Take","Pegar");
  M.put("Use","Usar");
  M.put("Examine","Examinar");
  M.put("Drop","Largar");
  M.put("Eat","Comer");
  M.put("Drink","Beber");
  M.put("Wield","Empunhar");
  M.put("Wear","Vestir");
  M.put("Talk-to","Conversar");
  M.put("Pickpocket","Furtar");
  M.put("Knock-out","Nocautear");
  M.put("Open","Abrir");
  M.put("Search","Procurar");
  M.put("Climb-up","Subir");
  M.put("Climb-down","Descer");
  M.put("Chop down","Cortar");
  M.put("Deposit","Depositar");
  M.put("Withdraw","Retirar");
  M.put("Please enter your password.","Digite sua senha.");
  M.put("Connecting to server..","Conectando ao jogo local...");
  M.put("Your username must be at least 3 characters long","O nome deve ter pelo menos 3 caracteres.");
  M.put("Invalid username or password.","Nome ou senha incorretos.");
  M.put("Your account is already logged in.","Este personagem já está conectado.");
  M.put("Unable to connect.","Não foi possível conectar. Abra o jogo pelo iniciador.");
  M.put("Error connecting to server, please try again.","Não foi possível conectar ao servidor local.");
  M.put("Invalid username whitespace usage. Please try again.","Remova espaços extras do nome do personagem.");
  M.put("Too many connection attempts, please wait before trying again.","Aguarde um pouco antes de tentar entrar novamente.");
  M.put("This world is full.","O mundo está cheio.");
  M.put("Your username is too long.","O nome do personagem é longo demais.");
  M.put("Login limit exceeded.","Limite de conexões atingido.");
  M.put("Your account has been disabled.","Este personagem está desativado.");
 }
 private static final Set<String> LOCALIZED=new HashSet<>();
 static {Locale71Catalog.fill(M);for(Map.Entry<String,String> e:M.entrySet())if(!e.getKey().equals(e.getValue())&&!M.containsKey(e.getValue()))LOCALIZED.add(e.getValue());}
 private PtBr(){}
 public static String message(String text){if(text==null)return null;return legacyMessage(text.replace("MyRuneS MMO","OSRS Solo")).replace("OSRS Solo","MyRuneS MMO").replace("OSRS SOLO","MyRuneS MMO");}
 private static String legacyMessage(String text){
  if(LOCALIZED.contains(text))return text;
  if(text==null)return null;
  String exact=M.get(text);if(exact!=null)return exact;
  String prefix="",suffix="",core=text;
  while(core.startsWith("@")&&core.length()>=5&&core.charAt(4)=='@'){prefix+=core.substring(0,5);core=core.substring(5);}
  while(core.startsWith("<col=")||core.startsWith("<shad=")||core.startsWith("<img=")){int end=core.indexOf('>');if(end<0)break;prefix+=core.substring(0,end+1);core=core.substring(end+1);}
  while(core.endsWith("</col>")||core.endsWith("</shad>")){int begin=core.lastIndexOf("</");suffix=core.substring(begin)+suffix;core=core.substring(0,begin);}
  while(core.length()>=5&&core.charAt(core.length()-5)=='@'&&core.endsWith("@")){suffix=core.substring(core.length()-5)+suffix;core=core.substring(0,core.length()-5);}
  exact=M.get(core);if(exact!=null)return prefix+exact+suffix;
  String translated=sentence(core);
  if(translated.equals(core)){
   String plain=core.replaceAll("@[a-zA-Z0-9]{3}@|</?(?:col|shad|img)[^>]*>", "");
   if(!plain.equals(core)){String candidate=M.get(plain);if(candidate==null)candidate=sentence(plain);if(!candidate.equals(plain))translated=candidate;}
  }
  if(translated.equals(core))translated=Locale71Templates.translate(core);
  if(translated.equals(core)){String full=Locale71Templates.translate(text);if(!full.equals(text))return full;}
  return prefix+translated+suffix;
 }
 private static String sentence(String s){
  Matcher congratulations=Pattern.compile("Congratulations[,!] (?:you(?:'ve| have)? )?just advanced (?:an? )?(.+?) level[!.]",Pattern.CASE_INSENSITIVE).matcher(s);if(congratulations.matches())return "Parabéns! Você avançou um nível em "+skill(congratulations.group(1))+"!";

  Matcher label=Pattern.compile("^([A-Za-z][A-Za-z ]{0,32}): ?(.+)$").matcher(s);if(label.matches()){String title=M.get(label.group(1));if(title!=null)return title+": "+label.group(2);}
  Matcher shortLabel=Pattern.compile("^(Wave|Waves|Level|Points|Score|Kills|Deaths) ([0-9.,/ +:-]+)$").matcher(s);if(shortLabel.matches())return M.getOrDefault(shortLabel.group(1),shortLabel.group(1))+" "+shortLabel.group(2);

  Matcher m=Pattern.compile("You need (?:an? |to have a )?(?:agility|Agility) level of (?:at least )?([0-9]+) to do this\\.").matcher(s);if(m.matches())return "Você precisa de nível "+m.group(1)+" em Agilidade para fazer isso.";
  m=Pattern.compile("You need a thieving level of ([0-9]+) to steal from this(?: npc)?\\.").matcher(s);if(m.matches())return "Você precisa de nível "+m.group(1)+" em Roubo para furtar aqui.";
  m=Pattern.compile("You need (?:to have a magic|Magic) level (?:of )?([0-9]+) to cast this (?:spell|enchantment)\\.").matcher(s);if(m.matches())return "Você precisa de nível "+m.group(1)+" em Magia para lançar esta magia.";
  m=Pattern.compile("You need (.+) level ([0-9]+)\\.").matcher(s);if(m.matches())return "Você precisa de nível "+m.group(2)+" em "+M.getOrDefault(m.group(1),m.group(1))+".";
  m=Pattern.compile("You steal a (.+) from the stall\\.").matcher(s);if(m.matches())return "Você furtou "+m.group(1)+" da banca.";
  m=Pattern.compile("You build the (.+)\\.").matcher(s);if(m.matches())return "Você construiu "+m.group(1)+".";
  m=Pattern.compile("You have completed ([0-9]+) in a row!").matcher(s);if(m.matches())return "Você concluiu "+m.group(1)+" tarefas seguidas!";
  if(s.startsWith("You need a ")&&s.endsWith(" to cast this spell."))return "Você precisa de "+s.substring(11,s.length()-20)+" para lançar esta magia.";
  m=Pattern.compile("You need ([0-9]+) ([A-Za-z]+)(?: level)? to (.+)\\.").matcher(s);if(m.matches()&&!skill(m.group(2)).equals(m.group(2)))return "Você precisa de nível "+m.group(1)+" em "+skill(m.group(2))+" para realizar esta ação.";
  m=Pattern.compile("Added ([0-9]+) XP to (.+)\\. Total: ([0-9]+)\\.").matcher(s);if(m.matches())return "Adicionados "+m.group(1)+" XP em "+skill(m.group(2))+". Total: "+m.group(3)+".";
  m=Pattern.compile("You (?:need|require) (?:an? )?(.+?) level of (?:at least )?([0-9]+) to (.+)\\.").matcher(s);if(m.matches())return "Você precisa de nível "+m.group(2)+" em "+skill(m.group(1))+" para esta ação.";
  m=Pattern.compile("Your (.+?) level is (?:now )?([0-9]+)\\.").matcher(s);if(m.matches())return "Seu nível em "+skill(m.group(1))+" agora é "+m.group(2)+".";
  m=Pattern.compile("You (?:manage to |successfully )?(?:catch|caught) (?:an? |some )?(.+?)\\.").matcher(s);if(m.matches())return "Você pescou "+m.group(1)+".";
  m=Pattern.compile("You (?:manage to )?cook (?:an? |some )?(.+?)\\.").matcher(s);if(m.matches())return "Você cozinhou "+m.group(1)+".";
  m=Pattern.compile("You (?:accidentally )?burn (?:the |your |some )?(.+?)\\.").matcher(s);if(m.matches())return "Você queimou "+m.group(1)+".";
  m=Pattern.compile("You (?:get|receive) (?:an? |some )?(.+?)\\.").matcher(s);if(m.matches())return "Você recebeu "+m.group(1)+".";
  m=Pattern.compile("You have ([0-9,]+) (?:pest control|pc) points\\.",Pattern.CASE_INSENSITIVE).matcher(s);if(m.matches())return "Você tem "+m.group(1)+" pontos de Pest Control.";
  m=Pattern.compile("You are now on wave ([0-9]+) of ([0-9]+)\\.").matcher(s);if(m.matches())return "Onda "+m.group(1)+"/"+m.group(2)+".";
  m=Pattern.compile("There are ([0-9]+) enemies remaining\\.").matcher(s);if(m.matches())return "Inimigos restantes: "+m.group(1)+".";
  m=Pattern.compile("You have completed ([0-9]+) raids\\.").matcher(s);if(m.matches())return "Raids concluídas: "+m.group(1)+".";
  m=Pattern.compile("Current [Ee]xperience: ?(.+)").matcher(s);if(m.matches())return "XP atual: "+m.group(1);
  m=Pattern.compile("Next level (?:at|in): ?(.+)").matcher(s);if(m.matches())return "Próximo nível: "+m.group(1);
  if(s.startsWith("Welcome to OSRS Solo,"))return "Bem-vindo ao OSRS Solo. Seu mundo local está pronto.";
  m=Pattern.compile("(.*) has just logged in!(?: Welcome!)?").matcher(s);if(m.matches())return m.group(1)+" entrou no jogo!";
  m=Pattern.compile("The Bank of (.+)").matcher(s);if(m.matches())return "Banco de "+m.group(1);
  m=Pattern.compile("Spawned ([0-9]+)x (.+)\\.").matcher(s);if(m.matches())return "Adicionados "+m.group(1)+"x "+m.group(2)+".";
  m=Pattern.compile("You have set the skill id '(.*)' to '(.*)' to level ([0-9]+)\\.").matcher(s);if(m.matches())return "Habilidades "+m.group(1)+" a "+m.group(2)+" ajustadas para o nível "+m.group(3)+".";
  m=Pattern.compile("(.+) x([0-9,]+) has been added to your bank\\.").matcher(s);if(m.matches())return m.group(1)+" x"+m.group(2)+" foi enviado ao seu banco.";
  m=Pattern.compile("Congratulations! You have completed an achievement\\. You now have ([0-9]+) points\\.").matcher(s);if(m.matches())return "Conquista concluída! Você agora tem "+m.group(1)+" pontos.";
  if(s.equals("You don't have an account pin, it's highly recommended you set one with ::pin."))return "Você pode definir um PIN para o banco com ::pin.";
  m=Pattern.compile("Congratulations, you just advanced (?:an? )?(.+) level!").matcher(s);if(m.matches())return "Parabéns! Você avançou um nível em "+skill(m.group(1))+"!";
  m=Pattern.compile("([0-9]+) = (.+)").matcher(s);if(m.matches())return m.group(1)+" = "+skill(m.group(2));
  return s;
 }
 static String skill(String text){for(String name:new String[]{"Attack","Defence","Strength","Hitpoints","Ranged","Prayer","Magic","Cooking","Woodcutting","Fletching","Fishing","Firemaking","Crafting","Smithing","Mining","Herblore","Agility","Thieving","Slayer","Farming","Runecrafting","Hunter","Construction"})if(name.equalsIgnoreCase(text))return M.get(name);return text;}
 private static final Map<String,String> VERBS=new LinkedHashMap<>();
 static {
  String[][] verbs={{"Trade with","Negociar com"},{"Check-health","Verificar estado"},{"Climb-down","Descer"},{"Climb-up","Subir"},{"Chop down","Cortar"},{"Walk here","Caminhar aqui"},{"Talk-to","Conversar"},{"Pickpocket","Furtar"},{"Knock-out","Nocautear"},{"Attack","Atacar"},{"Store","Depositar"},{"Bank","Usar banco"},{"Enter","Entrar"},{"Exit","Sair"},{"Climb","Subir/descer"},{"Read","Ler"},{"Harvest","Colher"},{"Pick","Pegar"},{"Chop","Cortar"},{"Mine","Minerar"},{"Fish","Pescar"},{"Light","Acender"},{"Smelt","Fundir"},{"Smith","Forjar"},{"Craft","Fabricar"},{"Follow","Seguir"},{"Take","Pegar"},{"Use","Usar"},{"Examine","Examinar"},{"Drop","Largar"},{"Eat","Comer"},{"Drink","Beber"},{"Wield","Empunhar"},{"Wear","Vestir"},{"Open","Abrir"},{"Search","Pesquisar"},{"Deposit","Depositar"},{"Withdraw","Retirar"},{"Remove","Remover"},{"Buy","Comprar"},{"Sell","Vender"},{"Cook","Cozinhar"},{"Cast","Lançar"},{"Fill","Encher"},{"Empty","Esvaziar"},{"Shear","Tosquiar"},{"Milk","Ordenhar"},{"Bury","Enterrar"},{"Build","Construir"},{"Mix","Misturar"},{"Clean","Limpar"},{"Check","Verificar"},{"Collect","Coletar"},{"Repair","Reparar"},{"Charge","Carregar"},{"Uncharge","Remover cargas"},{"Dismantle","Desmontar"},{"Claim","Resgatar"}};
  for(String[] v:verbs)VERBS.put(v[0],v[1]);
 }
 public static String action(String text){
  if(text==null)return null;
  Matcher bank=Pattern.compile("^(Withdraw|Deposit|Buy|Sell)-(All-but-1|All|[0-9]+|X)(.*)$").matcher(text);
  if(bank.matches())return VERBS.get(bank.group(1))+" "+(bank.group(2).equals("All")?"tudo":bank.group(2).equals("All-but-1")?"tudo menos 1":bank.group(2))+bank.group(3);

  if(text.startsWith("Cast ")){String tail=text.substring(5);int on=tail.indexOf(" on ");if(on>=0)return "Lançar "+message(tail.substring(0,on))+" em "+tail.substring(on+4);return "Lançar "+message(tail);}
  if(text.startsWith("Walk here"))return "Caminhar aqui"+text.substring(9);
  // Entity names and the original action values remain untouched.
  for(Map.Entry<String,String> v:VERBS.entrySet()){
   if(text.equals(v.getKey()))return v.getValue();
   if(text.startsWith(v.getKey()+" "))return v.getValue()+text.substring(v.getKey().length());
  }
  int entity=text.indexOf(" @");int html=text.indexOf(" <col=");if(entity<0||html>=0&&html<entity)entity=html;
  if(entity>0){String verb=text.substring(0,entity),shown=message(verb);if(!shown.equals(verb))return shown+text.substring(entity);}
  return message(text);
 }
 public static List<String> dialogueLines(String[] texts,int width){
  List<String> out=new ArrayList<>();
  for(String raw:texts){String translated=message(raw);if(translated==null)continue;for(String paragraph:translated.replace("\\n","\n").split("\n",-1)){String line="";for(String word:paragraph.split(" +")){if(!line.isEmpty()&&plainLength(line)+plainLength(word)+1>width){out.add(line);line="";}line+=(line.isEmpty()?"":" ")+word;}out.add(line);}}
  if(out.isEmpty())out.add("");return out;
 }
 private static int plainLength(String text){return text.replaceAll("@[a-zA-Z0-9]{3}@|<[^>]*>","").length();}
 public static List<String> chatLines(String text){
  if(text==null)return Collections.emptyList();List<String> out=new ArrayList<>();
  for(String paragraph:text.replace("\\n","\n").replace("\r","").split("\n",-1)){if(paragraph.isEmpty()){out.add("");continue;}while(paragraph.length()>90){int split=paragraph.lastIndexOf(' ',90);if(split<1)split=90;out.add(paragraph.substring(0,split));paragraph=paragraph.substring(split).stripLeading();}out.add(paragraph);}
  return out;
 }
}
