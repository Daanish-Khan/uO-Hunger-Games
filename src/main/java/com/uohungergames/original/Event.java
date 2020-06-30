package com.uohungergames.original;

public class Event {

	private String type;
	private boolean fatal;
	private int numTributes;
	private int[] kill;
	private String event;

	public Event(String type, boolean fatal, int numTributes, int[] kill, String event) {

		this.type = type;
		this.fatal = fatal;
		this.numTributes = numTributes;
		this.kill = kill;
		this.event = event;

	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isFatal() {
		return fatal;
	}

	public void setFatal(boolean fatal) {
		this.fatal = fatal;
	}

	public int getNumTributes() {
		return numTributes;
	}

	public void setNumTributes(int numTributes) {
		this.numTributes = numTributes;
	}

	public int[] getKill() {
		return kill;
	}

	public void setKiller(int[] kill) {
		this.kill = kill;
	}

}
