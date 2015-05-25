package org.mpetnuch.gauss.store;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.junit.Test;
import org.mpetnuch.gauss.matrix.dense.DenseMatrix;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore2DTest {

    private final int[] dimensionLength = {7, 5, 9};

    @Test
    public void testRowMajorArrayStructure2D() {
        ArrayStore2D arrayStore2D = ArrayStore2D.from(new double[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        System.out.println(arrayStore2D.get(2, 2, 2));
    }

    @Test
    public void matrixMult() {
        final Random randomStream = new Random();
        final int M = 100, N = 100, P = 100;
        final double[][] dataA = new double[M][];
        Arrays.setAll(dataA, value -> randomStream.doubles(P).toArray());

        final double[][] dataB = new double[P][];
        Arrays.setAll(dataB, value -> randomStream.doubles(N).toArray());

        final DenseMatrix a = DenseMatrix.from(dataA);
        final DenseMatrix b = DenseMatrix.from(dataB);

        final Array2DRowRealMatrix aa = new Array2DRowRealMatrix(dataA);
        final Array2DRowRealMatrix bb = new Array2DRowRealMatrix(dataB);

        final BlockRealMatrix aaa = new BlockRealMatrix(dataA);
        final BlockRealMatrix bbb = new BlockRealMatrix(dataB);

        final long t1 = System.currentTimeMillis();
        final DenseMatrix c = a.multiply(b);
        final long t2 = System.currentTimeMillis() - t1;


        System.out.println("Michael: " + t2);

        final long t3 = System.currentTimeMillis();
        final Array2DRowRealMatrix cc = aa.multiply(bb);
        final long t4 = System.currentTimeMillis() - t3;

        System.out.println("ApacheA: " + t4);

        final long t5 = System.currentTimeMillis();
        final BlockRealMatrix ccc = aaa.multiply(bbb);
        final long t6 = System.currentTimeMillis() - t5;

        System.out.println("ApacheB: " + t6);


//        for(int i = 0; i < M; i++) {
//            for(int j = 0; j < N; j++) {
//                Assert.assertEquals(c.get(i, j), cc.getEntry(i, j), 1.0e-6);
//            }
//        }
    }


    // max --> 315
    // 101 -> {2, 1, 2}
    // 255 -> {5, 3, 3}
    //
    @Test
    public void xx() {
        int dimension = 3;
        int[] indicies = {2, 1, 2};

        int splitAdvance = 255 - 101;
        int[] subDimensionSize = new int[dimension];
        for (int i = dimension - 1, p = 1; i >= 0; i--) {
            subDimensionSize[i] = p;
            p *= dimensionLength[i];
        }

        int size = Arrays.stream(dimensionLength).reduce(1, (left, right) -> left * right);

        int element = IntStream.range(0, dimension).
                reduce(0, (left, right) -> left + indicies[right] * subDimensionSize[right]);

        int maxSubDimension = dimension - 1;
        for (; maxSubDimension >= 0; maxSubDimension--) {
            final int n = subDimensionSize[maxSubDimension];
            final int l = dimensionLength[maxSubDimension];
            for (int j = indicies[maxSubDimension]; splitAdvance >= n && j < l; j++) {
                splitAdvance -= n;
                indicies[maxSubDimension]++;
            }

            if (indicies[maxSubDimension] == l) {
                indicies[maxSubDimension] = 0;
                indicies[maxSubDimension - 1]++;
            }
        }

        for (maxSubDimension = maxSubDimension + 1; maxSubDimension < dimension; maxSubDimension++) {
            final int n = subDimensionSize[maxSubDimension];
            final int l = dimensionLength[maxSubDimension];
            for (int j = indicies[maxSubDimension]; splitAdvance >= n && j < l; j++) {
                splitAdvance -= n;
                indicies[maxSubDimension]++;
            }
        }


        System.out.println(Arrays.toString(indicies));

    }
}
