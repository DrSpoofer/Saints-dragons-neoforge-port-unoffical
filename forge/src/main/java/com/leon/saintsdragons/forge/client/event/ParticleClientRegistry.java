package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.client.init.CommonParticleFactories;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public class ParticleClientRegistry {
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        CommonParticleFactories.register(new CommonParticleFactories.Registrar() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type,
                                                            CommonParticleFactories.SpriteFactory<T> factory) {
                event.registerSpriteSet(type, factory::create);
            }
        });
    }
}
