package com.photo.mixer.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public abstract class StickerView extends FrameLayout {

    public static final String TAG = "stickerView";
    private final static int BUTTON_SIZE_DP = 30;
    private final static int SELF_SIZE_DP = 100;
    Paint mborderPaint;
    boolean isVisible;
    int count = 0;
    private BorderView iv_border;
    private ImageView miv_scale;
    private ImageView mmiv_delete;
    private ImageView miv_flip;
    // For scalling
    private float this_orgX = -1, this_orgY = -1;
    private float scale_orgX = -1, scale_orgY = -1;
    private double scale_orgWidth = -1, scale_orgHeight = -1;
    // For rotating
    private float rotate_orgX = -1, rotate_orgY = -1, rotate_newX = -1, rotate_newY = -1;
    // For moving
    private float move_orgX = -1, move_orgY = -1;
    private double centerX, centerY;
    public OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if (view.getTag().equals("DraggableViewGroup")) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.v(TAG, "sticker view action down");
                        move_orgX = event.getRawX();
                        move_orgY = event.getRawY();

//                        if (count == 0) {
//                            count = 1;
//                            setControlsVisibility(false);
//                        } else {
//                            count = 0;
//                            setControlsVisibility(true);
//                        }

                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "sticker view action move");
                        float offsetX = event.getRawX() - move_orgX;
                        float offsetY = event.getRawY() - move_orgY;
                        StickerView.this.setX(StickerView.this.getX() + offsetX);
                        StickerView.this.setY(StickerView.this.getY() + offsetY);
                        move_orgX = event.getRawX();
                        move_orgY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.v(TAG, "sticker view action up");
                        break;
                }
            } else if (view.getTag().equals("miv_scale")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.v(TAG, "miv_scale action down");

                        this_orgX = StickerView.this.getX();
                        this_orgY = StickerView.this.getY();

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();
                        scale_orgWidth = StickerView.this.getLayoutParams().width;
                        scale_orgHeight = StickerView.this.getLayoutParams().height;

                        rotate_orgX = event.getRawX();
                        rotate_orgY = event.getRawY();

                        centerX = StickerView.this.getX() +
                                ((View) StickerView.this.getParent()).getX() +
                                (float) StickerView.this.getWidth() / 2;


                        //double statusBarHeight = Math.ceil(25 * getContext().getResources().getDisplayMetrics().density);
                        int result = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            result = getResources().getDimensionPixelSize(resourceId);
                        }
                        double statusBarHeight = result;
                        centerY = StickerView.this.getY() +
                                ((View) StickerView.this.getParent()).getY() +
                                statusBarHeight +
                                (float) StickerView.this.getHeight() / 2;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "miv_scale action move");

                        rotate_newX = event.getRawX();
                        rotate_newY = event.getRawY();

                        double angle_diff = Math.abs(
                                Math.atan2(event.getRawY() - scale_orgY, event.getRawX() - scale_orgX)
                                        - Math.atan2(scale_orgY - centerY, scale_orgX - centerX)) * 180 / Math.PI;

                        Log.v(TAG, "angle_diff: " + angle_diff);

                        double length1 = getLength(centerX, centerY, scale_orgX, scale_orgY);
                        double length2 = getLength(centerX, centerY, event.getRawX(), event.getRawY());

                        int size = convertDpToPixel(SELF_SIZE_DP, getContext());
                        if (length2 > length1
                                && (angle_diff < 25 || Math.abs(angle_diff - 180) < 25)
                        ) {
                            //scale up
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            double offset = Math.max(offsetX, offsetY);
                            offset = Math.round(offset);
                            StickerView.this.getLayoutParams().width += offset;
                            StickerView.this.getLayoutParams().height += offset;
                            onScaling(true);
                            //DraggableViewGroup.this.setX((float) (getX() - offset / 2));
                            //DraggableViewGroup.this.setY((float) (getY() - offset / 2));
                        } else if (length2 < length1
                                && (angle_diff < 25 || Math.abs(angle_diff - 180) < 25)
                                && StickerView.this.getLayoutParams().width > size / 2
                                && StickerView.this.getLayoutParams().height > size / 2) {
                            //scale down
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            double offset = Math.max(offsetX, offsetY);
                            offset = Math.round(offset);
                            StickerView.this.getLayoutParams().width -= offset;
                            StickerView.this.getLayoutParams().height -= offset;
                            onScaling(false);
                        }

                        //rotate

                        double angle = Math.atan2(event.getRawY() - centerY, event.getRawX() - centerX) * 180 / Math.PI;
                        Log.v(TAG, "log angle: " + angle);

                        //setRotation((float) angle - 45);
                        setRotation((float) angle - 45);
                        Log.v(TAG, "getRotation(): " + getRotation());

                        onRotating();

                        rotate_orgX = rotate_newX;
                        rotate_orgY = rotate_newY;

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        postInvalidate();
                        requestLayout();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.v(TAG, "miv_scale action up");
                        break;
                }
            }
            return true;
        }
    };

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    private void init(Context context) {
        this.iv_border = new BorderView(context);
        this.miv_scale = new ImageView(context);
        this.mmiv_delete = new ImageView(context);
        this.miv_flip = new ImageView(context);

        this.miv_scale.setImageResource(R.drawable.zoom);
        this.mmiv_delete.setImageResource(R.drawable.cross);
        this.miv_flip.setImageResource(R.drawable.flip);

        this.setTag("DraggableViewGroup");
        this.iv_border.setTag("iv_border");
        this.miv_scale.setTag("miv_scale");
        this.mmiv_delete.setTag("mmiv_delete");
        this.miv_flip.setTag("miv_flip");

        int margin = convertDpToPixel(BUTTON_SIZE_DP, getContext()) / 2;
        int size = convertDpToPixel(SELF_SIZE_DP, getContext());

        LayoutParams this_params =
                new LayoutParams(
                        size,
                        size
                );
        this_params.gravity = Gravity.CENTER;

        LayoutParams iv_main_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        iv_main_params.setMargins(margin, margin, margin, margin);

        LayoutParams iv_border_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        iv_border_params.setMargins(margin, margin, margin, margin);

        LayoutParams miv_scale_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        miv_scale_params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        LayoutParams mmiv_delete_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        mmiv_delete_params.gravity = Gravity.TOP | Gravity.RIGHT;

        LayoutParams miv_flip_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        miv_flip_params.gravity = Gravity.TOP | Gravity.LEFT;

        this.setLayoutParams(this_params);
        this.addView(getMainView(), iv_main_params);
        this.addView(iv_border, iv_border_params);
        this.addView(miv_scale, miv_scale_params);
        this.addView(mmiv_delete, mmiv_delete_params);
        this.addView(miv_flip, miv_flip_params);
        this.setOnTouchListener(mTouchListener);
        this.miv_scale.setOnTouchListener(mTouchListener);
        this.mmiv_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StickerView.this.getParent() != null) {
                    ViewGroup myCanvas = ((ViewGroup) StickerView.this.getParent());
                    myCanvas.removeView(StickerView.this);
                }
            }
        });
        this.miv_flip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "flip the view");
                View mainView = getMainView();
                mainView.setRotationY(mainView.getRotationY() == -180f ? 0f : -180f);
                mainView.invalidate();
                requestLayout();
            }
        });
       /* this.iv_border.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count == 0) {
                    count = 1;
                    iv_border.setVisibility(GONE);
                    mmiv_delete.setVisibility(GONE);
                    miv_flip.setVisibility(GONE);
                    miv_scale.setVisibility(GONE);
                } else {
                    count = 0;
                    iv_border.setVisibility(VISIBLE);
                    mmiv_delete.setVisibility(VISIBLE);
                    miv_flip.setVisibility(VISIBLE);
                    miv_scale.setVisibility(VISIBLE);
                }
            }
        });*/
    }

    public boolean isFlip() {
        return getMainView().getRotationY() == -180f;
    }

    protected abstract View getMainView();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private double getLength(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }

    private float[] getRelativePos(float absX, float absY) {
        Log.v("ken", "getRelativePos getX:" + ((View) this.getParent()).getX());
        Log.v("ken", "getRelativePos getY:" + ((View) this.getParent()).getY());
        float[] pos = new float[]{
                absX - ((View) this.getParent()).getX(),
                absY - ((View) this.getParent()).getY()
        };
        Log.v(TAG, "getRelativePos absY:" + absY);
        Log.v(TAG, "getRelativePos relativeY:" + pos[1]);
        return pos;
    }

    public void setControlsVisibility(boolean isVisible) {
        if (!isVisible) {
            iv_border.setVisibility(View.GONE);
            mmiv_delete.setVisibility(View.GONE);
            miv_flip.setVisibility(View.GONE);
            miv_scale.setVisibility(View.GONE);
            visible = false;
        } else {
            iv_border.setVisibility(View.VISIBLE);
            mmiv_delete.setVisibility(View.VISIBLE);
            miv_flip.setVisibility(View.VISIBLE);
            miv_scale.setVisibility(View.VISIBLE);
            visible = true;
        }
    }
    static boolean visible;
    public boolean getControlsVisibility()
    {
        return visible;
    }

    protected View getImageViewFlip() {
        return miv_flip;
    }

    protected void onScaling(boolean scaleUp) {
    }

    protected void onRotating() {
    }

    private class BorderView extends View {

        public BorderView(Context context) {
            super(context);
        }

        public BorderView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public BorderView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // Draw sticker border
            LayoutParams params = (LayoutParams) this.getLayoutParams();
            Log.v(TAG, "params.leftMargin: " + params.leftMargin);
            Rect border = new Rect();
            border.left = (int) this.getLeft() - params.leftMargin;
            border.top = (int) this.getTop() - params.topMargin;
            border.right = (int) this.getRight() - params.rightMargin;
            border.bottom = (int) this.getBottom() - params.bottomMargin;
            mborderPaint = new Paint();
            mborderPaint.setStrokeWidth(6);
            mborderPaint.setColor(Color.WHITE);
            mborderPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(border, mborderPaint);
        }
    }
}
