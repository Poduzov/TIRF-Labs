#include <Wire.h>
#include <MsTimer2.h>
#include <TimerOne.h>
#include "DAC.h"
#include "Curves.h"
#include "Sequencer.h"

#define DAC_ADDRESS_1   0x1F
#define DAC_ADDRESS_2   0x1C
#define PIN_LDAC        13
#define PIN_TRIG_I1     3
#define PIN_TRIG_I2     2
#define PIN_TRIG_O1     4
#define PIN_TRIG_O2     5

#define CMD_UNKNOWN       0
#define CMD_WRITE_SEQ     1
#define CMD_READ_SEQ      2
#define CMD_EXEC_STEP     3
#define CMD_WRITE_VOLTAGE 4
#define CMD_WRITE_CURVE   5
#define CMD_WRITE_WATTAGE 6

DAC dac1 = DAC(DAC_ADDRESS_1);
DAC dac2 = DAC(DAC_ADDRESS_2);

Sequencer seq = Sequencer();
Curves curves = Curves();

byte          rxBuf[BUFFER_SIZE];
char          txBuf[BUFFER_SIZE];
int           rxPos = -1;
unsigned int  rxTimer = 0;

int           startTimer = -1;
int           pingTimer = 0;

bool          blink;

void setup() {
  pinMode(PIN_LDAC, OUTPUT);
  pinMode(PIN_TRIG_I1, INPUT_PULLUP);
  pinMode(PIN_TRIG_I2, INPUT_PULLUP);
  pinMode(PIN_TRIG_O1, OUTPUT);
  pinMode(PIN_TRIG_O2, OUTPUT);

  attachInterrupt(digitalPinToInterrupt(PIN_TRIG_I1), Trig1, CHANGE);
  attachInterrupt(digitalPinToInterrupt(PIN_TRIG_I2), Trig2, CHANGE);

  MsTimer2::set(100, CyclicInt);
  MsTimer2::start();

  Timer1.initialize(10000000);
  Timer1.attachInterrupt(DelayInt);

  digitalWrite(PIN_LDAC, HIGH);
  Serial.begin(57600);
  Wire.begin();

  dac1.Init(true, 0x00);
  dac2.Init(true, 0x00);
  delay(100);
  AOWrite(0, 0);
  AOWrite(1, 0);
  AOWrite(2, 0);
  AOWrite(3, 0);

  AOWrite(4, 0);
  AOWrite(5, 0);
  AOWrite(6, 0);
  AOWrite(7, 0);

  LDAC();

  DOWrite(0, false);
  DOWrite(1, false);

  Serial.println("Start!");

  rxPos = 0;
  blink = 0;

  for (int i = 0; i < DO_COUNT; i++)
  {
    DOWrite(i, false);
  }

  seq.LoadCount();
  seq.TempIndex = 0;
  StartSeqStep();

  interrupts();
}

void loop() {
  byte b;

  while (Serial.available() > 0)
  {
    b = Serial.read();

    if (rxPos < BUFFER_SIZE)
      rxPos++;
    rxBuf[rxPos] = b;
    if (rxBuf[0] != '<')
      rxPos = -1;

    rxTimer = 0;
    ProcessCommand();
  }
}

void Blink() {
  digitalWrite(PIN_TRIG_O1, blink ? HIGH : LOW);
  digitalWrite(PIN_TRIG_O2, blink ? LOW : HIGH);
  blink = !blink;
}

void AOWrite(byte channel, unsigned int value)
{
  switch (channel)
  {
    case 0:
      dac1.Write(1, value);
      break;
    case 1:
      dac1.Write(3, value);
      break;
    case 2:
      dac1.Write(0, value);
      break;
    case 3:
      dac1.Write(2, value);
      break;
    case 4:
      dac2.Write(3, value);
      break;
    case 5:
      dac2.Write(1, value);
      break;
    case 6:
      dac2.Write(0, value);
      break;
    case 7:
      dac2.Write(2, value);
      break;
  }
}

