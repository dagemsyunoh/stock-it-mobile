package com.lock.stockit.Helpers;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lock.stockit.Models.CustomerModel;
import com.lock.stockit.R;

public class CustomerViewHolder extends CustomerBaseViewHolder {

    private final CollectionReference customerRef = FirebaseFirestore.getInstance().collection("stores").document(new SecurePreferences(itemView.getContext(), "store-preferences", "store-key", true).getString("sid")).collection("customers");
    private final TextView name, transactions;
    private final TextInputLayout editName;
    private final TextInputEditText inputName;
    private final ImageView leftImage, rightImage;
    private final FloatingActionButton saveButton;
    private final CardView cardView;
    private final Logger logger = new Logger();

    public CustomerViewHolder(View itemView, CustomerListeners customerListeners) {
        super(itemView, customerListeners);
        name = itemView.findViewById(R.id.name_text);
        editName = itemView.findViewById(R.id.name_edit);
        inputName = itemView.findViewById(R.id.name_input);
        transactions = itemView.findViewById(R.id.transactions_text);
        saveButton = itemView.findViewById(R.id.save_button);
        cardView = itemView.findViewById(R.id.card_view);
        leftImage = itemView.findViewById(R.id.button_left);
        rightImage = itemView.findViewById(R.id.button_right);
    }

    @Override
    public void bindDataToViewHolder(CustomerModel item, int position, SwipeState swipeState) {
        String transactText = item.getTransactions() + " transactions";
        //region Input Data
        name.setText(item.getName());
        transactions.setText(transactText);
        inputName.setText(String.valueOf(item.getName()));
        editName.setVisibility(View.GONE);
        //endregion
        //region Swipe
        setSwipe(cardView, item.getState());
        //endregion
        setSwipeEventListener(item, position, swipeState);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSwipeEventListener(final CustomerModel item, final int position, final SwipeState swipeState) {
        //region On Click
        if (swipeState != SwipeState.NONE) {
            leftImage.setOnClickListener(view -> {
                changeLayout(item, position);
                getListener().onClickLeft(item, position);
                onAnimate(cardView, onSwipeUp(swipeState), 250L);
            });
            rightImage.setOnClickListener(view -> getListener().onClickRight(item, position));
        }

        saveButton.setOnClickListener(view -> updateData(inputName));

        cardView.setOnClickListener(view -> {
        }); // Do not remove, it is required for the swipe to work
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

    private void changeLayout(CustomerModel item, int position) {
        editName.setVisibility(View.VISIBLE);
        inputName.setVisibility(View.VISIBLE);
        name.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
        getListener().onClickLeft(item, position);
    }

    private void updateData(TextInputEditText cName) {
        customerRef.whereEqualTo("name", name.getText().toString())
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    customerRef.document(task.getResult().getDocuments().get(0).getId()).update("name", cName.getText().toString());
                });
        returnLayout();
        Toast.makeText(itemView.getContext(), "Updated", Toast.LENGTH_SHORT).show();
    }

    private void returnLayout() {
        editName.setVisibility(View.GONE);
        inputName.setVisibility(View.GONE);
        name.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
    }
}
