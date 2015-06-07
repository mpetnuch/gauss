/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `NonContiguousStructureUnitStrideDimensionSpliterator.java` is part of Gauss.
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

import org.mpetnuch.gauss.structure.array.ArrayStructure2D;

import static org.mpetnuch.gauss.structure.Structure2D.COLUMN_DIMENSION;
import static org.mpetnuch.gauss.structure.Structure2D.ROW_DIMENSION;

/**
 * @author Michael Petnuch
 */
public final class NonContiguousStructureUnitStrideDimensionSpliterator extends AbstractArrayStructureSpliterator<ArrayStructure2D> {
    private final int nonUnitStriddedDimension;
    private final int nonUnitStridedDimensionLength;
    private final int nonUnitStriddedDimensionOffset;
    private int nonUnitStriddedDimensionIndex;

    private NonContiguousStructureUnitStrideDimensionSpliterator(ArrayStructure2D structure, double[] array, int index, int fence) {
        super(structure, array, index, fence);

        final int unitStrideDimension = structure.unitStrideDimension();
        switch (unitStrideDimension) {
            case ROW_DIMENSION:
                this.nonUnitStriddedDimension = COLUMN_DIMENSION;
                break;
            case COLUMN_DIMENSION:
                this.nonUnitStriddedDimension = ROW_DIMENSION;
                break;
            default:
                throw new AssertionError(String.format(
                        "ArrayStructure2D had unexpected unit stride dimensionLength: %d", unitStrideDimension));
        }

        this.nonUnitStriddedDimensionIndex = structure.indices(index)[nonUnitStriddedDimension];
        this.nonUnitStridedDimensionLength = structure.dimensionLength(nonUnitStriddedDimension);
        this.nonUnitStriddedDimensionOffset = structure.stride(nonUnitStriddedDimension) +
                structure.backstride(nonUnitStriddedDimension);
    }

    public NonContiguousStructureUnitStrideDimensionSpliterator(ArrayStructure2D structure, double[] array) {
        this(structure, array, 0, structure.lastIndex());
    }

    @Override
    public OfDouble trySplit() {
        final int lo = index, mid = (lo + fence) >>> 1;
        if (lo < mid) {
            this.nonUnitStriddedDimensionIndex = structure.indices(mid)[nonUnitStriddedDimension];
            return new NonContiguousStructureUnitStrideDimensionSpliterator(structure, array, lo, mid);
        } else {
            // can't split any more
            return null;
        }
    }

    @Override
    int nextArrayIndex(int currentArrayIndex) {
        if (++nonUnitStriddedDimensionIndex < nonUnitStridedDimensionLength) {
            return currentArrayIndex + 1;
        }

        nonUnitStriddedDimensionIndex = 0;
        return currentArrayIndex + nonUnitStriddedDimensionOffset;
    }
}
