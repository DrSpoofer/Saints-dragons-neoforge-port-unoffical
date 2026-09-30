package com.leon.saintsdragons.forge.mixin.client;

import com.leon.saintsdragons.client.renderer.vfx.BloodTempestAfterimageRenderContext;
import net.minecraft.client.model.AgeableListModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.util.FastColor;

@Mixin(AgeableListModel.class)
public abstract class BloodTempestAfterimageModelMixin {
    // 1.21 passes the model tint as one packed ARGB int (light, overlay, colour are int args 0..2).
    @ModifyVariable(method = "renderToBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private int saintsdragons$tintAfterimage(int original) {
        return BloodTempestAfterimageRenderContext.isActive()
                ? FastColor.ARGB32.colorFromFloat(
                        BloodTempestAfterimageRenderContext.alpha(),
                        BloodTempestAfterimageRenderContext.red(),
                        BloodTempestAfterimageRenderContext.green(),
                        BloodTempestAfterimageRenderContext.blue())
                : original;
    }
}
