package party.stoat.patchwork.virtual;

import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.saveddata.SavedData;
import party.stoat.patchwork.patchgraph.StorageConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerSavedData extends SavedData {

    private final HashMap<UUID, StorageConfiguration> configs = new HashMap<>();

    public ServerSavedData() {
    }

    public static ServerSavedData create() {
        return new ServerSavedData();
    }

    public HashMap<UUID, StorageConfiguration> configs() {
        return configs;
    }

    public static ServerSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ServerSavedData data = new ServerSavedData();

        CompoundTag configsTag = tag.getCompound("configs");

        for (String key : configsTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);

            DataResult<StorageConfiguration> result =
                    StorageConfiguration.CODEC.parse(
                            NbtOps.INSTANCE,
                            configsTag.get(key)
                    );

            result.result().ifPresent(config -> data.configs.put(uuid, config));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag configsTag = new CompoundTag();

        for (Map.Entry<UUID, StorageConfiguration> entry : configs.entrySet()) {
            StorageConfiguration.CODEC.encodeStart(
                    NbtOps.INSTANCE,
                    entry.getValue()
            ).result().ifPresent(encoded ->
                    configsTag.put(entry.getKey().toString(), encoded)
            );
        }

        tag.put("configs", configsTag);
        return tag;
    }
}
