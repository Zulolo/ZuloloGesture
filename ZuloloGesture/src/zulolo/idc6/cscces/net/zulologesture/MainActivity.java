package zulolo.idc6.cscces.net.zulologesture;

//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;  
import org.achartengine.GraphicalView;  
import org.achartengine.chart.PointStyle;  
//import org.achartengine.chart.BarChart.Type;  
//import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
//import org.achartengine.renderer.SimpleSeriesRenderer;  
import org.achartengine.renderer.XYMultipleSeriesRenderer;  
import org.achartengine.renderer.XYSeriesRenderer; 
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import android.annotation.SuppressLint;
import android.app.Activity;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {
	
	private static final String ZULOLO_ORIENTATION_RECORD_TXT = "ZuloloOrientationRecord.txt";
	//private static final String ZULOLO_ORIENTATION_RECORD_DIR = "ZuloloOrientationRecord";
	private static final int CHART_SERIES_MAX_LENGTH = 360;
	private static final String TITLE_GESTURE_Z = "Gesture Z";
	private static final String TITLE_GESTURE_Y = "Gesture Y";
	private static final String TITLE_GESTURE_X = "Gesture X";
//	private static final String SAVE_STATE_GESTURE_Z_RENDERER = "gestureZRenderer";
//	private static final String SAVE_STATE_GESTURE_Y_RENDERER = "gestureYRenderer";
//	private static final String SAVE_STATE_GESTURE_X_RENDERER = "gestureXRenderer";
//	private static final String SAVE_STATE_GESTURE_Z_SERIES = "gestureZ_XYSeriesSeries";
//	private static final String SAVE_STATE_GESTURE_Y_SERIES = "gestureY_XYSeriesSeries";
//	private static final String SAVE_STATE_GESTURE_X_SERIES = "gestureX_XYSeriesSeries";
//	private static final String SAVE_STATE_GESTURE_RENDERER = "gestureRenderer";
//	private static final String SAVE_STATE_GESTURE_DATASET = "gestureDataset";
//	private static final String SAVE_STATE_I_GESTURE_TIME = "iGestureTime";
	private static final int REFRESH_CHART_START_DELAY = 1000;
	private static final int REFRESH_CHART_INTERVAL = 100;
	private static final int MSG_TIMER_CHART_REFRESH = 1;
	
	private XYMultipleSeriesDataset gestureDataset = new XYMultipleSeriesDataset();  
	private XYMultipleSeriesRenderer gestureRenderer = new XYMultipleSeriesRenderer(); 
	
	private int iGestureTime;
	private XYSeries gestureX_XYSeries = new XYSeries(TITLE_GESTURE_X);
	private XYSeries gestureY_XYSeries = new XYSeries(TITLE_GESTURE_Y);
	private XYSeries gestureZ_XYSeries = new XYSeries(TITLE_GESTURE_Z);
	
	private XYSeriesRenderer gestureXSeriesRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer gestureYSeriesRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer gestureZSeriesRenderer = new XYSeriesRenderer();
	private GraphicalView gestureChartView;
	
	private FileOutputStream myFileOutputStream;
	private String sSaveRecord = "Time\tOrientation X\tOrientation Y\tOrientation Z\t" +
			"Accelerometer X\tAccelerometer Y\tAccelerometer Z\t" +
			"MagneticField X\tMagneticField Y\tMagneticField Z\t" +
			"RotationVector X\tRotationVector Y\tRotationVector Z\t" +
			"Gravity X\tGravity Y\tGravity Z\t" +
			"Gyroscope X\tGyroscope Y\tGyroscope Z\n"; 
	
	SensorManager mySensorManager;
	TextView tvOrientation;
	TextView tvAcceRawData;
	TextView tvMagRawData;
    float[] fAccelerometerValues = new float[3];  
    float[] fMagneticFieldValues = new float[3];
    float[] fRotationMatrix = new float[9];  
    float[] fOrientationValues = new float[3];
    float[] fRotationVectorValues = new float[3];
    float[] fGravityValues = new float[3];
    float[] fGyroscopeValues = new float[3];
   
    Timer refreshChartTimer = new Timer(); 
    
	@SuppressLint("HandlerLeak")
	Handler myhandler = new Handler(){ 
		public void handleMessage(Message msg) {	
			double dShowTime;
			switch (msg.what) { 
			case MSG_TIMER_CHART_REFRESH: 
				// Refresh all the chart
				iGestureTime++;
				dShowTime = ((double)iGestureTime * REFRESH_CHART_INTERVAL) / 1000;
				gestureX_XYSeries.add(dShowTime, fOrientationValues[0]);
				gestureY_XYSeries.add(dShowTime, fOrientationValues[1]);
				gestureZ_XYSeries.add(dShowTime, fOrientationValues[2]);

				if (myFileOutputStream!= null)
				{
					sSaveRecord = sSaveRecord + dShowTime + "\t" + fOrientationValues[0] + 
							"\t" + fOrientationValues[1] + 
							"\t" + fOrientationValues[2] + 
							"\t" + fAccelerometerValues[0] + 
							"\t" + fAccelerometerValues[1] + 
							"\t" + fAccelerometerValues[2] + 
							"\t" + fMagneticFieldValues[0] + 
							"\t" + fMagneticFieldValues[1] + 
							"\t" + fMagneticFieldValues[2] + 
							"\t" + fRotationVectorValues[0] + 
							"\t" + fRotationVectorValues[1] + 
							"\t" + fRotationVectorValues[2] +
							"\t" + fGravityValues[0] + 
							"\t" + fGravityValues[1] + 
							"\t" + fGravityValues[2] +
							"\t" + fGyroscopeValues[0] + 
							"\t" + fGyroscopeValues[1] + 
							"\t" + fGyroscopeValues[2] +	
							"\n";
					if (iGestureTime%10 == 0){
						try {
							myFileOutputStream.write(sSaveRecord.getBytes());
							sSaveRecord = "";
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}

				}
				while (gestureX_XYSeries.getItemCount() > CHART_SERIES_MAX_LENGTH)
				{
					gestureX_XYSeries.remove(0);
				}
				while (gestureY_XYSeries.getItemCount() > CHART_SERIES_MAX_LENGTH)
				{
					gestureY_XYSeries.remove(0);
				}
				while (gestureZ_XYSeries.getItemCount() > CHART_SERIES_MAX_LENGTH)
				{
					gestureZ_XYSeries.remove(0);
				}
				if (gestureChartView != null)
				{
					gestureRenderer.setXAxisMin(gestureX_XYSeries.getMinX());
					gestureRenderer.setXAxisMax(gestureX_XYSeries.getMaxX());
					gestureChartView.repaint();
				}
			//setTitle("hear me?"); 
			break; 
			} 
		super.handleMessage(msg); 
		} 
	}; 
	
	TimerTask refreshChartTask = new TimerTask(){ 
		public void run() { 
		Message myMessage = new Message(); 
		myMessage.what = MSG_TIMER_CHART_REFRESH; 
		myhandler.sendMessage(myMessage); 
		} 
	}; 
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		GLSurfaceView myGLView = new GLSurfaceView(this);
//		myGLView.setRenderer(new OpenGLRenderer());
//		setContentView(myGLView);
		setContentView(R.layout.activity_main);
		tvOrientation = (TextView)findViewById(R.id.textViewOrientationData);
		tvAcceRawData = (TextView)findViewById(R.id.textViewAcceRawData);
		tvMagRawData = (TextView)findViewById(R.id.textViewMagRawData);
		
		// Chart
		iGestureTime = 0;
		gestureDataset.addSeries(gestureX_XYSeries);
		gestureDataset.addSeries(gestureY_XYSeries);
		gestureDataset.addSeries(gestureZ_XYSeries);

		FillOutsideLine myFillOutsideLine = new FillOutsideLine(FillOutsideLine.Type.NONE);
		gestureXSeriesRenderer.setColor(Color.RED);	
		gestureXSeriesRenderer.addFillOutsideLine(myFillOutsideLine);
		gestureXSeriesRenderer.setPointStyle(PointStyle.POINT); 
		gestureXSeriesRenderer.setLineWidth(3.0f); 

		gestureYSeriesRenderer.setColor(Color.GREEN);	
		gestureYSeriesRenderer.addFillOutsideLine(myFillOutsideLine);
		gestureYSeriesRenderer.setPointStyle(PointStyle.POINT); 
		gestureYSeriesRenderer.setLineWidth(3.0f); 
		
		gestureZSeriesRenderer.setColor(Color.BLUE);	
		gestureZSeriesRenderer.addFillOutsideLine(myFillOutsideLine);
		gestureZSeriesRenderer.setPointStyle(PointStyle.POINT); 
		gestureZSeriesRenderer.setLineWidth(3.0f); 
		
		gestureRenderer.addSeriesRenderer(gestureXSeriesRenderer);
		gestureRenderer.addSeriesRenderer(gestureYSeriesRenderer);
		gestureRenderer.addSeriesRenderer(gestureZSeriesRenderer);
		gestureRenderer.setYAxisMin(-5d); 
		gestureRenderer.setYAxisMax(5d); 
		gestureRenderer.setXAxisMin(0d);
		gestureRenderer.setXAxisMax(360d);
		gestureRenderer.setShowGrid(true); 
		gestureRenderer.setXLabels(24); 
		gestureRenderer.setYLabels(24);
		gestureRenderer.setYLabelsAlign(Align.RIGHT);
		gestureRenderer.setChartTitle("Orientation");
		
		if(isExternalStorageWritable()){
			File myFile = new File(Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_PICTURES), ZULOLO_ORIENTATION_RECORD_TXT);
//				if(myFile.exists())
//				{
//					myFile.delete();	
//				}
//		    	myBufWriter = new BufferedWriter(new FileWriter(ZULOLO_ORIENTATION_RECORD_TXT));
			try {
				myFileOutputStream = new FileOutputStream(myFile);
			} catch (FileNotFoundException e) {
				myFileOutputStream = null;
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast myToast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
				myToast.show();	
			}
			Toast myToast = Toast.makeText(this, "File generated", Toast.LENGTH_LONG);
			myToast.show();	
		}else{
			Toast myToast = Toast.makeText(this, "External Storage Unreachable", Toast.LENGTH_LONG);
			myToast.show();	
		}
		// Get System Service of sensors
		mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		refreshChartTimer.schedule(refreshChartTask, REFRESH_CHART_START_DELAY, REFRESH_CHART_INTERVAL); 
	}

	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_GAME);
