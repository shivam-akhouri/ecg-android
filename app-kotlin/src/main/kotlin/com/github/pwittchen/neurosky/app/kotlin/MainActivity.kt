package com.github.pwittchen.neurosky.app.kotlin

import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.pwittchen.neurosky.library.NeuroSky
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener
import com.github.pwittchen.neurosky.library.message.enums.BrainWave
import com.github.pwittchen.neurosky.library.message.enums.Signal
import com.github.pwittchen.neurosky.library.message.enums.State
import kotlinx.android.synthetic.main.activity_main.btn_connect
import kotlinx.android.synthetic.main.activity_main.btn_disconnect
import kotlinx.android.synthetic.main.activity_main.btn_start_monitoring
import kotlinx.android.synthetic.main.activity_main.btn_stop_monitoring
import kotlinx.android.synthetic.main.activity_main.thresholdInput
import kotlinx.android.synthetic.main.activity_main.tv_attention
import kotlinx.android.synthetic.main.activity_main.tv_blink
import kotlinx.android.synthetic.main.activity_main.tv_meditation
import kotlinx.android.synthetic.main.activity_main.tv_state
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale


class MainActivity : AppCompatActivity() {

  companion object {
    const val LOG_TAG = "wave"
  }
  private class NetworkRequestTask(private val listener: NetworkRequestListener) :
          AsyncTask<String, Void, String>() {

    interface NetworkRequestListener {
      fun onNetworkRequestComplete(result: String)
    }

    override fun doInBackground(vararg params: String): String {
      val urlString = params[0]
      var result = ""
      try {
        val url = URL(urlString)
        val urlConnection = url.openConnection() as HttpURLConnection
        Log.e("lol", urlConnection.responseCode.toString())
      } catch (e: IOException) {
        Log.e("lol", "Error in network request", e)
      }
      return result
    }

    override fun onPostExecute(result: String) {
      super.onPostExecute(result)
      listener.onNetworkRequestComplete(result)
    }
  }
  var baseUrl = "https://ecg-3.onrender.com/"



//  val connection = url.openConnection() as HttpURLConnection
//  val responseCode = connection.responseCode
  private lateinit var neuroSky: NeuroSky
  private lateinit var low_alpha : ArrayList<Int>
  private lateinit var delta : ArrayList<Int>
  private lateinit var theta : ArrayList<Int>
  private lateinit var high_alpha : ArrayList<Int>
  private lateinit var low_beta : ArrayList<Int>
  private lateinit var high_beta : ArrayList<Int>
  private lateinit var low_gamma : ArrayList<Int>
  private lateinit var mid_gamma : ArrayList<Int>
  private var threshold = 10
  private lateinit var path : String

  private lateinit var thetaChart : LineChart
  private lateinit var deltaChart : LineChart
  private lateinit var lowAlphaChart : LineChart
  private lateinit var highAlphaChart : LineChart
  private lateinit var lowBetaChart : LineChart
  private lateinit var highBetaChart : LineChart
  private lateinit var lowGammaChart : LineChart
  private lateinit var midGammaChart : LineChart

  private lateinit var thetaDeque : ArrayDeque<Int>
  private lateinit var deltaDeque : ArrayDeque<Int>
  private lateinit var lowAlphaDeque : ArrayDeque<Int>
  private lateinit var highAlphaDeque : ArrayDeque<Int>
  private lateinit var lowBetaDeque : ArrayDeque<Int>
  private lateinit var highBetaDeque : ArrayDeque<Int>
  private lateinit var lowGammaDeque : ArrayDeque<Int>
  private lateinit var midGammaDeque : ArrayDeque<Int>
  private var flag = false;
  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    neuroSky = createNeuroSky()
    low_alpha = ArrayList()
    delta = ArrayList()
    theta = ArrayList()
    high_alpha = ArrayList()
    low_beta = ArrayList()
    high_beta = ArrayList()
    low_gamma = ArrayList()
    mid_gamma = ArrayList()

