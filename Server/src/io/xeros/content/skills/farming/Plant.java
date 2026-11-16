package io.xeros.content.skills.farming;


import java.util.List;

import com.google.common.collect.Lists;
import io.xeros.Server;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.achievement_diary.impl.FaladorDiaryEntry;
import io.xeros.content.skills.Skill;
import io.xeros.model.Animation;
import io.xeros.model.Items;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.model.items.GameItem;
import io.xeros.model.items.ImmutableItem;
import io.xeros.util.Misc;

public class Plant {

	public int patch;
	public int plant;
	public long time = 0;
	public byte stage = 0;
	public byte disease = -1;
	public byte watered = 0;
	public boolean magicCan = false;

	public boolean dead = false;
    public boolean paidProtection;
    public boolean checkedHealth, stumped;
    public int fruitRemaining=6;
    private long lastFruitPick;
    public boolean isTree() { return getPatch().seedType==SeedType.TREE || getPatch().seedType==SeedType.FRUIT_TREE; }
    public boolean checkHealth(Player player) {
        Plants data=Plants.values()[plant];
        if(!player.getFarming().contains(this)||!player.getFarming().canReach(getPatch()))return false;
        if(!isTree() || stage<data.stages || dead || isDiseased() || checkedHealth) return false;
        checkedHealth=true;
        player.getPA().addSkillXPMultiplied(data.checkHealthExperience * io.xeros.content.skills.SkillingOutfit.FARMER.multiplier(player),Skill.FARMING.getId(),true);
        player.sendMessage("You check the health of your tree. It is healthy.");
        doConfig(player);
        return true;
    }

	public int harvested = 0;
    public int compost, harvestLives=3;
    public boolean herbPlantXpAwarded;
	private java.util.function.IntSupplier harvestRoll=()->Misc.trueRand(256);
	private java.util.function.IntSupplier diseaseRoll=()->Misc.trueRand(128);
	boolean harvesting = false;

	public Plant(int patchId, int plantId) {
		patch = patchId;
		plant = plantId;
	}

	public void water(Player player, int item) {
        if (!(item==5331||item>=5333&&item<=5340||item==25025))return;
        if (!player.getFarming().contains(this)||!player.getFarming().canReach(getPatch()) || !player.getItems().playerHasItem(item)) return;
		if (item == 5332) {
			return;
		}

		if ((getPatch().seedType != SeedType.ALLOTMENT && getPatch().seedType != SeedType.FLOWER)) {
			player.sendMessage("This patch doesn't need watering.");
			return;
		}

		if(dead || isDiseased() || stage>=Plants.values()[plant].stages)return;
		if (isWatered()) {
			player.sendMessage("Your plants have already been watered.");
			return;
		}

		if (item == 5331) {
			player.sendMessage("Your watering can is empty.");
			return;
		}

		player.sendMessage("You water the plant.");
        if (item != 25025) {
            player.getItems().deleteItem2(item, 1);
            player.getItems().addItem(item > 5333 ? item - 1 : 5331, 1);
        }
		player.startAnimation(new Animation(2293));
		watered = -1;
		doConfig(player);
	}

	public void setTime() {
		time = System.currentTimeMillis();
	}

