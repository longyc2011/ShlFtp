
package com.setvens.shlftp;

public interface IRetrieveListener {
    void onStart();

    void onTrack(long nowOffset);

    void onError(Object obj, int type);

    void onCancel(Object obj);

    void onDone();
}
