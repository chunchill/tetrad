package edu.cmu.tetrad.algcomparison.statistic;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * The adjacency precision. The true positives are the number of adjacencies in both
 * the true and estimated graphs.
 *
 *
 *
 * @author jdramsey
 */
public class TrueClusterCounter implements Statistic {
    static final long serialVersionUID = 23L;

    @Override
    public String getAbbreviation() {
        return "tCC";
    }

    @Override
    public String getDescription() {
        return "True Counter Clusters";
    }

    @Override
    public double getValue(Graph trueGraph, Graph estGraph, DataModel dataModel) {

        double trueClusters = GrabCluster(trueGraph);

        return trueClusters;

    }

    private double GrabCluster(Graph graph){

        double Clusters = 0;

        for (Node d : graph.getNodes()){

            if (d.getName().contains("L")){

                Clusters++;

            }

        }

        return Clusters;


    }

    @Override
    public double getNormValue(double value) {
        return value;
    }
}
