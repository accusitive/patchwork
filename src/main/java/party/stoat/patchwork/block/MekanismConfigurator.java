package party.stoat.patchwork.block;

import mekanism.api.IConfigCardAccess;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ITileDirectional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import party.stoat.patchwork.patchgraph.NodeDescriptor;
import party.stoat.patchwork.patchgraph.StorageConfiguration;

import java.util.HashMap;
import java.util.function.Function;

public class MekanismConfigurator implements StorageConfiguration.BlockConfigurator {

    private HashMap<Integer, TypeConfig> configs = new HashMap<>();
    private Function<Function<String, String>, NodeDescriptor> descriptorSupplier;

    public static class TypeConfig {

        HashMap<Integer, Direction[]> sets = new HashMap<>();

        private final MekanismConfigurator parent;
        final int ordinal;

        TypeConfig(MekanismConfigurator parent, int ordinal) {
            this.parent = parent;
            this.ordinal = ordinal;
        }

        public TypeConfig set(DataType dataType, Direction... dirs) {
            this.sets.put(dataType.ordinal(), dirs);
            return this;
        }

        public MekanismConfigurator finish() {
            this.parent.configs.put(this.ordinal, this);
            return this.parent;
        }

    }

    public TypeConfig config(TransmissionType type) {
        return new TypeConfig(this, type.ordinal());
    }

    @Override
    public void apply(BlockPos pos, BlockState state, BlockEntity entity, ServerLevel level, ServerPlayer player) {
        IConfigCardAccess access = level.getCapability(Capabilities.CONFIG_CARD, pos, null);

        if (access != null && entity instanceof ITileDirectional directional) {
            // Get current configuration data
            TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);

            Direction machineFacing = directional.getDirection();

            access.writeConfigurationData(valueOutput, player);

            var currentConfig = valueOutput.buildResult();
            var component_config = currentConfig.getCompoundOrEmpty("component_config");

            for(var configOrdinal : this.configs.keySet()) {
                var sfConfig = this.configs.get(configOrdinal);
                var mekConfig = component_config.getIntArray(SerializationConstants.CONFIG + configOrdinal).orElse(new int[6]);

                for(var transmissionTypeOrdinal : sfConfig.sets.keySet()) {
                    var sides = sfConfig.sets.get(transmissionTypeOrdinal);

                    for(Direction d : sides) {
                        RelativeSide side = RelativeSide.fromDirections(machineFacing, d);
                        mekConfig[side.ordinal()] = transmissionTypeOrdinal;
                    }
                }
            }

            ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), currentConfig);
            access.setConfigurationData(input, player);
        }
    }
}
