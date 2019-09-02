package cn.tklvyou.guiderobot.utils;

import static java.lang.Math.exp;


/**
 * Created by Forrest on 2019/04/04.
 * 用于将Gray Pixel 转换为ARGB
 */

public class GREY2RGB {

    static final int TABLE_SIZE = 256;
    public static int GREY2RGB_TABLE[] = new int[TABLE_SIZE];

    static {
        // The factor for converting log2-odds into integers:
        final double LOGODD_K = 16;
        final double LOGODD_K_INV = 1.0 / LOGODD_K;

        for (int i = 0; i < TABLE_SIZE; i++) {
            float f = (float) (1.0f / (1.0f + exp((127 - i) * LOGODD_K_INV)));
            GREY2RGB_TABLE[i] = (int) (f * 255.0f);
        }
    }

    private GREY2RGB() {
    }

    /**
     * Scales an integer representation of the log-odd into a linear scale [0,255], using p=exp(l)/(1+exp(l))
     */
    static int greyToRGB(final int grey) {
        return GREY2RGB_TABLE[grey];
    }
}
