/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `JBlasLevel3Test.java` is part of Gauss.
 *
 * Gauss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.mpetnuch.gauss.linearalgebra.blas3;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mpetnuch.gauss.matrix.dense.DenseMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * @author Michael Petnuch
 */
@RunWith(Parameterized.class)
public class JBlasLevel3Test {

    private final int M, N, P;

    public JBlasLevel3Test(int[] sizes) {
        M = sizes[0];
        P = sizes[1];
        N = sizes[2];
    }

    @Parameterized.Parameters
    public static List<int[]> primeNumbers() {
        final List<int[]> parameters = new ArrayList<>();
        parameters.add(new int[]{3000, 2500, 3500});
        return parameters;
    }

    private static double[][] generateData(int m, int n) {
        final Random randomStream = new Random((long) m * n);
        final double[][] data = new double[m][];
        Arrays.setAll(data, value -> randomStream.doubles(n).toArray());
        return data;
    }

    @Test
    public void testDgemmSpeed() {
        final DenseMatrix a = DenseMatrix.from(generateData(M, P));
        final DenseMatrix b = DenseMatrix.from(generateData(P, N));

        final Array2DRowRealMatrix aa = new Array2DRowRealMatrix(generateData(M, P));
        final Array2DRowRealMatrix bb = new Array2DRowRealMatrix(generateData(P, N));

        IntStream.range(0, 100).mapToLong(value -> {
            final long t1 = System.currentTimeMillis();
            final DenseMatrix c = a.multiply(b);
            c.get(0, 0);
            return System.currentTimeMillis() - t1;
        }).average().ifPresent(System.out::println);

        IntStream.range(0, 100).mapToLong(value -> {
            final long t1 = System.currentTimeMillis();
            final Array2DRowRealMatrix cc = aa.multiply(bb);
            cc.getEntry(0, 0);
            return System.currentTimeMillis() - t1;
        }).average().ifPresent(System.out::println);
    }

    @Test
    public void testDgemm() {
        final DenseMatrix a = DenseMatrix.from(generateData(M, P));
        final DenseMatrix b = DenseMatrix.from(generateData(P, N));

        final Array2DRowRealMatrix aa = new Array2DRowRealMatrix(generateData(M, P));
        final Array2DRowRealMatrix bb = new Array2DRowRealMatrix(generateData(P, N));

        final DenseMatrix c = a.multiply(b);
        final Array2DRowRealMatrix cc = aa.multiply(bb);

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                Assert.assertEquals(c.get(i, j), cc.getEntry(i, j), 1.0e-6);
            }
        }
    }
}
