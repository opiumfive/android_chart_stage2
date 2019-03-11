package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.view.TextureView;


public class TextureViewHolder implements ISurfaceHolder {

    private final TextureView textureView;

    public TextureViewHolder(TextureView textureView) {
        this.textureView = textureView;
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
        textureView.unlockCanvasAndPost(canvas);
    }

    @Override
    public Canvas lockCanvas() {
        return textureView.lockCanvas();
    }
}
