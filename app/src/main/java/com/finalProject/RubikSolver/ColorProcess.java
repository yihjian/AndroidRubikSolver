package com.finalProject.RubikSolver;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ColorProcess {

    private static final int[] RED = {255, 0, 0};
    private static final int[] ORANGE = {255, 150, 0};
    private static final int[] GREEN = {0, 255, 0};
    private static final int[] BLUE = {0, 0, 255};
    private static final int[] YELLOW = {255, 255, 0};
    private static final int[] WHITE = {255, 255, 255};

    public static double[] averageColor(Bitmap bm) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        int s = w * h;
        double r = 0;
        double g = 0;
        double b = 0;
        int[] pixels = new int[s];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int c = pixels[i * w + j];
                int pr = Color.red(c);
                int pg = Color.green(c);
                int pb = Color.blue(c);
                if (pr < 75 && pg < 75 && pb < 75) {
                    s--;
                    continue;
                }
                r += pr * pr;
                g += pg * pg;
                b += pb * pb;
            }
        }
        r = Math.sqrt(r / (double) s);
        g = Math.sqrt(g / (double) s);
        b = Math.sqrt(b / (double) s);
        double[] rgb = new double[]{r, g, b};
        return rgb;
    }

    public static String getColorName(double[] rgb) {
        double r = rgb[0];
        double g = rgb[1];
        double b = rgb[2];
        double[] dist = new double[6];

        dist[0] = Math.sqrt(Math.pow((r - RED[0]), 2) + Math.pow((g - RED[1]), 2) + Math.pow((b - RED[2]), 2));
        dist[1] = Math.sqrt(Math.pow((r - ORANGE[0]), 2) + Math.pow((g - ORANGE[1]), 2) + Math.pow((b - ORANGE[2]), 2));
        dist[2] = Math.sqrt(Math.pow((r - GREEN[0]), 2) + Math.pow((g - GREEN[1]), 2) + Math.pow((b - GREEN[2]), 2));
        dist[3] = Math.sqrt(Math.pow((r - BLUE[0]), 2) + Math.pow((g - BLUE[1]), 2) + Math.pow((b - BLUE[2]), 2));
        dist[4] = Math.sqrt(Math.pow((r - YELLOW[0]), 2) + Math.pow((g - YELLOW[1]), 2) + Math.pow((b - YELLOW[2]), 2));
        dist[5] = Math.sqrt(Math.pow((r - WHITE[0]), 2) + Math.pow((g - WHITE[1]), 2) + Math.pow((b - WHITE[2]), 2));

        double min = dist[0];
        int index = 0;
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] < min) {
                min = dist[i];
                index = i;
            }
        }

        switch (index) {
            case 0:
                return "r";
            case 1:
                return "o";
            case 2:
                return "g";
            case 3:
                return "b";
            case 4:
                return "y";
            default:
                    return "w";
        }

    }

}
