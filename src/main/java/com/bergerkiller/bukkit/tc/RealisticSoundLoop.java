package com.bergerkiller.bukkit.tc;

import java.util.HashMap;
import java.util.Random;

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
    private boolean pitchChange;
    public RealisticSoundLoop(MinecartMember<?> newMember, boolean b, String s, float... args) {
        soundTarget = s;
        member = newMember;
        delay_a = 0;
        delay_b = 0;
        pitchChange = b;
    	float stacked = 0f;
    	int repeat = 0;
    	for (float f : args) {
    		stacked += f;
    		nodes.put(repeat, stacked);
    		repeat++;
    	}
    }
	public void play(String sound, float volume) {
    	play(sound, volume, 1);
    }
    public void play(String sound, float volume, float pitch) {
    	member.getEntity().getWorld().playSound(member.getEntity().getLocation(), sound, volume, pitch);
    	for (Player p : member.getEntity().getWorld().getPlayers()) {
    		if (member.getEntity().getPassengers().contains(p)) {
        		p.playSound(member.getEntity().getLocation(), sound, volume * 10f, pitch);
    		} else if (!p.isInsideVehicle()) {
        		p.playSound(member.getEntity().getLocation(), sound, volume, pitch);
    		}
    	}
    }
    /*public void play(Sound sound, float volume, float pitch) {
        member.getEntity().getWorld().playSound(member.getEntity().getLocation(), sound, volume, pitch);
    }*/
    public void stop() {
    	for (Player p : member.getWorld().getPlayers()) {
			if (!pitchChange) {
	    		for (int i : nodes.keySet()) {
	        		p.stopSound(soundTarget + ".motor" + i);
	        		p.stopSound(soundTarget + ".motor" + i + "r");
	    		}
			}
			p.stopSound(soundTarget + ".base");
    		//p.stopSound(Sound.ITEM_ELYTRA_FLYING);
    	}
    }
    public void onTick() {
        LCTManual handler = member.getGroup().lctManual;
    	double speed = member.getGroup().getAverageForce();
    	double limit = (double) (member.getGroup().getProperties().getSpeedLimit() * (double) ((double)handler.notch / 4.0d));
		if (pitchChange) {
			delay_a++;
			float pitch = 1f;
			if (handler.notch >= 0) {
				pitch = 0.8f + ((float) handler.notch / 10f);
			} else if (handler.notch < 0) {
				pitch = 0.8f - ((float) handler.notch / 20f);
			}
			if (nodes.get(0) != null) {
				if (nodes.get(0) * 10 < delay_a) {
					play(soundTarget + ".motor0", pitch * 5, pitch);
	    			delay_a = 0;
				}
			}
		} else {
			if (speed == 0) {
				delay_a = 0;
			} else if (handler.notch > 0 && limit > speed + 0.01d) {
				delay_a++;
			} else if (handler.notch < 0 && delay_a > 0) {
				delay_a--;
			}
		}
		if (nodes.get(savedLevel) != null && !pitchChange) {
			//System.out.println(savedLevel + ", " + (nodes.get(savedLevel) * 20) + ", " + delay_a);
			if (nodes.get(savedLevel) * 20 < delay_a && handler.notch > 0) {
				if (speed != 0) {
    				play(soundTarget + ".motor" + savedLevel, 2);
				}
    			if (nodes.size() - 1 > savedLevel) {
        			savedLevel++;
    			}
			} else if (nodes.get(savedLevel) * 20 > delay_a && handler.notch < 0) {
				if (speed != 0) {
	    			play(soundTarget + ".motor" + savedLevel + "r", 2);
				}
    			if (savedLevel > 0) {
        			savedLevel--;
    			}
    		}
		}
		delay_b+=1;
	    if (delay_b > 20) {
	    	delay_b = 0;
	    	play(soundTarget + ".base", Math.min((float)speed, 1.0f));
    	}
    }
}
