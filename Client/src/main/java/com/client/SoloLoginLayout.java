package com.client;
import java.awt.*;
import java.awt.image.BufferedImage;
/** Vector login rendered at the actual canvas resolution; uniform input/drawing transform. */
public final class SoloLoginLayout {
 public final Rectangle card,user,password,rememberUser,rememberPassword,login;
 private static BufferedImage canvas;
 public SoloLoginLayout(int width,int height){int x=width/2-210,y=Math.max(132,height/2-160);card=new Rectangle(x,y,420,340);user=new Rectangle(x+24,y+65,372,35);password=new Rectangle(x+24,y+126,372,35);rememberUser=new Rectangle(x+24,y+179,160,22);rememberPassword=new Rectangle(x+224,y+179,160,22);login=new Rectangle(x+24,y+215,372,40);}
 public static double scale(int w,int h){return Math.max(.01,Math.min(w/765.0,h/503.0));}
 public static Point logicalPoint(int x,int y,int w,int h){double s=scale(w,h);return new Point((int)Math.floor((x-(w-765*s)/2)/s),(int)Math.floor((y-(h-503*s)/2)/s));}
 private static void centered(Graphics2D g,String text,int x,int y,int size,Color color,boolean bold){g.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));g.setColor(color);g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,y);}
 private static void box(Graphics2D g,Rectangle r,Color fill,Color border){g.setColor(fill);g.fillRoundRect(r.x,r.y,r.width,r.height,5,5);g.setColor(border);g.drawRoundRect(r.x,r.y,r.width,r.height,5,5);}
 private static void check(Graphics2D g,Rectangle r,boolean selected,String text){box(g,new Rectangle(r.x,r.y+4,12,12),new Color(11,23,29),new Color(selected?0xd4b77b:0x596e72));if(selected){g.setStroke(new BasicStroke(1.8f));g.setColor(new Color(224,196,140));g.drawLine(r.x+3,r.y+10,r.x+5,r.y+13);g.drawLine(r.x+5,r.y+13,r.x+10,r.y+7);g.setStroke(new BasicStroke(1));}g.setFont(new Font("Segoe UI",Font.PLAIN,11));g.setColor(new Color(187,205,207));g.drawString(text,r.x+21,r.y+14);}
 public static synchronized void render(Graphics target,Component component,String username,String password,int focus,boolean rememberUser,boolean rememberPassword,String message,boolean blink,int mouseX,int mouseY){
  int w=Math.max(1,component.getWidth()),h=Math.max(1,component.getHeight());if(canvas==null||canvas.getWidth()!=w||canvas.getHeight()!=h)canvas=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
  Graphics2D g=canvas.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  g.setPaint(new GradientPaint(0,0,new Color(9,21,30),w,h,new Color(23,48,56)));g.fillRect(0,0,w,h);double s=scale(w,h);g.translate((w-765*s)/2,(h-503*s)/2);g.scale(s,s);
  g.setColor(new Color(160,196,192,25));g.fillOval(575,66,68,68);for(int layer=0;layer<3;layer++){g.setColor(new Color(9+layer*2,25+layer*2,34+layer*2));Polygon p=new Polygon();p.addPoint(0,503);for(int n=0;n<12;n++)p.addPoint(n*77,290+layer*45-(int)(Math.sin(n*1.6+layer)*57));p.addPoint(765,503);g.fillPolygon(p);}
  g.setColor(new Color(5,17,24));g.fillRect(62,313,66,190);g.fillRect(50,269,25,234);g.fillRect(115,243,25,260);for(int i=0;i<4;i++){g.fillRect(50+i*7,261,4,12);g.fillRect(115+i*7,235,4,12);}g.setColor(new Color(179,144,80,65));g.fillRoundRect(60,294,6,18,3,3);g.fillRoundRect(125,272,6,18,3,3);
  g.setColor(new Color(144,123,81,60));g.drawLine(32,27,733,27);g.drawLine(32,481,733,481);centered(g,"MyRuneS MMO",382,78,34,new Color(230,205,157),true);centered(g,"OLD SCHOOL  ·  SEU MUNDO PARTICULAR",382,103,10,new Color(140,176,181),false);
  SoloLoginLayout l=new SoloLoginLayout(765,503);g.setColor(new Color(0,0,0,65));g.fillRoundRect(l.card.x-7,l.card.y-6,l.card.width+14,l.card.height+14,14,14);box(g,l.card,new Color(14,31,40),new Color(65,92,99));centered(g,"Sua próxima aventura começa aqui",382,l.card.y+31,16,new Color(224,203,161),true);
  g.setFont(new Font("Segoe UI",Font.PLAIN,12));g.setColor(new Color(175,197,199));g.drawString("Personagem",l.user.x,l.user.y-9);g.drawString("Senha",l.password.x,l.password.y-9);
  box(g,l.user,new Color(8,21,28),new Color(focus==0?0xc1a471:0x3a555d));box(g,l.password,new Color(8,21,28),new Color(focus==1?0xc1a471:0x3a555d));g.setFont(new Font("Segoe UI",Font.PLAIN,15));g.setColor(new Color(228,237,235));g.drawString(username+(focus==0&&blink?"|":""),l.user.x+12,l.user.y+24);g.drawString(password+(focus==1&&blink?"|":""),l.password.x+12,l.password.y+24);
  check(g,l.rememberUser,rememberUser,"Lembrar usuário");check(g,l.rememberPassword,rememberPassword,"Lembrar senha");boolean hover=l.login.contains(logicalPoint(mouseX,mouseY,w,h));box(g,l.login,new Color(hover?0x3b777a:0x285b62),new Color(hover?0xe6ca91:0x6b9da1));centered(g,"ENTRAR EM GIELINOR",382,l.login.y+25,14,new Color(243,229,198),true);
  centered(g,"Personagem novo? Escolha um nome e uma senha.",382,l.card.y+278,11,new Color(156,180,184),false);
  String text=message==null?"":message.replaceAll("@[a-zA-Z0-9]+@|<[^>]+>","");if(text.isEmpty())text="Seu progresso é salvo automaticamente ao sair.";g.setFont(new Font("Segoe UI",Font.PLAIN,11));if(g.getFontMetrics().stringWidth(text)>388){int mid=text.lastIndexOf(' ',Math.min(text.length()-1,text.length()/2+10));if(mid>0){centered(g,text.substring(0,mid),382,l.card.y+306,11,new Color(207,184,134),false);centered(g,text.substring(mid+1),382,l.card.y+322,11,new Color(207,184,134),false);}}else centered(g,text,382,l.card.y+315,11,new Color(150,177,181),false);
  g.dispose();target.drawImage(canvas,0,0,null);
 }
}
