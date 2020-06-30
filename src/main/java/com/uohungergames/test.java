package com.uohungergames;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class test {

	public static void main(String[] args) {

		BufferedImage img = null;

		try {
			img = ImageIO.read(new File("stats2.png"));
			BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

			Graphics2D g2d = img2.createGraphics();
			g2d.drawImage(img, 0, 0, null);

			int arcWidth = 20;
			int arcHeight = 20;
			int width = 300;

			int hp = 10;
			int atk = 20;
			int def = 30;
			int cool = 50;

			int textHeight = 18;

			g2d.setColor(Color.decode("#494C54"));
			g2d.fillRoundRect(116, 6, width, 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 48, width, 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 90, width, 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 132, width, 21, arcWidth, arcHeight);

			g2d.setColor(Color.decode("#353E54"));
			g2d.fillRoundRect(116, 6, (int) ((hp / 100f) * width), 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 48, (int) ((atk / 100f) * width), 21, arcWidth, arcHeight);
			g2d.fillRoundRect(116, 90, (int) ((def / 100f) * width), 21, arcWidth, arcHeight);

			g2d.setColor(Color.decode("#DBD68A"));
			g2d.fillRoundRect(116, 132, (int) ((cool / 100f) * width), 21, arcWidth, arcHeight);

			Font f = Font.createFont(Font.TRUETYPE_FONT, new File("ARCADECLASSIC.TTF"));
			g2d.setFont(f.deriveFont(Font.PLAIN, 25));

			g2d.setColor(Color.white);
			g2d.drawString(String.valueOf(hp), 116 + ((int) ((hp / 100f) * width) / 2)
					- (g2d.getFontMetrics().stringWidth(String.valueOf(hp)) / 2), 6 + textHeight);
			g2d.drawString(String.valueOf(atk), 116 + ((int) ((atk / 100f) * width) / 2)
					- (g2d.getFontMetrics().stringWidth(String.valueOf(atk)) / 2), 48 + textHeight);
			g2d.drawString(String.valueOf(def), 116 + ((int) ((def / 100f) * width) / 2)
					- (g2d.getFontMetrics().stringWidth(String.valueOf(def)) / 2), 90 + textHeight);

			g2d.setColor(Color.decode("#332464"));
			g2d.drawString(String.valueOf(cool), 116 + ((int) ((cool / 100f) * width) / 2)
					- (g2d.getFontMetrics().stringWidth(String.valueOf(cool)) / 2), 132 + textHeight);

			g2d.dispose();

			ImageIO.write(img2, "png", new File("stats3.png"));
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}

	}

}
