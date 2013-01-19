package org.jenkinsci.plugins.uptime;

import hudson.model.Result;
import hudson.model.Run;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

/**
 * Default implementation of the UptimeService 
 * 
 * @author Chris Mair
 */
public class DefaultUptimeService implements UptimeService {

	@Override
	public BigDecimal getUptimePercentage(Iterator<Run<?,?>> iterator) {

		if (!iterator.hasNext()) {
			return null;
		}
		
		long timeNow = getTimeNow();
		long previousTime = timeNow;
		long startTime = 0L;
		long totalFailedMillis = 0L;
		
        while (iterator.hasNext()) {
			Run<?,?> run = (Run<?,?>) iterator.next();
			long runStartTime = run.getTimestamp().getTimeInMillis();
			System.out.println("Run: " + run + " runStartTime=" + new Date(runStartTime) + " result=" + run.getResult());

			// Always overwrite with oldest build so far
			startTime = runStartTime;
			
			if (isFailed(run)) {
				totalFailedMillis += previousTime - runStartTime;
			}
			previousTime = runStartTime;
		}

        long totalMinutes = timeNow - startTime;
        BigDecimal failedPercentage = calculatePercentage(totalFailedMillis, totalMinutes); 
		return new BigDecimal("1.00").subtract(failedPercentage);
	}

	private BigDecimal calculatePercentage(long value, long total) {
		return BigDecimal.valueOf(value).setScale(2).divide(BigDecimal.valueOf(total), BigDecimal.ROUND_HALF_DOWN);
	}
	
	private boolean isFailed(Run<?,?> run) {
		return run.getResult() == Result.FAILURE;
	}

	// Enable test to override and specify exact time
	protected long getTimeNow() {
		return System.currentTimeMillis();
	}
	
}