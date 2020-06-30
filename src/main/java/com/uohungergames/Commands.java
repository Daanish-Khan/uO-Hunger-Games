package com.uohungergames;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.uohungergames.original.Event;
import com.uohungergames.original.Player;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Commands {

	private static final String VERSION = "1.4";
	protected static ArrayList<Player> playerList = new ArrayList<Player>();
	private ArrayList<String> ideas = new ArrayList<String>();

	public void removeRole(MessageReceivedEvent event) {

		event.getGuild().removeRoleFromMember(event.getMember().getId(),
				event.getGuild().getRolesByName("tributes", true).get(0)).queue();
		sendMessage(event, event.getAuthor().getAsMention()
				+ ", I removed the Tributes role from you! Sad to see you go <:notcrying:650392380297707540>");

	}

	public void addPlayer(MessageReceivedEvent event) {

		Message message = event.getMessage();
		String content = message.getContentRaw();

		ArrayList<String> list = new ArrayList<String>(Arrays.asList(content.split(" ")));
		list.remove(0);

		if (list.size() > 2) {

			sendMessage(event,
					":x: Incorrect format! Correct usage: `hg!join [avatar_url (optional)|name (optional)]`");
			return;

		}

		// Defaults if there are no params
		if (list.size() == 0) {

			if (event.getAuthor().getAvatarUrl() != null)
				list.add(event.getAuthor().getAvatarUrl());
			else
				list.add(event.getAuthor().getDefaultAvatarUrl());

			list.add(event.getMember().getEffectiveName());

		}

		if (!imgExist(list.get(0))) {

			// Image doesn't resolve, so the entry must have been a name
			if (list.size() < 2) {

				if (event.getAuthor().getAvatarUrl() != null)
					list.add(0, event.getAuthor().getAvatarUrl());
				else
					list.add(0, event.getAuthor().getDefaultAvatarUrl());

			} else {

				// If second entry is a url, first entry becomes name
				if (imgExist(list.get(1))) {

					Collections.swap(list, 0, 1);

				} else {

					// Both entries are not url's

					// Defaults to general pfp if it is null
					if (event.getAuthor().getAvatarUrl() != null)
						list.set(0, event.getAuthor().getAvatarUrl());
					else
						list.set(0, event.getAuthor().getDefaultAvatarUrl());

				}

			}

		} else {

			// If image exists, the entry must have been a url
			if (list.size() < 2)
				list.add(event.getMember().getEffectiveName());
			else if (imgExist(list.get(1)))
				// If both are urls, the name is defaulted
				list.set(1, event.getMember().getEffectiveName());

		}

		event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRolesByName("tributes", true).get(0))
				.queue();

		playerList.add(new Player(list.get(0), list.get(1), event.getAuthor().getAsMention(), event.getMember().getId(),
				event.getMember().getEffectiveName()));
		sendMessage(event,
				"Character created! Type `hg!me` to view profile at anytime or `hg!edit` to edit before the game starts.");
		profileDisplay(event);

	}

	public void editPlayer(MessageReceivedEvent event) {

		Message message = event.getMessage();
		String content = message.getContentRaw();

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
		for (Player p : playerList) {

			if (p.getID().equals(event.getMember().getId())) {

				if (list.get(0) != null)
					p.setPFP(list.get(0));
				if (list.get(1) != null)
					p.setName(list.get(1));

				sendMessage(event, "Edit successful! Type `hg!me` to view your player.");
				return;

			}

		}

		sendMessage(event, ":x: Edit failed, cannot find player! Please create one with `hg!join`.");

	}

	public void deletePlayer(MessageReceivedEvent event) {

		Message message = event.getMessage();
		String content = message.getContentRaw();

		List<String> list = new ArrayList<String>(Arrays.asList(content.split(" ")));
		list.remove(0);

		if (list.size() > 2) {

			sendMessage(event,
					":x: Incorrect format! Correct usage: `hg!delete [{MODS ONLY} district #|player#  (1/2))]`");

		} else if (list.size() == 0) {

			for (int i = 0; i < playerList.size(); i++) {

				if (event.getMember().getId().equals(playerList.get(i).getID())) {

					sendMessage(event, event.getAuthor().getAsMention() + "'s character sucessfully deleted!");
					playerList.remove(i);
					return;

				}

			}

			sendMessage(event, ":x: Cannot find " + event.getAuthor().getAsMention()
					+ "'s character! Please create a player with `hg!join`.");

		} else {

			if (list.get(0).chars().allMatch(Character::isDigit) && list.get(1).chars().allMatch(Character::isDigit)) {

				int index = (Integer.parseInt(list.get(0)) - 1) * 2 + (Integer.parseInt(list.get(1)) - 1);

				if (Integer.parseInt(list.get(0)) > 12 || Integer.parseInt(list.get(0)) < 1
						|| Integer.parseInt(list.get(1)) != 1 && Integer.parseInt(list.get(1)) != 2) {

					sendMessage(event, ":x: District number must be 1-12, and player number must be 1-2!");
					return;

				}

				if (index < playerList.size()) {

					sendMessage(event, playerList.get(index).getUser() + "'s character sucessfully deleted!");
					playerList.remove(index);

				} else {

					sendMessage(event, ":x: Index is too big!");

				}

			} else {

				sendMessage(event,
						":x: Incorrect format! Correct usage: `hg!delete [{MODS ONLY} district #|player#  (1/2))]`");

			}

		}

	}

	// Reset Playerlist
	public void resetPlayers(MessageReceivedEvent event) {

		playerList = new ArrayList<Player>();
		setStart(false);
		event.getTextChannel().getManager()
				.putPermissionOverride(event.getGuild().getPublicRole(), EnumSet.of(Permission.MESSAGE_WRITE), null)
				.queue();
		sendMessage(event, "Player list reset!");

	}

	public void botCrashReset(MessageReceivedEvent event) {

		for (Player p : playerList)
			p.setHP(100);
		setStart(false);
		event.getTextChannel().getManager()
				.putPermissionOverride(event.getGuild().getPublicRole(), EnumSet.of(Permission.MESSAGE_WRITE), null)
				.queue();

		sendMessage(event,
				"Sowwy miwstew " + event.getAuthor().getAsMention() + " i have been a bad boi pls fowgive me x3");

	}

	public abstract void setStart(boolean start);

	public void fillPlayers(MessageReceivedEvent event) {

		List<Member> ls = event.getGuild().getMembersWithRoles(event.getGuild().getRolesByName("tributes", true));
		EmbedBuilder eb = new EmbedBuilder();
		Random r = new Random();
		int rand;
		int count = 0;
		String str = "";

		sendMessage(event,
				"<:OMEGALUL:491040357908348959> Since there are not enough players to start the game, a few have been dragged into the arena against their will! <:OMEGALUL:491040357908348959>");

		while (playerList.size() < 24) {

			rand = r.nextInt(ls.size());
			Member m = ls.get(rand);

			if (!checkUserAdded(m.getId())) {
				playerList.add(new Player(m.getUser().getEffectiveAvatarUrl(), m.getEffectiveName(), m.getAsMention(),
						m.getId(), m.getEffectiveName()));
				str += m.getEffectiveName() + ", owned by " + m.getAsMention() + "\n\n";
				count++;
			}

		}

		eb.setTitle(count + " players have been yeeted into the arena!");
		eb.setDescription(str);
		eb.setColor(Color.red);
		event.getChannel().sendMessage(eb.build()).queue();

	}

	public boolean checkUserAdded(String user) {

		List<String> userList = playerList.stream().map(p -> p.getID()).collect(Collectors.toList());

		return userList.contains(user);

	}

	public void profileDisplay(MessageReceivedEvent event) {

		Player player = null;
		String user = event.getMember().getId();
		String userMention = event.getMember().getAsMention();

		Event killEvent = null;
		Event deathlessEvent = null;

		EmbedBuilder eb = new EmbedBuilder();

		for (Player p : playerList) {

			if (p.getID().equals(user)) {

				player = p;
				break;

			}

		}

		if (player == null) {

			sendMessage(event,
					":x: Cannot find profile for " + userMention + "! Please use `hg!join` to add a player.");

		} else {

			killEvent = player.getKillEvent();
			deathlessEvent = player.getDeathlessEvent();

			eb.setAuthor(event.getMember().getEffectiveName() + "'s Character", null, event.getAuthor().getAvatarUrl());

			eb.setThumbnail(player.getPFP());

			eb.setTitle(player.getName());

			eb.setColor(Color.CYAN);

			eb.addField("Name: ", player.getName(), true);
			eb.addField("Status: ", ((player.getHP() > 0) ? "ALIVE" : "DEAD"), true);
			eb.addField("Kills: ", String.valueOf(player.getKills()), true);

			if (player.getHP() == 0)
				eb.addField("How you died: ", player.getDeathEvent(), false);

			// Custom events
			if (killEvent == null && deathlessEvent == null)
				eb.addField("Events", "No events have been set! Use hg!addevent to add a custom event.", false);
			else {

				if (killEvent != null) {

					eb.addField("Kill Event", killEvent.getEvent(), true);

					String str = "";

					for (int i = 0; i < killEvent.getKill().length; i++) {

						if (killEvent.getKill()[i] == 0) {

							str += "Player " + (i + 1) + ", ";

						}

					}

					str = str.replaceAll(", $", "");
					eb.addField("Who dies?", str, true);

				}

				if (deathlessEvent != null)
					eb.addField("Deathless Event", deathlessEvent.getEvent(), false);

			}

			sendMessage(event, event.getAuthor().getAsMention());
			event.getChannel().sendMessage(eb.build()).queue();

		}

	}

	public void help(MessageReceivedEvent event) {

		String[] msg = event.getMessage().getContentRaw().split(" ");

		if (msg.length > 2) {
			sendMessage(event, ":x: Illegal usage! Format: `hg!help (optional)[join/edit/delete/addevent/addidea]`");
			return;
		}

		if (msg.length == 1) {
			EmbedBuilder eb = new EmbedBuilder();

			eb.setAuthor("uO Hunger Games Bot", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
			eb.setTitle("Prefix: hg!");

			eb.addField("**join**", "Joins the game with your player. Format: `avatar_url (optional)|name (optional)`",
					false);
			eb.addField("**me**", "Displays your player profile.", false);
			eb.addField("**edit**", "Edits your player profile. Format: `url|name`", false);
			eb.addField("**delete**", "Deletes your profile. Format: `{MODS ONLY} district #|player#  (1/2))`", false);
			eb.addField("**reset**", "[MODS ONLY] Resets the player list.", false);
			eb.addField("**players**", "Displays all the players participating.", false);
			eb.addField("**addevent**",
					"Adds your custom event to the game. Format: `'event'|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`",
					false);
			eb.addField("**start**", "[MODS ONLY] Starts the game.", false);
			eb.addField("**removerole**", "Removes the Tributes role.", false);
			eb.addField("**help**",
					"Displays commands and their usage. Format: `(optional) [join/edit/delete/addevent/addidea]`",
					false);
			eb.addField("**changelog**", "Changelog of updates to the bot.", false);
			eb.addField("**addidea**", "Adds an idea to the heap. Format: `idea`", false);
			eb.setFooter("Created by Dish#7522 | v" + VERSION);
			eb.setColor(Color.CYAN);

			sendMessage(event, event.getAuthor().getAsMention() + ", I've sent a message to your DMs!");

			event.getAuthor().openPrivateChannel().queue((channel) -> {

				channel.sendMessage(eb.build()).queue();

			});

		} else {

			EmbedBuilder eb = new EmbedBuilder();
			eb.setAuthor("uO Hunger Games Bot", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
			Boolean flag = true;

			switch (msg[1]) {

			case "join":
				eb.setTitle("hg!join");
				eb.addField("**Format**", "`avatar_url (optional)|name (optional)`", false);
				eb.addField("**Example (Profile Picture and Name)**",
						"hg!join https://media.sproutsocial.com/uploads/2017/02/10x-featured-social-media-image-size.png Player",
						false);
				eb.addField("**Example (Profile Picture Only)**",
						"hg!join https://media.sproutsocial.com/uploads/2017/02/10x-featured-social-media-image-size.png",
						false);
				eb.addField("**Example (Name Only)**", "hg!join Player", false);
				break;
			case "edit":
				eb.setTitle("hg!join");
				eb.addField("**Format**", "`url|name`", false);
				eb.addField("**Example (Profile Picture and Name)**",
						"hg!edit https://media.sproutsocial.com/uploads/2017/02/10x-featured-social-media-image-size.png Player",
						false);
				eb.addField("**Example (Profile Picture Only)**",
						"hg!edit https://media.sproutsocial.com/uploads/2017/02/10x-featured-social-media-image-size.png",
						false);
				eb.addField("**Example (Name Only)**", "hg!edit Player", false);
				break;
			case "delete":
				eb.setTitle("hg!delete");
				eb.addField("**Format**", "`{MODS ONLY} district #|player#  (1/2))`", false);
				eb.addField("**Example (First District, 2nd Player)**", "hg!delete 1 2", false);
				break;
			case "addevent":
				eb.setTitle("hg!addevent");
				eb.addField("**Format**",
						"`'event'|# of Tributes (1/2)|death (yes/no)|(Required if death) who dies? (Player1/Player2)`",
						false);
				eb.addField("**Example (2 Players, with Death)**",
						"hg!addevent \"[Player1] eviscerates [Player2].\" 2 yes Player2", false);
				eb.addField("**Example (1 Player, with Death)**",
						"hg!addevent \"[Player1] drops off the face of this Earth.\" 1 yes Player1", false);
				eb.addField("**Example (1 Player, no Death)**",
						"hg!addevent \"[Player1] cries themselves to sleep.\" 1 no", false);
				break;
			case "idea":
				eb.setTitle("hg!addidea");
				eb.addField("**Format**", "idea", false);
				eb.addField("**Example**", "hg!addidea We want more events!", false);
				break;
			default:
				flag = false;
				sendMessage(event,
						":x: Illegal usage! Format: `hg!help (optional)[join/edit/delete/addevent/addidea]`");
				break;

			}

			if (flag) {
				eb.setColor(Color.CYAN);
				eb.setFooter("Created by Dish#7522 | v" + VERSION);

				event.getChannel().sendMessage(eb.build()).queue();
			}

		}

	}

	public void displayChangelog(MessageReceivedEvent event) throws IOException {

		EmbedBuilder eb = new EmbedBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("changelog.txt")));
		String line = "";

		while (br.ready()) {

			line += br.readLine() + "\n";

		}

		br.close();

		eb.setAuthor("uO Hunger Games Bot", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
		eb.setDescription(line);
		eb.setColor(Color.CYAN);
		eb.setFooter("Created by Dish#7522 | v" + VERSION);

		sendMessage(event, event.getAuthor().getAsMention() + ", I've sent a message to your DMs!");

		event.getAuthor().openPrivateChannel().queue((channel) -> {

			channel.sendMessage(eb.build()).queue();

		});

	}

	public boolean imgExist(String url) {

		try {

			BufferedImage img = ImageIO.read(new URL(url));

			return (img != null);

		} catch (MalformedURLException e) {

			return false;

		} catch (IOException e) {

			return false;

		}

	}

	public void setIdeas(MessageReceivedEvent event) {

		if (event.getMessage().getContentRaw().replace("hg!addidea", "").replace(" ", "").isEmpty()) {
			sendMessage(event, ":x: You must add an idea! Usage: `hg!addidea We want more events`");
		} else {

			ideas.add(event.getMessage().getContentRaw().replace("hg!addidea", ""));
			sendMessage(event, "Your idea has been added! Thanks for your feedback :)");

		}
	}

	public void getIdeas(MessageReceivedEvent event) {

		EmbedBuilder eb = new EmbedBuilder();
		String str = "";

		if (ideas.size() == 0)
			str = "No ideas in memory!";
		else {

			for (int i = 0; i < ideas.size(); i++) {

				str = (i + 1) + ". " + ideas.get(i) + "\n\n";
				ideas.remove(i);

				if (str.length() > 1900) {

					str = "Char limit exceeded";
					break;

				}

			}

		}

		eb.setDescription(str);
		eb.setColor(Color.BLUE);

		event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(eb.build()).queue());

	}

	public ArrayList<Player> getPlayerList() {

		return playerList;

	}

	public void sendMessage(MessageReceivedEvent event, String message) {

		event.getChannel().sendMessage(message).queue();

	}

	public int playerCount() {

		int count = 0;

		for (Player p : playerList) {

			if (p.getHP() > 0)
				count++;

		}

		return count;

	}

}
