package controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import models.Track;

import org.apache.commons.vfs2.FileSystemException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import utils.Constants;
import utils.NoLongerFoundException;
import utils.TrackManager;
import utils.TrackUtils;

public class Application extends Controller {

	private static TrackManager trackManager = new TrackManager();
	private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	public static void index() {
		List<Track> top = Arrays.asList(trackManager.getTop());
		render(top);
	}

	public static void vote(String id) {
		try {
			Track track = trackManager.getTrackDetails(id);
			render(track);
		} catch (NoLongerFoundException e) {
			Logger.error(e.getMessage());
			renderHtml("<h1>" + e.getMessage() + "</h1>");
		}
	}

	public static void voteUp(String id) {
		try {
			trackManager.voteUp(id);
			index();
		} catch (NoLongerFoundException e) {
			Logger.error(e.getMessage());
			renderHtml("<h1>" + e.getMessage() + "</h1>");
		}
	}

	public static void voteDown(String id) {
		try {
			trackManager.voteDown(id);
			index();
		} catch (NoLongerFoundException e) {
			Logger.error(e.getMessage());
			renderHtml("<h1>" + e.getMessage() + "</h1>");
		}
	}

	public static void artwork(String id) {
		try {
			Artwork artwork = trackManager.getTrackDetails(id).getArtwork();
			if (artwork != null) {
				response.setContentTypeIfNotSet(artwork.getMimeType());
				byte[] binaryData = artwork.getBinaryData();
				renderBinary(new ByteArrayInputStream(binaryData));
			} else {
				File defaultArtwork = Play.getFile("public/images/musicnote.jpg");
				response.setContentTypeIfNotSet("image/jpg");
				renderBinary(defaultArtwork);
			}
		} catch (NoLongerFoundException e) {
			Logger.error(e.getMessage());
			renderHtml("<h1>" + e.getMessage() + "</h1>");
		}
	}

	public static void track(String id) {
		try {
			Track track = trackManager.getTrackDetails(id);
			File audioFile = Play.getFile(Constants.TRACKS_DIRECTORY_URI + "/" + track.getFileName());
			if (audioFile.exists()) {
				renderBinary(audioFile);
			}
		} catch (NoLongerFoundException e) {
			Logger.error(e.getMessage());
			response.status = 404;
		}
	}

	public static void player() {
		int topCount = trackManager.getTop().length;
		render(topCount);
	}

	public static void top5() {
		Track[] top = trackManager.getTop();
		if (top.length > 5) {
			top = Arrays.copyOf(top, Constants.TOP_LIMIT);
		}
		renderJSON(gson.toJson(top));
	}

	public static void next() {
		Track[] top = trackManager.getTop();
		if (top == null || top.length == 0) {
			response.status = 500;
		}
		renderJSON(gson.toJson(top[0]));
	}

	public static void markAsPlayed(String id) {
		Logger.info("Marking " + id + " as played");
		try {
			trackManager.markAsPlayed(id);
		} catch (NoLongerFoundException e) {
			Logger.error(e.getMessage());
			renderHtml("<h1>" + e.getMessage() + "</h1>");
		}
	}

	public static TrackManager getTrackManager() {
		return trackManager;
	}

}