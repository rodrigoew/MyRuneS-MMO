package io.xeros.content.shooting_star;


/**
 *
 * Author C.T for koranse
 *
 *
 */

public class ShootingStarLocation {
	
	private int x;
	
	private int y;
	
	private String locationName;
	
	private String hint;
		
	public ShootingStarLocation(int x, int y, String locationName, String hint) {
		this.x = x;
		this.y = y;
		this.locationName = locationName;
		this.hint = hint;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	
	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}
}
