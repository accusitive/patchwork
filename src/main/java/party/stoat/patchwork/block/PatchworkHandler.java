package party.stoat.patchwork.block;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface PatchworkHandler<T> {

    int size();

    T getResource(int slot);

    long insert(T resource, long amount, boolean simulate);

    long insert(int slot, T resource, long amount, boolean simulate);

    long extract(int slot, T resource, long amount, boolean simulate);

    boolean isValid(int slot, T resource);

    long getCapacityAsLong(int index, @NonNull T resource);

    long getAmountAsLong(int index);
}
