/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

/**
 *
 * @author user
 */
public class SequenceStepActionParam {
    public static final int EVENT_ADD = 0;
    public static final int EVENT_DELETE = 1;
    public static final int EVENT_EDIT_TRIGGER = 2;
    public static final int EVENT_EDIT_ANALOG_ACTION = 3;
    public static final int EVENT_EDIT_DIGITAL_ACTION = 4;
    public static final int EVENT_START = 5;
    
    public int EventType;
    public int ActionIndex;
}
