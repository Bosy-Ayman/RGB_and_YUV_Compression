import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class VQCompressor {

    public static void compressAndDecompress(File imgFile,
                                             List<double[]> redCB,
                                             List<double[]> greenCB,
                                             List<double[]> blueCB) throws Exception {
        System.out.println("  [VQ] Loading image...");
        BufferedImage img = ImageIO.read(imgFile);
        if (img == null) {
            System.out.println("  [VQ] Skipping (invalid image): " + imgFile.getName());
            return;
        }

        System.out.println("  [VQ] Extracting RGB channels...");
        int[][] r = ImageUtilsQV.extractComponent(img, 'R');
        int[][] g = ImageUtilsQV.extractComponent(img, 'G');
        int[][] b = ImageUtilsQV.extractComponent(img, 'B');

        System.out.println("  [VQ] Compressing channels...");
        int[][] cr = ImageUtilsQV.compressComponent(r, redCB);
        int[][] cg = ImageUtilsQV.compressComponent(g, greenCB);
        int[][] cb = ImageUtilsQV.compressComponent(b, blueCB);

        System.out.println("  [VQ] Decompressing channels...");
        int[][] dr = ImageUtilsQV.decompressComponent(cr, redCB);
        int[][] dg = ImageUtilsQV.decompressComponent(cg, greenCB);
        int[][] db = ImageUtilsQV.decompressComponent(cb, blueCB);

        System.out.println("  [VQ] Merging channels...");
        BufferedImage out = ImageUtilsQV.mergeComponents(dr, dg, db);

        System.out.println("  [VQ] Calculating PSNR...");
        double psnr = ImageUtilsQV.computePSNR(r, dr);
        System.out.printf("  [VQ] PSNR: %.2f dB%n", psnr);

        File outDir = new File("output/decoded_rgb");
        if (!outDir.exists()) outDir.mkdirs();

        File outFile = new File(outDir, imgFile.getName().replaceAll("\\.[^.]+$", ".png"));
        System.out.println("  [VQ] Saving output to " + outFile.getPath());
        ImageIO.write(out, "png", outFile);
    }
}
