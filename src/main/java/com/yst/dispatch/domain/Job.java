package com.yst.dispatch.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "T_JOB")
@SequenceGenerator(name = "SEQ_GEN", sequenceName = "S_JOB", allocationSize = 1)
public class Job {

	@Id
	@Column(name = "jobid")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GEN")
	private long id;

	private String jobtype;

	private String jobmc;

	private long priority;

	@Column(name = "CREATE_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	@Column(name = "BEGIN_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date beginTime;

	@Column(name = "LAST_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastTime;

	private String status;

	private long times;

	private String position;

	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getJobtype() {
		return jobtype;
	}

	public void setJobtype(String jobtype) {
		this.jobtype = jobtype;
	}

	public String getJobmc() {
		return jobmc;
	}

	public void setJobmc(String jobmc) {
		this.jobmc = jobmc;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getLastTime() {
		return lastTime;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getTimes() {
		return times;
	}

	public void setTimes(long times) {
		this.times = times;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

}