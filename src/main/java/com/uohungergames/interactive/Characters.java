package com.uohungergames.interactive;

import java.util.ArrayList;
import java.util.Random;

import com.uohungergames.original.Player;

public class Characters extends Player {

	private Abilities ability;
	private Player[] characterControllers = new Player[3];
	private ArrayList<String> items;

	private int map[][];
	private int[] position = new int[2];

	private int atk;
	private int def;
	private int cool;

	private Weapons wep;
	private Armor armor;

	private int district;

	private boolean encounterEnemy;
	private boolean encounterTreasure;
	private boolean encounterPlayer;

	public Characters(String url, String name, int district, Player[] characterControllers, Abilities ability) {
		super(url, name, null, null, null);

		this.ability = ability;
		this.characterControllers = characterControllers;

		this.district = district;

		items = new ArrayList<String>();

		atk = new Random().nextInt(10) + 5;
		def = new Random().nextInt(10) + 5;

		cool = 0;

		this.armor = Armor.NONE;

	}

	public int[][] getMap() {
		return map;
	}

	public void setMapPosition(int x, int y) {
		map[y][x] = 1;
	}

	public int[] getPosition() {
		return position;
	}

	public void setPosition(int x, int y) {
		position[0] = x;
		position[1] = y;
	}

	public ArrayList<String> getItemList() {
		return items;
	}

	public Player[] getCharacterControllers() {
		return characterControllers;
	}

	public Abilities getAbility() {
		return ability;
	}

	public int getAtk() {
		return atk;
	}

	public int getDef() {
		return (int) Math.round(def * armor.getDef());
	}

	public int getCool() {
		return cool;
	}

	public void setAtk(int atk) {
		this.atk = atk;
	}

	public void setDef(int def) {
		this.def = def;
	}

	public void setCool(int cool) {
		this.cool = cool;
	}

	public int getDistrcit() {
		return district;
	}

	public void setDistrict(int district) {
		this.district = district;
	}

	public boolean getEncounterEnemy() {
		return encounterEnemy;
	}

	public void setEncounterEnemy(boolean encounterEnemy) {
		this.encounterEnemy = encounterEnemy;
	}

	public boolean getEncounterTreasure() {
		return encounterTreasure;
	}

	public boolean getEncounterPlayer() {
		return encounterPlayer;
	}

	public void setEncounterTreasure(boolean encounterTreasure) {
		this.encounterTreasure = encounterTreasure;
	}

	public void initalizeMap(int mapSize) {
		this.map = new int[mapSize][mapSize];
	}

	public void setEncounterPlayer(boolean encounterPlayer) {
		this.encounterPlayer = encounterPlayer;
	}

	public void setWeapon(Weapons wep) {
		this.wep = wep;
	}

	public Weapons getWeapon() {
		return wep;
	}

	public void setArmor(Armor armor) {
		this.armor = armor;
	}

	public Armor getArmor() {
		return armor;
	}

}
