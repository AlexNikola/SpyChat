/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.mobileconnectors.s3.transferutility;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.incode_it.spychat.C;
import com.incode_it.spychat.QuickstartPreferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class that tracks active transfers. It has a static map that holds weak
 * references of {@link TransferListener}.
 */
class TransferStatusUpdater {
    private static final String TAG = "TransferStatusUpdater";
    /**
     * Some temporary states that should not be notified.
     */
    private static HashSet<TransferState> STATES_NOT_TO_NOTIFY = new HashSet<TransferState>(
            Arrays.asList(TransferState.PART_COMPLETED,
                    TransferState.PENDING_CANCEL, TransferState.PENDING_PAUSE,
                    TransferState.PENDING_NETWORK_DISCONNECT));
    /**
     * The threshold to update progress in milliseconds to prevent triggering
     * listeners too often.
     */
    private static final int UPDATE_THRESHOLD_MS = 1000;

    /**
     * A map of listeners.
     */
    static final Map<Integer, List<TransferListener>> listeners = new HashMap<>();

    /**
     * A map of active transfers.
     */
    private final Map<Integer, TransferRecord> transfers;
    /**
     * A map of transfer to its last update time to prevent events from being
     * fired too often.
     */
    private final Map<Integer, Long> lastUpdateTime;
    /**
     * Database util to update transfer status.
     */
    private final TransferDBUtil dbUtil;
    /**
     * The handler of main thread that runs callbacks.
     */
    private final Handler mainHandler;

    /**
     * This class is instantiated by TransferService.
     */
    TransferStatusUpdater(TransferDBUtil dbUtil) {
        this.dbUtil = dbUtil;
        mainHandler = new Handler(Looper.getMainLooper());
        transfers = new HashMap<>();
        lastUpdateTime = new HashMap<>();
    }

    /**
     * Gets all active transfers.
     *
     * @return an unmodifiable map of transfers
     */
    Map<Integer, TransferRecord> getTransfers() {
        return Collections.unmodifiableMap(transfers);
    }

    /**
     * Adds an active transfer for tracking.
     *
     * @param transfer a transfer object
     */
    void addTransfer(TransferRecord transfer) {
        transfers.put(transfer.id, transfer);
    }

    /**
     * Gets an active transfer by id.
     *
     * @param id id of the transfer
     * @return transfer if exists, null otherwise
     */
    TransferRecord getTransfer(int id) {
        return transfers.get(id);
    }

    /**
     * Removes a transfer from tracking.
     *
     * @param id id of the transfer to remove
     */
    void removeTransfer(int id) {
        transfers.remove(id);
        listeners.remove(id);
        lastUpdateTime.remove(id);
    }

