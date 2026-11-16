package io.xeros.content.skills.farming;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.common.base.Preconditions;
import io.xeros.Server;
import io.xeros.content.skills.Skill;
import io.xeros.model.Animation;
import io.xeros.model.Items;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;
import io.xeros.util.Misc;

public class Farming {

	public static final int HARVEST_ANIMATION = 2275;

	/**
	 * Placeholder for Tree harvesting
	 */
	public static final int HARVEST_CHOPPING = -2;

	private static final int[] farmingOutfit = { 13646, 13642, 13640, 13644 };

	private final Player player;
    private final FarmingServices services;
    public FarmingServices services(){return services;}
	private final SaplingGrowth saplings;
	public SaplingGrowth getSaplings(){return saplings;}
	private final CompostBins bins;
	public CompostBins getBins(){return bins;}
	private Plant[] plants = new Plant[50];
	private GrassyPatch[] patches = new GrassyPatch[50];

	public Farming(Player player) {
		this.player = player;
        services=new FarmingServices(player);
		this.saplings=new SaplingGrowth(player);
		this.bins=new CompostBins(player);

		for (int i = 0; i < patches.length; i++)
			if (patches[i] == null)
				patches[i] = new GrassyPatch();
	}

    public boolean canReach(FarmingPatches patch) {
        if(!player.getUtilityMagic().canInteract() || player.heightLevel!=0 || player.isForceMovementActive())return false;
        int dx=Math.max(patch.bottomLeft.getX()-player.absX,player.absX-patch.topLeft.getX());
        int dy=Math.max(patch.bottomLeft.getY()-player.absY,player.absY-patch.topLeft.getY());
        return Math.max(dx,dy)<=2;
    }

    public boolean contains(Plant plant) {
        for(Plant entry:plants)if(entry==plant)return true;
        return false;
    }

    private boolean canReach(int x,int y) {
        for(FarmingPatches patch:FarmingPatches.values())
            if(x>=patch.bottomLeft.getX()&&x<=patch.topLeft.getX()&&y>=patch.bottomLeft.getY()
                    &&y<=patch.topLeft.getY()&&!isPatchException(x,y,patch))return canReach(patch);
        return false;
    }

