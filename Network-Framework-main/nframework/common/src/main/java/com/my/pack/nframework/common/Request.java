package com.my.pack.nframework.common;
public class Request implements java.io.Serializable
{
private String servicePath;
private Object [] arguments;
public void setServicePath(String servicePath)
{
this.servicePath=servicePath;
}
public String getServicePath()
{
return this.servicePath;
}
public void setArguments(Object ...arguments)
{
this.arguments=arguments;
}
public Object [] getArguments()
{
return arguments;
}  
}//Request class ends 