package com.uohungergames.interactive;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

	public void test(MessageReceivedEvent event) {

		deleteLockedChannels(event);
		createCharacterList();
		createLockedChannels(event);
		generateMap(500);

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

		eb.addField("HP: ", String.valueOf(character.getHP()), true);
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
			int cool = c.getCool();

			// Set font
			Font f = Font.createFont(Font.TRUETYPE_FONT, new File("ARCADECLASSIC.TTF"));
			int textHeight = 19;

			// Set colors
			Color emptyBar = Color.decode("#494C54");
			Color fillBar = Color.decode("#353E54");
			Color text = Color.WHITE;
			Color coolBar = Color.decode("#DBD68A");
			Color coolText = Color.decode("#332464");

			// Read image
			BufferedImage blankStats = ImageIO.read(new File("stats.png"));
			BufferedImage modStats = new BufferedImage(blankStats.getWidth(), blankStats.getHeight(),
					BufferedImage.TYPE_INT_ARGB);

			// Create drawable layer
			Graphics2D g2d = modStats.createGraphics();
			g2d.drawImage(blankStats, 0, 0, null);

			g2d.setFont(f.deriveFont(Font.PLAIN, 25));

			// Draw empty bars
			g2d.setColor(emptyBar);
			g2d.fillRoundRect(116, 6, width, 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 48, width, 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 90, width, 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 132, width, 21, arcWidth, arcHeight);

			// Draw filled bars
			g2d.setColor(fillBar);
			g2d.fillRoundRect(116, 6, barLength(hp), 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 48, barLength(atk), 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 90, barLength(def), 21, arcWidth, arcHeight);

			g2d.setColor(coolBar);
			g2d.fillRoundRect(116, 132, barLength(cool), 21, arcWidth, arcHeight);

			// Draw stat numbers
			g2d.setColor(text);
			g2d.drawString(String.valueOf(hp), fontPlacement(g2d, hp), 6 + textHeight);
			g2d.drawString(String.valueOf(atk), fontPlacement(g2d, atk), 48 + textHeight);
			g2d.drawString(String.valueOf(def), fontPlacement(g2d, def), 90 + textHeight);

			g2d.setColor(coolText);
			g2d.drawString(String.valueOf(cool), fontPlacement(g2d, cool), 132 + textHeight);

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
			Thread.sleep(500);
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

			for (Player p : characterList.get(i).getCharacterControllers())
				c.getTextChannels().get(i).createPermissionOverride(event.getGuild().getMemberById(p.getID()))
						.setAllow(EnumSet.of(Permission.VIEW_CHANNEL)).queue();

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
			Thread.sleep(500);
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
					else if (posx > 49)
						posx = 49;

					if (posy < 0)
						posy = 0;
					else if (posy > 49)
						posy = 49;

					c.setMapPosition(posx, posy);

				}

		}

	}

	public void move(Characters c, int spaces, String dir) {

		int[][] charMap = c.getMap();
		int currentPosition[] = c.getPosition();
		int nextPos[] = new int[2];

		/*
		 * 
		 * [0, 1, 2, 3] [4, 5, 6, 7] [8, 9, 10, 11] [12, 13, 14, 15]
		 */

		for (int i = 0; i < spaces; i++) {

			switch (dir) {

			case "N":
				nextPos[0] = currentPosition[0] - 1 < 0 ? 0 : currentPosition[0] - 1; // y
				nextPos[1] = currentPosition[1]; // x

				encounterEnemy(nextPos, c);
				encounterTreasure(nextPos, c);
				encounterPlayer(nextPos, c);

				c.setPosition(nextPos[1], nextPos[0]);
				c.setMapPosition(nextPos[1], nextPos[0]);
				break;
			case "E":
				nextPos[0] = currentPosition[0]; // y
				nextPos[1] = currentPosition[1] + 1 > 49 ? 49 : currentPosition[1] + 1; // x

				encounterEnemy(nextPos, c);
				encounterTreasure(nextPos, c);
				encounterPlayer(nextPos, c);

				c.setPosition(nextPos[1], nextPos[0]);
				c.setMapPosition(nextPos[1], nextPos[0]);
				break;
			case "S":
				nextPos[0] = currentPosition[0] + 1 > 49 ? 49 : currentPosition[0] + 1; // y
				nextPos[1] = currentPosition[1]; // x

				encounterEnemy(nextPos, c);
				encounterTreasure(nextPos, c);
				encounterPlayer(nextPos, c);

				c.setPosition(nextPos[1], nextPos[0]);
				c.setMapPosition(nextPos[1], nextPos[0]);
				break;
			case "W":
				nextPos[0] = currentPosition[0]; // y
				nextPos[1] = currentPosition[1] - 1 < 0 ? 0 : currentPosition[1] - 1; // x

				encounterEnemy(nextPos, c);
				encounterTreasure(nextPos, c);
				encounterPlayer(nextPos, c);

				c.setPosition(nextPos[1], nextPos[0]);
				c.setMapPosition(nextPos[1], nextPos[0]);
				break;

			}

		}

	}

	private void encounterPlayer(int[] nextPos, Characters c) {
		// TODO Auto-generated method stub

	}

	private void encounterEnemy(int[] nextPos, Characters c) {
		// TODO Auto-generated method stub

	}

	private void encounterTreasure(int[] nextPos, Characters c) {
		// TODO Auto-generated method stub

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
