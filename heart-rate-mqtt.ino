#define USE_ARDUINO_INTERRUPTS false
#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <PulseSensorPlayground.h>

// Wifi properties: ssid, password
// ########################################################################
char *wifiSSID = "Wifi-ssid";                      // Wifi SSID
char *wifiPassword = "wifi-password";     // Wifi password
WiFiClient wifiClient;
int wifiStatus = WL_IDLE_STATUS;
IPAddress staticIp(192, 168, 0, 200); // ip address
IPAddress subnet(255, 255, 255, 0);   // subnet mask
IPAddress gateway(192, 168, 0, 254);  // default gateway
IPAddress dns_1(1, 1, 1, 1);          // Domain name service 1
IPAddress dns_2(8, 8, 8, 8);          // Domain name service 2
unsigned long interval_check = 10000; // interval to check connection status
unsigned long currentTime = 0;        // variable to keep current time
// ########################################################################

// MQTT server properties: username, password, client_id, topic
// ########################################################################
char *username = "test";                  // username
char *password = "test";                 // password
char *client_id = "test";               // client-id
char *topic = "test";                   // topic
char MQTTServer[] = "192.168.0.223";   // MQTT server ip address
// ########################################################################

// Variables for MQTT client
// ########################################################################
PubSubClient MQTTClient(wifiClient);
unsigned long lastSend;      // last sent time
unsigned int interval = 10; // How often send data(milli seconds)
int BPM = 0;              // Beat Per Minute
int lastBPM = 1;            // last BPM
String payload;             // JSON Payload
// ########################################################################

// Variables for pulseSensor
// ########################################################################
const int OUTPUT_TYPE = SERIAL_PLOTTER;
const int PULSE_INPUT = A0; // Input
const int PULSE_BLINK = 2;    // Pin 2 is the on-board LED
const int PULSE_FADE = 16;    // Pin 16 is the on-board LED
byte samplesUntilReport;
const int THRESHOLD = 550; // Determine which Signal to "count as a beat", and which to ingore.
const byte SAMPLES_PER_SERIAL_SAMPLE = 10;
PulseSensorPlayground pulseSensor;

// ########################################################################

void setup() {
  Serial.begin(115200);                       // Set's up Serial Communication at certain speed.
  connectToWifi();                           // connect to wifi function
  MQTTClient.setServer(MQTTServer, 1883);       // set connection properties(ip, port)
  lastSend = 0;
  pulseSensorData();
  Serial.println("Connecting to MQTT Server ...");
  if (MQTTClient.connect(client_id, username, password)) {
    Serial.println("[DONE]");
  }
}

void loop() {
  currentTime = millis();
  if (millis() - currentTime >= interval_check) {
    delay(5);
    if (!MQTTClient.connected()) {
      reconnectToServer();

      currentTime = millis();
    }
  }

  readPulseData();
  if (millis() - lastSend > interval) // update and send every 1 second
  { // send data data
    lastSend = millis();
    if (BPM < 100 && BPM > 50) {
      if (!isDuplicateValue(lastBPM, BPM)) {
        makeJsonAndSend(BPM);
      }
    }
  }

  MQTTClient.loop();
}

void pulseSensorData() {
  // Configure the PulseSensor manager.
  pulseSensor.analogInput(PULSE_INPUT);
  pulseSensor.blinkOnPulse(PULSE_BLINK);
  pulseSensor.fadeOnPulse(PULSE_FADE);
  pulseSensor.setThreshold(THRESHOLD);

  // Skip the first SAMPLES_PER_SERIAL_SAMPLE in the loop().
  samplesUntilReport = SAMPLES_PER_SERIAL_SAMPLE;

  // Now that everything is ready, start reading the PulseSensor signal.
  if (!pulseSensor.begin()) {
    for (;;) {
      // Flash the led to show things didn't work.
      Serial.println("STUCK NOTHING WORKED :(");
      digitalWrite(PULSE_BLINK, LOW);
      //      delay(50);
      digitalWrite(PULSE_BLINK, HIGH);
      //      delay(50);
    }
  }
}

void readPulseData() {
  if (pulseSensor.sawNewSample()) {
    if (--samplesUntilReport == (byte) 0) {
      samplesUntilReport = SAMPLES_PER_SERIAL_SAMPLE;
      if (pulseSensor.sawStartOfBeat()) {
        pulseSensor.outputBeat();
      }
    }
    BPM = pulseSensor.getBeatsPerMinute();
  }
}


void connectToWifi() {
  WiFi.disconnect();                      // disconnect from last wifi connection
  WiFi.mode(WIFI_STA);                   // Wifi mode Station
  WiFi.config(staticIp, subnet, gateway, dns_1, dns_2); // IP settings
  WiFi.begin(wifiSSID, wifiPassword);   // connect to wifi

  Serial.println("");
  Serial.print("Connecting to (" + String(wifiSSID) + ") Access point");

  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println("");
  Serial.println("[Connected to WiFi]");
  delay(1000);
}

void reconnectToServer() {
  while (!MQTTClient.connected()) {
    Serial.print("Wifi Status:");
    Serial.println(WiFi.status());

    Serial.println("Reconnecting to MQTT Server ...");

    if (MQTTClient.connect(client_id, username, password)) {
      Serial.println("[DONE]");
      delay(1000);
    }

    else {
      Serial.print("[Failed] [rc= ");
      Serial.print(MQTTClient.state());
      Serial.println(": retrying in 5 second(s)]");
      delay(5000);
    }
  }
}

bool isDuplicateValue(int oldBPM, int newBPM) {
  return (oldBPM == newBPM) ? true : false;
}

void makeJsonAndSend(int BPM) {
  DynamicJsonDocument document(1024);
  lastBPM = BPM;
  document["BPM"] = BPM;
  serializeJson(document, payload);

  // send payload
  MQTTClient.publish(topic, payload.c_str());
  Serial.println(payload);

  payload = "";
}