    thetaDeque = ArrayDeque()
    deltaDeque = ArrayDeque()
    lowAlphaDeque = ArrayDeque()
    highAlphaDeque = ArrayDeque()
    lowBetaDeque = ArrayDeque()
    highBetaDeque = ArrayDeque()
    lowGammaDeque = ArrayDeque()
    midGammaDeque = ArrayDeque()
    for(i in 1..19){
      thetaDeque.add(0)
      deltaDeque.add(0)
      lowAlphaDeque.add(0)
      highAlphaDeque.add(0)
      lowBetaDeque.add(0)
      highBetaDeque.add(0)
      lowGammaDeque.add(0)
      midGammaDeque.add(0)
    }

    path = Environment.getExternalStorageDirectory().absolutePath + "/capturedata"

    thetaChart = findViewById(R.id.theta_chart)
    deltaChart = findViewById(R.id.delta_chart)
    lowAlphaChart = findViewById(R.id.lowalpha_chart)
    highAlphaChart = findViewById(R.id.highalpha_chart)
    lowBetaChart = findViewById(R.id.lowbeta_chart)
    highBetaChart = findViewById(R.id.highbeta_chart)
    lowGammaChart = findViewById(R.id.lowgamma_chart)
    midGammaChart = findViewById(R.id.midgamma_chart)

    thetaChart.setDrawGridBackground(false);
    thetaChart.getDescription().setEnabled(false);
    thetaChart.setTouchEnabled(true);
    thetaChart.setDragEnabled(true);
    thetaChart.setScaleEnabled(true);
    thetaChart.setPinchZoom(true);
    thetaChart.setDrawMarkers(true);

    initButtonListeners()

    var thetaDataset : LineDataSet = LineDataSet(createDataVals(thetaDeque), "Theta wave data")
    thetaDataset.setCircleColor(Color.rgb(130, 29, 207))
    thetaDataset.setValueTextColor(Color.rgb(130, 29, 207))
    thetaDataset.setColor(Color.rgb(130, 29, 207))
    var lowAlhpaDataset : LineDataSet = LineDataSet(createDataVals(lowAlphaDeque), "Low Alpha wave data")
    lowAlhpaDataset.setCircleColor(Color.rgb(219, 64, 94))
    lowAlhpaDataset.setValueTextColor(Color.rgb(219, 64, 94))
    lowAlhpaDataset.setColor(Color.rgb(219, 64, 94))
    var highAlphaDataset : LineDataSet = LineDataSet(createDataVals(highAlphaDeque), "High Alpha wave data")
    highAlphaDataset.setCircleColor(Color.rgb(176, 4, 37))
    highAlphaDataset.setValueTextColor(Color.rgb(176, 4, 37))
    highAlphaDataset.setColor(Color.rgb(176, 4, 37))
    var lowBetaDataset : LineDataSet = LineDataSet(createDataVals(lowBetaDeque), "Low Beta wave data")
    lowBetaDataset.setCircleColor(Color.rgb(95, 113, 227))
    lowBetaDataset.setValueTextColor(Color.rgb(95, 113, 227))
    lowBetaDataset.setColor(Color.rgb(95, 113, 227))
    var highBetaDataset : LineDataSet = LineDataSet(createDataVals(highBetaDeque), "High Beta wave data")
    highBetaDataset.setCircleColor(Color.rgb(4, 32, 207))
    highBetaDataset.setValueTextColor(Color.rgb(4, 32, 207))
    highBetaDataset.setColor(Color.rgb(4, 32, 207))
    var lowGammaDataset : LineDataSet = LineDataSet(createDataVals(lowGammaDeque), "Low Gamma wave data")
    lowGammaDataset.setCircleColor(Color.rgb(57, 204, 165))
    lowGammaDataset.setValueTextColor(Color.rgb(57, 204, 165))
    lowGammaDataset.setColor(Color.rgb(57, 204, 165))
    var midGammaDataset : LineDataSet = LineDataSet(createDataVals(midGammaDeque), "Mid Gamma wave data")
    midGammaDataset.setCircleColor(Color.rgb(12, 150, 100))
    midGammaDataset.setValueTextColor(Color.rgb(12, 150, 100))
    midGammaDataset.setColor(Color.rgb(12, 150, 100))
    var deltaDataset : LineDataSet = LineDataSet(createDataVals(deltaDeque), "Delta wave data")
    deltaDataset.setCircleColor(Color.rgb(191, 138, 15))
    deltaDataset.setValueTextColor(Color.rgb(191, 138, 15))
    deltaDataset.setColor(Color.rgb(191, 138, 15))


