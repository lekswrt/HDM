/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bluu.hdm.cwmp.message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

/**
 *
 * @author Gonzalo Torres
 */
public class Inform extends Message {


    public String mac;
    private String cpu;
    private String ram;
    private String rom;
    private String modelName;
    private String oui;
    public String sn;
    public String ProductClass;
    public String Manufacturer;
    public int RetryCount;
    public String CurrentTime;
    public Map<String, String> params;
    private Set<Entry<String, String>> events;
    private UpdateFirmwareStatus updateFirmwareStatus;
    public int MaxEnvelopes;
    public String defns;
    private String root = null;

    public static final String EVENT_BOOT_STRAP = "0 BOOTSTRAP";
    public static final String EVENT_BOOT = "1 BOOT";
    public static final String EVENT_PERIODIC = "2 PERIODIC";
    public static final String EVENT_SCHEDULED = "3 SCHEDULED";
    public static final String EVENT_VALUE_CHANGE = "4 VALUE CHANGE";
    public static final String EVENT_KICKED = "5 KICKED";
    public static final String EVENT_CONNECTION_REQUEST = "6 CONNECTION REQUEST";
    public static final String EVENT_TRANSFER_COMPLETE = "7 TRANSFER COMPLETE";
    
    public class Event implements Entry<String, String> {

        private String event;
        private String cmdKey;

        public Event(String event, String cmdKey) {
            this.event = event;
            this.cmdKey = cmdKey;
        }

        public String getKey() {
            return event;
        }

        public String getValue() {
            return cmdKey;
        }

        public String setValue(String value) {
            return cmdKey = value;
        }
    }

    public class UpdateFirmwareStatus {

