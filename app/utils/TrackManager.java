package utils;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.cache.Cache;

import models.Track;

public class TrackManager {

	private Map<String, Track> tracks = new HashMap<String, Track>();
	private Track[] top = new Track[0];

	public synchronized void refreshTracksFromFilesystem() {
		Map<String, Track> updatedTracks = TrackUtils.readTracks();
		Track[] updatedTop = new Track[updatedTracks.size()];
		int updatedTopPosition = 0;
		tracks.clear();
		// preserve votes for tracks that were in the top before the update
		for (int i = 0; i < top.length; i++) {
			Track updatedTrack = updatedTracks.get(top[i].getId());
			if (updatedTrack != null) {
				updatedTop[updatedTopPosition] = top[i];
				updatedTop[updatedTopPosition].setFileName(updatedTrack.getFileName());
				updatedTop[updatedTopPosition].setArtwork(updatedTrack.getArtwork());
				updatedTop[updatedTopPosition].setPositionInTop(updatedTopPosition);
				tracks.put(updatedTop[updatedTopPosition].getId(), updatedTop[updatedTopPosition]);
				updatedTracks.remove(top[i].getId());
				updatedTopPosition++;
			}
		}
		// add new tracks with 0 votes at end of top
		for (Track newTrack : updatedTracks.values()) {
			newTrack.setPositionInTop(updatedTopPosition);
			updatedTop[updatedTopPosition] = newTrack;
			tracks.put(newTrack.getId(), newTrack);
			updatedTopPosition++;
		}
		top = updatedTop;
		// log possible inconsistency after diff
		if (top.length != tracks.size()) {
			Logger.error("Top size: " + top.length + " while tracklist size: " + tracks.size());
		}
	}

	public synchronized Track[] getTop() {
		Track[] copyOfTop = (Track[]) Cache.get(Constants.TOP_CACHE_KEY);
		if (copyOfTop == null) {
			// cache miss
			copyOfTop = new Track[top.length];
			for (int i = 0; i < top.length; i++) {
				copyOfTop[i] = top[i].clone();
			}
		}
		return copyOfTop;
	}

	public synchronized void voteUp(String trackId) throws NoLongerFoundException {
		Track track = getTrack(trackId);
		Cache.delete(Constants.TOP_CACHE_KEY);
		track.voteUp();
		int pos = track.getPositionInTop();
		while (pos > 0 && top[pos - 1].getVotes() < top[pos].getVotes()) {
			swapTracksInTop(pos, pos - 1);
			pos--;
		}
	}

	public synchronized void voteDown(String trackId) throws NoLongerFoundException {
		Track track = getTrack(trackId);
		Cache.delete(Constants.TOP_CACHE_KEY);
		track.voteDown();
		int pos = track.getPositionInTop();
		while (pos < top.length - 1 && top[pos + 1].getVotes() > top[pos].getVotes()) {
			swapTracksInTop(pos, pos + 1);
			pos++;
		}
	}

	public synchronized void markAsPlayed(String trackId) throws NoLongerFoundException {
		Track track = getTrack(trackId);
		Cache.delete(Constants.TOP_CACHE_KEY);
		track.markAsPlayed();
		int pos = track.getPositionInTop();
		while (pos < top.length - 1) {
			swapTracksInTop(pos, pos + 1);
			pos++;
		}
	}

	private void swapTracksInTop(int pos1, int pos2) {
		Track aux = top[pos1];
		top[pos1] = top[pos2];
		top[pos2] = aux;
		// also update track positions
		top[pos1].setPositionInTop(pos1);
		top[pos2].setPositionInTop(pos2);
	}

	public synchronized Track getTrackDetails(String trackId) throws NoLongerFoundException {
		Track track = getTrack(trackId);
		return track.clone();
	}

	private Track getTrack(String trackId) throws NoLongerFoundException {
		Track track = tracks.get(trackId);
		if (track == null) {
			throw new NoLongerFoundException("Track ID " + trackId
					+ " not found in tracklist. A filesystem refresh might have occured in the mean time.");
		}
		return track;
	}

}
