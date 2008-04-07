/**
 * Copyright (c) 2005-2007, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.tuckey.web.testhelper;


/**
 * Used to extablish a system benchmark that other performance things can be compared to.
 * <p/>
 * Adapted from...
 * http://mathsrv.ku-eichstaett.de/MGF/homes/grothmann/java/bench/BTest.java
 * http://mathsrv.ku-eichstaett.de/MGF/homes/grothmann/java/bench/Bench.html
 */
public final class BenchmarkRunner {
    private static final int ITER = 1000;
    private final int n = 60;
    private final int N = 1125;
    private final double XTotal = 395113;
    private final double[][] Data = {
            {158.40, 0},
            {33.99, 0},
            {78.40, 0},
            {103.52, 0},
            {145.60, 56},
            {313.60, 78.40},
            {74.25, 0},
            {368.89, 0},
            {65.78, 0},
            {109.78, 0},
            {428.40, 59.12},
            {345.98, 0},
            {162.20, 0},
            {489.50, 0},
            {181.49, 0},
            {27.94, 0},
            {292.69, 0},
            {305.40, 0},
            {579.55, 0},
            {101.91, 0},
            {796.87, 78.40},
            {545.36, 0},
            {994.50, 0},
            {45.55, 0},
            {448, 0},
            {243.06, 0},
            {137.49, 0},
            {210.80, 65.90},
            {91.10, 0},
            {545.62, 56},
            {1785.90, 28},
            {90.20, 0},
            {265.36, 0},
            {215.53, 0},
            {181.45, 0},
            {67, 0},
            {123.20, 0},
            {78.40, 0},
            {51.52, 0},
            {203.16, 0},
            {133.33, 0},
            {22.40, 0},
            {579.08, 0},
            {150.58, 0},
            {89.60, 89.60},
            {805, 0},
            {826.69, 0},
            {1429.77, 0},
            {181.99, 44.80},
            {259.09, 0},
            {266.40, 0},
            {298.68, 0},
            {564.36, 0},
            {33.93, 0},
            {56, 0},
            {90.38, 0},
            {35, 0},
            {965.66, 313.60},
            {142.76, 0},
            {268.85, 0}
    };

    private long time;

    private int count = 0;

    /**
     * Will give an amount of millisecs that is taken to run a specific complex chunk of code.
     */
    public float establishBenchmark() {
        return establishBenchmark(2);
    }

    private float establishBenchmark(int numIters) {
        // warm up
        System.out.println("Establishing benchmark");
        BenchmarkRunner br = new BenchmarkRunner();
        br.compute();
        long benchStarted = System.currentTimeMillis();
        for (float i = 0; i < numIters; i++) {
            BenchmarkRunner brIter = new BenchmarkRunner();
            brIter.compute();
        }
        long totalBenchTook = System.currentTimeMillis() - benchStarted;
        int benchAvg = Math.round((float) totalBenchTook / (float) numIters);
        System.out.println("average of " + benchAvg + "ms after " + numIters + " iterations");
        return benchAvg;
    }

    private void compute() {
        int i;
        double x[] = new double[n];
        double y[] = new double[n];
        for (i = 0; i < n; i++) {
            x[i] = Data[i][0];
            y[i] = Data[i][1];
        }
        time = System.currentTimeMillis();
        count = 1;
        Benchmarker T = new Benchmarker(this, x, y, n, N, XTotal);
        T.run();
    }

    private double Res = 0;

    public final synchronized void note(double res) {
        count--;
        Res += res;
        if (count <= 0) {
            int ms = (int) (System.currentTimeMillis() - time);
            System.out.println("iteration took: " + ms + "ms (result: " + Res / 1 * 100 + ")");
        }
    }


    final class Benchmarker implements Runnable {
        private final double[] x;
        private final double[] y;
        private final double xtotal;
        private int Nboot;
        private final int n;
        private final int N;
        private int[] I;
        private int imin;
        private double[] results;
        private double resmin;
        private int nresults;
        private int ntop;

