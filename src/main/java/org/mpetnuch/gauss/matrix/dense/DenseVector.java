package org.mpetnuch.gauss.matrix.dense;

import java.util.stream.DoubleStream;

import org.mpetnuch.gauss.matrix.Matrix;
import org.mpetnuch.gauss.matrix.Vector;
import org.mpetnuch.gauss.matrix.accessor.ArrayElementAccessor1D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseVector implements Vector  {
    private static final long serialVersionUID = 1577827666390044084L;
    private final ArrayElementAccessor1D elementAccessor;

    public DenseVector(ArrayElementAccessor1D elementAccessor) {
        this.elementAccessor = elementAccessor;
    }

    @Override
    public double get(int i) {
        return elementAccessor.get(i);
    }

    @Override
    public int length() {
        return elementAccessor.getLength();
    }

    @Override
    public Matrix reshape(int rowCount, int columnCount) {
        return new DenseGeneralMatrix(elementAccessor.reshape(rowCount, columnCount));
    }

    @Override
    public Vector slice(int startIndex, int endIndex) {
        return new DenseVector(elementAccessor.slice(startIndex, endIndex));
    }

    @Override
    public Vector compact() {
        return new DenseVector(elementAccessor.compact());
    }

    @Override
    public DoubleStream stream() {
        return elementAccessor.stream();
    }

    @Override
    public void toArray(double[] array) {
        elementAccessor.toArray(array);
    }
}