    /**
     * Updates the state of an active transfer. If the transfer isn't tracked,
     * i.e. not active, it won't do anything. It writes the status of the
     * transfer, including current state, bytes transfer, bytes total, etc into
     * database. It then triggers
     * {@link TransferListener#onStateChanged(int, TransferState)} event of
     * associated listeners if new state is different.
     * <p>
     * Note that when the newState is CANCELED, COMPLETED, or FAILED, associated
     * listeners will be removed after being invoked. Make sure you call
     * throwError before changing the state to one of these, or else listeners
     * won't be invoked.
     *
     * @param id id of the transfer to update
     * @param newState new state
     */
    void updateState(final int id, final TransferState newState, Context context) {
        boolean shouldNotNotify = STATES_NOT_TO_NOTIFY.contains(newState);
        TransferRecord transfer = transfers.get(id);
        if (transfer == null) {
            // still wants to save state
            if (dbUtil.updateState(id, newState) == 0) {
                Log.w(TAG, "Failed to update the status of transfer " + id);
            }
        } else {
            shouldNotNotify |= newState.equals(transfer.state);
            transfer.state = newState;
            // save to database
            if (dbUtil.updateTransferRecord(transfer) == 0) {
                Log.w(TAG, "Failed to update the status of transfer " + id);
            }
        }

        //sendBroadcast(context);
        if (shouldNotNotify) {
            return;
        }

        // invoke listeners
        final List<TransferListener> list = listeners.get(id);
        if (list == null || list.isEmpty()) {
            return;
        }


        // invoke on main thread
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (TransferListener l : list) {
                    l.onStateChanged(id, newState);
                }
                // remove all listeners when the transfer is in a final state so
                // as to release resources asap.
                if (TransferState.COMPLETED.equals(newState)
                        || TransferState.FAILED.equals(newState)
                        || TransferState.CANCELED.equals(newState)) {
                    list.clear();
                }
            }
        });
    }

    private void sendBroadcast(Context context)
    {
        Intent intent = new Intent(QuickstartPreferences.UPLOAD_MEDIA);
        //intent.putExtra(C.EXTRA_MESSAGE_ID, messageId);
        intent.putExtra(C.EXTRA_MEDIA_STATE, "s");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Updates the transfer progress of a transfer. It will trigger
     * {@link TransferListener#onProgressChanged(int, long, long)} of associated
     * listeners if the update exceeds either time threshold.
     *
     * @param id id of the transfer
     * @param bytesCurrent current transferred bytes
     * @param bytesTotal total bytes
     */
    void updateProgress(final int id, final long bytesCurrent, final long bytesTotal) {
        float curr = bytesCurrent;
        float total = bytesTotal;
        float percentage = (curr/total * 100f);
        //Log.d("amaz_upload", "lib_updateProgress " + percentage);
        TransferRecord transfer = transfers.get(id);
        if (transfer != null) {
            transfer.bytesCurrent = bytesCurrent;
            transfer.bytesTotal = bytesTotal;
        }

        // invoke listeners
        final List<TransferListener> list = listeners.get(id);
        if (list == null || list.isEmpty()) {
            return;
        }

        // Don't fire off the update too frequently, but still fire when it
        // comes to the last byte.
        long timeInMillis = System.currentTimeMillis();
        if (!lastUpdateTime.containsKey(id)
                || timeInMillis - lastUpdateTime.get(id) > UPDATE_THRESHOLD_MS
                || bytesCurrent == bytesTotal) {
            lastUpdateTime.put(id, timeInMillis);

            // invoke on main thread
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (TransferListener l : list) {
                        l.onProgressChanged(id, bytesCurrent, bytesTotal);
                    }
                }
            });
        }
    }



    /**
     * Throws an error to transfer. It triggers
     * {@link TransferListener#onError(int, Exception)} of associated listeners.
     *
     * @param id id of the transfer
     * @param e an exception object
     */
    void throwError(final int id, final Exception e) {
        // invoke listeners
        final List<TransferListener> list = listeners.get(id);
        if (list == null || list.isEmpty()) {
            return;
        }
        // invoke on main thread
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (TransferListener l : list) {
                    l.onError(id, e);
                }
            }
        });
    }

    /**
     * Clears all transfers, listeners, etc.
     */
    void clear() {
        listeners.clear();
        transfers.clear();
        lastUpdateTime.clear();
    }

    /**
     * Registers a {@link TransferListener} to a transfer.
     *
     * @param id id of the transfer
     * @param listener a listener object
     */
    static void registerListener(int id, TransferListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener can't be null");
        }
        synchronized (listeners) {
            List<TransferListener> list = listeners.get(id);
            if (list == null) {
                list = new CopyOnWriteArrayList<TransferListener>();
                list.add(listener);
                listeners.put(id, list);
            } else {
                // don't add the same listener more than once
                if (!list.contains(listener)) {
                    list.add(listener);
                }
            }
        }
    }

    /**
     * Unregisters the listener from the transfer.
     *
     * @param id id of the transfer
     * @param listener a listener object
     */
    static void unregisterListener(int id, TransferListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener can't be null");
        }
        List<TransferListener> list = listeners.get(id);
        if (list == null || list.isEmpty()) {
            return;
        }
        list.remove(listener);
    }

    /**
     * A helper class that updates transfer progress.
     */
    private class TransferProgressListener implements ProgressListener {

        private final TransferRecord transfer;

        public TransferProgressListener(TransferRecord transfer) {
            this.transfer = transfer;
        }

        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            // TODO: handle retry of upload where progress could be problematic.
            if (progressEvent.getBytesTransferred() > 0) {
                transfer.bytesCurrent += progressEvent.getBytesTransferred();
                updateProgress(transfer.id, transfer.bytesCurrent, transfer.bytesTotal);
            }
        }
    }

    /**
     * Creates a {@link ProgressListener} for the given transfer with current
     * transferred bytes and total bytes.
     *
     * @param id id of the transfer
     * @return a progress listener
     */
    ProgressListener newProgressListener(int id) {
        TransferRecord transfer = getTransfer(id);
        if (transfer == null) {
            throw new IllegalArgumentException("transfer " + id + " doesn't exist");
        }
        return new TransferProgressListener(transfer);
    }
}
