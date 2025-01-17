package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.watershed.Watershed;


/**
 * 
 * A plugin to perform marker-controlled watershed on a 2D or 3D image.
 * 
 * Reference: Fernand Meyer and Serge Beucher. "Morphological segmentation." 
 * Journal of visual communication and image representation 1.1 (1990): 21-46.
 *
 * @author Ignacio Arganda-Carreras
 */
public class MarkerControlledWatershed3DPlugin implements PlugIn 
{
	/** flag to calculate watershed dams */
	public static boolean getDams = true;
	/** flag to use 26-connectivity */
	public static boolean use26neighbors = true;
		
	/**
	 * Apply marker-controlled watershed to a grayscale 2D or 3D image.
	 *	 
	 * @param input grayscale 2D or 3D image (in principle a "gradient" image)
	 * @param marker the labeled marker image
	 * @param mask binary mask to restrict region of interest
	 * @param connectivity 6 or 26 voxel connectivity
	 * @return the resulting watershed
	 */
	public ImagePlus process(
			ImagePlus input, 
			ImagePlus marker,
			ImagePlus mask,
			int connectivity ) 
	{
		final long start = System.currentTimeMillis();
						
		IJ.log("-> Running watershed...");
								
		ImagePlus resultImage = Watershed.computeWatershed(input, marker, mask, connectivity, getDams );				
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-start) + " ms.");		
						
		return resultImage;				
	}
	

	/**
	 * Plugin run method to be called from ImageJ
	 */
	@Override
	public void run(String arg) 
	{
		int nbima = WindowManager.getImageCount();
		
		if( nbima < 2 )
		{
			IJ.error( "Marker-controlled Watershed", 
					"ERROR: At least two images need to be open to run Marker-controlled Watershed.");
			return;
		}
		
        String[] names = new String[ nbima ];
        String[] namesMask = new String[ nbima + 1 ];

        namesMask[ 0 ] = "None";
        
        for (int i = 0; i < nbima; i++) 
        {
            names[ i ] = WindowManager.getImage(i + 1).getShortTitle();
            namesMask[ i + 1 ] = WindowManager.getImage(i + 1).getShortTitle();
        }
        
        GenericDialog gd = new GenericDialog("Marker-controlled Watershed");

        int inputIndex = 0;
        int markerIndex = nbima > 1 ? 1 : 0;
        
        gd.addChoice( "Input", names, names[ inputIndex ] );
        gd.addChoice( "Marker", names, names[ markerIndex ] );
        gd.addChoice( "Mask", namesMask, namesMask[ nbima > 2 ? 3 : 0 ] );
        gd.addCheckbox( "Calculate dams", getDams );
        gd.addCheckbox( "Use diagonal connectivity", use26neighbors );

        gd.showDialog();
        
        if (gd.wasOKed()) 
        {
            inputIndex = gd.getNextChoiceIndex();
            markerIndex = gd.getNextChoiceIndex();
            int maskIndex = gd.getNextChoiceIndex();
            getDams = gd.getNextBoolean();
            use26neighbors = gd.getNextBoolean();

            ImagePlus inputImage = WindowManager.getImage( inputIndex + 1 );
            ImagePlus markerImage = WindowManager.getImage( markerIndex + 1 );
            ImagePlus maskImage = maskIndex > 0 ? WindowManager.getImage( maskIndex ) : null;
            
            // a 3D image is assumed but it will use 2D connectivity if the input is 2D
            final int connectivity = use26neighbors ? 26 : 6;
            
            ImagePlus result = process( inputImage, markerImage, maskImage, connectivity );
                                    
    		// Set result slice to the current slice in the input image
            result.setSlice( inputImage.getCurrentSlice() );
            
            // optimize display range
            Images3D.optimizeDisplayRange( result );
            
            // show result
            result.show();
        }
		
	}

}

