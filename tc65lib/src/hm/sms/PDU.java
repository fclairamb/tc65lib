/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hm.sms;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import org.javacint.utilities.Log;
import org.javacint.utilities.Utilities;

/**
 * PDU parsing class
 */
public class PDU {

//#if DebugLevel=="debug"
    private static final boolean DEBUG = true;
//#elif DebugLevel=="warn" || DebugLevel=="fatal"
//# private static final boolean DEBUG = false;
//#endif
    /**
     * Hex String representation
     */
    String hexStringPDU;
    /**
     * 07 : Length of the SMSC information
     */
    int smscLength;
    /**
     * 91 : Type-of-address of the SMSC. (0x91 means international format of the phone number)
     */
    TypeOfAddress smscTypeOfAddress;

    /**
     * The Type-of-Address octet indicates the format of a phone number.<br>
     * The most common value of this octet is 91 hex (10010001 bin), which indicates international format.<br>
     * A phone number in international format looks like 46708251358 (where the country code is 46).<br>
     * In the national (or unknown) format the same phone number would look like 0708251358.<br>
     * The international format is the most generic, and it has to be accepted also when the message is destined to a recipient in the same country as the MSC or as the SGSN.<br>
     *<br>
     * Note that bit no 7 should always be set to 1<br>
     * Bits 6, 5 and 4 denote the Type-of-number<br>
     * Bits 3, 2, 1, 0 denote the Numbering-Plan-Identification<br>
     */
    class TypeOfAddress {

        /**
         * integer Type-of-Address<br>
         * contains Type-of-Number and NumberingPlan
         */
        int typeOfAddress;
        /**
         * Type of number<br>
         * see PDU.TypeOfAddress.TypeOfNumber to explain
         */
        int typeOfNumber;

        class TypeOfNumber {

            /**
             * Unknown. This is used when the user or network has no a priori information about the numbering plan. In this case, the Address-Value field is organized according to the network dialling plan, e.g. prefix or escape digits might be present.
             */
            final static int UNKNOWN = 0;
            /**
             * International number.
             */
            final static int INTERNATIONAL = 1;
            /**
             * National number. Prefix or escape digits shall not be included.
             */
            final static int NATIONAL = 2;
            /**
             * Network specific number. This is used to indicate administration/service number specific to the serving network, e.g. used to access an operator.
             */
            final static int NETWORK = 3;
            /**
             * Subscriber number. This is used when a specific short number representation is stored in one or more SCs as part of a higher layer application. (Note that "Subscriber number" shall only be used in connection with the proper PID referring to this application).
             */
            final static int SUBSCRIBER = 4;
            /**
             * Alphanumeric, (coded according to GSM TS 03.38 7-bit default alphabet)
             */
            final static int ALPHANUMERIC = 5;
            /**
             * Abbreviated number
             */
            final static int ABBREVIATED = 6;
            /**
             * Reserved for extension
             */
            final static int RESERVED = 7;
        }
        /**
         * Numbering Plan Identification<br>
         * see PDU.TypeOfAddress.NumberingPlan to explain
         */
        int numberingPlan;

        /**
         * The Numbering-plan-identification applies for Type-of-number = 000, 001 and 010.<br>
         * For Type-of-number = 101 bits 3,2,1,0 are reserved and shall be transmitted as 0000.<br>
         * Note that for addressing any of the entities SC, MSC, SGSN or MS, Numbering-plan-identification = 0001 will always be used.<br>
         * However, for addressing the SME, any specified Numbering-plan-identification value may be used.<br>
         */
        class NumberingPlan {

            /**
             * Unknown.
             */
            final static int UNKNOWN = 0;
            /**
             * ISDN/telephone numbering plan (E.164/E.163).
             */
            final static int TELEPHONE = 1;
            /**
             * Data numbering plan (X.121)
             */
            final static int DATA = 3;
            /**
             * Telex numbering plan
             */
            final static int TELEX = 4;
            /**
             * National numbering plan
             */
            final static int NATIONAL = 8;
            /**
             * Private numbering plan
             */
            final static int PRIVATE = 9;
            /**
             * ERMES numbering plan (ETSI DE/PS 3 01-3)
             */
            final static int ERMES = 10;
            /**
             * Reserved for extension
             */
            final static int RESERVED = 15;
        }

