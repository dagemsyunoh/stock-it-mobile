package com.lock.stockit.Helpers;

import static com.lock.stockit.InventoryFragment.LOW_STOCK_THRESHOLD;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lock.stockit.Models.StockModel;
import com.lock.stockit.R;

import java.util.Locale;

public class StockViewHolder extends StockBaseViewHolder {

    private final CollectionReference colRef = FirebaseFirestore.getInstance().collection("stores").document(new SecurePreferences(itemView.getContext(), "store-preferences", "store-key", true).getString("sid")).collection("stocks");
    private final TextView itemName, itemSize, itemQty, itemRegPrice, itemDscPrice;
    private final LinearLayout editQty;
    private final TextInputLayout editRegPrice, editDscPrice;
    private final TextInputEditText inputQty, inputRegPrice, inputDscPrice;
    private final FloatingActionButton saveButton, plusOne, minusOne;
    private final ImageView leftImage, rightImage;
    private final CardView cardView;

    public StockViewHolder(View itemView, StockListeners customListeners) {
        super(itemView, customListeners);
        itemName = itemView.findViewById(R.id.item_text);
        itemSize = itemView.findViewById(R.id.size_text);
        itemQty = itemView.findViewById(R.id.qty_val_text);
        itemRegPrice = itemView.findViewById(R.id.reg_price_val_text);
        itemDscPrice = itemView.findViewById(R.id.dsc_price_val_text);
        editQty = itemView.findViewById(R.id.qty_edit);
        editRegPrice = itemView.findViewById(R.id.reg_price_edit);
        editDscPrice = itemView.findViewById(R.id.dsc_price_edit);
        plusOne = itemView.findViewById(R.id.plus_one);
        minusOne = itemView.findViewById(R.id.minus_one);
        inputQty = itemView.findViewById(R.id.qty);
        inputRegPrice = itemView.findViewById(R.id.reg_price);
        inputDscPrice = itemView.findViewById(R.id.dsc_price);
        saveButton = itemView.findViewById(R.id.save_button);
        cardView = itemView.findViewById(R.id.card_view);
        leftImage = itemView.findViewById(R.id.button_left);
        rightImage = itemView.findViewById(R.id.button_right);
    }

    @Override
    public void bindDataToViewHolder(StockModel item, int position, SwipeState swipeState) {
        //region Input Data
        String unit = item.getItemQtyType();
        String qtyText;
        if (item.getItemQuantity() % 1 == 0) qtyText = ((int) item.getItemQuantity()) + " " + unit;
        else qtyText = item.getItemQuantity() + " " + unit;
        String regPriceText = "₱" + String.format(Locale.getDefault(), "%.2f", item.getItemRegPrice());
        String dscPriceText = "₱" + String.format(Locale.getDefault(), "%.2f", item.getItemDscPrice());
        itemName.setText(item.getItemName());
        itemSize.setText(item.getItemSize());
        itemQty.setText(qtyText);
        itemRegPrice.setText(regPriceText);
        itemDscPrice.setText(dscPriceText);
        inputQty.setText(String.valueOf(item.getItemQuantity()));
        inputRegPrice.setText(String.valueOf(item.getItemRegPrice()));
        inputDscPrice.setText(String.valueOf(item.getItemDscPrice()));
        editQty.setVisibility(View.GONE);
        editRegPrice.setVisibility(View.GONE);
        editDscPrice.setVisibility(View.GONE);

        int originalCardColor = cardView.getCardBackgroundColor().getDefaultColor();

        if (item.getItemQuantity() == 0)
            cardView.setForeground(new ColorDrawable(Color.parseColor("#33F44336")));
        else if (item.getItemQuantity() <= LOW_STOCK_THRESHOLD)
            cardView.setForeground(new ColorDrawable(Color.parseColor("#33FFC107")));
        else cardView.setForeground(null);
        //endregion
        //region Swipe
        setSwipe(cardView, item.getState());
        //endregion
        setSwipeEventListener(item, position, swipeState);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setSwipeEventListener(final StockModel item, final int position, final SwipeState swipeState) {
        //region On Click
        if (swipeState != SwipeState.NONE) {
            leftImage.setOnClickListener(view -> {
                changeLayout(item, position);
                getListener().onClickLeft(item, position);
                onAnimate(cardView, onSwipeUp(swipeState), 250L);
            });
            rightImage.setOnClickListener(view -> getListener().onClickRight(item, position));
        }

        saveButton.setOnClickListener(view -> updateData(inputQty, inputRegPrice, inputDscPrice));

        plusOne.setOnClickListener(view -> QtyEditor.qtyEditor(inputQty, 1));

        minusOne.setOnClickListener(view -> {
            QtyEditor.qtyEditor(inputQty, -1);
            if (Integer.parseInt(inputQty.getText().toString()) == 0)
                Toast.makeText(cardView.getContext(), "Quantity cannot be negative.", Toast.LENGTH_SHORT).show();
        });

        cardView.setOnClickListener(view -> { }); // Do not remove, it is required for the swipe to work
        //endregion
        //region On Touch Swipe
        if (swipeState == SwipeState.NONE) return;
        cardView.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    dXLead = view.getX() - event.getRawX();
                    dXTrail = view.getRight() - event.getRawX();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    getListener().onRetainSwipe(item, position);
                    onAnimate(view, onSwipeMove(event.getRawX() + dXLead, event.getRawX() + dXTrail, swipeState), 250L);
                    item.setState(getSwipeState(event.getRawX() + dXLead, event.getRawX() + dXTrail, swipeState));
                    return false;
                case MotionEvent.ACTION_UP:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    onAnimate(view, onSwipeUp(item.getState()), 250L);
                    return false;
                case MotionEvent.ACTION_CANCEL:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                default:
                    return true;
            }
        });
        //endregion
    }

    private void changeLayout(StockModel item, int position) {
        editQty.setVisibility(View.VISIBLE);
        editRegPrice.setVisibility(View.VISIBLE);
        editDscPrice.setVisibility(View.VISIBLE);
        itemQty.setVisibility(View.GONE);
        itemRegPrice.setVisibility(View.GONE);
        itemDscPrice.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
        getListener().onClickLeft(item, position);
    }

    private void updateData(TextInputEditText qty, TextInputEditText regPrice, TextInputEditText dscPrice) {
        colRef.whereEqualTo("item name", itemName.getText().toString())
                .whereEqualTo("item size", itemSize.getText().toString())
                .get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;
            colRef.document(task.getResult().getDocuments().get(0).getId()).update(
                    "qty",
                    Double.parseDouble(qty.getText().toString()),
                    "reg price",
                    Double.parseDouble(regPrice.getText().toString()),
                    "dsc price",
                    Double.parseDouble(dscPrice.getText().toString()));
        });
        returnLayout();
        Toast.makeText(itemView.getContext(), "Updated", Toast.LENGTH_SHORT).show();
    }

    private void returnLayout() {
        editQty.setVisibility(View.GONE);
        editRegPrice.setVisibility(View.GONE);
        editDscPrice.setVisibility(View.GONE);
        itemQty.setVisibility(View.VISIBLE);
        itemRegPrice.setVisibility(View.VISIBLE);
        itemDscPrice.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
    }
}
