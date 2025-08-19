package com.steadyroom.project_d;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyCharacterManager {

    // 데이터 로딩 결과를 외부에 전달하기 위한 인터페이스
    public interface OnDataLoadListener {
        void onDataLoaded(List<CharacterInstance> characters);
        void onDataLoadFailed(String errorMessage);
    }

    private final OnDataLoadListener listener;
    private final DatabaseReference databaseReference;

    public MyCharacterManager(OnDataLoadListener listener) {
        this.listener = listener;

        //현재 로그인된 사용자의 UID 불러오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if( currentUser != null){
            String uid = currentUser.getUid();
            //users/{UID}/characters 경로의 데이터베이스
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid).child("characters");
            //Log.d("MyCharacterManager", "데이터베이스 참조 경로: " + databaseReference.toString());
        } else {
            // 사용자가 로그인되지 않은 경우
            databaseReference = null;
        }

    }

    public void loadMyCharacters() {
        if(databaseReference == null){
            listener.onDataLoadFailed("사용자가 로그인되어 있지 않아 캐릭터 정보를 불러올 수 없습니다.");
            return;
        }
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CharacterInstance> myCharacters = new ArrayList<>();
                for (DataSnapshot characterSnapshot : dataSnapshot.getChildren()) {
                    if (!characterSnapshot.exists()) continue; //  방어1
                    // 데이터베이스의 각 캐릭터 객체를 Character 클래스로 변환해서 리스트에 추가
                    CharacterInstance character = characterSnapshot.getValue(CharacterInstance.class);
                    if (character == null) continue; //  방어2

                    // DataSnapshot의 키(0, 1, 2...)를 가져와서 CharacterInstance에 저장
                    String firebaseKey = characterSnapshot.getKey();
                    character.setFirebaseKey(firebaseKey);
                    myCharacters.add(character);

                }
                // 로드된 데이터를 리스너를 통해 Activity로 전달
                listener.onDataLoaded(myCharacters);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터 로딩에 실패
                listener.onDataLoadFailed(databaseError.getMessage());
            }
        });
    }
}