        TypeOfAddress(String hexString) {
            try {
                typeOfAddress = Integer.parseInt(hexString, 16);
            } catch (NumberFormatException numberFormatException) {
                if (DEBUG) {
                    numberFormatException.printStackTrace();
                }
                typeOfAddress = 0;
            }
            typeOfNumber = Utilities.intFromIntegerSubset(typeOfAddress, 4, 6);
            numberingPlan = Utilities.intFromIntegerSubset(typeOfAddress, 0, 3);
        }

        public String toString() {
            return "typeOfAddress=" + typeOfAddress + ", typeOfNumber=" + typeOfNumber + ", numberingPlan=" + numberingPlan;
        }
    }
    /**
     * 72 83 01 00 10 F5 : Service center number(in decimal semi-octets).<br>
     * The length of the phone number is odd (11), so a trailing F has been added to form proper octets.<br>
     * The phone number of this service center is "+27381000015". See below.
     */
    String smscNumber;
    /**
     * 04 : First octet of this PDU message.
     */
    FirstOctet firstOctet;

    class FirstOctet {

        /**
         * First octet of this PDU message integer value
         */
        int firstOctet;
        /**
         * Reply path. Parameter indicating that reply path exists.
         */
        boolean TP_RP;
        //------------------------------------------
        /**
         * User data header indicator. This bit is set to 1 if the User Data field starts with a header
         */
        boolean TP_UDHI;
        //------------------------------------------
        /**
         * Status report indication. This bit is set to 1 if a status report is going to be returned to the SME
         */
        boolean TP_SRI;
        /**
         * Status report request. This bit is set to 1 if a status report is requested
         */
        boolean TP_SRR;
        //------------------------------------------
        /**
         * Validity Period Format.<br>
         * see PDU.FirstOctet.ValidityPeriodFormat to explain
         */
        int TP_VPF;

        class ValidityPeriodFormat {                          //Validity Period Format. Bit4 and Bit3 specify the TP-VP field according to this table:

            /**
             * 0 0 : TP-VP field not present;
             */
            final static int NOT_PRESENT = 0;
            /**
             * 1 0 : TP-VP field present. Relative format (one octet)
             */
            final static int RELATIVE = 2;
            /**
             * 0 1 : TP-VP field present. Enhanced format (7 octets)
             */
            final static int ENHANCED = 1;
            /**
             * 1 1 : TP-VP field present. Absolute format (7 octets)
             */
            final static int ABSOLUTE = 3;
        }
        //------------------------------------------
        /**
         * More messages to send. This bit is set to 0 if there are more messages to send
         */
        boolean TP_MMS;
        /**
         * Reject duplicates. Parameter indicating whether or not the SC shall accept an PDU-SUBMIT for an SM still held in the SC which has the same TP-MR and the same TP-DA as a previously submitted SM from the same OA.
         */
        boolean TP_RD;
        //------------------------------------------
        /**
         * Message type indicator.<br>
         * 0 0 (SMS_DELIVER): This PDU is an PDU-DELIVER<br>
         * 0 1 (SMS_SUBMIT) : This PDU is an PDU-SUBMIT
         */
        int TP_MTI;
        /**
         * TP_MTI<br>
         * 0 0 : This PDU is an PDU-DELIVER
         */
        final int SMS_DELIVER = 0;
        /**
         * TP_MTI<br>
         * 0 1 : This PDU is an PDU-SUBMIT
         */
        final int SMS_SUBMIT = 1;

        FirstOctet(String hexString) {
            try {
                firstOctet = Integer.parseInt(hexString, 16);
            } catch (NumberFormatException numberFormatException) {
                if (DEBUG) {
                    numberFormatException.printStackTrace();
                }
                firstOctet = 0;
            }
            TP_MTI = Utilities.intFromIntegerSubset(firstOctet, 0, 1);
            if (TP_MTI == SMS_DELIVER) {
                TP_MMS = Utilities.bitAt(firstOctet, 2);
                TP_SRI = Utilities.bitAt(firstOctet, 5);
            } else {
                TP_RD = Utilities.bitAt(firstOctet, 2);
                TP_VPF = Utilities.intFromIntegerSubset(firstOctet, 3, 4);
                TP_SRR = Utilities.bitAt(firstOctet, 5);
            }
            TP_UDHI = Utilities.bitAt(firstOctet, 6);
            TP_RP = Utilities.bitAt(firstOctet, 7);
        }

