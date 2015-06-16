/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `MathUtils.java` is part of Gauss.
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

package org.mpetnuch.gauss.misc;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Michael Petnuch
 */
public class MathUtils {
    public static int ceilDiv(int numerator, int denominator) {
        return (int) Math.ceil((double) numerator / denominator);
    }

    public static void swap(int[] array, int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public static int product(int[] a) {
        return Arrays.stream(a).reduce(1, (product, x) -> product * x);
    }

    public static IntStream product(int[] a, int[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException();
        }

        return IntStream.range(0, a.length).map(n -> a[n] * b[n]);
    }
}
