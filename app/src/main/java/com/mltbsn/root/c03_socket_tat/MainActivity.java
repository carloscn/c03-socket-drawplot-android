package com.mltbsn.root.c03_socket_tat;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


public class MainActivity extends AppCompatActivity {

    private Button mButtonSocketConnect;
    private Button mButtonSocketDiskconnect;
    private EditText mEditTextIp;
    private EditText mEditTextPort;
    private EditText mEditTextTemp;
    private EditText mEditTextHumi;
    private LineChart lineChartTemp;
    private LineChart lineChartHumi;
    private DynamicLineChartManager dynamicLineChartManager1;
    private DynamicLineChartManager dynamicLineChartManager2;
    private List<Integer> list = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合
    private int chartCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chartCount = 0;
        setTitle("温湿度检测系统");
        mButtonSocketConnect = findViewById(R.id.btn_connect);
        mButtonSocketDiskconnect = findViewById(R.id.btn_disconnect);
        mEditTextIp = findViewById(R.id.lineEdit_ip);
        mEditTextPort = findViewById(R.id.lineEdit_port);
        mEditTextTemp = findViewById(R.id.lineEdit_temp);
        mEditTextHumi = findViewById(R.id.lineEdit_humi);
        lineChartTemp = findViewById(R.id.chartTemp);
        lineChartHumi = findViewById(R.id.chartHumi);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setListener();
        mButtonSocketConnect.setEnabled(true);
        mButtonSocketDiskconnect.setEnabled(false);
        names.add("温度");
        names.add("湿度");
        //折线颜色
        colour.add(Color.CYAN);
        colour.add(Color.GREEN);
        colour.add(Color.BLUE);

        dynamicLineChartManager1 = new DynamicLineChartManager(lineChartTemp, names.get(0), colour.get(0));
        dynamicLineChartManager2 = new DynamicLineChartManager(lineChartHumi, names, colour);

        dynamicLineChartManager1.setYAxis(100, 0, 10);
        dynamicLineChartManager2.setYAxis(100, 0, 10);

    }
    private final int[] colors = new int[] {
            Color.rgb(240, 255, 255),
            Color.rgb(193, 205, 205),
            Color.rgb(89, 199, 250),
            Color.rgb(250, 104, 104)
    };

    private void setListener() {
        OnClick onClick = new OnClick();
        mButtonSocketDiskconnect.setOnClickListener(onClick);
        mButtonSocketConnect.setOnClickListener(onClick);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int count = 0;
            String msgStr = new String((String)msg.obj);
            for (int i = 0; i < msgStr.length(); i ++) {
                if (msgStr.charAt(i) == '#') {
                    count ++;
                }
            }
            if (count >= 3) {
                String[] strList;
                strList = ((String) msg.obj).split("#");
                mEditTextTemp.setText(strList[1]);
                mEditTextHumi.setText(strList[2]);
                float temp = (Float.parseFloat(strList[1]) );
                float humi = (Float.parseFloat(strList[2]) );
                dynamicLineChartManager1.addEntry( (temp));
                dynamicLineChartManager2.addEntry( (humi));
            }
        }
    };
    public class SocketMsgTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String host = mEditTextIp.getText().toString().trim();
                int port = Integer.parseInt(mEditTextPort.getText().toString());
                Log.e("MSG", "host:" + host + " port: " + mEditTextPort.getText().toString());
                Socket socket = new Socket(host, port);
                OutputStream writer;
                InputStream inputStream = socket.getInputStream();
                DataInputStream input = new DataInputStream(inputStream);
                writer = socket.getOutputStream();
                //写入要发送给服务器的数据
                //writer.write("Hello World".getBytes());
                //writer.flush();
                byte[] b = new byte[20];
                while (true) {
                    int length = input.read(b);
                    String Msg = new String(b, 0, length, "gb2312");
                    Log.v("data", Msg);
                    Message msg = new Message();
                    msg.obj = Msg;
                    (MainActivity.this).mHandler.sendMessage(msg);
                    if (isCancelled()) {
                        socket.close();
                        writer.close();
                        input.close();
                        return null;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }
    }
    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = null;
            SocketMsgTask task = new SocketMsgTask();
            switch (v.getId()) {
                case R.id.btn_connect:
                    mButtonSocketConnect.setEnabled(false);
                    mButtonSocketDiskconnect.setEnabled(true);
                    task.execute();
                    break;
                case R.id.btn_disconnect:
                    mButtonSocketConnect.setEnabled(true);
                    mButtonSocketDiskconnect.setEnabled(false);
                    if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                        task.cancel(true);
                    }
                    break;
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