	public void click(Player player, int option) {
        if(!player.getFarming().canReach(getPatch()))return;
		Plants plant = Plants.values()[this.plant];
		if (option == 1) {
            if(dead || stumped) {
                if(player.getItems().playerHasItem(952)) { clearWithSpade(player); }
                else player.sendMessage("You need a spade to clear this patch.");
                return;
            }
            if(isDiseased() && isTree()) {
                if(player.getItems().playerHasItem(5329)||player.getItems().playerHasItem(7409)||player.getItems().isWearingItem(7409)) { disease=-1;setTime();doConfig(player);player.sendMessage("You prune the diseased tree."); }
                else player.sendMessage("You need secateurs to prune this tree.");
                return;
            }
            if (stage >= plant.stages && isTree()) {
                if(!checkedHealth) {checkHealth(player);return;}
                if(plant.type==SeedType.TREE){io.xeros.content.skills.woodcutting.FarmTreeChopping.start(player,this);return;}
                if(fruitRemaining==0){io.xeros.content.skills.woodcutting.FarmTreeChopping.clearFruit(player,this);return;}
                if(System.currentTimeMillis()-lastFruitPick<1200)return;
                if(player.getItems().freeSlots()==0){player.sendMessage("Your inventory is full.");return;}
                if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[0][],new int[][]{{plant.harvest,1}}))return;
                lastFruitPick=System.currentTimeMillis();if(fruitRemaining==6)time=lastFruitPick;fruitRemaining--;
                player.getPA().addSkillXPMultiplied(plant.harvestExperience*io.xeros.content.skills.SkillingOutfit.FARMER.multiplier(player),Skill.FARMING.getId(),true);
                player.startAnimation(2282);doConfig(player);return;
            }
			if (stage == plant.stages) {
				harvest(player);
			} else if (plant.type == SeedType.HERB) {
				statusMessage(player, plant);
			}
		} else if ((option == 2)) {
			statusMessage(player, plant);
		}
	}

	private void statusMessage(Player player, Plants plant) {
		if (dead) {
			player.sendMessage("Oh dear, your plants have died!");
		} else if (isDiseased()) {
			player.sendMessage("Your plants are diseased!");
		} else if (stage == plant.stages) {
			player.sendMessage("Your plants are healthy and ready to harvest.");
		} else {
			int stagesLeft = plant.stages - stage;
			String s = "Your plants are healthy";
			if(!isWatered() && getPatch().seedType != SeedType.HERB)
				s += " but need some water to survive.";
			else {
				s += " and are currently growing (about " + (stagesLeft * (plant.getMinutes() / plant.stages)) + " minutes remain).";
			}
			player.sendMessage(s);
		}
	}

    public void harvest(final Player player) {
        final Plants crop=Plants.values()[plant];
        if(!player.getFarming().canReach(getPatch()) || harvesting || dead || isDiseased()
                ||stage<crop.stages||isTree())return;
        if(!player.getItems().playerHasItem(952)){
            player.sendMessage("You need a spade to harvest this patch.");return;
        }
        player.interruptActions();
        final long token=player.productionActionToken;
        final int x=player.absX,y=player.absY,h=player.heightLevel;
        harvesting=true;
        player.startAnimation(2282);
        Server.getEventHandler().submit(new io.xeros.model.cycleevent.Event<Player>("skilling",player,3){
            public void execute(){
                if(token!=player.productionActionToken||player.isDisconnected()||player.getSession()==null
                        ||player.isDead||player.getLock().cannotInteract(player)||player.absX!=x
                        ||player.absY!=y||player.heightLevel!=h||!player.isWalkingQueueEmpty()
                        ||!player.getFarming().contains(Plant.this)||!player.getFarming().canReach(getPatch())||!player.getItems().playerHasItem(952)
                        ||dead||isDiseased()||stage<crop.stages){stop();return;}
                int amount=crop.type==SeedType.FLOWER?FarmingYield.flowerAmount(crop,
                        player.playerLevel[Skill.FARMING.getId()],Misc.trueRand(Integer.MAX_VALUE)):1;
                if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[][]{{952,0}},
                        new int[][]{{crop.harvest,amount}})){stop();return;}
                double xp=crop.harvestExperience;
                if(crop.type==SeedType.HERB&&!herbPlantXpAwarded){xp+=crop.plantExperience;herbPlantXpAwarded=true;}
                player.getPA().addSkillXPMultiplied(xp*io.xeros.content.skills.SkillingOutfit.FARMER.multiplier(player),Skill.FARMING.getId(),true);
                player.startAnimation(2282);harvested+=amount;
                Achievements.increase(player,AchievementType.FARM,1);
                int petRate=player.skillingPetRateScroll?7500:10000;
                if(Misc.random(petRate)==2&&player.getItems().getItemCount(20661,false)==0&&player.petSummonId!=7352){
                    PlayerHandler.executeGlobalMessage("[<col=CC0000>News</col>] @cr20@ <col=255>"+player.getDisplayName()+"</col> found a <col=CC0000>Tangleroot</col> pet!");
                    player.getItems().addItemUnderAnyCircumstance(20661,1);
                    player.getCollectionLog().handleDrop(player,5,20661,1);
                }
                if(crop==Plants.TORSTOL&&getPatch()==FarmingPatches.FALADOR_HERB)
                    player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.HARVEST_TORSTOL);
                player.sendMessage("You harvest your "+ItemDef.forId(crop.harvest).getName()+".");
                if(crop.type==SeedType.FLOWER){player.getFarming().remove(Plant.this);stop();return;}
                int threshold=FarmingYield.saveThreshold(crop,player.playerLevel[Skill.FARMING.getId()],FarmingYield.bonus(player,crop));
                if(harvestRoll.getAsInt()>=threshold)harvestLives--;
                if(harvestLives<=0){player.getFarming().remove(Plant.this);stop();}
            }
            public void stop(){super.stop();harvesting=false;if(token==player.productionActionToken)player.startAnimation(65535);}
        });
    }

    public int rootItem() {
        switch(Plants.values()[plant]){
            case OAK_TREE:return 6043;case WILLOW_TREE:return 6045;case MAPLE_TREE:return 6047;
            case YEW_TREE:return 6049;case MAGIC_TREE:return 6051;default:return -1;
        }
    }

    public int rootAmount(int level) {
        return Math.min(4,1+Math.max(0,level-Plants.values()[plant].level)/8);
    }

    public void clearWithSpade(Player player) {
        if(!player.getFarming().canReach(getPatch())||!player.getFarming().contains(this))return;
        int root=!dead&&stumped?rootItem():-1;
        int[][] outputs=root<0?new int[0][]:new int[][]{{root,rootAmount(player.playerLevel[Skill.FARMING.getId()])}};
        io.xeros.content.skills.ProductionBatch.start(player,Skill.FARMING.getId(),1,0,1,830,3,
                new int[][]{{952,0}},outputs,()->{player.getFarming().remove(this);player.sendMessage("Você limpou o terreno.");},()->player.getFarming().contains(this)&&player.getFarming().canReach(getPatch()));
    }

	public boolean useItemOnPlant(final Player player, int item) {
        if (!player.getFarming().canReach(getPatch()) || !player.getItems().playerHasItem(item)) return false;
		if (item == 952) {
            if(isTree()&&!dead&&!stumped){player.sendMessage("Chop the tree down before digging up its roots.");return true;}
            if(!dead&&!stumped&&Plants.values()[plant].type!=SeedType.HERB&&stage<Plants.values()[plant].stages){
                player.sendMessage("This plant is still growing.");return true;
            }
            if(!dead&&!stumped){
                new io.xeros.content.dialogue.DialogueBuilder(player).option("Clear this healthy crop?",
                        new io.xeros.content.dialogue.DialogueOption("Yes, clear the patch.",p->{if(player.getItems().playerHasItem(952))clearWithSpade(player);}),
                        io.xeros.content.dialogue.DialogueBuilder.EXIT_DIALOGUE_OPTION).send();
            }else clearWithSpade(player);
			return true;
		}
		if (item == 6036) {
			if (dead) {
				player.sendMessage("Your plant is dead!");
			} else if (isDiseased()) {
				player.sendMessage("You cure the plant.");
				player.startAnimation(new Animation(2288));
				player.getItems().deleteItem(6036, 1);
				disease = -1;
                setTime();
				doConfig(player);
			} else {
				player.sendMessage("Your plant does not need this.");
			}

			return true;
		}
		if ((item >= 5331) && (item <= 5340) || item == 25025) {
			water(player, item);
			return true;
		}

		return false;
	}

    public void process(Player player) {
        if(dead)return;
        Plants data=Plants.values()[plant];
        long now=System.currentTimeMillis();
        if(time<=0 || time>now){time=now;return;}
        if(stage>=data.stages){
            if(data.type==SeedType.FRUIT_TREE && checkedHealth && !stumped && fruitRemaining<6){
                long cycles=(now-time)/(45*60_000L);
                if(cycles>0){fruitRemaining=(int)Math.min(6,fruitRemaining+cycles);time+=cycles*45*60_000L;doConfig(player);}
            }
            return;
        }
        long baseInterval=Math.max(60_000L,data.getMinutes()*60_000L/data.stages);
        int cycles=0;
        while(cycles<data.stages+1&&stage<data.stages){
            long interval=isDiseased()?Math.min(640*60_000L,baseInterval*2):baseInterval;
            if(now-time<interval)break;
            cycles++;
            time+=interval;
            if(isDiseased()){dead=true;break;}
            boolean wateredCycle=isWatered();if(!magicCan)watered=0;
            if(FarmingDisease.susceptible(data,stage)&&!magicCan&&!paidProtection&&!player.getFarming().flowerProtects(this,time)
                    &&diseaseRoll.getAsInt()<FarmingDisease.threshold(data,compost,wateredCycle)){disease=stage;continue;}
            stage++;
            if(stage>=data.stages){disease=-1;player.sendMessage("A crop you planted has finished growing.");}
        }
        if(cycles>0)doConfig(player);
    }

	public void doConfig(Player player) {
		player.getFarming().doConfig();
	}

    public int getConfig() {
        Plants data=Plants.values()[plant];
        int growth=Math.min(stage,data.stages);
        if(data.type==SeedType.TREE){
            if(stumped)return data.treeStump;
            if(dead)return data.healthy+growth+128;
            if(isDiseased())return data.healthy+growth+64;
            return data.healthy+growth+(growth==data.stages && checkedHealth?1:0);
        }
        if(data.type==SeedType.FRUIT_TREE){
            if(stumped)return data.treeStump;
            if(dead)return data.healthy+growth+18;
            if(isDiseased())return data.healthy+growth+12;
            if(growth==data.stages)return checkedHealth?data.healthy+growth+fruitRemaining:data.checkHealth;
            return data.healthy+growth;
        }
        if(data.type==SeedType.HERB){
            if(dead)return 170+Math.min(2,growth);
            if(isDiseased())return 128+Math.max(0,Math.min(13,Plants.values()[plant].seed-5291))*3+Math.max(0,growth-1);
            return data.healthy+growth;
        }
        // Snape grass reuses free slots in the original allotment morph table.
        if(data==Plants.SNAPE_GRASS){
            if(dead)return (growth<=3?192:205)+Math.max(1,growth);
            if(isDiseased())return (growth<=3?195:198)+Math.max(1,growth);
            if(growth>=data.stages)return 138;
            if(isWatered())return 128+growth;
            return 63+growth;
        }
        return data.healthy+growth+(dead?192:isDiseased()?128:isWatered() && growth<data.stages?64:0);
    }

	public FarmingPatches getPatch() {
		return FarmingPatches.values()[patch];
	}

	public boolean isDiseased() {
		return disease > -1;
	}

	public boolean isWatered() {
		return watered == -1;
	}
}
