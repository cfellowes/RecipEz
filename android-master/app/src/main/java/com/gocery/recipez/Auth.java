package com.gocery.recipez;

import com.gocery.recipez.forms.LoginForm;
import com.gocery.recipez.forms.PasswordResetForm;
import com.gocery.recipez.forms.RegistrationForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import androidx.annotation.NonNull;

/**
 * The Auth class follows the Singleton pattern and acts as a Facade for the functionality of the
 * FirebaseAuth class.
 */
public class Auth {
    private static Auth auth;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static Auth getInstance() {
        if (auth == null) {
            auth = new Auth();
        }
        return auth;
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public void createUserAndLogin(final RegistrationForm form, final OnCompleteListener<AuthResult> onCompleteListener) {
        firebaseAuth.createUserWithEmailAndPassword(form.getEmail(), form.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            firebaseAuth.signInWithEmailAndPassword(form.getEmail(), form.getPassword())
                                    .addOnCompleteListener(onCompleteListener);
                        }
                        else {
                            onCompleteListener.onComplete(task);
                        }
                    }
                });
    }

    public void getIdToken(final OnCompleteListener<GetTokenResult> onCompleteListener) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.getIdToken(false).addOnCompleteListener(onCompleteListener);
        }
    }

    public String getUserId() {
        return firebaseAuth.getUid();
    }

    public void login(LoginForm form, OnCompleteListener<AuthResult> onCompleteListener) {
        firebaseAuth.signInWithEmailAndPassword(form.getEmail(), form.getPassword())
                .addOnCompleteListener(onCompleteListener);
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public void sendPasswordResetLink(PasswordResetForm form, OnCompleteListener<Void> onCompleteListener) {
        firebaseAuth.sendPasswordResetEmail(form.getEmail())
                .addOnCompleteListener(onCompleteListener);
    }
}
