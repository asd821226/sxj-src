package com.sxj.supervisor.typehandler;

import org.apache.ibatis.type.EnumOrdinalTypeHandler;

import com.sxj.supervisor.enu.member.MemberTypeEnum;

public class MemberTypeEnumTypeHandler extends
        EnumOrdinalTypeHandler<MemberTypeEnum>
{
    
    public MemberTypeEnumTypeHandler(Class<MemberTypeEnum> type)
    {
        super(type);
    }
    
}
