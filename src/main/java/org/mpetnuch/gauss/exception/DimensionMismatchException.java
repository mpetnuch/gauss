/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `DimensionMismatchException.java` is part of Gauss.
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

package org.mpetnuch.gauss.exception;

/**
 * @author Michael Petnuch
 */
public class DimensionMismatchException extends IllegalArgumentException {
    private static final long serialVersionUID = 3567983539306804675L;
    private final Integer[] expected;
    private final Integer[] actual;

    public DimensionMismatchException(Integer actual, Integer expected) {
        this.expected = new Integer[]{expected};
        this.actual = new Integer[]{actual};
    }

    public DimensionMismatchException(Integer[] actual, Integer[] expected) {
        this.expected = expected;
        this.actual = actual;
    }
}
