package destiny.penumbra_phantasm.server.worldgen;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.server.block.ScarletLogMysteriousDoorBlock;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.FeatureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class ScarletMysteriousTrunkPlacer extends TrunkPlacer {
	public static final Codec<ScarletMysteriousTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("base_height").forGetter(tree -> tree.baseHeight),
			Codec.INT.fieldOf("height_rand_a").forGetter(tree -> tree.heightRandA),
			Codec.INT.fieldOf("height_rand_b").forGetter(tree -> tree.heightRandB)
	).apply(instance, ScarletMysteriousTrunkPlacer::new));

	public ScarletMysteriousTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
		super(baseHeight, heightRandA, heightRandB);
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return FeatureRegistry.SCARLET_MYSTERIOUS_TRUNK.get();
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
		List<FoliagePlacer.FoliageAttachment> foliageAttachment = Lists.newArrayList();
		Direction direction = Direction.from3DDataValue(randomSource.nextInt(2, 6));

		BlockState door = BlockRegistry.SCARLET_LOG_MYSTERIOUS_DOOR.get().defaultBlockState()
				.setValue(ScarletLogMysteriousDoorBlock.FACING, direction.getClockWise())
				.setValue(ScarletLogMysteriousDoorBlock.OPEN, false);
		biConsumer.accept(blockPos, door.setValue(ScarletLogMysteriousDoorBlock.HALF, DoubleBlockHalf.LOWER));
		biConsumer.accept(blockPos.above(), door.setValue(ScarletLogMysteriousDoorBlock.HALF, DoubleBlockHalf.UPPER));

		BlockPos trackPos = blockPos.above().relative(direction);
		this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()));
		trackPos = trackPos.above();
		this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
		trackPos = trackPos.above();
		this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
		trackPos = trackPos.above();
		this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
		trackPos = trackPos.above();
		foliageAttachment.add(new FoliagePlacer.FoliageAttachment(trackPos, 0, false));

		BlockPos branch = trackPos.below().below().relative(direction);
		this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()));
		branch = branch.relative(direction);
		this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()));
		branch = branch.above();
		this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration);
		branch = branch.above();
		this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration);

		BlockPos bushLower = trackPos.above().relative(direction).relative(direction);
		foliageAttachment.add(new FoliagePlacer.FoliageAttachment(bushLower, 0, false));

		Direction directionOpposite = direction.getOpposite();
		BlockPos bushUpper = trackPos.below().relative(directionOpposite).relative(directionOpposite).relative(directionOpposite.getClockWise());
		foliageAttachment.add(new FoliagePlacer.FoliageAttachment(bushUpper, 0, false));

		return foliageAttachment;
	}
}
