package com.yst.dispatch.dao;

import java.util.List;

import com.yst.client.job.JobEvent;
import com.yst.dispatch.domain.Job;

public interface IJobDao {

	public Job get(long jobid);

	public List<Job> getAllReady();

	public void run(long jobid, String remark);

	public boolean isAllowToRun(long jobid);

	public JobEvent toJobEvent(long jobid);
	
	public boolean isConfirmed(long jobid);

	
}
