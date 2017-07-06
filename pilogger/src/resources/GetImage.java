package resources;

import javax.swing.ImageIcon;

public class GetImage {
	public static final String ARROW_UP = "arrow_max.png";
	public static final String ARROW_DOWN = "arrow_min.png";
	
	public static ImageIcon getImageIcon(String filename) {
		return new ImageIcon( GetImage.class.getResource(filename) ); 
	}

}
