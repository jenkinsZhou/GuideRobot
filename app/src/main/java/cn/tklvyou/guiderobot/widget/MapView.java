package cn.tklvyou.guiderobot.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.Pose;

import java.util.ArrayList;

import cn.tklvyou.guiderobot.utils.ImageUtil;
import cn.tklvyou.guiderobot.utils.RPGestureDetector;

/**
 * Created by Forrest on 2019/04/20.
 */
public class MapView extends View {

    private final static String TAG = "SlamMapView";
    private final int TILE_PIXEL_SIZE = 200;

    // 将大地图分解为多个小地图
    private ArrayList<Tile> tiles;

    private Paint paint;

    // 控制地图的矩阵
    private Matrix matrix = new Matrix();

    private Map map;

    public MapView(Context context, Map map) {
        super(context);
        this.map = map;
        init();

    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.tiles = new ArrayList<Tile>(10);
        setBackgroundColor(Color.TRANSPARENT);

        paint = new Paint();
        paint.setDither(false);
    }


    @Override
    public void invalidate() {
        super.invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        try {
            canvas.save();
            drawTiles(canvas, paint);
            canvas.restore();
        }catch (Exception e){

        }
    }

    private void drawTiles(Canvas canvas, Paint paint) {
        if (tiles == null || tiles.isEmpty()) return;

        Matrix innerMatrix = new Matrix();

        for (Tile tile : tiles) {
            if (tile == null) return;

            innerMatrix.reset();
            Bitmap bmp = tile.bitmap;
            calculateRectTranslateMatrix(new RectF(0, 0, bmp.getWidth(), bmp.getHeight()), new RectF(tile.area), innerMatrix);
            innerMatrix.postConcat(matrix);
            canvas.drawBitmap(bmp, innerMatrix, paint);
        }
    }

    public void setMap(Map map) {
        this.map = map;
        int width = map.getDimension().getWidth();
        int height = map.getDimension().getHeight();

        int numX = (int) Math.ceil(((double) width) / TILE_PIXEL_SIZE);
        int numY = (int) Math.ceil(((double) height) / TILE_PIXEL_SIZE);
        tiles.clear();

        for (int i = 0; i < numX; i++) {
            int left = TILE_PIXEL_SIZE * i;
            int right = left + TILE_PIXEL_SIZE;
            if (right > width) right = width;

            for (int j = 0; j < numY; j++) {
                int top = TILE_PIXEL_SIZE * j;
                int bottom = top + TILE_PIXEL_SIZE;
                if (bottom > height) bottom = height;

                Tile tile = new Tile();
                tile.area = new Rect(left, top, right, bottom);

                // 创建Bitmap
                byte[] buffer = new byte[tile.area.width() * tile.area.height()];

                fetch(map, tile.area, buffer);
                Bitmap bmp = ImageUtil.createImage(buffer, tile.area.width(), tile.area.height());
                tile.bitmap = bmp;
                tiles.add(tile);
            }
        }
        invalidate();
    }

    private void fetch(Map map, Rect area, byte[] buffer) {
        int mapWidth = map.getDimension().getWidth();

        for (int i = area.top; i < area.bottom; i++) {
            int srcPos = mapWidth * +i + area.left;
            int destPos = area.width() * (i - area.top);
            System.arraycopy(map.getData(), srcPos, buffer, destPos, area.width());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }


    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
        invalidate();
    }

    // 小地图
    class Tile {
        public Rect area;
        public Bitmap bitmap;

        Tile() {
        }
    }

    public static void calculateRectTranslateMatrix(RectF from, RectF to, Matrix result) {
        if (from == null || to == null || result == null) {
            return;
        }
        if (from.width() == 0 || from.height() == 0) {
            return;
        }
        result.reset();
        result.postTranslate(-from.left, -from.top);
        result.postScale(to.width() / from.width(), to.height() / from.height());
        result.postTranslate(to.left, to.top);
    }

    private RPGestureDetector gestureDetector = new RPGestureDetector(new RPGestureDetector.OnRPGestureListener() {
        @Override
        public void onMapTap(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            com.slamtec.slamware.geometry.PointF pointF = widgetCoordinateToMapCoordinate(new PointF(x, y));

            Log.d(TAG, "onTouch: x = " + x + " y = " + y);
            Log.d(TAG, "onMapTap: x = " + pointF.getX() + " y = " + pointF.getY());
        }

        @Override
        public void onMapPinch(float factor, PointF centerPoint) {
            matrix.postScale(factor, factor, centerPoint.x, centerPoint.y);
            invalidate();
        }

        @Override
        public void onMapMove(int distanceX, int distanceY) {
            matrix.postTranslate(distanceX, distanceY);
            invalidate();
        }

    }, this);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    // 控件坐标转换为地图坐标
    public com.slamtec.slamware.geometry.PointF widgetCoordinateToMapCoordinate(PointF widgetPointF) {
        Matrix m = matrix;

        float[] points = new float[]{widgetPointF.x, widgetPointF.y};

        float[] dst = new float[2];
        Matrix inverse = new Matrix();
        m.invert(inverse);
        inverse.mapPoints(dst, points);
        LogUtils.e(widgetPointF.x, widgetPointF.y, dst[0], dst[1]);
        com.slamtec.slamware.geometry.PointF point = mapPixelCoordinateToMapCoordinate(new PointF(dst[0], dst[1]));
        return new com.slamtec.slamware.geometry.PointF(point.getX(), point.getY());
    }

    // 地图像素坐标转换为地图坐标
    public com.slamtec.slamware.geometry.PointF mapPixelCoordinateToMapCoordinate(PointF pixel) {
        PointF offset = new PointF(pixel.x * map.getResolution().getX(), pixel.y * map.getResolution().getY());
        return new com.slamtec.slamware.geometry.PointF(map.getOrigin().getX() + offset.x, map.getOrigin().getY() + offset.y);
    }


    // 地图坐标转换为屏幕坐标
    public PointF mapCoordinateWidgetCoordinate(Pose pose) {
        float x = (pose.getX() - map.getOrigin().getX()) / map.getResolution().getX();
        float y = (pose.getY() - map.getOrigin().getY()) / map.getResolution().getY();

        float[] points = new float[]{x, y};
        float[] dst = new float[2];
        matrix.mapPoints(dst, points);

        Log.d(TAG, "testMap: x = " + dst[0] + " y = " + dst[1]);
        return new PointF(dst[0], dst[1]);

    }

}
