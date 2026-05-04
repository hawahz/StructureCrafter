package io.github.hawah.structure_crafter.client.render.blockentity.state;

import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.lib.client.render.EaseHelper;
import io.github.hawah.structure_crafter.lib.client.render.toolkit.Animation;
import io.github.hawah.structure_crafter.lib.client.render.toolkit.AnimationPlayer;
import net.minecraft.util.Mth;

public class TelephoneBlockEntityRenderState implements BlockEntityRenderState<TelephoneBlockEntity> {

    public final AnimationPlayer animationPlayer = new AnimationPlayer().cycle(true);
    public float scale = 0;
    public float xRot = 0;
    public float jumpHeight = 0;
    public TelephoneBlockEntityRenderState(TelephoneBlockEntity ignored) {
        Animation<Float> animationScale = animationPlayer.<Float>registerAnimation(
                "scale",
                (scale) -> this.scale = scale,
                (from, to, delta) -> Mth.lerp( (float) delta, from, to),
                1.0F
        );

        Animation<Float> rotAnimation = animationPlayer.<Float>registerAnimation(
                "xRot",
                (xRot) -> this.xRot = xRot,
                (from, to, delta) -> Mth.lerp((float) delta, from, to),
                0.0F
        ).cycle(true).rewind(true).offset(20);
        rotAnimation.addKeyFrame(0, -7.5F);
        rotAnimation.addKeyFrame(1, 7.5F);

        Animation<Float> animationJump = animationPlayer.<Float>registerAnimation(
                "jumpHeight",
                (jumpHeight) -> this.jumpHeight = jumpHeight,
                (from, to, delta) -> Mth.lerp( (float) delta, from, to),
                0.0F
        );
        animationJump.addKeyFrame(0, 0.0F);
        animationScale.addKeyFrame(0, 1.0F);

        animationJump.addKeyFrame(20, 0.0F).withMapping(aDouble -> (double) EaseHelper.easeOutPow((float) aDouble.doubleValue(), 3));
        animationScale.addKeyFrame(20, 1.0F).withMapping(aDouble -> (double) EaseHelper.easeOutPow((float) aDouble.doubleValue(), 3));

        animationJump.addKeyFrame(22, 0.2F);
        animationScale.addKeyFrame(22, 2.0F);

        animationJump.addKeyFrame(98, 0.2F);
        animationScale.addKeyFrame(98, 2.0F);

        animationJump.addKeyFrame(100, 0.0F);
        animationScale.addKeyFrame(100, 1.0F);
        animationPlayer.registerAnimation("none", (o) -> o, Mth::lerp, 1D).addKeyFrame(100, 0D);

        animationPlayer.play();
    }

    @Override
    public void update(TelephoneBlockEntity blockEntity) {

    }
}
