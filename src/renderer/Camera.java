package renderer;

import java.util.MissingResourceException;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Camera represents a pinhole camera in 3D space.
 * <p>
 * The camera is built using the nested {@link Builder} class.
 * It stores an orthonormal basis: {@code vTo}, {@code vUp}, {@code vRight}.
 * The view-plane configuration includes size, distance and pixel resolution.
 * </p>
 */
public class Camera implements Cloneable {

    /** Camera position (lens center). */
    private Point _p0;

    /** Camera forward direction (normalized). */
    private Vector _vTo;

    /** Camera up direction (normalized, orthogonal to {@code _vTo}). */
    private Vector _vUp;

    /** Camera right direction (normalized, orthogonal to {@code _vTo} and {@code _vUp}). */
    private Vector _vRight;

    /** View-plane width. */
    private double _vpWidth;

    /** View-plane height. */
    private double _vpHeight;

    /** View-plane distance from camera. */
    private double _vpDistance;

    /** Horizontal resolution (number of columns). */
    private int _nX = 1;

    /** Vertical resolution (number of rows). */
    private int _nY = 1;

    /** Precomputed view-plane center point. */
    private Point _vpCenter;

    /** Precomputed pixel width. */
    private double _pixelWidth;

    /** Precomputed pixel height. */
    private double _pixelHeight;

    /**
     * Private default constructor – cameras are constructed via {@link Builder}.
     */
    private Camera() {
    }

    /**
     * Factory method for creating a new {@link Builder}.
     *
     * @return a new builder instance
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Constructs a ray from the camera location through the center of the pixel
     * identified by {@code (xIndex, yIndex)} on the view plane.
     *
     * @param xIndex pixel column index (X)
     * @param yIndex pixel row index (Y)
     * @return constructed ray
     */
    public Ray constructRay(int xIndex, int yIndex) {
        // Pixel center offsets relative to view-plane center
        double xJ = (xIndex - (_nX - 1) / 2d) * _pixelWidth;
        double yI = -(yIndex - (_nY - 1) / 2d) * _pixelHeight;

        Point pIJ = _vpCenter;
        if (!isZero(xJ)) {
            pIJ = pIJ.add(_vRight.scale(xJ));
        }
        if (!isZero(yI)) {
            pIJ = pIJ.add(_vUp.scale(yI));
        }

        return new Ray(_p0, pIJ.subtract(_p0));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Builder for {@link Camera}.
     * <p>
     * Setters do not validate or compute; all validation/computation happens in
     * {@link #build()}.
     * </p>
     */
    public static class Builder {
        private final Camera _camera = new Camera();

        // Temporary direction inputs (resolved during build)
        private Vector _toVector;
        private Point _targetPoint;
        private Vector _generalUp = Vector.AXIS_Y;

        /** Sets the camera location. */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /** Sets the camera direction explicitly by vectors (to, up). */
        public Builder setDirection(Vector to, Vector up) {
            _toVector = to;
            _targetPoint = null;
            _generalUp = up == null ? Vector.AXIS_Y : up;
            return this;
        }

        /** Sets the camera direction by target point and explicit up vector. */
        public Builder setDirection(Point target, Vector up) {
            _targetPoint = target;
            _toVector = null;
            _generalUp = up == null ? Vector.AXIS_Y : up;
            return this;
        }

        /** Sets the camera direction by target point only (up defaults to +Y axis). */
        public Builder setDirection(Point target) {
            _targetPoint = target;
            _toVector = null;
            _generalUp = Vector.AXIS_Y;
            return this;
        }

        /** Sets view-plane size. */
        public Builder setVpSize(double width, double height) {
            _camera._vpWidth = width;
            _camera._vpHeight = height;
            return this;
        }

        /** Sets view-plane distance from the camera. */
        public Builder setVpDistance(double distance) {
            _camera._vpDistance = distance;
            return this;
        }

        /** Sets view-plane resolution (number of pixels). */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Builds a valid camera instance.
         *
         * @return a cloned camera ready for use
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();
            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException ex) {
                return null;
            }
        }

        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("View-plane resolution must be positive");
            }
        }

        private void checkLocationAndDirection() {
            if (_camera._p0 == null) {
                throw new MissingResourceException("Camera location is missing", "Camera", "location");
            }

            if (_generalUp == null) {
                throw new MissingResourceException("Camera up direction is missing", "Camera", "up");
            }

            Vector vTo;
            if (_toVector != null) {
                vTo = _toVector;
            } else if (_targetPoint != null) {
                vTo = _targetPoint.subtract(_camera._p0);
            } else {
                throw new MissingResourceException("Camera direction is missing", "Camera", "direction");
            }

            // Normalize forward vector
            vTo = vTo.normalize();
            _camera._vTo = vTo;

            // Build orthonormal basis using provided general-up
            Vector vRight;
            try {
                vRight = vTo.crossProduct(_generalUp).normalize();
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Camera direction vectors are parallel", ex);
            }

            Vector vUp = vRight.crossProduct(vTo).normalize();

            _camera._vRight = vRight;
            _camera._vUp = vUp;
        }

        private void checkViewPlane() {
            if (_camera._vpWidth <= 0 || _camera._vpHeight <= 0) {
                throw new IllegalArgumentException("View-plane size must be positive");
            }
            if (_camera._vpDistance <= 0) {
                throw new IllegalArgumentException("View-plane distance must be positive");
            }

            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._vpDistance));
            _camera._pixelWidth = _camera._vpWidth / _camera._nX;
            _camera._pixelHeight = _camera._vpHeight / _camera._nY;
        }
    }
}
