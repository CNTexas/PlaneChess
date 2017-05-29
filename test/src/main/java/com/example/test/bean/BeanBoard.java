package com.example.test.bean;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author XQF
 * @created 2017/5/24
 */
public class BeanBoard {

    public static List<BeanCell> getAllBeanCell() {

        int[] x = new int[]{
                398, 372, 346,
                294, 268, 242,
                216, 190, 164, 138, 112, 86, 60,

                86, 112, 138,
                138, 112, 86,
                60, 86, 112, 138, 164, 190, 216,

                242, 268, 294,
                346, 372, 398,
                424, 450, 476, 502, 528, 554, 580,

                554, 528, 502,
                502, 528, 554,
                580, 554, 528, 502, 476, 450, 424
        };
        int[] y = new int[]{
                302, 288, 274,
                274, 288, 302,
                316, 302, 288, 274, 260, 246, 232,

                218, 204, 190,
                162, 148, 134,
                120, 106, 92, 78, 64, 50, 36,

                50, 64, 78,
                78, 64, 50,
                36, 50, 64, 78, 92, 106, 120,

                134, 148, 162,
                190, 204, 218,
                232, 246, 260, 274, 288, 302, 316
        };

        int[] redX = new int[]{
                138, 164, 190, 216, 242, 268, 294
        };
        int[] redY = new int[]{
                274, 260, 246, 232, 218, 204, 190
        };
        int[] blueX = new int[]{
                502, 476, 450, 424, 398, 372, 346
        };
        int[] blueY = new int[]{
                274, 260, 246, 232, 218, 204, 190
        };
        int[] yellowX = new int[]{
                138, 164, 190, 216, 242, 268, 294
        };
        int[] yellowY = new int[]{
                78, 92, 106, 120, 134, 148, 162
        };
        int[] greenX = new int[]{
                502, 476, 450, 424, 398, 372, 346
        };
        int[] greenY = new int[]{
                78, 92, 106, 120, 134, 148, 162
        };

        List<BeanCell> resultBeansCells = new ArrayList<>();
        int i;
        for (i = 0; i < x.length; i++) {
            int color = i % 4;
            BeanCell beanCell = new BeanCell(x[i], y[i], color);
            resultBeansCells.add(beanCell);
        }
        for (; i < 60; i++) {
            resultBeansCells.add(null);
        }
        for (; i < 70; i++) {
            if (i < 67) {
                int temp = i - 60;
                resultBeansCells.add(new BeanCell(blueX[temp], blueY[temp], BeanCell.COLOR_BLUE));
            } else {
                resultBeansCells.add(null);
            }
        }
        for (; i < 80; i++) {
            if (i < 77) {
                int temp = i - 70;
                resultBeansCells.add(new BeanCell(redX[temp], redY[temp], BeanCell.COLOR_RED));
            } else {
                resultBeansCells.add(null);
            }
        }
        for (; i < 90; i++) {
            if (i < 87) {
                int temp = i - 80;
                resultBeansCells.add(new BeanCell(yellowX[temp], yellowY[temp], BeanCell.COLOR_YELLOW));
            } else {
                resultBeansCells.add(null);
            }
        }
        for (; i < 100; i++) {
            if (i < 97) {
                int temp = i - 90;
                resultBeansCells.add(new BeanCell(greenX[temp], greenY[temp], BeanCell.COLOR_GREEN));
            } else {
                resultBeansCells.add(null);
            }
        }

        return resultBeansCells;
    }

    public static int getRealStep(int currentIndex, int step, int color) {
        int result = -1;
        BeanCell beanCell = getAllBeanCell().get(currentIndex);

        if (currentIndex >= 60 && currentIndex < 70) {
            result = 67 - (currentIndex + step) % 67;
        } else if (currentIndex >= 70 && currentIndex < 80) {
            result = 77 - (currentIndex + step) % 77;
        } else if (currentIndex >= 80 && currentIndex < 90) {
            result = 87 - (currentIndex + step) % 87;
        } else if (currentIndex >= 90 && currentIndex < 100) {
            result = 97 - (currentIndex + step) % 97;
        } else {
            currentIndex += step;
            if (beanCell.color == color) {
                if (color == BeanCell.COLOR_BLUE) {
                    if (currentIndex == 16) {
                        result = currentIndex + 12;
                    } else {
                        result = currentIndex + 4;
                    }
                } else if (color == BeanCell.COLOR_RED) {
                    if (currentIndex == 29) {
                        result = currentIndex + 12;
                    } else {
                        result = currentIndex + 4;
                    }

                } else if (color == BeanCell.COLOR_YELLOW) {
                    if (currentIndex == 42) {
                        result = currentIndex + 12;
                    } else {
                        result = currentIndex + 4;
                    }

                } else {
                    if (currentIndex == 3) {
                        result = currentIndex + 12;
                    } else {
                        result = currentIndex + 4;
                    }
                }
            } else {
                result = (currentIndex) % 52;
            }
        }


        Log.d("test", "currentIdndex :" + result);
        return result;
    }


}