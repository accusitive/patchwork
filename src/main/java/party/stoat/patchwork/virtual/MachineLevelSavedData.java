package party.stoat.patchwork.virtual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class MachineLevelSavedData extends SavedData {

    private int count;
    public HashSet<BlockPos> virtualized;

    public MachineLevelSavedData() { }

    public static MachineLevelSavedData create() {
        return new MachineLevelSavedData();
    }

    public static MachineLevelSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        MachineLevelSavedData data = new MachineLevelSavedData();

        data.count = tag.getInt("count");

        ListTag list = tag.getList("virtualized", Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag p = (CompoundTag)t;
            data.virtualized.add(new BlockPos(p.getInt("x"), p.getInt("y"), p.getInt("z")));
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        tag.putInt("count", count);
        ListTag list = new ListTag();

        for (BlockPos pos : virtualized) {
            CompoundTag p = new CompoundTag();
            p.putInt("x", pos.getX());
            p.putInt("y", pos.getY());
            p.putInt("z", pos.getZ());
            list.add(p);
        }

        tag.put("virtualized", list);
        return tag;
    }

    public int increment() {
        var old = this.count;
        this.count += 1;
        this.setDirty();

        return old;
    }

}
