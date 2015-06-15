/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStore.java` is part of Gauss.
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

import org.mpetnuch.gauss.store.Store;
import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.array.ArrayStructure;
import org.mpetnuch.gauss.structure.array.spliterator.ArrayStructureSpliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface ArrayStore extends Store {
    @Override
    ArrayStore1D reshape(int length);

    @Override
    ArrayStore2D reshape(int length, int width);

    @Override
    ArrayStore reshape(int... dimensions);

    @Override
    ArrayStore swapAxis(int axis1, int axis2);

    @Override
    ArrayStore slice(Slice... slices);


    @Override
    ArrayStructure structure();

    @Override
    ArrayStructureSpliterator spliterator();

    ArrayStore compact();
}