//		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_ALL),
//				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
				SensorManager.SENSOR_DELAY_GAME);	
		if (gestureChartView == null){
			LinearLayout myLayout = (LinearLayout)findViewById(R.id.gestureView);
			gestureChartView = ChartFactory.getLineChartView(this, gestureDataset, gestureRenderer);
			myLayout.addView(gestureChartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} 
		else {
			gestureChartView.setBackgroundResource(R.id.gestureView);
			gestureChartView.repaint();
		}
	}

	@Override
	protected void onStop()
	{
		mySensorManager.unregisterListener(this);
//		if (myBufWriter!= null)
//		{
//			try {
//				myBufWriter.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		StringBuilder mySB = new StringBuilder();
		int iSensorType = arg0.sensor.getType();
		switch(iSensorType)
		{
		case Sensor.TYPE_ACCELEROMETER:
			fAccelerometerValues = arg0.values;
			mySB.append("X:");
			mySB.append(fAccelerometerValues[0] + "\n");
			mySB.append("Y:");
			mySB.append(fAccelerometerValues[1] + "\n");		
			mySB.append("Z:");
			mySB.append(fAccelerometerValues[2]);
			tvAcceRawData.setText(mySB.toString());
			calculateOrientation(); 
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			fMagneticFieldValues = arg0.values;
			mySB.append("X:");
			mySB.append(fMagneticFieldValues[0] + "\n");
			mySB.append("Y:");
			mySB.append(fMagneticFieldValues[1] + "\n");		
			mySB.append("Z:");
			mySB.append(fMagneticFieldValues[2]);
			tvMagRawData.setText(mySB.toString());
			//calculateOrientation(); 
			break;
		case Sensor.TYPE_ROTATION_VECTOR:
			fRotationVectorValues = arg0.values;
			break;
		case Sensor.TYPE_GRAVITY:
			fGravityValues = arg0.values;
			break;
		case Sensor.TYPE_GYROSCOPE:
			fGyroscopeValues = arg0.values;
			break;
			default:
				break;
			   
		}
	}
	
	private  void calculateOrientation() {  
		StringBuilder mySB = new StringBuilder();
        float[] fOrientationResults = new float[3];  
        float[] tempRotationMatrix = new float[9];  
        if (SensorManager.getRotationMatrix(tempRotationMatrix, null, fAccelerometerValues, fMagneticFieldValues))
        {
            SensorManager.getOrientation(tempRotationMatrix, fOrientationResults);  
            fOrientationValues[0] = fOrientationResults[0];	//(float)Math.toDegrees(fOrientationResults[0]);
            fOrientationValues[1] = fOrientationResults[1];	//(float)Math.toDegrees(fOrientationResults[1]);
            fOrientationValues[2] = fOrientationResults[2];	//(float)Math.toDegrees(fOrientationResults[2]);
    		mySB.append("X:");
    		mySB.append(fOrientationValues[0] + "\n");
    		mySB.append("Y:");
    		mySB.append(fOrientationValues[1] + "\n");		
    		mySB.append("Z:");
    		mySB.append(fOrientationValues[2]);	
    		tvOrientation.setText(mySB.toString());
        }

	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
//		gestureDataset = (XYMultipleSeriesDataset)savedState.getSerializable(SAVE_STATE_GESTURE_DATASET);
//		gestureRenderer = (XYMultipleSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_RENDERER);
//		gestureX_XYSeries = (XYSeries)savedState.getSerializable(SAVE_STATE_GESTURE_X_SERIES);
//		gestureY_XYSeries = (XYSeries)savedState.getSerializable(SAVE_STATE_GESTURE_Y_SERIES);
//		gestureZ_XYSeries = (XYSeries)savedState.getSerializable(SAVE_STATE_GESTURE_Z_SERIES);
//		gestureXSeriesRenderer = (XYSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_X_RENDERER);
//		gestureYSeriesRenderer = (XYSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_Y_RENDERER);
//		gestureZSeriesRenderer = (XYSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_Z_RENDERER);
//		iGestureTime = savedState.getInt(SAVE_STATE_I_GESTURE_TIME);
//		gestureDataset.clear();
//		gestureDataset.addSeries(gestureX_XYSeries);
//		gestureDataset.addSeries(gestureY_XYSeries);
//		gestureDataset.addSeries(gestureZ_XYSeries);
//		if (gestureChartView != null)
//		{
//			gestureRenderer.setXAxisMin(gestureX_XYSeries.getMinX());
//			gestureRenderer.setXAxisMax(gestureX_XYSeries.getMaxX());
//			gestureChartView.repaint();
//		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
//		outState.putSerializable(SAVE_STATE_GESTURE_DATASET, gestureDataset);
//		outState.putSerializable(SAVE_STATE_GESTURE_RENDERER, gestureRenderer);
//		outState.putSerializable(SAVE_STATE_GESTURE_X_SERIES, gestureX_XYSeries);
//		outState.putSerializable(SAVE_STATE_GESTURE_Y_SERIES, gestureY_XYSeries);
//		outState.putSerializable(SAVE_STATE_GESTURE_Z_SERIES, gestureZ_XYSeries);
//		outState.putSerializable(SAVE_STATE_GESTURE_X_RENDERER, gestureXSeriesRenderer);
//		outState.putSerializable(SAVE_STATE_GESTURE_Y_RENDERER, gestureYSeriesRenderer);
//		outState.putSerializable(SAVE_STATE_GESTURE_Z_RENDERER, gestureZSeriesRenderer);
//		outState.putInt(SAVE_STATE_I_GESTURE_TIME, iGestureTime);
		super.onSaveInstanceState(outState);
	}
}
