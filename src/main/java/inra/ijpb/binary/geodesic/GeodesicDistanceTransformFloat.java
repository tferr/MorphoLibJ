package inra.ijpb.binary.geodesic;

import static java.lang.Math.min;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * Computation of Chamfer geodesic distances using floating point array for
 * storing result, and 3-by-3 chamfer masks.
 * 
 * @author David Legland
 * 
 */
public class GeodesicDistanceTransformFloat extends AlgoStub implements
		GeodesicDistanceTransform
{
	private final static int DEFAULT_MASK_LABEL = 255;

	float[] weights;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with non integer values. 
	 */
	boolean normalizeMap = true;
	
	int width;
	int height;

	ImageProcessor maskProc;

	int maskLabel = DEFAULT_MASK_LABEL;

	/** 
	 * The value assigned to result pixels that do not belong to the mask. 
	 * Default is Float.MAX_VALUE.
	 */
	float backgroundValue = Float.MAX_VALUE;
	
	float[][] array;

	boolean modif;

	public GeodesicDistanceTransformFloat(float[] weights)
	{
		this.weights = weights;
	}

	public GeodesicDistanceTransformFloat(float[] weights, boolean normalizeMap)
	{
		this.weights = weights;
		this.normalizeMap = normalizeMap;
	}

	/**
	 * @return the backgroundValue
	 */
	public float getBackgroundValue() 
	{
		return backgroundValue;
	}

	/**
	 * @param backgroundValue the backgroundValue to set
	 */
	public void setBackgroundValue(float backgroundValue) 
	{
		this.backgroundValue = backgroundValue;
	}

	/**
	 * Computes the geodesic distance function for each pixel in mask, using
	 * the given mask. Mask and marker should be ImageProcessor the same size 
	 * and containing float values.
	 * The function returns a new Float processor the same size as the input,
	 * with values greater or equal to zero. 
	 */
	@Override
	public FloatProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask) 
	{
		// size of image
		width = mask.getWidth();
		height = mask.getHeight();
		
		// update mask
		this.maskProc = mask;

		fireStatusChanged(this, "Initialization..."); 
		// create new empty image, and fill it with black
		FloatProcessor result = new FloatProcessor(width, height);
		result.setValue(0);
		result.fill();

		// initialize empty image with either 0 (foreground) or Inf (background)
		array = result.getFloatArray();
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++)
			{
				int val = marker.get(i, j) & 0x00ff;
				array[i][j] = val == 0 ? backgroundValue : 0;
			}
		}

		int iter = 0;
		do 
		{
			modif = false;

			// forward iteration
			fireStatusChanged(this, "Forward iteration " + iter);
			forwardIteration();

			// backward iteration
			fireStatusChanged(this, "Backward iteration " + iter); 
			backwardIteration();

			// Iterate while pixels have been modified
			iter++;
		} while (modif);

		// Normalize values by the first weight
		if (this.normalizeMap) 
		{
			fireStatusChanged(this, "Normalize map"); 
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					array[i][j] /= this.weights[0];
				}
			}
		}

		// Compute max value within the mask
		fireStatusChanged(this, "Normalize display"); 
		float maxVal = 0;
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++) 
			{
				if (maskProc.getPixel(i, j) != 0)
					maxVal = Math.max(maxVal, array[i][j]);
			}
		}

		// update and return resulting Image processor
		result.setFloatArray(array);
		result.setMinAndMax(0, maxVal);
		// Forces the display to non-inverted LUT
		if (result.isInvertedLut())
			result.invertLut();
		return result;
	}

	private void forwardIteration() 
	{
		// variables declaration
		float ortho;
		float diago;
		float newVal;

		// Process first line: consider only the pixel on the left
		for (int i = 1; i < width; i++) 
		{
			if (maskProc.getPixel(i, 0) != maskLabel)
				continue;
			ortho = array[i - 1][0];
			updateIfNeeded(i, 0, ortho + weights[0]);
		}

		// Process all other lines
		for (int j = 1; j < height; j++) 
		{
			fireProgressChanged(this, j, height); 
			// process first pixel of current line: consider pixels up and
			// upright
			if (maskProc.getPixel(0, j) == maskLabel)
			{
				ortho = array[0][j - 1];
				diago = array[1][j - 1];
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(0, j, newVal);
			}

			// Process pixels in the middle of the line
			for (int i = 1; i < width - 1; i++)
			{
				// process only pixels inside structure
				if (maskProc.getPixel(i, j) != maskLabel)
					continue;

				// minimum distance of neighbor pixels
				ortho = min(array[i - 1][j], array[i][j - 1]);
				diago = min(array[i - 1][j - 1], array[i + 1][j - 1]);

				// compute new distance of current pixel
				newVal = min(ortho + weights[0], diago + weights[1]);

				// modify current pixel if needed
				updateIfNeeded(i, j, newVal);
			}

			// process last pixel of current line: consider pixels left,
			// up-left, and up
			if (maskProc.getPixel(width - 1, j) == maskLabel) 
			{
				ortho = min(array[width - 2][j], array[width - 1][j - 1]);
				diago = array[width - 2][j - 1];
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(width - 1, j, newVal);
			}
		} // end of forward iteration
		
		fireProgressChanged(this, 1, 1); 
	}

	private void backwardIteration() 
	{
		// variables declaration
		float ortho;
		float diago;
		float newVal;

		// Process last line: consider only the pixel just after (on the right)
		for (int i = width - 2; i >= 0; i--) 
		{
			if (maskProc.getPixel(i, height - 1) != maskLabel)
				continue;

			ortho = array[i + 1][height - 1];
			updateIfNeeded(i, height - 1, ortho + weights[0]);
		}

		// Process regular lines
		for (int j = height - 2; j >= 0; j--)
		{
			fireProgressChanged(this, height-1-j, height); 

			// process last pixel of the current line: consider pixels
			// down and down-left
			if (maskProc.getPixel(width - 1, j) == maskLabel)
			{
				ortho = array[width - 1][j + 1];
				diago = array[width - 2][j + 1];
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(width - 1, j, newVal);
			}

			// Process pixels in the middle of the current line
			for (int i = width - 2; i > 0; i--) 
			{
				// process only pixels inside structure
				if (maskProc.getPixel(i, j) != maskLabel)
					continue;

				// minimum distance of neighbor pixels
				ortho = min(array[i + 1][j], array[i][j + 1]);
				diago = min(array[i - 1][j + 1], array[i + 1][j + 1]);

				// compute new distance of current pixel
				newVal = min(ortho + weights[0], diago + weights[1]);

				// modify current pixel if needed
				updateIfNeeded(i, j, newVal);
			}

			// process first pixel of current line: consider pixels right,
			// down-right and down
			if (maskProc.getPixel(0, j) == maskLabel)
			{
				// curVal = array[0][j];
				ortho = min(array[1][j], array[0][j + 1]);
				diago = array[1][j + 1];
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(0, j, newVal);
			}

		} // end of backward iteration
		
		fireProgressChanged(this, 1, 1); 
	}

	/**
	 * Update the pixel at position (i,j) with the value newVal. If newVal is
	 * greater or equal to current value at position (i,j), do nothing.
	 */
	private void updateIfNeeded(int i, int j, float newVal)
	{
		float value = array[i][j];
		if (newVal < value)
		{
			modif = true;
			array[i][j] = newVal;
		}
	}
}
