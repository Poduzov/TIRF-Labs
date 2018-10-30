#ifndef CURVES_H
#define CURVES_H

#include "Globals.h"

class Curves {
  public:
    Curves();

    unsigned int TempChannel;
    unsigned int TempIndex;
    unsigned int TempData[2];
    unsigned int TempStep;

    void SaveTemp();
    void LoadStep();
    void LoadCurve();

    unsigned int GetVoltage(unsigned int channel, unsigned int power);
  private:
    unsigned long CurvesStart;
    unsigned long CurveSize;
    unsigned long StepsStart;
};

#endif
