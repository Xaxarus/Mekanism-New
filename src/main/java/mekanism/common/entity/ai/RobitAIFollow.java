package mekanism.common.entity.ai;

import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.PlayerEntity;

public class RobitAIFollow extends RobitAIBase {

    /**
     * The robit's owner.
     */
    private PlayerEntity theOwner;

    /**
     * The distance between the owner the robit must be at in order for the protocol to begin.
     */
    private final float maxDist;

    /**
     * The distance between the owner the robit must reach before it stops the protocol.
     */
    private final float minDist;

    public RobitAIFollow(EntityRobit entityRobit, float speed, float min, float max) {
        super(entityRobit, speed);
        minDist = min;
        maxDist = max;
    }

    @Override
    public boolean shouldExecute() {
        PlayerEntity player = theRobit.getOwner();
        if (player == null) {
            return false;
        } else if (!theRobit.world.func_230315_m_().equals(player.world.func_230315_m_())) {
            return false;
        } else if (!theRobit.getFollowing()) {
            //Still looks up at the player if on chargepad or not following
            theRobit.getLookController().setLookPositionWithEntity(player, 6.0F, theRobit.getVerticalFaceSpeed() / 10);
            return false;
        } else if (theRobit.getDistanceSq(player) < (minDist * minDist)) {
            return false;
        } else if (theRobit.getEnergyContainer().isEmpty()) {
            return false;
        }
        theOwner = player;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !thePathfinder.noPath() && theRobit.getDistanceSq(theOwner) > (maxDist * maxDist) && theRobit.getFollowing() && !theRobit.getEnergyContainer().isEmpty()
               && theOwner.world.func_230315_m_().equals(theRobit.world.func_230315_m_());
    }

    @Override
    public void resetTask() {
        theOwner = null;
        super.resetTask();
    }

    @Override
    public void tick() {
        if (theRobit.getFollowing()) {
            updateTask(theOwner);
        }
    }
}