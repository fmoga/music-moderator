package utils;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

import controllers.Application;

import play.Logger;
import play.cache.Cache;

/**
 * Listener for filesystem events in the tracks directory
 */
public class TracksListener implements FileListener {

	private Timer timer = new Timer();
	private TimerTask task;

	@Override
	public void fileChanged(FileChangeEvent event) throws Exception {
		resetTimer();
	}

	@Override
	public void fileCreated(FileChangeEvent event) throws Exception {
		resetTimer();
	}

	@Override
	public void fileDeleted(FileChangeEvent event) throws Exception {
		resetTimer();
	}

	private void resetTimer() {
		synchronized (timer) {
			if (task != null) {
				task.cancel();
				timer.purge();
			}
			task = new FilesystemUpdateTask();
			timer.schedule(task, Constants.FILESYSTEM_UPDATE_DELAY);
		}
	}

	public class FilesystemUpdateTask extends TimerTask {

		@Override
		public void run() {
			Logger.info("Filesystem change detected. Refresh NOW!");
			Application.getTrackManager().refreshTracksFromFilesystem();
		}

	}

}
