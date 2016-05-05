package com.ubi.sesdksample.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;
import com.estimote.sdk.connection.MotionState;
import com.stripe.android.model.Card;
import com.ubi.sesdk.core.SE;
import com.ubi.sesdk.core.interfaces.IPaymentManager;
import com.ubi.sesdk.core.listeners.IBeacons;
import com.ubi.sesdk.core.listeners.IStripe;
import com.ubi.sesdk.core.managers.BeaconsManager;
import com.ubi.sesdk.core.managers.PaymentManager;
import com.ubi.sesdk.core.model.BeaconID;
import com.ubi.sesdk.core.model.StripeToken;
import com.ubi.sesdksample.R;

import java.util.List;

public class PaymentActivity extends AppCompatActivity implements IStripe {
    private static final String TAG = "PaymentActivity";
    private PaymentManager paymentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initPaymentManager();
    }

    private void initPaymentManager(){
        this.paymentManager = SE.getPaymentManager();
        this.paymentManager.setiStripe(this);
        this.paymentManager.createCreditCard("NOME","4242424242424242",12,20,"123");
    }

    @Override
    public void validCreditCard(Card card) {
        Toast.makeText(PaymentActivity.this, "Valid Credit Card: " + card.toString(), Toast.LENGTH_SHORT).show();
        paymentManager.createTokenToCharge(card,"description",12.00,"EUR");
    }

    @Override
    public void invalideCreditCard() {
        Log.e(TAG, "invalideCreditCard");
        Toast.makeText(PaymentActivity.this, "Invalid credit card", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stripeToken(StripeToken stripeToken) {
        Log.d(TAG, "stripeToken: " + stripeToken);
        Toast.makeText(PaymentActivity.this, "Stripe Token: " + stripeToken.getStripeToken(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stripeTokenError(String message) {
        Log.e(TAG, "stripeTokenError: " + message);
        Toast.makeText(PaymentActivity.this, "Stripe Token Error: " + message, Toast.LENGTH_SHORT).show();
    }
}
