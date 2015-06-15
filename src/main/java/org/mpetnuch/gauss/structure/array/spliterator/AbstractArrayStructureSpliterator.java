/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `AbstractArrayStructureSpliterator.java` is part of Gauss.
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

import java.util.Objects;
import java.util.function.DoubleConsumer;

/**
 * @author Michael Petnuch
 */
public abstract class AbstractArrayStructureSpliterator<Structure extends ArrayStructure> implements ArrayStructureSpliterator {
    final double[] array;
    final Structure structure;

    final int fence;  // one past last index
    int index;        // current index, modified on advance/split
    int arrayIndex;   // current index in the array, modified on advance/split

    protected AbstractArrayStructureSpliterator(Structure structure, double[] array, int index, int fence) {
        this.array = array;
        this.fence = fence;
        this.index = index;
        this.structure = structure;

        // need to double check that the structure isn't empty before asking for the index
        this.arrayIndex = structure.size() > 0 ? structure.index(index) : -1;
    }

    protected AbstractArrayStructureSpliterator(Structure structure, double[] array) {
        this(structure, array, 0, structure.size());
    }

    abstract int nextArrayIndex(int currentArrayIndex);

    @Override
    public void forEachRemaining(DoubleConsumer action) {
        Objects.requireNonNull(action);

        // hoist accesses and checks from loop
        int i;
        if ((i = index) >= 0 && i < (index = fence)) {
            do {
                action.accept(array[arrayIndex]);
                arrayIndex = nextArrayIndex(arrayIndex);
            } while (++i < fence);
        }
    }

    @Override
    public boolean tryAdvance(DoubleConsumer action) {
        Objects.requireNonNull(action);

        if (index < 0 || index >= fence) {
            return false;
        }

        action.accept(array[arrayIndex]);
        arrayIndex = nextArrayIndex(arrayIndex);
        index++;
        return true;
    }

    @Override
    public long estimateSize() {
        return (long) (fence - index);
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | ORDERED | SIZED | SUBSIZED;
    }
}
