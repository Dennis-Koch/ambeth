package com.koch.ambeth.log.slf4j;

import org.slf4j.Logger;

import com.koch.ambeth.log.ILogger;

public class Slf4jLogger implements ILogger {

	private Logger logger;

	public Slf4jLogger(String source) {
		logger = org.slf4j.LoggerFactory.getLogger(source);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	@Override
	public void debug(CharSequence message) {
		logger.debug(message != null ? message.toString() : null);
	}

	@Override
	public void debug(CharSequence message, Throwable e) {
		logger.debug(message != null ? message.toString() : null, e);
	}

	@Override
	public void debug(Throwable e) {
		logger.debug(e != null ? e.getMessage() : null, e);
	}

	@Override
	public void info(CharSequence message) {
		logger.info(message != null ? message.toString() : null);
	}

	@Override
	public void info(CharSequence message, Throwable e) {
		logger.info(message != null ? message.toString() : null, e);
	}

	@Override
	public void info(Throwable e) {
		logger.info(e != null ? e.getMessage() : null, e);
	}

	@Override
	public void warn(CharSequence message) {
		logger.warn(message != null ? message.toString() : null);
	}

	@Override
	public void warn(CharSequence message, Throwable e) {
		logger.warn(message != null ? message.toString() : null, e);
	}

	@Override
	public void warn(Throwable e) {
		logger.warn(e != null ? e.getMessage() : null, e);
	}

	@Override
	public void error(CharSequence message) {
		logger.error(message != null ? message.toString() : null);
	}

	@Override
	public void error(CharSequence message, Throwable e) {
		logger.error(message != null ? message.toString() : null, e);
	}

	@Override
	public void error(Throwable e) {
		logger.error(e != null ? e.getMessage() : null, e);
	}
}
