/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.owner.bi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.owner.bi.DeviceListFragment.DeviceActionListener;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static com.example.owner.bi.WiFiDirectActivity.TAG;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

	//static InterstitialAd mInterstitialAd;
	
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
	static {
		System.loadLibrary("opencv_java3");

	}

    private static ProgressDialog mProgressDialog;

    public static String WiFiServerIp = "";
	public static String WiFiClientIp = "";
	static Boolean ClientCheck = false;
	public static String GroupOwnerAddress = "";
	static long ActualFilelength = 0;
	static int Percentage = 0;
	public static String FolderName = "WiFiDirectDemo";
	Uri temp;
    String selectedfilePath;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);



        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                      /*  Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image*//*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);*/
						onActivityResult1();
                    }
                });

        return mContentView;
    }


    

    public void onActivityResult1() {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
    	if(true){
    		// Uri uri = data.getData();
    	        /*
    	         * get actual file name and size of file, it will be send to socket and recieved at other device.
    	         * File size help in displaying progress dialog actual progress.
    	         */

    		 try {
				 if(WiFiDirectActivity.URI!=null)
				 {

					//  temp=Uri.parse(WiFiDirectActivity.URI);
					  selectedfilePath =WiFiDirectActivity.URI;

				 }
    			 /*selectedfilePath = CommonMethods.getPath(temp,
     					getActivity());
*/
     			//Log.e("Original Selected File Path-> ", selectedfilePath);
				 Log.d(TAG,"weofih");
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
    			String Extension = "";
    			if(selectedfilePath!=null){
    				File f = new File(selectedfilePath);
        			System.out.println("file name is   ::" + f.getName());
        			Long FileLength = f.length();
        			ActualFilelength = FileLength;

        			try {
        				Extension = f.getName();
        				Log.e("Name of File-> ", "" + Extension);
        			} catch (Exception e) {
        				// TODO: handle exception
        				e.printStackTrace();
        			}
    			}
    			else{
    				CommonMethods.e("", "path is null");
    				return;

    			}
    			
    			
    	        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
    	        statusText.setText("Sending: " + WiFiDirectActivity.URI);
    	        Log.d(TAG, "Intent----------- " + WiFiDirectActivity.URI);
    	        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
    	        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
    	        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, WiFiDirectActivity.URI);
    	        /*
    	         * Choose on which device file has to send weather its server or client
    	         */
    	        String Ip = SharedPreferencesHandler.getStringValues(
    					getActivity(), "WiFiClientIp");
    			String OwnerIp = SharedPreferencesHandler.getStringValues(
    					getActivity(), "GroupOwnerAddress");
    			if (OwnerIp != null && OwnerIp.length() > 0) {
    				CommonMethods.e("", "inside the check -- >");
    				// if(!info.groupOwnerAddress.getHostAddress().equals(LocalIp)){
    				String host=null;
    				int  sub_port =-1;
    				
    				String ServerBool = SharedPreferencesHandler.getStringValues(getActivity(), "ServerBoolean");
    				if (ServerBool!=null && !ServerBool.equals("") && ServerBool.equalsIgnoreCase("true")) {
    					
    					//-----------------------------
    					if (Ip != null && !Ip.equals("")) {
    						CommonMethods.e(
    								"in if condition",
    								"Sending data to " + Ip);
    						// Get Client Ip Address and send data
    						host=Ip;
    						sub_port=FileTransferService.PORT;
    							serviceIntent
    									.putExtra(
    											FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
    											Ip);
//    							serviceIntent
//    									.putExtra(
//    											WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    											WiFiFileTransferService.CLIENTPORT);
    					
    					}
    				

    				} else {
    					CommonMethods.e(
    							"in else condition",
    							"Sending data to " + OwnerIp);
    					
    					FileTransferService.PORT = 8889;
    					
    					host=OwnerIp;
    					sub_port=FileTransferService.PORT;
    					serviceIntent
    							.putExtra(
    									FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
    									OwnerIp);

//    					serviceIntent
//    							.putExtra(
//    									WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    									WiFiFileTransferService.PORT);
    					// anuj
    					

    				}
    				

    				serviceIntent.putExtra(FileTransferService.Extension, Extension);

    				serviceIntent.putExtra(FileTransferService.Filelength,
    						ActualFilelength + "");
    				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
    				if(host !=null && sub_port!=-1)
    				{
    					CommonMethods.e("Going to intiate service", "service intent for initiating transfer");
    					//showprogress("Sending...");
    					getActivity().startService(serviceIntent);
    				}
    				else {
    					CommonMethods.DisplayToast(getActivity(),
    							"Host Address not found, Please Re-Connect");
    					DismissProgressDialog();
    				}
    				
    			} else {
    				DismissProgressDialog();
    				CommonMethods.DisplayToast(getActivity(),
    						"Host Address not found, Please Re-Connect");
    			}
    	}
    	else{
    		CommonMethods.DisplayToast(getActivity(), "Cancelled Request");
    	}
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
//                info.groupOwnerAddress.getHostAddress());
        
//        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        if(info.groupOwnerAddress.getHostAddress()!=null)
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        else{
        	CommonMethods.DisplayToast(getActivity(), "Host Address not found");
        }
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        try {
			String GroupOwner = info.groupOwnerAddress.getHostAddress();
			if(GroupOwner!=null && !GroupOwner.equals("")) SharedPreferencesHandler.setStringValues(getActivity(),
					"GroupOwnerAddress", GroupOwner);
			mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
        if (info.groupFormed && info.isGroupOwner) {
        	/*
        	 * set shaerdprefrence which remember that device is server.
        	 */
        	SharedPreferencesHandler.setStringValues(getActivity(),
					"ServerBoolean", "true");
        	
            /*new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();*/
        	FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
					getActivity(), FileTransferService.PORT);
			if (FileServerobj != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					FileServerobj.executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { null });
					// FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Void);
				}
				else
					FileServerobj.execute();
			}
        }
        else  {
            // The other device acts as the client. In this case, we enable the
            // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
        	if (!ClientCheck) {
				firstConnectionMessage firstObj = new firstConnectionMessage(
						GroupOwnerAddress);
				if (firstObj != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						firstObj.executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR,
								new String[] { null });
					} else
						firstObj.execute();
				}
			}
        	
        	FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
					getActivity(), FileTransferService.PORT);
			if (FileServerobj != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					FileServerobj.executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { null });
				}
				else
					FileServerobj.execute();

			}

        }
        }
        catch(Exception e){
        	
        }

        
		
	 
    }


    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
        /*
         * Remove All the prefrences here
         */
        SharedPreferencesHandler.setStringValues(getActivity(),
    			"GroupOwnerAddress", "");
    	SharedPreferencesHandler.setStringValues(getActivity(),
    			"ServerBoolean", "");
    	SharedPreferencesHandler.setStringValues(getActivity(),
    			"WiFiClientIp", "");
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    static Handler handler;
    public static class FileServerAsyncTask extends AsyncTask<String, String, String> {

//        private TextView statusText;
        private Context mFilecontext;
		private String Extension, Key;
		private File EncryptedFile;
		private long ReceivedFileLength;
		private int PORT;
        /**
         * @param context
         //* @param statusText
         */
        public FileServerAsyncTask(Context context, int port) {
            this.mFilecontext = context;
//            this.statusText = (TextView) statusText;
            handler = new Handler();
			this.PORT = port;
//			myTask = new FileServerAsyncTask();
			if (mProgressDialog == null)
				mProgressDialog = new ProgressDialog(mFilecontext,
						ProgressDialog.THEME_HOLO_LIGHT);
        }
        

		@Override
        protected String doInBackground(String... params) {
            try {
				CommonMethods.e("File Async task port", "File Async task port-> " + PORT);
				// init handler for progressdialog
				ServerSocket serverSocket = new ServerSocket(PORT);
				
				Log.d(CommonMethods.Tag, "Server: Socket opened");
				Socket client = serverSocket.accept();
				//Log.d("Client's InetAddresssss  ", "" + client.getInetAddress());

				WiFiClientIp = client.getInetAddress().getHostAddress();
				
				ObjectInputStream ois = new ObjectInputStream(
						client.getInputStream());
				WiFiTransferModal obj = null;
				// obj = (WiFiTransferModal) ois.readObject();
				String InetAddress;
				try {
					obj = (WiFiTransferModal) ois.readObject();
					InetAddress = obj.getInetAddress();
					if (InetAddress != null
							&& InetAddress
									.equalsIgnoreCase(FileTransferService.inetaddress)) {
						CommonMethods.e("File Async Group Client Ip", "port-> "
								+ WiFiClientIp);
						SharedPreferencesHandler.setStringValues(mFilecontext,
								"WiFiClientIp", WiFiClientIp);
						CommonMethods
								.e("File Async Group Client Ip from SHAREDPrefrence",
										"port-> "
												+ SharedPreferencesHandler
														.getStringValues(
																mFilecontext,
																"WiFiClientIp"));
						//set boolean true which identifiy that this device will act as server.
						SharedPreferencesHandler.setStringValues(mFilecontext,
								"ServerBoolean", "true");
						ois.close(); // close the ObjectOutputStream object
										// after saving
						serverSocket.close();

						return "Demo";
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final Runnable r = new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						/*mProgressDialog.setMessage("Receiving...");
						mProgressDialog.setIndeterminate(false);
						mProgressDialog.setMax(100);
						mProgressDialog.setProgress(0);
						mProgressDialog.setProgressNumberFormat(null);
//						mProgressDialog.setCancelable(false);
						mProgressDialog
								.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						mProgressDialog.show();*/
					}
				};
				handler.post(r);
				Log.e("FileName: ",obj.getFileName());
				Log.d("filelength",""+obj.getFileLength());

				final File fr = new File(
						Environment.getExternalStorageDirectory() + "/"
								+ FolderName + "/"
								+ obj.getFileName());

				File dirs = new File(fr.getParent());
				if (!dirs.exists())
					dirs.mkdirs();
				fr.createNewFile();

//				final File fr = new File(
//						Environment.getExternalStorageDirectory() + "/"
//								+ "whatislife.jpg");
//
//
//				fr.createNewFile();
				
				/*
				 * Recieve file length and copy after it
				 */
				this.ReceivedFileLength = obj.getFileLength();
				
				InputStream inputstream = client.getInputStream();
				OutputStream outputStream= new FileOutputStream(fr);
				byte buf[] = new byte[512];
				int len=0;


				try{
					while((len=inputstream.read(buf))!=-1) {
						outputStream.write(buf,0,len);
					}
				}catch (Exception e)
				{
					Log.d(TAG,"Write failed");
				}

				inputstream.close();
				outputStream.close();
	//			copyRecievedFile(inputstream,outputStream,ReceivedFileLength);
				ois.close(); // close the ObjectOutputStream object after saving
								// file to storage.
				serverSocket.close();

				File folder = new File(Environment.getExternalStorageDirectory() + "/" + FolderName);
				File[] listoffiles = folder.listFiles();
				int i=0;
				while(i<listoffiles.length) {
					sift(Environment.getExternalStorageDirectory() + "/" + FolderName + "/" + obj.getFileName(),Environment.getExternalStorageDirectory() + "/" + FolderName + "/" + listoffiles[i].getName() );
					i++;
				}

				/*
				 * Set file related data and decrypt file in postExecute.
				 */
				this.Extension = obj.getFileName();
				this.EncryptedFile = fr;

				return fr.getAbsolutePath();
			} catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }

        }

		public void sift(String Reference,String Source) {
			String mBaseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap refImage = BitmapFactory.decodeFile(Reference, options);

			Bitmap SourceImage = BitmapFactory.decodeFile(Source, options);


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
			double resp4 = Imgproc.compareHist(histRef, histSource, 3);



			if(resp1>0.7) {

				File fr = new File(Source);
				if(!Source.equals(Reference)){
					fr.delete();
				}
				Log.d(TAG,"  resp1:  "+(long)resp1 );


			}


			double resp2 = Imgproc.compareHist(histRef, histSource, 1);
			double resp3 = Imgproc.compareHist(histRef, histSource, 2);

			Log.d(TAG,"  resp1:  "+(long)resp1 );
		}

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
            	if(!result.equalsIgnoreCase("Demo")){

            		Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                    mFilecontext.startActivity(intent);
            	}
            	else{
            		/*
					 * To initiate socket again we are intiating async task
					 * in this condition.
					 */
					  FileServerAsyncTask FileServerobj = new
					  FileServerAsyncTask(mFilecontext,FileTransferService.PORT); 
					  if(FileServerobj != null) { 
						  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					  FileServerobj.executeOnExecutor (AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null }); 
					  
					  }
					  else FileServerobj.execute();
					  
					  }
            	}
