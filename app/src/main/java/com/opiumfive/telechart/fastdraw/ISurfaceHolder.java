package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;

public interface ISurfaceHolder {

    void unlockCanvasAndPost(Canvas canvas);

    Canvas lockCanvas();
}
