package com.finalProject.RubikSolver;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public class Pixel implements Serializable {
    int Red;
    int Green;
    int Blue;
    String Color = "";

    public Pixel(int red, int green, int blue) {
        Red = red;
        Green = green;
        Blue = blue;
        makeColor(this);
    }

    /**
     * 将RGB值转换为文字的方法 即用作显示，也用作魔方还原数据
     * <p>
     * 需要注意的是 由于光线 魔方型号 等等因素干扰
     * 这里面的数字你可以自己进行调整
     */

    private void makeColor(Pixel p) {
        if (p.Red > 120) {
            if (p.Green > 160) {
                if (p.Blue > 150) {
                    this.Color = "w";
                } else {
                    this.Color = "y";
                }
            } else if (p.Green > 50) {
                this.Color = "o";
            } else {
                this.Color = "r";
            }
        } else {
            if (p.Green > p.Blue) {
                this.Color = "g";
            } else {
                this.Color = "b";
            }
        }
    }
    public String getColor() {
        return Color;
    }
}
