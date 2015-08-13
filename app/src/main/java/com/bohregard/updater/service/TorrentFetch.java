package com.bohregard.updater.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.DHT;
import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;

import java.io.File;

/**
 * @author Anthony
 */
public class TorrentFetch extends Service {

    private Session mSession;
    private DHT mDht;
    private boolean mStarted;
    private final String TAG = TorrentFetch.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    boolean mAllowRebind;

    public void startSession() {
        if (!mStarted) {
            Log.d(TAG, "Session not started...");
            mSession = new Session();
            mDht = new DHT(mSession);
            mDht.start();
            mStarted = true;
            setSessionListener();
        }
    }

    public void setSessionListener(){
        Log.d(TAG, "Adding listener...");
        mSession.addListener(alertListener);
    }

    private AlertListener alertListener = new AlertListener() {
        @Override
        public int[] types() {
            return new int[] {AlertType.ADD_TORRENT.getSwig()};
        }

        @Override
        public void alert(Alert<?> alert) {
//            Log.d(TAG, "Alert: " + alert.getType() + " Message: " + alert.getMessage());
            TorrentAlert<?> ta = (TorrentAlert<?>) alert;
            final TorrentHandle th = ta.getHandle();

            switch(alert.getType()){
                case ADD_TORRENT:
//                        th.setSequentialDownload(true);
                    th.resume();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int i, int i1) {
        Log.d(TAG, "onStartCommand");
        startSession();
        return super.onStartCommand(intent, i, i1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ON DESTROY");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        startSession();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }

    public void addTorrent(File file, File dir) {
        mSession.asyncAddTorrent(file, dir);
    }

    public class LocalBinder extends Binder {
        public TorrentFetch getService() {
            return TorrentFetch.this;
        }
    }

    public void torrentSessionStats() {
        Log.d(TAG, "***********************");
        Log.d(TAG, "LibTorrent Version: " + LibTorrent.version());
        Log.d(TAG, "Session DHT: " + mSession.isDHTRunning());
        Log.d(TAG, "Session Listening: " + mSession.isListening() + " on " + mSession.getListenPort());
        Log.d(TAG, "Session Paused: " + mSession.isPaused());
        for (TorrentHandle torrentHandle : mSession.getTorrents()) {
            int torrentFiles = torrentHandle.getTorrentInfo().getNumFiles();
            int p = (int) (torrentHandle.getStatus().getProgress() * 100);
            Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~");
            Log.d(TAG, "Torrent Name: " + torrentHandle.getName());
            Log.d(TAG, "Torrent State: " + torrentHandle.getStatus().getState());
            Log.d(TAG, "Torrent Error: " + torrentHandle.getStatus().getError());
            Log.d(TAG, "Torrent Progress: " + p);
            Log.d(TAG, "Torrent Download Rate: " + torrentHandle.getStatus().getDownloadRate());
            Log.d(TAG, "Torrent Seeds: " + torrentHandle.getStatus().getNumSeeds());
            Log.d(TAG, "Torrent Peers: " + torrentHandle.getStatus().getNumPeers());
            Log.d(TAG, "Torrent Files: " + torrentFiles);
            Log.d(TAG, "Torrent Added Time: " + torrentHandle.getStatus().getAddedTime());
            Log.d(TAG, "Torrent Completed Time: " + torrentHandle.getStatus().getCompletedTime());
            for (int i = 0; i < torrentFiles; i++) {
                try {
                    Log.d(TAG, "File " + (i + 1) + ": " + torrentHandle.getTorrentInfo().getFiles().getFileName(i));
                } catch (Exception e) {
                    Log.e(TAG, "File not found?");
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Torrent Pieces: " + torrentHandle.getTorrentInfo().getNumPieces());
            Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~");
        }
        Log.d(TAG, "***********************");
    }

    public Session getSession(){
        return mSession;
    }
}
