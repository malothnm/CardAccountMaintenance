package in.nmaloth.maintenance.util;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.customer.*;
import in.nmaloth.entity.instrument.InstrumentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void getBlockType() {


        String[] testValues = {"0","1","2","3","4","5","6"};
        BlockType[] blockTypes = new BlockType[7];
        for (int i = 0; i < testValues.length ; i ++ ) {
            blockTypes[i ] = Util.getBlockType(testValues[i]);
        }

        String[] testValueReconverted = new String[7];
        for (int i = 0; i < blockTypes.length; i++ ){
            testValueReconverted[i] = Util.getBlockType(blockTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }


    @Test
    void getPeriodicType() {

        String[] testValues = {"S","D","M","Y"};
        PeriodicType[] periodicTypes = new PeriodicType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            periodicTypes[i ] = Util.getPeriodicType(testValues[i]);
        }

        String[] testValueReconverted = new String[periodicTypes.length];
        for (int i = 0; i < periodicTypes.length; i++ ){
            testValueReconverted[i] = Util.getPeriodicType(periodicTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);
    }



    @Test
    void getLimitType() {

        String[] testValues = {"N","C","R","O","Q","A"};
        LimitType[] limitTypes = new LimitType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            limitTypes[i ] = Util.getLimitType(testValues[i]);
        }

        String[] testValueReconverted = new String[limitTypes.length];
        for (int i = 0; i < limitTypes.length; i++ ){
            testValueReconverted[i] = Util.getLimitType(limitTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);
    }


    @Test
    void getCardStatus() {

        String[] testValues = {"A","I","F","P","T"};
        CardStatus[] cardStatuses = new CardStatus[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            cardStatuses[i ] = Util.getCardStatus(testValues[i]);
        }

        String[] testValueReconverted = new String[cardStatuses.length];
        for (int i = 0; i < cardStatuses.length; i++ ){
            testValueReconverted[i] = Util.getCardStatus(cardStatuses[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }


    @Test
    void getCardHolderType() {

        String[] testValues = {"P","S","A"};
        CardHolderType[] cardHolderTypes = new CardHolderType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            cardHolderTypes[i ] = Util.getCardHolderType(testValues[i]);
        }

        String[] testValueReconverted = new String[cardHolderTypes.length];
        for (int i = 0; i < cardHolderTypes.length; i++ ){
            testValueReconverted[i] = Util.getCardHolderType(cardHolderTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }


    @Test
    void getCardAction() {

        String[] testValues = {"0","1","2","3","4","5"};
        CardAction[] cardActions = new CardAction[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            cardActions[i] = Util.getCardAction(testValues[i]);
        }

        String[] testValueReconverted = new String[cardActions.length];
        for (int i = 0; i < cardActions.length; i++ ){
            testValueReconverted[i] = Util.getCardAction(cardActions[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }

    @Test
    void getBalanceTypes() {

        String[] testValues = {"0","1","2","3","4","5","6","7"};
        BalanceTypes[] balanceTypes = new BalanceTypes[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            balanceTypes[i] = Util.getBalanceTypes(testValues[i]);
        }

        String[] testValueReconverted = new String[balanceTypes.length];
        for (int i = 0; i < balanceTypes.length; i++ ){
            testValueReconverted[i] = Util.getBalanceTypes(balanceTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }

    @Test
    void getInstrumentTypes() {

        String[] testValues = {"0","1","2","3","4","5","6"};
        InstrumentType[] instrumentTypes = new InstrumentType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            instrumentTypes[i] = Util.getInstrumentType(testValues[i]);
        }

        String[] testValueReconverted = new String[instrumentTypes.length];
        for (int i = 0; i < instrumentTypes.length; i++ ){
            testValueReconverted[i] = Util.getInstrumentType(instrumentTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }


    @Test
    void getAccountType() {

        String[] testValues = {"S","CR","L","CU","U","P"};

        AccountType[] accountTypes = new AccountType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            accountTypes[i] = Util.getAccountType(testValues[i]);
        }

        String[] testValueReconverted = new String[accountTypes.length];
        for (int i = 0; i < accountTypes.length; i++ ){
            testValueReconverted[i] = Util.getAccountType(accountTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }

    @Test
    void getAddressType() {

        String[] testValues = {"H","O","P","C","1","2","3","4","S"};

        AddressType[] addressTypes = new AddressType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            addressTypes[i] = Util.getAddressType(testValues[i]);
        }

        String[] testValueReconverted = new String[addressTypes.length];
        for (int i = 0; i < addressTypes.length; i++ ){
            testValueReconverted[i] = Util.getAddressType(addressTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }


    @Test
    void getCustomerIDType() {

        String[] testValues = {"0","1","2","3","4","5","6","7"};

        CustomerIDType[] customerIDTypes = new CustomerIDType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            customerIDTypes[i] = Util.getCustomerIDType(testValues[i]);
        }

        String[] testValueReconverted = new String[customerIDTypes.length];
        for (int i = 0; i < customerIDTypes.length; i++ ){
            testValueReconverted[i] = Util.getCustomerIDType(customerIDTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }

    @Test
    void getCustomerType() {

        String[] testValues = {"O","C"};

        CustomerType[] customerTypes = new CustomerType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            customerTypes[i] = Util.getCustomerType(testValues[i]);
        }

        String[] testValueReconverted = new String[customerTypes.length];
        for (int i = 0; i < customerTypes.length; i++ ){
            testValueReconverted[i] = Util.getCustomerType(customerTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }

    @Test
    void getEmailType() {

        String[] testValues = {"P","W"};

        EmailType[] emailTypes = new EmailType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            emailTypes[i] = Util.getEmailType(testValues[i]);
        }

        String[] testValueReconverted = new String[emailTypes.length];
        for (int i = 0; i < emailTypes.length; i++ ){
            testValueReconverted[i] = Util.getEmailType(emailTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }

    @Test
    void getPhoneType() {

        String[] testValues = {"PL","PM","WL","WM","AP"};

        PhoneType[] phoneTypes = new PhoneType[testValues.length];
        for (int i = 0; i < testValues.length ; i ++ ) {
            phoneTypes[i] = Util.getPhoneType(testValues[i]);
        }

        String[] testValueReconverted = new String[phoneTypes.length];
        for (int i = 0; i < phoneTypes.length; i++ ){
            testValueReconverted[i] = Util.getPhoneType(phoneTypes[i]);
        }

        assertArrayEquals(testValues,testValueReconverted);

    }


}