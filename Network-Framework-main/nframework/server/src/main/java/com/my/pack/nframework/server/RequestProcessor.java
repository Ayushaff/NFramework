package com.my.pack.nframework.server;
import java.net.*;
import com.my.pack.nframework.common.*;
import java.nio.charset.*;
import java.lang.reflect.*;
import java.io.*;

class RequestProcessor extends Thread
{
private NFrameworkServer server;
private Socket socket;
RequestProcessor(NFrameworkServer server,Socket socket)
{
this.server=server;
this.socket=socket;
start();
}//constructor ends

public void run()
{
try
{
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
int bytesToReceive=1024;
byte tmp[]=new byte[1024];
byte header[]=new byte[1024];
int bytesReadCount;
int i,j,k;
i=0;
j=0;

//receiving header

while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}

//getting the request Length from header 

int requestLength=0;
j=1023;
i=1;
while(j>=0)
{
requestLength=requestLength+(header[j]*i);
i=i*10;
j--;
}

//sending ack.

byte ack[]=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();

//receiving request in form of byte array

byte request[]=new byte[requestLength];
bytesToReceive=requestLength;
i=0;
j=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
request[i]=tmp[k]; 
i++;
}
j=j+bytesReadCount;
}

//deSerializing the data from the byte array
//converting byte array in JSON String

String requestJSONString=new String(request,StandardCharsets.UTF_8);
Request requestObject=JSONUtil.fromJSON(requestJSONString,Request.class);

//the request object contains servicePath and arguments
//we want the reference of the TCPService that contains the
//Class reference and method reference

String servicePath=requestObject.getServicePath();
TCPService tcpService;
tcpService=this.server.getTCPService(servicePath);

Response responseObject=new Response();
if(tcpService==null)
{
responseObject.setSuccess(false);
responseObject.setResult("");
responseObject.setException(new RuntimeException("Invalid path : "+servicePath));
}
else
{
Class c=tcpService.c;
Method method=tcpService.method;
try
{
Object serviceObject=c.newInstance();

Object result=method.invoke(serviceObject,requestObject.getArguments());
responseObject.setSuccess(true);
responseObject.setResult(result);
responseObject.setException(null);
}catch(InstantiationException instantiationException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create object to service class associated with the path : "+servicePath));
}catch(IllegalAccessException illegalAccessException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create object to service class associated with the path : "+servicePath));
}catch(InvocationTargetException invocationTargetException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
Throwable t=invocationTargetException.getCause();
responseObject.setException(t);
}
}


//sending response 

String responseJSONString=JSONUtil.toJSON(responseObject);
// converting the response string into byte array(serializing)

byte objectBytes[]=responseJSONString.getBytes(StandardCharsets.UTF_8);
//creating header of response length ("Data Saved")
int responseLength=objectBytes.length;
int x;
i=1023;
x=responseLength;
header=new byte[1024];
while(x>0)
{
header[i]=(byte)(x%10);
x=x/10;
i--;
}
//sending header
os.write(header,0,1024); // from which index , how many 
os.flush();

//getting the ack.
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break; 
}
//sending byte array of response string in chunks of 1024 
int bytesToSend=responseLength;
int chunkSize=1024;
j=0;
while(j<bytesToSend)
{
if((bytesToSend-j)<chunkSize) chunkSize=bytesToSend-j;
os.write(objectBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}
//getting ack.
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break; 
}
socket.close();

}catch(IOException e)
{
System.out.println(e);
}

}//funtion ends

}//class ends