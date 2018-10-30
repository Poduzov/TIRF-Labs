/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

import com.fazecast.jSerialComm.*;
import java.util.ArrayList;

/**
 * @author Denis Poduzov poduzov@gmail.com
 */
public class Communicator {

    public final int CMD_WRITE_VOLTAGE = 0;
    public final int CMD_WRITE_WATTAGE = 1;
    public final int CMD_WRITE_CURVE = 2;
    public final int CMD_WRITE_SEQUENCE = 3;
    public final int CMD_EXECUTE_SEQ_STEP = 4;
    public final int CMD_NONE = 10;

    public interface CommuncatorMessageHandler {

        public abstract void Message(CommunicatorMessage message);
    }

    public final static int MSG_ERROR = 0;
    public final static int MSG_OK = 1;

    public class CommunicatorMessage {

        public CommunicatorMessage(int messageType, String message) {
            Message = message;
            MessageType = messageType;
        }
        public String Message;
        public int MessageType;
    }

    public int PendingCommand = this.CMD_NONE;

    private final int baudRate = 57600;

    private final Thread mainThread;
    private SerialPort comPort = null;
    private String comPortName = "";

    private String rxBuffer;

    private int CurveIndex = 0;
    private int CurveChannel = 0;
    private int CurveStep;
    private ArrayList<FloatPoint> CurveData;

    private int SeqIndex = 0;
    private ArrayList<SequenceStep> SeqData;

    public CommuncatorMessageHandler MessageHandler = null;

    public Communicator() {
        mainThread = new Thread(new MainRunnable());
    }

    public void Start() {
        mainThread.start();
    }

    public void WriteSequence(ArrayList<SequenceStep> seq) {
        if (comPort == null) {
            Report(new CommunicatorMessage(MSG_ERROR, "Port not available"));
            return;
        }

        PendingCommand = this.CMD_WRITE_SEQUENCE;
        SeqIndex = 0;
        SeqData = seq;

        WriteSeqStep();
    }

    public void ExecuteSeqStep(int index) {
        if (comPort == null) {
            Report(new CommunicatorMessage(MSG_ERROR, "Port not available"));
            return;
        }

        PendingCommand = this.CMD_EXECUTE_SEQ_STEP;
        SeqIndex = index;

        String cmd;
        cmd = "<ES>";
        cmd += String.format("%02d", index);
        cmd = AddCRC(cmd);

        comPort.writeBytes(cmd.getBytes(), cmd.length());
    }

    private void WriteSeqStep() {
        String cmd;
        SequenceStep step = SeqData.get(SeqIndex);

        cmd = "<WS>";
        cmd += String.format("%02d", SeqIndex);
        cmd += String.format("%01d", step.AnalogActions[0].ValueType);
        cmd += String.format("%01d", step.Trigger.Type);
        cmd += String.format("%04d", step.Trigger.TimerValue);

        int ch = 0;
        for (int i = 0; i < Configuration.DO_COUNT; i++) {
            cmd += String.format("%01d", step.DigitalActions[i].ActionType);
        }

        for (int i = 0; i < 8; i++, ch++) {
            if (ch < Configuration.AO_COUNT) {
                if (step.AnalogActions[i].ActionType == AnalogAction.ACTION_NONE) {
                    cmd += "65535";
                } else {
                    cmd += String.format("%05d", Math.round(step.AnalogActions[i].Value * 10.f));
                }
            } else {
                cmd += "65535";
            }
        }

        cmd = AddCRC(cmd);

        comPort.writeBytes(cmd.getBytes(), cmd.length());
    }

    public void WriteCurve(ArrayList<FloatPoint> curve, int channel, int step) {
        if (comPort == null) {
            Report(new CommunicatorMessage(MSG_ERROR, "Port not available"));
            return;
        }

        PendingCommand = this.CMD_WRITE_CURVE;
        CurveData = curve;
        CurveChannel = channel;
        CurveStep = step;
        CurveIndex = 0;

        WriteCurveRow();
    }

