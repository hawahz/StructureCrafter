package io.github.hawah.structure_crafter.client.render;

public class EaseHelper {
    public static float easeOutBounce(float x) {
        final float n1 = 7.5625f;
        final float d1 = 2.75f;

        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            x -= 1.5f / d1;
            return n1 * x * x + 0.75f;
        } else if (x < 2.5 / d1) {
            x -= 2.25f / d1;
            return n1 * x * x + 0.9375f;
        } else {
            x -= 2.625f / d1;
            return n1 * x * x + 0.984375f;
        }
    }

    public static float easeInPow(float x, float power) {
        float ret = x;
        for (int i = 0; i < power; i++) {
            ret *= x;
        }
        return ret;
    }
}
