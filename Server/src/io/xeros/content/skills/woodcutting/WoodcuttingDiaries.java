package io.xeros.content.skills.woodcutting;
import io.xeros.content.achievement_diary.impl.*;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.content.event.eventcalendar.EventChallenge;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
/** Progress only after a resource has actually been delivered. */
public final class WoodcuttingDiaries {
 private WoodcuttingDiaries(){}
 static void progress(Player player,HistoricalTreeData.Kind kind){
  switch(kind){
   case REGULAR:progress(player,Tree.NORMAL);break;
   case ARCTIC:progress(player,Tree.ARCTIC_PINE);break;
   case ACHEY:case HOLLOW:break;
   default:progress(player,Tree.valueOf(kind.name()));break;
  }
 }
	static void progress(Player player,Tree tree) {
		switch (tree) {

			case MAGIC:
				player.getEventCalendar().progress(EventChallenge.CUT_DOWN_X_MAGIC_LOGS, 2);
				if (Boundary.isIn(player, Boundary.AL_KHARID_BOUNDARY)) {
					player.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.CHOP_MAGIC_AL);
				}
				if (Boundary.isIn(player, Boundary.RESOURCE_AREA_BOUNDARY)) {
					player.getDiaryManager().getWildernessDiary().progress(WildernessDiaryEntry.MAGIC_LOG_WILD);
				}
				if (Boundary.isIn(player, Boundary.SEERS_BOUNDARY)) {
					player.getDiaryManager().getKandarinDiary().progress(KandarinDiaryEntry.CUT_MAGIC_SEERS);
				}
				DailyTasks.increase(player, DailyTasks.PossibleTasks.MAGIC_LOGS);//Daily tasks
				break;
			case MAPLE:
				break;
			case NORMAL:
				break;
			case OAK:
				if (Boundary.isIn(player, Boundary.LUMRIDGE_BOUNDARY) || Boundary.isIn(player, Boundary.DRAYNOR_BOUNDARY)) {
					player.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.CHOP_OAK);
				}
				if (Boundary.isIn(player, Boundary.RELLEKKA_BOUNDARY)) {
					player.getDiaryManager().getFremennikDiary().progress(FremennikDiaryEntry.CHOP_OAK_FREM);
				}
				break;
			case WILLOW:
				if (Boundary.isIn(player, Boundary.FALADOR_BOUNDARY)) {
					player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.CHOP_WILLOW);
				}
				if (Boundary.isIn(player, Boundary.DRAYNOR_BOUNDARY)) {
					player.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.CHOP_WILLOW_DRAY);
				}
				break;
			case YEW:
				if (Boundary.isIn(player, Boundary.FALADOR_BOUNDARY)) {
					player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.CHOP_YEW);
				}
				if (Boundary.isIn(player, Boundary.VARROCK_BOUNDARY)) {
					player.getDiaryManager().getVarrockDiary().progress(VarrockDiaryEntry.YEWS_AND_BURN);
				}
				DailyTasks.increase(player, DailyTasks.PossibleTasks.YEW_LOGS);//Daily tasks
				break;
			case TEAK:
				if (Boundary.isIn(player, Boundary.DESERT_BOUNDARY)) {
					player.getDiaryManager().getDesertDiary().progress(DesertDiaryEntry.CHOP_TEAK);
				}
				break;
			default:
				break;

		}
	}
}
