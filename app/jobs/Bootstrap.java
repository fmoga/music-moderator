package jobs;

import controllers.Application;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import utils.TrackUtils;

@OnApplicationStart
public class Bootstrap extends Job {

	public void doJob() {
		Logger.info("Running bootstrap job...");
		TrackUtils.monitorTracks();
		Application.getTrackManager().refreshTracksFromFilesystem();
		Logger.info("Finished running bootstrap job");
	}

}
