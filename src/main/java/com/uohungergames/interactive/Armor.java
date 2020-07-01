package com.uohungergames.interactive;

public enum Armor {

	LIGHT("Light armor", "Lightweight. Be careful of peircing weapons!", 1.2),
	MEDIUM("Medium armor",
			"Good allrounder, but weaker than the other armors. At least it doens't have a weapon weakness!", 1.1),
	HEAVY("Heavy armor", "Hard to move in, but tough. Be careful of bludgeoning weapons!", 1.3),
	NONE("No armor", "Just you and your clothes!", 1),;

	private String name;
	private String desc;

	private double defMod;

	Armor(String name, String desc, double defMod) {
		this.name = name;
		this.desc = desc;

		this.defMod = defMod;
	}

	public String toString() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public double getDef() {
		return defMod;
	}

}
