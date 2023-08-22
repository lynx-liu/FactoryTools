package org.hermit.instruments;

import org.hermit.core.SurfaceRunner;
import org.hermit.io.AudioReader;
import org.hermit.io.AudioReader.Listener;

import org.hermit.dsp.SignalPower;

public class AudioAnalyser {
    private static final String TAG = "instrument";
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate;
    private final AudioReader audioReader;
    private WaveformGauge waveformGauge;
    private PowerGauge powerGauge;
    private short[] audioData;
    private long audioSequence;
    private long audioProcessed;
    private double currentPower;
    private float[] biasRange;
    public AudioAnalyser.OnAudioDataChange onAudioDataChange;

    public AudioAnalyser() {
        sampleDecimate = 1;
        waveformGauge = null;
        powerGauge = null;
        audioSequence = 0L;
        audioProcessed = 0L;
        currentPower = 0.0D;
        biasRange = null;
        audioReader = new AudioReader();
        biasRange = new float[2];
    }

    public void setSampleRate(int rate) {
        sampleRate = rate;
    }

    public void setBlockSize(int size) {
        inputBlockSize = size;
    }

    public void setDecimation(int rate) {
        sampleDecimate = rate;
    }

    public void measureStart() {
        audioProcessed = audioSequence = 0L;
        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new Listener() {
            public final void onReadComplete(short[] buffer) {
                AudioAnalyser.this.receiveAudio(buffer);
            }
        });
    }

    public void measureStop() {
        audioReader.stopReader();
    }

    public WaveformGauge getWaveformGauge(SurfaceRunner surface) {
        if (waveformGauge != null) {
            throw new RuntimeException("Already have a WaveformGauge for this AudioAnalyser");
        } else {
            waveformGauge = new WaveformGauge(surface);
            return waveformGauge;
        }
    }

    public PowerGauge getPowerGauge(SurfaceRunner surface) {
        if (powerGauge != null) {
            throw new RuntimeException("Already have a PowerGauge for this AudioAnalyser");
        } else {
            powerGauge = new PowerGauge(surface);
            return powerGauge;
        }
    }

    public void resetGauge() {
        synchronized(this) {
            waveformGauge = null;
            powerGauge = null;
        }
    }

    private final void receiveAudio(short[] buffer) {
        synchronized(this) {
            audioData = buffer;
            ++audioSequence;
        }
    }

    public final void doUpdate(long now) {
        short[] buffer = (short[])null;
        synchronized(this) {
            if (audioData != null && audioSequence > audioProcessed) {
                audioProcessed = audioSequence;
                buffer = audioData;
            }
        }

        if (buffer != null) {
            processAudio(buffer);
        }
    }

    private final void processAudio(short[] buffer) {
        synchronized(buffer) {
            int len = buffer.length;
            if (waveformGauge != null) {
                SignalPower.biasAndRange(buffer, len - inputBlockSize, inputBlockSize, biasRange);
                float bias = biasRange[0];
                float range = biasRange[1];
                if (range < 1.0F) {
                    range = 1.0F;
                }

                waveformGauge.update(buffer, len - inputBlockSize, inputBlockSize, bias, range);
            }

            if (powerGauge != null) {
                currentPower = SignalPower.calculatePowerDb(buffer, 0, len);
            }
            buffer.notify();
        }

        if (powerGauge != null) {
            powerGauge.update(currentPower);
        }

        if (onAudioDataChange != null) {
            onAudioDataChange.onAudioDataChange(currentPower);
        }
    }

    public void setOnAudioDataChange(AudioAnalyser.OnAudioDataChange onAudioDataChange) {
        this.onAudioDataChange = onAudioDataChange;
    }

    public interface OnAudioDataChange {
        void onAudioDataChange(double var1);
    }
}
