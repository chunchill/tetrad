///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010, 2014, 2015 by Peter Spirtes, Richard Scheines, Joseph   //
// Ramsey, and Clark Glymour.                                                //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.algcomparison.examples;

import edu.cmu.tetrad.algcomparison.Comparison;
import edu.cmu.tetrad.algcomparison.algorithm.Algorithms;
import edu.cmu.tetrad.algcomparison.algorithm.cluster.Fofc;
import edu.cmu.tetrad.algcomparison.algorithm.mixed.Mgm;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pattern.*;
import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.graph.RandomSingleFactorMim;
import edu.cmu.tetrad.algcomparison.graph.SingleGraph;
import edu.cmu.tetrad.algcomparison.independence.FisherZ;
import edu.cmu.tetrad.algcomparison.score.MVPBicScore;
import edu.cmu.tetrad.algcomparison.score.SemBicScore;
import edu.cmu.tetrad.algcomparison.simulation.SemSimulation;
import edu.cmu.tetrad.algcomparison.simulation.Simulations;
import edu.cmu.tetrad.algcomparison.simulation.StandardizedSemSimulation;
import edu.cmu.tetrad.algcomparison.statistic.*;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.Dataset;

/**
 * An example script to simulate data and run a comparison analysis on it.
 *
 * @author jdramsey
 */
public class ExampleCompareSimulation {
    public static void main(String... args) {
        Parameters parameters = new Parameters();
        //https://arxiv.org/abs/1607.08110
        parameters.set("numRuns", 4);
        parameters.set("standardize", true);
//        parameters.set("coefLow",0.2);
//        parameters.set("coefHigh",1.8);
//        parameters.set("numMeasures", 100);
//        parameters.set("avgDegree", 4, 6);
        parameters.set("sampleSize", 2000);
//        parameters.set("alpha", 1e-4, 1e-3, 1e-2);

        parameters.set("alpha", 0.001);
        parameters.set("useWishart", false);
        parameters.set("useGap", true);
        parameters.set("verbose", true);

        parameters.set("numStructuralNodes", 3);
        parameters.set("numStructuralEdges", 1);
        parameters.set("measurementModelDegree", 4);
        parameters.set("latentMeasuredImpureParents", 0);
        parameters.set("measuredMeasuredImpureParents", 1);
        parameters.set("measuredMeasuredImpureAssociations", 0);

        Statistics statistics = new Statistics();

 //       statistics.add(new AdjacencyPrecision());
 //       statistics.add(new AdjacencyRecall());
//        statistics.add(new ArrowheadPrecision());
//        statistics.add(new ArrowheadRecall());
//        statistics.add(new MathewsCorrAdj());
//        statistics.add(new MathewsCorrArrow());
//        statistics.add(new F1Adj());
//        statistics.add(new F1Arrow());
//        statistics.add(new SHD());
//        statistics.add(new ElapsedTime());
        statistics.add(new MySuperDooperStatistic());
        statistics.add(new estClusterCounter());
        statistics.add(new TrueClusterCounter());
        statistics.add(new ClusterPrecision());
        statistics.add(new ClusterRecall());


//        statistics.setWeight("AP", 1.0);
//        statistics.setWeight("AR", 0.5);

        Algorithms algorithms = new Algorithms();

        //double i=1.0;

//        algorithms.add(new Fofc(false,true,2,true,true));
//        algorithms.add(new Fofc(true,false,1,false,false));
//        algorithms.add(new Fofc(true,true,1,false,false));
//        algorithms.add(new Fofc(true,false,1,false,true));
//        algorithms.add(new Fofc(true,false,2,false,false));
//        algorithms.add(new Fofc(true,true,3,false,false));
//        algorithms.add(new Fofc(true,true,4,false,true));
//        algorithms.add(new Fofc(true,true,5,false,true));
//        algorithms.add(new Fofc(true,true,6,false,false));
//        algorithms.add(new Fofc(true,true,7,false,false));
//        algorithms.add(new Fofc(true,true,8,false,false));
//        algorithms.add(new Fofc(true,true,9,false,false));
//        algorithms.add(new Fofc(true,true,10,true,false));
//        algorithms.add(new Fofc(true,true,2.2,false));
//        algorithms.add(new Fofc(true,true,2.2,true));
        algorithms.add(new Mgm());
        //algorithms.add(new Fofc(true,false,1));
//        algorithms.add(new Cpc(new FisherZ(), new Fges(new SemBicScore(), false)));
//        algorithms.add(new PcStable(new FisherZ()));
//        algorithms.add(new CpcStable(new FisherZ()));

        Simulations simulations = new Simulations();

//        Graph graph = new EdgeListGraph();
//
//        Node L1 = new GraphNode("L1");
//        L1.setNodeType(NodeType.LATENT);
//
//        Node L2 = new GraphNode("L2");
//        L2.setNodeType(NodeType.LATENT);
//
//        graph.addNode(L1);
//        graph.addNode(L2);
//
//        addMeasure(graph, L1, "X1");
//        addMeasure(graph, L1, "X2");
//        addMeasure(graph, L1, "X3");
//        addMeasure(graph, L1, "X4");
//
//        addMeasure(graph, L2, "X1");
//        addMeasure(graph, L2, "X2");
//        addMeasure(graph, L2, "X3");
//        addMeasure(graph, L2, "X4");



//        simulations.add(new StandardizedSemSimulation(new RandomSingleFactorMim()));
        simulations.add(new SemSimulation(new RandomSingleFactorMim()));
//        simulations.add(new SemSimulation(new SingleGraph(graph)));


        simulations.getSimulations().get(0).createData(parameters);
        DataModel D = simulations.getSimulations().get(0).getDataModel(0);

        CovarianceMatrix C = new CovarianceMatrix((DataSet) D);

        Comparison comparison = new Comparison();

        comparison.setShowAlgorithmIndices(true);
        comparison.setShowSimulationIndices(true);
        comparison.setSortByUtility(true);
        comparison.setShowUtilities(true);
        comparison.setParallelized(true);

        comparison.setComparisonGraph(Comparison.ComparisonGraph.true_DAG);

        comparison.compareFromSimulations("comparison", simulations, algorithms, statistics, parameters);
    }

//    private static void addMeasure(Graph graph, Node l1, String measureName) {
//        Node X1 = new GraphNode(measureName);
//        graph.addNode(X1);
//        graph.addDirectedEdge(l1, X1);
//    }
}




