package com.dawn.inventory;

public enum EquipmentSlot {
    HEAD,
    CHEST,
    PANTS,
    BOOTS,
    ACCESSORY_1,
    ACCESSORY_2,
    ACCESSORY_3,
    ACCESSORY_4;

    public boolean isAccessory() {
        return ordinal() >= ACCESSORY_1.ordinal();
    }

    public static EquipmentSlot[] wearOrder() {
        return new EquipmentSlot[] {HEAD, CHEST, PANTS, BOOTS};
    }

    public static EquipmentSlot[] accessoryOrder() {
        return new EquipmentSlot[] {ACCESSORY_1, ACCESSORY_2, ACCESSORY_3, ACCESSORY_4};
    }
}
