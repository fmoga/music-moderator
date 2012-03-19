package models;

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jaudiotagger.tag.images.Artwork;

import com.google.gson.annotations.Expose;

import play.Logger;
import play.db.jpa.Model;
import utils.TrackUtils;

public class Track {

	@Expose
	private String id;
	@Expose
	private String name;
	@Expose
	private String artist;
	private String fileName;
	private int positionInTop;
	private Artwork artwork;
	@Expose
	private int votes;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		determineArtistAndSongName();
	}

	private void determineArtistAndSongName() {
		String[] points = fileName.split("\\.");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < points.length - 1; i++) {
			builder.append(points[i]);
		}
		String nameWithoutExtension = builder.toString();
		int index = nameWithoutExtension.indexOf("-");
		if (index == -1) {
			Logger.error("Could not detect song and artist name for " + fileName);
			this.artist = "";
			this.name = nameWithoutExtension;
		} else {
			this.name = nameWithoutExtension.substring(index + 1);
			this.artist = nameWithoutExtension.substring(0, index);
		}
	}

	public String getName() {
		return name;
	}

	public String getArtist() {
		return artist;
	}

	public Artwork getArtwork() {
		return artwork;
	}

	public void setArtwork(Artwork artwork) {
		this.artwork = artwork;
	}

	public int getVotes() {
		return votes;
	}

	public void voteUp() {
		votes++;
	}

	public void voteDown() {
		votes--;
	}

	public void markAsPlayed() {
		votes = 0;
	}

	private void setVotes(int votes) {
		this.votes = votes;
	}

	public int getPositionInTop() {
		return positionInTop;
	}

	public void setPositionInTop(int positionInTop) {
		this.positionInTop = positionInTop;
	}

	public Track clone() {
		Track copy = new Track();
		copy.setId(id);
		copy.setFileName(fileName);
		copy.setArtwork(artwork);
		copy.setPositionInTop(positionInTop);
		copy.setVotes(votes);
		return copy;
	}

}
