package com.example.owner.bi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.os.Environment;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.cvtColor;


public class DuplicateRemover extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    static {
        System.loadLibrary("opencv_java3");

    }
    private ImageView imageView;
    private Bitmap inputImage; // make bitmap from image resource


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        sift();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void sift() {
        String mBaseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap refImage = BitmapFactory.decodeFile(mBaseDir+ "/" + "ref.jpg", options);

        Bitmap SourceImage = BitmapFactory.decodeFile(mBaseDir + "/" + "source.jpg", options);


        Mat hsvRef = new Mat();
        Mat hsvSource = new Mat();

        Mat srcRef = new Mat(refImage.getHeight(), refImage.getWidth(), CvType.CV_8U, new Scalar(4));
        Utils.bitmapToMat(refImage, srcRef);


        Mat srcSource = new Mat(SourceImage.getHeight(), SourceImage.getWidth(), CvType.CV_8U, new Scalar(4));
        Utils.bitmapToMat(SourceImage, srcSource);

        /// Convert to HSV
        Imgproc.cvtColor(srcRef, hsvRef, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(srcSource, hsvSource, Imgproc.COLOR_BGR2HSV);

        /// Using 50 bins for hue and 60 for saturation
        int hBins = 50;
        int sBins = 60;
        MatOfInt histSize = new MatOfInt(hBins, sBins);

        // hue varies from 0 to 179, saturation from 0 to 255
        MatOfFloat ranges = new MatOfFloat(0f, 180f, 0f, 256f);

        // we compute the histogram from the 0-th and 1-st channels
        MatOfInt channels = new MatOfInt(0, 1);


        Mat histRef = new Mat();
        Mat histSource = new Mat();

        ArrayList<Mat> histImages = new ArrayList<Mat>();
        histImages.add(hsvRef);
        Imgproc.calcHist(histImages, channels, new Mat(), histRef, histSize, ranges, false);
        Core.normalize(histRef, histRef, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        histImages = new ArrayList<Mat>();
        histImages.add(hsvSource);
        Imgproc.calcHist(histImages, channels, new Mat(), histSource, histSize, ranges, false);
        Core.normalize(histSource, histSource, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        double resp1 = Imgproc.compareHist(histRef, histSource, 0);



        double resp2 = Imgproc.compareHist(histRef, histSource, 1);
        double resp3 = Imgproc.compareHist(histRef, histSource, 2);
        double resp4 = Imgproc.compareHist(histRef, histSource, 3);

        Log.d(TAG,"  resp1:  "+(long)resp1 );
    }
}
