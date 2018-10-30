#ifndef DAC_H
#define DAC_H

#include <Wire.h>

class DAC {
  public:
    DAC(uint8_t address);
    void Init(bool internalReferenceOn, unsigned short LDACMask);   // LDACMask:  0 = LDAC pin enabled
    void Reset(bool mode);                                          // mode: 0 = Softwre reset, 1 = Power-On Reset
    void Write(unsigned short channel, unsigned int value);         // channel:   0 = DAC A, 1 = DAC B, 2 = DAC C, 3 = DAC D, 7 = All DACs 
    void WriteUpdate(unsigned short channel, unsigned int value);   // channel:   0 = DAC A, 1 = DAC B, 2 = DAC C, 3 = DAC D, 7 = All DACs 
  private:
    uint8_t _address;
};

#endif