    var thetaIDataset : ArrayList<ILineDataSet> = ArrayList()
    var deltaIDataSet : ArrayList<ILineDataSet> = ArrayList()
    var lowAlphaIDataSet : ArrayList<ILineDataSet> = ArrayList()
    var highAlhpaIDataSet : ArrayList<ILineDataSet> = ArrayList()
    var lowBetaIDataSet : ArrayList<ILineDataSet> = ArrayList()
    var highBetaIDataSet : ArrayList<ILineDataSet> = ArrayList()
    var lowGammaIDataSet : ArrayList<ILineDataSet> = ArrayList()
    var midGammaIDataSet : ArrayList<ILineDataSet> = ArrayList()

    thetaIDataset.add(thetaDataset)
    deltaIDataSet.add(deltaDataset)
    lowAlphaIDataSet.add(lowAlhpaDataset)
    highAlhpaIDataSet.add(highAlphaDataset)
    lowBetaIDataSet.add(lowBetaDataset)
    highBetaIDataSet.add(highBetaDataset)
    lowGammaIDataSet.add(lowGammaDataset)
    midGammaIDataSet.add(midGammaDataset)


    var thetaDat : LineData = LineData(thetaIDataset)
    var deltaDat : LineData = LineData(deltaIDataSet)
    var lowAlphaDat : LineData = LineData(lowAlphaIDataSet)
    var highAlphaDat : LineData = LineData(highAlhpaIDataSet)
    var lowBetaDat : LineData = LineData(lowBetaIDataSet)
    var highBetaDat : LineData = LineData(highBetaIDataSet)
    var lowGammaDat : LineData = LineData(lowGammaIDataSet)
    var midGammaDat : LineData = LineData(midGammaIDataSet)
    thetaChart.data = thetaDat
    deltaChart.data = deltaDat
    lowAlphaChart.data = lowAlphaDat
    highAlphaChart.data = highAlphaDat
    lowBetaChart.data = lowBetaDat
    highBetaChart.data = highBetaDat
    lowGammaChart.data = lowGammaDat
    midGammaChart.data = midGammaDat
  }

