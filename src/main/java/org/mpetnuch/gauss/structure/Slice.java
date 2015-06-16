/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `Slice.java` is part of Gauss.
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

package org.mpetnuch.gauss.structure;

/**
 * @author Michael Petnuch
 */
public abstract class Slice {
    private static final Slice ALL = new All(1);

    public static Slice All() {
        return ALL;
    }

    public static Slice All(int step) {
        return new All(step);
    }

    public static Slice S(int start, int stop) {
        return new Range(start, stop, 1);
    }

    public static Slice S(int start, int stop, int step) {
        return new Range(start, stop, step);
    }

    public abstract int start(Dimension dimension);

    public abstract int stop(Dimension dimension);

    public abstract int step();

    private static final class Range extends Slice {
        private int start, stop, step;

        private Range(int start, int stop, int step) {
            this.start = start;
            this.stop = stop;
            this.step = step;
        }

        public int start(Dimension dimension) {
            return dimension.index(start);
        }

        public int stop(Dimension dimension) {
            if (stop != 0) {
                return dimension.index(stop, true);
            } else {
                return 0;
            }
        }

        public int step() {
            return step;
        }
    }

    private static final class All extends Slice {
        private final int step;

        public All(int step) {
            this.step = step;
        }

        @Override
        public int start(Dimension dimension) {
            return step > 0 ? 0 : dimension.length() - 1;
        }

        @Override
        public int stop(Dimension dimension) {
            // we need to set stop to be -1 when we have a negative step as we need
            // to include zero as the stop
            return step > 0 ? dimension.length() : -1;
        }

        @Override
        public int step() {
            return step;
        }
    }
}
