package zulolo.idc6.cscces.net.zulologesture;

import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;  
import org.achartengine.GraphicalView;  
import org.achartengine.chart.PointStyle;  
import org.achartengine.chart.BarChart.Type;  
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;  
import org.achartengine.renderer.XYMultipleSeriesRenderer;  
import org.achartengine.renderer.XYSeriesRenderer; 
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
	
	private static final String TITLE_GESTURE_Z = "Gesture Z";
	private static final String TITLE_GESTURE_Y = "Gesture Y";
	private static final String TITLE_GESTURE_X = "Gesture X";
	private static final String SAVE_STATE_GESTURE_Z_RENDERER = "gestureZRenderer";
	private static final String SAVE_STATE_GESTURE_Y_RENDERER = "gestureYRenderer";
	private static final String SAVE_STATE_GESTURE_X_RENDERER = "gestureXRenderer";
	private static final String SAVE_STATE_GESTURE_Z_SERIES = "gestureZSeries";
	private static final String SAVE_STATE_GESTURE_Y_SERIES = "gestureYSeries";
	private static final String SAVE_STATE_GESTURE_X_SERIES = "gestureXSeries";
	private static final String SAVE_STATE_GESTURE_RENDERER = "gestureRenderer";
	private static final String SAVE_STATE_GESTURE_DATASET = "gestureDataset";
	private static final int REFRESH_CHART_START_DELAY = 1000;
	private static final int REFRESH_CHART_INTERVAL = 200;
	private static final int MSG_TIMER_CHART_REFRESH = 1;
	
	private XYMultipleSeriesDataset gestureDataset = new XYMultipleSeriesDataset();  
	private XYMultipleSeriesRenderer gestureRenderer = new XYMultipleSeriesRenderer(); 
	  
	private XYSeries gestureXSeries = new XYSeries(TITLE_GESTURE_X);
	private XYSeries gestureYSeries = new XYSeries(TITLE_GESTURE_Y);
	private XYSeries gestureZSeries = new XYSeries(TITLE_GESTURE_Z);
	
	private XYSeriesRenderer gestureXSeriesRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer gestureYSeriesRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer gestureZSeriesRenderer = new XYSeriesRenderer();
	private GraphicalView gestureChartView;
	
	SensorManager mySensorManager;
	TextView tvOrientation;
	TextView tvAcceRawData;
	TextView tvMagRawData;
    float[] fAccelerometerValues = new float[3];  
    float[] fMagneticFieldValues = new float[3];
    float[] fRotationMatrix = new float[9];  
    
   
    Timer refreshChartTimer = new Timer(); 
    
	@SuppressLint("HandlerLeak")
	Handler myhandler = new Handler(){ 
		public void handleMessage(Message msg) { 
			switch (msg.what) { 
			case MSG_TIMER_CHART_REFRESH: 
				// Refresh all the chart
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
		gestureDataset.addSeries(gestureXSeries);
		gestureDataset.addSeries(gestureYSeries);
		gestureDataset.addSeries(gestureZSeries);

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
		gestureRenderer.setYAxisMin(0d); 
		gestureRenderer.setYAxisMax(120d); 
		gestureRenderer.setXAxisMax(24d);
		gestureRenderer.setShowGrid(true); 
		gestureRenderer.setXLabels(24); 
		gestureRenderer.setChartTitle("Orientation");
		
		// Get System Service of sensors
		mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		refreshChartTimer.schedule(refreshChartTask, REFRESH_CHART_START_DELAY, REFRESH_CHART_INTERVAL); 
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
				SensorManager.SENSOR_DELAY_GAME);	
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
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
			break;
			default:
				break;
		}
	    calculateOrientation();  
	}
	
	private  void calculateOrientation() {  
		StringBuilder mySB = new StringBuilder();
        float[] fOrientationResults = new float[3];  
        float[] tempRotationMatrix = new float[9];  
        if (SensorManager.getRotationMatrix(tempRotationMatrix, null, fAccelerometerValues, fMagneticFieldValues))
        {
            SensorManager.getOrientation(tempRotationMatrix, fOrientationResults);  
    		
    		mySB.append("X:");
    		mySB.append((float)Math.toDegrees(fOrientationResults[0]) + "\n");
    		mySB.append("Y:");
    		mySB.append((float)Math.toDegrees(fOrientationResults[1]) + "\n");		
    		mySB.append("Z:");
    		mySB.append((float)Math.toDegrees(fOrientationResults[2]));	
    		tvOrientation.setText(mySB.toString());
        }

	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		gestureDataset = (XYMultipleSeriesDataset)savedState.getSerializable(SAVE_STATE_GESTURE_DATASET);
		gestureRenderer = (XYMultipleSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_RENDERER);
		gestureXSeries = (XYSeries)savedState.getSerializable(SAVE_STATE_GESTURE_X_SERIES);
		gestureYSeries = (XYSeries)savedState.getSerializable(SAVE_STATE_GESTURE_Y_SERIES);
		gestureZSeries = (XYSeries)savedState.getSerializable(SAVE_STATE_GESTURE_Z_SERIES);
		gestureXSeriesRenderer = (XYSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_X_RENDERER);
		gestureYSeriesRenderer = (XYSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_Y_RENDERER);
		gestureZSeriesRenderer = (XYSeriesRenderer)savedState.getSerializable(SAVE_STATE_GESTURE_Z_RENDERER);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_STATE_GESTURE_DATASET, gestureDataset);
		outState.putSerializable(SAVE_STATE_GESTURE_RENDERER, gestureRenderer);
		outState.putSerializable(SAVE_STATE_GESTURE_X_SERIES, gestureXSeries);
		outState.putSerializable(SAVE_STATE_GESTURE_Y_SERIES, gestureYSeries);
		outState.putSerializable(SAVE_STATE_GESTURE_Z_SERIES, gestureZSeries);
		outState.putSerializable(SAVE_STATE_GESTURE_X_RENDERER, gestureXSeriesRenderer);
		outState.putSerializable(SAVE_STATE_GESTURE_Y_RENDERER, gestureYSeriesRenderer);
		outState.putSerializable(SAVE_STATE_GESTURE_Z_RENDERER, gestureZSeriesRenderer);
	}

}
