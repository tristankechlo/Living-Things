package com.tristankechlo.livingthings.entities.ai;

import java.util.EnumSet;

import com.tristankechlo.livingthings.config.LivingThingsConfig;
import com.tristankechlo.livingthings.entities.AncientBlazeEntity;
import com.tristankechlo.livingthings.init.ModSounds;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.SoundCategory;

public class AncientBlazeChargeUpGoal extends Goal {

	private final AncientBlazeEntity blaze;

	public AncientBlazeChargeUpGoal(AncientBlazeEntity entity) {
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
		this.blaze = entity;
	}

	@Override
	public boolean shouldExecute() {
		return this.blaze.getInvulnerableTime() > 0;
	}

	@Override
	public void tick() {
		int chargedtime = this.blaze.getInvulnerableTime();
		int targetShoots = LivingThingsConfig.ANCIENT_BLAZE.largeFireballAmount.get();
		if (chargedtime > 0) {
			chargedtime--;
			int divider = LivingThingsConfig.ANCIENT_BLAZE.chargingTime.get() / targetShoots;
			if (chargedtime % divider < 1 && this.blaze.getShoots() < targetShoots) {
				this.blaze.setShoots((byte) (this.blaze.getShoots() + 1));
				if (!this.blaze.world.isRemote) {
					this.blaze.world.playSound(null, this.blaze.getPosition(), ModSounds.ANCIENT_BLAZE_CHARGE_UP.get(),
							SoundCategory.HOSTILE, 1.0F, 1.0F);
				}
			}
		}
		if (chargedtime == 0) {
			this.blaze.setHealth(this.blaze.getMaxHealth());
			this.blaze.setShoots((byte) targetShoots);

			if (!this.blaze.world.isRemote) {
				this.blaze.world.playSound(null, this.blaze.getPosition(), ModSounds.ANCIENT_BLAZE_SPAWN.get(),
						SoundCategory.HOSTILE, 1.0F, 1.0F);
			}

			for (int i = 0; i < 4; i++) {
				double accelX = Math.pow(-1, i) * 90;
				double accelZ = (i < 2) ? 90 : -90;
				SmallFireballEntity smallfireballentity = new SmallFireballEntity(this.blaze.world, this.blaze, accelX,
						-15D, accelZ);
				smallfireballentity.setPosition(smallfireballentity.getPosX(), this.blaze.getPosYHeight(0.5D),
						smallfireballentity.getPosZ());
				this.blaze.world.addEntity(smallfireballentity);
			}

			for (int i = 0; i < 4; i++) {
				double accelX = (i > 1) ? Math.pow(-1, i) * 90 : 0;
				double accelZ = (i < 2) ? Math.pow(-1, i) * 90 : 0;
				FireballEntity smallfireballentity = new FireballEntity(this.blaze.world, this.blaze, accelX, -15D,
						accelZ);
				smallfireballentity.setPosition(smallfireballentity.getPosX(), this.blaze.getPosYHeight(0.5D) + 0.5D,
						smallfireballentity.getPosZ());
				this.blaze.world.addEntity(smallfireballentity);
			}
		}
		this.blaze.setInvulnerableTime(chargedtime);
	}
}