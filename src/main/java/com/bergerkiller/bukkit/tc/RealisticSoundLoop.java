package com.bergerkiller.bukkit.tc;

import java.util.HashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;

/**
 * Keeps track of the current sound to
 */
public class RealisticSoundLoop {
    private int savedLevel = 0;
    private int delay_a;
    private int delay_b;
    private String soundTarget = "";
    private static HashMap<Integer, Float> nodes = new HashMap<Integer, Float>();
    private MinecartMember<?> member;
    public RealisticSoundLoop(MinecartMember<?> newMember, String s, float... args) {
        soundTarget = s;
        member = newMember;
        delay_a = 0;
        delay_b = 0;
    	float stacked = 0f;
    	int repeat = 0;
    	for (float f : args) {
    		stacked += (f - 0.5f);
    		nodes.put(repeat, stacked);
    		repeat++;
    	}
    }
	public void play(String sound, float volume) {
    	play(sound, volume, 1);
    }
    public void play(String sound, float volume, float pitch) {
    	member.getEntity().getWorld().playSound(member.getEntity().getLocation(), sound, volume, pitch);
    }
    public void play(Sound sound, float volume, float pitch) {
        member.getEntity().getWorld().playSound(member.getEntity().getLocation(), sound, volume, pitch);
    }
    public void stop() {
    	for (Player p : member.getWorld().getPlayers()) {
    		for (int i : nodes.keySet()) {
        		p.stopSound(soundTarget + ".motor" + i);
        		p.stopSound(soundTarget + ".motor" + i + "r");
    		}
    		p.stopSound("entity.minecart.riding");
    		p.stopSound(Sound.ITEM_ELYTRA_FLYING);
    	}
    }
    public void onTick() {
        LCTManual handler = member.getGroup().lctManual;
    	double speed = member.getGroup().getAverageForce();
    	double limit = (double) (member.getGroup().getProperties().getSpeedLimit() * (double) ((double)handler.notch / 4.0d));
		if (speed == 0) {
			delay_a = 0;
		} else if (handler.notch > 0 && limit > speed + 0.01d) {
			delay_a++;
		} else if (handler.notch < 0 && delay_a > 0) {
			delay_a--;
		}
		if (nodes.get(savedLevel) != null) {
			//System.out.println(savedLevel + ", " + (nodes.get(savedLevel) * 20) + ", " + delay_a);
			if (nodes.get(savedLevel) * 20 < delay_a && handler.notch > 0) {
				if (speed != 0) {
    				play(soundTarget + ".motor" + savedLevel, 3);
				}
    			if (nodes.size() - 1 > savedLevel) {
        			savedLevel++;
    			}
			} else if (nodes.get(savedLevel) * 20 > delay_a && handler.notch < 0) {
				if (speed != 0) {
	    			play(soundTarget + ".motor" + savedLevel + "r", 3);
				}
    			if (savedLevel > 0) {
        			savedLevel--;
    			}
    		}
		}
		delay_b++;
    	double avf = Math.min(2.0d, speed);
    	if (avf != 0) {
	    	if (delay_b > 30d / Math.max(0.5d, avf)) {
	    		delay_b = 0;
	    		play("entity.minecart.riding", 4f * (float) avf, (float) avf);
	    		play(Sound.ITEM_ELYTRA_FLYING, (float) avf, (float) avf);
	    	}
    	}
    }
}
