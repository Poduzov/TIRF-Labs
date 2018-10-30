#include "Sequencer.h"
#include <EEPROM.h>

Sequencer::Sequencer() {
  SeqStart = EEPROM_START + (CURVE_SIZE * 4 * AO_COUNT) + AO_COUNT * 2;
  SeqStepSize = sizeof(Step);
}

void Sequencer::SaveCount()
{
  EEPROM.put(SeqStart, StepsCount);
}

void Sequencer::LoadCount() {
  EEPROM.get(SeqStart, StepsCount);
}

void Sequencer::SaveTemp() {
  EEPROM.put(SeqStart + (SeqStepSize * TempIndex) + 1, TempStep);
}

void Sequencer::LoadTemp() {
  EEPROM.get(SeqStart + (SeqStepSize * TempIndex) + 1, TempStep);
}
