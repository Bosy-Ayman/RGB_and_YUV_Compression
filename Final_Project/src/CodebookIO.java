import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CodebookIO {


     //Save a list of codebook vectors to a text file.
     // Each vector is written on its own line, values separated by spaces.

    public static void saveCodebook(List<double[]> codebook, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (double[] vector : codebook) {
                StringBuilder sb = new StringBuilder();
                for (double value : vector) {
                    sb.append(value).append(" ");
                }
                writer.write(sb.toString().trim());
                writer.newLine();
            }
        }
    }


     //Load a codebook from a text file
     // Each line is parsed into a double[] and added to the returned list

    public static List<double[]> loadCodebook(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            List<double[]> codebook = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split("\\s+");
                double[] vector = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    vector[i] = Double.parseDouble(values[i]);
                }
                codebook.add(vector);
            }
            return codebook;
        }
    }


    public static double calculateCompressionRatio(int width,
                                                   int height,
                                                   int blockSize,
                                                   int codebookSize) {
        // Original image: 8 bits per channel, 3 channels (RGB)
        double origBits = 8.0 * 3 * width * height;

        // Compressed: each block is replaced by an index.
        double numBlocks = (double) width * height / (blockSize * blockSize);
        double bitsPerIndex = Math.log(codebookSize) / Math.log(2);

        double compressedBits = numBlocks * bitsPerIndex;
        return origBits / compressedBits;
    }
}