        public String toString() {
            return "firstOctet=" + firstOctet + ", TP_RP=" + TP_RP + ", TP_UDHI=" + TP_UDHI + ", TP_SRI=" + TP_SRI + ", TP_SRR=" + TP_SRR + ", TP_VPF=" + TP_VPF + ", TP_MMS=" + TP_MMS + ", TP_RD=" + TP_RD + ", TP_MTI=" + TP_MTI;
        }
    }
    /**
     * 0B : Address-Length. Length of the sender number (0B hex = 11 dec)
     */
    int addressLength;
    /**
     * C8 : Type-of-address of the sender number
     */
    TypeOfAddress senderTypeOfAddress;
    /**
     * 72 38 88 09 00 F1 : Sender number (decimal semi-octets), with a trailing F
     */
    String senderNumber;
    /**
     * 00 : TP-PID. Protocol identifier.
     */
    String protocolIdentifier;
    /**
     * 00 : TP-DCS Data coding scheme
     */
    DataCodingScheme dataCodingScheme;

    /**
     * The TP-Data-Coding-Scheme field, defined in GSM 03.40, indicates the data coding scheme of the TP-UD field, and may indicate a message class.<br>
     * Any reserved codings shall be assumed to be the GSM default alphabet (the same as codepoint 00000000) by a receiving entity.
     */
    class DataCodingScheme {

        /**
         * Data Coding Scheme (TP-DCS) integer value
         */
        int TP_DCS;
        /**
         * Coding Group Bits 7..4<br>
         * see PDU.DataCodingScheme.Bits74 to explain
         */
        int bits74;

        class Bits74 {

            /**
             * General Data Coding indication
             */
            static final int GENERAL = 0;
            /**
             * Reserved coding groups
             */
            static final int RESERVED = 4;
            /**
             * Message Waiting Indication Group: Discard Message<br>
             * Bits 3..0 are coded exactly the same as Group 1101, however with bits 7..4 set to 1100 the mobile may discard the contents of the message, and only present the indication to the user.
             */
            static final int INDICATE_DISCARD_MESSAGE = 12;
            /**
             * This Group allows an indication to be provided to the user about status of types of message waiting on systems connected to the GSM PLMN. The mobile may present this indication as an icon on the screen, or other MMI indication. The mobile may take note of the Origination Address for message in this group and group 1100. For each indication supported, the mobile may provide storage for the Origination Address which is to control the mobile indication.<br>
             * Text included in the user data is coded in the Default Alphabet.<br>
             * If there a message is received with bits 7..4 set to 1101, the mobile shall store the text of the PDU message in addition to setting the indication.
             */
            static final int INDICATE_STORE_MESSAGE_GSM = 13;
            /**
             * Message Waiting Indication Group: Store Message<br>
             * The coding of bits 3..0 and functionality of this feature are the same as for the Message Waiting Indication Group above, (bits 7..4 set to 1101) with the exception that the text included in the user data is coded in the uncompressed UCS2 alphabet.
             */
            static final int INDICATE_STORE_MESSAGE_UCS2 = 14;
            /**
             * Data coding/message class
             */
            static final int DATA = 15;
        }
        /**
         * 0 : Text is uncompressed<br>
         * 1 : Text is compressed
         */
        boolean compressed;
        /**
         * 0 : Bits 1 and 0 are reserved and have no message class meaning<br>
         * 1 : Bits 1 and 0 have a message class meaning
         */
        boolean bits10HaveMessageClassMeaning;
        /**
         * Alphabet being used<br>
         * see PDU.DataCodingScheme.Alphabet to explain
         */
        int alphabet;

        class Alphabet {

            /**
             * Default alphabet
             */
            static final int GSM = 0;
            /**
             * 8 bit data
             */
            static final int DATA = 1;
            /**
             * UCS2 (16bit)
             */
            static final int UCS2 = 2;
            /**
             * Reserved
             */
            static final int RESERVED = 3;
        }
        /**
         * Message class<br>
         * see PDU.DataCodingScheme.MessageClass to explain
         */
        int messageClass;

