/**
 * 
 */
package dev.roshin.eclipse.git_remote_change_alert.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.lib.Repository;

/**
 * This class encapsulate all the code that touches "discouraged access" areas.
 * This way, if EGit changes its internal APIs, only this class needs to
 * updated.
 */
public class UnsafeUtils {

	public static Repository getGitRepository(IProject project) {
		RepositoryMapping mapping = RepositoryMapping.getMapping(project);
		return (mapping != null) ? mapping.getRepository() : null;
	}

}
