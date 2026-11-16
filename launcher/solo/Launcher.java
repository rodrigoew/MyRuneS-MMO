package solo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.zip.*;
public final class Launcher {
    final Path root,java,serverDir;
    final JFrame frame=new JFrame("MyRuneS MMO — Central de jogo");
    final JLabel status=new JLabel("Pronto para jogar.");
    final JButton play=new JButton("Jogar"),stop=new JButton("Salvar e fechar"),backup=new JButton("Criar backup");
    final JComboBox<String> xp=new JComboBox<>(new String[]{"1","2","5","10","25","50","100","500","1000"});
    final ExecutorService worker=Executors.newSingleThreadExecutor();
    Process server,client;
    volatile boolean busy;
    boolean closing;
    boolean autoStart=true;
    Launcher(Path root){this.root=root;java=root.resolve("runtime/jre/bin/java.exe");serverDir=root.resolve("runtime/server");}
    static void check(Path root)throws Exception {
        for(String p:new String[]{"runtime/jre/bin/java.exe","runtime/client/lib/Client.jar","runtime/server/lib/runerogue-server.jar","runtime/server/etc/cfg/item/alchemy_2022.json","runtime/server/etc/cfg/item/impling_2022.json","runtime/server/etc/cfg/npc/impling_world_2022.json","runtime/server/etc/cfg/npc/spawns/elnock_inquisitor.json","runtime/server/etc/cfg/npc/spawns/falconry.json","runtime/server/etc/cfg/npc/spawns/net_trap_hunting.json","runtime/server/etc/cfg/npc/spawns/pitfall_hunting.json","runtime/server/etc/cfg/npc/spawns/probita.json","runtime/server/etc/cfg/item/item_definitions.yaml","runtime/userdata/.pndascape_v1/main_file_cache.dat","patches/classes/solo/Launcher.class"}) {
            if(!Files.isRegularFile(root.resolve(p))) throw new IOException("Falta um arquivo do pacote: "+p+". Extraia a pasta inteira.");
        }
        for(String p:new String[]{"logs","backups","runtime/server/save_files","runtime/server/etc/mapdata/custom_maps"})Files.createDirectories(root.resolve(p));
    }
    static final Color BG=new Color(12,22,29),CARD=new Color(20,36,43),GOLD=new Color(225,195,137),TEXT=new Color(223,233,232),MUTED=new Color(145,169,172);
    static JLabel label(String text,int size,Color color){JLabel l=new JLabel(text);l.setForeground(color);l.setFont(new Font("Segoe UI",size>=24?Font.BOLD:Font.PLAIN,size));return l;}
    static void style(JButton b,boolean primary){b.setUI(new javax.swing.plaf.basic.BasicButtonUI());b.setBackground(primary?GOLD:new Color(31,51,59));b.setForeground(primary?BG:TEXT);b.setFont(new Font("Segoe UI",Font.BOLD,14));b.setFocusPainted(false);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary?GOLD:new Color(53,78,84)),new EmptyBorder(12,17,12,17)));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
    void ui() throws Exception {
        Path settings=root.resolve("solo.properties");if(Files.exists(settings)){Properties p=new Properties();try(InputStream in=Files.newInputStream(settings)){p.load(in);}xp.setSelectedItem(p.getProperty("xp","5"));}else xp.setSelectedItem("5");
        xp.setEditable(true);xp.setFont(new Font("Segoe UI",Font.PLAIN,15));xp.setBackground(CARD);xp.setPreferredSize(new Dimension(110,38));
        JPanel panel=new JPanel(new BorderLayout(0,24));panel.setBackground(BG);panel.setBorder(new EmptyBorder(28,32,24,32));
        JPanel head=new JPanel(new BorderLayout(18,0));head.setOpaque(false);
        Path icon=root.resolve("assets/myrunes.png");if(Files.exists(icon)){Image image=new ImageIcon(icon.toString()).getImage();frame.setIconImage(image);head.add(new JLabel(new ImageIcon(image.getScaledInstance(76,76,Image.SCALE_SMOOTH))),BorderLayout.WEST);}
        JPanel brand=new JPanel(new GridLayout(0,1,0,3));brand.setOpaque(false);brand.add(label("MyRuneS MMO",32,GOLD));brand.add(label("OLD SCHOOL · SEU MUNDO PARTICULAR",11,MUTED));head.add(brand,BorderLayout.CENTER);panel.add(head,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(25,0));body.setOpaque(false);
        JPanel adventure=new JPanel();adventure.setLayout(new BoxLayout(adventure,BoxLayout.Y_AXIS));adventure.setBackground(CARD);adventure.setBorder(new EmptyBorder(25,25,25,25));
        adventure.add(label("Gielinor, do seu jeito.",25,TEXT));adventure.add(Box.createVerticalStrut(10));adventure.add(label("Explore, evolua e enfrente seus próximos bosses.",14,MUTED));adventure.add(Box.createVerticalStrut(27));adventure.add(label("Ritmo de evolução",13,GOLD));adventure.add(Box.createVerticalStrut(7));
        JPanel rate=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));rate.setOpaque(false);rate.add(xp);rate.add(label("   × XP por ação",14,TEXT));rate.setAlignmentX(Component.LEFT_ALIGNMENT);adventure.add(rate);adventure.add(Box.createVerticalStrut(7));adventure.add(label("De 1× a 1.000×. Alterações valem na próxima abertura.",12,MUTED));adventure.add(Box.createVerticalStrut(22));
        JPanel mainActions=new JPanel(new GridLayout(1,2,10,0));mainActions.setOpaque(false);style(play,true);style(stop,false);mainActions.add(play);mainActions.add(stop);mainActions.setAlignmentX(Component.LEFT_ALIGNMENT);adventure.add(mainActions);
        body.add(adventure,BorderLayout.CENTER);
        JPanel tools=new JPanel(new GridLayout(0,1,0,10));tools.setOpaque(false);JButton guide=new JButton("Comandos e cheats"),items=new JButton("Itens e números"),saves=new JButton("Meus personagens");for(JButton button:new JButton[]{guide,items,saves,backup}){style(button,false);tools.add(button);}body.add(tools,BorderLayout.EAST);panel.add(body,BorderLayout.CENTER);
        JPanel footer=new JPanel(new GridLayout(0,1,0,6));footer.setOpaque(false);status.setFont(new Font("Segoe UI",Font.PLAIN,13));status.setForeground(GOLD);footer.add(status);footer.add(label("O mundo abre no seu computador. Seu progresso é salvo ao sair.",12,MUTED));panel.add(footer,BorderLayout.SOUTH);
        xp.addActionListener(e->{if(busy)return;try{double value=Double.parseDouble(String.valueOf(xp.getSelectedItem()));if(!Double.isFinite(value)||value<1||value>1000)throw new NumberFormatException();Files.writeString(root.resolve("solo.properties"),"xp="+value+"\n",StandardCharsets.UTF_8);message("XP salvo. Será aplicado na próxima abertura do jogo.");}catch(Exception ex){message("Use um número entre 1 e 1000 para o XP.");}});
        play.addActionListener(e->submit(this::start));stop.addActionListener(e->submit(()->stop()));backup.addActionListener(e->submit(()->{backup();message("Backup criado na pasta backups.");}));
        guide.addActionListener(e->open(root.resolve("COMANDOS.html")));items.addActionListener(e->open(root.resolve("ITENS.html")));saves.addActionListener(e->open(serverDir.resolve("save_files/debug/character_saves")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){if(closing||busy)return;if(autoStart&&server!=null&&server.isAlive()){frame.setVisible(false);return;}closing=true;submit(()->{if(stop()){frame.dispose();worker.shutdown();}else closing=false;});}});
        frame.setContentPane(panel);frame.pack();frame.setMinimumSize(new Dimension(840,510));frame.setLocationRelativeTo(null);buttons();frame.setVisible(!autoStart);
    }
    interface Job{void run()throws Exception;}
    void submit(Job job){if(busy)return;busy=true;buttons();worker.submit(()->{try{job.run();}catch(Exception e){message("Erro: "+e.getMessage());closing=false;SwingUtilities.invokeLater(()->frame.setVisible(true));}finally{busy=false;buttons();}});}
    void buttons(){SwingUtilities.invokeLater(()->{boolean live=server!=null&&server.isAlive();play.setEnabled(!busy&&!live);stop.setEnabled(!busy&&live);backup.setEnabled(!busy&&!live);xp.setEnabled(!busy);});}
    void message(String s){SwingUtilities.invokeLater(()->status.setText(s));}
    void open(Path p){try{if(!Files.exists(p)&&!p.toString().endsWith(".txt")&&!p.toString().endsWith(".html"))Files.createDirectories(p);Desktop.getDesktop().open(p.toFile());}catch(Exception e){message("Não foi possível abrir "+p.getFileName());}}
    static boolean portOpen(){try(Socket s=new Socket()){s.connect(new InetSocketAddress("127.0.0.1",43597),200);return true;}catch(IOException e){return false;}}
    void start()throws Exception {
        check(root);if(portOpen())throw new IOException("A porta do jogo já está em uso. Feche a outra instalação antes de jogar.");
        String rate=String.valueOf(xp.getSelectedItem());double value;
        try{value=Double.parseDouble(rate);}catch(Exception e){throw new IOException("Use um número entre 1 e 1000 para o XP.");}
        if(!Double.isFinite(value)||value<1||value>1000)throw new IOException("O XP deve ficar entre 1 e 1000.");
        Files.writeString(root.resolve("solo.properties"),"xp="+value+"\n",StandardCharsets.UTF_8);backup();
        message("Abrindo seu mundo local...");
        ProcessBuilder s=new ProcessBuilder(java.toString(),"-Xmx1536m","-Dsolo.xp="+value,"-cp",serverDir.resolve("lib").toString()+File.separator+"*","io.xeros.solo.SoloServer");
        s.directory(serverDir.toFile());s.redirectErrorStream(true);s.redirectOutput(ProcessBuilder.Redirect.appendTo(root.resolve("logs/servidor.log").toFile()));server=s.start();
        boolean ready=false;
        for(int i=0;i<120;i++){if(!server.isAlive())break;if(portOpen()){ready=true;break;}Thread.sleep(500);}
        if(!ready)throw new IOException("O mundo não iniciou. Veja logs/servidor.log; use Salvar e fechar se o processo continuar ativo.");
        ProcessBuilder c=new ProcessBuilder(java.toString(),"-Xmx1536m","-Dsolo.offline=true","-Dsolo.root="+root,"-Duser.home="+root.resolve("runtime/userdata"),"-cp",root.resolve("runtime/client/lib").toString()+File.separator+"*","com.client.Client");
        c.directory(root.resolve("runtime").toFile());c.redirectErrorStream(true);c.redirectOutput(ProcessBuilder.Redirect.appendTo(root.resolve("logs/cliente.log").toFile()));client=c.start();
        watchClientExit(client);if(autoStart)SwingUtilities.invokeLater(()->frame.setVisible(false));message("Jogo aberto. Fechar a janela salva seu progresso automaticamente.");
    }
    void watchClientExit(Process watched){watched.onExit().thenRun(()->{
            try { worker.submit(()->{
                if(client!=watched)return;
                busy=true;buttons();
                try{if(stop()&&autoStart){SwingUtilities.invokeAndWait(()->frame.dispose());worker.shutdown();}}catch(Exception e){message("Erro ao salvar: "+e.getMessage());SwingUtilities.invokeLater(()->frame.setVisible(true));}
                finally{busy=false;buttons();}
            }); }catch(RejectedExecutionException ignored){}
        });
    }
    boolean stop()throws Exception {
        if(server!=null&&server.isAlive()) {
            message("Salvando os personagens...");server.getOutputStream().write("stop\n".getBytes(StandardCharsets.UTF_8));server.getOutputStream().flush();
            if(!server.waitFor(45,TimeUnit.SECONDS)){message("O salvamento ainda não terminou. Aguarde e tente fechar novamente.");return false;}
            if(server.exitValue()!=0){message("O servidor encerrou com erro. Consulte logs/servidor.log.");return false;}
        }
        if(client!=null&&client.isAlive())client.destroy();server=null;client=null;backup();message("Progresso salvo. Pode abrir o jogo quando quiser.");return true;
    }
    void backup()throws IOException {
        Path base=serverDir.resolve("save_files");Files.createDirectories(base);Files.createDirectories(root.resolve("backups"));
        String stamp=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(root.resolve("backups/personagens-"+stamp+".zip")));Stream<Path> walk=Files.walk(base)) {
            for(Path p:walk.filter(Files::isRegularFile).collect(Collectors.toList())){zip.putNextEntry(new ZipEntry(base.relativize(p).toString().replace('\\','/')));Files.copy(p,zip);zip.closeEntry();}
        }
        try(Stream<Path> backups=Files.list(root.resolve("backups"))){for(Path old:backups.filter(p->p.getFileName().toString().matches("personagens-[0-9-]+\\.zip")).sorted(Comparator.reverseOrder()).skip(10).collect(Collectors.toList()))Files.deleteIfExists(old);}
    }
    public static void main(String[] args)throws Exception {
        Path root=Paths.get(args[0]).toRealPath();check(root);
        if(args.length>1&&args[1].equals("--check")){System.out.println("OSRS_PACKAGE_OK");return;}
        try(FileChannel channel=FileChannel.open(root.resolve("launcher.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);FileLock lock=channel.tryLock()) {
            if(lock==null){Files.writeString(root.resolve("central.request"),"show");return;}
            Files.deleteIfExists(root.resolve("central.request"));Launcher launcher=new Launcher(root);launcher.autoStart=!Arrays.asList(args).contains("--settings");SwingUtilities.invokeAndWait(()->{try{launcher.ui();}catch(Exception e){throw new RuntimeException(e);}});
            if(launcher.autoStart)launcher.submit(launcher::start);
            while(launcher.frame.isDisplayable()){
                if(Files.exists(root.resolve("central.request"))){Files.deleteIfExists(root.resolve("central.request"));SwingUtilities.invokeLater(()->{launcher.frame.setVisible(true);launcher.frame.setExtendedState(Frame.NORMAL);launcher.frame.toFront();});}
                Thread.sleep(300);
            }
        }
    }
}

