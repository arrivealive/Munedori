package net.loglesslove.munedori;

public class Status {

	private String imageKey;
	private int x;
	private int y;
	private int duration;
	private int diffX;
	private int diffY;
	private boolean inverse = false;

	private Status partner;

	public Status() {
		this.x = 0;
		this.y = 0;
		this.duration = 0;
		this.diffX = 0;
		this.diffY = 0;
		this.partner = null;
	}

	public Status(String imageKey, int duration) {
		this.x = 0;
		this.y = 0;
		this.diffX = 0;
		this.diffY = 0;
		this.duration = duration;
		this.imageKey = imageKey;
		this.partner = null;
	}

	public Status(String imageKey, int duration, int x, int y) {
		this.x = x;
		this.y = y;
		this.diffX = 0;
		this.diffY = 0;
		this.duration = duration;
		this.imageKey = imageKey;
		this.partner = null;
	}

	public Status(String imageKey, int duration, int x, int y, boolean inverse) {
		this.x = x;
		this.y = y;
		this.diffX = 0;
		this.diffY = 0;
		this.duration = duration;
		this.imageKey = imageKey;
		this.inverse = inverse;
		this.partner = null;
	}

	public Status(String imageKey, int duration, int x, int y, Status partner) {
		this.x = x;
		this.y = y;
		this.diffX = 0;
		this.diffY = 0;
		this.duration = duration;
		this.imageKey = imageKey;
		this.partner = partner;
	}

	public void setImageKey(String key) {
		this.imageKey = key;
	}

	public String getImageKey() {
		return this.imageKey;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return this.x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return this.y;
	}

	public int getDuration() {
		return this.duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setDiffX(int x) {
		this.diffX = x;
	}

	public int getDiffX() {
		return this.diffX;
	}

	public void setDiffY(int y) {
		this.diffY = y;
	}

	public int getDiffY() {
		return this.diffY;
	}

	public boolean isInverse() {
		return this.inverse;
	}

	public void setPartner(Status partner) {
		this.partner = partner;
	}

	public Status getPartner() {
		return this.partner;
	}


}
