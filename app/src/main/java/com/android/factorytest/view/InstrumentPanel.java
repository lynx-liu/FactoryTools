package com.android.factorytest.view;

import org.hermit.core.SurfaceRunner;
import org.hermit.instruments.AudioAnalyser;
import org.hermit.instruments.AudioAnalyser.OnAudioDataChange;
import org.hermit.instruments.PowerGauge;
import org.hermit.instruments.WaveformGauge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

public class InstrumentPanel extends SurfaceRunner
{
    public InstrumentPanel(Context context) {
        super(context, SURFACE_DYNAMIC);
        audioAnalyser = new AudioAnalyser();
    }

    public void setSampleRate(int rate) {
        audioAnalyser.setSampleRate(rate);
    }

    public void setBlockSize(int size) {
        audioAnalyser.setBlockSize(size);
    }

    public void setDecimation(int rate) {
        audioAnalyser.setDecimation(rate);
    }

    protected void animStart() {
        audioAnalyser.measureStart();
    }

    protected void animStop() {
        audioAnalyser.measureStop();
    }

    protected void doUpdate(long now) {
        audioAnalyser.doUpdate(now);
    }

    protected void doDraw(Canvas canvas, long now) {
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);//设置背景颜色
            waveformGauge.drawBody(canvas, now);

            canvas.save();
            canvas.clipRect(powerRect);
            powerGauge.drawBody(canvas, now);
            canvas.restore();
        }
    }

    public void loadInstruments() {
    	onPause();//Stop surface update
   		audioAnalyser.resetGauge();//Clear analyse events
    	waveformGauge = audioAnalyser.getWaveformGauge(this);
    	powerGauge = audioAnalyser.getPowerGauge(this);

    	//Load current layout in Gauges if they're already define 
    	if ((currentWidth>0)&&(currentHeight>0))
    		refreshLayout();
    	
		//Restart
    	onResume();
    }

    protected void layout(int width, int height) {
    	//Save current layout
    	currentWidth=width;
    	currentHeight=height;
    	refreshLayout();
    }

    protected void refreshLayout() {   	
        // Make up some layout parameters.
        int min = Math.min(currentWidth, currentHeight);
        int gutter = min / (min > 400 ? 9 : 16);

        // Calculate the layout based on the screen configuration.
        if (currentWidth > currentHeight)
            layoutLandscape(currentWidth, currentHeight, gutter);
        else
            layoutPortrait(currentWidth, currentHeight, gutter);
        
        // Set the gauge geometries.
        if (waveformGauge!=null)
        	waveformGauge.setGeometry(waveRect);
        if (powerGauge!=null)
        	powerGauge.setGeometry(powerRect);
    }

    private void layoutLandscape(int width, int height, int gutter) {      
        int x = gutter;
        int y = gutter;

        // Divide the display into two columns.
        int col = width / 2;

    	//Init
        waveRect = new Rect(0,0,0,0);
    	powerRect = new Rect(0,0,0,0);
    	
        if (waveformGauge!=null) {
            // Divide the left pane in two.
            int row = (height - gutter * 3) / 2;

        	//Wave+Spectrum+Power or Wave+Sonagram+Power
            waveRect = new Rect(x, y, x + col, y + row);
            y += row + gutter;        	
            powerRect = new Rect(x, y, x + col, height - gutter);
        }
     }

    private void layoutPortrait(int width, int height, int gutter) {
        int x = gutter;
        int y = gutter;

        // Display one column.
        int col = width - gutter * 2;

    	//Init
        waveRect = new Rect(0,0,0,0);
    	powerRect = new Rect(0,0,0,0);
        
        if (waveformGauge!=null) {
            // Divide the display into three vertical elements, the
            // spectrum or sonagram display being double-height.
            int unit = (height - gutter * 4) / 4;

            //Wave+Spectrum+Power or Wave+Sonagram+Power
            waveRect = new Rect(x, y, x + col, y + unit);
            y += unit + gutter;
            y += unit * 2 + gutter;
            powerRect = new Rect(x, y, x + col, y + unit);        	
        }
    }

    // Debugging tag.
	private static final String TAG = "Audalyzer";

    //Current layout
    private int currentWidth=0;
    private int currentHeight=0;
    
    // Our audio input device.
    private final AudioAnalyser audioAnalyser;
    
    // The gauges associated with this instrument.
    private WaveformGauge waveformGauge = null;
    private PowerGauge powerGauge = null;

    // Bounding rectangles for the waveform, spectrum, sonagram, and VU meter displays.
    private Rect waveRect = null;
    private Rect powerRect = null;

    public void setOnAudioDataChange(OnAudioDataChange onAudioDataChange){
    	audioAnalyser.setOnAudioDataChange(onAudioDataChange);
    }
}

