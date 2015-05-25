package org.mpetnuch.gauss.linearalgebra.blas3;

import org.mpetnuch.gauss.matrix.MatrixSide;
import org.mpetnuch.gauss.matrix.TriangularMatrixType;
import org.mpetnuch.gauss.matrix.dense.DenseMatrix;
import org.mpetnuch.gauss.matrix.dense.DenseMatrixBuilder;
import org.mpetnuch.gauss.matrix.dense.DenseSymmetricMatrix;
import org.mpetnuch.gauss.matrix.dense.DenseTriangularMatrix;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class JBLASLevel3 implements BLASLevel3<DenseMatrix, DenseTriangularMatrix, DenseSymmetricMatrix, DenseMatrixBuilder> {
    private final int matrixMultiplyCrossoverDimension;
    private final ForkJoinPool pool;

    private JBLASLevel3(int matrixMultiplyCrossoverDimension, ForkJoinPool pool) {
        this.matrixMultiplyCrossoverDimension = matrixMultiplyCrossoverDimension;
        this.pool = pool;
    }

    @Override
    public void dgemm(double alpha, DenseMatrix a, DenseMatrix b, double beta, DenseMatrixBuilder c) {
        ForkJoinTask<Void> task = new GeneralMatrixMultiply(matrixMultiplyCrossoverDimension, alpha, a, b, c.scale(beta));
        pool.invoke(task);


    }

    @Override
    public void dsymm(double alpha, MatrixSide matrixSide, DenseSymmetricMatrix a, DenseMatrix b, double beta, DenseMatrixBuilder c) {
        if (MatrixSide.LEFT == matrixSide) {
            dgemm(alpha, a, b, beta, c);
        } else {
            dgemm(alpha, b, a, beta, c);
        }
    }

    @Override
    public void dtrmm(double alpha, MatrixSide matrixSide, DenseTriangularMatrix a, DenseMatrix b, double beta, DenseMatrixBuilder c) {
        if (MatrixSide.LEFT == matrixSide) {
            ForkJoinTask<Void> task = new TriangularMatrixMultiply(matrixMultiplyCrossoverDimension, alpha, a, b, c.scale(beta));
            pool.invoke(task);
        } else {
            dgemm(alpha, b, a, beta, c);
        }
    }

    public static class JBLASLevel3Builder {
        private int matrixMultiplyLeafDimension = 256;
        private ForkJoinPool pool = ForkJoinPool.commonPool();

        public JBLASLevel3Builder setPool(ForkJoinPool pool) {
            this.pool = pool;
            return this;
        }

        public JBLASLevel3Builder setMatrixMultiplyLeafDimension(int matrixMultiplyLeafDimension) {
            this.matrixMultiplyLeafDimension = matrixMultiplyLeafDimension;
            return this;
        }

        public JBLASLevel3 createJBLASLevel3() {
            return new JBLASLevel3(matrixMultiplyLeafDimension, pool);
        }
    }

    private static final class GeneralMatrixMultiply extends RecursiveAction {
        private static final long serialVersionUID = -4266937266787772842L;

        private final DenseMatrix a, b;
        private final DenseMatrixBuilder c;
        private final double alpha;
        private final int M, N, P;
        private final int largestDimension, leafDimension;

        private GeneralMatrixMultiply(int crossoverDimension, double alpha, DenseMatrix a, DenseMatrix b, DenseMatrixBuilder c) {
            this.alpha = alpha;

            this.a = a;
            this.M = a.getNumberOfRows();

            this.b = b;
            this.P = b.getNumberOfRows();
            this.N = b.getNumberOfColumns();

            this.c = c;
            this.largestDimension = Math.max(M, Math.max(P, N));
            this.leafDimension = crossoverDimension;
        }

        private static void computeDirectly(double alpha,
                                            DenseMatrix a11, DenseMatrix a12, DenseMatrix a21, DenseMatrix a22,
                                            DenseMatrix b11, DenseMatrix b12, DenseMatrix b21, DenseMatrix b22,
                                            DenseMatrixBuilder c11, DenseMatrixBuilder c12,
                                            DenseMatrixBuilder c21, DenseMatrixBuilder c22) {
            final int m = a11.getNumberOfRows(), mm = a21.getNumberOfRows();
            final int p = b11.getNumberOfRows(), pp = b21.getNumberOfRows();
            final int n = b11.getNumberOfColumns(), nn = b12.getNumberOfColumns();

            final double[] b11_j = new double[p], b12_j = new double[p], b21_j = new double[pp], b22_j = new double[pp];

            for (int j = 0; j < n; j++) {
                b11.getColumn(j).copyInto(b11_j);
                b12.getColumn(j).copyInto(b12_j);
                b21.getColumn(j).copyInto(b21_j);
                b22.getColumn(j).copyInto(b22_j);

                for (int i = 0; i < m; i++) {
                    double s11 = 0.0, s12 = 0.0, s21 = 0.0, s22 = 0.0;
                    for (int k = 0; k < p; k++) {
                        s11 += a11.get(i, k) * b11_j[k] + a12.get(i, k) * b21_j[k];
                        s12 += a11.get(i, k) * b12_j[k] + a12.get(i, k) * b22_j[k];
                        s21 += a21.get(i, k) * b11_j[k] + a22.get(i, k) * b21_j[k];
                        s22 += a21.get(i, k) * b12_j[k] + a22.get(i, k) * b22_j[k];
                    }

                    if (p != pp) { // Only need to consider A12 and A22 as A11 and A21 are m x p and mm x p respectively
                        s11 += a12.get(i, p) * b21_j[p];
                        s12 += a12.get(i, p) * b22_j[p];
                        s21 += a22.get(i, p) * b21_j[p];
                        s22 += a22.get(i, p) * b22_j[p];
                    }

                    c11.add(i, j, alpha * s11);
                    c12.add(i, j, alpha * s12);
                    c21.add(i, j, alpha * s21);
                    c22.add(i, j, alpha * s22);
                }

                if (m != mm) {
                    double s21 = 0.0, s22 = 0.0;
                    for (int k = 0; k < p; k++) {
                        s21 += a21.get(m, k) * b11_j[k] + a22.get(m, k) * b21_j[k];
                        s22 += a21.get(m, k) * b12_j[k] + a22.get(m, k) * b22_j[k];
                    }

                    if (p != pp) { // Only need to consider A22 since it's the only one mm x pp
                        s21 += a22.get(m, p) * b21_j[p];
                        s22 += a22.get(m, p) * b22_j[p];
                    }

                    c21.add(m, j, alpha * s21);
                    c22.add(m, j, alpha * s22);
                }
            }

            if (n != nn) {
                b12.getColumn(n).copyInto(b12_j);
                b22.getColumn(n).copyInto(b22_j);

                for (int i = 0; i < m; i++) {
                    double s12 = 0.0, s22 = 0.0;
                    for (int k = 0; k < p; k++) {
                        s12 += a11.get(i, k) * b12_j[k] + a12.get(i, k) * b22_j[k];
                        s22 += a21.get(i, k) * b12_j[k] + a22.get(i, k) * b22_j[k];
                    }

                    if (p != pp) { // Only need to consider A12 and A22 as A11 and A21 are m x p and mm x p respectively
                        s12 += a12.get(i, p) * b22_j[p];
                        s22 += a22.get(i, p) * b22_j[p];
                    }

                    c12.add(i, n, alpha * s12);
                    c22.add(i, n, alpha * s22);
                }

                if (m != mm) {
                    double s22 = 0.0;
                    for (int k = 0; k < p; k++) {
                        s22 += a21.get(m, k) * b12_j[k] + a22.get(m, k) * b22_j[k];
                    }

                    if (p != pp) { // Only need to consider A22 since it's the only one mm x pp
                        s22 += a22.get(m, p) * b22_j[p];
                    }

                    c22.add(m, n, alpha * s22);
                }
            }
        }

        @Override
        protected void compute() {
            if (largestDimension <= leafDimension) {
                final int m = M / 2, p = P / 2, n = N / 2;
                computeDirectly(
                        alpha,
                        a.slice(0, m, 0, p).compact(), a.slice(0, m, p, P).compact(),
                        a.slice(m, M, 0, p).compact(), a.slice(m, M, p, P).compact(),
                        b.slice(0, p, 0, n), b.slice(0, p, n, N), b.slice(p, P, 0, n), b.slice(p, P, n, N),
                        c.slice(0, m, 0, n), c.slice(0, m, n, N), c.slice(m, M, 0, n), c.slice(m, M, n, N)
                );
            } else if (M >= Math.max(P, N)) {
                final int m = M / 2;
                invokeAll(
                        new GeneralMatrixMultiply(leafDimension, alpha, a.slice(0, m, 0, P), b, c.slice(0, m, 0, N)),
                        new GeneralMatrixMultiply(leafDimension, alpha, a.slice(m, M, 0, P), b, c.slice(m, M, 0, N))
                );
            } else if (N >= Math.max(M, P)) {
                final int n = N / 2;
                invokeAll(
                        new GeneralMatrixMultiply(leafDimension, alpha, a, b.slice(0, P, 0, n), c.slice(0, M, 0, n)),
                        new GeneralMatrixMultiply(leafDimension, alpha, a, b.slice(0, P, n, N), c.slice(0, M, n, N))
                );
            } else if (P >= Math.max(M, N)) {
                final int p = P / 2;
                new GeneralMatrixMultiply(leafDimension, alpha, a.slice(0, M, 0, p), b.slice(0, p, 0, N), c).invoke();
                new GeneralMatrixMultiply(leafDimension, alpha, a.slice(0, M, p, P), b.slice(p, P, 0, N), c).invoke();
            }
        }
    }


    private static final class TriangularMatrixMultiply extends RecursiveAction {
        private static final long serialVersionUID = 2566423202392340965L;

        private final DenseTriangularMatrix a;
        private final DenseMatrix b;
        private final DenseMatrixBuilder c;
        private final double alpha;
        private final int N, P;
        private final int leafDimension;

        private TriangularMatrixMultiply(int leafDimension, double alpha, DenseTriangularMatrix a, DenseMatrix b, DenseMatrixBuilder c) {
            this.alpha = alpha;

            this.a = a;
            this.N = a.getNumberOfRows();

            this.b = b;
            this.P = b.getNumberOfColumns();

            this.c = c;
            this.leafDimension = leafDimension;
        }

        private static void computeDirectly(double alpha, DenseTriangularMatrix a, DenseMatrix b, DenseMatrixBuilder c) {
            final int n = a.getNumberOfRows(), p = b.getNumberOfColumns();

            final double[] b_j = new double[n];
            for (int j = 0; j < p; j++) {
                b.getColumn(j).copyInto(b_j);

                for (int i = 0; i < n; i++) {
                    double sum = 0.0;
                    for (int k = i; k < n; k++) {
                        sum += a.get(i, k) * b_j[k];
                    }

                    c.add(i, j, alpha * sum);
                }
            }
        }

        @Override
        protected void compute() {
            if (N <= leafDimension) {
                computeDirectly(alpha, a, b, c);
            } else if (TriangularMatrixType.UpperTriangular == a.getTriangularMatrixType()) {
                final int n = (N + 1) / 2;
                invokeAll(
                        new TriangularMatrixMultiply(leafDimension, alpha, a.triangularSlice(0, n), b.slice(0, n, 0, P), c.slice(0, n, 0, P)),
                        new TriangularMatrixMultiply(leafDimension, alpha, a.triangularSlice(n, N), b.slice(n, N, 0, P), c.slice(n, N, 0, P))
                );

                new GeneralMatrixMultiply(leafDimension, alpha, a.slice(0, n, n, N), b.slice(n, N, 0, P), c.slice(0, n, 0, P)).invoke();
            } else {
                final int n = (N + 1) / 2;
                invokeAll(
                        new TriangularMatrixMultiply(leafDimension, alpha, a.triangularSlice(0, n), b.slice(0, n, 0, P), c.slice(0, n, 0, P)),
                        new TriangularMatrixMultiply(leafDimension, alpha, a.triangularSlice(n, N), b.slice(n, N, 0, P), c.slice(n, N, 0, P))
                );

                new GeneralMatrixMultiply(leafDimension, alpha, a.slice(0, n, n, N), b.slice(n, N, 0, P), c.slice(0, n, 0, P)).invoke();
            }
        }
    }
}
