package com.ubi.sesdk.core.managers;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;
import com.ubi.sesdk.core.interfaces.IPaymentManager;
import com.ubi.sesdk.core.listeners.IStripe;
import com.ubi.sesdk.core.model.StripeToken;

import java.math.BigDecimal;

/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public class PaymentManager implements IPaymentManager {
    private IStripe iStripe;
    private String stripeKey;

    public PaymentManager(String stripeKey){
        this.stripeKey = stripeKey;
    }

    public void setiStripe(IStripe iStripe) {
        this.iStripe = iStripe;
    }

    @Override
    public double convertEURtoBitcoin(double bitcoinCurrentValue, double value) {
        return value/bitcoinCurrentValue;
    }

    @Override
    public double convertBitcointoEUR(double bitcoinCurrentValue,double value) {
        return bitcoinCurrentValue/value;
    }

    @Override
    public double convertEURtomBitcoin(double bitcoinCurrentValue, double value) {
        return (value*1000)/bitcoinCurrentValue;
    }

    @Override
    public double convertmBitcointoEUR(double bitcoinCurrentValue, double value) {
        return (value*bitcoinCurrentValue)/1000;
    }

    @Override
    public void createCreditCard(String name, String cardNumber, int expiryMonth, int expiryYear, String cvv) {
        Card card = new Card(cardNumber,expiryMonth,expiryYear,cvv);
        if(card.validateCard()){
            iStripe.validCreditCard(card);
        }else{
            iStripe.invalideCreditCard();
        }
    }

    @Override
    public void createTokenToCharge(Card card, final String description, final double amount, final String currency) {
        final BigDecimal bigInteger = new BigDecimal(amount);
        final Stripe stripe = new Stripe();
        try {
            stripe.setDefaultPublishableKey(stripeKey);
        } catch (AuthenticationException e) {
            iStripe.stripeTokenError(e.getMessage());
        }
        stripe.createToken(card, stripeKey, new TokenCallback() {
            public void onSuccess(Token token) {
                StripeToken stripeToken = new StripeToken();
                stripeToken.setDescription(description);
                stripeToken.setAmount(String.valueOf(bigInteger.intValue()));
                stripeToken.setCurrency(currency);
                stripeToken.setStripeToken(token.getId());
                iStripe.stripeToken(stripeToken);
            }

            public void onError(Exception error) {
                iStripe.stripeTokenError(error.getMessage());
            }
        });
    }
}
