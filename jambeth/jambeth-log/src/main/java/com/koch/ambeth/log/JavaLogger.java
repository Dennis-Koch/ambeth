package com.koch.ambeth.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLogger implements ILogger {

	private Logger logger;

	public JavaLogger(String source) {
		logger = Logger.getLogger(source);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isLoggable(Level.FINE);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isLoggable(Level.INFO);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isLoggable(Level.WARNING);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public void debug(CharSequence message) {
		logger.log(Level.FINE, message != null ? message.toString() : null);
	}

	@Override
	public void debug(CharSequence message, Throwable e) {
		logger.log(Level.FINE, message != null ? message.toString() : null, e);
	}

	@Override
	public void debug(Throwable e) {
		logger.log(Level.FINE, e, null);
	}

	@Override
	public void info(CharSequence message) {
		logger.log(Level.INFO, message != null ? message.toString() : null);
	}

	@Override
	public void info(CharSequence message, Throwable e) {
		logger.log(Level.INFO, message != null ? message.toString() : null, e);
	}

	@Override
	public void info(Throwable e) {
		logger.log(Level.INFO, e, null);
	}

	@Override
	public void warn(CharSequence message) {
		logger.log(Level.WARNING, message != null ? message.toString() : null);
	}

	@Override
	public void warn(CharSequence message, Throwable e) {
		logger.log(Level.WARNING, message != null ? message.toString() : null, e);
	}

	@Override
	public void warn(Throwable e) {
		logger.log(Level.WARNING, e, null);
	}

	@Override
	public void error(CharSequence message) {
		logger.log(Level.SEVERE, message != null ? message.toString() : null);
	}

	@Override
	public void error(CharSequence message, Throwable e) {
		logger.log(Level.SEVERE, message != null ? message.toString() : null, e);
	}

	@Override
	public void error(Throwable e) {
		logger.log(Level.SEVERE, e, null);
	}
}
