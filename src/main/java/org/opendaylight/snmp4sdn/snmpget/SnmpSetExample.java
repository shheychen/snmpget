package org.opendaylight.snmp4sdn.snmpget;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpSetExample
{
  private static String  ipAddress  = "192.168.1.253";

  private static String  port    = "161";

  // sysContact OID of MIB RFC 1213; Scalar Object = .iso.org.dod.internet.mgmt.mib-2.system.sysContact.0
  private static String  sysContactOid  = ".1.3.6.1.2.1.1.4.0";  // ends with 0 for scalar object

  private static String  sysContactValue  = "pcpswitc";
  
  private static int    snmpVersion  = SnmpConstants.version2c;

  private static String  community  = "private";

  public static void main(String[] args) throws Exception
  {

    System.out.println("SNMP SET Demo");

    // Create TransportMapping and Listen
    TransportMapping transport = new DefaultUdpTransportMapping();
    transport.listen();

    // Create Target Address object
    CommunityTarget comtarget = new CommunityTarget();
    comtarget.setCommunity(new OctetString(community));
    comtarget.setVersion(snmpVersion);
    comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
    comtarget.setRetries(2);
    comtarget.setTimeout(1000);

    // Create the PDU object
    PDU pdu = new PDU();
    
    // Setting the Oid and Value for sysContact variable
    OID oid = new OID(sysContactOid);
    Variable var = new OctetString(sysContactValue);
    VariableBinding varBind = new VariableBinding(oid,var);
    pdu.add(varBind);
    
    pdu.setType(PDU.SET);
    pdu.setRequestID(new Integer32(1));

    // Create Snmp object for sending data to Agent
    Snmp snmp = new Snmp(transport);

    System.out.println("\nRequest:\n[ Note: Set Request is sent for sysContact oid in RFC 1213 MIB.");
    System.out.println("Set operation will change the sysContact value to " + sysContactValue );
    System.out.println("Once this operation is completed, Querying for sysContact will get the value = " + sysContactValue + " ]");
    
    System.out.println("Request:\nSending Snmp Set Request to Agent...");
    ResponseEvent response = snmp.set(pdu, comtarget);

    // Process Agent Response
    if (response != null)
    {
      System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
      PDU responsePDU = response.getResponse();

      if (responsePDU != null)
      {
        int errorStatus = responsePDU.getErrorStatus();
        int errorIndex = responsePDU.getErrorIndex();
        String errorStatusText = responsePDU.getErrorStatusText();
        System.out.println(responsePDU);
        if (errorStatus == PDU.noError)
        {
          System.out.println("Snmp Set Response = " + responsePDU.getVariableBindings());
        }
        else
        {
          System.out.println("Error: Request Failed");
          System.out.println("Error Status = " + errorStatus);
          System.out.println("Error Index = " + errorIndex);
          System.out.println("Error Status Text = " + errorStatusText);
        }
      }
      else
      {
        System.out.println("Error: Response PDU is null");
      }
    }
    else
    {
      System.out.println("Error: Agent Timeout... ");
    }
    snmp.close();
  }
}