//                statusText.setText("File copied - " + result);
                
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
        	if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(mFilecontext);
			}
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
    	long total = 0;
		long test = 0;
		byte buf[] = new byte[FileTransferService.ByteSize];
		int len;
		int fl=0;
		try {
			if (inputStream.read(buf)==-1)
			{
				Log.d(TAG,"input stream is empty");
			}
		}
		catch (Exception e)
		{

		}
        try {

            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
				total += len;
				Percentage = (int) ((total * 100) / ActualFilelength);
				mProgressDialog.setProgress(Percentage);
//                try {
//					total += len;
//					if (ActualFilelength > 0) {
//						Percentage = (int) ((total * 100) / ActualFilelength);
//						if(Percentage>20&&fl==0)
//						{
//							Log.d(TAG,"jjyfj");
//							fl=1;
//						}
//
//					}
//					// Log.e("Percentage--->>> ", Percentage+"   FileLength" +
//					// EncryptedFilelength+"    len" + len+"");
//					mProgressDialog.setProgress(Percentage);
//				} catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//					Percentage = 0;
//					ActualFilelength = 0;
//				}
            }
            if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}

            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }

    public static boolean copyRecievedFile(InputStream inputStream,
			OutputStream out, Long length) {

		byte buf[] = new byte[512];
		byte Decryptedbuf[] = new byte[FileTransferService.ByteSize];
		String Decrypted;
		int len;
		long total = 0;
		int progresspercentage = 0;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				try {

					out.write(buf,0,len);
					total+=len;
					progresspercentage = (int) ((total * 100) / length);
					mProgressDialog.setProgress(progresspercentage);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				try {
//					total += len;
//					if (length > 0) {
//						progresspercentage = (int) ((total * 100) / length);
//					}
//					mProgressDialog.setProgress(progresspercentage);
//				} catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//					if (mProgressDialog != null) {
//						if (mProgressDialog.isShowing()) {
//							mProgressDialog.dismiss();
//						}
//					}
//				}
			}
			out.close();
			inputStream.close();
			// dismiss progress after sending
			Log.d(TAG,"aajefvg");
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}

		} catch (IOException e) {
			Log.d(TAG, e.toString());
			return false;
		}
		return true;
	}
    
    public void showprogress(final String task) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity(),
					ProgressDialog.THEME_HOLO_LIGHT);
		}
		Handler handle = new Handler();
		final Runnable send = new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				mProgressDialog.setMessage(task);
				// mProgressDialog.setProgressNumberFormat(null);
				// mProgressDialog.setProgressPercentFormat(null);
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
//				mProgressDialog.setCancelable(false);
				mProgressDialog.setProgressNumberFormat(null);
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.show();
			}
		};
		handle.post(send);
	}
    
    public static void DismissProgressDialog() {
		try {
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
    
    
    /*
     * Async class that has to be called when connection establish first time. Its main motive is to send blank message
     * to server so that server knows the IP address of client to send files Bi-Directional.
     */
    class firstConnectionMessage extends AsyncTask<String, Void, String> {

		String GroupOwnerAddress = "";

		public firstConnectionMessage(String owner) {
			// TODO Auto-generated constructor stub
			this.GroupOwnerAddress = owner;

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			CommonMethods.e("On first Connect", "On first Connect");
			
			Intent serviceIntent = new Intent(getActivity(),
					WiFiClientIPTransferService.class);

			serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

			if (info.groupOwnerAddress.getHostAddress() != null) {
				serviceIntent.putExtra(
						FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
						info.groupOwnerAddress.getHostAddress());

				serviceIntent.putExtra(
						FileTransferService.EXTRAS_GROUP_OWNER_PORT,
						FileTransferService.PORT);
				serviceIntent.putExtra(FileTransferService.inetaddress,
						FileTransferService.inetaddress);

			}

			getActivity().startService(serviceIntent);

			return "success";
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result!=null){
				if(result.equalsIgnoreCase("success")){
					CommonMethods.e("On first Connect",
							"On first Connect sent to asynctask");
					ClientCheck = true;
				}
			}
			
		}

	}
}
