package party.stoat.patchwork.patchgraph.nodes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.block.PatchworkHandler;
import party.stoat.patchwork.patchgraph.Node;
import party.stoat.patchwork.patchgraph.NodeDescriptor;
import party.stoat.patchwork.patchgraph.PatchInstance;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SplitterNode extends Node {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "splitter");

    public SplitterNode(UUID uuid, NodeDescriptor descriptor) {
        super(uuid, descriptor);
    }

    record SplitterItemHandler(List<IItemHandler> handlers) implements IItemHandler {
        @Override
        public int getSlots() {
            // Virtual handler acts as a single insertion point.
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || handlers.isEmpty())
                return stack;

            ItemStack remaining = stack.copy();

            int handlerCount = handlers.size();

            // First pass: attempt even distribution.
            int baseAmount = stack.getCount() / handlerCount;
            int extra = stack.getCount() % handlerCount;

            for (int i = 0; i < handlerCount && !remaining.isEmpty(); i++) {
                int desired = baseAmount + (i < extra ? 1 : 0);

                if (desired <= 0)
                    continue;

                ItemStack portion = remaining.copy();
                portion.setCount(Math.min(desired, remaining.getCount()));

                ItemStack remainder = insertIntoHandler(handlers.get(i), portion, simulate);

                int inserted = portion.getCount() - remainder.getCount();

                if (inserted > 0)
                    remaining.shrink(inserted);
            }

            // Second pass: greedily consume any leftovers.
            if (!remaining.isEmpty()) {
                for (IItemHandler handler : handlers) {
                    if (remaining.isEmpty())
                        break;

                    remaining = insertIntoHandler(handler, remaining, simulate);
                }
            }

            return remaining;
        }

        private static ItemStack insertIntoHandler(IItemHandler handler,
                                                   ItemStack stack,
                                                   boolean simulate) {
            ItemStack remaining = stack.copy();

            for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
                remaining = handler.insertItem(slot, remaining, simulate);
            }

            return remaining;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return handlers.stream().anyMatch(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (handler.isItemValid(i, stack))
                        return true;
                }
                return false;
            });
        }
    }

    @Override
    public @Nullable IItemHandler getItemHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance patch) {
        var outputs = getOutputConnections(patch.graph);

        return new SplitterItemHandler(outputs.stream().map(output -> {
            var foreignPort = patch.graph.nodeDescriptors.get(output.to()).getPort(output.keyTo());
            var foreignNode = patch.nodes.get(output.to());
            return foreignNode.getItemHandler(level, foreignPort, patch);
        }).filter(Objects::nonNull).toList()
        );
    }

    @Override
    public ResourceLocation getIdentifier() {
        return null;
    }

    @Override
    public void acceptConfiguration(String string) {

    }
}
