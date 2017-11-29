package com.yst.dispatch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yst.client.job.IJob;
import com.yst.client.job.JobEvent;
import com.yst.dispatch.dao.IJobDao;
import com.yst.dispatch.domain.Job;
import com.yst.util.Configure;

public class DispatchService implements IJob {

	private static Object lock = new Object();
	private static int addCnt = 0;
	private static int delCnt = 0;
	private static PriorityBlockingQueue<JobEvent> queue = new PriorityBlockingQueue<JobEvent>();
	private static PriorityBlockingQueue<JobEvent> sQMessageQueue = new PriorityBlockingQueue<JobEvent>();
	private static PriorityBlockingQueue<JobEvent> gzHMessageQueue = new PriorityBlockingQueue<JobEvent>();
	private static ExecutorService produce = Executors
			.newSingleThreadExecutor();

	private static Logger logger = Logger.getLogger(DispatchService.class);
	private static ApplicationContext context;
	private static IJobDao jobDAO;
	static {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		jobDAO = (IJobDao) context.getBean("jobDAO");

		produce.execute(new Runnable() {
			public void run() {
				logger.info("启动工作派遣线程,定期从数据库获取就绪的job");// 00-就绪；10-正在运行；11-成功
				while (true) {
					try {
						if (!queue.isEmpty()) {
							logger.info("queue不为空，休眠2妙");
							Thread.sleep(2000);
							continue;
						} else {
							logger.info("queue为空，将addCnt和delCnt置为0");
							addCnt = 0;
							delCnt = 0;
						}
						synchronized (lock) {
							List<Job> jobs = jobDAO.getAllReady();
							if (jobs == null) {
								logger.info("状态为00的job为空");
								continue;
							}
							for (int i = 0; i < jobs.size(); i++) {
								Job job = jobs.get(i);
								if (job == null) {
									continue;
								}

								// 该job是否都已确认,都已确认就无需再执行
								if (jobDAO.isConfirmed(job.getId())) {
									continue;
								}

								// 该job是否允许执行
								if (!jobDAO.isAllowToRun(job.getId())) {
									continue;
								}

								// 将就绪的job转化为统一形式
								JobEvent JobEvent = jobDAO.toJobEvent(job
										.getId());

								// 根据job类型，将job分别放入不同的队列
								if (JobEvent.getJobtype().equals("000")) {
									if (!gzHMessageQueue.contains(JobEvent)) {
										logger.info(String.format(
												"公众号消息推送-添加:%s", JobEvent));
										gzHMessageQueue.add(JobEvent);
									} else {
										logger.info(String.format(
												"公众号消息推送-已存在:%s", JobEvent));
									}
								} else if (JobEvent.getJobtype().equals("001")) {
									if (!sQMessageQueue.contains(JobEvent)) {
										logger.info(String.format(
												"商圈消息推送-添加:%s", JobEvent));
										sQMessageQueue.add(JobEvent);
									} else {
										logger.info(String.format(
												"商圈消息推送-已存在:%s", JobEvent));
									}
								} else {
									if (!queue.contains(JobEvent)) {
										logger.info(String.format("其他-添加:%s",
												JobEvent));
										
										queue.add(JobEvent);
									} else {
										logger.info(String.format("其他-已存在:%s",
												JobEvent));
									}
								}
							}
						}
						Thread.sleep(Configure.getLong("dispatch.interval"));
					} catch (Throwable t) {
						logger.error("工作派遣线程异常", t);
					}
				}
			}
		});
	}

	// 预留１,２,３,４,5号机器
	// 1,2,3 执行非消息推送任务
	// 4 执行商圈消息推送
	// 5 执行公众号消息推送任务
	// 目前启用　1,4,5号机器
	@Override
	public List<JobEvent> dispatch(String positon, int count) {
		synchronized (lock) {
			if (positon == null || "".equals(positon)) {
				return null;
			} else if (positon.equals("1") || positon.equals("2")
					|| positon.equals("3")) {
				return dispatchToOneTwoThree(positon, count);
			} else if (positon.equals("4")) {
				return dispatchToFour(positon, count);
			} else if (positon.equals("5")) {
				return dispatchToFive(positon, count);
			} else {
				return null;
			}
		}
	}

	// 将任务分配到1，２，３号执行方
	private List<JobEvent> dispatchToOneTwoThree(String positon, int count) {
		int cnt = 0;
		logger.info(String.format("其他-待分配job数目:%s", queue.size()));
		List<JobEvent> jobs = new ArrayList<JobEvent>();
		while (true) {
			JobEvent job = queue.peek();   //读头元素
			if (job == null) {
				logger.info("其他-沒有就绪的job可供执行");
				break;
			} else {
				jobs.add(job);
				queue.poll();    //出队列
				jobDAO.run(job.getJobid(), positon);// 更改job状态为运行中...
				logger.info(job + "其他-出队列,状态更新为运行中...");
				cnt++;
				if (cnt > count) {
					break;
				}
			}
		}
		return jobs;
	}

	// 将任务分配到４号执行方
	private List<JobEvent> dispatchToFour(String positon, int count) {
		int cnt = 0;
		logger.info(String.format("商圈消息推送-待分配job数目:%s", sQMessageQueue.size()));
		List<JobEvent> jobs = new ArrayList<JobEvent>();
		while (true) {
			JobEvent job = sQMessageQueue.peek();
			if (job == null) {
				logger.info("商圈消息推送-沒有就绪的job可供执行");
				break;
			} else {
				jobs.add(job);
				sQMessageQueue.poll();
				jobDAO.run(job.getJobid(), positon);// 更改job状态为运行中...
				logger.info(job + "商圈消息推送-出队列,状态更新为运行中...");
				cnt++;
				if (cnt > count) {
					break;
				}
			}
		}
		return jobs;
	}

	// 将任务分配到5号执行方
	private List<JobEvent> dispatchToFive(String positon, int count) {
		int cnt = 0;
		logger.info(String.format("公众号消息推送-待分配job数目:%s", gzHMessageQueue.size()));
		List<JobEvent> jobs = new ArrayList<JobEvent>();
		while (true) {
			JobEvent job = gzHMessageQueue.peek();
			if (job == null) {
				logger.info("公众号消息推送-沒有就绪的job可供执行");
				break;
			} else {
				jobs.add(job);
				gzHMessageQueue.poll();
				jobDAO.run(job.getJobid(), positon);// 更改job状态为运行中...
				logger.info(job + "公众号消息推送-出队列,状态更新为运行中...");
				cnt++;
				if (cnt > count) {
					break;
				}
			}
		}
		return jobs;
	}
}
