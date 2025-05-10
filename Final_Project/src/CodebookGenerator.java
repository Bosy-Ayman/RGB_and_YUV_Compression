import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CodebookGenerator {
    public static List<double[]> generate(String dir, char comp) throws Exception {
        List<double[]> vecs = new ArrayList<>();
        for (File category : new File(dir).listFiles()) {
            if (!category.isDirectory()) continue;
            File[] imgs = category.listFiles();
            if (imgs == null) continue;
            int count = 0;
            for (File img : imgs) {
                if (count++ >= 10) break; // Only take first 10 images per category
                BufferedImage bi = ImageIO.read(img);
                if (bi == null) continue;
                vecs.addAll(ImageUtilsQV.getBlocks(ImageUtilsQV.extractComponent(bi, comp)));
            }
        }
        return KMeans.cluster(vecs, 256); // Cluster into 256 code vectors
    }
}
