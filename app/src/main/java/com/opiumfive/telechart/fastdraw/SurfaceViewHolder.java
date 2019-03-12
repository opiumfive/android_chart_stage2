package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.os.Build;
import android.view.SurfaceHolder;


public class SurfaceViewHolder implements ISurfaceHolder {

    private final SurfaceHolder surfaceHolder;

    public SurfaceViewHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public Canvas lockCanvas() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return surfaceHolder.lockHardwareCanvas();
        }else{
            return surfaceHolder.lockCanvas(null);
        }
    }
}
