package com.prowolf.shared;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class IPTelegraph extends Telegraph {

    private Thread networkingThread;
    private ConnectivityManager connectivityManager;

    private LinkedBlockingQueue<String> sendQueue;

    public IPTelegraph(TelegraphHandler telegraphHandler, ConnectivityManager cm) {
        super(telegraphHandler);
        sendQueue = new LinkedBlockingQueue<>();
        connectivityManager = cm;
    }

    @Override
    protected boolean doConnect(Object target) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (target instanceof URI && activeNetwork != null && activeNetwork.isConnected()) {
            final URI uri = (URI)target;
            for (int attempt = 0; onAttemptConnect(attempt); attempt++) {
                networkingThread = new Thread() {
                    public void run() {
                        try {
                            Socket socket = new Socket(uri.getHost(), uri.getPort());
                            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
                            PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);

                            while (!interrupted() && isConnected()) {
                                String incoming = inputStream.readLine();
                                switch (incoming) {
                                    case "t":
                                        setIncoming(true);
                                        break;
                                    case "f":
                                        setIncoming(false);
                                        break;
                                    default:
                                        break;
                                }

                                outputStream.printf(sendQueue.take());
                            }

                            socket.close();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    }
                };

                networkingThread.start();

                return true;
            }
        }

        return false;
    }

    abstract protected boolean onAttemptConnect(int attempt);

    @Override
    protected void doDisconnect() {
        networkingThread.interrupt();
    }

    @Override
    protected void onOutgoing(boolean outgoing) {
        sendQueue.offer(outgoing ? "t" : "f");
    }

}
