package org.hermit.dsp;

public final class SignalPower {
    private static final float MAX_16_BIT = 32768.0F;
    private static final float FUDGE = 0.6F;

    private SignalPower() {
    }

    public static final void biasAndRange(short[] sdata, int off, int samples, float[] out) {
        short min = 32767;
        short max = -32768;
        int total = 0;

        for(int i = off; i < off + samples; ++i) {
            short val = sdata[i];
            total += val;
            if (val < min) {
                min = val;
            }

            if (val > max) {
                max = val;
            }
        }

        float bias = (float)total / (float)samples;
        float bmin = (float)min + bias;
        float bmax = (float)max - bias;
        float range = Math.abs(bmax - bmin) / 2.0F;
        out[0] = bias;
        out[1] = range;
    }

    public static final double calculatePowerDb(short[] sdata, int off, int samples) {
        double sum = 0.0D;
        double sqsum = 0.0D;

        for(int i = 0; i < samples; ++i) {
            long v = (long)sdata[off + i];
            sum += (double)v;
            sqsum += (double)(v * v);
        }

        double power = (sqsum - sum * sum / (double)samples) / (double)samples;
        power /= 1.073741824E9D;
        return Math.log10(power) * 10.0D + FUDGE;
    }
}
