package com.uohungergames.interactive;

public class Enemy {

	private int level;

	private int hp;
	private int def;
	private int spd;
	private int atk;

	private Weapons wep;
	private Armor armor;

	private Abilities ability;

	private String[] names;
	private String name;
	private Biomes biome;

	private int chrLevel;

	private boolean hasDefended;
	private boolean isPrepared;

	public Enemy() {

		/*
		 * TO DO
		 * 
		 * 1. Randomize stats and scale based on character level 2. Add modifier based
		 * on Biome (1 per biome, 1 regular enemy that can appear anywhere) 3. Add named
		 * monsters (enemies with abilities) 5. Add weapons and armor based on biome
		 * 
		 * 6. Add names for the enemies based on biome
		 * 
		 */

	}

	public int getHP() {
		return hp;
	}

	public int getAtk() {
		if (ability == Abilities.STRENGTH)
			return (int) Math.round(atk * (1 + (0.3333 * level)));
		else
			return atk;
	}

	public int getDef() {
		return (int) Math.round(def * armor.getDef());
	}

	public Weapons getWeapon() {
		return wep;
	}

	public Abilities getAbilities() {
		return ability;
	}

	public String getName() {
		return name;
	}

	public Armor getArmor() {
		return armor;
	}

	public void setHP(int hp) {
		this.hp = hp;

	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getSpd() {
		if (ability == Abilities.SPEED)
			return (int) Math.round(spd * (1 + (0.3333 * level)));
		else
			return spd;
	}

	public void setSpd(int spd) {
		this.spd = spd;
	}

	public boolean getHasDefended() {
		return hasDefended;
	}

	public void setHasDefended(boolean hasDefended) {
		this.hasDefended = hasDefended;
	}

	public boolean getPrepared() {
		return isPrepared;
	}

	public void setPrepared(boolean isPrepared) {
		this.isPrepared = isPrepared;
	}

}
