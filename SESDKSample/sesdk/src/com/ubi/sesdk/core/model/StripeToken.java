package com.ubi.sesdk.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author João Pedro Pedrosa, SE on 08/04/2016.
 */
public class StripeToken {

    @SerializedName("stripe_token")
    @Expose
    private String stripeToken;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("amount")
    @Expose
    private String amount;
    @SerializedName("currency")
    @Expose
    private String currency;

    /**
     *
     * @return
     * The stripeToken
     */
    public String getStripeToken() {
        return stripeToken;
    }

    /**
     *
     * @param stripeToken
     * The stripe_token
     */
    public void setStripeToken(String stripeToken) {
        this.stripeToken = stripeToken;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     *
     * @param amount
     * The amount
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     *
     * @return
     * The currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     *
     * @param currency
     * The currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
