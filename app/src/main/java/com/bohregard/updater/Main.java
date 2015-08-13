package com.bohregard.updater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;

import com.bohregard.updater.service.TorrentFetch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Anthony
 */
public class Main extends Activity {

    private final String TAG = Main.class.getSimpleName();
    TorrentFetch mService;
    final public String UPDATE_DIRECTORY = Environment.getExternalStorageDirectory() + "/.update";
    final public String HSTREAM_DIRECTORY = UPDATE_DIRECTORY + "/hstream/";
    private Session mSession;
    private int count;
    private LinearLayout linearLayout;
    private List<TextView> torrentNames;
    private List<ProgressBar> torrentProgresses;

    private HashMap<String, Integer> torrents;
    private Context mContext;

    private AlertListener alertListener = new AlertListener() {
        @Override
        public int[] types() {
            return new int[]{AlertType.ADD_TORRENT.getSwig(), AlertType.PIECE_FINISHED.getSwig(), AlertType.STATE_CHANGED.getSwig()};
        }

        @Override
        public void alert(Alert<?> alert) {
            TorrentAlert<?> ta = (TorrentAlert<?>) alert;
            TorrentHandle th = ta.getHandle();

            final int p;
            final int index;

            switch (alert.getType()) {
                case ADD_TORRENT:
                    Log.e(TAG, "Main Activity");
                    break;
                case STATE_CHANGED:
                case PIECE_FINISHED:
                    p = (int) (th.getStatus().getProgress() * 100);
                    System.out.println("Progress: " + p);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            torrentProgresses.get(0).setProgress(p);
                        }
                    });

                    Log.e(TAG, "Message: " + alert.getMessage());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        linearLayout = (LinearLayout) findViewById(R.id.content);
        torrentNames = new ArrayList<>();
        torrentProgresses = new ArrayList<>();

        torrents = new HashMap<>();

        Intent service = new Intent(this, TorrentFetch.class);
        startService(service);

        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Intent service = new Intent(this, TorrentFetch.class);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ON DESTROY");
    }

    private void updateUi() {
        linearLayout.removeAllViews();
        count = 0;

        mSession = mService.getSession();
        mSession.addListener(alertListener);
        for (TorrentHandle torrent : mSession.getTorrents()) {
            Log.d(TAG, "Torrent: " + torrent.getName());

            TextView name = new TextView(this);
            name.setText("Name: " + torrent.getName());

            ProgressBar progressBar = new ProgressBar(mContext, null, android.R.attr
                    .progressBarStyleHorizontal);
            progressBar.setProgress(0);

            torrentNames.add(count, name);
            torrentProgresses.add(count, progressBar);

            linearLayout.addView(torrentNames.get(count));
            linearLayout.addView(torrentProgresses.get(count));
            torrents.put(torrent.getName(), count);
            count++;
        }
    }

    @Override
    protected void onActivityResult(int i, int i1, Intent intent) {
        super.onActivityResult(i, i1, intent);
        Log.d(TAG, "Intent: " + intent);
        try {
            Log.d(TAG, "Intent Data: " + intent.getData());
            File file = new File(intent.getData().getPath());
            File dir = new File(HSTREAM_DIRECTORY);
            mService.addTorrent(file, dir);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    public void update(View view) {
        mService.torrentSessionStats();
    }

    public void fileBrowse(View view) {
        Log.d(TAG, "DEES NUTS");
        showFileChooser();
    }

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void serviceCheck(View view) {
        Intent service = new Intent(this, TorrentFetch.class);
        startService(service);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TorrentFetch.LocalBinder binder = (TorrentFetch.LocalBinder) iBinder;
            mService = binder.getService();
            updateUi();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
}
