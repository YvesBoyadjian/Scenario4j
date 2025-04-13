/**
 * 
 */
package retroboss.game.retroboss;

import retroboss.game.MRIGame;

/**
 * 
 */
public class RetroBossGame implements MRIGame {

	@Override
	public String getTitle() {
		return "Retro Boss";
	}

	@Override
	public float[] getStartingPosition() {
		return new float[] { 2532f, -69.5f, 933f };
	}

	public String toString() {
		return toLabel();
	}
}
