package com.sxj.supervisor.typehandler;

import org.apache.ibatis.type.EnumOrdinalTypeHandler;

import com.sxj.supervisor.enu.rfid.apply.PayStateEnum;

public class PayStateEnumTypeHandler extends
        EnumOrdinalTypeHandler<PayStateEnum>
{
    
    public PayStateEnumTypeHandler(Class<PayStateEnum> type)
    {
        super(type);
    }
    
}