        private String errorCode;
        private String message;

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("[UpdateFirmwareStatus: ErrorCode=%s, Message=%s]", errorCode, message);
        }
    }

    /**
     * Creates a new instance of Inform
     */
    public Inform() {
    }

    @Override
    protected void createBody(SOAPBodyElement body, SOAPFactory spf) throws SOAPException {
    }

    @Override
    protected void parseBody(SOAPBodyElement body, SOAPFactory spf) throws SOAPException {
        SOAPElement deviceid = getRequestChildElement(spf, body, "DeviceId");
        defns = deviceid.getNamespaceURI();

        oui = getRequestElement(spf, deviceid, "OUI");
        sn = getRequestElement(spf, deviceid, "SerialNumber");
        Manufacturer = getRequestElement(spf, deviceid, "Manufacturer");
        ProductClass = getRequestElement(spf, deviceid, "ProductClass");
        mac = getRequestElement(spf, deviceid, "MACAddress");
        if (ProductClass == null) {
            ProductClass = "N/A";
        }
        MaxEnvelopes = Integer.parseInt(getRequestElement(spf, body, "MaxEnvelopes"));
        RetryCount = Integer.parseInt(getRequestElement(spf, body, "RetryCount"));
        CurrentTime = getRequestElement(spf, body, "CurrentTime");

        Iterator pi = getRequestChildElement(spf, body, "ParameterList").getChildElements(spf.createName("ParameterValueStruct"));
        /*
         //System.out.println ("pi.hasNext: "+pi.hasNext());
         Iterator pii = getRequestChildElement (spf, body, "ParameterList").getChildElements(new QName("ParameterValueStruct"));
         //System.out.println ("pii.hasNext: "+pii.hasNext());
         pii = getRequestChildElement (spf, body, "ParameterList").getChildElements(spf.createName ("ParameterValueStruct","cwmp",defns));
         System.out.println ("pii.hasNext: "+pii.hasNext());
         pii = getRequestChildElement (spf, body, "ParameterList").getChildElements(spf.createName ("ParameterValueStruct","cwmp",URN_CWMP));
         System.out.println ("pii.hasNext: "+pii.hasNext());
         */
        Name nameKey = spf.createName("Name");
        Name nameValue = spf.createName("Value");
        params = new HashMap<>();
        while (pi.hasNext()) {
            SOAPElement param = (SOAPElement) pi.next();
            String key = getRequestElement(param, nameKey);
            if (root == null && !key.startsWith(".")) {
                if (key.startsWith("Device.")) {
                    root = "Device";
                } else if (key.startsWith("InternetGatewayDevice.")) {
                    root = "InternetGatewayDevice";
                } else {
                    throw new RuntimeException("Invalid root. Must be InternetGatewayDevice or Device: " + key);
                }
            }
            String value = "";
            try {
                value = getRequestElement(param, nameValue);
            } catch (SOAPException e) {
            }
            if (value == null) {
                value = "";
            }
            params.put(key, value);
        }

        if (root == null) {
            throw new RuntimeException("Invalid root. Must be InternetGatewayDevice or Device");
        }

        pi = getRequestChildElement(spf, body, "Event").getChildElements(spf.createName("EventStruct"));
        Name eventCode = spf.createName("EventCode");
        Name commandKey = spf.createName(COMMAND_KEY);
        events = new LinkedHashSet<>();
        while (pi.hasNext()) {
            SOAPElement param = (SOAPElement) pi.next();
            String event = getRequestElement(param, eventCode);
            String cmdKey = getRequestElement(param, commandKey);
            System.out.println("EVENT: " + event + "[" + cmdKey + "]");
            if (cmdKey == null) {
                cmdKey = "";
            } else if ("Update Firmware Status".equalsIgnoreCase(cmdKey)) {
                SOAPElement updateStatus = getRequestChildElement(spf, body, "UpdateFirmwareStatus");
                String errorCode = getRequestElement(spf, updateStatus, "ErrorCode");
                String message = getRequestElement(spf, updateStatus, "Message");

                updateFirmwareStatus = new UpdateFirmwareStatus();
                updateFirmwareStatus.setErrorCode(errorCode);
                updateFirmwareStatus.setMessage(message);
            }
            events.add(new Event(event, cmdKey));
        }

    }

    public String getSoftwareVersion() {
        String v = params.get(root + ".DeviceInfo.SoftwareVersion");
        if (v != null) {
            v = v.replace('-', '.');
            v = v.replace(',', ' ');
        }
        return v;
    }

    public String getHardwareVersion() {
        return params.get(root + ".DeviceInfo.HardwareVersion");
    }

    public String getConfigVersion() {
        return params.get(root + ".DeviceInfo.VendorConfigFile.1.Version");
    }

    public String getConnectionRequestURL() {
        return params.get(root + ".ManagementServer.ConnectionRequestURL");
    }

    //longdq
    public String getIPAddress() {
        return params.get(root + ".MyTV.IPAddress");
    }

    public String getStbUsername() {
        return params.get(root + ".MyTV.Username");
    }

    public String getStbPassword() {
        return params.get(root + ".MyTV.Password");
    }

    public String getUpgradeUrl() {
        return params.get(root + ".MyTV.UpgradeURL");
    }

    public String getHomepageUrl() {
        return params.get(root + ".MyTV.URL");
    }

    public String getURL() {
        String url = params.get(root + ".ManagementServer.ConnectionRequestURL");
        if (url != null) {
            return url;
        }
        url = params.get(root + ".ManagementServer.UDPConnectionRequestAddress");
        if (url != null) {
            url = (url.indexOf(':') == -1) ? "udp://" + url + ":80" : "udp://" + url;
        }
        return url;
    }

    public String getConreqUser() {
        return params.get(root + ".ManagementServer.ConnectionRequestUsername");
    }

    public String getConreqPass() {
        return params.get(root + ".ManagementServer.ConnectionRequestPassword");
    }

    public String getProvisiongCode() {
        return params.get(root + ".DeviceInfo.ProvisioningCode");
    }

    public void setProvisiongCode(String code) {
        params.put(root + ".DeviceInfo.ProvisioningCode", code);
    }

    public String getRoot() {
        return root;
    }

    public boolean isEvent(String event) {
        for (Entry<String, String> e : events) {
            if (e.getKey().equals(event)) {
                return true;
            }
        }
        return false;
    }

    public Set<Entry<String, String>> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(1024);
        s.append("Inform:\n");
        println(s, "\toui: ", oui);
        println(s, "\tsn: ", sn);
        println(s, "\tManufacturer: ", Manufacturer);

        s.append("\tEvents:\n");
        for (Entry<String, String> ev : events) {
            println(s, "\t\t", ev.getKey(), ev.getValue());
        }

        s.append("\tParams:\n");
        for (String k : params.keySet()) {
            println(s, "\t\t", k, params.get(k));
        }
        return s.toString();
    }

    public String getOui() {
        return oui;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public UpdateFirmwareStatus getUpdateFirmwareStatus() {
        return updateFirmwareStatus;
    }

   

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getRom() {
        return rom;
    }

    public void setRom(String rom) {
        this.rom = rom;
    }
}