package com.incode_it.spychat.interfaces;

public interface OnFragmentsAuthorizationListener {

    void onLogIn(String phone);

    void onHideKeyBoard();

    void onLogInSuccess(String accessToken, String refreshToken, String myPhoneNumber);

    void onSignUpSuccess(String accessToken, String refreshToken, String myPhoneNumber);

    void onError(String error);
}
