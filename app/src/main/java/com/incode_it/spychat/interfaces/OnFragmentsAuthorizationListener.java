package com.incode_it.spychat.interfaces;

public interface OnFragmentsAuthorizationListener {

    void onLogInSuccess(String accessToken, String refreshToken, String myPhoneNumber, String email);

    void onSignUpSuccess(String accessToken, String refreshToken, String myPhoneNumber, String email);

    void onError(String error);
}
