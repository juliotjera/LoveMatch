package com.esmia.lovematch.util;

import com.esmia.lovematch.enumeration.Genre;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Portraits des profils de démonstration : des silhouettes dessinées sur un fond coloré,
 * jamais des photos de vraies personnes.
 */
public final class Illustration {

    public static final int LARGEUR = 720;
    public static final int HAUTEUR = 960;

    // fonds : couleurs chaudes de Madagascar (jacaranda, flamboyant, lagon, latérite, vanille)
    private static final Color[][] FONDS = {
        {new Color(0x6B4FA0), new Color(0xC9A8E8)},
        {new Color(0xD9534F), new Color(0xF7B58C)},
        {new Color(0x1F7A8C), new Color(0x9FD8CB)},
        {new Color(0xA0522D), new Color(0xE8B07A)},
        {new Color(0x3D405B), new Color(0xE07A5F)},
        {new Color(0x8E3B62), new Color(0xF2A7C3)},
    };
    private static final Color[] PEAUX = {
        new Color(0x5A3825), new Color(0x7A4E32), new Color(0x8D5B3C), new Color(0xA86E4A), new Color(0x6B4226),
    };
    private static final Color[] VETEMENTS = {
        new Color(0xF4F1DE), new Color(0x264653), new Color(0xE9C46A), new Color(0x2A9D8F), new Color(0x1D3557),
    };

    private Illustration() {}

    public static BufferedImage portrait(Genre genre, int graine) {
        BufferedImage image = new BufferedImage(LARGEUR, HAUTEUR, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = preparer(image);
        Color[] fond = FONDS[Math.floorMod(graine, FONDS.length)];
        g.setPaint(new GradientPaint(0, 0, fond[0], 0, HAUTEUR, fond[1]));
        g.fillRect(0, 0, LARGEUR, HAUTEUR);

        // halo derrière la tête
        g.setColor(new Color(255, 255, 255, 46));
        g.fill(new Ellipse2D.Double(110, 120, 500, 500));

        Color peau = PEAUX[Math.floorMod(graine * 7, PEAUX.length)];
        Color cheveux = new Color(0x1E1410);
        Color vetement = VETEMENTS[Math.floorMod(graine * 3, VETEMENTS.length)];

        if (genre == Genre.FEMME) {
            // cheveux longs derrière les épaules
            g.setColor(cheveux);
            g.fill(new RoundRectangle2D.Double(205, 230, 310, 470, 160, 160));
        }
        // épaules et buste
        g.setColor(vetement);
        g.fill(new RoundRectangle2D.Double(110, 700, 500, 420, 260, 260));
        // cou
        g.setColor(peau.darker());
        g.fill(new RoundRectangle2D.Double(315, 560, 90, 170, 40, 40));
        // visage
        g.setColor(peau);
        g.fill(new Ellipse2D.Double(240, 300, 240, 300));
        // cheveux sur le dessus de la tête
        g.setColor(cheveux);
        Path2D frange = new Path2D.Double();
        if (genre == Genre.FEMME) {
            frange.moveTo(232, 440);
            frange.curveTo(220, 250, 500, 220, 488, 440);
            frange.curveTo(450, 330, 300, 330, 232, 440);
        } else {
            frange.moveTo(240, 400);
            frange.curveTo(235, 270, 485, 270, 480, 400);
            frange.curveTo(440, 345, 290, 345, 240, 400);
        }
        g.fill(frange);
        // col du vêtement
        g.setColor(new Color(0, 0, 0, 40));
        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(300, 660, 120, 90, 200, 140);
        g.dispose();
        return image;
    }

    private static Graphics2D preparer(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }
}
