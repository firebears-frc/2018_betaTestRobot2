#include <Adafruit_NeoPixel.h>
#include <PixelStrip.h>
#include <Animation.h>
#include <Wire.h>
#include "PulseAnimation.h"

const int MAX_ANIMATIONS = 3;
const int MAX_PIXELSTRIPS = 1;
const int BRIGHTNESS = 128;
const int I2C_ADDRESS = 4;

PixelStrip *strip[MAX_PIXELSTRIPS];
PulseAnimation *animation[MAX_ANIMATIONS];
int wireTimeout = 0;

/**
   Change animations when I2C messages are received.
   When a message is received, the built-in LED will flash for a half second.
*/
void setup() {
  Wire.begin(I2C_ADDRESS);
  Wire.onReceive(receiveEvent);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  // set up all Animations
  animation[0] = new PulseAnimation();
  animation[0] = new PulseAnimation();
  animation[1] = new PulseAnimation();
  animation[1]->setColor(0, 0x0000ff);
  animation[1]->setColor(1, 0x888888);
  animation[2] = new PulseAnimation(3);
  animation[2]->setColor(0, 0x880000);
  animation[2]->setColor(1, 0x444444);
  animation[2]->setColor(2, 0x000088);

  // set up all PixelStrips
  for (int s = 0; s < MAX_PIXELSTRIPS; s++) {
    strip[s] = new PixelStrip(8, 7 + s, NEO_GRB);
    strip[s]->setup();
    strip[s]->setAnimation(animation[0]);
    strip[s]->setBrightness(BRIGHTNESS);
  }
  Serial.begin(9600);
}

void loop() {
  for (int s = 0; s < MAX_PIXELSTRIPS; s++) {
    strip[s]->draw();
    strip[s]->show();
  }
  delay(10);
  if (wireTimeout > 0 && millis() > wireTimeout)  {
    digitalWrite(LED_BUILTIN, LOW);
    wireTimeout = 0;
  }
}

/**
   Expects to receive two characters.
   The first character specifies the pixel strip number.
   The second character sets the animation number on that strip.
*/
void receiveEvent(int howMany) {
  while (Wire.available() > 0) {
    int s = Wire.read() - '0';
    int a = Wire.read() - '0';
    if (a >= 0 && a < MAX_ANIMATIONS) {
      strip[s]->setAnimation(animation[a]);
    } else {
      strip[s]->setAnimation(0);
    }
    digitalWrite(LED_BUILTIN, HIGH);
    wireTimeout = millis() + 500;
    Serial.print("receiveEvent(");
    Serial.print(s); Serial.print(",");
    Serial.print(a); Serial.println(")");
  }
}
