package com.example.administrator.wifidirectapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Activity mActivity;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mInfoListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager,
                                       WifiP2pManager.Channel channel,
                                       Activity activity,
                                       WifiP2pManager.PeerListListener peerListListener,
                                       WifiP2pManager.ConnectionInfoListener infoListener
    ) {
        this.mManager = manager;
        this.mChannel = channel;
        this.mPeerListListener = peerListListener;
        this.mActivity = activity;
        this.mInfoListener = infoListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        /*check if the wifi is enable*/
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            Log.d("Hello", "P2P state changed - " + state);
        }

        /*get the list*/
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            /*
            *请求来自wifi p2p管理器的可用对等体。
            *这是一个异步调用，
            * 调用活动通过回调通知PeerListListener.onPeersAvailable（）
            */
            mManager.requestPeers(mChannel, mPeerListListener);
            Log.d("Hello","Peers changed");
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int State = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if (State == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)
                Toast.makeText(mActivity, "搜索开启", Toast.LENGTH_SHORT).show();
            else if (State == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED)
                Toast.makeText(mActivity, "搜索已关闭", Toast.LENGTH_SHORT).show();

        }
        /*Respond to new connection or disconnections*/
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                Log.i("Hello", "已连接");
                mManager.requestConnectionInfo(mChannel, mInfoListener);
            } else {
                Log.i("Hello", "断开连接");
            }
        }

        /*Respond to this device's wifi state changing*/
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }
    }
}
