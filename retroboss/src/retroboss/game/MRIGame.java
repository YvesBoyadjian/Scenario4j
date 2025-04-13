/**
 * 
 */
package retroboss.game;

/**
 * 
 */
public interface MRIGame {

	/**
	 * Game title
	 * @return
	 */
	String getTitle();
	
	/**
	 * Game starting position
	 */
	float[] getStartingPosition();

	/**
	 * Label
	 * @return
	 */
	default String toLabel() {
		float[] startingPosition = getStartingPosition();
		return getTitle() + " { "+ startingPosition[0] + ", "+ startingPosition[1] + ", "+startingPosition[2] + " }";
	}
}
