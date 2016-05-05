package com.ubi.sesdk.core.interfaces;

import com.stripe.android.model.Card;

/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public interface IPaymentManager {

    double convertEURtoBitcoin(double bitcoinCurrentValue, double value);

    double convertBitcointoEUR(double bitcoinCurrentValue, double value);

    double convertEURtomBitcoin(double bitcoinCurrentValue, double value);

    double convertmBitcointoEUR(double bitcoinCurrentValue, double value);

    void createCreditCard(String name, String cardNumber, int expiryMonth, int expiryYear, String cvv);

    void createTokenToCharge(Card card, String description, double amount, final String currency);
}
