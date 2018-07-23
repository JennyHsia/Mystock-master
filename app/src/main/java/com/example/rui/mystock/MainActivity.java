package com.example.rui.mystock;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.rui.mystock.com.example.rui.mystock.data.StockInfo;
import com.example.rui.mystock.com.example.rui.mystock.data.TradeAccountInfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static HashMap<String,TradeAccountInfo> mTradeInfo;

    private static Vector<String> mSelectedStockItems = new Vector();
    private final static int BackgroundColor = Color.WHITE;
    private final static int HighlightColor = Color.rgb(210, 233, 255);
    private final static int TitleColor = Color.rgb(0, 191, 255);
    private final static String ShIndex = "sh000001";
    private final static String SzIndex = "sz399001";
    private final static String ChuangIndex = "sz399006";

    //sp
    private final static String KEY_STOCK_IDS = "StockIds";
    private final static String KEY_ACCOUNT_INFO = "AccountInfo";
    private final static String KEY_SUM_PRICE = "sum_price_key";
    private final static String KEY_MAX_LOSS_RATE = "max_loss_key";


    private String mNowAccount;
    private int mNowPrice;
    private int mNowRate;

    String FILE_NAME = "TradeInfo.txt";
    String SDCARD_ROOT = Environment.getExternalStorageDirectory()
            .getAbsolutePath();//SD卡根目录路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAccountInfo();

        initViews();

        Timer timer = new Timer("RefreshStocks");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshStocks();
            }
        }, 0, 10000); // 10 seconds

    }

    private void saveTradeInfo(){
        File file = new File(SDCARD_ROOT, FILE_NAME);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            String infos = JSON.toJSONString(mTradeInfo);
            fos.write(infos.getBytes());
        } catch (Exception e) {
            Log.e(TAG,
                    "saveTradeInfo with exception : " + e.getMessage());
        }
    }
    
    private void deleteAccountInfo(String account){
        mTradeInfo.remove(account);
    }
    private void deleteStockInfo(String account, String id){
        TradeAccountInfo tradeAccountInfo = mTradeInfo.get(account);
        if (tradeAccountInfo != null) {
            tradeAccountInfo.stockInfos.remove(id);
        }
    }
    private void getAccountInfo(){
        File file = new File(SDCARD_ROOT,FILE_NAME);
        if (!file.exists()) {
            return;
        }else{
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));
                String accountStr = br.readLine();
                mTradeInfo = (HashMap<String, TradeAccountInfo>) TradeAccountInfo.parseTradesString(accountStr);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "getAccountInfo,FileNotFoundException:"+e.getMessage(),e);
                return;
            } catch (IOException e) {
                Log.e(TAG, "getAccountInfo,IOException:"+e.getMessage(),e);
                file.delete();
                return;
            }finally {
                if (mTradeInfo == null) {
                    mTradeInfo = new HashMap<>();
                }
            }
        }
    }
    private void initViews() {
        EditText editName = (EditText)findViewById(R.id.edit_account);
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mNowAccount = s.toString();
            }
        });
        EditText editSum = (EditText)findViewById(R.id.edit_sum);
        editSum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    mNowPrice = 0;
                } else {
                    mNowPrice = Integer.parseInt(s.toString());
                }
            }
        });
        EditText editRatio = (EditText)findViewById(R.id.edit_ratio);
        editRatio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    mNowRate = 0;
                } else {
                    mNowRate = Integer.parseInt(s.toString());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveTradeInfo();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        saveTradeInfo();
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_delete){
            mTradeInfo.clear();
            TableLayout table = (TableLayout)findViewById(R.id.stock_table);
            table.removeAllViews();
        }

        return super.onOptionsItemSelected(item);
    }


    // 浦发银行,15.06,15.16,15.25,15.27,14.96,15.22,15.24,205749026,3113080980,
    // 51800,15.22,55979,15.21,1404740,15.20,1016176,15.19,187800,15.18,300,15.24,457700,15.25,548900,15.26,712266,15.27,1057960,15.28,2015-09-10,15:04:07,00
    public class Stock {
        public String id, name;
        public String price;
        public String cost;
        public String quantity;
    }



    public void sinaResponseToStocks(String account, String response){
        response = response.replaceAll("\n", "");
        String[] stocks = response.split(";");
        for(String stock : stocks) {
            String[] leftRight = stock.split("=");
            if (leftRight.length < 2)
                continue;

            String right = leftRight[1].replaceAll("\"", "");
            if (right.isEmpty())
                continue;

            String left = leftRight[0];
            if (left.isEmpty())
                continue;

            String id = left.split("_")[2];

            String[] values = right.split(",");
            String name = values[0];
            String price = values[1];

            TradeAccountInfo info = mTradeInfo.get(account);
            if (info != null && info.stockInfos.containsKey(id)) {
                StockInfo stockInfo = info.stockInfos.get(id);
                stockInfo.name = name;
                stockInfo.price = price;
            }
        }
    }

    public void querySinaStocks(final String account, String list){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://hq.sinajs.cn/list=" + list;
        //http://hq.sinajs.cn/list=sh600000,sh600536

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        sinaResponseToStocks(account, response);
                        updateStockListView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "获取数据失败，检查下网络吧~",
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG,
                                "querySinaStocks with exception : " + error.getMessage());
                    }
                });

        queue.add(stringRequest);
    }

    private void refreshStocks(){
        if (mTradeInfo == null)  return;
        for (TradeAccountInfo info : mTradeInfo.values()) {
            String ids = "";
            for (StockInfo stockInfo : info.stockInfos.values()){
                ids += stockInfo.id;
                ids += ",";
            }
            if (!TextUtils.isEmpty(ids)) {
                querySinaStocks(info.name, ids);
            }
        }
    }

    public void addStock(View view) {
        EditText stockIdEditText = (EditText) findViewById(R.id.stock_id);
        String stockId = stockIdEditText.getText().toString();
        stockIdEditText.setText("");
        if(stockId.length() != 6)
            return;

        if (stockId.startsWith("6")) {
            stockId = "sh" + stockId;
        } else if (stockId.startsWith("0") || stockId.startsWith("3")) {
            stockId = "sz" + stockId;
        } else
            return;

        String cost = null;
        String quantity = null;
        EditText costEditText = (EditText) findViewById(R.id.stock_cost);
        cost = costEditText.getText().toString();
        costEditText.setText("");

        EditText quantityEditText = (EditText) findViewById(R.id.stock_quantity);
        quantity = quantityEditText.getText().toString();
        quantityEditText.setText("");
        if(TextUtils.isEmpty(cost) || TextUtils.isEmpty(quantity))
            return;

        TradeAccountInfo tradeAccountInfo = null;
        if (mTradeInfo.containsKey(mNowAccount)) {
            tradeAccountInfo = mTradeInfo.get(mNowAccount);
        } else if (!TextUtils.isEmpty(mNowAccount) && mNowPrice != 0 && mNowRate != 0) {
            tradeAccountInfo = new TradeAccountInfo();
            tradeAccountInfo.name= mNowAccount;
            tradeAccountInfo.sumPrice= mNowPrice;
            tradeAccountInfo.maxLossRate= mNowRate;
            mTradeInfo.put(mNowAccount, tradeAccountInfo);
        }
        if (tradeAccountInfo != null) {
            StockInfo stockInfo = new StockInfo();
            stockInfo.id = stockId;
            stockInfo.cost = cost;
            stockInfo.quantity = quantity;
            tradeAccountInfo.stockInfos.put(stockId, stockInfo);
        }

        refreshStocks();


    }

    public void sendNotifation(int id, String title, String text){
        NotificationCompat.Builder nBuilder =
                new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.ic_launcher);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(text);
        nBuilder.setVibrate(new long[]{100, 100, 100});
        nBuilder.setLights(Color.RED, 1000, 1000);

        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.notify(id, nBuilder.build());
    }
    public void sendWarning(String text){
        NotificationCompat.Builder nBuilder =
                new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.caution_small);
        nBuilder.setContentTitle("警报");
        nBuilder.setContentText(text);
        nBuilder.setVibrate(new long[]{100, 100, 100});
        nBuilder.setLights(Color.RED, 1000, 1000);

        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.notify((int)(Math.random()*10000), nBuilder.build());
    }

    public void updateStockListView(){

        // Table
        final TableLayout table = (TableLayout)findViewById(R.id.stock_table);
        table.setStretchAllColumns(true);
        table.setShrinkAllColumns(true);
        table.removeAllViews();


        // Title
        TableRow rowTitle = new TableRow(this);

        TextView nameTitle = new TextView(this);
        nameTitle.setText(getResources().getString(R.string.stock_name_title));
        rowTitle.addView(nameTitle);

        TextView gainLossTitle = new TextView(this);
        gainLossTitle.setText(getResources().getString(R.string.stock_increase_percent_title));
        rowTitle.addView(gainLossTitle);
        table.addView(rowTitle);

        TextView priceTitle = new TextView(this);
        priceTitle.setText(getResources().getString(R.string.stock_price_title));
        rowTitle.addView(priceTitle);

        TextView nowTitle = new TextView(this);
        nowTitle.setText(getResources().getString(R.string.stock_now_title));
        rowTitle.addView(nowTitle);


        for (final TradeAccountInfo info : mTradeInfo.values()) {
            // Account name
            final TableRow accountRow = new TableRow(this);

            TextView accountName = new TextView(this);
            accountName.setText(info.name);
            accountRow.addView(accountName);

            TextView gl = new TextView(this);
            accountRow.addView(gl);

            TextView sum = new TextView(this);
            sum.setText(info.sumPrice+"("+info.maxLossRate+"%)");
            accountRow.addView(sum);

            accountRow.setBackgroundColor(TitleColor);
            accountRow.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    PopupMenu popupMenu = new PopupMenu(MainActivity.this,v);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_item,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            table.removeView(accountRow);
                            deleteAccountInfo(info.name);
                            return false;
                        }
                    });
                    popupMenu.show();
                    return false;
                }
            });
            table.addView(accountRow);

            Collection<StockInfo> stocks = info.stockInfos.values();
            double loss = 0;
            for(final StockInfo stock : stocks) {
                if (!isComplete(stock)) return;
                final TableRow row = new TableRow(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                if (mSelectedStockItems.contains(stock.id)){
                    row.setBackgroundColor(HighlightColor);
                }

                LinearLayout nameId = new LinearLayout(this);
                nameId.setOrientation(LinearLayout.VERTICAL);
                TextView name = new TextView(this);
                name.setText(stock.name);
                nameId.addView(name);
                TextView id = new TextView(this);
                id.setTextSize(10);
                id.setText(stock.id);
                nameId.addView(id);
                row.addView(nameId);

                TextView gainLoss = new TextView(this);
                double gL = (Double.parseDouble(stock.price) - Double.parseDouble(stock.cost))* Double.parseDouble(stock.quantity);
                gainLoss.setText(String.format("%+f",gL));
                int color = Color.BLACK;
                if(gL > 0) {
                    color = Color.RED;
                }
                else if(gL < 0){
                    color = Color.GREEN;
                }
                gainLoss.setTextColor(color);
                row.addView(gainLoss);

                LinearLayout pricelayout = new LinearLayout(this);
                pricelayout.setOrientation(LinearLayout.VERTICAL);
                TextView value = new TextView(this);
                double price = Double.parseDouble(stock.price) * Double.parseDouble(stock.quantity);
                value.setText(String.format("%.2f", price));
                pricelayout.addView(value);
                TextView quantity = new TextView(this);
                quantity.setTextSize(10);
                quantity.setText(stock.quantity);
                pricelayout.addView(quantity);
                row.addView(pricelayout);

                LinearLayout nowlayout = new LinearLayout(this);
                nowlayout.setOrientation(LinearLayout.VERTICAL);
                TextView now = new TextView(this);
                now.setText(stock.price);
                nowlayout.addView(now);
                TextView cost = new TextView(this);
                cost.setTextSize(10);
                cost.setText(stock.cost);
                nowlayout.addView(cost);
                row.addView(nowlayout);
//
//                TextView percent = new TextView(this);
//                percent.setGravity(Gravity.RIGHT);
//                TextView increaseValue = new TextView(this);
//                increaseValue.setGravity(Gravity.RIGHT);
//                Double dOpen = Double.parseDouble(stock.open_);
//                Double dB1 = Double.parseDouble(stock.bp1_);
//                Double dS1 = Double.parseDouble(stock.sp1_);
//                if(dOpen == 0 && dB1 == 0 && dS1 == 0) {
//                    percent.setText("--");
//                    increaseValue.setText("--");
//                }
//                else{
//                    Double dNow = Double.parseDouble(stock.now_);
//                    if(dNow == 0) {// before open
//                        if(dS1 == 0) {
//                            dNow = dB1;
//                            now.setText(stock.bp1_);
//                        }
//                        else {
//                            dNow = dS1;
//                            now.setText(stock.sp1_);
//                        }
//                    }
//                    Double dYesterday = Double.parseDouble(stock.yesterday_);
//                    Double dIncrease = dNow - dYesterday;
//                    Double dPercent = dIncrease / dYesterday * 100;
//                    percent.setText(String.format("%.2f", dPercent) + "%");
//                    increaseValue.setText(String.format("%.2f", dIncrease));
//                    int color = Color.BLACK;
//                    if(dIncrease > 0) {
//                        color = Color.RED;
//                    }
//                    else if(dIncrease < 0){
//                        color = Color.GREEN;
//                    }
//
//                    now.setTextColor(color);
//                    percent.setTextColor(color);
//                    increaseValue.setTextColor(color);
//                }
//                row.addView(percent);
//                row.addView(increaseValue);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mSelectedStockItems.contains(stock.id)) {
                            v.setBackgroundColor(BackgroundColor);
                            mSelectedStockItems.remove(stock.id);
                        } else {
                            v.setBackgroundColor(HighlightColor);
                            mSelectedStockItems.add(stock.id);
                        }
                    }
                });
                row.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        PopupMenu popupMenu = new PopupMenu(MainActivity.this,v);
                        popupMenu.getMenuInflater().inflate(R.menu.menu_item,popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                table.removeView(row);
                                deleteStockInfo(info.name, stock.id);

                                return false;
                            }
                        });
                        popupMenu.show();
                        return false;
                    }
                });
                table.addView(row);
                loss += (Double.parseDouble(stock.price) - Double.parseDouble(stock.cost))* Double.parseDouble(stock.quantity);
            }
            gl.setText(String.format("%+f",loss));
        }

        checkIfMarginCall();
    }

    private boolean isComplete(StockInfo stockInfo) {
        return !TextUtils.isEmpty(stockInfo.id)
                && !TextUtils.isEmpty(stockInfo.cost) &&!TextUtils.isEmpty(stockInfo.quantity)
                && !TextUtils.isEmpty(stockInfo.name) &&!TextUtils.isEmpty(stockInfo.price);
    }
    private void checkIfMarginCall() {
        for (TradeAccountInfo info : mTradeInfo.values()) {
            double loss = 0;
            Collection<StockInfo> stocks = info.stockInfos.values();
            for(StockInfo stock : stocks) {
                loss += (Double.parseDouble(stock.cost) - Double.parseDouble(stock.price))* Double.parseDouble(stock.quantity);
            }

            if (loss >= info.sumPrice * (1- (double)info.maxLossRate/100)) {
                String text = "账户 " + info.name +"已经亏损" + String.format("%.2f", loss) + "元，超过总资产的" + info.maxLossRate + "%";
                sendWarning(text);
            }
        }

    }
}
