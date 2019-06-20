package control;
/* This program is licensed under the terms of the GPLV3 or newer*/
/* Written by Johannes Putzke*/
/* eMail: die_eule@gmx.net*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Control_http_Shoutcast_3 {
	private BufferedReader bw = null;
	private InputStream readGenresStream = null;
	private InputStream readStream = null;
	private String text = "";
	private Vector<String[]> streams = new Vector<String[]>(0, 1);
	private Boolean stopSearching = false; // stop an action
	private int currentPage = 0;
	private int totalPages = 0;

	/**
	 * the default constructor. Here is nothing to do at the moment
	 */
	public Control_http_Shoutcast_3() {

	}
	
	/**
	 * set the to laod page of the first one
	 */
	public void resetPages() {
		currentPage=0;
		totalPages=0;
	}
	
	/**
	 * Increases the to load page by one. This will only happen
	 * if the current page is not the last one
	 */
	public void nextPage() {
		if(totalPages > currentPage) {
			currentPage++;
		}
	}
	
	/**
	 * decreases the to load page by one. This will only happen
	 * if the current page is not the first
	 */
	public void prevoiousPage() {
		if( currentPage >= 1) {
			currentPage--;
//			
//			if(totalPages >=2) {
//				totalPages=totalPages-2;
//			}
		}
	}
	
	/**
	 * gives the current page back, which is loaded from shoutcast
	 * @return the current page for the current loaded search
	 */
	public int getCurrentPage() {
		return currentPage+1;
	}
	
	/**
	 * gives the total amount of pages back, which represents this
	 * search which all results
	 * @return All pages as an integer
	 */
	public int getTotalPages() {
		return totalPages+1;
	}
	
	/**
	 * Returns the number of pages available with streams if 
	 * this search. 
	 * @return [0] returns the current page
	 * [1] returns the max available pages
	 */
	public int[] getPages() {
		int[] pages = new int[2];
		pages[0] = currentPage;
		pages[1] = totalPages;
		
		return pages;
	}
	
	/**
	 * This method look for the stream address + port in a .pls or .m3u file and
	 * return the first one found. If it found a stream than it returns it. else
	 * it returns an empty string
	 * 
	 * @param streamURL
	 * @return
	 */
	public String getfirstStreamFromURL(String streamURL) {
		String url = "";
		String tmp = "";
		Boolean breakLook = false;

		try {
			// create URL
			URL stream = new URL(streamURL);

			// create Stream and open it to url
			readStream = stream.openStream();

			// create an buffered reader
			bw = new BufferedReader(new InputStreamReader(readStream));

			// read data and look for first address
			while (!breakLook && (tmp = bw.readLine()) != null) {

				// if it a .pls simply look for line File
				if (tmp.contains("File")) {
					// after = start the address
					int startAddress = tmp.indexOf("=");

					// read into url
					url = tmp.substring(startAddress + 1);

					// stop -> run finally() and return url
					breakLook = true;
				}
				// if a line contains the line http
				// StreamRipStar think its the address
				else if (tmp.contains("http://")) {
					url = tmp;
					// stop -> run finally() and return url
					breakLook = true;
				}
			}
		} catch (Exception e) {
			SRSOutput.getInstance().log("Could not get the playlist file from server");
			return null;
		} finally {
			if (readStream != null) {
				try {
					readStream.close();
				} catch (IOException e) {
				}
			}
		}
		return url;
	}

	/**
	 * Browse the list of stream on shoutcast.com in the given genre and save it
	 * into an Array of Strings into an vector called streaminfo. streaminfo
	 * contains following information: 
	 * 
	 * streamInfo[0] = Name 
	 * streamInfo[1] = now Playing 
	 * streamInfo[2] = Listeners
	 * streamInfo[3] = Bitrate 
	 * streamInfo[4] = Format
	 * streamInfo[5] = Stream ID from Shoutcast
	 * streamInfo[6] = Genres
	 * streamInfo[7] = Link to Website
	 * 
	 * @param genre the keyword (most cases the genre) 
	 * @param keyword true, if the search should be with keywords 
	 */
	public void getStreamsPerGenre(String genre, String keyword) {
		// make sure, that the Vector of streams is empty
		streams.removeAllElements();
		streams.trimToSize();
		
		try {

			String data = "";
			URL url;
			//if no genre is defined - its a keyword search
			if(!genre.trim().equals("") || genre == null) {
				data += URLEncoder.encode("genrename", "UTF-8") + "=" + URLEncoder.encode(genre, "UTF-8");
			    url =  new URL("https://directory.shoutcast.com/Home/BrowseByGenre");
			} else {
				data += URLEncoder.encode("query", "UTF-8") + "=" + URLEncoder.encode(keyword, "UTF-8");
			    url =  new URL("https://directory.shoutcast.com/Search/UpdateSearch");
			}
			
		    SRSOutput.getInstance().logD("Shoutcast query for genres: "+url);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setDoOutput(true);
		    conn.setRequestMethod("POST");
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    wr.close();
		    
		    // Get the response
		    bw = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		    JSONArray jsonStreams = (JSONArray)new JSONParser().parse(bw);
		    SRSOutput.getInstance().logD("SIZE: "+jsonStreams.size());
		    
		    //
		    // Now convert the json object into StreamRipStar compatible streams
		    //
		    for(int i=0; i < jsonStreams.size(); i++) {
		    	try {
		    		
		    		if(stopSearching) break;
		    		
					// create a stream to save the info from the website
					String[] streamInfo = new String[8];

		    		streamInfo[0] = (String) ((JSONObject)jsonStreams.get(i)).get("Name");
		    		streamInfo[1] = (String) ((JSONObject)jsonStreams.get(i)).get("CurrentTrack");
		    		streamInfo[2] = String.valueOf((long) ((JSONObject)jsonStreams.get(i)).get("Listeners"));
		    		streamInfo[3] = String.valueOf((long) ((JSONObject)jsonStreams.get(i)).get("Bitrate"));
		    		streamInfo[4] = (String) ((JSONObject)jsonStreams.get(i)).get("Format");
		    		streamInfo[5] = "http://yp.shoutcast.com/sbin/tunein-station.pls?id="+String.valueOf((long) ((JSONObject)jsonStreams.get(i)).get("ID"));
		    		streamInfo[6] = (String) ((JSONObject)jsonStreams.get(i)).get("Genre");
//		    		streamInfo[7] = (String) ((JSONObject)jsonStreams.get(i)).get("Website");
		    		
		    		//replace the MIME type into human readable format
		    		if(streamInfo[4].equals("audio/mpeg")) {
		    			streamInfo[4] = "MP3";
		    		} else if(streamInfo[4].equals("audio/aacp")) {
		    			streamInfo[4] = "AAC+";
		    		}
		    		
		    		//This stream has all information
					streams.add(streamInfo);
					
				} catch (NullPointerException e) {
					SRSOutput.getInstance().logE("Error while loading from shoutcast website: NullPointer");
					SRSOutput.getInstance().logE("Current text string was: " + text);
					e.printStackTrace();
				} catch (StringIndexOutOfBoundsException e) {
					SRSOutput.getInstance().logE("Error while loading from shoutcast website: IndexOutOfBoundsException");
					SRSOutput.getInstance().logE("Current text string was: " + text);
					e.printStackTrace();
				} catch (NumberFormatException e) {
					SRSOutput.getInstance().logE("Controled Crash in StreamBrowser");
					SRSOutput.getInstance().logE("Current text string was: " + text);
					e.printStackTrace();
				}
		    }
		} catch (ParseException e) {
				SRSOutput.getInstance().logE("Error while parsing the shoutcast website");
				SRSOutput.getInstance().logE("Current text string was: " + text);
				e.printStackTrace();
		} catch (Exception e) {
			if (e.getMessage().startsWith("stream is closed")) {
				stopSearching = true;
			} else {
				e.printStackTrace();
			}
		} finally {
			// reset for new run
			stopSearching = false;

			if (readGenresStream != null) {
				try {
					readGenresStream.close();
					
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * This Method return an Vector of Strings witch contains all streams from a
	 * specific genre
	 * 
	 * @return
	 */
	public Vector<String[]> getStreams() {
		return streams;
	}

	public String getBaseAddress() {
		String shoutcast = "http://yp.shoutcast.com/sbin/tunein-station.pls?id=";
		return shoutcast;
	}
}
