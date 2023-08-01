/**
 * 
 */
package application.terrain;

import java.awt.image.Raster;

import application.RasterProvider;
import loader.TerrainLoader;

/**
 * 
 */
public class IslandLoader {

	public final static RasterProvider loadWest() {

		TerrainLoader l = new TerrainLoader();
		RasterProvider rw = new RasterProvider() {
			@Override
			public Raster provide() {
				return l.load("ned19_n47x00_w122x00_wa_mounttrainier_2008/ned19_n47x00_w122x00_wa_mounttrainier_2008.tif");
			}
		};
		
		return rw;
	}
	
	public final static RasterProvider loadEast() {
		
		TerrainLoader l = new TerrainLoader();
		RasterProvider re = new RasterProvider() {
			@Override
			public Raster provide() {
				return l.load("ned19_n47x00_w121x75_wa_mounttrainier_2008/ned19_n47x00_w121x75_wa_mounttrainier_2008.tif");
			}
		};
		
		return re;
	}
}