void ProcessCommand()
{
  unsigned int cmd;
  unsigned int len;
  if (rxPos < 10)
    return;

  if (!(rxBuf[rxPos - 4] == '<' && rxBuf[rxPos - 3] == 'E' && rxBuf[rxPos - 2] == 'N' && rxBuf[rxPos - 1] == 'D' && rxBuf[rxPos] == '>'))
    return;

  cmd = GetCommandType();
  if (CheckSumValidate()) {
    rxPos = 0;
    switch (cmd) {
      case CMD_WRITE_VOLTAGE:
        pingTimer = 0;
        ParseWriteVoltage();
        break;
      case CMD_WRITE_WATTAGE:
        Blink();
        ParseWriteWattage();
        break;
      case CMD_WRITE_CURVE:
        pingTimer = 0;
        ParseWriteCurve();
        break;
      case CMD_WRITE_SEQ:
        startTimer = 0;
        pingTimer = 0;
        ParseWriteSeq();
        break;
    }

    rxPos = -1;
    sprintf(txBuf, "<RX>OK00<END>");
    AddCheckSum(6);
    Serial.println(txBuf);
  }
}

void ParseWriteSeq() {
  unsigned int i, p;

  seq.TempIndex = DecToInt(4, 2);
  seq.TempStep.ValueType = DecToInt(6, 1);
  seq.TempStep.TriggerType = DecToInt(7, 1);
  seq.TempStep.TriggerDelay = DecToInt(8, 4);
  seq.TempStep.DOValue[0] = DecToInt(12, 1);
  seq.TempStep.DOValue[1] = DecToInt(13, 1);

  for (i = 0, p = 14; i < AO_COUNT; i++, p += 5)
  {
    seq.TempStep.AOValue[i] = DecToInt(p, 5);
  }

  seq.StepsCount = seq.TempIndex + 1;
  seq.SaveCount();
  seq.SaveTemp();
  seq.TempIndex = 0;
  seq.Running = 0;

  startTimer = 0;
}

void ParseWriteWattage()
{
  unsigned int power[AO_COUNT];
  unsigned int i, p;

  for (i = 0, p = 4; i < AO_COUNT; i++, p += 5)
  {
    power[i] = DecToInt(p, 5);
  }

  unsigned int voltage;
  for (i = 0; i < AO_COUNT; i++)
  {
    voltage = curves.GetVoltage(i, power[i]);
    AOWrite(i, voltage);
  }

  LDAC();
}

void LDAC() {
  digitalWrite(PIN_LDAC, LOW);
  delay(1);
  digitalWrite(PIN_LDAC, HIGH);
}

void ParseWriteVoltage() {
  unsigned int voltage;
  for (int i = 0; i < AO_COUNT; i++)
  {
    voltage = HexToWord(i * 4 + 4);
    if (voltage != 0xFFFF)
      AOWrite(i, voltage);
  }

  LDAC();
}

void ParseWriteCurve() {
  unsigned int ch;
  unsigned int step;
  unsigned int index;
  unsigned int voltage;
  unsigned int k;

  ch = DecToInt(4, 1);
  step = DecToInt(5, 4);
  index = DecToInt(9, 2);
  voltage = DecToInt(11, 4);
  k = DecToInt(15, 4);

  curves.TempChannel = ch;
  curves.TempStep = step;
  curves.TempIndex = index;
  curves.TempData[0] = voltage;
  curves.TempData[1] = k;

  curves.SaveTemp();
}

unsigned int DecToInt(int pos, int places)
{
  unsigned int result = 0;
  for (int i = 0; i < places; i++)
  {
    result += (rxBuf[pos + i] - 0x30);

    if (i < places - 1)
      result = result * 10;
  }

  return result;
}

unsigned int HexToWord(unsigned int pos)
{
  unsigned int res;

  res = 0;
  res += Quadruple(rxBuf[pos + 0]) << 4;
  res += Quadruple(rxBuf[pos + 1]);
  res = res << 8;
  res += Quadruple(rxBuf[pos + 2]) << 4;
  res += Quadruple(rxBuf[pos + 3]);

  return res;
}

void AddCheckSum(int length) {
  byte cs = 0x00;
  byte q;
  char c;

  for (int i = 0; i < length; i++)
    cs = 0xFF & (cs + (byte)txBuf[i]);

  q = (cs & 0xF0) >> 4;
  txBuf[length] = QuadrupleToHex(q);
  q = cs & 0x0F;
  txBuf[length + 1] = QuadrupleToHex(q);
}

char QuadrupleToHex(byte q) {
  char c;
  if (q < 10)
    c = 48 + q;
  else
    c = 55 + q;
  return c;
}

bool CheckSumValidate() {
  int length = rxPos - 6;
  byte csCalculated = 0x00;
  byte csReceived = 0x00;
  char c;

  for (int i = 0; i < length; i++)
    csCalculated = (byte)(0xFF & (csCalculated + rxBuf[i]));

  csReceived += Quadruple(rxBuf[length]) << 4;
  csReceived += Quadruple(rxBuf[length + 1]);
  return csReceived == csCalculated;
}

