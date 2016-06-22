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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.CombinationIterator;
import edu.cmu.tetrad.util.TetradMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implements a conditional Gaussian BIC score for FGS.
 *
 * @author Joseph Ramsey
 */
public class Test {

    // Calculates the log of a list of terms, where the argument consists of the logs of the terms.
    private double logOfSum(List<Double> logs) {

        Collections.sort(logs, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return -Double.compare(o1, o2);
            }
        });

        double sum = 0.0;
        int N = logs.size() - 1;
        double loga0 = logs.get(0);

        for (int i = 1; i <= N; i++) {
            sum += Math.exp(logs.get(i) - loga0);
        }

        sum += 1;

        return loga0 + Math.log(sum);
    }

    @org.junit.Test
    public void test() {
        double[] a = {.3, .03, .01};

        List<Double> logs = new ArrayList<>();

        for (double _a : a) {
            logs.add(Math.log(_a));
        }

        double sum = 0.0;

        for (double _a : a) {
            sum += _a;
        }

        double logsum = logOfSum(logs);

        System.out.println(Math.exp(logsum) + " " + sum);
    }

}



