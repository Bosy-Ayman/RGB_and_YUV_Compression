import java.util.*;

public class KMeans {
    public static List<double[]> cluster(List<double[]> data, int k) {
        Random rand = new Random();
        List<double[]> centroids = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            centroids.add(data.get(rand.nextInt(data.size())));
        }

        for (int t = 0; t < 20; t++) {
            List<List<double[]>> clusters = new ArrayList<>();
            for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

            for (double[] vec : data) {
                int idx = nearest(vec, centroids);
                clusters.get(idx).add(vec);
            }

            for (int i = 0; i < k; i++) {
                if (!clusters.get(i).isEmpty()) {
                    centroids.set(i, average(clusters.get(i)));
                }
            }
        }
        return centroids;
    }

    private static int nearest(double[] vec, List<double[]> centroids) {
        double minDist = Double.MAX_VALUE;
        int bestIdx = 0;
        for (int i = 0; i < centroids.size(); i++) {
            double dist = 0;
            for (int j = 0; j < vec.length; j++) {
                dist += Math.pow(vec[j] - centroids.get(i)[j], 2);
            }
            if (dist < minDist) {
                minDist = dist;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private static double[] average(List<double[]> vectors) {
        double[] avg = new double[vectors.get(0).length];
        for (double[] v : vectors)
            for (int i = 0; i < avg.length; i++)
                avg[i] += v[i];
        for (int i = 0; i < avg.length; i++)
            avg[i] /= vectors.size();
        return avg;
    }
}
