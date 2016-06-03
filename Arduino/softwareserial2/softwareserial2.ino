
#include <SoftwareSerial.h>
#define BT_SERIAL_TX 10
#define BT_SERIAL_RX 0
SoftwareSerial BluetoothSerial(BT_SERIAL_TX, BT_SERIAL_RX);

#include <dht11.h>
#define DHTPIN 2
dht11 dht;

#define TEMPTYPE 0

int ledpin=13;
char c;

void setup() {

  BluetoothSerial.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  BluetoothSerial.print("$$$");  // Enter command mode
  delay(100);  // Short delay, wait for the Mate to send back CMD
  BluetoothSerial.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  BluetoothSerial.begin(9600); 
  pinMode(ledpin, OUTPUT);
}

void loop() {

if(BluetoothSerial.available())
  {
     c = (char)BluetoothSerial.read();     
     if(c=='1')
     {
      digitalWrite(ledpin,HIGH);
      char printS = 'Y';
      BluetoothSerial.write(printS);
     }
     if(c=='0')
     {
      digitalWrite(ledpin,LOW);      
      
      char printS = 'N';
      BluetoothSerial.write(printS);
     }
     if(c=='t'){
       int readData = dht.read(DHTPIN);
       float t = dht.temperature; 
       float h = dht.humidity; 

       BluetoothSerial.print("Temperature: ");
       BluetoothSerial.print(t);
       BluetoothSerial.print(", Humidity: ");
       BluetoothSerial.print(h);
       BluetoothSerial.print("\n");      
     }
  }  
}
