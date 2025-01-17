/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import ij.IJ;
import inra.ijpb.algo.AlgoStub;

/**
 * <p>
 * Implementation basis for geodesic reconstruction algorithms for 3D images.
 * </p>
 * 
 * <p>
 * This class provides the management of the connectivity, several fields to
 * manage algorithm monitoring, and protected utility methods.
 * </p>
 * 
 * @author dlegland
 *
 */
public abstract class GeodesicReconstruction3DAlgoStub extends AlgoStub implements
		GeodesicReconstruction3DAlgo
{
	/**
	 * The connectivity of the algorithm, either 6 or 26.
	 */
	protected int connectivity = 6;
	
	/**
	 * Boolean flag for the display of debugging infos.
	 */
	public boolean verbose = false;
	
	/**
	 * Boolean flag for the display of algorithm state in ImageJ status bar
	 */
	public boolean showStatus = true;
	
	/**
	 * Boolean flag for the display of algorithm progress in ImageJ status bar
	 */
	public boolean showProgress = false; 

	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo#getConnectivity()
	 */
	@Override
	public int getConnectivity()
	{
		return this.connectivity;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo#setConnectivity(int)
	 */
	@Override
	public void setConnectivity(int conn)
	{
		this.connectivity = conn;
	}

	/**
	 * Displays the specified message in the status bar of the ImageJ frame, if
	 * the <code>showStatus</code> flag is true.
	 * 
	 * @param status
	 *            the message to display
	 */
	protected void showStatus(String status)
	{
		if (this.showStatus) 
		{
			IJ.showStatus(status);
		}
	}

	/**
	 * Displays the current progression of the algorithme in the status bar of
	 * the ImageJ frame, if the <code>showProgress</code> flag is true.
	 * 
	 * @param current
	 *            the current progression
	 * @param max
	 *            the maximum possible value for progression
	 */            
	protected void showProgress(double current, double max)
	{
		if (showProgress) 
		{
			IJ.showProgress(current / max);
		}
	}
	
	/**
	 * Displays the current progression of the algorithme in the status bar of
	 * the ImageJ frame, if the <code>showProgress</code> flag is true.
	 * 
	 * @param current
	 *            the current progression
	 * @param max
	 *            the maximum possible value for progression
	 * @param msg
	 *            an additional message that will be displayed in the console
	 */            
	protected void showProgress(double current, double max, String msg)
	{
		if (showProgress) 
		{
			IJ.showProgress(current / max);
			if (msg != null && !msg.isEmpty())
			{
				trace(msg);
			}
		}
	}

	/**
	 * Display a trace message in the console, if the <code>verbose</code> flag
	 * is true.
	 * 
	 * @param traceMessage
	 *            the message to display
	 */
	protected void trace(String traceMessage)
	{
		// Display current status
		if (verbose) 
		{
			System.out.println(traceMessage);
		}
	}
}
