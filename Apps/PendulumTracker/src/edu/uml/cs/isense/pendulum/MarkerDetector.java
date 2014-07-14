package edu.uml.cs.isense.pendulum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class MarkerDetector {
	
	private static final String  TAG = "PendulumTracker::MarkerDetector";
	
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    Mat mResult = new Mat();
    
    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    // this processes one frame and return the location of the detected marker (e.g. pendulum) 
    public Point processGrey(Mat mGrey)
    {
		double downScale = 1.0;
		final Size imgSize = new Size(mGrey.cols()*downScale, mGrey.rows()*downScale);
	
		/*Mat */ ///result = new Mat();
		mResult = mGrey.clone(); // use when doing image processing on original grabbed image
		///Imgproc.resize(mResult, mResult, imgSize);
		Mat mask = new Mat(mResult.size(), CvType.CV_8U);

		Core.inRange(mResult, new Scalar(0), new Scalar(64), mResult);
		
		// Canny edge detection
		//Imgproc.Canny(result, result, 80, 100);
		Imgproc.Canny(mResult, mResult, 200, 255);
		
		// AND results
		//Core.bitwise_and(result, mask, result);
					
		// ========= contours --------------
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Mat resultCopy = mResult.clone();;
        
        // Detect contours
        Imgproc.findContours(resultCopy, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.e(TAG, "Contours count: " + contours.size());

        // Find max contour area
        List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
        double maxArea = 0;
        double mMinContourArea = 0.2;
        double mMaxContourArea = 0.9;
        Iterator<MatOfPoint> each = contours.iterator();
        // look for contour with maximum area
        while (each.hasNext())
        {
        	MatOfPoint wrapper = each.next();
        	double area = Imgproc.contourArea(wrapper);
        	if (area > maxArea)
        		maxArea = area;
        }
        
        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext())
        {
        	MatOfPoint contour = each.next();
       // 	if (Imgproc.contourArea(contour) > mMinContourArea*maxArea && Imgproc.contourArea(contour) < mMaxContourArea*maxArea)
        	{
        		Core.multiply(contour, new Scalar(5,5), contour);
        		mContours.add(contour);
        	}
        }
           
        // ------- end contours ----------------
		
        // imgSize = (640x320) * 0.4 * 3.125 = (800x400) <--- larger than grabbed image.
		//final double upScale = 3.125;
		final double upScale = 1;
		//final double upScale = 6;
		final Size greyImgSize = new Size(mGrey.cols()*upScale*downScale, mGrey.rows()*upScale*downScale); // downscale upscale 

		Point center = new Point(0,0);
		center = getBlobCentroid(mResult);
		
		// scale point and add to dataset (only if processing on downscaled image)
		double xScale = upScale*center.x;
		double yScale = upScale*center.y;
		
		if(xScale != 0 || yScale != 0)
		{
			// TODO: current order: col, -row (x and y backwards!)
			//this.addDataPoint(yScale, -xScale);	
			return new Point(yScale, -xScale);							
		}
		else
			return new Point(0,0);

    }
    
    Mat getLastDebugImg()
    {
    	// currently edge image
    	return mResult;
    }
    
    
    
    public void process(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);
            }
        }
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
    
	public Point getBlobCentroid(Mat mask)
	{
		// Assume binary blob mask
		//Point center = new Point(0,0);
		Mat centers = new Mat();
		int cx = 0, cy = 0;
		int numPts = 0;
		int numRows = mask.rows();
		int numCols = mask.cols();

		double[] val = new double[1];
		
		// Initialize an array of x,y Scalars to represent a grid
		for(int i=0; i<numRows; i = i + 4) // y
		{	
			for(int j=0; j<numCols; j = j + 4) // x
			{
				
				val = mask.get(i,j);
				//Log.i(TAG, "i/j (row/col) = " + i + " " + j + " " + val);
				
				if(val[0] > 0)
				{ 
					cx += j; // cols
					cy += i; // row
					numPts++;
				}		
			}
		}
		
		if(numPts > 0)
		{
			cx /= numPts;
			cy /= numPts;
		}
		
		// get centroid via k-mmeans clustering
		//Core.kmeans(data, K, bestLabels, criteria, attempts, flags, centers)
		//float radius = 0;
		//imgproc.minEnclosingCircle(mask., center, radius);
		
		return new Point(cy, cx);
	}

	
}
