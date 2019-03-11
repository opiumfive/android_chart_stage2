package com.opiumfive.telechart.fastdraw;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

public class FastTextureView extends TextureView implements TextureView.SurfaceTextureListener, IComposer {

    private SceneModelComposer sceneComposer;

    private DrawingThread drawingThread;

    public FastTextureView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        drawingThread = new DrawingThread(new TextureViewHolder(this));
        drawingThread.setRunning(true);
        drawingThread.start();
        drawingThread.setSceneComposer(sceneComposer);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        boolean retry = true;

        drawingThread.setRunning(false);

        while (retry) {
            try {
                drawingThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void setComposer(SceneModelComposer modelComposer) {
        sceneComposer = modelComposer;
        if (drawingThread != null) drawingThread.setRunning(false);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }
}
