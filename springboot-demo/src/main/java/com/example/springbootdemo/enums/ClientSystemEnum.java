package com.example.springbootdemo.enums;

import com.example.springbootdemo.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ClientSystemEnum {

    PC(1, "PC端"),
    APP(2, "APP"),
    CHILDREN(3, "儿童OSA"),
    ADULT(4, "成人初筛系统"),
    OSA_PATIENT(5, "OSA患者登录系统"),
    DATA_SCREEN(6, "数据大屏Web端"),
    THIRD_PARTY_API(7, "第三方接口"),
    WECHAT(8, "微信公众号"),
    SLEEP_PATIENT_H5(9, "H5/问卷扫码"),
    EQUIPMENT_OPERATION_PLATFORM(10, "设备运营平台");

    private final int type;
    private final String description;

    public static int requireValid(Integer type) {
        if (type == null) {
            throw new BaseException(BaseStatusCodeEnum.VALIDATION_ERROR, "system 请求头不能为空");
        }
        return Arrays.stream(values())
                .filter(item -> item.type == type)
                .findFirst()
                .map(ClientSystemEnum::getType)
                .orElseThrow(() -> new BaseException(
                        BaseStatusCodeEnum.VALIDATION_ERROR,
                        "system 请求头取值必须为 1 到 10"));
    }
}
