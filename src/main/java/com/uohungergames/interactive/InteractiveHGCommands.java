package com.uohungergames.interactive;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;

import javax.imageio.ImageIO;

import com.uohungergames.Commands;
import com.uohungergames.original.Player;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InteractiveHGCommands extends Commands {

	private boolean start = false;
	private int numGames = 0;
	private ArrayList<Characters> characterList = new ArrayList<Characters>();
	private Biomes[][] biomeMap;
	private int[][] treasureMap;
	private int[][] enemyMap;
	private Characters[][] characterMap;

	private static final double PREPARE_MOD = 1.4;
	private static final double COUNTER_MOD = 1.2;
	private static final double DEFEND_MOD = 1.5;

	private static final double ARMOR_WEAK_MOD = 1.3;
	private static final double ARMOR_STRONG_MOD = 0.7;

	public void test(MessageReceivedEvent event) {

		createCharacterList();
		createLockedChannels(event);
		generateMap(500);
		viewMap(event);

	}

	// Character Creation

	private void createCharacterList() {

		String[] pfpList = new String[] {

				"https://bit.ly/2yMdjz3", "https://bit.ly/2yRLsxD", "https://bit.ly/2UZXUnG", "https://bit.ly/2XsqOOW",
				"https://bit.ly/3eaG60w", "https://bit.ly/3ckrOZJ", "https://bit.ly/2RpuNry", "https://bit.ly/34sjwMp",
				"https://bit.ly/2RtVE5K", "https://bit.ly/3e8CIDo",

		};

		Abilities[] abilityList = Abilities.values();

		for (int i = 0; i < playerList.size(); i++) {

			if (i % 3 == 0) {

				Abilities ability = abilityList[new Random().nextInt(abilityList.length)];

				Player[] characterControllers = new Player[] { playerList.get(i), playerList.get(i + 1),
						playerList.get(i + 2) };

				Characters c = new Characters(pfpList[characterList.size()], "Character " + (characterList.size() + 1),
						(int) Math.ceil(i / 3.0), characterControllers, ability);

				characterList.add(c);

			}

		}

	}

	public void editCharacter(MessageReceivedEvent event) {

		Message message = event.getMessage();
		String content = message.getContentRaw();

		int characterInt = Integer.parseInt(event.getChannel().getName().split("-")[1]) - 1;

		ArrayList<String> list = new ArrayList<String>(Arrays.asList(content.split(" ")));
		list.remove(0);

		if (list.size() > 2 || list.size() < 1) {

			sendMessage(event, ":x: Incorrect format! Correct usage: `hg!edit [url|name]`");
			return;

		}

		if (!imgExist(list.get(0))) {

			// Image doesn't resolve, so the entry must have been a name
			if (list.size() < 2) {

				list.add(0, null);

			} else {

				// If second entry is a url, first entry becomes name
				if (imgExist(list.get(1))) {

					Collections.swap(list, 0, 1);

				} else {

					// Both entries are not url's, throws error
					sendMessage(event, ":x: Edit failed, cannot resolve link!");
					return;
				}

			}

		} else {

			// If image exists, the entry must have been a url
			if (list.size() < 2) {

				list.add(null);

			} else if (imgExist(list.get(1))) {

				// If both are urls, the name is defaulted
				sendMessage(event, ":x: Edit failed, both params can't be links!");
				return;

			}

		}

		// Looks for player in list and edits it

		Player p = characterList.get(characterInt);

		if (list.get(0) != null)
			p.setPFP(list.get(0));
		if (list.get(1) != null)
			p.setName(list.get(1));

		sendMessage(event, "Edit successful! Type `hg!character` to view your character.");
		return;

	}

	public void viewCharacter(MessageChannel c) {

		int characterInt = Integer.parseInt(c.getName().split("-")[1]) - 1;
		Characters character = characterList.get(characterInt);

		EmbedBuilder eb = new EmbedBuilder();

		eb.setAuthor("Team " + (characterInt + 1) + "'s Character", null);

		eb.setThumbnail(character.getPFP());

		eb.setTitle(character.getName());

		eb.setColor(Color.CYAN);

		eb.addField("Level: ", String.valueOf(character.getLevel()), true);
		eb.addField("Items: ", character.getItemList().toString(), true);

		eb.addField("Ability: ", character.getAbility().toString(), true);
		eb.addField("Ability Desc: ", character.getAbility().getDesc(), true);

		ByteArrayOutputStream s = new ByteArrayOutputStream();

		try {
			ImageIO.write(generateStatsImg(character), "png", s);
		} catch (IOException e) {
			e.printStackTrace();
		}

		eb.setImage("attachment://stats.png");

		c.sendFile(s.toByteArray(), "stats.png").embed(eb.build()).queue();

	}

	public void viewCharacterMap(MessageReceivedEvent event) {

		int characterInt = Integer.parseInt(event.getChannel().getName().split("-")[1]) - 1;
		Characters character = characterList.get(characterInt);

		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.cyan);

		ByteArrayOutputStream s = new ByteArrayOutputStream();

		try {
			ImageIO.write(generateCharacterMap(character), "png", s);
		} catch (IOException e) {
			e.printStackTrace();
		}

		eb.setImage("attachment://charMap.png");

		event.getChannel().sendFile(s.toByteArray(), "charMap.png").embed(eb.build()).queue();

	}

	public void viewMap(MessageReceivedEvent event) {

		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.cyan);

		ByteArrayOutputStream s = new ByteArrayOutputStream();

		try {
			ImageIO.write(generateMapImage(false), "png", s);
		} catch (IOException e) {
			e.printStackTrace();
		}

		eb.setImage("attachment://map.png");

		event.getChannel().sendFile(s.toByteArray(), "map.png").embed(eb.build()).queue();

	}

	private BufferedImage generateStatsImg(Characters c) {

		try {

			// Set bar vars
			int arcWidth = 20;
			int arcHeight = 20;
			int width = 300;

			// Get player stats
			int hp = c.getHP();
			int atk = c.getAtk();
			int def = c.getDef();
			int spd = c.getSpd();
			int cool = c.getCool();

			// Set font
			Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("ARCADECLASSIC.TTF"));
			int textHeight = 19;

			// Set colors
			Color emptyBar = Color.decode("#494C54");
			Color fillBar = Color.decode("#353E54");
			Color text = Color.WHITE;
			Color coolBar = Color.decode("#DBD68A");
			Color coolText = Color.decode("#332464");

			// Read image
			BufferedImage blankStats = ImageIO.read(getClass().getResourceAsStream("stats.png"));
			BufferedImage modStats = new BufferedImage(blankStats.getWidth(), blankStats.getHeight(),
					BufferedImage.TYPE_INT_ARGB);

			// Create drawable layer
			Graphics2D g2d = modStats.createGraphics();
			g2d.drawImage(blankStats, 0, 0, null);

			g2d.setFont(f.deriveFont(Font.PLAIN, 25));

			// Draw empty bars
			g2d.setColor(emptyBar);
			g2d.fillRoundRect(116, 6, width, 21, arcWidth, arcHeight); // hp
			g2d.fillRoundRect(116, 48, width, 21, arcWidth, arcHeight); // atk
			g2d.fillRoundRect(116, 90, width, 21, arcWidth, arcHeight); // def
			g2d.fillRoundRect(116, 132, width, 21, arcWidth, arcHeight); // spd
			g2d.fillRoundRect(116, 174, width, 21, arcWidth, arcHeight); // cooldown

			// Draw filled bars
			g2d.setColor(fillBar);
			g2d.fillRoundRect(116, 6, barLength(hp), 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 48, barLength(atk), 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 90, barLength(def), 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 132, barLength(spd), 21, arcWidth, arcHeight);

			g2d.setColor(coolBar);
			g2d.fillRoundRect(116, 174, barLength(cool), 21, arcWidth, arcHeight);

			// Draw stat numbers
			g2d.setColor(text);
			g2d.drawString(String.valueOf(hp), fontPlacement(g2d, hp), 6 + textHeight);
			g2d.drawString(String.valueOf(atk), fontPlacement(g2d, atk), 48 + textHeight);
			g2d.drawString(String.valueOf(def), fontPlacement(g2d, def), 90 + textHeight);
			g2d.drawString(String.valueOf(spd), fontPlacement(g2d, spd), 132 + textHeight);

			g2d.setColor(coolText);
			g2d.drawString(String.valueOf(cool), fontPlacement(g2d, cool), 174 + textHeight);

			g2d.dispose();

			return modStats;

		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}

		return null;

	}

	// Channel Creation
	public void createLockedChannels(MessageReceivedEvent event) {

		Category c;

		event.getGuild().createCategory("Interactive HG").queue();

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		c = event.getGuild().getCategoriesByName("Interactive HG", true).get(0);

		for (int i = 0; i < characterList.size(); i++) {

			c.createTextChannel("Team " + (i + 1))
					.addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
					.queue();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (Player p : characterList.get(i).getCharacterControllers()) {
				try {
					c.getTextChannels().get(i).createPermissionOverride(event.getGuild().getMemberById(p.getID()))
							.setAllow(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
				} catch (IllegalStateException e) {

					System.out.println("Player " + p.getName() + " already has perms");

				}
			}

			c.getTextChannels().get(i)
					.sendMessage(characterList.get(i).getCharacterControllers()[0].getUser() + ", "
							+ characterList.get(i).getCharacterControllers()[1].getUser() + ", "
							+ characterList.get(i).getCharacterControllers()[2].getUser()
							+ ", this is your base of operations! You character is displayed below:")
					.queue();

			viewCharacter(c.getTextChannels().get(i));

			c.getTextChannels().get(i).sendMessage("Type `hg!edit [url|name]` to edit your character!").queue();

		}

	}

	public void deleteLockedChannels(MessageReceivedEvent event) {

		if (event.getGuild().getCategoriesByName("Interactive HG", true).size() == 0)
			return;

		Category c = event.getGuild().getCategoriesByName("Interactive HG", true).get(0);

		for (TextChannel tc : c.getTextChannels())
			tc.delete().queue();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		c.delete().queue();

	}

	// Map Generation

	private void generateMap(int size) {

		// Calculate size
		int mapSize = (size / 10);

		// Init maps
		biomeMap = new Biomes[mapSize][mapSize];
		treasureMap = new int[mapSize][mapSize];
		enemyMap = new int[mapSize][mapSize];

		dropPlayers(mapSize);

		// Generate seeds
		int riverSeed = (int) (Math.random() * Math.pow(10, 9));
		int biomeSeed = (int) (Math.random() * Math.pow(10, 9));

		// Initialize Noise
		FastNoise rivers = new FastNoise();
		FastNoise biomes = new FastNoise();

		// Noise settings
		rivers.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
		rivers.SetFractalType(FastNoise.FractalType.RigidMulti);
		rivers.SetFrequency((float) 0.002);
		rivers.SetFractalLacunarity((float) 0.1);
		rivers.SetFractalOctaves(5);
		rivers.SetFractalGain((float) 0.3);
		rivers.SetSeed(riverSeed);

		biomes.SetNoiseType(FastNoise.NoiseType.Cellular);
		biomes.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Manhattan);
		biomes.SetFrequency((float) 0.01);
		biomes.SetSeed(biomeSeed);

		// Generate Map
		for (int x = 0; x < size; x += 10)
			for (int y = 0; y < size; y += 10) {

				// Calculate map position
				int w = x / 10;
				int z = y / 10;

				// Generate noise
				float riverNoise = rivers.GetNoise(x, y);
				float biomeNoise = biomes.GetNoise(x, y);

				// Generate random number
				double enemyChance = Math.random();
				double treasureChance = Math.random();

				// Biome Generation
				if (riverNoise >= 0.55)
					biomeMap[w][z] = Biomes.WATER;
				else if (riverNoise > 0.45 && riverNoise < 0.55)
					biomeMap[w][z] = Biomes.BEACH;
				else {

					if (biomeNoise >= 0.5)
						biomeMap[w][z] = Biomes.MOUNTAINS;
					else if (biomeNoise > 0 && biomeNoise < 0.5)
						biomeMap[w][z] = Biomes.FOREST;
					else
						biomeMap[w][z] = Biomes.PLAINS;

				}

				// Enemy and treasure generation
				if (enemyChance < 0.15)
					enemyMap[w][z] = 1;
				if (treasureChance < 0.01)
					treasureMap[w][z] = 1;

			}

	}

	public BufferedImage generateCharacterMap(Characters c) {

		int[][] map = c.getMap();

		// Create Image
		BufferedImage mapImg = new BufferedImage(biomeMap.length * 100, biomeMap.length * 100,
				BufferedImage.TYPE_INT_RGB);

		int rgb = 0x008000;

		// Iterate through map
		for (int x = 0; x < biomeMap.length * 100; x += 10)
			for (int y = 0; y < biomeMap.length * 100; y += 10) {

				int w = x / 100;
				int z = y / 100;

				// Set tile color
				if (map[z][w] == 1) {

					if (biomeMap[z][w] == Biomes.WATER)
						rgb = 0x0000FF;
					else if (biomeMap[z][w] == Biomes.BEACH)
						rgb = 0xD2691E;
					else if (biomeMap[z][w] == Biomes.MOUNTAINS)
						rgb = 0x808080;
					else if (biomeMap[z][w] == Biomes.FOREST)
						rgb = 0x006400;
					else
						rgb = 0x008000;

					if (w == c.getPosition()[0] && z == c.getPosition()[1])
						rgb = 0xff0000;

				} else
					rgb = 0xA9A9A9;

				// Draw color
				for (int j = 0; j < 10; j++)
					for (int k = 0; k < 10; k++)
						mapImg.setRGB(x + j, y + k, rgb);

			}

		return mapImg;

	}

	private BufferedImage generateMapImage(boolean onlyBiome) {

		// Create Image
		BufferedImage mapImg = new BufferedImage(biomeMap.length * 100, biomeMap.length * 100,
				BufferedImage.TYPE_INT_RGB);

		int rgb = 0x008000;

		// Iterate through map
		for (int x = 0; x < biomeMap.length * 100; x += 10)
			for (int y = 0; y < biomeMap.length * 100; y += 10) {

				int w = x / 100;
				int z = y / 100;

				// Set tile color
				if (!onlyBiome) {
					if (enemyMap[z][w] == 1 && treasureMap[z][w] == 1)
						rgb = 0x9400D3;
					else if (enemyMap[z][w] == 1)
						rgb = 0xFF0000;
					else if (treasureMap[z][w] == 1)
						rgb = 0xFFFF00;
					else {

						if (biomeMap[z][w] == Biomes.WATER)
							rgb = 0x0000FF;
						else if (biomeMap[z][w] == Biomes.BEACH)
							rgb = 0xD2691E;
						else if (biomeMap[z][w] == Biomes.MOUNTAINS)
							rgb = 0x808080;
						else if (biomeMap[z][w] == Biomes.FOREST)
							rgb = 0x006400;
						else
							rgb = 0x008000;

					}

				} else {

					if (biomeMap[z][w] == Biomes.WATER)
						rgb = 0x0000FF;
					else if (biomeMap[z][w] == Biomes.BEACH)
						rgb = 0xD2691E;
					else if (biomeMap[z][w] == Biomes.MOUNTAINS)
						rgb = 0x808080;
					else if (biomeMap[z][w] == Biomes.FOREST)
						rgb = 0x006400;
					else
						rgb = 0x008000;

				}

				// Draw color
				for (int j = 0; j < 10; j++)
					for (int k = 0; k < 10; k++)
						mapImg.setRGB(x + j, y + k, rgb);

			}

		return mapImg;

	}

	private void dropPlayers(int mapSize) {

		characterMap = new Characters[mapSize][mapSize];

		int randx;
		int randy;

		for (Characters c : characterList) {

			// Initialize respective maps
			c.initalizeMap(mapSize);

			// Choose player position
			do {
				randx = new Random().nextInt(mapSize);
				randy = new Random().nextInt(mapSize);
			} while (characterMap[randy][randx] != null);

			c.setPosition(randx, randy);
			characterMap[randy][randx] = c;

			for (int x = 0; x < 5; x++)
				for (int y = 0; y < 5; y++) {

					int posx = (randx - 2) + x;
					int posy = (randy - 2) + y;

					if (posx < 0)
						posx = 0;
					else if (posx > mapSize - 1)
						posx = mapSize - 1;

					if (posy < 0)
						posy = 0;
					else if (posy > mapSize - 1)
						posy = mapSize - 1;

					c.setMapPosition(posx, posy);

				}

		}

	}

	public void move(Characters c, int spaces, String dir) {

		int mapSize = c.getMap()[0].length;
		int currentPosition[] = c.getPosition();
		int nextPos[] = new int[2];

		/*
		 * 
		 * [0, 1, 2, 3] [4, 5, 6, 7] [8, 9, 10, 11] [12, 13, 14, 15]
		 */

		for (int i = 0; i < spaces; i++) {

			// Move in respective direction
			switch (dir) {

			case "N":
				nextPos[0] = currentPosition[1] - 1 <= 0 ? 0 : currentPosition[1] - 1; // y
				nextPos[1] = currentPosition[0]; // x
				break;
			case "E":
				nextPos[0] = currentPosition[1]; // y
				nextPos[1] = currentPosition[0] + 1 >= 49 ? 49 : currentPosition[0] + 1; // x
				break;
			case "S":
				nextPos[0] = currentPosition[1] + 1 >= 49 ? 49 : currentPosition[1] + 1; // y
				nextPos[1] = currentPosition[0]; // x
				break;
			case "W":
				nextPos[0] = currentPosition[1]; // y
				nextPos[1] = currentPosition[0] - 1 <= 0 ? 0 : currentPosition[0] - 1; // x
				break;

			}

			// Check if on the way, the player encounters an enemy, player, or loot box
			c.setEncounterEnemy(checkEncounterEnemy(nextPos, c));
			c.setEncounterTreasure(checkEncounterTreasure(nextPos, c));
			c.setEncounterPlayer(checkEncounterPlayer(nextPos, c));

			// Stop movement if player will occupy the same space
			if (characterMap[nextPos[0]][nextPos[1]] != null)
				break;

			c.setPosition(nextPos[1], nextPos[0]);

			// Update viewdistance
			for (int x = 0; x < 5; x++)
				for (int y = 0; y < 5; y++) {

					int posx = (nextPos[1] - 2) + x;
					int posy = (nextPos[0] - 2) + y;

					if (posx < 0)
						posx = 0;
					else if (posx > mapSize - 1)
						posx = mapSize - 1;

					if (posy < 0)
						posy = 0;
					else if (posy > mapSize - 1)
						posy = mapSize - 1;

					c.setMapPosition(posx, posy);

				}

		}

	}

	private boolean checkEncounterPlayer(int[] nextPos, Characters c) {

		int mapSize = c.getMap()[0].length;

		// Check if there is player in character viewdistance
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++) {

				int posx = (nextPos[1] - 2) + x;
				int posy = (nextPos[0] - 2) + y;

				if (posx < 0)
					posx = 0;
				else if (posx > mapSize - 1)
					posx = mapSize - 1;

				if (posy < 0)
					posy = 0;
				else if (posy > mapSize - 1)
					posy = mapSize - 1;

				if (characterMap[posy][posx] != null) {
					if (characterMap[posy][posx].isInAbility() != true
							|| characterMap[posy][posx].getAbility() != Abilities.INVISIBILITY)
						return true;
				}

			}

		return false;

	}

	public void battleStep(Characters c1, Characters c2, String choice1, String choice2) {

		// Get weapons
		Weapons c1wep = c1.getWeapon();
		Weapons c2wep = c2.getWeapon();

		// Attack prepare
		double c1atk = (c1.getPrepared() && choice1.equals("attack")) ? c1.getAtk() * PREPARE_MOD : c1.getAtk();

		// Calculate damage if player defended
		c1atk = (c1.getHasDefended() && choice1.equals("attack")) ? c1atk * COUNTER_MOD : c1atk;
		c1atk = c1atk * c1wep.getAtk();

		// Defense prepare
		double c1def = (c1.getPrepared() && choice1.equals("defend")) ? c1.getDef() * PREPARE_MOD : c1.getDef();

		// Defense fail
		c1def = (c1.getHasDefended() && choice1.equals("defend")) ? 0 : c1def;
		c1def = (choice1.equals("defend")) ? c1def * DEFEND_MOD : c1def; // Defense modifier

		// Attack prepare
		double c2atk = (c2.getPrepared() && choice2.equals("attack")) ? c2.getAtk() * PREPARE_MOD : c2.getAtk();

		// Defend damage
		c2atk = (c2.getHasDefended() && choice2.equals("attack")) ? c2atk * COUNTER_MOD : c2atk;
		c2atk = c2atk * c2wep.getAtk();

		// Defense prepare
		double c2def = (c2.getPrepared() && choice2.equals("defend")) ? c2.getDef() * PREPARE_MOD : c2.getDef();

		// Defense fail
		c2def = (c2.getHasDefended() && choice2.equals("defend")) ? 0 : c2def;
		c2def = (choice2.equals("defend")) ? c2def * DEFEND_MOD : c2def; // Defense modifier

		// Calculate damage if either attacks
		double c1damage = c2atk - c1def;
		double c2damage = c1atk - c2def;

		// Calculate damage bonus with armor
		if (c2wep.getType().equals("Piercing"))
			c1damage *= (c1.getArmor() == Armor.LIGHT) ? ARMOR_WEAK_MOD
					: (c1.getArmor() == Armor.HEAVY) ? ARMOR_STRONG_MOD : 1;
		else if (c2wep.getType().equals("Bludgeoning"))
			c1damage *= (c1.getArmor() == Armor.LIGHT) ? ARMOR_STRONG_MOD
					: (c1.getArmor() == Armor.HEAVY) ? ARMOR_WEAK_MOD : 1;

		if (c1wep.getType().equals("Piercing"))
			c2damage *= (c2.getArmor() == Armor.LIGHT) ? ARMOR_WEAK_MOD
					: (c2.getArmor() == Armor.HEAVY) ? ARMOR_STRONG_MOD : 1;
		else if (c1wep.getType().equals("Bludgeoning"))
			c2damage *= (c2.getArmor() == Armor.LIGHT) ? ARMOR_STRONG_MOD
					: (c2.getArmor() == Armor.HEAVY) ? ARMOR_WEAK_MOD : 1;

		switch (choice1) {

		case "attack":

			c1.setHasDefended(false);
			c1.setPrepared(false);

			if (choice2.equals("attack")) {

				c2.setHasDefended(false);
				c2.setPrepared(false);

				// Compare speeds and apply damage to the slower one
				if (c1.getSpd() > c2.getSpd()) {

					c2.setHP((int) Math.round(c2.getHP() - c2damage < 0 ? 0 : c2.getHP() - c2damage));

					if (c2.getHP() == 0)
						break;
					else
						c1.setHP((int) Math.round(c1.getHP() - c1damage < 0 ? 0 : c1.getHP() - c1damage));

				} else if (c2.getSpd() > c1.getSpd()) {

					c1.setHP((int) Math.round(c1.getHP() - c1damage < 0 ? 0 : c1.getHP() - c1damage));

					if (c1.getHP() == 0)
						break;
					else
						c2.setHP((int) Math.round(c1.getHP() - c2damage < 0 ? 0 : c1.getHP() - c2damage));

				} else {

					c1.setHP((int) Math.round(c1.getHP() - c1damage < 0 ? 0 : c1.getHP() - c1damage));
					c2.setHP((int) Math.round(c1.getHP() - c2damage < 0 ? 0 : c1.getHP() - c2damage));

				}

			} else if (choice2.equals("defend")) {

				c2.setHasDefended(true);
				c2.setPrepared(false);
				c2.setHP((int) Math.round(c1.getHP() - c2damage < 0 ? 0 : c1.getHP() - c2damage));

			} else if (choice2.equals("prepare")) {

				c2.setPrepared(true);
				c2.setHasDefended(false);
				c2.setHP((int) Math.round(c1.getHP() - c2damage < 0 ? 0 : c1.getHP() - c2damage));

			}

			break;

		case "defend":

			c1.setPrepared(false);

			if (choice2.equals("attack")) {

				c1.setHasDefended(true);

				c2.setHasDefended(false);
				c2.setPrepared(false);

				c1.setHP((int) Math.round(c1.getHP() - c1damage < 0 ? 0 : c1.getHP() - c1damage));

			} else if (choice2.equals("prepare")) {

				c1.setHasDefended(false);

				c2.setHasDefended(false);
				c2.setPrepared(true);

			}

			break;

		case "prepare":

			c1.setHasDefended(false);
			c1.setPrepared(true);

			if (choice2.equals("attack")) {

				c2.setHasDefended(false);
				c2.setPrepared(false);

				c1.setHP((int) Math.round(c1.getHP() - c1damage < 0 ? 0 : c1.getHP() - c1damage));

			} else if (choice2.equals("prepare")) {

				c2.setHasDefended(false);
				c2.setPrepared(true);

			}

			break;

		}

		// ---------------------- ADD ABILITIES ----------------------

	}

	public void battleStep(Characters c, Enemy e, String choice) {

		int randNum = new Random().nextInt(101);
		int enemyChoice;

		// Calculate likelyhood of attacking/defending
		double initialAttack = 72 - 0.2 * c.getHP();
		double initialDefend = 90 - initialAttack;

		int finalDefend = (int) Math.round((108 - initialAttack) - (0.2 * e.getHP()));
		int finalAttack = (int) Math.round(initialAttack - (finalDefend - initialDefend));

		if (e.getPrepared())
			randNum += 10;

		// Choose enemy action based on hp levels
		if ((randNum -= 10) < 0) {
			enemyChoice = 2;
		} else if ((randNum -= Math.min(finalDefend, finalAttack)) < 0) {

			if (Math.min(finalDefend, finalAttack) == finalAttack)
				enemyChoice = 0;
			else
				enemyChoice = 1;

		} else {

			if (Math.max(finalDefend, finalAttack) == finalAttack)
				enemyChoice = 0;
			else
				enemyChoice = 1;

		}

		// Get weapons
		Weapons cWep = c.getWeapon();
		Weapons eWep = e.getWeapon();

		// Calculate damage is player prepared last turn
		double cAtk = (c.getPrepared() && choice.equals("attack")) ? c.getAtk() * PREPARE_MOD : c.getAtk();

		// Calculate damage if player defended last turn
		cAtk = (c.getHasDefended() && choice.equals("attack")) ? cAtk * COUNTER_MOD : cAtk;
		cAtk = cAtk * cWep.getAtk();

		// Calculate defense if player prepared last turn
		double cDef = (c.getPrepared() && choice.equals("defend")) ? c.getDef() * PREPARE_MOD : c.getDef();

		// If player already defended last turn, defense fails
		cDef = (c.getHasDefended() && choice.equals("defend")) ? 0 : cDef;
		cDef = (choice.equals("defend")) ? cDef * DEFEND_MOD : cDef; // Defense modifier

		// Calculate damage if enemy prepared last turn
		double eAtk = (e.getPrepared() && enemyChoice == 0) ? e.getAtk() * PREPARE_MOD : e.getAtk();

		// Calculate damage if enemy prepared last turn
		eAtk = (e.getHasDefended() && enemyChoice == 0) ? eAtk * COUNTER_MOD : eAtk;
		eAtk = eAtk * eWep.getAtk();

		// Calulcate defense if enemy prepared last turn
		double eDef = (e.getPrepared() && enemyChoice == 1) ? e.getDef() * PREPARE_MOD : e.getDef();

		// Defense fail check
		eDef = (e.getHasDefended() && enemyChoice == 1) ? 0 : eDef;
		eDef = (enemyChoice == 1) ? eDef * DEFEND_MOD : eDef; // Defense modifier

		// Calculate damage if either attacks
		double cdamage = eAtk - cDef;
		double edamage = cAtk - eDef;

		// Calculate damage bonus with armor
		if (eWep.getType().equals("Piercing"))
			cdamage *= (c.getArmor() == Armor.LIGHT) ? ARMOR_WEAK_MOD
					: (c.getArmor() == Armor.HEAVY) ? ARMOR_STRONG_MOD : 1;
		else if (eWep.getType().equals("Bludgeoning"))
			cdamage *= (c.getArmor() == Armor.LIGHT) ? ARMOR_STRONG_MOD
					: (c.getArmor() == Armor.HEAVY) ? ARMOR_WEAK_MOD : 1;

		if (cWep.getType().equals("Piercing"))
			edamage *= (e.getArmor() == Armor.LIGHT) ? ARMOR_WEAK_MOD
					: (e.getArmor() == Armor.HEAVY) ? ARMOR_STRONG_MOD : 1;
		else if (cWep.getType().equals("Bludgeoning"))
			edamage *= (e.getArmor() == Armor.LIGHT) ? ARMOR_STRONG_MOD
					: (e.getArmor() == Armor.HEAVY) ? ARMOR_WEAK_MOD : 1;

		switch (choice) {

		case "attack":

			c.setHasDefended(false);
			c.setPrepared(false);

			if (enemyChoice == 0) {

				e.setHasDefended(false);
				e.setPrepared(false);

				// Compares speed and subtracts health from whomever is faster
				if (c.getSpd() > e.getSpd()) {

					e.setHP((int) Math.round(e.getHP() - edamage < 0 ? 0 : e.getHP() - edamage));

					if (e.getHP() == 0)
						break;
					else
						c.setHP((int) Math.round(c.getHP() - cdamage < 0 ? 0 : c.getHP() - cdamage));

				} else if (e.getSpd() > c.getSpd()) {

					c.setHP((int) Math.round(c.getHP() - cdamage < 0 ? 0 : c.getHP() - cdamage));

					if (c.getHP() == 0)
						break;
					else
						e.setHP((int) Math.round(c.getHP() - edamage < 0 ? 0 : c.getHP() - edamage));

				} else {

					c.setHP((int) Math.round(c.getHP() - cdamage < 0 ? 0 : c.getHP() - cdamage));
					e.setHP((int) Math.round(c.getHP() - edamage < 0 ? 0 : c.getHP() - edamage));

				}

			} else if (enemyChoice == 1) {

				e.setHasDefended(true);
				e.setPrepared(false);
				e.setHP((int) Math.round(c.getHP() - edamage < 0 ? 0 : c.getHP() - edamage));

			} else if (enemyChoice == 2) {

				e.setPrepared(true);
				e.setHasDefended(false);
				e.setHP((int) Math.round(c.getHP() - edamage < 0 ? 0 : c.getHP() - edamage));

			}

			break;

		case "defend":

			c.setPrepared(false);

			if (enemyChoice == 0) {

				c.setHasDefended(true);

				e.setHasDefended(false);
				e.setPrepared(false);

				c.setHP((int) Math.round(c.getHP() - cdamage < 0 ? 0 : c.getHP() - cdamage));

			} else if (enemyChoice == 2) {

				c.setHasDefended(false);

				e.setHasDefended(false);
				e.setPrepared(true);

			}

			break;

		case "prepare":

			c.setHasDefended(false);
			c.setPrepared(true);

			if (enemyChoice == 0) {

				e.setHasDefended(false);
				e.setPrepared(false);

				c.setHP((int) Math.round(c.getHP() - cdamage < 0 ? 0 : c.getHP() - cdamage));

			} else if (enemyChoice == 2) {

				e.setHasDefended(false);
				e.setPrepared(true);

			}

			break;

		}

		// ---------------------- ADD ABILITIES ----------------------

	}

	public void activateAbility(Characters c) {
		c.setInAbility(true);
	}

	private boolean checkEncounterEnemy(int[] nextPos, Characters c) {
		return enemyMap[nextPos[0]][nextPos[1]] == 1;
	}

	private boolean checkEncounterTreasure(int[] nextPos, Characters c) {
		return treasureMap[nextPos[0]][nextPos[1]] == 1;
	}

	private int barLength(int stat) {
		return (int) ((stat / 100f) * 300);
	}

	private int fontPlacement(Graphics2D g2d, int stat) {

		if (barLength(stat) <= g2d.getFontMetrics().stringWidth(String.valueOf(stat)))
			return 116 + (150 - (g2d.getFontMetrics().stringWidth(String.valueOf(stat)) / 2));

		return 116 + (barLength(stat) / 2) - (g2d.getFontMetrics().stringWidth(String.valueOf(stat)) / 2);

	}

	public Biomes[][] getBiomeMap() {
		return biomeMap;
	}

	public int[][] getTreasureMap() {
		return treasureMap;
	}

	public int[][] getEnemyMap() {
		return enemyMap;
	}

	public void setBiomeMap(Biomes[][] biomeMap) {

		this.biomeMap = biomeMap;

	}

	public void setTreasureMap(int[][] treasureMap) {

		this.treasureMap = treasureMap;

	}

	public void setEnemyMap(int[][] enemyMap) {

		this.enemyMap = enemyMap;

	}

	@Override
	public void setStart(boolean start) {
		this.start = start;

	}

}
