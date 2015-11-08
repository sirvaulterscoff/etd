package etd.demo;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author svs
 * @since 06.11.2015
 */
public class DemoFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		SynchLocker sync = new SynchLocker();
		ConcurrentLocker lock = new ConcurrentLocker();
		Deadlock deadlock = new Deadlock();
		try {
			new Thread(objectMethodToInifiniteRunnable(sync, "performSleep")).start();
			new Thread(objectMethodToInifiniteRunnable(sync, "performSleep")).start();
			new Thread(objectMethodToInifiniteRunnable(sync, "waitOnLock")).start();
			new Thread(objectMethodToInifiniteRunnable(sync, "signalOnLock")).start();
			new Thread(objectMethodToInifiniteRunnable(lock, "acuireAndReleaseLock")).start();
			new Thread(objectMethodToInifiniteRunnable(lock, "waitOnCondition")).start();
			new Thread(objectMethodToInifiniteRunnable(lock, "signalCondition")).start();
			new Thread(objectMethodToInifiniteRunnable(deadlock, "lockRighOrder")).start();
			new Thread(objectMethodToInifiniteRunnable(deadlock, "lockWrongOrder")).start();
		} catch (NoSuchMethodException ignore) {
		}
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

	}

	@Override
	public void destroy() {

	}

	private Runnable objectMethodToInifiniteRunnable(final Object obj, String method) throws NoSuchMethodException {
		final Method mtd = obj.getClass().getDeclaredMethod(method);
		return new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						mtd.invoke(obj);
					} catch (Exception ignored) {
					}
				}
			}
		};
	}
	private class SynchLocker {
		final Object lock = new Object();

		public synchronized void performSleep() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
			}
		}

		public void waitOnLock() throws InterruptedException {
			synchronized (lock) {
				//spurious wakeup - wha? never hear of it
				lock.wait();
				Thread.sleep(100);
			}
		}

		public void signalOnLock() throws InterruptedException {
			synchronized (lock) {
				Thread.sleep(100);
				lock.notify();
			}
		}
	}

	private class ConcurrentLocker {
		final ReentrantLock lock = new ReentrantLock();
		private final Condition waitCondition;

		public ConcurrentLocker() {
			waitCondition = lock.newCondition();
		}

		public void acuireAndReleaseLock() throws InterruptedException {
			lock.lock();
			Thread.sleep(100);
			lock.unlock();
		}

		public void waitOnCondition() throws InterruptedException {
			lock.lock();
			try {
				waitCondition.await();
			} finally {
				lock.unlock();
			}
		}

		public void signalCondition() {
			lock.lock();
			waitCondition.signal();
			lock.unlock();
		}
	}

	private class Deadlock {
		final Object lockOuter = new Object();
		final Object lockInner = new Object();

		public void lockRighOrder() throws InterruptedException {
			synchronized (lockOuter) {
				synchronized (lockInner) {
					Thread.sleep(1000);
				}
			}
		}
		public void lockWrongOrder() throws InterruptedException {
			synchronized (lockInner) {
				synchronized (lockOuter) {
					Thread.sleep(1000);
				}
			}
		}
	}
}
