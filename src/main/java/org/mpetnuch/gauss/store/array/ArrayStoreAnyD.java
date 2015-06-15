/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStoreAnyD.java` is part of Gauss.
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

import org.mpetnuch.gauss.store.DataFlag;
import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.array.ArrayStructure;
import org.mpetnuch.gauss.structure.array.ArrayStructure1D;
import org.mpetnuch.gauss.structure.array.ArrayStructure2D;
import org.mpetnuch.gauss.structure.array.ArrayStructureAnyD;
import org.mpetnuch.gauss.structure.array.spliterator.ArrayStructureSpliterator;
import org.mpetnuch.gauss.structure.array.spliterator.NaturalOrderSpliterator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Michael Petnuch
 */
public class ArrayStoreAnyD implements ArrayStore {

    private final Set<DataFlag> flags = EnumSet.noneOf(DataFlag.class);
    private final ArrayStructureAnyD structure;
    private final double[] array;

    protected ArrayStoreAnyD(double[] array, ArrayStructureAnyD structure) {
        this.array = array;
        this.structure = structure;
    }

    protected ArrayStoreAnyD(double[] array, ArrayStructure structure) {
        this.array = array;
        this.structure = ArrayStructureAnyD.from(structure);
    }

    @Override
    public Set<DataFlag> flags() {
        return flags;
    }

    @Override
    public double get(int... indices) {
        return array[structure.index(indices)];
    }

    @Override
    public ArrayStore1D reshape(int length) {
        return new ArrayStore1D(toArray(), new ArrayStructure1D(length));
    }

    @Override
    public ArrayStore2D reshape(int rowCount, int columnCount) {
        return new ArrayStore2D(toArray(), new ArrayStructure2D(rowCount, columnCount));
    }

    @Override
    public ArrayStore reshape(int... dimensions) {
        switch (dimensions.length) {
            case 2:
                return reshape(dimensions[0], dimensions[1]);
            case 1:
                return reshape(dimensions[1]);
        }

        return new ArrayStoreAnyD(toArray(), new ArrayStructureAnyD(dimensions));
    }

    @Override
    public ArrayStoreAnyD slice(Slice... slices) {
        return new ArrayStoreAnyD(array, structure.slice(slices));
    }

    @Override
    public ArrayStoreAnyD swapAxis(int axis1, int axis2) {
        return new ArrayStoreAnyD(array, structure.swapAxis(axis1, axis2));
    }

    @Override
    public ArrayStructureAnyD structure() {
        return structure;
    }

    @Override
    public ArrayStore compact() {
        throw new NotImplementedException();
    }

    @Override
    public ArrayStructureSpliterator spliterator() {
        return new NaturalOrderSpliterator(structure, array);
    }
}
