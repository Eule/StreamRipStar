package misc;
/* This program is licensed under the terms of the GPLV3 or newer*/
/* Written by Johanes Putzke*/
/* eMail: die_eule@gmx.net*/  

import gui.Gui_StreamRipStar;

import java.util.Locale;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import control.Control_GetPath;
import control.SRSOutput;
/**
 * with this class you can start StreamRipStar
 */
public class StreamRipStar
{	
	public static final int releaseRevision = 613;
	public static final String releaseVersion = "0.6.10";
	private static boolean noConfiFileFound = false;
	private static String lang="",reg ="", lnfClassName = null;
	
	public static void main(String[] args) {
		start(args);
	}
	
	private static void start(String[] args)
	{
		//check the command line arguments
		Commandline cmd = new Commandline(args);
		
		//don't start if the command line arguments are printed
		if(!cmd.requestHelpMessage())
		{
			//get language from file
			// 0 = english
			// 1 = german
			// else = system
			Short langs = getPrefsFromFile();
			if(langs==0){
				lang="en";
			} else if (langs==1) {
				lang="de";
				reg ="DE";
			} else if (langs==2) {
				lang="system";
			}  else if (langs==3) {
				lang="fr";
				reg="FR";
			} else {
				lang="system";
			}
	
			setLookAndFeel();
			
			//if the settings file is empty use
			//default language and open preferences on startup
			if(lang==null || noConfiFileFound)
				new Gui_StreamRipStar(true,cmd);
			
			//if system is selected, use system language
			//and don't open settings at startup
			else if(lang.equals("system"))
				new Gui_StreamRipStar(false,cmd);
			
			//else set the selected language
			else {
				if(reg.equals(""))
					Locale.setDefault(new Locale(lang));
				else
					Locale.setDefault(new Locale(lang,reg));
				
				//and open StreamRipStar with new language
				new Gui_StreamRipStar(false,cmd);
			}
		}
	}
	
	/**
	 * try to set an new look and feel
	 * @param newlnfClassName: the class name of the new look and feel
	 */
	public static void setLookAndFeel() {
		if(lnfClassName != null) {
			try {
				UIManager.setLookAndFeel(lnfClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * looks with index the language select box in settings had.
	 * If no settings can be found or something else goes wrong,
	 * it returns a -1
	 * @return
	 */
	private static Short getPrefsFromFile() {
		String loadPath =  new Control_GetPath().getStreamRipStarPath();
		Short tmpShort = -1;
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance(); 
			XMLStreamReader parser;
			parser = factory.createXMLStreamReader( new FileInputStream(loadPath+"/Settings-StreamRipStar.xml" ) );
			while ( parser.hasNext() ) { 
	 
				switch ( parser.getEventType() ) { 
					case XMLStreamConstants.START_DOCUMENT: 
						SRSOutput.getInstance().log( "Loading file Settings-StreamRipStar.xml" ); 
						break; 
				 
				    case XMLStreamConstants.END_DOCUMENT: 
				    	SRSOutput.getInstance().log( "End of read settings " ); 
				    	parser.close(); 
				    	break; 
				 
				    case XMLStreamConstants.START_ELEMENT: 
				    	for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
				    		if (parser.getAttributeLocalName( i ).equals("langMenu_index")) {
				    			tmpShort = Short.valueOf(parser.getAttributeValue(i));
			    				if(tmpShort==0){
			    					lang="en";
			    				} else if (tmpShort==1) {
			    					lang="de";
			    					reg ="DE";
			    				} else {
			    					lang="system";
			    				}
				    		}
				    		else if (parser.getAttributeLocalName( i ).equals("LookAndFeelBox_className")) {
				    			String tmp = parser.getAttributeValue(i);
				    			if(tmp.equals("null")) {
				    				lnfClassName = null;
				    			} else {
				    				lnfClassName = parser.getAttributeValue(i);
				    			}
				    		}
				    	}
				    	break; 
				 
				    default: 
				    	break; 
				  }
				parser.next(); 
			}

		} catch (FileNotFoundException e) {
			SRSOutput.getInstance().logE("No configuartion file found: Settings-StreamRipStar.xml");
			noConfiFileFound = true;
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return tmpShort;
	}
}