//  Test function for chart
  private fun createDataVals(data : ArrayDeque<Int>) : ArrayList<Entry> {
    var dataVals : ArrayList<Entry> = ArrayList()

    data.forEachIndexed{index,value->
      run {
        Log.d("deque", value.toString())
        dataVals.add(
          Entry(
            index.toFloat(), value.toFloat()
          )
        )
      }
    }
    return dataVals
  }
  private fun initButtonListeners() {
    btn_connect.setOnClickListener {
      try {
        neuroSky.connect()
      } catch (e: BluetoothNotEnabledException) {
        Toast.makeText(this, e.message, Toast.LENGTH_SHORT)
            .show()
        Log.d(LOG_TAG, e.message)
      }
    }

    btn_disconnect.setOnClickListener {
      neuroSky.disconnect()
    }

    btn_start_monitoring.setOnClickListener {
      neuroSky.start()
    }

    btn_stop_monitoring.setOnClickListener {
      neuroSky.stop()
    }
  }

  override fun onResume() {
    super.onResume()
    if (neuroSky.isConnected) {
      neuroSky.start()
    }
  }

  override fun onPause() {
    super.onPause()
    if (neuroSky.isConnected) {
      neuroSky.stop()
    }
  }

  private fun createNeuroSky(): NeuroSky {
    return NeuroSky(object : ExtendedDeviceMessageListener() {
      override fun onStateChange(state: State) {
        handleStateChange(state)
      }

      override fun onSignalChange(signal: Signal) {
        handleSignalChange(signal)
      }

      override fun onBrainWavesChange(brainWaves: Set<BrainWave>) {
        handleBrainWavesChange(brainWaves)
      }
    })
  }

  private fun handleStateChange(state: State) {
    try{
      threshold = thresholdInput.text.toString().toInt()
      if (state == State.CONNECTED) {
        neuroSky.start()
      }
      tv_state.text = state.toString()
    }catch (e :Exception){
      Toast.makeText(this, "Please Enter no of datapoints to capture in numeric", Toast.LENGTH_SHORT)
    }

    Log.d("fileout", threshold.toString())
    Log.d(LOG_TAG, state.toString())
  }

  private fun handleSignalChange(signal: Signal) {
    when (signal) {
      Signal.ATTENTION -> tv_attention.text = getFormattedMessage("attention: %d", signal)
      Signal.MEDITATION -> tv_meditation.text = getFormattedMessage("meditation: %d", signal)
      Signal.BLINK -> tv_blink.text = getFormattedMessage("blink: %d", signal)
      else -> Log.d(LOG_TAG, "unhandled signal")
    }

    Log.d(LOG_TAG, String.format("%s: %d", signal.toString(), signal.value))
  }

  private fun getFormattedMessage(
    messageFormat: String,
    signal: Signal
  ): String {
    return String.format(Locale.getDefault(), messageFormat, signal.value)
  }

  private fun writeExternalStorage(){
    var file : File = File(path, "test1.csv")
    file.parentFile.mkdirs()
    file.createNewFile()
    file.writeText("delta,theta,low_alpha,high_alpha,low_beta,high_beta,low_gamma,mid_gamma\n")
    for (i in 0..threshold-1){
//      var fullUrl = baseUrl +"?"+"lowalpha=10"+"&highalpha=20"+"&lowbeta=23"+"&highbeta=43"+"&lowgamma=12"+
//              "&midgamma=18"+"&theta=43"+"&delta=24";

      var fullUrl = baseUrl +"?"+"lowalpha="+low_alpha.get(0).toString()+"&highalpha="+high_alpha.get(0).toString()+
          "&lowbeta="+low_beta.get(0).toString()+"&highbeta="+high_beta.get(0).toString()+"&lowgamma="+
          low_gamma.get(0).toString()+"&midgamma="+mid_gamma.get(0).toString()+"&theta="+theta.get(0).toString()+
          "&delta="+delta.get(0).toString();
      Log.d("lol", fullUrl)
      val networkRequestTask = NetworkRequestTask(object : NetworkRequestTask.NetworkRequestListener {
        override fun onNetworkRequestComplete(result: String) {
          Log.d("LOL", "Network request result: $result")
          // Handle the result here
        }
      })
      networkRequestTask.execute(fullUrl)
      file.appendText(delta.get(i).toString()+","+theta.get(i).toString()+","+low_alpha.get(i).toString()+","
      +high_alpha.get(i).toString()+","+low_beta.get(i).toString()+","+high_beta.get(i).toString()+","
      +low_gamma.get(i).toString()+","+mid_gamma.get(i).toString()+"\n")
    }
  }

  private fun handleBrainWavesChange(brainWaves: Set<BrainWave>) {

    for (brainWave in brainWaves) {
//      Log.d("waves", "BrainwaveIndex: ${brainWave.type}, Brainwave Value: ${brainWave.value}")
      if(brainWave.type ==1 && delta.size < threshold){
        delta.add(brainWave.value)
      }
      else if(brainWave.type == 2 && theta.size < threshold){
        theta.add(brainWave.value)
      }else if(brainWave.type == 3 && low_alpha.size < threshold){
        low_alpha.add(brainWave.value)
      }else if(brainWave.type == 4 && high_alpha.size < threshold){
        high_alpha.add(brainWave.value)
      }else if(brainWave.type == 5 && low_beta.size < threshold){
        low_beta.add(brainWave.value)
      }else if(brainWave.type == 6 && high_beta.size < threshold){
        high_beta.add(brainWave.value)
      }else if(brainWave.type == 7 && low_gamma.size < threshold){
        low_gamma.add(brainWave.value)
      }else if(brainWave.type == 8 && mid_gamma.size < threshold){
        mid_gamma.add(brainWave.value)
      }

      Log.d("filesize", delta.size.toString())
      if(!flag && delta.size ==threshold && theta.size == threshold && low_alpha.size==threshold && low_beta.size == threshold && low_gamma.size == threshold && mid_gamma.size == threshold && high_alpha.size == threshold && high_beta.size == threshold){
        Log.d("fileout", "Done File output")
        writeExternalStorage()
        tv_blink.text = "CSV Upload Successful"
        Toast.makeText(this, "CSV file uploaded successfully!", Toast.LENGTH_LONG)
        flag = true
      }
      if(brainWave.type == 1){
        deltaDeque.add( if (brainWave.value > 50000) 50000 else brainWave.value)
        deltaDeque.removeFirst()
        val deltaDataset : LineDataSet = LineDataSet(createDataVals(deltaDeque), "Delta")
        deltaDataset.setCircleColor(Color.rgb(130, 29, 207))
        deltaDataset.setValueTextColor(Color.rgb(130, 29, 207))
        deltaDataset.setColor(Color.rgb(130, 29, 207))
        var deltaIDataset : ArrayList<ILineDataSet> = ArrayList()
        deltaIDataset.add(deltaDataset)
        var thetaDat : LineData = LineData(deltaIDataset)
        deltaChart.data = thetaDat
        deltaChart.invalidate()
        deltaChart.setVisibleYRange(0f,70000f, YAxis.AxisDependency.LEFT)
      }else if(brainWave.type == 2){
        thetaDeque.add( if (brainWave.value > 50000) 50000 else brainWave.value )
        thetaDeque.removeFirst()
        val thetaDataset : LineDataSet = LineDataSet(createDataVals(thetaDeque), "Theta")
        thetaDataset.setCircleColor(Color.rgb(130, 29, 207))
        thetaDataset.setValueTextColor(Color.rgb(130, 29, 207))
        thetaDataset.setColor(Color.rgb(130, 29, 207))
        var thetaIDataset : ArrayList<ILineDataSet> = ArrayList()
        thetaIDataset.add(thetaDataset)
        var thetaDat : LineData = LineData(thetaIDataset)
        thetaChart.data = thetaDat
        thetaChart.invalidate()
        thetaChart.setVisibleYRange(0f,70000f, YAxis.AxisDependency.LEFT)
      }else if(brainWave.type == 3){
        lowAlphaDeque.add( if (brainWave.value > 350000) 35000 else brainWave.value)
        lowAlphaDeque.removeFirst()
        val lowAlphaDataSet : LineDataSet = LineDataSet(createDataVals(lowAlphaDeque), "Low Alpha")
        lowAlphaDataSet.setCircleColor(Color.rgb(219, 64, 94))
        lowAlphaDataSet.setValueTextColor(Color.rgb(219, 64, 94))
        lowAlphaDataSet.setColor(Color.rgb(219, 64, 94))
        var lowAlphaIDataSet : ArrayList<ILineDataSet> = ArrayList()
        lowAlphaIDataSet.add(lowAlphaDataSet)
        var lowAlhpaDat : LineData = LineData(lowAlphaIDataSet)
        lowAlphaChart.data = lowAlhpaDat
        lowAlphaChart.invalidate()
        lowAlphaChart.setVisibleYRange(0f,50000f, YAxis.AxisDependency.LEFT)
      }else if(brainWave.type == 4){
        highAlphaDeque.add( if (brainWave.value > 20000) 20000 else brainWave.value)
        highAlphaDeque.removeFirst()
        val highAlphaDataSet : LineDataSet = LineDataSet(createDataVals(highAlphaDeque), "High Alpha")
        highAlphaDataSet.setCircleColor(Color.rgb(176, 4, 37))
        highAlphaDataSet.setValueTextColor(Color.rgb(176, 4, 37))
        highAlphaDataSet.setColor(Color.rgb(176, 4, 37))
        var highAlphaIDataSet : ArrayList<ILineDataSet> = ArrayList()
        highAlphaIDataSet.add(highAlphaDataSet)
        var lowAlhpaDat : LineData = LineData(highAlphaIDataSet)
        highAlphaChart.data = lowAlhpaDat
        highAlphaChart.invalidate()
        highAlphaChart.setVisibleYRange(0f,20000f, YAxis.AxisDependency.LEFT)
      }else if(brainWave.type == 5){
        lowBetaDeque.add( if (brainWave.value > 20000) 20000 else brainWave.value)
        lowBetaDeque.removeFirst()
        val lowBetaDataSet : LineDataSet = LineDataSet(createDataVals(lowBetaDeque), "Low Beta")
        lowBetaDataSet.setCircleColor(Color.rgb(95, 113, 227))
        lowBetaDataSet.setValueTextColor(Color.rgb(95, 113, 227))
        lowBetaDataSet.setColor(Color.rgb(95, 113, 227))
        var lowBetaIDataSet : ArrayList<ILineDataSet> = ArrayList()
        lowBetaIDataSet.add(lowBetaDataSet)
        var lowBetaDat : LineData = LineData(lowBetaIDataSet)
        lowBetaChart.data = lowBetaDat
        lowBetaChart.invalidate()
        lowBetaChart.setVisibleYRange(0f,20000f, YAxis.AxisDependency.LEFT)
      }else if(brainWave.type == 6){
        highBetaDeque.add( if (brainWave.value > 20000) 20000 else brainWave.value)
        highBetaDeque.removeFirst()
        val highBetaDataSet : LineDataSet = LineDataSet(createDataVals(highBetaDeque), "High Beta")
        highBetaDataSet.setCircleColor(Color.rgb(4, 32, 207))
        highBetaDataSet.setValueTextColor(Color.rgb(4, 32, 207))
        highBetaDataSet.setColor(Color.rgb(4, 32, 207))
        var highBetaIDataSet : ArrayList<ILineDataSet> = ArrayList()
        highBetaIDataSet.add(highBetaDataSet)
        var lowBetaDat : LineData = LineData(highBetaIDataSet)
        highBetaChart.data = lowBetaDat
        highBetaChart.invalidate()
        highBetaChart.setVisibleYRange(0f,20000f, YAxis.AxisDependency.LEFT)
      }else if(brainWave.type == 7){
        lowGammaDeque.add( if (brainWave.value > 20000) 20000 else brainWave.value)
        lowGammaDeque.removeFirst()
        val lowGammaDataSet : LineDataSet = LineDataSet(createDataVals(lowGammaDeque), "Low Gamma")
        lowGammaDataSet.setCircleColor(Color.rgb(57, 204, 165))
        lowGammaDataSet.setValueTextColor(Color.rgb(57, 204, 165))
        lowGammaDataSet.setColor(Color.rgb(57, 204, 165))
        var lowGammaIDataSet : ArrayList<ILineDataSet> = ArrayList()
        lowGammaIDataSet.add(lowGammaDataSet)
        var lowGammaDat : LineData = LineData(lowGammaIDataSet)
        lowGammaChart.data = lowGammaDat
        lowGammaChart.invalidate()
        lowGammaChart.setVisibleYRange(0f,20000f, YAxis.AxisDependency.LEFT)
      }else if(brainWave.type == 8){
        midGammaDeque.add( if (brainWave.value > 8000) 8000 else brainWave.value)
        midGammaDeque.removeFirst()
        val midGammaDataSet : LineDataSet = LineDataSet(createDataVals(midGammaDeque), "Mid Gamma")
        midGammaDataSet.setCircleColor(Color.rgb(12, 150, 100))
        midGammaDataSet.setValueTextColor(Color.rgb(12, 150, 100))
        midGammaDataSet.setColor(Color.rgb(12, 150, 100))
        var midGammaIDataSet : ArrayList<ILineDataSet> = ArrayList()
        midGammaIDataSet.add(midGammaDataSet)
        var midGammaDat : LineData = LineData(midGammaIDataSet)
        midGammaChart.data = midGammaDat
        midGammaChart.invalidate()
        midGammaChart.setVisibleYRange(0f,10000f, YAxis.AxisDependency.LEFT)
      }
      Log.d(LOG_TAG, String.format("%s: %d", brainWave.toString(), brainWave.value))
    }

  }
}



