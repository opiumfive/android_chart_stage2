package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.os.Build;
import android.view.Surface;
import android.view.TextureView;


public class TextureViewHolder implements ISurfaceHolder {

    //private final TextureView textureView;
    private Surface surface;

    public TextureViewHolder(TextureView textureView) {
        //this.textureView = textureView;
        this.surface = new Surface(textureView.getSurfaceTexture());
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
        surface.unlockCanvasAndPost(canvas);
    }

    @Override
    public Canvas lockCanvas() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return surface.lockHardwareCanvas();
        } else {
            return surface.lockCanvas(null);
        }
    }
}
