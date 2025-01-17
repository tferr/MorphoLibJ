package inra.ijpb.data.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * A collection of static methods for working on 3D images. 
 * 
 * @author David Legland
 *
 */
public class Images3D 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private Images3D()
	{
	}

	/**
	 * Converts the input ImageStack into an instance of Image3D, depending on
	 * the data type stored in the stack.
	 * 
	 * @param stack
	 *            the input ImageStack to convert
	 * @return a new instance of Image3D that can be used to access or modify
	 *         values in original stack
	 */
	public final static Image3D createWrapper(ImageStack stack) {
		switch(stack.getBitDepth()) {
		case 8:
			return new ByteStackWrapper(stack);
		case 16:
			return new ShortStackWrapper(stack);
		case 32:
			return new FloatStackWrapper(stack);
		default:
			throw new IllegalArgumentException(
					"Can not manage image stacks with bit depth "
							+ stack.getBitDepth());
		}
	}
	
	/**
	 * Find minimum and maximum value of input image
	 * 
	 * @param image input 2d/3d image
	 * @return array of 2 extreme values
	 */
	public static final double[] findMinAndMax( ImagePlus image )
	{
		// Adjust min and max values to display
		double min = 0;
		double max = 0;
		for( int slice=1; slice<=image.getImageStackSize(); slice++ )			
		{
			ImageProcessor ip = image.getImageStack().getProcessor(slice);
			ip.resetMinAndMax();
			if( max < ip.getMax() )
				max = ip.getMax();
			if( min > ip.getMin() )
				min = ip.getMin();
		}
		return new double[]{ min, max };				
	}
	
	/**
	 * Optimize display range of 2d/3d image based on its
	 * minimum and maximum values
	 * 
	 * @param image input image
	 */
	public static final void optimizeDisplayRange( ImagePlus image )
	{
		double[] extremeValue = findMinAndMax(image);
		image.setDisplayRange( extremeValue[ 0 ], extremeValue[ 1 ] );
		image.updateAndDraw();
	}
	
	/**
	 * Returns a new instance of ImageStack containing ByteProcessors such that
	 * display range is specified by vmin and vmax.
	 * 
	 * @param image
	 *            input image, that can be 8, 16 or 32 bits
	 * @param vmin
	 *            the value that will correspond to 0 in new image
	 * @param vmax
	 *            the value that will correspond to 255 in new image
	 * @return a new ImageStack of byte processors with adjusted dynamic
	 */
	public static final ImageStack adjustDynamic(ImageStack image, double vmin,
			double vmax)
	{
		// get image size
		int sizeX = image.getWidth(); 
		int sizeY = image.getHeight(); 
		int sizeZ = image.getSize();

		// create result image
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);

		// Iterate on image voxels, and choose result value depending on dynamic bounds
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// linearly interpolates new value
					double value = image.getVoxel(x, y, z);
					value = 255 * (value - vmin) / (vmax - vmin);
					value = Math.max(Math.min(value, 255), 0);
					result.setVoxel(x, y, z, value);
				}
			}
		}
		
		return result;
	}
	
	public static final void replaceValue(ImagePlus image, double initialValue, double finalValue) 
	{ 
		if (image.getStackSize() == 1) 
		{
			ImageProcessor img = image.getProcessor();
			for (int y = 0; y < img.getHeight(); y++)
			{
				for (int x = 0; x < img.getWidth(); x++)
				{
					if (img.getf(x, y) == initialValue) 
					{
						img.setf(x, y, (float) finalValue);
					}
				}
			}
		} 
		else 
		{
			ImageStack img = image.getStack();
			for (int z = 0; z < img.getSize(); z++)
			{
				for (int y = 0; y < img.getHeight(); y++)
				{
					for (int x = 0; x < img.getWidth(); x++)
					{
						if (img.getVoxel(x, y, z) == initialValue) 
						{
							img.setVoxel(x, y, z, finalValue);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Fills the input 3D image with the given value.
	 * 
	 * @param image the 3D image to process
	 * @param value the value used to fill input image
	 */ 
	public static final void fill(ImageStack image, double value) 
	{
		for (int z = 0; z < image.getSize(); z++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				for (int x = 0; x < image.getWidth(); x++)
				{
					image.setVoxel(x, y, z, value);
				}
			}
		}
	}
	
	/**
	 * Prints the content of the given 3D image on the console. This can be used
	 * for debugging (small) images.
	 * 
	 * @param image the 3D image to display on the console 
	 */
	public static final void print(ImageStack image) 
	{
		int nSlices = image.getSize();
		for (int z = 0; z < nSlices; z++)
		{
			System.out.println(String.format("slice %d/%d", z, nSlices - 1));
			for (int y = 0; y < image.getHeight(); y++)
			{
				for (int x = 0; x < image.getWidth(); x++)
				{
					System.out.print(String.format("%3d ", (int) image.getVoxel(x, y, z)));
				}
				System.out.println("");
			}
		}
	}
	
}
