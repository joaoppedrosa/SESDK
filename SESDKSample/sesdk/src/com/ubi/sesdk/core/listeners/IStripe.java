package com.ubi.sesdk.core.listeners;

import com.stripe.android.model.Card;
import com.ubi.sesdk.core.model.StripeToken;

/**
 * @author João Pedro Pedrosa, SESDK on 02/05/2016.
 */

public interface IStripe {
    void validCreditCard(Card card);

    void invalideCreditCard();

    void stripeToken(StripeToken stripeToken);

    void stripeTokenError(String message);
}
