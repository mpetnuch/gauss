/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructureTest.java` is part of Gauss.
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

package org.mpetnuch.gauss.store.array;

import org.junit.Test;
import org.mpetnuch.gauss.structure.array.ArrayStructure2D;

import java.util.Arrays;

import static org.mpetnuch.gauss.structure.Slice.All;
import static org.mpetnuch.gauss.structure.Slice.S;

/**
 * @author Michael Petnuch
 */
public class ArrayStructureTest {
    @Test
    public void testStructure() {
        final double[] x = new double[]{
                1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16,
                17, 18, 19, 20, 21, 22, 23, 24,
                25, 26, 27, 28, 29, 30, 31, 32
        };

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();

        final ArrayStructure2D wholeStructure = new ArrayStructure2D(4, 8, 0);
        final ArrayStore2D wholeStore = new ArrayStore2D(x, wholeStructure);
        for (double[] row : wholeStore.slice(All(), S(0, 0, 1)).toArray2D()) {
            System.out.println(Arrays.toString(row));
        }


        System.out.println();

        final ArrayStructure2D structure = new ArrayStructure2D(2, 16, 8, 2, 0);
        final ArrayStore2D store = new ArrayStore2D(x, structure);
        for (double[] row : store.toArray2D()) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println();

        final ArrayStructure2D structureX = new ArrayStructure2D(8, 4, 2, 2, 0);
        final ArrayStore2D storeX = new ArrayStore2D(x, structureX);
        for (double[] row : storeX.toArray2D()) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println();

        final ArrayStore2D storeSlice = store.slice(0, 2, 2, 4);
        for (double[] row : storeSlice.toArray2D()) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println();

        final ArrayStore2D storeReshape = store.reshape(8, 2);
        for (double[] row : storeReshape.slice(All(), All(-1)).toArray2D()) {
            System.out.println(Arrays.toString(row));
        }
    }
}
