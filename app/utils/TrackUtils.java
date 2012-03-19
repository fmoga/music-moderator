package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import models.Track;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import play.Logger;
import play.Play;
import play.cache.Cache;

/**
 * Provides operations for track retrieval and monitoring.
 */
public class TrackUtils {

	/**
	 * Starts monitoring the tracks directory for filesystem events (tracks
	 * added, removed, etc). Called during bootstrap.
	 */
	public static void monitorTracks() {
		Logger.info("Starting filesystem monitor...");
		try {
			FileSystemManager filesystem = VFS.getManager();
			FileObject tracksDirectory = filesystem.resolveFile(Play.getFile(Constants.TRACKS_DIRECTORY_URI), "");
			DefaultFileMonitor monitor = new DefaultFileMonitor(new TracksListener());
			monitor.setRecursive(true);
			monitor.addFile(tracksDirectory);
			monitor.start();
			Logger.info("Filesystem monitor succesfully started.");
		} catch (FileSystemException e) {
			Logger.error(e, "Error while starting filesystem monitoring");
			Logger.error("Killing application...");
			System.exit(0);
		}
	}

	/**
	 * Reads tracks from the filesystem.
	 * 
	 * @return map of track id and track instance as it provides optimal
	 *         contains and delete complexity needed to compute difference
	 *         between refreshes of the tracklist
	 */
	public static Map<String, Track> readTracks() {
		File directory = Play.getFile(Constants.TRACKS_DIRECTORY_URI);
		if (!directory.exists() || !directory.isDirectory()) {
			Logger.error("Invalid tracks directory: " + directory.getAbsolutePath());
			return null;
		}
		Map<String, Track> tracks = new HashMap<String, Track>();
		File[] files = directory.listFiles();
		for (File f : files) {
			Track t = new Track();
			t.setId(hashCode(f));
			t.setFileName(f.getName());
			t.setArtwork(getArtworkForTrack(f));
			tracks.put(t.getId(), t);
		}
		return tracks;
	}

	private static Artwork getArtworkForTrack(File track) {
		try {
			AudioFile af = AudioFileIO.read(track);
			Tag tag = af.getTag();
			if (tag != null) {
				Artwork artwork = tag.getFirstArtwork();
				if (artwork != null) {
					return artwork;
				}
			}
		} catch (CannotReadException e) {
			Logger.debug(e, e.getMessage());
		} catch (IOException e) {
			Logger.debug(e, e.getMessage());
		} catch (TagException e) {
			Logger.debug(e, e.getMessage());
		} catch (ReadOnlyFileException e) {
			Logger.debug(e, e.getMessage());
		} catch (InvalidAudioFrameException e) {
			Logger.debug(e, e.getMessage());
		}
		return null;
	}

	public static String hashCode(File track) {
		return "" + ((long) track.hashCode() + (long) Integer.MAX_VALUE + 1L);
	}

	/**
	 * Prevent instantiation of utility class
	 */
	private TrackUtils() {
	}

}
