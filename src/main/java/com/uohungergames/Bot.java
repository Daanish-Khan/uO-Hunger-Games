package com.uohungergames;

import javax.security.auth.login.LoginException;

import com.uohungergames.interactive.InteractiveHGCommands;
import com.uohungergames.original.HGCommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {

	public static void main(String[] args) throws LoginException {

		HGCommands hgCommands = new HGCommands();
		InteractiveHGCommands ihgCommands = new InteractiveHGCommands();
		String[] tokens = new String[] { "NjY4MjcxOTEyNDAwNTg0NzE0.XnEQ-g.u_MufurnCA7uJwg_RohmIv8YZWU", // Main Bot
				"NzI3MjU2NzQ1ODQ3NTU0MDU4.XvpOmg.46nxjvyyRkdrjDviSsI72-rRyOI" // Test Bot
		};

		JDA jda = new JDABuilder(tokens[1]).addEventListeners(new Listener(hgCommands, ihgCommands)).build();
		jda.getPresence().setActivity(Activity.playing("hg!help"));

	}

}
