package com.example.countdowntimer;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText ayarlananaSureTxt;
    private TextView sayacTxt;
    private Button baslatDurdurButonu;
    private Button ayarlaButonu;
    private Button resetButonu;
    private CountDownTimer sayac;
    private long baslangicZamaniMilisaniye;
    private boolean zamanlayiciCalisiyorMu;
    private long kalanZamanMilisaniye;
    private long bitisZamani;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ayarlananaSureTxt = findViewById(R.id.edit_text_input);
        sayacTxt = findViewById(R.id.text_view_countdown);
        baslatDurdurButonu = findViewById(R.id.button_start_stop);
        resetButonu = findViewById(R.id.button_reset);
        ayarlaButonu = findViewById(R.id.button_set);

        ayarlaButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = ayarlananaSureTxt.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Lüften süre giriniz.", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(getApplicationContext(), "Pozitif sayı giriniz.", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                ayarlananaSureTxt.setText("");
            }
        });

        baslatDurdurButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (zamanlayiciCalisiyorMu) {
                    zamanlayiciDurdur();
                } else {
                    zamanlayiciBaslat();
                }
            }
        });

        resetButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zamanlayiciReset();
            }
        });
    }

    private void setTime(long milliseconds) {
        baslangicZamaniMilisaniye = milliseconds;
        zamanlayiciReset();
    }

    private void zamanlayiciBaslat() {
        bitisZamani = System.currentTimeMillis() + kalanZamanMilisaniye;
        sayac = new CountDownTimer(kalanZamanMilisaniye, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                kalanZamanMilisaniye = millisUntilFinished;
                sayaciGuncelle();
            }

            @Override
            public void onFinish() {
                zamanlayiciCalisiyorMu = false;
                gorunumuGuncelle();
            }
        }.start();
        zamanlayiciCalisiyorMu = true;
        gorunumuGuncelle();
    }

    private void zamanlayiciDurdur() {
        sayac.cancel();
        zamanlayiciCalisiyorMu = false;
        gorunumuGuncelle();
    }

    private void zamanlayiciReset() {
        kalanZamanMilisaniye = baslangicZamaniMilisaniye;
        sayaciGuncelle();
        gorunumuGuncelle();
    }

    private void sayaciGuncelle() {
        int saat = (int) (kalanZamanMilisaniye / 1000) / 3600;
        int dakika = (int) ((kalanZamanMilisaniye / 1000) % 3600) / 60;
        int saniye = (int) (kalanZamanMilisaniye / 1000) % 60;

        String kalanZaman;
        if (saat > 0)
            kalanZaman = String.format(Locale.getDefault(), "%d:%02d:%02d", saat, dakika, saniye);
        else
            kalanZaman = String.format(Locale.getDefault(), "%02d:%02d", dakika, saniye);

        sayacTxt.setText(kalanZaman);
    }

    private void gorunumuGuncelle() {
        if (zamanlayiciCalisiyorMu) {
            //EditText'i, ayarlama ve resetleme butonlarını görünmez yapıyoruz.
            ayarlananaSureTxt.setVisibility(View.INVISIBLE);
            ayarlaButonu.setVisibility(View.INVISIBLE);
            resetButonu.setVisibility(View.INVISIBLE);
            //BaşlatDurdurma butonuna Durdur yazıyoruz.
            baslatDurdurButonu.setText("Durdur");
        } else {
            //EditText'i, ayarlama ve başlatma butonlarını görünür yapıyoruz.
            ayarlananaSureTxt.setVisibility(View.VISIBLE);
            ayarlaButonu.setVisibility(View.VISIBLE);
            //BaşlatDurdurma butonuna Başlat yazıyoruz.
            baslatDurdurButonu.setText("Başlat");
            if (kalanZamanMilisaniye < 1000)
                baslatDurdurButonu.setVisibility(View.INVISIBLE);
            else
                baslatDurdurButonu.setVisibility(View.VISIBLE);

            if (kalanZamanMilisaniye < baslangicZamaniMilisaniye)
                resetButonu.setVisibility(View.VISIBLE);
            else
                resetButonu.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        kalanZamanMilisaniye = savedInstanceState.getLong("kalanZaman");
        zamanlayiciCalisiyorMu = savedInstanceState.getBoolean("zamanlayiciCalisiyorMu");
        sayaciGuncelle();
        gorunumuGuncelle();

        if (zamanlayiciCalisiyorMu) {
            bitisZamani = savedInstanceState.getLong("bitisZamani");
            kalanZamanMilisaniye = bitisZamani - System.currentTimeMillis();
            zamanlayiciBaslat();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("baslanginZamani", baslangicZamaniMilisaniye);
        editor.putLong("kalanZaman", kalanZamanMilisaniye);
        editor.putBoolean("zamanlayiciCalisiyorMu", zamanlayiciCalisiyorMu);
        editor.putLong("bitisZamani", bitisZamani);

        editor.apply();
        sayac.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        baslangicZamaniMilisaniye = prefs.getLong("baslanginZamani", 600000);
        kalanZamanMilisaniye = prefs.getLong("kalanZaman", baslangicZamaniMilisaniye);
        zamanlayiciCalisiyorMu = prefs.getBoolean("zamanlayiciCalisiyorMu", false);

        sayaciGuncelle();
        gorunumuGuncelle();

        if (zamanlayiciCalisiyorMu) {
            bitisZamani = prefs.getLong("bitisZamani", 0);
            kalanZamanMilisaniye = bitisZamani - System.currentTimeMillis();

            if (kalanZamanMilisaniye < 0) {
                kalanZamanMilisaniye = 0;
                zamanlayiciCalisiyorMu = false;
                sayaciGuncelle();
                gorunumuGuncelle();
            } else {
                zamanlayiciBaslat();
            }
        }
    }
}
