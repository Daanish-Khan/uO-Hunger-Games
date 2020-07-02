package com.uohungergames.interactive;

public enum Weapons {

	FISTS("Fists", "Bareknuckled brawl.", "Bludgeoning", 1, false),
	SWORD("Sword", "A sword forged from iron. Sharp edge!", "Slashing", 1.3, true),
	KNIFE("Knife", "Makes a mean steak.", "Piercing", 1.1, true),
	SPEAR("Spear", "Did anyone say shish kebab?", "Piercing", 1.2, true),
	SHIELD("Shield", "You can *try* to throw this.", "None", 0, false),
	BOW("Bow", "Legolas, that you?", "Piercing", 1.15, false),
	GAUNTLETS("Gauntlets", "Up close and personal.", "Bludgeoning", 1.3, false),
	ROCK("Rock", "Kinda heavy.", "Bludgeoning", 1.05, true),
	STICK("Stick", "If you wave it around enough times it might turn into a wand.", "Bludgeoning", 1.05, true),
	WAND("Wand", "Oh shit it actually turned into a wand", "Bludgeoning", 1.6, false),
	DYNAMITE("Dynamite", "If you do anything but light and throw this then you're a disgrace.", "Bludgeoning", 1.05,
			true),
	SCYTHE("Scythe", "Throwback to when Death would always die in hunger games", "Slashing", 1.3, false),
	AXE("Axe", "Can cut trees and heads.", "Slashing", 1.3, true),
	HAMMER("Hammer", "Great for bashing skulls in.", "Bludgeoning", 1.25, true),
	PISTOL("Pistol", "Holy fuck run he's got a gun", "Piercing", 2, false),
	SHURIKEN("Shuriken", "*ninja noises*", "Slashing", 1.15, true),
	WHIP("Whip", "dont make that joke dont make that joke", "Slashing", 1.3, true),;

	private String name;
	private String desc;
	private String type;

	private double atkMod;
	private boolean throwable;

	Weapons(String name, String desc, String type, double atkMod, boolean throwable) {
		this.name = name;
		this.desc = desc;
		this.type = type;
		this.atkMod = atkMod;
		this.throwable = throwable;
	}

	public String toString() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public String getType() {
		return type;
	}

	public double getAtk() {
		return atkMod;
	}

	public boolean isThrowable() {
		return throwable;
	}
}
