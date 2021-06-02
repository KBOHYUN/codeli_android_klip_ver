package com.example.codeli_klip;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class RoomPagerActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private int pos;

    private TextView room_name; //가게 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room2);

        ViewPager pager = findViewById(R.id.pager);
        //캐싱을 해놓을 프래그먼트 개수
        pager.setOffscreenPageLimit(2);

        TabLayout tabLayout=findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

        //방 정보 받기
        Intent data=getIntent();
        pos=data.getIntExtra("position",0);

        room_name=findViewById(R.id.room_name);
        room_name.setText(MainActivity.roomItemArrayList.get(pos).getName());


        //getSupportFragmentManager로 프래그먼트 참조가능
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());

        RoomInfoFragment fragment1 = new RoomInfoFragment();
        adapter.addItem(fragment1); //첫번째 프레그먼트 화면

        RoomChatFragment fragment2 = new RoomChatFragment(pos);
        adapter.addItem(fragment2); //두번째 프레그먼트 화면

        pager.setAdapter(adapter);

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.chat);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        finish();
                        return true;
                    case R.id.chat:
                        //방목록으로 이동하기
                        return true;
                    case R.id.my:
                        Intent my_intent=new Intent(getApplicationContext(), MypageActivity.class);
                        startActivity(my_intent);
                        finish();
                        //Toast.makeText(getApplicationContext(), "마이 페이지로 이동", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }

    //어댑터 안에서 각각의 아이템을 데이터로서 관리한다
    class PagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment> items = new ArrayList<Fragment>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addItem(Fragment item){
            items.add(item);
        }

        @Override
        public Fragment getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0 : return "정보";
                case 1 : return "채팅";
            }
            return (position+1)+"";
        }
    }
}