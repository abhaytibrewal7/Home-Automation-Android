
//#include <SoftwareSerial.h>

#include <dht11.h>
#define DHTPIN 2
//#define rxPin 5
//#define txPin 6
dht11 dht;
int ledpin=13;
char c;
//SoftwareSerial bc(rxPin,txPin);


void setup() {
  Serial.begin(9600);
  //bc.begin(9600);
  pinMode(ledpin, OUTPUT);
  //pinMode(txPin,OUTPUT);
  //pinMode(rxPin,INPUT);
}

void loop()
{
  /*char recvChar;

  if (bc.available()) {//check if there's any data sent from the remote bluetooth shield
  recvChar = bc.read();
  Serial.print(recvChar);*/
  //Serial.print("abhay");
  if(Serial.available())
  {
     c = Serial.read();     
     if(c=='1')
     {
      digitalWrite(ledpin,HIGH);
      //Serial.print("LED IS ON");
     }
     if(c=='0')
     {
      digitalWrite(ledpin,LOW);
      //Serial.print("LED IS OFF");
     }
     if(c=='t'){

       int readData = dht.read(DHTPIN); // Reads the data from the sensor
       float t = dht.temperature; // Gets the values of the temperature
       float h = dht.humidity; // Gets the values of the humidity
  
       // Printing the results on the serial monitor       
       Serial.print("Temperature = ");       
       Serial.print(t);
       Serial.print(" *C ");
       Serial.print("    Humidity = ");
       Serial.print(h);
       Serial.print(" % ");       
       Serial.println();
       //bc.print(recvChar);
       //bc.print(t);
  
       //delay(10000); // Delays 2 secods, as the DHT22 sampling rate is 0.5Hz
     }      
  }
}


/*void readSensor() {
  int readData = dht.read(DHTPIN);
  float h = dht.humidity;
  float t = dht.temperature;

  if (isnan(h) || isnan(t)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  //float hic = dht.computeHeatIndex(t, h, false);
 // DHTPIN=digitalWrite(c);
  //BlueToothConnect.write(DHTPIN);
  //BlueToothConnect.print(DHTPIN);
  Serial.print("Humidity: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(t);
  Serial.print(" *C ");
  Serial.print("Heat index: ");
  //Serial.print(hic);
  //Serial.print(" *C ");
}*/


