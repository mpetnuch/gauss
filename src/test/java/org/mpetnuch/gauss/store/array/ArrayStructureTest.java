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

import java.util.Arrays;

/**
 * @author Michael Petnuch
 */
public class ArrayStructureTest {
    @Test
    public void testStructure() {
        final int dimensions[] = new int[]{5, 7};
        final int strides[] = new int[]{1, 7};
        ArrayStructure structure = new ArrayStoreAnyD.ArrayStructureAnyD(
                dimensions,
                strides,
                0
        );

        for (int i = 0; i < 35; i++) {
            int[] tuple = structure.indicies(i);
            int k = structure.ordinal(tuple);
            System.out.println(String.format("%d -> %s -> %d", i, Arrays.toString(tuple), k));
        }
    }
}
