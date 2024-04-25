package com.github.pwittchen.neurosky.app.rxjava;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.pwittchen.neurosky.library.RxNeuroSky;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  private final static String LOG_TAG = "NeuroSky";
  private RxNeuroSky neuroSky;
  private Disposable disposable;

  ArrayList<String> highbeta, lowgamma, highalpha, lowalpha, theta, lowbeta, midgamma, delta;

  public String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/capturedata";
  @BindView(R.id.tv_state) TextView tvState;
  @BindView(R.id.tv_attention) TextView tvAttention;
  @BindView(R.id.tv_meditation) TextView tvMeditation;
  @BindView(R.id.tv_blink) TextView tvBlink;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    neuroSky = new RxNeuroSky();
  }

  @Override protected void onResume() {
    super.onResume();
    disposable = neuroSky
        .stream()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(brainEvent -> {
          handleStateChange(brainEvent.state());
          handleSignalChange(brainEvent.signal());
          handleBrainWavesChange(brainEvent.brainWaves());
        });
  }

  @Override protected void onPause() {
    super.onPause();
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private void handleStateChange(final State state) {
    if (neuroSky != null && state.equals(State.CONNECTED)) {
      neuroSky.start();
    }

    tvState.setText(state.toString());
    Log.d(LOG_TAG, state.toString());
  }

  private void handleSignalChange(final Signal signal) {
    if(highbeta.size() == 10){
      for(int i = 0; i< highbeta.size(); i++){
        Log.d("prev high beta values", highbeta.get(i));
      }
    }
    switch (signal) {
      case ATTENTION:
        tvAttention.setText(getFormattedMessage("attention: %d", signal));
        break;
      case MEDITATION:
        tvMeditation.setText(getFormattedMessage("meditation: %d", signal));
        break;
      case BLINK:
        tvBlink.setText(getFormattedMessage("blink: %d", signal));
        break;

    }
    Log.d(LOG_TAG, String.format("%d", signal.toString(), signal.getValue()));
  }

  private void saveResult(){ //array list of signals
//    LocalDateTime now = LocalDateTime.now();
//    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//    String filename = dtf.format(now).toString();
//    File file = new File(path+"/"+dtf.format(now).toString());
//
//    FileOutputStream fout = null;
//    try {
//      fout = new FileOutputStream(file);
//
//    } catch (FileNotFoundException e) {
//      throw new RuntimeException(e);
//    }

  }

  private String getFormattedMessage(String messageFormat, Signal signal) {
    return String.format(Locale.getDefault(), messageFormat, signal.getValue());
  }

  private void handleBrainWavesChange(final Set<BrainWave> brainWaves) {

    if (brainWaves.isEmpty()) {
      return;
    }

    StringBuilder stringBuilder = new StringBuilder();

    for (BrainWave bw : brainWaves) {
      if(bw.toString().contains("HIGH_BETA") == true && highbeta.size() <= 150){
        highbeta.add(String.valueOf(bw.getValue()));
        Log.d(LOG_TAG, "done");
      }else if(bw.toString().equalsIgnoreCase("LOW_GAMMA") == true && lowgamma.size() <= 150){
        lowgamma.add(String.valueOf(bw.getValue()));
      }else if(bw.toString().equalsIgnoreCase("HIGH_ALPHA") == true && highalpha.size() <= 150){
        highalpha.add(String.valueOf(bw.getValue()));
      }else if(bw.toString().equalsIgnoreCase("LOW_ALPHA") == true && lowalpha.size() <= 150){
        lowalpha.add(String.valueOf(bw.getValue()));
      }else if(bw.toString().equalsIgnoreCase("LOW_BETA") == true && lowbeta.size() <= 150){
        lowbeta.add(String.valueOf(bw.getValue()));
      }else if(bw.toString().equalsIgnoreCase("MID_GAMMA") == true && midgamma.size() <= 150){
        midgamma.add(String.valueOf(bw.getValue()));
      }else if(bw.toString().equalsIgnoreCase("THETA") == true && theta.size() <= 150){
        theta.add(String.valueOf(bw.getValue()));
      }else if(bw.toString().equalsIgnoreCase("DELTA") == true && delta.size() <= 150){
        delta.add(String.valueOf(bw.getValue()));
      }
      String message = String.format(Locale.getDefault(), "%s: %d", bw.toString(), bw.getValue());
      stringBuilder.append(message).append(", ");
    }

    Log.d(LOG_TAG, stringBuilder.substring(0, stringBuilder.toString().length() - 2));
  }

  @SuppressLint("CheckResult") @OnClick(R.id.btn_connect) void connect() {
    neuroSky
        .connect()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            () -> showMessage("connecting..."),
            throwable -> {
              showMessage(throwable.getMessage());
              Log.d(LOG_TAG, throwable.getMessage());
            });
  }

  @SuppressLint("CheckResult") @OnClick(R.id.btn_disconnect) void disconnect() {
    neuroSky
        .disconnect()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            () -> showMessage("disconnected"),
            throwable -> showMessage(throwable.getMessage())
        );
  }

  @SuppressLint("CheckResult") @OnClick(R.id.btn_start_monitoring) void startMonitoring() {
    highalpha = new ArrayList<>();
    highbeta = new ArrayList<>();
    lowgamma = new ArrayList<>();
    lowalpha = new ArrayList<>();
    lowbeta = new ArrayList<>();
    theta =  new ArrayList<>();
    midgamma = new ArrayList<>();
    delta = new ArrayList<>();

    neuroSky
        .start()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            () -> showMessage("started monitoring..."),
            throwable -> showMessage(throwable.getMessage())
        );
  }

  @SuppressLint("CheckResult") @OnClick(R.id.btn_stop_monitoring) void stopMonitoring() {
    neuroSky
        .stop()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            () -> showMessage("stopped monitoring..."),
            throwable -> showMessage(throwable.getMessage())
        );
  }

  private void showMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
