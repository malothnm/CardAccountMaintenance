package in.nmaloth.maintenance.controllers;

public class EndPoints {

    public  static final String PRODUCTS = "/v1/products";
    public  static final String PRODUCTS_ORG_PRODUCT = "/v1/products/{org}/{product}";

    public  static final String PRODUCT_LIMITS = "/v1/product_limits";
    public  static final String PRODUCT_LIMITS_ORG_PRODUCT = "/v1/product_limits/{org}/{product}";

    public  static final String PRODUCT_CARD_GEN = "/v1/product_card_gen";
    public  static final String PRODUCT_CARD_GEN_ORG_PRODUCT = "/v1/product_card_gen/{org}/{product}";

    public  static final String DECLINE_REASONS = "/v1/decline_reason";
    public  static final String DECLINE_REASONS_SERVICE_NAME = "/v1/decline_reason/{serviceName}";

    public  static final String CUSTOMER = "/v1/customer";
    public  static final String CUSTOMER_ID = "/v1/customer/{customerId}";
    public  static final String ACCOUNTS = "/v1/accounts";
    public  static final String ACCOUNTS_ACCOUNT_NBR = "/v1/accounts/{accountNumber}";
    public  static final String ACCOUNTS_LIMITS_ACCOUNT_NBR = "/v1/accounts/limits/{accountNumber}";

    public  static final String CARDS = "/v1/cards";
    public  static final String CARDS_CARD_NBR = "/v1/cards/{cardNumber}";
    public  static final String CARDS_NEW_PLASTIC = "/v1/cards/plastic";
    public  static final String CARDS_PLASTIC_CARD_NUMBER = "/v1/cards/{cardNumber}/plastic";

    public  static final String CARDS_CARD_NBR_PLASTIC_ID = "/v1/cards/{cardNumber}/plastic/{plasticId}";

    public  static final String CARDS_LIMITS_CARD_NBR = "/v1/cards/limits/{cardNumber}";

    public static final String CARD_INSTRUMENT = "/v1/cards/{cardNumber}/instrument";
    public static final  String INSTRUMENT = "/v1/instrument";
    public static final  String INSTRUMENT_NBR = "/v1/instrument/{instrumentNumber}";





}
