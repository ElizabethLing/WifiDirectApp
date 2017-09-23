package com.example.administrator.wifidirectapp;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DataServerAsyncTask extends AsyncTask<Void,Void,String> {
    private TextView statusText;
    private MainActivity activity;

    public DataServerAsyncTask(MainActivity activity, View statusText) {
        this.statusText = (TextView) statusText;
        this.activity=activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            Log.i("Hello", "data doinback");
            ServerSocket serverSocket = new ServerSocket(8888);

            Log.i("Hello","串口创建完成");
            Socket client = serverSocket.accept();
            Log.i("Hello","阻塞已取消");
            InputStream inputstream = client.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i;
            while ((i = inputstream.read()) != -1) {
                baos.write(i);
            }

            String str = baos.toString();
            serverSocket.close();
            return str;

        } catch (IOException e) {
            Log.e("Hello", e.toString());
            return null;
        }
    }


    @Override
    protected void onPostExecute(String result) {

        Log.i("Hello", "data onpost");

        Toast.makeText(activity, "result"+result, Toast.LENGTH_SHORT).show();

        if (result != null) {
            statusText.setText("Data-String is " + result);
        }
    }

    @Override
    protected void onPreExecute() {

    }
}
