package de.osthus.ambeth.example.job;

import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobContext;

public class JobExample implements IJob {
	@Override
	public boolean canBePaused() {
		return true;
	}

	@Override
	public boolean canBeStopped() {
		return true;
	}

	@Override
	public boolean supportsCompletenessTracking() {
		return true;
	}

	@Override
	public boolean supportsStatusTracking() {
		return true;
	}

	@Override
	public void execute(IJobContext context) throws Throwable {
		int count = 100;
		for (int a = 0; a < count; a++) {
			if (context.isStopped()) {
				return;
			}
			context.pauseIfRequested();
			context.setStatusMessage("Step " + a + " of " + count);
			// do something here
			context.setCompleteness((a + 1) / (double) count);
		}
	}
}
