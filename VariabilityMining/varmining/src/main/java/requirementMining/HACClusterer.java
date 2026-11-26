package requirementMining;

import requirementMining.helper.ClusterNode;
import java.util.*;

/**
 * Description: Performs Hierarchical Agglomerative Clustering (HAC) on a pairwise similarity matrix
 * and automatically determines the optimal cut threshold using Dunn, Calinski-Harabaz, and Silhouette indices.
 */
public class HACClusterer {

    /**
     * Description: Evaluation criteria for selecting the optimal cut level in the HAC dendrogram.
     *   SILHOUETTE (recommended default): Compares intra- vs. nearest-cluster distance across all levels
     *       (2 <= k <= n - 1) and naturally penalizes singletons without needing an upper cluster limit.
     *   COMBINED: Equal-weighted normalized average of Dunn, Calinski-Harabaz, and Silhouette
     *       (bounded to k <= n / 2); provides balanced, medium-grained feature clusters.
     *   DUNN: Maximizes min inter-cluster distance / max intra-cluster diameter (bounded to k <= n / 2);
     *       tends to produce fewer, coarser high-level features.
     *   CALINSKI_HARABAZ: Maximizes between- vs. within-cluster variance ratio (bounded to k <= n / 2);
     *       tends to produce more fine-grained clusters.
     */
    public enum ThresholdCriterion {
        DUNN,
        CALINSKI_HARABAZ,
        SILHOUETTE,
        COMBINED
    }

    private static class InternalCluster {
        final int id;
        final List<Integer> memberIndices;
        final double mergeDistance;

        InternalCluster(int id, List<Integer> memberIndices, double mergeDistance) {
            this.id = id;
            this.memberIndices = memberIndices;
            this.mergeDistance = mergeDistance;
        }
    }

    private static class PartitionSnapshot {
        final List<InternalCluster> clusters;
        final double cutDistance;

        PartitionSnapshot(List<InternalCluster> clusters, double cutDistance) {
            this.clusters = clusters;
            this.cutDistance = cutDistance;
        }
    }

