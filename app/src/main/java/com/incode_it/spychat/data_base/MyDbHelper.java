package com.incode_it.spychat.data_base;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.MyContacts;
import com.incode_it.spychat.alarm.TimeHolder;
import com.incode_it.spychat.country_selection.Country;
import com.incode_it.spychat.data_base.MReaderContract.Chat;
import com.incode_it.spychat.data_base.MReaderContract.Countries;
import com.incode_it.spychat.data_base.MReaderContract.RegisteredContact;

import java.util.ArrayList;

public class MyDbHelper extends SQLiteOpenHelper
{
    public static final String LOG_TAG = "curs";
    ArrayList<Message> messageArrayList;
    private Context context;

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INT = " INTEGER";
    private static final String TYPE_REAL = " REAL";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CHAT_TABLE =
            "CREATE TABLE " + Chat.TABLE_NAME + " (" +
                    Chat._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Chat.MESSAGE + TYPE_TEXT + COMMA_SEP +
                    Chat.SENDER_PHONE_NUMBER + TYPE_TEXT + COMMA_SEP +
                    Chat.RECEIVER_PHONE_NUMBER + TYPE_TEXT + COMMA_SEP +
                    Chat.DATE + TYPE_TEXT + COMMA_SEP +
                    Chat.STATE + TYPE_INT + COMMA_SEP +
                    Chat.MESSAGE_ID + TYPE_INT + COMMA_SEP +
                    Chat.REMOVAL_TIME + TYPE_INT + COMMA_SEP +
                    Chat.MESSAGE_TYPE + TYPE_INT + COMMA_SEP +
                    Chat.IS_VIEWED + TYPE_INT + COMMA_SEP +
                    Chat.AUDIO_DURATION + TYPE_INT + COMMA_SEP +
                    Chat.COLOR + TYPE_INT + COMMA_SEP  +
                    Chat.SIZE + TYPE_REAL + COMMA_SEP +
                    Chat.ANIMATION + TYPE_INT + COMMA_SEP +
                    Chat.FONT + TYPE_TEXT + COMMA_SEP +
                    Chat.OWNER + TYPE_TEXT + " )";

    private static final String SQL_DELETE_CHAT_TABLE =
            "DROP TABLE IF EXISTS " + Chat.TABLE_NAME;

    private static final String SQL_CREATE_CONTACT_TABLE =
            "CREATE TABLE " + RegisteredContact.TABLE_NAME + " (" +
                    RegisteredContact._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RegisteredContact.PHONE_NUMBER + TYPE_TEXT+ " )";

    private static final String SQL_DELETE_CONTACT_TABLE =
            "DROP TABLE IF EXISTS " + RegisteredContact.TABLE_NAME;

    private static final String SQL_CREATE_COUNTRIES_TABLE =
            "CREATE TABLE " + Countries.TABLE_NAME + " (" +
                    Countries._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Countries.CODE + TYPE_TEXT + COMMA_SEP +
                    Countries.NAME + TYPE_TEXT + COMMA_SEP +
                    Countries.NATIVE + TYPE_TEXT + COMMA_SEP +
                    Countries.PHONE + TYPE_TEXT + COMMA_SEP +
                    Countries.CONTINENT + TYPE_TEXT + COMMA_SEP +
                    Countries.CAPITAL + TYPE_TEXT + COMMA_SEP +
                    Countries.CURRENCY + TYPE_TEXT + COMMA_SEP +
                    Countries.LANGUAGES + TYPE_TEXT  +" )";

    private static final String SQL_DELETE_COUNTRIES_TABLE =
            "DROP TABLE IF EXISTS " + Countries.TABLE_NAME;

