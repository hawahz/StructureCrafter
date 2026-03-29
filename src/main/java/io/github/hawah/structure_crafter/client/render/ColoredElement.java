package io.github.hawah.structure_crafter.client.render;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import net.minecraft.util.Mth;

@SuppressWarnings("unchecked")
public abstract class ColoredElement extends RenderElement{
    protected float r = 1.0F, g = 1.0F, b = 1.0F, a = 1.0F;
    protected float or, og, ob, oa;
    protected float targetR = 1.0F, targetG = 1.0F, targetB = 1.0F, targetA = 1.0F;



    public <T extends ColoredElement> T setRGBA(float r, float g, float b, float a) {
        setR(r);
        setG(g);
        setB(b);
        setA(a);
        return (T) this;
    }

    public <T extends ColoredElement> T fade() {
        setA(0);
        return (T) this;
    }

    public <T extends ColoredElement> T lazyFade(int delayTicks) {
        lazySet(delayTicks, this::fade);
        return (T) this;
    }

    @Override
    public <T extends RenderElement> T discard() {
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
