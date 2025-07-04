/**
 * 
 */
package retroboss.game.hikingisland;

import retroboss.game.MRIGame;

/**
 * 
 */
public class HikingIslandGame implements MRIGame {	

	@Override
	public String getTitle() {
		return "Bigfoot Hunting";
	}

	@Override
	public float[] getStartingPosition() {
		return new float[] {2947.3f, -5516.95f, 1056.32f};
	}

	public String toString() {
		return toLabel();
	}

}
