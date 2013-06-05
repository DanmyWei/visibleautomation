package com.androidApp.tcpListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * tcp listener thread: listens for connections, allows broadcast of messages. Normally, I'd use
 * a broadcast message, but the test instrumentation runs with the permissions of the instrumented
 * application
 * @author matt2
 *
 */
public class TCPListener implements Runnable {
	protected static final String TAG = "TCPListener";
	protected int					mPort;
	protected ServerSocket 			mServerSocket;
	protected List<Socket> 			mSocketList;
	
	public TCPListener(int port) throws IOException {
		mPort = port;
		mServerSocket = new ServerSocket(port);
		mSocketList = new ArrayList<Socket>();
	}
	
	/**
	 * background thread to accept connections
	 */
	public void run() {
		while (true) {
			Socket socket = null;
			try {
				socket = mServerSocket.accept();
			} catch (Exception ex) {
				Log.e(TAG, "threw exception attempting to accept connection " + ex.getMessage());
				ex.printStackTrace();
			}
			if (socket != null) {
				mSocketList.add(socket);
			}
		}
	}
	
	/**
	 * interface to broadcast message to anyone who connected to our socket.
	 * @param msg
	 */
	public void broadcast(String msg) {
		byte bytes[] = msg.getBytes();
		ArrayList<Socket> removeList = new ArrayList<Socket>();
		for (Socket socket : mSocketList) {
			try {
				OutputStream os = socket.getOutputStream();
				os.write(bytes);
				os.flush();
			} catch (IOException ioex) {
				removeList.add(socket);
			}
		}
		for (Socket socket : removeList) {
			mSocketList.remove(socket);
		}
	}
}
