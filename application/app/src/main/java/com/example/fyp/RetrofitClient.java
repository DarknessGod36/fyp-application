package com.example.fyp;
import androidx.appcompat.app.AppCompatDelegate;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://873a-27-125-250-241.ngrok-free.app/";
    //using 10.0.2.2:4444 for emulator as 127.0.0.1 for emulator already used as loopback interface
    //for exteral device 10.0.2.2:4444 and 127.0.0.1 is not workable, thus if using external device need to held an urls for it
    //using ngrok to held a website that not in local network other network also can connect to this local network but it also need my computer to on the terminal
    //and restart every 8 hours

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

