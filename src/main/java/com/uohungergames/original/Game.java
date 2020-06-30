package com.uohungergames.original;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Game implements Runnable {

	private Thread t;
	private ArrayList<Player> playerList;
	private String name;
	private MessageReceivedEvent event;
	private ArrayList<ArrayList<Event>> eventList;
	private ArrayList<Player> killList;

	private HGCommands hgCommands;

	private boolean howeeRevenge;

	public Game(HGCommands hgCommands, ArrayList<Player> playerList, MessageReceivedEvent event) {

		this.name = "Game";
		this.event = event;

		this.hgCommands = hgCommands;

	}

	@Override
	public void run() {

		if (hgCommands.getPlayerList().size() < 24)
			hgCommands.fillPlayers(event);
		this.playerList = (ArrayList<Player>) hgCommands.getPlayerList().clone();

		hgCommands.generateEvents();
		eventList = hgCommands.getEvents();

		int days = 0;
		String tribute = event.getGuild().getRolesByName("tributes", true).get(0).getAsMention();
		Random rand = new Random();
		int randEvent;
		int randPlayer;
		Event chosenEvent;
		String chosenEventString;
		int numInEvent;
		ArrayList<String> chosenEvents = new ArrayList<String>();
		ArrayList<Player> playersDied = new ArrayList<Player>();
		killList = new ArrayList<Player>();
		boolean isNight = false;
		int feastDay = rand.nextInt(6) + 2;

		hgCommands.setStart(true);

		hgCommands.sendMessage(event, "<:POGGERS:686008688816816171> The game will start in 10 seconds...");

		sleep(10000);

		event.getTextChannel().getManager()
				.putPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.MESSAGE_WRITE))
				.queue();

		hgCommands.incrementNumGames();

		hgCommands.sendMessage(event, tribute + " **Welcome to the " + ordinal(hgCommands.getNumGames())
				+ " uOttawa Hunger Games!** Here are the players participating:");
		hgCommands.displayPlayers(event);

		event.getChannel().sendTyping().queue();

		sleep(5000);

		hgCommands.sendMessage(event, "Just for new players, here are the rules for this game:");

		EmbedBuilder eb = new EmbedBuilder();

		eb.setColor(Color.CYAN);
		eb.setTitle("Rules:");
		eb.setDescription(
				"**1.** You cannot edit your player or change your custom event while the game is running!\n**2.** Git gud\n**3.** pray that howee doesn't die\n**4.** More gamemodes will be added soon, this is just a copy of the simulator we were using before, but in bot form");

		event.getChannel().sendMessage(eb.build()).queue();
		event.getChannel().sendTyping().queue();

		while (hgCommands.playerCount() > 1) {

			if (!isNight) {

				days++;

			}

			chosenEvents.clear();

			if (!isNight)
				playersDied.clear();

			sleep(5000);

			if (!isNight)
				hgCommands.sendMessage(event,
						":alarm_clock: The " + ordinal(days) + " day will start in 30 seconds...");
			else
				hgCommands.sendMessage(event,
						":alarm_clock: The " + ordinal(days) + " night will start in 30 seconds...");

			event.getTextChannel().getManager()
					.putPermissionOverride(event.getGuild().getPublicRole(), EnumSet.of(Permission.MESSAGE_WRITE), null)
					.queue();

			sleep(20000);

			hgCommands.sendMessage(event, "<:monkaS:498230647110762516> **Ten seconds remaining!** Get ready!");

			sleep(5000);

			hgCommands.sendMessage(event,
					"<:POGGERS:686008688816816171> **Five seconds remaining!** Chat will be muted!");

			sleep(5000);

			event.getTextChannel().getManager()
					.putPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.MESSAGE_WRITE))
					.queue();

			eb.clear();

			eb.setColor(Color.RED);

			if (!isNight)
				eb.setTitle("DAY " + days);
			else
				eb.setTitle("NIGHT " + days);
			eb.setAuthor("Players Left: " + playerList.size() + "/" + hgCommands.getPlayerList().size());
			event.getChannel().sendMessage(eb.build()).queue();

			sleep(3000);

			if (howeeRevenge && rand.nextInt(9) == 4) {

				if (allInEvent())
					break;

				howeeRevenge = false;
				int size;

				if (hgCommands.playerCountWithEvents() < 4)
					size = hgCommands.playerCountWithEvents() - 1;
				else
					size = 4;

				chosenEvents.add("**HOWEE HAS FOUND HIS VICTIMS. " + size
						+ " HAVE DIED IN ORDER TO SATIATE HIS GHOSTLY RAGE.**");

				for (int i = 0; i < size; i++) {

					randPlayer = rand.nextInt(playerList.size());

					if (playerList.get(randPlayer).isInEvent() || playerList.get(randPlayer).getHP() == 0) {
						i--;
						continue;
					} else {

						playerList.get(randPlayer).setInEvent(true);

						if (playerList.get(randPlayer).isSameUsername()) {

							chosenEventString = "**" + playerList.get(randPlayer).getName()
									+ "** succumbs to Howee's call towards their grave...";

						} else {

							chosenEventString = "**" + playerList.get(randPlayer).getName() + " ("
									+ playerList.get(randPlayer).getUser()
									+ ")** succumbs to Howee's call towards their grave...";

						}

						playerList.get(randPlayer).setHP(0);

						playersDied.add(playerList.get(randPlayer));
						killList.add(playerList.get(randPlayer));
						playerList.get(randPlayer).setDeathEvent(chosenEventString);
						playerList.remove(randPlayer);

					}

					chosenEvents.add(chosenEventString);

				}

			}

			for (int i = 0; i < rand.nextInt((int) Math.ceil(playerList.size() / 1.5)) + 1; i++) {

				if (days == 1 && !isNight) {

					randEvent = rand.nextInt(eventList.get(0).size());
					chosenEvent = eventList.get(0).get(randEvent);

				} else if (days == feastDay && !isNight) {

					randEvent = rand.nextInt(eventList.get(2).size());
					chosenEvent = eventList.get(2).get(randEvent);

				} else if (!isNight) {

					randEvent = rand.nextInt(eventList.get(1).size());
					chosenEvent = eventList.get(1).get(randEvent);

				} else {

					randEvent = rand.nextInt(eventList.get(3).size());
					chosenEvent = eventList.get(3).get(randEvent);

				}

				if (chosenEvent.getNumTributes() > hgCommands.playerCountWithEvents())
					i--;
				else {

					if (allInEvent())
						break;

					int killCount = 0;
					for (int k = 0; k < 5; k++) {

						if (chosenEvent.getKill()[k] == 0)
							killCount++;

					}

					if (playerList.size() == killCount) {
						i--;
						continue;
					}

					chosenEventString = chosenEvent.getEvent();
					numInEvent = chosenEvent.getNumTributes();

					for (int j = 0; j < chosenEvent.getNumTributes(); j++) {

						randPlayer = rand.nextInt(playerList.size());

						if (playerList.get(randPlayer).isInEvent() || playerList.get(randPlayer).getHP() == 0) {
							j--;
							continue;
						} else {

							playerList.get(randPlayer).setInEvent(true);

							if (playerList.get(randPlayer).isSameUsername()) {

								chosenEventString = chosenEventString.replace(
										"[Player" + (numInEvent - (numInEvent - j) + 1) + "]",
										"**" + playerList.get(randPlayer).getName() + "**");

							} else {

								chosenEventString = chosenEventString.replace(
										"[Player" + (numInEvent - (numInEvent - j) + 1) + "]",
										"**" + playerList.get(randPlayer).getName() + " ("
												+ playerList.get(randPlayer).getUser() + ")**");

							}

							if (chosenEvent.getKill()[j] == 0) {

								if (playerList.get(randPlayer).getID().equals("211981146941030401")) {

									howeeRevenge = true;

								}

								playerList.get(randPlayer).setHP(0);
								playersDied.add(playerList.get(randPlayer));
								killList.add(playerList.get(randPlayer));
								playerList.get(randPlayer).setDeathEvent(chosenEventString);
								playerList.remove(randPlayer);

							} else if (chosenEvent.getKill()[j] == 1) {

								playerList.get(randPlayer).setKills(playerList.get(randPlayer).getKills() + killCount);

							}

						}

					}

					chosenEvents.add(chosenEventString);

				}

			}

			if (howeeRevenge) {

				chosenEvents.add("**Howee's ghost wanders the arena, looking for revenge...**");

			}

			for (Player p : playerList) {

				p.setInEvent(false);

			}

			String eventString = "";

			for (String str : chosenEvents) {

				eventString += str + "\n\n";

			}

			eb.clear();
			eb.setDescription(eventString);

			if (!isNight) {
				eb.setTitle("Events for Day " + days);

				if (days == feastDay)
					eb.setTitle("Events for Day " + days + ": FEAST");

			} else
				eb.setTitle("Events for Night " + days);

			eb.setColor(Color.MAGENTA);
			event.getChannel().sendMessage(eb.build()).queue();

			event.getChannel().sendTyping().queue();
			sleep(3000);

			if (playersDied.size() > 0 && isNight) {

				String deaths = "";

				eb.clear();
				eb.setTitle("In the dead of night, the cannon sounds " + playersDied.size() + " time(s)...");

				for (Player p : playersDied) {

					deaths += p.getName() + ", owned by " + p.getUser() + " | Kills: " + p.getKills() + "\n\n";

				}

				eb.setDescription(deaths);
				eb.setColor(Color.RED);

				event.getChannel().sendMessage(eb.build()).queue();

			} else if (isNight) {

				eb.clear();
				eb.setTitle("You wait for the cannon to sound, but no shots are heard...");
				eb.setColor(Color.RED);

				event.getChannel().sendMessage(eb.build()).queue();

			}

			isNight = !isNight;

		}

		eb.clear();

		String playerSorted = "";

		for (int i = 0; i < killList.size(); i++) {

			Player p = killList.get(i);

			playerSorted += ordinal((killList.size() - i) + 1) + " place: " + p.getName() + ", owned by " + p.getUser()
					+ " | Kills: " + p.getKills() + "\n\n";

		}

		eb.setAuthor("Winner winner chicken dinner!", null, playerList.get(0).getPFP());
		eb.setTitle("<:POGGERS:686008688816816171> The winner is: " + playerList.get(0).getName()
				+ "! <:POGGERS:686008688816816171>");
		eb.setThumbnail(playerList.get(0).getPFP());
		eb.setDescription(playerSorted);
		eb.setFooter(ordinal(hgCommands.getNumGames()) + " Hunger Games");
		eb.setColor(Color.ORANGE);
		event.getChannel().sendMessage(eb.build()).queue();
		sleep(1000);
		event.getChannel().pinMessageById(event.getChannel().getLatestMessageIdLong()).queue();
		event.getGuild().addRoleToMember(playerList.get(0).getID(),
				event.getGuild().getRolesByName("Victor", true).get(0));

		event.getChannel().sendTyping().queue();

		sleep(3000);

		hgCommands.sendMessage(event, "Support the bot and it's development! https://www.buymeacoffee.com/dish");
		hgCommands.sendMessage(event,
				"**IF YOU HAVE ANY IDEAS FOR THE BOT, USE `hg!addidea [idea]` TO ADD A SUGGESTION!**");

		hgCommands.resetPlayers(event);
		hgCommands.setStart(false);

		event.getTextChannel().getManager()
				.putPermissionOverride(event.getGuild().getPublicRole(), EnumSet.of(Permission.MESSAGE_WRITE), null)
				.queue();

	}

	private boolean allInEvent() {

		for (Player p : playerList) {

			if (!p.isInEvent() && p.getHP() > 0) {

				return false;

			}

		}

		return true;

	}

	public void start() {

		System.out.println("Thread started");

		if (t == null) {

			t = new Thread(this, name);
			t.start();

		}

	}

	private void sleep(long millis) {

		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String ordinal(int i) {
		String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
		switch (i % 100) {
		case 11:
		case 12:
		case 13:
			return i + "th";
		default:
			return i + sufixes[i % 10];

		}
	}

}