byte Quadruple(byte c) {
  byte b;

  if (c < 58)
    b = c - 48;
  else
    b = c - 55;

  return b;
}

unsigned int GetCommandType() {
  if (rxBuf[1] == 'W' && rxBuf[2] == 'S') return CMD_WRITE_SEQ;
  if (rxBuf[1] == 'R' && rxBuf[2] == 'S') return CMD_READ_SEQ;
  if (rxBuf[1] == 'E' && rxBuf[2] == 'S') return CMD_EXEC_STEP;
  if (rxBuf[1] == 'W' && rxBuf[2] == 'V') return CMD_WRITE_VOLTAGE;
  if (rxBuf[1] == 'W' && rxBuf[2] == 'W') return CMD_WRITE_WATTAGE;
  if (rxBuf[1] == 'W' && rxBuf[2] == 'C') return CMD_WRITE_CURVE;

  return CMD_UNKNOWN;
}

void DelayInt() {
  noInterrupts();
  Timer1.stop();
  interrupts();

  if (seq.Running)
  {
    ExecuteSeqStep();
  }
}

void ExecuteSeqStep() {
  unsigned int raw;
  bool ldac = false;

  for (int i = 0; i < AO_COUNT; i++)
  {
    if (seq.TempStep.AOValue[i] != 0xFFFF)
    {
      if (seq.TempStep.ValueType == VALUE_VOLTAGE) {
        raw = 0x0FFF & ((long)seq.TempStep.AOValue[i] * 4095l / 50l);
        AOWrite(i, raw);
        ldac = true;
      }

      if (seq.TempStep.ValueType == VALUE_WATTAGE) {

      }
    }
  }

  if (ldac)
    LDAC();

  for (int i = 0; i < DO_COUNT; i++)
  {
    if (seq.TempStep.DOValue[i]  == D_ACTION_NONE) continue;

    if (seq.TempStep.DOValue[i]  == D_ACTION_OFF)
    {
      DOWrite(i, false);
      continue;
    }

    if (seq.TempStep.DOValue[i]  ==  D_ACTION_ON)
    {
      DOWrite(i, true);
      continue;
    }
  }

  if (seq.TempIndex < seq.StepsCount - 1)
  {
    seq.TempIndex++;
  }
  else
  {
    seq.TempIndex = 0;
  }

  StartSeqStep();
}

void DOWrite(byte ch, bool value)
{
  byte pin;

  if (ch == 0)
  {
    pin = PIN_TRIG_O1;
  }
  else if (ch == 1)
  {
    pin = PIN_TRIG_O2;
  }
  else {
    return;
  }

  digitalWrite(pin, value ? HIGH : LOW);
  return;
}

void StartSeqStep() {
  seq.LoadTemp();

  if (seq.TempStep.TriggerType == TRIGGER_TIMER) {
    long timer;

    timer = seq.TempStep.TriggerDelay * 100000l;
    noInterrupts();
    Timer1.stop();
    Timer1.setPeriod(timer);
    Timer1.resume();
    interrupts();
  }
  else {
    noInterrupts();
    Timer1.stop();
    interrupts();
  }

  seq.Running = true;
}

void CyclicInt()
{
  if (startTimer < 10 && startTimer >= 0)
  {
    startTimer++;
  }

  if (startTimer >= 10) {
    StartSeqStep();
    startTimer = -1;
  }

  if (rxTimer < 10)
    rxTimer++;
  else
  {
    rxPos = -1;
    rxTimer = 0;
  }

  if (pingTimer < 50)
    pingTimer++;
  else
  {
    Serial.println("<PN>18<END>");
    pingTimer = 0;
  }
}

void Trig1() {
  Serial.println("T1");

  if (!seq.Running) return;

  bool state;
  state = digitalRead(PIN_TRIG_I1);

  if ((seq.TempStep.TriggerType == TRIGGER_RISING_1 && state == HIGH) || (seq.TempStep.TriggerType == TRIGGER_FALLING_1 && state == LOW))
  {
    ExecuteSeqStep();
  }
}

void Trig2() {
  Serial.println("T2");
  if (!seq.Running) return;

  bool state;
  state = digitalRead(PIN_TRIG_I2);

  if ((seq.TempStep.TriggerType == TRIGGER_RISING_2 && state == HIGH) || (seq.TempStep.TriggerType == TRIGGER_FALLING_2 && state == LOW))
  {
    ExecuteSeqStep();
  }
}
