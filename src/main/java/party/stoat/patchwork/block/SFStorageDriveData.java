package party.stoat.patchwork.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record SFStorageDriveData(UUID id) {

    public static Codec<SFStorageDriveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(SFStorageDriveData::id)
    ).apply(instance, SFStorageDriveData::new));

}
