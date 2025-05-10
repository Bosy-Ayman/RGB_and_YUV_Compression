import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class ImageUtilsQV {

    //Extract R G B from image
    public static int[][] extractComponent(BufferedImage img, char component) {
        int w = img.getWidth(), h = img.getHeight();
        int[][] comp = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (component == 'R') comp[y][x] = r;
                else if (component == 'G') comp[y][x] = g;
                else comp[y][x] = b;
            }
        }
        return comp;
    }

    //Compress to Codebook
    public static int[][] compressComponent(int[][] comp, List<double[]> codebook) {
        int h = comp.length, w = comp[0].length;
        int[][] idx = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                idx[y][x] = nearest(comp[y][x], codebook);
            }
        }
        return idx;
    }


    public static List<double[]> getBlocks(int[][] comp) {
        int blockSize = 2;
        int h = comp.length;
        int w = comp[0].length;
        int cropH = (h / blockSize) * blockSize;
        int cropW = (w / blockSize) * blockSize;
        List<double[]> blocks = new ArrayList<>();

        for (int y = 0; y < cropH; y += blockSize) {
            for (int x = 0; x < cropW; x += blockSize) {
                double[] block = new double[blockSize * blockSize];
                int idx = 0;
                for (int dy = 0; dy < blockSize; dy++) {
                    for (int dx = 0; dx < blockSize; dx++) {
                        block[idx++] = comp[y + dy][x + dx];
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }
    //decompress codebook into RGB again
    public static int[][] decompressComponent(int[][] idx, List<double[]> codebook) {
        int h = idx.length, w = idx[0].length;
        int[][] comp = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                comp[y][x] = (int) codebook.get(idx[y][x])[0];
            }
        }
        return comp;
    }

    //merge block we made to the image
    public static BufferedImage mergeComponents(int[][] r, int[][] g, int[][] b) {
        int h = r.length, w = r[0].length;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = (r[y][x] << 16) | (g[y][x] << 8) | b[y][x];
                img.setRGB(x, y, pixel);
            }
        }
        return img;
    }

    //calculate the nearest codebook to the pixel
    private static int nearest(int value, List<double[]> codebook) {
        double minDist = Double.MAX_VALUE;
        int best = 0;
        for (int i = 0; i < codebook.size(); i++) {
            double d = Math.abs(value - codebook.get(i)[0]);
            if (d < minDist) {
                minDist = d;
                best = i;
            }
        }
        return best;
    }

    //calculate PNSR
    public static double computePSNR(int[][] orig, int[][] recon) {
        double mse = 0.0;
        int h = orig.length, w = orig[0].length;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double diff = orig[y][x] - recon[y][x];
                mse += diff * diff;
            }
        }
        mse /= (h * w);
        return 10 * Math.log10(255 * 255 / mse);
    }
}
