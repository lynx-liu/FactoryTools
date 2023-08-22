package org.hermit.utils;

public class CharFormatter {
    private CharFormatter() {
    }

    public static final void blank(char[] buf, int off, int field) {
        if (field < 0) {
            field = buf.length - off;
        }

        if (field < 0) {
            field = 0;
        }

        if (off + field > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + field);
        } else {
            for(int i = 0; i < field; ++i) {
                buf[off + i] = ' ';
            }

        }
    }

    public static final void formatString(char[] buf, int off, String val, int field) {
        formatString(buf, off, val, field, false);
    }

    public static final void formatString(char[] buf, int off, String val, int field, boolean right) {
        if (field < 0) {
            field = buf.length - off;
        }

        if (field < 0) {
            field = 0;
        }

        if (off + field > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + field);
        } else if (val != null && val.length() != 0) {
            int strlen = val.length();
            int len = val.length() < field ? val.length() : field;
            int pad = field - len;
            if (!right) {
                val.getChars(0, len, buf, off);
            } else {
                val.getChars(strlen - len, strlen, buf, off + pad);
            }

            int pads = !right ? off + len : off;

            for(int i = 0; i < pad; ++i) {
                buf[pads + i] = ' ';
            }

        } else {
            blank(buf, off, field);
        }
    }

    public static final void formatChar(char[] buf, int off, char val, int field, boolean right) {
        if (field < 0) {
            field = buf.length - off;
        }

        if (field < 0) {
            field = 0;
        }

        if (off + field > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + field);
        } else if (val != 0 && field >= 1) {
            int pad = field - 1;
            if (!right) {
                buf[off] = val;
            } else {
                buf[off + pad] = val;
            }

            int pads = !right ? off + 1 : off;

            for(int i = 0; i < pad; ++i) {
                buf[pads + i] = ' ';
            }

        } else {
            blank(buf, off, field);
        }
    }

    public static final void formatInt(char[] buf, int off, int val, int field, boolean signed) {
        formatInt(buf, off, val, field, signed, false);
    }

    public static final void formatInt(char[] buf, int off, int val, int field, boolean signed, boolean lz) {
        if (field < 0) {
            field = buf.length - off;
        }

        if (field < 0) {
            field = 0;
        }

        if (off + field > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + field);
        } else if (!signed && val < 0) {
            formatChar(buf, off, '-', field, true);
        } else {
            int sign = val >= 0 ? 1 : -1;
            val *= sign;
            int schar = signed ? (sign < 0 ? 45 : 32) : 0;

            try {
                formatInt(buf, off, val, field, (char)schar, lz);
            } catch (CharFormatter.OverflowException var9) {
                formatChar(buf, off, '+', field, true);
            }

        }
    }

    public static final void formatIntLeft(char[] buf, int off, int val, int field, boolean signed) {
        if (field < 0) {
            field = buf.length - off;
        }

        if (field < 0) {
            field = 0;
        }

        if (off + field > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + field);
        } else if (!signed && val < 0) {
            formatChar(buf, off, '-', field, false);
        } else {
            int sign = val >= 0 ? 1 : -1;
            val *= sign;
            int schar = signed ? (sign < 0 ? 45 : 32) : 0;

            try {
                formatIntLeft(buf, off, val, field, (char)schar);
            } catch (CharFormatter.OverflowException var8) {
                formatChar(buf, off, '+', field, false);
            }

        }
    }

    public static final void formatHex(char[] buf, int off, int val, int field) {
        if (field < 0) {
            field = buf.length - off;
        }

        if (field < 0) {
            field = 0;
        }

        if (off + field > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + field);
        } else if (field < 1) {
            throw new IllegalArgumentException("Field <" + field + "> too small");
        } else {
            for(int i = off + field - 1; i >= off; --i) {
                int d = val % 16;
                buf[i] = d < 10 ? (char)(48 + d) : (char)(97 + d - 10);
                val /= 16;
            }

            if (val != 0) {
                formatChar(buf, off, '+', field, true);
            }

        }
    }

    public static final void formatFloat(char[] buf, int off, double val, int field, int frac) {
        formatFloat(buf, off, val, field, frac, true);
    }

    public static final void formatFloat(char[] buf, int off, double val, int field, int frac, boolean signed) {
        if (field < 0) {
            field = buf.length - off;
        }

        if (field < 0) {
            field = 0;
        }

        if (off + field > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + field);
        } else if (!signed && val < 0.0D) {
            formatChar(buf, off, '-', field, true);
        } else {
            int intDigits = field - frac - 1;
            int sign = val >= 0.0D ? 1 : -1;
            val *= (double)sign;
            char schar = (char) (signed ? (sign < 0 ? 45 : 32) : 0);
            int intPart = (int)val;
            double fracPart = val - (double)intPart;

            for(int i = 0; i < frac; ++i) {
                fracPart *= 10.0D;
            }

            try {
                formatInt(buf, off, intPart, intDigits, (char)schar, false);
                buf[off + intDigits] = '.';
                formatInt(buf, off + intDigits + 1, (int)fracPart, frac, '\u0000', true);
            } catch (CharFormatter.OverflowException var14) {
                formatChar(buf, off, '+', field, true);
            }

        }
    }

    private static final void formatInt(char[] buf, int off, int val, int field, char schar, boolean leadZero) throws IllegalArgumentException, CharFormatter.OverflowException {
        int intDigits = field - (schar != 0 ? 1 : 0);
        if (intDigits < 1) {
            throw new IllegalArgumentException("Field <" + field + "> too small");
        } else {
            int last = 0;

            for(int i = off + field - 1; i >= off + field - intDigits; --i) {
                if (val == 0 && !leadZero && i < off + field - 1) {
                    buf[i] = ' ';
                } else {
                    buf[i] = val == 0 ? 48 : (char)(48 + val % 10);
                    val /= 10;
                    last = i;
                }
            }

            if (val != 0) {
                formatChar(buf, off, '+', field, true);
                throw new CharFormatter.OverflowException();
            } else {
                if (schar != 0) {
                    buf[off] = ' ';
                    buf[last - 1] = schar;
                }

            }
        }
    }

    private static final void formatIntLeft(char[] buf, int off, int val, int field, char schar) throws IllegalArgumentException, CharFormatter.OverflowException {
        int intDigits = field - (schar != 0 ? 1 : 0);
        if (intDigits < 1) {
            throw new IllegalArgumentException("Field <" + field + "> too small");
        } else {
            int valDigits = 1;

            for(int v = val / 10; v > 0; ++valDigits) {
                v /= 10;
            }

            if (intDigits < valDigits) {
                formatChar(buf, off, '+', field, false);
                throw new CharFormatter.OverflowException();
            } else {
                int index = off;
                if (schar != 0) {
                    index = off + 1;
                    buf[off] = schar;
                }

                for(int i = index + valDigits - 1; i >= index; --i) {
                    buf[i] = val == 0 ? 48 : (char)(48 + val % 10);
                    val /= 10;
                }

                for(index += valDigits; index < off + field; buf[index++] = ' ') {
                }

            }
        }
    }

    public static int formatDegMin(char[] buf, int off, double angle) {
        return formatDegMin(buf, off, angle, ' ', '-', false);
    }

    public static int formatDegMin(char[] buf, int off, double angle, char pos, char neg, boolean space) {
        if (off + (space ? 14 : 12) > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + (space ? 14 : 12));
        } else {
            int p;
            if (angle < 0.0D) {
                p = off + 1;
                buf[off] = neg;
                angle = -angle;
            } else {
                p = off + 1;
                buf[off] = pos;
            }

            if (space) {
                buf[p++] = ' ';
            }

            int deg = (int)angle;
            int min = (int)(angle * 60.0D % 60.0D);
            int frac = (int)(angle * 60000.0D % 1000.0D);

            try {
                formatInt(buf, p, deg, 3, '\u0000', false);
                p += 3;
                buf[p++] = 176;
                if (space) {
                    buf[p++] = ' ';
                }

                formatInt(buf, p, min, 2, '\u0000', true);
                p += 2;
                buf[p++] = '.';
                formatInt(buf, p, frac, 3, '\u0000', true);
                p += 3;
                buf[p++] = '\'';
            } catch (CharFormatter.OverflowException var12) {
                formatChar(buf, p, '+', 1, true);
            }

            return p - off;
        }
    }

    public static int formatLatLon(char[] buf, int off, double lat, double lon, boolean space) {
        if (off + (space ? 29 : 25) > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Buffer [" + buf.length + "] too small for " + off + "+" + (space ? 29 : 25));
        } else {
            int p = off + formatDegMin(buf, off, lat, 'N', 'S', space);
            buf[p++] = ' ';
            p += formatDegMin(buf, off, lon, 'E', 'W', space);
            return p - off;
        }
    }

    private static final class OverflowException extends Exception {
        private static final long serialVersionUID = -6009530000597939453L;

        private OverflowException() {
        }
    }
}