    private void Delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception ex) {
            System.out.println("CheckCRC error: " + ex.getMessage());
        }
    }

    private void WriteCurveRow() {
        String cmd;
        cmd = "<WC>";
        cmd += String.format("%01d", CurveChannel);
        cmd += String.format("%04d", CurveStep);
        cmd += String.format("%02d", CurveIndex);
        cmd += String.format("%04d", Math.round(CurveData.get(CurveIndex).y));
        cmd += String.format("%04d", Math.round(CurveData.get(CurveIndex).x));
        cmd = AddCRC(cmd);

        comPort.writeBytes(cmd.getBytes(), cmd.length());
    }

    public void WriteWattage(float values[]) {
        if (comPort == null) {
            Report(new CommunicatorMessage(MSG_ERROR, "Port not available"));
            return;
        }

        try {
            String cmd;

            cmd = "<WW>";
            for (int i = 0; i < values.length; i++) {
                cmd += String.format("%05d", (int) Math.round(values[i] * 10));
            }

            cmd = AddCRC(cmd);
            comPort.writeBytes(cmd.getBytes(), cmd.length());
        } catch (Exception ex) {
            Report(new CommunicatorMessage(MSG_ERROR, "Failed to send command [SetRawValues]"));
            return;
        }

        PendingCommand = this.CMD_WRITE_WATTAGE;
    }

    public void WriteVoltage(int values[]) {
        if (comPort == null) {
            Report(new CommunicatorMessage(MSG_ERROR, "Port not available"));
            return;
        }

        try {
            String cmd;

            cmd = "<WV>";
            for (int i = 0; i < values.length; i++) {
                cmd += String.format("%04X", values[i]);
            }

            cmd = AddCRC(cmd);
            comPort.writeBytes(cmd.getBytes(), cmd.length());
        } catch (Exception ex) {
            Report(new CommunicatorMessage(MSG_ERROR, "Failed to send command [SetRawValues]"));
            return;
        }

        PendingCommand = this.CMD_WRITE_VOLTAGE;
    }

    private String AddCRC(String cmd) {
        int crc;

        crc = 0;
        for (int i = 0; i < cmd.length(); i++) {
            crc = 0xFF & (crc + cmd.charAt(i));
        }

        String result;
        result = cmd + String.format("%02X", crc) + "<END>";

        return result;
    }

    public void SetPortName(String portName) {
        if (comPortName.equals(comPort)) {
            return;
        }

        ClosePort();

        comPortName = portName;
        if (!"".equals(comPortName)) {
            SerialPort ports[] = SerialPort.getCommPorts();
            for (SerialPort port : ports) {
                if (port.getSystemPortName().equals(portName)) {
                    comPort = port;
                    comPort.addDataListener(new RxHandler());
                    OpenPort();
                    break;
                }
            }
        }
    }

    private class RxHandler implements SerialPortDataListener {

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] data = event.getReceivedData();
            for (int i = 0; i < data.length; ++i) {
                rxBuffer += (char) data[i];
            }

            int pos;
            pos = rxBuffer.indexOf("<");
            if (pos > 0) {
                rxBuffer = rxBuffer.substring(pos);
            }

            ProcessResponse();
        }
    }

    private void ProcessResponse() {
        if (rxBuffer.length() < 11) {
            return;
        }

        int p;
        p = rxBuffer.indexOf("<END>");
        if (p < 0) {
            return;
        }

        String cmdString = rxBuffer.substring(0, p + 5);
        rxBuffer = rxBuffer.substring(p + 5).trim();

        if (!CheckCRC(cmdString)) {
            System.out.println("CRC Error: " + cmdString);
            return;
        }

        ResponseType cmd;

        cmd = GetCommand(cmdString);

        switch (cmd) {
            case Ping:
                Report(new CommunicatorMessage(MSG_OK, "Ping OK"));
                break;
            case RxOK:
                switch (PendingCommand) {
                    case CMD_WRITE_VOLTAGE:
                        Report(new CommunicatorMessage(MSG_OK, "Write Voltage OK"));
                        PendingCommand = this.CMD_NONE;
                        break;
                    case CMD_WRITE_WATTAGE:
                        Report(new CommunicatorMessage(MSG_OK, "Write Power OK"));
                        PendingCommand = this.CMD_NONE;
                        break;
                    case CMD_WRITE_CURVE:
                        CurveIndex++;
                        if (CurveIndex == CurveData.size()) {
                            Report(new CommunicatorMessage(MSG_OK, "Write Curve OK"));
                            PendingCommand = CMD_NONE;
                        } else {
                            Report(new CommunicatorMessage(MSG_OK, "Write Curve Row " + (CurveIndex + 1) + " OK"));
                            WriteCurveRow();
                        }
                        break;
                    case CMD_WRITE_SEQUENCE:
                        SeqIndex++;
                        if (SeqIndex == SeqData.size()) {
                            Report(new CommunicatorMessage(MSG_OK, "Write Sequence OK"));
                            PendingCommand = CMD_NONE;
                        } else {
                            Report(new CommunicatorMessage(MSG_OK, "Write Sequence Step " + (SeqIndex + 1) + " OK"));
                            WriteSeqStep();
                        }
                        break;
                    case CMD_EXECUTE_SEQ_STEP:
                        Report(new CommunicatorMessage(MSG_OK, "Execute Sequence Step " + (SeqIndex + 1) + " OK"));
                        WriteSeqStep();
                        break;
                }
                break;
        }

        System.out.println("Command: " + cmd);
    }

    private void Report(CommunicatorMessage message) {
        if (MessageHandler != null) {
            MessageHandler.Message(message);
        }
    }

    private boolean CheckCRC(String cmdString) {
        int crcRx = 0;
        int crcCalc = 0;
        int i;

        try {

            for (i = 0; cmdString.charAt(i + 2) != '<'; i++) {
                crcCalc = 0xFF & (crcCalc + (byte) cmdString.charAt(i));
            }

            crcRx = Integer.decode("0x" + cmdString.substring(i, i + 2));

            return (crcRx == crcCalc);
        } catch (Exception ex) {
            System.out.println("CheckCRC error: " + ex.getMessage());
            return false;
        }
    }

    private ResponseType GetCommand(String cmdString) {
        if (cmdString.charAt(0) != '<' || cmdString.charAt(3) != '>') {
            return ResponseType.Error;
        }

        if ("RX".equals(cmdString.substring(1, 3))) {
            if ("OK".equals(cmdString.substring(4, 6))) {
                return ResponseType.RxOK;
            }

            if ("CR".equals(cmdString.substring(4, 6))) {
                return ResponseType.RxCRC;
            }

            return ResponseType.RxError;
        }

        if ("PN".equals(cmdString.substring(1, 3))) {
            return ResponseType.Ping;
        }

        return ResponseType.Error;
    }

    private void OpenPort() {
        comPort.setComPortParameters(baudRate, 8, 1, 0);
        if (!comPort.openPort()) {
            ClosePort();
        }
        rxBuffer = "";
    }

    private void ClosePort() {

        if (comPort == null) {
            return;
        }

        comPort.closePort();

    }

    private class MainRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                if ("".equals(comPortName) || comPort == null) {
                    continue;
                }

                if (!comPort.isOpen()) {
                    comPort.openPort();
                    continue;
                }

            }
        }

    }
}
