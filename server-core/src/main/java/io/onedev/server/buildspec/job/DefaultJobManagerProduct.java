package io.onedev.server.buildspec.job;


import io.onedev.server.util.schedule.TaskScheduler;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;

public class DefaultJobManagerProduct {
	private final Map<Long, Collection<String>> scheduledTasks = new ConcurrentHashMap<>();
	private final TaskScheduler taskScheduler;
	private volatile Thread thread;

	public DefaultJobManagerProduct(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public Map<Long, Collection<String>> getScheduledTasks() {
		return scheduledTasks;
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	@Listen
	public void on(SystemStopping event) {
		if (thread != null) {
			Thread copy = thread;
			thread = null;
			try {
				copy.join();
			} catch (InterruptedException e) {
			}
		}
		scheduledTasks.values().stream().forEach(it1 -> it1.stream().forEach(it2 -> taskScheduler.unschedule(it2)));
		scheduledTasks.clear();
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event, TransactionManager thisTransactionManager) {
		if (event.getEntity() instanceof Project) {
			Long projectId = ((Project) event.getEntity()).getId();
			thisTransactionManager.runAfterCommit(new Runnable() {
				@Override
				public void run() {
					Collection<String> tasksOfProject = scheduledTasks.remove(projectId);
					if (tasksOfProject != null)
						tasksOfProject.stream().forEach(it -> taskScheduler.unschedule(it));
				}
			});
		}
	}
}