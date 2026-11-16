package io.xeros.content.bonus;

import java.util.Calendar;

public class DoubleExperience {

	public static boolean isDoubleExperience() {
			if (Boolean.getBoolean("solo.mode")) return false;
			return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
					|| Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
	}
}
