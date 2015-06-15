/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `NaturalOrderSpliterator.java` is part of Gauss.
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

package org.mpetnuch.gauss.structure.array.spliterator;

import org.mpetnuch.gauss.structure.array.ArrayStructure;

/**
 * @author Michael Petnuch
 */
public final class NaturalOrderSpliterator extends AbstractArrayStructureSpliterator<ArrayStructure> {
    private int[] indices;

    public NaturalOrderSpliterator(ArrayStructure structure, double[] array) {
        super(structure, array);
        this.indices = new int[structure.dimension()];
    }

    private NaturalOrderSpliterator(ArrayStructure structure, double[] array, int index, int fence) {
        super(structure, array, index, fence);
        this.indices = structure.indices(index);
    }

    @Override
    public OfDouble trySplit() {
        final int lo = index, mid = (lo + fence) >>> 1;
        if (lo >= mid) {
            // can't split any more
            return null;
        }

        // update the current spliterators index and indices to reflect
        // that it has been split in half
        indices = structure.indices(index = mid);

        return new NaturalOrderSpliterator(structure, array, lo, mid);
    }

    @Override
    int nextArrayIndex(int currentArrayIndex) {
        for (int i = structure.dimension() - 1; i >= 0; i--) {
            if (++indices[i] < structure.dimensionLength(i)) {
                currentArrayIndex += structure.stride(i);
                return currentArrayIndex;
            }

            indices[i] = 0;
            currentArrayIndex -= structure.backstride(i);
        }

        return currentArrayIndex;
    }
}
