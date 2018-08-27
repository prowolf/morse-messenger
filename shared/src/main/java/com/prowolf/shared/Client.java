package com.prowolf.shared;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;

public class Client implements Runnable {

    private URI connection;

    private LinkedBlockingQueue<String> receivedQueue;
    private LinkedBlockingQueue<String> sendQueue;
    private volatile boolean isRunning;
    private static final int NUM_RETRIES = 3;

    private static final String LOG_TAG = "Client";

    public Client(String uri) {
        try {
            this.connection = new URI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        receivedQueue = new LinkedBlockingQueue<>();
        sendQueue = new LinkedBlockingQueue<>();
    }

    public String getNextMessage() {
        return receivedQueue.poll();
    }

    public boolean sendMessage(String message) {
        return sendQueue.offer(message);
    }

    public void run() {
        int connectionFailures = 0;
        while (connectionFailures < NUM_RETRIES) {
            try {
                Socket socket = new Socket(connection.getHost(),
                        connection.getPort());
                // from server
                final BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
                // to server
                final PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);
                connectionFailures = 0;
                final Client client = this;
                Thread inputReader = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            while (!interrupted() && client.isRunning())
                                receivedQueue.put(inputStream.readLine());
                        }
                        catch (java.io.IOException ex)
                        {
                            Log.w(LOG_TAG, "IO Exception: ", ex);
                        }
                        catch (InterruptedException ex)
                        {
                            Log.i(LOG_TAG, "Receiving thread interrupted.");
                            Thread.currentThread().interrupt();
                        }
                        catch(Exception ex)
                        {
                            Log.e(LOG_TAG, "Unexpected exception in receiving thread:", ex);
                        }
                        client.setRunning(false);
                        synchronized (client)
                        {
                            client.notifyAll();
                        }
                    }
                };
                Thread outputWriter = new Thread() {
                    public void run() {
                        try {
                            while (!interrupted() && client.isRunning())
                                outputStream.printf(sendQueue.take());
                        } catch (InterruptedException ex) {
                            Log.i(LOG_TAG, "sending thread interrupted.");
                            Thread.currentThread().interrupt();
                        } catch (Exception ex) {
                            Log.e(LOG_TAG, "Unexpected exception in sending thread:", ex);
                        }
                        client.setRunning(false);
                        synchronized (client) {
                            client.notifyAll();
                        }
                    }
                };

                setRunning(true);
                inputReader.start();
                outputWriter.start();
                //wait for the threads to finish, they never should
                synchronized (client) {
                    while (isRunning())
                        wait();
                }

                // its not clear to us why the threads are stopping.
                // force them to stop so we can recreate
                inputReader.interrupt();
                outputWriter.interrupt();

                // Annoyingly, thread.interrupt() does not stop a blocking buffered stream
                // readline() io call. Close the socket to force the blocking call to end.
                socket.close();
                // wait for them to stop then attempt to recreate.
                inputReader.join();
                outputWriter.join();
            } catch (java.io.IOException ex) {
                setRunning(false);
                Log.w(LOG_TAG, "IO Exception:", ex);
                connectionFailures++;
            } catch (InterruptedException ex) {
                setRunning(false);
                Log.e(LOG_TAG, "Thread interrupted");
                Thread.currentThread().interrupt();
                return;
            }
            //wait 1 second then attempt to reconnect
            try {
                Thread.sleep(1000);
                Log.d(LOG_TAG, "attempting to reconnect");
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, "retry loop interrupted");
                Thread.currentThread().interrupt();
                return;
            }
        }
        Log.d(LOG_TAG, "failed to connect");
    }

    private boolean isRunning()
    {
        return isRunning;
    }

    synchronized private void setRunning(boolean running)
    {
        isRunning = running;
    }

    public URI getConnection() {
        return connection;
    }

}