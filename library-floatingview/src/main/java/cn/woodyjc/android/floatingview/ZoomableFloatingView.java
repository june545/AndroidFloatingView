package cn.woodyjc.android.floatingview;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

/**
 * 可两指触摸缩放的
 * <p>
 * 缩放的计算是通过两个触摸点之间的距离变化量计算的，这样不用各自计算横纵坐标缩放距离，使得缩放计算会显得简单些。
 * Created by June on 2016/8/17.
 */
public class ZoomableFloatingView extends DraggableFloatingView {
	private final String TAG = ZoomableFloatingView.class.getSimpleName();
	private Context context;

	private boolean enableZoom;
	private boolean zoomFlag;
	private double  startDis; // 两点初始距离
	private double  lastDis; // 上一次缩放后两点之间的距离

	public ZoomableFloatingView(Context context) {
		super(context);
	}

	/**
	 * 设置可两指缩放
	 * @param enable 默认可缩放
	 */
	public void setZoomable(boolean enable) {
		this.enableZoom = enable;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// >>>计算两点缩放<<<

		int pointerCount = event.getPointerCount();
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				if (pointerCount == 2) {
					zoomFlag = true;
					float disX = event.getX(0) - event.getX(1);
					float disY = event.getY(0) - event.getY(1);
					startDis = Math.sqrt(disX * disX + disY * disY); // 两个触摸点的初始距离
					lastDis = 0;
				}

				break;
			case MotionEvent.ACTION_MOVE:
				if (zoomFlag && pointerCount == 2) {
					float disX = event.getX(0) - event.getX(1);
					float disY = event.getY(0) - event.getY(1);
					double distance = Math.sqrt(disX * disX + disY * disY); // 瞬时距离
					Log.d(TAG, "---action move -> lastDis:" + lastDis + ", distance:" + distance + ", delta:" + (distance - lastDis));

					if (lastDis != 0 && Math.abs(distance - lastDis) > 0) { // 两个触摸点之间距离的变化量
						Log.d(TAG, " can update >>> distance=" + distance + ", startDis=" + startDis);
						// 如果(distance > startDis)为放大(ZoomIn)
						// 如果(distance < startDis)为缩小(ZoomOut)
						updateSize((int) (distance - lastDis));
					}
					lastDis = distance;
				}

				break;
			case MotionEvent.ACTION_UP:
				break;

			case MotionEvent.ACTION_POINTER_UP:
				if (pointerCount == 1) { // 由多个点释放到只剩一个点时，解除缩放状态
					zoomFlag = false;
				}
				break;
		}
		return super.dispatchTouchEvent(event);
	}

	/**
	 * 根据缩放距离的变化量来计算View的尺寸大小和位置坐标。
	 * 先把距离变化量作为横向缩放距离，同比例缩放时利用宽高比计算出纵向缩放距离即可。
	 *
	 * @param deltaDis 可正可负
	 */
	protected void updateSize(int deltaDis) {
		// >>> 计算尺寸
		int dw = deltaDis;
		int dh = (int) ((float) winParams.height / winParams.width * dw);
		int w = winParams.width + dw;
		int h = winParams.height + dh;
		Log.d(TAG, w + " ---  " + dm.widthPixels);
		if (w > dm.widthPixels || w < dm.widthPixels / 2) { // 设置缩放边界(这里只计算了宽度)
			return;
		}
		winParams.width = w;
		winParams.height = h;

		// >>>计算坐标，以中心点位置固定来计算
		int x = winParams.x - dw / 2;
		int y = winParams.y - dh / 2;
		super.updateLayout(x, y);
	}
}