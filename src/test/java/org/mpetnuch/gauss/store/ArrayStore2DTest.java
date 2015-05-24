package org.mpetnuch.gauss.store;

import org.junit.Test;

import java.util.Arrays;
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
