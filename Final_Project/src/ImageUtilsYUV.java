
import java.awt.image.BufferedImage;
import java.util.List;

public class ImageUtilsYUV {

    private static final int BLOCK_SIZE = 2;


     // Convert an RGB image to separate Y,U,V 2D arrays
    public static int[][][] rgbToYuv(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] Y = new int[width][height];
        int[][] U = new int[width][height];
        int[][] V = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8)  & 0xFF;
                int b = rgb & 0xFF;
                // standard conversion
                Y[x][y] = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                U[x][y] = (int)(-0.14713 * r - 0.28886 * g + 0.436 * b);
                V[x][y] = (int)( 0.615   * r - 0.51499 * g - 0.10001 * b);
            }
        }
        return new int[][][]{ Y, U, V };
    }


     //sub-sample a channel by averaging each non-overlapping 2×2 block → 1 pixel.
     //Result is half width, half height.

    public static int[][] subSample(int[][] channel) {
        int width  = channel.length;
        int height = channel[0].length;
        int newW = width  / BLOCK_SIZE;
        int newH = height / BLOCK_SIZE;
        int[][] sub = new int[newW][newH];

        for (int x = 0; x < newW; x++) {
            for (int y = 0; y < newH; y++) {
                int sum = 0;
                // sum over 2×2
                for (int i = 0; i < BLOCK_SIZE; i++) {
                    for (int j = 0; j < BLOCK_SIZE; j++) {
                        sum += channel[x*BLOCK_SIZE + i][y*BLOCK_SIZE + j];
                    }
                }
                sub[x][y] = sum / (BLOCK_SIZE * BLOCK_SIZE);
            }
        }
        return sub;
    }


     //Compress one channel: split into BLOCK_SIZE×BLOCK_SIZE blocks,
     //find nearest vector in codebook, write back its index.
    public static int[][] compressChannel(int[][] channel, List<double[]> codebook, int blockSize) {
        int width  = channel.length;
        int height = channel[0].length;
        int bxCount = width  / blockSize;
        int byCount = height / blockSize;
        int[][] indices = new int[bxCount][byCount];

        for (int bx = 0; bx < bxCount; bx++) {
            for (int by = 0; by < byCount; by++) {
                double[] block = new double[blockSize * blockSize];
                int idx = 0;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        block[idx++] = channel[bx*blockSize + i][by*blockSize + j];
                    }
                }
                // find nearest codebook vector
                int   bestK = 0;
                double bestD = Double.MAX_VALUE;
                for (int k = 0; k < codebook.size(); k++) {
                    double dist = 0;
                    double[] vec = codebook.get(k);
                    for (int d = 0; d < vec.length; d++) {
                        double diff = block[d] - vec[d];
                        dist += diff * diff;
                    }
                    if (dist < bestD) {
                        bestD = dist;
                        bestK = k;
                    }
                }
                indices[bx][by] = bestK;
            }
        }
        return indices;
    }


    //Decompress one channel: replace each block index with its codebook vector
    public static int[][] decompressChannel(int[][] indices, List<double[]> codebook, int blockSize) {
        int bxCount = indices.length;
        int byCount = indices[0].length;
        int width  = bxCount * blockSize;
        int height = byCount * blockSize;
        int[][] channel = new int[width][height];

        for (int bx = 0; bx < bxCount; bx++) {
            for (int by = 0; by < byCount; by++) {
                double[] vec = codebook.get(indices[bx][by]);
                int idx = 0;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        channel[bx*blockSize + i][by*blockSize + j] = (int)vec[idx++];
                    }
                }
            }
        }
        return channel;
    }


    //Upsample a half-res channel back to full resolution by duplicating each pixel 2×2.

    public static int[][] upsample(int[][] channel) {
        int w = channel.length;
        int h = channel[0].length;
        int[][] full = new int[w*BLOCK_SIZE][h*BLOCK_SIZE];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int v = channel[x][y];
                full[x*BLOCK_SIZE    ][y*BLOCK_SIZE    ] = v;
                full[x*BLOCK_SIZE + 1][y*BLOCK_SIZE    ] = v;
                full[x*BLOCK_SIZE    ][y*BLOCK_SIZE + 1] = v;
                full[x*BLOCK_SIZE + 1][y*BLOCK_SIZE + 1] = v;
            }
        }
        return full;
    }


    //Convert YUV arrays back into an RGB BufferedImage.

    public static BufferedImage yuvToRgb(int[][] Y, int[][] U, int[][] V) {
        int width = Y.length;
        int height = Y[0].length;
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int yv = Y[x][y];
                int uv = U[x][y];
                int vv = V[x][y];
                int r = clamp(yv + (int)(1.13983 * vv));
                int g = clamp(yv - (int)(0.39465 * uv) - (int)(0.58060 * vv));
                int b = clamp(yv + (int)(2.03211 * uv));
                int rgb = (r << 16) | (g << 8) | b;
                out.setRGB(x, y, rgb);
            }
        }
        return out;
    }

    // keep value in [0..255]
    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
