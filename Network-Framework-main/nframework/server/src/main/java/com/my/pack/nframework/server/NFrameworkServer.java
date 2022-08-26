package com.my.pack.nframework.server;
import com.my.pack.nframework.server.annotations.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
public class NFrameworkServer
{
private ServerSocket serverSocket;
private Set<Class> tcpNetworkServiceClasses;
private Map<String,TCPService> services;
public NFrameworkServer()
{
tcpNetworkServiceClasses=new HashSet<>();
services=new HashMap<>();
}//constructor ends

public void registerClass(Class c)
{
Path pathOnType;
Path pathOnMethod;
Method methods[];
String actualPath;
TCPService tcpService=null;

pathOnType=(Path)c.getAnnotation(Path.class);
if(pathOnType==null) return;
methods=c.getMethods();
int methodWithPathAnnotationCount=0;
for(Method method:methods)
{
pathOnMethod=(Path)method.getAnnotation(Path.class);
if(pathOnMethod==null) continue; 
methodWithPathAnnotationCount++;
actualPath=pathOnType.value()+pathOnMethod.value();
tcpService=new TCPService();
tcpService.c=c;
tcpService.method=method;
tcpService.path=actualPath;
services.put(actualPath,tcpService);
}
if(methodWithPathAnnotationCount==0) 
{
tcpNetworkServiceClasses.add(c);
}
}//funtion ends

public TCPService getTCPService(String path)
{
if(services.containsKey(path))
{
return services.get(path);
}
else return null;
}//function ends

public void start()
{
try
{
serverSocket=new ServerSocket(5500);
Socket socket;
RequestProcessor requestProcessor;
while(true)
{
socket=serverSocket.accept();
requestProcessor=new RequestProcessor(this,socket);
}
}catch(Exception e)
{
System.out.println(e);
}
}//function ends

}//class ends