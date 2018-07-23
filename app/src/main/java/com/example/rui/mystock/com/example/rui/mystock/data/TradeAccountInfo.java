package com.example.rui.mystock.com.example.rui.mystock.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jennyxia on 2018/5/23.
 */

public class TradeAccountInfo{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSumPrice() {
        return sumPrice;
    }

    public void setSumPrice(int sumPrice) {
        this.sumPrice = sumPrice;
    }

    public int getMaxLossRate() {
        return maxLossRate;
    }

    public void setMaxLossRate(int maxLossRate) {
        this.maxLossRate = maxLossRate;
    }

    public HashMap<String, StockInfo> getStockInfos() {
        return stockInfos;
    }

    public void setStockInfos(HashMap<String, StockInfo> stockInfos) {
        this.stockInfos = stockInfos;
    }

    public String name;
    public int sumPrice;
    public int maxLossRate;
    public HashMap<String,StockInfo> stockInfos = new HashMap<>();

    public static TradeAccountInfo parseTradeString(String info) {
        TradeAccountInfo  tradeAccountInfo = JSON.parseObject(info, TradeAccountInfo.class);
        return tradeAccountInfo;
    }
    public static  Map<String,TradeAccountInfo> parseTradesString(String info) {
        Map<String,TradeAccountInfo> tradeInfo = (Map<String, TradeAccountInfo>) JSON.parseObject(info, new TypeReference<Map<String, TradeAccountInfo>>() {
        });
        return tradeInfo;
    }


    public String toString() {
         return JSON.toJSONString(this);
    }
}
