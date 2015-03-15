package com.sendish.api.thumbnailator.filter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.coobird.thumbnailator.filters.ImageFilter;

public class TransparencyColorFilter implements ImageFilter {
	
	private final AlphaComposite composite;
	private final Color color;

	public TransparencyColorFilter(float alpha, Color color) {
		super();

		if (alpha < 0.0f || alpha > 1.0f) {
			throw new IllegalArgumentException("The alpha must be between 0.0f and 1.0f, inclusive.");
		}

		this.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		this.color = color;
	}

	
	public TransparencyColorFilter(float alpha) {
		this(alpha, Color.BLACK);
	}

	public BufferedImage apply(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();

		BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = finalImage.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.setComposite(composite);
		g.setColor(color);
    	g.fillRect(0, 0, width, height);
		g.dispose();

		return finalImage;
	}

	public float getAlpha() {
		return composite.getAlpha();
	}

	public Color getColor() {
		return color;
	}
	
}
