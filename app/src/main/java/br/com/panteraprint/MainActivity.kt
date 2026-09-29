package br.com.panteraprint
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
 private lateinit var adapter: BluetoothAdapter
 private lateinit var scanner: BluetoothLeScanner
 private lateinit var list: LinearLayout
 private lateinit var status: TextView
 private lateinit var report: TextView
 private val seen = mutableSetOf<String>()

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
  adapter=(getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
  list=findViewById(R.id.deviceList); status=findViewById(R.id.status); report=findViewById(R.id.report)
  findViewById<Button>(R.id.scanButton).setOnClickListener { ensurePermissionsAndScan() }
  findViewById<Button>(R.id.stopButton).setOnClickListener { stopScan() }
 }
 private fun ensurePermissionsAndScan() {
  val p=if(Build.VERSION.SDK_INT>=31) arrayOf(Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
  val m=p.filter{ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}
  if(m.isNotEmpty()) ActivityCompat.requestPermissions(this,m.toTypedArray(),7) else startScan()
 }
 override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){super.onRequestPermissionsResult(r,p,g);if(r==7&&g.all{it==PackageManager.PERMISSION_GRANTED})startScan()else status.text="Permissão Bluetooth necessária."}
 @SuppressLint("MissingPermission") private fun startScan(){
  if(!adapter.isEnabled){status.text="Ative o Bluetooth e tente novamente.";return}
  scanner=adapter.bluetoothLeScanner;seen.clear();list.removeAllViews();status.text="Procurando dispositivos BLE…";scanner.startScan(callback)
  Handler(Looper.getMainLooper()).postDelayed({stopScan()},12000)
 }
 @SuppressLint("MissingPermission") private fun stopScan(){if(::scanner.isInitialized)scanner.stopScan(callback);status.text=if(seen.isEmpty())"Busca encerrada. Nenhum BLE encontrado." else "Busca encerrada. Toque na possível impressora."}
 private val callback=object:ScanCallback(){
  @SuppressLint("MissingPermission") override fun onScanResult(t:Int,r:ScanResult){
   val d=r.device;if(!seen.add(d.address))return
   val n=d.name?:r.scanRecord?.deviceName?:"Dispositivo sem nome"
   val b=Button(this@MainActivity);b.text=n+"\n"+d.address+"   RSSI "+r.rssi;b.setOnClickListener{stopScan();connect(d,n)};list.addView(b)
  }
  override fun onScanFailed(e:Int){status.text="Falha no scanner BLE: "+e}
 }
 @SuppressLint("MissingPermission") private fun connect(d:BluetoothDevice,n:String){status.text="Conectando a "+n+"…";report.text="Dispositivo: "+n+"\nEndereço: "+d.address+"\n\n";d.connectGatt(this,false,gattCallback,BluetoothDevice.TRANSPORT_LE)}
 private val gattCallback=object:BluetoothGattCallback(){
  @SuppressLint("MissingPermission") override fun onConnectionStateChange(g:BluetoothGatt,s:Int,n:Int){runOnUiThread{status.text="GATT status="+s+" • estado="+n};if(n==BluetoothProfile.STATE_CONNECTED)g.discoverServices()}
  override fun onServicesDiscovered(g:BluetoothGatt,s:Int){
   val out=StringBuilder(report.text);out.append("\nServiços descobertos (status ").append(s).append("):\n")
   g.services.forEach{sv->out.append("\nSERVICE ").append(sv.uuid).append("\n");sv.characteristics.forEach{c->out.append("  CHAR ").append(c.uuid).append(" props=0x").append(c.properties.toString(16)).append("\n");c.descriptors.forEach{d->out.append("    DESC ").append(d.uuid).append("\n")}}}
   runOnUiThread{status.text="Diagnóstico concluído.";report.text=out.toString()}
  }
 }
}