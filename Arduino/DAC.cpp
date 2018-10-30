#include "DAC.h"
#include <Wire.h>
#include <Arduino.h>

DAC::DAC(uint8_t address) {
  _address = address;
}

void DAC::Init(bool internalReferenceOn, unsigned short LDACMask) {
  //Reference setup
  Wire.beginTransmission(_address);
  Wire.write(0x38);
  Wire.write(0x00);
  Wire.write(internalReferenceOn ? 0x01 : 0x00);
  Wire.endTransmission();
  //LDAC mode setup. 0 in LDACMask = LDAC pin enabled
  Wire.beginTransmission(_address);
  Wire.write(0x18);
  Wire.write(0x00);
  Wire.write(LDACMask & 0x0F);
  Wire.endTransmission();
}

void DAC::Reset(bool mode) {
  //Reference setup
  Wire.beginTransmission(_address);
  Wire.write(0x28);
  Wire.write(0x00);
  Wire.write(mode ? 0x01 : 0x00);
  Wire.endTransmission();
}

void DAC::Write(unsigned short channel, unsigned int value) {
  unsigned int data = value << 4;
  Wire.beginTransmission(_address);
  Wire.write(0x00 | (channel & 0x07));
  Wire.write((0xFF00 & data) >> 8);
  Wire.write(0x00F0 & value);
  Wire.endTransmission();
}

void DAC::WriteUpdate(unsigned short channel, unsigned int value) {
  unsigned int data = value << 4;
  Wire.beginTransmission(_address);
  Wire.write(0x18 | (channel & 0x07));
  Wire.write((0xFF00 & data) >> 8);
  Wire.write(0x00F0 & value);
  Wire.endTransmission();
}
