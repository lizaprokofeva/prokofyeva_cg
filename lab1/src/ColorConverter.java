import java.awt.Color;

public class ColorConverter {

    //RGB <-> CMYK

    public static double[] rgbToCmyk(int r, int g, int b) {
        double dr = r / 255.0;
        double dg = g / 255.0;
        double db = b / 255.0;

        double k = 1.0 - Math.max(dr, Math.max(dg, db));
        if (Math.abs(k - 1.0) < 1e-6) {
            return new double[]{0.0, 0.0, 0.0, 100.0};
        }

        double c = (1.0 - dr - k) / (1.0 - k) * 100.0;
        double m = (1.0 - dg - k) / (1.0 - k) * 100.0;
        double y = (1.0 - db - k) / (1.0 - k) * 100.0;
        double kPercent = k * 100.0;

        return new double[]{clamp(c, 0, 100), clamp(m, 0, 100), clamp(y, 0, 100), clamp(kPercent, 0, 100)};
    }

    public static Color cmykToRgb(double cPercent, double mPercent, double yPercent, double kPercent) {
        double c = cPercent / 100.0;
        double m = mPercent / 100.0;
        double y = yPercent / 100.0;
        double k = kPercent / 100.0;

        double r = (1.0 - c) * (1.0 - k);
        double g = (1.0 - m) * (1.0 - k);
        double b = (1.0 - y) * (1.0 - k);

        int red = (int) Math.round(clamp(r * 255.0, 0, 255));
        int green = (int) Math.round(clamp(g * 255.0, 0, 255));
        int blue = (int) Math.round(clamp(b * 255.0, 0, 255));

        return new Color(red, green, blue);
    }

    // RGB <-> HLS

    public static double[] rgbToHls(int r, int g, int b) {
        double dr = r / 255.0;
        double dg = g / 255.0;
        double db = b / 255.0;

        double max = Math.max(dr, Math.max(dg, db));
        double min = Math.min(dr, Math.min(dg, db));
        double delta = max - min;

        double l = (max + min) / 2.0;
        double h = 0.0;
        double s = 0.0;

        if (delta > 1e-6) {
            s = (l > 0.5) ? (delta / (2.0 - max - min)) : (delta / (max + min));

            if (max == dr) {
                h = (dg - db) / delta + (dg < db ? 6.0 : 0.0);
            } else if (max == dg) {
                h = (db - dr) / delta + 2.0;
            } else {
                h = (dr - dg) / delta + 4.0;
            }
            h *= 60.0;
        }

        return new double[]{clamp(h, 0, 360), clamp(l * 100.0, 0, 100), clamp(s * 100.0, 0, 100)};
    }

    public static Color hlsToRgb(double hDeg, double lPercent, double sPercent) {
        double h = hDeg / 360.0;
        double l = lPercent / 100.0;
        double s = sPercent / 100.0;

        double r, g, b;

        if (s == 0) {
            r = g = b = l;
        } else {
            double q = (l < 0.5) ? (l * (1.0 + s)) : (l + s - l * s);
            double p = 2.0 * l - q;

            r = hueToRgb(p, q, h + 1.0 / 3.0);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0 / 3.0);
        }

        int red = (int) Math.round(clamp(r * 255.0, 0, 255));
        int green = (int) Math.round(clamp(g * 255.0, 0, 255));
        int blue = (int) Math.round(clamp(b * 255.0, 0, 255));

        return new Color(red, green, blue);
    }

    private static double hueToRgb(double p, double q, double t) {
        if (t < 0.0) t += 1.0;
        if (t > 1.0) t -= 1.0;
        if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t;
        if (t < 1.0 / 2.0) return q;
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0;
        return p;
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}