package io.github.hawah.structure_crafter.client.render;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import net.minecraft.util.Mth;

@SuppressWarnings("unchecked")
public abstract class ColoredElement<Self extends ColoredElement<Self>> extends RenderElement<Self>{
    protected float r = 1.0F, g = 1.0F, b = 1.0F, a = 1.0F;
    protected float or;
    protected float og;
    protected float ob;
    public float oa;
    protected float targetR = 1.0F, targetG = 1.0F, targetB = 1.0F, targetA = 1.0F;


    /**
     * 设置元素的颜色和透明度值
     *
     * @param r 红色分量，范围通常为0.0-1.0
     * @param g 绿色分量，范围通常为0.0-1.0
     * @param b 蓝色分量，范围通常为0.0-1.0
     * @param a 透明度分量，范围通常为0.0-1.0
     * @return 返回当前对象的引用，支持链式调用
     */
    public Self setRGBA(float r, float g, float b, float a) {
        setR(r);
        setG(g);
        setB(b);
        setA(a);
        return (Self) this;
    }

    /**
     * 将元素设置为淡出状态
     *
     * @return 返回当前对象的引用，支持链式调用
     */
    public Self fade() {
        setA(0);
        return (Self) this;
    }

    /**
     * 延迟指定 tick 数后执行淡出操作
     *
     * @param delayTicks 延迟的 tick 数量
     * @return 返回当前对象的引用，支持链式调用
     */
    public Self lazyFade(int delayTicks) {
        lazySet(delayTicks, this::fade);
        return (Self) this;
    }
    /**
     * 丢弃元素，先执行淡出效果再调用父类的丢弃逻辑
     *
     * @return 返回当前对象的引用，支持链式调用
     */
    @Override
    public Self discard() {
        this.fade();
        return super.discard();
    }

    public void setR(float r) {
        this.targetR = r;
    }

    public void setG(float g) {
        this.targetG = g;
    }

    public void setB(float b) {
        this.targetB = b;
    }

    public void setA(float a) {
        this.targetA = a;
    }

    public void tick() {
        super.tick();
        or = r;
        og = g;
        ob = b;
        oa = a;
        r = Mth.lerp(StructureCrafterClient.ANI_DELTAF, r, targetR);
        g = Mth.lerp(StructureCrafterClient.ANI_DELTAF, g, targetG);
        b = Mth.lerp(StructureCrafterClient.ANI_DELTAF, b, targetB);
        a = Mth.lerp(StructureCrafterClient.ANI_DELTAF, a, targetA);
    }

    @Override
    public boolean finish() {
        if (!dirty)
            return false;
        dirty = false;
        r = targetR;
        g = targetG;
        b = targetB;
        a = targetA;
        return true;
    }
}
