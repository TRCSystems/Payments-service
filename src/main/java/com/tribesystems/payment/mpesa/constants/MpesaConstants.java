package com.tribesystems.payment.mpesa.constants;

public class MpesaConstants {
    public static final String MPESA = "/mpesa";

    public static final String INITIATE_STK_PUSH = "/initiate-stk-push";
    public static final String CONFIRM_PAYMENT = "/confirm-payment";
    public static final String GET_ALL_TRANSACTIONS = "/all";

    public static final String CONFIRM_PAYMENT_RESULT = "/confirm/result";
    public static final String CONFIRM_PAYMENT_TIMEOUT = "/confirm/timeout";

    public static final String INITIATE_STK_PUSH_CALLBACK = "/callback";
    public static final String REGISTER_C_2_B_URL_CALLBACKS = "/register-c2b";
    public static final String SIMULATE_C2B_TRANSACTION = "/c2b/simulate";

    public static final String C2B_CONFIRMATION = "/c2b/confirmation";
    public static final String C2B_VALIDATION = "/c2b/validation";
    public static final String C2B_GET_ALL_TRANSACTIONS = "/c2b/all";

    public static final String B2C_INITIATE_PAYMENT = "/b2c/initiate";
    public static final String B2C_GET_ALL_PAYMENTS = "/b2c/all";
    public static final String B2C_CALLBACK_RESULT = "/b2c/result";
    public static final String B2C_CALLBACK_TIMEOUT = "/b2c/timeout";

}