        class MessageClass {

            /**
             * Immediate display (alert)
             */
            static final int ALERT = 0;
            /**
             * ME specific
             */
            static final int ME_SPEC = 1;
            /**
             * SIM specific
             */
            static final int SIM_SPEC = 2;
            /**
             * TE specific
             */
            static final int TE_SPEC = 3;
        }
        /**
         * 0 : Set Indication Inactive<br>
         * 1 : Set Indication Active
         */
        boolean indication;
        /**
         * Indication Type<br>
         * see PDU.DataCodingScheme.IndicationType to explain
         */
        int indicationType;

        class IndicationType {

            /**
             * Voicemail Message Waiting
             */
            static final int VOICEMAIL = 0;
            /**
             * Fax Message Waiting
             */
            static final int FAX = 1;
            /**
             * Electronic Mail Message Waiting
             */
            static final int EMAIL = 2;
            /**
             * Other Message Waiting.<br>
             * Mobile manufacturers may implement the "Other Message Waiting" indication as an additional indication without specifying the meaning. The meaning of this indication is intended to be standardized in the future, so Operators should not make use of this indication until the standard for this indication is finalized.
             */
            static final int OTHER = 3;
        }

        DataCodingScheme(String hexString) {
            try {
                TP_DCS = Integer.parseInt(hexString, 16);
            } catch (NumberFormatException numberFormatException) {
                if (DEBUG) {
                    numberFormatException.printStackTrace();
                }
                TP_DCS = 0;
            }
            bits74 = Utilities.intFromIntegerSubset(TP_DCS, 4, 7);
            if (bits74 >= 0 && bits74 <= 3) {
                bits74 = Bits74.GENERAL;
            } else if (bits74 >= 4 && bits74 <= 11) {
                bits74 = Bits74.RESERVED;
            }
            compressed = Utilities.bitAt(TP_DCS, 5);
            bits10HaveMessageClassMeaning = Utilities.bitAt(TP_DCS, 4);
            alphabet = Utilities.intFromIntegerSubset(TP_DCS, 2, 3);
            messageClass = Utilities.intFromIntegerSubset(TP_DCS, 0, 1);
            indication = Utilities.bitAt(TP_DCS, 3);
            indicationType = Utilities.intFromIntegerSubset(TP_DCS, 0, 1);
        }

        public String toString() {
            return "TP_DCS=" + TP_DCS + ", bits74=" + bits74 + ", compressed=" + compressed + ", bits10HaveMessageClassMeaning=" + bits10HaveMessageClassMeaning + ", alphabet=" + alphabet + ", messageClass=" + messageClass + ", indication=" + indication + ", indicationType=" + indicationType;
        }
    }
    /**
     * TP-SCTS. Time stamp (semi-octets)
     */
    TimeStamp timeStamp;

    class TimeStamp { //The TP-Service-Centre-Time-Stamp field is given in semi-octet representation (each semi-octet consists of two digital decimals), and represents the local time in the following way:

        /**
         * Raw hex representation
         */
        String timeStamp;
        int year; //These semi-octets are in "Swapped Nibble" mode
        int month; //BCD code where nibbles within octet is swapped. E.g.: 0x31 Represents value of 13
        int day; //E.g.: 0x99 0x20 0x21 0x50 0x75 0x03 0x21 means 12. Feb 1999 05:57:30 GMT+3
        int hour;
        int minute;
        int second;
        private int timezone;

        float getGMT() {
            return (float) timezone / 4;
        }

        int getMinutesDifferenceGMT() {
            return timezone * 15;
        }

        TimeStamp(String hexString) {
            timeStamp = hexString;
            String swapped = hexInternalSwap(timeStamp);
            year = Integer.parseInt(swapped.substring(0, 2));
            month = Integer.parseInt(swapped.substring(2, 4));
            day = Integer.parseInt(swapped.substring(4, 6));
            hour = Integer.parseInt(swapped.substring(6, 8));
            minute = Integer.parseInt(swapped.substring(8, 10));
            second = Integer.parseInt(swapped.substring(10, 12));
            timezone = Integer.parseInt(swapped.substring(12, 14));
        }

