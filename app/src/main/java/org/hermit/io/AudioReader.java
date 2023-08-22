package org.hermit.io;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

public class AudioReader {
    private static final String TAG = "WindMeter";
    private AudioRecord audioInput;
    private short[][] inputBuffer = null;
    private int inputBufferWhich = 0;
    private int inputBufferIndex = 0;
    private int inputBlockSize = 0;
    private long sleepTime = 0L;
    private AudioReader.Listener inputListener = null;
    private boolean running = false;
    private Thread readerThread = null;

    public AudioReader() {
    }

    public void startReader(int rate, int block, AudioReader.Listener listener) {
        Log.i("WindMeter", "Reader: Start Thread");
        synchronized(this) {
            AudioFormat.Builder audioFormatBuilder = new AudioFormat.Builder();
            audioFormatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
            audioFormatBuilder.setSampleRate(rate);
            audioFormatBuilder.setChannelMask(AudioFormat.CHANNEL_IN_STEREO);
            AudioFormat audioFormat = audioFormatBuilder.build();

            AudioRecord.Builder builder = new AudioRecord.Builder();
            builder.setAudioSource(MediaRecorder.AudioSource.MIC);
            builder.setAudioFormat(audioFormat);

            int audioBuf = AudioRecord.getMinBufferSize(rate, 2, AudioFormat.ENCODING_PCM_16BIT);
            Log.i("vita", "audioBuf = " + audioBuf);
            builder.setBufferSizeInBytes(8 * audioBuf);
            audioInput = builder.build();
//            audioInput = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX, rate, 2, AudioFormat.ENCODING_PCM_16BIT, audioBuf / 1);
            inputBlockSize = block;
            sleepTime = (long)(1000.0F / ((float)rate / (float)block));
            inputBuffer = new short[2][inputBlockSize];
            inputBufferWhich = 0;
            inputBufferIndex = 0;
            inputListener = listener;
            running = true;
            readerThread = new Thread(new Runnable() {
                public void run() {
                    AudioReader.this.readerRun();
                }
            }, "Audio Reader");
            readerThread.start();
        }
    }

    public void stopReader() {
        Log.i("WindMeter", "Reader: Signal Stop");
        synchronized(this) {
            running = false;
        }

        try {
            if (readerThread != null) {
                readerThread.join();
            }
        } catch (InterruptedException var2) {
        }

        readerThread = null;
        synchronized(this) {
            if (audioInput != null) {
                audioInput.release();
                audioInput = null;
            }
        }

        Log.i("WindMeter", "Reader: Thread Stopped");
    }

    private void readerRun() {
        int timeout = 200;

        try {
            while(timeout > 0 && audioInput.getState() != 1) {
                Thread.sleep(50L);
                timeout -= 50;
            }
        } catch (InterruptedException var25) {
        }

        if (audioInput.getState() != 1) {
            Log.e("WindMeter", "Audio reader failed to initialize");
            running = false;
        } else {
            try {
                Log.i("WindMeter", "Reader: Start Recording");
                audioInput.startRecording();

                while(running) {
                    long stime = System.currentTimeMillis();
                    if (!running) {
                        break;
                    }

                    int readSize = inputBlockSize;
                    int space = inputBlockSize - inputBufferIndex;
                    if (readSize > space) {
                        readSize = space;
                    }

                    short[] buffer = inputBuffer[inputBufferWhich];
                    int index = inputBufferIndex;
                    synchronized(buffer) {
                        int nread = audioInput.read(buffer, index, readSize);
                        StringBuilder stringBuilder = new StringBuilder(String.format("recv(%d):",nread));
                        for (int i = 0; i < nread; i++)
                            stringBuilder.append(String.format("%04X ", buffer[i]));
                        Log.d("llx",stringBuilder.toString());

                        boolean done = false;
                        if (!running) {
                            break;
                        }

                        if (nread < 0) {
                            Log.e("WindMeter", "Audio read failed: error " + nread);
                            running = false;
                            break;
                        }

                        int end = inputBufferIndex + nread;
                        if (end >= inputBlockSize) {
                            inputBufferWhich = (inputBufferWhich + 1) % 2;
                            inputBufferIndex = 0;
                            done = true;
                        } else {
                            inputBufferIndex = end;
                        }

                        if (done) {
                            readDone(buffer);
                            long etime = System.currentTimeMillis();
                            long sleep = sleepTime - (etime - stime);
                            if (sleep < 5L) {
                                sleep = 5L;
                            }

                            try {
                                buffer.wait(sleep);
                            } catch (InterruptedException var22) {
                            }
                        }
                    }
                }
            } finally {
                Log.i("WindMeter", "Reader: Stop Recording");
                if (audioInput.getState() == 3) {
                    audioInput.stop();
                }

            }

        }
    }

    private void readDone(short[] buffer) {
        inputListener.onReadComplete(buffer);
    }

    public abstract static class Listener {
        public static final int ERR_OK = 0;
        public static final int ERR_INIT_FAILED = 1;
        public static final int ERR_READ_FAILED = 2;

        public Listener() {
        }

        public abstract void onReadComplete(short[] var1);
    }
}
