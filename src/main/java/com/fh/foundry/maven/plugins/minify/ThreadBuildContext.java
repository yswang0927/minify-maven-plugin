package com.fh.foundry.maven.plugins.minify;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.plexus.util.Scanner;

/**
 * BuildContext implementation that delegates actual work to thread-local build
 * context set using {@link #setThreadBuildContext(BuildContext)}.
 * {@link DefaultBuildContext} is used if no thread local build context was set.
 * 
 * Note that plexus component metadata is not generated for this implementation.
 * Apparently, older version of plexus used by maven-filtering and likely other
 * projects, does not honour "default" role-hint.
 */
public class ThreadBuildContext implements BuildContext {

	private static final ThreadLocal threadContext = new ThreadLocal();

	private static final DefaultBuildContext defaultContext = new DefaultBuildContext();

	public static BuildContext getContext() {
		BuildContext context = (BuildContext) threadContext.get();
		if (context == null) {
			context = defaultContext;
		}
		return context;
	}

	public static void setThreadBuildContext(BuildContext context) {
		threadContext.set(context);
	}

	public boolean hasDelta(String relPath) {
		return getContext().hasDelta(relPath);
	}

	public boolean hasDelta(File file) {
		return getContext().hasDelta(file);
	}

	public boolean hasDelta(List relPaths) {
		return getContext().hasDelta(relPaths);
	}

	public Scanner newDeleteScanner(File basedir) {
		return getContext().newDeleteScanner(basedir);
	}

	public OutputStream newFileOutputStream(File file) throws IOException {
		return getContext().newFileOutputStream(file);
	}

	public Scanner newScanner(File basedir) {
		return getContext().newScanner(basedir);
	}

	public Scanner newScanner(File basedir, boolean ignoreDelta) {
		return getContext().newScanner(basedir, ignoreDelta);
	}

	public void refresh(File file) {
		getContext().refresh(file);
	}

	public Object getValue(String key) {
		return getContext().getValue(key);
	}

	public boolean isIncremental() {
		return getContext().isIncremental();
	}

	public void setValue(String key, Object value) {
		getContext().setValue(key, value);
	}

	public void addMessage(File file, int line, int column, String message, int severity, Throwable cause) {
		getContext().addMessage(file, line, column, message, severity, cause);
	}

	public void removeMessages(File file) {
		getContext().removeMessages(file);
	}

	/**
	 * @deprecated Use addMessage with severity=SEVERITY_WARNING instead
	 */
	public void addWarning(File file, int line, int column, String message, Throwable cause) {
		addMessage(file, line, column, message, BuildContext.SEVERITY_WARNING, cause);
	}

	/**
	 * @deprecated Use addMessage with severity=SEVERITY_ERROR instead
	 */
	public void addError(File file, int line, int column, String message, Throwable cause) {
		addMessage(file, line, column, message, BuildContext.SEVERITY_ERROR, cause);
	}

	public boolean isUptodate(File target, File source) {
		return getContext().isUptodate(target, source);
	}
}
