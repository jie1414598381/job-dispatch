package com.yst.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 从配置文件加载sql
 */
public class Configure {

	private static Logger logger = Logger.getLogger(Configure.class);
	private static Properties props = new Properties();

	public Configure() {
		InputStream conf = this.getClass().getResourceAsStream(
				"/job.conf.properties");
		try {
			logger.info("\n\r\n\r");
			logger.info("===========================");
			logger.info("开始加载系统配置: /job.conf.properties");

			props.load(conf);

			logger.info(props);
			logger.info("加载系统配置完成.");
			logger.info("===========================");
			logger.info("\n\r\n\r");
		} catch (Exception e) {
			logger.info("加载系配置信息失败!");
			logger.info("===========================");
			logger.info("\n\r\n\r");
			System.exit(0);
		}
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	public static long getLong(String key) {
		try {
			long interval = Long.valueOf(props.getProperty(key));
			return interval;
		} catch (Throwable t) {
			logger.error("", t);
			return 3000;
		}
	}

	public static void main(String[] Args) {
		new Configure();
		System.out.println(Configure.getLong("dispatch.interval"));
	}
}
