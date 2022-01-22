package com.fh.foundry.maven.plugins.minify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;

/**
 * Filesystem based non-incremental build context implementation which behaves
 * as if all files were just created. More specifically,
 * 
 * hasDelta returns <code>true</code> for all paths newScanner returns Scanner
 * that scans all files under provided basedir newDeletedScanner always returns
 * empty scanner. isIncremental returns <code>false</code<
 * getValue always returns <code>null</code>
 * 
 * @plexus.component role="org.sonatype.plexus.build.incremental.BuildContext"
 *                   role-hint="default"
 */
public class DefaultBuildContext extends AbstractLogEnabled implements BuildContext {

	public boolean hasDelta(String relpath) {
		return true;
	}

	public boolean hasDelta(File file) {
		return true;
	}

	public boolean hasDelta(List relpaths) {
		return true;
	}

	public OutputStream newFileOutputStream(File file) throws IOException {
		return new FileOutputStream(file);
	}

	public Scanner newScanner(File basedir) {
		DirectoryScanner ds = new DirectoryScanner();
		ds.setBasedir(basedir);
		return ds;
	}

	public void refresh(File file) {
		// do nothing
	}

	public Scanner newDeleteScanner(File basedir) {
		return new EmptyScanner(basedir);
	}

	public Scanner newScanner(File basedir, boolean ignoreDelta) {
		return newScanner(basedir);
	}

	public boolean isIncremental() {
		return false;
	}

	public Object getValue(String key) {
		return null;
	}

	public void setValue(String key, Object value) {
	}

	private String getMessage(File file, int line, int column, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append(file.getAbsolutePath()).append(" [").append(line).append(':').append(column).append("]: ");
		sb.append(message);
		return sb.toString();
	}

	/**
	 * @deprecated Use addMessage with severity=SEVERITY_ERROR instead
	 */
	public void addError(File file, int line, int column, String message, Throwable cause) {
		addMessage(file, line, column, message, SEVERITY_ERROR, cause);
	}

	/**
	 * @deprecated Use addMessage with severity=SEVERITY_WARNING instead
	 */
	public void addWarning(File file, int line, int column, String message, Throwable cause) {
		addMessage(file, line, column, message, SEVERITY_WARNING, cause);
	}

	public void addMessage(File file, int line, int column, String message, int severity, Throwable cause) {
		switch (severity) {
		case BuildContext.SEVERITY_ERROR:
			getLogger().error(getMessage(file, line, column, message), cause);
			return;
		case BuildContext.SEVERITY_WARNING:
			getLogger().warn(getMessage(file, line, column, message), cause);
			return;
		}
		throw new IllegalArgumentException("severity=" + severity);
	}

	public void removeMessages(File file) {
	}

	public boolean isUptodate(File target, File source) {
		return target != null && target.exists() && source != null && source.exists() && target.lastModified() > source.lastModified();
	}
}
