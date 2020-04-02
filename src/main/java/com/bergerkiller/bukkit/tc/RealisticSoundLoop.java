package com.bergerkiller.bukkit.tc;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;

/**
 * Keeps track of the current sound to
 */
public class RealisticSoundLoop {
    private int savedLevel = 0;
    private int delay_a;
    private int delay_b;
    private int delay_c;
    private String soundTarget = "";
    private static HashMap<Integer, Float> nodes = new HashMap<Integer, Float>();
    private MinecartMember<?> member;
    private boolean pitchChange;
    public RealisticSoundLoop(MinecartMember<?> newMember, boolean b, String s, float... args) {
        soundTarget = s;
        member = newMember;
        delay_a = 0;
        delay_b = Math.round(((float)member.getIndex() / (float)member.getGroup().size()) * 50f);
        delay_c = 0;
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
			p.stopSound(soundTarget + ".engine");
    		//p.stopSound(Sound.ITEM_ELYTRA_FLYING);
    	}
    }
    public void onTick() {
        LCTManual handler = member.getGroup().lctManual;
    	double speed = Math.min(member.getRealSpeed(), member.getGroup().getProperties().getSpeedLimit());
    	float movedDist = (float)speed * 3.125f;
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
					play(soundTarget + ".motor0", pitch * 2f, pitch);
	    			delay_a = 0;
				}
			}
		} else {
			if (speed == 0) {
				delay_a = 0;
			} else if (handler.notch > 0 && nodes.size() - 1 > savedLevel) {
				delay_a++;
        		//System.out.println("delay a increase");
			} else if (handler.notch < 0 && delay_a > 0) {
				delay_a--;
				//System.out.println("delay a decrease");
			} else {
				delay_b++;
				//System.out.println("delay b increase");
			}
		}
		if (nodes.get(savedLevel) != null && !pitchChange) {
			//System.out.println(savedLevel + ", " + (nodes.get(savedLevel) * 20) + ", " + delay_a);
			if (nodes.get(savedLevel) * 20 < delay_a && handler.notch > 0) {
    			if (nodes.size() - 1 > savedLevel) {
    				if (speed != 0) {
    					play(soundTarget + ".motor" + savedLevel, movedDist);
    				}
        			savedLevel++;
    			}
			} else if (nodes.get(savedLevel) * 20 > delay_a && handler.notch < 0) {
				if (speed != 0) {
					play(soundTarget + ".motor" + savedLevel + "r", movedDist);
				}
    			if (savedLevel > 0) {
        			savedLevel--;
    			}
    		}
		}
	    if (speed != 0) {
	    	if (delay_b > 50 / speed) {
		    	play(soundTarget + ".base", movedDist, (float)speed);
		    	delay_b = 0;
	    	}
    	}
		delay_c++;
    	if (speed != 0) {
    	    if (delay_c > 50 / speed) {
    	    	play(soundTarget + ".engine", movedDist, (float)speed);
    	    	delay_c = 0;
    	    }
	    	//System.out.println((float)speed);
    	} else {
    	    if (delay_c > 50) {
    	    	play(soundTarget + ".idle", 1f);
    	    	delay_c = 0;
    	    }
    	}
    }
}
