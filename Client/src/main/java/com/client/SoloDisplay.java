package com.client;
import com.client.features.gameframe.ScreenMode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.io.*;
import java.util.Properties;
/** Classic interface scaling with matching inverse input and persistent window geometry. */
public final class SoloDisplay {
 private SoloDisplay(){}
 private static final Path FILE=Paths.get(System.getProperty("user.home"),"solo-display.properties");
 private static int normalWidth,normalHeight;private static javax.swing.Timer debounce;
 public static void initialize(){
  if(!Boolean.getBoolean("solo.offline")||ClientWindow.frame==null)return;
  SwingUtilities.invokeLater(()->{
   JFrame frame=ClientWindow.frame;JMenuBar bar=new JMenuBar();JMenu size=new JMenu("Tamanho do jogo");
   for(int percent:new int[]{100,125,150,175,200}){JMenuItem option=new JMenuItem(percent+"%");option.addActionListener(e->resize(percent));size.add(option);}
   JMenuItem maximize=new JMenuItem("Maximizar");maximize.addActionListener(e->{enableStretch();frame.setExtendedState(Frame.MAXIMIZED_BOTH);});size.add(maximize);bar.add(size);
   JMenu tools=new JMenu("Meu mundo");JMenuItem worldSettings=new JMenuItem("Configurações e ferramentas");worldSettings.addActionListener(e->{try{Path root=Paths.get(System.getProperty("solo.root",Paths.get(System.getProperty("user.home")).getParent().getParent().toString()));Files.writeString(root.resolve("central.request"),"show");}catch(IOException ex){JOptionPane.showMessageDialog(frame,"Não foi possível abrir as configurações: "+ex.getMessage());}});tools.add(worldSettings);bar.add(tools);frame.setJMenuBar(bar);
   frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
   frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){saveWindow();try{com.client.utilities.settings.SettingsManager.saveSettings(Client.instance);}catch(IOException ex){System.err.println("Falha ao guardar preferências: "+ex.getMessage());}Client.instance.resetLogout();frame.dispose();System.exit(0);}});
   Properties settings=new Properties();try(InputStream in=Files.newInputStream(FILE)){settings.load(in);}catch(IOException ignored){}
   resize(150);
   try{if(settings.containsKey("width")){applySize(Integer.parseInt(settings.getProperty("width")),Integer.parseInt(settings.getProperty("height")));}if(Boolean.parseBoolean(settings.getProperty("maximized","false")))frame.setExtendedState(Frame.MAXIMIZED_BOTH);}catch(NumberFormatException ignored){}
   debounce=new javax.swing.Timer(600,e->saveWindow());debounce.setRepeats(false);
   frame.addComponentListener(new ComponentAdapter(){public void componentResized(ComponentEvent e){if((frame.getExtendedState()&Frame.MAXIMIZED_BOTH)==0){normalWidth=frame.getWidth();normalHeight=frame.getHeight();}debounce.restart();}});
   frame.addWindowStateListener(e->debounce.restart());
  });
 }
 private static void enableStretch(){Client.getUserSettings().setStretchedMode(true);Client.stretched=true;ClientWindow.frame.setResizable(true);}
 public static void resize(int percent){JFrame frame=ClientWindow.frame;enableStretch();if(Client.currentScreenMode!=ScreenMode.FIXED)Client.instance.setGameMode(ScreenMode.FIXED,false);frame.setExtendedState(Frame.NORMAL);Insets border=frame.getInsets();int bar=frame.getJMenuBar()==null?0:frame.getJMenuBar().getPreferredSize().height;applySize(765*percent/100+border.left+border.right,503*percent/100+border.top+border.bottom+bar);}
 private static void applySize(int width,int height){JFrame frame=ClientWindow.frame;Insets border=frame.getInsets();Rectangle bounds=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();int bar=frame.getJMenuBar()==null?0:frame.getJMenuBar().getPreferredSize().height;int minW=Math.min(bounds.width,765+border.left+border.right),minH=Math.min(bounds.height,503+border.top+border.bottom+bar);frame.setMinimumSize(new Dimension(minW,minH));normalWidth=Math.max(minW,Math.min(bounds.width,width));normalHeight=Math.max(minH,Math.min(bounds.height,height));frame.setSize(normalWidth,normalHeight);frame.validate();}
 public static void saveWindow(){if(ClientWindow.frame==null)return;JFrame frame=ClientWindow.frame;Properties p=new Properties();p.setProperty("width",Integer.toString(normalWidth>0?normalWidth:frame.getWidth()));p.setProperty("height",Integer.toString(normalHeight>0?normalHeight:frame.getHeight()));p.setProperty("maximized",Boolean.toString((frame.getExtendedState()&Frame.MAXIMIZED_BOTH)!=0));try{Files.createDirectories(FILE.getParent());Path temp=FILE.resolveSibling(FILE.getFileName()+".tmp");try(OutputStream out=Files.newOutputStream(temp)){p.store(out,"MyRuneS MMO - tamanho da janela");}try{Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);}}catch(IOException ex){System.err.println("Falha ao guardar tamanho da janela: "+ex.getMessage());}}
}
