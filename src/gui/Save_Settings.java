package gui;
/* This program is licensed under the terms of the GPL V3 or newer*/
/* Written by Johannes Putzke*/
/* eMail: die_eule@gmx.net*/ 

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import control.SRSOutput;

public class Save_Settings extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
	
	private JPanel mainPanel = new JPanel();
	private JPanel topPanel = new JPanel();
	private JPanel buttomPanel = new JPanel();
	
	private ImageIcon abortIcon = new ImageIcon((URL)getClass().getResource("/Icons/abort_small.png"));
	private ImageIcon exportIcon = new ImageIcon((URL)getClass().getResource("/Icons/export_small.png"));

	private JButton abortButton = new JButton("Abort",abortIcon);
	private JButton exportButton = new JButton("Export",exportIcon);
	
	private JCheckBox exportSettingsCB = new JCheckBox("Settings (Preferences, Size of windows, Layout of tables)",true);
	private JCheckBox exportStreamsCB = new JCheckBox("Streams (All Streams, Default stream)",true);
	private JCheckBox exportSchedulesCB = new JCheckBox("Schedules",true);
	
	private JLabel explainLabel = new JLabel("Here you can save your settings to make a backup or send it to your friends.");
	
	private JFileChooser dirChooser = new JFileChooser();
	private TitledBorder exportBoarder = BorderFactory.createTitledBorder("What do you want to save?");
	
	public Save_Settings(Gui_StreamRipStar mainGui)
	{
		super(mainGui, "Save Settings");
		this.setModalityType(ModalityType.APPLICATION_MODAL);

		init();
		setLanguage();
		setVisible(true);
	}
	
	//set up all graphic components 
	private void init()
	{
	//set layouts
		mainPanel.setLayout(new GridBagLayout());
		topPanel.setLayout(new GridBagLayout());
		buttomPanel.setLayout(new GridBagLayout());
		
		topPanel.setBorder(exportBoarder);
	//set Constrains defaults
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets( 5, 5, 5, 5);
		
		add(mainPanel);
		
	//add panels
		c.gridy = 0;
		c.gridx = 0;
		mainPanel.add(explainLabel, c);
		c.weighty = 1.0;
		c.gridy = 1;
		mainPanel.add(topPanel, c);
		c.weighty = 0.0;
		c.gridy = 2;
		mainPanel.add(buttomPanel, c);

	
	//Add the list that contains all possible exportable streams
		c.gridy = 0;
		c.gridx = 0;
		topPanel.add(exportSettingsCB,c);
	//Add the list that contains all export streams
		c.gridy = 1;
		topPanel.add(exportStreamsCB,c);
		c.gridy = 2;
		topPanel.add(exportSchedulesCB,c);

		// Line with buttons
		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 0.0;
		buttomPanel.add(exportButton,c);
		c.gridx = 1;
		c.weightx = 1.0;
		buttomPanel.add(new JLabel(""),c);
		c.gridx = 2;
		c.weightx = 0.0;
		buttomPanel.add(abortButton,c);

	//set up Listeners
		abortButton.addActionListener( new AbortListener() );
		exportButton.addActionListener( new ExportListener() );
		
		dirChooser.setFileFilter(new PlayListFilter());
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		 //set size of window
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        pack();
        Dimension windowDim = getSize();
        // calculates the app. values
        int x = (screenDim.width - windowDim.width)/2;
        int y = (screenDim.height - windowDim.height)/2;
        // set location
        setLocation(x, y);
		
        //escape for exit
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        //register all Strokes
        getRootPane().registerKeyboardAction(new AbortListener(), escStroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void setLanguage() {
		try {
    		setTitle(trans.getString("exportStream"));
    		abortButton.setText(trans.getString("abortButton"));
    		exportButton.setText(trans.getString("export"));
    	} catch ( MissingResourceException e ) { 
		      SRSOutput.getInstance().logE( e.getMessage() ); 
	    }
	}
	
	private Save_Settings getMe()
	{
		return this;
	}

	class AbortListener implements ActionListener 
	{
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}

	
	class PlayListFilter extends FileFilter {
	    public boolean accept(File f) {
	        return f.isDirectory() || f.getName().toLowerCase().endsWith(".srsprops");
	    }
	    
	    public String getDescription() {
	        return ".SRSProps (StreamRipStar Preferences)";
	    }
	}
	
	class ExportListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{	
			dirChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int i = dirChooser.showOpenDialog(getMe());
			
			String savePath = "";
			
			if(i == JFileChooser.APPROVE_OPTION)
			{
				//make sure, it has a file extension (.SRSProps) 
				if(dirChooser.getSelectedFile().toString().toLowerCase().endsWith(".srsprops"))
				{
					savePath = dirChooser.getSelectedFile().toString();
				}
				else
				{
					savePath = dirChooser.getSelectedFile().toString() + ".SRSProps";
				}
				
				
				XMLOutputFactory outputFactory = XMLOutputFactory.newInstance(); 
				
				try {
					XMLEventWriter writer = outputFactory.createXMLEventWriter(new FileOutputStream(savePath) );
					XMLEventFactory eventFactory = XMLEventFactory.newInstance();
					
					//header for the file
					XMLEvent header = eventFactory.createStartDocument();
					XMLEvent startRootSettings = eventFactory.createStartElement( "", "", "Export of StreamRipStar" );
					
					//save preferences/Settings
					XMLEvent prefsMainWindow = eventFactory.createAttribute( "Prefs-MainWindow",  "");
					XMLEvent prefsScheduleManagerWindow = eventFactory.createAttribute( "ScheduleManager",  "");
					XMLEvent prefsSettings = eventFactory.createAttribute( "Settings-StreamRipStar",  "");
					XMLEvent prefsManagerWindow = eventFactory.createAttribute( "Streambrowser",  "");
					//save streams
					XMLEvent prefsAllStreams = eventFactory.createAttribute( "Streams",  "");
					XMLEvent prefsDefaultStream = eventFactory.createAttribute( "DefaultStream",  "");
					//Schedules
					XMLEvent prefsSchedules = eventFactory.createAttribute( "Scheduls",  "");
					
					XMLEvent endRoot = eventFactory.createEndElement( "", "", "Export of StreamRipStar" );
					XMLEvent endDocument = eventFactory.createEndDocument();
					
					//finally write into file
					writer.add( header ); 
					writer.add( startRootSettings );
					
					writer.add( prefsMainWindow );
					writer.add( prefsScheduleManagerWindow );
					writer.add( prefsSettings );
					writer.add( prefsManagerWindow );
					writer.add( prefsAllStreams );
					writer.add( prefsDefaultStream );
					writer.add( prefsSchedules );
					
					writer.add( endRoot ); 
					writer.add( endDocument ); 
					writer.close();

				} catch (FileNotFoundException f) {
					SRSOutput.getInstance().logE("Save_Settings: No configuartion file found:"+f.getStackTrace());
				} catch (XMLStreamException f) {
					SRSOutput.getInstance().logE("Save_Settings: XMLStreamExeption:"+f.getStackTrace());;
				} 
			}
		}
	}
}