    public static final int DATABASE_VERSION = 18;
    public static final String DATABASE_NAME = "SpyChat.db";

    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CHAT_TABLE);
        db.execSQL(SQL_CREATE_CONTACT_TABLE);
        db.execSQL(SQL_CREATE_COUNTRIES_TABLE);

        insertCountries(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        drop(db);
        onCreate(db);
    }

    public static synchronized void drop(SQLiteDatabase db)
    {
        db.execSQL(SQL_DELETE_CHAT_TABLE);
        db.execSQL(SQL_DELETE_CONTACT_TABLE);
        db.execSQL(SQL_DELETE_COUNTRIES_TABLE);
    }

    public static synchronized void insertMessage(SQLiteDatabase db, Message message, Context context)
    {
        ContentValues values = new ContentValues();
        values.put(Chat.MESSAGE, message.getMessage());
        values.put(Chat.SENDER_PHONE_NUMBER, message.getSenderPhoneNumber());
        values.put(Chat.RECEIVER_PHONE_NUMBER, message.getReceiverPhoneNumber());
        values.put(Chat.DATE, message.getDate());
        values.put(Chat.STATE, message.getState());
        values.put(Chat.MESSAGE_ID, message.getMessageId());
        values.put(Chat.REMOVAL_TIME, message.getRemovalTime());
        values.put(Chat.MESSAGE_TYPE, message.messageType);
        values.put(Chat.IS_VIEWED, message.isViewed);
        values.put(Chat.AUDIO_DURATION, message.audioDuration);
        values.put(Chat.COLOR, message.getColor());
        values.put(Chat.SIZE, message.getTextSize());
        values.put(Chat.ANIMATION, message.isAnimated() ? 1 : 0);
        values.put(Chat.FONT, message.getFont());
        values.put(Chat.OWNER, message.ownerPhoneNumber);

        db.insert(Chat.TABLE_NAME, null, values);
        db.close();
    }

    public static synchronized ArrayList<TimeHolder> readTime(SQLiteDatabase db, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        ArrayList<TimeHolder> arrayList = new ArrayList<>();

        String sql = "SELECT " +
                Chat.MESSAGE_ID + COMMA_SEP +
                " " + Chat.REMOVAL_TIME +
                " FROM " + Chat.TABLE_NAME +
                " WHERE " + Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst())
        {
            do
            {
                TimeHolder timeHolder = new TimeHolder();

                timeHolder.mId = cursor.getInt(0);
                timeHolder.individualRemovalTime = cursor.getLong(1);

                arrayList.add(timeHolder);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return arrayList;
    }

    public static synchronized void removeMessageFromGlobalTimer(SQLiteDatabase db, long mId)
    {
        String whereClause = Chat.MESSAGE_ID + " = " + mId;
        db.delete(Chat.TABLE_NAME, whereClause, null);
        db.close();
    }

    public static synchronized void removeMessageFromIndividualTimer(SQLiteDatabase db, long mId)
    {
        String whereClause = Chat.MESSAGE_ID + " = " + mId;
        db.delete(Chat.TABLE_NAME, whereClause, null);
        db.close();
    }

    public static synchronized void removeMessageFromUI(SQLiteDatabase db, long mId, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        String whereClause = Chat.MESSAGE_ID + " = " + mId + " AND " + Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";

        db.delete(Chat.TABLE_NAME, whereClause, null);
        db.close();
    }

    public static synchronized ArrayList<Message> readContactMessages(SQLiteDatabase db, MyContacts.Contact contact, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        String orderBy = "datetime("+Chat.DATE+")" + " ASC";

        String sql = "SELECT * FROM " + Chat.TABLE_NAME
                + " WHERE (" + Chat.SENDER_PHONE_NUMBER + " LIKE '%"+contact.phoneNumber+"%' OR "
                + Chat.RECEIVER_PHONE_NUMBER + " LIKE '%"+contact.phoneNumber+"%') AND "
                + Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'"
                + " ORDER BY " + orderBy;
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<Message> messagesArr = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do
            {
                String textMessage = cursor.getString(1);
                String senderPhoneNumber = cursor.getString(2);
                String receiverPhoneNumber = cursor.getString(3);
                String date = cursor.getString(4);
                int state = cursor.getInt(5);
                int messageId = cursor.getInt(6);
                long removalTime = cursor.getLong(7);
                int messageType = cursor.getInt(8);
                int isViewed = cursor.getInt(9);
                int audioDuration = cursor.getInt(10);

                Message message = new Message(textMessage, senderPhoneNumber, receiverPhoneNumber, date, state, messageId, removalTime, messageType, ownerPhoneNumber);
                message.setColor(cursor.getInt(11));
                message.setTextSize(cursor.getFloat(12));
                message.setAnimated(cursor.getFloat(13) == 1);
                message.setFont(cursor.getString(14));
                message.isViewed = isViewed;
                message.audioDuration = audioDuration;

                messagesArr.add(message);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messagesArr;
    }

    public static synchronized Message readMessage(SQLiteDatabase db, int messageId, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        String sql = "SELECT * FROM " + Chat.TABLE_NAME + " WHERE " + Chat.MESSAGE_ID + " = "+messageId
                + " AND "+ Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();

        String textMessage = cursor.getString(1);
        String senderPhoneNumber = cursor.getString(2);
        String receiverPhoneNumber = cursor.getString(3);
        String date = cursor.getString(4);
        int state = cursor.getInt(5);
        messageId = cursor.getInt(6);
        long removalTime = cursor.getLong(7);
        int messageType = cursor.getInt(8);
        int isViewed = cursor.getInt(9);
        int audioDuration = cursor.getInt(10);
        Message message = new Message(textMessage, senderPhoneNumber, receiverPhoneNumber, date, state, messageId, removalTime, messageType, ownerPhoneNumber);
        message.setColor(cursor.getInt(11));
        message.setTextSize(cursor.getFloat(12));
        message.setAnimated(cursor.getFloat(13) == 1);
        message.setFont(cursor.getString(14));
        message.isViewed = isViewed;
        message.audioDuration = audioDuration;
        cursor.close();
        db.close();
        return message;
    }

    public static synchronized ArrayList<String> readRegisteredContacts(SQLiteDatabase db)
    {
        String sql = "SELECT * FROM "+ RegisteredContact.TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<String> contactsArr = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do
            {
                String phoneNumber = cursor.getString(1);
                contactsArr.add(phoneNumber);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contactsArr;
    }

    public static synchronized void insertRegisteredContacts(SQLiteDatabase db, ArrayList<String> registeredContacts)
    {
        db.execSQL(SQL_DELETE_CONTACT_TABLE);
        db.execSQL(SQL_CREATE_CONTACT_TABLE);
        ContentValues values = new ContentValues();

        for (String phone: registeredContacts)
        {
            values.put(RegisteredContact.PHONE_NUMBER, phone);
            db.insert(RegisteredContact.TABLE_NAME, null, values);
            values.clear();
        }
        db.close();
    }

    public static synchronized void updateMessageState(SQLiteDatabase db, int state, int messageId, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        ContentValues values = new ContentValues();

        String whereClause = Chat.MESSAGE_ID + "=" + messageId
                + " AND "+ Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";

        values.put(Chat.STATE, state);

        db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();

    }

    public static synchronized void updateMessageViewedState(SQLiteDatabase db, int isViewed, int messageId, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        ContentValues values = new ContentValues();

        String whereClause = Chat.MESSAGE_ID + "=" + messageId
                + " AND "+ Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";

        values.put(Chat.IS_VIEWED, isViewed);

        db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();

    }

    public static synchronized void updateMediaPath(SQLiteDatabase db, String path, int messageId, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        ContentValues values = new ContentValues();

        String whereClause = Chat.MESSAGE_ID + "=" + messageId
                + " AND "+ Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";

        values.put(Chat.MESSAGE, path);

        int num = db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();

    }

    public static synchronized void updateAllMessagesViewState(SQLiteDatabase db, MyContacts.Contact contact, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        ContentValues values = new ContentValues();

        String whereClause = Chat.SENDER_PHONE_NUMBER + " LIKE '%" + contact.phoneNumber + "%'"
                + " AND "+ Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";
        values.put(Chat.IS_VIEWED, 1);

        int num = db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();

    }

    public static synchronized void updateMessageTimer(SQLiteDatabase db, long mId, long removalTime, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ownerPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        ContentValues values = new ContentValues();
        String whereClause = Chat.MESSAGE_ID + "=" + mId
                + " AND "+ Chat.OWNER + " LIKE '%" + ownerPhoneNumber + "%'";

        values.put(Chat.REMOVAL_TIME, removalTime);

        db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();
    }

    public static synchronized  ArrayList<Country> readCountries(SQLiteDatabase db)
    {
        String sql = "SELECT name, native, code, phone FROM " + Countries.TABLE_NAME + " ORDER BY name ASC";
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<Country> countries = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do
            {
                String nameEnglish = cursor.getString(0);
                String nameNative = cursor.getString(1);
                String codeISO = cursor.getString(2);
                String codePhone = cursor.getString(3);

                countries.add(new Country(nameNative, nameEnglish, codePhone, codeISO));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return countries;
    }

    private void insertCountries(SQLiteDatabase db)
    {
        String query = "INSERT INTO countries (code, name, native, phone, continent, capital, currency, languages) VALUES" +
                "  ('AD', 'Andorra', 'Andorra', '+376', 'EU', 'Andorra la Vella', 'EUR', 'ca'),\n"+
                "  ('AE', 'United Arab Emirates', 'دولة الإمارات العربية المتحدة', '+971', 'AS', 'Abu Dhabi', 'AED', 'ar'),\n" +
                "  ('AF', 'Afghanistan', 'افغانستان', '+93', 'AS', 'Kabul', 'AFN', 'ps,uz,tk'),\n" +
                "  ('AG', 'Antigua and Barbuda', 'Antigua and Barbuda', '+1268', 'NA', 'Saint John''s', 'XCD', 'en'),\n" +
                "  ('AI', 'Anguilla', 'Anguilla', '+1264', 'NA', 'The Valley', 'XCD', 'en'),\n" +
                "  ('AL', 'Albania', 'Shqipëria', '+355', 'EU', 'Tirana', 'ALL', 'sq'),\n" +
                "  ('AM', 'Armenia', 'Հայաստան', '+374', 'AS', 'Yerevan', 'AMD', 'hy,ru'),\n" +
                "  ('AO', 'Angola', 'Angola', '+244', 'AF', 'Luanda', 'AOA', 'pt'),\n" +
                "  ('AR', 'Argentina', 'Argentina', '+54', 'SA', 'Buenos Aires', 'ARS', 'es,gn'),\n" +
                "  ('AS', 'American Samoa', 'American Samoa', '+1684', 'OC', 'Pago Pago', 'USD', 'en,sm'),\n" +
                "  ('AT', 'Austria', 'Österreich', '+43', 'EU', 'Vienna', 'EUR', 'de'),\n" +
                "  ('AU', 'Australia', 'Australia', '+61', 'OC', 'Canberra', 'AUD', 'en'),\n" +
                "  ('AW', 'Aruba', 'Aruba', '+297', 'NA', 'Oranjestad', 'AWG', 'nl,pa'),\n" +
                "  ('AX', 'Aland', 'Åland', '+358', 'EU', 'Mariehamn', 'EUR', 'sv'),\n" +
                "  ('AZ', 'Azerbaijan', 'Azərbaycan', '+994', 'AS', 'Baku', 'AZN', 'az,hy'),\n" +
                "  ('BA', 'Bosnia and Herzegovina', 'Bosna i Hercegovina', '+387', 'EU', 'Sarajevo', 'BAM', 'bs,hr,sr'),\n" +
                "  ('BB', 'Barbados', 'Barbados', '+1246', 'NA', 'Bridgetown', 'BBD', 'en'),\n" +
                "  ('BD', 'Bangladesh', 'Bangladesh', '+880', 'AS', 'Dhaka', 'BDT', 'bn'),\n" +
                "  ('BE', 'Belgium', 'België', '+32', 'EU', 'Brussels', 'EUR', 'nl,fr,de'),\n" +
                "  ('BF', 'Burkina Faso', 'Burkina Faso', '+226', 'AF', 'Ouagadougou', 'XOF', 'fr,ff'),\n" +
                "  ('BG', 'Bulgaria', 'България', '+359', 'EU', 'Sofia', 'BGN', 'bg'),\n" +
                "  ('BH', 'Bahrain', 'البحرين', '+973', 'AS', 'Manama', 'BHD', 'ar'),\n" +
                "  ('BI', 'Burundi', 'Burundi', '+257', 'AF', 'Bujumbura', 'BIF', 'fr,rn'),\n" +
                "  ('BJ', 'Benin', 'Bénin', '+229', 'AF', 'Porto-Novo', 'XOF', 'fr'),\n" +
                "  ('BL', 'Saint Barthélemy', 'Saint-Barthélemy', '+590', 'NA', 'Gustavia', 'EUR', 'fr'),\n" +
                "  ('BM', 'Bermuda', 'Bermuda', '+1441', 'NA', 'Hamilton', 'BMD', 'en'),\n" +
                "  ('BN', 'Brunei', 'Negara Brunei Darussalam', '+673', 'AS', 'Bandar Seri Begawan', 'BND', 'ms'),\n" +
                "  ('BO', 'Bolivia', 'Bolivia', '+591', 'SA', 'Sucre', 'BOB,BOV', 'es,ay,qu'),\n" +
                "  ('BQ', 'Bonaire', 'Bonaire', '+599', 'NA', 'Kralendijk', 'USD', 'nl'),\n" +
                "  ('BR', 'Brazil', 'Brasil', '+55', 'SA', 'Brasília', 'BRL', 'pt'),\n" +
                "  ('BS', 'Bahamas', 'Bahamas', '+1242', 'NA', 'Nassau', 'BSD', 'en'),\n" +
                "  ('BT', 'Bhutan', 'ʼbrug-yul', '+975', 'AS', 'Thimphu', 'BTN,INR', 'dz'),\n" +
                "  ('BV', 'Bouvet Island', 'Bouvetøya', '+226', 'AN', '', 'NOK', ''),\n" +
                "  ('BW', 'Botswana', 'Botswana', '+267', 'AF', 'Gaborone', 'BWP', 'en,tn'),\n" +
                "  ('BY', 'Belarus', 'Белару́сь', '+375', 'EU', 'Minsk', 'BYR', 'be,ru'),\n" +
                "  ('BZ', 'Belize', 'Belize', '+501', 'NA', 'Belmopan', 'BZD', 'en,es'),\n" +
                "  ('CA', 'Canada', 'Canada', '+1', 'NA', 'Ottawa', 'CAD', 'en,fr'),\n" +
                "  ('CC', 'Cocos [Keeling] Islands', 'Cocos (Keeling) Islands', '+61', 'AS', 'West Island', 'AUD', 'en'),\n" +
                "  ('CD', 'Democratic Republic of the Congo', 'République démocratique du Congo', '+243', 'AF', 'Kinshasa', 'CDF', 'fr,ln,kg,sw,lu'),\n" +
                "  ('CF', 'Central African Republic', 'Ködörösêse tî Bêafrîka', '+236', 'AF', 'Bangui', 'XAF', 'fr,sg'),\n" +
                "  ('CG', 'Republic of the Congo', 'République du Congo', '+242', 'AF', 'Brazzaville', 'XAF', 'fr,ln'),\n" +
                "  ('CH', 'Switzerland', 'Schweiz', '+41', 'EU', 'Bern', 'CHE,CHF,CHW', 'de,fr,it'),\n" +
                "  ('CI', 'Ivory Coast', 'Côte d''Ivoire', '+225', 'AF', 'Yamoussoukro', 'XOF', 'fr'),\n" +
                "  ('CK', 'Cook Islands', 'Cook Islands', '+682', 'OC', 'Avarua', 'NZD', 'en'),\n" +
                "  ('CL', 'Chile', 'Chile', '+56', 'SA', 'Santiago', 'CLF,CLP', 'es'),\n" +
                "  ('CM', 'Cameroon', 'Cameroon', '+237', 'AF', 'Yaoundé', 'XAF', 'en,fr'),\n" +
                "  ('CN', 'China', '中国', '+86', 'AS', 'Beijing', 'CNY', 'zh'),\n" +
                "  ('CO', 'Colombia', 'Colombia', '+57', 'SA', 'Bogotá', 'COP', 'es'),\n" +
                "  ('CR', 'Costa Rica', 'Costa Rica', '+506', 'NA', 'San José', 'CRC', 'es'),\n" +
                "  ('CU', 'Cuba', 'Cuba', '+53', 'NA', 'Havana', 'CUC,CUP', 'es'),\n" +
                "  ('CV', 'Cape Verde', 'Cabo Verde', '+238', 'AF', 'Praia', 'CVE', 'pt'),\n" +
                "  ('CW', 'Curacao', 'Curaçao', '+5999', 'NA', 'Willemstad', 'ANG', 'nl,pa,en'),\n" +
                "  ('CX', 'Christmas Island', 'Christmas Island', '+61', 'AS', 'Flying Fish Cove', 'AUD', 'en'),\n" +
                "  ('CY', 'Cyprus', 'Κύπρος', '+357', 'EU', 'Nicosia', 'EUR', 'el,tr,hy'),\n" +
                "  ('CZ', 'Czech Republic', 'Česká republika', '+420', 'EU', 'Prague', 'CZK', 'cs,sk'),\n" +
                "  ('DE', 'Germany', 'Deutschland', '+49', 'EU', 'Berlin', 'EUR', 'de'),\n" +
                "  ('DJ', 'Djibouti', 'Djibouti', '+253', 'AF', 'Djibouti', 'DJF', 'fr,ar'),\n" +
                "  ('DK', 'Denmark', 'Danmark', '+45', 'EU', 'Copenhagen', 'DKK', 'da'),\n" +
                "  ('DM', 'Dominica', 'Dominica', '+1767', 'NA', 'Roseau', 'XCD', 'en'),\n" +
                "  ('DO', 'Dominican Republic', 'República Dominicana', '+1', 'NA', 'Santo Domingo', 'DOP', 'es'),\n" +
                "  ('DZ', 'Algeria', 'الجزائر', '+213', 'AF', 'Algiers', 'DZD', 'ar'),\n" +
                "  ('EC', 'Ecuador', 'Ecuador', '+593', 'SA', 'Quito', 'USD', 'es'),\n" +
                "  ('EE', 'Estonia', 'Eesti', '+372', 'EU', 'Tallinn', 'EUR', 'et'),\n" +
                "  ('EG', 'Egypt', 'مصر\u200E', '+20', 'AF', 'Cairo', 'EGP', 'ar'),\n" +
                "  ('EH', 'Western Sahara', 'الصحراء الغربية', '+212', 'AF', 'El Aaiún', 'MAD,DZD,MRO', 'es'),\n" +
                "  ('ER', 'Eritrea', 'ኤርትራ', '+291', 'AF', 'Asmara', 'ERN', 'ti,ar,en'),\n" +
                "  ('ES', 'Spain', 'España', '+34', 'EU', 'Madrid', 'EUR', 'es,eu,ca,gl,oc'),\n" +
                "  ('ET', 'Ethiopia', 'ኢትዮጵያ', '+251', 'AF', 'Addis Ababa', 'ETB', 'am'),\n" +
                "  ('FI', 'Finland', 'Suomi', '+358', 'EU', 'Helsinki', 'EUR', 'fi,sv'),\n" +
                "  ('FJ', 'Fiji', 'Fiji', '+679', 'OC', 'Suva', 'FJD', 'en,fj,hi,ur'),\n" +
                "  ('FK', 'Falkland Islands', 'Falkland Islands', '+500', 'SA', 'Stanley', 'FKP', 'en'),\n" +
                "  ('FM', 'Micronesia', 'Micronesia', '+691', 'OC', 'Palikir', 'USD', 'en'),\n" +
                "  ('FO', 'Faroe Islands', 'Føroyar', '+298', 'EU', 'Tórshavn', 'DKK', 'fo'),\n" +
                "  ('FR', 'France', 'France', '+33', 'EU', 'Paris', 'EUR', 'fr'),\n" +
                "  ('GA', 'Gabon', 'Gabon', '+241', 'AF', 'Libreville', 'XAF', 'fr'),\n" +
                "  ('GB', 'United Kingdom', 'United Kingdom', '+44', 'EU', 'London', 'GBP', 'en'),\n" +
                "  ('GD', 'Grenada', 'Grenada', '+1473', 'NA', 'St. George''s', 'XCD', 'en'),\n" +
                "  ('GE', 'Georgia', 'საქართველო', '+995', 'AS', 'Tbilisi', 'GEL', 'ka'),\n" +
                "  ('GF', 'French Guiana', 'Guyane française', '+594', 'SA', 'Cayenne', 'EUR', 'fr'),\n" +
                "  ('GG', 'Guernsey', 'Guernsey', '+44', 'EU', 'St. Peter Port', 'GBP', 'en,fr'),\n" +
                "  ('GH', 'Ghana', 'Ghana', '+233', 'AF', 'Accra', 'GHS', 'en'),\n" +
                "  ('GI', 'Gibraltar', 'Gibraltar', '+350', 'EU', 'Gibraltar', 'GIP', 'en'),\n" +
                "  ('GL', 'Greenland', 'Kalaallit Nunaat', '+299', 'NA', 'Nuuk', 'DKK', 'kl'),\n" +
                "  ('GM', 'Gambia', 'Gambia', '+220', 'AF', 'Banjul', 'GMD', 'en'),\n" +
                "  ('GN', 'Guinea', 'Guinée', '+224', 'AF', 'Conakry', 'GNF', 'fr,ff'),\n" +
                "  ('GP', 'Guadeloupe', 'Guadeloupe', '+590', 'NA', 'Basse-Terre', 'EUR', 'fr'),\n" +
                "  ('GQ', 'Equatorial Guinea', 'Guinea Ecuatorial', '+240', 'AF', 'Malabo', 'XAF', 'es,fr'),\n" +
                "  ('GR', 'Greece', 'Ελλάδα', '+30', 'EU', 'Athens', 'EUR', 'el'),\n" +
                "  ('GS', 'South Georgia and the South Sandwich Islands', 'South Georgia', '+500', 'AN', 'King Edward Point', 'GBP', 'en'),\n" +
                "  ('GT', 'Guatemala', 'Guatemala', '+502', 'NA', 'Guatemala City', 'GTQ', 'es'),\n" +
                "  ('GU', 'Guam', 'Guam', '+1671', 'OC', 'Hagåtña', 'USD', 'en,ch,es'),\n" +
                "  ('GW', 'Guinea-Bissau', 'Guiné-Bissau', '+245', 'AF', 'Bissau', 'XOF', 'pt'),\n" +
                "  ('GY', 'Guyana', 'Guyana', '+592', 'SA', 'Georgetown', 'GYD', 'en'),\n" +
                "  ('HK', 'Hong Kong', '香港', '+852', 'AS', 'City of Victoria', 'HKD', 'zh,en'),\n" +
                "  ('HM', 'Heard Island and McDonald Islands', 'Heard Island and McDonald Islands', '+61', 'AN', '', 'AUD', 'en'),\n" +
                "  ('HN', 'Honduras', 'Honduras', '+504', 'NA', 'Tegucigalpa', 'HNL', 'es'),\n" +
                "  ('HR', 'Croatia', 'Hrvatska', '+385', 'EU', 'Zagreb', 'HRK', 'hr'),\n" +
                "  ('HT', 'Haiti', 'Haïti', '+509', 'NA', 'Port-au-Prince', 'HTG,USD', 'fr,ht'),\n" +
                "  ('HU', 'Hungary', 'Magyarország', '+36', 'EU', 'Budapest', 'HUF', 'hu'),\n" +
                "  ('ID', 'Indonesia', 'Indonesia', '+62', 'AS', 'Jakarta', 'IDR', 'id'),\n" +
                "  ('IE', 'Ireland', 'Éire', '+353', 'EU', 'Dublin', 'EUR', 'ga,en'),\n" +
                "  ('IL', 'Israel', 'יִשְׂרָאֵל', '+972', 'AS', 'Jerusalem', 'ILS', 'he,ar'),\n" +
                "  ('IM', 'Isle of Man', 'Isle of Man', '+44', 'EU', 'Douglas', 'GBP', 'en,gv'),\n" +
                "  ('IN', 'India', 'भारत', '+91', 'AS', 'New Delhi', 'INR', 'hi,en'),\n" +
                "  ('IO', 'British Indian Ocean Territory', 'British Indian Ocean Territory', '+246', 'AS', 'Diego Garcia', 'USD', 'en'),\n" +
                "  ('IQ', 'Iraq', 'العراق', '+964', 'AS', 'Baghdad', 'IQD', 'ar,ku'),\n" +
                "  ('IR', 'Iran', 'ایران', '+98', 'AS', 'Tehran', 'IRR', 'fa'),\n" +
                "  ('IS', 'Iceland', 'Ísland', '+354', 'EU', 'Reykjavik', 'ISK', 'is'),\n" +
                "  ('IT', 'Italy', 'Italia', '+39', 'EU', 'Rome', 'EUR', 'it'),\n" +
                "  ('JE', 'Jersey', 'Jersey', '+44', 'EU', 'Saint Helier', 'GBP', 'en,fr'),\n" +
                "  ('JM', 'Jamaica', 'Jamaica', '+1876', 'NA', 'Kingston', 'JMD', 'en'),\n" +
                "  ('JO', 'Jordan', 'الأردن', '+962', 'AS', 'Amman', 'JOD', 'ar'),\n" +
                "  ('JP', 'Japan', '日本', '+81', 'AS', 'Tokyo', 'JPY', 'ja'),\n" +
                "  ('KE', 'Kenya', 'Kenya', '+254', 'AF', 'Nairobi', 'KES', 'en,sw'),\n" +
                "  ('KG', 'Kyrgyzstan', 'Кыргызстан', '+996', 'AS', 'Bishkek', 'KGS', 'ky,ru'),\n" +
                "  ('KH', 'Cambodia', 'Kâmpŭchéa', '+855', 'AS', 'Phnom Penh', 'KHR', 'km'),\n" +
                "  ('KI', 'Kiribati', 'Kiribati', '+686', 'OC', 'South Tarawa', 'AUD', 'en'),\n" +
                "  ('KM', 'Comoros', 'Komori', '+269', 'AF', 'Moroni', 'KMF', 'ar,fr'),\n" +
                "  ('KN', 'Saint Kitts and Nevis', 'Saint Kitts and Nevis', '+1869', 'NA', 'Basseterre', 'XCD', 'en'),\n" +
                "  ('KR', 'South Korea', '대한민국', '+82', 'AS', 'Seoul', 'KRW', 'ko'),\n" +
                "  ('KW', 'Kuwait', 'الكويت', '+965', 'AS', 'Kuwait City', 'KWD', 'ar'),\n" +
                "  ('KY', 'Cayman Islands', 'Cayman Islands', '+1345', 'NA', 'George Town', 'KYD', 'en'),\n" +
                "  ('KZ', 'Kazakhstan', 'Қазақстан', '+7', 'AS', 'Astana', 'KZT', 'kk,ru'),\n" +
                "  ('LA', 'Laos', 'ສປປລາວ', '+856', 'AS', 'Vientiane', 'LAK', 'lo'),\n" +
                "  ('LB', 'Lebanon', 'لبنان', '+961', 'AS', 'Beirut', 'LBP', 'ar,fr'),\n" +
                "  ('LC', 'Saint Lucia', 'Saint Lucia', '+1758', 'NA', 'Castries', 'XCD', 'en'),\n" +
                "  ('LI', 'Liechtenstein', 'Liechtenstein', '+423', 'EU', 'Vaduz', 'CHF', 'de'),\n" +
                "  ('LK', 'Sri Lanka', 'śrī laṃkāva', '+94', 'AS', 'Colombo', 'LKR', 'si,ta'),\n" +
                "  ('LR', 'Liberia', 'Liberia', '+231', 'AF', 'Monrovia', 'LRD', 'en'),\n" +
                "  ('LS', 'Lesotho', 'Lesotho', '+266', 'AF', 'Maseru', 'LSL,ZAR', 'en,st'),\n" +
                "  ('LT', 'Lithuania', 'Lietuva', '+370', 'EU', 'Vilnius', 'LTL', 'lt'),\n" +
                "  ('LU', 'Luxembourg', 'Luxembourg', '+352', 'EU', 'Luxembourg', 'EUR', 'fr,de,lb'),\n" +
                "  ('LV', 'Latvia', 'Latvija', '+371', 'EU', 'Riga', 'EUR', 'lv'),\n" +
                "  ('LY', 'Libya', 'ليبيا', '+218', 'AF', 'Tripoli', '+LYD', 'ar'),\n" +
                "  ('MA', 'Morocco', 'المغرب', '+212', 'AF', 'Rabat', 'MAD', 'ar'),\n" +
                "  ('MC', 'Monaco', 'Monaco', '+377', 'EU', 'Monaco', 'EUR', 'fr'),\n" +
                "  ('MD', 'Moldova', 'Moldova', '+373', 'EU', 'Chișinău', 'MDL', 'ro'),\n" +
                "  ('ME', 'Montenegro', 'Црна Гора', '+382', 'EU', 'Podgorica', 'EUR', 'sr,bs,sq,hr'),\n" +
                "  ('MF', 'Saint Martin', 'Saint-Martin', '+590', 'NA', 'Marigot', 'EUR', 'en,fr,nl'),\n" +
                "  ('MG', 'Madagascar', 'Madagasikara', '+261', 'AF', 'Antananarivo', 'MGA', 'fr,mg'),\n" +
                "  ('MH', 'Marshall Islands', 'M̧ajeļ', '+692', 'OC', 'Majuro', 'USD', 'en,mh'),\n" +
                "  ('MK', 'Macedonia', 'Македонија', '+389', 'EU', 'Skopje', 'MKD', 'mk'),\n" +
                "  ('ML', 'Mali', 'Mali', '+223', 'AF', 'Bamako', 'XOF', 'fr'),\n" +
                "  ('MM', 'Myanmar [Burma]', 'Myanma', '+95', 'AS', 'Naypyidaw', 'MMK', 'my'),\n" +
                "  ('MN', 'Mongolia', 'Монгол улс', '+976', 'AS', 'Ulan Bator', 'MNT', 'mn'),\n" +
                "  ('MO', 'Macao', '澳門', '+853', 'AS', '', 'MOP', 'zh,pt'),\n" +
                "  ('MP', 'Northern Mariana Islands', 'Northern Mariana Islands', '+1670', 'OC', 'Saipan', 'USD', 'en,ch'),\n" +
                "  ('MQ', 'Martinique', 'Martinique', '+596', 'NA', 'Fort-de-France', 'EUR', 'fr'),\n" +
                "  ('MR', 'Mauritania', 'موريتانيا', '+222', 'AF', 'Nouakchott', 'MRO', 'ar'),\n" +
                "  ('MS', 'Montserrat', 'Montserrat', '+1664', 'NA', 'Plymouth', 'XCD', 'en'),\n" +
                "  ('MT', 'Malta', 'Malta', '+356', 'EU', 'Valletta', 'EUR', 'mt,en'),\n" +
                "  ('MU', 'Mauritius', 'Maurice', '+230', 'AF', 'Port Louis', 'MUR', 'en'),\n" +
                "  ('MV', 'Maldives', 'Maldives', '+960', 'AS', 'Malé', 'MVR', 'dv'),\n" +
                "  ('MW', 'Malawi', 'Malawi', '+265', 'AF', 'Lilongwe', 'MWK', 'en,ny'),\n" +
                "  ('MX', 'Mexico', 'México', '+52', 'NA', 'Mexico City', 'MXN', 'es'),\n" +
                "  ('MY', 'Malaysia', 'Malaysia', '+60', 'AS', 'Kuala Lumpur', 'MYR', ''),\n" +
                "  ('MZ', 'Mozambique', 'Moçambique', '+258', 'AF', 'Maputo', 'MZN', 'pt'),\n" +
                "  ('NA', 'Namibia', 'Namibia', '+264', 'AF', 'Windhoek', 'NAD,ZAR', 'en,af'),\n" +
                "  ('NC', 'New Caledonia', 'Nouvelle-Calédonie', '+687', 'OC', 'Nouméa', 'XPF', 'fr'),\n" +
                "  ('NE', 'Niger', 'Niger', '+227', 'AF', 'Niamey', 'XOF', 'fr'),\n" +
                "  ('NF', 'Norfolk Island', 'Norfolk Island', '+672', 'OC', 'Kingston', 'AUD', 'en'),\n" +
                "  ('NG', 'Nigeria', 'Nigeria', '+234', 'AF', 'Abuja', 'NGN', 'en'),\n" +
                "  ('NI', 'Nicaragua', 'Nicaragua', '+505', 'NA', 'Managua', 'NIO', 'es'),\n" +
                "  ('NL', 'Netherlands', 'Nederland', '+31', 'EU', 'Amsterdam', 'EUR', 'nl'),\n" +
                "  ('NO', 'Norway', 'Norge', '+47', 'EU', 'Oslo', 'NOK', 'no,nb,nn'),\n" +
                "  ('NP', 'Nepal', 'नपल', '+977', 'AS', 'Kathmandu', 'NPR', 'ne'),\n" +
                "  ('NR', 'Nauru', 'Nauru', '+674', 'OC', 'Yaren', 'AUD', 'en,na'),\n" +
                "  ('NU', 'Niue', 'Niuē', '+683', 'OC', 'Alofi', 'NZD', 'en'),\n" +
                "  ('NZ', 'New Zealand', 'New Zealand', '+64', 'OC', 'Wellington', 'NZD', 'en,mi'),\n" +
                "  ('OM', 'Oman', 'عمان', '+968', 'AS', 'Muscat', 'OMR', 'ar'),\n" +
                "  ('PA', 'Panama', 'Panamá', '+507', 'NA', 'Panama City', 'PAB,USD', 'es'),\n" +
                "  ('PE', 'Peru', 'Perú', '+51', 'SA', 'Lima', 'PEN', 'es'),\n" +
                "  ('PF', 'French Polynesia', 'Polynésie française', '+689', 'OC', 'Papeetē', 'XPF', 'fr'),\n" +
                "  ('PG', 'Papua New Guinea', 'Papua Niugini', '+675', 'OC', 'Port Moresby', 'PGK', 'en'),\n" +
                "  ('PH', 'Philippines', 'Pilipinas', '+63', 'AS', 'Manila', 'PHP', 'en'),\n" +
                "  ('PK', 'Pakistan', 'Pakistan', '+92', 'AS', 'Islamabad', 'PKR', 'en,ur'),\n" +
                "  ('PL', 'Poland', 'Polska', '+48', 'EU', 'Warsaw', 'PLN', 'pl'),\n" +
                "  ('PM', 'Saint Pierre and Miquelon', 'Saint-Pierre-et-Miquelon', '+508', 'NA', 'Saint-Pierre', 'EUR', 'fr'),\n" +
                "  ('PN', 'Pitcairn Islands', 'Pitcairn Islands', '+64', 'OC', 'Adamstown', 'NZD', 'en'),\n" +
                "  ('PR', 'Puerto Rico', 'Puerto Rico', '+1', 'NA', 'San Juan', 'USD', 'es,en'),\n" +
                "  ('PS', 'Palestine', 'فلسطين', '+970', 'AS', 'Ramallah', 'ILS', 'ar'),\n" +
                "  ('PT', 'Portugal', 'Portugal', '+351', 'EU', 'Lisbon', 'EUR', 'pt'),\n" +
                "  ('PW', 'Palau', 'Palau', '+680', 'OC', 'Ngerulmud', 'USD', 'en'),\n" +
                "  ('PY', 'Paraguay', 'Paraguay', '+595', 'SA', 'Asunción', 'PYG', 'es,gn'),\n" +
                "  ('QA', 'Qatar', 'قطر', '+974', 'AS', 'Doha', 'QAR', 'ar'),\n" +
                "  ('RE', 'Réunion', 'La Réunion', '+262', 'AF', 'Saint-Denis', 'EUR', 'fr'),\n" +
                "  ('RO', 'Romania', 'România', '+40', 'EU', 'Bucharest', 'RON', 'ro'),\n" +
                "  ('RS', 'Serbia', 'Србија', '+381', 'EU', 'Belgrade', 'RSD', 'sr'),\n" +
                "  ('RU', 'Russia', 'Россия', '+7', 'EU', 'Moscow', 'RUB', 'ru'),\n" +
                "  ('RW', 'Rwanda', 'Rwanda', '+250', 'AF', 'Kigali', 'RWF', 'rw,en,fr'),\n" +
                "  ('SA', 'Saudi Arabia', 'العربية السعودية', '+966', 'AS', 'Riyadh', 'SAR', 'ar'),\n" +
                "  ('SB', 'Solomon Islands', 'Solomon Islands', '+677', 'OC', 'Honiara', 'SBD', 'en'),\n" +
                "  ('SC', 'Seychelles', 'Seychelles', '+248', 'AF', 'Victoria', 'SCR', 'fr,en'),\n" +
                "  ('SD', 'Sudan', 'السودان', '+249', 'AF', 'Khartoum', 'SDG', 'ar,en'),\n" +
                "  ('SE', 'Sweden', 'Sverige', '+46', 'EU', 'Stockholm', 'SEK', 'sv'),\n" +
                "  ('SG', 'Singapore', 'Singapore', '+65', 'AS', 'Singapore', 'SGD', 'en,ms,ta,zh'),\n" +
                "  ('SH', 'Saint Helena', 'Saint Helena', '+290', 'AF', 'Jamestown', 'SHP', 'en'),\n" +
                "  ('SI', 'Slovenia', 'Slovenija', '+386', 'EU', 'Ljubljana', 'EUR', 'sl'),\n" +
                "  ('SJ', 'Svalbard and Jan Mayen', 'Svalbard og Jan Mayen', '+47', 'EU', 'Longyearbyen', 'NOK', 'no'),\n" +
                "  ('SK', 'Slovakia', 'Slovensko', '+421', 'EU', 'Bratislava', 'EUR', 'sk'),\n" +
                "  ('SL', 'Sierra Leone', 'Sierra Leone', '+232', 'AF', 'Freetown', 'SLL', 'en'),\n" +
                "  ('SM', 'San Marino', 'San Marino', '+378', 'EU', 'City of San Marino', 'EUR', 'it'),\n" +
                "  ('SN', 'Senegal', 'Sénégal', '+221', 'AF', 'Dakar', 'XOF', 'fr'),\n" +
                "  ('SO', 'Somalia', 'Soomaaliya', '+252', 'AF', 'Mogadishu', 'SOS', 'so,ar'),\n" +
                "  ('SR', 'Suriname', 'Suriname', '+597', 'SA', 'Paramaribo', 'SRD', 'nl'),\n" +
                "  ('SS', 'South Sudan', 'South Sudan', '+211', 'AF', 'Juba', 'SSP', 'en'),\n" +
                "  ('ST', 'São Tomé and Príncipe', 'São Tomé e Príncipe', '+239', 'AF', 'São Tomé', 'STD', 'pt'),\n" +
                "  ('SV', 'El Salvador', 'El Salvador', '+503', 'NA', 'San Salvador', 'SVC,USD', 'es'),\n" +
                "  ('SX', 'Sint Maarten', 'Sint Maarten', '+1721', 'NA', 'Philipsburg', 'ANG', 'nl,en'),\n" +
                "  ('SY', 'Syria', 'سوريا', '+963', 'AS', 'Damascus', 'SYP', 'ar'),\n" +
                "  ('SZ', 'Swaziland', 'Swaziland', '+268', 'AF', 'Lobamba', 'SZL', 'en,ss'),\n" +
                "  ('TC', 'Turks and Caicos Islands', 'Turks and Caicos Islands', '+1649', 'NA', 'Cockburn Town', 'USD', 'en'),\n" +
                "  ('TD', 'Chad', 'Tchad', '+235', 'AF', 'N''Djamena', 'XAF', 'fr,ar'),\n" +
                "  ('TF', 'French Southern Territories', 'Territoire des Terres australes et antarctiques françaises', '+262', 'AN', 'Port-aux-Français', 'EUR', 'fr'),\n" +
                "  ('TG', 'Togo', 'Togo', '+228', 'AF', 'Lomé', 'XOF', 'fr'),\n" +
                "  ('TH', 'Thailand', 'ประเทศไทย', '+66', 'AS', 'Bangkok', 'THB', 'th'),\n" +
                "  ('TJ', 'Tajikistan', 'Тоҷикистон', '+992', 'AS', 'Dushanbe', 'TJS', 'tg,ru'),\n" +
                "  ('TK', 'Tokelau', 'Tokelau', '+690', 'OC', 'Fakaofo', 'NZD', 'en'),\n" +
                "  ('TL', 'East Timor', 'Timor-Leste', '+670', 'OC', 'Dili', 'USD', 'pt'),\n" +
                "  ('TM', 'Turkmenistan', 'Türkmenistan', '+993', 'AS', 'Ashgabat', 'TMT', 'tk,ru'),\n" +
                "  ('TN', 'Tunisia', '+تونس', '216', 'AF', 'Tunis', 'TND', 'ar'),\n" +
                "  ('TO', 'Tonga', 'Tonga', '+676', 'OC', 'Nuku''alofa', 'TOP', 'en,to'),\n" +
                "  ('TR', 'Turkey', 'Türkiye', '+90', 'AS', 'Ankara', 'TRY', 'tr'),\n" +
                "  ('TT', 'Trinidad and Tobago', 'Trinidad and Tobago', '+1868', 'NA', 'Port of Spain', 'TTD', 'en'),\n" +
                "  ('TV', 'Tuvalu', 'Tuvalu', '+688', 'OC', 'Funafuti', 'AUD', 'en'),\n" +
                "  ('TW', 'Taiwan', '臺灣', '+886', 'AS', 'Taipei', 'TWD', 'zh'),\n" +
                "  ('TZ', 'Tanzania', 'Tanzania', '+255', 'AF', 'Dodoma', 'TZS', 'sw,en'),\n" +
                "  ('UA', 'Ukraine', 'Україна', '+380', 'EU', 'Kiev', 'UAH', 'uk'),\n" +
                "  ('UG', 'Uganda', 'Uganda', '+256', 'AF', 'Kampala', 'UGX', 'en,sw'),\n" +
                "  ('UM', 'U.S. Minor Outlying Islands', 'United States Minor Outlying Islands', '+699', 'OC', '', 'USD', 'en'),\n" +
                "  ('US', 'United States of America', 'United States of America', '+1', 'NA', 'Washington D.C.', 'USD,USN,USS', 'en'),\n" +
                "  ('UY', 'Uruguay', 'Uruguay', '+598', 'SA', 'Montevideo', 'UYI,UYU', 'es'),\n" +
                "  ('UZ', 'Uzbekistan', 'O‘zbekiston', '+998', 'AS', 'Tashkent', 'UZS', 'uz,ru'),\n" +
                "  ('VA', 'Vatican City', 'Vaticano', '+39066', 'EU', 'Vatican City', 'EUR', 'it,la'),\n" +
                "  ('VC', 'Saint Vincent and the Grenadines', 'Saint Vincent and the Grenadines', '+1784', 'NA', 'Kingstown', 'XCD', 'en'),\n" +
                "  ('VE', 'Venezuela', 'Venezuela', '+58', 'SA', 'Caracas', 'VEF', 'es'),\n" +
                "  ('VG', 'British Virgin Islands', 'British Virgin Islands', '+1284', 'NA', 'Road Town', 'USD', 'en'),\n" +
                "  ('VI', 'U.S. Virgin Islands', 'United States Virgin Islands', '+1340', 'NA', 'Charlotte Amalie', 'USD', 'en'),\n" +
                "  ('VN', 'Vietnam', 'Việt Nam', '+84', 'AS', 'Hanoi', 'VND', 'vi'),\n" +
                "  ('VU', 'Vanuatu', 'Vanuatu', '+678', 'OC', 'Port Vila', 'VUV', 'bi,en,fr'),\n" +
                "  ('WF', 'Wallis and Futuna', 'Wallis et Futuna', '+681', 'OC', 'Mata-Utu', 'XPF', 'fr'),\n" +
                "  ('WS', 'Samoa', 'Samoa', '+685', 'OC', 'Apia', 'WST', 'sm,en'),\n" +
                "  ('YE', 'Yemen', 'اليَمَن', '+967', 'AS', 'Sana''a', 'YER', 'ar'),\n" +
                "  ('YT', 'Reunion and Mayotte', 'Mayotte', '+262', 'AF', 'Mamoudzou', 'EUR', 'fr'),\n" +
                "  ('ZA', 'South Africa', 'South Africa', '+27', 'AF', 'Pretoria', 'ZAR', 'af,en,nr,st,ss,tn,ts,ve,xh,zu'),\n" +
                "  ('ZM', 'Zambia', 'Zambia', '+260', 'AF', 'Lusaka', 'ZMK', 'en'),\n" +
                "  ('ZW', 'Zimbabwe', 'Zimbabwe', '+263', 'AF', 'Harare', 'ZWL', 'en,sn,nd');";
        db.execSQL(query);
    }
}


