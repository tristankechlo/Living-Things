package com.tristankechlo.livingthings.entities;

import com.tristankechlo.livingthings.LivingThings;
import com.tristankechlo.livingthings.config.LivingThingsConfig;
import com.tristankechlo.livingthings.entities.ai.AncientBlazeChargeUpGoal;
import com.tristankechlo.livingthings.init.ModItems;
import com.tristankechlo.livingthings.init.ModSounds;
import com.tristankechlo.livingthings.misc.ILexiconEntry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IChargeableMob;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT, _interface = IChargeableMob.class)
public class AncientBlazeEntity extends MonsterEntity implements IChargeableMob, IRangedAttackMob, ILexiconEntry {

	private static final DataParameter<Byte> SHOOTS = EntityDataManager.createKey(AncientBlazeEntity.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> INVULNERABLE_TIME = EntityDataManager.createKey(AncientBlazeEntity.class, DataSerializers.VARINT);
	private static final ResourceLocation LEXICON_ENTRY = new ResourceLocation(LivingThings.MOD_ID, "hostile_mobs/ancient_blaze");
	private final ServerBossInfo bossInfo = new ServerBossInfo(this.getDisplayName(), BossInfo.Color.YELLOW, BossInfo.Overlay.PROGRESS);

	public AncientBlazeEntity(EntityType<? extends AncientBlazeEntity> type, World world) {
		super(type, world);
		this.setPathPriority(PathNodeType.WATER, -1.0F);
		this.setPathPriority(PathNodeType.LAVA, 8.0F);
		this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
		this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
		this.experienceValue = 30;
	}

	public static AttributeModifierMap.MutableAttribute getAttributes() {
		return MobEntity.func_233666_p_()
				.createMutableAttribute(Attributes.MAX_HEALTH, LivingThingsConfig.ANCIENT_BLAZE.health.get())
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, LivingThingsConfig.ANCIENT_BLAZE.speed.get())
				.createMutableAttribute(Attributes.FOLLOW_RANGE, 48.0D)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, LivingThingsConfig.ANCIENT_BLAZE.damage.get());
	}

	@Override
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
		this.setInvulnerableTime(LivingThingsConfig.ANCIENT_BLAZE.chargingTime.get());
		return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new AncientBlazeChargeUpGoal(this));
		this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 30, 20.0F));
		this.goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 0.0F));
		this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(4, new LookRandomlyGoal(this));

		this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true, true));
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(INVULNERABLE_TIME, 0);
		this.dataManager.register(SHOOTS, (byte) 0);
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("ChargedTime", this.getInvulnerableTime());
		compound.putByte("Shoots", this.getShoots());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.setInvulnerableTime(compound.getInt("ChargedTime"));
		this.setShoots(compound.getByte("Shoots"));
		if (this.hasCustomName()) {
			this.bossInfo.setName(this.getDisplayName());
		}
	}

	@Override
	public void setCustomName(ITextComponent name) {
		super.setCustomName(name);
		this.bossInfo.setName(this.getDisplayName());
	}

	@Override
	public void livingTick() {
		// slow falling
		if (!this.onGround && this.getMotion().y < 0.0D) {
			this.setMotion(this.getMotion().mul(1.0D, 0.6D, 1.0D));
		}
		if (this.world.isRemote && this.getInvulnerableTime() == 0) {
			// burn sound
			if (this.rand.nextInt(24) == 0 && !this.isSilent()) {
				this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), ModSounds.ANCIENT_BLAZE_BURN.get(), this.getSoundCategory(), 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F, false);
			}
			// smoke particles
			for (int i = 0; i < 2; ++i) {
				this.world.addParticle(ParticleTypes.LARGE_SMOKE, this.getPosXRandom(0.5D), this.getPosYRandom(), this.getPosZRandom(0.5D), 0.0D, 0.0D, 0.0D);
			}
		}
		super.livingTick();
	}

	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
	}

	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
		super.dropSpecialItems(source, looting, recentlyHitIn);
		ItemEntity itementity = this.entityDropItem(ModItems.ANCIENT_HELMET.get());
		if (itementity != null) {
			itementity.setNoDespawn();
			itementity.isImmuneToFire();
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.ANCIENT_BLAZE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return ModSounds.ANCIENT_BLAZE_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.ANCIENT_BLAZE_DEATH.get();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		// dont get damaged while charging up
		if (this.getInvulnerableTime() > 0 && source != DamageSource.OUT_OF_WORLD) {
			return false;
			// catch large fireballs
		} else if (source.getImmediateSource() instanceof FireballEntity && source.getTrueSource() instanceof PlayerEntity) {
			int shoots = this.getShoots();
			if (shoots < LivingThingsConfig.ANCIENT_BLAZE.largeFireballAmount.get()) {
				this.setShoots((byte) (shoots + 1));
				return false;
			}
			return true;
			// random chance for arrows, tridents,.. to be blocked
		} else if (source instanceof IndirectEntityDamageSource) {
			return this.rand.nextInt(4) != 0 && super.attackEntityFrom(source, amount);
		} else {
			// normal damage handling
			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
		// don't attack if disabled in config
		boolean peaceful = (this.world.getDifficulty() == Difficulty.PEACEFUL);
		boolean ambientMode = LivingThingsConfig.GENERAL.ambientMode.get();
		if (peaceful || ambientMode || !LivingThingsConfig.ANCIENT_BLAZE.canAttack.get()) {
			return;
		}

		double d1 = target.getPosX() - this.getPosX();
		double d2 = target.getPosYHeight(0.5D) - this.getPosYHeight(0.5D);
		double d3 = target.getPosZ() - this.getPosZ();

		int shoots = this.getShoots();
		double chance = (double) LivingThingsConfig.ANCIENT_BLAZE.largeFireballChance.get() / 100.0D;

		if (this.rand.nextDouble() < chance && shoots > 0) {
			this.setShoots((byte) (shoots - 1));
			FireballEntity fireballentity = new FireballEntity(this.world, this, d1, d2, d3);
			fireballentity.setPosition(fireballentity.getPosX(), this.getPosYHeight(0.5D) + 0.5D, fireballentity.getPosZ());
			fireballentity.explosionPower = 1;
			this.world.addEntity(fireballentity);
		} else {
			SmallFireballEntity smallfireballentity = new SmallFireballEntity(this.world, this, d1, d2, d3);
			smallfireballentity.setPosition(smallfireballentity.getPosX(), this.getPosYHeight(0.5D) + 0.5D, smallfireballentity.getPosZ());
			this.world.addEntity(smallfireballentity);
		}
		if (!this.world.isRemote) {
			this.world.playSound(null, this.getPosition(), ModSounds.ANCIENT_BLAZE_SHOOT.get(), SoundCategory.HOSTILE, 2.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove(boolean keepData) {
		int amount = LivingThingsConfig.ANCIENT_BLAZE.blazeSpawnCount.get();
		if (!this.world.isRemote && amount >= 1 && this.getShouldBeDead() && !this.removed) {
			for (int i = 0; i < amount; i++) {
				BlazeEntity blaze = new BlazeEntity(EntityType.BLAZE, this.world);
				if (this.isNoDespawnRequired()) {
					blaze.enablePersistence();
				}
				blaze.setCustomName(this.getCustomName());
				blaze.setNoAI(this.isAIDisabled());
				blaze.setInvulnerable(this.isInvulnerable());
				blaze.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), this.rand.nextFloat() * 360.0F, 0.0F);
				this.world.addEntity(blaze);
			}
		}
		super.remove(keepData);
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	protected boolean isDespawnPeaceful() {
		return LivingThingsConfig.ANCIENT_BLAZE.peacefulDespawn.get() && super.isDespawnPeaceful();
	}

	@Override
	public boolean isWaterSensitive() {
		return true;
	}

	@Override
	public boolean onLivingFall(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
	}

	@Override
	public boolean isOnLadder() {
		return false;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public void addTrackingPlayer(ServerPlayerEntity player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void removeTrackingPlayer(ServerPlayerEntity player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	public int getInvulnerableTime() {
		return this.dataManager.get(INVULNERABLE_TIME);
	}

	public void setInvulnerableTime(int time) {
		this.dataManager.set(INVULNERABLE_TIME, time);
	}

	public byte getShoots() {
		return this.dataManager.get(SHOOTS);
	}

	public void setShoots(byte shoots) {
		this.dataManager.set(SHOOTS, shoots);
	}

	@Override
	public boolean isCharged() {
		return this.dataManager.get(INVULNERABLE_TIME) > 0;
	}

	@Override
	public ResourceLocation getLexiconEntry() {
		return LEXICON_ENTRY;
	}

}