        public String toString() {
            return "timeStamp=" + timeStamp + ", year=" + year + ", month=" + month + ", day=" + day + ", hour=" + hour + ", minute=" + minute + ", second=" + second + ", timezone=" + getGMT();
        }
    }
    /**
     * 0A : TP-UDL. User data length, length of message.<br>
     * The TP-DCS field indicated 7-bit data, so the length here is the number of septets (10).<br>
     * If the TP-DCS field were set to indicate 8-bit data or Unicode, the length would be the number of octets (9).<br>
     */
    int userDataLength;
    /**
     * E8329BFD4697D9EC37 : TP-UD. Message "hellohello" , 8-bit octets representing 7-bit data.
     */
    UserData userData;

    class UserData {

        /**
         * Raw hex user data
         */
        String userData;
        /**
         * UserDataHeader
         */
        UDH userDataHeader;

        /**
         * User Data Header class
         */
        class UDH {

            /**
             * Raw hex user data header
             */
            String userDataHeader;
            /**
             * Field 1 (1 octet)<br>
             * Length of User Data Header, without this field.
             */
            int udhLength;
            /**
             * Field 2 (1 octet)<br>
             * 00,08 : Information Element Identifier
             */
            int informationElementID;
            /**
             * Field 2 (1 octet) Value<br>
             * 00 : Information Element Identifier, equal to 00 (Concatenated short messages, 8-bit reference number)
             */
            final static int IEID_CONCATENATED_8BITREFERENCE = 0;
            /**
             * Field 2 (1 octet) Value<br>
             * 08 : Information Element Identifier, equal to 08 (Concatenated short messages, 16-bit reference number)
             */
            final static int IEID_CONCATENATED_16BITREFERENCE = 8;
            /**
             * Field 3 (1 octet)<br>
             * Length of the remaining header, excluding the first 3 fields
             */
            int udhDataLengh;
            /**
             * Field 4 (1 or 2 octets)<br>
             * 00-FF, 0000-FFFF : CSMS reference number, must be same for all the SMS parts in the CSMS
             */
            int csmsReferenceNumber;
            /**
             * Field 5 (1 octet)<br>
             * 00-FF : total number of parts. The value shall remain constant for every short message which makes up the concatenated short message. If the value is zero then the receiving entity shall ignore the whole information element
             */
            int totalNumberOfParts;
            /**
             * Field 6 (1 octet)<br>
             * 00-FF : this part's number in the sequence. The value shall start at 1 and increment for every short message which makes up the concatenated short message. If the value is zero or greater than the value in Field 5 then the receiving entity shall ignore the whole information element.
             */
            int thisPartNumber;

            UDH(String userDataHex, FirstOctet firstOctet) {
                if (firstOctet.TP_UDHI) { //TP-UDHI set to 1, User Data Header present
                    udhLength = Integer.parseInt(userData.substring(0, 2), 16);
                    informationElementID = Integer.parseInt(userData.substring(2, 4), 16);
                    udhDataLengh = Integer.parseInt(userData.substring(4, 6), 16);
                    userDataHeader = userDataHex.substring(0, (2 * 3) + udhDataLengh);
                    int shift = (informationElementID == IEID_CONCATENATED_8BITREFERENCE ? 0 : 2);
                    csmsReferenceNumber = Integer.parseInt(userData.substring(6, 8 + shift), 16);
                    totalNumberOfParts = Integer.parseInt(userData.substring(8 + shift, 10 + shift), 16);
                    thisPartNumber = Integer.parseInt(userData.substring(10 + shift, 12 + shift), 16);
                }

            }

