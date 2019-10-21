package com.konka.iot.baseframe.mqtt.model;

/**
 * 虚拟网关上传状态的命令格式
 * @author zwm
 * @date 2018-12-6
 */
public class VgCommand {
    private Integer index;
    private Integer type;
    private String value;

    public VgCommand() {
    }

    public VgCommand(Integer index, Integer type, String value) {
        this.index = index;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "VgCommand{" +
                "index=" + index +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