        private final BenchmarkRunner R;

        private final double alpha = 0.05;

        public Benchmarker(BenchmarkRunner r, double X[], double Y[], int nn, int NN, double XTotal) {
            x = new double[X.length];
            System.arraycopy(X, 0, x, 0, X.length);
            y = new double[Y.length];
            System.arraycopy(Y, 0, y, 0, Y.length);
            N = NN;
            n = nn;
            xtotal = XTotal;
            R = r;
        }

        private void shuffle(int n, int I[], int N) {
            int i, j, h;
            for (i = 0; i < n; i++) {
                j = i + (int) (Math.random() * (N - i));
                if (i != j) {
                    h = I[j];
                    I[j] = I[i];
                    I[i] = h;
                }
            }
        }

        private void shuffle(int n, int I[], int start, int N) {
            int i, j, h;
            for (i = 0; i < n; i++) {
                j = i + (int) (Math.random() * (N - i));
                if (i != j) {
                    h = I[start + j];
                    I[start + j] = I[start + i];
                    I[start + i] = h;
                }
            }
        }

        private double sqr(double x) {
            return x * x;
        }

        private void bootstr100() {
            int i, j, k;
            double xsum, ysum, mux, h, p, vp, est, ptrue;
            for (k = 0; k < 100; k++) {
                for (i = 0; i < Nboot; i++) I[i] = i % n;
                shuffle(N - (Nboot - n), I, Nboot - n, n);
                shuffle(n, I, N);

                xsum = 0;
                ysum = 0;
                for (j = 0; j < n; j++) {
                    xsum += x[I[j]];
                    ysum += y[I[j]];
                }
                if (xsum == 0) est = -10;
                else {
                    p = ysum / xsum;
                    xsum = 0;
                    ysum = 0;
                    for (int l = 0; l < N; l++) {
                        xsum += x[I[l]];
                        ysum += y[I[l]];
                    }
                    ptrue = ysum / xsum;
                    mux = xsum / N;
                    h = 0;
                    for (j = 0; j < n; j++) {
                        h += sqr(y[I[j]] - p * x[I[j]]);
                    }
                    vp = (1 - (double) (n - 1) / (N - 1)) / (n * mux * mux) * h / (n - 1);
                    if (vp == 0) est = -10;
                    else est = (p - ptrue) / Math.sqrt(vp);
                }

                if (nresults == 0) {
                    results[0] = est;
                    imin = 0;
                    resmin = est;
                    nresults = 1;
                } else if (nresults < ntop) {
                    results[nresults] = est;
                    if (est < resmin) {
                        resmin = est;
                        imin = nresults;
                    }
                    nresults++;
                } else if (est > resmin) {
                    results[imin] = est;
                    resmin = results[0];
                    imin = 0;
                    for (j = 0; j < ntop; j++) {
                        if (results[j] < resmin) {
                            resmin = results[j];
                            imin = j;
                        }
                    }
                }
            }
        }


        public void run() {
            int f, k, j;
            double xsum, ysum, h, vp, ptrue, mux;

            f = N / n + 1;
            Nboot = n * f;
            I = new int[Nboot];

            ntop = (int) (ITER * 100 * alpha);
            results = new double[ntop];
            nresults = 0;

            for (k = 0; k < ITER; k++) bootstr100();

            xsum = 0;
            ysum = 0;
            for (j = 0; j < n; j++) {
                xsum += x[j];
                ysum += y[j];
            }
            mux = xtotal / N;
            h = 0;
            ptrue = ysum / xsum;
            for (j = 0; j < n; j++) {
                h += sqr(y[j] - ptrue * x[j]);
            }
            vp = (1 - (double) (n - 1) / (N - 1)) / (n * mux * mux) * h / (n - 1);

            R.note(ptrue - Math.sqrt(vp) * resmin);
        }
    }


}


