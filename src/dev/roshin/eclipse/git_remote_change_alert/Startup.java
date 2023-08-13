/**
 * 
 */
package dev.roshin.eclipse.git_remote_change_alert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

import dev.roshin.eclipse.git_remote_change_alert.preferences.PreferenceConstants;
import dev.roshin.eclipse.git_remote_change_alert.util.LogUtil;
import dev.roshin.eclipse.git_remote_change_alert.util.UnsafeUtils;

class CachedData {
	long timestamp;
	String commitId;
}

/**
 * This class is responsible for checking if there are remote changes in the Git
 * projects within the workspace during the startup and after certain resource
 * changes.
 * 
 * It notifies the user about projects with remote changes.
 */
public class Startup implements IStartup, IResourceChangeListener {
	private Map<IProject, CachedData> cacheMap = new HashMap<>();

	/**
	 * This method is called during the Eclipse workspace startup. It checks all the
	 * open projects in the workspace for remote Git changes and displays
	 * notifications if any are found.
	 */
	@Override
	public void earlyStartup() {
		LogUtil.logInfo("Workspace Staring...");

		// Add a resource change listener to the workspace
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<IProject> projectsWithChanges = new ArrayList<>();

		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			if (project.isOpen() && isGitProject(project) && hasRemoteChanges(project)) {
				projectsWithChanges.add(project);
			}
		}

		if (!projectsWithChanges.isEmpty()) {
			displayNotification(projectsWithChanges);
		}
	}

	/**
	 * Checks if the provided project is a Git project.
	 * 
	 * @param project The Eclipse project to check.
	 * @return true if the project is a Git project, false otherwise.
	 */
	private boolean isGitProject(IProject project) {
		// Use EGit to determine if the project is a Git project
		try {
			File projectLocation = project.getLocation().toFile();
			File gitDirectory = new File(projectLocation, ".git");
			return gitDirectory.exists();
		} catch (Exception e) {
			LogUtil.logError("Error checking if project is a Git project: " + project.getName(), e);
			return false;
		}
	}

	/**
	 * Determines if the provided Git project has remote changes.
	 * 
	 * @param project The Git project to check.
	 * @return true if there are remote changes, false otherwise.
	 */
	private boolean hasRemoteChanges(IProject project) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean isCacheEnabled = store.getBoolean(PreferenceConstants.CACHE_ENABLED_KEY);
		int cooldownPeriodInMinutes = store.getInt(PreferenceConstants.COOLDOWN_PERIOD_KEY);
		long cooldownPeriod = cooldownPeriodInMinutes * 60 * 1000;

		if (isCacheEnabled) {
			CachedData data = cacheMap.get(project);

			if (data != null) {
				long currentTime = System.currentTimeMillis();
				if ((currentTime - data.timestamp) < cooldownPeriod) {
					// If within cooldown period, avoid fetch and check using the cached commit ID.
					try (RevWalk revWalk = new RevWalk(UnsafeUtils.getGitRepository(project))) {
						RevCommit localCommit = revWalk.parseCommit(UnsafeUtils.getGitRepository(project)
								.resolve(UnsafeUtils.getGitRepository(project).getFullBranch()));
						return !data.commitId.equals(localCommit.name());
					} catch (IOException e) {
						LogUtil.logError(
								"Failed to fetch or compare Git data for project (cache walk): " + project.getName(),
								e);
						return false;
					} catch (Exception e) {
						LogUtil.logError(
								"Unexpected error while checking for remote changes (cache walk): " + project.getName(),
								e);
						return false;
					}
				}
			}
		}

		// The original code for fetching from remote and comparing commits
		try {
			Repository repo = UnsafeUtils.getGitRepository(project);
			if (repo == null) {
				return false;
			}

			// Use JGit API to fetch
			try (Git git = new Git(repo)) {
				FetchCommand fetch = git.fetch();
				FetchResult result = fetch.call();

				String currentBranch = repo.getFullBranch();
				String remoteTrackingBranch = "refs/remotes/origin/" + repo.getBranch();

				// Get the commit objects for local and remote branches
				try (RevWalk revWalk = new RevWalk(repo)) {
					RevCommit localCommit = revWalk.parseCommit(repo.resolve(currentBranch));
					RevCommit remoteCommit = revWalk.parseCommit(repo.resolve(remoteTrackingBranch));

					// Update cache
					CachedData newData = new CachedData();
					newData.timestamp = System.currentTimeMillis();
					newData.commitId = remoteCommit.name();
					cacheMap.put(project, newData);

					// If they're different, there are changes
					return !localCommit.equals(remoteCommit);
				}
			}
		} catch (IOException e) {
			LogUtil.logError("Failed to fetch or compare Git data for project: " + project.getName(), e);
			return false;
		} catch (Exception e) {
			LogUtil.logError("Unexpected error while checking for remote changes: " + project.getName(), e);
			return false;
		}
	}

	/**
	 * Displays a notification about the provided project having remote changes.
	 * 
	 * @param project The project with remote changes.
	 */
	private void displayNotification(IProject project) {
		try {
			// Display a notification to the user
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Git Remote Changes",
							"Project '" + project.getName()
									+ "' has changes in the remote repository that you might want to pull.");
				}
			});
		} catch (Exception e) {
			LogUtil.logError("Failed to display notification for project: " + project.getName(), e);
		}
	}

	/**
	 * Displays a consolidated notification for multiple projects having remote
	 * changes.
	 * 
	 * @param projects A list of projects with remote changes.
	 */
	private void displayNotification(List<IProject> projects) {
		try {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					StringBuilder message = new StringBuilder(
							"The following projects have changes in the remote repository that you might want to pull:\n\n");
					for (IProject project : projects) {
						message.append("- ").append(project.getName()).append("\n");
					}
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Git Remote Changes",
							message.toString());
				}
			});
		} catch (Exception e) {
			LogUtil.logError("Failed to display notifications for projects", e);
		}
	}

	/**
	 * This method is triggered when a resource in the workspace changes.
	 * Specifically, it listens for POST_CHANGE events.
	 * 
	 * When a project is opened, it checks if that project has remote Git changes.
	 * 
	 * @param event The event describing the nature of the change.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		// We are only interested in POST_CHANGE events
		if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
			return;
		}
		// Check for opened projects
		try {
			event.getDelta().accept(delta -> {
				if (delta.getResource().getType() == IResource.PROJECT) {
					IProject project = (IProject) delta.getResource();
					if (project.isOpen() && (delta.getFlags() & IResourceDelta.OPEN) != 0) {
						if (isGitProject(project) && hasRemoteChanges(project)) {
							displayNotification(project);
						}
					}
				}
				return true; // continue visiting children
			});
		} catch (CoreException e) {
			LogUtil.logError("Error while processing resource changes", e);
		} catch (Exception e) {
			LogUtil.logError("Unexpected error while processing resource changes", e);
		}
	}

}
