package com.sxj.supervisor.enu.rfid.purchase;

public enum PayStateEnum
{
    UN_PAYED("未付款", 0), PAYED("已付款", 1);
    
    // 成员变量
    private Integer id;
    
    private String name;
    
    private PayStateEnum(String name, Integer id)
    {
        this.name = name;
        this.id = id;
    }
    
    public Integer getId()
    {
        return id;
    }
    
    public void setId(Integer id)
    {
        this.id = id;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
}
