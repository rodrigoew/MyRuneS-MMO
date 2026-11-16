package com.client;
import java.lang.reflect.Field;
import java.io.File;
import javax.imageio.ImageIO;
public final class LoginPreviewCheck {
 public static void main(String[] args)throws Exception{
  Client.main(new String[0]);
  for(int i=0;i<600&&(Client.instance==null||!Client.instance.soloStartupComplete);i++)Thread.sleep(100);
  Client c=Client.instance;if(c==null||!c.soloStartupComplete)throw new AssertionError("Startup");
  ClientWindow.frame.setTitle("OSRS Solo - Validação da atualização");ClientWindow.frame.setLocation(50,50);
  Field user=Client.class.getDeclaredField("myUsername");user.setAccessible(true);user.set(c,"");c.setPassword("");
  Thread.sleep(1500);Field frame=Client.class.getDeclaredField("loginScreenGraphicsBuffer");frame.setAccessible(true);
  RSImageProducer buffer=(RSImageProducer)frame.get(c);ImageIO.write(buffer.image,"png",new File(args[0]));
  System.out.println("LOGIN_PREVIEW_READY");
  for(int i=0;i<900;i++){if(new File(args[1]).exists())break;Thread.sleep(1000);}
  System.exit(0);
 }
}

