package com.yst.dispatch.dao.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.yst.client.job.JobEvent;
import com.yst.dispatch.dao.IJobDao;
import com.yst.dispatch.domain.Job;

@Component("jobDAO")
public class JobDaoImpl implements IJobDao {

	@Override
	public Job get(long jobid) {
		return htOracle.get(Job.class, jobid);
	}

	// 00-就绪；10-正在运行；11-成功 ;99－失败
	@Override
	public void run(long jobid, String position) {
		Job temp = htOracle.get(Job.class, jobid);
		if (temp != null) {
			if (temp.getTimes() <= 0) {
				temp.setBeginTime(new Date());
			}
			temp.setLastTime(new Date());
			temp.setStatus("10");
			temp.setPosition(position);
		}
	}

	@Override
	public boolean isAllowToRun(long jobid) {


		Job job = htOracle.get(Job.class, jobid);

		return false;
	}

	@Override
	public boolean isConfirmed(long jobid) {
		return false;
	}
	@Override
	public JobEvent toJobEvent(long jobid) {
		
		return null;
	}

	// 00-就绪；10-正在运行；11-成功 ;99－失败
	@Override
	public List<Job> getAllReady() {
		return (List<Job>) htOracle
				.find("select job from Job job where job.status ='00' order by job.priority asc ");
	}

	@Autowired
	private HibernateTemplate htOracle;

	public void setHibernateTemplate(HibernateTemplate ht) {
		this.htOracle = ht;
	}

	

}
