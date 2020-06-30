package com.uohungergames;

import java.io.IOException;

import com.uohungergames.interactive.InteractiveHGCommands;
import com.uohungergames.original.HGCommands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {

	private HGCommands hgCommands;
	private InteractiveHGCommands ihgCommands;

	public Listener(HGCommands hgCommands, InteractiveHGCommands ihgCommands) {

		this.hgCommands = hgCommands;
		this.ihgCommands = ihgCommands;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		// Check if message sent was by a bot
		if (event.getAuthor().isBot())
			return;

		Boolean perms = false;

		// Checks if user has perms for certain commands
		for (Role role : event.getMember().getRoles()) {

			if (role.getName().equals("hunger games manager") || role.getName().equals("Mod"))
				perms = true;

		}

		// Add ideas
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!addidea")) {

			hgCommands.setIdeas(event);
			event.getMessage().addReaction("U+2705").queue();

		}

		// Get Ideas
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!getideas")) {

			if (perms) {
				hgCommands.getIdeas(event);
				event.getMessage().addReaction("U+2705").queue();
			} else
				noPerms(event.getChannel());

		}

		// Ping
		if (event.getMessage().getContentRaw().equals("hg!ping")) {
			MessageChannel channel = event.getChannel();
			long time = System.currentTimeMillis();
			channel.sendMessage("Pong!") /* => RestAction<Message> */
					.queue(response /* => Message */ -> {
						response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
					});
		}

		// Adds player to game
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!join")) {

			if (!hgCommands.hasStarted()) {

				if (hgCommands.getPlayerList().size() != 24) {

					// Checks if the user already added a player
					if (!hgCommands.checkUserAdded(event.getMember().getId())) {

						hgCommands.addPlayer(event);
						event.getMessage().addReaction("U+2705").queue();

					} else if (!perms) {

						hgCommands.sendMessage(event, ":x: " + event.getMember().getAsMention()
								+ ", you already have a player added! Use hg!me to view your player!");

					} else {

						hgCommands.addPlayer(event);
						event.getMessage().addReaction("U+2705").queue();

					}

				} else {

					hgCommands.sendMessage(event, ":x: " + event.getAuthor().getAsMention()
							+ ", This hunger games has reached its max amount of players!");

				}

			} else {

				hgCommands.sendMessage(event, ":x: The game has already started!");

			}
		}

		// Gets profile of the player
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!me")) {

			if (hgCommands.checkUserAdded(event.getMember().getId())) {

				hgCommands.profileDisplay(event);
				event.getMessage().addReaction("U+2705").queue();

			} else {

				hgCommands.sendMessage(event, ":x: " + event.getMember().getAsMention()
						+ ", you do not have a player added! Please use `hg!join` to add your player!");

			}

		}

		// Delete profile
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!delete")) {

			if (!hgCommands.hasStarted()) {

				if (event.getMessage().getContentRaw().split(" ").length > 1 && perms) {

					hgCommands.deletePlayer(event);
					event.getMessage().addReaction("U+2705").queue();

				} else if (event.getMessage().getContentRaw().split(" ").length == 1) {

					hgCommands.deletePlayer(event);
					event.getMessage().addReaction("U+2705").queue();

				} else {

					noPerms(event.getChannel());

				}

			} else {

				hgCommands.sendMessage(event, ":x: The game has already started!");

			}

		}

		// Edit profile
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!edit")) {

			if (event.getTextChannel().getName().contains("team")) {

				ihgCommands.editCharacter(event);
				event.getMessage().addReaction("U+2705").queue();

			} else {

				if (!hgCommands.hasStarted()) {

					hgCommands.editPlayer(event);
					event.getMessage().addReaction("U+2705").queue();

				} else {

					hgCommands.sendMessage(event, ":x: The game has already started!");

				}

			}

		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!addevent")) {

			if (!hgCommands.hasStarted()) {

				hgCommands.addEvent(event);
				event.getMessage().addReaction("U+2705").queue();

			} else {

				hgCommands.sendMessage(event, ":x: The game has already started!");

			}

		}

		// Reset playerlist
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!reset") && perms) {

			if (perms) {

				hgCommands.resetPlayers(event);
				event.getMessage().addReaction("U+2705").queue();

			} else
				noPerms(event.getChannel());

		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!stopfuckingcrashing") && perms) {

			if (perms) {

				hgCommands.botCrashReset(event);
				event.getMessage().addReaction("U+2705").queue();

			} else
				noPerms(event.getChannel());

		}

		// Displays players
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!players")) {

			hgCommands.displayPlayers(event);
			event.getMessage().addReaction("U+2705").queue();

		}

		// Starts game
		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!start")) {

			if (perms) {

				if (!hgCommands.hasStarted()) {

					if (hgCommands.getPlayerList().size() >= 2
							|| event.getMessage().getContentRaw().split(" ").length > 1) {

						hgCommands.start(event, event.getMessage().getContentRaw());

						event.getMessage().addReaction("U+2705").queue();

					} else {

						hgCommands.sendMessage(event, "Not enough players to start!");

					}

				} else {

					hgCommands.sendMessage(event, ":x: The game has already started!");

				}

			} else
				noPerms(event.getChannel());

		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!help")) {

			hgCommands.help(event);
			event.getMessage().addReaction("U+2705").queue();

		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!changelog")) {

			try {
				hgCommands.displayChangelog(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
			event.getMessage().addReaction("U+2705").queue();

		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!character")) {

			if (event.getChannel().getName().contains("team")) {

				ihgCommands.viewCharacter(event.getChannel());
				event.getMessage().addReaction("U+2705").queue();

			} else {

				hgCommands.sendMessage(event, ":x: You must be in a team channel to use that command!");

			}

		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!map")) {

			if (event.getChannel().getName().contains("team")) {

				ihgCommands.viewCharacterMap(event);
				event.getMessage().addReaction("U+2705").queue();

			} else {

				hgCommands.sendMessage(event, ":x: You must be in a team channel to use that command!");

			}

		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!removerole")) {
			hgCommands.removeRole(event);
			event.getMessage().addReaction("U+2705").queue();
		}

		if (event.getMessage().getContentRaw().split(" ")[0].equals("hg!test")) {

			System.out.println(event.getAuthor().getId());

		}

	}

	public void noPerms(MessageChannel channel) {

		channel.sendMessage(":x: You do not have the permissions for this command! :x:").queue();

	}

}
