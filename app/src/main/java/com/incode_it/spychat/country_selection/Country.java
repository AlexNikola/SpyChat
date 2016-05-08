package com.incode_it.spychat.country_selection;

public class Country
{
    public String nameNative;
    public String nameEnglish;
    public String codePhone;
    public String codeISO;

    public Country(String nameNative,String nameEnglish, String codePhone, String codeISO) {
        this.nameNative = nameNative;
        this.nameEnglish = nameEnglish;
        this.codePhone = codePhone;
        this.codeISO = codeISO;
    }
}
