package com.bergerkiller.bukkit.tc.actions;

public class GroupActionSizzle extends GroupAction {

    @Override
    public void start() {
        this.getGroup().lctManual.stopSound();
        int j;
        for (int i = 0; i < this.getGroup().size(); i++) {
            j = i * 3;
            if (j < this.getGroup().size()) {
                this.getGroup().get(j).playLinkEffect(false);
            }
        }
    }
}
