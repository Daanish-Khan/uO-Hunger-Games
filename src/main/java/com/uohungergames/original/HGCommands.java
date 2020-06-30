package com.uohungergames.original;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.uohungergames.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HGCommands extends Commands {

	private boolean start = false;
	private int numGames = 18;
	private ArrayList<ArrayList<Event>> events = new ArrayList<ArrayList<Event>>();

	public void displayPlayers(MessageReceivedEvent event) {

		EmbedBuilder eb = new EmbedBuilder();

		String players = "";

		for (int i = 0; i < playerList.size(); i++) {

			if (i % 2 == 0) {

				players += "\n\n **District** **" + ((i / 2) + 1) + "**\n";

			}

			players += "\n" + playerList.get(i).getName() + ", owned by " + playerList.get(i).getUser();

			if (playerList.get(i).getHP() > 0)
				players += " [ALIVE]";
			else
				players += " [DEAD]";

		}

		eb.setAuthor("Players");

		eb.setTitle(playerList.size() + "/24");

		eb.setDescription(players);

		if (playerList.size() < 24)
			eb.setColor(Color.green);
		else
			eb.setColor(Color.red);

		event.getChannel().sendMessage(eb.build()).queue();

	}

	// Add custom event
	public void addEvent(MessageReceivedEvent event) {

		ArrayList<String> list = new ArrayList<String>(Arrays.asList(event.getMessage().getContentRaw().split("\"")));

		list.remove(0);

		String customEvent;
		boolean fatal = false;
		int[] kill = new int[] { -1, -1, -1, -1, -1 };
		int numTributes;
		boolean flag = false;

		// Check if player exists
		for (Player p : playerList) {

			if (p.getID().equals(event.getMember().getId())) {

				flag = true;

			}

		}

		if (!flag) {

			sendMessage(event, ":x: Cannot find profile for " + event.getMember().getAsMention()
					+ "! Please use `hg!join` to add a player.");
			return;

		}

		// Checks command size
		if (list.size() < 2) {
			sendMessage(event,
					":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`");
			return;
		} else {

			// Splits command into two so that the event can be processed properly
			ArrayList<String> list2 = new ArrayList<String>(Arrays.asList(list.get(1).split("\\s+")));

			list.addAll(list2);

			list.remove(1);
			list.remove(1);

		}

		// Check command length
		if (list.size() > 4)
			sendMessage(event,
					":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`");
		else {

			// Error checking
			if (list.get(0).toLowerCase().contains("[player1]")
					&& (list.get(2).equalsIgnoreCase("yes") || list.get(2).equalsIgnoreCase("no"))) {

				if (list.get(2).equalsIgnoreCase("yes") && list.size() == 3) {

					sendMessage(event,
							":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`");

				} else if (list.get(2).equalsIgnoreCase("no") && list.size() == 4)

					sendMessage(event,
							":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`");

				else if (!list.get(1).equals("1") && !list.get(1).equals("2")) {

					sendMessage(event,
							":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`");

				} else if (list.get(1).equals("2") && !list.get(0).toLowerCase().contains("[player2]")) {

					sendMessage(event,
							":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`");

				} else if (list.size() == 4 && list.get(1).equals("1") && list.get(3).equalsIgnoreCase("player2")) {

					sendMessage(event,
							":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`");

				} else {

					numTributes = Integer.parseInt(list.get(1));
					customEvent = list.get(0);

					if (list.size() == 4) {

						fatal = true;

						// Checks if death check is valid
						if (list.get(3).equalsIgnoreCase("player1"))
							kill = new int[] { 0, 1, -1, -1, -1 };
						else if (list.get(3).equalsIgnoreCase("player2")) {
							kill = new int[] { 1, 0, -1, -1, -1 };

						} else {

							sendMessage(event,
									":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)]`");
							return;

						}

					}

					// Add event to player
					for (Player p : playerList) {

						if (p.getID().equals(event.getMember().getId())) {

							// Checks if an event has already been made
							if (list.size() == 4) {

								if (p.hasKillEvent())
									sendMessage(event,
											"Event has already been set for this player! Event will be replaced!");

								p.setKillEvent("Day", fatal, numTributes, kill, customEvent);
								sendMessage(event, "Event added! Use `hg!me` to view your event!");
								break;

							} else {

								if (p.hasDeathlessEvent())
									sendMessage(event,
											"Event has already been set for this player! Event will be replaced!");

								p.setDeathlessEvent("Day", fatal, numTributes, kill, customEvent);
								sendMessage(event, "Event added! Use `hg!me` to view your event!");
								break;

							}

						}

					}

				}

			} else {

				sendMessage(event,
						":x: Format not valid! Please use `hg!addevent [\"event\"|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)]`");

			}

		}

	}

	public void start(MessageReceivedEvent event, String msg) {

		String[] arr = msg.split(" ");
		String tribute = event.getGuild().getRolesByName("tributes", true).get(0).getAsMention();
		Game gameThread = new Game(this, playerList, event);

		if (arr.length > 3 || arr.length == 2) {
			sendMessage(event, ":x: Illegal Argument! Usage: `hg!start (optional)[delay|hour/min]`");
			return;
		} else if (arr.length == 3) {

			long time = 0;

			try {
				time = Integer.parseInt(arr[1]);
			} catch (NumberFormatException e) {
				sendMessage(event, ":x: Delay must be a number! Usage: `hg!start (optional)[delay|hour/min]`");
				return;
			}

			if (time <= 0) {
				sendMessage(event, ":x: Delay must be greater than 0! Usage: `hg!start (optional)[delay|hour/min]`");
				return;
			}

			if (arr[2].equalsIgnoreCase("hour") || arr[2].equalsIgnoreCase("hours")) {

				time = time * 3600000;
				sendMessage(event, tribute + ", **Game will start in " + arr[1] + " hour(s)!**");

			} else if (arr[2].equalsIgnoreCase("min") || arr[2].equalsIgnoreCase("mins")) {

				time = time * 60000;
				sendMessage(event, tribute + ", **Game will start in " + arr[1] + " minute(s)!**");

			} else {

				sendMessage(event, ":x: Illegal Argument! Usage: `hg!start (optional)[delay|hour/min]`");
				return;

			}

			if (time - 3600000 > 0) {

				new java.util.Timer().schedule(

						new java.util.TimerTask() {

							@Override
							public void run() {

								sendMessage(event, tribute
										+ ", **the game will start in 1 hour!** Use `hg!join` to join, and `hg!help` for commands!");

								cancel();

							}

						}, time - 3600000);

			}

			if (time - 1800000 > 0) {

				new java.util.Timer().schedule(

						new java.util.TimerTask() {

							@Override
							public void run() {

								sendMessage(event, tribute
										+ ", **the game will start in 30 minutes!** Use `hg!join` to join, and `hg!help` for commands!");

								cancel();

							}

						}, time - 1800000);

			}

			if (time - 300000 > 0) {

				new java.util.Timer().schedule(

						new java.util.TimerTask() {

							@Override
							public void run() {

								sendMessage(event, tribute
										+ ", **the game will start in 5 minutes!** Use `hg!join` to join, and `hg!help` for commands!");

								cancel();

							}

						}, time - 300000);

			}

			new java.util.Timer().schedule(

					new java.util.TimerTask() {

						@Override
						public void run() {

							if (playerList.size() < 12) {

								sendMessage(event,
										"**Not enough players to start the game!** Adding 5 more minutes to timer.");

								start(event, "hg!start 5 min");

							} else {

								gameThread.start();

							}

							cancel();

						}

					}, time - 10000);

		} else {

			gameThread.start();

		}

	}

	public boolean hasStarted() {

		return start;

	}

	public void setStart(boolean start) {

		this.start = start;

	}

	public int playerCountWithEvents() {

		int count = 0;

		for (Player p : playerList) {

			if (p.getHP() > 0 && !p.isInEvent())
				count++;

		}

		return count;

	}

	public void generateEvents() {

		BufferedReader br = null;
		String line = "";

		events.clear();

		events.add(new ArrayList<Event>());
		events.add(new ArrayList<Event>());
		events.add(new ArrayList<Event>());
		events.add(new ArrayList<Event>());

		int[] kill;

		boolean fatal;

		for (Player p : playerList) {

			if (p.getKillEvent() != null) {

				if (p.getKillEvent().getType().equalsIgnoreCase("bloodbath"))
					events.get(0).add(p.getKillEvent());
				else if (p.getKillEvent().getType().equalsIgnoreCase("day"))
					events.get(1).add(p.getKillEvent());
				else if (p.getKillEvent().getType().equalsIgnoreCase("feast"))
					events.get(2).add(p.getKillEvent());
				else if (p.getKillEvent().getType().equalsIgnoreCase("night"))
					events.get(3).add(p.getKillEvent());

			}

			if (p.getDeathlessEvent() != null) {

				if (p.getDeathlessEvent().getType().equalsIgnoreCase("bloodbath"))
					events.get(0).add(p.getDeathlessEvent());
				else if (p.getDeathlessEvent().getType().equalsIgnoreCase("day"))
					events.get(1).add(p.getDeathlessEvent());
				else if (p.getDeathlessEvent().getType().equalsIgnoreCase("feast"))
					events.get(2).add(p.getDeathlessEvent());
				else if (p.getDeathlessEvent().getType().equalsIgnoreCase("night"))
					events.get(3).add(p.getDeathlessEvent());

			}

		}

		try {

			br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("events.csv")));
			br.readLine();

			while ((line = br.readLine()) != null) {

				// use "|" as separator
				String[] row = line.split("\\|");

				kill = new int[] { Integer.parseInt(row[3]), Integer.parseInt(row[4]), Integer.parseInt(row[5]),
						Integer.parseInt(row[6]), Integer.parseInt(row[7]) };

				if (row[1].equalsIgnoreCase("y"))
					fatal = true;
				else
					fatal = false;

				if (row[0].equalsIgnoreCase("bloodbath"))
					events.get(0).add(new Event(row[0], fatal, Integer.parseInt(row[2]), kill, row[8]));
				else if (row[0].equalsIgnoreCase("day"))
					events.get(1).add(new Event(row[0], fatal, Integer.parseInt(row[2]), kill, row[8]));
				else if (row[0].equalsIgnoreCase("feast"))
					events.get(2).add(new Event(row[0], fatal, Integer.parseInt(row[2]), kill, row[8]));
				else if (row[0].equalsIgnoreCase("night"))
					events.get(3).add(new Event(row[0], fatal, Integer.parseInt(row[2]), kill, row[8]));

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public int getNumGames() {

		return numGames;

	}

	public void incrementNumGames() {

		numGames++;
	}

	public ArrayList<ArrayList<Event>> getEvents() {

		return events;

	}

}
