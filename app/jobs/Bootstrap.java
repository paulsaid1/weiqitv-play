package jobs;

import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {

	public void doJob() {
		// Check if the database is empty
		// if (User.count() == 0) {
		// Fixtures.deleteDatabase();
		// Fixtures.loadModels("initial-data.yml");
		// }
	}

}