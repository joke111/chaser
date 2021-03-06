package chaser.core.chaser;

import chaser.core.listener.Listener;
import chaser.core.tail.DelimiterReadingTail;
import chaser.core.tail.Tail;
import chaser.core.file.ChaseFile;
import chaser.core.watcher.Watcher;
import chaser.util.IOUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DelimiterChaser implements Chaser {

	private Watcher watcher;
	private List<Listener> listeners;
	private Tail tail;

	private ExecutorService tailExecutorService;
	private ExecutorService processExecutorService;

	private ChaseFile target;

	public DelimiterChaser(String delimiter, Watcher watcher, Path target, List<Listener> listeners) {
		this.watcher = watcher;
		this.target = ChaseFile.of(target);
		this.listeners = listeners;
		this.tail = new DelimiterReadingTail(delimiter, this);

		watcher.setChaser(this);

		tailExecutorService = Executors.newFixedThreadPool(1);
		processExecutorService = Executors.newFixedThreadPool(1);
	}

	@Override
	public void chase() {
		watcher.startWatching();
	}

	@Override
	public void read() {
		tailExecutorService.execute(() -> tail.read(target));
	}

	@Override
	public void process(Byte[] bytes) {
		processExecutorService.execute(
			() -> listeners.parallelStream()
					.forEach(listener -> listener.process(bytes))
		);
	}

	@Override
	public void close() throws IOException {
		IOUtils.shutdownExecutorService(tailExecutorService);
		IOUtils.shutdownExecutorService(processExecutorService);
	}

}
