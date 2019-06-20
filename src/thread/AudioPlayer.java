package thread;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import misc.Stream;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.*;
import org.freedesktop.gstreamer.GstObject;
import control.SRSOutput;

import gui.Gui_StreamRipStar;

/**
 * Starts an instance for playing an stream with gstreamer. 
 * @author eule
 *
 */
public class AudioPlayer extends Thread{
	
	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
	private Stream stream;
	private Gui_StreamRipStar mainGui;
	private PlayBin playbin;
	private SRSOutput lg = SRSOutput.getInstance();
	
	/**
	 * Use this constructor if you want to play a stream
	 * @param stream The stream you want to hear
	 * @param mainGui Mainwindow
	 */
	public AudioPlayer(Stream stream, Gui_StreamRipStar mainGui)
	{
		this.stream = stream;
		this.mainGui = mainGui;
		try {
			playbin = new PlayBin("AudioPlayer");
			//set the valume for this new player to avoid a wrong gain at start
			playbin.setVolume(mainGui.getVolumeManager().getVolume()/100);
			lg.logD("AudioPlayer: Player created");
		}
		//if we get here an exception, we should disable the internal audio player with gstreamer
		catch(java.lang.UnsatisfiedLinkError e) {
			mainGui.showErrorMessageInPopUp(trans.getString("audioplayer.noGstreamerInstalled")
					+"\n\n"+e.getMessage());
			lg.logE("AudioPlayer: No gstreamer on System installed\n"+e.getMessage());
			mainGui.setUseInternalAudioPlayer(false);
		}
	}
	
	/**
	 * Use this constructor, if you wan't to initialize the audio system
	 *  @param mainGui Mainwindow
	 */
	public AudioPlayer(Gui_StreamRipStar mainGui )
	{
		try {
			Gst.init();
		}
		//if we get here an exception, we should disable the internal audio player with gstreamer
		catch(java.lang.UnsatisfiedLinkError e) {
			mainGui.showErrorMessageInPopUp(trans.getString("audioplayer.noGstreamerInstalled")
					+"\n\n"+e.getMessage());
			lg.logE("AudioPlayer: No gstreamer on System installed\n"+e.getMessage());
			mainGui.setUseInternalAudioPlayer(false);
		}
	}
	
	public void run() {
		
		//say, we are loading the stream
		if (mainGui != null) {
			mainGui.showMessageInTray(trans.getString("audioplayer.loadingStream"));
		}
		
		try {

	        try {
	        	//test if we should connect to the realy stream or to the internet
	        	if(stream.getStatus() && stream.connectToRelayCB) {
	        		playbin.setURI(new URI("http://127.0.0.1:"+stream.relayServerPortTF));
	        	} else {
	        		playbin.setURI(new URI(stream.address));
	        		
	        	}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			//try to catch the AUDIO - TAGS from the stream
			playbin.getBus().connect(new Bus.TAG() {
	
	            public void tagsFound(GstObject source, TagList tagList) {
	                for (String tagName : tagList.getTagNames()) {
                		for (Object tagData : tagList.getValues(tagName)) {
//	                    	if(tagList.equals("title"))
//		                	{
//	                    		mainGui.setTitleForAudioPlayer(stream.name ,tagData.toString(),false);
//		                	}
	                    	mainGui.setTitleForAudioPlayer(stream.name ,tagData.toString(),false);
	                    }
	                }
	            }
	        });
			
			playbin.getBus().connect(new Bus.ERROR() {
				@Override
				public void errorMessage(GstObject source, int errorCode, String errorMessage) {
//					//Cannot resolve hostname - no connection to the stream
					if(errorCode == 1) {
						SRSOutput.getInstance().log(errorMessage);
					}
					
					//show all other error messages in the tray
					else {
						mainGui.setTitleForAudioPlayer("",errorMessage,false);
					}
					
					lg.logD("The Errorcode was:"+errorCode);
					lg.log("The Errormessage was:"+errorMessage);
				}
			});

			playbin.getBus().connect(new Bus.BUFFERING()
			{
				@Override
				public void bufferingData(GstObject arg0, int arg1)
				{
					mainGui.showMessageInTray("Buffering... ("+arg1+")");
				}
			});

			lg.logD("AudioPlayer: Loading stream");
	        playbin.setState(org.freedesktop.gstreamer.State.PLAYING);
	        lg.logD("AudioPlayer: Start Playing");
	        Gst.main();

		} catch (java.lang.UnsatisfiedLinkError e) {
			mainGui.showErrorMessageInPopUp(trans.getString("audioplayer.noGstreamerInstalled")+"\n"+e.getMessage());
			lg.logD("AudioPlayer: UnsatisfiedLinkError\n"+e.getMessage());
		} catch (ExceptionInInitializerError e) {
			mainGui.showErrorMessageInPopUp(trans.getString("audioplayer.noGstreamerInstalled")+"\n"+e.getMessage());
			lg.logD("AudioPlayer: ExceptionInInitializerError\n"+e.getMessage());
		} catch (IllegalArgumentException e) {
			mainGui.showErrorMessageInPopUp(trans.getString("audioplayer.noGstreamerInstalled")+"\n"+e.getMessage());
			lg.logD("AudioPlayer: IllegalArgumentException\n"+e.getMessage());
		} catch (NoClassDefFoundError e) {
			mainGui.showErrorMessageInPopUp(trans.getString("audioplayer.noGstreamerInstalled")+"\n"+e.getMessage());
			lg.logD("AudioPlayer: NoClassDefFoundError\n"+e.getMessage());
		}
	}
	
	/**
	 * Stop the playback of the thread with the player. It set the new 
	 * status message
	 */
	public void stopPlaying()
	{
		if(stream != null)
		{
			lg.logD("AudioPlayer: Try to stop the audio player with the stream: "+stream.name);
		}
		
		if(playbin != null && playbin.getState(200) != org.freedesktop.gstreamer.State.NULL)
		{
			while(playbin.getState(200) != org.freedesktop.gstreamer.State.NULL) {
				playbin.setState(org.freedesktop.gstreamer.State.NULL);
			}
			mainGui.showMessageInTray("");
		}
		
		if(stream != null)
		{
			lg.logD("AudioPlayer: Audio Player with stream: "+stream.name + "has been stopped");
		}
	}
	
	/**
	 * Set a now volume for the audio player
	 * @param volumePercent The new volume in percent
	 */
	public void setAudioVolum(double volumePercent)
	{
		if(playbin != null)
		{
			lg.logD("AudioPlayer: set new volume to "+volumePercent);
			playbin.setVolume(volumePercent/100);
		}
	}
}
