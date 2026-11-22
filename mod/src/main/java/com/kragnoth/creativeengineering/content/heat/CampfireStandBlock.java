package com.kragnoth.creativeengineering.content.heat;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Campfire Stand (andesite / brass / copper): Create's metal scaffolding frame with a campfire inside, so whatever
 * needs heat (a Fermenting Vat, a Create boiler) sits flush on a full block instead of floating over a 7 px campfire.
 * Right-click with a campfire or soul campfire to fit one (it goes in lit); empty hand takes it back. Flint and
 * steel / fire charge light it, a shovel or water bottle douses it. While lit it is passive heat in Create's
 * BoilerHeater registry (vat x1, boilers as a campfire). Smoke vents out of the frame's sides.
 */
public class CampfireStandBlock extends Block implements IWrenchable {
    public enum Fire implements StringRepresentable {
        NONE, CAMPFIRE, SOUL;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<Fire> FIRE = EnumProperty.create("fire", Fire.class);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CampfireStandBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FIRE, Fire.NONE).setValue(LIT, false));
    }

    public static int light(BlockState s) {
        return !s.getValue(LIT) ? 0 : s.getValue(FIRE) == Fire.SOUL ? 10 : 15;
    }

    /** Create heater: passive while lit (registered in CreativeEngineering common setup). */
    public static final BoilerHeater HEATER = (level, pos, state) -> state.getValue(LIT) ? BoilerHeater.PASSIVE_HEAT : BoilerHeater.NO_HEAT;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FIRE, LIT);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        Fire fire = state.getValue(FIRE);
        if (fire == Fire.NONE && (stack.is(Items.CAMPFIRE) || stack.is(Items.SOUL_CAMPFIRE))) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(FIRE, stack.is(Items.SOUL_CAMPFIRE) ? Fire.SOUL : Fire.CAMPFIRE).setValue(LIT, true));
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1F, 0.9F);
                stack.consume(1, player);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (fire != Fire.NONE && !state.getValue(LIT) && (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE))) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(LIT, true));
                if (stack.is(Items.FIRE_CHARGE)) {
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1F, 1F);
                    stack.consume(1, player);
                } else {
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1F, 1F);
                    stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        boolean water = stack.is(Items.POTION) && stack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                net.minecraft.world.item.alchemy.PotionContents.EMPTY).is(net.minecraft.world.item.alchemy.Potions.WATER);
        if (state.getValue(LIT) && (stack.canPerformAction(ItemAbilities.SHOVEL_DOUSE) || water)) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(LIT, false));
                level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
                if (water && !player.isCreative()) {
                    player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
                } else if (!water) {
                    stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            } else {
                for (int i = 0; i < 12; i++) {
                    level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5 + level.random.nextGaussian() * 0.2, pos.getY() + 0.3,
                            pos.getZ() + 0.5 + level.random.nextGaussian() * 0.2, 0, 0.05, 0);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        Fire fire = state.getValue(FIRE);
        if (fire == Fire.NONE) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, state.setValue(FIRE, Fire.NONE).setValue(LIT, false));
            player.getInventory().placeItemBackInInventory(new ItemStack(fire == Fire.SOUL ? Items.SOUL_CAMPFIRE : Items.CAMPFIRE));
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 1F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5, y = pos.getY() + 0.35, z = pos.getZ() + 0.5;
        if (random.nextInt(10) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.6F, false);
        }
        if (random.nextInt(5) == 0) {   // embers inside the frame
            level.addParticle(ParticleTypes.LAVA, x + random.nextGaussian() * 0.15, y, z + random.nextGaussian() * 0.15, 0, 0, 0);
        }
        if (random.nextInt(3) == 0) {   // smoke vents out of an open side, drifting up
            Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            if (!level.getBlockState(pos.relative(d)).isSolidRender(level, pos.relative(d))) {
                level.addParticle(ParticleTypes.SMOKE, x + d.getStepX() * 0.52, pos.getY() + 0.6 + random.nextFloat() * 0.3, z + d.getStepZ() * 0.52,
                        d.getStepX() * 0.01, 0.03, d.getStepZ() * 0.01);
            }
        }
    }
}
