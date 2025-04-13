/**
 * 
 */
package retroboss.game.bigfoothunting;

import retroboss.game.MRIGame;

/**
 * 
 */
public class BigfootHuntingGame implements MRIGame {

	@Override
	public String getTitle() {
		return "Bigfoot Hunting, an Adventure Game";
	}

	@Override
	public float[] getStartingPosition() {
		return new float[] { 260f, 294f, 1255.5f };
	}

	public String toString() {
		return toLabel();
	}

}
