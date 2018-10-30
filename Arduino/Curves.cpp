#include "Curves.h"
#include <EEPROM.h>
#include <Arduino.h>

Curves::Curves() {
  CurvesStart = EEPROM_START;
  CurveSize = CURVE_SIZE * 4;
  StepsStart = CurvesStart + CurveSize * AO_COUNT;
}

void Curves::SaveTemp() {
  EEPROM.put(CurvesStart + (CurveSize * TempChannel) + (TempIndex * 4), TempData);
  EEPROM.put(StepsStart + (2 * TempChannel), TempStep);
}

void Curves::LoadCurve() {
  EEPROM.get(CurvesStart + (TempChannel * CurveSize) + (TempIndex * 4), TempData);
}

void Curves::LoadStep() {
  EEPROM.get(StepsStart + (TempChannel * 2), TempStep);
}

unsigned int Curves::GetVoltage(unsigned int channel, unsigned int power)
{
  unsigned int index;
  unsigned int rem;
  unsigned int base;
  unsigned int k;
  unsigned long voltage;
  unsigned int raw;

  TempChannel = channel;
  LoadStep();

  TempIndex = power / TempStep;
  LoadCurve();

  rem = power % TempStep;
  base = TempData[1];
  k = TempData[0];
  voltage = (unsigned long)base + (((unsigned long)rem * (unsigned long)k) / (unsigned long)1000);
  if (voltage > 5000) voltage = 0;
  raw = (unsigned int)((unsigned long)4095 * (voltage) / (unsigned long)5000);
  return raw;
}
