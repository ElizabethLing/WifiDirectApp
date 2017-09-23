package com.example.administrator.wifidirectapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity  {

        private Button discover;
        private Button stopdiscover;
        private Button stopconnect;
        private Button sendpicture;
        private Button senddata;
        private Button begroupowner;

        private RecyclerView mRecyclerView;
        private MyAdapter mAdapter;
        private List peers = new ArrayList();
        private List<HashMap<String, String>> peersshow = new ArrayList();

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private BroadcastReceiver mReceiver;
        private IntentFilter mFilter;
        private WifiP2pInfo info;

        private FileServerAsyncTask mServerTask;
        private DataServerAsyncTask mDataTask;

        @Override
   protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            initView();
            initIntentFilter();
            initReceiver();
            initEvents();
   }

      private void initView() {

        begroupowner= (Button) findViewById(R.id.bt_bgowner);
        stopdiscover = (Button) findViewById(R.id.bt_stopdiscover);
        discover = (Button) findViewById(R.id.bt_discover);
        stopconnect = (Button) findViewById(R.id.bt_stopconnect);
        sendpicture = (Button) findViewById(R.id.bt_sendpicture);
        senddata = (Button) findViewById(R.id.bt_senddata);
        sendpicture.setVisibility(View.GONE);
        senddata.setVisibility(View.GONE);


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new MyAdapter(peersshow);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
    }
      private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }
      private void initReceiver() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {
                peers.clear();
                peersshow.clear();
                Collection<WifiP2pDevice> aList = peersList.getDeviceList();
                peers.addAll(aList);

                for (int i = 0; i < aList.size(); i++) {
                    WifiP2pDevice a = (WifiP2pDevice) peers.get(i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", a.deviceName);
                    map.put("address", a.deviceAddress);
                    peersshow.add(map);
                }
                mAdapter = new MyAdapter(peersshow);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        CreateConnect(peersshow.get(position).get("address"),
                                peersshow.get(position).get("name"));
                    }

                    @Override
                    public void OnItemLongClick(View view, int position) {

                    }
                });
            }
        };

        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {

            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo minfo) {

                Log.i("Hello", "InfoAvailable is on");
                info = minfo;
                TextView view = (TextView) findViewById(R.id.tv_main);
                if (info.groupFormed && info.isGroupOwner) {
                    Log.i("Hello", "owner start");

                    mServerTask = new FileServerAsyncTask(MainActivity.this, view);
                    mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    mDataTask = new DataServerAsyncTask(MainActivity.this, view);
                    mDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else if (info.groupFormed) {
                    SetButtonVisible();
                }
            }
        };
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListerner, mInfoListener);
    }
      private void initEvents() {

            discover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DiscoverPeers();
                }
            });
            begroupowner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BeGroupOwner();
                }
            });

            stopdiscover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StopDiscoverPeers();
                }
            });
            stopconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StopConnect();
                }
            });
            sendpicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 20);

                }
            });

            senddata.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent serviceIntent = new Intent(MainActivity.this,
                            DataTransferService.class);

                    serviceIntent.setAction(DataTransferService.ACTION_SEND_FILE);

                    serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    Log.i("address", "groupOwnerIP is " + info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_PORT,8888);
                    MainActivity.this.startService(serviceIntent);
                }
            });


            mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
                @Override
                public void OnItemClick(View view, int position) {
                    CreateConnect(peersshow.get(position).get("address"),
                            peersshow.get(position).get("name"));
                }

                @Override
                public void OnItemLongClick(View view, int position) {
                }
            });
        }

        private void BeGroupOwner() {
            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 20) {
                super.onActivityResult(requestCode, resultCode, data);
                Uri uri = data.getData();
                Intent serviceIntent = new Intent(MainActivity.this,
                        FileTransferService.class);

                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
                        uri.toString());

                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,info.groupOwnerAddress.getHostAddress());
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
                MainActivity.this.startService(serviceIntent);
            }
        }

        private void StopConnect() {
            SetButtonGone();
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }

           /** 一个基于API的演示基础，可以通过wifidirect连接android设备
            * 可以通过套接字发送文件或数据
            * 设置哪个设备是客户端或服务
            */
        private void CreateConnect(String address, final String name) {
            WifiP2pDevice device;
            WifiP2pConfig config = new WifiP2pConfig();
            Log.i("Hello", address);

            config.deviceAddress = address;
            /*MAC Address*/

            config.wps.setup = WpsInfo.PBC;
            Log.i("address", "MAC IS " + address);
            if (address.equals("50:01:d9:ba:03:24")) {
                config.groupOwnerIntent = 0;
                Log.i("address", "HuaWei");
            }
            if (address.equals("00:0a:f5:7d:2e:80")) {
                config.groupOwnerIntent = 15;
                Log.i("address", "ChangHog");

            }

            Log.i("address", "WeiZhi" + String.valueOf(config.groupOwnerIntent));

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {
                }
            });
        }

        private void StopDiscoverPeers() {
            mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                }
            });
        }

        private void SetButtonVisible() {
            sendpicture.setVisibility(View.VISIBLE);
            senddata.setVisibility(View.VISIBLE);
        }

        private void SetButtonGone() {
            sendpicture.setVisibility(View.GONE);
            senddata.setVisibility(View.GONE);
        }

        private void DiscoverPeers() {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                }
            });
        }

        @Override
        protected void onResume() {
            super.onResume();
            registerReceiver(mReceiver, mFilter);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.i("Hello", "lalalalala");
            unregisterReceiver(mReceiver);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            StopConnect();
        }

    }


