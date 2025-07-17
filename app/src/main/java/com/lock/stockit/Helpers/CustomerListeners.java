package com.lock.stockit.Helpers;

import com.lock.stockit.Models.CustomerModel;

public interface CustomerListeners {

    void onClickLeft(CustomerModel item, int position);

    void onClickRight(CustomerModel item, int position);

    void onRetainSwipe(CustomerModel item, int position);
}
