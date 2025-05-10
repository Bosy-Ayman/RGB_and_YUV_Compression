import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class YUVCompressor {

    public static void compressAndDecompressYUV(
            File imgFile,
            List<double[]> yCodebook,
            List<double[]> uCodebook,
            List<double[]> vCodebook
    ) throws Exception {
        System.out.println("[YUV] Loading image...");
        BufferedImage image = ImageIO.read(imgFile);
        if (image == null) {
            System.out.println("[YUV] Skipping (invalid image): " + imgFile.getName());
            return;
        }

        // Ensure width and height are divisible by 4 for proper subsampling and block processing
        int w0 = image.getWidth();
        int h0 = image.getHeight();
        int w = w0 - (w0 % 4);
        int h = h0 - (h0 % 4);
        if (w != w0 || h != h0) {
            image = image.getSubimage(0, 0, w, h);
            System.out.println("[YUV] Cropped to dimensions divisible by 4: " + w + "×" + h);
        }

        int blockSize = 2;

        System.out.println("[YUV] Converting RGB → YUV...");
        int[][][] yuv = ImageUtilsYUV.rgbToYuv(image);
        int[][] Y = yuv[0];
        int[][] U = yuv[1];
        int[][] V = yuv[2];

        System.out.println("[YUV] Sub-sampling U and V channels...");
        U = subSample(U);
        V = subSample(V);

        System.out.println("[YUV] Compressing Y, U, V channels...");
        int[][] cY = ImageUtilsYUV.compressChannel(Y, yCodebook, blockSize);
        int[][] cU = ImageUtilsYUV.compressChannel(U, uCodebook, blockSize);
        int[][] cV = ImageUtilsYUV.compressChannel(V, vCodebook, blockSize);

        System.out.println("[YUV] Decompressing Y, U, V channels...");
        int[][] rY = ImageUtilsYUV.decompressChannel(cY, yCodebook, blockSize);
        int[][] rU = ImageUtilsYUV.decompressChannel(cU, uCodebook, blockSize);
        int[][] rV = ImageUtilsYUV.decompressChannel(cV, vCodebook, blockSize);

        System.out.println("[YUV] Upsampling U and V...");
        rU = ImageUtilsYUV.upsample(rU);
        rV = ImageUtilsYUV.upsample(rV);

        System.out.println("[YUV] Converting YUV → RGB...");
        BufferedImage outputImage = ImageUtilsYUV.yuvToRgb(rY, rU, rV);

        File outDir = new File("output/yuv_decoded");
        if (!outDir.exists()) outDir.mkdirs();
        File outFile = new File(outDir, imgFile.getName().replaceAll("\\.[^.]+$", ".jpg"));

        System.out.println("[YUV] Saving output to " + outFile.getPath());
        ImageIO.write(outputImage, "jpg", outFile);
    }

    // average each non overlapping 2×2 block into one pixel
    private static int[][] subSample(int[][] channel) {
        int width = channel.length;
        int height = channel[0].length;
        int newW = width / 2;
        int newH = height / 2;
        int[][] sub = new int[newW][newH];

        for (int x = 0; x < newW; x++) {
            for (int y = 0; y < newH; y++) {
                int sum = channel[2*x][2*y]
                        + channel[2*x+1][2*y]
                        + channel[2*x][2*y+1]
                        + channel[2*x+1][2*y+1];
                sub[x][y] = sum / 4;
            }
        }
        return sub;
    }
}