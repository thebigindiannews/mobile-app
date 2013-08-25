package org.quantumbadger.redreader.ui.list;

import android.graphics.*;
import org.quantumbadger.redreader.common.General;

import java.util.concurrent.locks.ReentrantLock;

public class RRListViewCacheBlock {

	private final Bitmap cache;
	private final Canvas canvas;

	private final int width, height;

	protected int firstVisibleItemPos, pxInFirstVisibleItem;

	private final ReentrantLock updateLock = new ReentrantLock();

	private final int backgroundCol;
	private static final Paint invalidPaint = General.createPaint(Color.MAGENTA);

	public RRListViewCacheBlock(final int width, final int height, final int backgroundCol) {
		cache = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(cache);
		this.width = width;
		this.height = height;
		this.backgroundCol = backgroundCol;
	}

	public void assign(RRListViewFlattenedContents data, int firstVisibleItemPos, int pxInFirstVisibleItem) {

		updateLock.lock();

		this.firstVisibleItemPos = firstVisibleItemPos;
		this.pxInFirstVisibleItem = pxInFirstVisibleItem;

		final RRListViewItem[] items = data.items;

		while(pxInFirstVisibleItem < 0 && firstVisibleItemPos > 0) {
			firstVisibleItemPos--;
			pxInFirstVisibleItem += items[firstVisibleItemPos].setWidth(width);
		}

		while(firstVisibleItemPos < data.itemCount - 1 && pxInFirstVisibleItem >= items[firstVisibleItemPos].setWidth(width)) {

			if(firstVisibleItemPos >= items.length - 1) {
				firstVisibleItemPos = -1;
				break;
			} else {
				pxInFirstVisibleItem -= items[firstVisibleItemPos].getOuterHeight();
				firstVisibleItemPos++;
			}
		}

		int pos = data.items[firstVisibleItemPos].setWidth(width) - pxInFirstVisibleItem;
		int lastVisibleItemPos = firstVisibleItemPos;

		while(pos <= height && lastVisibleItemPos < data.itemCount - 1) {
			lastVisibleItemPos++;
			pos += data.items[lastVisibleItemPos].setWidth(width);
		}

		canvas.save();
		canvas.drawColor(backgroundCol, PorterDuff.Mode.CLEAR);
		canvas.translate(0, -pxInFirstVisibleItem);

		for(int i = firstVisibleItemPos; i <= lastVisibleItemPos; i++) {
			data.items[i].draw(canvas, width);
			canvas.translate(0, data.items[i].getOuterHeight());
		}

		canvas.restore();

		updateLock.unlock();
	}

	public boolean draw(Canvas canvas) {

		if(updateLock.tryLock()) {
			canvas.drawBitmap(cache, 0, 0, null);
			updateLock.unlock();
			return true;

		} else {
			canvas.drawRect(0, 0, width, height, invalidPaint);
			return false;
		}

	}
}