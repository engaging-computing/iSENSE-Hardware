package edu.uml.cs.isense.riverwalk.dialogs;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    @SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
    	
    	// Set android picture size (continuous pictures only)
    	setBestPictureSize();
    	
    	if (holder == null){
    		Log.d("surfaceCreated", "holder is null");
    	}
    	try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException exception) {
            Log.e("surfaceCreated in CameraPreview", "IOException caused by setPreviewDisplay() or startPreview()" + exception.getMessage());
        }
        

    }
    
    /**
     * Try to get the best picture size with width or height 
     * no greater than 1024.
     */
    private void setBestPictureSize() {
    	
    	final int MAX = 1024;
    	int mHeight = 0, mWidth = 0;
    	
    	Parameters parameters = mCamera.getParameters();

    	for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
    		int h = size.height, w = size.width;
    		
    		// If we don't have a height/width yet, use this one by default and continue
    		if (mHeight == 0 || mWidth == 0) {
    			mHeight = h;
    			mWidth  = w;
    			continue;
    		}
    		
    		// If this height/width is better but still under MAX, use it
    		if ((h <= MAX && w <= MAX) && (h >= mHeight || w >= mWidth)) {
    			mHeight = h;
    			mWidth  = w;
    			continue;
    		}
    		
    		// To make this loop sane, once we get past 2*MAX, stop the loop just
    		// in case we have many, many greater sizes that we don't care about.
    		if (h >= (2*MAX) || w >= (2*MAX))
    			break;
    		
    	}
    	
    	System.out.println("Width: " + mWidth + ", Height: " + mHeight);
    	
    	// Finally, set our camera to use the best supported size we found
    	parameters.setPictureSize(mWidth, mHeight);
    	mCamera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	mCamera.stopPreview();
        mCamera.setPreviewCallback(null); 
		mCamera.release();
		mCamera = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

    	
    	
        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }
//        Log.d("CameraPreview", "Camera is:" + mCamera.toString());
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
//        int height = sizeList.get(0).height;
//        int width = sizeList.get(0).width;
//        parameters.setPreviewSize(width, height);
//        mCamera.setParameters(parameters);
//        holder.setFixedSize(width, height);
        // set preview size and make any resize, rotate or
        // reformatting changes here
       
        
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
        }

    	
    }
}