package com.incode_it.spychat.interfaces;

public interface OnFragmentsAuthorizationListener {

    void onLogIn();

    void onSignUp();

    void onAuthorizationSuccess(String accessToken, String refreshToken, String myPhoneNumber);

    void onError(String error);
}
