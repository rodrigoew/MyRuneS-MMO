package io.xeros.content.skills.construction;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.instances.DynamicMapScene;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.*;
import java.util.*;

/** A character owns its layout; each visit builds an independent native map scene. */
public final class PlayerHouse {
    public static final int SKILL=22,BASE=8000;
    public enum Kind {
        GARDEN(1,1000,232,713),PARLOUR(1,1000,232,719),KITCHEN(5,5000,234,719),WORKSHOP(15,10000,232,717),CHAPEL(45,50000,234,717),STUDY(40,50000,236,717),BEDROOM(20,10000,238,719),DINING_ROOM(10,5000,236,719),SUPERIOR_GARDEN(65,75000,237,712);
        public final int level,cost,cx,cy;
        Kind(int level,int cost,int cx,int cy){this.level=level;this.cost=cost;this.cx=cx;this.cy=cy;}
    }
    static final class Room {
        final int x,y;final Kind kind;int rotation;
        final Map<Integer,String> furniture=new TreeMap<>();
        Room(int x,int y,Kind kind,int rotation){this.x=x;this.y=y;this.kind=kind;this.rotation=rotation;}
    }
    static final class Binding {
        final Room room;final WorldObject root;
        Binding(Room room,WorldObject root){this.room=room;this.root=root;}
    }
    private final Player p;
    private final HouseKitchen kitchen;
    private final HouseWorkshop workshop;
    private final HouseChapel chapel;
    private final HouseStudy study;
    private boolean owned,building;
    private boolean rebuilding;
    private final Map<Integer,Room> rooms=new TreeMap<>();
    private final Map<String,Binding> bindings=new HashMap<>();
    private DynamicMapScene scene;
    public PlayerHouse(Player player){p=player;kitchen=new HouseKitchen(this,player);workshop=new HouseWorkshop(this,player);chapel=new HouseChapel(this,player);study=new HouseStudy(this,player);}
    public HouseStudy study(){return study;}
    public HouseChapel chapel(){return chapel;}
    public HouseKitchen kitchen(){return kitchen;}
    public HouseWorkshop workshop(){return workshop;}
    private static int cell(int x,int y){return x*10+y;}
    private static String objectKey(WorldObject o){return o.x+","+o.y+","+o.height+","+o.type;}
    public boolean owned(){return owned;}
    public boolean building(){return building;}
    public int roomCount(){return rooms.size();}
    public boolean inside(){return scene!=null&&scene.activeFor(p);}
    private boolean available(){return p.getUtilityMagic().canInteract()&&!p.isForceMovementActive()&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    public boolean purchase(){
        if(owned||!available()||p.getInstance()!=null)return false;
        if(!ProductionBatch.exchange(p,new int[][]{{995,1000}},new int[0][])){p.sendMessage("You need 1,000 coins to buy a house.");return false;}
        Room garden=new Room(2,2,Kind.GARDEN,0);garden.furniture.put(15361,"EXIT_PORTAL");
        rooms.put(cell(2,2),garden);rooms.put(cell(2,3),new Room(2,3,Kind.PARLOUR,0));owned=true;
        p.sendMessage("Your house is ready: a garden, exit portal and parlour.");return true;
    }
    public boolean enter(boolean build){
        if(!owned){menu();return false;}
        if(!available()||p.getInstance()!=null||p.underAttackByNpc>0||p.underAttackByPlayer>0||p.getPosition().inWild())return false;
        building=build;rebuild();return true;
    }
    private void rebuild(){
        workshop.clear();
        if(scene!=null&&scene.activeFor(p)){rebuilding=true;try{scene.remove(p);}finally{rebuilding=false;}}
        scene=new DynamicMapScene(p,BASE,BASE);bindings.clear();
        int yard=dimensions(p.getPA().getLevelForXP(p.playerXP[SKILL]))+2;
        for(Room room:rooms.values())yard=Math.max(yard,Math.max(room.x,room.y)+2);
        for(int x=0;x<yard;x++)for(int y=0;y<yard;y++){
            scene.place(x,y,0,new DynamicMapScene.Chunk(235,712,0,0));
            Room room=rooms.get(cell(x,y));
            scene.place(x,y,1,room==null?new DynamicMapScene.Chunk(233,712,0,0):new DynamicMapScene.Chunk(room.kind.cx,room.kind.cy,0,room.rotation));
        }
        for(var region:sceneRegions())for(WorldObject o:region.worldObjectsSnapshot()){
            if(o.height!=1||o.x<BASE||o.y<BASE||o.x>=BASE+yard*8||o.y>=BASE+yard*8)continue;
            Room room=rooms.get(cell((o.x-BASE)/8,(o.y-BASE)/8));if(room==null)continue;
            if(o.id==15313||o.id==15314){
                int nx=room.x+(o.face==0?-1:o.face==2?1:0),ny=room.y+(o.face==1?1:o.face==3?-1:0);
                if(rooms.containsKey(cell(nx,ny))||!building)scene.replace(o,-1);
            }else if(HouseFurnishing.all().stream().anyMatch(f->f.group==HouseFurnishing.group(o.id))){
                Binding b=new Binding(room,o);bindings.put(objectKey(o),b);
                String key=room.furniture.get(HouseFurnishing.slot(o.id));HouseFurnishing f=key==null?null:HouseFurnishing.get(key);
                if(f!=null)scene.replace(o,displayModel(b,f));else if(!building)scene.replace(o,-1);
            }
        }
        scene.onClick((o,option)->click(o,option));
        final DynamicMapScene current=scene;
        scene.onClose(()->{if(scene==current&&!rebuilding){workshop.clear();chapel.clear();kitchen.clear();kitchen.removeTemporaryInventory();bindings.clear();scene=null;}});
        p.getPA().removeAllWindows();p.getPA().sendConfig(261,building?1:0);p.getPA().sendConfig(262,rooms.size());
        scene.activate(new Position(BASE+20,BASE+18,1));
    }
    private List<Region> sceneRegions(){
        List<Region> out=new ArrayList<>();for(int x=BASE;x<=BASE+64;x+=64)for(int y=BASE;y<=BASE+64;y+=64)out.add(scene.get(x,y));return out;
    }
    public static int dimensions(int level){return level>=60?7:level>=45?6:level>=30?5:level>=15?4:3;}
    public static int maxRooms(int level){return level>=99?34:level>=96?33:level>=50?25+(level-50)/6:24;}
    public boolean addRoom(Kind kind,int x,int y,int rotation){
        if(!inside()||!building||!available()||kind==null||rotation<0||rotation>3)return false;
        int dim=dimensions(p.getPA().getLevelForXP(p.playerXP[SKILL]));
        if(x<1||y<1||x>dim||y>dim||rooms.containsKey(cell(x,y))||rooms.size()>=maxRooms(p.getPA().getLevelForXP(p.playerXP[SKILL])))return false;
        if(!rooms.containsKey(cell(x-1,y))&&!rooms.containsKey(cell(x+1,y))&&!rooms.containsKey(cell(x,y-1))&&!rooms.containsKey(cell(x,y+1)))return false;
        if(p.playerLevel[SKILL]<kind.level){p.sendMessage("You need Construction level "+kind.level+".");return false;}
        if(!ProductionBatch.exchange(p,new int[][]{{995,kind.cost}},new int[0][])){p.sendMessage("You need "+kind.cost+" coins.");return false;}
        p.interruptActions();rooms.put(cell(x,y),new Room(x,y,kind,rotation));rebuild();return true;
    }
    public boolean rotateRoom(int x,int y,int rotation){
        Room room=rooms.get(cell(x,y));if(!inside()||!building||!available()||room==null||rotation<0||rotation>3)return false;
        p.interruptActions();room.rotation=rotation;rebuild();return true;
    }
    public boolean removeRoom(int x,int y){
        Room room=rooms.get(cell(x,y));if(!inside()||!building||!available()||room==null||(x==2&&y==2))return false;
        if(room.furniture.containsValue("EXIT_PORTAL")&&rooms.values().stream().filter(r->r!=room).noneMatch(r->r.furniture.containsValue("EXIT_PORTAL"))){p.sendMessage("Keep at least one exit portal.");return false;}
        Set<Integer> seen=new HashSet<>();ArrayDeque<Integer> queue=new ArrayDeque<>();queue.add(cell(2,2));
        while(!queue.isEmpty()){int c=queue.remove();if(c==cell(x,y)||!rooms.containsKey(c)||!seen.add(c))continue;int cx=c/10,cy=c%10;queue.add(cell(cx-1,cy));queue.add(cell(cx+1,cy));queue.add(cell(cx,cy-1));queue.add(cell(cx,cy+1));}
        if(seen.size()!=rooms.size()-1){p.sendMessage("Removing this room would disconnect your house.");return false;}
        p.interruptActions();kitchen.removeRoom(room);chapel.removeRoom(room);rooms.remove(cell(x,y));rebuild();return true;
    }
    public void exit(){
        if(!inside())return;p.interruptActions();p.getPA().stopSkilling();scene.remove(p);scene=null;bindings.clear();p.getPA().removeAllWindows();p.getPA().sendConfig(261,0);p.moveTo(new Position(2953,3224,0));
    }
    private boolean near(WorldObject o){
        if(!inside()||!available()||p.heightLevel!=o.height)return false;var size=o.getObjectSize();
        return Math.max(Math.max(o.x-p.absX,p.absX-o.x-size.getX()+1),Math.max(o.y-p.absY,p.absY-o.y-size.getY()+1))<=1;
    }
    private boolean current(Binding b,DynamicMapScene expected){
        if(scene!=expected||!inside()||!building||!available()||!roomRegistered(b.room))return false;
        String key=b.room.furniture.get(HouseFurnishing.slot(b.root.id));HouseFurnishing f=key==null?null:HouseFurnishing.get(key);
        int id=f==null?b.root.id:displayModel(b,f);
        return scene.get(b.root.x,b.root.y).getWorldObject(id,b.root.x,b.root.y,b.root.height).map(this::near).orElse(false);
    }
    private boolean click(WorldObject o,int option){
        if(!near(o))return true;
        if(kitchen.click(o,option))return true;
        if(workshop.click(o,option))return true;
        if(chapel.click(o,option))return true;
        if(study.click(o,option))return true;
        if(usePool(o,option))return true;
        Binding b=bindings.get(objectKey(o));
        if(b==null){
            if(building&&option==4&&(o.id==15313||o.id==15314)){
                Room room=rooms.get(cell((o.x-BASE)/8,(o.y-BASE)/8));if(room==null)return true;
                int x=room.x+(o.face==0?-1:o.face==2?1:0),y=room.y+(o.face==1?1:o.face==3?-1:0);
                roomMenu(o,x,y,0,scene);
            }return true;
        }
        String key=b.room.furniture.get(HouseFurnishing.slot(b.root.id));
        if(key!=null){
            if(key.equals("EXIT_PORTAL")&&option==1){exit();return true;}
            if(building&&(option==5||option==4&&o.id==b.root.id&&HouseFurnishing.get(key).model(b.root.id)<0)){
                recipes(b,0);return true;
            }
            if(building&&option==4){
                DynamicMapScene expected=scene;
                new DialogueBuilder(p).option("Remove "+HouseFurnishing.get(key).name()+"? Materials are lost.",new DialogueOption("Remove",q->{q.getPA().removeAllWindows();if(current(b,expected)){if(key.equals("EXIT_PORTAL")&&rooms.values().stream().filter(r->r!=b.room).noneMatch(r->r.furniture.containsValue("EXIT_PORTAL")))q.sendMessage("Keep at least one exit portal.");else removeFurniture(b);}}),new DialogueOption("Keep",q->q.getPA().removeAllWindows())).send();
            }
        }else if(building&&option==4)recipes(b,0);
        return true;
    }
    private void roomMenu(WorldObject door,int x,int y,int page,DynamicMapScene expected){
        if(scene!=expected||!inside()||!building||!near(door))return;
        List<DialogueOption> options=new ArrayList<>();Kind[] kinds=Kind.values();
        for(int i=page*3;i<Math.min(kinds.length,page*3+3);i++){Kind kind=kinds[i];options.add(new DialogueOption(kind+" ("+kind.cost+" coins)",q->{q.getPA().removeAllWindows();if(scene==expected&&near(door))addRoom(kind,x,y,0);}));}
        if(page*3+3<kinds.length)options.add(new DialogueOption("More",q->roomMenu(door,x,y,page+1,expected)));else options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
        if(page>0)options.add(new DialogueOption("Página anterior",q->roomMenu(door,x,y,page-1,expected)));
        new DialogueBuilder(p).option("Adicionar cômodo",options.toArray(new DialogueOption[0])).send();
    }
    private void recipes(Binding b,int page){
        DynamicMapScene expected=scene;if(!current(b,expected))return;
        String previous=b.room.furniture.get(HouseFurnishing.slot(b.root.id));
        List<HouseFurnishing> list=new ArrayList<>();for(var f:HouseFurnishing.all())if(f.group==HouseFurnishing.group(b.root.id)&&Objects.equals(f.prerequisite(),previous))list.add(f);
        List<DialogueOption> options=new ArrayList<>();int start=page*3;
        for(int i=start;i<Math.min(start+3,list.size());i++){var f=list.get(i);options.add(new DialogueOption(f.name()+" (level "+f.level+")",q->{q.getPA().removeAllWindows();if(current(b,expected))build(b,f);}));}
        if(start+3<list.size())options.add(new DialogueOption("More",q->{if(scene==expected)recipes(b,page+1);}));
        else options.add(new DialogueOption("Cancel",q->q.getPA().removeAllWindows()));
        if(page>0)options.add(new DialogueOption("Página anterior",q->{if(scene==expected)recipes(b,page-1);}));
        new DialogueBuilder(p).option("Construir móvel",options.toArray(new DialogueOption[0])).send();
    }
    private boolean tools(HouseFurnishing f){
        if(f.plant){for(int id=5333;id<=5340;id++)if(p.getItems().playerHasItem(id))return true;p.sendMessage("You need a filled watering can.");return false;}
        return constructionTools();
    }
    private boolean hammer(){return p.getItems().playerHasItem(2347)||p.getItems().playerHasItem(25644)||p.getItems().isWearingItem(25644);}
    boolean constructionTools(){if(!hammer()||!p.getItems().playerHasItem(8794)){p.sendMessage("You need a hammer and saw.");return false;}return true;}
    private int[][] inputs(HouseFurnishing f){
        return materialInputs(f.materials);
    }
    int[][] materialInputs(int[][] materials){
        List<int[]> out=new ArrayList<>();for(int[] a:materials){
            if(a[0]!=4819){out.add(a.clone());continue;}int left=a[1];
            for(int id:new int[]{4824,4823,4822,4821,1539,4820,4819}){int take=Math.min(left,p.getItems().getInventoryCount(id));if(take>0){out.add(new int[]{id,take});left-=take;}}
            if(left>0)return null;
        }return out.toArray(new int[0][]);
    }
    boolean installFlatpack(HouseFlatpackData.Recipe recipe,WorldObject object){
        if(!inside()||!building||!available()||!hammer()||object==null)return false;
        Binding b=bindings.get(objectKey(object));if(b==null||!roomRegistered(b.room)||object.id!=b.root.id||!near(object)||scene.get(object.x,object.y).getWorldObject(object.id,object.x,object.y,object.height).filter(o->o.type==object.type).isEmpty())return false;
        HouseFurnishing f=HouseFurnishing.get(recipe.key);int slot=HouseFurnishing.slot(b.root.id);
        if(f==null||f.group!=HouseFurnishing.group(b.root.id)||b.room.furniture.containsKey(slot))return false;
        p.interruptActions();p.getPA().stopSkilling();
        if(!ProductionBatch.exchange(p,new int[][]{{recipe.item,1}},new int[0][]))return false;
        b.room.furniture.put(slot,f.key);updateFurniture(b.room,slot);p.startAnimation(898);return true;
    }
    private boolean build(Binding b,HouseFurnishing f){
        DynamicMapScene expected=scene;int slot=HouseFurnishing.slot(b.root.id);
        String previous=b.room.furniture.get(slot);
        if(!current(b,expected)||!Objects.equals(f.prerequisite(),previous)||f.group!=HouseFurnishing.group(b.root.id)||p.playerLevel[SKILL]<f.level||p.playerLevel[7]<f.cookingLevel()||!tools(f))return false;
        p.interruptActions();p.getPA().stopSkilling();long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;p.playerSkilling[SKILL]=true;p.startAnimation(f.plant?2291:898);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,5){
            public void execute(){
                int[][] in=inputs(f);
                if(token==p.productionActionToken&&p.playerSkilling[SKILL]&&!p.isDisconnected()&&p.getSession()!=null&&p.absX==x&&p.absY==y&&p.heightLevel==h&&p.isWalkingQueueEmpty()&&current(b,expected)&&Objects.equals(previous,b.room.furniture.get(slot))&&Objects.equals(f.prerequisite(),previous)&&p.playerLevel[SKILL]>=f.level&&p.playerLevel[7]>=f.cookingLevel()&&tools(f)&&in!=null&&ProductionBatch.exchange(p,in,new int[0][])){
                    b.room.furniture.put(slot,f.key);updateFurniture(b.room,slot);p.getPA().addSkillXPMultiplied(f.xp,SKILL,true);
                    if(f.plant){for(int id=5333;id<=5340;id++)if(p.getItems().playerHasItem(id)){ProductionBatch.exchange(p,new int[][]{{id,1}},new int[][]{{id==5333?5331:id-1,1}});break;}p.getPA().addSkillXPMultiplied(f.xp,19,true);}
                    p.sendMessage("You build the "+f.name().toLowerCase()+".");
                }else p.sendMessage("Construction stopped. Check your level, tools and materials.");stop();
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[SKILL]=false;p.startAnimation(65535);}}
        });return true;
    }
    private void updateFurniture(Room room,int slot){
        HouseFurnishing f=HouseFurnishing.get(room.furniture.get(slot));
        for(Binding b:bindings.values())if(b.room==room&&HouseFurnishing.slot(b.root.id)==slot){
            scene.overlayAt(b.root,f==null?b.root.id:displayModel(b,f));
        }scene.sendOverrides(p);
        if(slot==15269){updateFurniture(room,15270);updateFurniture(room,13733);updateFurniture(room,15275);}
    }
    private void removeFurniture(Binding b){int slot=HouseFurnishing.slot(b.root.id);p.interruptActions();kitchen.furnitureRemoved(b.room,slot);chapel.furnitureRemoved(b.room,slot);b.room.furniture.remove(slot);updateFurniture(b.room,slot);}
    boolean roomRegistered(Room room){return rooms.get(cell(room.x,room.y))==room;}
    WorldObject furniturePart(Room room,int group,int root){
        if(!inside())return null;
        for(Binding b:bindings.values())if(b.room==room&&HouseFurnishing.slot(b.root.id)==group&&b.root.id==root){
            HouseFurnishing f=furnishing(b);if(f==null)return null;
            return scene.get(b.root.x,b.root.y).getWorldObject(displayModel(b,f),b.root.x,b.root.y,b.root.height).orElse(null);
        }return null;
    }
    private int displayModel(Binding b,HouseFurnishing f){int id=b.room.kind==Kind.CHAPEL?chapel.model(b,f):kitchen.model(b,f);return id<0&&building?b.root.id:id;}
    boolean furnitureReady(Binding b){
        if(!inside()||!available()||b==null||!roomRegistered(b.room))return false;
        HouseFurnishing f=furnishing(b);if(f==null)return false;
        return scene.get(b.root.x,b.root.y).getWorldObject(displayModel(b,f),b.root.x,b.root.y,b.root.height).filter(o->o.type==b.root.type).map(this::near).orElse(false);
    }
    List<WorldObject> nearbyFurniture(){
        List<WorldObject> out=new ArrayList<>();if(!inside())return out;
        for(Binding b:bindings.values()){HouseFurnishing f=furnishing(b);if(f!=null)scene.get(b.root.x,b.root.y).getWorldObject(displayModel(b,f),b.root.x,b.root.y,b.root.height).filter(this::near).ifPresent(out::add);}
        return out;
    }
    Binding furnitureFixture(WorldObject o){
        Binding b=bindings.get(objectKey(o));if(!furnitureReady(b))return null;
        return o.id==displayModel(b,furnishing(b))?b:null;
    }
    boolean kitchenOwnerReady(){return inside()&&available();}
    HouseFurnishing furnishing(Binding b){return HouseFurnishing.get(b.room.furniture.get(HouseFurnishing.slot(b.root.id)));}
    boolean kitchenReady(Binding b){
        if(!inside()||!available()||b==null||b.room.kind!=Kind.KITCHEN||!roomRegistered(b.room))return false;
        HouseFurnishing f=furnishing(b);if(f==null)return false;
        int id=kitchen.model(b,f);
        return scene.get(b.root.x,b.root.y).getWorldObject(id,b.root.x,b.root.y,b.root.height).filter(o->o.type==b.root.type).map(this::near).orElse(false);
    }
    Binding kitchenFixture(WorldObject o){
        Binding b=bindings.get(objectKey(o));
        if(!kitchenReady(b))return null;
        HouseFurnishing f=furnishing(b);return f!=null&&o.id==kitchen.model(b,f)?b:null;
    }
    void renderKitchen(Room room){if(inside()&&roomRegistered(room))updateFurniture(room,15398);}
    void renderChapel(Room room){if(inside()&&roomRegistered(room))updateFurniture(room,15271);}
    public void menu(){
        if(!available())return;
        if(!owned){new DialogueBuilder(p).option("Buy a house in Rimmington for 1,000 coins?",new DialogueOption("Buy house",q->{q.getPA().removeAllWindows();purchase();}),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return;}
        new DialogueBuilder(p).option("Your house",new DialogueOption("Enter normally",q->{q.getPA().removeAllWindows();switchMode(false);}),new DialogueOption("Building mode",q->{q.getPA().removeAllWindows();switchMode(true);}),new DialogueOption("Leave house",q->exit())).send();
    }
    private void switchMode(boolean build){if(!available())return;p.interruptActions();p.getPA().stopSkilling();if(inside()){building=build;rebuild();}else enter(build);}
    public void command(String input){
        String[] a=input.trim().toUpperCase(Locale.ROOT).split("\\s+");
        try{
            if(a[0].equals("EXIT")){exit();return;}
            if(a[0].equals("BUILD")){switchMode(true);return;}
            if(a[0].equals("ENTER")){switchMode(false);return;}
            if(a[0].equals("ROOM")&&a.length==5){addRoom(Kind.valueOf(a[1]),Integer.parseInt(a[2]),Integer.parseInt(a[3]),Integer.parseInt(a[4]));return;}
            if(a[0].equals("ROTATE")&&a.length==4){rotateRoom(Integer.parseInt(a[1]),Integer.parseInt(a[2]),Integer.parseInt(a[3]));return;}
            if(a[0].equals("REMOVE")&&a.length==3){removeRoom(Integer.parseInt(a[1]),Integer.parseInt(a[2]));return;}
            if(a[0].equals("LIST")){for(Room room:rooms.values())p.sendMessage(room.kind+" "+room.x+" "+room.y+" rotation "+room.rotation+"; "+room.furniture.size()+" furnished slots.");return;}
            menu();
        }catch(IllegalArgumentException ex){p.sendMessage("House: ::house | build | enter | exit | list | room KIND X Y ROTATION | rotate X Y ROTATION | remove X Y");}
    }
    public String encode(){
        StringBuilder out=new StringBuilder("1|").append(owned?'1':'0');
        for(Room room:rooms.values()){out.append('|').append(room.x).append(',').append(room.y).append(',').append(room.kind.name()).append(',').append(room.rotation);for(var f:room.furniture.entrySet())out.append(',').append(f.getKey()).append('=').append(f.getValue());}
        return out.toString();
    }
    public boolean decode(String text){
        try{
            if(text==null||text.length()>30000)return false;String[] records=text.split("\\|",-1);if(records.length<2||!records[0].equals("1")||(!records[1].equals("0")&&!records[1].equals("1")))return false;
            boolean nextOwned=records[1].equals("1");Map<Integer,Room> next=new TreeMap<>();
            for(int i=2;i<records.length;i++){String[] fields=records[i].split(",",-1);if(fields.length<4)return false;int x=Integer.parseInt(fields[0]),y=Integer.parseInt(fields[1]),rotation=Integer.parseInt(fields[3]);Kind kind=Kind.valueOf(fields[2]);
                if(x<1||x>7||y<1||y>7||rotation<0||rotation>3)return false;Room room=new Room(x,y,kind,rotation);
                Set<Integer> allowed=slots(kind);
                for(int j=4;j<fields.length;j++){String[] value=fields[j].split("=",-1);if(value.length!=2)return false;int slot=Integer.parseInt(value[0]);HouseFurnishing f=HouseFurnishing.get(value[1]);if(f==null||!allowed.contains(slot)||f.group!=HouseFurnishing.group(slot)||room.furniture.put(slot,f.key)!=null)return false;}
                if(next.put(cell(x,y),room)!=null)return false;
            }
            if(!nextOwned&&!next.isEmpty()||nextOwned&&(next.isEmpty()||next.size()>34||!next.containsKey(cell(2,2))||next.get(cell(2,2)).kind!=Kind.GARDEN||next.values().stream().noneMatch(r->r.furniture.containsValue("EXIT_PORTAL"))))return false;
            if(nextOwned){Set<Integer> seen=new HashSet<>();ArrayDeque<Integer> q=new ArrayDeque<>();q.add(cell(2,2));while(!q.isEmpty()){int c=q.remove();if(!next.containsKey(c)||!seen.add(c))continue;int x=c/10,y=c%10;q.add(cell(x-1,y));q.add(cell(x+1,y));q.add(cell(x,y-1));q.add(cell(x,y+1));}if(seen.size()!=next.size())return false;}
            rooms.clear();rooms.putAll(next);owned=nextOwned;return true;
        }catch(RuntimeException ex){return false;}
    }
    private boolean usePool(WorldObject object,int option){
        Binding binding=bindings.get(objectKey(object));
        if(binding==null||binding.room.kind!=Kind.SUPERIOR_GARDEN||HouseFurnishing.group(binding.root.id)!=29122||building||option!=1)return false;
        HouseFurnishing pool=furnishing(binding);if(pool==null||!furnitureReady(binding))return true;
        int tier=pool.models[0]-29237;if(tier<0||tier>4)return true;
        p.specAmount=10;p.getItems().updateSpecialBar();
        if(tier>=1)p.setRunEnergy(100,true);
        if(tier>=2){int base=p.getLevelForXP(p.playerXP[5]);if(p.playerLevel[5]<base){p.playerLevel[5]=base;p.getPA().refreshSkill(5);}}
        if(tier>=3)for(int skill=0;skill<p.playerLevel.length;skill++)if(skill!=3&&p.playerLevel[skill]<p.getLevelForXP(p.playerXP[skill])){p.playerLevel[skill]=p.getLevelForXP(p.playerXP[skill]);p.getPA().refreshSkill(skill);}
        if(tier>=4){p.getHealth().increase(p.getHealth().getMaximumHealth());p.getHealth().removeAllStatuses();}
        p.startAnimation(7305);p.sendMessage("Você recuperou suas energias na piscina da casa.");return true;
    }
    private static Set<Integer> slots(Kind kind){
        if(kind==Kind.BEDROOM)return Set.of(15260,15261,15262,4524,4518,4523,15268);
        if(kind==Kind.DINING_ROOM)return Set.of(15298,15299,15300,4523,4524);
        if(kind==Kind.SUPERIOR_GARDEN)return Set.of(29122);
        if(kind==Kind.GARDEN)return Set.of(15361,15362,15363,15364,15365,15366,15367);
        if(kind==Kind.PARLOUR)return Set.of(4515,4516,4517,4518,4521,4523,4524);
        if(kind==Kind.WORKSHOP)return Set.of(15439,15441,15443,15448,15450);
        if(kind==Kind.STUDY)return Set.of(15420,15421,15422,15423,15424,15425);
        if(kind==Kind.CHAPEL)return Set.of(15269,15270,15271,13733,15272,15275,15276);
        return Set.of(15398,15399,15401,15402,15403,15404,15405);
    }
    private static boolean canvas(int x,int y){return x>=BASE-16&&x<BASE+88&&y>=BASE-16&&y<BASE+88;}
    public void safeLogin(){if(!inside())kitchen.removeTemporaryInventory();if(canvas(p.absX,p.absY)||canvas(p.getTeleportToX(),p.getTeleportToY()))p.moveTo(new Position(2953,3224,0));}
}
