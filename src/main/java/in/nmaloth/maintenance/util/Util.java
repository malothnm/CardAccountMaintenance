package in.nmaloth.maintenance.util;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.customer.*;
import in.nmaloth.entity.instrument.InstrumentType;
import in.nmaloth.maintenance.exception.InvalidEnumConversion;
import in.nmaloth.maintenance.exception.InvalidInputDataException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {

    public static int checkLuhn(String cardNo)
    {
        int nDigits = cardNo.length();
        int sum = 0;

        for(int i = 0; i < cardNo.length(); i ++ ){
            String digit = cardNo.substring(nDigits - i -1, nDigits - i );

            int digitValue = Integer.parseInt(digit);
            if(i%2 == 1){
                sum = sum + digitValue;
            } else {
                int doubledigit = digitValue * 2;
                sum = sum + doubledigit%10;
                sum = sum + doubledigit/10;
            }
        }

        if(sum%10 == 0){
            return 0;
        } else {
            return (10 - (sum%10));
        }

    }

    public static String generateNextCardNumber(String cardNumber){

        int cardLength = cardNumber.length();
        String cardWithoutCheckDigit = cardNumber.substring(0, cardLength -1);

        Long longCard = Long.parseLong(cardWithoutCheckDigit);
        longCard = longCard + 1;
        String newCard = longCard.toString();
        int lastDigit = Util.checkLuhn(newCard);

        return new StringBuilder()
                .append(newCard)
                .append(lastDigit)
                .toString();

    }

    public static String generateNextCardNumber(String cardNumber,int incrementBy){

        int cardLength = cardNumber.length();
        String cardWithoutCheckDigit = cardNumber.substring(0, cardLength -1);

        Long longCard = Long.parseLong(cardWithoutCheckDigit);
        longCard = longCard + incrementBy;
        String newCard = longCard.toString();
        int lastDigit = Util.checkLuhn(newCard);

        return new StringBuilder()
                .append(newCard)
                .append(lastDigit)
                .toString();

    }


    public static String generateCardNumberFromStarter(String cardStarter){

        int checkDigit = checkLuhn(cardStarter);
        return new StringBuilder()
                .append(cardStarter)
                .append(checkDigit)
                .toString();
    }

    public static BlockType getBlockType(String blockType){
        switch (blockType){
            case "0": {
                return BlockType.APPROVE;
            }
            case "1" : {
                return BlockType.BLOCK_DECLINE;
            }
            case "2": {
                return BlockType.BLOCK_PICKUP;
            }
            case "3": {
                return BlockType.BLOCK_FRAUD;
            }
            case "4" : {
                return BlockType.BLOCK_SUSPECTED_FRAUD;
            }
            case "5" : {
                return BlockType.BLOCK_TEMP;
            }
            case "6": {
                return BlockType.VIP_ALWAYS_APPROVE;
            }
            default: {
                log.error("Invalid Block Type requested...{}",blockType);
                throw  new InvalidEnumConversion("Invalid Block Type requested");
            }
        }
    }
    public static String getBlockType(BlockType blockType){
        switch (blockType){
            case APPROVE:{
                return "0";
            }
            case BLOCK_DECLINE: {
                return "1";
            }
            case BLOCK_PICKUP: {
                return "2";
            }
            case BLOCK_FRAUD: {
                return "3";
            }
            case BLOCK_SUSPECTED_FRAUD: {
                return "4";
            }
            case BLOCK_TEMP: {
                return "5";
            }
            case VIP_ALWAYS_APPROVE: {
                return "6";
            }
        }
         throw  new InvalidEnumConversion("Invalid Block Type requested");

    }

    public static PeriodicType getPeriodicType(String periodicType){
        switch (periodicType){
            case "S": {
                return PeriodicType.SINGLE;
            }
            case "D" : {
                return PeriodicType.DAILY;
            }
            case "M": {
                return PeriodicType.MONTHLY;
            }
            case "Y": {
                return PeriodicType.YEARLY;
            }
            default:{
                log.error("Invalid periodic Type ..{}",periodicType);
                throw  new InvalidEnumConversion("Invalid periodic Type");
            }

        }
    }

    public static String getPeriodicType(PeriodicType periodicType){
        switch (periodicType){
            case SINGLE:
                return "S";
            case DAILY:
                return "D";
            case MONTHLY:
                return "M";
            case YEARLY:
                return "Y";
        }
        log.error("Invalid PeriodicType ");
        throw new InvalidEnumConversion("Invalid periodic Type");
    }

    public static LimitType getLimitType(String limitType){

        switch (limitType){
            case "N" : return LimitType.NO_SPECIFIC;
            case "C" : return LimitType.CASH;
            case "R" : return LimitType.RETAIL;
            case "O" : return LimitType.OTC;
            case "Q" : return LimitType.QUASI_CASH;
            case "A" : return LimitType.ATM;
        }

        log.error(" Invalid Limit Type ..{}", limitType);
        throw new InvalidEnumConversion("Invalid Limit Type");

    }

    public static String getLimitType(LimitType limitType){
        switch (limitType){
            case NO_SPECIFIC: return "N";
            case CASH: return "C";
            case QUASI_CASH: return "Q";
            case ATM: return "A";
            case OTC: return "O";
            case RETAIL: return "R";
        }
        log.error("Invalid Limit Type ");
        throw new InvalidEnumConversion("Invalid Limit Type");
    }

    public static CardStatus getCardStatus(String cardStatus){

        switch (cardStatus){
            case "A": return CardStatus.ACTIVE;
            case "F": return CardStatus.FRAUD;
            case "T": return CardStatus.TRANSFER;
            case "P": return CardStatus.PURGED;
            case "I": return CardStatus.INACTIVE;
        }
        log.error(" Invalid Card Status ...{}", cardStatus);
        throw new InvalidEnumConversion("Invalid Card Status");
    }

    public static String getCardStatus(CardStatus cardStatus){
        return cardStatus.getCardStatus();
    }

    public static CardHolderType getCardHolderType(String cardHolderType){
        switch (cardHolderType){
            case "P": return CardHolderType.PRIMARY;
            case "S": return CardHolderType.SECONDARY;
            case "A": return CardHolderType.ADDITIONAL;
        }
        log.error("Invalid CardHolder Type ...{}",cardHolderType);
        throw new InvalidEnumConversion("Invalid Card Holder Type");
    }

    public static String getCardHolderType(CardHolderType cardHolderType){
        return cardHolderType.getCardHolderType();
    }

    public static CardAction getCardAction(String cardAction){

        switch (cardAction){

            case "0": return CardAction.NO_ACTION;
            case "1": return CardAction.NEW_CARD;
            case "2": return CardAction.ADDITIONAL_CARD;
            case "3": return CardAction.REPLACEMENT_CARD;
            case "4": return CardAction.EMERGENCY_REPLACEMENT_CARD;
            case "5": return CardAction.REISSUE_CARD;

        }

        log.error(" Invalid Card Action .. {}",cardAction);
        throw new InvalidEnumConversion("Invalid Card Action");

    }

    public static  String getCardAction(CardAction cardAction){
        switch (cardAction){
            case NO_ACTION: return "0";
            case NEW_CARD: return "1";
            case ADDITIONAL_CARD: return "2";
            case REPLACEMENT_CARD:  return "3";
            case EMERGENCY_REPLACEMENT_CARD: return "4";
            case REISSUE_CARD: return "5";
        }
        log.error(" Invalid card Action ..");
        throw new InvalidEnumConversion("Invalid Card Action");
    }

    public static BalanceTypes getBalanceTypes(String balanceTypes){

        switch (balanceTypes){
            case "0" : return BalanceTypes.CURRENT_BALANCE;
            case "1" : return BalanceTypes.CASH_BALANCE;
            case "2" : return BalanceTypes.INSTALLMENT_BALANCE;
            case "3" : return BalanceTypes.INSTALLMENT_CASH;
            case "4" : return  BalanceTypes.INTERNATIONAL;
            case "5" : return BalanceTypes.INTERNATIONAL_CASH;
            case "6" : return BalanceTypes.INTERNATIONAL_INSTALLMENT;
            case "7" : return BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT;

        }

        log.error(" Invalid Balance Types ..{}",balanceTypes);
        throw new InvalidEnumConversion("Invalid balance types");

    }

    public static String getBalanceTypes(BalanceTypes balanceTypes){
        switch (balanceTypes){
            case CURRENT_BALANCE: return "0";
            case CASH_BALANCE: return "1";
            case INSTALLMENT_BALANCE: return "2";
            case INSTALLMENT_CASH: return "3";
            case INTERNATIONAL: return "4";
            case INTERNATIONAL_CASH: return "5";
            case INTERNATIONAL_INSTALLMENT: return "6";
            case INTERNATIONAL_CASH_INSTALLMENT: return "7";
        }
        log.error(" Invalid Balance Type ");
        throw new InvalidEnumConversion("Invalid balance types");
    }

    public static InstrumentType getInstrumentType(String instrumentType){

        switch (instrumentType){
            case "0": return InstrumentType.PLASTIC_CREDIT;
            case "1": return InstrumentType.PLASTIC_DEBIT;
            case "2": return InstrumentType.PLASTIC_LOYALTY;
            case "3": return InstrumentType.TOKEN;
            case "4": return InstrumentType.TEMP_CARD;
            case "5": return InstrumentType.CARD_LESS;
            case "6": return InstrumentType.ONE_TIME_USE_CARD;
        }
        log.error(" Invalid Instrument Type .. {}", instrumentType);
        throw new InvalidEnumConversion("Invalid Instrument Type");

    }

    public static String getInstrumentType(InstrumentType instrumentType){

        switch (instrumentType){
            case PLASTIC_CREDIT: return "0";
            case PLASTIC_DEBIT: return "1";
            case PLASTIC_LOYALTY: return "2";
            case TOKEN: return "3";
            case TEMP_CARD: return "4";
            case CARD_LESS: return "5";
            case ONE_TIME_USE_CARD: return "6";
        }
        log.error("Invalid Instrument Type ...");
        throw new InvalidEnumConversion("Invalid Instrument Type");
    }

    public static String getAccountType(AccountType accountType){

        switch (accountType) {
            case LOANS:
                return "L";
            case CREDIT:
                return "CR";
            case CURRENT:
                return "CU";
            case PREPAID:
                return "P";
            case SAVINGS:
                return "S";
            case UNIVERSAL:
                return "U";
        }
        log.error(" Invalid Account Type ...");
        throw new InvalidEnumConversion("Invalid Account Type");
    }

    public static AccountType getAccountType(String accountType){

        switch (accountType){
            case "L":
                return AccountType.LOANS;
            case "CR":
                return AccountType.CREDIT;
            case "CU":
                return AccountType.CURRENT;
            case "P":
                return AccountType.PREPAID;
            case "S":
                return AccountType.SAVINGS;
            case "U":
                return AccountType.UNIVERSAL;
        }
        log.error(" Invalid Account Type {} " , accountType);
        throw new InvalidEnumConversion("Invalid Account Type");
    }

    public static String getAddressType(AddressType addressType){
        switch (addressType){
            case HOME:
                return "H";
            case OFFICE:
                return "O";
            case PRIMARY:
                return "P";
            case CORPORATE:
                return "C";
            case ADDRESS_TYPE_1:
                return "1";
            case ADDRESS_TYPE_2:
                return "2";
            case ADDRESS_TYPE_3:
                return "3";
            case ADDRESS_TYPE_4:
                return "4";
            case SECONDARY_ADDRESS:
                return "S";
        }
        log.error("Invalid Address Type");
        throw new InvalidEnumConversion("Invalid Address Type");

    }

    public static AddressType getAddressType(String addressType){
        switch (addressType){
            case "H":
                return AddressType.HOME;
            case "O":
                return AddressType.OFFICE;
            case "P":
                return AddressType.PRIMARY;
            case "C":
                return AddressType.CORPORATE;
            case "1":
                return AddressType.ADDRESS_TYPE_1;
            case "2":
                return AddressType.ADDRESS_TYPE_2;
            case "3":
                return AddressType.ADDRESS_TYPE_3;
            case "4":
                return AddressType.ADDRESS_TYPE_4;
            case "S":
                return AddressType.SECONDARY_ADDRESS;
        }

        log.error("Invalid Address Type {}", addressType);
        throw new InvalidEnumConversion("Invalid Address Type");
    }

    public static CustomerIDType getCustomerIDType(String customerType){

        switch (customerType){
            case "0":
                return CustomerIDType.SSN_OR_NATIONAL_ID;
            case "1":
                return CustomerIDType.TAX_ID;
            case "2":
                return CustomerIDType.DRIVERS_LICENCE;
            case "3":
                return CustomerIDType.VOTER_ID;
            case "4":
                return CustomerIDType.PASSPORT_ID;
            case "5":
                return CustomerIDType.CUSTOM_ID_1;
            case "6":
                return CustomerIDType.CUSTOM_ID_2;
            case "7":
                return CustomerIDType.CUSTOM_ID_3;

        }

        log.error("Invalid CustomerID Type {} ",customerType);
        throw new InvalidEnumConversion("Invalid CustomerID Type");

    }

    public static String getCustomerIDType(CustomerIDType customerType){

        switch (customerType){
            case SSN_OR_NATIONAL_ID:
                return "0";
            case TAX_ID:
                return "1";
            case DRIVERS_LICENCE:
                return "2";
            case VOTER_ID:
                return "3";
            case PASSPORT_ID:
                return "4";
            case CUSTOM_ID_1:
                return "5";
            case CUSTOM_ID_2:
                return "6";
            case CUSTOM_ID_3:
                return "7";
        }

        log.error("Invalid Customer ID ");
        throw new InvalidEnumConversion("Invalid CustomerID Type");

    }

    public static CustomerType getCustomerType(String customerType){
        switch (customerType){
            case "O":
                return CustomerType.OWNER;
            case "C":
                return CustomerType.CO_OWNER;
            }
            log.error("Invalid Customer Type {}", customerType);
            throw new InvalidEnumConversion("Invalid Customer Type");
        }

    public static String getCustomerType(CustomerType customerType){
        switch (customerType){
            case OWNER:
                return "O";
            case CO_OWNER:
                return "C";
        }
        log.error("Invalid Customer Type ");
        throw new InvalidEnumConversion("Invalid Customer Type");
    }

    public static String getEmailType(EmailType emailType){
        switch (emailType){
            case PERSONAL:
                return "P";
            case WORK:
                return "W";
        }
        log.error("Invalid Email Type ");
        throw new InvalidEnumConversion("Invalid Email Type");
    }

    public static EmailType getEmailType(String emailType){
        switch (emailType){
            case "P":
                return EmailType.PERSONAL;
            case "W":
                return EmailType.WORK;
        }
        log.error("Invalid Email Type {}",emailType);
        throw new InvalidEnumConversion("Invalid Email Type");
    }

    public static String getPhoneType(PhoneType phoneType){
        switch (phoneType){
            case PERSONAL_LAND_LINE:
                return "PL";
            case PERSONAL_MOBILE:
                return "PM";
            case WORK_PHONE_MOBILE:
                return "WM";
            case WORK_PHONE_LAND_LINE:
                return "WL";
            case ADDITIONAL_PHONE:
                return "AP";
        }

        log.error("Invalid Phone Type ");
        throw new InvalidEnumConversion("Invalid Phone Type");
    }

    public static PhoneType getPhoneType(String phoneType){
        switch (phoneType){
            case "PL" :
                return PhoneType.PERSONAL_LAND_LINE;
            case "PM":
                return PhoneType.PERSONAL_MOBILE;
            case "WM":
                return PhoneType.WORK_PHONE_MOBILE;
            case "WL":
                return PhoneType.WORK_PHONE_LAND_LINE;
            case "AP":
                return PhoneType.ADDITIONAL_PHONE;
        }

        log.error("Invalid Phone Type {} ", phoneType);
        throw new InvalidEnumConversion("Invalid Phone Type");
    }
}