            String cropUserData(String userDataHex, DataCodingScheme DCS, FirstOctet firstOctet, int udhLength, int UDL) {
                if (DEBUG) {
                    Log.println("Start cropUserData: userDataHex=" + userDataHex + ", DCS=" + DCS + ", UDL=" + UDL);
                }
                if (DCS.alphabet == DataCodingScheme.Alphabet.GSM) {
                    if (DEBUG) {
                        Log.println("Alphabet is GSM");
                    }
                    int skipBits = 0;
                    if (firstOctet.TP_UDHI) {
                        skipBits = ((udhLength + 1) * 8) + ((7 - ((udhLength + 1) * 8) % 7) % 7); //Number of bits to skip from beginning
                    }
                    if (DEBUG) {
                        Log.println("skipBits=" + skipBits);
                    }
                    return fromHexStringToAlphanumeric(userDataHex, UDL, skipBits);
                } else if (DCS.alphabet == DataCodingScheme.Alphabet.UCS2) {
                    if (DEBUG) {
                        Log.println("Alphabet is UCS2");
                    }
                    if (firstOctet.TP_UDHI) {
                        return ATStringConverter.UCS2Hex2Java(userDataHex.substring((udhLength + 1) * 2));
                    } else {
                        return ATStringConverter.UCS2Hex2Java(userDataHex);
                    }
                } else {
                    if (DEBUG) {
                        Log.println("Alphabet is Unknown");
                    }
                    if (firstOctet.TP_UDHI) {
                        return ATStringConverter.UCS2Hex2Java(userDataHex.substring((udhLength + 1) * 2));
                    } else {
                        return ATStringConverter.UCS2Hex2Java(userDataHex);
                    }
                }
            }
        }
        /**
         * User Data, cleaned from UserDataHeader, and converted to readable String
         */
        String decodedUserData;

        UserData(String hexString, FirstOctet firstOctet, DataCodingScheme DCS, int UDL) {
            userData = hexString;
            userDataHeader = new UDH(userData, firstOctet);
            decodedUserData = userDataHeader.cropUserData(userData, DCS, firstOctet, userDataHeader.udhLength, UDL);
        }

        public String toString() {
            return "decodedUserData=" + decodedUserData;
        }
    }

    private String hexInternalSwap(String hex) {
        if (DEBUG) {
            Log.println("Starting hexInternalSwap: " + hex);
        }
        if (hex.length() % 2 == 1) {
            if (DEBUG) {
                Log.println("ERROR: number of characters is odd");
            }
            return null;
        }
        String result = "";
        for (int i = 0; i < hex.length(); i++) {
            result += (i % 2 == 0 ? hex.charAt(i + 1) : hex.charAt(i - 1));
        }
        return result;
    }

    private String fromHexStringToAlphanumeric(String hexString, int numberOfUsefulSeptets, int skipBits) {
        if (DEBUG) {
            Log.println("Starting fromHexStringToAlphanumeric: hexString=" + hexString + ", numberOfUsefulSeptets=" + numberOfUsefulSeptets + ", skipBits=" + skipBits);
        }
        boolean[] udBool = new boolean[(hexString.length() / 2) * 8]; //User data presented in binary array
        if (DEBUG) {
            Log.println("udBool.length=" + udBool.length);
        }
        for (int i = 0; i < (hexString.length() / 2); i++) { //Convert all data to binary array
            boolean[] t = Utilities.intToBinaryArrayFixedWidth(Integer.parseInt(hexString.substring(i * 2, (i + 1) * 2), 16), 8);
            System.arraycopy(t, 0, udBool, i * 8, 8);
        }
        byte[] septets = new byte[numberOfUsefulSeptets - (skipBits / 7)];        //future array from which we'll make string
        if (DEBUG) {
            Log.println("septets.length=" + septets.length);
        }
        for (int i = skipBits / 7; i < numberOfUsefulSeptets; i++) { //Fill the septets array with portions of 7 bits from udBool, converted to byte
            boolean[] t = new boolean[7];
            System.arraycopy(udBool, i * 7, t, 0, 7);
            if (DEBUG) {
                String boolArrToString = "";
                for (int j = 0; j < t.length; j++) {
                    boolArrToString += (t[j] == true ? '1' : '0');
                }
                if (DEBUG) {
                    Log.println("t=" + boolArrToString);
                }
            }
            septets[i - (skipBits / 7)] = (byte) Utilities.binaryArrayToInt(t);
            if (DEBUG) {
                Log.println("septets[" + (i - (skipBits / 7)) + "]=" + septets[i - (skipBits / 7)]);
            }
        }
        return ATStringConverter.GSM2Java(new String(septets));
    }

    /**
     * Makes a PDU object from incoming HEX string
     * @param hexString 
     */
    public PDU(String hexString) {
        parsePDU(hexString);
    }

