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

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.Matrix;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

/**
 * Implements the continuous BIC score for FGES.
 *
 * @author Joseph Ramsey
 */
public class ZhangShenBoundScore implements Score {

    // The covariance matrix.
    private ICovarianceMatrix covariances;

    // The variables of the covariance matrix.
    private final List<Node> variables;

    // The sample size of the covariance matrix.
    private final int sampleSize;

    // True if verbose output should be sent to out.
    private boolean verbose = false;

    // Sample size or equivalent sample size.
    private double N;

    // A recpord of lambdas for each m0.
    private List<Double> lambdas;

    // The minimim probability lower bound for risk.
    private double riskBound = 0.05;

    // The data, if it is set.
    private Matrix data;

    // True if sume of squares should be calculated, false if estimated.
    private boolean calculateSquaredEuclideanNorms = false;

    // The variance of the error term in the true model; assumed to be constant.
    private double trueErrorVariance = 1;


    // True if row subsets should be calculated.
    private boolean calculateRowSubsets = false;

    /**
     * Constructs the score using a covariance matrix.
     */
    public ZhangShenBoundScore(ICovarianceMatrix covariances) {
        if (covariances == null) {
            throw new NullPointerException();
        }

        setCovariances(covariances);
        this.variables = covariances.getVariables();
        this.sampleSize = covariances.getSampleSize();
    }

    /**
     * Constructs the score using a covariance matrix.
     */
    public ZhangShenBoundScore(DataSet dataSet, boolean calculateSquaredEuclideanNorms) {
        if (dataSet == null) {
            throw new NullPointerException();
        }

        this.calculateSquaredEuclideanNorms = calculateSquaredEuclideanNorms;
        this.variables = dataSet.getVariables();
        this.sampleSize = dataSet.getNumRows();

        if (!dataSet.existsMissingValue()) {
            DataSet _dataSet = DataUtils.center(dataSet);
            this.data = _dataSet.getDoubleData();

            setCovariances(new CovarianceMatrix(dataSet));
            calculateRowSubsets = false;

            return;
        }

        calculateRowSubsets = true;
        DataSet _dataSet = DataUtils.center(dataSet);
        this.data = _dataSet.getDoubleData();
    }

    private int[] indices(List<Node> __adj) {
        int[] indices = new int[__adj.size()];
        for (int t = 0; t < __adj.size(); t++) indices[t] = variables.indexOf(__adj.get(t));
        return indices;
    }

    @Override
    public double localScoreDiff(int x, int y, int[] z) {
        return localScore(y, append(z, x)) - localScore(y, z);
    }

    @Override
    public double localScoreDiff(int x, int y) {
        return localScoreDiff(x, y, new int[0]);
    }

    public double localScore(int i, int... parents) {
        final int p = parents.length;
        double sum;

        double varey = getVarey(i, parents);

        if (calculateSquaredEuclideanNorms) {
            sum = getSquaredEucleanNorm(i, parents);
        } else {
            sum = N * varey;
        }

        double varey2 = trueErrorVariance;
        double lambda = getLambda(parents.length);

        return -sum - lambda * p * varey2;
    }

    private double getVarey(int i, int[] parents) {
        int[] all = concat(i, parents);
        Matrix cov = getCov(getRows(i, parents), all, all);
        return getVarey(cov, parents);
    }

    private double getVarey(Matrix cov, int[] parents) {
        int[] pp = indexedParents(parents);

        Matrix covxx = cov.getSelection(pp, pp);
        Matrix covxy = cov.getSelection(pp, new int[]{0});

        Matrix b = adjustedCoefs(covxx.inverse().times(covxy));
        Matrix times = b.transpose().times(cov).times(b);
        return sqrt(times.get(0, 0));
    }

    private double getLambda(int m) {
        if (lambdas == null) {
            lambdas = new ArrayList<>();
        }

        if (lambdas.size() - 1 < m) {
            for (int t = lambdas.size(); t <= m; t++) {
                double lambda = zhangShenLambda(variables.size() - 1, t, riskBound);
                lambdas.add(lambda);
            }
        }

        return lambdas.get(m);
    }

    private double getSquaredEucleanNorm(int i, int[] parents) {
        int[] rows = new int[data.rows()];
        for (int t = 0; t < rows.length; t++) rows[t] = t;

        Matrix y = data.getSelection(rows, new int[]{i});
        Matrix x = data.getSelection(rows, parents);

        Matrix x2 = new Matrix(x.rows(), x.columns() + 1);
        for (int q = 0; q < x.rows(); q++) {
            for (int r = 0; r < x.columns(); r++) {
                x2.set(q, r, x.get(q, r));
            }
        }

        for (int q = 0; q < x.rows(); q++) {
            x2.set(q, x.columns(), 1);
        }

        x = x2;

        Matrix xT = x.transpose();
        Matrix xTx = xT.times(x);
        Matrix xTxInv = xTx.inverse();
        Matrix xTy = xT.times(y);
        Matrix b = xTxInv.times(xTy);

        Matrix yhat = x.times(b);

        double sum = 0.0;

        for (int q = 0; q < data.rows(); q++) {
            double diff = data.get(q, i) - yhat.get(q, 0);
            sum += diff * diff;
        }

        return sum;
    }


