package com.incode_it.spychat.interfaces;

public interface OnFragmentsAuthorizationListener {

    void onLogIn(String phone);

    void onSignUp(String phone);

    void onAuthorizationSuccess(String accessToken, String refreshToken, String myPhoneNumber);

    void onError(String error);
}
