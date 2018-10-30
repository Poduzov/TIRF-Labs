#ifndef SEQUENCER_H
#define SEQUENCER_H

#include <Arduino.h>
#include "Globals.h"

#define TRIGGER_RISING_1    0
#define TRIGGER_FALLING_1   1
#define TRIGGER_RISING_2    2
#define TRIGGER_FALLING_2   3
#define TRIGGER_USB         4
#define TRIGGER_TIMER       5

#define VALUE_VOLTAGE       0
#define VALUE_WATTAGE       1

#define D_ACTION_NONE       0
#define D_ACTION_OFF        1
#define D_ACTION_ON         2

struct Step {
  unsigned int  AOValue[AO_COUNT];
  // Analog output value
  // 0xFFFF     - no change
  byte          DOValue[DO_COUNT];
  byte          TriggerType;
  unsigned int  TriggerDelay;
  byte          ValueType;
};

class Sequencer
{
  public:
    Sequencer();

    bool  Running = 0;

    byte  StepsCount = 0;

    byte  TempIndex;
    Step  TempStep;

    void SaveCount();
    void LoadCount();
    void SaveTemp();
    void LoadTemp();

  private:
    unsigned long SeqStart;
    unsigned long SeqStepSize;
};

#endif
