/**
 * 
 */
package inra.ijpb.data.image;

import ij.ImageStack;

/**
 * Access the data of a 3D image containing gray8 values stored as bytes.
 * 
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	ImageStack stack = IJ.getImage().getStack();
 *	Image3D image = new ByteStackWrapper(stack);
 *	int val = image.get(0, 0, 0);
 *}</pre>
 * 
 * @see ShortStackWrapper
 * @see FloatStackWrapper
 * 
 * @author David Legland
 *
 */
public class ByteStackWrapper implements Image3D 
{
	byte[][] slices;
	
	int sizeX;
	int sizeY;
	int sizeZ;
	
	public ByteStackWrapper(ImageStack stack) 
	{
		// Check type
		if (stack.getBitDepth() != 8) 
		{
			throw new IllegalArgumentException("Requires a 8-bits stack");
		}
		
		// store stack size
		this.sizeX = stack.getWidth();
		this.sizeY = stack.getHeight();
		this.sizeZ = stack.getSize();

		// Convert slices type
		this.slices = new byte[sizeZ][];
		Object[] array = stack.getImageArray();
		for (int i = 0; i < sizeZ; i++) 
		{
			slices[i] = (byte[]) array[i];
		}
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#get(int, int, int)
	 */
	@Override
	public int get(int x, int y, int z) 
	{
		return slices[z][y * sizeX + x] & 0x00FF;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#set(int, int, int, int)
	 */
	@Override
	public void set(int x, int y, int z, int value)
	{
		if (value > 255)
			value = 255;
		else if (value < 0)
			value = 0;
		slices[z][y * sizeX + x] = (byte) value;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#getValue(int, int, int)
	 */
	@Override
	public double getValue(int x, int y, int z) 
	{
		return (double) (slices[z][y * sizeX + x] & 0x00FF);
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#setValue(int, int, int, double)
	 */
	@Override
	public void setValue(int x, int y, int z, double value)
	{
		if (value > 255)
			value = 255;
		else if (value < 0)
			value = 0;
		slices[z][y * sizeX + x] = (byte) (value + .5);
	}

}
