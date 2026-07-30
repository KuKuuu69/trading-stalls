package net.kukuuuu.tradingstalls.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.kukuuuu.tradingstalls.block.entity.CashDrawerBlockEntity;
import net.kukuuuu.tradingstalls.screen.CashDrawerScreenHandler;
import net.kukuuuu.tradingstalls.screen.ShopScreenData;
import net.kukuuuu.tradingstalls.shop.ShopAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CashDrawerBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    // Base shape, built for Direction.NORTH. The 1-pixel inset is on the +Z (south) side.
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 14, 15),
            Block.box(0, 14, 0, 16, 16, 16)
    );
    private static final VoxelShape SHAPE_SOUTH = rotateShape(Direction.SOUTH);
    private static final VoxelShape SHAPE_EAST = rotateShape(Direction.EAST);
    private static final VoxelShape SHAPE_WEST = rotateShape(Direction.WEST);

    public CashDrawerBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getShape(state, world, pos, context);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CashDrawerBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(CashDrawerBlock::new);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide() && placer instanceof Player player
                && world.getBlockEntity(pos) instanceof CashDrawerBlockEntity drawer) {
            drawer.setOwner(player);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof CashDrawerBlockEntity drawer)) {
            return InteractionResult.PASS;
        }
        if (!drawer.hasOwner()) {
            drawer.setOwner(player);
        }
        if (!drawer.isOwner(player)) {
            player.displayClientMessage(Component.translatable("message.trading-stalls.not_owner"), true);
            return InteractionResult.CONSUME;
        }

        player.openMenu(new ExtendedScreenHandlerFactory<ShopScreenData>() {
            @Override
            public ShopScreenData getScreenOpeningData(ServerPlayer serverPlayer) {
                return new ShopScreenData(pos, false);
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("screen.trading-stalls.cash_drawer");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player menuPlayer) {
                return new CashDrawerScreenHandler(syncId, inventory, drawer);
            }
        });
        return InteractionResult.CONSUME;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CashDrawerBlockEntity drawer
                && !ShopAccess.canBreak(player, drawer)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, world, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof CashDrawerBlockEntity drawer) {
            Containers.dropContents(world, pos, drawer.getInventory());
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static VoxelShape rotateShape(Direction to) {
        VoxelShape[] buffer = new VoxelShape[]{SHAPE_NORTH, Shapes.empty()};
        int times = ((to.get2DDataValue() - Direction.NORTH.get2DDataValue()) % 4 + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(
                            1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }
}