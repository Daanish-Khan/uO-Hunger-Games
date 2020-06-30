package com.uohungergames.original;
public class Player {

	private String url;
	private String name;
	private String user;
	private String id;
	private Event killEvent;
	private Event deathlessEvent;
	private String userString;

	private String deathEvent;

	private int numKills;

	private int hp;

	private boolean inEvent;

	public Player(String url, String name, String user, String id, String userString) {

		this.url = url;
		this.name = name;
		this.user = user;
		this.id = id;
		this.userString = userString;

		hp = 100;

		this.inEvent = false;
		this.numKills = 0;

	}

	public String getPFP() {
		return url;
	}

	public String getName() {
		return name;
	}

	public String getUser() {
		return user;
	}

	public String getID() {
		return id;
	}

	public int getHP() {
		return hp;
	}

	public void setHP(int hp) {
		this.hp = hp;
	}

	public String getDeathEvent() {
		return deathEvent;
	}

	public Event getKillEvent() {
		return killEvent;
	}

	public Event getDeathlessEvent() {
		return deathlessEvent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPFP(String url) {
		this.url = url;
	}

	public boolean isInEvent() {
		return inEvent;
	}

	public void setInEvent(boolean inEvent) {
		this.inEvent = inEvent;
	}

	public int getKills() {
		return numKills;
	}

	public void setKills(int numKills) {
		this.numKills = numKills;
	}

	public void setKillEvent(String type, boolean fatal, int numTributes, int[] kill, String event) {

		Event e = new Event(type, fatal, numTributes, kill, event);

		this.killEvent = e;

	}

	public void setDeathlessEvent(String type, boolean fatal, int numTributes, int[] kill, String event) {

		Event e = new Event(type, fatal, numTributes, kill, event);

		this.deathlessEvent = e;

	}

	public void setDeathEvent(String deathEvent) {

		this.deathEvent = deathEvent;

	}

	public boolean isSameUsername() {
		return userString.equals(name);
	}

	public boolean hasKillEvent() {
		return killEvent != null;
	}

	public boolean hasDeathlessEvent() {
		return deathlessEvent != null;
	}

}
