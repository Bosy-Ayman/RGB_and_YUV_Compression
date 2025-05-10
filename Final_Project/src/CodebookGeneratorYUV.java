import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

public class CodebookGeneratorYUV {

    private static final int BLOCK_SIZE = 2;


    public static List<double[]> generate(String trainingDir,char channel,int codebookSize) throws IOException {

        List<double[]> allBlocks = new ArrayList<>();

        File dir = new File(trainingDir);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + trainingDir);
        }
        // Queue for recursive folder traversal
        Queue<File> queue = new LinkedList<>();
        queue.add(dir);

        while (!queue.isEmpty()) {
            File f = queue.poll();
            if (f.isDirectory()) {
                Collections.addAll(queue, f.listFiles());
            } else {
                // Accept only image files
                String name = f.getName().toLowerCase();
                if (name.endsWith(".jpg") || name.endsWith(".jpeg")
                        || name.endsWith(".png") || name.endsWith(".bmp")) {

                    BufferedImage img = ImageIO.read(f);
                    if (img == null) continue;

                    // Convert image to YUV
                    int[][][] yuv = ImageUtilsYUV.rgbToYuv(img);
                    int[][] channelData;
                    switch (channel) {
                        case 'Y':
                            channelData = yuv[0];
                            break;
                        case 'U':
                            channelData = ImageUtilsYUV.subSample(yuv[1]);
                            break;
                        case 'V':
                            channelData = ImageUtilsYUV.subSample(yuv[2]);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported channel: " + channel);
                    }

                    // Extract 2×2 non-overlapping blocks from the channel
                    int w = channelData.length - (channelData.length % BLOCK_SIZE);
                    int h = channelData[0].length - (channelData[0].length % BLOCK_SIZE);
                    for (int x = 0; x < w; x += BLOCK_SIZE) {
                        for (int y = 0; y < h; y += BLOCK_SIZE) {
                            double[] block = new double[BLOCK_SIZE * BLOCK_SIZE];
                            int idx = 0;
                            for (int i = 0; i < BLOCK_SIZE; i++) {
                                for (int j = 0; j < BLOCK_SIZE; j++) {
                                    block[idx++] = channelData[x + i][y + j];
                                }
                            }
                            allBlocks.add(block);
                        }
                    }
                }
            }
        }
        // get the codebook
        return KMeans.cluster(allBlocks, codebookSize);
    }
}
