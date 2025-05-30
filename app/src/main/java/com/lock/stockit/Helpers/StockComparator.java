package com.lock.stockit.Helpers;

import com.lock.stockit.Models.StockModel;

import java.util.Comparator;

public class StockComparator implements Comparator<StockModel> {
    @Override
    public int compare(StockModel o1, StockModel o2) {
        int nameCompare = o1.getItemName().compareTo(o2.getItemName());
        if (nameCompare != 0) return nameCompare;
        if (o1.getItemSize().contains("x") && o2.getItemSize().contains("x")) {
            String[] d1 = o1.getItemSize().split("x");
            String[] d2 = o2.getItemSize().split("x");
            for (int i = 0; i < d1.length; i++) {
                int a = Integer.parseInt(d1[i]);
                int b = Integer.parseInt(d2[i]);
                int compareSize = Integer.compare(a, b);
                if (compareSize != 0) return compareSize;
            }
        } else return o1.getItemSize().compareTo(o2.getItemSize());
        return 0;
    }
}
