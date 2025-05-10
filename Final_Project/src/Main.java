import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String trainingDir = "training";
        String testDir     = "test";

        System.out.println("=== Generating RGB codebooks ===");
        List<double[]> redCB   = CodebookGenerator.generate(trainingDir, 'R');
        List<double[]> greenCB = CodebookGenerator.generate(trainingDir, 'G');
        List<double[]> blueCB  = CodebookGenerator.generate(trainingDir, 'B');
        CodebookIO.saveCodebook(redCB,"codebooks/redCB.txt");
        CodebookIO.saveCodebook(greenCB, "codebooks/greenCB.txt");
        CodebookIO.saveCodebook(blueCB,"codebooks/blueCB.txt");

        // Codebook sizes
        int cbY   = 256;   // Y luminance
        int cbUV  = 64;    // U/V chroma subsampled

        System.out.println("=== Generating YUV codebooks ===");
        List<double[]> yCB = CodebookGeneratorYUV.generate(trainingDir, 'Y', cbY);
        List<double[]> uCB = CodebookGeneratorYUV.generate(trainingDir, 'U', cbUV);
        List<double[]> vCB = CodebookGeneratorYUV.generate(trainingDir, 'V', cbUV);
        CodebookIO.saveCodebook(yCB, "codebooks/yCB.txt");
        CodebookIO.saveCodebook(uCB, "codebooks/uCB.txt");
        CodebookIO.saveCodebook(vCB, "codebooks/vCB.txt");


        System.out.println("=== Processing test images ===");
        File testFolder = new File(testDir);
        for (File category : testFolder.listFiles()) {
            if (!category.isDirectory()) continue;
            System.out.println("-> Category: " + category.getName());

            for (File imgFile : category.listFiles()) {
                System.out.println(" * Image: " + imgFile.getName());

                // Load image once for dimensions
                BufferedImage img = ImageIO.read(imgFile);
                if (img == null) {
                    System.out.println("   [!] Cannot read image, skipping.");
                    continue;
                }
                int width  = img.getWidth();
                int height = img.getHeight();

                // --- RGB VQ ---
                System.out.println("   [RGB VQ]");
                VQCompressor.compressAndDecompress(imgFile, redCB, greenCB, blueCB);
                double rgbRatio = CodebookIO.calculateCompressionRatio(width, height, 1, redCB.size())
                        + CodebookIO.calculateCompressionRatio(width, height, 1, greenCB.size())
                        + CodebookIO.calculateCompressionRatio(width, height, 1, blueCB.size());
                rgbRatio /= 3.0;
                System.out.printf("   RGB VQ Compression Ratio: %.2f%n", rgbRatio);

                // --- YUV VQ ---
                System.out.println("   [YUV VQ]");
                YUVCompressor.compressAndDecompressYUV(imgFile, yCB, uCB, vCB);
                double yRatio = CodebookIO.calculateCompressionRatio(width, height, 2, yCB.size());
                double uRatio = CodebookIO.calculateCompressionRatio(width/2, height/2, 2, uCB.size());
                double vRatio = CodebookIO.calculateCompressionRatio(width/2, height/2, 2, vCB.size());
                double yuvRatio = (yRatio + uRatio + vRatio) / 3.0;
                System.out.printf("   YUV VQ Compression Ratio: %.2f%n", yuvRatio);

                // --- Comparison ---
                double compRatio = rgbRatio / yuvRatio;
                System.out.printf("   RGB/YUV Ratio: %.2f%n", compRatio);
                if (rgbRatio > yuvRatio) {
                    System.out.println("   => RGB VQ compresses better by a factor of "
                            + String.format("%.2f", compRatio));
                } else if (yuvRatio > rgbRatio) {
                    System.out.println("   => YUV VQ compresses better by a factor of "
                            + String.format("%.2f", 1/compRatio));
                } else {
                    System.out.println("   => Both methods compress equally.");
                }
            }
        }

        System.out.println("=== All done! ===");
    }
}
