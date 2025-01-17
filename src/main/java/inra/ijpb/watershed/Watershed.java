package inra.ijpb.watershed;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Several static methods for computing watershed in 2D/3D images. 
 * @author Ignacio Arganda-Carreras
 */
public class Watershed 
{
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus mask,
			int connectivity )
	{
		WatershedTransform3D wt = new WatershedTransform3D( input, mask, connectivity );
		
		return wt.apply();		
	}
	
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param hMin the minimum value for dynamic
	 * @param hMax the maximum value for dynamic
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus mask,
			int connectivity,
			double hMin,
			double hMax )
	{
		if( connectivity == 6 || connectivity == 26 )
		{
			WatershedTransform3D wt = new WatershedTransform3D( input, mask, connectivity );
			return wt.apply( hMin, hMax );
		}
		else if( connectivity == 4 || connectivity == 8 )
		{
			WatershedTransform2D wt = 
					new WatershedTransform2D( input.getProcessor(), 
							null != mask ? mask.getProcessor() : null, connectivity );
			final ImageProcessor ip = wt.apply( hMin, hMax );
			if( null != ip )
			{
				String title = input.getTitle();
				String ext = "";
				int index = title.lastIndexOf( "." );
				if( index != -1 )
				{
					ext = title.substring( index );
					title = title.substring( 0, index );				
				}
				
				final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, ip );
				ws.setCalibration( input.getCalibration() );
				return ws;
			}
			else
				return null;
		}
		else
			return null;
	}
	
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack mask,
			int connectivity )
	{
		final ImagePlus inputIP = new ImagePlus( "input", input );		
		final ImagePlus binaryMaskIP = ( null != mask ) ? new ImagePlus( "binary mask", mask ) : null;
		WatershedTransform3D wt = new WatershedTransform3D( inputIP, binaryMaskIP, connectivity );
		
		final ImagePlus ws = wt.apply();
		if( null != ws )
			return ws.getImageStack();
		else 
			return null;
	}
	
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity pixel connectivity to define neighborhoods (4 or 8)
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor mask,
			int connectivity )
	{		
		WatershedTransform2D wt = new WatershedTransform2D( input, mask, connectivity );
		return wt.apply();
	}
	
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods (4 or 8 for 2D, 6 or 26 for 3D)
	 * @param usePriorityQueue select/deselect the use of the algorithm based on a priority queue
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method and we will stop using it.
	 */
	@Deprecated
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus marker,
			ImagePlus binaryMask,
			int connectivity,
			boolean usePriorityQueue,
			boolean getDams )
	{
		if( connectivity == 6 || connectivity == 26 )
		{
			MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( input, marker, binaryMask, connectivity );
			if( usePriorityQueue )
			{
				if( getDams )
					return wt.applyWithPriorityQueueAndDams();
				else 
					return wt.applyWithPriorityQueue();
			}
			else
			{
				if( getDams )
					return wt.applyWithSortedListAndDams();
				else
					return wt.applyWithSortedList();			
			}
		}
		else if( connectivity == 4 || connectivity == 8 )
		{
			MarkerControlledWatershedTransform2D wt = new MarkerControlledWatershedTransform2D( 
					input.getProcessor(), marker.getProcessor(), 
					null != binaryMask ? binaryMask.getProcessor() : null, connectivity );
			ImageProcessor ip;
			if( usePriorityQueue )
			{
				if( getDams )
					ip = wt.applyWithPriorityQueueAndDams();
				else 
					ip = wt.applyWithPriorityQueue();
			}
			else
			{
				if( getDams )
					ip = wt.applyWithSortedListAndDams();
				else
					ip = wt.applyWithSortedList();			
			}
			
			if( null != ip )
			{
				String title = input.getTitle();
				String ext = "";
				int index = title.lastIndexOf( "." );
				if( index != -1 )
				{
					ext = title.substring( index );
					title = title.substring( 0, index );				
				}
				
				final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, ip );
				ws.setCalibration( input.getCalibration() );
				return ws;
			}
			else
				return null;
		}
		else
			return null;
	}
	
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param usePriorityQueue select/deselect the use of the algorithm based on a priority queue
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method and we will stop using it.
	 */
	@Deprecated
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack marker,
			ImageStack binaryMask,
			int connectivity,
			boolean usePriorityQueue,
			boolean getDams )
	{		
				
		final ImagePlus inputIP = new ImagePlus( "input", input );
		final ImagePlus markerIP = new ImagePlus( "marker", marker );
		final ImagePlus binaryMaskIP = ( null != binaryMask ) ? new ImagePlus( "binary mask", binaryMask ) : null;

		ImagePlus ws = computeWatershed( inputIP, markerIP, binaryMaskIP, connectivity, usePriorityQueue, getDams );
		if ( null != ws )
			return ws.getImageStack();
		else 
			return null;
	}
	
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param usePriorityQueue select/deselect the use of the algorithm based on a priority queue
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method and we will stop using it.
	 */
	@Deprecated
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor marker,
			ImageProcessor binaryMask,
			int connectivity,
			boolean usePriorityQueue,
			boolean getDams )
	{															
		MarkerControlledWatershedTransform2D wt = new MarkerControlledWatershedTransform2D( input, marker, binaryMask, connectivity );
		if( usePriorityQueue )
		{
			if( getDams )
				return wt.applyWithPriorityQueueAndDams();
			else 
				return wt.applyWithPriorityQueue();
		}
		else
		{
			if( getDams )
				return wt.applyWithSortedListAndDams();
			else
				return wt.applyWithSortedList();				
		}
	}

	/**
	 * Compute watershed with markers
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param usePriorityQueue select/deselect the use of the algorithm based on a priority queue
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method and we will stop using it.
	 */
	@Deprecated
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus marker,
			int connectivity,
			boolean usePriorityQueue,
			boolean getDams )
	{
		MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( input, marker, null, connectivity );
		if( usePriorityQueue )
		{
			if( getDams )
				return wt.applyWithPriorityQueueAndDams();
			else 
				return wt.applyWithPriorityQueue();
		}
		else
		{
			if( getDams )
				return wt.applyWithSortedListAndDams();
			else
				return wt.applyWithSortedList();			
		}
	}
	
	/**
	 * Compute watershed with markers
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param usePriorityQueue select/deselect the use of the algorithm based on a priority queue
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method and we will stop using it.
	 */
	@Deprecated
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack marker,
			int connectivity,
			boolean usePriorityQueue,
			boolean getDams )
	{		
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		final ImagePlus inputIP = new ImagePlus( "input", input );
		final ImagePlus markerIP = new ImagePlus( "marker", marker );	
		
		MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( inputIP, markerIP, null, connectivity );
		
		ImagePlus ws = null;
		
		if( usePriorityQueue )
		{
			if( getDams )			
				ws = wt.applyWithPriorityQueueAndDams();							
			else			
				ws = wt.applyWithPriorityQueue();			
		}
		else
		{
			if( getDams )			
				ws = wt.applyWithSortedListAndDams();			
			else			
				ws = wt.applyWithSortedList();			
		}
		
		if( null == ws )
			return null;
		return ws.getImageStack();
	}
	
	/**
	 * Compute watershed with markers
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param usePriorityQueue select/deselect the use of the algorithm based on a priority queue
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method and we will stop using it.
	 */
	@Deprecated
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor marker,
			int connectivity,
			boolean usePriorityQueue,
			boolean getDams )
	{		
		final ImagePlus inputIP = new ImagePlus( "input", input );
		final ImagePlus markerIP = new ImagePlus( "marker", marker );
		
		final int conn3d = connectivity == 4 ? 6 : 26;
									
		MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( inputIP, markerIP, null, conn3d );
		
		ImagePlus ws = null;
		
		if( usePriorityQueue )
		{
			if( getDams )			
				ws = wt.applyWithPriorityQueueAndDams();							
			else			
				ws = wt.applyWithPriorityQueue();			
		}
		else
		{
			if( getDams )			
				ws = wt.applyWithSortedListAndDams();			
			else			
				ws = wt.applyWithSortedList();			
		}
		
		if( null == ws )
			return null;
		return ws.getProcessor();
	}

	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods (4 or 8 for 2D, 6 or 26 for 3D)
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus marker,
			ImagePlus binaryMask,
			int connectivity,
			boolean getDams )
	{
		if( connectivity == 6 || connectivity == 26 )
		{
			MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( input, marker, binaryMask, connectivity );
			if( getDams )
				return wt.applyWithPriorityQueueAndDams();
			else 
				return wt.applyWithPriorityQueue();
		}
		else if( connectivity == 4 || connectivity == 8 )
		{
			MarkerControlledWatershedTransform2D wt = new MarkerControlledWatershedTransform2D( 
					input.getProcessor(), marker.getProcessor(), 
					null != binaryMask ? binaryMask.getProcessor() : null, connectivity );
			ImageProcessor ip;
			if( getDams )
				ip = wt.applyWithPriorityQueueAndDams();
			else 
				ip = wt.applyWithPriorityQueue();
			
			if( null != ip )
			{
				String title = input.getTitle();
				String ext = "";
				int index = title.lastIndexOf( "." );
				if( index != -1 )
				{
					ext = title.substring( index );
					title = title.substring( 0, index );				
				}
				
				final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, ip );
				ws.setCalibration( input.getCalibration() );
				return ws;
			}
			else
				return null;
		}
		else
			return null;
	}
	
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack marker,
			ImageStack binaryMask,
			int connectivity,
			boolean getDams )
	{		
				
		final ImagePlus inputIP = new ImagePlus( "input", input );
		final ImagePlus markerIP = new ImagePlus( "marker", marker );
		final ImagePlus binaryMaskIP = ( null != binaryMask ) ? new ImagePlus( "binary mask", binaryMask ) : null;

		ImagePlus ws = computeWatershed( inputIP, markerIP, binaryMaskIP, connectivity, getDams );
		if ( null != ws )
			return ws.getImageStack();
		else 
			return null;
	}
	
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor marker,
			ImageProcessor binaryMask,
			int connectivity,
			boolean getDams )
	{															
		MarkerControlledWatershedTransform2D wt = new MarkerControlledWatershedTransform2D( input, marker, binaryMask, connectivity );
		if( getDams )
			return wt.applyWithPriorityQueueAndDams();
		else 
			return wt.applyWithPriorityQueue();
	}

	/**
	 * Compute watershed with markers
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus marker,
			int connectivity,
			boolean getDams )
	{
		MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( input, marker, null, connectivity );
		if( getDams )
			return wt.applyWithPriorityQueueAndDams();
		else 
			return wt.applyWithPriorityQueue();
	}
	
	/**
	 * Compute watershed with markers
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack marker,
			int connectivity,
			boolean getDams )
	{		
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		final ImagePlus inputIP = new ImagePlus( "input", input );
		final ImagePlus markerIP = new ImagePlus( "marker", marker );	
		
		MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( inputIP, markerIP, null, connectivity );
		
		ImagePlus ws = null;

		if( getDams )			
			ws = wt.applyWithPriorityQueueAndDams();							
		else			
			ws = wt.applyWithPriorityQueue();			
		
		if( null == ws )
			return null;
		return ws.getImageStack();
	}
	
	/**
	 * Compute watershed with markers
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor marker,
			int connectivity,
			boolean getDams )
	{												
		MarkerControlledWatershedTransform2D wt = 
				new MarkerControlledWatershedTransform2D( input, marker, 
														  null, connectivity );		
		if( getDams )			
			return wt.applyWithPriorityQueueAndDams();							
		else			
			return wt.applyWithPriorityQueue();			
	}
	
}