    @NotNull
    public Matrix adjustedCoefs(Matrix b) {
        Matrix byx = new Matrix(b.rows() + 1, 1);
        byx.set(0, 0, 1);
        for (int j = 0; j < b.rows(); j++) byx.set(j + 1, 0, -b.get(j, 0));
        return byx;
    }

    /**
     * Specialized scoring method for a single parent. Used to speed up the effect edges search.
     */
    public double localScore(int i, int parent) {
        return localScore(i, new int[]{parent});
    }

    /**
     * Specialized scoring method for no parents. Used to speed up the effect edges search.
     */
    public double localScore(int i) {
        return localScore(i, new int[0]);
    }

    public ICovarianceMatrix getCovariances() {
        return covariances;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    @Override
    public boolean isEffectEdge(double bump) {
        return bump > 0;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public List<Node> getVariables() {
        return variables;
    }

    @Override
    public Node getVariable(String targetName) {
        for (Node node : variables) {
            if (node.getName().equals(targetName)) {
                return node;
            }
        }

        return null;
    }

    @Override
    public int getMaxDegree() {
        return (int) ceil(log(sampleSize));
    }

    @Override
    public boolean determines(List<Node> z, Node y) {
        int i = variables.indexOf(y);

        int[] k = indices(z);

        double v = localScore(i, k);

        return Double.isNaN(v);
    }

    @Override
    public Score defaultScore() {
        return new ZhangShenBoundScore(covariances);
    }

    private void setCovariances(ICovarianceMatrix covariances) {
//        this.covariances = new CorrelationMatrix(covariances);
        this.covariances = covariances;
        this.N = covariances.getSampleSize();
    }

    private static int[] append(int[] z, int x) {
        int[] _z = Arrays.copyOf(z, z.length + 1);
        _z[z.length] = x;
        return _z;
    }

    private static int[] indexedParents(int[] parents) {
        int[] pp = new int[parents.length];
        for (int j = 0; j < pp.length; j++) pp[j] = j + 1;
        return pp;
    }

    private static int[] concat(int i, int[] parents) {
        int[] all = new int[parents.length + 1];
        all[0] = i;
        System.arraycopy(parents, 0, all, 1, parents.length);
        return all;
    }

    private Matrix getCov(List<Integer> rows, int[] _rows, int[] cols) {
        if (rows == null) {
            return getCovariances().getSelection(_rows, cols);
        }

        Matrix cov = new Matrix(_rows.length, cols.length);

        for (int i = 0; i < _rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                double mui = 0.0;
                double muj = 0.0;

                for (int k : rows) {
                    mui += data.get(k, _rows[i]);
                    muj += data.get(k, cols[j]);
                }

                mui /= rows.size() - 1;
                muj /= rows.size() - 1;

                double _cov = 0.0;

                for (int k : rows) {
                    _cov += (data.get(k, _rows[i]) - mui) * (data.get(k, cols[j]) - muj);
                }

                double mean = _cov / (rows.size());
                cov.set(i, j, mean);
            }
        }

        return cov;
    }

    private List<Integer> getRows(int i, int[] parents) {
        if (!calculateRowSubsets) {
            return null;
        }

        List<Integer> rows = new ArrayList<>();

        K:
        for (int k = 0; k < data.rows(); k++) {
            if (Double.isNaN(data.get(k, i))) continue;

            for (int p : parents) {
                if (Double.isNaN(data.get(k, p))) continue K;
            }

            rows.add(k);
        }

        return rows;
    }

    public void setRiskBound(double riskBound) {
        if (riskBound < 0 || riskBound > 1) throw new IllegalStateException(
                "Risk probability should be in [0, 1]: " + this.riskBound);
        this.riskBound = riskBound;
    }

    public double zhangShenLambda(int pn, int m0, double riskBound) {
        double high = 100000.0;
        double low = 0.0;

        while (high - low > 1e-10) {
            double lambda = (high + low) / 2.0;

            double p = getP(pn, m0, lambda);

            if (p < 1.0 - riskBound) {
                low = lambda;
            } else {
                high = lambda;
            }
        }

        return (high + low) / 2.0;
    }

    private double getP(int pn, int m0, double lambda) {
        return 2 - pow(1 + exp(-(lambda - 1) / 2.) * sqrt(lambda), pn - m0);
    }

    public void setTrueErrorVariance(double trueErrorVariance) {
        this.trueErrorVariance = trueErrorVariance;
    }
}


