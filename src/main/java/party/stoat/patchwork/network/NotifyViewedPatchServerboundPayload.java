package party.stoat.patchwork.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import party.stoat.patchwork.Patchwork;

import java.util.Optional;
import java.util.UUID;

public record NotifyViewedPatchServerboundPayload(BlockPos controllerPos, UUID patch) implements CustomPacketPayload {

    public static final ResourceLocation CACHE_BLOCK = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "notify_viewing");
    public static final Type<NotifyViewedPatchServerboundPayload> TYPE = new Type<>(CACHE_BLOCK);

    public static final StreamCodec<RegistryFriendlyByteBuf, NotifyViewedPatchServerboundPayload> CODEC = StreamCodec
            .composite(
                    BlockPos.STREAM_CODEC, NotifyViewedPatchServerboundPayload::controllerPos,
                    UUIDUtil.STREAM_CODEC, NotifyViewedPatchServerboundPayload::patch,
                    NotifyViewedPatchServerboundPayload::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
