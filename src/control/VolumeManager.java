package control;

import gui.Gui_StreamRipStar;
import gui.VolumeControlGUI;

import java.util.Vector;
/**
 * This Class makes sure that all volume slider have the same volume and
 * Synchronize between it. All Volumeslider have to register for that function.
 * 
 * @author Johannes Putzke
 *
 */
public class VolumeManager
{
	private Vector<VolumeControlGUI> list = new Vector<VolumeControlGUI>(0,1);
	private Gui_StreamRipStar mainGui;
	
	public VolumeManager(Gui_StreamRipStar mainGui)
	{
		this.mainGui = mainGui;
	}
	
	/**
	 * Sets a new volume
	 * 
	 * @param newVolume: the new volume in percent
	 */
	public void changeVolume(VolumeControlGUI vc,int newVolume)
	{
		if(vc != null)
		{
			for(int i=0; i<list.capacity(); i++)
			{
				//only change the volume, if this slider does not changed the volume
				if(list.get(i) != vc)
				{
					list.get(i).setVolume(newVolume);
				}
				
				//finally set it in the sound system
				mainGui.setVolume(newVolume);
			}
		}
	}
	
	public void addVolumeControl(VolumeControlGUI vc)
	{
		if(vc != null)
		{
			//add to the vector to manager
			list.add(vc);
			//init it to the current value
			if(list.capacity() > 0)
			{
				vc.setVolume(list.get(0).getVolume());
			}
		}
	}
	
	public void deleteVolumeControl(VolumeControlGUI vc)
	{
		if(vc != null)
		{
			list.remove(vc);
			list.trimToSize();
		}
	}
	
	/**
	 * Get the actual volume in percent. If no volume control is present,
	 * the returnvalue is 50 percent. The returnvalue is between 0 and 100.
	 * 
	 * @return Volume in percent
	 */
	public double getVolume()
	{
		if(list.capacity() > 0)
		{
			return list.get(0).getVolume();
		} else {
			return 50;
		}
	}
}
