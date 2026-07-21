package party.stoat.patchwork.virtual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import party.stoat.patchwork.Patchwork;

public class MachineLevelSavedData extends SavedData {

    private int count;

    public static final SavedDataType<MachineLevelSavedData> ID = new SavedDataType<>(
            // The identifier of the saved data
            // Used as the path within the `data` folder
            Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "machine_count"),
            // The initial constructor
            MachineLevelSavedData::new,
            // The codec used to serialize the data
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("count").forGetter(sd -> sd.count)
            ).apply(instance, MachineLevelSavedData::new))
    );


    public MachineLevelSavedData() {
    }

    public MachineLevelSavedData(int count) {
        this.count = count;
    }

    public int increment() {
        var old = this.count;
        this.count += 1;
        this.setDirty();

        return old;
    }

}