    /**
     * Description: Clusters requirements based on the similarity matrix using automatic threshold optimization.
     *
     * @param similarityMatrix NxN pairwise similarity matrix with values in [0, 1].
     * @param requirementIds   Ordered list of requirement IDs matching matrix indices.
     * @param criterion        Index criterion for selecting the optimal dendrogram cut.
     * @return List of ClusterNode objects representing the optimal feature clusters.
     */
    public List<ClusterNode> cluster(double[][] similarityMatrix, List<String> requirementIds, ThresholdCriterion criterion) {
        int n = requirementIds.size();
        if (n == 0) return Collections.emptyList();
        if (n == 1) {
            return List.of(new ClusterNode(0, List.of(requirementIds.get(0)), 0.0));
        }

        // 1. Convert similarity [0..1] to distance [0..1]
        double[][] distMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distMatrix[i][j] = (i == j) ? 0.0 : Math.max(0.0, 1.0 - similarityMatrix[i][j]);
            }
        }

        // 2. Initialize singleton clusters
        List<InternalCluster> activeClusters = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            activeClusters.add(new InternalCluster(i, List.of(i), 0.0));
        }

        List<PartitionSnapshot> dendrogramHistory = new ArrayList<>();
        int nextClusterId = n;

        // 3. Agglomerative merging (Average Linkage / UPGMA)
        while (activeClusters.size() > 1) {
            int bestI = 0;
            int bestJ = 1;
            double minDistance = Double.MAX_VALUE;

            for (int i = 0; i < activeClusters.size(); i++) {
                for (int j = i + 1; j < activeClusters.size(); j++) {
                    double dist = computeAverageLinkageDistance(activeClusters.get(i), activeClusters.get(j), distMatrix);
                    if (dist < minDistance) {
                        minDistance = dist;
                        bestI = i;
                        bestJ = j;
                    }
                }
            }

            InternalCluster c1 = activeClusters.get(bestI);
            InternalCluster c2 = activeClusters.get(bestJ);

            List<Integer> mergedMembers = new ArrayList<>(c1.memberIndices);
            mergedMembers.addAll(c2.memberIndices);

            InternalCluster merged = new InternalCluster(nextClusterId++, mergedMembers, minDistance);

            // Remove higher index first to avoid index shift
            activeClusters.remove(bestJ);
            activeClusters.remove(bestI);
            activeClusters.add(merged);

            // Record valid partitions (2 <= k <= n - 1) for index evaluation
            if (activeClusters.size() >= 2 && activeClusters.size() <= n-1) { //maxK
                dendrogramHistory.add(new PartitionSnapshot(new ArrayList<>(activeClusters), minDistance));
            }
        }

        // 4. Select optimal partition via Dunn / Calinski-Harabaz index
        PartitionSnapshot bestPartition = selectOptimalPartition(dendrogramHistory, distMatrix, criterion);

        // 5. Map internal clusters to ClusterNode objects
        List<ClusterNode> resultNodes = new ArrayList<>();
        int nodeIdx = 1;
        for (InternalCluster ic : bestPartition.clusters) {
            List<String> reqIds = new ArrayList<>();
            for (int memberIdx : ic.memberIndices) {
                reqIds.add(requirementIds.get(memberIdx));
            }
            Collections.sort(reqIds);
            resultNodes.add(new ClusterNode(nodeIdx++, reqIds, ic.mergeDistance));
        }

        return resultNodes;
    }

    private double computeAverageLinkageDistance(InternalCluster a, InternalCluster b, double[][] distMatrix) {
        double sum = 0.0;
        int count = 0;
        for (int i : a.memberIndices) {
            for (int j : b.memberIndices) {
                sum += distMatrix[i][j];
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    /**
     * Selects the optimal dendrogram partition by scoring candidate cuts with the specified index criterion.
     * SILHOUETTE evaluates the full range (k <= n - 1), whereas DUNN, CALINSKI_HARABAZ, and COMBINED are bounded
     * to k <= n / 2 to prevent near-duplicate outliers from skewing [0, 1] normalization.
     * SILHOUETTE and COMBINED generally yield the best trade-off between cluster cohesion and feature granularity.
     */
    private PartitionSnapshot selectOptimalPartition(List<PartitionSnapshot> history, double[][] distMatrix, ThresholdCriterion criterion) {
        if (history.isEmpty()) {
            // Fallback for n == 2: keep two separate clusters
            List<InternalCluster> singletons = List.of(
                    new InternalCluster(0, List.of(0), 0.0),
                    new InternalCluster(1, List.of(1), 0.0)
            );
            return new PartitionSnapshot(singletons, 0.5);
        }

        int n = distMatrix.length;
        // SILHOUETTE without boundary Grenze (n - 1), all others with boundary (n / 2)
        int maxK = (criterion == ThresholdCriterion.SILHOUETTE)
                ? (n - 1)
                : Math.max(2, n / 2);

        List<PartitionSnapshot> candidates = history.stream()
                .filter(snap -> snap.clusters.size() <= maxK)
                .toList();

        if (candidates.isEmpty()) {
            candidates = history;
        }

        int m = candidates.size();
        double[] dunnScores = new double[m];
        double[] chScores = new double[m];
        double[] silScores = new double[m];

        for (int idx = 0; idx < m; idx++) {
            List<InternalCluster> partition = candidates.get(idx).clusters;
            dunnScores[idx] = computeDunnIndex(partition, distMatrix);
            chScores[idx] = computeCalinskiHarabazIndex(partition, distMatrix);
            silScores[idx] = computeSilhouetteScore(partition, distMatrix);
        }

        normalizeArray(dunnScores);
        normalizeArray(chScores);
        normalizeArray(silScores);

        int bestIdx = 0;
        double bestScore = -1.0;

        for (int idx = 0; idx < m; idx++) {
            double score = switch (criterion) {
                case DUNN -> dunnScores[idx];
                case CALINSKI_HARABAZ -> chScores[idx];
                case SILHOUETTE -> silScores[idx];
                case COMBINED -> (dunnScores[idx] + chScores[idx] + silScores[idx]) / 3.0 ;
            };
            if (score > bestScore) {
                bestScore = score;
                bestIdx = idx;
            }
        }

        return candidates.get(bestIdx);
    }

    /**
     * Dunn Index: min inter-cluster distance / max intra-cluster diameter. Higher is better.
     */
    private double computeDunnIndex(List<InternalCluster> clusters, double[][] distMatrix) {
        double maxIntraDiameter = 0.0;
        for (InternalCluster c : clusters) {
            for (int i = 0; i < c.memberIndices.size(); i++) {
                for (int j = i + 1; j < c.memberIndices.size(); j++) {
                    double d = distMatrix[c.memberIndices.get(i)][c.memberIndices.get(j)];
                    if (d > maxIntraDiameter) maxIntraDiameter = d;
                }
            }
        }

        if (maxIntraDiameter == 0.0) return 0.0;

        double minInterDistance = Double.MAX_VALUE;
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                double interDist = computeAverageLinkageDistance(clusters.get(i), clusters.get(j), distMatrix);
                if (interDist < minInterDistance) minInterDistance = interDist;
            }
        }

        return minInterDistance / maxIntraDiameter;
    }

    /**
     * Distance-based Calinski-Harabaz Index using Gower's sum-of-squares decomposition. Higher is better.
     */
    private double computeCalinskiHarabazIndex(List<InternalCluster> clusters, double[][] distMatrix) {
        int n = distMatrix.length;
        int k = clusters.size();
        if (k <= 1 || k >= n) return 0.0;

        double totalSumSq = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                totalSumSq += distMatrix[i][j] * distMatrix[i][j];
            }
        }
        double tss = totalSumSq / n;

        double wgss = 0.0;
        for (InternalCluster c : clusters) {
            int size = c.memberIndices.size();
            if (size <= 1) continue;
            double clusterSumSq = 0.0;
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    double d = distMatrix[c.memberIndices.get(i)][c.memberIndices.get(j)];
                    clusterSumSq += d * d;
                }
            }
            wgss += clusterSumSq / size;
        }

        double bgss = Math.max(0.0, tss - wgss);
        if (wgss == 0.0) return 0.0;

        return (bgss / (k - 1.0)) / (wgss / (n - k));
    }

    /**
     * Silhouette Score: compares mean intra-cluster distance a(i) with nearest-cluster distance b(i);
     * singletons receive 0.0 to prevent over-fragmentation. Higher is better.
     */
    private double computeSilhouetteScore(List<InternalCluster> clusters, double[][] distMatrix) {
        int n = distMatrix.length;
        if (clusters.size() <= 1 || clusters.size() >= n) return 0.0;

        double totalSilhouette = 0.0;

        for (InternalCluster ownCluster : clusters) {
            if (ownCluster.memberIndices.size() <= 1) {
                // Singletons bekommen 0.0 -> verhindert Zersplitterung in Einzelcluster
                continue;
            }
            for (int i : ownCluster.memberIndices) {
                // a(i): durchschnittliche Distanz innerhalb des eigenen Clusters
                double a = 0.0;
                for (int other : ownCluster.memberIndices) {
                    if (i != other) a += distMatrix[i][other];
                }
                a /= (ownCluster.memberIndices.size() - 1);

                // b(i): kleinste durchschnittliche Distanz zu einem fremden Cluster
                double b = Double.MAX_VALUE;
                for (InternalCluster otherCluster : clusters) {
                    if (otherCluster == ownCluster) continue;
                    double distSum = 0.0;
                    for (int j : otherCluster.memberIndices) {
                        distSum += distMatrix[i][j];
                    }
                    double avgDist = distSum / otherCluster.memberIndices.size();
                    if (avgDist < b) b = avgDist;
                }

                double denom = Math.max(a, b);
                if (denom > 0.0) {
                    totalSilhouette += (b - a) / denom;
                }
            }
        }
        // Auf [0, 1] verschieben, damit normalizeArray sauber funktioniert
        return Math.max(0.0, totalSilhouette / n);
    }

    /**
     * Normalizes array values into [0, 1] by dividing by the maximum entry so different index scales can be combined equally.
     */
    private void normalizeArray(double[] values) {
        double max = Arrays.stream(values).max().orElse(0.0);
        if (max > 0.0) {
            for (int i = 0; i < values.length; i++) {
                values[i] /= max;
            }
        }
    }
}