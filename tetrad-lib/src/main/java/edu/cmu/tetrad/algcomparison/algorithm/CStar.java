package edu.cmu.tetrad.algcomparison.algorithm;

import edu.cmu.tetrad.algcomparison.algorithm.oracle.pattern.Fges;
import edu.cmu.tetrad.annotation.AlgType;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.FgesMb;
import edu.cmu.tetrad.search.Ida;
import edu.cmu.tetrad.util.ForkJoinPoolInstance;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.util.StatUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static java.lang.Math.abs;

@edu.cmu.tetrad.annotation.Algorithm(
        name = "CStar",
        command = "cstar",
        algoType = AlgType.forbid_latent_common_causes,
        description = "Performs a CStar analysis of the given dataset (Stekhoven, Daniel J., et al. " +
                "Causal stability ranking.\" Bioinformatics 28.21 (2012): 2819-2823) and returns a graph " +
                "in which all selected variables are shown as into the target. The target is the first variables."
)
public class CStar implements Algorithm {

    static final long serialVersionUID = 23L;
    private Algorithm algorithm;
    private Graph initialGraph = null;

    public CStar() {
        this.algorithm = new Fges();
    }

    @Override
    public Graph search(DataModel dataSet, Parameters parameters) {
        DataSet _dataSet = (DataSet) dataSet;

        double percentSubsampleSize = parameters.getDouble("percentSubsampleSize");
        int numSubsamples = parameters.getInt("numSubsamples");
        int q = parameters.getInt("topQ");
        double pithreshold = parameters.getDouble("piThreshold");
        Node y = dataSet.getVariable(parameters.getString("targetName"));

        final List<Node> variables = dataSet.getVariables();
        variables.remove(y);

        Map<Node, Integer> counts = new ConcurrentHashMap<>();
        for (Node node : variables) counts.put(node, 0);

        class Task implements Callable<Boolean> {
            private int i;
            private Map<Node, Integer> counts;

            public Task(int i, Map<Node, Integer> counts) {
                this.i = i;
                this.counts = counts;
            }

            @Override
            public Boolean call() {
                System.out.println("\nBootstrap #" + (i + 1) + " of " + numSubsamples);

                BootstrapSampler sampler = new BootstrapSampler();
                sampler.setWithoutReplacements(true);
                DataSet sample = sampler.sample(_dataSet, (int) (percentSubsampleSize * _dataSet.getNumRows()));

                Ida ida = new Ida(new CovarianceMatrixOnTheFly(sample));

                Ida.NodeEffects effects = ida.getSortedMinEffects(y);

                for (int j = 0; j < variables.size(); j++) {
                    int f = 0;

                    final Node key = variables.get(j);

                    if (effects.getNodes().indexOf(key) < q) {
                        counts.put(key, counts.get(key) + 1);
                    }
                }

                return true;
            }
        }

        for (int i = 0; i < numSubsamples; i++) {
            List<Task> tasks = new ArrayList<>();
            tasks.add(new Task(i, counts));
            ForkJoinPoolInstance.getInstance().getPool().invokeAll(tasks);
        }

//        for (int i = 0; i < numSubsamples; i++) {
//            System.out.println("\nBootstrap #" + (i + 1) + " of " + numSubsamples);
//
//            BootstrapSampler sampler = new BootstrapSampler();
//            sampler.setWithoutReplacements(true);
//            DataSet sample = sampler.sample(_dataSet, (int) (percentSubsampleSize * _dataSet.getNumRows()));
//
//            Ida ida = new Ida(new CovarianceMatrixOnTheFly(sample));
//
//            Ida.NodeEffects effects = ida.getSortedMinEffects(y);
//
//            for (int j = 0; j < variables.size(); j++) {
//                int f = 0;
//
//                final Node key = variables.get(j);
//
//                if (effects.getNodes().indexOf(key) < q) {
//                    counts.put(key, counts.get(key) + 1);
//                }
//            }
//        }

        variables.sort((o1, o2) -> {
            final int d1 = counts.get(o1);
            final int d2 = counts.get(o2);
            return -Integer.compare(d1, d2);
        });

        double[] sortedFreqencies = new double[variables.size()];

        for (int i = 0; i < variables.size(); i++) {
            sortedFreqencies[i] = counts.get(variables.get(i));
        }

        for (int i = 0; i < sortedFreqencies.length; i++) {
            sortedFreqencies[i] /= (int) numSubsamples;
        }

        System.out.println(variables);

        System.out.println(Arrays.toString(sortedFreqencies));

        Graph graph = new EdgeListGraph(dataSet.getVariables());

        for (int i = 0; i < variables.size(); i++) {
            if (sortedFreqencies[i] > pithreshold) {
                graph.addUndirectedEdge(variables.get(i), y);
            }
        }

        return graph;
    }

    private void increment(Edge edge, Map<Edge, Integer> counts) {
        counts.putIfAbsent(edge, 0);
        counts.put(edge, counts.get(edge) + 1);
    }

    @Override
    public Graph getComparisonGraph(Graph graph) {
        return algorithm.getComparisonGraph(graph);
    }

    @Override
    public String getDescription() {
        return "CStar";
    }

    @Override
    public DataType getDataType() {
        return DataType.Continuous;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("numSubsamples");
        parameters.add("percentSubsampleSize");
        parameters.add("topQ");
        parameters.add("piThreshold");
        parameters.add("targetName");
        return parameters;
    }
}
