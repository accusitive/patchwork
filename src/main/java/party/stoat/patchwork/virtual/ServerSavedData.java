package party.stoat.patchwork.virtual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.patchgraph.StorageConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerSavedData extends SavedData {

    public HashMap<UUID, StorageConfiguration> configs;

    public static final SavedDataType<ServerSavedData> ID = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "configs"),
            ServerSavedData::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(UUIDUtil.STRING_CODEC, StorageConfiguration.CODEC)
                            .fieldOf("configs")
                            .forGetter(ServerSavedData::configs)
            ).apply(instance, ServerSavedData::new))
    );

    public ServerSavedData() {
        this.configs = new HashMap<>();
    }

    public HashMap<UUID, StorageConfiguration> configs() {
        return configs;
    }

    public ServerSavedData(Map<UUID, StorageConfiguration> configs) {
        this.configs = new HashMap<>(configs);
    }

}
