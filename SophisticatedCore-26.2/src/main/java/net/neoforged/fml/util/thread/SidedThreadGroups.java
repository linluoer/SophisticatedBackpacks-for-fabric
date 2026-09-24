package net.neoforged.fml.util.thread;

/**
 * SidedThreadGroups shim - provides ThreadGroup constants used to identify
 * whether the current thread is running on the client or server side.
 * <p>
 * On NeoForge these are dedicated thread groups assigned to sided threads.
 * On Fabric the client server thread and render thread share the same
 * ThreadGroup ("main"), so ThreadGroup comparison cannot distinguish them.
 * Therefore {@link #isServerThread()} compares the actual Thread reference
 * captured during server lifecycle start.
 * </p>
 */
public class SidedThreadGroups {
	/** Placeholder group, reassigned to the real server thread group on server start. */
	public static ThreadGroup SERVER = new ThreadGroup("Server");

	/** Placeholder group, reassigned to the real client thread group on client init. */
	public static ThreadGroup CLIENT = new ThreadGroup("Client");

	/** The actual server thread, captured in {@link #initServer()}. */
	private static Thread serverThread;

	private SidedThreadGroups() {
	}

	/**
	 * Captures the calling thread as the server-side thread.
	 * Should be invoked from the server thread during server lifecycle start.
	 */
	public static void initServer() {
		serverThread = Thread.currentThread();
		SERVER = Thread.currentThread().getThreadGroup();
	}

	/**
	 * Clears the captured server thread reference. Should be invoked when the
	 * server stops so that {@link #isServerThread()} does not match a stale
	 * thread reference from a previous server instance.
	 */
	public static void resetServer() {
		serverThread = null;
	}

	/**
	 * Captures the calling thread's ThreadGroup as the client-side group.
	 * Should be invoked from the client render thread during client setup.
	 */
	public static void initClient() {
		CLIENT = Thread.currentThread().getThreadGroup();
	}

	/**
	 * Returns true if the current thread is the server thread captured in
	 * {@link #initServer()}. On Fabric this correctly returns false for the
	 * render thread even though it shares the same ThreadGroup as the server
	 * thread.
	 */
	public static boolean isServerThread() {
		return serverThread != null && Thread.currentThread() == serverThread;
	}
}
