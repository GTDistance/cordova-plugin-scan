/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thomas.view;

import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.thomas.zxing.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 */
public final class ViewfinderView extends View {
	private static final String TAG = "log";

	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;


	private int ScreenRate;


	private static final int CORNER_WIDTH = 4;
//	private static final int CORNER_WIDTH = 10;

	private static final int MIDDLE_LINE_WIDTH = 6;


	private static final int MIDDLE_LINE_PADDING = 5;


	private static final int SPEEN_DISTANCE = 5;


	private static float density;

	private static final int TEXT_SIZE = 16;

	private static final int TEXT_PADDING_TOP = 30;


	private Paint paint;


	private int slideTop;


	private int slideBottom;


	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;

	private final int resultPointColor;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	boolean isFirst;
	private Context context;

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		density = context.getResources().getDisplayMetrics().density;

		ScreenRate = (int)(50 * density);
//		ScreenRate = (int)(20 * density);

		paint = new Paint();
		Resources resources = getResources();
//		maskColor = resources.getColor(R.color.viewfinder_mask);
		maskColor = Color.parseColor("#60000000");

//		resultColor = resources.getColor(R.color.result_view);
		resultColor = Color.parseColor("#b0000000");

		resultPointColor = Color.parseColor("#c0ffff00");
//		resultPointColor = resources.getColor(R.color.possible_result_points);
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	@Override
	public void onDraw(Canvas canvas) {

		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}

		if(!isFirst){
			isFirst = true;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}


		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);



		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {


			paint.setColor(Color.GREEN);
//			canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
//					frame.top + CORNER_WIDTH, paint);
//			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top
//					+ ScreenRate, paint);
//			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
//					frame.top + CORNER_WIDTH, paint);
//			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top
//					+ ScreenRate, paint);
//			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
//					+ ScreenRate, frame.bottom, paint);
//			canvas.drawRect(frame.left, frame.bottom - ScreenRate,
//					frame.left + CORNER_WIDTH, frame.bottom, paint);
//			canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
//					frame.right, frame.bottom, paint);
//			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
//					frame.right, frame.bottom, paint);

			int scanWidth = 50;
			Rect lineRectLT = new Rect();
			lineRectLT.left = frame.left;
			lineRectLT.right = frame.left + scanWidth;
			lineRectLT.top = frame.top;
			lineRectLT.bottom = frame.top + scanWidth;
//			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.scan_left_top))).getBitmap(), null, lineRectLT, paint);
			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(getId("scan_left_top","drawable")))).getBitmap(), null, lineRectLT, paint);


			Rect lineRectRT = new Rect();
			lineRectRT.left = frame.right - scanWidth;
			lineRectRT.right = frame.right;
			lineRectRT.top = frame.top;
			lineRectRT.bottom = frame.top + scanWidth;
//			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.scan_right_top))).getBitmap(), null, lineRectRT, paint);
			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(getId("scan_right_top","drawable")))).getBitmap(), null, lineRectRT, paint);


			Rect lineRectLB = new Rect();
			lineRectLB.left = frame.left;
			lineRectLB.right = frame.left + scanWidth;
			lineRectLB.top = frame.bottom -scanWidth;
			lineRectLB.bottom = frame.bottom ;
//			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.scan_left_bottom))).getBitmap(), null, lineRectLB, paint);
			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(getId("scan_left_bottom","drawable")))).getBitmap(), null, lineRectLB, paint);


			Rect lineRectRB = new Rect();
			lineRectRB.left = frame.right - scanWidth;
			lineRectRB.right = frame.right;
			lineRectRB.top = frame.bottom -scanWidth;
			lineRectRB.bottom = frame.bottom ;
			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(getId("scan_right_bottom","drawable")))).getBitmap(), null, lineRectRB, paint);
//			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.scan_right_bottom))).getBitmap(), null, lineRectRB, paint);




			slideTop += SPEEN_DISTANCE;
			if(slideTop >= frame.bottom){
				slideTop = frame.top;
			}
//			canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH/2, frame.right - MIDDLE_LINE_PADDING,slideTop + MIDDLE_LINE_WIDTH/2, paint);
			Rect lineRect = new Rect();
			lineRect.left = frame.left;
			lineRect.right = frame.right;
			lineRect.top = slideTop;
//			lineRect.bottom = slideTop + 18;
			lineRect.bottom = slideTop + 3;
//			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.scan_center_line))).getBitmap(), null, lineRect, paint);
			canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(getId("scan_center_line","drawable")))).getBitmap(), null, lineRect, paint);

			paint.setColor(Color.WHITE);
			paint.setTextSize(TEXT_SIZE * density);
			paint.setAlpha(0x40);
			paint.setTypeface(Typeface.create("System", Typeface.BOLD));
//			String textPileCode = getResources().getString(R.string.scan_pile_code);
			String textPileCode = getResources().getString(getId("scan_pile_code","string"));
			float textWidth = paint.measureText(textPileCode);
			canvas.drawText(textPileCode, frame.left+(frame.right-frame.left-textWidth)/2, (float) (frame.bottom + (float)TEXT_PADDING_TOP *density), paint);



//			Collection<ResultPoint> currentPossible = possibleResultPoints;
//			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
//			if (currentPossible.isEmpty()) {
//				lastPossibleResultPoints = null;
//			} else {
//				possibleResultPoints = new HashSet<ResultPoint>(5);
//				lastPossibleResultPoints = currentPossible;
//				paint.setAlpha(OPAQUE);
//				paint.setColor(resultPointColor);
//				for (ResultPoint point : currentPossible) {
//					canvas.drawCircle(frame.left + point.getX(), frame.top
//							+ point.getY(), 6.0f, paint);
//				}
//			}
//			if (currentLast != null) {
//				paint.setAlpha(OPAQUE / 2);
//				paint.setColor(resultPointColor);
//				for (ResultPoint point : currentLast) {
//					canvas.drawCircle(frame.left + point.getX(), frame.top
//							+ point.getY(), 3.0f, paint);
//				}
//			}



			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);

		}
	}

	private int getId(String idName,String type){
		return context.getResources().getIdentifier(idName, type, context.getPackageName());
	}
	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 *
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

}
