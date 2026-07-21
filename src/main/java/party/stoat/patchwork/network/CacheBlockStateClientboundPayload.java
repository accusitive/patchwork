package party.stoat.patchwork.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import party.stoat.patchwork.Patchwork;

import java.util.Optional;

public record CacheBlockStateClientboundPayload(BlockState state, Optional<CompoundTag> tag, BlockPos pos) implements CustomPacketPayload {

    public static final Identifier CACHE_BLOCK = Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "sync_block");
    public static final CustomPacketPayload.Type<CacheBlockStateClientboundPayload> TYPE = new CustomPacketPayload.Type<>(CACHE_BLOCK);

    public static final StreamCodec<RegistryFriendlyByteBuf, CacheBlockStateClientboundPayload> CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.fromCodec(BlockState.CODEC), CacheBlockStateClientboundPayload::state,
                    ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)), CacheBlockStateClientboundPayload::tag,
                    BlockPos.STREAM_CODEC, CacheBlockStateClientboundPayload::pos,
                    CacheBlockStateClientboundPayload::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
