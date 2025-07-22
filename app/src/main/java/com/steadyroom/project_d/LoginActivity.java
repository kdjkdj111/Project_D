package com.steadyroom.project_d;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.Credential;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 자동 로그인(세션 유지) 체크
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // 이미 로그인된 상태 → 바로 메인 화면으로 이동
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        SignInButton btnGoogleSignIn = findViewById(R.id.btn_GoogleSignIn);

        // CredentialManager 인스턴스 생성
        CredentialManager credentialManager = CredentialManager.create(this);

        // GoogleIdOption 생성 (Web client ID 필수)
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        // CredentialRequest 생성
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        btnGoogleSignIn.setOnClickListener(v -> {
            credentialManager.getCredentialAsync(
                    this,
                    request,
                    new CancellationSignal(),
                    getMainExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            Credential credential = result.getCredential();
                            if (credential instanceof CustomCredential) {
                                CustomCredential customCredential = (CustomCredential) credential;
                                if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                                    try {
                                        GoogleIdTokenCredential googleIdTokenCredential =
                                                GoogleIdTokenCredential.createFrom(customCredential.getData());
                                        String idToken = googleIdTokenCredential.getIdToken();
                                        if (idToken != null) {
                                            firebaseAuthWithGoogle(idToken);
                                        } else {
                                            // idToken이 null인 경우
                                            // 예: 에러 메시지 표시 등
                                        }
                                    } catch (Exception e) {
                                        // GoogleIdTokenCredential 파싱 오류 처리
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        @Override
                        public void onError(GetCredentialException e) {
                            // 로그인 실패 처리 (예: 토스트 메시지 등)
                            e.printStackTrace();
                        }
                    }
            );
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공 → 메인 화면 이동
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        // 로그인 실패 처리 (예: 토스트 메시지 등)
                    }
                });
    }
}
