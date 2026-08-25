package destiny.penumbra_phantasm.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class UnbreakableDarknessBlock extends Block {
	public UnbreakableDarknessBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
		return adjacentState.getBlock() instanceof UnbreakableDarknessBlock || super.skipRendering(state, adjacentState, direction);
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
		return 15;
	}
}
