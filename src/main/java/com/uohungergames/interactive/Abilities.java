package com.uohungergames.interactive;

public enum Abilities {

	INVISIBILITY("Invisibility", "Other players are unable to see you for 3 turns. Stops on attack."),
	FLIGHT("Flight", "Allows you to fly for 3 turns over rivers and mountains. Gives increased move range."),
	ACCURACY("Precision", "Greater accuracy with ranged weapons."),
	SPEED("Speed", "Greatly increased move range. Allows to escape battles more easily."),
	TIME("Za Warudo", "Stops time and prevents the enemy from attacking for 1 turn."),
	HEAL("Medicine", "Heals yourself."), REPAIR("Mechanic", "Able to repair weapons with decreased item cost."),
	SIGHT("Birds Eye", "Increased detection range and map range."), SUMMON("Summoner", "Summons a random item."),
	PSY("Psychic", "Increased attack range. Able to utilize surroundings as projectiles."),
	STRENGTH("Strength", "Increased attack. Able to throw weapons at an enemy."),
	HEARING("Echolocation", "Increased detection range, and last location heard will be shown on the map."),;

	String name;
	String desc;

	Abilities(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	public String toString() {
		return name;
	}

	public String getDesc() {
		return desc;
	}
}