    /**
     * Filling all the fields of the PDU object
     * @param hexString 
     */
    private void parsePDU(String hexString) {
        if (DEBUG) {
            Log.println("Starting to parse PDU: " + hexString);
        }
        hexStringPDU = hexString;
        int index = 0;
        smscLength = Integer.parseInt(hexStringPDU.substring(index, index + 2), 16);
        if (DEBUG) {
            Log.println("smscLength: " + smscLength);
        }
        index += 2;
        if (smscLength > 0) {
            smscTypeOfAddress = new TypeOfAddress(hexStringPDU.substring(index, index + 2));
            if (DEBUG) {
                Log.println("smscTypeOfAddress: " + smscTypeOfAddress);
            }
            index += 2;
            smscNumber = parseNumberFromHexPDU(hexStringPDU.substring(index, (smscLength + 1) * 2), smscTypeOfAddress, ((smscLength - 1) * 8) / 7);
            if (DEBUG) {
                Log.println("smscNumber: " + smscNumber);
            }
            index = (smscLength + 1) * 2;
        }
        firstOctet = new FirstOctet(hexStringPDU.substring(index, index + 2));
        if (DEBUG) {
            Log.println("firstOctet: " + firstOctet);
        }
        index += 2;
        addressLength = Integer.parseInt(hexStringPDU.substring(index, index + 2), 16);
        if (DEBUG) {
            Log.println("addressLength: " + addressLength);
        }
        index += 2;
        senderTypeOfAddress = new TypeOfAddress(hexStringPDU.substring(index, index + 2));
        if (DEBUG) {
            Log.println("senderTypeOfAddress: " + senderTypeOfAddress);
        }
        index += 2;
        senderNumber = parseNumberFromHexPDU(hexStringPDU.substring(index, index + (addressLength % 2 == 0 ? addressLength : addressLength + 1)), senderTypeOfAddress, addressLength);
        if (DEBUG) {
            Log.println("senderNumber: " + senderNumber);
        }
        index += (addressLength % 2 == 0 ? addressLength : addressLength + 1);
        protocolIdentifier = hexStringPDU.substring(index, index + 2);
        if (DEBUG) {
            Log.println("protocolIdentifier: " + protocolIdentifier);
        }
        index += 2;
        dataCodingScheme = new DataCodingScheme(hexStringPDU.substring(index, index + 2));
        if (DEBUG) {
            Log.println("dataCodingScheme: " + dataCodingScheme);
        }
        index += 2;
        timeStamp = new TimeStamp(hexStringPDU.substring(index, index + (7 * 2)));
        if (DEBUG) {
            Log.println("timeStamp: " + timeStamp);
        }
        index += 7 * 2;
        userDataLength = Integer.parseInt(hexStringPDU.substring(index, index + 2), 16);
        if (DEBUG) {
            Log.println("userDataLength: " + userDataLength);
        }
        index += 2;
        userData = new UserData(hexStringPDU.substring(index), firstOctet, dataCodingScheme, userDataLength);
        if (DEBUG) {
            Log.println("userData: " + userData);
        }
    }

    /**
     * Gets phone number from PDU's hex part, as a String
     * @param hex
     * @param toa
     * @param addressLength
     * @return 
     */
    private String parseNumberFromHexPDU(String hex, PDU.TypeOfAddress toa, int addressLength) {
        String number;
        if (toa.typeOfNumber == TypeOfAddress.TypeOfNumber.ALPHANUMERIC) {
            number = fromHexStringToAlphanumeric(hex, (addressLength * 4) / 7, 0);
        } else {
            number = hexInternalSwap(hex);
            if (number.endsWith("F")) {
                number = number.substring(0, number.length() - 1);
            }
            if (toa.numberingPlan == PDU.TypeOfAddress.NumberingPlan.TELEPHONE) {
                number = "+" + number;
            }
        }
        return number;
    }

    /**
     * Gets sender number as a String, from the PDU
     * @return 
     */
    public String getSenderNumber() {
        return senderNumber;
    }

    /**
     * Gets user data in decoded form, from the PDU
     * @return 
     */
    public String getUserData() {
        return userData.decodedUserData;
    }

    /**
     * Returns true if the PDU is part of a bigger concatenated SMS
     * @return 
     */
    public boolean isConcatenatedSMSPart() {
        return firstOctet.TP_UDHI;
    }
}