    public boolean applyCompost(int item,int x,int y) {
        int tier=item==6032?1:item==6034?2:item==21483?3:0;
        if(tier==0)return false;
        if(!canReach(x,y))return true;
        Plant crop=getPlantedPatch(x,y);int empty=getGrassyPatch(x,y);
        if(crop==null&&empty<0)return false;
        int previous=crop==null?patches[empty].compost:crop.compost;
        if(tier<=previous){player.sendMessage("This patch already has this compost or better.");return true;}
        if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[][]{{item,1}},new int[][]{{1925,1}}))return true;
        player.interruptActions();
        if(crop==null)patches[empty].compost=tier;
        else {crop.compost=tier;crop.harvestLives+=tier-previous;}
        player.startAnimation(2283);
        player.getPA().addSkillXPMultiplied((tier==1?18:tier==2?26:36)
                *io.xeros.content.skills.SkillingOutfit.FARMER.multiplier(player),Skill.FARMING.getId(),true);
        player.sendMessage("You treat the patch with compost.");return true;
    }

    public boolean itemOnItem(int first,int second) {
        if(bins.bucketUpgrade(first,second))return true;
        if(saplings.itemOnItem(first,second))return true;
        if(!((first==6034&&second==21622)||(first==21622&&second==6034)))return false;
        int quantity=Math.min(player.getItems().getInventoryCount(6034),player.getItems().getInventoryCount(21622)/2);
        if(quantity==0){player.sendMessage("You need two volcanic ashes for each bucket of supercompost.");return true;}
        // Creating ultracompost did not grant XP in the 2022 revision.
        io.xeros.content.skills.ProductionBatch.start(player,Skill.FARMING.getId(),1,0,quantity,2283,2,
                new int[][]{{6034,1},{21622,2}},new int[][]{{21483,1}},null);
        return true;
    }

	public static int getFarmingPieces(Player player) {
		int pieces = 0;
		for (int aFarmingOutfit : farmingOutfit) {
			if (player.getItems().isWearingItem(aFarmingOutfit)) {
				pieces++;
			}
		}
		return pieces;
	}
	public void handleLogin() {
		doConfig();
		player.addTickable((container, player1) -> sequence());
	}

	public void handleObjectClick(int objectId, int x, int y, int option) {
		if(bins.click(objectId,x,y,option))return;
		player.getFarming().click(player, x, y, option);
	}

	public void handleItemOnObject(int itemId, int objectId, int objectX, int objectY) {
		if(bins.use(itemId,objectId,objectX,objectY))return;

        if(itemId==5350&&canReach(objectX,objectY)){
            int empty=getGrassyPatch(objectX,objectY);
            if(empty<0||!patches[empty].isRaked()){player.sendMessage("Use the pot on an empty, weed-free patch.");return;}
            if(!player.getItems().playerHasItem(5325)){player.sendMessage("You need a gardening trowel to fill plant pots.");return;}
            io.xeros.content.skills.ProductionBatch.start(player,Skill.FARMING.getId(),1,0,
                    player.getItems().getInventoryCount(5350),832,2,new int[][]{{5350,1},{5325,0}},new int[][]{{5354,1}},null);
            return;
        }
        if(applyCompost(itemId,objectX,objectY))return;
		if (plant(itemId, objectX, objectY))
			return;
		if (useItemOnPlant(itemId, objectX, objectY))
			return;
	}

	public void regionChanged() {
		player.getFarming().doConfig();
	}

	public void sequence() {

        saplings.process();

        // Resolve flowers first so their mature state is available to neighbouring crops offline.
        for(Plant crop:plants)if(crop!=null&&Plants.values()[crop.plant].type==SeedType.FLOWER)crop.process(player);
		for (Plant i : plants) {
			if (i != null && Plants.values()[i.plant].type!=SeedType.FLOWER) {
				i.process(player);
			}
		}
		for (int i = 0; i < patches.length; i++) {
			if (i >= FarmingPatches.values().length)
				break;
			if ((patches[i] != null) && (!inhabited(FarmingPatches.values()[i].bottomLeft.getX(), FarmingPatches.values()[i].bottomLeft.getY()))) {
				patches[i].process(player, i);
			}
		}
	}

    public boolean flowerProtects(Plant target,long cycleTime) {
        Plants crop=Plants.values()[target.plant];if(crop.type!=SeedType.ALLOTMENT)return false;
        String area=target.getPatch().name().split("_")[0];
        for(Plant flower:plants){
            if(flower==null||Plants.values()[flower.plant].type!=SeedType.FLOWER||flower.dead
                    ||flower.isDiseased()||flower.stage<Plants.values()[flower.plant].stages
                    ||flower.time>cycleTime||!flower.getPatch().name().startsWith(area+"_"))continue;
            Plants kind=Plants.values()[flower.plant];
            if(kind==Plants.WHITE_LILY||(kind==Plants.MARIGOLD&&(crop==Plants.POTATO||crop==Plants.ONION||crop==Plants.TOMATO))
                    ||(kind==Plants.ROSEMARY&&crop==Plants.CABBAGE)||(kind==Plants.NASTURTIUM&&crop==Plants.WATERMELON))return true;
        }
        return false;
    }

	public int config(FarmingPatches patch) {
		if (inhabited(patch.bottomLeft.getX(), patch.bottomLeft.getY())) {
			for (Plant plant : plants) {
				if (plant != null && plant.getPatch() == patch) {
					return plant.getConfig();
				}
			}
		}
		
		return patches[patch.ordinal()].stage;
	}
	
    /** Our cache uses four shared 8-bit morph fields (varbits 4771..4774).
     * Select the closest patch for each field, including trees outside allotment areas. */
    public int clientConfiguration() {
        FarmingPatches[] nearest = new FarmingPatches[4];
        long[] distances = {Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE};
        for (FarmingPatches patch : FarmingPatches.values()) {
            int group=patch.clientGroup();
            if(group<0)continue;
            long dx=player.getX()-patch.bottomLeft.getX(),dy=player.getY()-patch.bottomLeft.getY();
            long distance=dx*dx+dy*dy;
            if(distance<distances[group]){nearest[group]=patch;distances[group]=distance;}
        }
        int value=0;
        for(int i=0;i<4;i++)if(nearest[i]!=null)value|=(config(nearest[i])&255)<<(i*8);
        return value;
    }
    public void doConfig() { player.getPA().sendConfig(529, clientConfiguration()); bins.sendConfigs(); }

    public boolean fillWateringCans(int item,int objectId,int x,int y) {
        return io.xeros.content.skills.WaterFilling.wateringCan(item)
                &&io.xeros.content.skills.WaterFilling.use(player,item,objectId,x,y);
    }

	public void clear() {
		for (int i = 0; i < plants.length; i++) {
			plants[i] = null;
		}

		for (int i = 0; i < patches.length; i++) {
			patches[i] = new GrassyPatch();
		}
	}

	public void insert(Plant patch) {
		for (int i = 0; i < plants.length; i++)
			if (plants[i] == null) {
				plants[i] = patch;
				break;
			}
	}

	public boolean inhabited(int x, int y) {
		for (int i = 0; i < plants.length; i++) {
			if (plants[i] != null) {
				FarmingPatches patch = plants[i].getPatch();
				if ((x >= patch.bottomLeft.getX()) && (y >= patch.bottomLeft.getY()) && (x <= patch.topLeft.getX()) && (y <= patch.topLeft.getY())) {
					if (isPatchException(x, y, patch)) {
						continue;
					}
					return true;
				}
			}
		}

		return false;
	}

	public int getGrassyPatch(int x, int y) {
		for (int i = 0; i < FarmingPatches.values().length; i++) {
			FarmingPatches patch = FarmingPatches.values()[i];
			if (x >= patch.bottomLeft.getX() && y >= patch.bottomLeft.getY() && x <= patch.topLeft.getX() && y <= patch.topLeft.getY()) {
				if (!isPatchException(x, y, patch)) {
					if (inhabited(x, y) || patches[i] == null)
						break;
					return i;
				}
			}
		}

		return -1;
	}

	public Plant getPlantedPatch(int x, int y) {
		for (int i = 0; i < FarmingPatches.values().length; i++) {
			FarmingPatches patch = FarmingPatches.values()[i];
			if (x >= patch.bottomLeft.getX() && y >= patch.bottomLeft.getY() && x <= patch.topLeft.getX() && y <= patch.topLeft.getY()) {
				if (!isPatchException(x, y, patch)) {
					for (Plant plant : plants) {
						if (plant != null && plant.patch == patch.ordinal()) {
							return plant;
						}
					}
				}
			}
		}

		return null;
	}

	private boolean isPatchException(int x, int y, FarmingPatches patch) {
		if(x == 3054 && y == 3307 && patch != FarmingPatches.FALADOR_FLOWER)
			return true;
		if (x == 3601 && y == 3525 && patch != FarmingPatches.PHAS_FLOWER)
			return true;
		return false;
	}

	public boolean click(Player player, int x, int y, int option) {

        if(!canReach(x,y))return false;
		int grass = getGrassyPatch(x, y);
		if (grass != -1) {
			if (option == 1) {
				patches[grass].click(player, option, grass);
			}
			return true;
		} else {
			Plant plant = getPlantedPatch(x, y);

			if (plant != null) {
				plant.click(player, option);
				return true;
			}
		}

		return false;
	}

	public void remove(Plant plant) {
		for (int i = 0; i < plants.length; i++) {
			if ((plants[i] != null) && (plants[i] == plant)) {

				patches[plants[i].getPatch().ordinal()].compost=0;
				patches[plants[i].getPatch().ordinal()].setTime();
				plants[i] = null;
				doConfig();
				return;
			}
		}
	}

	public boolean useItemOnPlant(int item, int x, int y) {

        if(!canReach(x,y))return false;
        if(applyCompost(item,x,y))return true;
		if (item == Items.RAKE) {
			int patch = getGrassyPatch(x, y);
			if (patch != -1) {
				patches[patch].rake(player, patch);
				return true;
			}
		}

		Plant plant = getPlantedPatch(x, y);
		if (plant != null) {
			plant.useItemOnPlant(player, item);
			return true;
		}

		return false;
	}

	public boolean plant(int seed, int x, int y) {

        if(!canReach(x,y))return false;
		if (!Plants.isSeed(seed) || !player.getItems().playerHasItem(seed)) {
			return false;
		}

		for (FarmingPatches patch : FarmingPatches.values()) {
			if ((x >= patch.bottomLeft.getX()) && (y >= patch.bottomLeft.getY()) && (x <= patch.topLeft.getX()) && (y <= patch.topLeft.getY())) {
				if (isPatchException(x, y, patch)) {
					continue;
				}
				if (!patches[patch.ordinal()].isRaked()) {
					player.sendMessage("This patch needs to be raked before anything can grow in it.");
					return true;
				}

				for (Plants plant : Plants.values()) {
					if (plant.seed == seed) {
						if (player.playerLevel[Skill.FARMING.getId()] >= plant.level) {
							if (inhabited(x, y)) {
								player.sendMessage("There are already seeds planted here.");
								return true;
							}

							if (patch.seedType != plant.type) {
								player.sendMessage("You can't plant this type of seed here.");
								return true;
							}

							int seedsNeeded = plant.type == SeedType.ALLOTMENT ? 3 : 1;
                            if (!player.getItems().playerHasItem(seed, seedsNeeded)) {
                                player.sendMessage("You need " + seedsNeeded + " seeds to plant this patch.");
                                return true;
                            }
                            if (player.getItems().playerHasItem(patch.planter)) {
								player.startAnimation(new Animation(2291));
								player.sendMessage("You bury the seed in the dirt.");
								if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[][]{{seed,seedsNeeded}},
                                    plant.type==SeedType.TREE||plant.type==SeedType.FRUIT_TREE?new int[][]{{5350,1}}:new int[0][]))return true;
								Plant planted = new Plant(patch.ordinal(), plant.ordinal());
								planted.compost=patches[patch.ordinal()].compost;
								planted.harvestLives=3+planted.compost;
								planted.setTime();
								insert(planted);
								if (player.getItems().playerHasItem(25025)) { // Magic watering can (doesn't exist in osrs)
									planted.watered = -1;
									planted.magicCan = true;
									player.sendMessage("Your magic watering can waters and fertilizes the soil.");
								}
								doConfig();
								if(plant.type!=SeedType.HERB)player.getPA().addSkillXPMultiplied(plant.plantExperience * io.xeros.content.skills.SkillingOutfit.FARMER.multiplier(player), Skill.FARMING.getId(), true);
							} else {
								String name = ItemDef.forId(patch.planter).getName();
								player.sendMessage("You need " + Misc.anOrA(name) + " " +name+ " to plant seeds.");
							}

						} else {
							player.sendMessage("You need a Farming level of " + plant.level + " to plant this.");
						}

						return true;
					}
				}

				return false;
			}
		}

		return false;
	}

	private String getDirectory() {
		return Server.getSaveDirectory() + "farming/";
	}

	private String getFile() {
		return getDirectory() + player.getLoginName() + ".txt";
	}

	public void save() {
		try {
			if (!new File(getDirectory()).exists()) {
				Preconditions.checkState(new File(getDirectory()).mkdirs());
			}
			java.io.StringWriter buffer = new java.io.StringWriter();
            BufferedWriter writer = new BufferedWriter(buffer);
			for (int i = 0; i < patches.length; i++) {
				if (i >= FarmingPatches.values().length)
					break;
				if (patches[i] != null) {
					writer.write("[PATCH]");
					writer.newLine();
					writer.write("patch: "+i);
					writer.newLine();
					writer.write("stage: "+patches[i].stage);
					writer.newLine();
					writer.write("compost: "+patches[i].compost);writer.newLine();
					writer.write("time: "+patches[i].time);
					writer.newLine();
					writer.write("END PATCH");
					writer.newLine();
					writer.newLine();
				}
			}
			for (int i = 0; i < plants.length; i++) {
				if (plants[i] != null) {
					writer.write("[PLANT]");
					writer.newLine();
					writer.write("patch: "+plants[i].patch);
					writer.newLine();
					writer.write("plant: "+plants[i].plant);
					writer.newLine();
					writer.write("compost: "+plants[i].compost);writer.newLine();
                    writer.write("lives: "+plants[i].harvestLives);writer.newLine();
                    writer.write("herbPlantXp: "+plants[i].herbPlantXpAwarded);writer.newLine();
                    writer.write("checked: "+plants[i].checkedHealth);writer.newLine();
                    writer.write("stumped: "+plants[i].stumped);writer.newLine();
                    writer.write("dead: "+plants[i].dead);writer.newLine();
                    writer.write("fruit: "+plants[i].fruitRemaining);writer.newLine();
                    writer.write("stage: "+plants[i].stage);
					writer.newLine();
					writer.write("disease: "+plants[i].disease);
                    writer.newLine();
                    writer.write("watered: "+plants[i].watered);
					writer.newLine();
					writer.write("harvested: "+plants[i].harvested);
					writer.newLine();
					writer.write("protected: "+plants[i].paidProtection);writer.newLine();
                    writer.write("magicCan: "+plants[i].magicCan);
					writer.newLine();
					writer.write("time: "+plants[i].time);
					writer.newLine();
					writer.write("END PLANT");
					writer.newLine();
					writer.newLine();
				}
			}
			writer.close();
            io.xeros.solo.AtomicSaveFile.write(java.nio.file.Paths.get(getFile()), buffer.toString());
		} catch (Exception e) {
			throw new IllegalStateException("Could not save Farming progress", e);
		}
	}

    public void load() {
        java.nio.file.Path file=java.nio.file.Paths.get(getFile());
        if(!java.nio.file.Files.exists(file))return;
        try(BufferedReader reader=java.nio.file.Files.newBufferedReader(file)){
            java.util.Map<String,String> block=new java.util.HashMap<>();String type="",line;
            while((line=reader.readLine())!=null){
                line=line.trim();
                if(line.equals("[PATCH]")||line.equals("[PLANT]")){type=line;block.clear();continue;}
                int colon=line.indexOf(':');
                if(colon>0){block.put(line.substring(0,colon),line.substring(colon+1).trim());continue;}
                if(!line.equals("END PATCH")&&!line.equals("END PLANT"))continue;
                try{
                    int patch=Integer.parseInt(block.getOrDefault("patch","-1"));
                    if(patch<0||patch>=FarmingPatches.values().length)continue;
                    int stage=Integer.parseInt(block.getOrDefault("stage","0"));
                    long time=Long.parseLong(block.getOrDefault("time","0"));
                    int compost=Math.max(0,Math.min(3,Integer.parseInt(block.getOrDefault("compost","0"))));
                    if(type.equals("[PATCH]")&&line.equals("END PATCH")){
                        patches[patch].stage=(byte)Math.max(0,Math.min(3,stage));
                        patches[patch].time=time;patches[patch].compost=compost;
                    }else if(type.equals("[PLANT]")&&line.equals("END PLANT")){
                        int index=Integer.parseInt(block.getOrDefault("plant","-1"));
                        if(index<0||index>=Plants.values().length||Plants.values()[index].type!=FarmingPatches.values()[patch].seedType)continue;
                        Plant plant=new Plant(patch,index);
                        plant.stage=(byte)Math.max(0,Math.min(Plants.values()[index].stages,stage));
                        plant.time=time;plant.compost=compost;
                        plant.harvested=Math.max(0,Integer.parseInt(block.getOrDefault("harvested","0")));
                        plant.harvestLives=Math.max(1,Math.min(6,Integer.parseInt(block.getOrDefault("lives",Integer.toString(3+compost)))));
                        // Older saves already received planting XP at seed time; never award it twice.
                        plant.herbPlantXpAwarded=Boolean.parseBoolean(block.getOrDefault("herbPlantXp","true"));
                        plant.checkedHealth=Boolean.parseBoolean(block.getOrDefault("checked","false"));
                        plant.stumped=Boolean.parseBoolean(block.getOrDefault("stumped","false"));
                        plant.dead=Boolean.parseBoolean(block.getOrDefault("dead","false"));
                        plant.magicCan=Boolean.parseBoolean(block.getOrDefault("magicCan","false"));
                        plant.paidProtection=Boolean.parseBoolean(block.getOrDefault("protected","false"));
                        plant.fruitRemaining=Math.max(0,Math.min(6,Integer.parseInt(block.getOrDefault("fruit","6"))));
                        plant.watered=(byte)(Integer.parseInt(block.getOrDefault("watered","0"))==-1?-1:0);
                        int disease=Integer.parseInt(block.getOrDefault("disease","-1"));
                        plant.disease=(byte)(disease>=0&&disease<plant.stage+1?disease:-1);
                        plants[patch]=plant;
                    }
                }catch(NumberFormatException malformed){
                    System.err.println("Skipped a malformed Farming save block for "+player.getLoginName());
                }finally{type="";block.clear();}
            }
        }catch(java.io.IOException failure){throw new IllegalStateException("Could not load Farming progress",failure);}
    }
}
